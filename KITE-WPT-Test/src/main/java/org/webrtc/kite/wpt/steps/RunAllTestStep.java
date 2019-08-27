package org.webrtc.kite.wpt.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import org.webrtc.kite.wpt.RunInfo;
import org.webrtc.kite.wpt.TestSummary;

import java.util.List;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.processTestStep;

public class RunAllTestStep extends TestStep {
  private final String revision;
  private final String testName;
  private final List<String> urlList;
  private TestSummary testSummary = new TestSummary();
  private final Runner runner;
  
  
  public RunAllTestStep(Runner runner, List<String> urlList, String revision, String testName) {
    super(runner);
    this.testName = testName;
    this.urlList = urlList;
    this.revision = revision;
    this.runner = runner;
  }
  
  @Override
  public void finish() {
    this.urlList.clear();
  }
  
  /**
   * Get information about the run from various sources.
   *
   * @return the information about the run.
   */
  private RunInfo getRunInfo() throws KiteTestException {
    try {
      RunInfo info = new RunInfo(this.revision);
      info.getInfoFromWebDriver(this.webDriver);
      logger.info("Run information: " + info.toString());
      return info;
    } catch (Exception e) {
      throw new KiteTestException("Exception getting run info from web driver", Status.BROKEN);
    }
  }
  
  @Override
  protected void step() throws KiteTestException {
    testSummary.setRunInfo(getRunInfo());
    if (urlList.isEmpty()) {
      throw new KiteTestException("Could not find any test, possibly because the page was not loaded correctly", Status.FAILED);
    }
    
    for (int index = 0; index < urlList.size(); index++) {
      String url = urlList.get(index);
      RunOneTestStep runOneTestStep = new RunOneTestStep(runner, url);
      logger.info("---->>> [" + (index + 1) + "/" + urlList.size() + "]");
      processTestStep(StepPhase.DEFAULT, runOneTestStep, this.report);
      testSummary.addResult(runOneTestStep.getTestResult());
    }
    reporter.textAttachment(this.report, "Test run summary", testSummary.toString(), "json");
    if (!this.report.broken()) {
      reporter.saveAttachmentToSubFolder(testSummary.getName(), testSummary.toString(), "json",
        "wpt-results/" + revision.substring(revision.length() - 7) + "/" + testName + "(" + timestamp().split(" ")[0] + ")");
    }
  }
  
  @Override
  public String stepDescription() {
    return "Running " + urlList.size() + " found test(s)";
  }
}
