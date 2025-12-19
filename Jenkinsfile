pipeline {
  agent any

  options {
    ansiColor('xterm')
    timestamps()
  }

  environment {
    // Appium runs manually on the Windows host. Docker tests reach it via host.docker.internal.
    APPIUM_SERVER_URL = 'http://host.docker.internal:4723'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build Docker Image') {
      steps {
        bat 'docker version'
        bat 'docker build -t mobilex-test:ci .'
      }
    }

    stage('Run Tests (Docker)') {
      steps {
        script {
          echo "Running tests against Appium at ${env.APPIUM_SERVER_URL}"
          // Run Maven tests inside the container.
          // Mount workspace so surefire reports are written to host for Jenkins to archive.
          // Note: host.docker.internal works on Docker Desktop Windows.
          bat '''
            docker run --rm ^
              -e APPIUM_SERVER_URL=%APPIUM_SERVER_URL% ^
              -v "%CD%\target":/workspace/target ^
              mobilex-test:ci mvn -q test
          '''
        }
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
