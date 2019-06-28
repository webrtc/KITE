package org.webrtc.kite.example;

import org.webrtc.kite.example.checks.GoogleFirstResultCheck;
import org.webrtc.kite.example.steps.GoogleSearchStep;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.TestRunner;

public class KiteExampleSearchTest extends KiteBaseTest {
  
  @Override
  public void populateTestSteps(TestRunner runner) {
    runner.addStep(new GoogleSearchStep(runner));
    runner.addStep(new GoogleFirstResultCheck(runner));
  }
  
}
