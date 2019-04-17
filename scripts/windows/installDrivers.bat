@echo off
call configure.bat
rem download the drivers zith the specified version from the configure.bat
Powershell.exe -executionpolicy remotesigned Invoke-WebRequest -OutFile chromedriver.zip https://chromedriver.storage.googleapis.com/%CHROMEDRIVER_VERSION%/chromedriver_win32.zip
Powershell.exe -executionpolicy remotesigned Invoke-WebRequest -OutFile geckodriver.zip https://github.com/mozilla/geckodriver/releases/download/%GECKO_VERSION%/geckodriver-%GECKO_VERSION%-win64.zip

rem unzip the downloaded files 
powershell.exe -NoP -NonI -Command "Expand-Archive 'chromedriver.zip' '.\unziped\'"
powershell.exe -NoP -NonI -Command "Expand-Archive 'geckodriver.zip' '.\unziped\'"

rem move the drivers in the localGrid folder

cd unziped
move chromedriver.exe ../../../localGrid/chrome
move geckodriver.exe ../../../localGrid/firefox
cd ..

rem delete the zip filed

del /f geckodriver.zip
del /f chromedriver.zip
rmdir /s /Q unziped
