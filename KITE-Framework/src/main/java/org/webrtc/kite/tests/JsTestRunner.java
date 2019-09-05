package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.*;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.util.TestUtils;
import org.webrtc.kite.exception.KiteGridException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.TestUtils.readJsonFile;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

public class JsTestRunner extends TestRunner {
  
  //public static as it is being set by KITEServer 
  public static String WORKING_DIR = "js/";
  
  private final String jsTestImpl;
  private final String implJar;
  private int numberOfParticipant;
  private String reportPath;
  
  public JsTestRunner(LinkedHashMap<StepPhase, AllureTestReport> testReport, String jsTestImpl, 
                      KiteLogger logger, Reporter reporter, int id, String implJar) throws KiteGridException {
    super(null, testReport, logger, reporter, id);
    this.jsTestImpl = jsTestImpl;
    this.implJar = implJar;
  }
  
  @Override
  public Object call() {
    // todo: run the js test with a unique id (self-generated)
    // todo: test completion check (by file existence maybe)

    String dir = WORKING_DIR;
    
    if (!"".equals(implJar)) {
      File file = new File(dir + File.separator + implJar);
      if (file.exists()) {
        dir += File.separator + implJar;
      }
    }
    logger.info("JsTestRunner will execute in " + dir);
    String resultPath = dir + this.reportPath + "/" + id + "/result.json";
    try {
      String uuid = this.reports.get(this.stepPhase).getUuid();
      List<String> command = java.util.Arrays.asList("node", jsTestImpl,
        "" + numberOfParticipant, "" + id, uuid, "." + reportPath);
      logger.info("Executing command: " + "node " + jsTestImpl + " " + numberOfParticipant
        + " " + id + " " + uuid + " " + "." + reportPath);
      TestUtils.executeCommand(dir, command, logger, jsTestImpl + "_" + id);
      waitForResultFile(resultPath);
      processResult(dir, readJsonFile(resultPath));
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    }
    return null;
  }
  
  // Retrieves data from the result file generated in JavaScript
  private AllureStepReport processResult(String workingDir, JsonObject result) throws KiteTestException {
    String path = workingDir + this.reportPath + "/" + id + "/";
    if (result != null) {
      String status = result.getString("status", "passed");
      this.reports.get(this.stepPhase).setStatus(Status.fromValue(status));
      
      // Step report
      AllureStepReport stepReport = null;
      JsonArray steps = result.getJsonArray("steps");
      for (int i = 0; i < steps.size(); i++) {
        JsonObject idx = (JsonObject) steps.get(i);
        stepReport = new AllureStepReport(idx.getString("name", "place holder"));
        String stepStatus = idx.getString("status", "passed");
        stepReport.setStatus((Status.fromValue(stepStatus)));
        stepReport.setStartTimestamp((long) idx.getInt("start"));
        stepReport.setStopTimestamp((long) idx.getInt("stop"));
        if (idx.get("attachments") != null) {
          JsonArray attachmentsObj = (JsonArray) idx.get("attachments");
          for (int j = 0; j < attachmentsObj.size(); j++) {
            JsonObject attachmentJSON = (JsonObject) attachmentsObj.get(j);
            String attachmentSource = (String) attachmentJSON.getString("source");
            // To recover file extension
            String extension = attachmentSource.substring(attachmentSource.lastIndexOf(".")).replace(".", "");
            try {
              if (!extension.equals("png")) {
                String text = TestUtils.readFile(path + attachmentSource);
                reporter.textAttachment(stepReport, "attachment", text, extension);
              } else {
                reporter.screenshotAttachment(stepReport, "Screenshot", Files.readAllBytes(Paths.get(path + "/screenshots/" + attachmentSource)));
              }
            } catch (IOException e) {
              throw new KiteTestException("Error reading the report generated in javascript", Status.BROKEN, e);
            }
          }
        }
        JsonArray childSteps = idx.getJsonArray("steps");
        for (int k = 0; k < childSteps.size(); k++) {
          JsonObject child = (JsonObject) childSteps.get(k);
          stepReport.addStepReport(processResult(workingDir, child));
        }
        this.reports.get(this.stepPhase).addStepReport(stepReport);
      }
      return stepReport;
    }
    throw new KiteTestException("There's a null value in the report", Status.BROKEN);
  }

  public void setNumberOfParticipant(int numberOfParticipant) {
    this.numberOfParticipant = numberOfParticipant;
  }
  
  public void setReportPath(String reportPath) {
    this.reportPath = "/" + reportPath;
  }
  
  private void waitForResultFile(String filePath) throws KiteTestException {
    logger.info("JsTestRunner filePath = " + filePath);
    for (int wait = 0; wait < TEN_SECOND_INTERVAL; wait += ONE_SECOND_INTERVAL) {
      File resultFile = new File(filePath);
      logger.info("resultFile = " + resultFile.getAbsolutePath());
      if (resultFile.exists()) {
        return;
      }
      waitAround(ONE_SECOND_INTERVAL);
    }
    throw new KiteTestException("Could not find result file generated by Js", Status.FAILED);
  }
}
