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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;

import org.quartz.Job;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;
import org.webrtc.kite.scheduler.MatrixRunnerJob;

/**
 * The type Config type one handler.
 */
public class ConfigTypeOneHandler extends ConfigHandler {

  /**
   * Instantiates a new Config type one handler.
   *
   * @param callbackURL       the callback url
   * @param remoteObjectList  the remote object list
   * @param testObjectList    the test object list
   * @param browserObjectList the browser object list
   * @throws KiteInsufficientValueException the kite insufficient value exception
   * @throws KiteUnsupportedRemoteException the kite unsupported remote exception
   * @throws InvocationTargetException      the invocation target exception
   * @throws NoSuchMethodException          the no such method exception
   * @throws InstantiationException         the instantiation exception
   * @throws IllegalAccessException         the illegal access exception
   */
  public ConfigTypeOneHandler(String callbackURL, List<JsonObject> remoteObjectList,
      List<JsonObject> testObjectList, List<JsonObject> browserObjectList)
      throws KiteInsufficientValueException, KiteUnsupportedRemoteException,
      InvocationTargetException, NoSuchMethodException, InstantiationException,
      IllegalAccessException {

    this.testList = new ArrayList<Test>();
    for (JsonObject object : testObjectList)
      this.testList.add(new TestConf(callbackURL, object));

    this.adjustRemotes(new RemoteManager(remoteObjectList), browserObjectList, Browser.class);
  }

  @Override public Class<? extends Job> getJobClass() {
    return MatrixRunnerJob.class;
  }

}
