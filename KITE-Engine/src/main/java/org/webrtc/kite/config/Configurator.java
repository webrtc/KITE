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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.webrtc.kite.Utility;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteUnsupportedIntervalException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;
import org.webrtc.kite.scheduler.Interval;

import javax.json.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

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

  private ConfigHandler configHandler;

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
   * Gets config handler.
   *
   * @return the config handler
   */
  public ConfigHandler getConfigHandler() {
    return this.configHandler;
  }

  /**
   * Gets job class.
   *
   * @return the job class
   */
  public Class<? extends Job> getJobClass() {
    return configHandler.getJobClass();
  }

  ;

  /**
   * Builds itself based on the content of the config file.
   *
   * @param file a File representation of the config file.
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
  public void buildConfig(File file) throws FileNotFoundException, KiteUnsupportedIntervalException,
      KiteInsufficientValueException, NoSuchMethodException, IllegalAccessException,
      InstantiationException, KiteUnsupportedRemoteException, InvocationTargetException {

    FileReader fileReader = null;
    JsonReader jsonReader = null;
    JsonObject jsonObject = null;
    try {
      fileReader = new FileReader(file);
      jsonReader = Json.createReader(fileReader);
      jsonObject = jsonReader.readObject();
    } finally {
      if (fileReader != null)
        try {
          fileReader.close();
        } catch (IOException e) {
          logger.warn(e.getMessage(), e);
        }
      if (jsonReader != null)
        jsonReader.close();
    }

    this.type = jsonObject.getInt("type", 1);

    this.name =
        (String) Utility.throwNoKeyOrBadValueException(jsonObject, "name", String.class, false);

    this.interval = Interval.interval(
        (String) Utility.throwNoKeyOrBadValueException(jsonObject, "interval", String.class, true));

    String callbackURL = jsonObject.getString("callback", null);

    List<JsonObject> testObjectList = (List<JsonObject>) Utility
        .throwNoKeyOrBadValueException(jsonObject, "tests", JsonArray.class, false);
    if (testObjectList.size() < 1)
      throw new KiteInsufficientValueException("Test objects are less than one.");

    List<JsonObject> browserObjectList = (List<JsonObject>) Utility
        .throwNoKeyOrBadValueException(jsonObject, "browsers", JsonArray.class, false);
    int size = browserObjectList.size();
    if (size < 1)
      throw new KiteInsufficientValueException("Browser objects are less than one.");
    browserObjectList = new ArrayList<JsonObject>(new LinkedHashSet<JsonObject>(browserObjectList));
    if (browserObjectList.size() != size)
      logger.warn("Duplicate browser configurations in the config file have been removed.");

    List<JsonObject> remoteObjectList = null;
    Object object = Utility
        .throwNoKeyOrBadValueException(jsonObject, "remotes", JsonArray.class, this.type == 2);
    if (object != null)
      remoteObjectList = (List<JsonObject>) object;

    if ((remoteObjectList == null || remoteObjectList.size() < 1))
      throw new KiteInsufficientValueException("Either remotes are missing or are less than one.");

    configHandler =
        new ConfigTypeOneHandler(callbackURL, remoteObjectList, testObjectList, browserObjectList);

    logger.info("Finished reading the configuration file");
  }

  /**
   * Creates a matrix of browser tuples.
   *
   * @param tupleSize tuple size
   * @return a matrix of browser tuples as List<List<Browser>>
   */
  public List<List<Browser>> buildTuples(int tupleSize) {

    List<List<Browser>> listOfTuples = new ArrayList<List<Browser>>();

    double totalTuples = Math.pow(this.configHandler.getBrowserList().size(), tupleSize);

    logger.info(totalTuples + " test cases to run");

    for (int i = 0; i < totalTuples; i++)
      listOfTuples.add(new ArrayList<Browser>());

    for (int i = 0; i < tupleSize; i++) {
      double marge = totalTuples / Math.pow(this.configHandler.getBrowserList().size(), i + 1);
      double rep = Math.pow(this.configHandler.getBrowserList().size(), i);
      for (int x = 0; x < rep; x++)
        for (int j = 0; j < this.configHandler.getBrowserList().size(); j++)
          for (int k = 0; k < marge; k++)
            (listOfTuples.get((int) (x * totalTuples / rep + j * marge + k)))
                .add(i, new Browser(this.configHandler.getBrowserList().get(j)));
    }

    Collections.shuffle(listOfTuples);
    return listOfTuples;
  }

  /**
   * Returns a JsonArrayBuilder based on the browser list.
   *
   * @return JsonArrayBuilder browser list json array
   */
  public JsonArrayBuilder getBrowserListJsonArray() {
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
    for (Browser browser : this.configHandler.getBrowserList())
      jsonArrayBuilder.add(browser.getJsonObjectBuilder());
    return jsonArrayBuilder;
  }

}
