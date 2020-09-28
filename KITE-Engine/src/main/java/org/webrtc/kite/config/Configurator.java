/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.config;

import io.cosmosoftware.kite.exception.BadEntityException;
import io.cosmosoftware.kite.instrumentation.NetworkInstrumentation;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.CircularLinkedList;
import io.cosmosoftware.kite.usrmgmt.EmailSender;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.paas.Paas;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;
import org.webrtc.kite.exception.KiteInsufficientValueException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.*;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.readJsonFile;
import static org.webrtc.kite.Utils.getStackTrace;
import static org.webrtc.kite.Utils.throwNoKeyOrBadValueException;

/**
 * Representation of the config file as a singleton.
 * <p>
 * {
 * "name": "config_name",
 * "interval": "HOURLY|DAILY|WEEKLY",
 * "callback": "http://localhost:8080/kiteweb/datacenter",
 * "grids": [],
 * "tests": [],
 * "browsers": []
 * }
 */
public class Configurator {

  private static final KiteLogger logger = KiteLogger.getLogger(Configurator.class.getName());

  private String commandName;
  private String configFilePath;
  private String reportPath;
  private ConfigHandler configHandler;
  private CircularLinkedList<Paas> gridList = new CircularLinkedList<>();
  private JsonObject jsonConfigObject;
  private String name;
  private boolean skipSame = false;
  private List<JsonObject> testObjectList;
  private List<List<Integer>> matrix = new ArrayList<>();
  private long timeStamp = System.currentTimeMillis();

  public Configurator() {
  }

  /**
   * Builds itself based on the content of the config file.
   *
   * @throws IOException                    the io exception
   * @throws KiteInsufficientValueException if the number of grids, tests and browsers is less than 1.
   * @throws BadEntityException             the bad entity exception
   */
  public void buildConfig()
      throws IOException, KiteInsufficientValueException, BadEntityException {
    this.jsonConfigObject = readJsonFile(configFilePath);

    this.name = jsonConfigObject.getString("name", "")  + " [" + timestamp() + "]" ;
    this.name = name.contains("%ts") ? name.replaceAll("%ts", "") + " (" + timestamp() + ")" : name;
    this.reportPath = jsonConfigObject.getString("reportFolder", null);

    // for CI testing purpose, the report path will be the default one: pwd/kite-allure-reports
    if (System.getProperty("kite.custom.config") != null) {
      reportPath = "";
    }

    String callbackURL = jsonConfigObject.getString("callback", null);

    JsonArray jsonArray = jsonConfigObject.getJsonArray("matrix");
    if (jsonArray != null) {
      for (int i = 0; i < jsonArray.size(); i++) {
        JsonArray jArray = jsonArray.getJsonArray(i);
        List<Integer> tuple = new ArrayList<>();
        for (int j = 0; j < jArray.size(); j++) {
          tuple.add(jArray.getInt(j));
        }
        this.matrix.add(tuple);
      }
    }

    this.testObjectList = (List<JsonObject>)
        throwNoKeyOrBadValueException(jsonConfigObject, "tests", JsonArray.class, false);

    if (testObjectList.size() < 1) {
      throw new KiteInsufficientValueException("Test objects are less than one.");
    }

    List<JsonObject> clientList = new ArrayList<>();

    // for testing with custom client (1 only)
    if (System.getProperty("kite.custom.config") != null) {
      JsonObject customBrowser = readJsonFile(System.getProperty("kite.custom.config"));
      logger.info("Running test on custom browser config: \n" + customBrowser.toString());
      clientList.add(customBrowser);
    } else {
      clientList = (List<JsonObject>) throwNoKeyOrBadValueException(jsonConfigObject, "clients", JsonArray.class, false);

      int size = (clientList != null ? clientList.size() : 0);

      if (size < 1) {
        throw new KiteInsufficientValueException("Less than one browser or app object.");
      }

      if (clientList != null) {
        clientList = new ArrayList<>(new LinkedHashSet<>(clientList));
        if (clientList.size() != size) {
          logger.warn("Duplicate browser configurations in the config file have been removed.");
        }
      }
    }

    if (jsonConfigObject.get("grids") == null) {
      throw new KiteInsufficientValueException("There need to be at least one grid for this version of KITE Engine to work, please check!");
    }

    JsonArray gridArray = jsonConfigObject.getJsonArray("grids");
    if (gridArray.isEmpty()) {
    }

    for (int index = 0; index < gridArray.size(); index++) {
      gridList.add(new Paas(gridArray.getJsonObject(index)));
    }
    configHandler =
      new ConfigHandler(testObjectList, clientList);

    if (jsonConfigObject.containsKey("networkInstrumentation")) {
      try {
        NetworkInstrumentation networkInstrumentation =
            new NetworkInstrumentation(
                jsonConfigObject.getJsonObject("networkInstrumentation"), this.getRemoteAddress());

        for (TestConfig testConfig: configHandler.getTestList()) {
          testConfig.setNetworkInstrumentation(networkInstrumentation);
        }

      } catch (Exception e){
        logger.error(getStackTrace(e));
      }
    }
    
    if (jsonConfigObject.containsKey("email")) {
      try {
        EmailSender sendEmailSMTP = new EmailSender(jsonConfigObject.get("email"));
        logger.debug(sendEmailSMTP.toString());
        for (TestConfig testConfig: configHandler.getTestList()) {
          testConfig.setEmailSender(sendEmailSMTP);
        }
      } catch (Exception e){
        logger.error(getStackTrace(e));
      }
    }
    
    skipSame = jsonConfigObject.getBoolean("skipSame", skipSame);
    logger.info("Finished reading the configuration file");
  }

