package org.webrtc.kite.tests;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;
import static org.webrtc.kite.Utils.populateInfoFromNavigator;

import io.cosmosoftware.kite.entities.Stage;
import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.instrumentation.NetworkInstrumentation;
import io.cosmosoftware.kite.instrumentation.Scenario;
import io.cosmosoftware.kite.manager.RoomManager;
import io.cosmosoftware.kite.report.AllureStepReport;
import io.cosmosoftware.kite.report.AllureTestReport;
import io.cosmosoftware.kite.report.Container;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.StepSynchronizer;
import io.cosmosoftware.kite.steps.TestStep;
import io.cosmosoftware.kite.util.ReportUtils;
import io.cosmosoftware.kite.util.WebDriverUtils;
import java.beans.Transient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;
import org.webrtc.kite.exception.KiteGridException;

public abstract class KiteBaseTest extends ArrayList<TestRunner> implements StepSynchronizer {

  protected final LinkedHashMap<StepPhase, AllureTestReport> reports = new LinkedHashMap<>();
  protected final ArrayList<Scenario> scenarioArrayList = new ArrayList<>();
  protected final Map<WebDriver, Map<String, Object>> sessionData = new HashMap<>();
  protected String configFilePath;
  protected String description;
  protected JsonObject getStatsConfig = null;
  protected boolean isLoadTest = false;
  protected String kiteServerGridId;
  protected KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());
  protected int meetingDuration = 0; //in seconds
  protected boolean multiThread = true;
  protected String name = this.getClass().getSimpleName();
  protected NetworkInstrumentation networkInstrumentation = null;
  protected String parentSuite = "";
  protected JsonObject payload;
  protected int port;
  protected Object resultObject;
  protected Container suite;
  protected Tuple tuple;
  protected int tupleSize;
  protected int currentIteration = 0;
  protected String url;
  protected Reporter reporter;
  protected RoomManager roomManager;
  protected int windowWidth = 0;
  protected int windowHeight = 0;
  private String testJar = null;
  protected boolean jsTest = false;
  protected TestConfig testConfig;
  private boolean consoleLogs = true;
  private boolean csvReport = false;
  private int expectedTestDuration = 60; //in minutes
  private boolean fastRampUp = false;
  private boolean finished = false;
  private boolean loopRooms = false;
  private int maxUsersPerRoom = 1;
  private List<StepPhase> phases = new ArrayList<>();
  private boolean takeScreenshotForEachTest = false; // false by default
  private int testTimeout = 60;
  private long delayForClosing = 0;

  public KiteBaseTest() {}

  /**
   * Creates the TestRunners and add them to the testRunners list.
   */
  protected void createTestRunners() throws IOException, KiteTestException {
    try {
      if (multiThread) {
        //creates the TestRunner in parallel
        List<TestRunnerCreator> creatorList = new ArrayList<>();
        for (int index = 0; index < this.tuple.size(); index++) {
          creatorList.add(new TestRunnerCreator(this.tuple.get(index), this, isLoadTest ? (this.currentIteration + index) : index));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(creatorList.size());
        List<Future<TestRunner>> futureList = executorService.invokeAll(creatorList,
          expectedTestDuration, TimeUnit.MINUTES);
        executorService.shutdown();
        for (Future<TestRunner> future : futureList) {
          this.add(future.get());
        }
      } else {
        for (int index = 0; index < this.tuple.size(); index++) {
          this.add(new TestRunner(this.tuple.get(index), this, index));
        }
      }
      this.tupleSize = size();
      for (TestRunner runner : this) {
        populateTestSteps(runner);
      }
    } catch (Exception e) {
      logger.debug("Error creating test runners : \n" + getStackTrace(e));
    }
  }

  /**
   * Execute json object.
   *
   * @return the json object
   */
  public Object execute() {
    return execute(phases.get(0));
  }


  /**
   * Execute json object.
   *
   * @param stepPhase the phase for this Step.
   *
   * @return the json object
   */
  public Object execute(StepPhase stepPhase) {
    try {
      logger.info("execute(" + stepPhase + ") for the test case: " + this.generateTestCaseName());
      if (!stepPhase.equals(StepPhase.LOADREACHED)) {
        init(stepPhase);
      }
      if (!this.isLoadTest) {
        this.reports.get(stepPhase).setStartTimestamp();
      }

      if (multiThread) {
        testInParallel(stepPhase);
      } else {
        testSequentially(stepPhase);
      }

      if (!this.isLoadTest) {
        this.reports.get(stepPhase).setStopTimestamp();
      }
    } catch (Exception e) {
      // this is for the initiation mostly
      reporter.processException(reports.get(stepPhase), e, false);
    } finally {
      logger.info("execute(" + stepPhase + ") completed.");
      if (stepPhase.isLastPhase()) {
        if (!jsTest) {
          keepBrowsersAlive();
        }
        terminate(stepPhase);
      } else {
        if (this.isLoadTest) {
          setStepPhaseToRunner(StepPhase.LOADREACHED);
        }
      }
    }
    if (resultObject == null && !this.isLoadTest) {
      resultObject = reports.get(stepPhase).toJson();
      // todo results for load test
    }
    return resultObject;
  }

  /**
   * Fast ramp up boolean.
   *
   * @return true for fastRampUp
   */
  public boolean fastRampUp() {
    return fastRampUp;
  }

  private void init(StepPhase stepPhase) throws KiteTestException {
    AllureStepReport initStep =
        new AllureStepReport("Filling out reports, handling payload, creating runners..");
    try {
      if (!this.isLoadTest) {
        logger.info("Not load test, init report for test");
        fillOutReport(stepPhase); // only for interop now
        initStep.setPhase(stepPhase);
        this.reports.get(stepPhase).addStepReport(initStep);
        initStep.setStartTimestamp();
      }
      if (this.payload != null) {
        logger.info("payload handling");
        payloadHandling();
      } else {
        logger.warn("payload is null");
      }
      reporter.setLogger(logger);
      addExtraCategories();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> terminate(stepPhase)));
      logger.info("Creating runners");
      createTestRunners();
      getInfoFromNavigator();
      setStepPhaseToRunner(stepPhase); // set phase for runner
      initStep.setStatus(Status.PASSED);
      Runtime.getRuntime().removeShutdownHook(new Thread(() -> terminate(stepPhase)));
    } catch (IOException e) {
      logger.error(getStackTrace(e));
      reporter.textAttachment(initStep, "IOException", getStackTrace(e), "plain");
      initStep.setStatus(Status.FAILED);
      throw new KiteTestException("IOException", Status.BROKEN, e);
    }
  }

  public void terminate(StepPhase stepPhase) {
    if (!this.finished) {
      this.finished = true;
      for (TestRunner runner : this) {
        runner.terminate();
      }
      AllureStepReport terminateStep = new AllureStepReport("Cleaning up and finishing the test");
      terminateStep.setStartTimestamp();
      if (stepPhase.isLastPhase() && !jsTest) {
        logger.info("Terminating, quiting webdriver");
        WebDriverUtils.closeDrivers(this.tuple.getWebDrivers());
      }

      terminateStep.setStopTimestamp();

      // try to put stop time for all phase
      for (StepPhase phase : reports.keySet()) {
        AllureStepReport report = reports.get(phase);
        if (!this.isLoadTest) {
          report.addStepReport(terminateStep);
          if (!report.getStage().equals(Stage.FINISHED)) {
            report.setStopTimestamp();
          }
        }
      }
    }
  }

  /**
   * /**
   * Executes the tests in parallel.
   *
   * @throws Exception if an Exception occurs during method execution.
   */
  protected void testInParallel(StepPhase stepPhase) throws Exception {
    logger.info("Starting the execution of the test runners in parallel " + stepPhase.getName());
    expectedTestDuration = Math.max(expectedTestDuration, size() * 2);
    if (size() > 0) {
      ExecutorService executorService = Executors.newFixedThreadPool(size());
      List<Future<Object>> futureList =
          executorService.invokeAll(this, expectedTestDuration, TimeUnit.MINUTES);
      executorService.shutdown();
      for (Future<Object> future : futureList) {
        future.get();
      }
    }
  }

  /**
   * Executes the tests sequentially.
   * Assuming that all the callables have the same number of steps
   * If not, overwrite this function with appropriate order.
   */
  protected void testSequentially(StepPhase stepPhase) throws KiteGridException {
    logger.info("Starting the execution of the test runners sequentially " + stepPhase.getName());

    for (int i = 0; i < get(0).size(); i++) {
      for (TestRunner runner : this) {
        TestStep step = runner.get(i);
        step.processTestStep(stepPhase, reports.get(stepPhase), isLoadTest());
      }
    }
  }

  /**
   * Fill out reports.get(stepPhase).
   */
  private void fillOutReport(StepPhase stepPhase) {
    String phaseReportName = (stepPhase.equals(StepPhase.DEFAULT)
        ? "" : stepPhase.getShortName() + "_") + generateTestCaseName();
    AllureTestReport phaseReport = new AllureTestReport(phaseReportName);
    phaseReport.setReporter(this.reporter);
    phaseReport.setFullName(getClass().getName());
    phaseReport.addLabel("package", getClass().getPackage().toString());
    phaseReport.addLabel("testClass", getClass().toString());
    phaseReport.addLabel("testMethod", "execute");
    phaseReport.setTestClientMatrix(tuple.getMatrix());
    try {
      phaseReport.addLabel("host", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      phaseReport.addLabel("host", "N/A");
    }
    phaseReport.addLabel("suite", suite.getName());
    phaseReport.addLabel("parentSuite", this.parentSuite);
    phaseReport.setName(stepPhase.getName() + generateTestCaseName() + " - " + this.currentIteration);
    this.suite.addChild(phaseReport.getUuid());
    this.reports.put(stepPhase, phaseReport);
    logger.info("Finished filling out initial report for phase " + stepPhase.getName());
}

  /**
   * Generate test case name string.
   *
   * @return the string
   */
  protected String generateTestCaseName() {
    StringBuilder name = new StringBuilder();
    for (int index = 0; index < tuple.size(); index++) {
      Client client = tuple.get(index);
      name.append(getClientName(client));
      if (index < tuple.size() - 1) {
        name.append("-");
      }
    }
    return name.toString() + " (" +timestamp() + ")";
  }

  public String getClientName (Client client) {
    StringBuilder name = new StringBuilder();
    name.append(client.getPlatform().name(), 0, 3);
    if (!client.isApp()) {
      name.append("_").append(client.getBrowserName(), 0, 2);
      if (client.getVersion() != null) {
        String version = client.getVersion().split(" ").length > 1 ? client.getVersion().split(" ")[1] : client.getVersion();
        name.append("_").append(version);
      }
    } else {
      name.append("_").append(client.getDeviceName(), 0, 2);
    }
    return name.toString();
  }

  /**
   * Gets config file path.
   *
   * @return the config file path
   */
  public String getConfigFilePath() {
    return this.configFilePath;
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
   * Gets expected test duration.
   *
   * @return the expected test duration (in minutes)
   */
  public int getExpectedTestDuration() {
    return expectedTestDuration;
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
   * Retrieves the navigator.userAgent from all of the config objects and passes it to the the respective
   * Config object for processing.
   */
  protected void getInfoFromNavigator() {
    for (int i = 0; i < tuple.size(); i++) {
      if (!this.tuple.get(i).isApp()) {
        populateInfoFromNavigator(this.tuple.get(i).getWebDriver(), this.tuple.get(i));
      }
    }
  }

  public String getKiteServerGridId() {
    return kiteServerGridId;
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
  public NetworkInstrumentation getNetworkInstrumentation() {
    return this.networkInstrumentation;
  }

  /**
   * Sets instrumentation.
   *
   * @param networkInstrumentation the instrumentation
   */
  public void setNetworkInstrumentation(NetworkInstrumentation networkInstrumentation) {
    this.networkInstrumentation = networkInstrumentation;
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
   * Gets report.
   *
   * @return the report
   */
  public AllureTestReport getReport(StepPhase stepPhase) {
    return reports.get(stepPhase);
  }

  /**
   * Gets room manager.
   *
   * @return the roomManager
   */
  @Transient
  protected RoomManager getRoomManager() {
    return roomManager;
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
   * Gets stats.
   *
   * @return true to call and collect getStats, false otherwise, as set in the config file.
   */
  public boolean getStats() {
    return getStatsConfig != null && getStatsConfig.getBoolean("enabled", false);
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
   * Gets the consoleLogs boolean (true by default)
   *
   * @return consoleLogs
   */
  public boolean getConsoleLogs() {
    return consoleLogs;
  }
  public boolean isLoadTest() {
    return isLoadTest;
  }

  public void setLoadTest(boolean loadTest) {
    isLoadTest = loadTest;
  }

  /**
   * Restructuring the test according to options given in payload object from config file. This
   * function processes the parameters common to all load tests.
   */
  protected void payloadHandling() {
    if (payload != null) {
      logger.info("the payload is " + payload.toString());
      url = payload.getString("url", null);
      port = payload.getInt("port", 30000);
      testTimeout = payload.getInt("testTimeout", testTimeout);
      takeScreenshotForEachTest =
          payload.getBoolean("takeScreenshotForEachTest", false);
      getStatsConfig = payload.getJsonObject("getStats");
      multiThread = payload.getBoolean("multiThread", true);
      expectedTestDuration = payload.getInt("expectedTestDuration", expectedTestDuration);
      meetingDuration = this.payload.getInt("meetingDuration", meetingDuration);
      setExpectedTestDuration(Math.max(getExpectedTestDuration(), (meetingDuration + 300) / 60));
      maxUsersPerRoom = payload.getInt("usersPerRoom", maxUsersPerRoom);
      csvReport = payload.getBoolean("csvReport", csvReport);
      consoleLogs = payload.getBoolean("consoleLogs", consoleLogs);
      fastRampUp = payload.getBoolean("fastRampUp", fastRampUp);
      if (payload.containsKey("windowSize") && payload.getString("windowSize").contains("x")) {
        StringTokenizer st = new StringTokenizer(payload.getString("windowSize"), "x");
        windowWidth = Integer.parseInt(st.nextToken());
        windowHeight = Integer.parseInt(st.nextToken());
      }
      if (this.payload.containsKey("scenarios")) {
        JsonArray jsonArray2 = this.payload.getJsonArray("scenarios");
        for (int i = 0; i < jsonArray2.size(); ++ i) {
          try {
            this.scenarioArrayList.add(new Scenario(jsonArray2.getJsonObject(i), logger, networkInstrumentation));
          } catch (Exception e) {
            logger.error("Invalid scenario number : " + i + "\r\n" + ReportUtils.getStackTrace(e));
          }
        }
      }
    }
  }

  /**
   * Abstract method to be overridden by the client to add steps to the TestRunner.
   *
   * @param runner the TestRunner
   */
  protected abstract void populateTestSteps(TestRunner runner);

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

  public void setKiteServerGridId(String kiteServerGridId) {
    this.kiteServerGridId = kiteServerGridId;
  }

  /**
   * Sets logger.
   *
   * @param logger the logger
   */
  public void setLogger(KiteLogger logger) {
    this.logger = logger;
  }

  public void setTestConfig(TestConfig testConfig) {
    this.testConfig = testConfig;
  }


  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets parent suite.
   *
   * @param parentSuite the parent test suite
   */
  public void setParentSuite(String parentSuite) {
    this.parentSuite = parentSuite;
  }

  /**
   * Sets payload.
   *
   * @param payload the payload
   */
  public void setPayload(JsonValue payload) {
    this.payload = (JsonObject) payload;
  }

  /**
   * Sets the List of StepPhases.
   *
   * @param phases
   */
  public void setPhases(List<StepPhase> phases) {
    this.phases = phases;
  }


  private void setStepPhaseToRunner(StepPhase stepPhase) {
    for (TestRunner runner : this) {
      runner.setStepPhase(stepPhase);
    }
  }

  /**
   * Sets suite.
   *
   * @param suite the suite
   */
  public void setSuite(Container suite) {
    this.suite = suite;
    for (StepPhase phase : phases) {
//      this.suite.addChild(this.reports.get(phase).getUuid());
//      this.reports.get(phase).addLabel("suite", suite.getName());
    }
  }

  /**
   * Sets suite.
   *
   * @param suite the suite
   */
  public void setSuite(Container suite, StepPhase stepPhase) {
    for (StepPhase phase : phases) {
      if (!phase.equals(stepPhase)) {
        this.suite.removeChild(this.reports.get(stepPhase).getUuid());
      }
    }
    this.suite = suite;
    if (!this.isLoadTest) {
      this.suite.addChild(this.reports.get(stepPhase).getUuid());
      this.reports.get(stepPhase).addLabel("suite", suite.getName());
    }

  }

  /**
   * Sets client list.
   *
   * @param clientList the client list
   */
  public void setTuple(List<Client> clientList) {
    Tuple tuple = new Tuple(clientList);
    setTuple(tuple);
  }

  /**
   * Sets client list.
   *
   * @param tuple the client list
   */
  public void setTuple(Tuple tuple) {
    this.tuple = tuple;
    this.tupleSize = this.tuple.size();
    for (StepPhase stepPhase : phases) {
//      this.reports.get(stepPhase).setName(stepPhase.getName() + generateTestCaseName());
    }
  }

  /**
   * Gets selected stats.
   * /**
   * Check if the steps are completed for all runners.
   *
   * @param stepName class name of the step
   *
   * @return true if the step has been completed on all runners
   * @throws KiteTestException the kite test exception
   */
  @Override
  public boolean stepCompleted(String stepName) throws KiteTestException {
    for (TestRunner runner : this) {
      if (!runner.completed(stepName)) {
        return false;
      }
    }
    return true;
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
   * Sets reporter.
   *
   * @param reporter the reporter
   */
  public void setReporter(Reporter reporter) {
    this.reporter = reporter;
  }

  /**
   * Sets room manager.
   *
   * @param roomManager the room manager
   */
  public void setRoomManager(RoomManager roomManager) {
    this.roomManager = roomManager;
  }


  /**
   * Gets the jar file
   *
   * @return the jar file
   */
  public String getTestJar() {
    return testJar;
  }

  /**
   * Sets the jar file
   *
   * @param testJar the jar file
   */
  public void setTestJar(String testJar) {
    this.testJar = testJar;
  }

  protected void addExtraCategories() {
  }

  /**
   * Gets the interval depending of the id and increment
   * @param id
   * @return the interval as a function of the id and increment.
   *
   */
  public int getInterval(int id) {
    //min 200 ms
    int interval = testConfig.getInterval() < 200 ? 200 : testConfig.getInterval();
    return id % testConfig.getIncrement() * interval;
  }

  public void setDelayForClosing(long delayForClosing) {
    this.delayForClosing = delayForClosing;
  }

  private void keepBrowsersAlive() {
    if (!this.hasWebdriverIssue() && this.delayForClosing > 0) {
      logger.info("Keeping the browsers alive for " + this.delayForClosing + " seconds..");
      for (int wait = 0; wait < delayForClosing; wait ++) {
        waitAround(ONE_SECOND_INTERVAL);
        //todo: review this logic. this makes the wait 6 times longer than expected. It could overload the hub
        // and the emtpy catch block is dangerous
//        for (WebDriver webDriver : this.tuple.getWebDrivers()) {
//          try {
//            webDriver.getWindowHandle(); // interaction with webdriver to avoid timeout
//          } catch (Exception e) {
//            //ignore
//          }
//        }
      }
    }
  }

  public boolean hasWebdriverIssue () {
    if (this.isLoadTest) {
      for (TestRunner runner : this) {
        if (runner.getWebDriver() != null) {
          return false;
        }
      }
      return true;
    } else {
      for (TestRunner runner : this) {
        if (runner.getWebDriver() == null) {
          return true;
        }
      }
      return false;
    }
  }

  public void setCurrentIteration(int currentIteration) {
    this.currentIteration = currentIteration;
  }
}
