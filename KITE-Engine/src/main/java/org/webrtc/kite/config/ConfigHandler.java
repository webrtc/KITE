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

import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;

import javax.json.JsonObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Config handler.
 */
public class ConfigHandler {

  /**
   * The Client list.
   */
  protected List<Client> clientList = new ArrayList<>();
  /**
   * The TestConfig list.
   */
  protected List<TestConfig> testList;


  /**
   * Instantiates a new Config type one handler.
   *
   * @param testObjectList the test object list
   * @throws KiteInsufficientValueException the kite insufficient value exception
   * @throws KiteUnsupportedRemoteException the kite unsupported remote exception
   * @throws InvocationTargetException      the invocation target exception
   * @throws NoSuchMethodException          the no such method exception
   * @throws InstantiationException         the instantiation exception
   * @throws IllegalAccessException         the illegal access exception
   */
  public ConfigHandler(List<JsonObject> testObjectList, List<JsonObject> clientList)
      throws KiteInsufficientValueException, IOException {
    for (JsonObject client : clientList) {
      this.clientList.add(new Client(client));
    }
    this.testList = new ArrayList<>();
    for (JsonObject object : testObjectList) {
      this.testList.add(new TestConfig(object));
    }
  }

  /**
   * Gets browser list.
   *
   * @return the browser list
   */
  public List<Client> getClientList() {
    return this.clientList;
  }

  public void setClientList(List<Client> clientList) {
    this.clientList = clientList;
  }

  /**
   * Gets test list.
   *
   * @return the test list
   */
  public List<TestConfig> getTestList() {
    return this.testList;
  }
}

