@echo off

if not defined KITE_HOME (
  echo KITE_HOME is not defined. Please run configure.bat or manually add KITE_HOME to your environment variables.
  goto :end
)


if [%1] == [] (
  echo Usage:
  echo:
  echo init testname
  goto :end
)

java -cp %KITE_HOME%\scripts\init\init.zip Init %1

:end