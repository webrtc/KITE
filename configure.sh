# This script will set the USER enviroment variable KITE_HOME to the current folder
# and add '%KITE_HOME%\scripts\path' to the USER PATH.
# It will close the current and open a new one with the command c, r and a ready to use
# to respectively compile, run and launch allure reports from the KITE Tests

KITE_HOME=`pwd`
NEW_PATH=\$PATH:\$KITE_HOME/scripts/linux/path:\$KITE_HOME/third_party/allure-2.10.0/bin

if [ -n "$(grep "KITE_HOME" ~/.bashrc)" ]
then
	echo "Updating KITE_HOME"
	sed -i '/KITE_HOME/d' ~/.bashrc
fi

echo export KITE_HOME="$KITE_HOME" >> ~/.bashrc

chmod +x scripts/linux/path/*
chmod +x $KITE_HOME/third_party/allure-2.10.0/bin/allure

if [ -n "$(grep "\$KITE_HOME" ~/.bashrc)" ]
then
	sed -i '/$KITE_HOME/d' ~/.bashrc
fi

sed -i 's,/'"$KITE_HOME"'.*$,'"$NEW_PATH"',g' ~/.bashrc
echo export PATH="$NEW_PATH" >> ~/.bashrc

x-terminal-emulator -e 'sh -c "exec bash; cd $KITE_HOME"'

