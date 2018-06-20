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

package org.webrtc.kite.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.Utility;
import org.webrtc.kite.pojo.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A class in charged of getting information on executed tests in the database. */
public class TestDao {

  private static final Log log = LogFactory.getLog(TestDao.class);

  private Connection connection;

  /**
   * Constructs a new TestDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public TestDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Returns a list of all the test included in a configuration.
   *
   * @param configId the Id of the configuration.
   */
  public List<Test> getTestListByExecutionId(int configId) throws SQLException {
    String query = "SELECT * FROM TESTS WHERE CONFIG_ID = ?";

    List<Test> testList = new ArrayList<Test>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      ps.setInt(1, configId);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        Test test = buildTest(rs);
        testList.add(test);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return testList;
  }

  /**
   * Returns a list of all the test with a specific name.
   *
   * @param testName name of the test.
   */
  public List<Test> getTestListByTestName(String testName) throws SQLException {
    String query =
        "SELECT * FROM TESTS WHERE TEST_NAME LIKE '%" + testName + "%' AND  TEST_NAME NOT LIKE 'WPT%' ORDER BY START_TIME DESC";

    List<Test> testList = new ArrayList<Test>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        Test test = buildTest(rs);
        testList.add(test);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return testList;
  }

  /**
   * Returns a list of all the test with a specific name.
   *
   * @param testName name of the test.
   */
  public int getTupleSizeTestName(String testName) throws SQLException {
    String query =
        "SELECT TUPLE_SIZE FROM TESTS WHERE TEST_NAME ='"+testName+"'";


    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        return rs.getInt("TUPLE_SIZE");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return -1;
  }

  /**
   * Returns a list of all the tests that are not WPT.
   *
   */
  public List<String> getNonWPTList() throws SQLException {
    String query ="SELECT DISTINCT TEST_NAME FROM TESTS WHERE TEST_NAME NOT LIKE 'WPT%'";
    List<String> testList = new ArrayList<String>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        testList.add(rs.getString("TEST_NAME"));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return testList;
  }

  /** Returns a list of all the executed tests, non-repetitively. */
  public List<Test> getDistinctTestList() throws SQLException {
    String query =
        "SELECT DISTINCT TEST_NAME,TUPLE_SIZE,DESCRIPTION FROM TESTS  WHERE TEST_NAME NOT LIKE 'WPT%' ORDER BY TEST_NAME DESC, DESCRIPTION DESC";

    List<Test> resultTestList = new ArrayList<>();
    List<String> testNameList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        Test tmp = new Test();
        tmp.setTestName(rs.getString("TEST_NAME"));
        tmp.setTupleSize(rs.getInt("TUPLE_SIZE"));
        tmp.setDescription(rs.getString("DESCRIPTION"));
        if (!testNameList.contains(tmp.getTestName())) {
          testNameList.add(tmp.getTestName());
          resultTestList.add(tmp);
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return resultTestList;
  }

  /** Returns a list of all the executed tests, ALL of them. */
  public List<Test> getTestList() throws SQLException {
    String query = "SELECT * FROM TESTS WHERE TEST_NAME NOT LIKE 'WPT%' ORDER BY START_TIME DESC";

    List<Test> resultTestList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) {
        log.debug("Executing: " + query);
      }
      rs = ps.executeQuery();
      while (rs.next()) {
        Test test = buildTest(rs);
        resultTestList.add(test);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return resultTestList;
  }

  /** Returns a list of all the executed 1v1 tests, non-repetitively. */
  public List<String> get1v1TestList() throws SQLException {
    String query =
        "SELECT DISTINCT TEST_NAME FROM TESTS WHERE TUPLE_SIZE=2 AND TEST_NAME NOT LIKE 'WPT%' ORDER BY TEST_NAME DESC";

    List<String> resultTestList = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        resultTestList.add(rs.getString(1));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return resultTestList;
  }

  /** Returns a Test object of a test with specific ID. */
  public Test getTestById(int id) throws SQLException {
    Test test = new Test();
    String query = "SELECT * FROM TESTS WHERE TEST_ID=" + id + ";";
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        test = buildTest(rs);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return test;
  }

  /**
   * Returns a list of all the result tables of a test in the DB.
   *
   * @param testName the name of the test in question.
   */
  public List<Long> getRunList(String testName) throws SQLException {
    String query =
            "SELECT START_TIME FROM TESTS WHERE TEST_NAME='"+testName+"' ORDER BY START_TIME DESC";
    List<Long> runList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) {
        log.debug("Executing: " + query);
      }
      rs = ps.executeQuery();
      while (rs.next()) {
        if (log.isTraceEnabled()) {
          final StringBuilder rsLog = new StringBuilder();
          for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
            rsLog
                    .append(rs.getMetaData().getColumnName(c))
                    .append(":")
                    .append(rs.getString(c))
                    .append("-");
          }
          log.trace(rsLog.toString());
        }
        runList.add(Long.parseLong(rs.getString("START_TIME")));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return runList;
  }

