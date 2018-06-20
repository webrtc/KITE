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

import javax.json.JsonObject;

import org.webrtc.kite.exception.KiteUnsupportedRemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the operations around the provided array of Remotes.
 */
public class RemoteManager {

  private List<Remote> remoteList = new ArrayList<Remote>();

  /**
   * Constructs a new RemoteManager with the given List<JsonObject>.
   *
   * @param jsonObjectList an implementation of List<JsonObject>.
   * @throws KiteUnsupportedRemoteException if any of the 'name' in the provided jsonObjectList is
   *                                        not specified in SupportedRemote.
   */
  public RemoteManager(List<JsonObject> jsonObjectList) throws KiteUnsupportedRemoteException {
    for (JsonObject jsonObject : jsonObjectList) {
      Remote remote = new Remote(jsonObject);
      if (remote.isLocal())
        this.remoteList.add(0, remote);
      else
        this.remoteList.add(remote);
    }
  }

  /**
   * Gets remote list.
   *
   * @return the remote list
   */
  public List<Remote> getRemoteList() {
    return remoteList;
  }

  /**
   * Returns Remote matching the SupportedRemote.
   *
   * @param supportedRemote SupportedRemote
   * @return Remote remote
   */
  public Remote getRemote(SupportedRemote supportedRemote) {
    for (Remote remote : this.remoteList)
      if (remote.getName().equalsIgnoreCase(supportedRemote.name()))
        return remote;
    return null;
  }

}
