#!/bin/bash
########################################################################
# Installation de Constellio EIM 7.x
# install_constellio7.sh
########################################################################

LOG_FILE="/tmp/install_constellio7.log"
GIT_INSTALL="y"
JDK_INSTALL="y"
GRADLE_INSTALL="y"
CONSTELLIO_SRC_INSTALL="y"
SOLR_INSTALL="y"
LO_INSTALL="y"
FIREFOX_INSTALL="n"
IDEA_INSTALL="n"
CONSTELLIO_COMPIL="y"
JSW_INSTALL="y"
BACKUP_INSTALL="y"
NGINX_INSTALL="y"
SONAR_INSTALL="n"
LDAP_INSTALL="n"


########################################################################
# ajustements selon la distribution
# script testé avec Ubuntu 16.04/18.04 et Centos 7
########################################################################

DISTRO=$(grep "^NAME=" /etc/os-release | cut -d "=" -f 2 |\
 sed -e 's/^"//' -e 's/"$//' | awk '{print $1}')

echo "Distribution: "${DISTRO} >> ${LOG_FILE}
case ${DISTRO} in
  Ubuntu|ubuntu|Debian|debian)
    PROC_VERSION="Ubuntu"
    INSTALL="sudo apt-get install -y -qq"
    ${INSTALL} acl
    ;;
  CentOS|centos|"Red Hat"|"red hat")
    PROC_VERSION="Red Hat"
    INSTALL="sudo yum install -y -q"
    yum install -y -q sudo
    ;;
  *)
    echo "Distribution non supportée"
    ;;
esac


########################################################################
# rép. de base de constellio
########################################################################

echo -e "\nIntallation de Constellio home...\n"
CONSTELLIO_HOME="/opt/constellio"
sudo mkdir -p ${CONSTELLIO_HOME}


########################################################################
# installation des paquets de base
########################################################################

echo -e "\nIntallation des paquets de base...\n"
${INSTALL} wget unzip


########################################################################
# installation de Git
########################################################################

if [ "${GIT_INSTALL}" == "y" ]; then
  echo -e "\nIntallation de Git...\n"
  echo -e "\nIntallation de Git...\n" >> ${LOG_FILE}
  ${INSTALL} git
  git --version >> ${LOG_FILE}
fi


########################################################################
# dépôt git Constellio
########################################################################

if [ "${CONSTELLIO_SRC_INSTALL}" == "y" ]; then
  echo -e "\nIntallation du dépôt Constellio...\n"
  echo -e "\nIntallation du dépôt Constellio...\n" >> ${LOG_FILE}
  GIT_HOME="${CONSTELLIO_HOME}/git"

  mkdir ${GIT_HOME}
  cd ${GIT_HOME}
  git clone https://github.com/doculibre/constellio >> ${LOG_FILE}
  cd
fi


########################################################################
# installation de OpenJDK8
########################################################################

if [ "${JDK_INSTALL}" == "y" ]; then
  echo -e "\nIntallation de OpenJDK...\n"
  echo -e "\nIntallation de OpenJDK...\n" >> ${LOG_FILE}
  case ${DISTRO} in
    Ubuntu|ubuntu|Debian|debian)
      JDK="openjdk-8-jdk"
      JDK_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
      ;;
    CentOS|centos|"Red Hat"|"red hat")
      JDK="java-1.8.0-openjdk-devel"
      JDK_HOME="/etc/alternatives/java_sdk_openjdk"
      ;;
  esac

  ${INSTALL} ${JDK}
  echo 'export JAVA_HOME='"${JDK_HOME}" | sudo tee -a /etc/profile.d/constellio.sh &>/dev/null
  source /etc/profile.d/constellio.sh
  echo $JAVA_HOME >> ${LOG_FILE}
  java -version
fi


########################################################################
# installation de Gradle
########################################################################

if [ "${GRADLE_INSTALL}" == "y" ]; then
  echo -e "\nIntallation de Gradle...\n"
  echo -e "\nIntallation de Gradle...\n" >> ${LOG_FILE}
  GRADLE_VERSION=3.5.1
  GRADLE_HOME="/opt/gradle"

  wget -nv https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
  sudo mkdir -p ${GRADLE_HOME}
  sudo unzip -q -d ${GRADLE_HOME} gradle-${GRADLE_VERSION}-bin.zip
  sudo ln -s ${GRADLE_HOME}/gradle-${GRADLE_VERSION} ${GRADLE_HOME}/default
  echo 'export PATH=${PATH}':${GRADLE_HOME}'/default/bin' | sudo tee -a /etc/profile.d/constellio.sh &>/dev/null
  source /etc/profile.d/constellio.sh
  gradle -v >> ${LOG_FILE}