  /**
   * Gets the remoteAddress, which is the first address in the "remotes"
   *
   * @return the RemoteObjectList
   */
  public String getRemoteAddress() {
    if (gridList != null && gridList.size() > 0) {
      return gridList.get(0).getUrl().split("/")[2].split(":")[0];
    }
    return null;
  }

  /**
   * Creates a matrix of browser tuples.
   *
   * @param tupleSize  tuple size
   * @param permute    the permute
   * @param regression the regression
   *
   * @return a matrix of browser tuples as List<Tuple>
   */
  public List<Tuple> buildTuples(int tupleSize, boolean permute, boolean regression) {
    List<Tuple> listOfTuples = new ArrayList<>();
    if (regression) {
      // only add 1 placeholder tuple
      listOfTuples.add(new Tuple());
    } else {

      List<Client> clientList = this.configHandler.getClientList();

      List<Client> focusedList = new ArrayList<>();
      for (Client client : clientList) {
        if (! client.isExclude()) {
          focusedList.add(client);
        }
      }

      listOfTuples = recursivelyBuildTuples(tupleSize, 0, clientList, (new Tuple()).getClients(), permute);

//      List<Tuple> tempListOfTuples = new ArrayList<>(listOfTuples);
//      for (Tuple tuple : tempListOfTuples) {
//        if (Collections.disjoint(tuple.getClients(), focusedList)
//            || (skipSame && tuple.getClients().stream().distinct().limit(2).count() <= 1)) {
//          listOfTuples.remove(tuple);
//        }
//      }
      for (Tuple t :listOfTuples) {
        logger.debug("Tuple = " + t.getId());
      }      
    }
    return listOfTuples;
  }

  /**
   * Gets the command name for the NW instrumentation.
   *
   * @return the name of the command
   */
  public String getCommandName() {
    return this.commandName;
  }

  /**
   * Sets the command name for the NW instrumentation.
   *
   * @param commandName name of the command
   */
  public void setCommandName(String commandName) {
    this.commandName = commandName;
  }

  /**
   * Gets config file path.
   *
   * @return the config file path
   */
  public String getConfigFilePath() {
    return configFilePath;
  }

  /**
   * Sets config file path.
   *
   * @param configFilePath the config file path
   */
  public void setConfigFilePath(String configFilePath) {
    this.configFilePath = configFilePath;
  }

  /**
   * Gets config handler.
   *
   * @return the config handler
   */
  public ConfigHandler getConfigHandler() {
    return this.configHandler;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the RemoteObjectList
   *
   * @return the RemoteObjectList
   */
  public CircularLinkedList<Paas> getRemoteList() {
    return gridList;
  }

  /**
   * Gets time stamp.
   *
   * @return the time stamp
   */
  public long getTimeStamp() {
    return this.timeStamp;
  }

  /**
   * Build a list of tuple from given endpoints list, permutation or combination
   *
   * @param targetSize   targeted tuple size
   * @param currentIndex index of the endpoint in the list
   * @param fullList     list of provided endpoints
   * @param refList      list of endpoint currently being built
   * @param permutation  true if this is permutation, false if combination
   *
   * @return a list of tuples of endpoints.
   */
  public static List<Tuple> recursivelyBuildTuples(int targetSize, int currentIndex, List<Client> fullList,
      List<Client> refList, boolean permutation) {
    List<Tuple> result = new ArrayList<>();
    if (currentIndex < targetSize - 1) {
      for (int index = 0; index < fullList.size(); index++) {
        List<Client> temp = new ArrayList<>(refList);
        temp.add(fullList.get(index));
        result.addAll(recursivelyBuildTuples(targetSize, currentIndex + 1,
            fullList.subList(permutation ? 0 : index, fullList.size()), temp, permutation));
      }
    } else {
      for (Client client : fullList) {
        List<Client> temp = new ArrayList<>(refList);
        temp.add(client);
        result.add(new Tuple(temp));
      }
    }
    return result;
  }

  /**
   * Sets time stamp.
   */
  public void setTimeStamp() {
    this.timeStamp = System.currentTimeMillis();
  }

  /**
   * Gets report path.
   *
   * @return the report path
   */
  public String getReportPath() {
    return reportPath;
  }

  /**
   * Gets json config object.
   *
   * @return the json config object
   */
  public JsonObject getJsonConfigObject() {
    return jsonConfigObject;
  }

  /**
   * Gets matrix.
   *
   * @return the matrix
   */
  public List<List<Integer>> getMatrix() {
    return matrix;
  }
}
