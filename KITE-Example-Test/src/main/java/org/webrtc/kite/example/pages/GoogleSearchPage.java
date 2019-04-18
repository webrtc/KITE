package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleSearchPage extends BasePage {
  
  final String GOOGLE_PAGE = "https://google.com";
  
  @FindBy(className="gLFyf")
  WebElement searchBar;
  
  
  
  public GoogleSearchPage(WebDriver webDriver) {
    super(webDriver);
  }
  
  public void open() {
    webDriver.get(GOOGLE_PAGE);
  }
  
  public void searchFor(String target) {
    searchBar.sendKeys(target);
    searchBar.sendKeys(Keys.ENTER);
  }
  
  public String getURL() {
    return GOOGLE_PAGE;
  }
}
