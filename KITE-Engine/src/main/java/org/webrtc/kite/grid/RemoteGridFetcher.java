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

package org.webrtc.kite.grid;

import org.apache.log4j.Logger;
import org.webrtc.kite.Utility;
import org.webrtc.kite.config.Browser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Parent class dealing with remote API communication.
 */
public abstract class RemoteGridFetcher implements Callable<Object> {

  private static final Logger logger = Logger.getLogger(RemoteGridFetcher.class.getName());

  private static final List<String> OS_X_CODENAME = Arrays
      .asList("SNOW_LEOPARD", "LION", "MOUNTAIN_LION", "MAVERICKS", "YOSEMITE", "EL_CAPITAN",
          "SIERRA");

  private String pathToDB;
  private String tableName;
  private String remoteAddress;

  /**
   * The Rest api url.
   */
  protected String restApiUrl;
  /**
   * The Browser list.
   */
  protected List<Browser> browserList = new ArrayList<Browser>();

  /**
   * Constructs a new RemoteGridFetcher with the given pathToDB, tableName, remoteAddress and
   * restApiUrl.
   *
   * @param pathToDB      path to db
   * @param tableName     name of the table
   * @param remoteAddress string representation of the Selenium hub url.
   * @param restApiUrl    string representation of the rest API url for fetching the supported
   *                      browsers.
   */
  public RemoteGridFetcher(String pathToDB, String tableName, String remoteAddress,
      String restApiUrl) {
    this.pathToDB = pathToDB;
    this.tableName = tableName;
    this.remoteAddress = remoteAddress;

    this.restApiUrl = restApiUrl;
  }

