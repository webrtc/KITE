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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A class containing the information on browsers, versioning and platforms. */
public class BrowserMapping {

  public static List<String> IrrelevantList =
      Arrays.asList("beta", "dev", "?", "Unknown", "UNKNOWN");
  public static List<String> OsList = new ArrayList<>();
  public static List<String> VersionList = new ArrayList<>();
  public static String StableEdge = "17.17134";
  public static String NightlyEdge = "17.17686";
  public static String StableSafari = "11.0";
  public static String NightlySafari = "11.1";
  public static String StableFirefox = "60.0";
  public static String NightlyFirefox = "62.0";
  public static String StableChrome = "67.0";
  public static String CanaryChrome = "69.0";
  public static List<String> ClientList = new ArrayList<>();
  public static List<String> StableList = new ArrayList<>();
  public static List<Browser> BrowserList = new ArrayList<>();

  static {
    VersionList.add(StableFirefox);
    VersionList.add(NightlyFirefox);
    VersionList.add(StableChrome);
    VersionList.add(CanaryChrome);
    VersionList.add(StableEdge);
    VersionList.add(NightlyEdge);
    VersionList.add(StableSafari);
    VersionList.add(NightlySafari);

    StableList.add(StableFirefox);
    StableList.add(StableChrome);
    StableList.add(StableEdge);
    StableList.add(StableSafari);

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

    BrowserList.add(new Browser("chrome", StableChrome, "LINUX"));
    BrowserList.add(new Browser("chrome", StableChrome, "WINDOWS 10"));
    BrowserList.add(new Browser("chrome", StableChrome, "OS X 10.13"));
    BrowserList.add(new Browser("chrome", CanaryChrome, "LINUX"));
    BrowserList.add(new Browser("chrome", CanaryChrome, "WINDOWS 10"));
    BrowserList.add(new Browser("chrome", CanaryChrome, "OS X 10.13"));
    BrowserList.add(new Browser("firefox", StableFirefox, "LINUX"));
    BrowserList.add(new Browser("firefox", StableFirefox, "WINDOWS 10"));
    BrowserList.add(new Browser("firefox", StableFirefox, "OS X 10.13"));
    BrowserList.add(new Browser("firefox", NightlyFirefox, "LINUX"));
    BrowserList.add(new Browser("firefox", NightlyFirefox, "WINDOWS 10"));
    BrowserList.add(new Browser("firefox", NightlyFirefox, "OS X 10.13"));
    BrowserList.add(new Browser("MicrosoftEdge", StableEdge, "WINDOWS 10"));
    BrowserList.add(new Browser("MicrosoftEdge", NightlyEdge, "WINDOWS 10"));
    BrowserList.add(new Browser("safari", StableSafari, "OS X 10.13"));
    BrowserList.add(new Browser("safari", NightlySafari, "OS X 10.13"));
    BrowserList.add(new Browser("safari", StableSafari, "IOS"));
    BrowserList.add(new Browser("chrome", StableChrome, "android"));
    BrowserList.add(new Browser("firefox", StableFirefox, "android"));
  }
}
