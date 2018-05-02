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

package org.webrtc.kite;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by hussainsajid on 3/23/17.
 */
public class Utility {

  /**
   * Closes the given jdbc resources if they are not null.
   *
   * @param s  Statement
   * @param rs ResultSet
   */
  public static void closeDBResources(Statement s, ResultSet rs) {
    if (s == null && rs == null) {
      // Both are null, don't do anything.
    } else if (s == null && rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
      }
    } else if (s != null && rs == null) {
      try {
        s.close();
      } catch (SQLException e) {
      }
    } else {
      try {
        rs.close();
      } catch (SQLException e) {
      }
      try {
        s.close();
      } catch (SQLException e) {
      }
    }
  }

  /**
   * Gets the DBConnection object inside the ServletContext.
   *
   * @param servletContext ServletContext
   * @return Connection
   */
  public static Connection getDBConnection(ServletContext servletContext) {
    return (Connection) servletContext.getAttribute("DBConnection");
  }

  /**
   * Gets the CompDBConnection object inside the ServletContext.
   *
   * @param servletContext ServletContext
   * @return Connection
   */
  public static Connection getCompDBConnection(ServletContext servletContext) {
    return (Connection) servletContext.getAttribute("CompDBConnection");
  }

  /**
   * Replaces all the unwanted characters that might cause problems to the DB
   *
   * @param payload raw payload received from KITE
   * @return String with (all) special characters replaced
   */
  public static String escapeSpecialCharacter(String payload) {
    return payload.replaceAll("\\n", "").replaceAll("\\\\", "").replaceAll("\"", "").replaceAll("\'", "");
  }


  /**
   * Checks whether both the given objects are null.
   *
   * @param object1 Object
   * @param object2 Object
   * @return true if both the provided objects are null.
   */
  public static boolean areBothNull(Object object1, Object object2) {
    return object1 == null && object2 == null;
  }

  /**
   * Checks whether both the given objects are not null.
   *
   * @param object1 Object
   * @param object2 Object
   * @return true if both the provided objects are not null.
   */
  public static boolean areBothNotNull(Object object1, Object object2) {
    return object1 != null && object2 != null;
  }
}
