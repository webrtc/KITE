# This is KITE, Karoshi Interoperability Testing Engine

The effortless way to test WebRTC compliance, prevent [Karoshi](https://en.wikipedia.org/wiki/Kar%C5%8Dshi) with KITE!

# This is not an official Google product

See LICENSE for licensing.

# I. Single Machine Test setup

## A. Setup Selenium Standalone:

### Install prerequisite software

* Install the browsers you would like to test, available for your machine. Chrome, Edge and Firefox are supported at this stage. **Attention:** WebDriver for Firefox on Windows has some limitations (not supporting testing involving using media, or Firefox profile with fake media stream).
* Make sure you have a recent Java SDK installed (or get e.g. [*JDK 8.1*](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html))

### Download webdrivers and selenium server standalone

*  Create a new working directory and move in there.
*  Download the corresponding webdrivers on the root of a new working directory:

   *   Download the latest [*chrome webdriver*](https://sites.google.com/a/chromium.org/chromedriver/downloads),
   *   Download the latest [*edge webdriver*](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/),
   *   Download the latest [*firefox webdriver*](https://github.com/mozilla/geckodriver/releases),
   *   Safari webdrivers (if you are on macOs 10.12+, and have Safari 10+), is located at ```/usr/bin/safaridriver```

*  Download [*Selenium Server Standalone 3.4.0*](http://selenium-release.storage.googleapis.com/3.4/selenium-server-standalone-3.4.0.jar) in the same folder

### If on MAC, enable safari automation

Enable the 'Allow Remote Automation' option in Safari's Develop menu to control Safari via WebDriver.

### Start selenium-server-standalone:

Run this command, don't stop it until testing session has finished

```
java -Dwebdriver.chrome.driver=./chromedriver -Dwebdriver.edge.driver=./MicrosoftWebDriver -Dwebdriver.gecko.driver=./geckodriver -Dwebdriver.safari.driver=/usr/bin/safaridriver -jar selenium-server-standalone-3.4.0.jar
```

*  ```-Dwebdriver.xxxx.driver``` specifies the path to the webdriver executable matching the browser xxxx (possible values of xxxx are: gecko, chrome, edge, ...).
*  Depending on the testing needs, command line can include one, two or the three drivers

## B. Setup the Dashboard

### Deploy and run the dashboard locally

1.  Download compressed tomcat distribution, (version 8.5.20 at time of this writing) \[[*http://tomcat.apache.org/download-80.cgi\#8.5.20*](http://tomcat.apache.org/download-80.cgi#8.5.20)\]

1.  Unzip/extract anywhere.

1.  Build KITE-Dashboard, copy kiteweb.war file to apache-tomcat-8.5.20/webapps

1.  Start tomcat:

| Windows  | Linux / Mac |
| ------------- | ------------- |
| cd apache-tomcat-8.5.20\bin  | cd apache-tomcat-8.5.20/bin  |
| startup  | ./catalina.sh run  |

1.  Now open a browser and access the following URL [*http://localhost:8080/kiteweb*](http://localhost:8080/kiteweb)

1.  When test session finishes, stop tomcat:

| Windows  | Linux / Mac |
| ------------- | ------------- |
| cd apache-tomcat-8.5.20\bin  | cd apache-tomcat-8.5.20/bin  |
| shutdown  | ./shutdown.sh  |

## C. Build and KITE Engine and the basic sample AppRTC Test

### Build KITE-Engine and KITE-AppRTC-Test

1. Build and install (maven clean install) first KITE-Engine-IF

1. Then build and install KITE-Engine

1. Finally, build KITE-AppRTC-Test

* KITE-AppRTC-Test build includes a Junit test that requires local selenium, you can skip this (not recommended) running maven with -DskipTests 

### Choose and edit your test run configuration

You can use example configuration file `./KITE-AppRTC-Test/configs/local.config.json` as starting point.

Read below about the configuration file, check that the desired browsers listed in your configuration file are available in your system.

### Understanding a basic configuration file

The example local.config.json file is almost the simplest config file you can get:

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
      "testImpl": "org.webrtc.kite.IceConnectionTest"
    }
  ],
  "browsers": [
    {
      "browserName": "firefox"
    },
    {
      "browserName": "chrome"
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
      "testImpl": "org.webrtc.kite.IceConnectionTest"
    }
  ],
```

It requests for firefox and chrome, but without specifying a platform or a version, so it should run on any computer with those browsers installed. The version and platform actually used will be reported in the result, will appear in the dashboard.

Sample config files in ```KITE-AppRTC-Test/configs``` contain different examples for explicit version and platform configuration, take a look

 **Attention**: WebDriver for Firefox on Windows has some limitations (not supporting testing involving using media, or Firefox profile with fake media stream).
```json
  "browsers": [
    {
      "browserName": "firefox"
    },
    {
      "browserName": "chrome"
    }
  ]
```

## Run the local test

Execute the following command in the working directory, the last argument specifies the configuration file specifying the tests:

```
java -cp KITE-Engine/target/kite-jar-with-dependencies.jar:KITE-AppRTC-Test/target/apprtc-test-1.0.jar org.webrtc.kite.Engine ./KITE-AppRTC-Test/configs/local.config.json
```

Check the dashboard for the results and reports.

If you have followed steps above, that's [*http://localhost:8080/kiteweb*](http://localhost:8080/kiteweb).

# II. Distributed Test setup

## Setup Dashboard

KITE-Dashboard can be setup on any machine as described in previous section, you will need to change the callback url in your config file accordingly.

## Setup a hosted test service account

SauceLabs, BrowserStack and TestingBot have been tested and are supported. In theory any webdriver compliant service is usable.

See example files in ```KITE-AppRTC-Test/configs``` mixing different hosted test services.

Complete the fields username and accesskey appropriately.

* Don't forget to modify the example browsers, versions and platforms to suit your needs.

