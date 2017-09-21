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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.Utility;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.ResultTable;

/**
 * A class in charged of getting information on results of a test in the database.
 */
public class ResultTableDao {

  private static final Log log = LogFactory.getLog(ResultTableDao.class);

  private Connection connection;

  /**
   * Constructs a new ResultTableDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public ResultTableDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Returns a list of all the result of a test, in a specific table.
   *
   * @param tableName name of the table which contains the results of the test.
   */
  public List<ResultTable> getResultTestList(String tableName) throws SQLException {
    String query = "SELECT BROWSERS1.NAME, BROWSERS1.VERSION, BROWSERS1.PLATFORM,"
        + " BROWSERS2.NAME, BROWSERS2.VERSION, BROWSERS2.PLATFORM,"
        + " RES.RESULT, RES.DURATION FROM" + " BROWSERS AS BROWSERS1, BROWSERS AS BROWSERS2, "
        + tableName + " AS RES" + " WHERE RES.BROWSER_1 = BROWSERS1.BROWSER_ID"
        + " AND RES.BROWSER_2 = BROWSERS2.BROWSER_ID"
        + " ORDER BY BROWSERS1.NAME DESC, BROWSERS2.NAME DESC, BROWSERS1.VERSION DESC, BROWSERS2.VERSION DESC;";

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
        long duration = rs.getLong("DURATION");
        ResultTable resultTable = new ResultTable(result, duration);
        resultTable.setTableName(tableName);
        resultTable.addBrowser(new Browser(rs.getString(1), rs.getString(2), rs.getString(3)));
        resultTable.addBrowser(new Browser(rs.getString(4), rs.getString(5), rs.getString(6)));
        resultTableList.add(resultTable);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return resultTableList;
  }

}
