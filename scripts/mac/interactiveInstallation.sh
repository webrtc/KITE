#!/bin/bash
set +v
cd $KITE_HOME/scripts/mac
echo -e '\n'Welcome to the interactive setup of the local grid. '\n''\n'Here are the defaults settings:
. ./gridConfig.sh


echo -e '\n'Whether or not to install the Browsers '\n' 
echo INSTALL_BROWSERS=$INSTALL_BROWSERS
echo -e '\n'Browser versions
echo CHROME_VERSION=$CHROME_VERSION
echo FIREFOX_VERSION=$FIREFOX_VERSION
echo -e '\n'Whether to use 'localhost' or the computer's IP as the grid's hub address 
echo LOCALHOST=$LOCALHOST
echo -e '\n'GeckoDriver and ChromeDriver versions
echo GECKO_VERSION=$GECKO_VERSION
echo CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION
echo -e '\n'Selenium Standalone Server version
echo SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT
echo SELENIUM_VERSION=$SELENIUM_VERSION
echo -e '\n'Whether to use CAPABILITY MATCHER and the KITE Extras and Grid Utils versions
echo USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER
echo KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION
echo -e GRID_UTILS_VERSION=$GRID_UTILS_VERSION '\n'
function quit() {
	cd $KITE_HOME
}

function run() {
	cd $KITE_HOME/scripts/mac
	./setupLocalGrid.command
	quit
}


function remind() {
	rm  gridConfig.sh-e

	echo -e '\n'Here are the new configuration: 
	. ./gridConfig.sh


	echo -e '\n'Whether or not to install the Browsers '\n' 
	echo INSTALL_BROWSERS=$INSTALL_BROWSERS
	echo -e '\n'Browser versions
	echo CHROME_VERSION=$CHROME_VERSION
	echo FIREFOX_VERSION=$FIREFOX_VERSION
	echo -e '\n'Whether to use 'localhost' or the computer's IP as the grid's hub address 
	echo LOCALHOST=$LOCALHOST
	echo -e '\n'GeckoDriver and ChromeDriver versions
	echo GECKO_VERSION=$GECKO_VERSION
	echo CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION
	echo -e '\n'Selenium Standalone Server version
	echo SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT
	echo SELENIUM_VERSION=$SELENIUM_VERSION
	echo -e '\n'Whether to use CAPABILITY MATCHER and the KITE Extras and Grid Utils versions
	echo USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER
	echo KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION
	echo -e GRID_UTILS_VERSION=$GRID_UTILS_VERSION '\n'
	read -p "Do you want to start the Grid (y/n)?  " yn
case $yn in
    [Yy]* )
           run
           ;;
    [Nn]* )
          quit
          ;;
    * ) echo "Please answer yes or no."
		remind
		;;
esac
}

function configSelenium() {
	echo -e '\n'Please check the corresponding selenium version from:
	echo https://selenium-release.storage.googleapis.com/
	echo currently the config file has the following version:
	echo SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT
	echo SELENIUM_VERSION=$SELENIUM_VERSION
	read -p "Are those versions correct? (y/n/q)" ynq
	case $ynq in
		[Nn]* )
			   echo Please enter the current version of Selenium \(short version\)
			   read InputSeleniumshort
			   sed -i'' -e s/SELENIUM_VERSION_SHORT=$SELENIUM_VERSION_SHORT/SELENIUM_VERSION_SHORT=$InputSeleniumshort/ ./gridConfig.sh			   
			   echo Please enter the current version of Selenium \(complete version\)
			   read InputSelenium
			   sed -i'' -e s/SELENIUM_VERSION=$SELENIUM_VERSION/SELENIUM_VERSION=$InputSelenium/ ./gridConfig.sh
			   remind
			   ;;
		[Yy]* )
			   remind
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	configSelenium;;
	esac
}

function configFirefoxDriver() {

	echo -e '\n'Please check the corresponding geckodriver version from:
	echo https://github.com/mozilla/geckodriver/releases
	echo currently the config file has the following version:
	echo GECKO_VERSION=$GECKO_VERSION
	read -p "Is this version correct? (y/n/q)" ynq
	case $ynq in
		[Nn]* )
			   echo Please enter the current version of geckodriver
			   read InputGeckodriver
			   sed -i'' -e s/GECKO_VERSION=$GECKO_VERSION/GECKO_VERSION=$InputGeckodriver/ ./gridConfig.sh
			   configSelenium
			   ;;
		[Yy]* )
			   configSelenium
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	configFirefoxDriver;;
	esac
}

function configChromeDriver() {

	echo -e '\n'Please check the corresponding chromedriver version from:
	echo http://chromedriver.chromium.org/downloads
	echo currently the config file has the following version:
	echo CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION
	read -p "Is this version correct? (y/n/q)" ynq
	case $ynq in
		[Nn]* )
			   echo Please enter the current version of chrome Driver
			   read InputChromeDriver
			   sed -i'' -e s/CHROMEDRIVER_VERSION=$CHROMEDRIVER_VERSION/CHROMEDRIVER_VERSION=$InputChromeDriver/ ./gridConfig.sh
			   configFirefoxDriver
			   ;;
		[Yy]* )
			   configFirefoxDriver
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	configChromeDriver;;
	esac
}

