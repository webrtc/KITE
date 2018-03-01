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

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GetUpdateTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of
 * web sites: 1) Open it up. 2) Look for the right information (version). 3) Store it in the result
 * array. 4) If at the end of the web site list, return all the obtained result, otherwise, go back
 * to 1 and do the same for the next in line.
 */
public class GetUpdateTest extends KiteTest {

    private final static Logger logger = Logger.getLogger(GetUpdateTest.class.getName());
    private final static Map<String, String> subjectList = new HashMap<String, String>();
    private String subject = "";
    private WebDriver webDriver = null;

    static {
        subjectList.put("Selenium", "http://www.seleniumhq.org/download/");
        subjectList.put("Chromedriver", "https://sites.google.com/a/chromium.org/chromedriver/home");
        subjectList.put("Geckodriver", "https://github.com/mozilla/geckodriver/releases");
        subjectList.put("MicrosoftWebDriver", "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/");
        subjectList.put("Firefox Stable", "https://www.mozilla.org/en-US/firefox/releases/");
        subjectList.put("Firefox Nightly", "https://www.mozilla.org/en-US/firefox/nightly/notes/");
        subjectList.put("Edge Stable", "https://developer.microsoft.com/en-us/microsoft-edge/platform/changelog/");
        subjectList.put("Edge Insider", "https://developer.microsoft.com/en-us/microsoft-edge/platform/changelog/");
        subjectList.put("Safari Stable", "https://developer.apple.com/library/content/releasenotes/General/WhatsNewInSafari/Introduction/Introduction.html#//apple_ref/doc/uid/TP40014305-CH1-SW1");
        subjectList.put("Safari Technology Preview", "https://developer.apple.com/safari/technology-preview/release-notes/");
        subjectList.put("Chrome Stable", "https://en.wikipedia.org/wiki/Google_Chrome_version_history");
        subjectList.put("Chrome Dev", "https://en.wikipedia.org/wiki/Google_Chrome_version_history");
        subjectList.put("Chrome Canary", "https://en.wikipedia.org/wiki/Google_Chrome_version_history");
    }



    /**
     * Opens the browser and go to the firsrt page on the list.
     */
    private void takeAction() {
        String add = subjectList.get(subject);
        webDriver.get(add);
    }

    /**
     * Gets the version of appropriate subject on the current site, accordingly.
     *
     * @return the JavaScript as string.
     */
    private String testJavaScript() {
        List<WebElement> webElementList;
        String versionNumber = "";
        char[] version;
        String style = "";
        switch (subject) {
            case "Selenium":
                webElementList = webDriver.findElements(By.tagName("td"));
                versionNumber = webElementList.get(1).getText();
                break;
            case "Chromedriver":
                version = new char[4];
                webElementList = webDriver.findElements(By.tagName("h2"));
                versionNumber = webElementList.get(1).getText();
                versionNumber.getChars(versionNumber.length()-4,versionNumber.length(),version,0);
                versionNumber = String.valueOf(version);
                break;
            case "Geckodriver":
                webElementList = webDriver.findElements(By.tagName("h1"));
                versionNumber = webElementList.get(1).getText();
                versionNumber = versionNumber.substring(1,versionNumber.length());
                break;
            case "MicrosoftWebDriver":
                version = new char[7];
                webElementList = webDriver.findElements(By.tagName("p"));
                versionNumber = webElementList.get(7).getText();
                versionNumber.getChars(9,16,version,0);
                versionNumber = String.valueOf(version);
                break;
            case "Firefox Stable":
                webElementList = webDriver.findElements(By.tagName("strong"));
                versionNumber = webElementList.get(0).getText();
                webElementList = webDriver.findElements(By.tagName("ol"));
                WebElement element = webElementList.get(1);
                webElementList = element.findElements(By.tagName("li"));
                String lastLi = webElementList.get(webElementList.size()-1).getText();
                String[] compare = lastLi.split("\\.");
                int firstStrong = Integer.valueOf(versionNumber.split("\\.")[0]);
                int compareInt = Integer.valueOf(compare[0]);
                if (firstStrong==compareInt)
                    versionNumber=webElementList.get(webElementList.size()-1).getText();
                break;
            case "Firefox Nightly":
                webElementList = webDriver.findElements(By.tagName("h2"));
                versionNumber = webElementList.get(1).getText();
                break;
            case "Edge Stable":
                version = new char[14];
                webDriver.findElements(By.tagName("strong")).get(0).click();
                webElementList = webDriver.findElements(By.tagName("option"));
                for (WebElement webElement:webElementList){
                    versionNumber = webElement.getText();
                    if (versionNumber.length()>14)
                        versionNumber.getChars(versionNumber.length()-14,versionNumber.length(),version,0);
                    if (String.valueOf(version).equalsIgnoreCase("Current Branch")){
                        version = new char[5];
                        versionNumber.getChars(0,5,version,0);
                        versionNumber = String.valueOf(version);
                        break;
                    }

                }

                break;
            case "Edge Insider":
                versionNumber = webDriver.findElements(By.tagName("strong")).get(0).getText();
                break;
            case "Safari Stable":
                version = new char[4];
                WebElement webElement = webDriver.findElement(By.id("toc"));
                webElementList = webElement.findElements(By.tagName("li"));
                while (webElementList.size()<2){
                    webElementList = webElement.findElements(By.tagName("li"));
                }
                versionNumber = webElementList.get(1).getText();
                versionNumber.getChars(versionNumber.length()-4,versionNumber.length(),version,0);
                versionNumber = String.valueOf(version);
                break;
            case "Safari Technology Preview":
                webElementList = webDriver.findElements(By.tagName("h3"));
                versionNumber = webElementList.get(0).getText();
                break;
            case "Chrome Stable":
                webElementList = webDriver.findElements(By.tagName("td"));

                for (WebElement anElement: webElementList){
                    style = anElement.getAttribute("style");
                    if (style.equalsIgnoreCase("white-space: nowrap; background: rgb(160, 231, 90);")){
                        versionNumber = anElement.getText();
                        break;
                    }
                }
                break;
            case "Chrome Dev":
                webElementList = webDriver.findElements(By.tagName("td"));
                for (WebElement anElement: webElementList){
                    style = anElement.getAttribute("style");
                    if (style.equalsIgnoreCase("white-space: nowrap; background: rgb(255, 255, 128);")){
                        versionNumber = anElement.getText();
                        break;
                    }
                }
                break;
            case "Chrome Canary":
                webElementList = webDriver.findElements(By.tagName("td"));
                for (WebElement anElement: webElementList){
                    style = anElement.getAttribute("style");
                    if (style.equalsIgnoreCase("white-space: nowrap; background: rgb(255, 255, 128);")){
                        versionNumber = anElement.getText();
                        break;
                    }
                }
                break;
        }

        return versionNumber;
    }


    @Override
    public Object testScript() throws Exception {
        String result = "";
        webDriver = this.getWebDriverList().get(0);
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for (String target: subjectList.keySet()){
            subject = target;
            this.takeAction();
            result = this.testJavaScript();
            subjectList.replace(target,result);
            jsonObjectBuilder.add(target, result);
        }
        JsonObject payload = jsonObjectBuilder.build();
        if (logger.isInfoEnabled())
          logger.info(payload.toString());
        return payload.toString();
    }

}
