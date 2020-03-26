@echo off
rem Grid setup configuration for Windows 10
rem
rem Please check the Firefox and Chrome versions installed on your computer. If none installed, this script will download and install them.
rem
rem Please check and update the corresponding chromedriver and geckodriver versions from:
rem http://chromedriver.chromium.org/downloads
rem https://github.com/mozilla/geckodriver/releases
rem
rem Please check the selenium version from: https://selenium-release.storage.googleapis.com/

rem Set to TRUE to install Chrome and Firefox, or FALSE to skip this step if the correct versions are already installed.
set INSTALL_BROWSERS=FALSE

rem Set to TRUE/FALSE to enable/disable the CAPABILITY MATCHER
set USE_CAPABILITY_MATCHER=FALSE

rem Set to TRUE to use localhost or FALSE to use the host's IP address
set LOCALHOST=TRUE

rem Capability Matcher
set KITE_EXTRAS_VERSION=0.1.5
set GRID_UTILS_VERSION=0.0.1

rem Selenium version
set SELENIUM_VERSION_SHORT=3.141
set SELENIUM_VERSION=3.141.59

rem Browser versions
set CHROME_VERSION=80
set FIREFOX_VERSION=74

rem ChromeDriver and GeckoDriver versions
set CHROMEDRIVER_VERSION=80.0.3987.106
set GECKO_VERSION=v0.26.0