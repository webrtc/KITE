/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.webrtc.kite.stats;

import io.cosmosoftware.kite.entities.Timeouts;
import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Status;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.json.*;
import java.util.*;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.executeJsScript;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;


/**
 * StatsUtils is a Singleton class that collects and save KITE load testing stats into a CSV file.
 */
public class StatsUtils {
  private static final KiteLogger logger = KiteLogger.getLogger(StatsUtils.class.getName());
  

  /**
   * Stashes stats into a global variable and collects them 1s after
   *
   * @param webDriver      used to execute command.
   * @param peerConnection the peer connection
   *
   * @return String. pc stat once
   * @throws KiteTestException the KITE test exception
   */
  public static RTCStats getPCStatOnce(WebDriver webDriver, String peerConnection) throws KiteTestException {
    return getPCStatOnce(webDriver, peerConnection, null);
  }
  
  /**
   * Stashes stats into a global variable and collects them 1s after
   *
   * @param webDriver      used to execute command.
   * @param peerConnection the peer connection
   * @param selectedStats  array of chosen stats
   *
   * @return String. pc stat once
   * @throws KiteTestException the KITE test exception
   */
  public static RTCStats getPCStatOnce(WebDriver webDriver, String peerConnection, JsonArray selectedStats) throws KiteTestException {
    try {
      String stashStatsScript = "const getStatsValues = () =>" +
          peerConnection + "  .getStats()" +
          "    .then(data => {" +
          "      return [...data.values()];" +
          "    });" +
          "const stashStats = async () => {" +
          "  window.KITEStats = await getStatsValues();" +
          "  return 0;" +
          "};" +
          "stashStats();";
      String getStashedStatsScript = "return window.KITEStats;";

      executeJsScript(webDriver, stashStatsScript);
      waitAround(Timeouts.ONE_SECOND_INTERVAL);
      return new RTCStats(peerConnection,
          (List<Map>) executeJsScript(webDriver, getStashedStatsScript), selectedStats);
    } catch (Exception e) {
      throw new KiteTestException("Could not get stats from peer connection", Status.BROKEN ,e);
    }
  }
  
  /**
   * stat JsonObjectBuilder to add to callback result.
   *
   * @param webDriver              used to execute command.
   * @param getStatsConfig         the getStatsConfig
   *
   * @return JsonObjectBuilder of the stat object
   * @throws KiteTestException the kite test exception
   */
  public static RTCStatMap getPCStatOvertime(WebDriver webDriver, JsonObject getStatsConfig)
    throws KiteTestException {
    RTCStatMap result = new RTCStatMap();
    for (JsonString pc : getStatsConfig.getJsonArray("peerConnections").getValuesAs(JsonString.class)) {
      result.put(pc.toString(), getPCStatOvertime(
        webDriver,
        pc.getString(),
        getStatsConfig.getInt("statsCollectionTime"),
        getStatsConfig.getInt("statsCollectionInterval"),
        getStatsConfig.getJsonArray("selectedStats")));
    }
    return result;
  }

  /**
   * Transform list of RTCStats to list of Json Object
   *
   * @param stats list of RTCStats object.
   *
   * @return list of Json Object
   */
  public static JsonObject transformToJson(RTCStatList stats) {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (RTCStats stat: stats) {
      arrayBuilder.add(stat.toJson());
    }
    return Json.createObjectBuilder().add("statsArray", arrayBuilder).build();
  }

  /**
   * stat JsonObjectBuilder to add to callback result.
   *
   * @param webDriver              used to execute command.
   * @param peerConnection         the peer connection
   * @param durationInMilliSeconds during which the stats will be collected.
   * @param intervalInMilliSeconds between each time getStats gets called.
   *
   * @return JsonObjectBuilder of the stat object
   * @throws KiteTestException the kite test exception
   */
  public static RTCStatList getPCStatOvertime(WebDriver webDriver, String peerConnection, int durationInMilliSeconds, int intervalInMilliSeconds)
      throws KiteTestException {
    return getPCStatOvertime(webDriver, peerConnection, durationInMilliSeconds, intervalInMilliSeconds, null);
  }

