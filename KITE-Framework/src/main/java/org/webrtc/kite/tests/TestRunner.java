package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import io.cosmosoftware.kite.util.TestHelper;
import org.apache.log4j.Logger;
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
public class TestRunner extends ArrayList<TestStep> implements Callable<Object> {
  
  protected final Logger logger = Logger.getLogger(this.getClass().getName());
  protected final AllureTestReport testReport;
  protected final WebDriver webDriver;
  protected boolean csv = false;
  private final String uid = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
  private final String resultPath;
  protected int id;

  private StepPhase stepPhase = StepPhase.RAMPUP;
  
  /**
   * Instantiates a new Test runner.
   *
   * @param webDriver  the web driver
   * @param testReport the test report
   * @param id         the id
   */
  public TestRunner(WebDriver webDriver, AllureTestReport testReport, int id) {
    this.webDriver = webDriver;
    this.testReport = testReport;
    this.id = id;
    this.resultPath = "results/" + uid + "_" + testReport.getName() + "/";
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
   * Set to true to print CSV file
   *
   * @param csv
   */
  public void setCsv(boolean csv) {
    this.csv = csv;
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
    TestHelper testHelper = TestHelper.getInstance(stepPhase.name());
    LinkedHashMap<String, String> csvReport = new LinkedHashMap<>();
    csvReport.put("clientId", this.get(0).getClientID());
    csvReport.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()));
    for (TestStep step : this) {
       processTestStep(stepPhase, step, testReport);
       LinkedHashMap<String, String> csvResult = step.getCsvResult();
       if (csvResult != null) {
        for (String s : csvResult.keySet()) {
          csvReport.put(s, csvResult.get(s));
         }
       }
    }
    if (csv) {
      testHelper.println(csvReport, resultPath);
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

  public void setStepPhase(StepPhase stepPhase) {
    this.stepPhase = stepPhase;
  }
}
