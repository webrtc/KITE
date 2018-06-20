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
import org.webrtc.kite.pojo.ConfigurationOverall;
import org.webrtc.kite.pojo.Execution;
import org.webrtc.kite.pojo.Result;
import org.webrtc.kite.pojo.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A class in charged of getting information on executed configurations in the database. */
public class ExecutionDao {

  private static final Log log = LogFactory.getLog(ExecutionDao.class);

  private Connection connection;

  /**
   * Constructs a new ExecutionDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public ExecutionDao(Connection connection) {
    this.connection = connection;
  }


  /**
   *
   * @param configName name of the new configuration.
   * @param startTime start time
   * @return
   * @throws SQLException
   */
  public int insertNewConfig (String configName, long startTime) throws SQLException {

    String query =
        "INSERT INTO CONFIG_EXECUTION(CONFIG_NAME, START_TIME, TEST_COUNT) SELECT '"
            + configName
            + "',"
            + startTime
            + ", 0 " +
            " WHERE NOT EXISTS (SELECT 1 FROM CONFIG_EXECUTION WHERE START_TIME = "
            + startTime
            + ");";
    Statement statement = null;
    try {
      statement = connection.createStatement();
      if (log.isDebugEnabled()){
        log.debug("Executing Preliminary Insert:" + query);
      }
      statement.addBatch(query);
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }

    return getId(configName,startTime);
  }

/*
  *//** Returns the list of all executed configurations, non-repetitively. *//*
  public List<ConfigurationOverall> getDistinctConfigExecutionList() throws SQLException {
    String query =
        "SELECT CONFIG_ID,CONFIG_NAME FROM CONFIG_EXECUTION GROUP BY CONFIG_NAME ORDER BY START_TIME DESC ;";

    List<ConfigurationOverall> configurationOverallList = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        String name = rs.getString("CONFIG_NAME");
        List<Long> listOfStartTime = getConfigurationStartTimeList(name);
        List<String> listOfResulTableName = new ArrayList<>();
        ConfigurationOverall tmp;
        if (listOfStartTime.size() > 1)
          tmp =new ConfigurationOverall(
                  name, listOfStartTime.get(listOfStartTime.size() - 1), listOfStartTime.get(0));
        else tmp = new ConfigurationOverall(name, listOfStartTime.get(0), listOfStartTime.get(0));
        tmp.setStartTimeList(listOfStartTime);
        tmp.setIdList(getIdList(name));
        for (long startTime : listOfStartTime) {
          listOfResulTableName.add(getResultTableNameFromStartTime(startTime));
        }
        for (String resultTable : listOfResulTableName) {
          tmp.setNumberOfRuns(tmp.getNumberOfRuns() + 1);
          updateIntoOverall(tmp, resultTable);
        }
        configurationOverallList.add(tmp);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return configurationOverallList;
  }*/


  /** Returns the list of all executed configurations, non-repetitively. */
  public List<ConfigurationOverall> getDistinctConfigExecutionList() throws SQLException {
    String query =
        "SELECT DISTINCT CONFIG_NAME FROM CONFIG_EXECUTION WHERE CONFIG_NAME NOT LIKE 'WPT%' ORDER BY START_TIME DESC;";
    List<String> configNames = new ArrayList<>();
    List<ConfigurationOverall> configurationOverallList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        // TODO
        String configName = rs.getString("CONFIG_NAME");
        configNames.add(configName);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    for (String configName: configNames){
      ConfigurationOverall configurationOverall = new ConfigurationOverall(configName);
      List<Execution> configs = getConfigExecutionList(configName);
      configurationOverall.setNumberOfRuns(configs.size());
      List<Integer> configIds = new ArrayList<>();
      for (Execution config: configs){
        int configId = config.getConfigId();
        configIds.add(configId);
      }
      configurationOverall.setIdList(configIds);
      configurationOverall.setLatestRun(configs.get(0).getStartTime());
      configurationOverall.setOldestRun(configs.get(configs.size()-1).getStartTime());
      configurationOverallList.add(configurationOverall);
    }


    return configurationOverallList;
  }

  public Execution getExecutionById (int id) throws SQLException {
    String query = "SELECT * FROM CONFIG_EXECUTION WHERE CONFIG_ID="+id;

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        return buildExecution(rs);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }

  private String getResultTableNameFromStartTime(long startTime) throws SQLException {
    String query =
        "SELECT tbl_name FROM sqlite_master WHERE type='table' AND name LIKE '%" + startTime + "%'";

    PreparedStatement ps = null;
    ResultSet rs = null;
    String res = "";
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        res = rs.getString("tbl_name");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return res;
  }
/*
  private void updateIntoOverall(ConfigurationOverall overall, String tableName)
      throws SQLException {
    String query = "SELECT RESULT FROM " + tableName;

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        overall.setNumberOfTested(overall.getNumberOfTested() + 1);
        switch (rs.getString("RESULT")) {
          case "SCHEDULED":
            overall.setNumberOfPending(overall.getNumberOfPending() + 1);
            break;
          case "SUCCESSFUL":
            overall.setNumberOfSuccess(overall.getNumberOfSuccess() + 1);
            break;
          case "TIME OUT":
            overall.setNumberOfFailed(overall.getNumberOfFailed() + 1);
            break;
          case "FAILED":
            overall.setNumberOfFailed(overall.getNumberOfFailed() + 1);
            break;
          default:
            overall.setNumberOfError(overall.getNumberOfError() + 1);
            break;
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
  }*/

