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
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.Result;

import javax.json.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A class in charged of getting information on results of a test in the database. */
public class ResultDao {

  private static final Log log = LogFactory.getLog(ResultDao.class);

  private Connection connection;

  /**
   * Constructs a new ResultDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public ResultDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Get corresponded stat by id.
   *
   * @param tableName name of the table which contains the results of the test.
   * @param id id of result.
   */
/*  public JsonObject getStatById(String tableName, int id) throws SQLException {

    String query = "SELECT STATS FROM " + tableName + " WHERE ID=" +id;

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
        String stat = rs.getString("STATS");
        if (stat.equalsIgnoreCase("{\"stats\":\"NA\"}")
            || stat.equalsIgnoreCase("{}")) {
          return null;
        } else {
          JsonReader jsonReader = Json.createReader(new StringReader(stat));
          JsonObject statJson = jsonReader.readObject();
          jsonReader.close();
          for (String browser : statJson.keySet()) {
            JsonObject browserJson = statJson.getJsonObject(browser);
            if (browserJson.get("stats") == null) {
              return null;
            }
          }
          return  statJson;
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }*/
  /**
   * Get corresponded stat by browsers.
   *
   * @param tableName name of the table which contains the results of the test.
   * @param browsers browsers of result.
   */
/*  public List<String> getStatByBrowsers(String tableName, String browsers) throws SQLException {

    String query = "SELECT STATS FROM " + tableName + " WHERE BROWSERS=" +browsers;
    List<String> stats= new ArrayList<>();
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
        stats.add(rs.getString("STATS"));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return stats;
  }*/
  /**
   * Get corresponded stat by id.
   *
   * @param tableName name of the table which contains the results of the test.
   */
/*  public String getLogById(String tableName, int id) throws SQLException {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    JsonObject statJson = getStatById(tableName,id);
    if (statJson == null) {
      jsonObjectBuilder.add("message", "No message provided.");
    } else {
      Set<String> browserList = statJson.keySet();
      for (String browser : browserList) {
        JsonObject jsonObject = statJson.getJsonObject(browser);
        JsonObjectBuilder tmp = Json.createObjectBuilder();
        tmp.add("message", jsonObject.getString("message"));
        //tmp.add("log", jsonObject.getJsonArray("log"));
        jsonObjectBuilder.add(browser, tmp);
      }
    }
    return jsonObjectBuilder.build().toString();
  }*/

  public int insertNewResult (String tableName, int configId, int testId, long startTime, long duration, String browsers, int statID, String result) throws SQLException {
    String query =
        "INSERT INTO " + tableName + "(TEST_ID, CONFIG_ID, START_TIME, DURATION, BROWSERS, STAT_ID, RESULT) " +
            "VALUES("+testId+", " + configId + ", " + startTime + ", "+duration
            + ", '" + browsers + "', "+statID+", '"+result + "');";


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

    return getId(tableName, startTime, browsers);
  }

  public int getId (String tableName, long startTime, String browsers ) throws SQLException {
    String query = "SELECT ID FROM " + tableName + " WHERE START_TIME = " +startTime
        + " AND BROWSERS = '" + browsers + "'";

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
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
        return rs.getInt("ID");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return -1;
  }

  /**
   *
   * @param tableName table name
   * @param filter filter by result
   * @param testId test id
   * @return
   * @throws SQLException
   */
  public String getRequestedResultList(String tableName, String filter, int testId)
      throws SQLException {
    JsonArrayBuilder jsonArrayBuilder;
    String query = "SELECT * FROM " + tableName;
    query += " WHERE TEST_ID=" +testId;
    switch (filter) {
      case "all":
        query += "";
        break;
      case "FAILED":
        query += " AND (RESULT='FAILED' OR RESULT='TIME OUT') ";
        break;
      case "ERROR":
        query += " AND RESULT<>'FAILED' AND RESULT<>'TIME OUT' AND RESULT<>'SUCCESSFUL' AND RESULT<>'SCHEDULED'";
        break;
      default:
        query += " AND RESULT = '" + filter + "'";
    }
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      rs = ps.executeQuery();
      jsonArrayBuilder = buildJsonResultList(rs);
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return jsonArrayBuilder.build().toString();
  }

