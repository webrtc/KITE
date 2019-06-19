package org.webrtc.kite.example.pages;

import io.cosmosoftware.kite.exception.KiteInteractionException;
import io.cosmosoftware.kite.pages.BasePage;
import io.cosmosoftware.kite.interfaces.Runner;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static io.cosmosoftware.kite.entities.Timeouts.THREE_SECOND_INTERVAL;

public class GmailSignInPage extends BasePage {
  private final static String GMAIL_PAGE = "https://accounts.google.com/signin/v2/identifier?continue=https%3A%2F%2F" +
    "mail.google.com%2Fmail%2F&service=mail&sacu=1&rip=1&flowName=GlifWebSignIn&flowEntry=ServiceLogin";
  @FindBy(className = "RveJvd snByac")
  WebElement confirmButton;
  @FindBy(id = "identifierId")
  WebElement credentialField;
  @FindBy(xpath = "//input[@type='password']")
  WebElement passwordField;
  
  
  public GmailSignInPage(Runner runner) {
    super(runner);
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
  
  public void inputCredential(String credential) throws KiteInteractionException {
    waitUntilVisibilityOf(credentialField, THREE_SECOND_INTERVAL);
    sendKeys(credentialField, credential);
  }
  
  public void inputPassword(String password) throws KiteInteractionException {
    waitUntilVisibilityOf(passwordField, THREE_SECOND_INTERVAL);
    sendKeys(passwordField, password);
  }
  
  public void open() {
    webDriver.get(GMAIL_PAGE);
  }
}
