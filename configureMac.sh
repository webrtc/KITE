#!/bin/bash
[[ -z ${JAVA_HOME} ]] && echo "Error: JAVA_HOME is not set." && exit -1;
if ! [ -x "$(command -v mvn)" ]; then
  echo "Error: mvn is not set, please install maven. If you just ran installMaven.sh, run the script on a new terminal as the environment variables have been updated." >&2
  exit 1
fi
# This script will set the USER enviroment variable KITE_HOME to the current folder
# and add '%KITE_HOME%\scripts\path' to the USER PATH.
# It will close the current and open a new one with the command kite_c, kite_r and kite_a ready to use
# to respectively compile, run and launch allure reports from the KITE Tests

KITE_HOME=`pwd`

if [ -n "$(grep "KITE_HOME" ~/.bash_profile)" ]
then
	echo "Updating KITE_HOME"
	sed -i'' -e '/KITE_HOME/d' ~/.bash_profile 
fi

if [ -n "$(grep "KITE_HOME" ~/.zshenv)" ]
then
	echo "Updating KITE_HOME"
	sed -i'' -e '/KITE_HOME/d' ~/.zshenv
fi

echo export KITE_HOME="$KITE_HOME" >> ~/.bash_profile

if [ -n "$(grep "scripts/mac/path" ~/.bash_profile)" ]
then
	sed -i'' -e '/scripts/mac/path/d' ~/.bash_profile 

fi

echo export KITE_HOME="$KITE_HOME" >> ~/.zshenv

if [ -n "$(grep "scripts/mac/path" ~/.zshenv)" ]
then
	sed -i'' -e '/scripts/mac/path/d' ~/.zshenv

fi


chmod +x $KITE_HOME/scripts/mac/
chmod +x $KITE_HOME/scripts/mac/path/*

chmod +x $KITE_HOME/third_party/allure-2.10.0/bin/allure

echo export PATH="\$PATH:\$KITE_HOME/scripts/mac/path" >> ~/.bash_profile
source ~/.bash_profile

echo export PATH="\$PATH:\$KITE_HOME/scripts/mac/path" >> ~/.zshenv
source ~/.zshenv

if [ -n "$(grep "allure-2.10.0/bin" ~/.bash_profile)" ]
then
	sed -i'' -e '/allure-2.10.0/bin/d' ~/.bash_profile 
fi

if [ -n "$(grep "allure-2.10.0/bin" ~/.zshenv)" ]
then
	sed -i'' -e '/allure-2.10.0/bin/d' ~/.zshenv
fi

echo export PATH="\$PATH:\$KITE_HOME/third_party/allure-2.10.0/bin/" >> ~/.bash_profile
echo export PATH="\$PATH:\$KITE_HOME/third_party/allure-2.10.0/bin/" >> ~/.zshenv

function skipchoice(){
read -p "Do you want to  install the local grid now? (y/n) " yn
case $yn in
    [Yy]* )
           installGrid
           ;;
    [Nn]* )
          startNewPrompt
          ;;
    * ) echo "Please answer yes or no."
		skipchoice;;
esac
}


startNewPrompt(){
osascript -e 'tell application "Terminal" to do script "rm .bash_profile-e;rm .zshenv-e;cd $KITE_HOME"'
kill -9 $PPID

}

installGrid(){
osascript -e 'tell application "Terminal" to do script "rm .bash_profile-e;rm .zshenv-e;cd $KITE_HOME;$KITE_HOME/scripts/mac/interactiveInstallation.sh"'
kill -9 $PPID 
}

skipchoice

