package org.webrtc.kite.wpt;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * The type Sub test.
 */
public class SubTest {
  private String name = "n/a";
  private String message = "n/a";
  private String actualResult = "n/a"; // or status
  
  
  /**
   * Instantiates a new Sub test.
   */
  public SubTest() {
  }
  
  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Sets actual result.
   *
   * @param actualResult the actual result
   */
  public void setActualResult(String actualResult) {
    this.actualResult = actualResult;
  }
  
  /**
   * Sets message.
   *
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /**
   * Gets json.
   *
   * @return the json
   */
  public JsonObject getJson() {
    JsonObjectBuilder res = Json.createObjectBuilder();
    res.add("name", name);
    res.add("status", actualResult);
    res.add("message", message);
    String expectedResult = "PASS";
    if (!actualResult.equalsIgnoreCase(expectedResult)) {
      res.add("expected", expectedResult);
    }
    return res.build();
  }
  
  @Override
  public String toString() {
    return getJson().toString();
  }
}
