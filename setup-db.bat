@echo off
title Database Setup
color 0B

echo ================================================
echo   CMS Manager - Database Setup
echo ================================================
echo.

:: Find MySQL bin
set MYSQL_BIN=
for /d %%d in ("C:\Program Files\MySQL\MySQL Server 8.*") do (
    if exist "%%d\bin\mysql.exe" set MYSQL_BIN=%%d\bin
)

if "%MYSQL_BIN%"=="" (
    echo ERROR: MySQL not found. Please install MySQL first.
    echo Download: https://dev.mysql.com/downloads/installer/
    pause & exit /b 1
)

echo Found MySQL at: %MYSQL_BIN%
echo.

:: Try root with password "root"
echo Trying to connect as root (password: root)...
"%MYSQL_BIN%\mysql.exe" -u root -proot -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo Connected successfully!
    "%MYSQL_BIN%\mysql.exe" -u root -proot -e "CREATE DATABASE IF NOT EXISTS cms_seo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo Database cms_seo_db created.
    goto :done
)

:: Try root with no password
echo Trying root with no password...
"%MYSQL_BIN%\mysql.exe" -u root -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo Connected with no password!
    "%MYSQL_BIN%\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS cms_seo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo Database cms_seo_db created.
    echo.
    echo IMPORTANT: Update application.properties - set password to empty:
    echo   spring.datasource.password=
    goto :done
)

:: Ask for password
echo.
set /p MYSQL_PASS=Enter your MySQL root password: 
"%MYSQL_BIN%\mysql.exe" -u root -p%MYSQL_PASS% -e "CREATE DATABASE IF NOT EXISTS cms_seo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if not errorlevel 1 (
    echo Database created successfully!
    echo.
    echo IMPORTANT: Update this line in src\main\resources\application.properties:
    echo   spring.datasource.password=%MYSQL_PASS%
) else (
    echo ERROR: Could not connect to MySQL. Please check your password.
)

:done
echo.
echo ================================================
echo   Done! Now run:  run.bat
echo ================================================
pause
