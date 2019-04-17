@echo off
call configure.bat
rem check chrome version - Install if it is not the latest
wmic datafile where name="C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe" get Version /value > chrome.txt
find /c "Version=%CHROME_VERSION%" chrome.txt
del /f chrome.txt

if %errorlevel% equ 1 goto notfound
echo Chrome ok
goto done
:notfound
echo "Chrome version %CHROME_VERSION% not found in C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe, installing it."
ECHO $Path = $env:TEMP; $Installer = "chrome_installer.exe"; Invoke-WebRequest "http://dl.google.com/chrome/install/375.126/chrome_installer.exe" -OutFile $Path\$Installer; Start-Process -FilePath $Path\$Installer -Args "/silent /install" -Verb RunAs -Wait; Remove-Item $Path\$Installer >> chromeInstall.ps1
Powershell.exe -executionpolicy remotesigned -File  chromeInstall.ps1
goto done
:done



del /f chromeInstall.ps1
