/*
Perquisites before this can be run in a machine ( irrespective of whether it run using Jenkins):
1. Install boto
2. Configure AWS KEYS
3. Configure ssh to enable shh thorugh bastion
*/


stage("Installing ag-bie") {
node{
checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/ag-bie-infra.git']]])

checkout([$class: 'GitSCM', branches: [[name: '*/DAWR_reskin']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/ag-bie.git']]])

checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ala-install']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/AtlasOfLivingAustralia/ala-install.git']]])

checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ala-config']], submoduleCfg: [], userRemoteConfigs: [[url: '/var/lib/jenkins/nxl-private']]])

 dir('ag-bie-infra'){    
    def extra_vars = /'{"env_name":"$ENVIRONMENT_NAME"}'/
    /*sh "ansible-playbook -vvv playbooks/infra.yml  -e $extra_vars"*/
 }
}
}