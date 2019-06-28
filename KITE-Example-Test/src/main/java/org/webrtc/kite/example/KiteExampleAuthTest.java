package org.webrtc.kite.example;

import org.webrtc.kite.example.checks.GmailSignInCheck;
import org.webrtc.kite.example.steps.GmailSignInStep;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.TestRunner;

public class KiteExampleAuthTest extends KiteBaseTest {
  
  @Override
  public void populateTestSteps(TestRunner runner) {
    runner.addStep(new GmailSignInStep(runner));
    runner.addStep(new GmailSignInCheck(runner));
  }
  
}
