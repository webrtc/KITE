echo off
echo Welcome to the installation tutorial of KITE-2.0
echo Here are the default configuration
call gridConfig.bat

echo INSTALL_BROWSERS=%INSTALL_BROWSERS%
echo USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER%
echo LOCALHOST=%LOCALHOST%
echo GECKO_VERSION=%GECKO_VERSION%
echo CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%
echo SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%
echo SELENIUM_VERSION=%SELENIUM_VERSION%
echo CHROME_VERSION=%CHROME_VERSION%
echo FIREFOX_VERSION=%FIREFOX_VERSION%
echo KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%
echo GRID_VERSION=%GRID_VERSION%


:choice
echo.
set /P c=Would you like to run the script with those configuration? (y/n)
if /I "%c%" EQU "Y" goto :run
if /I "%c%" EQU "N" goto :modify
goto :choice


:modify
echo "you chose to change the current configuration"

:choiceInstallBrowser
echo.
set /P c=Would you like to install the browsers? (y/n)
if /I "%c%" EQU "Y" goto :installBrowser
if /I "%c%" EQU "N" goto :skipBrowser
goto :choiceInstallBrowser

:installBrowser
echo "you chose to install Browser"
cscript replace.vbs gridConfig.bat INSTALL_BROWSERS=%INSTALL_BROWSERS% INSTALL_BROWSERS=TRUE


goto :capabilityMatcher

:skipBrowser
echo "you chose to skip the browser installation" goto :capabilityMatcher
cscript replace.vbs gridConfig.bat INSTALL_BROWSERS=%INSTALL_BROWSERS% INSTALL_BROWSERS=FALSE 
goto :capabilityMatcher


:capabilityMatcher
echo.
set /P c=Would you like to use the capability Matcher ? (y/n)
if /I "%c%" EQU "Y" goto :useCapabilityMatcher 
if /I "%c%" EQU "N" goto :skipCapabilityMatcher
goto :capabilityMatcher

:useCapabilityMatcher
echo "you chose to use capability matcher"
cscript replace.vbs gridConfig.bat USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER% USE_CAPABILITY_MATCHER=TRUE 
goto :localhost

:skipCapabilityMatcher
echo "you chose to not use capability matcher"
cscript replace.vbs gridConfig.bat USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER% USE_CAPABILITY_MATCHER=FALSE 
goto :localhost


:localhost
echo.
set /P c=Would you like to run the localGrid using Localhost? (y/n)
if /I "%c%" EQU "Y" goto :uselocalhost 
if /I "%c%" EQU "N" goto :skiplocalhost
goto :localhost

:uselocalhost
echo "you chose to not use localhost"
cscript replace.vbs gridConfig.bat "LOCALHOST=%LOCALHOST%" "LOCALHOST=TRUE" 
goto :configChrome

:skiplocalhost

echo "you chose to use localhost"
cscript replace.vbs gridConfig.bat "LOCALHOST=%LOCALHOST%" "LOCALHOST=FALSE" 
goto :configChrome


:configChrome
echo.
echo Please check that the versions of Chrome match the one in the config file.
echo currently the config file has the following version:
echo CHROME_VERSION=%CHROME_VERSION%
set /P c=Is this version correct? (y/n)
if /I "%c%" EQU "N" goto :changeChromeVersion 
if /I "%c%" EQU "Y" goto :configFirefox 
goto :configChrome

:changeChromeVersion
echo Please enter the current version of chrome
set /P InputChromeVersion=
cscript replace.vbs gridConfig.bat "CHROME_VERSION=%CHROME_VERSION%" "CHROME_VERSION=%InputChromeVersion%" 
goto :configFirefox

:configFirefox
echo.
echo Please check that the versions of Firefox match the one in the config file.
echo currently the config file has the following version:
echo FIREFOX_VERSION=%FIREFOX_VERSION%
set /P c=Is this version correct? (y/n)
if /I "%c%" EQU "N" goto :changeFirefoxVersion 
if /I "%c%" EQU "Y" goto :configChromeDriver
goto :configFirefox

:changeFirefoxVersion
echo Please enter the current version of Firefox
set /P InputFirefoxVersion=
cscript replace.vbs gridConfig.bat "FIREFOX_VERSION=%FIREFOX_VERSION%" "FIREFOX_VERSION=%InputFirefoxVersion%" 
goto :configChromeDriver


