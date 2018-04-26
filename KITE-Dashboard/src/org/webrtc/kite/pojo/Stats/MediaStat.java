package org.webrtc.kite.pojo.Stats;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MediaStat {
  private String mediaType;
  private List<JsonObject> outbound_obj_list = new ArrayList<>();
  private List<JsonObject> inbound_obj_list = new ArrayList<>();
  private double avgBytesSent = 0;
  private double avgBytesReceived = 0;
  private double avgPacketsSent = 0;
  private double avgPacketsReceived = 0;
  private double avgPacketsLost = 0;
  private double avgJitter = 0;

  public MediaStat(JsonArray statArray, String mediaType) {
    this.mediaType = mediaType;
    if (statArray != null) {
      for (JsonValue statJsonValue : statArray) {
        JsonObject statJsonObj = (JsonObject) statJsonValue;
        Set<String> statTypeList = (statJsonObj).keySet();
        for (String type : statTypeList) {
          switch (type) {
            case "outbound-rtp": {
              JsonObject outbound_rtp_json = statJsonObj.getJsonObject(type);
              Set<String> outbound_rtp_stream_list = outbound_rtp_json.keySet();
              for (String outbound_rtp_stream : outbound_rtp_stream_list) {
                if (outbound_rtp_stream.toLowerCase().contains("outbound") && outbound_rtp_stream.toLowerCase().contains(this.mediaType))
                  this.outbound_obj_list.add(outbound_rtp_json.getJsonObject(outbound_rtp_stream));
              }
              break;
            }
            case "inbound-rtp": {
              JsonObject inbound_rtp_json = statJsonObj.getJsonObject(type);
              Set<String> inbound_rtp_stream_list = inbound_rtp_json.keySet();
              for (String inbound_rtp_stream : inbound_rtp_stream_list) {
                if (inbound_rtp_stream.toLowerCase().contains("inbound") && inbound_rtp_stream.toLowerCase().contains(this.mediaType))
                  this.inbound_obj_list.add(inbound_rtp_json.getJsonObject(inbound_rtp_stream));
              }
              break;
            }

          }
        }
      }
    }
  }

  private List<Integer> getBytesSentOvertime() {
    List<Integer> res = new ArrayList<>();
    int cur = 0;
    int prev = 0;
    int skip = 1;
    for (JsonObject jsonObject : this.outbound_obj_list) {
      String temp = jsonObject.getString("bytesSent", "N/A");
      if (temp.equalsIgnoreCase("N/A") || temp.equalsIgnoreCase("NA")) {
        cur = 0;
        prev = 0;
        skip++;
        res.add(0);
      } else {
        prev = cur;
        cur = Integer.parseInt(temp) / 1000;
        res.add((cur - prev) / (2 * skip));
      }
    }
    if (res.size() > 0)
      res.remove(0);
    return res;
  }

  private List<Integer> getPacketsSentOvertime() {
    List<Integer> res = new ArrayList<>();
    int prev;
    int cur = 0;
    int skip = 1;
    for (JsonObject jsonObject : this.outbound_obj_list) {
      String temp = jsonObject.getString("packetsSent", "N/A");
      if (temp.equalsIgnoreCase("N/A") || temp.equalsIgnoreCase("NA")) {
        cur = 0;
        prev = 0;
        skip++;
        res.add(0);
      } else {
        prev = cur;
        cur = Integer.parseInt(temp);
        res.add((cur - prev) / (2 * skip));
      }
    }
    if (res.size() > 0)
      res.remove(0);
    return res;
  }

  private List<Integer> getBytesReceivedOvertime() {
    List<Integer> res = new ArrayList<>();
    int prev;
    int cur = 0;
    int skip = 1;
    for (JsonObject jsonObject : this.inbound_obj_list) {
      String temp = jsonObject.getString("bytesReceived", "N/A");
      if (temp.equalsIgnoreCase("N/A") || temp.equalsIgnoreCase("NA")) {
        prev = cur;
        skip++;
        res.add(0);
      } else {
        prev = cur;
        cur = Integer.parseInt(temp) / 1000;
        res.add((cur - prev) / (2 * skip));
      }
    }
    if (res.size() > 0)
      res.remove(0);
    return res;
  }

  private List<Integer> getPacketsReceivedOvertime() {
    List<Integer> res = new ArrayList<>();
    int prev;
    int cur = 0;
    int skip = 1;
    for (JsonObject jsonObject : this.inbound_obj_list) {
      String temp = jsonObject.getString("packetsReceived", "N/A");
      if (temp.equalsIgnoreCase("N/A") || temp.equalsIgnoreCase("NA")) {
        cur = 0;
        prev = 0;
        skip++;
        res.add(0);
      } else {
        prev = cur;
        cur = Integer.parseInt(temp);
        res.add((cur - prev) / (2 * skip));
      }
    }
    if (res.size() > 0)
      res.remove(0);
    return res;
  }


  private List<Integer> getPacketsLostOvertime() {
    List<Integer> res = new ArrayList<>();
    for (JsonObject jsonObject : this.inbound_obj_list) {
      String temp = jsonObject.getString("packetsLost", "N/A");
      if (temp.equalsIgnoreCase("N/A") || temp.equalsIgnoreCase("NA"))
        res.add(0);
      else
        res.add(Integer.parseInt(temp));
    }
    if (res.size() > 0)
      res.remove(0);
    return res;
  }


  private List<Double> getJitterOvertime() {
    List<Double> res = new ArrayList<>();
    for (JsonObject jsonObject : this.inbound_obj_list) {
      String temp = jsonObject.getString("jitter", "N/A");
      if (temp.equalsIgnoreCase("N/A") || temp.equalsIgnoreCase("NA"))
        res.add(0.0);
      else
        res.add(Double.parseDouble(temp));
    }
    if (res.size() > 0)
      res.remove(0);
    return res;
  }

  private double getAvgBytesReceived() {
    int sum = 0;
    List<Integer> tmp = getBytesReceivedOvertime();
    for (int bytesReceived : tmp)
      sum += bytesReceived;
    this.avgBytesReceived = sum / tmp.size();
    return avgBytesReceived;
  }

  private double getAvgBytesSent() {
    int sum = 0;
    List<Integer> tmp = getBytesSentOvertime();
    for (int avgBytesSent : tmp)
      sum += avgBytesSent;
    this.avgBytesSent = sum / tmp.size();
    return avgBytesSent;
  }

  private double getAvgJitter() {
    double sum = 0;
    List<Double> tmp = getJitterOvertime();
    for (double avgJitter : tmp)
      sum += avgJitter;
    this.avgJitter = sum / tmp.size();
    return avgJitter;
  }

  private double getAvgPacketsLost() {
    int sum = 0;
    List<Integer> tmp = getPacketsLostOvertime();
    for (int avgPacketsLost : tmp)
      sum += avgPacketsLost;
    this.avgPacketsLost = sum / tmp.size();
    return avgPacketsLost;
  }

  private double getAvgPacketsReceived() {
    int sum = 0;
    List<Integer> tmp = getPacketsReceivedOvertime();
    for (int avgPacketsReceived : tmp)
      sum += avgPacketsReceived;
    this.avgPacketsReceived = sum / tmp.size();
    return avgPacketsReceived;
  }

  private double getAvgPacketsSent() {
    int sum = 0;
    List<Integer> tmp = getPacketsSentOvertime();
    for (int avgPacketsSent : tmp)
      sum += avgPacketsSent;
    this.avgPacketsSent = sum / tmp.size();
    return avgPacketsSent;
  }

  public String getJsonData() {
    List<Integer> temp;
    String jsonData = "\"" + this.mediaType + "\":{";

    if (getJitterOvertime().size() > 0) {
      jsonData += "\"avgJitterOvertime\":" + getAvgJitter() + ",";
      jsonData += "\"JitterOvertime\": [";
      for (int i = 0; i < this.getJitterOvertime().size(); i++) {
        jsonData += this.getJitterOvertime().get(i);
        if (i < this.getJitterOvertime().size() - 1)
          jsonData += ",";
      }
      jsonData += "],";
    }

    if (getPacketsReceivedOvertime().size() > 0) {
      jsonData += "\"avgPacketsReceived\":" + getAvgPacketsReceived() + ",";
      jsonData += "\"PacketsReceivedOvertime\": [";
      temp = getPacketsReceivedOvertime();
      for (int i = 0; i < temp.size(); i++) {
        jsonData += temp.get(i);
        if (i < temp.size() - 1) {
          jsonData += ",";
        }
      }
      jsonData += "],";
    }

    if (getPacketsSentOvertime().size() > 0) {
      jsonData += "\"avgPacketsSent\":" + getAvgPacketsSent() + ",";
      jsonData += "\"PacketsSentOvertime\": [";
      temp = getPacketsSentOvertime();
      for (int i = 0; i < temp.size(); i++) {
        jsonData += temp.get(i);
        if (i < temp.size() - 1)
          jsonData += ",";
      }
      jsonData += "],";
    }

    if (getPacketsLostOvertime().size() > 0) {
      jsonData += "\"avgPacketsLost\":" + getAvgPacketsLost() + ",";
      jsonData += "\"PacketsLostOvertime\": [";
      temp = getPacketsLostOvertime();
      for (int i = 0; i < temp.size(); i++) {
        jsonData += temp.get(i);
        if (i < temp.size() - 1)
          jsonData += ",";
      }
      jsonData += "],";
    }
    if (getBytesReceivedOvertime().size() > 0) {
      jsonData += "\"avgBytesReceived\":" + getAvgBytesReceived() + ",";
      jsonData += "\"BytesReceivedOvertime\": [";
      temp = getBytesReceivedOvertime();
      for (int i = 0; i < temp.size(); i++) {
        jsonData += temp.get(i);
        if (i < temp.size() - 1)
          jsonData += ",";
      }
      jsonData += "],";
    }

    if (getBytesSentOvertime().size() > 0) {
      jsonData += "\"avgBytesSent\":" + getAvgBytesSent() + ",";
      jsonData += "\"BytesSentOvertime\": [";
      temp = getBytesSentOvertime();
      for (int i = 0; i < temp.size(); i++) {
        jsonData += temp.get(i);
        if (i < temp.size() - 1)
          jsonData += ",";
      }
      jsonData += "]";
    }

    jsonData += "}";
    return jsonData;
  }
}