  /**
   * Returns the Test object of a specific test
   *
   * @param testName the name of the test in question.
   * @param startTime the start Time of the test in question.
   */
  public Test getTestByNameAndStartTime(String testName, long startTime) throws SQLException {
    Test test = new Test();
    String query = "SELECT * FROM TESTS WHERE TEST_NAME='" + testName + "' AND START_TIME=" +startTime;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing get description query: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        if (log.isTraceEnabled()) {
          final StringBuilder rsLog = new StringBuilder();
          for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
            rsLog
                .append(rs.getMetaData().getColumnName(c))
                .append(":")
                .append(rs.getString(c))
                .append("-");
          }
          log.trace(rsLog.toString());
        }
        test = buildTest(rs);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return test;
  }

  /**
   *
   * @param testName test name
   * @param startTime test start time
   * @return test id int
   */
  public int getId (String testName, long startTime) throws SQLException {
    String query = "SELECT TEST_ID FROM TESTS WHERE TEST_NAME='"+testName+"' " +
        "AND START_TIME=" +startTime;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        int id = rs.getInt("TEST_ID");
        return id;
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return 0;
  }

  private Test buildTest(ResultSet rs) throws SQLException {
    Test test = new Test();
    test.setTestId(rs.getInt("TEST_ID"));
    test.setStartTime(rs.getLong("START_TIME"));
    test.setEndTime(0);
    test.setTestName(rs.getString("TEST_NAME"));
    test.setImpl(rs.getString("IMPL"));
    test.setTupleSize(rs.getInt("TUPLE_SIZE"));
    test.setResultTable(rs.getString("RESULT_TABLE"));
/*    List<Integer> stats =
        new ResultDao(connection).getResultStatistic(rs.getString("RESULT_TABLE"));
    test.setStats(stats);
    int numberOfFinishedCases = stats.get(0) + stats.get(1) + stats.get(2);
    test.setDoneTests(numberOfFinishedCases);*/
    test.setTotalTests(rs.getInt("TOTAL_TESTS"));
    test.setConfigId(rs.getInt("CONFIG_ID"));
    if (rs.getString("STATUS") == null) test.setStatus(false);
    else {
      test.setStatus(true);
      test.setEndTime(rs.getLong("END_TIME"));
    }
    if (rs.getString("DESCRIPTION") != null) {
      test.setDescription(rs.getString("DESCRIPTION"));
    } else {
      test.setDescription("No description was provided fot this test.");
    }
    return test;
  }


  public int insertNewTest(JsonObject testJsonObject, long startTime, String description, int totalTest, int configId)
      throws SQLException {
    String query = "";
    if (testJsonObject.get("subTests") != null){
      // TODO
    } else {
      query = "INSERT INTO TESTS(START_TIME,TEST_NAME, IMPL, TUPLE_SIZE, RESULT_TABLE, TOTAL_TESTS, CONFIG_ID, DESCRIPTION) "
          + "VALUES("
          + startTime
          + ",'"
          + testJsonObject.getString("testName")
          + "','"
          + testJsonObject.getString("testImpl")
          + "',"
          + testJsonObject.getInt("tupleSize")
          + ",'"
          + "NONE"
          + "',"
          + totalTest
          + ","
          + configId
          + ",'"
          + description
          + "');";
    }

    Statement statement = null;
    try {
      statement = connection.createStatement();
      if (log.isDebugEnabled()){
        log.debug("Executing Preliminary Insert:" + query);
      }
      System.out.println("Executing: " + query);
      statement.addBatch(query);
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }

    System.out.println("test name is: " + testJsonObject.getString("testName"));
    int id = getId(testJsonObject.getString("testName"), startTime);
    String testArray = new ExecutionDao(connection).getTestArray(configId);
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
    if (testArray != "[]") {
      testArray = testArray.substring(1, testArray.length()-1);
      List <String> testIds = Arrays.asList(testArray.split(","));
      for (String testId : testIds) {
        jsonArrayBuilder.add(Integer.parseInt(testId));
      }
    }
    jsonArrayBuilder.add(id);
    new ExecutionDao(connection).updateTestArrayAndCount(configId, jsonArrayBuilder.build().toString());
    return id;
  }

}
