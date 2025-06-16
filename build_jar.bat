@echo off
echo Building HabiTV executable JAR...

REM Build the executable JAR
call mvn clean package -f application\habiTv\pom.xml

if %ERRORLEVEL% neq 0 (
    echo Failed to build the executable JAR.
    exit /b %ERRORLEVEL%
)

echo.
echo Creating target directory if it doesn't exist...
if not exist target mkdir target

echo.
echo Copying JAR to convenience location...
copy application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar target\habiTv.jar

echo.
echo Build completed successfully!
echo.
echo The executable JAR is available at:
echo - Primary location: application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar
echo - Convenience location: target\habiTv.jar
echo.
echo To run in GUI mode:
echo java -jar target\habiTv.jar
echo.
echo To run in console mode:
echo java -jar target\habiTv.jar [arguments]
echo.