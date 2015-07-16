#!/bin/sh
# Installs tmc-core

git clone https://github.com/rage/tmc-langs.git
mvn clean install -U -Dmaven.test.skip=true -f tmc-langs/pom.xml
