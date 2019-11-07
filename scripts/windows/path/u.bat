@echo off
set KITE_JAR=%KITE_HOME%/KITE-Engine/target/kite-jar-with-dependencies.jar

if not defined KITE_HOME (
  echo KITE_HOME is not defined. Please run configure.bat or manually add KITE_HOME to your environment variables.
  goto end
)


if [%2] == [] (
  echo "Usage (with absolute path):"
  echo "u "old-allure-report-folder" "new-allure-report-folder" "
  goto end
)

set OLD_REPORT="%1"
set NEW_REPORT="%2"

if exist "%KITE_JAR%" (
  echo java -cp "%KITE_JAR%" org.webrtc.kite.ReportMerger %OLD_REPORT% %NEW_REPORT%
  java -cp "%KITE_JAR%" org.webrtc.kite.ReportMerger %OLD_REPORT% %NEW_REPORT%
) else (
  echo "File not found: %KITE_JAR%"
  echo "Please edit this file and set KITE_JAR to the correct path."
)

:end
