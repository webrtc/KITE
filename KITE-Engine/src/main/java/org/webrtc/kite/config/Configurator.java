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

import io.cosmosoftware.kite.instrumentation.Instrumentation;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.usrmgmt.AccountCollection;
import io.cosmosoftware.kite.usrmgmt.AccountManager;
import io.cosmosoftware.kite.usrmgmt.AccountRole;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.webrtc.kite.exception.KiteGridException;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteUnsupportedIntervalException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;
import org.webrtc.kite.scheduler.Interval;

import javax.json.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.downloadFile;
import static org.webrtc.kite.Utils.readJsonFile;
import static org.webrtc.kite.Utils.throwNoKeyOrBadValueException;

/**
 * Representation of the config file as a singleton.
 * <p>
 * {
 * "name": "config_name",
 * "interval": "HOURLY|DAILY|WEEKLY",
 * "callback": "http://localhost:8080/kiteweb/datacenter",
 * "remotes": [],
 * "tests": [],
 * "browsers": []
 * }
 */
public class Configurator {

  private static final Logger logger = Logger.getLogger(Configurator.class.getName());

  /* Singleton boiler plate code */
  private static Configurator instance = new Configurator();

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static Configurator getInstance() {
    return instance;
  }

  private Configurator() {
  }
  /* Singleton boiler plate code */

  private long timeStamp = System.currentTimeMillis();

  private int type;
  private String name;
  private int interval;
  private String commandName;
  private Instrumentation instrumentation;
  private String configFilePath;

  private List<JsonObject> remoteObjectList = null;

  private boolean customMatrixOnly = false;
  private List<JsonObject> customMatrix;
  private List<List<EndPoint>> customBrowserMatrix = new ArrayList<>();

  private ConfigHandler configHandler;
  private boolean skipSame = false;


  /**
   * Sets the command name for the NW instrumentation.
   *
   * @param commandName name of the command
   */
  public void setCommandName(String commandName) {
    this.commandName = commandName;
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
   * Gets time stamp.
   *
   * @return the time stamp
   */
  public long getTimeStamp() {
    return this.timeStamp;
  }

  /**
   * Sets time stamp.
   */
  public void setTimeStamp() {
    this.timeStamp = System.currentTimeMillis();
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
   * Gets interval.
   *
   * @return the interval
   */
  public int getInterval() {
    return this.interval;
  }


  /**
   * Gets the RemoteObjectList
   * @return the RemoteObjectList
   */
  public List<JsonObject> getRemoteObjectList() {
    return remoteObjectList;
  }

  /**
   * Gets the remoteAddress, which is the first address in the "remotes"
   * @return the RemoteObjectList
   */
  public String getRemoteAddress() {
    if (remoteObjectList != null && remoteObjectList.size() > 0) {
      return remoteObjectList.get(0).getString("remoteAddress");
    }
    return null;
  }

  /**
   * isCustomerMatrixOnly
   *
   * @return
   */
  public boolean isCustomMatrixOnly() {
    return customMatrixOnly;
  }

  /**
   * getCustomBrowserMatrix
   *
   * @return
   */
  public List<List<EndPoint>> getCustomBrowserMatrix() {
    return this.customBrowserMatrix;
  }


  public Instrumentation getInstrumentation() {
    return instrumentation;
  }

  /**
   * Gets config handler.
   *
   * @return the config handler
   */
  public ConfigHandler getConfigHandler() {
    return this.configHandler;
  }

  public String getConfigFilePath() {
    return configFilePath;
  }

  public void setConfigFilePath(String configFilePath) {
    this.configFilePath = configFilePath;
  }

  /**
   * Gets job class.
   *
   * @return the job class
   */
  public Class<? extends Job> getJobClass() {
    return configHandler.getJobClass();
  }

  /**
   * Builds itself based on the content of the config file.
   *
   * @throws FileNotFoundException            if the file is not found on provided path.
   * @throws KiteUnsupportedIntervalException if an unsupported interval is found in 'interval'.
   * @throws KiteInsufficientValueException   if the number of remotes, tests and browsers is less
   *                                          than 1.
   * @throws NoSuchMethodException            the no such method exception
   * @throws IllegalAccessException           the illegal access exception
   * @throws InstantiationException           the instantiation exception
   * @throws KiteUnsupportedRemoteException   if an unsupported remote is found in 'remotes'.
   * @throws InvocationTargetException        the invocation target exception
   * @throws KiteGridException                the kite grid exception
   */
  public void buildConfig()
    throws IOException, KiteUnsupportedIntervalException, KiteInsufficientValueException,
    NoSuchMethodException, IllegalAccessException, InstantiationException,
    KiteUnsupportedRemoteException, InvocationTargetException, KiteGridException {

    FileReader fileReader = null;
    JsonReader jsonReader = null;
    JsonObject jsonObject = null;
    try {
      logger.info("Reading '" + this.configFilePath + "' ...");
      fileReader = new FileReader(new File(this.configFilePath));
      jsonReader = Json.createReader(fileReader);
      jsonObject = jsonReader.readObject();
    } finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
          logger.warn(e.getMessage(), e);
        }
      }
      if (jsonReader != null) {
        jsonReader.close();
      }
    }

