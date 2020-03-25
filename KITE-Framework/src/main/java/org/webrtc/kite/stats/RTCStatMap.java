package org.webrtc.kite.stats;

import java.util.LinkedHashMap;

public class RTCStatMap extends LinkedHashMap<String, RTCStatList> {
  
  private String regionId = "NC";
  public RTCStatMap() {
    super();
  }
  
  /**
   * Get the stat list using index
   * @param index index of the stat list
   * @return
   */
  public RTCStatList get(int index) {
    String pcName = keySet().toArray()[index].toString();
    return new RTCStatList(pcName, this.get(keySet().toArray()[index])) ;
  }


  @Override
  public RTCStatList get(Object key) {
    RTCStatList res = super.get(key);
    res.setRegionId(this.regionId);
    return res;
  }

  /**
   * Assuming that the first stat list is of the local pc
   * @return the first stat list in the map.
   */
  public RTCStatList getLocalPcStats() {
    return this.get(0);
  }

  public void setRegionId(String regionId) {
    this.regionId = regionId;
  }

  public String getRegionId() {
    return regionId;
  }
}
