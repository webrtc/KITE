package org.webrtc.kite.stats;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class EmptyStatObject extends RTCSingleStatObject {
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder();
  }
}
