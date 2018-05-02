package org.webrtc.kite.pojo.Stats;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CandidateStat {
  private String candidateType;
  private int numberOfCandidates;
  private List<JsonObject> candidate_obj_list = new ArrayList<>();
  private List<String> candidate_name_list = new ArrayList<>();
  ;

  public CandidateStat(JsonArray statArray, String candidateType) {
    this.candidateType = candidateType + "-candidate";
    if (statArray != null) {
      for (JsonValue statJsonValue : statArray) {
        JsonObject statJsonObj = (JsonObject) statJsonValue;
        Set<String> statTypeList = (statJsonObj).keySet();
        for (String type : statTypeList) {
          if (type.equalsIgnoreCase(this.candidateType)) {
            JsonObject candidate_json = statJsonObj.getJsonObject(type);
            this.candidate_name_list = new ArrayList<>(candidate_json.keySet());
            for (String candidate : this.candidate_name_list) {
              this.candidate_obj_list.add(candidate_json.getJsonObject(candidate));
            }
          }
        }
      }
      this.numberOfCandidates = this.candidate_name_list.size();
    }
  }

  public List<JsonObject> getCandidate_obj_list() {
    return candidate_obj_list;
  }

  public String getJsonData() {

    String jsonData = "\"" + this.candidateType + "\":{";
    jsonData += "\"count\":" + this.numberOfCandidates + ",";
    jsonData += "\"candidates\": {";

    if (this.numberOfCandidates > 0) {
      for (int i = 0; i < this.numberOfCandidates; i++) {
        JsonObject candidate = this.candidate_obj_list.get(i);
        jsonData += "\"" + this.candidate_name_list.get(i) + "\":{";
        jsonData += "\"ip\":" + "\"" + candidate.getString("ip", "n/a") + "\",";
        jsonData += "\"port\":" + "\"" + candidate.getString("port", "n/a") + "\",";
        jsonData += "\"protocol\":" + "\"" + candidate.getString("protocol", "n/a") + "\",";
        jsonData += "\"candidateType\":" + "\"" + candidate.getString("candidateType", "n/a") + "\",";
        jsonData += "\"priority\":" + "\"" + candidate.getString("priority", "n/a") + "\",";
        jsonData += "\"url\":" + "\"" + candidate.getString("url", "n/a") + "\"";
        jsonData += "}";
        if (i < this.numberOfCandidates - 1)
          jsonData += ",";
      }
    }
    jsonData += "}";
    jsonData += "}";
    return jsonData;
  }
}