function configFirefox() {
	echo -e '\n'Please check that the versions of Firefox match the one in the config file.
	echo currently the config file has the following version:
	echo FIREFOX_VERSION=$FIREFOX_VERSION
	read -p "Is this version correct? (y/n/q)" ynq
	case $ynq in
		[Nn]* )
			   echo Please enter the current version of Firefox
			   read InputFirefoxVersion
			   sed -i'' -e s/FIREFOX_VERSION=$FIREFOX_VERSION/FIREFOX_VERSION=$InputFirefoxVersion/ ./gridConfig.sh
			   configChromeDriver
			   ;;
		[Yy]* )
			   configChromeDriver
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	configFirefox;;
	esac
}

function configChrome() {
	echo -e '\n'Please check that the versions of Chrome match the one in the config file.
	echo currently the config file has the following version:
	echo CHROME_VERSION=$CHROME_VERSION
	read -p "Is this version correct? (y/n/q)" ynq
	case $ynq in
		[Nn]* )
			   echo Please enter the current version of chrome
			   read InputChromeVersion
			   sed -i'' -e s/CHROME_VERSION=$CHROME_VERSION/CHROME_VERSION=$InputChromeVersion/ ./gridConfig.sh
			   configFirefox
			   ;;
		[Yy]* )
			   configFirefox
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
				configChrome
				;;
	esac
}





function localhost() {
	echo -e 
	read -p "Would you like to use 'localhost' as the hub address (instead of the IP)?  (y/n/q)" ynq
	case $ynq in
		[Yy]* )
				echo You chose to use 'localhost' as the hub address instead of the IP

			   sed -i'' -e s/LOCALHOST=$LOCALHOST/LOCALHOST=TRUE/ ./gridConfig.sh
			   configChrome
			   ;;
		[Nn]* )
				echo You chose to use the IP as the hub address instead of 'localhost' 

			   sed -i'' -e s/LOCALHOST=$LOCALHOST/LOCALHOST=FALSE/ ./gridConfig.sh
			   configChrome
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	localhost;;
	esac
}



function configGrid() {

	echo -e '\n'Please check the corresponding KITE-Extras version from:
	echo https://github.com/CoSMoSoftware/KITE-Extras/releases
	echo currently the config file has the following versions:
	echo KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION
	echo GRID_UTILS_VERSION=$GRID_UTILS_VERSION
	read -p "Are those versions correct? (y/n/q)" ynq
	case $ynq in
		[Nn]* )
			   echo Please enter the current version of KITE Extras
			   read InputKiteExtrasVersion
			   sed -i'' -e s/KITE_EXTRAS_VERSION=$KITE_EXTRAS_VERSION/KITE_EXTRAS_VERSION=$InputKiteExtrasVersion/ ./gridConfig.sh			   
			   echo Please enter the current version of grid-utils
			   read inputGridVersion
			   sed -i'' -e s/GRID_UTILS_VERSION=$GRID_UTILS_VERSION/GRID_UTILS_VERSION=$inputGridVersion/ ./gridConfig.sh
			   localhost
			   ;;
		[Yy]* )
			   localhost
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	configGrid;;
	
	esac
}


function capabilityMatcher() {
		echo -e 
		read -p "Would you like to use the capability Matcher? (y/n/q)" ynq
	case $ynq in
		[Yy]* )
			echo You chose to use capability matcher
			   sed -i'' -e s/USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER/USE_CAPABILITY_MATCHER=TRUE/ ./gridConfig.sh
			   configGrid
			   ;;
		[Nn]* )
					echo You chose to not use capability matcher
			   sed -i'' -e s/USE_CAPABILITY_MATCHER=$USE_CAPABILITY_MATCHER/USE_CAPABILITY_MATCHER=FALSE/ ./gridConfig.sh
			   localhost
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	capabilityMatcher;;
	esac
}


function choiceInstallBrowser() {
		echo -e 
	read -p "Would you like to install the browsers? (y/n/q)" ynq
	case $ynq in
		[Yy]* )
			echo You chose to install the Browsers
			   sed -i'' -e s/INSTALL_BROWSERS=$INSTALL_BROWSERS/INSTALL_BROWSERS=TRUE/ ./gridConfig.sh
			   capabilityMatcher
			   ;;
		[Nn]* )
				echo You chose to skip the browser installation
			   sed -i'' -e s/INSTALL_BROWSERS=$INSTALL_BROWSERS/INSTALL_BROWSERS=FALSE/ ./gridConfig.sh
			   capabilityMatcher
			  ;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
	choiceInstallBrowser;;
	esac
}

function modify() {
	echo "you chose to change the current configuration"
	choiceInstallBrowser
}





function skipchoice() {
		echo -e 
read -p "Would you like to run the script with those configuration? (y/n/q)" ynq
case $ynq in
    [Yy]* )
           run
		;;
    [Nn]* )
		   modify
	;;
    [Qq]* )
           quit
		;;
    * ) echo "Please answer yes, no or quit."
		skipchoice;;
esac
}

skipchoice
