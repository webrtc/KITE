package org.webrtc.kite.stats;

import java.util.LinkedList;
import java.util.List;

public class RTCStatList extends LinkedList<RTCStats> {

  private String pcName = "peerconnection";

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
}
