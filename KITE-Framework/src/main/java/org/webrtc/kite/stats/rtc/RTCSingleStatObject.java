/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.stats.rtc;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

/**
 * Parent class for any WebRTC stats object.
 */
public class RTCSingleStatObject {
  protected String id;
  protected String timestamp;
  protected final Map statObject;

  public RTCSingleStatObject(Map statObject) {
    this.statObject = statObject;
    if (statObject != null) {
      this.setId(getStatByName("id"));
      this.timestamp = getStatByName("timestamp");
    }
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  public long getTimestamp() {
    return timestamp == null ? 0 : Long.parseLong(this.timestamp);
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns a JsonObject representation.
   *
   * @return JsonObject json object
   */
  public JsonObject getJsonObject() {
    return this.getJsonObjectBuilder().build();
  }

  /**
   * Returns JsonObjectBuilder.
   *
   * @return JsonObjectBuilder json object builder
   */
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
        .add("id", this.id)
        .add("timestamp", this.timestamp);
  }


  /**
   * Obtain a value of a key in the data map if not null
   *
   * @param statName   name of the key
   *
   * @return true if both the provided objects are not null.
   */
  protected String getStatByName( String statName) {
    String str = this.statObject.get(statName) != null ? statObject.get(statName).toString() : "NA";
    if ("timestamp".equals(statName)) {
      str = formatTimestamp(str);
    }
    return str;
  }

  /**
   * format 1.536834943435905E12 (nano seconds) to 1536834943435 (ms)
   * and convert timestamp to milliseconds
   *
   * @param s raw String obtained from getStats.
   *
   * @return the formatted timestamp
   */
  private String formatTimestamp(String s) {
    String str = s;
    if (str.contains("E")) {
      str = "1" + str.substring(str.indexOf(".") + 1, str.indexOf("E"));
    }
    if (str.length() > 13) {
      str = str.substring(0, 13);
    }
    return str;
  }

  protected double parseDouble(String string) {
    try {
      return Double.parseDouble(string);
    } catch (Exception e) {
      return -1.0;
    }
  }

  public boolean isEmpty() {
    return this instanceof EmptyStatObject;
  }

  protected int parseInt(String string) {
    return (int) parseDouble(string);
  }

  @Override
  public String toString() {
    return this.getJsonObject().toString();
  }
}
