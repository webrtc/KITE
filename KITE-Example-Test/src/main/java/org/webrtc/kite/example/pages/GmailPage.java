package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.exception.KiteInteractionException;
import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.pages.BasePage;
import io.cosmosoftware.kite.report.Status;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.THREE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

public class GmailPage extends BasePage {
  
  public GmailPage(WebDriver webDriver, Logger logger) {
    super(webDriver, logger);
  }
  
  public void verifySignIn() throws KiteTestException {
    waitAround(THREE_SECOND_INTERVAL);
    if (webDriver.getTitle().contains("inbox")) {
      throw new KiteTestException("Failed to sign in to gmail inbox", Status.FAILED);
    }
    logger.info("Signed in to gmail inbox successfully");
  }
}
