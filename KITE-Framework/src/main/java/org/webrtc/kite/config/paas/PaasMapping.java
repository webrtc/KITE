/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;


import java.util.HashMap;
import java.util.Map;


/**
 * The Class PaasMapping.
 */
public class PaasMapping {
  
  /**
   * The paas hub map.
   */
  private static Map<PaasType, String> paasHubMap = new HashMap<PaasType, String>();
  /**
   * The paas rest map.
   */
  private static Map<PaasType, String> paasRestMap = new HashMap<PaasType, String>();
  
  static {
    paasHubMap.put(PaasType.saucelabs, "https://%s:%s@ondemand.saucelabs.com:443/wd/hub");
    paasHubMap.put(PaasType.browserstack, "http://%s:%s@hub.browserstack.com/wd/hub");
    paasHubMap.put(PaasType.testingbot, "http://%s:%s@hub.testingbot.com:4444/wd/hub");
  }
  
  static {
    paasRestMap.put(PaasType.saucelabs, "https://saucelabs.com/rest/v1/info/browsers/webdriver");
    paasRestMap.put(PaasType.browserstack, "https://browserstack.com/automate/browsers.json");
    paasRestMap.put(PaasType.testingbot, "https://api.testingbot.com/v1/browsers");
  }
  
  /**
   * Gets the hub url.
   *
   * @param paasType the paas type
   *
   * @return the hub url
   */
  public static String getHubUrl(PaasType paasType) {
    return paasHubMap.get(paasType);
  }
  
  /**
   * Gets the rest api url.
   *
   * @param paasType the paas type
   *
   * @return the rest api url
   */
  public static String getRestApiUrl(PaasType paasType) {
    return paasRestMap.get(paasType);
  }
  
}
