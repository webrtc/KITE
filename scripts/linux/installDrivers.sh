#!/bin/bash
set +v
. ./gridConfig.sh


wget https://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip
unzip chromedriver_linux64.zip 

wget https://github.com/mozilla/geckodriver/releases/download/$GECKO_VERSION/geckodriver-$GECKO_VERSION-linux32.tar.gz
tar xvzf geckodriver-$GECKO_VERSION-linux32.tar.gz

mv chromedriver ../../localGrid/chrome
mv geckodriver ../../localGrid/firefox

rm -f chromedriver_linux64.zip 
rm -f geckodriver-$GECKO_VERSION-linux32.tar.gz

exit
