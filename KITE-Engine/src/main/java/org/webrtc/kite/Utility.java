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

import org.apache.log4j.Logger;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteNoKeyException;

import javax.json.JsonObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class holding various static methods.
 */
public class Utility {

  private static final Logger logger = Logger.getLogger(Utility.class.getName());

  /**
   * Returns stack trace of the given exception.
   *
   * @param e Exception
   * @return string representation of e.printStackTrace()
   */
  public static String getStackTrace(Throwable e) {
    Writer writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  /**
   * Checks if the given key exists in the given JsonObject with a valid value.
   *
   * @param jsonObject JsonObject
   * @param key        key
   * @param valueClass Class object for the value of the key.
   * @param isOptional A boolean specifying that the value may be optional. Note: This only works
   *                   if the valueClass is String.
   * @return the value of the key
   * @throws KiteNoKeyException    if the key is not mapped in the JsonObject.
   * @throws KiteBadValueException if the value of the key is invalid.
   */
  public static Object throwNoKeyOrBadValueException(JsonObject jsonObject, String key,
      Class<?> valueClass, boolean isOptional) throws KiteNoKeyException, KiteBadValueException {
    Object value = null;
    try {
      switch (valueClass.getSimpleName()) {
        case "String":
          value = jsonObject.getString(key);
          break;
        case "Integer":
          value = jsonObject.getInt(key);
          break;
        case "JsonArray":
          value = jsonObject.getJsonArray(key);
          break;
        case "JsonObject":
          value = jsonObject.getJsonObject(key);
          break;
        default:
          value = jsonObject.get(key);
      }
      return value;
    } catch (NullPointerException e) {
      if (isOptional)
        return null;
      else
        throw new KiteNoKeyException(key);
    } catch (ClassCastException e) {
      throw new KiteBadValueException(key);
    }
  }

  /**
   * Closes the given jdbc resources if they are not null.
   *
   * @param s  Statement
   * @param rs ResultSet
   */
  public static void closeDBResources(Statement s, ResultSet rs) {
    if (rs != null)
      try {
        rs.close();
      } catch (SQLException e) {
        logger.warn("Exception while closing the ResultSet", e);
      }
    if (s != null)
      try {
        s.close();
      } catch (SQLException e) {
        logger.warn("Exception while closing the Statement", e);
      }
  }

  /**
   * Checks whether the given string is not null and is not an empty string.
   *
   * @param value string
   * @return true if the provided value is not null and is not empty.
   */
  public static boolean isNotNullAndNotEmpty(String value) {
    return value != null && !value.isEmpty();
  }

  /**
   * Prints the stack trace of the provided exception object.
   *
   * @param logger the logger
   * @param e      Exception
   */
  public static void printStackTrace(Logger logger, Exception e) {
    logger.error(Utility.getStackTrace(e));
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
