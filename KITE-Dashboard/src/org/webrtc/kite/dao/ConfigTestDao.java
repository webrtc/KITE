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
import org.webrtc.kite.pojo.ConfigTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class in charged of getting information on executed tests in the database.
 */
public class ConfigTestDao {

  private static final Log log = LogFactory.getLog(ConfigTestDao.class);

  private Connection connection;

  /**
   * Constructs a new ConfigTestDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public ConfigTestDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Returns a list of all the test included in a configuration.
   *
   * @param configId the Id of the configuration.
   */
  public List<ConfigTest> getConfigTestList(int configId) throws SQLException {
    String query = "SELECT * FROM TESTS WHERE CONFIG_ID = ?";

    List<ConfigTest> configTestList = new ArrayList<ConfigTest>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      ps.setInt(1, configId);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        ConfigTest configTest = new ConfigTest();
        configTest.setTestId(rs.getInt("TEST_ID"));
        configTest.setStartTime(rs.getLong("START_TIME"));
        configTest.setEndTime(0);
        configTest.setTestName(rs.getString("TEST_NAME"));
        configTest.setImpl(rs.getString("IMPL"));
        configTest.setTupleSize(rs.getInt("TUPLE_SIZE"));
        configTest.setResultTable(rs.getString("RESULT_TABLE"));
        List<Integer> stats = getStats(rs.getString("RESULT_TABLE"));
        configTest.setStats(stats);
        int numberOfFinishedCases = stats.get(0) + stats.get(1) + stats.get(2);
        configTest.setDoneTests(numberOfFinishedCases);
        configTest.setTotalTests(rs.getInt("TOTAL_TESTS"));
        configTest.setConfigId(rs.getInt("CONFIG_ID"));
        if (rs.getString("STATUS") == null)
          configTest.setStatus(false);
        else {
          configTest.setStatus(true);
          configTest.setEndTime(rs.getLong(3));
        }
        if (rs.getString("DESCRIPTION") != null)
          configTest.setDescription(rs.getString("DESCRIPTION"));
        else
          configTest.setDescription("No description was provided fot this test.");
        configTestList.add(configTest);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return configTestList;
  }

  /**
   * Returns a list of all the test with a specific name.
   *
   * @param testName name of the test.
   */
  public List<ConfigTest> getConfigTestList(String testName) throws SQLException {
    String query = "SELECT * FROM TESTS WHERE TEST_NAME LIKE '%" + testName + "%' ORDER BY START_TIME DESC";

    List<ConfigTest> configTestList = new ArrayList<ConfigTest>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        ConfigTest configTest = new ConfigTest();
        configTest.setTestId(rs.getInt("TEST_ID"));
        configTest.setStartTime(rs.getLong("START_TIME"));
        configTest.setEndTime(0);
        configTest.setTestName(rs.getString("TEST_NAME"));
        configTest.setImpl(rs.getString("IMPL"));
        configTest.setTupleSize(rs.getInt("TUPLE_SIZE"));
        configTest.setResultTable(rs.getString("RESULT_TABLE"));
        List<Integer> stats = getStats(rs.getString("RESULT_TABLE"));
        configTest.setStats(stats);
        int numberOfFinishedCases = stats.get(0) + stats.get(1) + stats.get(2);
        configTest.setDoneTests(numberOfFinishedCases);
        configTest.setTotalTests(rs.getInt("TOTAL_TESTS"));
        configTest.setConfigId(rs.getInt("CONFIG_ID"));
        if (rs.getString("STATUS") == null)
          configTest.setStatus(false);
        else {
          configTest.setStatus(true);
          configTest.setEndTime(rs.getLong(3));
        }
        if (rs.getString("DESCRIPTION") != null)
          configTest.setDescription(rs.getString("DESCRIPTION"));
        else
          configTest.setDescription("No description was provided fot this test.");
        configTestList.add(configTest);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return configTestList;
  }

