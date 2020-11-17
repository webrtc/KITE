package org.webrtc.kite.stats.rtc.msource;

import java.util.Map;
import javax.json.JsonObjectBuilder;

public class RTCAudioSourceStats extends RTCMediaSourceStats {
  protected final String  totalAudioEnergy;
  protected final String totalSamplesDuration;

  public RTCAudioSourceStats(Map statObject) {
    super(statObject);
    this.totalAudioEnergy = getStatByName( "totalAudioEnergy");
    this.totalSamplesDuration = getStatByName( "totalSamplesDuration");
  }

  public String getTotalAudioEnergy() {
    return totalAudioEnergy;
  }

  public String getTotalSamplesDuration() {
    return totalSamplesDuration;
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("totalAudioEnergy", this.totalAudioEnergy)
        .add("totalSamplesDuration", this.totalSamplesDuration);
  }
}
