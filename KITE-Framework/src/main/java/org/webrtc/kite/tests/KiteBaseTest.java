package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.instrumentation.Instrumentation;
import io.cosmosoftware.kite.instrumentation.Scenario;
import io.cosmosoftware.kite.manager.RoomManager;
import io.cosmosoftware.kite.report.AllureStepReport;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import io.cosmosoftware.kite.util.ReportUtils;
import io.cosmosoftware.kite.util.TestUtils;
import io.cosmosoftware.kite.util.WebDriverUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.webrtc.kite.WebDriverFactory;
import org.webrtc.kite.config.App;
import org.webrtc.kite.config.Browser;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.exception.KiteGridException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.processTestStep;
import static org.webrtc.kite.Utils.populateInfoFromNavigator;

public abstract class KiteBaseTest {
  protected String name = this.getClass().getSimpleName();
  protected Logger logger = Logger.getLogger(this.getClass().getName());
  protected final List<TestRunner> testRunners = new ArrayList<>();
  protected String parentSuite = "";
  protected String suite = "";
  
  protected String description;
  protected boolean multiThread = true;
  protected int tupleSize;
  protected JsonObject payload;
  protected Instrumentation instrumentation = null;

  protected String url;
  private boolean fastRampUp = false;
  private boolean getStats = false;
  private JsonArray selectedStats = null;
  private int statsCollectionInterval = 1;
  private int statsCollectionTime = 10;
  private boolean takeScreenshotForEachTest = false; // false by default
  private int testTimeout = 60;
  private int maxUsersPerRoom = 1;
  private int expectedTestDuration = 60; //in minutes
  private static RoomManager roomManager = null;

  
  protected List<EndPoint> endPointList;

  protected List<WebDriver> webDriverList = new ArrayList<>();

  protected Map<WebDriver, Map<String, Object>> sessionData =
      new HashMap<WebDriver, Map<String, Object>>();
  protected ArrayList<Scenario> scenarioArrayList = new ArrayList<>();

  /**
   * The Remote address.
   */
  protected String remoteAddress;
  
  protected AllureTestReport report;
  
  public KiteBaseTest() {
    fillOutReport();
  }

  public JsonObject execute() {
    try {
      init();
      logger.info("Test initialisation completed...");
      if (multiThread) {
        testInParallel();
      } else {
        testSequentially();
      }
    } catch (Exception e) {
      // this is for the initiation mostly
      Reporter.getInstance().processException(report,e);
    } finally {
      if (!webDriverList.isEmpty()) {
        WebDriverUtils.closeDrivers(this.webDriverList);
      }
    }
    return report.toJson();
  }

  public void init() throws KiteTestException, IOException {
    this.report.setStartTimestamp();
    AllureStepReport initStep = new AllureStepReport("Creating webdrivers and preparing threads..");
    instrumentation = getInstrumentation();
    try {
      initStep.setStartTimestamp();
      if (this.payload != null) {
        payloadHandling();
        Reporter.getInstance().jsonAttachment(initStep, "Test payload", this.payload);
      } else {
        logger.warn("payload is null");
      }
      Reporter.getInstance().setLogger(logger);
      populateDrivers();
      getInfoFromNavigator();
      populateTestRunners();
    } catch (KiteGridException e) {
      logger.error("Exception while populating web drivers, " +
        "closing already created webdrivers...\r\n" + getStackTrace(e));
      Reporter.getInstance().textAttachment(initStep, "KiteGridException", getStackTrace(e), "plain");
      initStep.setStatus(Status.FAILED);
      throw new KiteTestException("Exception while populating web drivers", Status.FAILED);
    }
    initStep.setStatus(Status.PASSED);
    this.report.addStepReport(initStep);
  }



