#! /bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR
MAVEN_VERSION=3.6.3

function installMaven(){
curl https://www-us.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip  -o apache-maven-$MAVEN_VERSION.zip
unzip apache-maven-$MAVEN_VERSION.zip


mv apache-maven-$MAVEN_VERSION ~

rm -f apache-maven-$MAVEN_VERSION.zip
echo export PATH="~/apache-maven-$MAVEN_VERSION/bin:$PATH" >> ~/.bash_profile

source ~/.bashrc
exit
}

echo -e '\n'Please check the corresponding Maven version from:
echo https://maven.apache.org/download.cgi
echo currently the script will install the following version:
echo MAVEN_VERSION=$MAVEN_VERSION
read -p "Is this version correct? (y/n/q)" ynq
case $ynq in
		[Nn]* )
			   echo Please enter the current version of Maven
			   read InputMavenVersion
			   MAVEN_VERSION=$InputMavenVersion
			   installMaven
			   ;;
		[Yy]* )
			   installMaven
			  ;;
esac
