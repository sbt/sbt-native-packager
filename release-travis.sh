#!/usr/bin/env bash


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

function setup_ssh {
    chmod 600 project/native_packager_deploy_key
    eval `ssh-agent -s`
    ssh-add project/native_packager_deploy_key
}


function release {
    sbt releaseFromTravis
}

fix_git()
gpg --import project/key.asc
gem install github_changelog_generator
release()