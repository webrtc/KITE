/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.config.test;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.readJsonString;
import static org.webrtc.kite.Utils.getIntFromJsonObject;
import static org.webrtc.kite.Utils.getStackTrace;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.instrumentation.NetworkInstrumentation;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import io.cosmosoftware.kite.interfaces.SampleData;
import io.cosmosoftware.kite.manager.RoomManager;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.usrmgmt.EmailSender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.webrtc.kite.exception.KiteInsufficientValueException;

/**
 * The type TestConfig.
 */
@Entity(name = TestConfig.TABLE_NAME)
public class TestConfig extends KiteEntity implements JsonBuilder, SampleData {
  
  /**
   * The constant TABLE_NAME.
   */
  final static String TABLE_NAME = "testconfigs";
  private final String DEFAULT_DESC = "No description was provided fot this test.";
  private String callbackUrl;
  private String tagName;
  private String firefoxProfile;
  private String chromeExtension;
  private Long delayForClosing = 0L;
  private String description = "";
  private String id = "";
  private String implJar = "";
  private Integer increment = 1;
  private Integer interval = 1;
  private KiteLogger logger;
  private Integer maxRetryCount = 1;
  private String name = "";
  private String kiteRequestId = "";
  private Integer noOfThreads = 1;
  private String payload = "";
  private Boolean permute = false;
  private Boolean regression = false;
  private String testImpl = "";
  private String pathToConfigFile = "";
  private JsonObject testJsonConfig;
  private Integer tupleSize = 2;
  private TestType type;
  private Reporter reporter;
  private RoomManager roomManager;
  private NetworkInstrumentation networkInstrumentation = null;
  private Boolean done = false;
  private EmailSender emailSender = null;
  private Boolean csvReport;
  
  /**
   * Instantiates a new test config.
   */
  public TestConfig() {
    super();
  }
  
  /**
   * Constructs a new TestConfig with the given callback url and JsonObject.
   * <p>
   * Representation of a test object in the config file.
   * <p>
   * {
   * "name": "IceConnectionTest",
   * "description": "Some Description",
   * "tupleSize": 2,
   * "testImpl": "org.webrtc.kite.IceConnectionTest",
   * "payload": {},
   * "noOfThreads": 10,
   * "maxRetryCount": 2,
   * }
   *
   * @param jsonObject JsonObject
   *
   * @throws KiteInsufficientValueException the kite insufficient value exception
   * @throws IOException                    the io exception
   */
  public TestConfig(JsonObject jsonObject) throws KiteInsufficientValueException, IOException {
    this.testJsonConfig = jsonObject;

  
    // Mandatory
    this.type = TestType.valueOf(jsonObject.getString("type", "interop"));
    this.name = jsonObject.getString("name");
    this.testImpl = jsonObject.getString("testImpl")
      + (System.getProperty("testName") == null ? "" : System.getProperty("testName"));
    
    this.implJar = jsonObject.getString("implJar", null);
    this.description = jsonObject.getString("description", DEFAULT_DESC);
  
    this.reporter = new Reporter(this.name);
    this.csvReport = jsonObject.getBoolean("csvReport", false);
    this.payload = jsonObject.getJsonObject("payload").toString();

    // Override the global value with the local value
    this.callbackUrl = jsonObject.getString("callbackurl", null);
    this.tagName = jsonObject.getString("tag", null);
    this.tupleSize = getIntFromJsonObject(jsonObject, "tupleSize", -1);

    this.initRoomManagerFromPayload();
    this.noOfThreads = getIntFromJsonObject(jsonObject, "noOfThreads", 1);
    this.maxRetryCount = getIntFromJsonObject(jsonObject, "maxRetryCount", 0);
    this.increment = getIntFromJsonObject(jsonObject, "increment", 1);
    this.interval = getIntFromJsonObject(jsonObject, "interval", 0);
    this.delayForClosing = (long)getIntFromJsonObject(jsonObject, "delayForClosing", 0);

    this.regression = jsonObject.getBoolean("regression", false);
    this.permute = jsonObject.getBoolean("permute", true);
  }
  
  /**
   * Sets kite request id.
   *
   * @param kiteRequestId the kite request id
   */
  public void setKiteRequestId(String kiteRequestId) {
    this.kiteRequestId = kiteRequestId;
  }
  
  /**
   * Gets reporter.
   *
   * @return the reporter
   */
  @Transient
  public Reporter getReporter() {
    return reporter;
  }
  
  public void setReportPath(String reportPath) {
    if (this.reporter == null) {
      this.reporter = new Reporter(this.name);
    }
    this.reporter.setReportPath(reportPath);
    this.reporter.setConfigFilePath(this.pathToConfigFile);
    this.reporter.setTestConfig(this.testJsonConfig);
    this.reporter.setCsvReport(this.csvReport);
  }

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.JsonBuilder#buildJsonObjectBuilder()
   */
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() throws NullPointerException {
    return Json.createObjectBuilder()
      .add("type", this.type.name())
      .add("name", this.name)
      .add("testImpl", this.testImpl)
      .add("description", this.description)
      .add("tupleSize", this.tupleSize)
      .add("noOfThreads", this.noOfThreads)
      .add("maxRetryCount", this.maxRetryCount)
      .add("delayForClosing", this.delayForClosing)
      .add("permute", this.permute);
  }
  
