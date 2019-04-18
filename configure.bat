@echo off

rem This script will set the USER enviroment variable KITE_HOME to the current folder
rem and add '%KITE_HOME%\scripts\path' to the USER PATH.
rem It will close the current and open a new one with the command c, r and a ready to use
rem to respectively compile, run and launch allure reports from the KITE Tests

set KITE_HOME=%cd%

setx KITE_HOME "%KITE_HOME%"
for /F "tokens=2* delims= " %%f IN ('reg query HKCU\Environment /v PATH ^| findstr /i path') do set OLD_SYSTEM_PATH=%%g

set NEW_PATH=%KITE_HOME%\scripts\windows\path
echo %OLD_SYSTEM_PATH% | FINDSTR /C:"%NEW_PATH%" >nul & IF ERRORLEVEL 1 (setx PATH "%OLD_SYSTEM_PATH%;%NEW_PATH%") else (ECHO %NEW_PATH% was already present)
timeout /t 2 >nul
set Path=%PATH%;%NEW_PATH%
start "Command Promt" cmd /K "cd %KITE_HOME%&&echo:&&echo Environment variable [1mKITE_HOME[0m set to [1m%KITE_HOME%[0m&&echo The path [1m%KITE_HOME%\scripts\windows\path;[0m has been added to the USER PATH.&&echo:&&echo You can now use commands [93mc[0m to compile, [93mr[0m to run and [93ma[0m to launch Allure."

exit
