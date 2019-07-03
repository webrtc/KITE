/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.tests;

import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.steps.StepPhase;
import org.webrtc.kite.config.client.Client;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
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
  private Process process;
  private Thread nodeServerThread;
  private NodeServerRunnable nodeServerRunnable;
  
  /**
   * Instantiates a new Kite js test.
   *
   * @param jsTestImpl the js file
   */
  public KiteJsTest(String jsTestImpl) {
    logger.info(" KiteJsTest");
    this.jsTestImpl = jsTestImpl;
    this.tempPath = "temp/" + jsTestImpl + "_" + timestamp() + "_" + UUID.randomUUID().toString().substring(0, 5);
  }
  
  @Override
  protected void createTestRunners() {
    for (int index = 0; index < this.tuple.size(); index++) {
      JsTestRunner runner = new JsTestRunner(this.reports, jsTestImpl, logger, reporter, index);
      runner.setNumberOfParticipant(tuple.size());
      runner.setReportPath(tempPath);
      this.add(runner);
    }
  }
  
  @Override
  protected void getInfoFromNavigator() {
  }
  
  @Override
  protected void payloadHandling() {
    super.payloadHandling();
    createDirs(JS_PATH + tempPath);
    printJsonTofile(this.payload.toString(), JS_PATH + tempPath + "/payload.json");
    if (this.networkInstrumentation != null) {
      printJsonTofile(this.networkInstrumentation.getJsonObject().toString(), 
        JS_PATH + tempPath + "/networkInstrumentation.json");
    }
    if (!this.multiThread) {
      logger.error("JavaScript test with KITE cannot be run sequentially at the moment.");
    }
  }
  
  @Override
  protected void populateDrivers() {
    // not creating webdriver but write capabilities to temp dirs
    for (int index = 0; index < this.tuple.size(); index++) {
      Client client = this.tuple.get(index);
      createDirs(JS_PATH + tempPath + "/" + index + "/screenshots");
      printJsonTofile(client.toString(), JS_PATH + tempPath + "/" + index + "/capabilities.json");
    }
  }
  
  @Override
  protected void populateTestSteps(TestRunner runner) {
  }
  
  @Override
  protected void testSequentially(StepPhase stepPhase) {
    logger.error("JavaScript test with KITE cannot be run sequentially at the moment.");
  }

  @Override
  protected void testInParallel(StepPhase stepPhase) {
    int numberOfParticipant = this.tuple.size();
    if (numberOfParticipant > 1) {
      startNodeServer(numberOfParticipant);
    }
    try {
      super.testInParallel(stepPhase);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (numberOfParticipant > 1) {
      stopNodeServer();
    }
  }

  private void startNodeServer(int numberOfParticipant) {
    logger.info("Starting server on " + this.port + "...");
    nodeServerRunnable = new NodeServerRunnable(port, numberOfParticipant, logger, "NodeServer" );
    nodeServerThread = new Thread(nodeServerRunnable);
    nodeServerThread.start();
  }

  private void stopNodeServer() {
    logger.info("Stopping server on " + this.port + "...");
    nodeServerRunnable.stopThread();
    process.destroyForcibly();
  }

  public class NodeServerRunnable implements Runnable {
    private int numberOfParticipant;
    private int port;
    private KiteLogger logger;
    private String logHeader;
    private Boolean running = false;

    public NodeServerRunnable(int port, int numberOfParticipant, KiteLogger logger, String logHeader) {
      this.port = port;
      this.numberOfParticipant = numberOfParticipant;
      this.logger = logger;
      this.logHeader= logHeader;
    }

    @Override
    public void run() {
      running = true;
      List<String> command = java.util.Arrays.asList("node", "server.js",
        "" + this.port, "" + numberOfParticipant);
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.directory((new File(JS_PATH)).getAbsoluteFile());
      builder.redirectErrorStream(true);
      process = null;
      try {
        process = builder.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
      Scanner s = new Scanner(process.getInputStream());
      StringBuilder text = new StringBuilder();

      while(s.hasNextLine() && running) {
        String line = s.nextLine();
        text.append(line);
        text.append("\n");
        logger.info("[nodejs console " + logHeader + " " + this.port + " ] " + line);
      }
    }

    public void stopThread() {
      this.running = false;
    }
  }
}