    this.name = "" + throwNoKeyOrBadValueException(jsonObject, "name", String.class, false);
    this.name = name.contains("%ts") ? name.replaceAll("%ts", "") + " (" + timestamp() + ")" : name;
    String reportPath = (String) throwNoKeyOrBadValueException(jsonObject, "reportFolder", String.class, true);

    Reporter.getInstance().setReportPath(reportPath);

    this.interval = Interval.interval(
      (String) throwNoKeyOrBadValueException(jsonObject, "interval", String.class, true));

    String callbackURL = jsonObject.getString("callback", null);

    List<JsonObject> testObjectList = (List<JsonObject>)
      throwNoKeyOrBadValueException(jsonObject, "tests", JsonArray.class, false);
    if (testObjectList.size() < 1) {
      throw new KiteInsufficientValueException("Test objects are less than one.");
    }

    List<JsonObject> browserObjectList = (List<JsonObject>) throwNoKeyOrBadValueException(jsonObject, "browsers", JsonArray.class, false);

    List<JsonObject> appObjectList = (List<JsonObject>)
      throwNoKeyOrBadValueException(jsonObject, "apps", JsonArray.class, true);

    int size =  + (browserObjectList != null ? browserObjectList.size() : 0) + (appObjectList != null ? appObjectList.size() : 0);

    if (size < 1) {
      throw new KiteInsufficientValueException("Less than one browser or app object.");
    }

    this.customMatrixOnly = jsonObject.getBoolean("customMatrixOnly", this.customMatrixOnly);

    this.customMatrix = (List<JsonObject>)
      throwNoKeyOrBadValueException(jsonObject, "matrix", JsonArray.class, true);

    boolean permute = jsonObject.getBoolean("permute", true);

    if (browserObjectList != null) {
      browserObjectList = new ArrayList<>(new LinkedHashSet<>(browserObjectList));
      if (browserObjectList.size() != size) {
        logger.warn("Duplicate browser configurations in the config file have been removed.");
      }
    }
    if (appObjectList != null) {
      appObjectList = new ArrayList<>(new LinkedHashSet<>(appObjectList));
      if (appObjectList.size() != size) {
        logger.warn("Duplicate app configurations in the config file have been removed.");
      }
    }

    String accountFilePath = jsonObject.getString("accounts", null);
    logger.info("accountFilePath = " + accountFilePath);

    if (accountFilePath != null) {
      AccountManager.getInstance().init(new AccountCollection(readJsonFile(accountFilePath)));
    }

    this.type = jsonObject.getInt("type", 1);

    Object object =
      throwNoKeyOrBadValueException(jsonObject, "remotes", JsonArray.class, this.type == 2);

    if (object != null) {
      remoteObjectList = (List<JsonObject>) object;
    }

    configHandler =
      new InteropConfigHandler(permute, callbackURL, remoteObjectList, testObjectList,
        browserObjectList, appObjectList);

