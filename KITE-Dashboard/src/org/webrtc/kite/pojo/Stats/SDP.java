package org.webrtc.kite.pojo.Stats;

import javax.json.JsonObject;

public class SDP {

  private String offer;
  private String answer;

  public SDP(JsonObject sdpObject) {
    if (sdpObject == null) {
      this.offer = "n/a";
      this.answer = "n/a";
    } else {
      JsonObject offer = sdpObject.getJsonObject("offer");
      this.offer = offer.getString("sdp", "n/a").replaceAll("\n", "</br>").replaceAll("\r", "");

      JsonObject answer = sdpObject.getJsonObject("answer");
      this.answer = answer.getString("sdp", "n/a").replaceAll("\n", "</br>").replaceAll("\r", "");
    }
  }

  public String getJsonData() {
    String jsonData = "\"sdp\":{";
    jsonData += "\"answer\": \"" + this.answer + "\",";
    jsonData += "\"offer\": \"" + this.offer + "\"";
    jsonData += "}";
    return jsonData;
  }

}
