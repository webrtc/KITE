#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
source gridConfig.sh


curl https://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_mac64.zip  -o chromezip
unzip chromezip

curl -O -L https://github.com/mozilla/geckodriver/releases/download/$GECKO_VERSION/geckodriver-$GECKO_VERSION-macos.tar.gz
tar xopf geckodriver-$GECKO_VERSION-macos.tar.gz


mv chromedriver ../../localGrid/chrome
mv geckodriver ../../localGrid/firefox


rm -f chromezip
rm -f geckodriver-$GECKO_VERSION-macos.tar.gz

kill -9 $PPID
