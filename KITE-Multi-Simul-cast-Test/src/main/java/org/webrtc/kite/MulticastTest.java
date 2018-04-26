/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.webrtc.kite;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.json.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MulticastTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of
 * WebDriver:
 * <ul>
 * <li>1) Opens all the browsers with the url specified in MULTI_STREAM_URL.</li>
 * <li>2) Inputs roomId and clicks START_BUTTON.</li>
 * <li>3) Waits for videos to be displayed on page and verifies if they are actually being displayed</li>
 * <li>4) The test is considered as successful if all the videos are displayed correctly.</li>
 * </ul>
 * </p>
 */
public class MulticastTest extends KiteTest {

  private final static Logger logger = Logger.getLogger(MulticastTest.class.getName());

  private final static String RESULT_FAILED = "FAILED";
  private final static String RESULT_TIMEOUT = "TIME OUT";
  private final static String RESULT_SUCCESSFUL = "SUCCESSFUL";
  private final static String CHANNEL_INPUT = "channelId";
  private final static String START_BUTTON = "startButton";
  private final static int TIMEOUT = 30000;

  private final static int INTERVAL = 1000;
  private final static Random rand = new Random(System.currentTimeMillis());

  private static String url = null;
  private static String IP = "localhost";
  private static int port = 8085;
  private static String alertMsg = null;
  private static int NUMBER_OF_MEDIA_TRACKS = 4;


  /**
   * Returns the test's getSDPOfferScript to retrieve simulcast.pc.localDescription.sdp
   * or simulcast.pc.remoteDescription.sdp.
   * If it doesn't exist then the method returns 'unknown'.
   * @param local boolean
   *
   * @return the getSDPOfferScript as string.
   */
  private final static String getSDPOfferScript(boolean local) {
    if (local) {
      return "var SDP;"
          + "try {SDP = pc.localDescription.sdp;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
    } else {
      return "var SDP;"
          + "try {SDP = pc.remoteDescription.sdp;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
    }
  }


  /**
   * Returns the test's checkPeerConnectionExistScript
   *
   * @return the string format of a boolean value returned from the JS console.
   */

  private final static String checkPeerConnectionExistScript() {
    return "var res;"
        + "try {res = pc} catch (exception) {} "
        + "if (res) {return true;} else {return false;}";
  }

  /**
   * Returns the test's canvasCheck to check if the video is blank.
   * @param video_id index of the video on the list of video elements.
   * @return the canvasCheck as string.
   */
  private final static String getFrameValueSum(int video_id) {
    return "function getSum(total, num) {" + "    return total + num;" + "};"
        + "var canvas = document.createElement('canvas');" + "var ctx = canvas.getContext('2d');"
        + "var videos = document.getElementsByTagName('video');"
        + "var video = videos["+video_id+"];"
        + "ctx.drawImage(video,0,0,video.videoHeight-1,video.videoWidth-1);"
        + "var imageData = ctx.getImageData(0,0,video.videoHeight-1,video.videoWidth-1).data;"
        + "var sum = imageData.reduce(getSum);"
        + "if (sum===255*(Math.pow(video.videoHeight-1,(video.videoWidth-1)*(video.videoWidth-1))))"
        + "   return 0;" + "return sum;";
  }

  /**
   * Returns the information on sdp message
   * @param sdp message to analyse
   * @return the canvasCheck as string.
   */
  private JsonObject analyseSDP(String sdp){
    JsonObjectBuilder res = Json.createObjectBuilder();
    List<String> lines = Arrays.asList(sdp.split("\n"));
    int m_lines = 0;
    List<String> ssrc_lines = new ArrayList<>();
    List<String> msid_lines = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith("m=")){
        m_lines ++;
      }

      if (line.startsWith("a=ssrc")){
        ssrc_lines.add(line);
      }

