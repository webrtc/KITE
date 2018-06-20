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
import org.webrtc.kite.pojo.ClientVersion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A class in charged of getting information on client versions in the database.
 */
public class ClientVersionDao {
  private static final Log log = LogFactory.getLog(BrowserDao.class);

  private Connection connection;

  /**
   * Constructs a new ClientVersionDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public ClientVersionDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Returns a list of all client version information.
   */
  public List<ClientVersion> getClientVersionList() throws SQLException {
    String query = "SELECT * FROM CLIENT_VERSION;";
    List<ClientVersion> clientVersionList = new ArrayList<>();
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
        if (version == null)
          version = "-";
        String lastVersion = rs.getString("LAST_VERSION");
        if (lastVersion == null)
          lastVersion = "-";
        long lastUpdate = rs.getLong("LAST_UPDATE");
        if (lastUpdate != 0) {
          Date date = new Date(lastUpdate);
          SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
          String dateText = df2.format(date);
          clientVersionList.add(new ClientVersion(name, version, lastVersion, dateText));
        } else
          clientVersionList.add(new ClientVersion(name, version, lastVersion, "Never"));
      }

    } finally {
      Utility.closeDBResources(ps, rs);
    }
    return clientVersionList;
  }
}
