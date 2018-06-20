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

package org.webrtc.kite.config;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.webrtc.kite.exception.KiteUnsupportedRemoteException;
import org.webrtc.kite.grid.BrowserStackGridFetcher;
import org.webrtc.kite.grid.RemoteGridFetcher;
import org.webrtc.kite.grid.SauceLabsGridFetcher;
import org.webrtc.kite.grid.TestingBotGridFetcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a remote object in the config file.
 * <p>
 * {
 * "name": "local",
 * "username": "xxx",
 * "accesskey": "xxx",
 * "remoteAddress": "http://localhost:4444/wd/hub"
 * }
 * <p>
 * If name is 'local' then remoteAddress is mandatory to provide.
 * <p>
 * See SupportedRemote for possible values of the name.
 */
public class Remote extends KiteConfigObject {

  private static final String PATH_TO_DB = "KITE.db";

  private static Map<String, String> supportedRemoteMap = new HashMap<String, String>();

  static {
    for (SupportedRemote supportedRemote : SupportedRemote.values())
      supportedRemoteMap.put(supportedRemote.name(), supportedRemote.name());
  }

  private String name;
  private String username;
  private String accesskey;
  private String remoteAddress;

  /**
   * Constructs a new Remote with given JsonObject.
   *
   * @param jsonObject JsonObject
   * @throws KiteUnsupportedRemoteException if the 'name' is other than what is specified in
   *                                        SupportedRemote.
   */
  public Remote(JsonObject jsonObject) throws KiteUnsupportedRemoteException {
    String name = jsonObject.getString("type");
    Remote.validateRemote(name);

    this.name = name;
    if (this.isLocal()) {
      this.remoteAddress = jsonObject.getString("remoteAddress");
    } else {
      this.username = jsonObject.getString("username");
      this.accesskey = jsonObject.getString("accesskey");
      this.remoteAddress =
          SupportedRemote.valueOf(this.name).remoteAddress(this.username, this.accesskey);
    }
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets remote address.
   *
   * @return the remote address
   */
  public String getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * Checks whether the Remote represents 'local'.
   *
   * @return true if the name is 'local'.
   */
  public boolean isLocal() {
    return this.name.equalsIgnoreCase(SupportedRemote.local.name());
  }

  /**
   * Returns RemoteGridFetcher implementation for various external Selenium grids.
   *
   * @return RemoteGridFetcher grid fetcher
   */
  public RemoteGridFetcher getGridFetcher() {
    switch (this.name) {
      case "saucelabs":
        return new SauceLabsGridFetcher(PATH_TO_DB, this.remoteAddress,
            SupportedRemote.saucelabs.restApiUrl());
      case "browserstack":
        return new BrowserStackGridFetcher(PATH_TO_DB, this.remoteAddress,
            SupportedRemote.browserstack.restApiUrl(), this.username, this.accesskey);
      case "testingbot":
        return new TestingBotGridFetcher(PATH_TO_DB, this.remoteAddress,
            SupportedRemote.testingbot.restApiUrl());
      default:
        return null;
    }
  }

  @Override public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder().add("name", this.getName());
  }

  @Override public JsonObjectBuilder getJsonObjectBuilderForResult() {
    return this.getJsonObjectBuilder();
  }

  /**
   * Checks whether the provided remote is currently supported.
   *
   * @param remoteName name of the remote
   * @throws KiteUnsupportedRemoteException if the 'name' is other than what is specified in
   *                                        SupportedRemote.
   */
  private static void validateRemote(String remoteName) throws KiteUnsupportedRemoteException {
    if (Remote.supportedRemoteMap.get(remoteName) == null)
      throw new KiteUnsupportedRemoteException(remoteName);
  }

}