:configChromeDriver
echo.
echo Please check the corresponding chromedriver version from:
echo http://chromedriver.chromium.org/downloads
echo currently the config file has the following version:
echo CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%
set /P c=Is this version correct? (y/n)
if /I "%c%" EQU "N" goto :changeChromeDriver
if /I "%c%" EQU "Y" goto :configFirefoxDriver
goto :configChromeDriver

:changeChromeDriver
echo Please enter the current version of chromeDriver
set /P InputChromeDriver=
cscript replace.vbs gridConfig.bat "CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%" "CHROMEDRIVER_VERSION=%InputChromeDriver%" 
goto :configFirefoxDriver


:configFirefoxDriver
echo.
echo Please check the corresponding geckodriver version from:
echo https://github.com/mozilla/geckodriver/releases
echo currently the config file has the following version:
echo GECKO_VERSION=%GECKO_VERSION%
set /P c=Is this version correct? (y/n)
if /I "%c%" EQU "N" goto :changeFirefoxDriver
if /I "%c%" EQU "Y" goto :configSeleniumDriver
goto :configFirefoxDriver

:changeFirefoxDriver
echo Please enter the current version of geckodriver
set /P InputFirefoxDriver=
cscript replace.vbs gridConfig.bat "GECKO_VERSION=%GECKO_VERSION%" "GECKO_VERSION=%InputFirefoxDriver%" 
goto :configSeleniumDriver


:configSeleniumDriver
echo.
echo Please check the corresponding selenium version from:
echo https://selenium-release.storage.googleapis.com/
echo currently the config file has the following version:
echo SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%
echo SELENIUM_VERSION=%SELENIUM_VERSION%
set /P c=Are those version correct? (y/n)
if /I "%c%" EQU "N" goto :changeSelenium
if /I "%c%" EQU "Y" goto :configGrid
goto :configSeleniumDriver

:changeSelenium
echo Please enter the current version of Selenium (short version)
set /P InputSeleniumshort=
cscript replace.vbs gridConfig.bat "SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%" "SELENIUM_VERSION_SHORT=%InputSeleniumshort%" 
echo Please enter the current version of Selenium (complete version)
set /P InputSelenium=
cscript replace.vbs gridConfig.bat "SELENIUM_VERSION=%SELENIUM_VERSION%" "SELENIUM_VERSION=%InputSelenium%" 
goto :configGrid


:configGrid
echo.
echo Please check the corresponding KITE-Extras version from:
echo https://github.com/CoSMoSoftware/KITE-Extras/releases
echo currently the config file has the following version:
echo KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%
echo GRID_VERSION=%GRID_VERSION%
set /P c=Are those version correct? (y/n)
if /I "%c%" EQU "N" goto :changeGrid
if /I "%c%" EQU "Y" goto :runScript
goto :configGrid

:changeGrid
echo Please enter the current version of KITE Extras
set /P InputKiteExtrasVersion=
cscript replace.vbs gridConfig.bat "KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%" "KITE_EXTRAS_VERSION=%InputKiteExtrasVersion%" 
echo Please enter the current version of grid-utils
set /P inputGridVersion=
cscript replace.vbs gridConfig.bat "GRID_VERSION=%GRID_VERSION%" "GRID_VERSION=%inputGridVersion%" 
goto :runScript


:runScript
echo.
echo Here are the new configuration: 
call gridConfig.bat
echo INSTALL_BROWSERS=%INSTALL_BROWSERS%
echo USE_CAPABILITY_MATCHER=%USE_CAPABILITY_MATCHER%
echo LOCALHOST=%LOCALHOST%
echo GECKO_VERSION=%GECKO_VERSION%
echo CHROMEDRIVER_VERSION=%CHROMEDRIVER_VERSION%
echo SELENIUM_VERSION_SHORT=%SELENIUM_VERSION_SHORT%
echo SELENIUM_VERSION=%SELENIUM_VERSION%
echo CHROME_VERSION=%CHROME_VERSION%
echo FIREFOX_VERSION=%FIREFOX_VERSION%
echo KITE_EXTRAS_VERSION=%KITE_EXTRAS_VERSION%
echo GRID_VERSION=%GRID_VERSION%

pause

goto :run

:run

setupLocalGrid.bat
exit
pause