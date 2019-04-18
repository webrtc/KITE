package org.webrtc.kite.wpt;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;

/**
 * The type Run info.
 */
public class RunInfo {
  private final String revision;
  private String product = "n/a";
  private String browserVersion = "n/a";
  private int bits = 64;
  private String os = "n/a";
  private String osVersion = "n/a";
  
  /**
   * Gets json.
   *
   * @return the json
   */
  public JsonObject getJson() {
    return Json.createObjectBuilder()
      .add("revision", revision)
      .add("product", product)
      .add("browserVersion", browserVersion)
      .add("bits", bits)
      .add("os", os)
      .add("osVersion", osVersion).build();
  }
  
  @Override
  public String toString() {
    return getJson().toString();
  }
  
  /**
   * Instantiates a new Run info.
   *
   * @param revision the revision
   */
  public RunInfo(String revision) {
    this.revision = revision;
  }
  
  /**
   * Sets browser version.
   *
   * @param browserVersion the browser version
   */
  public void setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
  }
  
  /**
   * Sets os.
   *
   * @param os the os
   */
  public void setOs(String os) {
    this.os = os;
  }
  
  /**
   * Sets os version.
   *
   * @param osVersion the os version
   */
  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }
  
  /**
   * Sets product.
   *
   * @param product the product
   */
  public void setProduct(String product) {
    this.product = product;
  }
  
  /**
   * Sets bits.
   *
   * @param bits the bits
   */
  public void setBits(int bits) {
    this.bits = bits;
  }
  
  /**
   * Gets revision.
   *
   * @return the revision
   */
  public String getRevision() {
    return revision;
  }
  
  /**
   * Gets info from web driver.
   *
   * @param webDriver the web driver
   *
   * @throws IOException    the io exception
   */
  public void getInfoFromWebDriver(WebDriver webDriver) throws IOException, ParseException {
    org.openqa.selenium.Capabilities cap = ((RemoteWebDriver)webDriver).getCapabilities();
    setProduct(cap.getBrowserName());
    setBrowserVersion(cap.getVersion());
    
    webDriver.get("http://www.google.com");
    String userAgentString =  (String)((JavascriptExecutor) webDriver).executeScript(userAgentScript());
    getInfoFromUserAgent(userAgentString);
  }
  
  /**
   * Gets info from user agent.
   *
   * @param userAgentString the user agent string
   *
   * @throws IOException    the io exception
   * @throws ParseException the parse exception
   */
  private void getInfoFromUserAgent(String userAgentString) throws IOException, ParseException {
    final UserAgentParser parser = new UserAgentService().loadParser();
    final Capabilities capabilities = parser.parse(userAgentString);
    this.os = capabilities.getPlatform();
    this.osVersion = capabilities.getPlatformVersion();
  }
  
  /**
   * Put the product name, version and os into a String as a summarized name of the run
   *
   * @return a summarized name of the run
   */
  public String getSummarizedName() {
    return this.product + "_" + this.browserVersion + "_" + this.os;
  }
  
  
  private final static String userAgentScript() {
    return "var nav = '';" + "try { var myNavigator = {};"
      + "for (var i in navigator) myNavigator[i] = navigator[i];"
      + "nav = myNavigator.userAgent; } catch (exception) { nav = exception.message; }"
      + "return nav;";
  }
}
