@echo off
title CMS Manager

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "MVN=C:\Users\Vishal Sharma\maven\apache-maven-3.9.6\bin\mvn.cmd"
set "PATH=%JAVA_HOME%\bin;C:\Users\Vishal Sharma\maven\apache-maven-3.9.6\bin;%PATH%"

echo Java Home : %JAVA_HOME%
echo.
"%JAVA_HOME%\bin\java.exe" -version
echo.

echo Starting CMS App (H2 mode - no MySQL needed)...
echo.
echo   Admin   : http://localhost:8080/admin
echo   Website : http://localhost:8080
echo   Login   : admin / admin123
echo.

cd /d "C:\Users\Vishal Sharma\Desktop\kiro"
"%MVN%" spring-boot:run -Dspring-boot.run.profiles=h2

pause
