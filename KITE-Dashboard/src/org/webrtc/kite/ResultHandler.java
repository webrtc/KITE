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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.dao.*;
import org.webrtc.kite.pojo.Browser;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/** A class handling the input into the database */
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



  /** Returns the connection to the database. */
  private Connection getDatabaseConnection() throws SQLException, ClassNotFoundException {
    return new DBConnectionManager(this.pathToDB).getConnection();
  }

  /**
   * Inserts new config and test to appropriate table and create result table for new test.
   *
   * @param connection a JDBC connection to the database.
   * @param configName name of the new configuration.
   * @param testJsonObject json object containing all of the needed information about the test.
   * @param testSuiteBrowserPairList list of participating browser pairs in the test suite.
   */
  private void preliminaryInsert(
      Connection connection,
      String configName,
      JsonObject testJsonObject,
      List<List<Browser>> testSuiteBrowserPairList,
      String description)
      throws SQLException {
    List<String> queryList = new ArrayList<>();
    long startTime = testJsonObject.getJsonNumber("timeStamp").longValue();
    int configId = new ExecutionDao(connection).insertNewConfig( configName, startTime);
    int testId = new TestDao(connection).insertNewTest(testJsonObject, startTime, description, testSuiteBrowserPairList.size(), configId);
    String resultTableName = testJsonObject.getString("testName");
    String resultTableQuery = "CREATE TABLE IF NOT EXISTS "+resultTableName+" (ID INTEGER NOT NULL, " +
        "TEST_ID INTEGER NOT NULL, " +
        "CONFIG_ID INTEGER, " +
        "START_TIME INTEGER NOT NULL, " +
        "DURATION INTEGER, " +
        "BROWSERS TEXT , " +
        "STAT_ID INTEGER, " +
        "RESULT TEXT NOT NULL, " +
        "PRIMARY KEY (ID) )";

    queryList.add(resultTableQuery);

    for (List<Browser> testCaseBrowserList : testSuiteBrowserPairList) {
      JsonArrayBuilder browserIds = Json.createArrayBuilder();
      for (Browser browser: testCaseBrowserList) {
        int browserID = new BrowserDao(connection).getId(browser);
        browserIds.add(browserID);
      }
      String blankResult =
          "INSERT INTO " + resultTableName + "(TEST_ID, CONFIG_ID, START_TIME, DURATION, BROWSERS, STAT_ID, RESULT) " +
              "VALUES("+testId+", " + configId + ", " + startTime + ", 0"
              + ", '" + browserIds.build().toString() + "', 0, 'SCHEDULED');";
      queryList.add(blankResult);
    }

    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        if (log.isDebugEnabled()) {
          log.debug("Executing Preliminary Insert:" + query);
        }
        statement.addBatch(query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }




  /**
   * Inserts new browser in BROWSERS table update new result in appropriate place result table
   *
   * @param connection a JDBC connection to the database.
   * @param tableName name of the result table for this test.
   * @param targetList list of targeted browsers in the test.
   * @param destinationList list of actual browsers in the test.
   * @param payload actual result of the test.
   * @param timeTaken duration of the test.
   */
  private void postResultInsert(
      Connection connection,
      String tableName,
      List<Browser> targetList,
      List<Browser> destinationList,
      JsonObject payload,
      long timeTaken,
      long starTime)
      throws SQLException {

    List<String> queries = new ArrayList<>();
    String targetBrowsers, destidationBrowsers;
    int tupleSize = destinationList.size();
    if (log.isDebugEnabled()) {
      log.debug("Tuple size is " + tupleSize);
    }
    JsonArrayBuilder browsers = Json.createArrayBuilder();
    for (Browser target : targetList) {
      browsers.add(new BrowserDao(connection).getId(target));
    }
    targetBrowsers = browsers.build().toString();
    if (log.isDebugEnabled()) {
      log.debug("targetIdList:" + targetBrowsers);
    }

    browsers = Json.createArrayBuilder();
    for (Browser destination : destinationList) {
      browsers.add(new BrowserDao(connection).getId(destination));
    }
    destidationBrowsers = browsers.build().toString();
    if (log.isDebugEnabled()){
      log.debug("destinationIdList:" + destidationBrowsers);
    }
    /* Create query Strings */
    String resultString = null;
    String resultUpdate = null;
    int testId = new TestDao(connection).getId(tableName, starTime);
    int resultId = new ResultDao(connection).getId(tableName, starTime, targetBrowsers);
    resultString = payload.getString("result").replaceAll("\\n", "").replaceAll("'", "");
    resultUpdate =
        "UPDATE " + tableName + " SET RESULT='" + resultString + "', DURATION=" + timeTaken;
    if (!tableName.startsWith("WPT")) {
      JsonObject resultObject = null;
      try {
        InputStream stream =
            new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
        JsonReader reader = Json.createReader(stream);
        resultObject = reader.readObject();
        resultString = Utility.escapeSpecialCharacter(resultObject.getString("result"));
      } catch (JsonParsingException e) {
        resultString = e.getMessage().replaceAll("'", "");
      }
      resultUpdate =
          "UPDATE " + tableName + " SET RESULT='" + resultString + "', DURATION=" + timeTaken;

      if (resultObject != null && resultObject.get("stats") != null) {
        String statString = resultObject.getJsonObject("stats").toString();
        int statID =
            new StatsDao(connection).insertNewStat(resultId, testId, targetBrowsers, statString);
        resultUpdate += " , STAT_ID=" + statID + "";
      }
    }


    /*    if (!targetBrowsers.equalsIgnoreCase(destidationBrowsers)) {
      for (Browser browser: destinationList){
        if (new BrowserDao(connection).getId(browser) == -1 ){
          new BrowserDao(connection).insertNewBrowser(browser);
        }
      }
      resultUpdate += " , BROWSERS='" + destidationBrowsers +"'";
    }*/

    resultUpdate += " WHERE ID=" + resultId;
    queries.add(resultUpdate);

    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queries) {
        if (log.isDebugEnabled()){
          log.debug("Executing Result Insert:" + query);
        }
        System.out.println("Executing Result Insert:" + query);
        statement.addBatch(query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }

  }

  /**
   * Updates the status for configuration & test when they're done.
   *
   * @param connection a JDBC connection to the database.
   * @param configName name of the configuration.
   * @param testName name of the test.
   * @param timeStamp start time of the configuration.
   * @param endTime end time of the configuration.
   */
  private void updateStatus(
      Connection connection,
      String configName,
      String testName,
      long timeStamp,
      long endTime)
      throws SQLException {
    String query1 =
        "UPDATE TESTS SET STATUS='DONE', END_TIME="
            + endTime
            + " WHERE TEST_NAME='"
            + testName
            + "' AND START_TIME="
            + timeStamp
            + ";";

    String query2 =
        "UPDATE CONFIG_EXECUTION SET STATUS='DONE', END_TIME="
            + endTime
            + " WHERE CONFIG_NAME='"
            + configName
            + "' AND START_TIME="
            + timeStamp
            + " ;";
    Statement statement = null;
    try {
      statement = connection.createStatement();
      statement.addBatch(query1);
      statement.addBatch(query2);
      if (log.isDebugEnabled()) {
        log.debug("Executing Status Update: " + query1);
        log.debug("Executing Status Update: " + query2);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }


  /** Updates version of different clients in database. */
  private void updateClientVersion(Connection connection, String payload, long timeStamp)
      throws SQLException {
    List<String> queryList = new ArrayList<>();
    String version;
    String client;
    List<String> entryPrime;
    if (log.isDebugEnabled()) log.debug("Incoming update payload: " + payload.toString());
    String trimmedPayload = payload.substring(1, payload.length() - 1);
    List<String> jsonPayload = new ArrayList<>(Arrays.asList(trimmedPayload.split(",")));

    for (String entry : jsonPayload) {
      entryPrime = new ArrayList<>(Arrays.asList(entry.split(":")));
      client = entryPrime.get(0).replaceAll("\\\\\"", "");
      version = entryPrime.get(1).replaceAll("\\\\\"|}\"", "");
      String currentVersion = new ClientVersionDao(connection).getVersionByClient(client);
      if (!version.equalsIgnoreCase(currentVersion)) {
        queryList.add(
            "UPDATE CLIENT_VERSION SET LAST_VERSION = (select VERSION WHERE NAME = '"
                + client
                + "'), LAST_UPDATE="
                + timeStamp
                + ", VERSION = '"
                + version
                + "' WHERE NAME = '"
                + client
                + "'");
      }
    }
    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        statement.addBatch(query);
        if (log.isDebugEnabled()) {
          log.debug("Executing client version Update: " + query);
        }
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }

  /**
   * Puts received results into the database accordingly from a json object containing every thing.
   *
   * @param jsonObject json object containing the result, received from client's side callback.
   */
  public void dumpResult(JsonObject jsonObject) throws SQLException, ClassNotFoundException {
    JsonObject testObject = jsonObject.getJsonObject("test");
    Connection connection = null;
    String testImpl = testObject.getString("testImpl");
    String testName = testObject.getString("testName");
    String configName = testObject.getString("configName");
    long timeStamp = testObject.getJsonNumber("timeStamp").longValue();
    int tupleSize = testObject.getInt("tupleSize");
    JsonObject resultObject = jsonObject.getJsonObject("result");
    JsonObject jsonPayload = resultObject.getJsonObject("payload");

    JsonArray testCaseBrowserJsonTargetList = jsonObject.getJsonArray("target");
    JsonArray testCaseBrowserJsonDestinationList = jsonObject.getJsonArray("destination");
    List<Browser> testCaseBrowserTargetList = new ArrayList<>();
    List<Browser> testCaseBrowserDestinationList = new ArrayList<>();
    for (int i = 0; i < tupleSize; i++) {
      testCaseBrowserTargetList.add(
          new Browser((JsonObject) testCaseBrowserJsonTargetList.get(i)));
      testCaseBrowserDestinationList.add(
          new Browser((JsonObject) testCaseBrowserJsonDestinationList.get(i)));
    }

    if (!testImpl.endsWith("GetUpdateTest")) { // normal tests
      try {
        JsonObject metaObject = jsonObject.getJsonObject("meta");
        connection = this.getDatabaseConnection();
        if (metaObject != null) {
          int totalTests = metaObject.getInt("totalTests", 0);
          if (totalTests > 0) {
            String description =
                metaObject.getString("description", "No description was provided fot this test.");
            JsonArray testSuiteBrowserJsonList = (JsonArray) metaObject.get("browsers");
            if (log.isDebugEnabled()){
              log.debug("test suite json browser list ->>" + testSuiteBrowserJsonList.toString());
            }

            List<Browser> testSuiteBrowserList = new ArrayList<>();
            for (JsonValue jsonBrowser : testSuiteBrowserJsonList) {
              testSuiteBrowserList.add(new Browser((JsonObject) jsonBrowser));
            }

            if (log.isDebugEnabled()) {
              log.debug(
                  "test suite browser list ->>"
                      + Arrays.deepToString(testSuiteBrowserList.toArray()));
            }
            List<List<Browser>> testSuiteBrowserPairMatrix =
                Utility.buildTuples(testSuiteBrowserList, tupleSize);
            connection.setAutoCommit(false);
            for (Browser browser: testSuiteBrowserList) {
              if (new BrowserDao(connection).getId(browser) == -1 ){
                new BrowserDao(connection).insertNewBrowser(browser);
              }
            }
            connection.commit();
            if (testImpl.endsWith("WPTest")){
              String resultString = jsonPayload.getString("result");
              JsonObject result = this.transformResult(resultString);
              if (result != null) {
                for (String testSuiteName : result.keySet()) {
                  JsonObject testSuite = result.getJsonObject(testSuiteName);
                  if (!testSuite.keySet().isEmpty()) {
                    for (String wpTest : testSuite.keySet()) {
                      if (!wpTest.equalsIgnoreCase("total")
                              && !wpTest.equalsIgnoreCase("passed")
                              && !wpTest.equalsIgnoreCase("isTest")) {
                        JsonObjectBuilder newWPTestObject = Json.createObjectBuilder();
                        newWPTestObject.add("testName", "WPT_" + wpTest.replaceAll("-", "_").replaceAll("\\.", "_"))
                                .add("timeStamp", timeStamp)
                                .add("configName", "WPT_" + configName)
                                .add("testImpl", testImpl)
                                .add("tupleSize", tupleSize);
                        this.preliminaryInsert(
                                connection,
                                "WPT_" + configName,
                                newWPTestObject.build(),
                                testSuiteBrowserPairMatrix,
                                description);
                        connection.commit();

                      }
                    }
                  }
                }
              }
              connection.setAutoCommit(true);
            } else {
              this.preliminaryInsert(
                  connection,
                  configName,
                  testObject,
                  testSuiteBrowserPairMatrix,
                  description);
              connection.commit();
              connection.setAutoCommit(true);
            }
          }
          if (metaObject.get("lastTest") != null) {
            long endTime = System.currentTimeMillis();
            if (testImpl.endsWith("WPTest")){
              String resultString = jsonPayload.getString("result");
              JsonObject result = this.transformResult(resultString);
              for (String testSuiteName: result.keySet()){
                JsonObject testSuite = result.getJsonObject(testSuiteName);
                if (!testSuite.keySet().isEmpty()){
                  for (String wpTest: testSuite.keySet()){
                    if (!wpTest.equalsIgnoreCase("total")
                        &&!wpTest.equalsIgnoreCase("passed")
                        &&!wpTest.equalsIgnoreCase("isTest")){
                      JsonObjectBuilder newWPTestObject = Json.createObjectBuilder();
                      newWPTestObject.add("testName","WPT_"+wpTest.replaceAll("-", "_").replaceAll("\\.", "_"))
                          .add("timeStamp",timeStamp)
                          .add("configName", "WPT_"+configName)
                          .add("testImpl", testImpl)
                          .add("tupleSize", tupleSize);
                      this.updateStatus(
                          connection, "WPT_"+configName, "WPT_"+wpTest.replaceAll("-", "_").replaceAll("\\.", "_"), timeStamp, endTime);
                    }
                  }
                }
              }
            } else {
              this.updateStatus(
                  connection, configName, testName, timeStamp, endTime);
            }
          }
        }
        // this.putInBrowserTable(connection, testCaseBrowserDestinationList);
        if (testImpl.endsWith("WPTest")){
          String resultString = jsonPayload.getString("result");
          try {
            JsonObject result = this.transformResult(resultString);
            for (String testSuiteName : result.keySet()) {
              JsonObject testSuite = result.getJsonObject(testSuiteName);
              if (!testSuite.keySet().isEmpty()) {
                for (String wpTest : testSuite.keySet()) {
                  if (!wpTest.equalsIgnoreCase("total")
                          && !wpTest.equalsIgnoreCase("passed")
                          && !wpTest.equalsIgnoreCase("isTest")) {
                    JsonObject wpTestObject = testSuite.getJsonObject(wpTest);
                    JsonObjectBuilder newWPTestObject = Json.createObjectBuilder();
                    newWPTestObject.add("testName", "WPT_" + wpTest.replaceAll("-", "_").replaceAll("\\.", "_"))
                            .add("timeStamp", timeStamp)
                            .add("configName", "WPT_" + configName)
                            .add("testImpl", testImpl)
                            .add("tupleSize", tupleSize);

                    JsonObjectBuilder newWPTesPayload = Json.createObjectBuilder();
                    newWPTesPayload.add("result", wpTestObject.toString());
                    this.postResultInsert(
                            connection,
                            "WPT_" + wpTest.replaceAll("-", "_").replaceAll("\\.", "_"),
                            testCaseBrowserTargetList,
                            testCaseBrowserDestinationList,
                            newWPTesPayload.build(),
                            resultObject.getJsonNumber("timeTaken").longValue(),
                            timeStamp);
                  }
                }
              }
            }
          } catch (JsonParsingException e) {
            log.error("Exception handling WPTest results", e);
            e.printStackTrace();
          }
        } else {
          this.postResultInsert(
              connection,
              testName,
              testCaseBrowserTargetList,
              testCaseBrowserDestinationList,
              jsonPayload,
              resultObject.getJsonNumber("timeTaken").longValue(),
              timeStamp);
        }
      } catch (SQLException e) {
        log.error("Exception update result", e);
        e.printStackTrace();
        if (connection != null)
          try {
            connection.rollback();
          } catch (SQLException e1) {
          }
      } finally {
        if (connection != null) connection.close();
      }

      // For updating client version purpose only
    } else {
      try {
        connection = this.getDatabaseConnection();
        this.updateClientVersion(connection, jsonPayload.toString(), timeStamp);
      } catch (SQLException e) {
        log.error("dumping result", e);
        if (connection != null)
          try {
            connection.rollback();
          } catch (SQLException e1) {
          }
      } finally {
        if (connection != null) connection.close();
      }
    }
  }


  /**
   * Transform usual result to isWebRTCReady-like result
   */
  private JsonObject transformResult(String resultString) {
    try {
      InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
      JsonReader reader = Json.createReader(stream);
      JsonObject resultObject = reader.readObject();
      resultObject = resultObject.getJsonObject("result");
      return resultObject;
    } catch (ClassCastException e) {
      log.error("Error casting WPT results");
      return null;
    }
  }
}
