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

import io.cosmosoftware.kite.interfaces.SampleData;
import org.openqa.selenium.Platform;
import org.webrtc.kite.config.media.MediaFile;
import org.webrtc.kite.config.media.MediaFileType;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a browser object in the config file.
 * <p>
 * { "browserName": "chrome", "version": "48.0", "platform": "LINUX" }
 * <p>
 * See README for possible values of platform.
 */
@Entity(name = Client.TABLE_NAME)
public class Browser extends Client {

  private final String DEFAULT_WINDOW_SIZE = "1920,1200";
  private List<String> flags = new ArrayList<>();
  private boolean headless = false;
  private BrowserSpecs specs;
  private boolean technologyPreview = false;
  private boolean useFakeMedia = true;
  private String windowSize = DEFAULT_WINDOW_SIZE;
  private MediaFile audio;
  private MediaFile video;

  /**
   * Instantiates a new Browser.
   */
  public Browser() {
    super();
  }

  /**
   * Constructs a new Browser with the given remote address and JsonObject.
   *
   * @param jsonObject JsonObject
   */
  public Browser(JsonObject jsonObject) {
    super(jsonObject);
    this.specs = new BrowserSpecs(jsonObject);
    this.headless = jsonObject.getBoolean("headless", headless);
    this.useFakeMedia = jsonObject.getBoolean("useFakeMedia", useFakeMedia);
    this.technologyPreview = jsonObject.getBoolean("technologyPreview", technologyPreview);
    this.windowSize = jsonObject.getString("windowSize", windowSize);
    JsonValue jsonValue = jsonObject.getOrDefault("flags", null);
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
   * Constructs a new Browser with the a Browser.
   *
   * @param browser Browser
   */
  public Browser(Browser browser) {
    super(browser);
    this.specs = browser.getSpecs();
    this.headless = browser.isHeadless();
    this.useFakeMedia = browser.useFakeMedia();
    this.technologyPreview = browser.isTechnologyPreview();
    this.windowSize = this.getWindowSize();
    this.flags = browser.getFlags();
    this.video = browser.getVideo();
    this.audio = browser.getAudio();
  }

  /**
   * Adds one flag to the flag list.
   *
   * @param flag to add.
   */
  public void addFlag(String flag) {
    this.flags.add(flag);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.cosmo.kite.dao.JsonBuilder#buildJsonObjectBuilder()
   */
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() {
    JsonObjectBuilder builder = super.buildJsonObjectBuilder()
        .add("browserName", this.getBrowserName())
        .add("platform", this.retrievePlatform().name())
        .add("headless", this.headless)
        .add("technologyPreview", this.technologyPreview);
    if (this.getVersion() != null) {
      builder.add("version", this.getVersion());
    }
    if (this.getRemoteVersion() != null) {
      builder.add("remoteVersion", this.getRemoteVersion());
    }
    if (this.getRemotePlatform() != null) {
      builder.add("remotePlatform", this.getRemotePlatform());
    }

    return builder;
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

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    Browser other = (Browser) obj;
    if (specs == null) {
      return other.specs == null;
    } else {
      return specs.equals(other.specs);
    }
  }

  @Transient
  @Override
  public Platform retrievePlatform() {
    return this.specs.getPlatform();
  }

  @Override
  public MobileSpecs buildMobileSpecs(JsonObject jsonObject) {
    if (jsonObject.get("mobile") == null) {
      return null;
    } else {
      return new MobileSpecs(jsonObject.getJsonObject("mobile"));
    }
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
   * Gets remote platform.
   *
   * @return the remote platform
   */
  @Transient
  public String getRemotePlatform() {
    return this.specs.getRemotePlatform();
  }

  /**
   * Sets remote platform.
   *
   * @param remotePlatform the remote platform
   */
  public void setRemotePlatform(String remotePlatform) {
    this.specs.setRemotePlatform(remotePlatform);
  }

  /**
   * Gets remote version.
   *
   * @return the remote version
   */
  @Transient
  public String getRemoteVersion() {
    return this.specs.getRemoteVersion();
  }

  /**
   * Sets remote version.
   *
   * @param remoteVersion the remote version
   */
  public void setRemoteVersion(String remoteVersion) {
    this.specs.setRemoteVersion(remoteVersion);
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
   * Gets use fake media.
   *
   * @return the use fake media
   */
  public boolean getUseFakeMedia() {
    return useFakeMedia;
  }

  /**
   * Sets use fake media.
   *
   * @param useFakeMedia the use fake media
   */
  public void setUseFakeMedia(boolean useFakeMedia) {
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


//  public boolean getExclude() {
//    return this.exclude;
//  }

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
  public boolean isHeadless() {
    return headless;
  }

  /**
   * Sets headless.
   *
   * @param headless the headless
   */
  public void setHeadless(boolean headless) {
    this.headless = headless;
  }

  /**
   * Is technologyPreview boolean.
   *
   * @return the boolean
   */
  public boolean isTechnologyPreview() {
    return technologyPreview;
  }

  /**
   * Sets technology preview.
   *
   * @param technologyPreview the technology preview
   */
  public void setTechnologyPreview(boolean technologyPreview) {
    this.technologyPreview = technologyPreview;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.SampleData#makeSampleData()
   */
  @Override
  public SampleData makeSampleData() {
    this.specs = (BrowserSpecs) new BrowserSpecs().makeSampleData();
    return this;
  }

  /**
   * Retrieve family or platform platform.
   *
   * @return the platform
   */
  public Platform retrieveFamilyOrPlatform() {
    Platform platform = this.retrievePlatform();
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
   * Parses the user agent string using user-agent-RCutils and retrieve the browser version and the
   * platform details. If the operating system is 'mac' then further parses using Woothie to get the
   * operating system version.
   *
   * @param userAgentString navigator.userAgent
   */
  public void setUserAgentVersionAndPlatform(String userAgentString) {
//    UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
//    Version version = userAgent.getBrowserVersion();
//    if (version != null) {
//      this.userAgentVersion = version.getVersion();
//    }
//    OperatingSystem operatingSystem = userAgent.getOperatingSystem();
//    if (operatingSystem != null) {
//      this.userAgentPlatform = operatingSystem.getName();
//    }
//    if (this.userAgentPlatform != null && this.userAgentPlatform.toLowerCase().startsWith("mac")) {
//      Map<String, String> map = Classifier.parse(userAgentString);
//      this.userAgentPlatform = this.userAgentPlatform + " " + map.get("os_version");
//    }
  }

  /**
   * Checks whether it is required to get navigator.userAgent from the browser.
   *
   * @return true if either of the user agent version and user agent platform is null.
   */
  public boolean shouldGetUserAgent() {
    return false;
  }

  /**
   * Use fake media boolean.
   *
   * @return true if to use the fake media from the browser
   */
  public boolean useFakeMedia() {
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
   * @param media       the media
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
}
