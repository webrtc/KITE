package org.webrtc.kite.wpt.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.*;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.ArrayList;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.tests.TestRunner;
import org.webrtc.kite.wpt.Result;
import org.webrtc.kite.wpt.RunInfo;
import org.webrtc.kite.wpt.SubTest;
import org.webrtc.kite.wpt.TestSummary;

import java.util.List;
import org.webrtc.kite.wpt.pages.WPTTestPage;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;

public class RunAllTestStep extends TestStep {
  private final String revision;
  private final String testCaseName;
  private final String testName;
  private final List<String> urlList;
  private final TestRunner runner;
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
//    this.testCaseName = testCaseName.split(" ")[0];
    this.testCaseName = testCaseName;
    this.testName = testName;
    this.urlList = urlList;
    this.revision = revision;
    this.runner = (TestRunner) runner;
    this.reportStyle = reportStyle;
    this.setOptional(true);
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
      info.getInfoFromWebDriver(this.runner.getWebDriver());
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
    int success = 0;
    Container testSuite = new Container(testName);
    testSuite.setParentSuite(this.report.getName() + "-" + this.revision);
    List<AllureTestReport> testReports = new ArrayList<>();
    for (int index = 0; index < urlList.size(); index++) {
      String url = urlList.get(index);
      RunOneTestStep runOneTestStep =
          new RunOneTestStep(runner,url, "[" + (index + 1) + "/" + urlList.size() + "]");
      long start = System.currentTimeMillis();
      runOneTestStep.processTestStep(StepPhase.DEFAULT, report, false);
      long stop = System.currentTimeMillis();
      Result result = runOneTestStep.getTestResult();
      total += result.size();
      success += result.getSucessCount();
      testSummary.addResult(result);
      switch (reportStyle) {
        case 0: {
          //both
          // grouped
          String[] split = urlList.get(index).split("/");
          String shortName = split[split.length -1];
          AllureTestReport testReport = new AllureTestReport(shortName);
          testSuite.addChild(testReport.getUuid());
          testReport.addLabel("parentSuite", this.testCaseName);
          testReport.setFullName(this.logger.getName() + "_" + urlList.get(index));
          testReport.setReporter(this.reporter);
          testReports.add(testReport);
          makeGroupedResults(testReport, result, start, stop);
          makeIndividualResults(testSuite, urlList.get(index), result, start, stop);
          break;
        }
        case 1:
          { // grouped
            String[] split = urlList.get(index).split("/");
            String shortName = split[split.length -1];
            AllureTestReport testReport = new AllureTestReport(shortName);
            testSuite.addChild(testReport.getUuid());
            testReport.addLabel("parentSuite", this.testCaseName);
            testReport.setFullName(this.logger.getName() + "_" + urlList.get(index));
            testReport.setReporter(this.reporter);
            testReports.add(testReport);
            makeGroupedResults(testReport, result, start, stop);
            break;
          }
        case 2:
          { // individual
            makeIndividualResults(testSuite, urlList.get(index), result, start, stop);
            break;
          }
        default:
          // do nothing
          break;
      }
    }
    // testSuite.setName(testSuite.getName() + " - " + total + " tests");
    reporter.textAttachment(this.report, "Test run summary", testSummary.toString(), "json");
    logger.info("updating test suite name from " + testName + " ->" + testName + "(" + success + "/" + total + ")");
    testSuite.setName(testName + " (" + success + "/" + total + ")");
    testSuite.setReporter(this.reporter);
    for (AllureTestReport testReport : testReports) {
      testReport.addLabel("suite", testSuite.getName());
    }
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
        reporter.textAttachment(subStepReport, "Reason ", subTest.getMessage(), "plain");
      } else {
        subStepReport.setStatus(Status.PASSED);
      }
      subStepReport.setStopTimestamp(stop);
      testReport.addStepReport(subStepReport);
    }
  }

  private void makeGroupedResults(
      AllureTestReport testReport, Result result, long start, long stop) {
    testReport.setName(
        testReport.getName() + " (" + result.getSucessCount() + "/" + result.size() + ")");
    testReport.setStartTimestamp(start);
    for (SubTest subTest : result) {
      AllureStepReport subStepReport = new AllureStepReport(subTest.getName());
      subStepReport.setStartTimestamp(start);
      StatusDetails details = new StatusDetails();
      details.setMessage(subTest.getMessage());
      subStepReport.setDetails(details);
      subStepReport.setIgnore(true);
      if (!subTest.getActualResult().equals("PASS") && !subTest.getActualResult().equals("OK")) {
        subStepReport.setStatus(Status.FAILED);
        reporter.textAttachment(subStepReport, "Reason ", subTest.getMessage(), "plain");
      } else {
        subStepReport.setStatus(Status.PASSED);
      }
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
