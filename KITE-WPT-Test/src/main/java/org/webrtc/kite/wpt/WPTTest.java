package org.webrtc.kite.wpt;

import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.TestRunner;
import org.webrtc.kite.wpt.steps.RetrieveTestStep;
import org.webrtc.kite.wpt.steps.RunAllTestStep;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * WPTest implementation of KiteTest.
 * Checks and retrieves the tests and sub tests from WPT server.
 * Runs the tests and retrieves the results displayed on the page.
 */
public class WPTTest extends KiteBaseTest {
  

  private String securedURL;
  private String nonSecuredURL;
  private String revision;
  private List<String> tests = new ArrayList<>();
  private List<String> testURLList = new ArrayList<>();
  
  
  @Override
  public void populateTestSteps(TestRunner runner) {
    for (String test : tests) {
      runner.addStep(new RetrieveTestStep(runner.getWebDriver(), nonSecuredURL + test, securedURL + test, testURLList));
      runner.addStep(new RunAllTestStep(runner.getWebDriver(), testURLList, this.revision, test));
    }
  }
  
  @Override
  protected void payloadHandling() {
    if (this.payload != null) {
      JsonObject payload = (JsonObject) this.payload;
      this.securedURL = payload.getString("securedURL");
      this.nonSecuredURL = payload.getString("nonSecuredURL");
      if (!this.securedURL.endsWith("/")) {
        this.securedURL += "/";
      }
      if (!this.nonSecuredURL.endsWith("/")) {
        this.nonSecuredURL += "/";
      }
      this.revision = payload.getString("revision");
      if (payload.get("tests") == null) {
        logger.warn("Running all tests is not yet implemented. Currently supported: \r\n" +
          "webrtc \r\n" +
          "mediacapture-streams \r\n");
      } else {
        JsonArray testArray = payload.getJsonArray("tests");
        logger.info("Running tests for: " + testArray.toString());
        for (int index = 0; index < testArray.size(); index ++) {
          tests.add(testArray.getString(index));
        }
      }
    }
  }
  
}