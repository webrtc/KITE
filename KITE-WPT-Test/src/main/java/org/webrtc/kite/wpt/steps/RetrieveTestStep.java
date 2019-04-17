package org.webrtc.kite.wpt.steps;

import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.wpt.pages.WPTDirPage;

import java.util.ArrayList;
import java.util.List;

import static io.cosmosoftware.kite.util.TestUtils.processTestStep;
import static io.cosmosoftware.kite.util.TestUtils.verifyPathFormat;

public class RetrieveTestStep extends TestStep {
  private final String url;
  private final String sercureURL;
  private final WPTDirPage wptDirPage;
  List<String> testUrlList;
  
  
  public RetrieveTestStep(WebDriver webDriver, String url, String sercureURL, List<String> testUrlList) {
    super(webDriver);
    this.url = verifyPathFormat(url);
    this.sercureURL = verifyPathFormat(sercureURL);
    this.wptDirPage = new WPTDirPage(webDriver, url);
    this.testUrlList = testUrlList;
  }
  
  
  @Override
  public String stepDescription() {
    return "Get test from: " + url;
  }
  
  @Override
  protected void step() {
    List<String> temp =new ArrayList<>();
    wptDirPage.openPage();
    for (String testName: wptDirPage.getTestList()) {
      if (isHttps(testName)) {
        temp.add(sercureURL + testName);
      } else {
        temp.add(url + testName);
      }
    }
    testUrlList.addAll(temp);
    logger.info("Found : " + temp.size() + " test(s) in :" + url);
    logger.info("Total is now: " + testUrlList.size() + " test(s)" );
    for (String dirName : wptDirPage.getDirNameList()) {
      processTestStep(new RetrieveTestStep(this.webDriver, url+dirName, sercureURL+dirName, testUrlList) ,this.report);
    }
    Reporter.getInstance().textAttachment(this.report, "List of tests", temp.toString(), "plain");
  }
  
  
  private boolean isHttps(String testName) {
    if (testName.contains("https")) {
      return true;
    } else {
      if (// leave place for more test filtering
        testName.contains("supported-by-feature-policy [NO]") // this is only on safari ????
        // another condition...
      ) {
        return true;
      } else {
        return false;
      }
    }
  }
}