  /**
   * Gets remote address.
   *
   * @return the remote address
   */
  public String getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * Returns a database connection.
   *
   * @return Connection
   * @throws SQLException if a database access error occurs or the url is null.
   */
  private Connection getDatabaseConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
  }

  /**
   * Creates the table if it doesn't already exist.
   *
   * @param c Connection
   * @throws SQLException if a database access error occurs.
   */
  private void createTableIfNotExists(Connection c) throws SQLException {
    String sql =
        "CREATE TABLE IF NOT EXISTS " + this.tableName + "(BROWSER       TEXT    NOT NULL, "
            + " VERSION       TEXT, " + " PLATFORM      TEXT, " + " PLATFORM_TYPE TEXT, "
            + " LAST_UPDATE   INTEGER);";

    Statement s = null;
    try {
      s = c.createStatement();
      s.executeUpdate(sql);
    } finally {
      Utility.closeDBResources(s, null);
    }
  }

  /**
   * Deletes all the rows from the table.
   *
   * @param c Connection
   * @throws SQLException if a database access error occurs.
   */
  private void clearTable(Connection c) throws SQLException {
    String sql = "DELETE FROM " + this.tableName + ";";

    Statement s = null;
    try {
      s = c.createStatement();
      s.executeUpdate(sql);
    } finally {
      Utility.closeDBResources(s, null);
    }
  }

  /**
   * Insert values into the table.
   *
   * @param c Connection
   * @throws SQLException if a database access error occurs.
   */
  private void insertValues(Connection c) throws SQLException {
    String sql = "INSERT INTO " + this.tableName
        + "(BROWSER, VERSION, PLATFORM, PLATFORM_TYPE, LAST_UPDATE) " + "VALUES "
        + "(?, ?, ?, ?, ?);";

    PreparedStatement ps = null;
    try {
      ps = c.prepareStatement(sql);
      for (Browser browser : browserList) {
        String platformType = browser.getPlatform();
        if (platformType.contains("XP") || platformType.contains("VISTA") || platformType
            .contains("WIN"))
          platformType = "WINDOWS";
        if (RemoteGridFetcher.OS_X_CODENAME.contains(platformType))
          platformType = "MAC";
        ps.setString(1, browser.getBrowserName().trim().toLowerCase());
        ps.setString(2, browser.getVersion().trim().toLowerCase());
        ps.setString(3, browser.getPlatform().trim());
        ps.setString(4, platformType);
        ps.setLong(5, System.currentTimeMillis());
        ps.executeUpdate();
      }
    } finally {
      Utility.closeDBResources(ps, null);
    }
  }

  /**
   * Checks whether the table exists.
   *
   * @param c Connection
   * @return true if the table exists.
   * @throws SQLException if a database access error occurs.
   */
  private boolean checkTableExist(Connection c) throws SQLException {
    String sql =
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + this.tableName + "';";

    Statement s = null;
    ResultSet rs = null;
    try {
      s = c.createStatement();
      rs = s.executeQuery(sql);
      if (rs.next())
        return true;
      else
        return false;
    } finally {
      Utility.closeDBResources(s, rs);
    }
  }

  /**
   * Checks whether the table was updated in last 24 hours.
   *
   * @return true if the table is updated within last 24 hours.
   * @throws SQLException if a database access error occurs.
   */
  private boolean isUpdatedInLast24h() throws SQLException {
    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = this.getDatabaseConnection();

      if (this.checkTableExist(c)) {
        String sql = "SELECT * FROM " + this.tableName + " LIMIT 1;";
        s = c.createStatement();
        rs = s.executeQuery(sql);
        if (rs.next())
          return (System.currentTimeMillis() - rs.getLong("LAST_UPDATE") < 86400000);
        else
          return false;
      } else
        return false;
    } finally {
      Utility.closeDBResources(s, rs);
      if (c != null)
        c.close();
    }
  }

  /**
   * Creates the table if doesn't already exist. If it exists then clear it. Insert the values into
   * it.
   *
   * @throws SQLException if a database access error occurs.
   */
  private void createAndFillTable() throws SQLException {
    Connection c = null;
    try {
      c = this.getDatabaseConnection();
      this.createTableIfNotExists(c);
      try {
        // begin transaction
        c.setAutoCommit(false);
        this.clearTable(c);
        this.insertValues(c);
        c.commit();
        // end transaction
      } finally {
        try {
          c.rollback();
        } catch (SQLException sqle) {
          logger.warn("SQLException while rolling back", sqle);
        }
        c.close();
      }
    } finally {
      if (c != null)
        c.close();
    }
  }

  /**
   * Checks whether the given browser is supported by the remote.
   *
   * @param browser Browser
   * @return true if the browser is supported by the remote.
   * @throws SQLException if a database access error occurs.
   */
  public boolean search(Browser browser) throws SQLException {
    String name = browser.getBrowserName().toLowerCase();
    String version = browser.getVersion();
    if (version != null)
      version = version.trim().split("\\.")[0];
    String platform = browser.getPlatform();
    boolean result = false;

    String sql =
        "SELECT * FROM " + this.tableName + " WHERE BROWSER = '" + name.trim().toLowerCase() + "'";
    if (version != null && !version.trim().isEmpty())
      sql += " AND VERSION LIKE '" + version + "%'";
    if (platform != null && !platform.trim().isEmpty() && platform != "ANY") {
      switch (platform) {
        case "WINDOWS":
          sql += " AND PLATFORM_TYPE = 'WINDOWS'";
          break;
        case "MAC":
          sql += " AND PLATFORM_TYPE = 'MAC'";
          break;
        default:
          sql += " AND PLATFORM = '" + platform + "'";
          break;
      }
    }
    sql += ";";

    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = this.getDatabaseConnection();
      s = c.createStatement();
      rs = s.executeQuery(sql);
      if (rs.next())
        result = true;
    } finally {
      Utility.closeDBResources(s, rs);
      if (c != null)
        c.close();
    }

    return result;
  }

  /**
   * Gets available config list.
   *
   * @param username  the username
   * @param accesskey the accesskey
   * @return the available config list
   * @throws IOException the io exception
   */
  protected List<JsonObject> getAvailableConfigList(String username, String accesskey)
      throws IOException {
    URL myurl = new URL(this.restApiUrl);

    JsonReader reader = null;
    List<JsonObject> availableConfigList = null;

    HttpURLConnection con = null;
    InputStream is = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      if (username != null && accesskey != null)
        Authenticator.setDefault(new Authenticator() {
          @Override protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, accesskey.toCharArray());
          }
        });
      con = (HttpURLConnection) myurl.openConnection();
      is = con.getInputStream();
      isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      reader = Json.createReader(br);
      availableConfigList = reader.readArray().getValuesAs(JsonObject.class);
    } finally {
      if (reader != null)
        reader.close();
      if (br != null)
        try {
          br.close();
        } catch (IOException ioe) {
          logger.warn("Exception while closing BufferedReader", ioe);
        }
      if (isr != null)
        try {
          isr.close();
        } catch (IOException ioe) {
          logger.warn("Exception while closing InputStreamReader", ioe);
        }
      if (is != null)
        try {
          is.close();
        } catch (IOException ioe) {
          logger.warn("Exception while closing InputStream", ioe);
        }
      if (con != null)
        con.disconnect();
    }

    return availableConfigList;
  }

  @Override public Object call() throws Exception {
    if (!this.isUpdatedInLast24h()) {
      this.fetchConfig();
      this.createAndFillTable();
    }
    return "";
  }

  /**
   * Fetches and parses the supported browser list from the remote.
   *
   * @throws IOException if an I/O error occurs.
   */
  public abstract void fetchConfig() throws IOException;

}
