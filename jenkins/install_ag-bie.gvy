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

checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-config']], submoduleCfg: [], userRemoteConfigs: [[url: '/var/lib/jenkins/nxl-private']]])

 dir('ag-bie/ansible'){
     sh "export env_name=$ENVIRONMENT_NAME && echo $env_name"
     sh "export env_name_us=$(echo $env_name |sed -e 's/-/_/g') && echo $env_name_us"
     sh "export agbie_private_dns_name=$(/var/lib/jenkins/workspace/Agbie_Install_Step1/ag-bie-infra/aws_utils/ec2.py --list |jq -r ._meta.hostvars.$env_name_us.ansible_host) && echo $agbie_private_dns_name"
echo $TARGET_SERVER_NAME
    def extra_vars = /'{"env_name":"$ENVIRONMENT_NAME","":""}'/
    /*sh "ansible-playbook -vvv playbooks/infra.yml  -e $extra_vars"*/
 }
}
}