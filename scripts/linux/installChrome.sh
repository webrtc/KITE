#!/bin/bash
set +v
. ./gridConfig
google-chrome-stable -version >chrome.txt
chromeFile="chrome.txt"
chrome=$(cat "$chromeFile")


if [[ $chrome == *$CHROME_VERSION* ]]; then
  echo Chrome version $CHROME_VERSION was found
else
  echo Chrome version $CHROME_VERSION was not found, installing it.
  wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
  sudo dpkg -i google-chrome-stable_current_amd64.deb
  rm -f google-chrome-stable_current_amd64.deb
fi

rm -f chrome.txt

exit
