@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "APP_ROOT=%%~fI"
set "LIB_DIR=%APP_ROOT%\lib"

where java >nul 2>&1
if errorlevel 1 (
  echo Java 8 with JavaFX support is required but "java" was not found on PATH.
  echo Install a JDK 8 distribution that includes JavaFX, for example Liberica JDK 8 Full.
  exit /b 1
)

set "HABITV_JAR="
for %%F in ("%LIB_DIR%\habiTv-*.jar") do set "HABITV_JAR=%%~fF"
if not defined HABITV_JAR (
  echo Habitv application JAR not found in %LIB_DIR%
  exit /b 1
)

java -jar "%HABITV_JAR%" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