    String instrumentUrl = jsonObject.getString("instrumentUrl", null);
    if (instrumentUrl != null) {
      String instrumentFile = System.getProperty("java.io.tmpdir") + "instrumentation.json";
      downloadFile(instrumentUrl, instrumentFile);
      JsonObject instrumentObject = readJsonFile(instrumentFile);
      this.instrumentation = new Instrumentation(instrumentObject);
    }
    skipSame = jsonObject.getBoolean("skipSame", skipSame);
    logger.info("Finished reading the configuration file");
  }

  /**
   * Creates a matrix of browser tuples.
   *
   * @param tupleSize tuple size
   * @return a matrix of browser tuples as List<List<EndPoint>>
   */
  public List<List<EndPoint>> buildTuples(int tupleSize, boolean permute) {

    if (this.customMatrix != null) {
      for (JsonStructure structure : this.customMatrix) {
        JsonArray jsonArray = (JsonArray) structure;
        List<EndPoint> browserList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++)
          browserList.add(this.configHandler.getEndPointList().get(jsonArray.getInt(i)));
        this.customBrowserMatrix.add(browserList);
      }

      if (this.customMatrixOnly)
        return null;
    }

    List<EndPoint> focusedList = new ArrayList<>();
    List<EndPoint> browserList = (List<EndPoint>) this.configHandler.getEndPointList();
    for (EndPoint browser : browserList) {
      if (browser.isFocus()) {
        focusedList.add(browser);
      }
    }

    List<EndPoint> endPointList =
      (List<EndPoint>) this.configHandler.getEndPointList();
    List<List<EndPoint>> listOfTuples = new ArrayList<List<EndPoint>>();

    double totalTuples = Math.pow(endPointList.size(), tupleSize);

    logger.info(totalTuples + " test cases to run");

    for (int i = 0; i < totalTuples; i++) {
      listOfTuples.add(new ArrayList<>());
    }

    for (int i = 0; i < tupleSize; i++) {
      double marge = totalTuples / Math.pow(endPointList.size(), i + 1);
      double rep = Math.pow(endPointList.size(), i);
      for (int x = 0; x < rep; x++) {
        for (int j = 0; j < endPointList.size(); j++) {
          for (int k = 0; k < marge; k++) {
            EndPoint endPoint = endPointList.get(j);
            if (endPoint instanceof Browser) {
              endPoint = new Browser((Browser) endPoint);
            } else {
              endPoint = new App((App) endPoint);
            }
            if (i == 0) {
              endPoint.getTypeRole().setRole(AccountRole.CALLER);
            }
            listOfTuples.get((int) (x * totalTuples / rep + j * marge + k))
              .add(i, endPoint);
          }
        }
      }
    }

    if (!permute) filterPermutations(listOfTuples);

    List<List<EndPoint>> tempListOfTuples = new ArrayList<>(listOfTuples);
    for (List<EndPoint> tuple : tempListOfTuples) {
      if (Collections.disjoint(tuple, focusedList)
        || (skipSame && tuple.stream().distinct().limit(2).count() <= 1)) {
        listOfTuples.remove(tuple);
      }
    }

    Collections.shuffle(listOfTuples);
    return listOfTuples;
  }

  private void filterPermutations(List<List<EndPoint>> listOfTuples) {
    List<List<EndPoint>> listOfBrowserList1 = new ArrayList<>();
    for (List<EndPoint> browserList : listOfTuples) listOfBrowserList1.add(browserList);
    List<List<EndPoint>> listOfBrowserList2 = new ArrayList<>();
    for (List<EndPoint> browserList : listOfTuples) listOfBrowserList2.add(browserList);
    for (int i = 0; i < listOfBrowserList1.size(); i++) {
      List<EndPoint> browserList1 = listOfBrowserList1.get(i);
      for (int j = i + 1; j < listOfBrowserList2.size(); j++) {
        List<EndPoint> browserList2 = listOfBrowserList2.get(j);
        if (listMatch(browserList1, browserList2)) {
          listOfTuples.remove(browserList2);
        }
      }
    }
  }

  private boolean listMatch(List<EndPoint> elements1, List<EndPoint> elements2) {
    // Optional quick test since size must match
    if (elements1.size() != elements2.size()) {
      return false;
    }
    List<EndPoint> work = new ArrayList(elements2);
    for (EndPoint element : elements1) {
      if (!work.remove(element)) {
        return false;
      }
    }
    return work.isEmpty();
  }
  
}
