package org.webrtc.kite.wpt.steps;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.processTestStep;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.AllureStepReport;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.Container;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.report.StatusDetails;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.List;
import org.webrtc.kite.wpt.Result;
import org.webrtc.kite.wpt.RunInfo;
import org.webrtc.kite.wpt.SubTest;
import org.webrtc.kite.wpt.TestSummary;

public class RunAllTestStep extends TestStep {
  private final String revision;
  private final String testCaseName;
  private final String testName;
  private final List<String> urlList;
  private final Runner runner;
  private final int reportStyle;
  private TestSummary testSummary = new TestSummary();

  public RunAllTestStep(
      Runner runner,
      List<String> urlList,
      String revision,
      String testName,
      String testCaseName,
      int reportStyle) {
    super(runner);
    this.testCaseName = testCaseName.split(" ")[0];
    this.testName = testName;
    this.urlList = urlList;
    this.revision = revision;
    this.runner = runner;
    this.reportStyle = reportStyle;
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
      throw new KiteTestException(
          "Could not find any test, possibly because the page was not loaded correctly",
          Status.FAILED);
    }
    int total = 0;
    Container testSuite = new Container(testName);
    testSuite.setParentSuite(this.report.getName() + "-" + this.revision);
    testSuite.setReporter(this.reporter);

    for (int index = 0; index < urlList.size(); index++) {
      String url = urlList.get(index);
      RunOneTestStep runOneTestStep =
          new RunOneTestStep(runner, url, "[" + (index + 1) + "/" + urlList.size() + "]");
      long start = System.currentTimeMillis();
      processTestStep(StepPhase.DEFAULT, runOneTestStep, this.report);
      long stop = System.currentTimeMillis();
      Result result = runOneTestStep.getTestResult();
      total += result.size();
      testSummary.addResult(result);
      switch (reportStyle) {
        case 1:
          { // grouped
            AllureTestReport testReport = new AllureTestReport(urlList.get(index));
            testSuite.addChild(testReport.getUuid());
            testReport.addLabel("parentSuite", this.testCaseName);
            testReport.addLabel("suite", testSuite.getName());
            testReport.setFullName(this.logger.getName() + "_" + urlList.get(index));
            testReport.setReporter(this.reporter);
            makeGroupedResults(testReport, result, start, stop);
            break;
          }
        case 2:
          { // individual
            makeIndividualResults(testSuite, urlList.get(index), result, start, stop);
            break;
          }
      }
    }
    // testSuite.setName(testSuite.getName() + " - " + total + " tests");
    reporter.textAttachment(this.report, "Test run summary", testSummary.toString(), "json");
    if (!this.report.broken()) {
      reporter.saveAttachmentToSubFolder(
          testSummary.getName(),
          testSummary.toString(),
          "json",
          "wpt-results/"
              + revision.substring(revision.length() - 7)
              + "/"
              + testName
              + "("
              + timestamp().split(" ")[0]
              + ")");
    }
  }

  private void makeIndividualResults(
      Container testSuite, String url, Result result, long start, long stop) {
    for (SubTest subTest : result) {
      AllureTestReport testReport = new AllureTestReport(subTest.getName());
      testReport.setDescription(url);
      testReport.setStartTimestamp(start);
      testReport.setStopTimestamp(stop);
      testSuite.addChild(testReport.getUuid());
      testReport.addLabel("parentSuite", this.testCaseName);
      testReport.addLabel("suite", testSuite.getName());
      testReport.setFullName(this.logger.getName() + "_" + url);
      testReport.setReporter(this.reporter);
      AllureStepReport subStepReport = new AllureStepReport(subTest.getName());
      subStepReport.setStartTimestamp(start);
      subStepReport.setIgnore(true);
      if (!subTest.getActualResult().contains("PASS") && !subTest.getActualResult().equals("OK")) {
        StatusDetails details = new StatusDetails();
        details.setMessage(subTest.getName());
        subStepReport.setDetails(details);
        subStepReport.setStatus(Status.FAILED);
      } else {
        subStepReport.setStatus(Status.PASSED);
      }
      //      reporter.textAttachment(subStepReport, "Result", subTest.toString(), "json");
      subStepReport.setStopTimestamp(stop);
      testReport.addStepReport(subStepReport);
    }
  }

  private void makeGroupedResults(
      AllureTestReport testReport, Result result, long start, long stop) {
    testReport.setName(
        testReport.getName() + " (" + result.getSucessCount() + "/" + result.size() + ")");
    testReport.setStopTimestamp(start);
    for (SubTest subTest : result) {
      AllureStepReport subStepReport = new AllureStepReport(subTest.getName());
      subStepReport.setStartTimestamp(start);
      StatusDetails details = new StatusDetails();
      details.setMessage(subTest.getMessage());
      subStepReport.setDetails(details);
      subStepReport.setIgnore(true);
      if (!subTest.getActualResult().equals("PASS") && !subTest.getActualResult().equals("OK")) {
        subStepReport.setStatus(Status.FAILED);
      } else {
        subStepReport.setStatus(Status.PASSED);
      }
      //      reporter.textAttachment(subStepReport, "Result", subTest.toString(), "json");
      subStepReport.setStopTimestamp(stop);
      testReport.addStepReport(subStepReport);
    }
    testReport.setStopTimestamp(stop);
    testReport.setStage("finished");
  }

  @Override
  public String stepDescription() {
    return "Running " + urlList.size() + " found test(s)";
  }
}
