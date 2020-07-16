/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.exception.BadEntityException;
import io.cosmosoftware.kite.instrumentation.NetworkProfile;
import io.cosmosoftware.kite.interfaces.SampleData;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.json.JsonObject;
import javax.persistence.*;
import org.webrtc.kite.config.client.BrowserSpecs;

/**
 * Entity implementation class for Entity: Paas.
 */
@Entity (name = Paas.TABLE_NAME)
public class Paas extends KiteEntity implements SampleData {

  final static String TABLE_NAME = "paas";
  private String accesskey;
  private String gridId;
  private String id;
  private PaasType type;
  private String url;
  private String username;
  private String region = "NC";
  private NetworkProfile networkProfile;
  private List<BrowserSpecs> specList = new ArrayList<>();
  private int availableSlots = 0;

  /**
   * Instantiates a new paas.
   */
  public Paas() {
    super();
  }

  /**
   * Instantiates a new paas.
   *
   * @param url the url
   */
  public Paas(String url) {
    super();
    this.type = PaasType.local;
    this.url = url;
  }

  /**
   * Instantiates a new paas.
   *
   * @param jsonObject the json object
   *
   * @throws BadEntityException the bad entity exception
   */
  public Paas(JsonObject jsonObject) throws BadEntityException {
    this();

    // Mandatory
    this.type = PaasType.valueOf(jsonObject.getString("type"));

    // Optional
    this.username = jsonObject.getString("username", this.username);
    this.accesskey = jsonObject.getString("accessKey", this.accesskey);
    this.url = jsonObject.getString("url", this.url);
    this.gridId = jsonObject.getString("gridId", this.gridId);

    if (this.type == PaasType.local) {
      if (this.url == null)
        throw new BadEntityException("'url' cannot be null for a local Paas");
    } else {
      if (this.username == null || this.accesskey == null)
        throw new BadEntityException(
            "Either 'username' or 'accesskey' is not found in the " + this.type + " Paas");
    }
  }

  /**
   * Checks if is local.
   *
   * @return true, if is local
   */
  public boolean checkLocal() {
    return this.type == PaasType.local;
  }

  /**
   * Gets the accesskey.
   *
   * @return the accesskey
   */
  public String getAccesskey() {
    return this.accesskey;
  }

  /**
   * Sets the accesskey.
   *
   * @param accesskey the new accesskey
   */
  public void setAccesskey(String accesskey) {
    this.accesskey = accesskey;
  }

  /**
   * Gets the grid id.
   *
   * @return the grid id
   */
  public String getGridId() {
    return gridId;
  }

  /**
   * Sets the grid id.
   *
   * @param gridId the new grid id
   */
  public void setGridId(String gridId) {
    this.gridId = gridId;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue (generator = Paas.TABLE_NAME)
  @GenericGenerator (name = Paas.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
      @Parameter (name = "prefix", value = "PAAS")})
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
   * Gets the type.
   *
   * @return the type
   */
  @Enumerated (EnumType.STRING)
  public PaasType getType() {
    return this.type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(PaasType type) {
    this.type = type;
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Sets the url.
   *
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the username.
   *
   * @return the username
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Sets the username.
   *
   * @param username the new username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Make paas handler.
   *
   * @param pathToDB the path to DB
   *
   * @return the paas handler
   */
  public PaasHandler makePaasHandler(String pathToDB) {
    switch (this.type) {
      case saucelabs:
        return new SauceLabsPaasHandler(pathToDB, this);
      case browserstack:
        return new BrowserStackPaasHandler(pathToDB, this);
      case testingbot:
        return new TestingBotPaasHandler(pathToDB, this);
      default:
        return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.dao.SampleData#makeSampleData()
   */
  @Override
  public SampleData makeSampleData() {
    this.type = PaasType.local;
    this.url = "http://localhost:4444/wd/hub";
    return this;
  }

  /**
   * Retrieve hub url.
   *
   * @return the string
   */
  public String retrieveHubUrl() {
    return this.checkLocal() ? this.url : this.type.hubUrl(this.username, this.accesskey);
  }

  @Transient
  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  @Transient
  public int getAvailableSlots() {
    return availableSlots;
  }

  public void setAvailableSlots(int availableSlots) {
    this.availableSlots = availableSlots;
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

  public void addSpec(BrowserSpecs specs) {
    this.specList.add(specs);
  }

  @Transient
  public List<BrowserSpecs> getSpecList() {
    return specList;
  }

  public void minusOneSlot() {
    if (this.availableSlots > 0) {
      this.availableSlots--;
    }
  }

  @Override
  public String toString() {
    return "[URL: " + this.url
        + " - Available Slots: " + this.availableSlots
        + " - Network Profile: " + (this.networkProfile == null ? "NONE" : this.networkProfile.getName()) + "]\n";
  }
}
