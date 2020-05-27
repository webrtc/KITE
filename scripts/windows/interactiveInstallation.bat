echo off
cls
echo:
echo Welcome to the interactive setup of the local grid.
echo:
echo:
echo Here are the defaults settings:
call gridConfig.bat

echo:
echo Whether or not to install the Browsers
echo INSTALL_BROWSERS=%INSTALL_BROWSERS%
echo:
echo Browser versions
echo CHROME_VERSION=%CHROME_VERSION%
echo FIREFOX_VERSION=%FIREFOX_VERSION%
echo:
echo Whether to use 'localhost' or the computer's IP as the grid's hub address 
echo LOCALHOST=%LOCALHOST%
echo:
echo GeckoDriver and ChromeDriver versions
echo GECKO_VERSION=%GECKO_VERSION%
echo CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%
echo:
echo Selenium Standalone Server version
echo SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%
echo SELENIUM_VERSION=%SELENIUM_VERSION%
echo:
echo Whether to use CAPABILITY MATCHER and the KITE Extras and Grid Utils versions
echo USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER%
echo KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%
echo GRID_UTILS_VERSION=%GRID_UTILS_VERSION%
echo:

:choice
echo.
set /P c=Would you like to run the script with these settings? (y/n/q)  
if /I "%c%" EQU "Y" goto :run
if /I "%c%" EQU "N" goto :modify
if /I "%c%" EQU "Q" goto :quit
goto :choice


:modify
echo You chose to change the current configuration

:choiceInstallBrowser
echo.
set /P c=Would you like to install the browsers? (y/n/q)  
if /I "%c%" EQU "Y" goto :installBrowser
if /I "%c%" EQU "N" goto :skipBrowser
if /I "%c%" EQU "Q" goto :quit
goto :choiceInstallBrowser

:installBrowser
echo You chose to install the Browsers
cscript //Nologo replace.vbs gridConfig.bat INSTALL_BROWSERS=%INSTALL_BROWSERS% INSTALL_BROWSERS=TRUE
goto :capabilityMatcher

:skipBrowser
echo You chose to skip the browser installation
cscript //Nologo replace.vbs gridConfig.bat INSTALL_BROWSERS=%INSTALL_BROWSERS% INSTALL_BROWSERS=FALSE
goto :capabilityMatcher


:capabilityMatcher
echo.
set /P c=Would you like to use the capability Matcher? (y/n/q)  
if /I "%c%" EQU "Y" goto :useCapabilityMatcher 
if /I "%c%" EQU "N" goto :skipCapabilityMatcher
if /I "%c%" EQU "Q" goto :quit
goto :capabilityMatcher

:skipCapabilityMatcher
echo You chose to not use capability matcher
cscript //Nologo replace.vbs gridConfig.bat USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER% USE_CAPABILITY_MATCHER=FALSE
goto :localhost

:useCapabilityMatcher
echo You chose to use capability matcher
cscript //Nologo replace.vbs gridConfig.bat USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER% USE_CAPABILITY_MATCHER=TRUE
goto :configGrid

:configGrid
echo.
echo Please check the corresponding KITE-Extras version from:
echo https://github.com/CoSMoSoftware/KITE-Extras/releases
echo currently the config file has the following version:
echo KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%
echo GRID_UTILS_VERSION=%GRID_UTILS_VERSION%
set /P c=Are those versions correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeGrid
if /I "%c%" EQU "Y" goto :localhost
if /I "%c%" EQU "Q" goto :quit
goto :configGrid

:changeGrid
echo Please enter the current version of KITE Extras
set /P InputKiteExtrasVersion=
cscript //Nologo replace.vbs gridConfig.bat "KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%" "KITE_EXTRAS_VERSION=%InputKiteExtrasVersion%"
goto :localhost

:localhost
echo.
set /P c=Would you like to use 'localhost' as the hub address (instead of the IP)?  (y/n/q)  
if /I "%c%" EQU "Y" goto :uselocalhost 
if /I "%c%" EQU "N" goto :skiplocalhost
if /I "%c%" EQU "Q" goto :quit
goto :localhost

:uselocalhost
echo You chose to use 'localhost' as the hub address instead of the IP
cscript //Nologo replace.vbs gridConfig.bat "LOCALHOST=%LOCALHOST%" "LOCALHOST=TRUE"
goto :configChrome

:skiplocalhost
echo You chose to use the IP as the hub address instead of 'localhost' 
cscript //Nologo replace.vbs gridConfig.bat "LOCALHOST=%LOCALHOST%" "LOCALHOST=FALSE"
goto :configChrome


