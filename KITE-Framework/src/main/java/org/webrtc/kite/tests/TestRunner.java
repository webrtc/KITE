package org.webrtc.kite.tests;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.AllureStepReport;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.report.StatusDetails;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.exception.KiteGridException;

/**
 * The type Test runner.
 */
public class TestRunner extends ArrayList<TestStep> implements Callable<Object>, Runner {
  
  protected final KiteLogger logger;
  protected final Client client;
  protected final Reporter reporter;
  protected final TestConfig testConfig;
  protected int id;
  protected int interval = 0;
  protected final Map<WebDriver, Map<String, Object>> sessionData;
  protected LinkedHashMap<StepPhase, AllureTestReport> reports;
  protected WebDriver webDriver;
  protected StepPhase stepPhase = StepPhase.DEFAULT;
  private final KiteBaseTest test;
  /**
   * Instantiates a new Test runner.
   *
   * @param client the web driver
   * @param test   the test 
   * @param id        the id
   */
  public TestRunner(Client client, KiteBaseTest test, int id)
      throws IOException {
    this.client = client;
    this.test = test;
    this.testConfig = test.testConfig;
    this.logger = testConfig.getLogger();
    this.sessionData = test.sessionData;
    this.reporter = testConfig.getReporter();
    this.reports = new LinkedHashMap<>();
    this.id = id;

    if (test.isLoadTest) {
      this.stepPhase = StepPhase.RAMPUP; // todo check with KITE TEST
      this.setReportForCurrentPhase();
    } else {
      this.reports = test.reports;
    }
    setInterval(test.getInterval(id));
    if (client != null) {
      // client is null for JsTestRunner since the webdriver and client are created in JS.
      InitClientWebDriverStep initClient = new InitClientWebDriverStep(this, this.id, this.client, this.sessionData);
      initClient.setStepPhase(this.stepPhase);
      initClient.processTestStep(this.stepPhase, this.reports.get(stepPhase), this.testConfig.isLoadTest());
      this.webDriver = initClient.getWebDriver();
//      initClient();
    }
  }

  private void initClient() {
    AllureStepReport initStep = new AllureStepReport("Creating webDriver" + (testConfig.isLoadTest() ? "" : (" for runner - " + this.id))) ;
    initStep.setPhase(stepPhase);
    initStep.setStartTimestamp();
    try {
      this.client.setName(this.client.getName() == null ? ("" + id) : ( id + "_" + this.client.getName())) ;
      logger.info("Creating web driver for " + client);
      this.webDriver = client.createWebDriver(sessionData);
      if (sessionData != null && sessionData.containsKey(this.webDriver)) {
        Map<String, Object> clientSessionData = sessionData.get(this.webDriver);
        if (clientSessionData.containsKey("node_host")) {
          logger.debug("created " + client + " on node: " + clientSessionData.get("node_host"));
        }
      }
      initStep.setStatus(Status.PASSED);
    } catch (KiteGridException e) {
      this.webDriver = null;
      logger.error("Exception while populating webdriver: " + getClientName() + "\r\n" + getStackTrace(e));
      reporter.textAttachment(initStep, "KiteGridException", getStackTrace(e), "plain");
      initStep.setStatus(Status.FAILED);
      StatusDetails details = new StatusDetails();
      details.setCode(1);
      details.setMessage("Exception while populating webdrivers: \r\n" + getStackTrace(e));
      initStep.setDetails(details);
    }
    this.reports.get(stepPhase).addStepReport(initStep);
  }

