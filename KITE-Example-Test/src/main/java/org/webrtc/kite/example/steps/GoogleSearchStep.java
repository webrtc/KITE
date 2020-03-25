package org.webrtc.kite.example.steps;

import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.steps.TestStep;
import org.webrtc.kite.example.pages.GoogleSearchPage;

public class GoogleSearchStep extends TestStep {
  
  private final String TARGET = "CoSMo Software Consulting";
  private final GoogleSearchPage searchPage;
  
  public GoogleSearchStep(Runner runner) {
    super(runner);
    this.searchPage = new GoogleSearchPage(runner);
  }
  
  @Override
  protected void step() {
    
    searchPage.open();
//    searchPage.searchFor(TARGET);
  }
  
  @Override
  public String stepDescription() {
    return "Open " + GoogleSearchPage.getURL() + " and look for " + TARGET;
  }
}
