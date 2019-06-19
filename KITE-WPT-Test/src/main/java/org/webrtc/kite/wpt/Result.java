package org.webrtc.kite.wpt;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.ArrayList;

/**
 * The type Result.
 */
public class Result extends ArrayList<SubTest> {
  private final String test; // or path to test
  private long duration = 0;
  private String status = "n/a"; // or harness status
  
  /**
   * Instantiates a new Result.
   *
   * @param test the test
   */
  public Result(String test) {
    this.test = test;
  }
  
  public boolean failed() {
    return !this.status.equalsIgnoreCase("ok");
  }
  
  /**
   * Gets json.
   *
   * @return the json
   */
  public JsonObject getJson() {

    JsonArrayBuilder subTestResults = Json.createArrayBuilder();
    for (SubTest subTest : this) {
      subTestResults.add(subTest.getJson());
    }

    return Json.createObjectBuilder()
      .add("test", test)
      .add("status", status)
      .add("duration", duration)
      .add("subtests", subTestResults).build();
  }
  
  public String getStatus() {
    return status;
  }
  
  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(String status) {
    this.status = status;
  }
  
  public boolean isBroken() {
    return this.size() < 1;
  }
  
  /**
   * Sets duration.
   *
   * @param duration the duration
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }
  
  @Override
  public String toString() {
    return getJson().toString();
  }
}
