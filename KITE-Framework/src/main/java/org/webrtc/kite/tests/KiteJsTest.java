/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.tests;

import io.cosmosoftware.kite.steps.StepPhase;
import org.webrtc.kite.config.EndPoint;

import java.util.UUID;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.createDirs;
import static io.cosmosoftware.kite.util.TestUtils.printJsonTofile;

/**
 * The type Kite js test.
 */
public class KiteJsTest extends KiteBaseTest {
  
  // todo : get JS_PATH from config file
  private final String JS_PATH = "js/";
  private final String jsTestImpl;
  private final String tempPath;
  
  /**
   * Instantiates a new Kite js test.
   *
   * @param jsTestImpl the js file
   */
  public KiteJsTest(String jsTestImpl) {
    logger.info(" KiteJsTest");
    this.jsTestImpl = jsTestImpl;
    this.tempPath = "temp/" + jsTestImpl + "_" + timestamp() + "_" +UUID.randomUUID().toString().substring(0,5);
}
  
  @Override
  protected void populateDrivers() {
    // not creating webdriver but write capabilities to temp dirs
    for (int index = 0; index < this.endPointList.size(); index ++) {
      EndPoint endpoint = this.endPointList.get(index);
      createDirs(JS_PATH + tempPath + "/" + index + "/screenshots");
      printJsonTofile(endpoint.toString(), JS_PATH + tempPath + "/" + index + "/capabilities.json");
    }
  }
  
  @Override
  protected void createTestRunners() {
    for (int index = 0; index < this.endPointList.size(); index ++) {
      JsTestRunner runner = new JsTestRunner(this.reports, jsTestImpl, index);
      runner.setNumberOfParticipant(endPointList.size());
      runner.setReportPath(tempPath);
      this.add(runner);
    }
  }

  @Override
  protected void populateTestSteps(TestRunner runner) {}
  
  @Override
  protected void getInfoFromNavigator() {}
  
  @Override
  protected void payloadHandling() {
    super.payloadHandling();
    createDirs(JS_PATH + tempPath);
    printJsonTofile(this.payload.toString(), JS_PATH + tempPath +"/payload.json");
    if (!this.multiThread) {
      logger.error("JavaScript test with KITE cannot be run sequentially at the moment.");
    }
  }
  
  @Override
  protected void testSequentially(StepPhase stepPhase) {
    logger.error("JavaScript test with KITE cannot be run sequentially at the moment.");
  }
  
}
