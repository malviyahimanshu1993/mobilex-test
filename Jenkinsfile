pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
  }

  environment {
    // Tests run in Docker must reach Appium running on the Jenkins agent (host).
    APPIUM_SERVER_URL = 'http://host.docker.internal:4723'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build docker image') {
      steps {
        // Prefer bat on Windows agents
        bat 'docker version'
        bat 'docker compose version'
        bat 'docker build -t mobilex-test:ci .'
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
              # Consider any HTTP response as reachable (2xx/3xx/4xx). We only need to know the server is up.
              return ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500)
            } catch {
              return $false
            }
          }

          # Prefer checking Appium on localhost first
          $statusUrl = 'http://127.0.0.1:4723/status'

          if (-not (Test-Url $statusUrl)) {
            Write-Host 'Appium not responding on 127.0.0.1:4723. Attempting to start Appium 2.x...'

            # Ensure Node/npm are installed on the agent
            $node = (Get-Command node -ErrorAction SilentlyContinue)
            $npx  = (Get-Command npx  -ErrorAction SilentlyContinue)
            if (-not $node -or -not $npx) {
              throw 'Node.js/npm (npx) not found on this agent. Install Node.js LTS and run: npm i -g appium@2'
            }

            # Start appium via npx to avoid broken global shim issues.
            $log = Join-Path $env:WORKSPACE 'appium-host.log'
            $cmd = "npx.cmd --yes appium@2 appium --address 0.0.0.0 --port 4723 --base-path /"

            Write-Host "Starting: $cmd"
            Write-Host "Logging to: $log"

            # Use cmd.exe so npx.cmd resolution is reliable under Jenkins.
            Start-Process -FilePath 'cmd.exe' -ArgumentList "/c $cmd 1>>\"$log\" 2>>&1" -WindowStyle Hidden
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

          throw 'Timed out waiting for Appium /status on 127.0.0.1:4723. Check appium-host.log in the workspace.'
        '''
      }
    }

    stage('Run tests (Docker)') {
      steps {
        // Run only tests in Docker; Appium runs on host.
        // APPIUM_SERVER_URL is passed to compose and mapped into the tests container.
        bat 'set APPIUM_SERVER_URL=%APPIUM_SERVER_URL% && docker compose up --abort-on-container-exit --exit-code-from tests'
      }
      post {
        always {
          bat 'docker compose down -v'
        }
      }
    }

    stage('Allure report') {
      when {
        expression { fileExists('target/allure-results') }
      }
      steps {
        // Requires Jenkins Allure plugin
        allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'target/surefire-reports/**, target/allure-results/**', allowEmptyArchive: true
      junit testResults: 'target/surefire-reports/junitreports/*.xml', allowEmptyResults: true
    }
  }
}
