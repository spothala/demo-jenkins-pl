package com.example.demo.handler

import static com.example.demo.logger.Echoer.*

class OpsPipeline {
  def script          // Pipeline Script

  OpsPipeline(def script) {
    this.script = script
  }

  void trigger(Closure body) {
    def containerTemplates = [
      // Deployment Tools
      script.steps.containerTemplate(
        name: 'docker',
        image: 'docker',
        alwaysPullImage: true,
        ttyEnabled: true,
        command: 'cat',
      ),

      script.steps.containerTemplate(
        name: 'kubectl',
        image: 'lachlanevenson/k8s-kubectl:v1.16.3',
        alwaysPullImage: true,
        ttyEnabled: true,
        command: 'cat',
      ),
    ]
    def volumeMounts = [
      script.steps.emptyDirVolume(
        mountPath: '/var/lib/docker', 
        memory: false
      ),

      script.steps.hostPathVolume(
        hostPath: '/var/run/docker.sock',
        mountPath: '/var/run/docker.sock'
      ),
    ]
    def label = "worker-${UUID.randomUUID().toString()}"
    script.steps.podTemplate(
        label: label,
        containers: containerTemplates,
        volumes: volumeMounts
      ) {
        script.steps.node(label) {
          try {
            script.steps.stage('Perform Closure') {
              body()
            }
          } catch(GroovyRuntimeException e) {
            throw e
          }
          script.steps.stage('Build'){
            script.steps.container('docker') {
                script.steps.checkout changelog: false, poll: false, scm: [
                  $class: 'GitSCM', branches: [[name: script.scm.branches[0].name]],
                  doGenerateSubmoduleConfigurations: false, extensions: [[
                    $class: 'WipeWorkspace'
                  ]], submoduleCfg: [],
                  userRemoteConfigs: [[
                    url: script.scm.userRemoteConfigs[0].url
                  ]]
                ]
                script.steps.sh """#!/bin/sh -x
                ls -altr

                set -eo pipefail
                docker build -t hello:3.0 .
                """
            }
          }

          script.steps.stage('Deploy'){
            script.steps.container('kubectl') {
                script.steps.checkout changelog: false, poll: false, scm: [
                  $class: 'GitSCM', branches: [[name: script.scm.branches[0].name]],
                  doGenerateSubmoduleConfigurations: false, extensions: [[
                    $class: 'WipeWorkspace'
                  ]], submoduleCfg: [],
                  userRemoteConfigs: [[
                    url: script.scm.userRemoteConfigs[0].url
                  ]]
                ]
                script.steps.sh """#!/bin/sh -x
                set -eo pipefail
                kubectl apply -f k8s.yaml
                kubectl get pods
                kubectl get svc
                """
            }
          }
        }
      }
  }

}