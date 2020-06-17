package org.webrtc.kite.stats;

import java.util.LinkedHashMap;

public class RTCStatMap extends LinkedHashMap<String, RTCStatList> {
  
  private String regionId = "NC";
  private String networkProfile = "NC";
  private int itinerary = 0;
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
    res.addNewData("Region Id", this.regionId);
    res.addNewData("Network Profile", this.networkProfile);
    res.addNewData("Itinerary", this.itinerary + "");
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

  public void setNetworkProfile(String networkProfile) {
    this.networkProfile = networkProfile;
  }

  public String getNetworkProfile() {
    return networkProfile;
  }

  public void setItinerary(int itinerary) {
    this.itinerary = itinerary;
  }

  public int getItinerary() {
    return itinerary;
  }
}
