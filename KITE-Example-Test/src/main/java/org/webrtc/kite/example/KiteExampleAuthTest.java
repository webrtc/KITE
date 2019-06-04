package org.webrtc.kite.example;

import org.webrtc.kite.example.checks.GmailSignInCheck;
import org.webrtc.kite.example.checks.GoogleFirstResultCheck;
import org.webrtc.kite.example.steps.GmailSignInStep;
import org.webrtc.kite.example.steps.GoogleSearchStep;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.TestRunner;

public class KiteExampleAuthTest extends KiteBaseTest {

  
  @Override
  protected void payloadHandling() {}

  @Override
  public void populateTestSteps(TestRunner runner) {
    runner.addStep(new GmailSignInStep(runner.getWebDriver()));
    runner.addStep(new GmailSignInCheck(runner.getWebDriver()));
  }

}
