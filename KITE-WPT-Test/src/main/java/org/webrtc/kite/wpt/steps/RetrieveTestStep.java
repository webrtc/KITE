package org.webrtc.kite.wpt.steps;

import static io.cosmosoftware.kite.util.TestUtils.processTestStep;
import static io.cosmosoftware.kite.util.TestUtils.verifyPathFormat;

import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.ArrayList;
import java.util.List;
import org.webrtc.kite.wpt.pages.WPTDirPage;

public class RetrieveTestStep extends TestStep {
  private final String sercureURL;
  private final String url;
  private final WPTDirPage wptDirPage;
  private final Runner runner;
  List<String> testUrlList;

  public RetrieveTestStep(Runner runner, String url, String sercureURL, List<String> testUrlList) {
    super(runner);
    this.url = verifyPathFormat(url);
    this.runner = runner;
    this.sercureURL = verifyPathFormat(sercureURL);
    this.wptDirPage = new WPTDirPage(runner, url);
    this.testUrlList = testUrlList;
  }

  private boolean isHttps(String testName) {
    if (testName.contains("https")) {
      return true;
    } else {
      if ( // leave place for more test filtering
      testName.contains("supported-by-feature-policy [NO]") // this is only on safari ????
      // another condition...
      ) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  protected void step() {
    List<String> temp = new ArrayList<>();
    wptDirPage.openPage();
    for (String testName : wptDirPage.getTestList()) {
      if (isHttps(testName)) {
        temp.add(sercureURL + testName);
      } else {
        temp.add(url + testName);
      }
    }
    testUrlList.addAll(temp);
    logger.info("Found : " + temp.size() + " test(s) in :" + url);
    logger.info("Total is now: " + testUrlList.size() + " test(s)");
    for (String dirName : wptDirPage.getDirNameList()) {
      processTestStep(
          StepPhase.DEFAULT,
          new RetrieveTestStep(runner, url + dirName, sercureURL + dirName, testUrlList),
          this.report);
    }
    reporter.textAttachment(this.report, "List of tests", temp.toString(), "plain");
  }

  @Override
  public String stepDescription() {
    return "Get test from: " + url;
  }
}
