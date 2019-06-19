package org.webrtc.kite.example.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.TestStep;
import org.webrtc.kite.example.pages.GmailSignInPage;

public class GmailSignInStep extends TestStep {
  
  private final String CREDENTIAL = "kite.test.cosmo\n";
  private final String PASSWORD = "Test!123\n";
  private final GmailSignInPage gmailSignInPage;
  
  
  public GmailSignInStep(Runner runner) {
    super(runner);
    gmailSignInPage = new GmailSignInPage(runner);
  }
  
  @Override
  protected void step() throws KiteTestException {
    gmailSignInPage.open();
    gmailSignInPage.inputCredential(CREDENTIAL);
    gmailSignInPage.inputPassword(PASSWORD);
    //gmailSignInPage.confirmInformation();
  }
  
  @Override
  public String stepDescription() {
    return "Open " + GmailSignInPage.getURL() + " and sign in";
  }
}
