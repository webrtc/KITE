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
import org.webrtc.kite.Mapping;
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
   * Returns the browser with a specific ID.
   *
   * @param id the browser ID in question.
   */
  public Browser getBrowserById(int id) throws SQLException {
    String query =
        "SELECT * FROM BROWSERS WHERE BROWSER_ID=" + id + ";";

    PreparedStatement ps = null;
    ResultSet rs = null;
    Browser browser = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      if (rs.next()) {
        String name = rs.getString("NAME");
        String version = rs.getString("VERSION");
        String platform = rs.getString("PLATFORM");
        browser = new Browser(name, version, platform);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    //if (log.isDebugEnabled()) getBrowserList();
    if (log.isDebugEnabled())
      log.debug("Returning: " + browser.toString());
    return browser;
  }

  /**
   * Returns a list of all registered browsers
   */
  public List<Browser> getBrowserList() throws SQLException {
    String query = "SELECT * FROM BROWSERS ORDER BY NAME DESC, VERSION DESC, PLATFORM DESC;";

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
        if (!Mapping.IrrelevantList.contains(version) && !Mapping.IrrelevantList.contains(platform))
          listOfBrowsers.add(new Browser(name, version, platform));
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return listOfBrowsers;
  }

  /**
   * Returns a list of browser by platform.
   */
  public List<Browser> getBrowserListByPlatform(String platform) throws SQLException {
    String query = "SELECT NAME,VERSION FROM BROWSERS WHERE PLATFORM='" + platform + "' OR UPPER(PLATFORM)='" + platform.toUpperCase() + "' ORDER BY NAME DESC, VERSION ASC;";

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
        if (!Mapping.IrrelevantList.contains(version))
          listOfBrowsers.add(new Browser(name, version, platform));
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return listOfBrowsers;
  }

  /**
   * Returns a list of all browsers that should be in overview
   * For Edge and Safari, this will only determine the nightly, since they don't really have a pattern between stable and
   * nightly.
   */
  public List<Browser> getOverviewBrowserList() throws SQLException {
    List<Browser> listOfOverviewBrowser = new ArrayList<>(); // The ones with the relevant version //
    for (String platform : Mapping.OsList) {
      List<Browser> listOfBrowsers = this.getBrowserListByPlatform(platform);
      // Presumably that the list had already been sorted by browser name //
      String name = null;
      String nightly = "0";
      String stable = "0";
      for (Browser browser : listOfBrowsers) {

        if (!browser.getName().equalsIgnoreCase(name)) {
          if (name == null) { // First browser on the list //
            name = browser.getName();
          } else { // Change to next browser on the list, register current one with nightly and stable version found.
            if (!name.equalsIgnoreCase("safari"))
              listOfOverviewBrowser.add(new Browser(name, nightly, platform));
            if (name.equalsIgnoreCase("chrome") || name.equalsIgnoreCase("firefox")) {
              listOfOverviewBrowser.add(new Browser(name, stable, platform));
            }
            name = browser.getName();
            nightly = "0";
            stable = "0";
          }
        }

        String version = browser.getVersion().split("\\.")[0] + ".0";
        if (version.compareTo(nightly) > 0) { // Get the highest version as nightly (canary) //
          nightly = version;
          stable = String.valueOf((Integer.parseInt(nightly.split("\\.")[0]) - 2)) + ".0"; // Stable is normally 2 version down //
        }

        if (listOfBrowsers.indexOf(browser) == listOfBrowsers.size() - 1) {
          if (!name.equalsIgnoreCase("safari"))
            listOfOverviewBrowser.add(new Browser(name, nightly, platform));
          if (name.equalsIgnoreCase("chrome") || name.equalsIgnoreCase("firefox"))
            listOfOverviewBrowser.add(new Browser(name, stable, platform));
        }
      }
    }
    listOfOverviewBrowser.add(new Browser("MicrosoftEdge", Mapping.NightlyEdge, Mapping.OsList.get(0)));
    listOfOverviewBrowser.add(new Browser("MicrosoftEdge", Mapping.StableEdge, Mapping.OsList.get(0)));
    listOfOverviewBrowser.add(new Browser("safari", Mapping.StableSafari, Mapping.OsList.get(1)));
    listOfOverviewBrowser.add(new Browser("safari", Mapping.NightlySafari, Mapping.OsList.get(1)));
    return listOfOverviewBrowser;
  }

}
