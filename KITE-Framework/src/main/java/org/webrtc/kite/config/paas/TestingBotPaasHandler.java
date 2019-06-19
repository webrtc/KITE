/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.BrowserType;
import org.webrtc.kite.config.client.Browser;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.List;

/**
 * The Class TestingBotPaasHandler.
 */
public class TestingBotPaasHandler extends PaasHandler {
  
  /**
   * Instantiates a new testing bot paas handler.
   *
   * @param pathToDB the path to DB
   * @param paas     the paas
   */
  public TestingBotPaasHandler(String pathToDB, Paas paas) {
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
      Browser browser = new Browser();
      browser.setVersion(jsonObject.getString("version", ""));
      
      String browserName = jsonObject.getString("name", "");
      if (browserName.endsWith("edge"))
        browserName = BrowserType.EDGE;
      else if (browserName.equalsIgnoreCase(BrowserType.GOOGLECHROME))
        browserName = BrowserType.CHROME;
      browser.setBrowserName(browserName);
      
      String platform = jsonObject.getString("platform", "");
      browser.setPlatform(
        platform.equalsIgnoreCase("CAPITAN") ? Platform.EL_CAPITAN : Platform.fromString(platform));
      
      this.clientList.add(browser);
    }
    
  }
  
}
