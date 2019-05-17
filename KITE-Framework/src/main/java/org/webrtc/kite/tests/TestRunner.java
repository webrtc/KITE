package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static io.cosmosoftware.kite.util.TestUtils.processTestStep;

/**
 * The type Test runner.
 */
public class TestRunner extends ArrayList<TestStep> implements Callable<Object> {
  
  protected final Logger logger = Logger.getLogger(this.getClass().getName());
  protected final AllureTestReport testReport;
  protected final WebDriver webDriver;
  protected int id;
  
  /**
   * Instantiates a new Test runner.
   *
   * @param webDriver  the web driver
   * @param testReport the test report
   */
  public TestRunner(WebDriver webDriver, AllureTestReport testReport) {
    super();
    this.webDriver = webDriver;
    this.testReport = testReport;
  }
  
  /**
   * Instantiates a new Test runner.
   *
   * @param webDriver  the web driver
   * @param testReport the test report
   * @param id         the id
   */
  public TestRunner(WebDriver webDriver, AllureTestReport testReport, int id) {
    super();
    this.webDriver = webDriver;
    this.testReport = testReport;
    this.id = id;
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
   * Gets id.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }
  
  @Override
  public Object call() {
    for (TestStep step : this) {
      processTestStep(step, testReport);
    }
    return null;
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
  public WebDriver getWebDriver() {
    return webDriver;
  }
  
  /**
   * Completed boolean.
   *
   * @param stepName the step name
   *
   * @return the boolean
   * @throws KiteTestException the kite test exception
   */
  public boolean completed(String stepName) throws  KiteTestException{
    for (TestStep step : this) {
      if (step.getName().equalsIgnoreCase(stepName)) {
        return step.stepCompleted();
      }
    }
    throw new KiteTestException("Could not find the step with name: " + stepName, Status.BROKEN);
  }
  
  public boolean addStep(TestStep step) {
    return add(step);
  }
}
