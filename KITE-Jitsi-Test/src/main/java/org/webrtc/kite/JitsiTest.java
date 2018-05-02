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

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * JitsiTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of WebDriver:
 * 1) Opens all the browsers with the url specified in JITSI_URL.
 * 2) Do the following every 1 second for 1 minute:
 * a) Executes the JavaScript on all browsers given via getConnectionScript() which returns iceConnectionState.
 * b) Executes the JavaScript on all browsers given via getNumberOfParticipantsScript() which returns number of participants.
 * b) Checks whether all the browsers have returned either 'completed' or 'connected' for iceConnectionState and correct
 * number of participants.
 * 4) The test is considered as successful if all the browsers either returns 'completed' or 'connected' with correct
 * number of participants within 1 minute.
 * 5) A successful test returns a boolean 'true' while the unsuccessful test returns a boolean 'false'
 */
public class JitsiTest extends KiteTest {

    private final static Logger logger = Logger.getLogger(JitsiTest.class.getName());

    private final static Map<String, String> expectedResultMap = new HashMap<String, String>();
    static {
        expectedResultMap.put("completed", "completed");
        expectedResultMap.put("connected", "connected");
    }

    private final static String JITSI_URL = "https://meet.jit.si/";
    private final static int TIMEOUT = 600000;
    private final static int INTERVAL = 1000;
    private static String alertMsg;


    /**
     * Opens the JITSI_URL with random room number.
     */
    private void takeAction() {
        Random rand = new Random(System.currentTimeMillis());
        long channel = Math.abs(rand.nextLong());

        for (WebDriver webDriver : this.getWebDriverList()) {
            webDriver.get(JITSI_URL + channel);
            try {
                Alert alert = webDriver.switchTo().alert();
                alertMsg = alert.getText();
                if (alertMsg != null) {
                    alertMsg = ((RemoteWebDriver) webDriver).getCapabilities().getBrowserName() + " alert: " +alertMsg;
                    logger.warn(alertMsg);
                    alert.accept();
                }
            } catch (NoAlertPresentException e) {
                alertMsg = null;
            }
        }
    }

    /**
     * Returns the test getConnectionScript to retrieve APP.conference.getConnectionState().
     * If it doesn't exist then the method returns 'unknown'.
     *
     * @return the getConnectionScript as string.
     */
    private String getConnectionScript() {
        return "var retValue;" +
                "try {" +
                "retValue = APP.conference.getConnectionState();" +
                "} catch (exception) {} " +
                "if (retValue) {return retValue;} else {return 'unknown';}";
    }

    /**
     * Returns the test getNumberOfParticipantsScript to retrieve APP.conference.membersCount.
     * If it doesn't exist then the method returns 0.
     *
     * @return the getNumberOfParticipantsScript as string.
     */
    private String getNumberOfParticipantsScript() {
        return "var retValue;" +
                "try {" +
                "retValue = APP.conference.membersCount;" +
                "} catch (exception) {} " +
                "if (retValue) {return retValue;} else {return 0;}";
    }

    /**
     * Checks whether all of the iceConnectionState strings match at least one entry from the expectedResultMap.
     * Checks whether all of the participant numbers match the tuple size.
     *
     * @param resultList ice connection states for the browsers as List<String>
     * @return true if all of the results strings match at least one entry from the expectedResultMap.
     */
    private boolean validateResults(List<RoomState> resultList) {
        int numberOfParticipants = this.getWebDriverList().size();
        for (RoomState room : resultList)
            if ( (expectedResultMap.get(room.getState()) == null) || room.getHeadCount() == 0) {
                return false;
            } else{
                if (room.getHeadCount()!=numberOfParticipants) {
                    return false;
                }
            }
        return true;
    }


    /**
     * Checks whether at least one the room state string matches 'failed'.
     *
     * @param resultList ice connection states for the browsers as List<String>
     * @return true if atleast one of the result string matches 'failed'.
     */
    private boolean checkForFailed(List<RoomState> resultList) {
        for (RoomState room : resultList) {
            if (room.getState().equalsIgnoreCase("failed")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object testScript() throws Exception {
        this.takeAction();
/*    if (alertMsg != null) {
      return Json.createObjectBuilder().add("result", alertMsg).build().toString();
    }*/
        String result = "TIME OUT";

        for (int i = 0; i < TIMEOUT; i += INTERVAL) {

            List<RoomState> resultList = new ArrayList<RoomState>();
            for (WebDriver webDriver : this.getWebDriverList()) {
                String connectionState = (String) ((JavascriptExecutor) webDriver).executeScript(this.getConnectionScript());
                long numberOfParticipants = (Long) ((JavascriptExecutor) webDriver).executeScript(this.getNumberOfParticipantsScript());
                if (logger.isInfoEnabled())
                    logger.info(webDriver + ": " + connectionState + " and Counted: "+ numberOfParticipants);
                resultList.add(new RoomState(connectionState,numberOfParticipants));
            }

            if (this.checkForFailed(resultList)) {
                result = "FAILED";
                break;
            } else if (this.validateResults(resultList)) {
                result = "SUCCESSFUL";
                break;
            } else {
                Thread.sleep(INTERVAL);
            }
        }

        return result;
    }

    /**
     * A class representing the relevant parameters of the room from a participant's perspective.
     *
     */
    public static class RoomState {
        private String state;
        private long headCount;

        RoomState(String state, long headCount){
            this.state = state;
            this.headCount = headCount;
        }

        public long getHeadCount() {
            return headCount;
        }

        public String getState() {
            return state;
        }

        public String toJsonString(){
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("connectionState",this.getState())
                    .add("numberOfParticipants", this.getHeadCount());

            JsonObject result = jsonObjectBuilder.build();
            return result.toString();
        }
    }
}
