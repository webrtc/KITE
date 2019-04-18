package org.webrtc.kite.wpt.pages;

import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

public class WPTDirPage extends BasePage {
  
  @FindBy(tagName = "li")
  List<WebElement> items;
  
  @FindBy(className = "dir")
  List<WebElement> dirs;
  
  @FindBy(className = "file")
  List<WebElement> files;
  
  private final String pageUrl;
  
  public WPTDirPage(WebDriver webDriver, String pageUrl) {
    super(webDriver);
    this.pageUrl = pageUrl;
  }
  
  public List<WebElement> getDirs() {
    return dirs;
  }
  
  public List<WebElement> getFiles() {
    return files;
  }
  
  public List<WebElement> getItems() {
    return items;
  }
  
  public void openPage() {
    this.webDriver.get(pageUrl);
  }
  
  public List<String> getTestList() {
    List<String> testList = new ArrayList<>();
    for (WebElement file : files) {
      String fileName = fixTestName(file.getText());
      if (testURLIsValid(fileName)) {
        testList.add(fileName);
      }
    }
    return  testList;
  }
  
  public List<String> getDirNameList() {
    List<String> dirNameList = new ArrayList<>();
    for (WebElement dir: dirs) {
      if (dirNameIsValid(dir.getText())) {
        dirNameList.add(dir.getText() + "/");
      }
    }
    return dirNameList;
  }
  
  /**
   * Determine whether the item name is a valid test name
   *
   * @param itemName name of the item to validate
   *
   * @return true if the item name is a valid test name
   */
  private boolean testURLIsValid(String itemName) {
    if (itemName.endsWith(".html")) {
      if (itemName.contains("manual")) {
        //return itemName.contains("none-manual");
        return false;
      } else {
        return true;
      }
    }
    return false;
  }
  
  private String fixTestName(String itemName) {
    // particular cases
    if (itemName.contains("idlharness.https") && itemName.endsWith("js")) {
      return itemName.replace("js", "html");
    }
    return itemName;
  }
  
  /**
   * Determine whether the item name is a valid directory that contains test
   *
   * @param itemName name of the item to validate
   *
   * @return true if the item name is a valid directory that contains test
   */
  private boolean dirNameIsValid(String itemName) {
    return !(
      itemName.contains("resources")
      || itemName.startsWith(".")
      || itemName.equalsIgnoreCase("tools")
    );
  }
}
