/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.client;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.CommandMaker;
import io.cosmosoftware.kite.interfaces.SampleData;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.openqa.selenium.Platform;

import javax.json.JsonObject;
import javax.persistence.*;

import static org.webrtc.kite.Utils.getSystemPlatform;

/**
 * Entity implementation class for Entity: BrowserSpecs.
 */
@Entity(name = BrowserSpecs.TABLE_NAME)
public class BrowserSpecs extends KiteEntity implements CommandMaker, SampleData {

  /**
   * The Constant TABLE_NAME.
   */
  final static String TABLE_NAME = "browserspecs";

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The id.
   */
  private String id;

  /**
   * The browser name.
   */
  private String browserName;

  /**
   * The version.
   */
  private String version;

  /**
   * The remote version.
   */
  private String remoteVersion;

  /**
   * The platform.
   */
  private Platform platform;

  /**
   * The remote platform.
   */
  private String remotePlatform;

  /**
   * The path to binary.
   */
  private String pathToBinary;

  /**
   * The path to binary.
   */
  private String pathToDriver;

  /**
   * Instantiates a new browser specs.
   */
  public BrowserSpecs() {
    super();
  }

  /**
   * Instantiates a new browser specs.
   *
   * @param jsonObject the json object
   */
  public BrowserSpecs(JsonObject jsonObject) {
    this();

    // Mandatory
    this.browserName = jsonObject.getString("browserName");
    this.version = jsonObject.getString("version", null);
    String platform = jsonObject.getString("platform", "localhost");
    if (platform.equalsIgnoreCase("localhost")) {
      platform = getSystemPlatform();
    }
    this.platform = Platform.valueOf(platform);
    // Optional
    this.pathToBinary = jsonObject.getString("pathToBinary", this.pathToBinary);
    this.pathToDriver = jsonObject.getString("pathToDriver", this.pathToDriver);

  }

  /**
   * Instantiates a new browser specs.
   *
   * @param browserSpecs the browser specs
   */
  public BrowserSpecs(BrowserSpecs browserSpecs) {
    this();
    this.browserName = browserSpecs.getBrowserName();
    this.version = browserSpecs.getVersion();
    this.platform = browserSpecs.getPlatform();
    this.pathToBinary = browserSpecs.getPathToBinary();
    this.pathToDriver = browserSpecs.getPathToDriver();
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue(generator = BrowserSpecs.TABLE_NAME)
  @GenericGenerator(name = BrowserSpecs.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
      @Parameter(name = "prefix", value = "BROS")
  })
  public String getId() {
    return this.id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the browser name.
   *
   * @return the browser name
   */
  public String getBrowserName() {
    return this.browserName;
  }

  /**
   * Sets the browser name.
   *
   * @param browserName the new browser name
   */
  public void setBrowserName(String browserName) {
    this.browserName = browserName;
  }

  /**
   * Gets the version.
   *
   * @return the version
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets the remote version.
   *
   * @return the remote version
   */
  public String getRemoteVersion() {
    return this.remoteVersion;
  }

  /**
   * Sets the remote version.
   *
   * @param remoteVersion the new remote version
   */
  public void setRemoteVersion(String remoteVersion) {
    this.remoteVersion = remoteVersion;
  }

  /**
   * Gets the platform.
   *
   * @return the platform
   */
  @Enumerated(EnumType.STRING)
  public Platform getPlatform() {
    return this.platform;
  }

  /**
   * Sets the platform.
   *
   * @param platform the new platform
   */
  public void setPlatform(Platform platform) {
    this.platform = platform;
  }

  /**
   * Gets the remote platform.
   *
   * @return the remote platform
   */
  public String getRemotePlatform() {
    return this.remotePlatform;
  }

  /**
   * Sets the remote platform.
   *
   * @param remotePlatform the new remote platform
   */
  public void setRemotePlatform(String remotePlatform) {
    this.remotePlatform = remotePlatform;
  }

  /**
   * Gets the path to binary.
   *
   * @return the path to binary
   */
  public String getPathToBinary() {
    return this.pathToBinary;
  }

  /**
   * Sets the path to binary.
   *
   * @param pathToBinary the new path to binary
   */
  public void setPathToBinary(String pathToBinary) {
    this.pathToBinary = pathToBinary;
  }

  /**
   * Gets the path to driver.
   *
   * @return the path to driver
   */
  public String getPathToDriver() {
    return this.pathToDriver;
  }

  /**
   * Sets the path to binary.
   *
   * @param pathToDriver the new path to driver
   */
  public void setPathToDriver(String pathToDriver) {
    this.pathToDriver = pathToDriver;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((browserName == null) ? 0 : browserName.hashCode());
    result = prime * result + ((platform == null) ? 0 : platform.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    BrowserSpecs other = (BrowserSpecs) obj;
    if (browserName == null) {
      if (other.browserName != null) {
        return false;
      }
    } else if (!browserName.equals(other.browserName)) {
      return false;
    }
    if (platform != other.platform) {
      return false;
    }
    if (version == null) {
      return other.version == null;
    } else {
      return version.equals(other.version);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.interfaces.CommandMaker#makeCommand(boolean)
   */
  @Override
  public String makeCommand(boolean isHub, boolean isWindows) {
    return isHub ? ""
        : String.format("browserName=%s,version=%s,platform=%s", this.browserName, this.version,
        this.platform.name());
  }

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.interfaces.SampleData#makeSampleData()
   */
  @Override
  public SampleData makeSampleData() {
    this.browserName = "chrome";
    this.version = "73";
    this.platform = Platform.LINUX;
    this.pathToDriver = "./chromedriver";

    return this;
  }

}