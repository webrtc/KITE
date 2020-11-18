package org.webrtc.kite.stats.rtc;

import io.cosmosoftware.kite.interfaces.JsonBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.webrtc.kite.stats.rtc.msource.RTCAudioSourceStats;
import org.webrtc.kite.stats.rtc.msource.RTCMediaSourceStats;
import org.webrtc.kite.stats.rtc.msource.RTCVideoSourceStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCInboundRtpStreamStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCOutboundRtpStreamStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCReceivedRtpStreamStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCRemoteInboundRtpStreamStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCRemoteOutboundRtpStreamStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCRtpStreamStats;
import org.webrtc.kite.stats.rtc.rtpstream.RTCSentRtpStreamStats;

public class RTCStats extends TreeMap<String, List<RTCSingleStatObject>> implements JsonBuilder {
  
  private final String pcName;
  private long timestamp = System.currentTimeMillis();
  private boolean noData = true;
  private String roomUrl = "unknown";

  public RTCStats(String pcName, TreeMap<String, List<RTCSingleStatObject>> statMap) {
    super(statMap);
    this.pcName = pcName;
    this.noData = false;
  }
  
  public RTCStats (String pcName, List<Map> statArray, JsonArray selectedStats) {
    super();
    this.pcName = pcName;
    if (statArray != null) {
      this.noData = false;
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
                String kind = (String) statMap.get("kind");
                if (kind.equals("video")) {
                  statObject = new RTCVideoSourceStats(statMap);
                } else {
                  statObject = new RTCAudioSourceStats(statMap);
                }
                break;
              }
//              case "stream": {
//                statObject = new RTCMediaSourceStats(statMap);
//                break;
//              }
              case "inbound-rtp": {
                statObject = new RTCInboundRtpStreamStats(statMap);
                break;
              }
              case "remote-inbound-rtp": {
                statObject = new RTCRemoteInboundRtpStreamStats(statMap);
                break;
              }
              case "outbound-rtp": {
                statObject = new RTCOutboundRtpStreamStats(statMap);
                break;
              }
              case "remote-outbound-rtp": {
                statObject = new RTCRemoteOutboundRtpStreamStats(statMap);
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
        return ((RTCIceCandidateStats) statObject).getAddress();
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
    return new EmptyStatObject(null);
  }
  
  public List<RTCRtpStreamStats> getStreamsStats(String boundDirection) {
    List<RTCRtpStreamStats> result = new ArrayList<>();
    if (this.get(boundDirection + "-rtp") != null) {
      for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
        result.add((RTCRtpStreamStats) statObject);
      }
    }
    if (this.get("remote-" + boundDirection + "-rtp") != null) {

      for (RTCSingleStatObject statObject : this.get("remote-" + boundDirection + "-rtp")) {
        result.add((RTCRtpStreamStats) statObject);
      }
    }
    return result;
  }
  
  public String getPcName() {
    return pcName;
  }

  public int getTotalBytesByMedia(String boundDirection, String media) {
    int total = 0;
    if (this.get(boundDirection + "-rtp") != null) {
      if (boundDirection.equals("inbound")) {
        for (RTCSingleStatObject statObject : this.get("remote -" + boundDirection + "-rtp")) {
          if (((RTCRemoteInboundRtpStreamStats) statObject).getKind().equals(media)) {
            total += ((RTCRemoteInboundRtpStreamStats) statObject).getBytesReceived();
          }
        }
      } else {
        for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
          if (((RTCOutboundRtpStreamStats) statObject).getKind().equals(media)) {
            total += ((RTCOutboundRtpStreamStats) statObject).getBytesSent();
          }
        }
      }
    }
    return total;
  }

  public int getTotalFrames(String boundDirection) {
    int total = 0;
    if (this.get(boundDirection + "-rtp") != null) {
      if (boundDirection.equals("inbound")) {
        for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
          if (((RTCInboundRtpStreamStats) statObject).getKind().equals("video")) {
            total += ((RTCInboundRtpStreamStats) statObject).getFramesReceived();
          }
        }
      } else {
        for (RTCSingleStatObject statObject : this.get(boundDirection + "-rtp")) {
          if (((RTCOutboundRtpStreamStats) statObject).getKind().equals("video")) {
            total += ((RTCOutboundRtpStreamStats) statObject).getFramesSent();
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

  public boolean hasNoData() {
    return noData;
  }

  public void setRoomUrl(String roomUrl) {
    this.roomUrl = roomUrl;
  }

  public String getRoomUrl() {
    return roomUrl;
  }

}
