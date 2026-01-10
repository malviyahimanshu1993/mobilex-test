pipeline {
    agent any

    options {
        ansiColor('xterm')
        timestamps()
    }

    parameters {
        choice(
            name: 'ENV',
            choices: ['docker', 'local'],
            description: 'Execution environment: docker (runs in container) or local (direct Maven)'
        )
    }

    environment {
        // Appium URL for Docker to reach Windows host
        APPIUM_SERVER_URL = "http://host.docker.internal:4723"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Tests - Docker') {
            when {
                expression { params.ENV == 'docker' }
            }
            steps {
                script {
                    echo "╔══════════════════════════════════════════════════════════════╗"
                    echo "║  Running tests in DOCKER mode                                ║"
                    echo "║  Appium Server: ${APPIUM_SERVER_URL}                         ║"
                    echo "╚══════════════════════════════════════════════════════════════╝"

                    bat '''
                        docker build -t mobilex-test:ci .
                        docker run --rm ^
                            --add-host=host.docker.internal:host-gateway ^
                            -e APPIUM_SERVER_URL=%APPIUM_SERVER_URL% ^
                            -e ENV=docker ^
                            -v "%CD%\\target":/workspace/target ^
                            -w /workspace ^
                            mobilex-test:ci mvn test -Denv=docker
                    '''
                }
            }
        }

        stage('Run Tests - Local') {
            when {
                expression { params.ENV == 'local' }
            }
            steps {
                script {
                    echo "╔══════════════════════════════════════════════════════════════╗"
                    echo "║  Running tests in LOCAL mode                                 ║"
                    echo "║  Appium Server: http://127.0.0.1:4723                        ║"
                    echo "╚══════════════════════════════════════════════════════════════╝"

                    bat 'mvn test -Denv=local'
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/allure-results/**/*', allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/reports/**/*', allowEmptyArchive: true
        }
    }
}