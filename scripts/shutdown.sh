#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CONFIGPATH=$DIR
CONFIGPATH+="/config"

kill -9 `cat $CONFIGPATH`
echo "" > $CONFIGPATH
