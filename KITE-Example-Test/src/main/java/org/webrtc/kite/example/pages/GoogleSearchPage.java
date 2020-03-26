package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleSearchPage extends BasePage {
  
  private final static String GOOGLE_PAGE = "https://www.youtube.com/watch?v=DCgfVBoBy9s";
  
  @FindBy(className = "gLFyf")
  WebElement searchBar;
  
  
  public GoogleSearchPage(Runner runner) {
    super(runner);
  }
  
  public static String getURL() {
    return GOOGLE_PAGE;
  }
  
  public void open() {
    webDriver.get(GOOGLE_PAGE);
  }
  
  public void searchFor(String target) {
    searchBar.sendKeys(target);
    searchBar.sendKeys(Keys.ENTER);
  }
}
