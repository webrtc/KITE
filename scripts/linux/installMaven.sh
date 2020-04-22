#!/bin/bash
set +v
function install(){
wget https://www-us.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip 
unzip apache-maven-$MAVEN_VERSION-bin.zip

mv apache-maven-$MAVEN_VERSION ~


rm -f apache-maven-$MAVEN_VERSION-bin.zip
echo export PATH="\$PATH:~/apache-maven-$MAVEN_VERSION/bin" >> ~/.bashrc
exit
}
export MAVEN_VERSION=3.6.3
echo -e '\n'Please check the corresponding Maven version from:
echo https://maven.apache.org/download.cgi
echo currently the config file has the following versions:
echo MAVEN_VERSION=$MAVEN_VERSION
read -p "Is this version correct? (y/n/q)" ynq
case $ynq in
		[Nn]* )
			   echo Please enter the current version of Maven
			   read InputMavenVersion
			   export MAVEN_VERSION=$InputMavenVersion
			   install
			   ;;
		[Yy]* )
			   install
			  ;;

esac
