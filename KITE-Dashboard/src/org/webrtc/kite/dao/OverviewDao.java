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
import org.webrtc.kite.pojo.ResultTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class in charged of getting information on the overview of a test in the database.
 */
public class OverviewDao {

  private static final Log log = LogFactory.getLog(OverviewDao.class);

  private Connection connection;

  /**
   * Constructs a new OverviewDao object that creates a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public OverviewDao(Connection connection) {
    this.connection = connection;
  }


  /**
   * Returns a list of all the results of a test in the overview table.
   *
   * @param testName  the name of the test in question.
   * @param tupleSize size of the tuple.
   */
  public List<ResultTable> getOverviewResultList(String testName, int tupleSize) throws SQLException {
    String query = "SELECT";
    for (int i = 1; i <= tupleSize; i++)
      query += " BROWSERS" + i + ".NAME, BROWSERS" + i + ".VERSION, BROWSERS" + i + ".PLATFORM,";
    for (int i = 1; i <= tupleSize; i++)
      query += " RES.BROWSER_" + i + ", ";
    query += " RES.TEST_NAME, RES.START_TIME, RES.DURATION, RES.RESULT FROM";
    for (int i = 1; i <= tupleSize; i++)
      query += " BROWSERS AS BROWSERS" + i + ", ";
    query += "kiteOVERVIEW" + testName.trim().replaceAll("[^a-zA-Z0-9]", "_") + "  AS RES WHERE";
    for (int i = 1; i <= tupleSize; i++) {
      query += " RES.BROWSER_" + i + " = BROWSERS" + i + ".BROWSER_ID";
      if (i < tupleSize)
        query += " AND";
    }
    query += " AND RES.TEST_NAME='" + testName + "' ";
    query += " ORDER BY";
    for (int i = 1; i <= tupleSize; i++) {
      query += " BROWSERS" + i + ".PLATFORM DESC, BROWSERS" + i + ".NAME ASC, BROWSERS" + i + ".VERSION DESC";
      if (i < tupleSize)
        query += ",";
    }

    List<ResultTable> resultTableList = new ArrayList<ResultTable>();

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
        String result = rs.getString("RESULT");
        long startTime = rs.getLong("START_TIME");
        long duration = rs.getLong("DURATION");
        ResultTable resultTable = new ResultTable(result, duration, false);
        resultTable.setTableName("TN" + testName + "_" + startTime);
        resultTable.setStartTime(startTime);
        for (int i = 0; i < tupleSize; i++) {
          Browser tmp = new Browser(rs.getString(i * 3 + 1), rs.getString(i * 3 + 2), rs.getString(i * 3 + 3));
          tmp.setId(rs.getInt("BROWSER_" + (i + 1)));
          resultTable.addBrowser(tmp);
        }
        resultTableList.add(resultTable);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }


