package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.exception.KiteInteractionException;
import io.cosmosoftware.kite.pages.BasePage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.THREE_SECOND_INTERVAL;

public class GmailSignInPage extends BasePage {
  private final static String GMAIL_PAGE = "https://accounts.google.com/signin/v2/identifier?continue=https%3A%2F%2F" +
    "mail.google.com%2Fmail%2F&service=mail&sacu=1&rip=1&flowName=GlifWebSignIn&flowEntry=ServiceLogin";
  
  
  @FindBy(id="identifierId")
  WebElement credentialField;
  
  @FindBy(xpath="//input[@type='password']")
  WebElement passwordField;
  
  @FindBy(className="RveJvd snByac")
  WebElement confirmButton;
  
  
  public GmailSignInPage(WebDriver webDriver, Logger logger) {
    super(webDriver, logger);
  }
  
  public void open() {
    webDriver.get(GMAIL_PAGE);
  }
  
  public void inputCredential(String credential) throws KiteInteractionException {
    waitUntilVisibilityOf(credentialField, THREE_SECOND_INTERVAL);
    sendKeys(credentialField, credential);
  }
  
  public void inputPassword(String password) throws KiteInteractionException {
    waitUntilVisibilityOf(passwordField, THREE_SECOND_INTERVAL);
    sendKeys(passwordField, password);
  }
  
  public void confirmInformation() throws KiteInteractionException {
    try {
      waitUntilVisibilityOf(confirmButton, THREE_SECOND_INTERVAL);
    } catch (Exception e) {
      logger.info("No confirmation needed");
      return;
    }
    click(confirmButton);
  }
  
  public static String getURL() {
    return GMAIL_PAGE;
  }
}
