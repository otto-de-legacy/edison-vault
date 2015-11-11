#!/usr/bin/env bash

wget https://releases.hashicorp.com/vault/0.3.1/vault_0.3.1_linux_386.zip
unzip vault_0.3.1_linux_386.zip
./vault server -dev &

export VAULT_ADDR='http://127.0.0.1:8200'
sleep 1

./vault auth-enable app-id
./vault write auth/app-id/map/app-id/test-app-id value=root display_name=foo
./vault write auth/app-id/map/user-id/test-user-id value=test-app-id

./vault write secret/keyOne value=secretNumberOne
./vault write secret/keyTwo value=secretNumberTwo
./vault write secret/keyThree value=secretNumberThree

