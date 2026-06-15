# ============================================================
#  CMS Manager - Full Auto Setup & Run Script
#  Double-click this file or right-click > Run with PowerShell
# ============================================================

$ErrorActionPreference = "Continue"
$Host.UI.RawUI.WindowTitle = "CMS Manager Setup"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  CMS + SEO Manager - Auto Setup & Launch" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ── Paths ─────────────────────────────────────────────────
$JAVA_HOME  = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$MVN_HOME   = "C:\Users\$env:USERNAME\maven\apache-maven-3.9.6"
$PROJECT    = "C:\Users\$env:USERNAME\Desktop\kiro"

$env:JAVA_HOME = $JAVA_HOME
$env:Path = "$JAVA_HOME\bin;$MVN_HOME\bin;" + $env:Path

# ── Step 1: Verify Java ───────────────────────────────────
Write-Host "[1/5] Java..." -NoNewline -ForegroundColor Yellow
$jv = & java -version 2>&1 | Select-Object -First 1
Write-Host " $jv" -ForegroundColor Green

# ── Step 2: Verify Maven ──────────────────────────────────
Write-Host "[2/5] Maven..." -NoNewline -ForegroundColor Yellow
$mv = & mvn -version 2>&1 | Select-Object -First 1
Write-Host " $mv" -ForegroundColor Green

# ── Step 3: Start MySQL (try all known service names) ─────
Write-Host "[3/5] Starting MySQL service..." -ForegroundColor Yellow
$mysqlServices = @("MySQL84","MySQL80","MySQL81","MySQL","mysql")
$mysqlStarted  = $false
foreach ($svc in $mysqlServices) {
    $s = Get-Service -Name $svc -ErrorAction SilentlyContinue
    if ($s) {
        if ($s.Status -ne "Running") {
            Start-Service $svc -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 3
        }
        $s = Get-Service -Name $svc -ErrorAction SilentlyContinue
        if ($s.Status -eq "Running") {
            Write-Host "      MySQL service '$svc' is Running" -ForegroundColor Green
            $mysqlStarted = $true
            break
        }
    }
}

# Also try XAMPP MySQL
if (-not $mysqlStarted) {
    if (Test-Path "C:\xampp\mysql_start.bat") {
        Write-Host "      Trying XAMPP MySQL..." -ForegroundColor Yellow
        Start-Process "C:\xampp\mysql_start.bat" -WindowStyle Hidden
        Start-Sleep -Seconds 5
        $mysqlStarted = $true
    }
}

if (-not $mysqlStarted) {
    Write-Host "      WARNING: Could not auto-start MySQL." -ForegroundColor Red
    Write-Host "      Please start MySQL manually, then press Enter to continue..." -ForegroundColor Red
    Read-Host
}

# ── Step 4: Create database ───────────────────────────────
Write-Host "[4/5] Creating database..." -ForegroundColor Yellow

# Find mysql.exe
$mysqlExe = $null
$searchPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe"
)
foreach ($p in $searchPaths) {
    if (Test-Path $p) { $mysqlExe = $p; break }
}
if (-not $mysqlExe) {
    $found = Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter "mysql.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $mysqlExe = $found.FullName }
}

if ($mysqlExe) {
    Write-Host "      mysql.exe: $mysqlExe" -ForegroundColor Gray
    # Try password "root"
    $result = & "$mysqlExe" -u root "-proot" -e "CREATE DATABASE IF NOT EXISTS cms_seo_db CHARACTER SET utf8mb4;" 2>&1
    if ($LASTEXITCODE -ne 0) {
        # Try empty password
        $result = & "$mysqlExe" -u root -e "CREATE DATABASE IF NOT EXISTS cms_seo_db CHARACTER SET utf8mb4;" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "      DB created (empty password). Updating application.properties..." -ForegroundColor Green
            $propsFile = "$PROJECT\src\main\resources\application.properties"
            (Get-Content $propsFile) -replace "spring.datasource.password=.*", "spring.datasource.password=" | Set-Content $propsFile
        } else {
            Write-Host "      Could not auto-create DB. Please run manually:" -ForegroundColor Red
            Write-Host "      CREATE DATABASE cms_seo_db;" -ForegroundColor White
        }
    } else {
        Write-Host "      Database 'cms_seo_db' ready." -ForegroundColor Green
    }
} else {
    Write-Host "      mysql.exe not found — skipping auto DB creation." -ForegroundColor Red
    Write-Host "      Please run this in MySQL: CREATE DATABASE cms_seo_db;" -ForegroundColor White
    Write-Host "      Then press Enter to continue..." -ForegroundColor White
    Read-Host
}

# ── Step 5: Run Spring Boot ───────────────────────────────
Write-Host ""
Write-Host "[5/5] Starting Spring Boot..." -ForegroundColor Yellow
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Admin  Panel : http://localhost:8080/admin" -ForegroundColor White
Write-Host "  Website      : http://localhost:8080" -ForegroundColor White
Write-Host "  Login        : admin / admin123" -ForegroundColor White
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Wait ~30 seconds for startup. Press Ctrl+C to stop." -ForegroundColor Gray
Write-Host ""

Set-Location $PROJECT
& mvn spring-boot:run

Write-Host ""
Write-Host "Application stopped. Press Enter to exit."
Read-Host
