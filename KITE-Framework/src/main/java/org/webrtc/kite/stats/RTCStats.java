package org.webrtc.kite.stats;

import io.cosmosoftware.kite.interfaces.JsonBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.webrtc.kite.Utils.getStackTrace;

public class RTCStats extends TreeMap<String, List<RTCSingleStatObject>> implements JsonBuilder {
  
  private final String pcName;
  private long timestamp = System.currentTimeMillis();
  
  public RTCStats(String pcName, TreeMap<String, List<RTCSingleStatObject>> statMap) {
    super(statMap);
    this.pcName = pcName;
  }
  
  public RTCStats (String pcName, List<Map> statArray, JsonArray selectedStats) {
    super();
    this.pcName = pcName;
    if (statArray != null) {
      for (Map statMap : statArray) {
        if (statMap != null) {
          String type = (String) statMap.get("type");
          if (selectedStats == null || selectedStats.size() == 0 || selectedStats.toString().contains(type)) {
            RTCSingleStatObject statObject = null;
            switch (type) {
              case "codec": {
                statObject = new RTCCodecStats(statMap);
                break;
              }
              case "track": {
                statObject = new RTCMediaStreamTrackStats(statMap);
                break;
              }
              case "stream": {
                statObject = new RTCMediaStreamStats(statMap);
                break;
              }
              case "inbound-rtp": {
                statObject = new RTCRTPStreamStats(statMap, true);
                break;
              }
              case "outbound-rtp": {
                statObject = new RTCRTPStreamStats(statMap, false);
                break;
              }
              case "peer-connection": {
                statObject = new RTCPeerConnectionStats(statMap);
                break;
              }
              case "transport": {
                statObject = new RTCTransportStats(statMap);
                break;
              }
              case "candidate-pair": {
                statObject = new RTCIceCandidatePairStats(statMap);
                break;
              }
              case "remote-candidate":
              case "local-candidate": {
                statObject = new RTCIceCandidateStats(statMap);
                break;
              }
            }
            if (statObject != null) {
              if (this.timestamp == 0) {
                this.timestamp = statObject.getTimestamp();
              }
              if (!this.keySet().contains(type)) {
                this.put(type, new ArrayList<>());
              }
              this.get(type).add(statObject);
            }
          }
        }
      }
      
      // organize track to corresponding stream
      List<RTCRTPStreamStats> streams = new ArrayList<>();
      streams.addAll(this.getStreamsStats("inbound"));
      streams.addAll(this.getStreamsStats("outbound"));
      for (RTCRTPStreamStats stream : streams) {
        if (this.get("track") != null) {
          for (int index = 0; index < this.get("track").size(); index++) {
            RTCMediaStreamTrackStats track = (RTCMediaStreamTrackStats) this.get("track").get(index);
            if (track.getId().equals(stream.getTrackId())) {
              stream.setTrack(track);
            }
          }
        }
      }
    }
  }
  
  public RTCStats (String pcName, List<Map> rawInputFromGetStats) {
    this(pcName, rawInputFromGetStats, null);
  }
  
  public long getTimestamp() {
    return timestamp;
  }
  
  public JsonObject toJson() {
    try {
      return buildJsonObjectBuilder().build();
    } catch (NullPointerException e) {
//      return Json.createObjectBuilder().add("NullPointerException", getStackTrace(e)).build();
      return Json.createObjectBuilder().build();
    }
  }


  public String getRemoteIP() {
    if (this.get("remote-candidate") != null) {
      for (RTCSingleStatObject statObject : this.get("remote-candidate")) {
        return ((RTCIceCandidateStats) statObject).getIp();
      }
    }
    return "no remote-candidate";
  }
  
  public RTCSingleStatObject getSuccessfulCandidate() {
    if (this.get("candidate-pair") != null) {
      for (RTCSingleStatObject statObject : this.get("candidate-pair")) {
        RTCIceCandidatePairStats candidatePairStats = (RTCIceCandidatePairStats) statObject;
        if (candidatePairStats.getState().equals("succeeded")) {
          //sometimes there are multiple successful candidates but only one carries non zero stats
          if (candidatePairStats.getBytesReceived() > 0 || candidatePairStats.getBytesSent() > 0) {
            return candidatePairStats;
          }
        }
        //sometimes there are no "succeeded" pair, but the "in-progress" with
        //a valid currentRoundTripTime value looks just fine.
        if (candidatePairStats.getState().equals("in-progress")) {
          if (candidatePairStats.getCurrentRoundTripTime() > 0) {
            return candidatePairStats;
          }
        }
      }
    }
    return new EmptyStatObject();
  }
  
  public List<RTCRTPStreamStats> getStreamsStats(String boundDirection) {
    List<RTCRTPStreamStats> result = new ArrayList<>();
    if (this.get(boundDirection + "-rtp") != null) {
      for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
        result.add((RTCRTPStreamStats) statObject);
      }
    }
    return result;
  }
  
  public String getPcName() {
    return pcName;
  }
  
  public int getTotalBytes(String boundDirection) {
    int total = 0;
    if (this.get(boundDirection + "-rtp") != null) {
      for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
        if (boundDirection.equals("inbound")) {
          total += ((RTCRTPStreamStats) statObject).getInboundStats().getBytesReceived();
        } else {
          total += ((RTCRTPStreamStats) statObject).getOutboundStats().getBytesSent();
        }
      }
    }
    return total;
  }
  
  
  public int getTotalBytesByMedia(String boundDirection, String media) {
    int total = 0;
    if (this.get(boundDirection + "-rtp") != null) {
      for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
        if (((RTCRTPStreamStats) statObject).getMediaType().equals(media)) {
          if (boundDirection.equals("inbound")) {
            total += ((RTCRTPStreamStats) statObject).getInboundStats().getBytesReceived();
          } else {
            total += ((RTCRTPStreamStats) statObject).getOutboundStats().getBytesSent();
          }
        }
      }
    }
    return total;
  }
  
  
  
  @Override
  public String toString() {
    return toJson().toString();
  }
  
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() throws NullPointerException {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    for (String key : this.keySet()) {
      JsonObjectBuilder tmp = Json.createObjectBuilder();
      for (RTCSingleStatObject statObject: this.get(key)) {
        tmp.add(statObject.getId(), statObject.getJsonObjectBuilder());
      }
      builder.add(key, tmp);
    }
    return builder;
  }
  
}
