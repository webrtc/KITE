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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
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
  private int callbackPort;
  private String username;
  private String password;
  private File file;
  private boolean uploadComplete;

  /**
   * Constructs a new CallBackThread object with the given callbackURL and JsonObject.
   *
   * @param callbackURL a string representation of the callback URL.
   * @param file  File
   */
  public CallbackThread(String callbackURL, int callbackPort, String username, String password, File file) {
    this.callbackURL = callbackURL;
    this.file = file;
    this.callbackPort = callbackPort;
    this.username = username;
    this.password = password;
  }
  
  /**
   * Posts result to the callback URL.
   */
  public void postResult() throws IOException {
    FTPClient ftp = new FTPClient();
    boolean error = false;
    try {
      int reply;
      ftp.connect(callbackURL, callbackPort);
      ftp.login(username, password);
      ftp.enterLocalPassiveMode();
      ftp.setFileType(FTP.BINARY_FILE_TYPE);
      logger.info("Connected to " + callbackURL + ".");
      reply = ftp.getReplyCode();
      if(!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        logger.error("FTP server refused connection.");
      }
      FileInputStream inputStream = new FileInputStream(file);
      this.uploadComplete = ftp.storeFile(file.getName(), inputStream);
      if(this.uploadComplete) {
        inputStream.close();
        this.file.delete();
      }
      ftp.logout();
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      if(ftp.isConnected()) {
        try {
          ftp.disconnect();
        } catch(IOException ioe) {
          // do nothing
        }
      }
    }
  }
  
  @Override
  public void run() {
    try {
      this.postResult();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isUploadComplete() {
    return this.uploadComplete;
  }

}
