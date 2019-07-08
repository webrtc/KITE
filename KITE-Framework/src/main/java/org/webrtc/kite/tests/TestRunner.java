package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

import static io.cosmosoftware.kite.util.TestUtils.processTestStep;

/**
 * The type Test runner.
 */
public class TestRunner extends ArrayList<TestStep> implements Callable<Object>, Runner {
  
  protected final KiteLogger logger;
  protected final LinkedHashMap<StepPhase, AllureTestReport> reports;
  protected final WebDriver webDriver;
  protected final Reporter reporter;
  protected int id;
  
  protected StepPhase stepPhase = StepPhase.DEFAULT;
  
  /**
   * Instantiates a new Test runner.
   *
   * @param webDriver the web driver
   * @param reports   the test reports
   * @param id        the id
   */
  public TestRunner(WebDriver webDriver, LinkedHashMap<StepPhase, AllureTestReport> reports, 
                    KiteLogger logger, Reporter reporter, int id) {
    this.webDriver = webDriver;
    this.reports = reports;
    this.logger = logger;
    this.reporter = reporter;
    this.id = id;
  }
  
  public boolean addStep(TestStep step) {
    return add(step);
  }
  
  @Override
  public Object call() {
    for (TestStep step : this) {
      processTestStep(stepPhase, step, reports.get(stepPhase));
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
  
  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(int id) {
    this.id = id;
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
