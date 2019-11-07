@echo off
taskkill /fi "WINDOWTITLE eq Allure Server" /F
set ALLURE_BAT="%KITE_HOME%\third_party\allure-2.10.0\bin\allure.bat"
IF EXIST %ALLURE_BAT% (
  if [%1] == [] (
    %ALLURE_BAT% generate kite-allure-reports
  ) ELSE (
    %ALLURE_BAT% generate %1
  )
) ELSE (
  echo "File not found: %ALLURE_BAT%"
  echo "Please edit this file and set ALLURE_BAT to the correct path. You may need to check your KITE_HOME variable."
)


