package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.exception.KiteInteractionException;
import io.cosmosoftware.kite.pages.BasePage;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.interfaces.Runner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class GoogleResultPage extends BasePage {
  
  @FindBy(className = "LC20lb")
  WebElement result;
  
  public GoogleResultPage(Runner runner) {
    super(runner);
  }
  
  public String getTitle() {
    return webDriver.getTitle();
  }
  
  public void openFirstResult() throws KiteInteractionException {
    waitUntilVisibilityOf(result, 10);
    result.click();
  }
}
