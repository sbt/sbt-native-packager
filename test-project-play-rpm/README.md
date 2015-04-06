## Requirements

1. [Install ansible](http://docs.ansible.com/intro_installation.html)
2. Install vagrant

## Provision and running

The Vagrant file [invokes Ansible](http://docs.vagrantup.com/v2/provisioning/ansible.html) as a ‘provisioner’
The run-test-playbook.sh assumes the vagrant up has already been run, and invokes runs the test ‘playbook’ against that vm.

```bash
vagrant up --provision
./run-test-playbook
```

Output should be something like this

```bash
PLAY [all] ******************************************************************** 

GATHERING FACTS *************************************************************** 
ok: [default]

TASK: [sbt build] ************************************************************* 
changed: [default]

TASK: [check for output rpm] ************************************************** 
ok: [default]

TASK: [assert ] *************************************************************** 
ok: [default]

TASK: [install rpm] *********************************************************** 
changed: [default]

TASK: [pause seconds=5] ******************************************************* 
(^C-c = continue early, ^C-a = abort)
[default]
Pausing for 5 seconds
ok: [default]

TASK: [run http health check] ************************************************* 
ok: [default]

TASK: [restart service] ******************************************************* 
changed: [default]

TASK: [pause seconds=5] ******************************************************* 
(^C-c = continue early, ^C-a = abort)
[default]
Pausing for 5 seconds
ok: [default]

TASK: [run http health check again] ******************************************* 
ok: [default]

TASK: [uninstall rpm] ********************************************************* 
changed: [default]

PLAY RECAP ******************************************************************** 
default                    : ok=11   changed=4    unreachable=0    failed=0  
```

## Links

- http://docs.vagrantup.com/v2/provisioning/ansible.html
- http://docs.ansible.com/intro_installation.html


