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
                script {
                    echo "Workspace contents:"
                    bat 'dir /s src\\test\\java\\com\\framework 2>nul | findstr /C:"pages" /C:"tests" /C:"base" || echo "Checking directory structure..."'
                    bat 'dir src\\test\\java\\com\\framework\\pages\\ 2>nul || echo "Pages directory check"'
                    bat 'dir src\\test\\java\\com\\framework\\tests\\ 2>nul || echo "Tests directory check"'
                }
            }
        }

        stage('Clean Docker') {
            steps {
                script {
                    echo "Cleaning old Docker images..."
                    bat 'docker rmi -f mobilex-test:ci 2>nul || echo "No previous image to remove"'
                }
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
                    echo "║  Build Context: %CD%                                         ║"
                    echo "╚══════════════════════════════════════════════════════════════╝"

                    bat '''
                        echo Listing source files before Docker build...
                        dir src\\test\\java\\com\\framework\\pages\\
                        dir src\\test\\java\\com\\framework\\tests\\

                        echo Building Docker image (no cache for clean build)...
                        docker build --no-cache -t mobilex-test:ci .

                        echo Running tests in Docker container...
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

                    bat 'mvn clean test -Denv=local'
                }
            }
        }
    }

    post {
        always {
            script {
                echo "═══════════════════════════════════════════════════════════════"
                echo "  Archiving test results..."
                echo "═══════════════════════════════════════════════════════════════"
            }
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/allure-results/**/*', allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/reports/**/*', allowEmptyArchive: true
        }
        failure {
            script {
                echo "╔══════════════════════════════════════════════════════════════╗"
                echo "║  BUILD FAILED                                                 ║"
                echo "║  Common causes:                                               ║"
                echo "║  1. Code not committed to git (use: git add -A && git commit) ║"
                echo "║  2. Appium not running on host                                ║"
                echo "║  3. Device not connected                                      ║"
                echo "║  4. Docker build cache issues                                 ║"
                echo "╚══════════════════════════════════════════════════════════════╝"
            }
        }
    }
}