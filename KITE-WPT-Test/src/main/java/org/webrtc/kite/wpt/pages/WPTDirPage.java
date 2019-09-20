package org.webrtc.kite.wpt.pages;

import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.pages.BasePage;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class WPTDirPage extends BasePage {

  private final String pageUrl;

  @FindBy(className = "dir")
  List<WebElement> dirs;

  @FindBy(className = "file")
  List<WebElement> files;

  @FindBy(tagName = "li")
  List<WebElement> items;

  public WPTDirPage(Runner runner, String pageUrl) {
    super(runner);
    this.pageUrl = pageUrl;
  }

  /**
   * Determine whether the item name is a valid directory that contains test
   *
   * @param itemName name of the item to validate
   * @return true if the item name is a valid directory that contains test
   */
  private boolean dirNameIsValid(String itemName) {
    return !(itemName.contains("resources")
        || itemName.startsWith(".")
        || itemName.equalsIgnoreCase("tools"));
  }

  private String fixTestName(String itemName) {
    // particular cases
    if (itemName.endsWith("js")) {
      if (itemName.contains("idlharness.https") || itemName.contains("idlharness.window"))
        return itemName.replace("js", "html");
    }
    return itemName;
  }

  public List<String> getDirNameList() {
    List<String> dirNameList = new ArrayList<>();
    for (WebElement dir : dirs) {
      if (dirNameIsValid(dir.getText())) {
        dirNameList.add(dir.getText() + "/");
      }
    }
    return dirNameList;
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

  public List<String> getTestList() {
    List<String> testList = new ArrayList<>();
    for (WebElement file : files) {
      String fileName = fixTestName(file.getText());
      if (testURLIsValid(fileName)) {
        testList.add(fileName);
      }
    }
    return testList;
  }

  public void openPage() {
    this.webDriver.get(pageUrl);
  }

  /**
   * Determine whether the item name is a valid test name
   *
   * @param itemName name of the item to validate
   * @return true if the item name is a valid test name
   */
  private boolean testURLIsValid(String itemName) {
    if (itemName.endsWith(".html")) {
      if (itemName.contains("manual") || itemName.contains("MediaRecorder-iframe")) {
        // return itemName.contains("none-manual");
        return false;
      } else {
        return true;
      }
    }

    return false;
  }
}
