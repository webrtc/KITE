package org.webrtc.kite;

import static io.cosmosoftware.kite.util.TestUtils.printJsonTofile;
import static io.cosmosoftware.kite.util.TestUtils.readJsonFile;
import static io.cosmosoftware.kite.util.TestUtils.verifyPathFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.apache.commons.io.FileUtils;

public class ReportMerger {

  public static void main(String[] args) {
    String pathToOldReport = verifyPathFormat(args[0]);
    String pathToNewReport = verifyPathFormat(args[1]);
    List<String> failedCasesInOldReport = extractFailedCases(pathToOldReport);
    for (int index = 0; index < failedCasesInOldReport.size(); index++) {
      String failedCase = failedCasesInOldReport.get(index);
      System.out.println("Processing [" + (index+1) + "/" + failedCasesInOldReport.size() + "]" + failedCase);
      String otherCase = findOtherCaseInNewReports(failedCase, pathToNewReport);
      if (!otherCase.isEmpty()) {
        try {
          System.out.println("Updating " + failedCase + " with " + otherCase);
          updateResultFile(failedCase, otherCase);
          List<String> attachments = getAttachments(otherCase);
          for (String attachment : attachments) {
            try {
              FileUtils.copyFile(new File(pathToNewReport + "/" +  attachment),
                  new File(pathToOldReport+ "/" +  attachment));
            } catch (Exception e) {
              System.out.println("Could not copy " + attachment + ": " + e.getLocalizedMessage());
            }
          }
        } catch (Exception e) {
          System.out.println("Could not update " + failedCase + ": " + e.getLocalizedMessage());
        }
      } else {
        System.out.println("No possible update for " + failedCase);
      }
    }
  }

  private static void updateResultFile(String pathToResultFile, String pathToOtherResultFile)
      throws IOException {
    JsonObject result = readJsonFile(pathToResultFile);
    JsonObject otherResult = readJsonFile(pathToOtherResultFile);
    JsonObjectBuilder builder = Json.createObjectBuilder();
    for (String key: result.keySet()) {
      switch (key) {
        case "steps":
        case "statusDetails":
        case "attachments":
        case "name":
        case "status":
        case "stage": {
          builder.add(key, otherResult.get(key));
          break;
        }
        default: {
          builder.add(key, result.get(key));
          break;
        }
      }
    }

    FileUtils.forceDelete(new File(pathToResultFile));
    printJsonTofile(builder.build().toString(), pathToResultFile);
  }

  private static String findOtherCaseInNewReports(String pathToFailedCase, String pathToNewReports) {
    JsonObject result = readJsonFile(pathToFailedCase);
    boolean webdriverIssue = result.getJsonObject("statusDetails").getString("message").contains("populating web drivers");
    String testCaseName = result.getString("name").split(Pattern.quote("("))[0];
    String fullName = result.getString("fullName");
    List<String> testCasesInNewReports = findTestCasesInReports(pathToNewReports, testCaseName);
    for (String testCasePath : testCasesInNewReports) {
      JsonObject testCase = readJsonFile(testCasePath);
      if (testCase.getString("fullName").equals(fullName)) {
        if (webdriverIssue) {
          return testCasePath;
        } else {
          if (result.getString("status").equals("BROKEN")) {
            if (testCase.getString("status").equals("PASSED")
                || testCase.getString("status").equals("FAILED")) {
              return testCasePath;
            }
          } else { // FAILED
            if (testCase.getString("status").equals("PASSED")) {
              return testCasePath;
            }
          }
        }
      }
    }
    return "";
  }

  private static List<String> findTestCasesInReports(String pathToReportFolder, String testCaseName) {
    List<String> res = new ArrayList<>();
    try {
      File reportFolder = new File(pathToReportFolder);
      File[] subFiles = reportFolder.listFiles();
      for (int index = 0; index < subFiles.length; index++) {
        if (subFiles[index].getName().contains("result.json")) {
          JsonObject result = readJsonFile(subFiles[index].getAbsolutePath());
          if (result.getString("name").contains(testCaseName)) {
            res.add(subFiles[index].getAbsolutePath());
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Error getting failed cases from " + pathToReportFolder + ":\n" + e.getLocalizedMessage());
    }
    return res;
  }

  private static List<String> getAttachments(String pathToTestCase) {
    JsonObject result = readJsonFile(pathToTestCase);
    return getAttachments(result);
  }

  private static List<String> getAttachments(JsonObject step) {
    List<String> res = new ArrayList<>();
    JsonArray steps = step.getJsonArray("steps");
    for (JsonValue subStep : steps) {
      res.addAll(getAttachments((JsonObject) subStep));
    }
    JsonArray attachments = step.getJsonArray("attachments");
    for (JsonValue attachment : attachments) {
      res.add(((JsonObject) attachment).getString("source"));
    }
    return res;
  }

  private static List<String> extractFailedCases(String pathToReportFolder) {
    List<String> res = new ArrayList<>();
    try {
      File reportFolder = new File(pathToReportFolder);
      File[] subFiles = reportFolder.listFiles();
      for (int index = 0; index < subFiles.length; index++) {
        if (subFiles[index].getName().contains("result.json")) {
          JsonObject result = readJsonFile(subFiles[index].getAbsolutePath());
          if (!result.getString("status").equals("PASSED")) {
            res.add(subFiles[index].getAbsolutePath());
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Error getting failed cases from " + pathToReportFolder + ":\n" + e.getLocalizedMessage());
    }
    return res;
  }

}
