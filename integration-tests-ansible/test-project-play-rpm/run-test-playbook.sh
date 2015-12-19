#!/usr/bin/env bash

# http://redsymbol.net/articles/unofficial-bash-strict-mode/
set -euo pipefail
IFS=$'\n\t'

ansible-playbook --private-key=.vagrant/machines/default/virtualbox/private_key --user=vagrant --connection=ssh --limit='default' --inventory-file=.vagrant/provisioners/ansible/inventory provisioning/test.yml
