package org.webrtc.kite.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.tests.KiteBaseTest;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.SHORT_TIMEOUT_IN_SECONDS;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

/**
 * Special Step implementation that waits for a given step to complete on all other runners.
 * <p>
 * Usage:
 * In your KiteBaseTest implementation, add this step as follows to wait for the previous step to complete:
 * runner.addStep(new WaitForOthersStep(webDriver, this, runner.getLastStep()));
 */
public class WaitForOthersStep extends TestStep {

  private final KiteBaseTest test;
  private final TestStep stepToWaitFor;
  private int  timeoutInSeconds = SHORT_TIMEOUT_IN_SECONDS;
  
  /**
   * Instantiates a new Wait for others step.
   *
   * @param webDriver     the web driver
   * @param test          the test
   * @param stepToWaitFor the step to wait for
   */
  public WaitForOthersStep(WebDriver webDriver, KiteBaseTest test, TestStep stepToWaitFor) {
    super(webDriver);
    this.test = test;
    this.stepToWaitFor = stepToWaitFor;
  }

  @Override
  public String stepDescription() {
    return "Waiting for " + stepToWaitFor.getName() + " to complete.";
  }
  
  /**
   * Sets timeout.
   *
   * @param timeout the timeout
   */
  public void setTimeout(int timeout) {
    this. timeoutInSeconds = timeout;
  }

  @Override
  protected void step() throws KiteTestException {
    int i = 0;
    while(!test.stepCompleted(stepToWaitFor.getName())) {
      logger.debug( "WaitForOtherRunners waiting for " + stepToWaitFor.getName());
      i++;
      waitAround(ONE_SECOND_INTERVAL);
      if (i >  timeoutInSeconds) {
        throw new KiteTestException("Timed out waiting for other runners to complete the step " + stepToWaitFor.getName(), Status.FAILED);
      }
    }
    Reporter.getInstance().textAttachment(report, "All runners completed", "" + stepToWaitFor.getName() + " within " + i + "s.", "plain");
  }

}


