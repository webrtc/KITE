@echo off

rem check firefox version - Install if it is not the latest

call gridConfig.bat
wmic datafile where name="C:\\Program Files\\Mozilla Firefox\\firefox.exe" get Version /value > firefox.txt
find /c "Version=%FIREFOX_VERSION%" firefox.txt
if %errorlevel% equ 1 goto notfound
echo "Firefox version %FIREFOX_VERSION% is already installed"
goto done
:notfound
echo "Firefox version %FIREFOX_VERSION% not found in C:\\Program Files\\Mozilla Firefox\\, installing it."
ECHO $workdir = "c:\installer\" >> firefoxInstall.ps1
ECHO If (Test-Path -Path $workdir -PathType Container) >> firefoxInstall.ps1
ECHO { Write-Host "$workdir already exists" -ForegroundColor Red} >> firefoxInstall.ps1
ECHO ELSE >> firefoxInstall.ps1
ECHO { New-Item -Path $workdir  -ItemType directory } >> firefoxInstall.ps1
ECHO $source = "https://download.mozilla.org/?product=firefox-latest&os=win64&lang=en-US" >> firefoxInstall.ps1
ECHO $destination = "$workdir\firefox.exe" >> firefoxInstall.ps1
ECHO if (Get-Command 'Invoke-Webrequest') >> firefoxInstall.ps1
ECHO { >> firefoxInstall.ps1
ECHO      Invoke-WebRequest $source -OutFile $destination >> firefoxInstall.ps1
ECHO } >> firefoxInstall.ps1
ECHO else >> firefoxInstall.ps1
ECHO { >> firefoxInstall.ps1
ECHO     $WebClient = New-Object System.Net.WebClient >> firefoxInstall.ps1
ECHO     $webclient.DownloadFile($source, $destination) >> firefoxInstall.ps1
ECHO } >> firefoxInstall.ps1
ECHO Start-Process -FilePath "$workdir\firefox.exe" -ArgumentList "/S" >> firefoxInstall.ps1
ECHO Start-Sleep -s 35 >> firefoxInstall.ps1
ECHO rm -Force $workdir\firefox* >> firefoxInstall.ps1
Powershell.exe -executionpolicy remotesigned -File  firefoxInstall.ps1
goto done
:done


del /f firefox.txt
del /f firefoxInstall.ps1

