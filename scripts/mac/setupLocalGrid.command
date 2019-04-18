BASEDIR=$(dirname "$0")
cd $BASEDIR
source gridConfig
osascript -e "tell application \"Terminal\" to do script \"$BASEDIR/createFolderLocalGrid.sh;exit\" "
sleep 1



if [[ "$INSTALL_BROWSERS" = "TRUE" ]]
then
  osascript -e "tell application \"Terminal\" to do script \"$BASEDIR/installChrome.sh;exit\" "
  osascript -e "tell application \"Terminal\" to do script \"$BASEDIR/installFirefox.sh;exit\" "
else
  echo "Skipping Chrome and Firefox installation"
fi

osascript -e "tell application \"Terminal\" to do script \"$BASEDIR/installDrivers.sh;exit\" "
osascript -e "tell application \"Terminal\" to do script \"$BASEDIR/installSelenium.sh;exit\" "

read -p "Please ensure you enabled the Allow Remote Automation option in Safari's Develop menu. Press y to continue" -n 1 -r


read -p "Do you want to start the Grid (y/n)?  " yn
case $yn in
    [Yy]* )
    	 cd $BASEDIR
   	 cd ..
   	 cd ..
  	 cd localGrid
   	 ./startGrid.sh
         ;;
    [Nn]* )
         echo " "
         ;;
    * ) echo "Please answer yes or no.";;
esac


echo "Setup completed."