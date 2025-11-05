pipeline {
    agent any
    environment {
        MAVEN_LOCAL = '/root/.m2/repository'
    }

    stages {
        stage('Checkout Starter') {
            steps {
                checkout scm
            }
        }

        stage('Build & Publish to Maven Local') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean build publishToMavenLocal --no-daemon -Dmaven.repo.local=${MAVEN_LOCAL}
                    echo "Published to ${MAVEN_LOCAL}"
                '''
            }
        }
    }

    post {
        success { echo 'Starter built and published to Maven local' }
        failure { echo 'Starter build failed' }
    }
}
