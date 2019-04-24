package org.webrtc.kite.example.steps;

import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.example.pages.GoogleSearchPage;

public class GoogleSearchStep extends TestStep {
  
  final String TARGET = "CoSMo Software Consulting";
  
  
  public GoogleSearchStep(WebDriver webDriver) {
    super(webDriver);
  }
  
  
  @Override
  public String stepDescription() {
    return "Open " + GoogleSearchPage.getURL() + " and look for " + TARGET;
  }
  
  @Override
  protected void step() {
    final GoogleSearchPage searchPage = new GoogleSearchPage(this.webDriver, logger);
    searchPage.open();
    searchPage.searchFor(TARGET);
  }
}
