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

package org.webrtc.kite;

import io.cosmosoftware.kite.usrmgmt.TypeRole;
import org.openqa.selenium.WebDriver;

import javax.json.JsonValue;
import java.util.List;
import java.util.Map;

/**
 * Parent class for a test case.
 * <p>
 * It provides a list of WebDriver objects to the child classes to step the test algorithm.
 * <p>
 * It also contains the details of the node to which the test case is assigned.
 */
public abstract class KiteTest {

  private JsonValue payload;

  private List<WebDriver> webDriverList;
  
  private Map<WebDriver, TypeRole> driverRoleMap;
  
  private String commandName;
  
  /**
   * Gets payload.
   *
   * @return the payload
   */
  protected JsonValue getPayload() {
    return this.payload;
  }
  
  /**
   * The Name.
   */
  protected String name;
  
  /**
   * The Remote address.
   */
  protected String remoteAddress;
  
  
  /**
   * Method to set a payload.
   *
   * @param payload JsonValue
   */
  public void setPayload(JsonValue payload) {
    this.payload = payload;
    payloadHandling();
  }
  
  /**
   * Method to set a the remoteAddress (IP of the hub).
   *
   * @param remoteAddress String the address of the hub
   */
  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }
  
  /**
   * Gets web driver list.
   *
   * @return the web driver list
   */
  protected List<WebDriver> getWebDriverList() {
    return this.webDriverList;
  }
  
  /**
   * Method to set a web driver list.
   *
   * @param webDriverList Web Driver List
   */
  public void setWebDriverList(List<WebDriver> webDriverList) {
    this.webDriverList = webDriverList;
  }
  
  /**
   * Gets driver role map.
   *
   * @return the driver role map
   */
  public Map<WebDriver, TypeRole> getDriverRoleMap() {
    return driverRoleMap;
  }
  
  /**
   * Sets driver role map.
   *
   * @param driverRoleMap the driver role map
   */
  public void setDriverRoleMap(Map<WebDriver, TypeRole> driverRoleMap) {
    this.driverRoleMap = driverRoleMap;
  }
  
  /**
   * Method to set the name of this KITE test.
   *
   * @param name String
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Sets the command name for the NW instrumentation.
   *
   * @param commandName name of the command
   */
  public void setCommandName(String commandName) {
    this.commandName = commandName;
  }
  
  /**
   * Gets the command name for the NW instrumentation.
   *
   * @return the name of the command
   */
  public String getCommandName() {
    return this.commandName;
  }
  
  /**
   * Tests against List<WebDriver>
   *
   * @param testDescription test description
   *
   * @return Any object with a toString() implementation.
   * @throws Exception if an Exception occurs while method execution.
   */
  public abstract Object testScript(String testDescription ) throws Exception;
  
  /**
   * Payload handling method
   */
  protected abstract void payloadHandling();

}
