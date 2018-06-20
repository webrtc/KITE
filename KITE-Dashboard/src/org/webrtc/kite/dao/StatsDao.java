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
import javax.json.stream.JsonParsingException;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A class in charged of getting information on results of a test in the database. */
public class StatsDao {

  private static final Log log = LogFactory.getLog(StatsDao.class);

  private Connection connection;

  /**
   * Constructs a new ResultDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public StatsDao(Connection connection) {
    this.connection = connection;
  }

  public int insertNewStat (int resultID, int testId, String browsers, String stat) throws SQLException {
    String query = "INSERT INTO STATS (RESULT_ID, TEST_ID, BROWSERS, STATS) VALUES (" +
        +resultID +", "+ testId + ", '" +browsers +"', '"+ stat+ "')";
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
    return getId(testId,browsers);
  }

  public int getId (int testId, String browsers) throws SQLException {
    String query = "SELECT ID FROM STATS WHERE TEST_ID=" +testId + " AND BROWSERS ='" +browsers+"'";

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
   * Get corresponded stat by id.
   *
   * @param id id of stats.
   */
  public JsonObject getStatById(int id) throws SQLException {

    String query = "SELECT STATS FROM STATS WHERE ID=" +id;

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
        try {
          JsonReader jsonReader = Json.createReader(new StringReader(stat));
          JsonObject statJson = jsonReader.readObject();
          jsonReader.close();
          boolean containBrowser = false;
          for (String key : statJson.keySet()) {
            if (key.contains("_")) {
              JsonObject browserJson = statJson.getJsonObject(key);
              if (browserJson.get("stats") == null) {
                return null;
              }
              containBrowser = true;
            }
          }
          if (containBrowser){
            return  statJson;
          } else {
            return null;
          }
        } catch (Exception e) {
          log.error("Exception while getting stats from db", e);
          return null;
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }
  /**
   * Get corresponded stat by id.
   *
   * @param id id of stats.
   */
  public JsonObject getLogById(int id) throws SQLException {

    String query = "SELECT STATS FROM STATS WHERE ID=" +id;
    JsonObjectBuilder logObject = Json.createObjectBuilder();
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
        try {
          JsonReader jsonReader = Json.createReader(new StringReader(stat));
          JsonObject statJson = jsonReader.readObject();
          jsonReader.close();
          boolean containBrowser = false;
          for (String key : statJson.keySet()) {
            if (key.contains("_")) {
              JsonObject browserJson = statJson.getJsonObject(key);
              if (browserJson.get("logs") == null) {
                logObject.add(key, browserJson.getString("log"));
              }
              containBrowser = true;
            }
          }
          if (containBrowser){
            return  logObject.build();
          } else {
            return null;
          }
        } catch (Exception e) {
          log.error("Exception while getting stats from db", e);
          return null;
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }

  /**
   * Get corresponded stat by browsers.
   *
   * @param browsers browsers of stats.
   */
  public List<String> getStatByBrowsers(String browsers) throws SQLException {

    String query = "SELECT STATS FROM STATS WHERE BROWSERS=" +browsers;
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
  }

}
