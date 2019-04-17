#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
source config.sh


curl https://selenium-release.storage.googleapis.com/$SELENIUM_VERSION_SHORT/selenium-server-standalone-$SELENIUM_VERSION.jar -o selenium.jar


mv selenium.jar ../../localGrid
exit
