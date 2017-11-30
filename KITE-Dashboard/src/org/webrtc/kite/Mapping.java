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

package org.webrtc.kite;

import org.webrtc.kite.pojo.Browser;

import java.util.*;

/**
 * A class containing the information on browsers, versioning and platforms.
 */
public class Mapping {

  public static final Map<String, String> OS = new LinkedHashMap<String, String>();
  public static List<String> BrowserTypeList =
      Arrays.asList("firefox", "chrome", "MicrosoftEdge", "safari", "opera");
  public static List<String> OVERVIEW = new ArrayList<>();
  public static List<String> FirefoxVersionList = new ArrayList<>();
  public static List<String> ChromeVersionList = new ArrayList<>();
  public static List<String> EdgeVersionList = new ArrayList<>();
  public static List<String> SafariVersionList = new ArrayList<>();
  public static List<Browser> CurrentBrowserList = new ArrayList<>();

  public static List<String> ClientList = new ArrayList<>();

  public static final Map<String, String> resultColorMap = new HashMap<>();

  static {
    FirefoxVersionList.add("57.0");
    FirefoxVersionList.add("59.0");
    ChromeVersionList.add("62.0.3202.94");
    ChromeVersionList.add("64.0");
    EdgeVersionList.add("16.16299");
    EdgeVersionList.add("16.17035");
    SafariVersionList.add("11");
    SafariVersionList.add("11.1");

    OVERVIEW.add("Windows 10");
    OVERVIEW.add("OS X 10.13");
    OVERVIEW.add("Linux");

    for (String os:OVERVIEW){
      for (String ver: FirefoxVersionList)
        CurrentBrowserList.add(new Browser("firefox",ver,os));
      for (String ver: ChromeVersionList)
        CurrentBrowserList.add(new Browser("chrome",ver,os));
      for (String ver: EdgeVersionList)
        CurrentBrowserList.add(new Browser("MicrosoftEdge",ver,os));
    }


    ClientList.add("Selenium");
    ClientList.add("Chromedriver");
    ClientList.add("Geckodriver");
    ClientList.add("MicrosoftWebDriver");
    ClientList.add("Firefox Stable");
    ClientList.add("Firefox Nightly");
    ClientList.add("Edge Stable");
    ClientList.add("Edge Insider");
    ClientList.add("Safari Stable");
    ClientList.add("Safari Technology Preview");
    ClientList.add("Chrome Stable");
    ClientList.add("Chrome Dev");
    ClientList.add("Chrome Canary");


    resultColorMap.put("SUCCESSFUL", "ok");
    resultColorMap.put("TIME OUT", "notok");
    resultColorMap.put("FAILED", "notok");
    resultColorMap.put("NA", "tbd");
    resultColorMap.put("NP", "untestable");
  }

}
