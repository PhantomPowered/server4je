pipeline {
    agent any

    tools {
        jdk "11"
    }

    options {
        buildDiscarder logRotator(numToKeepStr: '10')
    }

    environment {
        PROJECT_VERSION = getProjectVersion().replace("-SNAPSHOT", "");
        IS_SNAPSHOT = getProjectVersion().endsWith("-SNAPSHOT");
    }

    stages {
        stage('Update snapshot version') {
            when {
                allOf {
                    environment name:'IS_SNAPSHOT', value: 'true'
                }
            }

            steps {
                sh 'mvn versions:set -DnewVersion="${PROJECT_VERSION}.${BUILD_NUMBER}-SNAPSHOT"';
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean';
            }
        }

        stage('Build') {
            steps {
                sh 'mvn package';
            }
        }

        stage('Verify') {
            steps {
                sh 'mvn verify';
            }
        }

        stage('Deploy release') {
            when {
                allOf {
                    branch 'master'
                    environment name:'IS_SNAPSHOT', value: 'false'
                }
            }

            steps {
                echo "Deploy new release...";
                sh 'mvn clean deploy -P deploy';
            }
        }

        stage('Prepare zip') {
            steps {
                sh "rm -rf server.zip";
                sh "mkdir -p results";
                sh "cp -r .templates/* results/";
                sh "cp launcher/target/launcher.jar results/launcher.jar";

                zip archive: true, dir: 'results', glob: '', zipFile: 'server.zip';

                sh "rm -rf results/";
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'server.zip'
                archiveArtifacts artifacts: 'launcher/target/launcher.jar'
            }
        }
    }

    post {
        always {
            withCredentials([string(credentialsId: 'discord-webhook', variable: 'url')]) {
                discordSend description: 'New build of server4je', footer: 'Update', link: BUILD_URL, successful: currentBuild.resultIsBetterOrEqualTo('SUCCESS'), title: JOB_NAME, webhookURL: url
            }
        }

        success {
            junit allowEmptyResults: true, testResults: 'server/target/surefire-reports/TEST-*.xml'
            junit allowEmptyResults: true, testResults: 'api/target/surefire-reports/TEST-*.xml'
            junit allowEmptyResults: true, testResults: 'natives/target/surefire-reports/TEST-*.xml'
            junit allowEmptyResults: true, testResults: 'launcher/target/surefire-reports/TEST-*.xml'
        }
    }
}

def getProjectVersion() {
  return sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout  | tail -1", returnStdout: true)
}