  /**
   *
   * @param tableName table name
   * @param configId configuration id
   * @return
   * @throws SQLException
   */
  public List<Result> getresultByExecutionId(String tableName, int configId) throws SQLException {

    String query = "SELECT * FROM "+ tableName +" WHERE CONFIG_ID="+configId+" ORDER BY START_TIME DESC";
    List<Result> resultTableList = new ArrayList<Result>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) log.debug("Executing: " + query);
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
        resultTableList.add(buildResult(rs));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return resultTableList;

  }

  /**
   *
   * @param tableName table name
   * @param testId test id
   * @return
   * @throws SQLException
   */
  public List<Result> getresultByTestId (String tableName, int testId) throws SQLException {
    String query = "SELECT * FROM "+ tableName +" WHERE TEST_ID="+testId +" ORDER BY START_TIME DESC";
    List<Result> resultTableList = new ArrayList<Result>();

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
        resultTableList.add(buildResult(rs));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return resultTableList;
  }

  /**
   *
   * @param tableName table name
   * @param id result id
   * @return
   * @throws SQLException
   */
  public List<Browser> getBrowsersById(String tableName, int id) throws SQLException {
    List<Browser> browsers = new ArrayList<>();
    String query = "SELECT BROWSERS FROM "+ tableName + " WHERE ID=" +id;
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
        String browserIds = rs.getString("BROWSERS");
        for (String browserId: Arrays.asList(browserIds.substring(1, browserIds.length()-1).split(","))){
          browsers.add(new BrowserDao(connection).getBrowserById(Integer.parseInt(browserId)));
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return browsers;
  }
  /**
   *
   * @param tableName table name
   * @param id result id
   * @return
   * @throws SQLException
   */
  public int getStatIdById(String tableName, int id) throws SQLException {
    String query = "SELECT STAT_ID FROM "+ tableName + " WHERE ID=" +id;
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
        return rs.getInt("STAT_ID");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return 0;
  }

  /**
   *
   * @param tableName table name
   * @param browsers browser Ids list as String
   * @return
   * @throws SQLException
   */
  public String getLatestResultByBrowser(String tableName, String browsers) throws SQLException {
    String query = "SELECT RESULT FROM " + tableName + " WHERE BROWSERS='" + browsers + "' AND RESULT <> 'SCHEDULED' " +
        "ORDER BY START_TIME DESC LIMIT 1";
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
        return rs.getString("RESULT");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }

  /**
   * Build the result object in Json format
   *
   * @param rs
   * @return JsonArrayBuilder
   * @throws SQLException
   */
  private JsonArrayBuilder buildJsonResultList(ResultSet rs) throws SQLException {
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
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
      jsonArrayBuilder.add(buildJsonResult(rs));
    }
    return jsonArrayBuilder;
  }

  /**
   * Build the result object in Json format
   *
   * @param rs
   * @return JsonObjectBuilder
   */
  private JsonObjectBuilder buildJsonResult(ResultSet rs) throws SQLException {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    jsonObjectBuilder
        .add("id", rs.getInt("ID"))
        .add("result", rs.getString("RESULT"))
        .add("duration", rs.getLong("DURATION"));
    if (rs.getObject("STAT_ID") == null) {
      jsonObjectBuilder.add("stats", false);
    } else {
      jsonObjectBuilder.add("stats", true);
    }
    JsonArrayBuilder browsers = Json.createArrayBuilder();
    String browserIds = rs.getString("BROWSERS");
    for (String browserId: Arrays.asList(browserIds.substring(1, browserIds.length()-1).split(","))){
      browsers.add(new BrowserDao(connection).getBrowserById(Integer.parseInt(browserId)).getJsonObjectBuilder());
    }

    jsonObjectBuilder.add("browsers", browsers);
    return jsonObjectBuilder;
  }

  /**
   *
   * @param rs
   * @return Result
   * @throws SQLException
   */
  private Result buildResult (ResultSet rs) throws SQLException {
    Result result;
    if (rs.getObject("STAT_ID") == null) {
      result = new Result(rs.getString("RESULT"), rs.getLong("DURATION"), false);
    } else {
      int stat = rs.getInt("STAT_ID");
      if (stat == 0) {
        result = new Result(rs.getString("RESULT"), rs.getLong("DURATION"), false);
      } else {
        if (new StatsDao(connection).getStatById(stat) == null) {
          result = new Result(rs.getString("RESULT"), rs.getLong("DURATION"), false);
        } else {
          result = new Result(rs.getString("RESULT"), rs.getLong("DURATION"), true);
        }
      }
    }
    String browserIds = rs.getString("BROWSERS");
    for (String browserId: Arrays.asList(browserIds.substring(1, browserIds.length()-1).split(","))){
      result.addBrowser(new BrowserDao(connection).getBrowserById(Integer.parseInt(browserId)));
    }
    result.setId(rs.getInt("ID"));
    result.setStartTime(rs.getLong("START_TIME"));
    return result;
  }
}
