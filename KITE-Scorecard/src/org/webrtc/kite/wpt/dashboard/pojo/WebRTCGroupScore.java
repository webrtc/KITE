package org.webrtc.kite.wpt.dashboard.pojo;

import java.util.ArrayList;
import java.util.List;

public class WebRTCGroupScore {
  private String name;
  private int total;
  private int pass;
  private String description;
  private List<WPTTest> testList;


  public WebRTCGroupScore(String name, int total, int pass, String description) {
    this.name = name;
    this.total = total;
    this.pass = pass;
    this.description = description;
    this.testList = new ArrayList<>();
  }

  public String getJson() {
    String res = "\"" + this.name + "\":{";
    res += "\"total\":" + this.total + ",";
    res += "\"passed\":" + this.pass + ",";
    res += "\"description\":\"" + this.description.replaceAll("\"", "'") + "\",";
    res += "\"tests\":{";
    for (int i = 0; i < this.testList.size(); i++) {
      res += this.testList.get(i).getJson(2);
      if (i < this.testList.size() - 1)
        res += ",";
    }
    res += "}}";
    return res;
  }

  public void setTestList(List<WPTTest> testList) {
    this.testList = testList;
  }

  public String getName() {
    return name;
  }

}
