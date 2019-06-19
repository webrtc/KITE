package org.webrtc.kite.wpt;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Test report.
 */
public class TestSummary {
  private long endTime = System.currentTimeMillis();
  private String name;
  private List<Result> results = new ArrayList<>();
  private RunInfo runInfo;
  private long startTime = System.currentTimeMillis();
  
  /**
   * Instantiates a new Test report.
   */
  public TestSummary() {

  }
  
  /**
   * Add result.
   *
   * @param result the result
   */
  public void addResult(Result result) {
    this.results.add(result);
  }
  
  /**
   * Gets json.
   *
   * @return the json
   */
  private JsonObject getJson() {
    JsonArrayBuilder resultArray = Json.createArrayBuilder();
    for (Result result : results) {
      resultArray.add(result.getJson());
    }

    return Json.createObjectBuilder()
      .add("run_info", runInfo.getJson())
      .add("startTime", startTime)
      .add("endTime", endTime)
      .add("results", resultArray).build();
  }
  
  public String getName() {
    return name;
  }
  
  /**
   * Sets end time.
   *
   * @param endTime the end time
   */
  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }
  
  /**
   * Sets run info.
   *
   * @param runInfo the run info
   */
  public void setRunInfo(RunInfo runInfo) {
    this.runInfo = runInfo;
    this.name = runInfo.getSummarizedName();
  }
  
  /**
   * Sets start time.
   *
   * @param startTime the start time
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }
  
  @Override
  public String toString() {
    return getJson().toString();
  }
}
