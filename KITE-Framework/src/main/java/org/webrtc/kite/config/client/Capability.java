/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.config.client;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.CommandMaker;
import io.cosmosoftware.kite.interfaces.SampleData;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.*;
import javax.print.attribute.standard.Media;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.openqa.selenium.Platform;
import org.webrtc.kite.config.media.MediaFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name = Capability.TABLE_NAME)
public class Capability extends KiteEntity implements SampleData {

  final static String TABLE_NAME = "capabilities";
  private static final long serialVersionUID = 1L;
  private final String DEFAULT_WINDOW_SIZE = "1920,1200";
  private final int DEFAULT_MAXINTANCES = 5;

  private String id;
  private Gateway gateway = Gateway.none;
  private Map<String, String> extraCapabilities = new HashMap<>();
  private Integer maxInstances;
  private List<String> flags = new ArrayList<>();
  private Boolean headless = false;
  private Boolean technologyPreview = false;
  private Boolean useFakeMedia = true;
  private String windowSize = DEFAULT_WINDOW_SIZE;
  private MediaFile audio;
  private MediaFile video;

  public Capability() {
    super();
  }

  public Capability(JsonObject jsonObject) {
    this();
    // Mandatory
    this.maxInstances = jsonObject.getInt("maxInstances", DEFAULT_MAXINTANCES);
    this.gateway = Gateway.valueOf(jsonObject.getString("gateway", "none"));
    this.headless = jsonObject.getBoolean("headless", headless);
    this.useFakeMedia = jsonObject.getBoolean("useFakeMedia", useFakeMedia);
    this.technologyPreview = jsonObject.getBoolean("technologyPreview", technologyPreview);
    this.windowSize = jsonObject.getString("windowSize", windowSize);
    JsonValue jsonValue = jsonObject.getOrDefault("extraCapabilities", null);
    if (jsonValue != null) {
      JsonObject extraCapabilitiesArray = (JsonObject) jsonValue;
      for (String capabilityName : extraCapabilitiesArray.keySet()) {
        this.addCapabilities(capabilityName, extraCapabilitiesArray.getString(capabilityName));
      }
    }
    jsonValue = jsonObject.getOrDefault("flags", null);
    if (jsonValue != null) {
      JsonArray flagArray = (JsonArray) jsonValue;
      for (int i = 0; i < flagArray.size(); i++) {
        this.flags.add(flagArray.getString(i));
      }
    }
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

  @Id

  @GeneratedValue(generator = Capability.TABLE_NAME)
  @GenericGenerator(name = Capability.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
      @Parameter(name = "prefix", value = "CAPA")
  })
  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public int getMaxInstances() {
    return this.maxInstances;
  }

  public void setMaxInstances(int maxInstances) {
    this.maxInstances = maxInstances;
  }

  @Enumerated(EnumType.STRING)
  public Gateway getGateway() {
    return gateway == null ? Gateway.none : gateway;
  }

  public void setGateway(Gateway gateway) {
    this.gateway = gateway;
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
   * Gets extra capabilities.
   *
   * @return extra capability map to be used to create webdriver
   */
  @Transient
  public Map<String, String> getExtraCapabilities() {
    return extraCapabilities;
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
   * Sets use fake media.
   *
   * @param useFakeMedia the use fake media
   */
  public void setUseFakeMedia(Boolean useFakeMedia) {
    this.useFakeMedia = useFakeMedia;
  }

  public Boolean isUseFakeMedia() {
    return useFakeMedia;
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

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.SampleData#makeSampleData()
   */
  @Override
  public SampleData makeSampleData() {
    return this;
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
    return false;
  }
}
