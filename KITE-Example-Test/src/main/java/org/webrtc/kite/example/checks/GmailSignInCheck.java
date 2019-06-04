package org.webrtc.kite.example.checks;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.example.pages.GmailPage;
import org.webrtc.kite.example.pages.GmailSignInPage;

public class GmailSignInCheck extends TestStep {
  
  final String CREDENTIAL = "kite.test.cosmo";
  final String PASSWORD = "Test!123";
  
  
  public GmailSignInCheck(WebDriver webDriver) {
    super(webDriver);
  }
  
  
  @Override
  public String stepDescription() {
    return "Verify that your gmail account is signed in";
  }
  
  @Override
  protected void step() throws KiteTestException {
    final GmailPage gmailPage = new GmailPage(this.webDriver, logger);
    gmailPage.verifySignIn();
  }
}
