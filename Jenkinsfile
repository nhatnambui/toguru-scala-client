loadLibrary "as24-fizz-community-library@v0.11.0"
pipeline {
    agent { node { label 'build-docker' } }
    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(daysToKeepStr: '90'))
    }
    environment {
        FAST_TOKEN = getFastToken('as24')
        FAST_USER = getFastUser('as24')
    }
    stages {
        stage('Build') {
            steps {
                script {
                    dockerfile('Dockerfile.build').inside {
                        caching('~/.sbt', '~/.ivy2/cache', '~/.cache/coursier') {
                            fast {
                                sh "sbt formatCheck semVerCheck test"
                            }
                        }
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/target/test-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        stage('Publish') {
            when {
                anyOf {
                    branch 'master'
                    tag 'v*'
                }
            }
            steps {
                script {
                    dockerfile('Dockerfile.build').inside {
                        caching('~/.sbt', '~/.ivy2/cache', '~/.cache/coursier') {
                            fast {
                                sh "sbt publish"
                            }
                        }
                    }
                }
            }
        }
    }
     post {
        failure {
            script {
                if (env.BRANCH_NAME == 'master') {
                    slackSend channel: 'as24_cxp_eng_ret_web', color: 'danger',
                              message: "The pipeline <${env.BUILD_URL}|${currentBuild.fullDisplayName}> failed."
                }
            }
        }
    }
}