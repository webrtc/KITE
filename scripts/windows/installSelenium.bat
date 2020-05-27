@echo off
call gridConfig.bat
rem install selenium-server-standalone with the version specified in the gridConfig.bat
Powershell.exe -executionpolicy remotesigned Invoke-WebRequest -OutFile selenium.jar https://selenium-release.storage.googleapis.com/%SELENIUM_VERSION_SHORT%/selenium-server-standalone-%SELENIUM_VERSION%.jar

rem move the selenium file to the localGrid Folder
move selenium.jar ../../localGrid


