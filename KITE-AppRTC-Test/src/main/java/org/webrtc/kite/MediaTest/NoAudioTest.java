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

package org.webrtc.kite.MediaTest;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.webrtc.kite.KiteTest;
import org.webrtc.kite.stat.Utility;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * NoAudioTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of
 * WebDriver:
 * <ul>
 * <li>1) Opens the browser with the url specified in APPRTC_URL with option argument.</li>
 * <li>2) Clicks 'confirm-join-button'.</li>
 * <li>3) Do the following every 1 second for 1 minute:</li>
 * <ul>
 * <li>a) Executes the JavaScript on the given via getIceConnectionScript() which returns
 * iceConnectionState.</li>
 * <li>b) Checks whether the state is at least 'new'.</li>
 * <li>c) Executes the JavaScript on the given via stashResultScript() which store result and stats
 * in a global variable to fetch later.</li>
 * <li>d) Executes the JavaScript on the given via checkResultScript() which returns the result
 * stashed earlier.</li>
 * <li>e) Executes the JavaScript on the given via getStatsScript() which returns the stats
 * stashed earlier.</li>
 * </ul>
 * <li>4) The test is considered as successful if there's no outbound audio stream.</li>
 * <li>5) A successful test returns a boolean 'true' while the unsuccessful test returns a boolean
 * 'false'.</li>
 * </ul>
 * </p>
 */
public class NoAudioTest extends KiteTest {

    private final static Logger logger = Logger.getLogger(NoAudioTest.class.getName());

    private final static Map<String, String> expectedResultMap = new HashMap<String, String>();
    private final static String APPRTC_URL = "https://appr.tc/r/";
    private final static int TIMEOUT = 60000;
    private final static int INTERVAL = 1000;
    private static String alertText = null;

    static {
        expectedResultMap.put("completed", "completed");
        expectedResultMap.put("connected", "connected");
    }
    private final String value = "false";
    private final String option = "audio=" + value;

    /**
     * Returns the test's getIceConnectionScript to retrieve appController.call_.pcClient_.pc_.iceConnectionState.
     * If it doesn't exist then the method returns 'unknown'.
     *
     * @return the getIceConnectionScript as string.
     */
    private final static String getIceConnectionScript() {
        return "var retValue;" +
                "try {" +
                "   retValue = appController.call_.pcClient_.pc_.iceConnectionState;" +
                "} catch (exception) {} " +
                "if (retValue) {" +
                "   return retValue;" +
                "} else {" +
                "   return 'unknown';}";
    }

    /**
     * Returns the test's stashResultScript to stash the result and stats of the test in a global variable to retrieve later.
     *
     * @return the stashResultScript as string.
     */
    private final static String stashResultScript() {
        return "window.result = 'SUCCESSFUL';" +
                "var outbound = false;" +
                "appController.call_.pcClient_.pc_.getStats()" +
                "   .then(data => {            " +
                "   window.KITEStats = [...data.values()];" +
                "   [...data.values()].forEach(function(e){" +
                "     if (e.type.startsWith('outbound')){" +
                "       outbound = true;" +
                "       if (e.mediaType.startsWith('audio')) " +
                "           window.result = 'FAILED';" +
                "       }" +
                "   });" +
                "   if (!outbound)" +
                "     window.result = 'ERROR';" +
                "});";
    }


    /**
     * Returns the test's stashStatsScript to stash the stats of the test in a global variable to retrieve later.
     *
     * @return the stashStatsScript as string.
     */
    private final static String stashStatsScript() {
        return "const getStatsValues = () =>" +
                "  appController.call_.pcClient_.pc_.getStats()" +
                "    .then(data => {" +
                "      return [...data.values()];" +
                "    });" +
                "const stashStats = async () => {" +
                "  window.KITEStats = await getStatsValues();" +
                "  return 0;" +
                "};" +
                "stashStats();" +
                "return 0";
    }

    /**
     * Returns the test's getResultScript to get the stashed result.
     *
     * @return the getResultScript as string.
     */
    private final static String getResultScript() {
        return "return window.result;";
    }