    return resultTableList;
  }


  /**
   * Returns a list of all the requested results of a test in the overview table.
   *
   * @param testName  name of the test we want to query.
   * @param tupleSize size of the tuple.
   * @param filter    filter string to get the right results, format X-X-X-X with X either 1 ot 0.
   */
  public List<ResultTable> getRequestedOverviewResultList(String testName, int tupleSize, String filter) throws SQLException {
    String query = "SELECT";
    for (int i = 1; i <= tupleSize; i++)
      query += " BROWSERS" + i + ".NAME, BROWSERS" + i + ".VERSION, BROWSERS" + i + ".PLATFORM,";
    for (int i = 1; i <= tupleSize; i++)
      query += " RES.BROWSER_" + i + ", ";
    query += " RES.TEST_NAME, RES.START_TIME, RES.DURATION, RES.RESULT FROM";
    for (int i = 1; i <= tupleSize; i++)
      query += " BROWSERS AS BROWSERS" + i + ", ";
    query += "kiteOVERVIEW" + testName.trim().replaceAll("[^a-zA-Z0-9]", "_") + "  AS RES WHERE";
    for (int i = 1; i <= tupleSize; i++) {
      query += " RES.BROWSER_" + i + " = BROWSERS" + i + ".BROWSER_ID";
      if (i < tupleSize)
        query += " AND";
    }
    query += " AND RES.TEST_NAME='" + testName + "' ";
    switch (filter) {
      case "-1-1-1-1":
        break;
      case "-1-1-1-0":
        query += "AND RES.RESULT<>'SCHEDULED'";
        break;
      case "-1-1-0-1":
        query += "AND (RES.RESULT='SUCCESSFUL' OR RES.RESULT='FAILED' OR RES.RESULT='TIME OUT' OR RES.RESULT='SCHEDULED')";
        break;
      case "-1-1-0-0":
        query += "AND (RES.RESULT='SUCCESSFUL' OR RES.RESULT='FAILED' OR RES.RESULT='TIME OUT')";
        break;
      case "-1-0-1-1":
        query += "AND RES.RESULT<>'FAILED' AND RES.RESULT<>'TIME OUT'";
        break;
      case "-1-0-1-0":
        query += "AND RES.RESULT<>'FAILED' AND RES.RESULT<>'TIME OUT' AND RES.RESULT<>'SCHEDULED'";
        break;
      case "-1-0-0-1":
        query += "AND (RES.RESULT='SUCCESSFUL' OR RES.RESULT='SCHEDULED')";
        break;
      case "-1-0-0-0":
        query += "AND RES.RESULT='SUCCESSFUL'";
        break;
      case "-0-1-1-1":
        query += "AND RES.RESULT<>'SUCCESSFUL'";
        break;
      case "-0-1-1-0":
        query += "AND RES.RESULT<>'SUCCESSFUL' AND RES.RESULT<>'SCHEDULED'";
        break;
      case "-0-1-0-1":
        query += "AND (RES.RESULT='SCHEDULED' OR RES.RESULT='FAILED' OR RES.RESULT='TIME OUT')";
        break;
      case "-0-1-0-0":
        query += "AND (RES.RESULT='FAILED' OR RES.RESULT='TIME OUT')";
        break;
      case "-0-0-1-1":
        query += "AND RES.RESULT<>'FAILED' AND RES.RESULT<>'TIME OUT' AND RES.RESULT<>'SUCCESSFUL'";
        break;
      case "-0-0-1-0":
        query += "AND RES.RESULT<>'FAILED' AND RES.RESULT<>'TIME OUT' AND RES.RESULT<>'SUCCESSFUL' AND RES.RESULT<>'SCHEDULED'";
        break;
      case "-0-0-0-1":
        query += "AND RES.RESULT='SCHEDULED'";
        break;
      case "-0-0-0-0":
        query += "AND RES.RESULT='NAN'";
        break;

    }
    query += " ORDER BY";
    for (int i = 1; i <= tupleSize; i++) {
      query += " BROWSERS" + i + ".PLATFORM DESC, BROWSERS" + i + ".NAME ASC, BROWSERS" + i + ".VERSION DESC";
      if (i < tupleSize)
        query += ",";
    }

    List<ResultTable> resultTableList = new ArrayList<ResultTable>();

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
        String result = rs.getString("RESULT");
        long startTime = rs.getLong("START_TIME");
        long duration = rs.getLong("DURATION");
        ResultTable resultTable = new ResultTable(result, duration, false);
        resultTable.setTableName("TN" + testName + "_" + startTime);
        resultTable.setStartTime(startTime);
        for (int i = 0; i < tupleSize; i++) {
          Browser tmp = new Browser(rs.getString(i * 3 + 1), rs.getString(i * 3 + 2), rs.getString(i * 3 + 3));
          tmp.setId(rs.getInt("BROWSER_" + (i + 1)));
          resultTable.addBrowser(tmp);
        }
        resultTableList.add(resultTable);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return resultTableList;
  }

}
