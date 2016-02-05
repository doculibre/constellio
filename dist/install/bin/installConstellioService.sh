#!/bin/bash
# -----------------------------------------------------------------------------
#                             INSTALL Constellio
# -----------------------------------------------------------------------------

java -server -Xmx4096m -XX:MaxPermSize=512m -classpath ../webapp/WEB-INF/lib/\* com.constellio.app.start.MainConstellio

EXECUTABLE=installDaemon.sh
sudo bash ./"$EXECUTABLE"