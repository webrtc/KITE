#!/bin/bash
set +v
. ./gridConfig.sh

firefox -version >firefox.txt
firefoxFile="firefox.txt"
firefox=$(cat "$firefoxFile")
echo $FIREFOX_VERSION
if [[ $firefox == *$FIREFOX_VERSION* ]]; then
  echo Firefox version $FIREFOX_VERSION was found
else
  echo Firefox version $FIREFOX_VERSION was not found, installing it
  sudo apt-get -y update
  sudo apt-get -y install firefox
fi

rm -f firefox.txt

exit


