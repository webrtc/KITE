package org.webrtc.kite.example.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.example.pages.GmailSignInPage;

public class GmailSignInStep extends TestStep {
  
  final String CREDENTIAL = "kite.test.cosmo\n";
  final String PASSWORD = "Test!123\n";
  
  
  public GmailSignInStep(WebDriver webDriver) {
    super(webDriver);
  }
  
  
  @Override
  public String stepDescription() {
    return "Open " + GmailSignInPage.getURL() + " and sign in";
  }
  
  @Override
  protected void step() throws KiteTestException {
    final GmailSignInPage gmailSignInPage = new GmailSignInPage(this.webDriver, logger);
    gmailSignInPage.open();
    gmailSignInPage.inputCredential(CREDENTIAL);
    gmailSignInPage.inputPassword(PASSWORD);
    //gmailSignInPage.confirmInformation();
  }
}
