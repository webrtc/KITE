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

import org.apache.log4j.Logger;
import org.webrtc.kite.config.Browser;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Container of all the RemoteGridFetchers to communicate with the REST APIs and find the
 * appropriate match from the given remotes.
 */
public class RemoteAddressManager {

  private static final Logger logger = Logger.getLogger(RemoteAddressManager.class.getName());

  private static final Map<String, String> BROWSER_STACK_PLATFORMS =
      new LinkedHashMap<String, String>();
  static {
    BROWSER_STACK_PLATFORMS.put("MAC", "MAC");
    BROWSER_STACK_PLATFORMS.put("WIN8", "WIN8");
    BROWSER_STACK_PLATFORMS.put("XP", "XP");
    BROWSER_STACK_PLATFORMS.put("WINDOWS", "WINDOWS");
    BROWSER_STACK_PLATFORMS.put("ANY", "ANY");
  }

  private List<RemoteGridFetcher> fetcherList;

  /**
   * Constructs a new RemoteAddressManager with the given List<RemoteGridFetcher>.
   *
   * @param fetcherList List<RemoteGridFetcher>
   */
  public RemoteAddressManager(List<RemoteGridFetcher> fetcherList) {
    this.fetcherList = fetcherList;
  }

  /**
   * Calls rest APIs of all the remotes concurrently.
   */
  public void communicateWithRemotes() {
    ExecutorService executorService = Executors.newFixedThreadPool(this.fetcherList.size());
    try {
      executorService.invokeAll(this.fetcherList);
    } catch (InterruptedException e) {
      logger.warn("Threads were interrupted", e);
    } finally {
      executorService.shutdown();
    }
  }

  /**
   * Finds the appropriate remote address for the given Browser object in sequential manner.
   *
   * @param browser Browser
   * @return string representation of the Selenium hub url or null if none of the external grids
   *         supports the given browser.
   */
  public String findAppropriateRemoteAddress(Browser browser) {
    for (RemoteGridFetcher fetcher : this.fetcherList)
      try {
        if (fetcher.search(browser)) {
          if (fetcher instanceof BrowserStackGridFetcher && browser.getPlatform() != null) {
            if (RemoteAddressManager.BROWSER_STACK_PLATFORMS.get(browser.getPlatform()) != null)
              return fetcher.getRemoteAddress();
          } else {
            return fetcher.getRemoteAddress();
          }
        }
      } catch (SQLException e) {
        logger.warn("SQLException while searching for: " + fetcher.getClass().getName(), e);
      }
    return null;
  }

}
