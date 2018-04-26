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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.json.JsonObject;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A thread managing the queue concerning the input into the database
 */
public class DataCenterQueueManager extends Thread {

  private static final Log log = LogFactory.getLog(DataCenterQueueManager.class);

  /* Singleton boiler plate code */
  private static DataCenterQueueManager instance = new DataCenterQueueManager();
  public BlockingQueue<JsonObject> queue = new ArrayBlockingQueue<JsonObject>(1000);
  private ResultHandler resultHandler;
  /* Singleton boiler plate code */

  private DataCenterQueueManager() {
  }

  public static DataCenterQueueManager getInstance() {
    return instance;
  }

  public void initResultHandler(String databasePath) {
    this.resultHandler = new ResultHandler(databasePath);
  }

  public void startManager() {
    this.start();
  }

  public void stopManager() {
    this.interrupt();
  }

  @Override
  public void run() {
    // TODO Auto-generated method stub
    try {
      while (true) {
        JsonObject jsonObject = this.queue.take();
        if (log.isDebugEnabled())
          log.debug("Dumping: " + jsonObject);
        try {
          this.resultHandler.dumpResult(jsonObject);
        } catch (ClassNotFoundException | SQLException e) {
          log.error("dumping result", e);
        }
      }
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      queue.clear();
    }
  }

}
