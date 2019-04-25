# This is KITE 2.0, Karoshi Interoperability Testing Engine (version 2.0)

The effortless way to test WebRTC compliance, prevent [Karoshi](https://en.wikipedia.org/wiki/Kar%C5%8Dshi) with __KITE!__


#### _This is not an official Google product_

See [LICENSE](LICENSE) for licensing.
&nbsp;    

## A. Install prerequisite software  

You will need Git, JDK 8 and Maven. Here's where you can find them:

* [Git](https://git-scm.com/downloads)  
* [JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)  
* [Maven](https://maven.apache.org/download.cgi?Preferred=ftp://mirror.reverse.net/pub/apache/)  

Maven requires you to add JAVA_HOME to your environment variables and MAVEN/bin to your PATH.  

If this is your first time installing Maven or JDK, you might want to check out these guides:
* On Windows: https://www.mkyong.com/maven/how-to-install-maven-in-windows/
* On Mac: https://www.mkyong.com/maven/install-maven-on-mac-osx/
* On Ubuntu: https://www.mkyong.com/maven/how-to-install-maven-in-ubuntu/


To verify your setup, in a new command prompt or shell terminal, type
``` 
mvn -version
```
Expected output on Windows 10:
```
Apache Maven 3.6.1
Maven home: C:\Program Files\Maven\apache-maven-3.6.1\bin\..
Java version: 1.8.0_191, vendor: Oracle Corporation
Java home: C:\Program Files\Java\jdk1.8.0_191\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

Install your favorite Java IDE. We recommend [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download) but you can use Eclipe or any other IDE if you prefer.
&nbsp;    

## B. Install KITE 2.0

1. Clone this repo into a folder __without any space__, for example under `\GitHub\`:  
    ```
    cd \
    mkdir GitHub
    cd GitHub
    git clone https://github.com/webrtc/KITE.git
    cd KITE
    ```

	

2. Configure __KITE__  

    This will set KITE_HOME environment variable and add utility scripts to your path.  

    2.1 On Windows, open a Command Prompt window and enter the followng commands:
    ```
    cd \GitHub\KITE
    configure.bat
    ```

    2.2 On Linux, open a terminal and enter the followng commands:
    ```
    cd \GitHub\KITE
    chmod +x configure.sh
    ./configure.sh
    ```     
    
    2.3 On Mac, open a terminal and enter the followng commands:
    ```
    cd \GitHub\KITE
    ./configure.command
    ```
    
    During this step, you will be prompt to setup the local grid. This is an interactive setup. 
    It is important to configure the Firefox and Chrome versions according to the versions installed on your computer.  
    If Chrome and/or Firefox are not installed, this script will automatically download and install the latest stable releases. 
          
    To check the browser versions:  
    __1. Chrome__  
    Open Chrome and enter <a href="chrome://settings/help" target="_blank">chrome://settings/help</a> into the address bar.
       
    __2. Firefox__  
    Open Firefox, top right menu, then select Help, then About Firefox. You can also find out the latest version
     at [www.mozilla.org/en-US/firefox/releases/](https://www.mozilla.org/en-US/firefox/releases/).
    
    Different browser versions require a different ChromeDriver (to control Chrome) and a different GeckoDriver (to control Firefox).
    You will need to find out what are the corresponding driver versions. Please visit the following two pages:
    * http://chromedriver.chromium.org/downloads
    * https://github.com/mozilla/geckodriver/releases 
    
    
    By default, the local grid setup script is configured for __Chrome__ version __73__ and __Firefox__ version __66__. 
    If these are the versions installed on your computer, you can safely use the default settings.
    Otherwise, you will need to edit the following settings when prompt to do so:  
    ```
    CHROME_VERSION=73
    FIREFOX_VERSION=66
    CHROMEDRIVER_VERSION=73.0.3683.68
    GECKO_VERSION=v0.24.0
    ```
    __Note:__ Please input only the major (i.e. 73 or 66) for the browser versions, but the full version with the minor (i.e. 73.0.3683.68 or v0.24.0) for the driver versions.   
   More details are available in the [local grid setup guide](scripts/README.md).

3. Compile 

    Just type `c` (which will execute `mvn clean install -DskipTests`)
    ```
    cd \GitHub\KITE
    c
    ```
    If you are within a test folder, for example in KITE-AppRTC-Test, you can type __`c`__ to compile the test module
     only or __`c all`__ to recompile the entire project:
    ```
    cd \GitHub\KITE\KITE-AppRTC-Test
    c all
    ```
     
    &nbsp;    

## C. Install the local grid

If you have chosen to skip the grid installation during configure, you can still do it
by following [local grid setup guide](scripts/README.md).
  
&nbsp;    
&nbsp;      
      

    
## D. Run the sample tests


__Note:__ You will need to have your [local grid](scripts/README.md) running before you can execute any test.  
You can check if your local grid is running and the browser versions installed by 
opening the [Grid Console](http://localhost:4444/grid/console).
In the following example, we are assuming __Chrome__ version __73__ and __Firefox__ version __66__.


### Edit the test config file

Edit the file `./KITE-Example-Test/configs/example.config.json` with your favorite text editor.  
You will need to change __`version`__ and __`platform`__ according to what is installed on your local grid.
For example, if your local grid is windows and the latest stable version of __Chrome__ is __73__, you should set: 
```json
      "version": "73",
      "platform": "WINDOWS",
```

If you're using Linux or Mac, change "WINDOWS" to "LINUX" or "MAC".



Read below about the configuration file, check that the desired browsers listed in your configuration file are available in your system.

### Understanding a basic configuration file

The example example.config.json file is almost the simplest config file you can get
 (Change the version of browsers to the appropriated one that you have installed on your testing machine):

```json
{
  "name": "Kite test example (with Allure reporting)",
  "callback": null,
  "remotes": [
    {
      "type": "local",
      "remoteAddress": "http://localhost:4444/wd/hub"
    }
  ],
  "tests": [
    {
      "name": "KiteExampleTest",
      "tupleSize": 1,
      "description": "This example test opens google and searches for Cosmo Software Consulting and verify the first result",
      "testImpl": "KiteExampleTest",
      "payload" : {
        "test1": "ONE",
        "test2": "TWO"
      }
    }
  ],
  "browsers": [
    {
      "browserName": "chrome",
      "version": "73",
      "platform": "WINDOWS",
      "flags": []
    },
    {
      "browserName": "firefox",
      "version": "66",
      "platform": "WINDOWS",
      "flags": []
    }
  ]
}

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
      "name": "KiteExampleTest",
      "tupleSize": 1,
      "description": "This example test opens google and searches for Cosmo Software Consulting and verify the first result",
      "testImpl": "KiteExampleTest",
      "payload" : {
        "test1": "ONE",
        "test2": "TWO"
      }
    }
  ],
```

It requests for firefox and chrome. Version and platform are required fields. Version and platform actually used in the tests will be reported in the result, and will appear in the dashboard.

Sample config files in `KITE-Example-Test/configs` contain the example with different browser, version and platform configuration, take a closer look

```json
  "browsers": [
    {
      "browserName": "chrome",
      "version": "72",
      "platform": "LINUX",
      "flags": []
    },
    {
      "browserName": "firefox",
      "version": "65",
      "platform": "MAC",
      "flags": []
    }
  ]
```


#### Run KITE-Example-Test


To run the example test,  
__On Windows:__  
on Windows:
```
cd %KITE_HOME%\KITE-Example-Test
r example.config.json
```
__On Linux and Mac:__  
```
cd $KITE_HOME/KITE-Example-Test
r example.config.json
```


#### Run KITE-AppRTC-Test

Edit the file `./KITE-AppRTC-Test/configs/iceconnection.local.config.json` with your favorite text editor.  
You will need to change __`version`__ and __`platform`__ according to what is installed on your local grid.

To run the AppRTC iceconnection test:
```
cd %KITE_HOME%\KITE-AppRTC-Test
r iceconnection.local.config.json
```

Alternatively, you can launch the test with the full command.
On Windows:  
```
-Dkite.firefox.profile="%KITE_HOME%"/third_party/ -cp "%KITE_HOME%/KITE-Engine/target/kite-jar-with-dependencies.jar;target/*" org.webrtc.kite.Engine configs/iceconnection.local.json
```
On Linux/Mac:  
```
-Dkite.firefox.profile="$KITE_HOME"/third_party/ -cp "$KITE_HOME/KITE-Engine/target/kite-jar-with-dependencies.jar:target/*" org.webrtc.kite.Engine configs/iceconnection.local.json
```

### Open the dashboard

After running the test, you can open the Allure dashboard with the command `a`.

on Windows:
```
cd %KITE_HOME%\KITE-AppRTC-Test
a
```
on Linux and Mac:
```
cd $KITE_HOME/KITE-AppRTC-Test
a
```

Congratulation! You should see the results of your first KITE test.

![KITE Test Dashboard](third_party/allure-2.10.0/lib/Alluredashboard.png)  




Alternatively, the full command to launch the Allure dashboard is:  
```
allure serve PATH_TO/kite-allure-reports
```



