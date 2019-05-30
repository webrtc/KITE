package org.webrtc.kite.tests;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.instrumentation.Instrumentation;
import io.cosmosoftware.kite.instrumentation.Scenario;
import io.cosmosoftware.kite.manager.RoomManager;
import io.cosmosoftware.kite.report.AllureStepReport;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import io.cosmosoftware.kite.util.ReportUtils;
import io.cosmosoftware.kite.util.TestHelper;
import io.cosmosoftware.kite.util.TestUtils;
import io.cosmosoftware.kite.util.WebDriverUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.webrtc.kite.WebDriverFactory;
import org.webrtc.kite.config.App;
import org.webrtc.kite.config.Browser;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.config.Tuple;
import org.webrtc.kite.exception.KiteGridException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.processTestStep;
import static org.webrtc.kite.Utils.populateInfoFromNavigator;

public abstract class KiteBaseTest extends ArrayList<TestRunner> {
  
  protected Logger logger = Logger.getLogger(this.getClass().getName());
  protected String name = this.getClass().getSimpleName();
  protected String parentSuite = "";
  protected String suite = "";
  protected String description;
  protected String url;
  protected String configFilePath;
  protected String remoteAddress;
  protected int tupleSize;
  protected boolean multiThread = true;
  protected boolean isLoadTest = false;
  protected int meetingDuration = 0; //in seconds
  protected JsonObject payload;
  protected JsonObject getStatsConfig = null;
  protected Instrumentation instrumentation = null;

  protected final LinkedHashMap<StepPhase, AllureTestReport> reports = new LinkedHashMap<>();
  protected List<EndPoint> endPointList = new ArrayList<>();
  protected final List<WebDriver> webDriverList = new ArrayList<>();
  protected final ArrayList<Scenario> scenarioArrayList = new ArrayList<>();
  protected final Map<WebDriver, Map<String, Object>> sessionData = new HashMap<WebDriver, Map<String, Object>>();
  private List<StepPhase> phases = null;
  protected String kiteServerGridId;
  
  private int testTimeout = 60;
  private int maxUsersPerRoom = 1;
  private int expectedTestDuration = 60; //in minutes
  private boolean takeScreenshotForEachTest = false; // false by default
  private boolean fastRampUp = false;
  private boolean loopRooms = false;
  private boolean csvReport = false;

  private boolean closeWebDrivers = true;
  private static RoomManager roomManager = null;
  protected JsonArray networks;
  
  public KiteBaseTest() {}
  
  public void setPhases(List<StepPhase> phases) {
    this.phases = phases;
    for (StepPhase stepPhase : phases) {
      fillOutReport(stepPhase);
    }
  }

  /**
   * Sets config file path.
   *
   * @param configFilePath the config file path
   */
  public void setConfigFilePath(String configFilePath) {
    this.configFilePath = configFilePath;
  }

  /**
   * Gets config file path.
   *
   * @return the config file path
   */
  public String getConfigFilePath() {
    return configFilePath;
  }

  /**
   * Execute json object.
   *
   * @return the json object
   */
  public JsonObject execute() {
    return execute(phases.get(0));
  }
  
  /**
   * Execute json object.
   * 
   * @param stepPhase the phase for this Step.
   * @return the json object
   */
  public JsonObject execute(StepPhase stepPhase) {
    try {
      if (stepPhase != StepPhase.LOADREACHED) {
        init(stepPhase);
      }
      setStepPhase(stepPhase);
      if (multiThread) {
        testInParallel(stepPhase);
      } else {
        testSequentially(stepPhase);
      }
    } catch (Exception e) {
      // this is for the initiation mostly
      Reporter.getInstance().processException(reports.get(stepPhase), e, false);
    } finally {
      if (this.closeWebDrivers && stepPhase.isLastPhase()) {
        WebDriverUtils.closeDrivers(this.webDriverList);
      }
      reports.get(stepPhase).setStopTimestamp();
    }
    return reports.get(stepPhase).toJson();
  }
  
  /**
   * Init.
   *
   * @throws KiteTestException the kite test exception
   */
  public void init(StepPhase stepPhase) throws KiteTestException {
    this.reports.get(stepPhase).setStartTimestamp();
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
    this.reports.get(stepPhase).addStepReport(initStep);
  }
  
