@echo off
start cmd.exe /c createFolderLocalGrid.bat
if ["%INSTALL_BROWSERS%"] == ["TRUE"] (
  start "Installing Chrome" cmd.exe /c installChrome.bat
  start "Installing Firefox" cmd.exe /c installFirefox.bat
)

start "Installing ChromeDriver and GeckoDriver" cmd.exe /c installDrivers.bat
start "Installing Selenium" cmd.exe /c installSelenium.bat
echo:
echo:
pause>nul|set/p ="   Please wait until all the installations are completed, then press any key to launch the grid..."
cd ../../localGrid
startGrid.bat
cd %KITE_HOME%

