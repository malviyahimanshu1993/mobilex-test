<#
.SYNOPSIS
    Run tests in Docker container (Appium on host)
.DESCRIPTION
    This script builds and runs tests in a Docker container.
    The container connects to Appium running on the host machine via host.docker.internal.
.EXAMPLE
    .\run-docker.ps1
    .\run-docker.ps1 -RebuildImage
#>

param(
    [switch]$RebuildImage = $false,
    [switch]$SkipBuild = $false
)

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║              MOBILEX TEST - DOCKER EXECUTION                 ║" -ForegroundColor Magenta
Write-Host "╠══════════════════════════════════════════════════════════════╣" -ForegroundColor Magenta
Write-Host "║  Environment  : DOCKER                                       ║" -ForegroundColor Magenta
Write-Host "║  Appium URL   : http://host.docker.internal:4723             ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""

# Check if Appium is running on host
try {
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:4723/status" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ Appium server is running on host" -ForegroundColor Green
} catch {
    Write-Host "✗ Appium server is not running at http://127.0.0.1:4723" -ForegroundColor Red
    Write-Host "  Docker container needs Appium running on the host machine." -ForegroundColor Yellow
    exit 1
}

# Check Docker
try {
    docker version | Out-Null
    Write-Host "✓ Docker is available" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not running or not installed" -ForegroundColor Red
    exit 1
}

# Build Docker image
if (-not $SkipBuild -or $RebuildImage) {
    Write-Host ""
    Write-Host "Building Docker image..." -ForegroundColor Yellow

    if ($RebuildImage) {
        docker build --no-cache -t mobilex-test:local .
    } else {
        docker build -t mobilex-test:local .
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Docker build failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ Docker image built successfully" -ForegroundColor Green
}

# Run tests in container
Write-Host ""
Write-Host "Running tests in Docker container..." -ForegroundColor Yellow
Write-Host ""

docker run --rm `
    --add-host=host.docker.internal:host-gateway `
    -e ENV=docker `
    -e APPIUM_SERVER_URL=http://host.docker.internal:4723 `
    -v "${PWD}\target:/workspace/target" `
    mobilex-test:local

# Show results
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Magenta
Write-Host "  Test execution complete. Check target/surefire-reports for results." -ForegroundColor Magenta
Write-Host "  Allure results: target/allure-results" -ForegroundColor Magenta
Write-Host "  HTML report: target/reports" -ForegroundColor Magenta
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Magenta

