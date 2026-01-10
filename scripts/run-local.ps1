<#
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  HTML report: target/reports" -ForegroundColor Cyan
Write-Host "  Allure results: target/allure-results" -ForegroundColor Cyan
Write-Host "  Test execution complete. Check target/surefire-reports for results." -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
# Show results

Invoke-Expression $mvnCmd
# Execute

Write-Host ""
Write-Host "Running: $mvnCmd" -ForegroundColor Yellow

}
    $mvnCmd += " -Dtest=$TestClass"
if ($TestClass -ne "") {

}
    $mvnCmd = "mvn clean test -Denv=local"
if (-not $SkipClean) {

$mvnCmd = "mvn test -Denv=local"
# Build Maven command

}
    exit 1
    Write-Host "  Please start Appium first: appium" -ForegroundColor Yellow
    Write-Host "✗ Appium server is not running at http://127.0.0.1:4723" -ForegroundColor Red
} catch {
    Write-Host "✓ Appium server is running" -ForegroundColor Green
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:4723/status" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
try {
# Check if Appium is running

Write-Host ""
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host "║  Appium URL   : http://127.0.0.1:4723                        ║" -ForegroundColor Cyan
Write-Host "║  Environment  : LOCAL                                        ║" -ForegroundColor Cyan
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║              MOBILEX TEST - LOCAL EXECUTION                  ║" -ForegroundColor Cyan
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host ""

)
    [switch]$SkipClean = $false
    [string]$TestClass = "",
param(

#>
    .\run-local.ps1 -TestClass "SecurityMattersTest"
    .\run-local.ps1
.EXAMPLE
    Make sure Appium is running before executing this script.
    This script runs the mobile tests using the local Appium server at http://127.0.0.1:4723
.DESCRIPTION
    Run tests locally (Appium on localhost)
.SYNOPSIS