  /**
   * Restructuring the test according to options given in payload object from config file. This
   * function processes the parameters common to all load tests.
   */
  protected void payloadHandling() {
    url = payload.getString("url", null);
    testTimeout = payload.getInt("testTimeout", testTimeout);
    takeScreenshotForEachTest =
      payload.getBoolean("takeScreenshotForEachTest", takeScreenshotForEachTest);
    getStats = payload.getBoolean("getStats", false);
    multiThread = payload.getBoolean("multiThread", true);
    statsCollectionTime = payload.getInt("statsCollectionTime", statsCollectionTime);
    statsCollectionInterval = payload.getInt("statsCollectionInterval", statsCollectionInterval);
    if (payload.containsKey("selectedStats")) {
      selectedStats = payload.getJsonArray("selectedStats");
    }
    expectedTestDuration = payload.getInt("expectedTestDuration", expectedTestDuration);
    maxUsersPerRoom = payload.getInt("usersPerRoom", 0);
    if (maxUsersPerRoom > 0) {
      roomManager = RoomManager.getInstance(url, getMaxUsersPerRoom());
    }
    fastRampUp = payload.getBoolean("fastRampUp", fastRampUp);
    if (this.payload.containsKey("scenarios")) {
      JsonArray jsonArray2 = this.payload.getJsonArray("scenarios");
      for (int i = 0; i < jsonArray2.size(); ++i) {
        try {
          this.scenarioArrayList.add(new Scenario(jsonArray2.getJsonObject(i), logger, i, instrumentation));
        } catch (Exception e) {
          logger.error("Invalid scenario number : " + i + "\r\n" + ReportUtils.getStackTrace(e));
        }
      }
    }
  }
  
  /**
   /**
   * Executes the tests in parallel.
   *
   * @throws Exception if an Exception occurs during method execution.
   */
  private void testInParallel() throws Exception {
    logger.info("Starting the execution of the test runners in parallel");
    expectedTestDuration = Math.max(expectedTestDuration, this.testRunners.size() * 2);
    logger.info("testRunners.size() = " + testRunners.size());
    ExecutorService executorService = Executors.newFixedThreadPool(testRunners.size());
    List<Future<Object>> futureList =
      executorService.invokeAll(testRunners, expectedTestDuration, TimeUnit.MINUTES);
    executorService.shutdown();
    for (Future<Object> future : futureList) {
      future.get();
    }
  }
  
  /**
   * Executes the tests sequentially.
   * Assuming that all the callables have the same number of steps
   * If not, overwrite this function with appropriate order.
   */
  protected void testSequentially(){
    logger.info("Starting the execution of the test runners sequentially");
    for (int i = 0; i < testRunners.get(0).getSteps().size(); i++) {
      for (TestRunner runner : testRunners) {
        TestStep step = runner.getSteps().get(i);
        processTestStep(step, report);
      }
    }
  }

  /**
   * Check if the steps are completed for all runners.
   *
   * @param stepName class name of the step
   * @return true if the step has been completed on all runners
   */

