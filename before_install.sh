#!/bin/sh
# NOTE: should only be used in travis (or other temporary automated setups)
#
# Clones and install tmc-langs locally, till the tmc-langs has been officially 
# released to a public maven repository.

git clone https://github.com/rage/tmc-langs.git
mvn clean install -U -Dmaven.test.skip=true -f tmc-langs/pom.xml
