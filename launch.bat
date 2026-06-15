@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "MVN_CMD=C:\Users\Vishal Sharma\maven\apache-maven-3.9.6\bin\mvn.cmd"
set "PROJECT=C:\Users\Vishal Sharma\Desktop\kiro"

set "PATH=%JAVA_HOME%\bin;C:\Users\Vishal Sharma\maven\apache-maven-3.9.6\bin;%PATH%"

echo.
echo [CHECK] Java:
"%JAVA_EXE%" -version
echo.
echo [CHECK] Maven:
"%MVN_CMD%" -version
echo.
echo [START] Launching Spring Boot with H2 (no MySQL needed)...
echo.
echo   Admin   -^> http://localhost:8080/admin
echo   Website -^> http://localhost:8080
echo   Login   -^> admin / admin123
echo.

cd /d "%PROJECT%"
"%MVN_CMD%" spring-boot:run "-Dspring-boot.run.profiles=h2"

endlocal
pause
