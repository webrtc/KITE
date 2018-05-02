package org.webrtc.kite.pojo.Stats;

import org.webrtc.kite.pojo.Browser;

import java.util.List;

public class OverallStats {
  private List<Browser> browserList;
  private List<Stats> statsList;


  public void setBrowserList(List<Browser> browserList) {
    this.browserList = browserList;
  }

  public void setStatsList(List<Stats> statsList) {
    this.statsList = statsList;
  }


}