  /**
   * Returns a list of all start time of an executed configuration, ordered from latest to oldest.
   *
   * @param configName name of the configuration we want.
   */
  private List<Long> getConfigurationStartTimeList(String configName) throws SQLException {
    // velocity template expects ascending order
    String query =
        "SELECT START_TIME FROM CONFIG_EXECUTION WHERE CONFIG_NAME = ? ORDER BY START_TIME ASC";

    List<Long> configConfigurationStartTimeList = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      ps.setString(1, configName);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        configConfigurationStartTimeList.add(rs.getLong("START_TIME"));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return configConfigurationStartTimeList;
  }

  /**
   * Returns a list of all id of an executed configuration, ordered from latest to oldest.
   *
   * @param configName name of the configuration we want.
   */
  private List<Integer> getIdList(String configName) throws SQLException {
    String query =
        "SELECT CONFIG_ID FROM CONFIG_EXECUTION WHERE CONFIG_NAME = ? ORDER BY START_TIME ASC";

    List<Integer> idList = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      ps.setString(1, configName);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        idList.add(rs.getInt("CONFIG_ID"));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return idList;
  }

  /**
   * Returns a list of all runs of an executed configuration, ordered from latest to oldest.
   *
   * @param configName name of the configuration we want.
   */
  public List<Execution> getConfigExecutionList(String configName) throws SQLException {
    String query = "SELECT * FROM CONFIG_EXECUTION WHERE CONFIG_NAME = ? ORDER BY START_TIME DESC";

    List<Execution> executionList = new ArrayList<Execution>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      ps.setString(1, configName);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        Execution tmp = buildExecution(rs);
        tmp.setTestList(new TestDao(connection).getTestListByExecutionId(tmp.getConfigId()));
        executionList.add(tmp);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return executionList;
  }

  /**
   * Returns a list of all runs of an executed configuration in search, ordered from latest to
   * oldest.
   *
   * @param configName name of the configuration we search for.
   */
  public List<Execution> searchConfigExecutionList(String configName) throws SQLException {
    String query =
        "SELECT * FROM CONFIG_EXECUTION WHERE CONFIG_NAME LIKE '%"
            + configName
            + "%' AND CONFIG_NAME NOT LIKE 'WPT%' ORDER BY START_TIME DESC";

    List<Execution> executionList = new ArrayList<Execution>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        executionList.add(buildExecution(rs));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return executionList;
  }

  /**
   *
   * @param configName configuration name
   * @param startTime configuration start time
   * @return configuration id int
   */
  public int getId(String configName, long startTime) throws SQLException {
    String query = "SELECT CONFIG_ID FROM CONFIG_EXECUTION WHERE CONFIG_NAME='"+configName+"' " +
        "AND START_TIME=" +startTime;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) {
        log.debug("Executing: " + query);
      }
      rs = ps.executeQuery();
      while (rs.next()) {
        int id = rs.getInt("CONFIG_ID");
        return id;
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return 0;
  }

  public String getTestArray (int configId) throws SQLException {
    String query = "SELECT TESTS FROM CONFIG_EXECUTION WHERE CONFIG_ID="+ configId;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) {
        log.debug("Executing: " + query);
      }
      rs = ps.executeQuery();
      while (rs.next()) {
        String tests = rs.getString("TESTS");
        if (tests != null) {
          return tests;
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return "[]";
  }

  public void updateTestArrayAndCount (int configId, String testIds) throws SQLException {
    String query = "UPDATE CONFIG_EXECUTION SET TEST_COUNT=TEST_COUNT+1, TESTS='" +testIds+"' " +
        "WHERE CONFIG_ID="+ configId;

    Statement statement = null;
    try {
      statement = connection.createStatement();
      if (log.isDebugEnabled()){
        log.debug("Executing Preliminary Insert:" + query);
      }
      statement.addBatch(query);
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }

  private Execution buildExecution(ResultSet rs) throws SQLException {
    int id = rs.getInt("CONFIG_ID");
    String name = rs.getString("CONFIG_NAME");
    long timestamp = rs.getLong("START_TIME");
    int testCount = rs.getInt("TEST_COUNT");
    Execution tmp = new Execution(id, name, timestamp, testCount);
    if (rs.getString("STATUS") != null) {
      tmp.setEndTime(rs.getLong("END_TIME"));
      tmp.setDone(true);
    }
    return tmp;
  }
}
