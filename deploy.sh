#!/bin/bash
if [ "$TRAVIS_BRANCH" = "master" -a "$TRAVIS_PULL_REQUEST" = "false" -a "$TRAVIS_JDK_VERSION" = "oraclejdk8" ]; then
  echo "Deploying"
  # We already ran the tests
  mvn deploy -Dmaven.test.skip=true --settings deploy.xml
else
  echo "Not on master or PR or java8 -> skipping deploy"
fi
