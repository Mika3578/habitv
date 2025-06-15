@echo off
echo Deploying Maven artifacts to GitHub Pages repository...

REM Switch to gh-pages branch
git checkout gh-pages

REM Clean the repository directory
if exist repository rmdir /s /q repository

REM Create repository directory structure
mkdir repository
mkdir repository\com
mkdir repository\com\dabi
mkdir repository\com\dabi\habitv

REM Copy Maven artifacts from local repository
xcopy /s /e /y "%USERPROFILE%\.m2\repository\com\dabi\habitv\*" "repository\com\dabi\habitv\"

REM Add all files to git
git add -A

REM Commit the changes
git commit -m "Deploy Maven artifacts for version 5.0.0-SNAPSHOT"

REM Push to GitHub Pages
git push origin gh-pages

REM Switch back to java8 branch
git checkout java8

echo Deployment completed!
echo Repository URL: https://mika3578.github.io/habitv/repository/
pause 