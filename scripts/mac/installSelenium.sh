#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
source config.sh


curl https://selenium-release.storage.googleapis.com/$SELENIUM_VERSION_SHORT/selenium-server-standalone-$SELENIUM_VERSION.jar -o selenium.jar

if [[ "$USE_CAPABILITY_MATCHER" = "TRUE" ]]
then
  brew install wget
  wget https://github.com/CoSMoSoftware/KITE-Extras/releases/download/$KITE_EXTRAS_VERSION/grid-utils-$GRID_VERSION.jar
  mv grid-utils-$GRID_VERSION.jar grid.jar
  mv grid.jar ../../localGrid/hub
fi




mv selenium.jar ../../localGrid
kill -9 $PPID
