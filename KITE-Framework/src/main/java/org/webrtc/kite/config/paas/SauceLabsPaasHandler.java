/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.BrowserType;
import org.webrtc.kite.config.client.Client;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.List;

/**
 * The Class SauceLabsPaasHandler.
 */
public class SauceLabsPaasHandler extends PaasHandler {
  
  /**
   * Instantiates a new sauce labs paas handler.
   *
   * @param pathToDB the path to DB
   * @param paas     the paas
   */
  public SauceLabsPaasHandler(String pathToDB, Paas paas) {
    super(pathToDB, paas);
  }
  
  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.test.paas.PaasHandler#fetchConfig()
   */
  @Override
  public void fetchConfig() throws IOException {
    
    List<JsonObject> availableConfigList = this.getAvailableConfigList(null, null);
    
    /* might be not necessary, depending on data format it DB */
    for (JsonObject jsonObject : availableConfigList) {
      Client client = new Client();
      client.getBrowserSpecs().setVersion(jsonObject.getString("short_version", ""));
      
      String browserName = jsonObject.getString("api_name", "");
      if (browserName.equalsIgnoreCase(BrowserType.IE))
        browserName = BrowserType.IEXPLORE;
      client.getBrowserSpecs().setBrowserName(browserName);
      
      String platform = jsonObject.getString("os", "").toLowerCase();
      client.getBrowserSpecs().setPlatform(
        Platform.fromString(platform.startsWith("mac") ? platform.replaceAll("mac", "os x") : platform));
      
      this.clientList.add(client);
    }
    
  }
  
}