  public boolean stepCompleted(String stepName)  throws  KiteTestException {
    for (TestRunner runner : testRunners) {
      if (!runner.completed(stepName)) {
        return false;
      }
    }
    return true;
  }

  
  protected void fillOutReport(){
    this.report = new AllureTestReport(timestamp());
    this.report.setFullName(getClass().getName());
    this.report.addLabel("package", getClass().getPackage().toString());
    this.report.addLabel("testClass", getClass().toString());
    this.report.addLabel("testMethod", "execute");
    try {
      this.report.addLabel("host", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      this.report.addLabel("host", "N/A");
    }
  }

  /**
   * Constructs a list of web drivers against the number of provided config objects.
   *
   * @throws MalformedURLException if no protocol is specified in the remoteAddress of a config
   *     object, or an unknown protocol is found, or spec is null.
   */
  //  @AllureStepReport("Populating webdriver from endpoints")
  protected void populateDrivers() throws KiteGridException {
    for (EndPoint endPoint : this.endPointList) {
      try {
        WebDriver webDriver = WebDriverFactory.createWebDriver(endPoint, null, null);
        this.webDriverList.add(webDriver);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("end_point", endPoint);
        String node =
            TestUtils.getNode(
                endPoint.getRemoteAddress(),
                ((RemoteWebDriver) webDriver).getSessionId().toString());
        if (node != null) {
          map.put("node_host", node);
        }
        this.sessionData.put(webDriver, map);
      } catch (Exception e) {
        throw new KiteGridException(
            e.getClass().getSimpleName()
                + " creating webdriver for \n"
                + endPoint.getJsonObject().toString()
                + ":\n"
                + e.getLocalizedMessage());
      }
    }
  }

  /**
   * Retrieves the navigator.userAgent from all of the config objects and passes it to the the respective
   * Config object for processing.
   */
//  @AllureStepReport("Populating webdriver from navigators")
  protected void getInfoFromNavigator() {
    for (int i = 0; i < tupleSize; i++) {
      if (this.endPointList.get(i) instanceof Browser)
      populateInfoFromNavigator(this.webDriverList.get(i), (Browser)this.endPointList.get(i));
    }
  }
  
  public void setTupleSize(int tupleSize) {
    this.tupleSize = tupleSize;
  }
  
  public void setDescription(String description) {
    this.description = description;
    this.report.setDescription(description);
  }

  /**
   * Method to set a the remoteAddress (IP of the hub).
   *
   * @param remoteAddress String the address of the hub
   */
  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public void setPayload(JsonValue payload) {
    this.payload = (JsonObject)payload;
  }

  public void setInstrumentation(Instrumentation instrumentation) {
    this.instrumentation = instrumentation;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }
  
  public void setEndPointList(List<EndPoint> endPointList) {
    this.endPointList = endPointList;
    this.report.setName(generateTestCaseName());
  }
  
  public void setParentSuite(String parentTestSuite) {
    this.parentSuite = parentTestSuite;
    this.report.addLabel("parentSuite", parentTestSuite);
  }
  
  public void setSuite(String suite) {
    this.suite = suite;
    this.report.addLabel("suite", suite);
  }


  public List<WebDriver> getWebDriverList() {
    return webDriverList;
  }
  
  public AllureTestReport getReport() {
    return report;
  }
  
  protected String generateTestCaseName() {
    String name = "";
    for (int index = 0; index < endPointList.size(); index ++) {
      EndPoint endPoint = endPointList.get(index);
      name += endPoint.getPlatform().substring(0,3);
      if (endPoint instanceof Browser) {
        name += "_" + ((Browser)endPoint).getBrowserName().substring(0,2);
        name += "_" + ((Browser)endPoint).getVersion();
      } else {
        name += "_" + ((App)endPoint).getDeviceName().substring(0,2);
      }
      
      if (index < endPointList.size() -1) {
        name += "-";
      }
    }
    return name;
  }

  /**
   * Gets stats.
   *
   * @return true to call and collect getStats, false otherwise, as set in the config file.
   */
  public boolean getStats() {
    return getStats;
  }

  /**
   * Gets stats collection interval.
   *
   * @return statsCollectionInterval Time interval between each getStats call (Default 1)
   */
  public int getStatsCollectionInterval() {
    return statsCollectionInterval;
  }

  /**
   * Gets stats collection time.
   *
   * @return statsCollectionTime Time in seconds to collect stats (Default 10)
   */
  public int getStatsCollectionTime() {
    return statsCollectionTime;
  }

  /**
   * Gets max users per room.
   *
   * @return the max users per room
   */
  public int getMaxUsersPerRoom() {
    return maxUsersPerRoom;
  }

  /**
   * Gets the Instrumentation object
   *
   * @return the network instrumentation object
   */
  public Instrumentation getInstrumentation() {
    return this.instrumentation;
  }

  /**
   *
   * @return the jsonArray of selected stats for getStats
   */
  public JsonArray getSelectedStats() {
    return selectedStats;
  }

  /**
   *
   * @return the expected test duration (in minutes)
   */
  public int getExpectedTestDuration() {
    return expectedTestDuration;
  }

  /**
   * Take screenshot for each test boolean.
   *
   * @return true or false as set in config file
   */
  public boolean takeScreenshotForEachTest() {
    return takeScreenshotForEachTest;
  }

  /**
   * Fast ramp up boolean.
   *
   * @return true for fastRampUp
   */
  public boolean fastRampUp() {
    return fastRampUp;
  }

  /**
   *
   *  Sets the expected test duration (in minutes)
   */
  public void setExpectedTestDuration(int expectedTestDuration) {
    this.expectedTestDuration = expectedTestDuration;
  }

  /**
   *
   * @return the roomManager
   */
  public static RoomManager getRoomManager() {
    return roomManager;
  }


  /**
   * Populate the testRunners.
   */
  protected void populateTestRunners() {
    createTestRunners();
    for (TestRunner runner:testRunners) {
      populateTestSteps(runner);
      for (TestStep step:runner.getSteps()) {
        step.setLogger(logger);
      }
    }
  }


  /**
   * Creates the TestRunners and add them to the testRunners list.
   */
  protected void createTestRunners() {
    for (int index = 0; index < this.webDriverList.size(); index ++) {
      this.testRunners.add(new TestRunner(this.webDriverList.get(index), this.report, index));
    }
  }


  /**
   * Abstract method to be overridden by the client to add steps to the TestRunner.
   *
   * @param runner the TestRunner
   */
  protected abstract void populateTestSteps(TestRunner runner);

  public void setName(String name) { this.name = name; }

}
