package org.webrtc.kite.stats;

import java.util.LinkedList;
import java.util.List;

public class RTCStatList extends LinkedList<RTCStats> {

  private String pcName = "peerconnection";
  private String regionId = "NC";

  public RTCStatList() {
    super();
  }

  public RTCStatList(RTCStatList otherList) {
    super(otherList);
  }

  public RTCStatList(String pcName, RTCStatList otherList) {
    super(otherList);
    this.pcName = pcName;
    this.regionId = otherList.getRegionId();
  }

  public RTCStatList(List<RTCStats> otherList) {
    super(otherList);
  }

  public String getPcName() {
    return pcName;
  }

  public void setRegionId(String regionId) {
    this.regionId = regionId;
  }

  public String getRegionId() {
    return regionId;
  }
}
