@echo off
@REM Maven Wrapper Script for Windows

setlocal
set MAVEN_VERSION=3.9.9
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\maven-%MAVEN_VERSION%
set MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd

if not exist "%MAVEN_BIN%" (
    echo Downloading Maven %MAVEN_VERSION%...
    mkdir "%MAVEN_HOME%" 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%USERPROFILE%\.m2\wrapper\maven-%MAVEN_VERSION%.zip'"
    powershell -Command "Expand-Archive -Path '%USERPROFILE%\.m2\wrapper\maven-%MAVEN_VERSION%.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\' -Force"
    move "%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%\*" "%MAVEN_HOME%\"
    del "%USERPROFILE%\.m2\wrapper\maven-%MAVEN_VERSION%.zip"
    rmdir /s /q "%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%"
)

@REM Run Maven with all arguments
call "%MAVEN_BIN%" %*