package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.exception.KiteInteractionException;
import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GoogleResultPage extends BasePage {
  
  @FindBy(className="LC20lb")
  WebElement result;
  
  public GoogleResultPage(WebDriver webDriver) {
    super(webDriver);
  }
  
  public void openFirstResult() throws KiteInteractionException {
    waitUntilVisibilityOf(result, 10);
    result.click();
  }
  
  public String getTitle() {
    return webDriver.getTitle();
  }
}
