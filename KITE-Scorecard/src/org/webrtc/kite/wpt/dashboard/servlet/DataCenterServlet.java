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

package org.webrtc.kite.wpt.dashboard.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.wpt.dashboard.DataCenterQueueManager;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/datacenter")
public class DataCenterServlet extends HttpServlet {

  private static final long serialVersionUID = -6598067107001853086L;
  private static final Log log = LogFactory.getLog(DataCenterServlet.class);

  public DataCenterServlet() {
    super();
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    JsonReader jsonReader = null;
    JsonObject jsonObject = null;
    try {
      jsonReader = Json.createReader(request.getReader());
      jsonObject = jsonReader.readObject();
      if (log.isDebugEnabled())
        log.debug("in->jsonObject: " + jsonObject);
      DataCenterQueueManager.getInstance().queue.put(jsonObject);
    } catch (JsonException | IllegalStateException | InterruptedException e) {
      log.error("adding to queue", e);
    } finally {
      if (jsonReader != null)
        jsonReader.close();
    }
  }

}
