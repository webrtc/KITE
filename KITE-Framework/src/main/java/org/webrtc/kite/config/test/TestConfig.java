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

import io.cosmosoftware.kite.interfaces.JsonBuilder;
import io.cosmosoftware.kite.interfaces.SampleData;
import io.cosmosoftware.kite.report.KiteLogger;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.webrtc.kite.config.KiteEntity;
import org.webrtc.kite.exception.KiteInsufficientValueException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static org.webrtc.kite.Utils.getIntFromJsonObject;

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
  private boolean closeWebDrivers = true;
  private long delayForClosing;
  private String description;
  private String id;
  private String implJar;
  private int increment;
  private int interval;
  private KiteLogger logger;
  private List<List<Integer>> matrix = new ArrayList<>();
  private boolean matrixOnly = false;
  private int maxRetryCount;
  private String name;
  private int noOfThreads;
  private String payload;
  private boolean permute;
  private boolean regression;
  private String testImpl;
  private JsonObject testJsonConfig;
  private int tupleSize;
  private TestType type;
  
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
    this.payload = jsonObject.getJsonObject("payload").toString();
    // Mandatory
    this.type = TestType.valueOf(jsonObject.getString("type", "interop"));
    this.name = jsonObject.getString("name").contains("%ts")
      ? jsonObject.getString("name").replaceAll("%ts", "") + " (" + timestamp() + ")"
      : jsonObject.getString("name");
    this.testImpl = jsonObject.getString("testImpl")
      + (System.getProperty("testName") == null ? "" : System.getProperty("testName"));
    
    this.implJar = jsonObject.getString("implJar", null);
    this.description = jsonObject.getString("description", DEFAULT_DESC);
    
    // Override the global value with the local value
    this.callbackUrl = jsonObject.getString("callbackurl", null);
    
    this.tupleSize = getIntFromJsonObject(jsonObject, "tupleSize", -1);
    this.noOfThreads = getIntFromJsonObject(jsonObject, "noOfThreads", 1);
    this.maxRetryCount = getIntFromJsonObject(jsonObject, "maxRetryCount", 0);
    this.increment = getIntFromJsonObject(jsonObject, "increment", 1);
    this.interval = getIntFromJsonObject(jsonObject, "interval", 5);
    
    this.regression = jsonObject.getBoolean("regression", false);
    this.permute = jsonObject.getBoolean("permute", true);
    this.closeWebDrivers = jsonObject.getBoolean("closeWebDrivers", true);
    this.matrixOnly = jsonObject.getBoolean("matrixOnly", false);
    
    
    JsonArray jsonArray = jsonObject.getJsonArray("matrix");
    if (jsonArray != null) {
      for (int i = 0; i < jsonArray.size(); i++) {
        JsonArray jArray = jsonArray.getJsonArray(i);
        List<Integer> tuple = new ArrayList<>();
        for (int j = 0; j < jArray.size(); j++) {
          tuple.add(jArray.getInt(j));
        }
        this.matrix.add(tuple);
      }
    }
    this.logger = createTestLogger();
  }
  
  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.JsonBuilder#buildJsonObjectBuilder()
   */
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("type", this.type.name())
      .add("name", this.name)
      .add("testImpl", this.testImpl)
      .add("description", this.description)
      .add("tupleSize", this.tupleSize)
      .add("noOfThreads", this.noOfThreads)
      .add("maxRetryCount", this.maxRetryCount)
      .add("delayForClosing", this.delayForClosing)
      .add("closeWebDrivers", this.closeWebDrivers)
      .add("permute", this.permute)
      .add("matrixOnly", this.matrixOnly);
  }
  
  /**
   * Create a common test logger for all test cases of a given test
   *
   * @return the logger for tests
   * @throws IOException if the FileAppender fails
   */
  private KiteLogger createTestLogger() throws IOException {
    KiteLogger testLogger = KiteLogger.getLogger(new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()));
    FileAppender fileAppender = new FileAppender(new PatternLayout("%d %-5p - %m%n"), "logs/" + getTestClassName() + "/test_" + testLogger.getName() + ".log", false);
    fileAppender.setThreshold(Level.INFO);
    testLogger.addAppender(fileAppender);
    return testLogger;
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
   * Gets web drivers.
   *
   * @return the web drivers
   */
  public boolean getCloseWebDrivers() {
    return closeWebDrivers;
  }
  
  /**
   * Sets web drivers.
   *
   * @param closeWebDrivers the close web drivers
   */
  public void setcloseWebDrivers(boolean closeWebDrivers) {
    this.closeWebDrivers = closeWebDrivers;
  }
  
  /**
   * Gets delay for closing.
   *
   * @return the delay for closing
   */
  public long getDelayForClosing() {
    return delayForClosing;
  }
  
  /**
   * Sets delay for closing.
   *
   * @param delayForClosing the delay for closing
   */
  public void setDelayForClosing(long delayForClosing) {
    this.delayForClosing = delayForClosing;
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
  public int getIncrement() {
    return increment;
  }
  
  /**
   * Sets increment.
   *
   * @param increment the increment
   */
  public void setIncrement(int increment) {
    this.increment = increment;
  }
  
  /**
   * Gets interval.
   *
   * @return the interval
   */
  public int getInterval() {
    return interval;
  }
  
  /**
   * Sets interval.
   *
   * @param interval the interval
   */
  public void setInterval(int interval) {
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
    if (logger == null) {
      logger = createTestLogger();
    }
    return logger;
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
   * Gets matrix.
   *
   * @return the matrix
   */
  @Transient
  public List<List<Integer>> getMatrix() {
    return matrix;
  }
  
  /**
   * Sets matrix.
   *
   * @param matrix the matrix
   */
  public void setMatrix(List<List<Integer>> matrix) {
    this.matrix = matrix;
  }

//  public boolean iscloseWebDrivers() {
//    return closeWebDrivers;
//  }
  
  /**
   * Gets max retry count.
   *
   * @return the max retry count
   */
  public int getMaxRetryCount() {
    return maxRetryCount;
  }
  
  /**
   * Sets max retry count.
   *
   * @param maxRetryCount the max retry count
   */
  public void setMaxRetryCount(int maxRetryCount) {
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
  
  /**
   * Gets no of threads.
   *
   * @return the no of threads
   */
  public int getNoOfThreads() {
    return noOfThreads;
  }
  
  /**
   * Sets no of threads.
   *
   * @param noOfThreads the no of threads
   */
  public void setNoOfThreads(int noOfThreads) {
    this.noOfThreads = noOfThreads;
  }
  
  /**
   * Gets payload.
   *
   * @return the payload
   */
  public String getPayload() {
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
  public int getTupleSize() {
    return tupleSize;
  }
  
  /**
   * Sets tuple size.
   *
   * @param tupleSize the tuple size
   */
  public void setTupleSize(int tupleSize) {
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
  public boolean isJavascript() {
    return testImpl.endsWith("js");
  }
  
  /**
   * Is load test boolean.
   *
   * @return the boolean
   */
  @Transient
  public boolean isLoadTest() {
    return this.type.equals(TestType.load);
  }
  
  /**
   * Is matrix only boolean.
   *
   * @return the boolean
   */
  public boolean isMatrixOnly() {
    return matrixOnly;
  }
  
  /**
   * Sets matrix only.
   *
   * @param matrixOnly the matrix only
   */
  public void setMatrixOnly(boolean matrixOnly) {
    this.matrixOnly = matrixOnly;
  }
  
  /**
   * Is permute boolean.
   *
   * @return the boolean
   */
  public boolean isPermute() {
    return permute;
  }
  
  /**
   * Sets permute.
   *
   * @param permute the permute
   */
  public void setPermute(boolean permute) {
    this.permute = permute;
  }
  
  /**
   * Is regression boolean.
   *
   * @return true if this is a regression test
   */
  public boolean isRegression() {
    return regression;
  }
  
  /**
   * Sets regression.
   *
   * @param regression the regression
   */
  public void setRegression(boolean regression) {
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
        this.delayForClosing = 0;
        break;
      case load:
        this.name = "MyLoadTest";
        this.testImpl = "io.cosmosoftware.kite.SampleLoadTest";
        this.implJar = "http://localhost:8080/KITEServer/getfile?name=jar";
        break;
    }
    
    try {
      this.logger = createTestLogger();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }
  
  @Override
  public String toString() {
    return buildJsonObjectBuilder().build().toString();
  }
  
}
