# This is KITE, Karoshi Interoperability Testing Engine

The effortless way to test WebRTC compliance, prevent [Karoshi](https://en.wikipedia.org/wiki/Kar%C5%8Dshi) with KITE!

# This is not an official Google product

See LICENSE for licensing.

# I. Single Machine Test setup

## A. Setup Selenium Standalone:

### Install prerequisite software

* Install the browsers you would like to test, available for your machine. Chrome, Edge, Firefox and Safari are supported at this stage. See the wiki for some limitations or hints for each browser.
* Make sure you have a recent Java JDK installed, at least Java 8 (e.g. from [*Java SE downloads*](http://www.oracle.com/technetwork/java/javase/downloads/index.html)). Sometimes it might be neccessary to set JAVA_HOME and add it to PATH for java to work properly. 

### Download webdrivers and selenium server standalone

*  Create a new working directory and move in there.
*  Download the corresponding webdrivers on the root of a new working directory:

   *   Download the latest [*chrome webdriver*](https://sites.google.com/a/chromium.org/chromedriver/downloads),
   *   Download the latest [*firefox webdriver*](https://github.com/mozilla/geckodriver/releases),
   *   On Windows, download the latest [*edge webdriver*](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/),

*  Download [*Selenium Server Standalone*](https://www.seleniumhq.org/download/) in the same folder

### If on MAC, enable safari automation

Enable the 'Allow Remote Automation' option in Safari's Develop menu to control Safari via WebDriver.

### Start selenium-server-standalone

Run this command , don't stop it until testing session has finished. Also make sure that you have Firefox and Chrome already installed on your testing machine.

On Linux and Mac run:
```
java -Dwebdriver.chrome.driver=./chromedriver -Dwebdriver.gecko.driver=./geckodriver -jar selenium-server-standalone-3.x.x.jar
```

On Windows run:
```
java -Dwebdriver.chrome.driver=./chromedriver.exe -Dwebdriver.gecko.driver=./geckodriver.exe -Dwebdriver.edge.driver=./MicrosoftWebDriver.exe -jar selenium-server-standalone-3.x.x.jar
```

*  ```-Dwebdriver.xxxx.driver``` specifies the path to the webdriver executable matching the browser xxxx (possible values of xxxx are: gecko, chrome, edge, safari ...).
*  Depending on platform and the testing needs, command line can include one, two or the three drivers

## B. Build and KITE Engine and the basic sample AppRTC Test

Build uses [*maven*](https://maven.apache.org/) tool. Installable maven packages are available for common platforms, see [*link*](https://maven.apache.org/install.html) for manual installation.

### Build KITE-Engine and KITE-AppRTC-Test

1. Build and install running the command

```
mvn clean install
```

* If mvn (maven) is not recognisable by the system, you might need to set MAVEN_HOME and add it to PATH.

* If selenium is not running (step mentioned above), build will fail, as KITE-AppRTC-Test includes a Junit test that requires local selenium. 

You can skip all tests (not recommended) running maven with -DskipTests.
```
mvn -DskipTests clean install 
```

## C. Setup the Dashboard

### Deploy and run the dashboard locally

1.  Download compressed tomcat distribution, (version 8.5.20 at time of this writing) \[[*http://tomcat.apache.org/download-80.cgi\#8.5.20*](http://tomcat.apache.org/download-80.cgi#8.5.20)\]

1.  Unzip/extract anywhere.

1.  Copy ```KITE-Dashboard/target/kiteweb.war``` file to ```apache-tomcat-8.5.20/webapps```
  * If the file ```KITE-Dashboard/target/kiteweb.war``` doesn't exist, rerun the build (previous step)

4.  Start tomcat:

| Windows  | Linux / Mac |
| ------------- | ------------- |
| cd apache-tomcat-8.5.20\bin  | cd apache-tomcat-8.5.20/bin  |
| startup  | ./catalina.sh run  |

5.  Now open a browser and access the following URL [*http://localhost:8080/kiteweb*](http://localhost:8080/kiteweb)

6.  When test session finishes, stop tomcat:

| Windows  | Linux / Mac |
| ------------- | ------------- |
| cd apache-tomcat-8.5.20\bin  | cd apache-tomcat-8.5.20/bin  |
| shutdown  | ./shutdown.sh  |


## D. Run sample basic test

### Choose and edit your test run configuration

You can use example configuration file `./KITE-AppRTC-Test/configs/local.config.json` as starting point.

Read below about the configuration file, check that the desired browsers listed in your configuration file are available in your system.

### Understanding a basic configuration file

The example local.config.json file is almost the simplest config file you can get (Change the version of browsers to the appropriated one that you have installed on your testing machine):

```json
{
  "name": "local selenium example",
  "callback": "http://localhost:8080/kiteweb/datacenter",
  "remotes": [
    {
      "type": "local",
      "remoteAddress": "http://localhost:4444/wd/hub"
    }
  ],
  "tests": [
    {
      "name": "IceConnectionTest",
      "tupleSize": 2,
      "noOfThreads": 4,
      "description": "This test check the ICEConnection state between two browsers communicating via appr.tc",
      "testImpl": "org.webrtc.kite.apprtc.network.IceConnectionTest"
    }
  ],
  "browsers": [
    {
      "browserName": "chrome",
      "version": "65",
      "platform": "MAC"
    },
    {
      "browserName": "firefox",
      "version": "59",
      "platform": "LINUX"
    }
  ]
}
```

It assumes that the dashboard runs on the same machine:
```json
"callback": "http://localhost:8080/kiteweb/datacenter"
```

It registers only selenium server in the local machine:
```json
  "remotes": [
    {
      "type": "local",
      "remoteAddress": "http://localhost:4444/wd/hub"
    }
  ],
```

It registers IceConnectionTest class as a test (this class is implemented in KITE-AppRTC-Test)
```json
  "tests": [
    {
      "name": "IceConnectionTest",
      "tupleSize": 2,
      "noOfThreads": 4,
      "description": "This test check the ICEConnection state between two browsers communicating via appr.tc",
      "testImpl": "org.webrtc.kite.apprtc.network.IceConnectionTest"
    }
  ],
```

It requests for firefox and chrome. Version and platform are required fields. Version and platform actually used in the tests will be reported in the result, and will appear in the dashboard.

Sample config files in ```KITE-AppRTC-Test/configs``` contain different examples with different browser, version and platform configuration, take a look

```json
  "browsers": [
    {
      "browserName": "chrome",
      "version": "63",
      "platform": "MAC"
    },
    {
      "browserName": "firefox",
      "version": "57",
      "platform": "LINUX"
    },
    {
      "browserName": "safari",
      "version": "11",
      "platform": "MAC"
    },
    {
      "browserName": "MicrosoftEdge",
      "version": "16.16299",
      "platform": "WINDOWS"
    }
  ]
```

## Run the local test

Execute the following command in the working directory, the last argument specifies the configuration file specifying the tests:

On Linux and Mac run:
```
java -cp KITE-Engine/target/kite-jar-with-dependencies.jar:KITE-AppRTC-Test/target/apprtc-test-1.0.jar org.webrtc.kite.Engine ./KITE-AppRTC-Test/configs/local.config.json
```

On Windows run:
```
java -cp KITE-Engine/target/kite-jar-with-dependencies.jar;KITE-AppRTC-Test/target/apprtc-test-1.0.jar org.webrtc.kite.Engine ./KITE-AppRTC-Test/configs/local.config.json
```

Since the h264 plugin for firefox is only installed after the browser has already been opened, it will be too late to use (results in failed/timeout tests between firefox and safari). You can use the minimal firefox profiles provided in folder 'third_party'. All you need to do is specify the location of the profile folder:
```
java -Dkite.firefox.profile=/PATH/TO/firefox-h264-profiles/FOLDER/ -cp KITE-Engine/target/kite-jar-with-dependencies.ja...
```

Check the dashboard for the results and reports.

If you have followed steps above, that's [*http://localhost:8080/kiteweb*](http://localhost:8080/kiteweb).

# II. Distributed Test setup

## Setup Dashboard

KITE-Dashboard can be setup on any machine as described in previous section, you will need to change the callback url in your config file accordingly.

## Setup a hosted test service account

SauceLabs, BrowserStack and TestingBot have been tested and are supported.

See example files in ```KITE-AppRTC-Test/configs``` mixing different hosted test services.

Complete the fields username and accesskey appropriately.

* Don't forget to modify the example browsers, versions and platforms to suit your needs.

