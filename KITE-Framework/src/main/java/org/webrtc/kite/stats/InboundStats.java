package org.webrtc.kite.stats;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

public class InboundStats extends RTCSingleStatObject {
  private String remoteId;
  private String packetsReceived;
  private String packetsLost;
  private String packetsDiscarded;
  private String bytesReceived;
  private String framesDecoded;
  private String jitter;
  
  public InboundStats(Map statObject) {
    this.packetsReceived = getStatByName(statObject, "packetsReceived");
    this.packetsLost = getStatByName(statObject, "packetsLost");
    this.bytesReceived = getStatByName(statObject, "bytesReceived");
    this.packetsDiscarded = getStatByName(statObject, "packetsDiscarded");
    this.framesDecoded = getStatByName(statObject, "framesDecoded");
    this.jitter = getStatByName(statObject, "jitter");
  }
  
  public double getPacketsReceived() {
    return parseDouble(packetsReceived);
  }
  
  public double getPacketsLost() {
    return parseDouble(packetsLost);
  }
  
  public double getPacketsDiscarded() {
    return parseDouble(packetsDiscarded);
  }
  
  public double getBytesReceived() {
    return parseDouble(bytesReceived);
  }
  
  public double getFramesDecoded() {
    return parseDouble(framesDecoded);
  }
  
  public double getJitter() {
    return 1000* parseDouble(jitter);
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("packetsReceived",  packetsReceived)
      .add("bytesReceived",  bytesReceived)
      .add("packetsLost",  packetsLost)
      .add("packetsDiscarded",  packetsDiscarded)
      .add("jitter",  jitter)
      .add("remoteId",  remoteId)
      .add("framesDecoded",  framesDecoded);
  }
}
