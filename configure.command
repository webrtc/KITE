# This script will set the USER enviroment variable KITE_HOME to the current folder
# and add '%KITE_HOME%\scripts\path' to the USER PATH.
# It will close the current and open a new one with the command c, r and a ready to use
# to respectively compile, run and launch allure reports from the KITE Tests

KITE_HOME=`pwd`

if [ -n "$(grep "KITE_HOME" ~/.bash_profile)" ]
then
	echo "Updating KITE_HOME"
	sed -i'' -e '/KITE_HOME/d' ~/.bash_profile 
fi

echo export KITE_HOME="$KITE_HOME" >> ~/.bash_profile

if [ -n "$(grep "scripts/mac/path" ~/.bash_profile)" ]
then
	sed -i'' -e '/scripts/mac/path/d' ~/.bash_profile 
fi



chmod +x scripts/mac/path/*
chmod +x $KITE_HOME/third_party/allure-2.10.0/bin/allure

echo export a="$KITE_HOME/scripts/mac/path/a" >> ~/.bash_profile
echo export c="$KITE_HOME/scripts/mac/path/c" >> ~/.bash_profile
echo export r="$KITE_HOME/scripts/mac/path/r" >> ~/.bash_profile

if [ -n "$(grep "third_party/allure" ~/.bash_profile)" ]
then
	sed -i'' -e '/third_party/allure/d' ~/.bash_profile 
fi

echo export allure="$KITE_HOME/third_party/allure-2.10.0/bin/allure" >> ~/.bash_profile

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
osascript -e 'tell application "Terminal" to do script "cd $KITE_HOME"'
kill -9 $PPID 

}

installGrid(){
cd $KITE_HOME/scripts/linux
./interactiveInstallation.sh
}

skipchoice

