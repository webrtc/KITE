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

import org.quartz.Job;
import org.webrtc.kite.grid.RemoteAddressManager;
import org.webrtc.kite.grid.RemoteGridFetcher;

import javax.json.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The type Config handler.
 */
public abstract class ConfigHandler {
  
  /**
   * The Endpoint list.
   */
  protected List<EndPoint> endPointList;
  /**
   * The Test list.
   */
  protected List<Test> testList;
  
  /**
   * Builds the browser list and sets the remote address in each of the browser object.
   * <p>
   * The algorithm is as follows:
   * 1) If there is only one remote provided then sets that remote for every browser.
   * 2) If there are more than one remotes then query all remotes against the provided browsers in
   * sequential order to check if a remote can spawn the browser.
   * 3) If a browser is not supported by a remote then set 'local' as its remote if provided
   * otherwise set the top remote from the remote array.
   *
   * @param remoteManager  RemoteManager
   * @param jsonObjectList an implementation of List<JsonObject>.
   * @param objectClass    the browser class
   *
   * @throws NoSuchMethodException     the no such method exception
   * @throws IllegalAccessException    the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws InstantiationException    the instantiation exception
   */
  protected void adjustRemotes(RemoteManager remoteManager, List<JsonObject> jsonObjectList,
                               Class objectClass)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
    InstantiationException {
    
    Set<EndPoint> set = new LinkedHashSet<>();
    
    List<Remote> remoteList = remoteManager.getRemoteList();
    int remoteListSize = remoteList.size();
    
    if (remoteListSize == 1) {
      String remoteAddress = remoteList.get(0).getRemoteAddress();
      for (JsonObject object : jsonObjectList) {
        Constructor constructor =
          objectClass.getConstructor(new Class[]{String.class, JsonObject.class});
        EndPoint endPoint = (EndPoint) constructor.newInstance(null, object);
        if (endPoint.getRemoteAddress() == null) {
          endPoint.setRemoteAddress(remoteAddress);
        }
        set.add(endPoint);
      }
    } else {
      int index = 0;
      Remote defaultRemote = remoteList.get(0);
      if (defaultRemote.isLocal())
        index = 1;
      
      List<RemoteGridFetcher> fetcherList = new ArrayList<>();
      for (; index < remoteListSize; index++)
        fetcherList.add(remoteList.get(index).getGridFetcher());
      
      RemoteAddressManager remoteAddressManager = new RemoteAddressManager(fetcherList);
      remoteAddressManager.communicateWithRemotes();
      
      for (JsonObject object : jsonObjectList) {
        Constructor constructor =
          objectClass.getConstructor(new Class[]{String.class, JsonObject.class});
        EndPoint endPoint = (EndPoint) constructor.newInstance(null, object);
        if (endPoint.getRemoteAddress() == null) {
          String remoteAddress = remoteAddressManager.findAppropriateRemoteAddress(endPoint);
          endPoint.setRemoteAddress(
            remoteAddress == null ? defaultRemote.getRemoteAddress() : remoteAddress);
        }
        set.add(endPoint);
      }
    }
    if (this.endPointList != null) {
      set.addAll(this.endPointList);
    }
    this.endPointList = new ArrayList<>(set);
  }
  
  /**
   * Gets browser list.
   *
   * @return the browser list
   */
  public List<? extends EndPoint> getEndPointList() {
    return this.endPointList;
  }
  
  /**
   * Gets test list.
   *
   * @return the test list
   */
  public List<? extends Test> getTestList() {
    return this.testList;
  }
}