  /**
   * Returns a list of all the test with a specific name.
   *
   * @param testName name of the test.
   */
  public List<String> getResultTableList(String testName) throws SQLException {
    String query = "SELECT RESULT_TABLE FROM TESTS WHERE TEST_NAME='" + testName + "' ORDER BY START_TIME DESC";
    List<String> resultTableList = new ArrayList<String>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        resultTableList.add(rs.getString("RESULT_TABLE"));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return resultTableList;
  }

  /**
   * Returns a list of all the executed tests, non-repetitively.
   */
  public List<ConfigTest> getTestList() throws SQLException {
    String query = "SELECT DISTINCT TEST_NAME,TUPLE_SIZE,DESCRIPTION FROM TESTS ORDER BY TEST_NAME DESC, DESCRIPTION DESC";

    List<ConfigTest> resultTestList = new ArrayList<>();
    List<String> testNameList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        ConfigTest tmp = new ConfigTest();
        tmp.setTestName(rs.getString(1));
        tmp.setTupleSize(rs.getInt(2));
        tmp.setDescription(rs.getString(3));
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

  /**
   * Returns a list of all the executed 1v1 tests, non-repetitively.
   */
  public List<String> get1v1TestList() throws SQLException {
    String query = "SELECT DISTINCT TEST_NAME FROM TESTS WHERE TUPLE_SIZE=2 ORDER BY TEST_NAME DESC";

    List<String> resultTestList = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        resultTestList.add(rs.getString(1));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return resultTestList;
  }

  /**
   * Returns a ConfigTest object of a test with specific ID.
   */
  public ConfigTest getTestById(int id) throws SQLException {
    String query = "SELECT * FROM TESTS WHERE TEST_ID=" + id + ";";
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        ConfigTest configTest = new ConfigTest();
        configTest.setTestId(rs.getInt("TEST_ID"));
        configTest.setStartTime(rs.getLong("START_TIME"));
        configTest.setEndTime(0);
        configTest.setTestName(rs.getString("TEST_NAME"));
        configTest.setImpl(rs.getString("IMPL"));
        configTest.setTupleSize(rs.getInt("TUPLE_SIZE"));
        configTest.setResultTable(rs.getString("RESULT_TABLE"));
        List<Integer> stats = getStats(rs.getString("RESULT_TABLE"));
        configTest.setStats(stats);
        int numberOfFinishedCases = stats.get(0) + stats.get(1) + stats.get(2);
        configTest.setDoneTests(numberOfFinishedCases);
        configTest.setTotalTests(rs.getInt("TOTAL_TESTS"));
        configTest.setConfigId(rs.getInt("CONFIG_ID"));
        if (rs.getString("STATUS") == null)
          configTest.setStatus(false);
        else {
          configTest.setStatus(true);
          configTest.setEndTime(rs.getLong(3));
        }
        if (rs.getString("DESCRIPTION") != null)
          configTest.setDescription(rs.getString("DESCRIPTION"));
        else
          configTest.setDescription("No description was provided fot this test.");
        return configTest;
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }

  /**
   * Returns the stats on test cases in a test.
   *
   * @param tableName name of the result table in the database.
   */
  private List<Integer> getStats(String tableName) throws SQLException {
    String query = "SELECT RESULT FROM " + tableName + ";";
    List<Integer> stats = new ArrayList<>();
    int success = 0, failed = 0, error = 0, pending = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        String res = rs.getString("RESULT");
        switch (res) {
          case "SUCCESSFUL":
            success += 1;
            break;
          case "FAILED":
            failed += 1;
            break;
          case "TIME OUT":
            failed += 1;
            break;
          case "SCHEDULED":
            pending += 1;
            break;
          default:
            error += 1;
        }
      }
      stats.add(success);
      stats.add(failed);
      stats.add(error);
      stats.add(pending);
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return stats;
  }
}
