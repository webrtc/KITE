package org.webrtc.kite.wpt;

import io.cosmosoftware.kite.report.Category;
import io.cosmosoftware.kite.report.FailedCategory;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.TestRunner;
import org.webrtc.kite.wpt.steps.RetrieveTestStep;
import org.webrtc.kite.wpt.steps.RunAllTestStep;

/**
 * WPTest implementation of KiteTest. Checks and retrieves the tests and sub tests from WPT server.
 * Runs the tests and retrieves the results displayed on the page.
 */
public class WPTTest extends KiteBaseTest {

  private String nonSecuredURL;
  private String revision;
  private String securedURL;
  private int reportStyle;
  private List<String> testURLList = new ArrayList<>();
  private List<String> tests = new ArrayList<>();

  @Override
  protected void payloadHandling() {
    if (this.payload != null) {
      JsonObject payload = this.payload;
      this.securedURL = payload.getString("securedURL");
      this.nonSecuredURL = payload.getString("nonSecuredURL");
      if (!this.securedURL.endsWith("/")) {
        this.securedURL += "/";
      }
      if (!this.nonSecuredURL.endsWith("/")) {
        this.nonSecuredURL += "/";
      }
      this.revision = payload.getString("revision");
      this.reportStyle = payload.getInt("reportStyle", 1);
      this.reporter.addEnvironmentParam("revision ", this.revision);
      this.reporter.addEnvironmentParam("secure-URL ", this.securedURL);
      this.reporter.addEnvironmentParam("non-secured-URL ", this.nonSecuredURL);
      this.reporter.addEnvironmentParam(
          "report-style ", this.reportStyle == 1 ? "Grouped" : "Individually");
      if (payload.get("tests") == null) {
        logger.warn(
            "Running all tests is not yet implemented. Currently supported: \r\n"
                + "webrtc \r\n"
                + "mediacapture-streams \r\n");
      } else {
        JsonArray testArray = payload.getJsonArray("tests");
        logger.info("Running tests for: " + testArray.toString());
        for (int index = 0; index < testArray.size(); index++) {
          tests.add(testArray.getString(index));
        }
      }
    }
  }

  @Override
  protected void addExtraCategories() {
    super.addExtraCategories();
    Category decodingIssues = new FailedCategory("Decoding Issues");
    decodingIssues.setMessageRegex("decoding");
    this.reporter.addCategory(decodingIssues);
    Category encodingIssues = new FailedCategory("Encoding Issues");
    encodingIssues.setMessageRegex("encoding");
    this.reporter.addCategory(encodingIssues);
    Category mediaIssues = new FailedCategory("Media Issues");
    mediaIssues.setMessageRegex("edia");
    this.reporter.addCategory(mediaIssues);
    Category codecIssues = new FailedCategory("Codec Issues");
    codecIssues.setMessageRegex("odec");
    this.reporter.addCategory(codecIssues);
    Category captureIssues = new FailedCategory("Capture Issues");
    captureIssues.setMessageRegex("apture");
    this.reporter.addCategory(captureIssues);
    Category streamIssues = new FailedCategory("Stream Issues");
    streamIssues.setMessageRegex("tream");
    this.reporter.addCategory(streamIssues);
    Category trackIssues = new FailedCategory("Track Issues");
    trackIssues.setMessageRegex("rack");
    this.reporter.addCategory(trackIssues);
    Category recorderIssues = new FailedCategory("Recorder Issues");
    recorderIssues.setMessageRegex("ecorder");
    this.reporter.addCategory(recorderIssues);
    Category UMIssues = new FailedCategory("User Media Issues");
    UMIssues.setMessageRegex("UserMedia");
    this.reporter.addCategory(UMIssues);
    Category stateIssues = new FailedCategory("State Issues");
    stateIssues.setMessageRegex("tate");
    this.reporter.addCategory(stateIssues);
    Category configIssues = new FailedCategory("Configuration Issues");
    configIssues.setMessageRegex("onfig");
    this.reporter.addCategory(configIssues);
    Category candidateIssues = new FailedCategory("Candidate Issues");
    candidateIssues.setMessageRegex("andidate");
    this.reporter.addCategory(candidateIssues);
    Category channelIssues = new FailedCategory("Channel Issues");
    channelIssues.setMessageRegex("hannel");
    this.reporter.addCategory(channelIssues);
    Category certificateIssues = new FailedCategory("Certificate Issues");
    certificateIssues.setMessageRegex("ertificate");
    this.reporter.addCategory(certificateIssues);
    Category tranceiverIssues = new FailedCategory("Transceiver Issues");
    tranceiverIssues.setMessageRegex("ransceiver");
    this.reporter.addCategory(tranceiverIssues);
    Category offerIssues = new FailedCategory("Offer Issues");
    offerIssues.setMessageRegex("offer");
    this.reporter.addCategory(offerIssues);
    Category parameterIssues = new FailedCategory("Parameters Issues");
    parameterIssues.setMessageRegex("arameter");
    this.reporter.addCategory(parameterIssues);
  }

  @Override
  public void populateTestSteps(TestRunner runner) {
    for (String test : tests) {
      runner.addStep(
          new RetrieveTestStep(runner, nonSecuredURL + test, securedURL + test, testURLList));
      runner.addStep(
          new RunAllTestStep(
              runner,
              testURLList,
              this.revision,
              test,
              this.generateTestCaseName(),
              this.reportStyle));
    }
  }
}
