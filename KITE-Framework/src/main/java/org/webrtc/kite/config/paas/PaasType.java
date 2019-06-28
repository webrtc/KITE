/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;

/**
 * The Enum PaasType.
 */
public enum PaasType {
  
  /**
   * The local.
   */
  local,
  
  /**
   * The saucelabs.
   */
  saucelabs,
  
  /**
   * The browserstack.
   */
  browserstack,
  
  /**
   * The testingbot.
   */
  testingbot;
  
  /**
   * Hub url.
   *
   * @return the string
   */
  public String hubUrl() {
    return PaasMapping.getHubUrl(this);
  }
  
  /**
   * Hub url.
   *
   * @param username  the username
   * @param accesskey the accesskey
   *
   * @return the string
   */
  public String hubUrl(String username, String accesskey) {
    return String.format(PaasMapping.getHubUrl(this), username, accesskey);
  }
  
  /**
   * Rest api url.
   *
   * @return the string
   */
  public String restApiUrl() {
    return PaasMapping.getRestApiUrl(this);
  }
  
}
