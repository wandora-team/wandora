#!/bin/bash
echo "*****************************************************"
echo "***   WANDORA - THE KNOWLEDGE MANAGEMENT STUDIO   ***"
echo "***     Copyright (C) 2004-2017 Wandora Team      ***"
echo "***              http://wandora.org               ***" 
echo "*****************************************************"
echo "Xms12000m Xmx15000m"

WANDORALIB=

source SetClasspath.sh
source SetR.sh
source SetProcessing.sh
source SetTesseract.sh

cd ..
if [ "$(uname)" == "Darwin" ]; then
 java -Xms12000m -Xmx15000m -Xdock:icon=resources/gui/appicon/wandora.icns -Djava.library.path=$WANDORALIB -classpath $WANDORACLASSES org.wandora.application.Wandora $1
else
 java -Xms12000m -Xmx15000m -Djava.library.path=$WANDORALIB -classpath $WANDORACLASSES org.wandora.application.Wandora $1
fi