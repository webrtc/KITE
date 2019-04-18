package org.webrtc.kite.example.steps;

import org.webrtc.kite.example.pages.GoogleSearchPage;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;

public class GoogleSearchStep extends TestStep {
  
  final String TARGET = "CoSMo Software Consulting";
  final GoogleSearchPage searchPage = new GoogleSearchPage(this.webDriver);
  
  public GoogleSearchStep(WebDriver webDriver) {
    super(webDriver);
  }
  
  
  @Override
  public String stepDescription() {
    return "Open " + searchPage.getURL() + " and look for " + TARGET;
  }
  
  @Override
  protected void step() {
    searchPage.open();
    searchPage.searchFor(TARGET);
  }
}
