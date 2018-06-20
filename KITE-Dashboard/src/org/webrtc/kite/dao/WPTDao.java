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
import org.webrtc.kite.BrowserMapping;
import org.webrtc.kite.Utility;
import org.webrtc.kite.pojo.Browser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** A class in charged of all data gesture concerning the BROWSERS table in database. */
public class WPTDao {

  private static final Log log = LogFactory.getLog(WPTDao.class);

  private Connection connection;

  /**
   * Constructs a new BrowserDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public WPTDao(Connection connection) {
    this.connection = connection;
  }

  public List<String> getWPTList () throws SQLException {
    List<String> list = new ArrayList<>();
    String query = " SELECT tbl_name FROM sqlite_master WHERE type='table' AND name LIKE 'WPT%' ";
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
        list.add(rs.getString("tbl_name"));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return list;
  }

  public JsonObject getLatestTestResultByBrowser(String tableName ,String browsers) throws SQLException {
    String resultString = new ResultDao(connection).getLatestResultByBrowser(tableName, browsers);
    if (resultString != null) {
      InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
      try {
        JsonReader reader = Json.createReader(stream);
        JsonObject resultObject = reader.readObject();
        return resultObject;
      } catch (JsonParsingException e){
        //System.out.println(resultString);
        return null;
      }
    } else {
      return null;
    }
  }

  public String testExist(String testName) throws SQLException {
    String query = " SELECT tbl_name FROM sqlite_master WHERE type='table' AND name= '"+testName+"' ";
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
        return rs.getString("tbl_name");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    testName = "WPT_" + testName.replaceAll("\\.", "_");
    query = " SELECT tbl_name FROM sqlite_master WHERE type='table' AND name= '"+testName+"' ";
    ps = null;
    rs = null;
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
        return rs.getString("tbl_name");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return null;
  }
}
