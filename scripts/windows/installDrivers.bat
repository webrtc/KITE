@echo off
call gridConfig.bat
rem download the drivers zith the specified version from the gridConfig.bat
echo [Net.ServicePointManager]::SecurityProtocol = 'TLS11','TLS12','ssl3' >> installDrivers.ps1
echo Invoke-WebRequest -OutFile chromedriver.zip https://chromedriver.storage.googleapis.com/%CHROMEDRIVER_VERSION%/chromedriver_win32.zip >> installDrivers.ps1
echo Invoke-WebRequest -OutFile geckodriver.zip https://github.com/mozilla/geckodriver/releases/download/%GECKO_VERSION%/geckodriver-%GECKO_VERSION%-win64.zip >> installDrivers.ps1


rem unzip the downloaded files 
Powershell.exe -executionpolicy remotesigned -File  installDrivers.ps1
Powershell.exe -NoP -NonI -Command "Expand-Archive 'chromedriver.zip' '.\unziped\'" 
Powershell.exe -NoP -NonI -Command "Expand-Archive 'geckodriver.zip' '.\unziped\'"

rem move the drivers in the localGrid folder
cd unziped
move chromedriver.exe ../../../localGrid/chrome
move geckodriver.exe ../../../localGrid/firefox
cd ..

rem delete the zip filed
del /f installDrivers.ps1
del /f geckodriver.zip
del /f chromedriver.zip
rmdir /s /Q unziped