fi


########################################################################
# installation de solr
# visiter http://localhost:8983/
########################################################################

if [ "${SOLR_INSTALL}" == "y" ]; then
  echo -e "\nIntallation de Solr...\n"
  echo -e "\nIntallation de Solr...\n" >> ${LOG_FILE}
  SOLR_VERSION=5.5.5
  #SOLR_VERSION=6.6.3
  SOLR_HOME="/opt/solr"
  SOLR_DATA="/var/solr/data"

  ${INSTALL} lsof
  wget -nv http://www.us.apache.org/dist/lucene/solr/${SOLR_VERSION}/solr-${SOLR_VERSION}.tgz
  tar xzf solr-${SOLR_VERSION}.tgz solr-${SOLR_VERSION}/bin/install_solr_service.sh --strip-components=2

  # optionnel: pour solr version 5.x sous LXD ou docker seulement car l'install ne détecte pas l'OS
  sed -i -e "s/proc_version=.*/proc_version=\"${PROC_VERSION}\"/" install_solr_service.sh
  #

  sudo ./install_solr_service.sh solr-${SOLR_VERSION}.tgz -f
  sudo systemctl stop solr
  kill $(ps aux|grep solr -m1|awk '{print $2}')
  # TODO valider si nécessaire
  #sudo ln -s /etc/default/solr.in.sh ${SOLR_HOME}/bin/solr.in.sh

  # solr tuning
  sudo sed -i -e 's/#SOLR_JAVA_MEM="-Xms512m -Xmx512m"/SOLR_JAVA_MEM="-Xms1G -Xmx4096m"/' /etc/default/solr.in.sh
  sudo sed -i -e 's/#GC_LOG_OPTS=/GC_LOG_OPTS=/'                                          /etc/default/solr.in.sh
  sudo sed -i -e 's/#  -XX:+PrintGCDateStamps/  -XX:+PrintGCDateStamps/'                  /etc/default/solr.in.sh
  sudo sed -i -e 's/#SOLR_TIMEZONE="UTC"/SOLR_TIMEZONE="EST"/'                            /etc/default/solr.in.sh
  sudo sed -i -e 's/#ENABLE_REMOTE_JMX_OPTS=/ENABLE_REMOTE_JMX_OPTS=/'                    /etc/default/solr.in.sh
  echo '#'                                             | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo 'GC_TUNE="-XX:NewRatio=3 \'                     | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:SurvivorRatio=4 \'                         | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:TargetSurvivorRatio=90 \'                  | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:MaxTenuringThreshold=8 \'                  | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:+UseConcMarkSweepGC \'                     | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:+UseParNewGC \'                            | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:ConcGCThreads=4 -XX:ParallelGCThreads=4 \' | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:+CMSScavengeBeforeRemark \'                | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:PretenureSizeThreshold=64m \'              | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:+UseCMSInitiatingOccupancyOnly \'          | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:CMSInitiatingOccupancyFraction=50 \'       | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:CMSMaxAbortablePrecleanTime=6000 \'        | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:+CMSParallelRemarkEnabled \'               | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  echo '-XX:+ParallelRefProcEnabled"'                  | sudo tee -a /etc/default/solr.in.sh &>/dev/null
  sudo sed -i -e 's/"solr.jetty.request.header.size" default="8192"/"solr.jetty.request.header.size" default="8192000"/' ${SOLR_HOME}/server/etc/jetty.xml

  # cores pour Constellio
  #sudo rm -rf ${SOLR_HOME}/server/solr/configsets/*_configs
  #sudo cp -r ${GIT_HOME}/constellio/solrHome5/configsets/*_configs ${SOLR_HOME}/server/solr/configsets
  sudo mkdir ${SOLR_DATA}/configsets
  sudo cp -r ${GIT_HOME}/constellio/solrHome5/configsets/*_configs ${SOLR_DATA}/configsets
  sudo chown -R solr. ${SOLR_DATA}

  # optionnel: modifications pour solr 6.x+ (fonctionne aussi en 5)
  #sudo sed -i -e "s/units=/distanceUnits=/" ${SOLR_DATA}/configsets/events_configs/conf/schema.xml
  #sudo sed -i -e "/-- Thai/,+10d"           ${SOLR_DATA}/configsets/events_configs/conf/schema.xml
  #sudo sed -i -e "s/units=/distanceUnits=/" ${SOLR_DATA}/configsets/notifications_configs/conf/schema.xml
  #sudo sed -i -e "/-- Thai/,+10d"           ${SOLR_DATA}/configsets/notifications_configs/conf/schema.xml
  #sudo sed -i -e "s/units=/distanceUnits=/" ${SOLR_DATA}/configsets/records_configs/conf/schema.xml
  #sudo sed -i -e "/-- Thai/,+10d"           ${SOLR_DATA}/configsets/records_configs/conf/schema.xml
  # TODO ajouter autres modif aux fichiers managed_schema

  # note. Constellio doit avoir accès directement aux fichiers de solr
  sudo systemctl enable solr
  sudo systemctl start solr
  sudo systemctl show solr -p ActiveState >> ${LOG_FILE}
fi


########################################################################
# installation de libreoffice
########################################################################

if [ "${LO_INSTALL}" == "y" ]; then
  echo -e "\nInstallation de Libreoffice...\n"
  echo -e "\nInstallation de Libreoffice...\n" >> ${LOG_FILE}
  LO_HOME="/opt/libreoffice"
  LO_VERSION="6.0.4.2"
  LO_SHORT_VERSION="6.0"

  # méthode manuelle
  case ${DISTRO} in
    Ubuntu|ubuntu|Debian|debian)
      LO_TYPE="deb"
      INSTALL_LO="dpkg -i LibreOffice_${LO_VERSION}_Linux_x86-64_${LO_TYPE}/DEBS/*.deb"
      ${INSTALL} libdbus-glib-1-2
      ;;
    CentOS|centos|"Red Hat"|"red hat")
      LO_TYPE="rpm"
      INSTALL_LO="rpm --quiet -ivh --upgrade LibreOffice_${LO_VERSION}_Linux_x86-64_${LO_TYPE}/RPMS/*.rpm"
      ${INSTALL} which cairo cups-libs libSM libXinerama dbus-glib
      ;;
  esac
  wget -nv http://downloadarchive.documentfoundation.org/libreoffice/old/${LO_VERSION}/${LO_TYPE}/x86_64/LibreOffice_${LO_VERSION}_Linux_x86-64_${LO_TYPE}.tar.gz
  tar -xf LibreOffice_${LO_VERSION}_Linux_x86-64_${LO_TYPE}.tar.gz
  ${INSTALL_LO}
  ln -s /opt/libreoffice${LO_SHORT_VERSION} ${LO_HOME}
  rm -Rf LibreOffice_${LO_VERSION}_Linux_x86-64_${LO_TYPE}

  # méthode de la distribution
  #${INSTALL} libreoffice-common libreoffice-writer libreoffice-calc libreoffice-draw libreoffice-impress

  /opt/libreoffice/program/soffice --version >> ${LOG_FILE}
fi


########################################################################
# installation de firefox (pour tests lors de la compilation)
########################################################################

if [ "${FIREFOX_INSTALL}" == "y" ]; then
  echo -e "\nInstallation de Firefox...\n"
  echo -e "\nInstallation de Firefox...\n" >> ${LOG_FILE}
  ${INSTALL} firefox
  # TODO valider si requis
  sudo mkdir /opt/firefox
  sudo ln -s /usr/bin/firefox /opt/firefox/firefox
  /opt/firefox/firefox --version >> ${LOG_FILE}
fi


########################################################################
# installation de IntelliJ Idea without JDK
########################################################################

if [ "${IDEA_INSTALL}" == "y" ]; then
  echo -e "\nInstallation de IntelliJ...\n"
  echo -e "\nInstallation de IntelliJ...\n" >> ${LOG_FILE}
  IDEA_VERSION=ideaIC-2018.1-no-jdk
  IDEA_HOME="/opt/ideaic"

  case ${DISTRO} in
    CentOS|centos|"Red Hat"|"red hat")
      ${INSTALL} which
      ;;
  esac
  wget -nv https://download.jetbrains.com/idea/${IDEA_VERSION}.tar.gz
  sudo mkdir -p ${IDEA_HOME}/${IDEA_VERSION}
  sudo tar xf ${IDEA_VERSION}.tar.gz -C ${IDEA_HOME}/${IDEA_VERSION} --strip-components 1
  sudo ln -s ${IDEA_HOME}/${IDEA_VERSION} ${IDEA_HOME}/default
  echo 'export PATH=${PATH}':${IDEA_HOME}'/default/bin' | sudo tee -a /etc/profile.d/constellio.sh &>/dev/null
  source /etc/profile.d/constellio.sh
  echo 'idea.gradle.prefer.idea_test_runner=true' | sudo tee -a ${IDEA_HOME}/default/bin/idea.properties &>/dev/null
  sudo sed -i -e 's/-Xmx512m/-Xmx2048m/' ${IDEA_HOME}/default/bin/idea.vmoptions

  #idea.sh
  # Do not import settings -> OK
  # IntelliJ IDEA User License Agreement -> Accept
  # DATA Sharing Options : Send anonymous usage statistics to JetBrains : not selected -> OK
  # Customize IntelliJ IDEA -> Skip Remaining and Set Defaults
  # Exit
fi


########################################################################
# compilation avec IntelliJ
########################################################################

# idea.sh
# Import Project
#  sélectionner le répertoire doculibre
# Import project from external model
#  Gradle
# Use auto-import : selected
#  Gradle-home: /opt/gradle/default
# Show tips on startup : not selected
#  wait for full import
# File > Project Structure > Project
#  s'assurer que Project SDK : 1.8
# File > Settings > Build, Execution, Deployment > Compiler > Java Compiler
#  Project bytecode version : 7
# VCS > Git > Branches
#  Checkout tag or revision : 7.7.5
# Build > Build project
# View > Tool Windows > Gradle
# Gradle, goto doculibre > constellio > Tasks > build > build
# *.war généré dans /opt/constellio/git/constellio/build/libs/*.war


########################################################################
# compilation en ligne de commande
########################################################################

if [ "${CONSTELLIO_COMPIL}" == "y" ]; then
  echo -e "\nCompilation de Constellio...\n"
  CONSTELLIO_TAG="7.7.5"
  CONSTELLIO_HOME="/opt/constellio"
  GIT_HOME="${CONSTELLIO_HOME}/git"
  #SOLR_VERSION=6.6.3

  cd ${GIT_HOME}
  cd constellio
  git fetch
  git checkout tags/${CONSTELLIO_TAG}
  cd ..
  # TODO valider pourquoi on prend la version du master
  #cp constellio/build.gradle.main build.gradle
  #cp constellio/settings.gradle.main settings.gradle
  wget -nv https://raw.githubusercontent.com/doculibre/constellio/master/build.gradle.main -O build.gradle
  wget -nv https://raw.githubusercontent.com/doculibre/constellio/master/settings.gradle.main -O settings.gradle
  sed -i -e "s/4.2.42/${CONSTELLIO_TAG}/" build.gradle
  # TODO valider
  # optionnel: ajuster librairies pour solr 6.6+
  #sed -i -e "s/6.5.0/${SOLR_VERSION}/" build.gradle
  echo "${CONSTELLIO_TAG}" > constellio/version
  # compiler sans les tests en mode silencieux
  gradle build --no-daemon -q -x test >> ${LOG_FILE}
  # compiler avec les tests (sudo requis)
  #sudo gradle build
  cd

  # copie du war de constellio
  sudo unzip -q ${GIT_HOME}/constellio/build/libs/constellio-${CONSTELLIO_TAG}.war -d ${CONSTELLIO_HOME}/webapp

fi


########################################################################
# installation de Java Service Wrapper (service jetty pour Constellio)
# visiter http://localhost:8080/constellio
########################################################################

if [ "${JSW_INSTALL}" == "y" ]; then
  echo -e "\nInstallation de JSW...\n"
  echo -e "\nInstallation de JSW...\n" >> ${LOG_FILE}
  CONSTELLIO_HOME="/opt/constellio"
  JSW_VERSION="3.5.34"
  JSW_HOME="${CONSTELLIO_HOME}/wrapper-linux"
  JSW_USER="constellio"

  # optionnel: accélerer le démarrage de Jetty si sur une machine virtuelle (entropy)
  case ${DISTRO} in
    Ubuntu|ubuntu|Debian|debian)
      ;;
    CentOS|centos|"Red Hat"|"red hat")
      ${INSTALL} epel-release
      ;;
  esac
  ${INSTALL} haveged

  ${INSTALL} curl
  wget -nv https://download.tanukisoftware.com/wrapper/${JSW_VERSION}/wrapper-linux-x86-64-${JSW_VERSION}.tar.gz
  sudo mkdir -p ${JSW_HOME}
  sudo tar -xf wrapper-linux-x86-64-${JSW_VERSION}.tar.gz -C ${JSW_HOME} --strip-components 1
  sudo useradd ${JSW_USER}
  sudo usermod -a -G solr ${JSW_USER}
  sudo chgrp -R ${JSW_USER} ${CONSTELLIO_HOME}
  sudo chmod -R 2775 ${CONSTELLIO_HOME}
  sudo setfacl -m d:g::rwx ${CONSTELLIO_HOME}
  sudo mkdir -p ${CONSTELLIO_HOME}/bin ${CONSTELLIO_HOME}/conf/settings ${CONSTELLIO_HOME}/contents \
   ${CONSTELLIO_HOME}/lib ${CONSTELLIO_HOME}/logs ${CONSTELLIO_HOME}/temp ${CONSTELLIO_HOME}/transaction_log
  cd ${CONSTELLIO_HOME}
  sudo cp -r ${JSW_HOME}/lib ./
  sudo cp -r ${JSW_HOME}/bin ./
  sudo cp ${JSW_HOME}/src/bin/sh.script.in ./bin/startup
  sudo sed -i -e 's/@app.name@/constellio/'                bin/startup
  sudo sed -i -e 's/@app.long.name@/constellio/'           bin/startup
  sudo sed -i -e 's/#RUN_AS_USER=/RUN_AS_USER=constellio/' bin/startup
  sudo chmod +x bin/startup
  sudo cp ${JSW_HOME}/src/conf/wrapper.conf.in ./conf/wrapper.conf
  sudo sed -i -e '/wrapper.java.classpath.1=/ c\wrapper.java.classpath.1=../lib/wrapper.jar\n#wrapper.java.classpath.2='          conf/wrapper.conf
  sudo sed -i -e '/wrapper.java.classpath.2=/ c\wrapper.java.classpath.2=../webapp/WEB-INF/lib/*.jar\n#wrapper.java.classpath.3=' conf/wrapper.conf
  sudo sed -i -e '/wrapper.java.classpath.3=/ c\wrapper.java.classpath.3=../webapp/WEB-INF/classes'                               conf/wrapper.conf
  sudo sed -i -e '/wrapper.java.additional.1=/ c\wrapper.java.additional.1=-Db=t'                                                 conf/wrapper.conf
  sudo sed -i -e '/wrapper.java.maxmemory=/ c\wrapper.java.maxmemory=16384'                                                       conf/wrapper.conf
  sudo sed -i -e '/wrapper.app.parameter.1=/ c\wrapper.app.parameter.1=com.constellio.app.start.MainConstellio'                   conf/wrapper.conf
  sudo sed -i -e '/wrapper.logfile.maxsize=/ c\wrapper.logfile.maxsize=100m'                                                      conf/wrapper.conf
  sudo sed -i -e '/wrapper.logfile.maxfiles=/ c\wrapper.logfile.maxfiles=5'                                                       conf/wrapper.conf
  sudo sed -i -e '/wrapper.console.title=/ c\wrapper.console.title=Constellio EIM'                                                conf/wrapper.conf
  sudo sed -i -e '/wrapper.name=/ c\wrapper.name=constellio'                                                                      conf/wrapper.conf
  sudo sed -i -e '/wrapper.displayname=/ c\wrapper.displayname=constellio'                                                        conf/wrapper.conf
  sudo sed -i -e '/wrapper.description=/ c\wrapper.description=Constellio EIM Application'                                        conf/wrapper.conf
  # TODO valider
  #echo "#Restart command file"                             | sudo tee -a conf/wrapper.conf
  #echo "wrapper.commandfile=../webapp/WEB-INF/command/cmd" | sudo tee -a conf/wrapper.conf
  #echo "wrapper.restart.reload_configuration=TRUE"         | sudo tee -a conf/wrapper.conf

  sudo tee ./bin/waitforsolr &>/dev/null <<EOF
#!/bin/bash
# Create a loop in bash that is waiting for a webserver to respond
until \$(curl --output /dev/null --silent --head --fail http://localhost:8983/solr); do
  printf '.'
  sleep 2
done
EOF
  sudo chmod +x ./bin/waitforsolr
  # systemd constellio service
  sudo tee /lib/systemd/system/constellio.service &>/dev/null <<EOF
[Unit]
Description=Constellio EIM
After=syslog.target network.target

[Service]
Type=forking
ExecStartPre=${CONSTELLIO_HOME}/bin/waitforsolr
ExecStart=${CONSTELLIO_HOME}/bin/startup start sysd
ExecStop=${CONSTELLIO_HOME}/bin/startup stop sysd
User=${JSW_USER}

[Install]
WantedBy=multi-user.target
EOF

  sudo tee ./conf/constellio.setup.properties &>/dev/null <<EOF
#These configs are used when Constellio is started the first time
admin.servicekey=adminkey
admin.password=password
mainDataLanguage=fr
collections=collection
collection.collection.languages=fr
collection.collection.modules=com.constellio.app.modules.rm.ConstellioRMModule
EOF
  sudo tee ./conf/constellio.properties &>/dev/null <<EOF
# === DATASTORE CONFIGURATION ===
# *** WARNING : collections will be deleted ***
dao.records.type=http
dao.records.http.url=http://localhost:8983/solr/
dao.contents.type=filesystem
dao.settings.type=filesystem
dao.contents.filesystem.folder=/opt/constellio/contents/
secondTransactionLog.enabled=true
secondTransactionLog.folder=/opt/constellio/transaction_log/
hashing.encoding=BASE64_URL_ENCODED
dao.contents.filesystem.separatormode=THREE_LEVELS_OF_ONE_DIGITS
EOF
  # TODO valider si nécessaire
  #sudo tee ./conf/plugins.xml &>/dev/null <<EOF
#<?xml version="1.0" encoding="UTF-8"?>
#<plugins />
#EOF
  #
  #sudo tee ./conf/key.txt &>/dev/null <<EOF
#constellio_408-95-593_ext
#EOF
  cd

  # console manuelle: CTRL+C pour quitter
  #sudo ${CONSTELLIO_HOME}/bin/startup console
  sudo systemctl enable constellio
  sudo systemctl start constellio
  sudo systemctl show constellio -p ActiveState >> ${LOG_FILE}
fi


########################################################################
# script de backup
########################################################################

echo -e "\nInstallation du script de backup...\n"
if [ "${BACKUP_INSTALL}" == "y" ]; then
  sudo tee /usr/local/sbin/constellio-backup &>/dev/null <<EOF
#!/bin/bash
BACKUP_DIR="/backup"
CONSTELLIO_CONTENT_DIR="/opt/constellio/contents"
CONSTELLIO_CONF_DIR="/opt/constellio/conf"
SOLR_URL="http://localhost:8983/solr"
DATE=\$(date "+%Y-%m-%d")
DAYS_TO_KEEP=1

mkdir -p \${BACKUP_DIR}
chmod 700 \${BACKUP_DIR}
systemctl stop constellio
# backup de solr
rm -rf /var/solr/backup
for CORE in events notifications records
do
  # attention, ceci est une commande asynchrone
  curl "\${SOLR_URL}/\${CORE}/replication?command=backup&name=\${CORE}&location=/var/solr/backup"
  #while ! grep -q "success" $(curl "\${SOLR_URL}/\${CORE}/replication?command=details"); do
  #  sleep 2
  #done
done
tar -czPf  \${BACKUP_DIR}/solr_cores_\${DATE}.tgz /var/solr/backup/snapshot.*
rm -rf /var/solr/backup
# backup des donnees de Constellio
tar -czPf \${BACKUP_DIR}/constellio_contents_\${DATE}.tgz \${CONSTELLIO_CONTENT_DIR}
# backup de la configuration de Constellio
tar -czPf \${BACKUP_DIR}/constellio_conf_txt_\${DATE}.tgz \${CONSTELLIO_CONF_DIR}/*.txt
tar -czPf \${BACKUP_DIR}/constellio_conf_settings_\${DATE}.tgz \${CONSTELLIO_CONF_DIR}/settings
systemctl start constellio
# securisation des backups
chmod 600 \${BACKUP_DIR}/*
# epuration des fichiers de backup
find \${BACKUP_DIR} -type f -mtime +\${DAYS_TO_KEEP} | xargs rm -R -f -v
exit 0
EOF
  sudo chmod 500 /usr/local/sbin/constellio-backup
fi


########################################################################
# installation de nginx (reverse proxy vers constellio, solr et sonarqube)
########################################################################

if [ "${NGINX_INSTALL}" == "y" ]; then
  echo -e "\nInstallation de Nginx...\n"
  echo -e "\nInstallation de Nginx...\n" >> ${LOG_FILE}
  case ${DISTRO} in
    Ubuntu|ubuntu|Debian|debian)
      ${INSTALL} ssl-cert nginx
      sudo rm /etc/nginx/sites-enabled/default
      PROXY_CONF="/etc/nginx/sites-available/servlet-proxy.conf"
      sudo ln -s ${PROXY_CONF} /etc/nginx/sites-enabled/servlet-proxy.conf
      ;;
    CentOS|centos|"Red Hat"|"red hat")
      ${INSTALL} epel-release
      ${INSTALL} nginx
      sed -i -e '/    server {/,+20d' /etc/nginx/nginx.conf
      PROXY_CONF="/etc/nginx/conf.d/servlet-proxy.conf"
      mkdir -p /etc/ssl/private
      chmod 700 /etc/ssl/private
      /etc/ssl/certs/make-dummy-cert /etc/ssl/certs/ssl-cert-snakeoil.pem
      ln -s /etc/ssl/certs/ssl-cert-snakeoil.pem /etc/ssl/private/ssl-cert-snakeoil.key
      firewall-cmd --zone=public --permanent --add-service=http
      firewall-cmd --zone=public --permanent --add-service=https
      firewall-cmd --reload
      ;;
  esac
  sudo tee -a ${PROXY_CONF} &>/dev/null <<EOF
server {
  listen 80;
  #return 301 https://\$host\$request_uri;
  server_name _;
  location /constellio {
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_read_timeout 90;
    proxy_pass http://127.0.0.1:8080/constellio;
    proxy_redirect http://localhost:8080/constellio http://localhost/constellio;
  }
  location /solr {
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_read_timeout 90;
    proxy_pass http://127.0.0.1:8983/solr;
    proxy_redirect http://localhost:8983/solr http://localhost/solr;
  }
  location /sonarqube {
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_read_timeout 90;
    proxy_pass http://127.0.0.1:9000/sonarqube;
    proxy_redirect http://localhost:9000/ http://localhost/sonarqube;
  }
}
server {
  listen 443 ssl http2;
  server_name _;
  ssl_certificate /etc/ssl/certs/ssl-cert-snakeoil.pem;
  ssl_certificate_key /etc/ssl/private/ssl-cert-snakeoil.key;
  ssl on;
  ssl_session_cache builtin:1000  shared:SSL:10m;
  ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
  ssl_ciphers HIGH:!aNULL:!eNULL:!EXPORT:!CAMELLIA:!DES:!MD5:!PSK:!RC4;
  ssl_prefer_server_ciphers on;
  location /constellio {
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_read_timeout 90;
    proxy_pass http://127.0.0.1:8080/constellio;
    proxy_redirect http://localhost:8080/constellio https://localhost/constellio;
  }
  location /solr {
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_read_timeout 90;
    proxy_pass http://127.0.0.1:8983/solr;
    proxy_redirect http://localhost:8983/solr http://localhost/solr;
  }
  location /sonarqube {
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_read_timeout 90;
    proxy_pass http://127.0.0.1:9000/sonarqube;
    proxy_redirect http://localhost:9000/ http://localhost/sonarqube;
  }
}
EOF
  sudo systemctl enable nginx
  sudo systemctl restart nginx
  sudo systemctl show nginx -p ActiveState >> ${LOG_FILE}
fi


########################################################################
# installation sonarqube
# visiter http://localhost:9000/sonarqube
# usager/mdp: admin/admin
# créer un token "constellio" et prendre en note ce token
# TODO valider
# ? editer ${GIT_HOME}/constellio/sdk/build.gradle :
#  sonar.host.url=http://localhost:9000/sonarqube
#  sonar.login=<token_sonar_pour_constellio>
# cd ${GIT_HOME}
# gradle sonarqube -Dsonar.host.url=http://localhost:9000/sonarqube -Dsonar.login=<token_sonar_pour_constellio>
########################################################################

if [ "${SONAR_INSTALL}" == "y" ]; then
  echo -e "\nInstallation de Sonarqube...\n"
  echo -e "\nInstallation de Sonarqube...\n" >> ${LOG_FILE}
  PG_DB=sonar
  PG_USER=sonar
  PG_PASS=password
  SONAR_USER=sonar
  SONAR_VERSION=6.7.3
  SONAR_HOME=/opt/sonarqube

  case ${DISTRO} in
    Ubuntu|ubuntu|Debian|debian)
      ${INSTALL} postgresql
      ;;
    CentOS|centos|"Red Hat"|"red hat")
      ${INSTALL} postgresql-server postgresql-contrib
      sudo postgresql-setup initdb
      sudo sed -i -e "s/ ident/ md5/" /var/lib/pgsql/data/pg_hba.conf
      ;;
  esac
  sudo systemctl enable postgresql && sudo systemctl restart postgresql
  sudo -u postgres psql postgres -c "CREATE DATABASE ${PG_DB} template=template0 encoding='UTF8'"
  sudo -u postgres psql postgres -c "CREATE USER ${PG_USER} WITH PASSWORD '${PG_PASS}'"
  sudo -u postgres psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${PG_DB} TO ${PG_USER}"

  wget -nv https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-${SONAR_VERSION}.zip
  sudo unzip -q sonarqube-${SONAR_VERSION}.zip -d /opt
  sudo useradd ${SONAR_USER}
  sudo ln -s /opt/sonarqube-${SONAR_VERSION} ${SONAR_HOME}
  sudo chown -R ${SONAR_USER}. ${SONAR_HOME}/
  sudo sed -i -e "s|#sonar.jdbc.username=|sonar.jdbc.username=${PG_USER}|"                                                  ${SONAR_HOME}/conf/sonar.properties
  sudo sed -i -e "s|#sonar.jdbc.password=|sonar.jdbc.password=${PG_PASS}|"                                                  ${SONAR_HOME}/conf/sonar.properties
  sudo sed -i -e "s|#sonar.jdbc.url=jdbc:postgresql://localhost/sonar|sonar.jdbc.url=jdbc:postgresql://localhost/${PG_DB}|" ${SONAR_HOME}/conf/sonar.properties
  sudo sed -i -e "s|#sonar.web.context=|sonar.web.context=/sonarqube|"                                                      ${SONAR_HOME}/conf/sonar.properties
  sudo sed -i -e "s|#RUN_AS_USER=|RUN_AS_USER=${SONAR_USER}|" ${SONAR_HOME}/bin/linux-x86-64/sonar.sh
  sudo tee -a /etc/systemd/system/sonarqube.service &>/dev/null <<EOF
[Unit]
Description=SonarQube service
After=syslog.target network.target

[Service]
Type=forking
ExecStart=${SONAR_HOME}/bin/linux-x86-64/sonar.sh start
ExecStop=${SONAR_HOME}/bin/linux-x86-64/sonar.sh stop
User=${SONAR_USER}

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl enable sonarqube
  sudo systemctl start sonarqube
  sudo systemctl show sonarqube -p ActiveState >> ${LOG_FILE}
fi


########################################################################
# optionnel : zookeeper
# préférable au zookeeper intégré à solr...
########################################################################


# TODO compléter
#${INSTALL} zookeeper zookeeperd
#sudo systemctl enable zookeeper
#sudo systemctl start zookeeper
#/etc/zookeeper/conf/zoo.cfg
#/usr/share/zookeeper/bin/zkCli.sh
#éditer /etc/default/solr.in.sh


########################################################################
#
########################################################################
echo "Installation log: "${LOG_FILE}
