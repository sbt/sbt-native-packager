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
echo "Fixing git setup for $TRAVIS_BRANCH (master)"
git checkout -b "${RELEASE_BRANCH}"

# TODO we should check that the SHA of $TRAVIS_BRANCH and $RELEASE_BRANCH match

git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"
git config commit.gpgsign true
git config --global user.signingkey 7E26A821BA75234D

git branch -u origin/${RELEASE_BRANCH}
git config branch.${RELEASE_BRANCH}.remote origin
git config branch.${RELEASE_BRANCH}.merge refs/heads/${RELEASE_BRANCH}

# CHANGELOG GENREATOR
gem install github_changelog_generator

# RELEASE
sbt releaseFromTravis