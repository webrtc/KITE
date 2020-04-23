@echo off
echo.
set MAVEN_VERSION=3.6.3
echo Please check the corresponding Maven version from:
echo http://maven.apache.org/download.cgi
echo currently the config file has the following version:
echo MAVEN_VERSION=%MAVEN_VERSION%
set /P c=Is this version correct?  (y/n/q)  
if /I "%c%" EQU "N" goto :changeMaven
if /I "%c%" EQU "Y" goto :install
if /I "%c%" EQU "Q" goto :quit
goto :mavenConfig

:changeMaven
echo Please enter the current version of Maven
set /P InputMavenVersion=
set MAVEN_VERSION=%InputMavenVersion%
goto :install

:install
rem download Maven with the specified version from the gridConfig.bat
echo [Net.ServicePointManager]::SecurityProtocol = 'TLS11','TLS12','ssl3' >> installMaven.ps1
echo Invoke-WebRequest -OutFile maven.zip https://www-us.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip >> installMaven.ps1

rem unzip the downloaded files 
Powershell.exe -executionpolicy remotesigned -File installMaven.ps1
del /f installMaven.ps1
jar xf maven.zip
move apache-maven-%MAVEN_VERSION% %USERPROFILE%
del /f maven.zip

setx mvn "%USERPROFILE%/apache-maven-%MAVEN_VERSION%/"

for /F "tokens=2* delims= " %%f IN ('reg query HKCU\Environment /v PATH ^| findstr /i path') do set OLD_SYSTEM_PATH=%%g

set NEW_PATH=%mvn%bin/
echo %OLD_SYSTEM_PATH% | FINDSTR /C:"%NEW_PATH%" >nul & IF ERRORLEVEL 1 (setx PATH "%OLD_SYSTEM_PATH%;%NEW_PATH%") else (ECHO %NEW_PATH% was already present)
timeout /t 2 >nul
set Path=%PATH%;%NEW_PATH%
