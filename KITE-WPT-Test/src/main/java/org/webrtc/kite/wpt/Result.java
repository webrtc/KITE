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
  private String status = "n/a"; // or harness status
  private long duration = 0;
  
  /**
   * Instantiates a new Result.
   *
   * @param test the test
   */
  public Result(String test) {
    this.test = test;
  }
  
  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(String status) {
    this.status = status;
  }
  
  /**
   * Sets duration.
   *
   * @param duration the duration
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }
  
  /**
   * Gets json.
   *
   * @return the json
   */
  public JsonObject getJson() {
  
    JsonArrayBuilder subTestResults = Json.createArrayBuilder();
    for(SubTest subTest: this) {
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
  
  public boolean failed(){
    return !this.status.equalsIgnoreCase("ok");
  }
  
  public boolean isBroken(){
    return this.size() < 1;
  }
  
  @Override
  public String toString() {
    return getJson().toString();
  }
}
