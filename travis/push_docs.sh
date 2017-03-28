#!/usr/bin/env bash

set -o errexit -o nounset

git checkout $TRAVIS_BRANCH
git config user.name "Deployment Bot"
git config user.email "deploy@travis-ci.org"
git add docs/*
git commit -m "Update docs [skip ci]"
git push origin $TRAVIS_BRANCH
