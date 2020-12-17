#!/bin/bash
# Grid setup configuration for Ubuntu
#
# Please check the Firefox and Chrome versions installed on your computer. If none installed, this script will download and install them.
#
# Please check and update the corresponding chromedriver and geckodriver versions from:
# http://chromedriver.chromium.org/downloads
# https://github.com/mozilla/geckodriver/releases
#
# Please check the selenium version from: https://selenium-release.storage.googleapis.com/


# Set to TRUE to install Chrome and Firefox, or FALSE to skip this step if the correct versions are already installed.
export INSTALL_BROWSERS=TRUE

# Set to TRUE if this is a full desktop environment, FALSE if it's without any display
export DESKTOP_ENVIRONMENT=TRUE

# Set to TRUE to use localhost or FALSE to use the host's IP address
export LOCALHOST=TRUE

# ChromeDriver and GeckoDriver versions
export GECKO_VERSION=v0.28.0
export CHROMEDRIVER_VERSION=87.0.4280.88

# Selenium version
export SELENIUM_VERSION_SHORT=3.141
export SELENIUM_VERSION=3.141.59

# Browser versions
export FIREFOX_VERSION=83
export CHROME_VERSION=87

