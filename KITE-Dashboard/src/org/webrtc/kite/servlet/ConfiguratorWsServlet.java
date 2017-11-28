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

package org.webrtc.kite.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@ServerEndpoint("/configurator")
public class ConfiguratorWsServlet {

  private static final Log log = LogFactory.getLog(ConfiguratorWsServlet.class);

  private String configName = "KITE configuration";
  private String callback = null;

  @OnOpen
  public void onOpen() {
    if (log.isDebugEnabled())
      log.debug("Open Connection ...");
  }

  @OnClose
  public void onClose() {
    if (log.isDebugEnabled())
      log.debug("Close Connection ...");
  }

  @OnMessage
  public String onMessage(String message) {
    List<String> splittedMessage = Arrays.asList(message.split(Pattern.quote("|")));
    switch (splittedMessage.get(0)) {
      case "configuration-name":
        this.configName = splittedMessage.get(1);
        break;
      case "callback":
        this.callback = splittedMessage.get(1);
        break;
    }
    if (log.isDebugEnabled())
      log.debug("Message from the client: " + message);
    return this.createConfigurationText();

  }

  @OnError
  public void onError(Throwable e) {
    e.printStackTrace();
  }

  private String createConfigurationText() {
    String configText = "<p>{</p>";
    configText += "<p>\"name\": \"" + this.configName + "\",</p>";
    if (this.callback == null)
      configText += "<p>\"callback\": null,</p>";
    else
      configText += "<p>\"callback\": \"" + this.callback + "\",</p>";
    configText += "<p>\"remotes\": [</p>";
    configText += "<p>],</p>";
    configText += "<p>\"test\": [</p>";
    configText += "<p>],</p>";
    configText += "<p>\"browsers\": [</p>";
    configText += "<p>]</p>";
    configText += "<p>}</p>";
    return configText;
  }
}
