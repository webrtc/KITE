package org.webrtc.kite.stats;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

public class OutboundStats extends RTCSingleStatObject {
  private final String packetsSent;
  private final String bytesSent;
  private final String framesDecoded;
  
  public OutboundStats(Map statObject) {
    this.packetsSent = getStatByName(statObject, "packetsSent");
    this.bytesSent = getStatByName(statObject, "bytesSent");
    this.framesDecoded = getStatByName(statObject, "framesDecoded");
  }
  
  
  public double getPacketsSent() {
    return parseDouble(packetsSent);
  }
  
  public double getBytesSent() {
    return parseDouble(bytesSent);
  }
  
  public double getFramesDecoded() {
    return parseDouble(framesDecoded);
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("packetsSent", this.packetsSent)
      .add("bytesSent", this.bytesSent)
      .add("framesDecoded", this.framesDecoded);
  }
}
