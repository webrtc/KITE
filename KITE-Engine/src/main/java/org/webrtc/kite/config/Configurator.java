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

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.instrumentation.Instrumentation;
import io.cosmosoftware.kite.report.Reporter;
import org.apache.log4j.Logger;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteUnsupportedIntervalException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.downloadFile;
import static org.webrtc.kite.Utils.*;

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
  private String name;
  private String commandName;
  private String configFilePath;
  private boolean customMatrixOnly = false;
  private boolean skipSame = false;
  private long timeStamp = System.currentTimeMillis();
  private JsonObject jsonConfigObject;
  private ConfigHandler configHandler;
  private List<Tuple> customBrowserMatrix = new ArrayList<>();
  private List<JsonObject> testObjectList;
  private List<JsonObject> customMatrix;
  private List<JsonObject> remoteObjectList = null;
  private Instrumentation instrumentation = null;
  
  private Configurator() {
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
   */
  public void buildConfig()
    throws IOException, KiteInsufficientValueException,
    NoSuchMethodException, IllegalAccessException, InstantiationException,
    KiteUnsupportedRemoteException, InvocationTargetException {
  
    this.jsonConfigObject = readJsonFile(configFilePath);
    
    this.name = jsonConfigObject.getString("name", "");
    this.name = name.contains("%ts") ? name.replaceAll("%ts", "") + " (" + timestamp() + ")" : name;
    String reportPath = jsonConfigObject.getString("reportFolder", null);
    
    // for CI testing purpose, the report path will be the default one: pwd/kite-allure-reports
    if (System.getProperty("kite.custom.config") != null) {
      Reporter.getInstance().setReportPath("");
    } else {
      Reporter.getInstance().setReportPath(reportPath);
    }
    
    String callbackURL = jsonConfigObject.getString("callback", null);
    
    this.testObjectList = (List<JsonObject>)
      throwNoKeyOrBadValueException(jsonConfigObject, "tests", JsonArray.class, false);
    
    if (testObjectList.size() < 1) {
      throw new KiteInsufficientValueException("Test objects are less than one.");
    }
  
    List<JsonObject> browserObjectList = new ArrayList<>();
    List<JsonObject> appObjectList = new ArrayList<>();
    
    // for testing with custom endpoint (1 only)
    if (System.getProperty("kite.custom.config") != null) {
      JsonObject customBrowser = readJsonFile(System.getProperty("kite.custom.config"));
      logger.info("Running test on custom browser config: \n" + customBrowser.toString());
      browserObjectList.add(customBrowser);
    } else {
      browserObjectList = (List<JsonObject>) throwNoKeyOrBadValueException(jsonConfigObject, "browsers", JsonArray.class, false);
  
      appObjectList = (List<JsonObject>)
        throwNoKeyOrBadValueException(jsonConfigObject, "apps", JsonArray.class, true);
  
      int size = (browserObjectList != null ? browserObjectList.size() : 0) 
                   + (appObjectList != null ? appObjectList.size() : 0);
  
      if (size < 1) {
        throw new KiteInsufficientValueException("Less than one browser or app object.");
      }
  
      this.customMatrixOnly = jsonConfigObject.getBoolean("customMatrixOnly", this.customMatrixOnly);
  
      this.customMatrix = (List<JsonObject>)
        throwNoKeyOrBadValueException(jsonConfigObject, "matrix", JsonArray.class, true);
  
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
    }
  
    boolean permute = jsonConfigObject.getBoolean("permute", true);
  
    int type = jsonConfigObject.getInt("type", 1);
    
    Object object =
      throwNoKeyOrBadValueException(jsonConfigObject, "remotes", JsonArray.class, type == 2);
    
    if (object != null) {
      remoteObjectList = (List<JsonObject>) object;
    }
    
    configHandler =
      new ConfigHandler(permute, callbackURL, remoteObjectList, testObjectList,
        browserObjectList, appObjectList);
    
    String instrumentUrl = jsonConfigObject.getString("instrumentUrl", null);
    if (instrumentUrl != null) {
      try {
        String instrumentFile = System.getProperty("java.io.tmpdir") + "instrumentation.json";
        if (instrumentUrl.contains("://")) {
          //if this is a url, then download it
          downloadFile(instrumentUrl, instrumentFile);
        } else {
          //otherwise assume it can be read directly.
          instrumentFile = instrumentUrl;
        }
        JsonObject instrumentObject = readJsonFile(instrumentFile);
        this.instrumentation = new Instrumentation(instrumentObject);
      } catch (KiteTestException e) {
        logger.error(getStackTrace(e));
      }
    }
    skipSame = jsonConfigObject.getBoolean("skipSame", skipSame);
    logger.info("Finished reading the configuration file");
  }
  
  /**
   * Creates a matrix of browser tuples.
   *
   * @param tupleSize tuple size
   *
   * @return a matrix of browser tuples as List<Tuple>
   */
  public List<Tuple> buildTuples(int tupleSize, boolean permute, boolean regression) {
    List<Tuple> listOfTuples = new ArrayList<>();
    if (regression) {
      // only add 1 placeholder tuple
      listOfTuples.add(new Tuple());
    } else {
      if (this.customMatrix != null) {
        for (JsonStructure structure : this.customMatrix) {
          JsonArray jsonArray = (JsonArray) structure;
          Tuple tuple = new Tuple();
          for (int i = 0; i < jsonArray.size(); i++) {
            tuple.add(this.configHandler.getEndPointList().get(jsonArray.getInt(i)));
          }
          this.customBrowserMatrix.add(tuple);
        }
        
        if (this.customMatrixOnly)
          return null;
      }
      
      List<EndPoint> endPointList = this.configHandler.getEndPointList();
      
      List<EndPoint> focusedList = new ArrayList<>();
      for (EndPoint browser : endPointList) {
        if (browser.isFocus()) {
          focusedList.add(browser);
        }
      }
      
      listOfTuples = recursivelyBuildTuples(tupleSize, 0, endPointList, new Tuple(), permute);
      
      List<Tuple> tempListOfTuples = new ArrayList<>(listOfTuples);
      for (Tuple tuple : tempListOfTuples) {
        if (Collections.disjoint(tuple, focusedList)
          || (skipSame && tuple.stream().distinct().limit(2).count() <= 1)) {
          listOfTuples.remove(tuple);
        }
      }
      
      Collections.shuffle(listOfTuples);
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
  
  public String getConfigFilePath() {
    return configFilePath;
  }
  
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
   * getCustomBrowserMatrix
   *
   * @return the custom browser matrix
   */
  public List<Tuple> getCustomBrowserMatrix() {
    return this.customBrowserMatrix;
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static Configurator getInstance() {
    return instance;
  }
  
  public Instrumentation getInstrumentation() {
    return instrumentation;
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
   * Gets the remoteAddress, which is the first address in the "remotes"
   *
   * @return the RemoteObjectList
   */
  public String getRemoteAddress() {
    if (remoteObjectList != null && remoteObjectList.size() > 0) {
      return remoteObjectList.get(0).getString("remoteAddress");
    }
    return null;
  }
  
  /**
   * Gets the RemoteObjectList
   *
   * @return the RemoteObjectList
   */
  public List<JsonObject> getRemoteObjectList() {
    return remoteObjectList;
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
   * isCustomerMatrixOnly
   *
   * @return  true if the test is using custom matrix only
   */
  public boolean isCustomMatrixOnly() {
    return customMatrixOnly;
  }
  
  /**
   * Build a list of tuple from given endpoints list, permutation or combination
   *
   * @param targetSize   targeted tuple size
   * @param currentIndex index of the endpoint in the list
   * @param fullList     list of provided endpoints
   * @param refTuple     tuple currently being built
   * @param permutation  true if this is permutation, false if combination
   *
   * @return a list of tuples of endpoints.
   */
  private List<Tuple> recursivelyBuildTuples(int targetSize, int currentIndex, List<EndPoint> fullList, Tuple refTuple, boolean permutation) {
    List<Tuple> result = new ArrayList<>();
    if (currentIndex < targetSize - 1) {
      for (int index = 0; index < fullList.size(); index++) {
        Tuple temp = new Tuple(refTuple);
        temp.add(fullList.get(index));
        result.addAll(recursivelyBuildTuples(targetSize, currentIndex + 1,
          fullList.subList(permutation ? 0 : index, fullList.size()), temp, permutation));
      }
    } else {
      for (EndPoint endPoint : fullList) {
        Tuple temp = new Tuple(refTuple);
        temp.add(endPoint);
        result.add(temp);
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
  
  public JsonObject getJsonConfigObject() {
    return jsonConfigObject;
  }
}
