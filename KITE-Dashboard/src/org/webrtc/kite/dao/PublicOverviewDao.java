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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class in charged of getting information on the stable overview of a test in the database.
 */
public class PublicOverviewDao {

  private static final Log log = LogFactory.getLog(PublicOverviewDao.class);

  private Connection connection;

  /**
   * Constructs a new PublicOverviewDao object that creates a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public PublicOverviewDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Returns a list of all the result tables of a test in the DB.
   *
   * @param testName the name of the test in question.
   */
  public List<Long> getRunList(String testName) throws SQLException {
    String query = "SELECT tbl_name FROM sqlite_master WHERE type='table' AND name LIKE 'TN" + testName + "%' ORDER BY name DESC";
    List<Long> runList = new ArrayList<>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        if (log.isTraceEnabled()) {
          final StringBuilder rsLog = new StringBuilder();
          for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
            rsLog.append(rs.getMetaData().getColumnName(c)).append(":").append(rs.getString(c))
                .append("-");
          }
          log.trace(rsLog.toString());
        }
        List<String> tmp = Arrays.asList(rs.getString("tbl_name").split("_"));
        runList.add(Long.parseLong(tmp.get(tmp.size() - 1)));
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return runList;
  }

  /**
   * Returns the description of a specific test
   *
   * @param testName the name of the test in question.
   */
  public String getDescription(String testName) throws SQLException {
    String query = "SELECT DESCRIPTION FROM TESTS WHERE TEST_NAME='" + testName + "';";
    String res = "No description was provided fot this test.";
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing get description query: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        if (log.isTraceEnabled()) {
          final StringBuilder rsLog = new StringBuilder();
          for (int c = 1; c <= rs.getMetaData().getColumnCount(); c++) {
            rsLog.append(rs.getMetaData().getColumnName(c)).append(":").append(rs.getString(c))
                .append("-");
          }
          log.trace(rsLog.toString());
        }
        if (rs.getString("DESCRIPTION") != null && !rs.getString("DESCRIPTION").equalsIgnoreCase("")) {
          res = rs.getString("DESCRIPTION");
          break;
        }
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return res;
  }
}
