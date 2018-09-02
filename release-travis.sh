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
echo "Fixing git setup for $TRAVIS_BRANCH"
git checkout "${TRAVIS_BRANCH}"

git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"
git config commit.gpgsign true
git config --global user.signingkey 7E26A821BA75234D

git branch -u origin/${TRAVIS_BRANCH}
git config branch.${TRAVIS_BRANCH}.remote origin
git config branch.${TRAVIS_BRANCH}.merge refs/heads/${TRAVIS_BRANCH}

# CHANGELOG GENREATOR
gem install github_changelog_generator

# RELEASE
sbt releaseFromTravis