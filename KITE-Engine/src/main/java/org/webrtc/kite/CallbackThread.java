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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import javax.json.JsonObject;
import java.io.IOException;

/**
 * A thread to post the result in json format to the callback URL.
 */
public class CallbackThread extends Thread {

  private static final Logger logger = Logger.getLogger(CallbackThread.class.getName());

  private String callbackURL;
  private JsonObject jsonObject;

  /**
   * Constructs a new CallBackThread object with the given callbackURL and JsonObject.
   *
   * @param callbackURL a string representation of the callback URL.
   * @param jsonObject  JsonObject
   */
  public CallbackThread(String callbackURL, JsonObject jsonObject) {
    this.callbackURL = callbackURL;
    this.jsonObject = jsonObject;
  }

  @Override public void run() {
    this.postResult();
  }

  /**
   * Posts result to the callback URL.
   */
  public void postResult() {

    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;

    if (logger.isDebugEnabled())
      logger.debug("Posting to '" + callbackURL + "' with body '" + jsonObject.toString() + "'");

    try {
      client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(callbackURL);
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");
      StringEntity entity = new StringEntity(jsonObject.toString());
      httpPost.setEntity(entity);
      response = client.execute(httpPost);
    } catch (IOException e) {
      logger.error("Exception while posting the result", e);
    } finally {
      if (response != null) {
        if (logger.isDebugEnabled())
          logger.debug("response->" + response);
        try {
          response.close();
        } catch (IOException e) {
          logger.warn("Exception while closing the CloseableHttpResponse", e);
        }
      }
      if (client != null)
        try {
          client.close();
        } catch (IOException e) {
          logger.warn("Exception while closing the CloseableHttpClient", e);
        }
    }

  }

}
