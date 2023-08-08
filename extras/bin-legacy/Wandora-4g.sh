#!/bin/bash
echo "*****************************************************"
echo "***   WANDORA - THE KNOWLEDGE MANAGEMENT STUDIO   ***"
echo "***     Copyright (C) 2004-2017 Wandora Team      ***"
echo "***              http://wandora.org               ***" 
echo "*****************************************************"
echo "Xms3000m Xmx3500m"

WANDORALIB=

source SetClasspath.sh
source SetR.sh
source SetProcessing.sh
source SetTesseract.sh

cd ..
if [ "$(uname)" == "Darwin" ]; then
 java -Xms3000m -Xmx3500m -Xdock:icon=gui/appicon/wandora.icns -Djava.library.path=$WANDORALIB -classpath $WANDORACLASSES org.wandora.application.Wandora $1
else
 java -Xms3000m -Xmx3500m -Djava.library.path=$WANDORALIB -classpath $WANDORACLASSES org.wandora.application.Wandora $1
fi