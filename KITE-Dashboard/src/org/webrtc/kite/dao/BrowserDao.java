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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class in charged of all data gesture concerning the BROWSERS table in database.
 */
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
   * Returns the Id of a browser previously registered.
   *
   * @param browser a Browser object which contains information as name, version and platform.
   */
  public int getBrowserId(Browser browser) throws SQLException {
    String query =
        "SELECT BROWSER_ID FROM BROWSERS WHERE NAME='" + browser.getName() + "' AND VERSION='"
            + browser.getVersion() + "' AND PLATFORM='" + browser.getPlatform() + "';";

    int res = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      if (rs.next())
        res = rs.getInt("BROWSER_ID");
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    //if (log.isDebugEnabled()) getBrowserList(); 
    if (log.isDebugEnabled())
      log.debug("Returning: " + res);
    return res;
  }

  /**
   * Returns a list of all registered browsers
   */
  public List<Browser> getBrowserList() throws SQLException {
    String query = "SELECT * FROM BROWSERS;";

    List<Browser> listOfBrowsers = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        if (log.isDebugEnabled()) {
          final StringBuilder rsLog = new StringBuilder();
          for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
            rsLog.append(rs.getMetaData().getColumnName(c)).append(":").append(rs.getString(c))
                .append("-");
          }
          log.debug(rsLog.toString());
        }
        // int id = rs.getInt("BROWSER_ID");
        String name = rs.getString("NAME");
        String version = rs.getString("VERSION");
        String platform = rs.getString("PLATFORM");
        if (!version.equals("dev") && !version.equals("beta") && !platform.equals("?"))
          listOfBrowsers.add(new Browser(name, version, platform));
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return listOfBrowsers;
  }

}
