#!/bin/bash
set +v
. ./gridConfig.sh




function startGrid() {
  cd ../../localGrid
  if [ "$DESKTOP_ENVIRONMENT" = "TRUE" ]
  then
    x-terminal-emulator -e ./startGrid.sh &
  else
    ./startGrid.sh &
  fi
}

if [[ "$DESKTOP_ENVIRONMENT" = "TRUE" ]]
then
sudo apt-get -y upgrade -f gnome-terminal
fi

./createFolderLocalGrid.sh

if [[ "$INSTALL_BROWSERS" = "TRUE" ]]
then
  ./installChrome.sh
  ./installFirefox.sh
else
  echo "Skipping Chrome and Firefox installation"
fi

./installDrivers.sh
./installSelenium.sh

read -p "Do you want to start the Grid (y/n)?  " yn
case $yn in
    [Yy]* )
           startGrid
           ;;
    [Nn]* )
          echo " "
          ;;
    * ) echo "Please answer yes or no.";;
esac


echo "Setup completed."
