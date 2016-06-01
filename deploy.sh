#!/bin/bash
if [ "$TRAVIS_BRANCH" = "master" -a "$TRAVIS_PULL_REQUEST" = "false" -a "$TRAVIS_JDK_VERSION" = "oraclejdk8" ]; then
  echo "Deploying"
  mvn deploy -Dsurefire.rerunFailingTestsCount=3 --settings deploy.xml
else
  echo "Not on master or PR or java8 -> skipping deploy"
fi
