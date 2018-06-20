package org.webrtc.kite.wpt;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utility {

  /**
   * Returns the information on sdp message
   *
   * @param sdp message to analyse
   * @return the canvasCheck as string.
   */
  public static JsonObject UnifiedPlanCheck(String sdp, int NUMBER_OF_MEDIA_TRACKS) {
    JsonObjectBuilder res = Json.createObjectBuilder();
    List<String> lines = Arrays.asList(sdp.split("\n"));
    int m_lines = 0;
    List<String> ssrc_lines = new ArrayList<>();
    List<String> msid_lines = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith("m=")) {
        m_lines++;
      }

      if (line.startsWith("a=ssrc")) {
        ssrc_lines.add(line);
      }

      if (line.startsWith("a=msid")) {
        msid_lines.add(line);
      }
    }

    if (m_lines == NUMBER_OF_MEDIA_TRACKS && ssrc_lines.size() == NUMBER_OF_MEDIA_TRACKS) {
      res.add("type", "unified plan");
      String streamId = null;
      for (String msid : msid_lines) {
        String Ids = msid.split(":")[1];
        if (streamId == null) {
          streamId = Ids.split(" ")[0];
          res.add("stream 1", streamId);
        } else {
          if (!Ids.split(" ")[0].equalsIgnoreCase(streamId)) {
            res.add("stream 2", Ids.split(" ")[0]);
          }
        }
      }
    }
    return res.build();
  }

  /**
   * Returns the test's canvasCheck to check if the video is blank.
   *
   * @param id index of the video on the list of video elements.
   * @return the canvasCheck as string.
   */
  public static final String getVideoValueSum(int id) {
    return "function getSum(total, num) {"
        + "    return total + num;"
        + "};"
        + "var canvas = document.createElement('canvas');"
        + "var ctx = canvas.getContext('2d');"
        + "var videos = document.getElementsByTagName('video');"
        + "var video = videos["
        + id
        + "];"
        + "if(video){"
        + "ctx.drawImage(video,0,0,video.videoHeight-1,video.videoWidth-1);"
        + "var imageData = ctx.getImageData(0,0,video.videoHeight-1,video.videoWidth-1).data;"
        + "var sum = imageData.reduce(getSum);"
        + "if (sum===255*(Math.pow(video.videoHeight-1,(video.videoWidth-1)*(video.videoWidth-1))))"
        + "   return 0;"
        + "return sum;"
        + "} else {"
        + "return 0 "
        + "}";
  }
  /**
   * Returns the test's canvasCheck to check if the video is blank.
   *
   * @param id index of the video on the list of video elements.
   * @return the canvasCheck as string.
   */
  public static final String getCanvasValueSum(int id) {
    return "function getSum(total, num) {"
        + "    return total + num;"
        + "};"
        + "var canvas = document.createElement('canvas');"
        + "var ctx = canvas.getContext('2d');"
        + "var canvass = document.getElementsByTagName('canvas');"
        + "var canvas = canvass["
        + id
        + "];"
        + "if(canvas){"
        + "ctx.drawImage(canvas,0,0,canvas.height-1,canvas.width-1);"
        + "var imageData = ctx.getImageData(0,0,canvas.height-1,canvas.width-1).data;"
        + "var sum = imageData.reduce(getSum);"
        + "if (sum===255*(Math.pow(canvas.height-1,(canvas.width-1)*(canvas.width-1))))"
        + "   return 0;"
        + "return sum;"
        + "} else {"
        + "return 0 "
        + "}";
  }

  /**
   * Returns the test's checkPeerConnectionExistScript
   *
   * @return the string format of a boolean value returned from the JS console.
   */
  private static final String checkPeerConnectionExistScript() {
    return "var res;"
        + "try {res = pc} catch (exception) {} "
        + "if (res) {return true;} else {return false;}";
  }

  /**
   * @return whether the peer connection exists
   * @throws InterruptedException
   */
  public static boolean checkPeerConnectionObject(WebDriver webDriver, int TIMEOUT, int INTERVAL) throws InterruptedException {
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      boolean pcExist =
          (boolean)
              ((JavascriptExecutor) webDriver)
                  .executeScript(checkPeerConnectionExistScript());
      if (!pcExist) {
        Thread.sleep(INTERVAL);
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the test's checkIceConnectionStateScript
   *
   * @return the string format of a boolean value returned from the JS console.
   */
  public static final String checkIceConnectionStateScript() {
    return "var res;"
        + "try {res = pc.iceConnectionState} catch (exception) {} "
        + "if (res==='connected' || res==='completed' ) {return true;} else {return false;}";
  }



  /**
   * @return whether the peer connection state is connected
   * @throws InterruptedException
   */
  public static boolean checkPeerConnectionState(WebDriver webDriver, int TIMEOUT, int INTERVAL) throws InterruptedException {
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      boolean pcExist =
          (boolean)
              ((JavascriptExecutor) webDriver)
                  .executeScript(checkIceConnectionStateScript());
      if (!pcExist) {
        Thread.sleep(INTERVAL);
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the test's playVideoScript
   *
   * @return the string format .
   */
  public static final String playVideoScript(String id) {
    return "document.getElementById('"+id+"').play();";
  }

  /**
   * Returns the test's getSDPOfferScript to retrieve simulcast.pc.localDescription.sdp or
   * simulcast.pc.remoteDescription.sdp. If it doesn't exist then the method returns 'unknown'.
   *
   * @param local boolean
   * @return the getSDPOfferScript as string.
   */
  public static final String getSDPOfferScript(boolean local) {
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
   * Calls the playVideoScript function to play the video.
   *
   * @param video_id id the video in the list of video elements.
   * @throws InterruptedException
   */
  public static void playVideo(WebDriver webDriver, String video_id) throws InterruptedException {
    ((JavascriptExecutor) webDriver).executeScript(playVideoScript(video_id));
  }

  /**
   * Calls the getVideoValueSum function to verify video content.
   *
   * @param id index of the video in the list of video elements.
   * @return whether the video is actually showing.
   * @throws InterruptedException
   */
  public static boolean checkVideoDisplay(
          WebDriver webDriver, int id, int TIMEOUT, int INTERVAL) throws InterruptedException {
    long canvasData = 0;
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      canvasData =
              (Long) ((JavascriptExecutor) webDriver).executeScript(getVideoValueSum(id));
      if (canvasData == 0) {
        Thread.sleep(INTERVAL);
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Calls the getVideoValueSum function to verify video content.
   *
   * @param id index of the video in the list of video elements.
   * @return whether the video is actually showing.
   * @throws InterruptedException
   */
  public static boolean checkCanvasDisplay(
          WebDriver webDriver, int id, int TIMEOUT, int INTERVAL) throws InterruptedException {
    long canvasData = 0;
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      canvasData =
              (Long) ((JavascriptExecutor) webDriver).executeScript(getVideoValueSum(id));
      if (canvasData == 0) {
        Thread.sleep(INTERVAL);
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieves the log displayed in the page (on purpose)
   *
   * @return
   */
  public static String getLog(WebDriver webDriver) {
    String res = "";
    WebElement logElem = webDriver.findElement(By.id("logs"));
    if (logElem != null) {
      List<WebElement> logLines = logElem.findElements(By.tagName("li"));
      if (logLines.size() == 0) {
        return "NA";
      } else {
        for (WebElement logLine : logLines) {
          try {
            res += logLine.getText() + "/r/n";
          } catch (Exception e) {
            // do nothing
          }
        }
        return res;
      }
    }
    return res;
  }
}
