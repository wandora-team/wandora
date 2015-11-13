#!/bin/bash
echo "*****************************************************"
echo "***   WANDORA - THE KNOWLEDGE MANAGEMENT STUDIO   ***"
echo "***     Copyright (C) 2004-2015 Wandora Team      ***"
echo "***              http://wandora.org               ***" 
echo "*****************************************************"
echo "Xms320m Xmx512m"

WANDORALIB=

source SetClasspath.sh
source SetR.sh
source SetProcessing.sh
source SetTesseract.sh

cd ../build
# java -Xms320m -Xmx512m -Xdock:icon=resources/gui/appicon/wandora.icns -Djava.library.path=$WANDORALIB -classpath $WANDORACLASSES org.wandora.application.Wandora $1
java -Xms320m -Xmx512m -Djava.library.path=$WANDORALIB -classpath $WANDORACLASSES org.wandora.application.Wandora $1
