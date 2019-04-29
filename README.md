This repository contains code to install the application stack as required for the ag-bie. This repository will use ALA's repositories.  

Technologies used are:
* [Ansible](https://www.ansible.com/)
* [Vagrant](https://www.vagrantup.com/)
* [AWS](https://aws.amazon.com/)
* [Jenkins](https://jenkins.io/)

### Steps to stand up a new environment: 

##### NOTE: We use a private repository for certificates, build scripts, etc hosted on our internal fileserver... If you need more information about deploying the software stack and need to know what files need to be on the fileserver, please contact us via github issue.

# Manual steps in any cloud or Datacenter

1. Provision a ubuntu machine possibly in AWS ( or elsewhere) ( If using Vagrant Oracle VirtualBox is used to automatically provision a VM) 
1. Clone the following repositories in your working folder on the local machine and checkout the specific SHA of the repositories

    ```
    git clone https://github.com/ess-acppo/ag-bie-infra.git
    git checkout ag-master

    git clone https://github.com/ess-acppo/ag-bie.git
    git checkout DAWR_reskin

    git clone https://github.com/AtlasOfLivingAustralia/ala-install.git
    git checkout be0b2fa77f71530a040d97af134900990b5e4c3a

    git clone https://github.com/nxl-private-repo.git
    ```
1. From ag-bie directory, run this command to build the war file `./gradlew assemble`'
1. (Optional if no DNS)Update the hostname of the remote host in the /etc/hosts file of your local machineif there is no DNS
1. To install the software stack, change to the directory `ag-bie/ansible` and run the command `ansible-playbook -i agbie-inv.yml ag-bie.yml -u ubuntu -b --skip-tags=version_check -e 'host_key_checking=False'`.
1. Run the following ansible playbook to perform post install actions and triggering solr index build `ansible-playbook -i agbie-inv.yml ag-bie-infra/playbooks/ag-bie-customize.yml -v`. You need to be in the root directory to run this command. You might have to copy the inv file to the root path as well.

# Automated provisioning in AWS
The process is same as abbove except for using a dynamic inventory file. We have an agbie-inv.yml template developed from ec2-dynamic inv with relevant ag-bie variabled updated in it. Alternatively, you can use the private DNS name of ec2 instance as well to update the ALA inventory with the right hostname. Use the below command:
`sed -ie 's/agbie_host_variable_here/10.1.1.21/g' agbie-inv.yml && sed -ie 's/agbie_servername_variable_here/mo-ag-bie.oztaxa.com/g' agbie-inv.yml`.
After that we just run the above commands as manual install as a shell script from the control device.

# Using Jenkins
* The above ansible commands has also been configured to run via Jenkins using the jenkins/aws_infra.gvy to build a server with our SOE. The following jobs are used currently...
    ```
    * Agbie_Infra_Standup
        1. Stands up a linux debian server with our default SOE
        1. Creates all underlying aws infrastructure for a private/public subnet isolated architecture
        1. Attach ssl certficates
        1. Create DNS entries using ec2 alias

    2. Agbie_Install_Step1
        1. Prep the server
        1. Install underlying software like tomcat, nginx, etc
        1. Build war file
        1. Install components of ag-bie from ALA maven repositories
        1. Customizes the installation
        1. Builds solr index
    ```

# In Vagrant
1. Have [Vagrant](https://www.vagrantup.com/) installed int he machine
1. Have a virtualization software like [Virtual box](https://www.virtualbox.org/) installed
1. Follow the manual installation process. We are working on automating the vagrant installation directly the repository root.

### CI/CD set:

Jenkins is our CI server. Under the covers jenkins uses 
* shell scripts, 
* Ansible playbooks, 
* github wehooks 

As soon as a developer pushes changes to the remote github repo ; github webhook will trigger a build job in jenkins. 
Jenkins plugins used ( not an exaustive list):
* Build Token Root Plugin (https://wiki.jenkins.io/display/JENKINS/Build+Token+Root+Plugin) to enable build trigger without authentication.
* Pipeline to enable pipeline as code (https://jenkins.io/solutions/pipeline/)

### Issues
* Please raise issues on github for any questions or issues you might have about the installation process