:configChrome
echo.
echo Please check that the versions of Chrome match the one in the config file.
echo currently the config file has the following version:
echo CHROME_VERSION=%CHROME_VERSION%
set /P c=Is this version correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeChromeVersion 
if /I "%c%" EQU "Y" goto :configFirefox 
if /I "%c%" EQU "Q" goto :quit
goto :configChrome

:changeChromeVersion
echo Please enter the current version of chrome
set /P InputChromeVersion=
cscript //Nologo replace.vbs gridConfig.bat "CHROME_VERSION=%CHROME_VERSION%" "CHROME_VERSION=%InputChromeVersion%"
goto :configFirefox

:configFirefox
echo.
echo Please check that the versions of Firefox match the one in the config file.
echo currently the config file has the following version:
echo FIREFOX_VERSION=%FIREFOX_VERSION%
set /P c=Is this version correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeFirefoxVersion 
if /I "%c%" EQU "Y" goto :configChromeDriver
if /I "%c%" EQU "Q" goto :quit
goto :configFirefox

:changeFirefoxVersion
echo Please enter the current version of Firefox
set /P InputFirefoxVersion=
cscript //Nologo replace.vbs gridConfig.bat "FIREFOX_VERSION=%FIREFOX_VERSION%" "FIREFOX_VERSION=%InputFirefoxVersion%"
goto :configChromeDriver


:configChromeDriver
echo.
echo Please check the corresponding chromedriver version from:
echo http://chromedriver.chromium.org/downloads
echo currently the config file has the following version:
echo CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%
set /P c=Is this version correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeChromeDriver
if /I "%c%" EQU "Y" goto :configFirefoxDriver
if /I "%c%" EQU "Q" goto :quit
goto :configChromeDriver

:changeChromeDriver
echo Please enter the current version of chromeDriver
set /P InputChromeDriver=
cscript //Nologo replace.vbs gridConfig.bat "CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%" "CHROMEDRIVER_VERSION=%InputChromeDriver%"
goto :configFirefoxDriver


:configFirefoxDriver
echo.
echo Please check the corresponding geckodriver version from:
echo https://github.com/mozilla/geckodriver/releases
echo currently the config file has the following version:
echo GECKO_VERSION=%GECKO_VERSION%
set /P c=Is this version correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeFirefoxDriver
if /I "%c%" EQU "Y" goto :configSeleniumDriver
if /I "%c%" EQU "Q" goto :quit
goto :configFirefoxDriver

:changeFirefoxDriver
echo Please enter the current version of geckodriver
set /P InputFirefoxDriver=
cscript //Nologo replace.vbs gridConfig.bat "GECKO_VERSION=%GECKO_VERSION%" "GECKO_VERSION=%InputFirefoxDriver%"
goto :configSeleniumDriver


:configSeleniumDriver
echo.
echo Please check the corresponding selenium version from:
echo https://selenium-release.storage.googleapis.com/
echo currently the config file has the following version:
echo SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%
echo SELENIUM_VERSION=%SELENIUM_VERSION%
set /P c=Are those versions correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeSelenium
if /I "%c%" EQU "Y" goto :runScript
if /I "%c%" EQU "Q" goto :quit
goto :configSeleniumDriver

:changeSelenium
echo Please enter the current version of Selenium (short version)
set /P InputSeleniumshort=
cscript //Nologo replace.vbs gridConfig.bat "SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%" "SELENIUM_VERSION_SHORT=%InputSeleniumshort%"
echo Please enter the current version of Selenium (complete version)
set /P InputSelenium=
cscript //Nologo replace.vbs gridConfig.bat "SELENIUM_VERSION=%SELENIUM_VERSION%" "SELENIUM_VERSION=%InputSelenium%"
goto :runScript




:runScript
echo.
echo Here are the new settings: 
call gridConfig.bat
echo:
echo Whether or not to install the Browsers
echo INSTALL_BROWSERS=%INSTALL_BROWSERS%
echo:
echo Browser versions
echo CHROME_VERSION=%CHROME_VERSION%
echo FIREFOX_VERSION=%FIREFOX_VERSION%
echo:
echo Whether to use 'localhost' or the computer's IP as the grid's hub address 
echo LOCALHOST=%LOCALHOST%
echo:
echo GeckoDriver and ChromeDriver versions
echo GECKO_VERSION=%GECKO_VERSION%
echo CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%
echo:
echo Selenium Standalone Server version
echo SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%
echo SELENIUM_VERSION=%SELENIUM_VERSION%
echo:
echo Whether to use CAPABILITY MATCHER and the KITE Extras and Grid Utils versions
echo USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER%
echo KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%
echo GRID_UTILS_VERSION=%GRID_UTILS_VERSION%
echo:

pause

goto :run

:run
cd %KITE_HOME%/scripts/windows
setupLocalGrid.bat
goto :quit

:quit
cd %KITE_HOME%