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

package org.webrtc.kite.wpt.dashboard.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.wpt.dashboard.Mapping;
import org.webrtc.kite.wpt.dashboard.Utility;
import org.webrtc.kite.wpt.dashboard.pojo.Browser;
import org.webrtc.kite.wpt.dashboard.pojo.WPTScore;
import org.webrtc.kite.wpt.dashboard.pojo.WPTTest;
import org.webrtc.kite.wpt.dashboard.pojo.WebRTCGroupScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class in charged of all data gesture concerning the BROWSERS table in database.
 */
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

  /**
   * Returns WPT result table name corresponding to a browser, if exist.
   */
  public String getTableNameByBrowser(Browser browser) throws SQLException {
    String query = "SELECT TABLE_NAME FROM WPT WHERE BROWSER='" + browser.getName() +
        "' AND VERSION='" + browser.getVersion() + "' AND PLATFORM='" + browser.getPlatform() + "';";
    String res = "N/A";

    System.out.println("Executing: " + query);
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
            rsLog.append(rs.getMetaData().getColumnName(c)).append(":").append(rs.getString(c))
                .append("-");
          }
          log.debug(rsLog.toString());
        }
        res = rs.getString("TABLE_NAME");
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return res;
  }

  /**
   * Returns WPT result table name corresponding to a browser, if exist.
   */
  public String getWebRTCReadyTableNameByBrowser(Browser browser) throws SQLException {
    String query = "SELECT IS_WEBRTC_READY_TABLE_NAME FROM WPT WHERE BROWSER='" + browser.getName() +
        "' AND VERSION='" + browser.getVersion() + "' AND PLATFORM='" + browser.getPlatform() + "';";
    String res = "N/A";

    System.out.println("Executing: " + query);
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
            rsLog.append(rs.getMetaData().getColumnName(c)).append(":").append(rs.getString(c))
                .append("-");
          }
          log.debug(rsLog.toString());
        }
        res = rs.getString("TABLE_NAME");
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return res;
  }


  public List<WPTScore> getScoreList() throws SQLException {
    String query = "SELECT * FROM WPT ORDER BY BROWSER, VERSION, PLATFORM";

    List<WPTScore> wptScores = new ArrayList<>();
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
        String name = rs.getString("BROWSER");
        String version = rs.getString("VERSION");
        String platform = rs.getString("PLATFORM");
        String tableName = rs.getString("TABLE_NAME");
        String webRTCReadyTableName = rs.getString("IS_WEBRTC_READY_TABLE_NAME");
        long lastUpdate = rs.getLong("LAST_UPDATE");
        Browser browser = new Browser(name, version, platform);
        WPTScore score = new WPTScore(browser, tableName, lastUpdate);
        score.setWebRTCReadyTableName(webRTCReadyTableName);
        wptScores.add(score);
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return wptScores;
  }

  public List<WPTTest> getTestListFromTable(String tableName, boolean isWebRTCReady) throws SQLException {
    String query = "SELECT * FROM " + tableName;

    List<WPTTest> testList = new ArrayList<>();
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
        String group;
        String name = rs.getString("TEST_NAME");
        String result = rs.getString("RESULT");
        int total = rs.getInt("TOTAL");
        int pass = rs.getInt("PASS");
        long lastUpdate = rs.getLong("LAST_UPDATE");
        WPTTest test = new WPTTest(name, total, pass, lastUpdate, result);
        if (isWebRTCReady) {
          group = rs.getString("TEST_GROUP");
          test.setGroup(group);
        }
        testList.add(test);
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return testList;
  }


  public List<WebRTCGroupScore> getGroupListFromTable(String tableName) throws SQLException {
    String query = "SELECT * FROM " + tableName + " ORDER BY TEST_GROUP";

    List<WebRTCGroupScore> groupList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    int total = 0;
    int pass = 0;
    String description = "N/A";
    String currentGroup = "N/A";
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
        String group = rs.getString("TEST_GROUP");
        if (!group.equalsIgnoreCase(currentGroup)) {
          if (!currentGroup.equalsIgnoreCase("N/A")) {
            WebRTCGroupScore score = new WebRTCGroupScore(currentGroup, total, pass, description);
            groupList.add(score);
          }
          currentGroup = group;
          total = 0;
          pass = 0;
          description = Mapping.WPTDescriptionMapping.get(currentGroup);
        } else {
          total += rs.getInt("TOTAL");
          pass += rs.getInt("PASS");
        }

      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return groupList;
  }

  public WebRTCGroupScore getGroupByNameFromTable(String tableName, String groupName) throws SQLException {
    String query = "SELECT * FROM " + tableName + " WHERE TEST_GROUP='" + groupName + "';";
    WebRTCGroupScore groupScore;
    List<WPTTest> testList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    int total = 0;
    int pass = 0;
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

        String name = rs.getString("TEST_NAME");
        String result = rs.getString("RESULT");
        int test_total = rs.getInt("TOTAL");
        int test_pass = rs.getInt("PASS");
        long lastUpdate = rs.getLong("LAST_UPDATE");
        WPTTest test = new WPTTest(name, test_total, test_pass, lastUpdate, result);
        test.setGroup(groupName);
        testList.add(test);
        total += test_total;
        pass += test_pass;


      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    groupScore = new WebRTCGroupScore(groupName, total, pass, Mapping.WPTDescriptionMapping.get(groupName));
    groupScore.setTestList(testList);
    return groupScore;
  }

}