      if (line.startsWith("a=msid")){
        msid_lines.add(line);
      }

    }

    if (m_lines == NUMBER_OF_MEDIA_TRACKS && ssrc_lines.size() == NUMBER_OF_MEDIA_TRACKS){
      res.add("type", "unified plan");
      String streamId = null;
      for (String msid : msid_lines){
        String Ids = msid.split(":")[1];
        if (streamId == null){
          streamId = Ids.split(" ")[0];
          res.add("stream 1", streamId);
        } else {
          if (!Ids.split(" ")[0].equalsIgnoreCase(streamId)){
            res.add("stream 2", Ids.split(" ")[0]);
          }
        }
      }

    }
    return res.build();
  }


  /**
   * Restructuring the test according to options given in payload object from config file.
   * This function will not be the same for every test.
   */
  private void payloadHandling() {
    if (this.getPayload() != null) {
      JsonValue jsonValue = this.getPayload();
      JsonObject payload = (JsonObject) jsonValue;
      url = payload.getString("url", null);
      if (url == null ) {
        IP = payload.getString("ip", "localhost");
        port = payload.getInt("port", 8083);
      }
    }
  }



  /**
   * Opens the MULTI_STREAM_URL, fills channelId input and clicks startButton
   *
   */
  private void takeAction() {
    payloadHandling();
    final String channelId = Long.toString(Math.abs(rand.nextLong()));
    for (WebDriver webDriver : this.getWebDriverList()) {
      if (url == null ) {
        url = new StringBuilder("https://")
          .append(IP).append(":").append(port).append("/").toString();
      }
      webDriver.get(url);
      webDriver.findElement(By.id(CHANNEL_INPUT)).sendKeys(channelId);
      webDriver.findElement(By.id(START_BUTTON)).click();
      try {
        Alert alert = webDriver.switchTo().alert();
        alertMsg = alert.getText();
        if (alertMsg != null) {
          alertMsg = ((RemoteWebDriver) webDriver).getCapabilities().getBrowserName() + " alert: " +alertMsg;
          alert.accept();
        }
      } catch (NoAlertPresentException e) {
        alertMsg = null;
      }
    }
  }

  @Override
  public Object testScript() {
    this.takeAction();
    JsonObjectBuilder res = Json.createObjectBuilder();
    JsonObjectBuilder stats = Json.createObjectBuilder();
    List<JsonObject> browserResults = new ArrayList<>();
    List<Boolean> resultList = new ArrayList<>();
    try {
      List<WebDriver> webDriverList = this.getWebDriverList();
      List<MulticastTester> testerList = new ArrayList<>();
      for (WebDriver webDriver : webDriverList) {
        testerList.add(new MulticastTest.MulticastTester( webDriver));
      }
      ExecutorService executorService = Executors.newFixedThreadPool(webDriverList.size());
      List<Future<JsonObject>> futureList = executorService.invokeAll(testerList, 3, TimeUnit.MINUTES);
      executorService.shutdown();
      for (Future<JsonObject> future : futureList) {
        try {
          JsonObject jsonObject = future.get();
          browserResults.add(jsonObject);
        } catch (Exception e) {
          browserResults.add(null);
          logger.error("Exception in MulticastTester: " + e.getLocalizedMessage() + "\r\n" + e.getStackTrace());
          logger.error("Cause Exception in MulticastTester: " + e.getCause().getLocalizedMessage() + "\r\n" + e.getCause());
        }
      }
    } catch(TimeoutException e) {
      res.add("result", RESULT_TIMEOUT).add("stats", Json.createObjectBuilder());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    for (int i = 0; i < browserResults.size(); i++){
      JsonObject browserResult = browserResults.get(i);
      if (browserResult == null){
        resultList.add(false);
      } else {
        String browser = browserResult.getString("browser","client_"+ (i+1));
        browser = (i+1) + "_" + browser;
        resultList.add(browserResult.getBoolean("result"));
        stats.add(browser,browserResult.getJsonObject("stats") );
      }
    }

    if (resultList.contains(false)){
      res.add("result", RESULT_FAILED);
    } else {
      res.add("result", RESULT_SUCCESSFUL);
    }

    res.add("stats", stats);

    return res.build().toString();
  }

  private class MulticastTester implements Callable<JsonObject> {
    private final WebDriver webDriver;
    private String browser;
    public MulticastTester (WebDriver webDriver){
      this.webDriver = webDriver;
      Capabilities capabilities = ((RemoteWebDriver) this.webDriver).getCapabilities();
      this.browser = capabilities.getBrowserName() + "_" + capabilities.getVersion() +"_" + capabilities.getPlatform();
    }

    /**
     *
     * @return whether the peer connection exists
     * @throws InterruptedException
     */
    private boolean checkPeerConnectionObject() throws InterruptedException {
      for (int i = 0; i < TIMEOUT; i += INTERVAL) {
        boolean pcExist = (boolean) ((JavascriptExecutor) this.webDriver).executeScript(checkPeerConnectionExistScript());
        if (!pcExist) {
          logger.info(browser + ": looking for pc");
          Thread.sleep(INTERVAL);
        } else {
          logger.info(browser + ": found pc");
          return true;
        }
      }
      return false;
    }

    /**
     * Calls the getFrameValueSum function to verify video content.
     * @param video_id index of the video in the list of video elements.
     * @return whether the video is actually showing.
     * @throws InterruptedException
     */
    private boolean checkVideoDisplay(int video_id) throws InterruptedException {
      long canvasData = 0;
      for (int i = 0; i < TIMEOUT; i += INTERVAL) {
        canvasData = (Long) ((JavascriptExecutor) this.webDriver).executeScript(getFrameValueSum(video_id));
        if (canvasData == 0) {
          logger.info(browser + ": canvas video " + video_id + " : " + canvasData);
          Thread.sleep(INTERVAL);
        } else {
          logger.info(browser + ": canvas video " + video_id + " : " + canvasData);
          return true;
        }
      }
      return false;
    }

    /**
     * Retrieves the log displayed in the page (on purpose)
     * @return
     */
    private String getLog(){
      String res = "";
      WebElement logElem = this.webDriver.findElement(By.id("log"));
      List<WebElement> logLines = logElem.findElements(By.tagName("p"));
      if (logLines.size() == 0){
        return "NA";
      } else {
        for (WebElement logLine : logLines){
          try {
            res += logLine.getText() + "/r/n";
          } catch (Exception e){
            // do nothing
          }
        }
        return res;
      }
    }

    @Override
    public JsonObject call() throws Exception {
      JsonObjectBuilder res = Json.createObjectBuilder();
      JsonObjectBuilder stats = Json.createObjectBuilder();

      res.add("browser", browser);
      boolean everythingOK;

      everythingOK = checkPeerConnectionObject();

      if (!everythingOK){
        logger.error("No peer connection was established");
        res.add("result", false );
        stats.add("log", getLog());
        res.add("stats", stats);
        return res.build();
      } else {
        everythingOK = checkVideoDisplay(0) && checkVideoDisplay(1);
      }

      if (!everythingOK){
        logger.error("Local streams display error");
        res.add("result", false );
        stats.add("log", getLog());
        res.add("stats", stats);
        return res.build();
      } else {
        everythingOK = checkVideoDisplay(2) && checkVideoDisplay(3);
      }

      if (!everythingOK){
        logger.error("Remote streams display error");
        res.add("result", false );
        stats.add("log", getLog());
        res.add("stats", stats);
        return res.build();
      } else {
        res.add("result", true );
        stats.add("log", getLog());
        res.add("stats", stats);
        return res.build();
      }
    }
  }
}
