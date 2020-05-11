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

import io.cosmosoftware.kite.report.KiteLogger;
import java.util.HashMap;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.json.JsonObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

/**
 * A thread to post the result in json format to the callback URL.
 */
public class CallbackThread extends Thread {
  
  private static final KiteLogger logger = KiteLogger.getLogger(CallbackThread.class.getName());
  
  private String callbackURL;
  private File file;
  private HashMap<String,String> parameters = new HashMap<>();
  
  /**
   * Constructs a new CallBackThread object with the given callbackURL and JsonObject.
   *
   * @param callbackURL a string representation of the callback URL.
   * @param file  File
   */
  public CallbackThread(String callbackURL, File file) {
    this.callbackURL = callbackURL;
    this.file = file;
    
  }
  
  /**
   * Posts result to the callback URL.
   */
  public void postResult() throws IOException {
    String charset = "UTF-8";
    String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    String CRLF = "\r\n"; // Line separator required by multipart/form-data.

    URLConnection connection = new URL(this.callbackURL + this.getParameterString()).openConnection();
    logger.debug("Sending result to callback url: " + connection);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

    try (
            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
    ) {
      // Send binary file.
      writer.append("--" + boundary).append(CRLF);
      writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + this.file.getName() + "\"").append(CRLF);
      writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(this.file.getName())).append(CRLF);
      writer.append("Content-Transfer-Encoding: binary").append(CRLF);
      writer.append(CRLF).flush();
      Files.copy(this.file.toPath(), output);
      output.flush(); // Important before continuing with writer!
      writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

      // End of multipart/form-data.
      writer.append("--" + boundary + "--").append(CRLF).flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

// Request is lazily fired whenever you need to obtain information about response.
    int responseCode = 0;
    try {
      responseCode = ((HttpURLConnection) connection).getResponseCode();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(responseCode); // Should be 200
  }
  
  @Override
  public void run() {
    try {
      this.postResult();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getParameterString() {
    String res = "?";
    if (this.parameters.isEmpty()) {
      return "";
    }
    for (String key : parameters.keySet()) {
      res += key + "=" + parameters.get(key) + "&";
    }
    // to remove the last &
    return res.substring(0,res.length() - 1);
  }

  public void addParameter(String key, String value) {
    this.parameters.put(key, value);
  }
  
}
