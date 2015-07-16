#!/bin/sh
# Installs tmc-core

git clone https://github.com/rage/tmc-langs.git
mvn clean install -U -f -Dmaven.test.skip=true tmc-langs/pom.xml
