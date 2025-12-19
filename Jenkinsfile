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

    stage('Run test stack (Appium + tests)') {
      steps {
        // Starts Appium, then runs the test container. Results are written to ./target via volume.
        sh 'docker compose up --abort-on-container-exit --exit-code-from tests'
      }
      post {
        always {
          sh 'docker compose down -v || true'
        }
      }
    }

    stage('Allure report') {
      steps {
        // If you have Jenkins Allure plugin installed
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

