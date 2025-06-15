@echo off
echo Deploying Maven artifacts to GitHub Pages Maven repository...

REM Switch to gh-pages branch
git checkout gh-pages

REM Clean the repository directory
if exist repository rmdir /s /q repository

REM Create repository directory structure
mkdir repository
mkdir repository\com\dabi\habitv

REM Copy Maven artifacts from local repository
set LOCAL_REPO=%USERPROFILE%\.m2\repository\com\dabi\habitv
set GH_PAGES_REPO=repository\com\dabi\habitv

if exist "%LOCAL_REPO%" (
    xcopy /s /e /y "%LOCAL_REPO%\*" "%GH_PAGES_REPO%\"
    echo Copied Maven artifacts from %LOCAL_REPO%
    echo Repository structure created at: %GH_PAGES_REPO%
) else (
    echo Local Maven repository not found at %LOCAL_REPO%
    echo Please run 'mvn clean install' first to build the artifacts
    pause
    exit /b 1
)

REM Add all files to git
git add -A

REM Commit the changes
git commit -m "Deploy Maven artifacts for version 5.0.0-SNAPSHOT"

REM Push to GitHub Pages
git push origin gh-pages

REM Switch back to java8 branch
git checkout java8

echo.
echo Deployment completed!
echo Maven Repository URL: https://mika3578.github.io/habitv/repository/
echo Repository structure:
echo   - JAR files: repository/com/dabi/habitv/*/5.0.0-SNAPSHOT/*.jar
echo   - POM files: repository/com/dabi/habitv/*/5.0.0-SNAPSHOT/*.pom
echo   - Metadata: repository/com/dabi/habitv/*/maven-metadata.xml
pause 