  private void setReportForCurrentPhase() {
    logger.debug("SETTING REPORT FOR STEP PHASE " + this.stepPhase);
    AllureTestReport report = new AllureTestReport(test.getClass().getName() + " - " + this.id);
    if (test.isLoadTest) {
      report.setName(test.getClientName(this.client) + " - " + this.id);
    }
    report.setReporter(this.reporter);
    report.setFullName(test.getClass().getName() + " - " + this.id);
    report.addLabel("package", test.getClass().getPackage().toString());
    report.addLabel("testClass", test.getClass().toString());
    report.addLabel("testMethod", "execute");
    this.reports.put(stepPhase, report);
    this.reporter.addTest(report);
    try {
      report.addLabel("host", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      report.addLabel("host", "N/A");
    }
  }


  /**
   * Super constructor for JsTestRunner, which does not create the webdriver,
   * since it's done by the js script
   *
   * @param test   the test 
   * @param id        the id
   */
  protected TestRunner(KiteBaseTest test, int id)
      throws  IOException {
    this(null, test, id);
  }

  public boolean addStep(TestStep step) {
    return addStep(step, StepPhase.DEFAULT);
  }

  public boolean addStep(TestStep step, StepPhase stepPhase) {
    step.setStepPhase(stepPhase);
    return add(step);
  }


  public boolean addStep(TestStep step, TestStep conditionStep) {
    return addStep(step, conditionStep, StepPhase.DEFAULT);
  }

  public boolean addStep(TestStep step, TestStep conditionStep,  StepPhase stepPhase) {
    step.setDependOn(conditionStep);
    return addStep(step, stepPhase);
  }

  
  @Override
  public Object call()  {
    logger.info("Start processing the TestRunner(" + this.stepPhase + ") id " + id + " in " + interval + "ms");
    waitAround(interval);
    if (stepPhase.equals(StepPhase.LOADREACHED)) {
      this.setReportForCurrentPhase();
    }
    this.reports.get(stepPhase).setStartTimestamp();
    test.suite.addChild(this.reports.get(stepPhase).getUuid());
    this.reports.get(stepPhase).addLabel("suite", test.suite.getName());
    this.reports.get(stepPhase).addLabel("parentSuite",test.suite.getParentSuite());
    if (this.webDriver == null) {
      StatusDetails details = new StatusDetails();
      details.setCode(4);
      details.setMessage("Skipped due to absence of webdriver");
      this.reports.get(stepPhase).setDetails(details);
      this.reports.get(stepPhase).setStatus(Status.SKIPPED);
    }

    for (TestStep step : this) {
      if (this.webDriver != null) {
        if (this.test.isLoadTest) {
          if (step.getStepPhase().equals(StepPhase.ALL) || step.getStepPhase().equals(stepPhase)) {
            step.processTestStep(stepPhase, this.reports.get(stepPhase), testConfig.isLoadTest());
          }
        } else {
          if (!this.test.hasWebdriverIssue()) {
            step.processTestStep(stepPhase, this.reports.get(stepPhase), testConfig.isLoadTest());
          } else {
            step.skipTestStep(stepPhase, this.reports.get(stepPhase), testConfig.isLoadTest());
          }
        }
      } else {
        step.skipTestStep(stepPhase, this.reports.get(stepPhase), testConfig.isLoadTest());
      }
    }
    this.reports.get(stepPhase).setStopTimestamp();
    return null;
  }

  public void terminate() {
    try {
      for (TestStep step : this) {
        if (!completed(step.getName())) {
          step.skipTestStep(stepPhase, this.reports.get(stepPhase), testConfig.isLoadTest());
        }
      }
    } catch (Exception e) {
      //ignore
    }
  }

  /**
   * Completed boolean.
   *
   * @param stepName the step name
   *
   * @return the boolean
   * @throws KiteTestException the kite test exception
   */
  public boolean completed(String stepName) throws KiteTestException {
    for (TestStep step : this) {
      if (step.getName().equalsIgnoreCase(stepName)) {
        return step.stepCompleted();
      }
    }
    throw new KiteTestException("Could not find the step with name: " + stepName, Status.BROKEN);
  }
  
  /**
   * Gets id.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }
  
  /**
   * Gets the logger.
   *
   * @return the logger
   */
  @Override
  public KiteLogger getLogger() {
    return logger;
  }

  @Override
  public StepPhase getStepPhase() {
    return stepPhase;
  }

  @Override
  public Reporter getReporter() {
    return reporter;
  }

  @Override
  public String getClientName() {
    return client.getName();
  }

  @Override
  public String getClientRegion() {
    return this.client.getRegion();
  }

  @Override
  public String getPlatform() {
    return this.client.getBrowserSpecs().getPlatform().toString();
  }

  @Override
  public String getNetworkProfile() {
    return this.client.getNetworkProfile() == null ? "NC" : this.client.getNetworkProfile().getName();
  }

  @Override
  public String getPublicIpAddress() {
    // to be changed
    return null;
  }

  @Override
  public boolean isApp() {
    return this.client.isApp();
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(int id) {
    this.id = id;
  }
  

  /**
   * Sets interval.
   *
   * @param interval the interval
   */
  public void setInterval(int interval) {
    this.interval = interval;
  }

  /**
   * Gets last step.
   *
   * @return the last step
   */
  public TestStep getLastStep() {
    return get(this.size() - 1);
  }
  
  /**
   * Gets web driver.
   *
   * @return the web driver
   */
  @Override
  public WebDriver getWebDriver() {
    return webDriver;
  }
  
  public void setStepPhase(StepPhase stepPhase) {
    logger.debug("SETTING STEP PHASE FOR RUNNER TO " + stepPhase);
    this.stepPhase = stepPhase;
  }

  public AllureTestReport getReport(StepPhase phase) {
    return reports.get(phase);
  }

  public Map<String, Object> getSessionData() {
    return sessionData.get(this.webDriver);
  }

  public void setWebDriver(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  public Client getClient() {
    return client;
  }

}
