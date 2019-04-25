#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
source gridConfig.sh

/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --version >chrome.txt
chromeFile="chrome.txt"
chrome=$(cat "$chromeFile")

rm -f chrome.txt

if [[ $chrome == *$CHROME_VERSION* ]]; then
  echo Chrome version $CHROME_VERSION is already installed
else
  echo Chrome version $CHROME_VERSION was not found, installing it
  Brew cask install google-chrome
fi

kill -9 $PPID