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

package org.webrtc.kite.pojo;

import org.webrtc.kite.OverviewResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Score object containing the information as test name, map of browser list and simplified results.
 */
public class Score {

  private String testName;
  private String description;
  private HashMap<Browser,String> score;
  private int tupleSize;

  /**
   * Constructs a new Score from given information as test name, browser list and simplified results.
   *
   * @param test the test.
   * @param overviewResult list of result to construct the score map.
   */
  public Score(ConfigTest test, OverviewResult overviewResult) {
    this.testName = test.getTestName();
    this.description = test.getDescription();
    this.score = processResult(overviewResult.getListOfResultTable());
    this.tupleSize = overviewResult.getTupleSize();
  }


  /**
   * Returns a non repetitive map of all browsers with corresponded simplified result
   * which are SUCCESSFUL, FAILED, or ELSE.
   *
   * @param listOfResult list of results.
   */
  private HashMap<Browser,String> processResult(List<ResultTable> listOfResult){
    HashMap<Browser,String> res = new LinkedHashMap<>();
    List<Browser> browsers = getDistincBrowserList(listOfResult);
    List<ResultTable> tmp = new ArrayList<>(listOfResult);
    for (Browser browser: browsers){
      int total=0, success=0;
      String resTmp="";
      boolean hasSUCCESSFUL = false;
      boolean hasFAILED = false;
      for (ResultTable result: tmp){
        if (result.hasBrowser(browser)){
          total+=1;
          if (result.getResult().equalsIgnoreCase("SUCCESSFUL")) {
            hasSUCCESSFUL = true;
            success+=1;
          } else
            hasFAILED=true;
          //listOfResult.remove(result);
        }
      }
      if (hasFAILED&&hasSUCCESSFUL)
        resTmp="ELSE";
      if (hasFAILED&&!hasSUCCESSFUL)
        resTmp="FAILED";
      if (!hasFAILED&&hasSUCCESSFUL)
        resTmp="SUCCESSFUL";
      resTmp+="_"+success+"/"+total;
      res.put(browser,resTmp);
      tmp = new ArrayList<>(listOfResult);
    }
    return res;
  }

  /**
   * Returns a non repetitive list of all browsers in a result list.
   *
   * @param listOfResult list of results.
   */
  private List<Browser> getDistincBrowserList(List<ResultTable> listOfResult){
    List<Browser> res = new ArrayList<>();
    for (ResultTable result: listOfResult){
      for (Browser browser: result.getBrowserList())
        if (!res.contains(browser))
          res.add(browser);
    }
    return res;
  }

  /**
   * Returns under JSON format the scoreboard
   *
   */
  public String getScoreJson(){
    String res = "";
    res+="[";
    List<Browser> browsers= new ArrayList<>(score.keySet());
    for (int i=0;i<browsers.size(); i++){
      Browser browser = browsers.get(i);
      res+="{";
      res+="\"description\":"+"\""+this.description+"\",";
      res+="\"name\":"+"\""+browser.getName()+"\",";
      res+="\"version\":"+"\""+browser.getVersion()+"\",";
      res+="\"platform\":"+"\""+browser.getPlatform()+"\",";
      String tmp = score.get(browser);
      String result = tmp.split("_")[0];
      String ratio = tmp.split("_")[1];
      res+="\"result\":"+"\""+result+"\",";
      res+="\"ratio\":"+"\""+ratio+"\",";
      res+="\"percentage\":"+(100*Integer.parseInt(ratio.split("/")[0])/Integer.parseInt(ratio.split("/")[1]));
      res+="}";
      if (i<browsers.size()-1)
        res+=",";
    }
    res+="]";
    return res;
  }

  public HashMap<Browser, String> getScore() {
    return score;
  }

  public int getTupleSize() {
    return tupleSize;
  }

  public String getTestName() {
    return testName;
  }

  public String getDescription() {
    return description;
  }
}
