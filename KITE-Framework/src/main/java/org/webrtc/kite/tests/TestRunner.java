package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.exception.KiteGridException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static io.cosmosoftware.kite.util.TestUtils.waitAround;

/**
 * The type Test runner.
 */
public class TestRunner extends ArrayList<TestStep> implements Callable<Object>, Runner {
  
  protected final KiteLogger logger;
  protected final LinkedHashMap<StepPhase, AllureTestReport> reports;
  protected final WebDriver webDriver;
  protected final Client client;
  protected final Reporter reporter;
  protected final TestConfig testConfig;
  protected int id;
  protected int interval = 0;
  private final KiteBaseTest test;
  protected final Map<WebDriver, Map<String, Object>> sessionData;
  
  protected StepPhase stepPhase = StepPhase.DEFAULT;
  
  /**
   * Instantiates a new Test runner.
   *
   * @param client the web driver
   * @param test   the test 
   * @param id        the id
   */
  public TestRunner(Client client, KiteBaseTest test, int id) throws KiteGridException, IOException {
    this.client = client;
    this.test = test;
    this.testConfig = test.testConfig;
    this.logger = testConfig.getLogger();
    this.sessionData = test.sessionData;
    logger.info("Creating webdriver for " + client);
    client.createWebDriver(sessionData);
    this.webDriver = client != null ? client.getWebDriver() : null; //client is null for JsTestRunner since it's created in JS.
    this.reports = test.reports;
    this.reporter = testConfig.getReporter();
    this.id = id;
    setInterval(id % testConfig.getIncrement() * testConfig.getInterval());
  }


  /**
   * Super constructor for JsTestRunner, which does not create the webdriver,
   * since it's done by the js script
   *
   * @param test   the test 
   * @param id        the id
   */
  protected TestRunner(KiteBaseTest test, int id) throws KiteGridException, IOException {
    this.client = null;
    this.sessionData = null;
    this.test = test;
    this.testConfig = test.testConfig;
    this.logger = testConfig.getLogger();
    this.webDriver = null; 
    this.reports = test.reports;
    this.reporter = testConfig.getReporter();
    this.id = id;
    setInterval(id % testConfig.getIncrement() * testConfig.getInterval());
  }
  
  public boolean addStep(TestStep step) {
    return add(step);
  }
  
  @Override
  public Object call() throws KiteGridException {
    interval = interval < 200 ? 200 : interval;
    logger.info("Start processing the TestRunner id " + id + " in " + interval + "ms");
    waitAround(interval);
    for (TestStep step : this) {
      step.processTestStep(stepPhase, reports.get(stepPhase), testConfig.isLoadTest());
    }
    return null;
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
    this.stepPhase = stepPhase;
  }
  
}
