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
 * The Class BrowserStackPaasHandler.
 */
public class BrowserStackPaasHandler extends PaasHandler {
  
  /**
   * Instantiates a new browser stack paas handler.
   *
   * @param pathToDB the path to DB
   * @param paas     the paas
   */
  public BrowserStackPaasHandler(String pathToDB, Paas paas) {
    super(pathToDB, paas);
  }
  
  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.test.paas.PaasHandler#fetchConfig()
   */
  @Override
  public void fetchConfig() throws IOException {
    
    List<JsonObject> availableConfigList = this.getAvailableConfigList(this.paas.getUsername(),
      this.paas.getAccesskey());
    
    /* might be not necessary, depending on data format it DB */
    for (JsonObject jsonObject : availableConfigList) {
      Client client = new Client();
      client.getBrowserSpecs().setVersion(jsonObject.getString("browser_version", ""));
      
      String browserName = jsonObject.getString("browser", "");
      if (browserName.equalsIgnoreCase("edge"))
        browserName = BrowserType.EDGE;
      else if (browserName.equalsIgnoreCase("ie"))
        browserName = BrowserType.IEXPLORE;
      client.getBrowserSpecs().setBrowserName(browserName);
      
      String platform = jsonObject.getString("os", "").toLowerCase();
      String os_version = jsonObject.getString("os_version", "").toLowerCase();
      client.getBrowserSpecs().setPlatform(Platform
        .fromString(platform.equalsIgnoreCase("os x") ? os_version : (platform + " " + os_version).trim()));
      
      this.clientList.add(client);
    }
    
  }
  
}
