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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class containing the information on browsers, versioning and platforms.
 */
public class Mapping {

  public static List<String> IrrelevantList =
      Arrays.asList("beta", "dev", "?", "Unknown", "UNKNOWN");
  public static List<String> OsList = new ArrayList<>();
  public static List<String> VersionList = new ArrayList<>();
  public static String StableEdge = "16.16299";
  public static String StableSafari = "11.0";
  public static String NightlyEdge = "17.17133";
  public static String NightlySafari = "11.1";
  public static List<String> ClientList = new ArrayList<>();
  public static List<String> StableList = new ArrayList<>();


  static {
    VersionList.add("59");
    VersionList.add("61");
    VersionList.add("66");
    VersionList.add("68");
    VersionList.add("16.16");
    VersionList.add("17.17");
    VersionList.add("11");

    StableList.add("59.0");
    StableList.add("66.0");
    StableList.add("16.16");
    StableList.add("11.0");

    OsList.add("WINDOWS 10");
    OsList.add("OS X 10.13");
    OsList.add("LINUX");

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

  }

}
