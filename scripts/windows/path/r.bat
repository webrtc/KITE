@echo off
set KITE_JAR=%KITE_HOME%/KITE-Engine/target/kite-jar-with-dependencies.jar

if not defined KITE_HOME (
  echo KITE_HOME is not defined. Please run configure.bat or manually add KITE_HOME to your environment variables.
  goto :end
)


if [%1] == [] (
  echo Usage:
  echo:
  echo r example.config.json
  echo:
  echo or
  echo:
  echo r configs\example.config.json
  goto :end
)

echo %1 | FINDSTR /C:"configs\\" >nul & if errorlevel 1 (
  set CONFIG=configs/%1
) else (
  set CONFIG=%1
)


if exist "%KITE_JAR%" (
  echo java -Dkite.firefox.profile="%KITE_HOME%/third_party/" -cp "%KITE_JAR%;target/*" org.webrtc.kite.Engine %CONFIG%
  java -Dkite.firefox.profile="%KITE_HOME%"/third_party/ -cp "%KITE_JAR%;target/*" org.webrtc.kite.Engine %CONFIG%
) else (
  echo "File not found: %KITE_JAR%"
  echo "Please edit this file and set KITE_JAR to the correct path."
)

:end