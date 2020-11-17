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

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.instrumentation.NetworkProfile;
import io.cosmosoftware.kite.interfaces.CommandMaker;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import io.cosmosoftware.kite.interfaces.SampleData;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.ReportUtils;
import io.cosmosoftware.kite.util.TestUtils;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.CascadeType;
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
import org.webrtc.kite.config.paas.Paas;
import org.webrtc.kite.exception.KiteGridException;

/**
 * The type End point.
 */
@Entity(name = Client.TABLE_NAME)
public class Client extends KiteEntity implements CommandMaker, JsonBuilder, SampleData {

  final static String TABLE_NAME = "clients";
  protected final KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());


  protected Integer count;
  protected Integer clientIndex = -1; // in the config file
  protected String id;
  protected Capability capability;
  private BrowserSpecs browserSpecs;
  private WebDriver webDriver;
  private String name = "";
  protected Paas paas;
  private Boolean exclude = false;
  private JsonObject jsonConfig;
  protected String kind;
  private App app;
  private NetworkProfile networkProfile;



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
    this.capability = client.getCapability();
    this.exclude = client.isExclude();
    this.jsonConfig = client.getJsonConfig();
    this.count = client.getCount();
    this.paas = client.getPaas();
    this.browserSpecs = client.getBrowserSpecs();
    this.clientIndex = client.getClientIndex();
    this.capability = client.getCapability();
    this.app = client.getApp();
    this.name = client.getName();
    this.networkProfile = client.getNetworkProfile();
    this.kind = this.app != null ? "app" : "browser";

    
  }

  /**
   * Constructs a new KiteConfigObject with the given remote address and JsonObject.
   *
   * @param jsonObject JsonObject
   */
  public Client(JsonObject jsonObject) {
    this.browserSpecs = new BrowserSpecs(jsonObject);
    this.jsonConfig = jsonObject;
    this.exclude = jsonObject.getBoolean("exclude", false);
    this.capability = new Capability(jsonObject);
    this.name = jsonObject.getString("name", null);
    this.count = jsonObject.getInt("count", 5);
    if (jsonObject.containsKey("app")) {
      this.app = new App(jsonObject.getJsonObject("app"));
    }
    this.kind = this.app != null ? "app" : "browser";
    if (jsonObject.get("networkProfile") != null) {
      this.networkProfile = new NetworkProfile(jsonObject.getJsonObject("networkProfile"));
    } else {
      this.networkProfile = new NetworkProfile();
    }
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
   * Gets the webDriver
   *
   * @return the webDriver
   */
  @Transient
  public WebDriver getWebDriver() {
    return webDriver;
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
    logger.debug("Assigning client to " + paas);
    if (paas != null) {
      paas.minusOneSlot();
      if (this.networkProfile == null && paas.getNetworkProfile() != null) {
        this.networkProfile = paas.getNetworkProfile();
      }
    }
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

    if (this.networkProfile == null)  {
      if (other.networkProfile != null) {
        return false;
      }
    } else {
      if (other.networkProfile != null) {
        if (!this.networkProfile.getName().equals(other.networkProfile.getName())) {
          return false;
        }
      }
    }

    if ((this.browserSpecs == null && other.browserSpecs != null)
      || (this.browserSpecs != null && other.browserSpecs == null)) {
      return false;
    } else {
      if (!this.browserSpecs.equals(other.browserSpecs)) {
        return false;
      }
    }
//
//    if ((this.capability == null && other.capability != null)
//      || (this.capability != null && other.capability == null)) {
//      return false;
//    } else {
//      if (!this.capability.equals(other.capability)) {
//        return false;
//      }
//    }

    return true;

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

//  @Override
//  public String toString() {
//    return super.toString();
//  }

  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() throws NullPointerException {
    if (this.browserSpecs == null) {
      return Json.createObjectBuilder().add("BROWSER_SPEC", "NOT_FOUND");
    }
    JsonObjectBuilder builder = Json.createObjectBuilder()
      .add("platform", this.browserSpecs.getPlatform().name());
    if (this.name != null) {
      builder.add("name", this.name);
    }
    if (this.capability.getGateway() != null) {
      builder.add("gateway", this.capability.getGateway().toString());
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
    if (isApp()) {
      builder.add("app", app.buildJsonObjectBuilder());
    } else {
      builder.add("browserName", this.browserSpecs.getBrowserName())
              .add("headless", this.capability.isHeadless() == null ? false : this.capability.isHeadless())
              .add("technologyPreview", this.capability.isTechnologyPreview() == null ? false : this.capability.isTechnologyPreview());
      if (this.browserSpecs.getVersion() != null) {
        builder.add("version", this.browserSpecs.getVersion());
      }
      if (this.capability.useFakeMedia() != null) {
        builder.add("useFakeMedia", this.capability.useFakeMedia());
      }
      if (this.capability.getWindowSize() != null) {
        builder.add("windowSize", this.capability.getWindowSize());
      }
      if (this.capability.getFlags() != null) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (int i = 0; i < this.capability.getFlags().size(); i++) {
          jsonArrayBuilder.add(this.capability.getFlags().get(i));
        }
        builder.add("flags", jsonArrayBuilder.build());
      }
      if (this.capability.getExtraCapabilities() != null && !this.capability.getExtraCapabilities().isEmpty()) {
        for (Map.Entry<String, String> entry : this.capability.getExtraCapabilities().entrySet()) {
          builder.add(entry.getKey(), entry.getValue());
        }
      }
    }

    if (this.paas != null && this.paas.getGridId() != null) {
      builder.add("gridId", this.paas.getGridId());
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
    result = prime * result + ((this.capability == null) ? 0 : this.capability.hashCode());
    return result;
  }

  /**
   * Retrieve family or platform platform.
   *
   * @return the platform
   */
  public Platform retrieveFamilyOrPlatform() {
    Platform platform = this.browserSpecs.getPlatform();
    return platform.family() == null ? platform : platform.family();
  }

  /**
   * Checks whether it is required to get navigator.userAgent from the browser.
   *
   * @return true if either of the user agent version and user agent platform is null.
   */
  public Boolean shouldGetUserAgent() {
    return false;
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
      if (sessionData!= null) {
        addToSessionMap(sessionData);
      }
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


  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public BrowserSpecs getBrowserSpecs() {
    return this.browserSpecs;
  }

  public void setBrowserSpecs(BrowserSpecs browserSpecs) {
    this.browserSpecs = browserSpecs;
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

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public Capability getCapability() {
    return capability;
  }

  public void setCapability(Capability capability) {
    this.capability = capability;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.CommandMaker#makeCommand(boolean)
   */
  @Override
  public String makeCommand(boolean isHub, boolean isWindows) {
    String command = "";
    if (!isHub) {
      String binary = "";
      if (this.browserSpecs.getPathToBinary() != null && this.browserSpecs.getPathToBinary().length() > 0) {
        if (browserSpecs.getBrowserName().equalsIgnoreCase("chrome")) {
          binary += ",chrome_binary=" + browserSpecs.getPathToBinary();
        }
        if (browserSpecs.getBrowserName().equalsIgnoreCase("firefox")) {
          binary += ",firefox_binary=" + browserSpecs.getPathToBinary();
        }
      }
      command = String
              .format(" -browserSpecs %s,maxInstances=%d%s", this.browserSpecs.makeCommand(isHub, isWindows),
                      this.capability.getMaxInstances(), binary);
      if (this.capability.getGateway() != Gateway.none) {
        command = command + ",gateway=" + this.capability.getGateway().name();
      }
    }
    return command;
  }

  @Transient
  public String getRegion() {
    return this.paas.getRegion();
  }


  @Transient
  public void setNetworkProfile(NetworkProfile networkProfile) {
    this.networkProfile = networkProfile;
  }

  @Transient
  public NetworkProfile getNetworkProfile() {
    if (this.networkProfile == null) {
      NetworkProfile none = new NetworkProfile();
      none.setName("NONE");
      return none;
    }
    return networkProfile;
  }

  public void removeWebdriver() {
    this.webDriver = null;
  }
}
