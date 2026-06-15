@echo off
title CMS Manager - Startup
color 0A

echo ================================================
echo   CMS + SEO Manager - Auto Startup
echo ================================================
echo.

:: ── Set Java and Maven paths ──────────────────────
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set MAVEN_HOME=C:\Users\Vishal Sharma\maven\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

:: ── Verify Java ───────────────────────────────────
echo [1/4] Checking Java...
java -version 2>&1 | findstr /i "version"
if errorlevel 1 (
    echo ERROR: Java not found at %JAVA_HOME%
    pause & exit /b 1
)
echo       Java OK

:: ── Verify Maven ─────────────────────────────────
echo [2/4] Checking Maven...
mvn -version 2>&1 | findstr /i "Maven"
if errorlevel 1 (
    echo ERROR: Maven not found at %MAVEN_HOME%
    pause & exit /b 1
)
echo       Maven OK

:: ── Start MySQL service ──────────────────────────
echo [3/4] Starting MySQL service...
net start MySQL84 2>&1 | findstr /v "^$"
if errorlevel 2 (
    net start MySQL80 2>&1 | findstr /v "^$"
)
if errorlevel 2 (
    net start MySQL 2>&1 | findstr /v "^$"
)
echo       MySQL started (or already running)

:: ── Find MySQL bin and create database ──────────
echo [4/4] Setting up database...
set MYSQL_BIN=
for /d %%d in ("C:\Program Files\MySQL\MySQL Server 8.*") do (
    if exist "%%d\bin\mysql.exe" set MYSQL_BIN=%%d\bin
)
if "%MYSQL_BIN%"=="" (
    echo       WARNING: mysql.exe not found, skipping DB creation
    echo       Please manually run: CREATE DATABASE cms_seo_db;
) else (
    echo       Found MySQL at %MYSQL_BIN%
    "%MYSQL_BIN%\mysql.exe" -u root --password=root -e "CREATE DATABASE IF NOT EXISTS cms_seo_db;" 2>&1
    if errorlevel 1 (
        echo       Trying with empty password...
        "%MYSQL_BIN%\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS cms_seo_db;" 2>&1
    )
    echo       Database ready
)

echo.
echo ================================================
echo   Starting Spring Boot Application...
echo   Admin  : http://localhost:8080/admin
echo   Website: http://localhost:8080
echo   Login  : admin / admin123
echo ================================================
echo.

:: ── Run Spring Boot ───────────────────────────────
cd /d "%~dp0"
mvn spring-boot:run

pause
