pipeline {
    agent any

    options {
        ansiColor('xterm')
        timestamps()
    }

    environment {
        // Appium URL for Docker to reach Windows host
        APPIUM_SERVER_URL = "http://host.docker.internal:4723/"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t mobilex-test:ci .'
            }
        }

        stage('Run Tests (Docker)') {
            steps {
                script {
                    echo "Running tests against Appium at ${APPIUM_SERVER_URL}"

                    // We pass the Windows Current Directory (%CD%) into the container
                    // as a variable named 'HOST_WORKSPACE'
                    bat '''
                        docker run --rm ^
                        --add-host=host.docker.internal:host-gateway ^
                        -e APPIUM_SERVER_URL=%APPIUM_SERVER_URL% ^
                        -e HOST_WORKSPACE="%CD%" ^
                        -v "%CD%":/workspace ^
                        -w /workspace ^
                        mobilex-test:ci mvn -q test
                    '''
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
        }
    }
}