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

import io.cosmosoftware.kite.instrumentation.NetworkInstrumentation;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.WebDriverUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.config.client.App;
import org.webrtc.kite.config.client.Browser;
import org.webrtc.kite.config.media.MediaFile;
import org.webrtc.kite.config.test.Tuple;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteNoKeyException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static io.cosmosoftware.kite.util.TestUtils.getPrivateIp;
import static io.cosmosoftware.kite.util.TestUtils.readJsonFile;

/**
 * Utils class holding various static methods.
 */
public class Utils {
  
  private static final KiteLogger logger = KiteLogger.getLogger(Utils.class.getName());
  
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
  
  /**
   * Gets the list of Clients from the config file.
   *
   * @param configFile the kite test config file
   *
   * @return the list of Browser from the config file.
   */
  public static Tuple getFirstTuple(String configFile) {
    Tuple tuple = new Tuple();
    try {
      JsonObject jsonObject = readJsonFile(configFile);
      List<JsonObject> clientObjectList =
        (List<JsonObject>)
          throwNoKeyOrBadValueException(jsonObject, "clients", JsonArray.class, false);
      List<JsonObject> testObjectList =
        (List<JsonObject>)
          throwNoKeyOrBadValueException(jsonObject, "tests", JsonArray.class, false);
      int tupleSize = testObjectList.get(0).getInt("tupleSize");
      
      for (JsonObject object : clientObjectList) {
        for (int i = 0; i < tupleSize; i++) {
          tuple.add(
            object.get("browserName") != null
              ? new Browser(object)
              : new App(object));
        }
      }
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    }
    return tuple;
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
   * The OS Name without the version, meant for Browser.setPlatform(String platform) (Windows, Linux or Mac).
   * It will return System.getProperty("os.name") if it's not a standard Linux, Windows or Mac OS name.
   *
   * @return OS Name without the version(Windows, Linux or Mac)
   */
  public static String getSystemPlatform() {
    String osName = System.getProperty("os.name");
    if (osName.toLowerCase().contains("win")) {
      return "WINDOWS";
    }
    if (osName.toLowerCase().contains("mac")) {
      return "MAC";
    }
    if (osName.toLowerCase().contains("nux")) {
      return "LINUX";
    }
    return osName.toUpperCase();
  }
  
  /**
   * Populate info from navigator.
   *
   * @param webDriver the web driver
   * @param browser   the browser
   */
  public static void populateInfoFromNavigator(WebDriver webDriver, Browser browser) {
    
    String userAgentScript = "var nav = '';" + "try { var myNavigator = {};"
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


  static void makeCommand(String gridId, String nodeIp, String command){
    logger.info(gridId + " GRID ID");
    logger.info(nodeIp + " Node IP ");
    logger.info(command + " command");

    String url = "http://localhost:8080/KITEServer" + "/command?id=" + gridId + "&ip=" + nodeIp + "&cmd=" + command;
    try {
      URLConnection connection = new URL(url).openConnection();
      connection.setRequestProperty("Accept-Charset", "UTF-8");
      InputStream response = connection.getInputStream();
      System.out.println("SUCCEEDED and got response: " + response);
    } catch (IOException e) {
      System.out.println("ERROR: " + e.getLocalizedMessage());
    }
  }
}
