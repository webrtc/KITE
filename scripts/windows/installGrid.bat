@echo off
start cmd.exe /c createFolderLocalGrid.bat
if [%INSTALL_BROWSERS%] == ["TRUE"] (
  start cmd.exe /c installChrome.bat
  start cmd.exe /c installFirefox.bat
)

start cmd.exe /c installDrivers.bat
start cmd.exe /c installSelenium.bat
echo "Wait until all the installation are over to launch the grid"
pause>nul|set/p =Wait until all the installations are completed, then press any key to launch the grid...
cd ../../localGrid
startGrid.bat

