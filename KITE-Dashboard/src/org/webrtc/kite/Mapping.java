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

  public static final Map<String, String> resultColorMap = new HashMap<>();
  public static final Map<String, String> ErrorMessageMap = new HashMap<>();
  public static final Map<Integer, String> ErrorCodeMap = new HashMap<>();

  static {
    FirefoxVersionList.add("53");
    FirefoxVersionList.add("54");
    FirefoxVersionList.add("55");
    ChromeVersionList.add("58");
    ChromeVersionList.add("59");
    ChromeVersionList.add("60");
    ChromeVersionList.add("61");
    EdgeVersionList.add("14");
    EdgeVersionList.add("15");


    OVERVIEW.add("Vista");
    OVERVIEW.add("Windows 8.1");
    OVERVIEW.add("Windows 10");
    OVERVIEW.add("OS X 10.11");
    OVERVIEW.add("OS X 10.12");
    OVERVIEW.add("Linux");

    OS.put("WIN10", "Windows 10");
    OS.put("WIN8_1", "Windows 8.1");
    OS.put("Vista", "Vista");
    OS.put("SIERRA", "OS X 10.12");
    OS.put("EL_CAPITAN", "OS X 10.11");
    OS.put("LINUX", "Linux");
    OS.put("WIN8", "Windows 8");
    OS.put("XP", "XP");
    OS.put("YOSEMITE", "OS X 10.10");
    OS.put("MAVERICKS", "OS X 10.9");
    OS.put("MOUNTAIN_LION", "OS X 10.8");
    OS.put("SNOW_LEOPARD", "OS X 10.6");
    OS.put("UNIX", "BSD");
    OS.put("ANDROID", "Android");
    OS.put("WINDOWS", "Windows");
    OS.put("MAC", "OS X");
    OS.put("ANY", "Any");


    // Errors
    ErrorMessageMap.put("TIME OUT", "The test has timed out after 60 seconds");
    ErrorMessageMap.put("untestable", "The requested browser was untestable");
    ErrorMessageMap.put("Insufficient", "Not enough credits on TestingBot account");
    ErrorMessageMap.put("forwarding",
        "Selenium couldn't forward your request to an appropriate remote");
    ErrorMessageMap.put("Unable", "Selenium couldn't create the requested remote session");
    ErrorMessageMap.put("The",
        "The Sauce VM failed to prepare for this test. For help, please check https://wiki.saucelabs.com/display/DOCS/Common+Error+Messages");
    ErrorMessageMap.put("", "");
    ErrorMessageMap.put("", "");
    ErrorMessageMap.put("", "");
    ErrorMessageMap.put("", "");
    ErrorMessageMap.put("", "");

    // something for wrong username and key
    // something for wrong address
    // something for connection failure
    ErrorCodeMap.put(0, "The test has timed out after 60 seconds");
    ErrorCodeMap.put(1, "untestable");
    ErrorCodeMap.put(2, "Not enough credits on TestingBot account");
    ErrorCodeMap.put(3,
        "Selenium couldn't forward your request to an appropriate remote, choose another grid or another DesiredCapabilities");
    ErrorCodeMap.put(4, "Selenium couldn't create the requested remote session");
    ErrorCodeMap.put(5,
        "The Sauce VM failed to prepare for this test. For help, please check https://wiki.saucelabs.com/display/DOCS/Common+Error+Messages");


    resultColorMap.put("SUCCESSFUL", "ok");
    resultColorMap.put("TIME OUT", "notok");
    resultColorMap.put("FAILED", "notok");
    resultColorMap.put("NA", "tbd");
    resultColorMap.put("NP", "untestable");
  }

}
