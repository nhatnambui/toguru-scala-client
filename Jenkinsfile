loadLibrary "as24-fizz-community-library@v0.13.0"

pipeline {
    agent { node { label 'build-docker' } }

    options {
        timestamps()
        timeout(time: 2, unit: 'HOURS')
        buildDiscarder(logRotator(daysToKeepStr: '90'))
        preserveStashes(buildCount: 50)
    }

    stages {

        stage('Build') {
            steps {
                script {
                    dockerfile('Dockerfile.build').inside {
                        fast {
                            caching('~/.sbt', '~/.ivy2/cache', '~/.cache/coursier') {
                                sh 'scripts/build.sh'
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
                    branch 'main'
                    tag 'v*'
                }
            }
            steps {
                script {
                    dockerfile('Dockerfile.build').inside {
                        caching('~/.sbt', '~/.ivy2/cache', '~/.cache/coursier') {
                            fast {
                                sh 'scripts/publish.sh'
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
                if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                    slackSend channel: 'as24-product-platform', color: 'danger',
                              message: "The pipeline <${env.BUILD_URL}|${currentBuild.fullDisplayName}> failed."
                }
            }
        }
    }
}