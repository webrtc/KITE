package org.webrtc.kite.wpt.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.TestStep;
import org.webrtc.kite.wpt.Result;
import org.webrtc.kite.wpt.pages.WPTTestPage;

import java.util.Arrays;
import java.util.List;

public class RunOneTestStep extends TestStep {
  private final String url;
  private final WPTTestPage wptTestPage;
  
  
  public RunOneTestStep(Runner runner, String url) {
    super(runner);
    this.url = url;
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
      throw new KiteTestException("No sub tests was found, possibly because connection problem", Status.BROKEN);
    }
    reporter.textAttachment(this.report, "Test result", result.toString(), "json");
    if (result.failed()) {
      this.report.setStatus(Status.FAILED);
      this.report.setIgnore(true);
    }
  }
  
  @Override
  public String stepDescription() {
    List<String> brokenDownUrl = Arrays.asList(this.url.split("/"));
    return "Run and get result: " + brokenDownUrl.get(brokenDownUrl.size() - 1);
  }
}
