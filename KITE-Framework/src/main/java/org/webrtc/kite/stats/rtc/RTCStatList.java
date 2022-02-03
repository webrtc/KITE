package org.webrtc.kite.stats.rtc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RTCStatList extends LinkedList<RTCStats> {

  private String pcName = "peerconnection";
  private String regionId = "NC";
  private String networkProfile = "NC";
  private HashMap<String, String> addtionalData = new HashMap<>();

  public RTCStatList() {
    super();
  }

  public RTCStatList(RTCStatList otherList) {
    super(otherList);
  }

  public RTCStatList(String pcName, RTCStatList otherList) {
    super(otherList);
    this.pcName = pcName;
  }

  public RTCStatList(List<RTCStats> otherList) {
    super(otherList);
  }

  public String getPcName() {
    return pcName;
  }

  public boolean hasNoData() {
    for (RTCStats stats: this) {
      if (!stats.hasNoData()) {
        return false;
      }
    }
    return true;
  }

  public String getRoomUrl() {
    if (!this.isEmpty()) {
      return this.get(0).getRoomUrl();
    }
    return "unknown";
  }

  public String getBatchId() {
    if(!this.isEmpty()) {
      return Integer.toString(this.get(0).getBatchId());
    }
    return "unknown";
  }

  public void addNewData(String key, String value) {
    this.addtionalData.put(key, value);
  }

  public HashMap<String, String> getAdditionalData() {
    return addtionalData;
  }
}
