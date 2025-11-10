pipeline {
    agent any
    stages {
        stage('Load env from Jenkins credentials') {
                steps {
                    withCredentials([file(credentialsId: 'leasing-env', variable: 'ENV_FILE')]) {
                        script {
                            def envLines = readFile(ENV_FILE).split('\n')
                            envLines.each { line ->
                                if (line.trim() && !line.startsWith('#')) {
                                    def (key, value) = line.split('=', 2)
                                    env[key.trim()] = value.trim()
                                }
                            }

                            echo "NEXUS_URL = ${env.NEXUS_URL}"
                            echo "NEXUS_USER = ${env.NEXUS_USER}"
                            echo "NEXUS_PASS = ${env.NEXUS_PASS}"

                        }
                    }
                }
            }
        stage('Build & Publish Starter') {
            steps {
                sh '''
                  chmod +x gradlew
                  ./gradlew clean build publishMavenPublicationToNexusRepository --no-daemon
                '''
            }
        }
    }
    post {
        success {
            echo 'Starter published to Nexus'
        }
    }
}
