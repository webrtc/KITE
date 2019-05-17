package org.webrtc.kite.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;

import static io.cosmosoftware.kite.util.TestUtils.waitAround;

public class StayInMeetingStep extends TestStep {

  private final int meetingDuration;

  public StayInMeetingStep(WebDriver webDriver, int meetingDuration) {
    super(webDriver);
    this.meetingDuration = meetingDuration;
  }
  
  
  @Override
  public String stepDescription() {
    return "Stay in the meeting for " + meetingDuration + "s.";
  }
  
  @Override
  protected void step() throws KiteTestException {
    waitAround(meetingDuration * 1000);
  }
}
