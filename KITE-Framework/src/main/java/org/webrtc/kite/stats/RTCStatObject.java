/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.stats;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static org.webrtc.kite.stats.StatsUtils.formatTimestamp;

/**
 * Parent class for any WebRTC stats object.
 */
public abstract class RTCStatObject {
  private String id;
  
  /**
   * Obtain a value of a key in the data map if not null
   *
   * @param statObject data Map
   * @param statName   name of the key
   * @return true if both the provided objects are not null.
   */
  protected String getStatByName(Map<Object, Object> statObject, String statName) {
    String str = statObject.get(statName) != null ?  statObject.get(statName).toString() : "NA";
    if ("timestamp".equals(statName)){
      str = formatTimestamp(str);
    }
    return str;
  }


  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
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
  public abstract JsonObjectBuilder getJsonObjectBuilder();
  
  @Override
  public String toString() {
    return this.getJsonObject().toString();
  }
}
