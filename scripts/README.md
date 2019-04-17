[back](../README.md)

# Local Grid Setup Guide

## Windows 10

### Firefox and Chrome setup

Please check the Firefox and Chrome versions installed on your computer. If none installed, this script will download and install them.  

### ChromeDriver and GeckoDriver setup

Please check and update the corresponding chromedriver and geckodriver versions from:  
* http://chromedriver.chromium.org/downloads
* https://github.com/mozilla/geckodriver/releases


### Selenium Standalone jar

Please check the selenium version from: https://selenium-release.storage.googleapis.com/


### Edit the configure.bat accordingly:
```
set GECKO_VERSION=v0.24.0
set CHROMEDRIVER_VERSION=73.0.3683.68
set SELENIUM_VERSION_SHORT=3.141
set SELENIUM_VERSION=3.141.59
set CHROME_VERSION=73
set FIREFOX_VERSION=66
```


### Installing the Grid

Run  
```
installGrid.bat
```


The install script will launch the following scripts:

```
createFolderLocalGrid.bat
```
This will create the folder localGrid and will create the different script to be able to run the Grid

```
installChrome.bat
```
This will check which version of Google Chrome is installed on your computer, if it is not the one you specified in your configure.bat, it will install the latest version of Chrome

```
installFirefox.bat
```
This will check which version of Mozilla Firefox is installed on your computer, if it is not the one you specified in your configure.bat, it will install the latest version of Firefox

```
installDrivers.bat
```
This will download the version of chromedriver and selenium driver you specified in your configure.bat, it will then unzip them and move them to the localGrid folder.

```
installSelenium.bat
```
This will download the version of selenium standalone server you specified in your configure.bat and move it to the localGrid folder.



Once all those script are over, you will be able to launch the local grid by running the startGrid.bat script from the localGrid folder.




## Linux (Ubuntu)  

### Firefox and Chrome setup

Please check the Firefox and Chrome versions installed on your computer. If none installed, this script will download and install them.  

### ChromeDriver and GeckoDriver setup

Please check and update the corresponding chromedriver and geckodriver versions from:  
* http://chromedriver.chromium.org/downloads
* https://github.com/mozilla/geckodriver/releases


### Selenium Standalone jar

Please check the selenium version from: https://selenium-release.storage.googleapis.com/


### Edit the configure.bat accordingly:
```
set GECKO_VERSION=v0.24.0
set CHROMEDRIVER_VERSION=73.0.3683.68
set SELENIUM_VERSION_SHORT=3.141
set SELENIUM_VERSION=3.141.59
set CHROME_VERSION=73
set FIREFOX_VERSION=66
```


### Installing the Grid

Open your terminal in this folder and run:  
```
chmod +x *.sh
./script.sh
```


The install script will launch the following scripts:

```
createFolderLocalGrid.sh
```
This will create the folder localGrid and will create the different script to be able to run the Grid

```
installChrome.sh
```
This will check which version of Google Chrome is installed on your computer, if it is not the one you specified in your configure.bat, it will install the latest version of Chrome

```
installFirefox.sh
```
This will check which version of Mozilla Firefox is installed on your computer, if it is not the one you specified in your configure.bat, it will install the latest version of Firefox

```
installDrivers.sh
```
This will download the version of chromedriver and selenium driver you specified in your configure.bat, it will then unzip them and move them to the localGrid folder.

```
installSelenium.sh
```
This will download the version of selenium standalone server you specified in your configure.bat and move it to the localGrid folder.

```
startGrid.sh
```
This will launch the Grid by creating a Hub and two Nodes  



## MAC

### Firefox and Chrome setup

Please check the Firefox and Chrome versions installed on your computer. If none installed, this script will download and install them.  

### ChromeDriver and GeckoDriver setup

Please check and update the corresponding chromedriver and geckodriver versions from:  
* http://chromedriver.chromium.org/downloads
* https://github.com/mozilla/geckodriver/releases

### Safari setup

Enable the 'Allow Remote Automation' option in Safari's Developer menu to control Safari via WebDriver.

### Selenium Standalone jar

Please check the selenium version from: https://selenium-release.storage.googleapis.com/


### Edit the configure.bat accordingly:
```
set GECKO_VERSION=v0.24.0
set CHROMEDRIVER_VERSION=73.0.3683.68
set SELENIUM_VERSION_SHORT=3.141
set SELENIUM_VERSION=3.141.59
set CHROME_VERSION=73
set FIREFOX_VERSION=66
set SAFARI_VERSION=66
```


### Installing the Grid

Open your terminal in this folder and run:  
```
chmod +x *.sh
./script.sh
```


The install script will launch the following scripts:

```
createFolderLocalGrid.sh
```
This will create the folder localGrid and will create the different script to be able to run the Grid

```
installChrome.sh
```
This will check which version of Google Chrome is installed on your computer, if it is not the one you specified in your configure.bat, it will install the latest version of Chrome

```
installFirefox.sh
```
This will check which version of Mozilla Firefox is installed on your computer, if it is not the one you specified in your configure.bat, it will install the latest version of Firefox

```
installDrivers.sh
```
This will download the version of chromedriver and selenium driver you specified in your configure.bat, it will then unzip them and move them to the localGrid folder.

```
installSelenium.sh
```
This will download the version of selenium standalone server you specified in your configure.bat and move it to the localGrid folder.

```
startGrid.sh
```
This will launch the Grid by creating a Hub and three Nodes
