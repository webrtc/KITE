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

package org.webrtc.kite;

import io.cosmosoftware.kite.util.WebDriverUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.config.App;
import org.webrtc.kite.config.Browser;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.config.Tuple;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteNoKeyException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
/**
 * Utils class holding various static methods.
 */
public class Utils {

  private static final Logger logger = Logger.getLogger(Utils.class.getName());
  
  /**
   * Returns stack trace of the given exception.
   *
   * @param e Exception
   *
   * @return string representation of e.printStackTrace()
   */
  public static String getStackTrace(Throwable e) {
    Writer writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }
  
  /**
   * Checks if the given key exists in the given JsonObject with a valid value.
   *
   * @param jsonObject JsonObject
   * @param key        key
   * @param valueClass Class object for the value of the key.
   * @param isOptional A boolean specifying that the value may be optional. Note: This only works if the valueClass is String.
   *
   * @return the value of the key
   * @throws KiteNoKeyException    if the key is not mapped in the JsonObject.
   * @throws KiteBadValueException if the value of the key is invalid.
   */
  public static Object throwNoKeyOrBadValueException(JsonObject jsonObject, String key,
      Class<?> valueClass, boolean isOptional) throws KiteNoKeyException, KiteBadValueException {
    Object value = null;
    try {
      switch (valueClass.getSimpleName()) {
        case "String":
          value = jsonObject.getString(key);
          break;
        case "Integer":
          value = jsonObject.getInt(key);
          break;
        case "JsonArray":
          value = jsonObject.getJsonArray(key);
          break;
        case "JsonObject":
          value = jsonObject.getJsonObject(key);
          break;
        default:
          value = jsonObject.get(key);
      }
      return value;
    } catch (NullPointerException e) {
      if (isOptional) {
        return null;
      } else {
        throw new KiteNoKeyException(key);
      }
    } catch (ClassCastException e) {
      throw new KiteBadValueException(key);
    }
  }
  
  /**
   * Closes the given jdbc resources if they are not null.
   *
   * @param s  Statement
   * @param rs ResultSet
   */
  public static void closeDBResources(Statement s, ResultSet rs) {
    if (rs != null)
      try {
        rs.close();
      } catch (SQLException e) {
        logger.warn("Exception while closing the ResultSet", e);
      }
    if (s != null)
      try {
        s.close();
      } catch (SQLException e) {
        logger.warn("Exception while closing the Statement", e);
      }
  }
  
  /**
   * Checks whether both the given objects are null.
   *
   * @param object1 Object 1
   * @param object2 Object 2
   *
   * @return true if both the provided objects are null.
   */
  public static boolean areBothNull(Object object1, Object object2) {
    return object1 == null && object2 == null;
  }
  
  /**
   * Checks whether both the given objects are not null.
   *
   * @param object1 Object 1
   * @param object2 Object 2
   *
   * @return true if both the provided objects are not null.
   */
  public static boolean areBothNotNull(Object object1, Object object2) {
    return object1 != null && object2 != null;
  }
  
  /**
   * The OS Name without the version, meant for Browser.setPlatform(String platform) (e.g.: Windows, Linux, Mac...).
   * It will return System.getProperty("os.name") if it's not a standard Linux, Windows or Mac OS name.
   *
   * @return OS Name without the version(e.g.: Windows, Linux, Mac...)
   */
  public static String getSystemPlatform() {
    String osName = System.getProperty("os.name");
    if (osName.toLowerCase().contains("win")) {
      return "Windows";
    }
    if (osName.toLowerCase().contains("mac")) {
      return "Mac";
    }
    if (osName.toLowerCase().contains("nux")) {
      return "Linux";
    }
    if (osName.toLowerCase().contains("nix")) {
      return "Unix";
    }
    return osName;
  }
  
