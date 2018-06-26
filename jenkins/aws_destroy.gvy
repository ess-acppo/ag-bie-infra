/*
Perquisites before this can be run in a machine ( irrespective of whether it run using Jenkins):
1. Install boto
2. Configure AWS KEYS
3. Configure ssh to enable shh thorugh bastion
*/

stage("Delete environment") {
node{
checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/collections-infra.git']]])
 dir('ag-bie-infra'){
    def extra_vars = /'{"env_name":"$ENVIRONMENT_NAME","VPC_ID":"$VPC_ID","INSTANCE_ID":"$INSTANCE_ID"}'/
    sh "ansible-playbook -vvv playbooks/infra_destroy.yml  -e $extra_vars"
  }
 }
}