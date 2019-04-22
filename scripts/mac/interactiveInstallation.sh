#!/bin/bash
set +v
echo Welcome to the installation tutorial of KITE-2.0
echo Here are the default configuration
. ./gridConfig.sh

echo INSTALL_BROWSERS=$INSTALL_BROWSERS
echo USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER
echo LOCALHOST=$LOCALHOST
echo GECKO_VERSION=$GECKO_VERSION
echo CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION
echo SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT
echo SELENIUM_VERSION=$SELENIUM_VERSION
echo CHROME_VERSION=$CHROME_VERSION
echo FIREFOX_VERSION=$FIREFOX_VERSION
echo KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION
echo GRID_VERSION=$GRID_VERSION



function run() {
	./setupLocalGrid.command
}

function remind() {
	rm  gridConfig.sh-e
	echo Here are the new configuration: 
	. ./gridConfig.sh

	echo INSTALL_BROWSERS=$INSTALL_BROWSERS
	echo USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER
	echo LOCALHOST=$LOCALHOST
	echo GECKO_VERSION=$GECKO_VERSION
	echo CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION
	echo SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT
	echo SELENIUM_VERSION=$SELENIUM_VERSION
	echo CHROME_VERSION=$CHROME_VERSION
	echo FIREFOX_VERSION=$FIREFOX_VERSION
	echo KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION
	echo GRID_VERSION=$GRID_VERSION
	run
}

function configGrid() {

	echo Please check the corresponding KITE-Extras version from:
	echo https://github.com/CoSMoSoftware/KITE-Extras/releases
	echo currently the config file has the following version:
	echo KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION
	echo GRID_VERSION=$GRID_VERSION
	read -p "Are those versions correct? (y/n)" yn
	case $yn in
		[Nn]* )
			   echo Please enter the current version of KITE Extras
			   read InputKiteExtrasVersion
			   sed -i'' -e s/KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION/KITE_EXTRAS_VERSION=$InputKiteExtrasVersion/ ./gridConfig.sh
			   echo Please enter the current version of grid-utils
			   read inputGridVersion
			   sed -i'' -e s/GRID_VERSION=$GRID_VERSION/GRID_VERSION=$inputGridVersion/ ./gridConfig.sh
			   remind
			   ;;
		[Yy]* )
			   remind
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function configSelenium() {
	echo Please check the corresponding selenium version from:
	echo https://selenium-release.storage.googleapis.com/
	echo currently the config file has the following version:
	echo SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT
	echo SELENIUM_VERSION=$SELENIUM_VERSION
	read -p "Are those versions correct? (y/n)" yn
	case $yn in
		[Nn]* )
			   echo Please enter the current version of Selenium - short version
			   read InputSeleniumshort
			   sed -i'' -e s/SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT/SELENIUM_VERSION_SHORT=$InputSeleniumshort/ ./gridConfig.sh
			   echo Please enter the current version of Selenium - complete version
			   read InputSelenium
			   sed -i'' -e s/SELENIUM_VERSION=$SELENIUM_VERSION/SELENIUM_VERSION=$InputSelenium/ ./gridConfig.sh
			   configGrid
			   ;;
		[Yy]* )
			   configGrid
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function configGeckodriver() {

	echo Please check the corresponding geckodriver version from:
	echo https://github.com/mozilla/geckodriver/releases
	echo currently the config file has the following version:
	echo GECKO_VERSION=$GECKO_VERSION
	read -p "Is this version correct? (y/n)" yn
	case $yn in
		[Nn]* )
			   echo Please enter the current version of geckodriver
			   read InputGeckodriver
			   sed -i'' -e  s/GECKO_VERSION=$GECKO_VERSION/GECKO_VERSION=$InputGeckodriver/ ./gridConfig.sh
			   configSelenium
			   ;;
		[Yy]* )
			   configSelenium
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function configChromeDriver() {

	echo Please check the corresponding chromedriver version from:
	echo http://chromedriver.chromium.org/downloads
	echo currently the config file has the following version:
	echo CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION
	read -p "Is this version correct? (y/n)" yn
	case $yn in
		[Nn]* )
			   echo Please enter the current version of chrome Driver
			   read InputChromeDriver
			   sed -i'' -e  s/CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION/CHROMEDRIVER_VERSION=$InputChromeDriver/ ./gridConfig.sh
			   configGeckodriver
			   ;;
		[Yy]* )
			   configGeckodriver
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function configFirefox() {
	echo Please check that the versions of Firefox match the one in the config file.
	echo currently the config file has the following version:
	echo FIREFOX_VERSION=$FIREFOX_VERSION
	read -p "Is this version correct? (y/n)" yn
	case $yn in
		[Nn]* )
			   echo Please enter the current version of Firefox
			   read InputFirefoxVersion
			   sed -i'' -e  s/FIREFOX_VERSION=$FIREFOX_VERSION/FIREFOX_VERSION=$InputFirefoxVersion/ ./gridConfig.sh
			   configChromeDriver
			   ;;
		[Yy]* )
			   configChromeDriver
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function configChrome() {
	echo Please check that the versions of Chrome match the one in the config file.
	echo currently the config file has the following version:
	echo CHROME_VERSION=$CHROME_VERSION
	read -p "Is this version correct? (y/n)" yn
	case $yn in
		[Nn]* )
			   echo Please enter the current version of chrome
			   read InputChromeVersion
			   sed -i'' -e  s/CHROME_VERSION=$CHROME_VERSION/CHROME_VERSION=$InputChromeVersion/ ./gridConfig.sh
			   configFirefox
			   ;;
		[Yy]* )
			   configFirefox
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}



function capabilityMatcher() {
	read -p "Would you like to use the capability Matcher? (y/n)" yn
	case $yn in
		[Yy]* )
			   sed -i'' -e s/USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER/USE_CAPABILITY_MATCHER=TRUE/ ./gridConfig.sh
			   configChrome
			   ;;
		[Nn]* )
			   sed -i'' -e s/USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER/USE_CAPABILITY_MATCHER=FALSE/ ./gridConfig.sh
			   configChrome
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function localhost() {
	read -p "Would you like to run the localGrid on localhost ? (y/n)" yn
	case $yn in
		[Yy]* )
			   sed -i'' -e s/LOCALHOST=$LOCALHOST/LOCALHOST=TRUE/ ./gridConfig.sh
			   capabilityMatcher
			   ;;
		[Nn]* )
			   sed -i'' -e s/LOCALHOST=$LOCALHOST/LOCALHOST=FALSE/ ./gridConfig.sh
			   capabilityMatcher
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function choiceInstallBrowser() {
	read -p "Would you like to install the browsers? (y/n)" yn
	case $yn in
		[Yy]* )
			   sed -i'' -e s/INSTALL_BROWSERS=$INSTALL_BROWSERS/INSTALL_BROWSERS=TRUE/ ./gridConfig.sh
			   localhost
			   ;;
		[Nn]* )
			   sed -i'' -e s/INSTALL_BROWSERS=$INSTALL_BROWSERS/INSTALL_BROWSERS=FALSE/ ./gridConfig.sh
			   localhost
			  ;;
		* ) echo "Please answer yes or no.";;
	esac
}

function modify() {
	echo "you chose to change the current configuration"
	choiceInstallBrowser
}






read -p "Would you like to run the script with those configuration? (y/n)" yn
case $yn in
    [Yy]* )
           run
		;;
    [Nn]* )
	modify
	;;
    * ) echo "Please answer yes or no.";;
esac