  /**
   * stat JsonObjectBuilder to add to callback result.
   *
   * @param webDriver              used to execute command.
   * @param peerConnection         the peer connection
   * @param durationInMilliSeconds during which the stats will be collected.
   * @param intervalInMilliSeconds between each time getStats gets called.
   * @param selectedStats          list of selected stats.
   *
   * @return JsonObjectBuilder of the stat object
   * @throws KiteTestException the kite test exception
   */
  public static RTCStatList getPCStatOvertime(WebDriver webDriver, String peerConnection, int durationInMilliSeconds, int intervalInMilliSeconds, JsonArray selectedStats)
    throws KiteTestException {
    RTCStatList statsOverTime = new RTCStatList();
    for (int timer = 0; timer <= durationInMilliSeconds; timer += intervalInMilliSeconds) {
      statsOverTime.add(getPCStatOnce(webDriver, peerConnection, selectedStats));
      if (timer <= durationInMilliSeconds - intervalInMilliSeconds) {
        waitAround(Math.abs(intervalInMilliSeconds - ONE_SECOND_INTERVAL));
      }
    }
    return statsOverTime;
  }
  
  /**
   * Execute and return the requested SDP message
   *
   * @param webDriver      used to execute command.
   * @param peerConnection the peer connection
   * @param type           offer or answer.
   *
   * @return SDP object.
   * @throws KiteTestException the kite test exception
   */
  public static Object getSDPMessage(WebDriver webDriver, String peerConnection, String type) throws KiteTestException {
    return ((JavascriptExecutor) webDriver).executeScript(getSDPMessageScript(peerConnection, type));
  }
  
  /**
   * Returns the test's getSDPMessageScript to retrieve the sdp message for either the offer or answer.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getSDPMessageScript as string.
   */
  private static String getSDPMessageScript(String peerConnection, String type) throws KiteTestException {
    switch (type) {
      case "offer":
        return "var SDP;"
          + "try {SDP = " + peerConnection + ".remoteDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      case "answer":
        return "var SDP;"
          + "try {SDP = " + peerConnection + ".localDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      default:
        throw new KiteTestException("Not a valid type for sdp message.", Status.BROKEN);
    }
  }
  
  public static JsonObject buildStatSummary(RTCStatMap statMap) {
    return buildStatSummary(statMap, false);
  }
  
