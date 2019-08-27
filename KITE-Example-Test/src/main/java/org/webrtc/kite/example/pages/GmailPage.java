package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.pages.BasePage;
import io.cosmosoftware.kite.report.Status;

import static io.cosmosoftware.kite.entities.Timeouts.THREE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

public class GmailPage extends BasePage {
  
  public GmailPage(Runner runner) {
    super(runner);
  }
  
  public void verifySignIn() throws KiteTestException {
    waitAround(THREE_SECOND_INTERVAL);
    if (webDriver.getTitle().contains("inbox")) {
      throw new KiteTestException("Failed to sign in to gmail inbox", Status.FAILED);
    }
    logger.info("Signed in to gmail inbox successfully");
  }
}
