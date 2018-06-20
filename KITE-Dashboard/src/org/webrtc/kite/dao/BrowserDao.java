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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** A class in charged of all data gesture concerning the BROWSERS table in database. */
public class BrowserDao {

  private static final Log log = LogFactory.getLog(BrowserDao.class);

  private Connection connection;

  /**
   * Constructs a new BrowserDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public BrowserDao(Connection connection) {
    this.connection = connection;
  }


  /**
   * Updates the status for configuration & test when they're done.
   *
   * @param browser browser to put in the BROWSERS Table if not already exist
   */
  public void insertNewBrowser(Browser browser)
      throws SQLException {
    List<String> queryList = new ArrayList<>();
    if (new BrowserDao(connection).getId(browser)==-1)
      queryList.add(
          "INSERT INTO BROWSERS(NAME, VERSION, PLATFORM) " +
              "SELECT '"+browser.getName()+"', " +
              "'"+browser.getVersion()+"', " +
              "'"+browser.getPlatform().toUpperCase()+"' " +
              "WHERE NOT EXISTS(" +
              "SELECT 1 FROM BROWSERS WHERE " +
              "NAME='"+ browser.getName()
              + "' AND VERSION='" + browser.getVersion()
              + "' AND PLATFORM='" + browser.getPlatform().toUpperCase()
              + "' )");

    Statement statement = null;
    try {
      statement = connection.createStatement();
      for (String query : queryList) {
        statement.addBatch(query);
        if (log.isDebugEnabled()) {
          log.debug("Executing browser entry Update: " + query);
        }
        System.out.println("Executing browser entry Update: " + query);
      }
      statement.executeBatch();
    } finally {
      Utility.closeDBResources(statement, null);
    }
  }

  /**
   * Returns the Id of a browser previously registered.
   *
   * @param browser a Browser object which contains information as name, version and platform.
   */
  public int getId(Browser browser) throws SQLException {
    String query =
        "SELECT BROWSER_ID FROM BROWSERS WHERE NAME='"
            + browser.getName()
            + "' AND VERSION='"
            + browser.getVersion()
            + "' AND PLATFORM='"
            + browser.getPlatform().toUpperCase()
            + "' ORDER BY BROWSER_ID LIMIT 1";


    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()){
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
        return rs.getInt("BROWSER_ID");
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return -1;
  }

  /**
   * Returns the browser with a specific ID.
   *
   * @param id the browser ID in question.
   */
  public Browser getBrowserById(int id) throws SQLException {
    String query = "SELECT * FROM BROWSERS WHERE BROWSER_ID=" + id + ";";

    PreparedStatement ps = null;
    ResultSet rs = null;
    Browser browser = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) {
        log.debug("Executing: " + query);
      }
      rs = ps.executeQuery();
      if (rs.next()) {
        browser = buildBrowser(rs);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    if (log.isDebugEnabled()) {
      log.debug("Returning: " + browser.toString());
    }
    return browser;
  }

  /** Returns a list of all registered browsers */
  public List<Browser> getBrowserList() throws SQLException {
    String query = "SELECT * FROM BROWSERS ORDER BY NAME DESC, VERSION DESC, PLATFORM DESC;";

    List<Browser> listOfBrowsers = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled()) {
        log.debug("Executing: " + query);
      }
      rs = ps.executeQuery();
      while (rs.next()) {
        if (log.isDebugEnabled()) {
          final StringBuilder rsLog = new StringBuilder();
          for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
            rsLog
                .append(rs.getMetaData().getColumnName(c))
                .append(":")
                .append(rs.getString(c))
                .append("-");
          }
          log.debug(rsLog.toString());
        }
        Browser browser= buildBrowser(rs);
        if (!BrowserMapping.IrrelevantList.contains(browser.getVersion()) && !BrowserMapping.IrrelevantList.contains(browser.getPlatform().toUpperCase()))
          listOfBrowsers.add(browser);
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return listOfBrowsers;
  }

  /**
   * Builds browser object from result set
   *
   * @param rs result set
   * @return Browser object
   * @throws SQLException
   */
  private Browser buildBrowser(ResultSet rs) throws SQLException {
    int id = rs.getInt("BROWSER_ID");
    String name = rs.getString("NAME");
    String version = rs.getString("VERSION");
    String longVersion = rs.getString("LONG_VERSION");
    String platform = rs.getString("PLATFORM");
    Browser browser = new Browser(name, version, platform);
    browser.setId(id);
    browser.setLongVersion(longVersion);
    return browser;
  }
}
