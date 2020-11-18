package org.webrtc.kite.stats.rtc.mhandler;

import java.util.Map;
import javax.json.JsonObjectBuilder;
import org.webrtc.kite.stats.rtc.RTCSingleStatObject;

public class RTCMediaHandlerStats extends RTCSingleStatObject {
  protected final String trackIdentifier;

  public RTCMediaHandlerStats(Map statObject) {
    super(statObject);
    this.trackIdentifier = getStatByName( "trackIdentifier");
  }

  public String getTrackIdentifier() {
    return trackIdentifier;
  }


  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("trackIdentifier", this.trackIdentifier);
  }
}
