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
            script {
                withBuildUser  {
                    notifyTelegram("✅ SUCCESS", env.BUILD_USER, currentBuild.duration)
                }
            }
        }
        failure {
            script {
                withBuildUser  {
                    notifyTelegram("❌ FAILURE", env.BUILD_USER, currentBuild.duration)
                }
            }
        }
        always {
                sh 'docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"'
        }
    }
}


def notifyTelegram(String status, String user, long durationMs) {

    def branch =
        env.BRANCH_NAME ?:
        (env.GIT_BRANCH ? env.GIT_BRANCH.replaceFirst(/^origin\//, '') : 'unknown')

    def job   = env.JOB_NAME ?: 'unknown'
    def build = env.BUILD_NUMBER ?: 'unknown'
    def url   = env.BUILD_URL ?: ''

    long durationSec = durationMs.intdiv(1000)

    def durationStr = durationSec >= 60
        ? "${durationSec.intdiv(60)}m ${durationSec % 60}s"
        : "${durationSec}s"

    withCredentials([
        string(credentialsId: 'telegram-bot-token', variable: 'BOT_TOKEN'),
        string(credentialsId: 'telegram-chat-id', variable: 'CHAT_ID')
    ]) {
        withEnv([
            "TG_JOB=${job}",
            "TG_BRANCH=${branch}",
            "TG_USER=${user}",
            "TG_BUILD=${build}",
            "TG_STATUS=${status}",
            "TG_DURATION=${durationStr}",
            "TG_URL=${url}"
        ]) {
            sh '''
              MESSAGE=$(printf "🚀 *%s*\nBranch: *%s*\nStarted by: *%s*\nBuild #%s\nDuration: *%s*\nStatus: *%s*\n%s" \
                "$TG_JOB" \
                "$TG_BRANCH" \
                "$TG_USER" \
                "$TG_BUILD" \
                "$TG_DURATION" \
                "$TG_STATUS" \
                "$TG_URL")

              curl -s -X POST "https://api.telegram.org/bot$BOT_TOKEN/sendMessage" \
                -d chat_id="$CHAT_ID" \
                --data-urlencode text="$MESSAGE" \
                -d parse_mode=Markdown
            '''
        }
    }
}



