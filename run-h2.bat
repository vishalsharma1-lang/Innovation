@echo off
title CMS Manager (H2 Mode - No MySQL needed)
color 0E

echo ================================================
echo   CMS Manager - H2 Mode (No MySQL required)
echo   Data resets on restart. Use run.bat for MySQL.
echo ================================================
echo.

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set MAVEN_HOME=C:\Users\%USERNAME%\maven\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Java:  
java -version 2>&1 | findstr "version"
echo Maven: 
mvn -version 2>&1 | findstr "Maven"
echo.
echo Starting app with H2 in-memory database...
echo.
echo ================================================
echo   Admin  : http://localhost:8080/admin
echo   Website: http://localhost:8080
echo   Login  : admin / admin123
echo   H2 DB  : http://localhost:8080/h2-console
echo ================================================
echo.

cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.profiles=h2

pause
