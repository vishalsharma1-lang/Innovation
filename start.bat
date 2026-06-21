@echo off
:: Fix Windows UDS socket bug with Java NIO
if not exist C:\tmp mkdir C:\tmp
set TEMP=C:\tmp
set TMP=C:\tmp

echo ==========================================
echo  Starting CarDekho Deals Platform
echo ==========================================

:: ── 1. Build CarDekho Deals JAR if needed ─────────────────────────────────
if not exist target\cms-seo-manager-1.0.0.jar (
    echo [1/3] Building CarDekho Deals...
    mvn package -DskipTests -q
)

:: ── 2. Build API Testcase Validator backend JAR if needed ─────────────────
set API_PORTAL_DIR=C:\Users\Vishal Sharma\Desktop\API TestCases\api-test-portal\backend
set API_PORTAL_JAR=%API_PORTAL_DIR%\target\api-test-portal-1.0.0.jar
if not exist "%API_PORTAL_JAR%" (
    echo [2/3] Building API Testcase Validator backend...
    pushd "%API_PORTAL_DIR%"
    mvn package -DskipTests -q
    popd
)

:: ── 3. Start CarDekho Deals (port 8080) ───────────────────────────────────
echo [1/3] Starting CarDekho Deals on http://localhost:8080 ...
start "CarDekho Deals" cmd /k "set TEMP=C:\tmp && set TMP=C:\tmp && java -jar target\cms-seo-manager-1.0.0.jar"

:: ── 4. Start API Testcase Validator backend (port 8081) ───────────────────
echo [2/3] Starting API Testcase Validator backend on http://localhost:8081 ...
start "API Portal Backend" cmd /k "set TEMP=C:\tmp && set TMP=C:\tmp && java -jar \"%API_PORTAL_JAR%\""

:: ── 5. Start API Testcase Validator frontend (port 4200) ──────────────────
echo [3/3] Starting API Testcase Validator frontend on http://localhost:4200 ...
set FRONTEND_DIR=C:\Users\Vishal Sharma\Desktop\API TestCases\api-test-portal\frontend
start "API Portal Frontend" cmd /k "cd /d \"%FRONTEND_DIR%\" && npm run dev"

:: ── 6. Wait for services to boot then open browser tabs ───────────────────
echo.
echo Waiting for services to start...
timeout /t 15 /nobreak > nul

echo Opening browser tabs...
start "" "http://localhost:8080"
start "" "http://localhost:8080/admin/login"
start "" "http://localhost:8080/fuel-calculator"
start "" "http://localhost:4200"

echo.
echo ==========================================
echo  All services started!
echo  CarDekho Deals    : http://localhost:8080
echo  Admin Panel       : http://localhost:8080/admin/login
echo  Fuel Cost Calc    : http://localhost:8080/fuel-calculator
echo  API Test Validator: http://localhost:4200
echo ==========================================
echo.
echo Press any key to close this window (services keep running)
pause > nul
