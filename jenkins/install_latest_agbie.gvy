/*
Perquisites before this can be run in a machine ( irrespective of whether it run using Jenkins):
1. Install boto
2. Configure AWS KEYS
3. Configure ssh to enable shh thorugh bastion
4. Install and configure AWS CLI
*/
node {
    def git_tag_agbie_infra = '*/ag-master-idem'
    def git_tag_agbie = '*/bie-upgrade'
    def git_tag_alainstall = 'ag-bie-install'
    def git_tag_nxlprivate = '*/master'

    stage("Clean ws & checkout repositories") {
        slackSend color: 'good', message: "ag-bie Job Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"

        sh 'touch test.file && rm -rf *'
        checkout([$class: 'GitSCM', branches: [[name: "${git_tag_agbie_infra}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/ag-bie-infra.git']]])
        checkout([$class: 'GitSCM', branches: [[name: "${git_tag_agbie}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/ag-bie.git']]])
        checkout([$class: 'GitSCM', branches: [[name: "${git_tag_alainstall}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ala-install']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/AtlasOfLivingAustralia/ala-install.git']]])
        checkout([$class: 'GitSCM', branches: [[name: "${git_tag_nxlprivate}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'ag-bie-config']], submoduleCfg: [], userRemoteConfigs: [[url: '/var/lib/jenkins/nxl-private']]])
        slackSend color: 'good', message: "ag-bie Checkout Stage complete... ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
    }

    stage("Build war file for ag-bie env: $ENVIRONMENT_NAME") {
            dir('ag-bie') {
                slackSend color: 'good', message: "ag-bie Build WAR stage Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
                // sh './gradlew dependencies && ./gradlew assemble && find ./ -name "*.war"'
                sh 'mkdir -p build/libs/'
                sh 'cp ~/builds/ag-bie-0.5-SNAPSHOT.war build/libs/ag-bie-0.5-SNAPSHOT.war'
                slackSend color: 'good', message: "ag-bie WAR File Built... ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
            }
    }

    stage("Installing ag-bie for env: $ENVIRONMENT_NAME") {
        dir('ag-bie/ansible') {
            slackSend color: 'good', message: "ag-bie Prepare stage Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"

            def env_name = "$ENVIRONMENT_NAME"
            def env_pub_hostname = "$env_name" + '.oztaxa.com'
            sh 'echo $(aws ec2 describe-instances --filter "Name=tag:env,Values=$ENVIRONMENT_NAME" | jq -r ".Reservations[0].Instances[0].PrivateDnsName") > pvt-dns-name.txt'
            def env_pvt_hostname = ""
            println "Standalone Value: $STANDALONE"
            if ("$STANDALONE" == "true") {
                env_pvt_hostname = env_pub_hostname
            } else {
                env_pvt_hostname = readFile('pvt-dns-name.txt').trim()
            }
            println "pub_env_name: ${env_pub_hostname}"
            println "private_env_name: ${env_pvt_hostname}"
            sh 'rm -rf pvt-dns-name.txt'
            sh 'cp ../../ag-bie-config/ag-bie/agbie-inv.yml agbie-inv.yml'
            sh "sed -ie 's/agbie_servername_variable_here/${env_pub_hostname}/g' agbie-inv.yml"
            sh "sed -ie 's/agbie_hostname_variable_here/${env_pvt_hostname}/g' agbie-inv.yml"
            //sh 'cat agbie-inv.yml'
            slackSend color: 'good', message: "ag-bie Running Install Playbook ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
            //sh 'cp ../../ag-bie-config/ag-bie/ag-bie.yml.template ag-bie.yml'
            sh 'ansible-playbook -i agbie-inv.yml ag-bie.yml -b -u ubuntu --skip-tags=version_check'
            slackSend color: 'good', message: "ag-bie Installation Complete ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"

        }
    }

/*
# On successful completion of the playbook, we need to do the following tasks
# Run second playbook
# Copy Jenkins jobs from tarfile to jobs dir
# Copy data-taxxas to the remote machine to /tmp folder
# Run the todo-automa:tion-script on the remote host from /tmp folder
# Update new certificates 
# Create Jenkins Virtualhost entry and nginx config
# Create a hosts entry for shortname and FQDN of the machine we are building as localhost
# Below playbook encompasses all the above items, however Jenkins setup wizard is still 
# not neutralized or completed with curl. Only created the user and presents with login prompt
*/
    stage("Customize ag-bie for env: $ENVIRONMENT_NAME") {
        dir('ag-bie-infra') {
            slackSend color: 'good', message: "ag-bie Customize stage Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
            sh 'cp -r ../ag-bie-config/ag-bie/* playbooks/roles/customize/files/'
            //sh 'cp ../ag-bie-config/ag-bie/pki/* playbooks/roles/customize/files/'
            sh 'ansible-playbook -i ../ag-bie/ansible/agbie-inv.yml playbooks/ag-bie-customize.yml -b -u ubuntu'
            slackSend color: 'good', message: "ag-bie Customization Complete... Job Succeeded... ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
        }
    }

    /* Delete Jenkins installed by ala playbooks and install automated version of 
    Jenkins, copy jobs and install plugins. 
    stage("Uninstall ALA Jenkins for env: $ENVIRONMENT_NAME") {
        node {
            dir('ag-bie-infra') {
                slackSend color: 'good', message: "ag-bie ALA Jenkins Uninstallation Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
                sh 'echo "Uninstall will folow..."'
                slackSend color: 'good', message: "ag-bie ALA Jenkins Uninstallation Will Follow... ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
            }
        }
    }

    /*
    # Disable the ala-install jenkins playbook from ag-bie/ansible/ag-bie.yml
    # Run the below playbook to install the jenkins role and import jobs
    # Copy the defaults/main/yml from the private repository on mgmt-jenkins box


    stage("Install Jenkins for env: $ENVIRONMENT_NAME") {
        node {
            dir('ag-bie-infra') {
                slackSend color: 'good', message: "ag-bie Jenkins Installation Started ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
                sh 'cp ../ag-bie/ansible/agbie-inv.yml agbie-inv.yml'
                sh 'ansible-playbook -i agbie-inv.yml playbooks/jenkins.yml'
                slackSend color: 'good', message: "ag-bie Jenkins Installation Complete... ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Details...>)"
            }
        }
    }
    */



}
