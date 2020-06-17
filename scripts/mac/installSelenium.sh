#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
source gridConfig.sh

curl https://selenium-release.storage.googleapis.com/$SELENIUM_VERSION_SHORT/selenium-server-standalone-$SELENIUM_VERSION.jar -o selenium.jar

mv selenium.jar ../../localGrid
kill -9 $PPID