  /**
   * Gets callback url.
   *
   * @return the callback url
   */
  public String getCallbackUrl() {
    return callbackUrl;
  }
  
  /**
   * Sets callback url.
   *
   * @param callbackUrl the callback url
   */
  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }
  
  
  /**
   * Gets delay for closing.
   *
   * @return the delay for closing
   */
  public Long getDelayForClosing() {
    return delayForClosing != null ? delayForClosing : 0;
  }
  
  /**
   * Sets delay for closing.
   *
   * @param delayForClosing the delay for closing
   */
  public void setDelayForClosing(Long delayForClosing) {
    this.delayForClosing = delayForClosing;
  }

  /**
   * Gets the Sets the sendMailSMTP
   * 
   * @return sendMailSMTP
   */
  @Transient
  public EmailSender getEmailSender() {
    return emailSender;
  }

  /**
   * Sets the sendMailSMTP
   * 
   * @param emailSender
   */
  @Transient
  public void setEmailSender(EmailSender emailSender) {
    this.emailSender = emailSender;
  }
  
  
  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Gets id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue(generator = TestConfig.TABLE_NAME)
  @GenericGenerator(name = TestConfig.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
    @Parameter(name = "prefix", value = "TEST")})
  public String getId() {
    return this.id;
  }
  
  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * Gets impl jar.
   *
   * @return the impl jar
   */
  public String getImplJar() {
    return implJar;
  }
  
  /**
   * Sets impl jar.
   *
   * @param implJar the impl jar
   */
  public void setImplJar(String implJar) {
    this.implJar = implJar;
  }
  
  /**
   * Gets increment.
   *
   * @return the increment
   */
  public Integer getIncrement() {
    return increment;
  }
  
  /**
   * Sets increment.
   *
   * @param increment the increment
   */
  public void setIncrement(Integer increment) {
    this.increment = increment;
  }
  
  /**
   * Gets interval.
   *
   * @return the interval
   */
  public Integer getInterval() {
    return interval;
  }
  
  /**
   * Sets interval.
   *
   * @param interval the interval
   */
  public void setInterval(Integer interval) {
    this.interval = interval;
  }
  
  /**
   * Get logger
   *
   * @return the logger that will be pass down to test runners.
   *
   * @throws IOException the io exception
   */
  @Transient
  public KiteLogger getLogger() throws IOException {
    return this.logger;
  }
  
  /**
   * Sets logger.
   *
   * @param logger the logger
   */
  public void setLogger(KiteLogger logger) {
    this.logger = logger;
  }

  /**
   * Gets max retry count.
   *
   * @return the max retry count
   */
  public Integer getMaxRetryCount() {
    return maxRetryCount;
  }
  
  /**
   * Sets max retry count.
   *
   * @param maxRetryCount the max retry count
   */
  public void setMaxRetryCount(Integer maxRetryCount) {
    this.maxRetryCount = maxRetryCount;
  }
  
  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  @Transient
  public String getNameWithTS() {
    return processName(this.name);
  }

  /**
   * Process the %ts in the name
   * 
   * @param s
   * @return
   */
  private String processName(String s) {
    return s.contains("%ts")
      ? s.replaceAll("%ts", "") + " (" + timestamp() + ")"
      : "" + s;
  }
  
  /**
   * Gets no of threads.
   *
   * @return the no of threads
   */
  public Integer getNoOfThreads() {
    return noOfThreads;
  }
  
  /**
   * Sets no of threads.
   *
   * @param noOfThreads the no of threads
   */
  public void setNoOfThreads(Integer noOfThreads) {
    this.noOfThreads = noOfThreads;
  }
  
  /**
   * Gets payload.
   *
   * @return the payload
   */
  public String getPayload() {
    if (this.payload != null && this.payload.isEmpty()) {
      this.payload = "{}";
    }
    return this.payload;
  }
  
  /**
   * Sets payload.
   *
   * @param payload the payload
   */
  public void setPayload(String payload) {
    this.payload = payload;
  }
  
  /**
   * Gets test class name.
   *
   * @return the test class name
   */
  @Transient
  public String getTestClassName() {
    if (isJavascript()) {
      return this.testImpl.substring(0, this.testImpl.indexOf("."));
    } else {
      return this.testImpl.substring(this.testImpl.lastIndexOf(".") + 1);
    }
  }
  
  /**
   * Gets test.
   *
   * @return the test
   */
  public String getTestImpl() {
    return testImpl;
  }
  
  /**
   * Sets test.
   *
   * @param testImpl the test
   */
  public void setTestImpl(String testImpl) {
    this.testImpl = testImpl;
  }
  
  /**
   * Gets the test config in json object format
   *
   * @return the test config in json object format
   */
  @Transient
  public JsonObject getTestJsonConfig() {
    return testJsonConfig;
  }
  
  /**
   * Sets test json config.
   *
   * @param testJsonConfig the test json config
   */
  public void setTestJsonConfig(JsonObject testJsonConfig) {
    this.testJsonConfig = testJsonConfig;
  }
  
  /**
   * Gets tuple size.
   *
   * @return the tuple size
   */
  public Integer getTupleSize() {
    return tupleSize;
  }
  
  /**
   * Sets tuple size.
   *
   * @param tupleSize the tuple size
   */
  public void setTupleSize(Integer tupleSize) {
    this.tupleSize = tupleSize;
  }
  
  /**
   * Gets type.
   *
   * @return the type
   */
  @Enumerated(EnumType.STRING)
  public TestType getType() {
    return this.type;
  }
  
  /**
   * Sets type.
   *
   * @param type the type
   */
  public void setType(TestType type) {
    this.type = type;
  }
  
  /**
   * Is javascript boolean.
   *
   * @return the boolean
   */
  @Transient
  public Boolean isJavascript() {
    return testImpl.endsWith("js");
  }
  
  /**
   * Is load test boolean.
   *
   * @return the boolean
   */
  @Transient
  public Boolean isLoadTest() {
    return this.type.equals(TestType.load);
  }

   /**
   * Is permute boolean.
   *
   * @return the boolean
   */
  public Boolean isPermute() {
    return permute;
  }
  
  /**
   * Sets permute.
   *
   * @param permute the permute
   */
  public void setPermute(Boolean permute) {
    this.permute = permute != null ? permute : false;
  }
  
  /**
   * Is regression Boolean.
   *
   * @return true if this is a regression test
   */
  public Boolean isRegression() {
    return regression;
  }
  
  private void initRoomManagerFromPayload() {
    JsonObject payload = readJsonString(this.payload);
    String url = payload.getString("url", null);
    int maxUsersPerRoom = payload.getInt("usersPerRoom", this.tupleSize);
    if (maxUsersPerRoom > 0) {
      roomManager = new RoomManager(url, maxUsersPerRoom);
      List<String> rooms;
      if (payload.getJsonArray("rooms") != null) {
        JsonArray roomArr = payload.getJsonArray("rooms");
        rooms = new ArrayList<>();
        for (int i = 0; i < roomArr.size(); i++) {
          rooms.add(roomArr.getString(i));
        }
        roomManager.setPreconfiguredRooms(rooms);
      }
    }
  }
  
  public void setNetworkInstrumentation(NetworkInstrumentation networkInstrumentation) {
    this.networkInstrumentation = networkInstrumentation;
  }
  
  @Transient
  public NetworkInstrumentation getNetworkInstrumentation() {
    return networkInstrumentation;
  }
  
  @Transient
  public RoomManager getRoomManager() {
    if (this.roomManager == null) {
      this.initRoomManagerFromPayload();
    }
    return roomManager;
  }
  
  /**
   * Sets regression.
   *
   * @param regression the regression
   */
  public void setRegression(Boolean regression) {
    this.regression = regression;
  }
  
  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.SampleData#makeSampleData()
   */
  @Override
  public SampleData makeSampleData() {
    switch (this.type) {
      case interop:
        this.name = "MyInteropTest";
        this.testImpl = "io.cosmosoftware.kite.SampleInteropTest";
        this.implJar = "http://localhost:8080/KITEServer/getfile?name=jar";
        this.tupleSize = 2;
        this.noOfThreads = 2;
        this.maxRetryCount = 1;
        this.delayForClosing = Long.valueOf(0);
        break;
      case load:
        this.name = "MyLoadTest";
        this.testImpl = "io.cosmosoftware.kite.SampleLoadTest";
        this.implJar = "http://localhost:8080/KITEServer/getfile?name=jar";
        break;
    }
    return this;
  }
  
  @Override
  public String toString() {
    try {
      return buildJsonObjectBuilder().build().toString();
    } catch (NullPointerException e) {
      return getStackTrace(e);
    }
  }

  public void setChromeExtension(String chromeExtension) {
    this.chromeExtension = chromeExtension;
  }

  public void setFirefoxProfile(String firefoxProfile) {
    this.firefoxProfile = firefoxProfile;
  }

  public String getChromeExtension() {
    return chromeExtension;
  }
  
  public String getFirefoxProfile() {
    return firefoxProfile;
  }

  @Transient
  public String getKiteRequestId() {
    return kiteRequestId;
  }

  @Transient
  public String getPathToConfigFile() {
    return pathToConfigFile;
  }

  public void setPathToConfigFile(String pathToConfigFile) {
    this.pathToConfigFile = pathToConfigFile;
  }

  public synchronized void setDone(Boolean done) {
    this.done = done;
  }

  @Transient
  public synchronized Boolean isDone() {
    return done;
  }

  @Transient
  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public Boolean getCsvReport() {
    return csvReport;
  }

  public void setCsvReport(Boolean csvReport) {
    this.csvReport = csvReport;
  }
}
