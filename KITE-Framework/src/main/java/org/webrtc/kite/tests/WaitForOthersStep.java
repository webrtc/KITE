package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.SHORT_TIMEOUT_IN_SECONDS;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

/**
 * Special Step implementation that waits for a given step to complete on all other runners.
 *
 * Usage:
 *  In your KiteBaseTest implementation, add this step as follows to wait for the previous step to complete:
 *  runner.addStep(new WaitForOthersStep(webDriver, this, runner.getLastStep()));
 *
 */
public class WaitForOthersStep extends TestStep {

  private final KiteBaseTest test;
  private final TestStep stepToWaitFor;
  private int  timeoutInSeconds = SHORT_TIMEOUT_IN_SECONDS;

  public WaitForOthersStep(WebDriver webDriver, KiteBaseTest test, TestStep stepToWaitFor) {
    super(webDriver);
    this.test = test;
    this.stepToWaitFor = stepToWaitFor;
  }

  @Override
  public String stepDescription() {
    return "Waiting for " + stepToWaitFor.getName() + " to complete.";
  }

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


