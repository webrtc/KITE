package org.webrtc.kite.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;

import static io.cosmosoftware.kite.util.ReportUtils.saveScreenshotPNG;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;

public class ScreenshotStep extends TestStep {

  public ScreenshotStep(WebDriver webDriver) {
    super(webDriver);
  }

  @Override
  public String stepDescription() {
    return "Get a screenshot";
  }

  @Override
  protected void step() throws KiteTestException {
    Reporter.getInstance()
        .screenshotAttachment(
            report, "ScreenshotStep_" + timestamp(), saveScreenshotPNG(webDriver));
  }
}