  public static JsonObject buildStatSummary(RTCStatMap statMap, boolean fullyDetailed) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    for (String pcName: statMap.keySet()) {
      String key = pcName.toLowerCase().startsWith("windows.") ? pcName.substring(8) : pcName;
      builder.add(key, buildStatSummary(statMap.get(pcName), fullyDetailed));
    }
    return builder.build();
  }
  
  /**
   *
   * @param statArray     array of stats provided from getStats()
   * @return  the stat summary : (see below)
   */
  public static JsonObject buildStatSummary(RTCStatList statArray) {
    return buildStatSummary(statArray, false);
  }

  /**
   * @param statArray     array of stats provided from getStats()
   * @param fullyDetailed true if the detailed arrays of specific stat values are to be added to the summary
   *
   * @return  the stat summary : (Sample stats on Chrome 74)
   *     "Starting Timestamp": "2019-06-28 162729",
   *     "Ending Timestamp": "2019-06-28 162740",
   *     "Total Inbound Bytes Received (Bytes)": 642114,
   *     "Total Inbound Audio Bytes Received (Bytes)": 114190,
   *     "Total Inbound Video Bytes Received (Bytes)": 527924,
   *     "Total Outbound Bytes Sent (Bytes)": 644136,
   *     "Total Outbound Audio Bytes Sent (Bytes)": 114371,
   *     "Total Outbound Video Bytes Sent (Bytes)": 529765,
   *     "Total Round Trip Time (ms)": 0.0,
   *     "Average Current Round Trip Time (ms)": 0.0,
   *     "inbound": {
   *             "audio": [
   *             {
   *                 "streamId": "RTCInboundRTPAudioStream_3333556480",
   *                 "Total Bytes Received (Bytes)": "34997",
   *                 "Average Received Bitrate (bps)": "3071",
   *                 "Total Packets Received": "585",
   *                 "Average Audio Level (dB)": "0",
   *                 "Total Packets Lost": 0,
   *                 "Packets Lost (%)": "0",
   *                 "Packets Discarded (%)": "0",
   *                 "Average Audio Jitter (ms)": "0"
   *             }
   *         ],
   *         "video": [
   *             {
   *                 "streamId": "RTCInboundRTPVideoStream_3131213379",
   *                 "Total Bytes Received (Bytes)": "1031833",
   *                 "Average Received Bitrate (bps)": "95078",
   *                 "Total Packets Received": "1006",
   *                 "Frames Received": "234",
   *                 "Average Frame Rate (fps)": "0",
   *                 "Total Packets Lost": 0,
   *                 "Packets Lost (%)": "0",
   *                 "Packets Discarded (%)": "0"
   *             }
   *         ]
   *     },
   *     "outbound": {
   *         "audio": [ same as inbound stats ],
   *         "video": [ same as inbound stats ]
   *     }
   */
  public static JsonObject buildStatSummary(RTCStatList statArray, boolean fullyDetailed) {
  
    JsonObjectBuilder builder = Json.createObjectBuilder();
    double totalRTT = 0;
    double agvRTT = 0;
    List<List<RTCRTPStreamStats>> inboundStreamStatsList = new ArrayList<>();
    List<List<RTCRTPStreamStats>> outboundStreamStatsList = new ArrayList<>();
    builder.add("Starting Timestamp", timestamp(statArray.get(0).getTimestamp()));
    builder.add("Ending Timestamp", timestamp(statArray.get(statArray.size() - 1).getTimestamp()));

    RTCStats lastRtcStats = statArray.get(statArray.size() -1);
    if (lastRtcStats.get("inbound-rtp") != null) {
      builder
        .add(StatEnum.TOTAL_INBOUND_BYTES_RECEIVED.toString(), lastRtcStats.getTotalBytes("inbound"))
        .add(StatEnum.TOTAL_INBOUND_AUDIO_BYTES_RECEIVED.toString(), lastRtcStats.getTotalBytesByMedia("inbound", "audio"))
        .add(StatEnum.TOTAL_INBOUND_VIDEO_BYTES_RECEIVED.toString(), lastRtcStats.getTotalBytesByMedia("inbound", "video"));
    }
    if (lastRtcStats.get("outbound-rtp") != null) {
      builder
        .add(StatEnum.TOTAL_OUTBOUND_BYTES_SENT.toString(), lastRtcStats.getTotalBytes("outbound"))
        .add(StatEnum.TOTAL_OUTBOUND_AUDIO_BYTES_SENT.toString(), lastRtcStats.getTotalBytesByMedia("outbound", "audio"))
        .add(StatEnum.TOTAL_OUTBOUND_VIDEO_BYTES_SENT.toString(), lastRtcStats.getTotalBytesByMedia("outbound", "video"));
    }
    
    for (int index = 0; index < statArray.size(); index++) {
      RTCStats rtcStats = statArray.get(index);
      if (!rtcStats.getSuccessfulCandidate().isEmpty()) {
        RTCIceCandidatePairStats candidatePairStats = (RTCIceCandidatePairStats) rtcStats.getSuccessfulCandidate();
        totalRTT += candidatePairStats.getTotalRoundTripTime();
        agvRTT = (agvRTT + candidatePairStats.getCurrentRoundTripTime()) / (index + 1);
      }
      
      inboundStreamStatsList.add(rtcStats.getStreamsStats("inbound"));
      outboundStreamStatsList.add(rtcStats.getStreamsStats("outbound"));
    }
    
    builder
      .add(StatEnum.TOTAL_RTT.toString(), totalRTT)
      .add(StatEnum.AVG_CURRENT_RTT.toString(), agvRTT);
    if (lastRtcStats.get("inbound-rtp") != null) {
      builder
          .add("inbound", processStreamStats(inboundStreamStatsList,fullyDetailed));
    }
    if (lastRtcStats.get("outbound-rtp") != null) {
      builder
        .add("outbound", processStreamStats(outboundStreamStatsList, fullyDetailed));
    }
    
    return builder.build();
  }
  
  
  private static JsonObject processStreamStats(List<List<RTCRTPStreamStats>> streamStatsList, boolean fullyDetailed) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    Map<String, List<RTCRTPStreamStats>> audioStreamMap = new HashMap<>();
    Map<String, List<RTCRTPStreamStats>> videoStreamMap = new HashMap<>();
    
    for (List<RTCRTPStreamStats> streamStatsArray : streamStatsList) {
      for (RTCRTPStreamStats streamStats : streamStatsArray) {
        String streamId = streamStats.getId();
        if (streamStats.getMediaType().equals("audio")) {
          if (!audioStreamMap.keySet().contains(streamId)) {
            audioStreamMap.put(streamId, new ArrayList<>());
          }
          audioStreamMap.get(streamId).add(streamStats);
        } else {
          if (!videoStreamMap.keySet().contains(streamId)) {
            videoStreamMap.put(streamId, new ArrayList<>());
          }
          videoStreamMap.get(streamId).add(streamStats);
        }
      }
    }
    
    builder.add("audio", transformStreamStatToJson(audioStreamMap, fullyDetailed));
    builder.add("video", transformStreamStatToJson(videoStreamMap, fullyDetailed));
    return builder.build();
  }
  
  private static JsonArray transformStreamStatToJson(Map<String, List<RTCRTPStreamStats>> streamStatMap, boolean fullyDetailed) {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    
    for (String streamId : streamStatMap.keySet()) {
      List<RTCRTPStreamStats> streamStatsList = streamStatMap.get(streamId);
      
      boolean inbound = streamStatsList.get(0).isInbound();
      boolean audio = streamStatsList.get(0).isAudio();
      boolean video = !audio;
      int size = streamStatsList.size();
      int last = size -1;
      int durationInSeconds = (int) ((streamStatsList.get(last).getTimestamp() - streamStatsList.get(0).getTimestamp()) / 1000);
     
      JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
      objectBuilder.add("streamId", streamId);
      
      List<Double> bytes = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.BYTES, fullyDetailed);
      objectBuilder.add(inbound
          ? StatEnum.TOTAL_BYTES_RECEIVED.toString()
          : StatEnum.TOTAL_BYTES_SENT.toString()
        , checkNegativeValue(bytes.get(last)));
      objectBuilder.add(inbound
          ? StatEnum.RECEIVED_BITRATE.toString()
          : StatEnum.SENT_BITRATE.toString()
        , checkNegativeValue(8 * getDiffEndToStart(bytes)/durationInSeconds));
  
      List<Double> packets = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.PACKETS, fullyDetailed);
      objectBuilder.add(inbound
          ? StatEnum.TOTAL_PACKETS_RECEIVED.toString()
          : StatEnum.TOTAL_PACKETS_SENT.toString()
        , checkNegativeValue(packets.get(last)));
      
      
      if (audio) {
        List<Double> audioLvl = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.AUDIO_LEVEL, fullyDetailed);
        objectBuilder.add(StatEnum.AVG_AUDIO_LEVEL.toString(), checkNegativeValue(getAverage(audioLvl)));
      }
      
      if (video) {
        List<Double> frames = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.FRAME, fullyDetailed);
        objectBuilder.add(inbound
          ? StatEnum.TOTAL_FRAME_RECEIVED.toString()
          : StatEnum.TOTAL_FRAME_SENT.toString(), checkNegativeValue(frames.get(last)));
        
        List<Double> framerate = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.FRAME_RATE, fullyDetailed);
        objectBuilder.add(StatEnum.AVG_FRAME_RATE.toString(),  checkNegativeValue(getAverage(framerate)));
      }
      
      if (inbound) {
        List<Double> packetsLost = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.PACKETS_LOST, fullyDetailed);
        objectBuilder.add(StatEnum.TOTAL_PACKETS_LOST.toString(), packetsLost.get(last).intValue());
        objectBuilder.add(StatEnum.PACKETS_LOST_PERCENTAGE.toString(), checkNegativeValue(100 * packetsLost.get(last)/ packets.get(last)));
        
        List<Double> packetsDiscarded = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.PACKETS_DISCARDED, fullyDetailed);
        objectBuilder.add(StatEnum.TOTAL_PACKETS_DISCARDED.toString(), packetsDiscarded.get(last).intValue());
        objectBuilder.add(StatEnum.PACKETS_DISCARDED_PERCENTAGE.toString(), checkNegativeValue(100 * packetsDiscarded.get(last)/ packets.get(last)));
  
  
        if (audio) {
          List<Double> jitter = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.JITTER, fullyDetailed);
          objectBuilder.add(StatEnum.AVG_JITTER.toString(), checkNegativeValue(getAverage(jitter)));
          
        } else {
          // could be useful someday
          List<Double> framedropped = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.FRAME_DROPPED, fullyDetailed);
          List<Double> framedecoded = addStatToJsonBuilder(objectBuilder, streamStatsList, StatEnum.FRAME_DECODED, fullyDetailed);
        }
      }
      arrayBuilder.add(objectBuilder);
    }
    return arrayBuilder.build();
  }
  
  private static List<Double> addStatToJsonBuilder(JsonObjectBuilder objectBuilder, List<RTCRTPStreamStats> streams, StatEnum stat, boolean fullyDetailed) {
    List<Double> temp = extractStreamTrackStat(streams, stat);
    if (fullyDetailed) {
      objectBuilder.add(stat.toString(), toJsonArray(temp));
    }
    return temp;
  }
  
  private static JsonArray toJsonArray(List<Double> values) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (double value: values) {
      if (value < 0) {
        builder.add(checkNegativeValue(value));
      } else {
        if (value > (int) value) {
          builder.add(value);
        } else {
          builder.add((int) value);
        }
      }
    }
    return builder.build();
  }
  
  private static List<Double> extractStreamTrackStat(List<RTCRTPStreamStats> streams, StatEnum stat) {
    List<Double> result = new ArrayList<>();
    for (RTCRTPStreamStats stream : streams) {
      switch (stat) {
        case RECEIVED_BITRATE:
        case SENT_BITRATE:
        case BYTES:
          if (stream.isInbound()) {
            result.add(stream.getInboundStats().getBytesReceived());
          } else {
            result.add(stream.getOutboundStats().getBytesSent());
          }
          break;
        case PACKETS:
          if (stream.isInbound()) {
            result.add(stream.getInboundStats().getPacketsReceived());
          } else {
            result.add(stream.getOutboundStats().getPacketsSent());
          }
          break;
        case PACKETS_LOST:
        case AVG_PACKETS_LOST:
          result.add(stream.getInboundStats().getPacketsLost());
          break;
        case PACKETS_DISCARDED:
          result.add(stream.getInboundStats().getPacketsDiscarded());
          break;
        case JITTER:
        case AVG_JITTER:
          if (stream.isAudio() && stream.isInbound()) {
            result.add(stream.getInboundStats().getJitter());
          }
          break;
        case AUDIO_LEVEL:
        case AVG_AUDIO_LEVEL:
          if (stream.isAudio()) {
            result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getAudioLevel());
          }
          break;
        case FRAME:
        case TOTAL_FRAME_RECEIVED:
        case TOTAL_FRAME_SENT:
          if (stream.isVideo()) {
            if (stream.isInbound()) {
              result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getFramesReceived());
            } else {
              result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getFramesSent());
            }
          }
          break;
        case FRAME_RATE:
          if (stream.isVideo()) {
            result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getFramesPerSecond());
          }
          break;
        case FRAME_DECODED:
          if (stream.isVideo()) {
            result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getFramesDecoded());
          }
          break;
        case FRAME_DROPPED:
          if (stream.isVideo()) {
            result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getFramesDropped());
          }
          break;
        case FRAME_CORRUPTED:
          if (stream.isVideo()) {
            result.add(stream.getTrack() == null ? -1.0 : stream.getTrack().getFramesCorrupted());
          }
          break;
        default:
          logger.error("Wrong place to look for :" + stat.toString());
      }
    }
    if (result.isEmpty()) {
      logger.debug("There seems to be no values available for " + stat + ". " +
        "Please verify that you're trying to extract from the right streams/tracks (inbound/outbound and/or audio/video)");
    }
    if (containOnly(result, -1.0)) {
      logger.debug("There seems to be no track available for " + stat + ". " +
        "Please verify that the media track stats are included in the provided stats.");
    }
    
    return result;
  }
  
  private static double getDiffEndToStart(List<Double> values) {
    if (values.size() > 1) {
      return values.get(values.size() - 1) - values.get(0);
    } else {
      return values.get(0);
    }
  }
  
  private static double getAverage(List<Double> values) {
    double sum = 0;
    for (Double value: values) {
      sum += value;
    }
    return sum/values.size();
  }
  
  private static boolean containOnly(List objectList, Object object) {
    Set objectSet = new HashSet(objectList);
    if (objectSet.size() == 1 && objectSet.contains(object)) {
      return true;
    }
    return false;
  }
  
  private static String checkNegativeValue(double value) {
    if (value < 0) {
      return "NA (" + value + ")";
    }
    String s = value + "";
    if (s.endsWith(".0")) {
      s = s.substring(0, s.length() - 2);
    }
    return (int)value + "";
  }
}
