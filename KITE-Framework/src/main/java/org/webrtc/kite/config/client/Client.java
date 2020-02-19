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

package org.webrtc.kite.config.client;

import static org.webrtc.kite.Utils.getStackTrace;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import io.cosmosoftware.kite.interfaces.SampleData;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.ReportUtils;
import io.cosmosoftware.kite.util.TestUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.webrtc.kite.WebDriverFactory;
import org.webrtc.kite.config.media.MediaFile;
import org.webrtc.kite.config.media.MediaFileType;
import org.webrtc.kite.config.paas.Paas;
import org.webrtc.kite.exception.KiteGridException;

/**
 * The type End point.
 */
@Entity(name = Client.TABLE_NAME)
public class Client extends KiteEntity implements JsonBuilder, SampleData {

  final static String TABLE_NAME = "clients";
  protected final KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());
  protected Integer count;
  protected Integer clientIndex = -1; // in the config file
  protected String id;
  protected BrowserSpecs specs;
  private String name = "";
  protected Paas paas;
  private Boolean exclude = false;
  private Map<String, String> extraCapabilities = new HashMap<>();
  private String gateway;
  private JsonObject jsonConfig;
  private Integer maxInstances;
  protected String kind;
  private App app;
  private final String DEFAULT_WINDOW_SIZE = "1920,1200";
  private List<String> flags = new ArrayList<>();
  private Boolean headless = false;
  private Boolean technologyPreview = false;
  private Boolean useFakeMedia = true;
  private String windowSize = DEFAULT_WINDOW_SIZE;
  private MediaFile audio;
  private MediaFile video;
  private WebDriver webDriver;


  /**
   * Instantiates a new End point.
   */
  public Client() {
  }

  /**
   * Instantiates a new End point.
   *
   * @param client the client
   */
  public Client(Client client) {
    this.exclude = client.isExclude();
    this.jsonConfig = client.getJsonConfig();
    this.gateway = client.getGateway();
    this.count = client.getCount();
    this.paas = client.getPaas();
    this.specs = client.getSpecs();
    this.clientIndex = client.getClientIndex();
    for (String capabilityName : client.getExtraCapabilities().keySet()) {
      this.addCapabilities(capabilityName, client.getExtraCapabilities().get(capabilityName));
    }
    this.app = client.getApp();
    this.name = client.getName();
    this.kind = this.app != null ? "app" : "browser";

    this.specs = client.getSpecs();
    this.headless = client.isHeadless();
    this.useFakeMedia = client.useFakeMedia();
    this.technologyPreview = client.isTechnologyPreview();
    this.windowSize = this.getWindowSize();
    this.flags = client.getFlags();
    this.video = client.getVideo();
    this.audio = client.getAudio();
    
  }

  /**
   * Constructs a new KiteConfigObject with the given remote address and JsonObject.
   *
   * @param jsonObject JsonObject
   */
  public Client(JsonObject jsonObject) {

    this.jsonConfig = jsonObject;
    this.exclude = jsonObject.getBoolean("exclude", false);
    this.specs = new BrowserSpecs(jsonObject);
    this.name = jsonObject.getString("name", null);
    this.count = jsonObject.getInt("count", 1);
    JsonValue jsonValue = jsonObject.getOrDefault("extraCapabilities", null);
    this.gateway = jsonObject.getString("gateway", null);
    if (jsonValue != null) {
      JsonObject extraCapabilitiesArray = (JsonObject) jsonValue;
      for (String capabilityName : extraCapabilitiesArray.keySet()) {
        this.addCapabilities(capabilityName, extraCapabilitiesArray.getString(capabilityName));
      }
    }
    if (jsonObject.containsKey("app")) {
      this.app = new App(jsonObject.getJsonObject("app"));
    }
    this.kind = this.app != null ? "app" : "browser";

    this.headless = jsonObject.getBoolean("headless", headless);
    this.useFakeMedia = jsonObject.getBoolean("useFakeMedia", useFakeMedia);
    this.technologyPreview = jsonObject.getBoolean("technologyPreview", technologyPreview);
    this.windowSize = jsonObject.getString("windowSize", windowSize);
    
    jsonValue = jsonObject.getOrDefault("flags", null);
    JsonObject jObject = jsonObject.getJsonObject("video");
    if (jObject != null) {
      this.video = new MediaFile(jObject);
    }
    jObject = jsonObject.getJsonObject("audio");
    if (jObject != null) {
      this.audio = new MediaFile(jObject);
    }
    if (jsonValue != null) {
      JsonArray flagArray = (JsonArray) jsonValue;
      for (int i = 0; i < flagArray.size(); i++) {
        this.flags.add(flagArray.getString(i));
      }
    }
    
    
  }


  /**
   * Adds one flag to the flag list.
   *
   * @param flag to add.
   */
  public void addFlag(String flag) {
    this.flags.add(flag);
  }

  /**
   * add new capability name/value pair to browser
   *
   * @param capabilityName capability name
   * @param capabilityValue capability value
   */
  public void addCapabilities(String capabilityName, String capabilityValue) {
    this.extraCapabilities.put(capabilityName, capabilityValue);
  }


  /**
   * Gets count.
   *
   * @return the count
   */
  public Integer getCount() {
    return count;
  }

  /**
   * Sets count.
   *
   * @param count the count
   */
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * Gets extra capabilities.
   *
   * @return extra capability map to be used to create webdriver
   */
  @Transient
  public Map<String, String> getExtraCapabilities() {
    return extraCapabilities;
  }

  /**
   * Gets the webDriver
   *
   * @return the webDriver
   */
  @Transient
  public WebDriver getWebDriver() {
    return webDriver;
  }
  /**
   * Sets extra capabilities.
   *
   * @param extraCapabilities the extra capabilities
   */
  public void setExtraCapabilities(Map<String, String> extraCapabilities) {
    this.extraCapabilities = extraCapabilities;
  }

  /**
   * Gets gateway.
   *
   * @return the gateway
   */
  public String getGateway() {
    return gateway;
  }

  /**
   * Sets gateway.
   *
   * @param gateway the gateway
   */
  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue(generator = Client.TABLE_NAME)
  @GenericGenerator(name = Client.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
      @org.hibernate.annotations.Parameter(name = "prefix", value = "CLNT")
  })
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
   * Gets json config.
   *
   * @return the json config
   */
  @Transient
  public JsonObject getJsonConfig() {
    return jsonConfig;
  }

  /**
   * Sets json config.
   *
   * @param jsonConfig the json config
   */
  @Transient
  public void setJsonConfig(JsonObject jsonConfig) {
    this.jsonConfig = jsonConfig;
  }

  /**
   * Gets logger.
   *
   * @return the logger
   */
  @Transient
  public KiteLogger getLogger() {
    return logger;
  }

  /**
   * Gets max instances.
   *
   * @return the max instances
   */
  @Transient
  public int getMaxInstances() {
    return maxInstances;
  }

  /**
   * Sets max instances.
   *
   * @param maxInstances the max instances
   */
  public void setMaxInstances(int maxInstances) {
    this.maxInstances = maxInstances;
  }

  /**
   * Gets paas.
   *
   * @return the paas
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public Paas getPaas() {
    return this.paas;
  }

  /**
   * Sets paas.
   *
   * @param paas the paas
   */
  public void setPaas(Paas paas) {
    this.paas = paas;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Client other = (Client) obj;
    if (specs == null) {
      return other.specs == null;
    } else {
      return specs.equals(other.specs);
    }
  }

  /**
   * Gets the kind
   *
   * @return kind the String "app" or "browser" (if null, this is a browser)
   */
  public String getKind() {
    return kind;
  }

  /**
   * Sets the kind
   *
   * @param kind the String "app" or "browser"
   */
  public void setKind(String kind) {
    this.kind = kind;
  }


  /**
   * Is app boolean.
   *
   * @return the boolean
   */
  @Transient
  public boolean isApp() {
    return this.app != null;
  }

  /**
   * Is exclude boolean.
   *
   * @return the boolean
   */
  public Boolean isExclude() {
    return exclude;
  }

  /**
   * Sets exclude.
   *
   * @param exclude the exclude
   */
  public void setExclude(Boolean exclude) {
    this.exclude = exclude;
  }

  /**
   * Gets specs.
   *
   * @return the specs
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public BrowserSpecs getSpecs() {
    return this.specs;
  }

  /**
   * Sets specs.
   *
   * @param specs the specs
   */
  public void setSpecs(BrowserSpecs specs) {
    this.specs = specs;
  }

  /**
   * Get platform from specs
   *
   * @return the spec's platform
   */
  @Transient
  public Platform getPlatform() {
    return this.specs.getPlatform();
  }

  /**
   * Gets device name.
   *
   * @return the device name
   */
  @Transient
  public String getDeviceName() {
    return this.specs.getDeviceName();
  }

  /**
   * Gets platform version.
   *
   * @return the platform version
   */
  @Transient
  public String getPlatformVersion() {
    return this.specs.getPlatformVersion();
  }

  @Override
  public String toString() { 
    try {
      return buildJsonObjectBuilder().build().toString();
    } catch (NullPointerException e) {
      return getStackTrace(e);
    }
  }

  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() throws NullPointerException {
    JsonObjectBuilder builder = Json.createObjectBuilder()
      .add("platform", getPlatform().name());
    if (this.name != null) {
      builder.add("name", this.name);
    }
    if (this.gateway != null) {
      builder.add("gateway", this.gateway);
    }
    if (this.count != null) {
      builder.add("count", this.count);
    }
    if (this.exclude != null) {
      builder.add("exclude", this.exclude);
    }
    if (this.paas != null) {
      builder.add("paas", this.paas.getType().name());
      builder.add("remoteUrl", this.paas.getUrl());
    }
    if (this.useFakeMedia != null) {
      builder.add("useFakeMedia", this.useFakeMedia);
    }
    if (this.windowSize != null) {
      builder.add("windowSize", this.windowSize);
    }
    if (this.flags != null) {
      JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
      for (int i = 0; i < this.flags.size(); i++) {
        jsonArrayBuilder.add(this.flags.get(i));
      }
      builder.add("flags", jsonArrayBuilder.build());
    }
    if (this.video != null) {
      builder.add("video", this.video.buildJsonObjectBuilder());
    }
    if (this.audio != null) {
      builder.add("audio", this.audio.buildJsonObjectBuilder());
    }
    if (this.extraCapabilities != null && !this.extraCapabilities.isEmpty()) {
      for (Map.Entry<String, String> entry : this.extraCapabilities.entrySet()) {
        builder.add(entry.getKey(), entry.getValue());
      }
    }
    if (this.paas != null && this.paas.getGridId() != null) {
      builder.add("gridId", this.paas.getGridId());
    }
    if (isApp()) {
      builder.add("app", app.buildJsonObjectBuilder());
    } else {
      builder.add("browserName", this.getBrowserName())
        .add("headless", this.headless == null ? false : this.headless)
        .add("technologyPreview", this.technologyPreview == null ? false : technologyPreview);
      if (this.getVersion() != null) {
        builder.add("version", this.getVersion());
      }
    }
    return builder;
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
   * returns the App
   *
   * @return app app
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public App getApp() {
    return app;
  }

  /**
   * Sets app.
   *
   * @param app the app
   */
  public void setApp(App app) {
    this.app = app;
  }

  /**
   * returns app's starting Activity
   *
   * @return String app activity
   */
  @Transient
  public String getAppActivity() {
    return this.app != null ? this.app.getAppActivity() : null;
  }

  /**
   * returns app's filename
   *
   * @return String app filename
   */
  @Transient
  public String getAppName() {
    return this.app != null ? this.app.getAppFileOrName() : null;
  }

  /**
   * returns app's package
   *
   * @return String app package
   */
  @Transient
  public String getAppPackage() {
    return this.app != null ? this.app.getAppPackage() : null;    
  }

  /**
   * Is full reset boolean.
   *
   * @return whether the Appium fullReset option should be set to true
   */
  @Transient
  public Boolean isFullReset() {
    return this.app != null ? this.app.isFullReset() : false;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = super.hashCode();
    int result = 1;
    result = prime * result + ((specs == null) ? 0 : specs.hashCode());
    return result;
  }

  /**
   * Gets browser name.
   *
   * @return the browser name
   */
  @Transient
  public String getBrowserName() {
    return this.specs.getBrowserName();
  }

  /**
   * Gets profile.
   *
   * @return the profile
   */
  @Transient
  public String getProfile() {
    return this.specs.getProfile();
  }

  /**
   * Sets profile.
   */
  @Transient
  public void setProfile(String profile) {
    this.specs.setProfile(profile);
  }

  /**
   * Gets extension.
   *
   * @return the extension
   */
  @Transient
  public String getExtension() {
    return this.specs.getExtension();
  }

  /**
   * Sets extension.
   */
  @Transient
  public void setExtension(String extension) {
    this.specs.setExtension(extension);
  }

  /**
   * Sets browser name.
   *
   * @param browserName the browser name
   */
  public void setBrowserName(String browserName) {
    this.specs.setBrowserName(browserName);
  }

  /**
   * Gets flags.
   *
   * @return the flags
   */
  @Column
  @ElementCollection(fetch = FetchType.EAGER)
  public List<String> getFlags() {
    return this.flags;
  }

  /**
   * Sets flags.
   *
   * @param flags the flags
   */
  public void setFlags(List<String> flags) {
    this.flags = flags;
  }

  /**
   * Gets use fake media.
   *
   * @return the use fake media
   */
  public Boolean getUseFakeMedia() {
    return useFakeMedia;
  }

  /**
   * Sets use fake media.
   *
   * @param useFakeMedia the use fake media
   */
  public void setUseFakeMedia(Boolean useFakeMedia) {
    this.useFakeMedia = useFakeMedia;
  }

  /**
   * Gets version.
   *
   * @return the version
   */
  @Transient
  public String getVersion() {
    return this.specs.getVersion();
  }

  /**
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.specs.setVersion(version);
  }

  /**
   * Gets the window size
   *
   * @return the window size
   */
  public String getWindowSize() {
    return windowSize;
  }

  /**
   * Sets window size.
   *
   * @param windowSize the window size
   */
  public void setWindowSize(String windowSize) {
    this.windowSize = windowSize;
  }

  /**
   * Is headless boolean.
   *
   * @return the boolean
   */
  public Boolean isHeadless() {
    return headless;
  }

  /**
   * Sets headless.
   *
   * @param headless the headless
   */
  public void setHeadless(Boolean headless) {
    this.headless = headless;
  }

  /**
   * Is technologyPreview boolean.
   *
   * @return the boolean
   */
  public Boolean isTechnologyPreview() {
    return technologyPreview;
  }

  /**
   * Sets technology preview.
   *
   * @param technologyPreview the technology preview
   */
  public void setTechnologyPreview(Boolean technologyPreview) {
    this.technologyPreview = technologyPreview;
  }

  /**
   * Retrieve family or platform platform.
   *
   * @return the platform
   */
  public Platform retrieveFamilyOrPlatform() {
    Platform platform = this.getPlatform();
    return platform.family() == null ? platform : platform.family();
  }

  /**
   * Sets platform.
   *
   * @param platform the platform
   */
  public void setPlatform(Platform platform) {
    this.specs.setPlatform(platform);
  }

  /**
   * Checks whether it is required to get navigator.userAgent from the browser.
   *
   * @return true if either of the user agent version and user agent platform is null.
   */
  public Boolean shouldGetUserAgent() {
    return false;
  }

  /**
   * Use fake media boolean.
   *
   * @return true if to use the fake media from the browser
   */
  public Boolean useFakeMedia() {
    return useFakeMedia;
  }

  /**
   * Gets video.
   *
   * @return the video
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public MediaFile getVideo() {
    return video;
  }

  /**
   * Sets video.
   *
   * @param video the video
   */
  public void setVideo(MediaFile video) {
    this.video = video;
  }

  /**
   * Gets audio.
   *
   * @return the audio
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public MediaFile getAudio() {
    return audio;
  }

  /**
   * Sets audio.
   *
   * @param audio the audio
   */
  public void setAudio(MediaFile audio) {
    this.audio = audio;
  }


  /**
   * Fetch media path string.
   *
   * @param media the media
   * @param browserName the browser name
   * @return the string
   */
  public String fetchMediaPath(MediaFile media, String browserName) {
    String extension;
    if (media.getType().equals(MediaFileType.Video)) {
      if (browserName.equalsIgnoreCase("chrome")) {
        extension = ".y4m";
      } else {
        extension = ".mp4";
      }
    } else {
      extension = ".wav";
    }
    return media.getFilepath() + extension;
  }
  
  @Override
  public SampleData makeSampleData() {
    // todo
    return null;
  }


  /**
   * Constructs a the webdriver.
   *
   * @throws KiteGridException the kite grid exception
   */
  public WebDriver createWebDriver(Map<WebDriver, Map<String, Object>> sessionData) throws KiteGridException {
    if (this.webDriver != null) {
      logger.warn("createWebDriver() webdriver already exists, skipping.");
      return this.webDriver;
    }
    try {
      this.webDriver = WebDriverFactory.createWebDriver(this, null, null, this.getPaas().getGridId());
      addToSessionMap(sessionData);
      return this.webDriver;
    } catch (Exception e) {
      logger.error(ReportUtils.getStackTrace(e));
      throw new KiteGridException(
        e.getClass().getSimpleName()
          + " creating webdriver for \n"
          + this.toString()
          + ":\n"
          + e.getLocalizedMessage());
    }
  }

  public void setClientIndex(int clientIndex) {
    this.clientIndex = clientIndex;
  }

  @Transient
  public int getClientIndex() {
    return clientIndex;
  }


  /**
   * Adds the webdriver to the sessionData map.
   * 
   * @param sessionData
   * @throws KiteGridException
   */
  private void addToSessionMap(Map<WebDriver, Map<String, Object>> sessionData) throws KiteGridException {
    Map<String, Object> map = new HashMap<>();
    map.put("end_point", this);
//    if (!this.isApp()) {
      String node = TestUtils.getNode(
        this.getPaas().getUrl(),
        ((RemoteWebDriver) this.getWebDriver()).getSessionId().toString());
      if (node != null) {
        map.put("node_host", node);
      }
//    }
    sessionData.put(this.getWebDriver(), map);
  }
  
  
}
