#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
source config.sh

/Applications/Firefox.app/Contents/MacOS/firefox --version >firefox.txt
firefoxFile="firefox.txt"
firefox=$(cat "$firefoxFile")

rm -f firefox.txt

if [[ $firefox == *$FIREFOX_VERSION* ]]; then
  echo Firefox version $FIREFOX_VERSION is already installed
else
  echo Firefox version $FIREFOX_VERSION was not found, installing it
  brew cask install firefox
fi

kill -9 $PPID