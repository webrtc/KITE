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

import io.cosmosoftware.kite.interfaces.JsonBuilder;
import io.cosmosoftware.kite.interfaces.SampleData;
import io.cosmosoftware.kite.report.KiteLogger;
import org.hibernate.annotations.GenericGenerator;
import org.openqa.selenium.Platform;
import org.webrtc.kite.config.KiteEntity;
import org.webrtc.kite.config.paas.Paas;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The type End point.
 */
@Entity (name = Client.TABLE_NAME)
@Inheritance (
  strategy = InheritanceType.SINGLE_TABLE
)
public abstract class Client extends KiteEntity implements JsonBuilder, SampleData {
  /**
   * The constant TABLE_NAME.
   */
  final static String TABLE_NAME = "clients";
  /**
   * The KiteLogger.
   */
  protected final KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());
  /**
   * The Count.
   */
  protected int count;
  /**
   * The Id.
   */
  protected String id;
  /**
   * The Mobile.
   */
  protected MobileSpecs mobile;
  /**
   * The Paas.
   */
  protected Paas paas;
  private boolean exclude;
  private Map<String, String> extraCapabilities = new HashMap<>();
  private String gateway;
  private JsonObject jsonConfig;
  private int maxInstances;
  
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
    for (String capabilityName : client.getExtraCapabilities().keySet()) {
      this.addCapabilities(capabilityName, client.getExtraCapabilities().get(capabilityName));
    }
  }
  
  /**
   * Constructs a new KiteConfigObject with the given remote address and JsonObject.
   *
   * @param jsonObject JsonObject
   */
  protected Client(JsonObject jsonObject) {
    this.jsonConfig = jsonObject;
    this.exclude = jsonObject.getBoolean("exclude", false);
    this.count = jsonObject.getInt("count", 1);
    JsonValue jsonValue = jsonObject.getOrDefault("extraCapabilities", null);
    this.gateway = jsonObject.getString("gateway", null);
    
    if (jsonValue != null) {
      JsonObject extraCapabilitiesArray = (JsonObject) jsonValue;
      for (String capabilityName : extraCapabilitiesArray.keySet()) {
        this.addCapabilities(capabilityName, extraCapabilitiesArray.getString(capabilityName));
      }
    }
  }
  
  /**
   * add new capability name/value pair to browser
   *
   * @param capabilityName  capability name
   * @param capabilityValue capability value
   */
  public void addCapabilities(String capabilityName, String capabilityValue) {
    this.extraCapabilities.put(capabilityName, capabilityValue);
  }
  
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() {
    JsonObjectBuilder builder = Json.createObjectBuilder()
      .add("exclude", this.exclude);
    if (this.mobile != null) {
      builder.add("mobile", this.mobile.buildJsonObjectBuilder());
    }
    if (this.paas != null) {
      builder.add("paas", this.paas.getType().name());
    }
    return builder;
  }
  
  /**
   * Gets count.
   *
   * @return the count
   */
  public int getCount() {
    return count;
  }
  
  /**
   * Sets count.
   *
   * @param count the count
   */
  public void setCount(int count) {
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
  @GeneratedValue (generator = Client.TABLE_NAME)
  @GenericGenerator (name = Client.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
    @org.hibernate.annotations.Parameter (name = "prefix", value = "CLNT")
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
   * Gets mobile.
   *
   * @return the mobile
   */
  @OneToOne (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public MobileSpecs getMobile() {
    return this.mobile;
  }
  
  /**
   * Sets mobile.
   *
   * @param mobile the mobile
   */
  public void setMobile(MobileSpecs mobile) {
    this.mobile = mobile;
  }
  
  /**
   * Gets paas.
   *
   * @return the paas
   */
  @OneToOne (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
  public int hashCode() {
    final int prime = 31;
    return prime + ((mobile == null) ? 0 : mobile.hashCode());
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
    if (mobile == null) {
      return other.mobile == null;
    } else {
      return mobile.equals(other.mobile);
    }
  }
  
  /**
   * Is exclude boolean.
   *
   * @return the boolean
   */
  public boolean isExclude() {
    return exclude;
  }
  
  /**
   * Sets exclude.
   *
   * @param exclude the exclude
   */
  public void setExclude(boolean exclude) {
    this.exclude = exclude;
  }
  
  /**
   * Retrieve platform platform.
   *
   * @return the platform
   */
  public abstract Platform retrievePlatform();
  
  @Override
  public String toString() {
    return buildJsonObjectBuilder().build().toString();
  }
}
