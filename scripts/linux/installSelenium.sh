#!/bin/bash
set +v
. ./gridConfig.sh

echo "Downloading selenium version $SELENIUM_VERSION"

wget https://selenium-release.storage.googleapis.com/$SELENIUM_VERSION_SHORT/selenium-server-standalone-$SELENIUM_VERSION.jar

mv selenium-server-standalone-$SELENIUM_VERSION.jar selenium.jar
mv selenium.jar ../../localGrid

exit
