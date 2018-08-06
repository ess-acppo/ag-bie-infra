/*
Perquisites before this can be run in a machine ( irrespective of whether it run using Jenkins):
1. Install boto
2. Configure AWS KEYS
3. Configure ssh to enable shh thorugh bastion
*/


stage("Clean And Checkout Repositories") {
    node{
        sh 'touch test.file && rm -rf *'

        checkout([$class: 'GitSCM', branches: [[name: '*/ag-master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/ag-bie-infra.git']]])

        checkout([$class: 'GitSCM', branches: [[name: '*/DAWR_reskin']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/ag-bie.git']]])

        checkout([$class: 'GitSCM', branches: [[name: 'be0b2fa77f71530a040d97af134900990b5e4c3a']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ala-install']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/AtlasOfLivingAustralia/ala-install.git']]])

        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-config']], submoduleCfg: [], userRemoteConfigs: [[url: '/var/lib/jenkins/nxl-private']]])
    }
}

stage("Prepare For Installation") {
    node {
        dir('ag-bie/ansible') {
            def build_env = { 

            }
            sh 'echo "Step 1"'
            def env_name = "$ENVIRONMENT_NAME"
            sh 'echo "Step 2"'
            println "env_name: ${env_name}"
            def env_pub_hostname = "$env_name" + '.oztaxa.com'
            println "env_name: ${env_pub_hostname}"
            sh 'echo "Step 3"'
            def env_pvt_hostname = sh 'echo $(aws ec2 describe-instances --filter "Name=tag:env,Values=$ENVIRONMENT_NAME" | jq -r ".Reservations[0].Instances[0].PrivateDnsName")'
            sh 'echo "Step 4"'
            println "env_name: ${env_pvt_hostname}"
            sh 'echo "Step 5"'
            sh 'cp ../../ag-bie-config/ag-bie/agbie-inv.yml agbie-inv.yml'
            sh 'cat agbie-inv.yml'
        }
    }
}

/*
stage("Installing ag-bie") {
    node{
        dir('ag-bie/ansible'){
            sh "export env_name=$ENVIRONMENT_NAME && echo $env_name"
            sh "export env_name_us=${echo $env_name |sed -e 's/-/_/g'} && echo $env_name_us"
            sh "export agbie_private_dns_name=${"/var/lib/jenkins/workspace/Agbie_Install_Step1/ag-bie-infra/aws_utils/ec2.py --list |jq -r ._meta.hostvars.$env_name_us.ansible_host"} && echo $agbie_private_dns_name"
        echo $TARGET_SERVER_NAME
            def extra_vars = /'{"env_name":"$ENVIRONMENT_NAME","":""}'/
            sh "ansible-playbook -vvv playbooks/infra.yml  -e $extra_vars"
        }
    }
}
*/