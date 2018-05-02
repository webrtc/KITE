package org.webrtc.kite.pojo.Stats;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CandidatePairStat {

  private int numberOfPairs;
  private List<JsonObject> candidate_pair_obj_list = new ArrayList<>();
  private List<String> candidate_pair_list;

  public CandidatePairStat(JsonArray statArray) {
    if (statArray != null) {
      JsonValue statJsonValue = statArray.get(statArray.size() - 2);
      JsonObject statJsonObj = (JsonObject) statJsonValue;
      Set<String> statTypeList = (statJsonObj).keySet();
      for (String type : statTypeList) {
        if (type.equalsIgnoreCase("candidate-pair")) {
          JsonObject candidate_json = statJsonObj.getJsonObject(type);
          this.candidate_pair_list = new ArrayList<>(candidate_json.keySet());
          for (String candidate : this.candidate_pair_list) {
            this.candidate_pair_obj_list.add(candidate_json.getJsonObject(candidate));
          }
        }
      }
      if (this.candidate_pair_list != null)
        this.numberOfPairs = this.candidate_pair_list.size();
      else
        this.numberOfPairs = 0;
    }
  }


  public String getJsonData() {
    String jsonData = "\"candidates\":{";
    jsonData += "\"count\":" + this.numberOfPairs + ",";
    jsonData += "\"candidates\": [";

    if (this.numberOfPairs > 0) {
      for (int i = 0; i < this.numberOfPairs; i++) {
        JsonObject candidate_pair = this.candidate_pair_obj_list.get(i);
        jsonData += "{";
        jsonData += "\"transportId\":" + "\"" + candidate_pair.getString("transportId", "n/a") + "\",";
        String localID = candidate_pair.getString("localCandidateId", "n/a");
                /*if (localID.startsWith("RTCIceCandidate"))
                    localID = localID.split("RTCIceCandidate_")[1];*/
        jsonData += "\"localCandidateId\":" + "\"" + localID + "\",";
        String remoteID = candidate_pair.getString("remoteCandidateId", "n/a");
                /*if (remoteID.startsWith("RTCIceCandidate"))
                    remoteID = remoteID.split("RTCIceCandidate_")[1];*/
        jsonData += "\"remoteCandidateId\":" + "\"" + remoteID + "\",";
        jsonData += "\"state\":" + "\"" + candidate_pair.getString("state", "n/a") + "\",";
        jsonData += "\"priority\":" + "\"" + candidate_pair.getString("priority", "n/a") + "\",";
        jsonData += "\"nominated\":" + "\"" + candidate_pair.getString("nominated", "n/a") + "\",";
        jsonData += "\"bytesSent\":" + "\"" + candidate_pair.getString("bytesSent", "n/a") + "\",";
        jsonData += "\"bytesReceived\":" + "\"" + candidate_pair.getString("bytesReceived", "n/a") + "\"}";
        if (i < this.numberOfPairs - 1)
          jsonData += ",";
      }
    }
    jsonData += "]";
    jsonData += "}";
    return jsonData;
  }
}
