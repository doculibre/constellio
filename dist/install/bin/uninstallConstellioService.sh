#!/bin/bash
# -----------------------------------------------------------------------------
#                              STOP Constellio
# -----------------------------------------------------------------------------

EXECUTABLE=stopDaemon.sh
sudo bash ./"$EXECUTABLE"

EXECUTABLE=uninstallDaemon.sh
sudo bash ./"$EXECUTABLE"

cat /dev/null > ../conf/wrapper.conf