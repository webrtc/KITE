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
import java.util.List;

/**
 * A class in charged of getting information on registered remote grids in database.
 */
public class CompatibilityDao {

  private static final Log log = LogFactory.getLog(CompatibilityDao.class);

  private Connection connection;

  /**
   * Constructs a new CompabilityDao object associated with a connection to the database.
   *
   * @param connection a JDBC connection to the database.
   */
  public CompatibilityDao(Connection connection) {
    this.connection = connection;
  }

  /**
   * Returns the list of browsers supported by the remote grid.
   *
   * @param sourceName name of remote grin.
   */
  public List<List<String>> getCompabilityList(String sourceName) throws SQLException {
    String query = "SELECT * FROM " + sourceName;

    List<List<String>> res = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = this.connection.prepareStatement(query);
      if (log.isDebugEnabled())
        log.debug("Executing: " + query);
      rs = ps.executeQuery();
      while (rs.next()) {
        List<String> tmp = new ArrayList<>();
        for (int i = 1; i <= 4; i++)
          tmp.add(rs.getString(i));
        res.add(tmp);
      }
    } finally {
      Utility.closeDBResources(ps, rs);
    }

    return res;
  }

}
