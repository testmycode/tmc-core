#!/bin/sh
# Installs tmc-core

git clone https://github.com/rage/tmc-langs.git
mvn clean install -U -f tmc-langs/pom.xml