  /**
   * Restructuring the test according to options given in payload object from config file. This
   * function processes the parameters common to all load tests.
   */
  protected void payloadHandling() {
    logger.info("the payload is " + payload.toString());
    url = payload.getString("url", null);
    testTimeout = payload.getInt("testTimeout", testTimeout);
    takeScreenshotForEachTest =
      payload.getBoolean("takeScreenshotForEachTest", false);
    getStatsConfig = payload.getJsonObject("getStats");
    multiThread = payload.getBoolean("multiThread", true);
    expectedTestDuration = payload.getInt("expectedTestDuration", expectedTestDuration);
    meetingDuration = this.payload.getInt("meetingDuration", meetingDuration);
    setExpectedTestDuration(Math.max(getExpectedTestDuration(), (meetingDuration + 300) / 60));
    maxUsersPerRoom = payload.getInt("usersPerRoom", maxUsersPerRoom);
    loopRooms = payload.getBoolean("loopRooms", loopRooms);
    csvReport = payload.getBoolean("csvReport", csvReport);
    if (maxUsersPerRoom > 0) {
      roomManager = RoomManager.getInstance(this.name, url, getMaxUsersPerRoom(), loopRooms);
    }
    String[] rooms;
    if(payload.getJsonArray("rooms") != null && maxUsersPerRoom > 0) {
      JsonArray roomArr = this.payload.getJsonArray("rooms");
      rooms = new String[roomArr.size()];
      for (int i = 0; i < roomArr.size(); i++) {
        rooms[i] = roomArr.getString(i);
        roomManager.setRoomNames(rooms);
      }
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
  private void testInParallel(StepPhase stepPhase) throws Exception {
    logger.info("Starting the execution of the test runners in parallel " + stepPhase.getName());
    expectedTestDuration = Math.max(expectedTestDuration, size() * 2);
    ExecutorService executorService = Executors.newFixedThreadPool(size());
    List<Future<Object>> futureList =
      executorService.invokeAll(this, expectedTestDuration, TimeUnit.MINUTES);
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
  protected void testSequentially(StepPhase stepPhase){
    logger.info("Starting the execution of the test runners sequentially " + stepPhase.getName());
    for (int i = 0; i < get(0).size(); i++) {
      for (TestRunner runner : this) {
        TestStep step = runner.get(i);
        processTestStep(stepPhase, step, reports.get(stepPhase));
      }
    }
  }

  private void setStepPhase(StepPhase stepPhase) {
    for (TestRunner runner : this) {
      runner.setStepPhase(stepPhase);
    }
  }
  
  /**
   * Check if the steps are completed for all runners.
   *
   * @param stepName class name of the step
   *
   * @return true if the step has been completed on all runners
   * @throws KiteTestException the kite test exception
   */
  public boolean stepCompleted(String stepName)  throws  KiteTestException {
    for (TestRunner runner : this) {
      if (!runner.completed(stepName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Fill out reports.get(stepPhase).
   */
  protected void fillOutReport(StepPhase stepPhase){
    this.reports.put(stepPhase, new AllureTestReport(timestamp()));
    this.reports.get(stepPhase).setFullName(getClass().getName());
    this.reports.get(stepPhase).addLabel("package", getClass().getPackage().toString());
    this.reports.get(stepPhase).addLabel("testClass", getClass().toString());
    this.reports.get(stepPhase).addLabel("testMethod", "execute");
    try {
      this.reports.get(stepPhase).addLabel("host", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      this.reports.get(stepPhase).addLabel("host", "N/A");
    }
    
    logger.info("Finished filling out initial report for phase " + stepPhase.getName());
  }
  
  /**
   * Constructs a list of web drivers against the number of provided config objects.
   *
   * @throws KiteGridException the kite grid exception
   * @throws KiteTestException the kite test exception
   */
  protected void populateDrivers() throws KiteGridException, KiteTestException {
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
  protected void getInfoFromNavigator() {
    for (int i = 0; i < endPointList.size(); i++) {
      if (this.endPointList.get(i) instanceof Browser) {
        populateInfoFromNavigator(this.webDriverList.get(i), (Browser) this.endPointList.get(i)); 
      }
    }
  }
  
  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
    for (StepPhase stepPhase : phases) {
      this.reports.get(stepPhase).setDescription(stepPhase.getName() + description);
    }
  }
  
  /**
   * Method to set a the remoteAddress (IP of the hub).
   *
   * @param remoteAddress String the address of the hub
   */
  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }
  
  /**
   * Sets payload.
   *
   * @param payload the payload
   */
  public void setPayload(JsonValue payload) {
    this.payload = (JsonObject)payload;
  }

  /**
   * Sets instrumentation.
   *
   * @param instrumentation the instrumentation
   */
  public void setInstrumentation(Instrumentation instrumentation) {
    this.instrumentation = instrumentation;
  }

  /**
   * Sets logger.
   *
   * @param logger the logger
   */
  public void setLogger(Logger logger) {
    this.logger = logger;
  }
  
  /**
   * Sets end point list.
   *
   * @param tuple the end point list
   */
  public void setEndPointList(Tuple tuple) {
    this.endPointList = tuple;
    this.tupleSize = endPointList.size();
    for (StepPhase stepPhase : phases) {
      this.reports.get(stepPhase).setName(stepPhase.getName() + generateTestCaseName());
    }
  }
  
  /**
   * Sets parent suite.
   *
   * @param parentTestSuite the parent test suite
   */
  public void setParentSuite(String parentTestSuite) {
    this.parentSuite = parentTestSuite;
    for (StepPhase stepPhase : phases) {
      this.reports.get(stepPhase).addLabel("parentSuite", parentTestSuite);
    }
  }
  
  /**
   * Sets suite.
   *
   * @param suite the suite
   */
  public void setSuite(String suite) {
    this.suite = suite;
    for (StepPhase stepPhase : phases) {
      this.reports.get(stepPhase).addLabel("suite", suite);
    }
  }
  
  
  /**
   * Gets web driver list.
   *
   * @return the web driver list
   */
  public List<WebDriver> getWebDriverList() {
    return webDriverList;
  }
  
  /**
   * Gets report.
   *
   * @return the report
   */
  public AllureTestReport getReport(StepPhase stepPhase) {
    return reports.get(stepPhase);
  }
  
  /**
   * Generate test case name string.
   *
   * @return the string
   */
  protected String generateTestCaseName() {
    StringBuilder name = new StringBuilder();
    for (int index = 0; index < endPointList.size(); index ++) {
      EndPoint endPoint = endPointList.get(index);
      name.append(endPoint.getPlatform().substring(0, 3));
      if (endPoint instanceof Browser) {
        name.append("_").append(((Browser) endPoint).getBrowserName().substring(0, 2));
        name.append("_").append(((Browser) endPoint).getVersion());
      } else {
        name.append("_").append(((App) endPoint).getDeviceName().substring(0, 2));
      }
      
      if (index < endPointList.size() -1) {
        name.append("-");
      }
    }
    return name.toString();
  }
  
  /**
   * Gets stats.
   *
   * @return true to call and collect getStats, false otherwise, as set in the config file.
   */
  public boolean getStats() {
    return getStatsConfig != null ? getStatsConfig.getBoolean("enabled", false) : false;
  }
  
  /**
   * Gets stats collection interval.
   *
   * @return statsCollectionInterval Time interval between each getStats call (Default 1)
   */
  public int getStatsCollectionInterval() {    
    return getStatsConfig != null ? getStatsConfig.getInt("statsCollectionInterval", 0) : 0;
  }
  
  /**
   * Gets stats collection time.
   *
   * @return statsCollectionTime Time in seconds to collect stats (Default 10)
   */
  public int getStatsCollectionTime() {
    return getStatsConfig != null ? getStatsConfig.getInt("statsCollectionTime", 0) : 0;
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
   * Gets selected stats.
   *
   * @return the jsonArray of selected stats for getStats
   */
  public JsonArray getSelectedStats() {
    return getStatsConfig != null && getStatsConfig.containsKey("selectedStats") ?
      getStatsConfig.getJsonArray("selectedStats") : null;
  }
  
  /**
   * Gets peerConnections 
   *
   * @return the jsonArray of peerConnections
   */
  public JsonArray getPeerConnections() {
    return getStatsConfig != null && getStatsConfig.containsKey("peerConnections") ?
      getStatsConfig.getJsonArray("peerConnections") : null;
  }
  
  /**
   * Gets expected test duration.
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
   * Sets the expected test duration (in minutes)
   *
   * @param expectedTestDuration the expected test duration
   */
  public void setExpectedTestDuration(int expectedTestDuration) {
    this.expectedTestDuration = expectedTestDuration;
  }
  
  /**
   * Gets room manager.
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
    for (TestRunner runner: this) {
      populateTestSteps(runner);
      for (TestStep step: runner) {
        step.setLogger(logger);
      }
      runner.setCsv(csvReport);
    }
  }
  
  /**
   * Creates the TestRunners and add them to the testRunners list.
   */
  protected void createTestRunners() {
    for (int index = 0; index < this.webDriverList.size(); index ++) {
      this.add(new TestRunner(this.webDriverList.get(index), this.reports, index));
    }
    this.tupleSize = size();
  }

  /**
   * Abstract method to be overridden by the client to add steps to the TestRunner.
   *
   * @param runner the TestRunner
   */
  protected abstract void populateTestSteps(TestRunner runner);

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
    for (StepPhase stepPhase : phases) {
      this.reports.get(stepPhase).setName(stepPhase.getName() + name);
    }
  }

  public String getKiteServerGridId() {
    return kiteServerGridId;
  }

  public void setKiteServerGridId(String kiteServerGridId) {
    this.kiteServerGridId = kiteServerGridId;
  }
  
  
  public void setLoadTest(boolean loadTest) {
    isLoadTest = loadTest;
  }
  
  public boolean isLoadTest() {
    return isLoadTest;
  }

  public void setNetworks(JsonArray networks) {
    this.networks = networks;
  }

  public void setCloseWebDrivers(boolean closeWebDrivers) {
    this.closeWebDrivers = closeWebDrivers;
  }

}
