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

package org.webrtc.kite.wpt.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.wpt.dashboard.dao.DBConnectionManager;
import org.webrtc.kite.wpt.dashboard.pojo.Browser;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * A class handling the input into the database
 */
public class ResultHandler {

  private static final Log log = LogFactory.getLog(ResultHandler.class);
  private static final String DEFAULT_PAYLOAD = "INIT PAYLOAD";

  private String pathToDB;

  /**
   * Constructs a ResultHandler object with a given path to the database.
   *
   * @param pathToDB path to the database
   */
  public ResultHandler(String pathToDB) {
    this.pathToDB = pathToDB;
  }

  /**
   * Returns the connection to the database.
   */
  private Connection getDatabaseConnection() throws SQLException, ClassNotFoundException {
    return new DBConnectionManager(this.pathToDB).getConnection();
  }


  /**
   * Updates existing or registers new WPT results into the DB.
   */
  private void updateWPTResultTable(Connection connection, JsonObject payload, Browser browser, long timeStamp) throws SQLException {
    List<String> queryList = new ArrayList<>();

    String browserTableName = Utility.generateTableNameFromBrowser(browser);
    String createTableQuery = "CREATE TABLE IF NOT EXISTS " + "WPT_" + browserTableName + " (TEST_NAME TEXT NOT NULL, TOTAL INTEGER, PASS INTEGER, " +
        "RESULT TEXT NOT NULL, LAST_UPDATE, PRIMARY KEY (TEST_NAME));";
    queryList.add(createTableQuery);
    String insertResultTableQuery = "REPLACE INTO WPT (BROWSER,VERSION,PLATFORM,TABLE_NAME, IS_WEBRTC_READY_TABLE_NAME ,LAST_UPDATE)" +
        " VALUES ('" + browser.getName() + "','" + browser.getVersion().split("\\.")[0] + "','" + browser.getPlatform() + "','" + "WPT_" + browserTableName
        + "','" + "WPT_READY_" + browserTableName + "'," + timeStamp + ");";
    queryList.add(insertResultTableQuery);

    String resultString = payload.getString("result");
    InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
    JsonReader reader = Json.createReader(stream);
    JsonObject resultObject = reader.readObject();
    resultObject = resultObject.getJsonObject("result");
    Set<String> testSuiteList = resultObject.keySet();
    for (String testSuite : testSuiteList) {
      if (!testSuite.equalsIgnoreCase("total") && !testSuite.equalsIgnoreCase("passed")) {
        JsonObject testSuiteObj = resultObject.getJsonObject(testSuite);
        String result = testSuiteObj.toString().replaceAll("'", "");

        int total = testSuiteObj.getInt("total", 0);
        int pass = testSuiteObj.getInt("passed", 0);
        String insertTestSuiteResultQuery = "REPLACE INTO " + "WPT_" + browserTableName + " (TEST_NAME, TOTAL, PASS, RESULT, LAST_UPDATE) " +
            "VALUES ('" + testSuite + "'," + total + "," + pass + ",'" + result + "'," + timeStamp + ")";
        queryList.add(insertTestSuiteResultQuery);
      }
    }
    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        statement.addBatch(query);
        if (log.isDebugEnabled())
          log.debug("Executing WPT result update: " + query);
        //System.out.println("Executing WPT result update: " + query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }


  /**
   * Updates existing or registers new WPT results into the DB.
   */
  private void updateWPTREADYResultTable(Connection connection, JsonObject payload, Browser browser, long timeStamp) throws SQLException {
    List<String> queryList = new ArrayList<>();

    String tableName = "WPT_READY_" + Utility.generateTableNameFromBrowser(browser);
    String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (TEST_GROUP TEXT NOT NULL, TEST_NAME TEXT NOT NULL, TOTAL INTEGER, PASS INTEGER, " +
        "RESULT TEXT NOT NULL, LAST_UPDATE ,PRIMARY KEY(TEST_GROUP, TEST_NAME));";
    queryList.add(createTableQuery);

    String resultString = payload.getString("result");
    JsonObject result = this.transformResult(resultString);

    //System.out.println("transformed:=>" + result);
    for (String test : result.keySet()) {
      JsonObject testObject = result.getJsonObject(test);
      for (String subTest : testObject.keySet()) {
        if (subTest.equalsIgnoreCase("total") || subTest.equalsIgnoreCase("passed")) {
          //skip
        } else {
          JsonObject subTestObj = testObject.getJsonObject(subTest);
          String resultStr = subTestObj.toString().replaceAll("'", "").replaceAll("\\'", "").replaceAll("\'", "");
          int total = subTestObj.getInt("total", 0);
          int passed = subTestObj.getInt("passed", 0);
          String insertQuery = "REPLACE INTO " + tableName + " (TEST_GROUP, TEST_NAME, TOTAL, PASS, RESULT, LAST_UPDATE) " +
              "VALUES ('" + test + "','" + subTest + "'," + total + "," + passed + ",'" + resultStr + "'," + timeStamp + ")";
          queryList.add(insertQuery);
        }
      }
    }
    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        statement.addBatch(query);
        if (log.isDebugEnabled())
          log.debug("Executing WPT result update: " + query);
        // System.out.println("Executing WPT result update: " + query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }


  /**
   * Transform usual result to isWebRTCReady-like result
   */
  private JsonObject transformResult(String resultString) {
    InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
    JsonReader reader = Json.createReader(stream);
    JsonObject resultObject = reader.readObject();
    resultObject = resultObject.getJsonObject("result");
    Set<String> testSuiteList = resultObject.keySet();
    Map<String, JsonObjectBuilder> WPT_READY_MAP = new HashMap<>();
    for (String testSuite : testSuiteList) {
      JsonObject testSuiteJsonObject = resultObject.getJsonObject(testSuite);
      for (String test : Mapping.WPTMapping.keySet()) {
        if (WPT_READY_MAP.get(test) == null)
          WPT_READY_MAP.put(test, Json.createObjectBuilder());
        JsonObjectBuilder testJsonObjectbBuidler = WPT_READY_MAP.get(test);
        List<String> subTestList = Mapping.WPTMapping.get(test);
        if (subTestList.size() == 0) {
          // Tests without any sub tests specified at the moment in mapping.
        } else {
          for (String subTest : subTestList) {
            System.out.println("sub test -> " + subTest);
            if (subTest.equalsIgnoreCase("total") || subTest.equalsIgnoreCase("passed")) {
              //skip
            } else {
              JsonObject subTestObject = testSuiteJsonObject.getJsonObject(subTest);
              if (subTestObject == null) {
                // Skipping these
              } else {
                testJsonObjectbBuidler.add(subTest, Json.createObjectBuilder().add("result", subTestObject)
                    .add("total", subTestObject.getInt("total", 0))
                    .add("passed", subTestObject.getInt("passed", 0)));
              }
            }
          }
        }
        WPT_READY_MAP.put(test, testJsonObjectbBuidler);
      }
    }
    JsonObjectBuilder result = Json.createObjectBuilder();
    for (String test : WPT_READY_MAP.keySet()) {
      result.add(test, WPT_READY_MAP.get(test));
    }
    return result.build();
  }

  /**
   * Puts received results into the database accordingly from a json object containing every thing.
   *
   * @param jsonObject json object containing the result, received from client's side callback.
   */
  public void dumpResult(JsonObject jsonObject) throws SQLException, ClassNotFoundException {
    JsonObject testObject = jsonObject.getJsonObject("test");
    Connection connection = null;
    long timeStamp = testObject.getJsonNumber("timeStamp").longValue();
    JsonArray testCaseBrowserJsonTargetList = jsonObject.getJsonArray("target");
    Browser browser = new Browser((JsonObject) testCaseBrowserJsonTargetList.get(0));
    JsonObject resultObject = jsonObject.getJsonObject("result");
    try {
      connection = this.getDatabaseConnection();
      JsonObject jsonPayload = resultObject.getJsonObject("payload");
      boolean RUN_ALL = jsonPayload.getBoolean("all", false);
      this.updateWPTResultTable(connection, jsonPayload, browser, timeStamp);
      if (!RUN_ALL)
        this.updateWPTREADYResultTable(connection, jsonPayload, browser, timeStamp);
    } catch (SQLException e) {
      e.printStackTrace();

      if (connection != null)
        try {
          connection.rollback();
        } catch (SQLException e1) {
        } finally {
          if (connection != null)
            connection.close();
        }
    }
  }

}
