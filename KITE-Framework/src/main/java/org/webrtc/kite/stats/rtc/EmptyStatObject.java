package org.webrtc.kite.stats.rtc;

import java.util.Map;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class EmptyStatObject extends RTCSingleStatObject {

  public EmptyStatObject(Map statObject) {
    super(statObject);
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder();
  }
}
