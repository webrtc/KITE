package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleResultPage extends BasePage {
  
  @FindBy(className="LC20lb")
  WebElement result;
  
  public GoogleResultPage(WebDriver webDriver) {
    super(webDriver);
  }
  
  public void openFirstResult() {
    result.click();
  }
}
