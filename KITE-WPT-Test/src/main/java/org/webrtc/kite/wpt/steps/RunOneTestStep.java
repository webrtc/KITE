package org.webrtc.kite.wpt.steps;

import static io.cosmosoftware.kite.util.TestUtils.waitAround;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.WebDriverException;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.tests.InitClientWebDriverStep;
import org.webrtc.kite.tests.TestRunner;
import org.webrtc.kite.wpt.Result;
import org.webrtc.kite.wpt.pages.WPTTestPage;

public class RunOneTestStep extends TestStep {
  private final String url;
  private final String progress;
  private WPTTestPage wptTestPage;
  private final TestRunner runner;
  private final Client client;


  public RunOneTestStep(TestRunner runner, String url, String progress) {
    super(runner);
    this.runner = runner;
    this.client = this.runner.getClient();
    this.url = url;
    this.progress = progress;
    this.setOptional(true);
    this.setIgnoreBroken(true);
    this.setScreenShotOnFailure(false);
  }

  public Result getTestResult() {
    return this.wptTestPage.getResultReport();
  }

  @Override
  public void step() throws KiteTestException {
    this.wptTestPage = new WPTTestPage(runner);
    this.wptTestPage.setPageUrl(url);
    this.report.addParam("test-url", url);
    try {
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
    } catch (WebDriverException e) {
      // try to re-initiate the crashed web driver
        if (e.getMessage().contains("session deleted because of page crash") || e.getMessage().contains("was terminated")) {
          this.client.removeWebdriver();
          InitClientWebDriverStep initClientWebDriverStep = new InitClientWebDriverStep(this.runner, this.runner.getId(), this.client,null);
          initClientWebDriverStep.processTestStep(this.getStepPhase(), this.report, false);
          this.webDriver = initClientWebDriverStep.getWebDriver();
          this.runner.setWebDriver(this.webDriver);
        }
      }
  }

  @Override
  public String stepDescription() {
    List<String> brokenDownUrl = Arrays.asList(this.url.split("/"));
    return this.progress
        + " Run and get result for -> "
        + brokenDownUrl.get(brokenDownUrl.size() - 1);
  }
}