  /**
   * Reads a json file into a JsonObject
   *
   * @param jsonFile the file to read
   *
   * @return the jsonObject
   */
  public static JsonObject readJsonFile(String jsonFile) {
    FileReader fileReader = null;
    JsonReader jsonReader = null;
    try {
      logger.info("Reading '" + jsonFile + "' ...");
      fileReader = new FileReader(new File(jsonFile));
      jsonReader = Json.createReader(fileReader);
      return jsonReader.readObject();
    } catch (Exception e) {
      logger.error(getStackTrace(e));
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
    return null;
  }
  
  /**
   * Populate info from navigator.
   *
   * @param webDriver the web driver
   * @param browser   the browser
   */
  public static void populateInfoFromNavigator(WebDriver webDriver, Browser browser) {
  
    String userAgentScript =  "var nav = '';" + "try { var myNavigator = {};"
        + "for (var i in navigator) myNavigator[i] = navigator[i];"
        + "nav = JSON.stringify(myNavigator); } catch (exception) { nav = exception.message; }"
        + "return nav;";
    
    if (!browser.shouldGetUserAgent() || !WebDriverUtils.isAlive(webDriver))
      return;
    
    webDriver.get("http://www.google.com");
    Object resultObject = ((JavascriptExecutor) webDriver).executeScript(userAgentScript);
    logger.info("Browser platform and userAgent for: " + browser.toString() + "->" + resultObject);
    
    if (resultObject instanceof String) {
      String resultOfScript = (String) resultObject;
      browser.setUserAgentVersionAndPlatform(resultOfScript);
    }
  }
  
  /**
   * Gets the payload object for the test at index 'testIndex' in the config
   *
   * @param configFile the kite test config file
   * @param testIndex  the index of the test to get the payload from
   *
   * @return the payload object for the test at index 'testIndex' in the config
   */
  public static JsonObject getPayload(String configFile, int testIndex) {
    try {
      JsonObject jsonObject = readJsonFile(configFile);
      List<JsonObject> testObjectList =
        (List<JsonObject>)
          Utils.throwNoKeyOrBadValueException(jsonObject, "tests", JsonArray.class, false);
      return testObjectList.get(testIndex).getJsonObject("payload");
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    }
    return null;
  }
  
  /**
   * Gets the list of Browser from the config file.
   *
   * @param configFile the kite test config file
   * @param type       the type of endpoint: "browser" or "apps"
   *
   * @return the list of Browser from the config file.
   */
  public static Tuple getEndPointList(String configFile, String type) {
    Tuple endPoints = new Tuple();
    try {
      JsonObject jsonObject = readJsonFile(configFile);
      List<JsonObject> endpointObjectList =
        (List<JsonObject>)
          throwNoKeyOrBadValueException(jsonObject, type, JsonArray.class, false);
      List<JsonObject> remotes =
        (List<JsonObject>)
          throwNoKeyOrBadValueException(jsonObject, "remotes", JsonArray.class, false);
      String remoteAddress = remotes.get(0).getString("remoteAddress");
      List<JsonObject> testObjectList =
        (List<JsonObject>)
          throwNoKeyOrBadValueException(jsonObject, "tests", JsonArray.class, false);
      int tupleSize = testObjectList.get(0).getInt("tupleSize");
      for (JsonObject object : endpointObjectList) {
        for (int i = 0; i < tupleSize; i++) {
          endPoints.add(
            "browsers".equalsIgnoreCase(type)
              ? new Browser(remoteAddress, object)
              : new App(remoteAddress, object));
        }
      }
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    }
    return endPoints;
  }
  
  /**
   * Gets seconds from a string. e.g. 01:00 will return 60 seconds 01:00:00 will return 3600 seconds
   *
   * @param timeFormat the time format
   *
   * @return the seconds
   */
  public static int getSeconds(String timeFormat) {
    String[] timeSplit = timeFormat.split(":");
    return (timeSplit.length == 2)
        ? Integer.parseInt(timeSplit[0]) * 60 + Integer.parseInt(timeSplit[1])
        : Integer.parseInt(timeSplit[0]) * 60 * 60
            + Integer.parseInt(timeSplit[1]) * 60
            + Integer.parseInt(timeSplit[2]);
  }
  
  /**
   * Gets the integer value from a given json object,
   * overwrite it with System env or a minimum positive value if applicable
   *
   * @param jsonObject           the json object
   * @param key                  name of the attribute
   * @param minimumPositiveValue minimum positive value
   *
   * @return an integer value
   * @throws KiteInsufficientValueException if the value does not exist and no minimum positive value is provided
   */
  public static int getIntFromJsonObject(JsonObject jsonObject, String key, int minimumPositiveValue)
    throws KiteInsufficientValueException {
    if (System.getProperty(key) == null) {
      try {
        int value = jsonObject.getInt(key);
        if (value < minimumPositiveValue) {
          return minimumPositiveValue;
        } else {
          return value;
        }
      } catch (NullPointerException e) {
        if (minimumPositiveValue >= 0) {
          return minimumPositiveValue;
        } else {
          throw new KiteInsufficientValueException("Invalid minimum positive value for " + key);
        }
      }
    } else {
      return Integer.parseInt(System.getProperty(key));
    }
  }
  
  /**
   * Gets the String value from a given json object,
   * overwrite it with System env or a default value if applicable
   *
   * @param jsonObject   the json object
   * @param key          name of the attribute
   * @param defaultValue default value
   *
   * @return an String value
   */
  public static String getStringFromJsonObject(JsonObject jsonObject, String key, String defaultValue) {
    if (System.getProperty(key) == null) {
      return jsonObject.getString(key, defaultValue);
    } else {
      return System.getProperty(key);
    }
  }
  
  /**
   * Gets the boolean value from a given json object,
   * overwrite it with System env or a default value if applicable
   *
   * @param jsonObject   the json object
   * @param key          name of the attribute
   * @param defaultValue default value
   *
   * @return an Boolean value
   */
  public static boolean getBooleanFromJsonObject(JsonObject jsonObject, String key, boolean defaultValue) {
    if (System.getProperty(key) == null) {
      return jsonObject.getBoolean(key, defaultValue);
    } else {
      return Boolean.parseBoolean(System.getProperty(key));
    }
  }
  
}
