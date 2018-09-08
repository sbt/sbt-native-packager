#!/usr/bin/env bash

# GPG & SSH
# travis can only encrypt one file
# https://docs.travis-ci.com/user/encrypting-files/#encrypting-multiple-files
tar -C project -xvf project/secrets.tar
gpg --import project/key.asc
chmod 600 project/native_packager_deploy_key
eval `ssh-agent -s`
ssh-add project/native_packager_deploy_key


# SETUP GIT
# travis checks out a specific commit which creates an unatteched HEAD.
# this leads to an error like this: "ref HEAD is not a symbolic ref"
# https://github.com/sbt/sbt-release/issues/210#issuecomment-348210828
# https://stackoverflow.com/questions/6802145/how-to-convert-a-git-shallow-clone-to-a-full-clone/17937889#17937889
TAG_SHA=$(git rev-parse HEAD)
echo "Fixing git setup for $TRAVIS_BRANCH ( release branch: $RELEASE_BRANCH )"
# use ssh for automatic credentials management
git remote set-url origin git@github.com:${TRAVIS_REPO_SLUG}.git
# revert the --singleBranch checkout
git fetch --unshallow
# make remote branches available
git config remote.origin.fetch "+refs/heads/*:refs/remotes/origin/*"
# fetch the release branch, check it out and follow it
git fetch origin ${RELEASE_BRANCH}
git checkout -b "${RELEASE_BRANCH}"
git branch -u origin/${RELEASE_BRANCH}

MASTER_SHA=$(git rev-parse HEAD)
if [ $MASTER_SHA != $TAG_SHA ] then
    echo "You tagged an older commit with SHA $TAG_SHA , but the master is on $MASTER_SHA"
    echo "Automatic releases are only possible from the current master"
    exit 1
fi


# configure basic git stuff for commits
git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"
git config commit.gpgsign true
git config --global user.signingkey 7E26A821BA75234D


# see if we really need these
# git config branch.${RELEASE_BRANCH}.remote origin
# git config branch.${RELEASE_BRANCH}.merge refs/heads/${RELEASE_BRANCH}

# CHANGELOG GENREATOR
gem install github_changelog_generator

# RELEASE
sbt releaseFromTravis
