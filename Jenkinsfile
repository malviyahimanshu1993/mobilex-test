pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
  }

  environment {
    // Used by test code (update your framework to read this if not already)
    APPIUM_SERVER_URL = 'http://localhost:4723'
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build docker images') {
      steps {
        sh 'docker version'
        sh 'docker compose version || true'
        sh 'docker build -t mobilex-test:ci .'
      }
    }

    stage('Start Appium on agent (host)') {
      steps {
        // Appium + emulator should run on the Windows agent for reliable ADB access.
        // This stage starts Appium if it is not already running.
        powershell '''
          $ErrorActionPreference = 'Stop'

          function Test-Url($url) {
            try {
              $resp = Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 -Uri $url
              return ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 300)
            } catch { return $false }
          }

          $statusUrl = 'http://127.0.0.1:4723/status'
          if (-not (Test-Url $statusUrl)) {
            Write-Host 'Appium not responding on 127.0.0.1:4723. Attempting to start Appium 2.x...'
            # Require node + appium installed on the agent: npm i -g appium@2
            Start-Process -FilePath 'appium' -ArgumentList '--address 0.0.0.0 --port 4723 --base-path /' -NoNewWindow
          } else {
            Write-Host 'Appium already running.'
          }

          # Wait for Appium to be ready
          $deadline = (Get-Date).AddSeconds(60)
          while ((Get-Date) -lt $deadline) {
            if (Test-Url $statusUrl) {
              Write-Host 'Appium is ready.'
              exit 0
            }
            Start-Sleep -Seconds 2
          }
          throw 'Timed out waiting for Appium /status on 127.0.0.1:4723'
        '''
      }
    }
