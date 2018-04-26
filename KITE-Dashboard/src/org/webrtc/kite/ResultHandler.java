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
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.DBConnectionManager;
import org.webrtc.kite.pojo.Browser;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
   * Creates a matrix of browser tuples.
   *
   * @param tupleSize tuple size
   * @return a matrix of browser tuples as List<List<Browser>>
   */
  public static List<List<Browser>> buildTuples(List<Browser> browserList, int tupleSize) {

    List<List<Browser>> listOfBrowserList = new ArrayList<List<Browser>>();

    double totalTuples = Math.pow(browserList.size(), tupleSize);


    for (int i = 0; i < totalTuples; i++)
      listOfBrowserList.add(new ArrayList<Browser>());

    for (int i = 0; i < tupleSize; i++) {
      double marge = totalTuples / Math.pow(browserList.size(), i + 1);
      double rep = Math.pow(browserList.size(), i);
      for (int x = 0; x < rep; x++)
        for (int j = 0; j < browserList.size(); j++)
          for (int k = 0; k < marge; k++) {
            (listOfBrowserList.get((int) (x * totalTuples / rep + j * marge + k))).add(i,
                browserList.get(j));
          }
    }
    if (tupleSize > 1) {
      for (Browser browser : browserList) {
        if (browser.getPlatform().equalsIgnoreCase("android") || browser.getPlatform().equalsIgnoreCase("ios")) {
          List<Browser> tmp = new ArrayList<>();
          for (int i = 0; i < tupleSize; i++) {
            tmp.add(browser);
          }
          listOfBrowserList.remove(tmp);
        }
      }
    }
    if (log.isDebugEnabled())
      log.debug("matrix-->" + listOfBrowserList.toString());
    return listOfBrowserList;
  }

  /**
   * Returns the connection to the database.
   */
  private Connection getDatabaseConnection() throws SQLException, ClassNotFoundException {
    return new DBConnectionManager(this.pathToDB).getConnection();
  }

  /**
   * Inserts new config and test to appropriate table
   * and create result table for new test.
   *
   * @param connection               a JDBC connection to the database.
   * @param configName               name of the new configuration.
   * @param resultTableName          name of the result table for this test.
   * @param jsonObject               json object containing all of the needed information about the test.
   * @param testSuiteBrowserPairList list of participating browser pairs in the test suite.
   * @param totalTests               number of test cases in this test.
   */
  private void preliminaryInsert(Connection connection, String configName, String testName, String resultTableName, JsonObject jsonObject, List<List<Browser>> testSuiteBrowserPairList, int totalTests, String description) throws SQLException {
    List<String> queryList = new ArrayList<>();
    long startTime = jsonObject.getJsonNumber("timeStamp").longValue();
    queryList.add("INSERT INTO CONFIG_EXECUTION(CONFIG_NAME, START_TIME) SELECT '" + configName + "'," + startTime
        + " WHERE NOT EXISTS (SELECT 1 FROM CONFIG_EXECUTION WHERE START_TIME = " + startTime + ");");

    queryList.add("INSERT INTO TESTS(START_TIME,TEST_NAME, IMPL, TUPLE_SIZE, RESULT_TABLE, TOTAL_TESTS, CONFIG_ID, DESCRIPTION) "
        + "VALUES(" + startTime + ",'" + jsonObject.getString("testName") + "','"
        + jsonObject.getString("testImpl") + "'," + jsonObject.getInt("tupleSize") + ",'" + resultTableName
        + "'," + totalTests + "," + "(SELECT CONFIG_ID FROM CONFIG_EXECUTION" + " WHERE START_TIME=" + startTime
        + "), '" + description + "');");

    queryList.add("UPDATE CONFIG_EXECUTION SET TEST_COUNT=" +
        "(SELECT COUNT(*) FROM TESTS WHERE RESULT_TABLE LIKE '%" + startTime + "')" +
        " WHERE CONFIG_NAME='" + configName + "' AND START_TIME=" + startTime + " ;");

    int tupleSize = testSuiteBrowserPairList.get(0).size();
    String resultTableQuery = "CREATE TABLE IF NOT EXISTS " + resultTableName + "(RESULT TEXT NOT NULL,"
        + " DURATION INTEGER NOT NULL,STATS TEXT,";
    String overviewTableQuery = "CREATE TABLE IF NOT EXISTS kiteOVERVIEW" + testName.trim().replaceAll("[^a-zA-Z0-9]", "_") + " (";

    for (int i = 0; i < tupleSize; i++) {
      resultTableQuery += " BROWSER_" + (i + 1) + " INTEGER NOT NULL, ";
      overviewTableQuery += " BROWSER_" + (i + 1) + " INTEGER NOT NULL, ";
    }

    resultTableQuery += "PRIMARY KEY(";
    overviewTableQuery += " TEST_NAME TEXT NOT NULL, " +
        "START_TIME INTEGER NOT NULL, " +
        "DURATION INTEGER NOT NULL, " +
        "RESULT TEXT, " +
        "PRIMARY KEY(";

    for (int i = 0; i < tupleSize; i++) {
      resultTableQuery += " BROWSER_" + (i + 1);
      overviewTableQuery += " BROWSER_" + (i + 1);
      if (i != tupleSize - 1) {
        resultTableQuery += ", ";
        overviewTableQuery += ", ";
      } else {
        resultTableQuery += ")); ";
        overviewTableQuery += ")); ";
      }
    }

    queryList.add(resultTableQuery);
    queryList.add(overviewTableQuery);

    for (List<Browser> testCaseBrowserList : testSuiteBrowserPairList) {
      String blankResult = "REPLACE INTO " + resultTableName + " VALUES('SCHEDULED', 0, '{\"stats\":\"NA\"}' ,";
      for (int i = 0; i < testCaseBrowserList.size(); i++) {
        int browserID = new BrowserDao(connection).getBrowserId(testCaseBrowserList.get(i));
        blankResult += browserID;
        if (i < testCaseBrowserList.size() - 1)
          blankResult += ",";
        else
          blankResult += ");";
      }
      queryList.add(blankResult);
    }

    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        if (log.isDebugEnabled())
          log.debug("Executing Preliminary Insert:" + query);
        System.out.println("Executing Preliminary Insert:" + query);
        statement.addBatch(query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }

  /**
   * Inserts new browser in BROWSERS table
   * update new result in appropriate place result table
   * Inserts new result in OVERVIEW table if qualified.
   *
   * @param connection      a JDBC connection to the database.
   * @param tableName       name of the result table for this test.
   * @param targetList      list of targeted browsers in the test.
   * @param destinationList list of actual browsers in the test.
   * @param testName        name of the test in question.
   * @param payload         actual result of the test.
   * @param startTime       start time of the test.
   * @param timeTaken       duration of the test.
   */
  private void postResultInsert(Connection connection, String tableName, List<Browser> targetList, List<Browser> destinationList, String testName, JsonObject payload,
                                long startTime, long timeTaken) throws SQLException {
    List<String> queryList = new ArrayList<>();
    boolean browserChange = false;
    /* Get targeted id list and destination id list */
    List<Integer> targetIdList = new ArrayList<>();
    List<Integer> destinationIdList = new ArrayList<>();
    int tupleSize = destinationList.size();
    if (log.isDebugEnabled())
      log.debug("Tuple size is " + tupleSize);
    for (Browser target : targetList)
      targetIdList.add(new BrowserDao(connection).getBrowserId(target));
    if (log.isDebugEnabled())
      log.debug("targetIdList:" + Arrays.deepToString(targetIdList.toArray()));
    for (Browser destination : destinationList)
      destinationIdList.add(new BrowserDao(connection).getBrowserId(destination));
    if (log.isDebugEnabled())
      log.debug("destinationIdList:" + Arrays.deepToString(destinationIdList.toArray()));
    /* Create query Strings */


    String resultString = payload.getString("result").replaceAll("\\n", "");
    JsonObject resultObject = null;
    try {
      InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
      JsonReader reader = Json.createReader(stream);
      resultObject = reader.readObject();
      resultString = Utility.escapeSpecialCharacter(resultObject.getString("result"));
    } catch (JsonParsingException e) {
      resultString = "FAILED";
    }
    String resultUpdate = "";
    if (resultObject != null && resultObject.get("stats") != null) {
      String statString = resultObject.getJsonObject("stats").toString();
      resultUpdate = "UPDATE " + tableName + " SET RESULT='" + resultString + "', DURATION=" + timeTaken + ", STATS='" + statString + "' WHERE";
    } else {
      resultUpdate = "UPDATE " + tableName + " SET RESULT='" + resultString + "', DURATION=" + timeTaken + " WHERE";
    }
    String browserUpdate = "UPDATE " + tableName + " SET";
    for (int i = 0; i < tupleSize; i++) {
      if (targetIdList.get(i) != destinationIdList.get(i)) {
        if (!browserChange) {
          //browserChange = true;
          browserUpdate += " BROWSER_" + (i + 1) + "=" + destinationIdList.get(i);
        } else
          browserUpdate += " ,BROWSER_" + (i + 1) + "=" + destinationIdList.get(i);
      }
      if (browserChange) {
        resultUpdate += " BROWSER_" + (i + 1) + "=" + destinationIdList.get(i);
      } else {
        resultUpdate += " BROWSER_" + (i + 1) + "=" + targetIdList.get(i);
      }
      if (i < tupleSize - 1) {
        resultUpdate += " AND";
      } else {
        resultUpdate += ";";
      }
    }
    browserUpdate += " WHERE";
    for (int i = 0; i < tupleSize; i++) {
      browserUpdate += " BROWSER_" + (i + 1) + "=" + targetIdList.get(i);
      if (i < tupleSize - 1)
        browserUpdate += " AND";
      else
        browserUpdate += ";";
    }
    if (browserChange)
      queryList.add(browserUpdate);
    queryList.add(resultUpdate);

    String overviewQuery = "REPLACE INTO kiteOVERVIEW" + testName.trim().replaceAll("[^a-zA-Z0-9]", "_") + " (";
    for (int i = 0; i < tupleSize; i++)
      overviewQuery += "BROWSER_" + (i + 1) + ", ";
    overviewQuery += "TEST_NAME, START_TIME, DURATION, RESULT) VALUES (";
    for (int i = 0; i < tupleSize; i++) {
      //overviewQuery += destinationIdList.get(i)+ ", ";
      overviewQuery += targetIdList.get(i) + ", ";
    }

    overviewQuery += "'" + testName + "'," + startTime + ", " + timeTaken + ",'" + Utility.escapeSpecialCharacter(resultString) + "');";
    queryList.add(overviewQuery);

    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        if (log.isDebugEnabled())
          log.debug("Executing Result Insert:" + query);
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
   * @param connection      a JDBC connection to the database.
   * @param configName      name of the configuration.
   * @param testName        name of the test.
   * @param resultTableName name of the appropriate result table.
   * @param timeStamp       start time of the configuration.
   * @param endTime         end time of the configuration.
   */
  private void updateStatus(Connection connection, String configName, String testName,
                            String resultTableName, long timeStamp, long endTime) throws SQLException {
    String query1 = "UPDATE TESTS SET STATUS='DONE', END_TIME=" + endTime + " WHERE TEST_NAME='"
        + testName + "' AND RESULT_TABLE='" + resultTableName + "';";

    String query2 = "UPDATE CONFIG_EXECUTION SET STATUS='DONE', END_TIME=" + endTime
        + " WHERE CONFIG_NAME='" + configName + "' AND START_TIME=" + timeStamp + " ;";
    Statement statement = null;
    try {
      statement = connection.createStatement();
      statement.addBatch(query1);
      statement.addBatch(query2);
      if (log.isDebugEnabled())
        log.debug("Executing Status Update: " + query1);
      if (log.isDebugEnabled())
        log.debug("Executing Status Update: " + query2);
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }

  /**
   * Updates the status for configuration & test when they're done.
   *
   * @param connection  a JDBC connection to the database.
   * @param browserList list of browsers to put in the BROWSERS Table if not already exist
   */
  private void putInBrowserTable(Connection connection, List<Browser> browserList)
      throws SQLException {
    List<String> queryList = new ArrayList<>();
    for (Browser browser : browserList) {
      queryList.add(
          "INSERT INTO BROWSERS(NAME, VERSION, PLATFORM) " + "SELECT '" + browser.getName() + "','"
              + browser.getVersion() + "','" + browser.getPlatform() + "' " + "WHERE NOT EXISTS( "
              + "SELECT 1 FROM BROWSERS " + "WHERE NAME='" + browser.getName() + "' AND VERSION='"
              + browser.getVersion() + "' AND PLATFORM='" + browser.getPlatform() + "');");
    }
    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        statement.addBatch(query);
        if (log.isDebugEnabled())
          log.debug("Executing browser entry Update: " + query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }

  /**
   * Updates version of different clients in database.
   */
  private void updateClientVersion(Connection connection, String payload, long timeStamp) throws SQLException {
    List<String> queryList = new ArrayList<>();
    String version;
    String client;
    List<String> entryPrime;
    if (log.isDebugEnabled())
      log.debug("Incoming update payload: " + payload.toString());
    String trimmedPayload = payload.substring(1, payload.length() - 1);
    List<String> jsonPayload = new ArrayList<>(Arrays.asList(trimmedPayload.split(",")));


    for (String entry : jsonPayload) {
      entryPrime = new ArrayList<>(Arrays.asList(entry.split(":")));
      client = entryPrime.get(0).replaceAll("\\\\\"", "");
      version = entryPrime.get(1).replaceAll("\\\\\"|}\"", "");

      queryList.add("UPDATE CLIENT_VERSION SET LAST_VERSION = (select VERSION WHERE NAME = '" + client + "'), LAST_UPDATE=" + timeStamp + ", VERSION = '" + version + "' WHERE NAME = '" + client
          + "'");
    }
    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        statement.addBatch(query);
        if (log.isDebugEnabled())
          log.debug("Executing client version Update: " + query);
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
    String testName = testObject.getString("testName");
    if (!testName.equalsIgnoreCase("SYSTEM_UPDATE Client version update")) {
      String configName = testObject.getString("configName");
      long timeStamp = testObject.getJsonNumber("timeStamp").longValue();
      int tupleSize = testObject.getInt("tupleSize");
      String resultTableName =
          "TN" + testName.trim().replaceAll("[^a-zA-Z0-9]", "_") + "_" + timeStamp;

      JsonArray testCaseBrowserJsonTargetList = jsonObject.getJsonArray("target");
      JsonArray testCaseBrowserJsonDestinationList = jsonObject.getJsonArray("destination");
      List<Browser> testCaseBrowserTargetList = new ArrayList<>();
      List<Browser> testCaseBrowserDestinationList = new ArrayList<>();
      for (int i = 0; i < tupleSize; i++) {
        testCaseBrowserTargetList.add(new Browser((JsonObject) testCaseBrowserJsonTargetList.get(i)));
        testCaseBrowserDestinationList
            .add(new Browser((JsonObject) testCaseBrowserJsonDestinationList.get(i)));
      }

      JsonObject resultObject = jsonObject.getJsonObject("result");


      try {
        connection = this.getDatabaseConnection();
        JsonObject metaObject = jsonObject.getJsonObject("meta");
        if (metaObject != null) {
          int totalTests = metaObject.getInt("totalTests", 0);
          if (totalTests > 0) {
            String description = metaObject.getString("description", "No description was provided fot this test.");
            JsonArray testSuiteBrowserJsonList = (JsonArray) metaObject.get("browsers");
            if (log.isDebugEnabled())
              log.debug("test suite json browser list ->>" + testSuiteBrowserJsonList.toString());

            List<Browser> testSuiteBrowserList = new ArrayList<>();
            for (JsonValue jsonBrowser : testSuiteBrowserJsonList)
              testSuiteBrowserList.add(new Browser((JsonObject) jsonBrowser));

            if (log.isDebugEnabled()) {
              log.debug("test suite browser list ->>"
                  + Arrays.deepToString(testSuiteBrowserList.toArray()));
            }
            List<List<Browser>> testSuiteBrowserPairMatrix =
                buildTuples(testSuiteBrowserList, tupleSize);
            connection.setAutoCommit(false);
            this.putInBrowserTable(connection, testSuiteBrowserList);
            connection.commit();
            this.preliminaryInsert(connection, configName, testName, resultTableName, testObject,
                testSuiteBrowserPairMatrix, totalTests, description);
            connection.commit();
            connection.setAutoCommit(true);
          }
          if (metaObject.get("lastTest") != null) {
            long endTime = System.currentTimeMillis();
            this.updateStatus(connection, configName, testName, resultTableName, timeStamp, endTime);
          }
        }
        //this.putInBrowserTable(connection, testCaseBrowserDestinationList);
        JsonObject jsonPayload = resultObject.getJsonObject("payload");
        this.postResultInsert(connection, resultTableName, testCaseBrowserTargetList,
            testCaseBrowserDestinationList, testName,
            jsonPayload, timeStamp,
            resultObject.getJsonNumber("timeTaken").longValue());
      } catch (SQLException e) {
        e.printStackTrace();

        if (connection != null)
          try {
            connection.rollback();
          } catch (SQLException e1) {
          }
      } finally {
        if (connection != null)
          connection.close();
      }

      // For updating client version purpose only
    } else {
      try {
        connection = this.getDatabaseConnection();
        JsonObject resultObject = jsonObject.getJsonObject("result");
        String payload = resultObject.getJsonObject("payload").toString();
        //String payload = (String) resultObject.getString("payload");
        long timeStamp = testObject.getJsonNumber("timeStamp").longValue();
        this.updateClientVersion(connection, payload, timeStamp);
      } catch (SQLException e) {
        log.error("dumping result", e);
        if (connection != null)
          try {
            connection.rollback();
          } catch (SQLException e1) {
          }
      } finally {
        if (connection != null)
          connection.close();
      }
    }
  }

}
