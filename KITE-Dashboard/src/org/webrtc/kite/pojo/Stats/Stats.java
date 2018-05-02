package org.webrtc.kite.pojo.Stats;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Stats {
  private String browser;
  private MediaStat video, audio;
  private CandidateStat local, remote;
  private CandidatePairStat candidatePairStat;
  private SDP sdp;
  private boolean caller;

  public Stats(String browser, JsonObject browserJsonObject) {
    JsonArray statArray = null;
    JsonObject sdpObject = null;

    //this.browser = browser.substring(2, browser.length());
    this.browser = browser;
    if (browser.endsWith("1"))
      this.caller = true;
    else
      this.caller = false;

    if (browserJsonObject.get("sdp") != null) {
      sdpObject = browserJsonObject.getJsonObject("sdp");
    }
    this.sdp = new SDP(sdpObject);

    if (browserJsonObject.get("stats") != null) {
      statArray = browserJsonObject.getJsonArray("stats");
    }


    this.video = new MediaStat(statArray, "video");
    this.audio = new MediaStat(statArray, "audio");
    this.candidatePairStat = new CandidatePairStat(statArray);
    this.local = new CandidateStat(statArray, "local");
    this.remote = new CandidateStat(statArray, "remote");
  }

  public String getJsonData() {
    String jsonData = "{\"browser\":" + "\"" + this.browser + "\",";
    jsonData += "\"caller\":" + this.caller + ",";
    jsonData += this.local.getJsonData();
    jsonData += ",";
    jsonData += this.remote.getJsonData();
    jsonData += ",";
    jsonData += this.candidatePairStat.getJsonData();
    jsonData += ",";
    jsonData += this.video.getJsonData();
    jsonData += ",";
    jsonData += this.audio.getJsonData();
    jsonData += ",";
    jsonData += this.sdp.getJsonData();
    jsonData += "}";
    return jsonData;
  }

  public boolean isCaller() {
    return caller;
  }
}
