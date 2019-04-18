#!/bin/bash
set +v
. ./config.sh

echo "Downloading selenium version $SELENIUM_VERSION"

wget https://selenium-release.storage.googleapis.com/$SELENIUM_VERSION_SHORT/selenium-server-standalone-$SELENIUM_VERSION.jar
if [[ "$USE_CAPABILITY_MATCHER" = "TRUE" ]]
then
  wget https://github.com/CoSMoSoftware/KITE-Extras/releases/download/$KITE_EXTRAS_VERSION/grid-utils-$GRID_VERSION.jar
fi
mv grid-utils-$GRID_VERSION.jar grid.jar
mv grid.jar ../../localGrid/hub

mv selenium-server-standalone-$SELENIUM_VERSION.jar selenium.jar
mv selenium.jar ../../localGrid

exit
