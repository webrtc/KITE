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

package org.webrtc.kite.grid;

import org.webrtc.kite.config.Browser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;

/**
 * browserstack implementation of RemoteGridFetcher.
 */
public class BrowserStackGridFetcher extends RemoteGridFetcher {

  private final String username;
  private final String accesskey;

  /**
   * Constructs a new BrowserStackGridFetcher with the given pathToDB, remoteAddress, restApiUrl,
   * username and password.
   *
   * @param pathToDB path to db.
   * @param remoteAddress string representation of the Selenium hub url.
   * @param restApiUrl string representation of the rest API url for fetching the supported
   *        browsers.
   * @param usr username
   * @param pass password
   */
  public BrowserStackGridFetcher(String pathToDB, String remoteAddress, String restApiUrl,
      String usr, String pass) {
    super(pathToDB, "BROWSER_STACK", remoteAddress, restApiUrl);
    this.username = usr;
    this.accesskey = pass;
  }

  @Override
  public void fetchConfig() throws IOException {

    URL myurl = new URL(this.restApiUrl);

    JsonReader reader = null;
    List<JsonObject> availableConfigList = null;

    HttpURLConnection con = null;
    InputStream is = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      Authenticator.setDefault(new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, accesskey.toCharArray());
        }
      });
      con = (HttpURLConnection) myurl.openConnection();
      is = con.getInputStream();
      isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      reader = Json.createReader(br);
      availableConfigList = reader.readArray().getValuesAs(JsonObject.class);
    } finally {
      if (reader != null)
        reader.close();
      if (br != null)
        try {
          br.close();
        } catch (IOException ioe) {
        }
      if (isr != null)
        try {
          isr.close();
        } catch (IOException ioe) {
        }
      if (is != null)
        try {
          is.close();
        } catch (IOException ioe) {
        }
      if (con != null)
        con.disconnect();
    }

    /* might be not necessary, depending on data format it DB */
    for (JsonObject jsonObject : availableConfigList) {
      String name = jsonObject.getString("browser", "").trim().toLowerCase();
      if (name.equalsIgnoreCase("edge"))
        name = "microsoftedge";
      if (name.equalsIgnoreCase("ie"))
        name = "iexplore";

      Browser browser = new Browser(name);
      browser.setVersion(jsonObject.getString("browser_version", ""));

      String os = jsonObject.getString("os", "");
      String platform = jsonObject.getString("os_version", "").toUpperCase();
      if (os.equalsIgnoreCase("OS X")) {
        platform = platform.replaceAll(" ", "_");
      } else if (os.equalsIgnoreCase("Windows")) {
        switch (platform) {
          case "7":
            platform = "VISTA";
            break;
          case "8":
            platform = "WIN8";
            break;
          case "8.1":
            platform = "WIN8_1";
            break;
          case "10":
            platform = "WIN10";
            break;
        }
      }
      browser.setPlatform(platform.toUpperCase());

      this.browserList.add(browser);
    }

  }

}