    /**
     * Returns the test's getResultScript to get the stashed stats.
     *
     * @return the getStatsScript as string.
     */
    private final static String getStatsScript() {
        return "return window.KITEStats;";
    }

    /**
     * Opens the APPRTC_URL and clicks 'confirm-join-button'.
     */
    /**
     * Opens the APPRTC_URL and clicks 'confirm-join-button'.
     */
    private void takeAction() {
        Random rand = new Random(System.currentTimeMillis());
        long channel = Math.abs(rand.nextLong());

        for (WebDriver webDriver : this.getWebDriverList()) {
            webDriver.get(APPRTC_URL + channel + "?" + option);
            try {
                Alert alert = webDriver.switchTo().alert();
                alertText = alert.getText();
                alert.accept();
            } catch (NoAlertPresentException e) {
                logger.warn(e.getLocalizedMessage());
            }
            webDriver.findElement(By.id("confirm-join-button")).click();
        }
    }
    /**
     * Checks whether all of the result strings match at least one entry from the expectedResultMap.
     *
     * @param resultList ice connection states for the browsers as List<String>
     * @return true if all of the results strings match at least one entry from the expectedResultMap.
     */
    private boolean validateResults(List<String> resultList) {
        for (String result : resultList)
            if (expectedResultMap.get(result) == null)
                return false;
        return true;
    }

    /**
     * Checks whether atleast one the result string matches 'failed'.
     *
     * @param resultList ice connection states for the browsers as List<String>
     * @return true if atleast one of the result string matches 'failed'.
     */
    private boolean checkForFailed(List<String> resultList) {
        for (String result : resultList)
            if (result.equalsIgnoreCase("failed"))
                return true;
        return false;
    }


    @Override
    public Object testScript() throws Exception {
        this.takeAction();
        if (alertText != null) {
            throw new UnhandledAlertException(alertText);
        }
        String result = "TIME OUT";
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (int i = 0; i < TIMEOUT; i += INTERVAL) {
            List<String> resultList = new ArrayList<String>();
            for (WebDriver webDriver : this.getWebDriverList()) {
                String resultOfScript =
                        (String) ((JavascriptExecutor) webDriver).executeScript(getIceConnectionScript());
                if (logger.isInfoEnabled())
                    logger.info(webDriver + ": " + resultOfScript);
                resultList.add(resultOfScript);
            }

            if (this.checkForFailed(resultList)) {
                result = "FAILED";
                int count = 1;
                for (WebDriver webDriver : this.getWebDriverList()) {
                    ((JavascriptExecutor) webDriver).executeScript(this.stashStatsScript());
                    Thread.sleep(INTERVAL);
                    Object stats = ((JavascriptExecutor) webDriver).executeScript(this.getStatsScript());
                    resultMap.put("client_" + count, stats);
                    count += 1;
                }
                break;
            } else if (this.validateResults(resultList)) {
                result = "SUCCESSFUL";
                int count = 1;
                for (WebDriver webDriver : this.getWebDriverList()) {
                    ((JavascriptExecutor) webDriver).executeScript(stashResultScript());
                    Thread.sleep(INTERVAL);
                    Object stats = ((JavascriptExecutor) webDriver).executeScript(getStatsScript());
                    result = (String) ((JavascriptExecutor) webDriver).executeScript(getResultScript());
                    resultMap.put("client_" + count, stats);
                    count += 1;
                }
                break;
            }
        }
        if (result.equalsIgnoreCase("ERROR"))
            return new Exception("No outbound stream was found by getStats()");
        else {
            resultMap.put("result", result);
            JsonObjectBuilder tmp = Json.createObjectBuilder();
            for (int i = 1; i <= this.getWebDriverList().size(); i++) {
                String name = "client_"+i;
                if (resultMap.get(name)!=null)
                    tmp.add(name, Utility.buildStatObject(resultMap.get(name)));
            }

            return Json.createObjectBuilder().add("result", (String) resultMap.get("result"))
                    .add("stats", tmp).build().toString();
        }
    }
}
