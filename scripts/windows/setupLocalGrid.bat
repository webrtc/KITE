@echo off
cd %KITE_HOME%/scripts/windows
start cmd.exe /c createFolderLocalGrid.bat
if ["%INSTALL_BROWSERS%"] == ["TRUE"] (
  start "Installing Chrome" cmd.exe /c installChrome.bat
  start "Installing Firefox" cmd.exe /c installFirefox.bat
)

start "Installing ChromeDriver and GeckoDriver" cmd.exe /c installDrivers.bat
start "Installing Selenium" cmd.exe /c installSelenium.bat
echo:
echo:
cd %KITE_HOME%
pause>nul|set/p ="   Please wait until all the installations are completed, then press any key to launch the grid..."
echo:
start %KITE_HOME%/localGrid/startGrid.bat
start "Command Promt" cmd /K "cd %KITE_HOME%&&echo:&&echo Environment variable [1mKITE_HOME[0m set to [1m%KITE_HOME%[0m&&echo The path [1m%KITE_HOME%\scripts\windows\path;[0m has been added to the USER PATH.&&echo:&&echo You can now use commands [93mc[0m to compile, [93mr[0m to run and [93ma[0m to launch Allure."
exit
