FROM ubuntu:groovy as KiteBase

ARG DEBIAN_FRONTEND=noninteractive
ENV DEBIAN_FRONTEND=noninteractive

USER root
RUN apt update
RUN apt -y upgrade
RUN apt update
RUN apt -y install \
  curl \
  dirmngr \
  apt-transport-https \
  lsb-release \
  ca-certificates \
  default-jre \
  default-jdk \
  openjdk-8-jdk \
  maven \
  git \
  gcc \
  build-essential \
  nano \
  wget \
  dpkg \
  unzip \
  xvfb \
  nodejs \
  fonts-liberation \
  libappindicator3-1 \
  libgbm1 \
  libgtk-3-0 \
  libxss1 \
  xdg-utils \
  keyboard-configuration \
  nmap \
  xterm
RUN apt -y update
RUN apt -y install npm

# KITE
RUN git clone https://github.com/webrtc/KITE.git KITE

WORKDIR /KITE/
COPY ./scripts ./scripts/
COPY ./third_party ./third_party/
COPY ./configureLinux.sh .

RUN chmod +x *.sh ./configureLinux.sh

ENV KITE_HOME=/KITE
RUN export KITE_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

RUN export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/bin
ENV MAVEN_HOME=/usr/share/maven
RUN export MAVEN_HOME
RUN /bin/bash -c "source ~/.bashrc"
RUN echo 'export PATH=$PATH:/KITE/localGrid/chrome' >> ~/.bashrc

ARG testpathdir
ENV KITE_TEST_PATH=$testpathdir
ARG configfilename
ENV KITE_CONFIG_NAME=$configfilename

WORKDIR /KITE/$KITE_TEST_PATH
RUN /KITE/scripts/linux/path/c all
WORKDIR /KITE

RUN yes | /KITE/configureLinux.sh

FROM KiteBase

COPY ./KITE-Framework .
COPY ./KITE-Engine .
COPY ./scripts/linux/setupLocalGrid.sh .
COPY ./scripts/linux/gridConfig.sh .
COPY ./scripts/linux/path/c .
COPY ./scripts/linux/path/r .
COPY ./scripts/linux/path/t .
COPY ./scripts/linux/path/a .
COPY ./scripts/linux/path/kite_init .
COPY ./scripts/linux/createFolderLocalGrid.sh .
COPY ./scripts/linux/installChrome.sh .
COPY ./scripts/linux/installFirefox.sh .
COPY ./scripts/linux/installSelenium.sh .
COPY ./scripts/linux/installMaven.sh .
COPY ./scripts/linux/installDrivers.sh .
COPY ./scripts/linux/interactiveInstallation.sh .
COPY ./scripts/entrypoint.sh ./scripts/

ADD pom.xml /KITE/pom.xml

VOLUME /KITE/$KITE_TEST_PATH
VOLUME /KITE/scripts

WORKDIR /KITE/$KITE_TEST_PATH

ENTRYPOINT ["/KITE/scripts/entrypoint.sh"]
