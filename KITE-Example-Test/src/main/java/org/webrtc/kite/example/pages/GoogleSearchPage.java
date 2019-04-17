package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleSearchPage extends BasePage {
  
  @FindBy(className="gLFyf")
  WebElement searchBar;
  
  public GoogleSearchPage(WebDriver webDriver) {
    super(webDriver);
  }
  
  public void searchFor(String target) {
    searchBar.sendKeys(target + "\n");
  }
}
