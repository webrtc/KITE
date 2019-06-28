package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import io.cosmosoftware.kite.util.TestHelper;
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
  private final String resultPath;
  private final String uid = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
  protected boolean csv = false;
  protected int id;
  
  protected StepPhase stepPhase = StepPhase.DEFAULT;
  
  /**
   * Instantiates a new Test runner.
   *
   * @param webDriver the web driver
   * @param reports   the test reports
   * @param id        the id
   */
  public TestRunner(WebDriver webDriver, LinkedHashMap<StepPhase, AllureTestReport> reports, KiteLogger logger, int id) {
    this.webDriver = webDriver;
    this.reports = reports;
    this.logger = logger;
    this.id = id;
    this.resultPath = "results/" + uid + "_" + reports.get(reports.keySet().toArray()[0]).getLabel("suite") + "/";
  }
  
  public boolean addStep(TestStep step) {
    return add(step);
  }
  
  @Override
  public Object call() {
    TestHelper testHelper = TestHelper.getInstance(stepPhase.getShortName());
    LinkedHashMap<String, String> csvReport = new LinkedHashMap<>();
    csvReport.put("clientId", this.get(0).getClientID());
    csvReport.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()));
    for (TestStep step : this) {
      processTestStep(stepPhase, step, reports.get(stepPhase));
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
  
  /**
   * Set to true to print CSV file
   *
   * @param csv
   */
  public void setCsv(boolean csv) {
    this.csv = csv;
  }
  
  public void setStepPhase(StepPhase stepPhase) {
    this.stepPhase = stepPhase;
  }
}
