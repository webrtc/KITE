@echo off
set mypath=%cd%
IF "%1"=="all" (
	cd ..
	mvn -DskipTests clean install %2 %3 %4 
	cd %mypath%
) else (
	mvn -DskipTests clean install %1 %2 %3 %4 
)


