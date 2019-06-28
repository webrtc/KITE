package org.webrtc.kite.example.checks;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.TestStep;
import org.webrtc.kite.example.pages.GmailPage;

public class GmailSignInCheck extends TestStep {
  
  private final GmailPage gmailPage; 
  
  public GmailSignInCheck(Runner runner) {
    super(runner);
    this.gmailPage = new GmailPage(runner);
  }
  
  @Override
  protected void step() throws KiteTestException {
    gmailPage.verifySignIn();
  }
  
  @Override
  public String stepDescription() {
    return "Verify that your gmail account is signed in";
  }
}
