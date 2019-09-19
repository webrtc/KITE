package org.webrtc.kite.wpt.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.Arrays;
import java.util.List;
import org.webrtc.kite.wpt.Result;
import org.webrtc.kite.wpt.pages.WPTTestPage;

public class RunOneTestStep extends TestStep {
  private final String url;
  private final String progress;
  private final WPTTestPage wptTestPage;

  public RunOneTestStep(Runner runner, String url, String progress) {
    super(runner);
    this.url = url;
    this.progress = progress;
    this.setOptional(true);
    wptTestPage = new WPTTestPage(runner, url);
  }

  public Result getTestResult() {
    return this.wptTestPage.getResultReport();
  }

  @Override
  public void step() throws KiteTestException {
    this.report.addParam("test-url", url);
    wptTestPage.runTest();
    wptTestPage.fillResultReport();
    Result result = wptTestPage.getResultReport();
    if (result.isBroken()) {
      throw new KiteTestException(
          "No sub tests was found, possibly because connection problem", Status.BROKEN);
    }
    reporter.textAttachment(this.report, "Test result", result.toString(), "json");
    if (result.failed()) {
      this.report.setStatus(Status.FAILED);
      this.report.setIgnore(true);
    }
    logger.info(
        "Result: " + result.getSucessCount() + " successes out of " + result.size() + " sub tests");
  }

  @Override
  public String stepDescription() {
    List<String> brokenDownUrl = Arrays.asList(this.url.split("/"));
    return this.progress
        + " Run and get result for -> "
        + brokenDownUrl.get(brokenDownUrl.size() - 1);
  }
}
