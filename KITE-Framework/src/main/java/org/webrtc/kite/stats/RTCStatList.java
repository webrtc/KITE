package org.webrtc.kite.stats;

import java.util.LinkedList;
import java.util.List;

public class RTCStatList extends LinkedList<RTCStats> {
  
  public RTCStatList() {
    super();
  }
  
  public RTCStatList(List<RTCStats> otherList) {
    super(otherList);
  }
  
}
