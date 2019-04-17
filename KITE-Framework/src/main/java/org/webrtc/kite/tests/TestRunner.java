package org.webrtc.kite.tests;

import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.steps.TestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static io.cosmosoftware.kite.util.TestUtils.processTestStep;

public class TestRunner implements Callable<Object> {
  
  protected final Logger logger = Logger.getLogger(this.getClass().getName());
  protected final AllureTestReport testReport;
  protected final WebDriver webDriver;
  protected final List<TestStep> steps;
  protected int id;
  
  public TestRunner(WebDriver webDriver, AllureTestReport testReport) {
    this.webDriver = webDriver;
    this.testReport = testReport;
    this.steps = new ArrayList<>();
  }
  
  public TestRunner(WebDriver webDriver, AllureTestReport testReport, int id) {
    this.webDriver = webDriver;
    this.testReport = testReport;
    this.steps = new ArrayList<>();
    this.id = id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public int getId() {
    return id;
  }
  
  public void addStep(TestStep step) {
    this.steps.add(step);
  }
  
  @Override
  public Object call() {
    for (TestStep step : steps) {
      processTestStep(step, testReport);
    }
    return null;
  }
  
  public List<TestStep> getSteps() {
    return steps;
  }
  
  public WebDriver getWebDriver() {
    return webDriver;
  }
}
