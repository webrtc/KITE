package org.webrtc.kite.stats.rtc.msource;

import java.util.Map;
import javax.json.JsonObjectBuilder;

public class RTCVideoSourceStats extends RTCMediaSourceStats {
  protected final String width;
  protected final String height;
  protected final String framesPerSecond;

  public RTCVideoSourceStats(Map statObject) {
    super(statObject);
    this.width = getStatByName( "width");
    this.height = getStatByName( "height");
    this.framesPerSecond = getStatByName( "framesPerSecond");
  }

  public String getWidth() {
    return width;
  }

  public String getHeight() {
    return height;
  }

  public String getFramesPerSecond() {
    return framesPerSecond;
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("width", this.width)
        .add("height", this.height)
        .add("framesPerSecond", this.framesPerSecond);
  }
}
