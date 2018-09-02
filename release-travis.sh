#!/usr/bin/env bash

# travis can only encrypt one file
# https://docs.travis-ci.com/user/encrypting-files/#encrypting-multiple-files
function setup_secrets() {
    tar -C project -xvf project/secrets.tar
    gpg --import project/key.asc
    chmod 600 project/native_packager_deploy_key
    eval `ssh-agent -s`
    ssh-add project/native_packager_deploy_key
}

# travis checks out a specific commit which creates an unatteched HEAD.
# this leads to an error like this: "ref HEAD is not a symbolic ref"
# https://github.com/sbt/sbt-release/issues/210#issuecomment-348210828
function fix_git {
    echo "Fixing git setup for $TRAVIS_BRANCH"
    git checkout ${TRAVIS_BRANCH}

    git branch -u origin/${TRAVIS_BRANCH}
    git config branch.${TRAVIS_BRANCH}.remote origin
    git config branch.${TRAVIS_BRANCH}.merge refs/heads/${TRAVIS_BRANCH}
}



setup_secrets()
fix_git()
gem install github_changelog_generator
sbt releaseFromTravis