#!/bin/bash
echo "*****************************************************"
echo "***   WANDORA - THE KNOWLEDGE MANAGEMENT STUDIO   ***"
echo "***     Copyright (C) 2004-2015 Wandora Team      ***"
echo "***              http://wandora.org               ***" 
echo "*****************************************************"
echo "Xms3000m Xmx3500m"

WANDORALIB=

source SetClasspath.sh
source SetR.sh
source SetProcessing.sh
source SetTesseract.sh

cd ../build
java -Xms3000m -Xmx3500m -Djava.library.path=$WANDORALIB -Dorg.wandora.mediafw="GST" -classpath $WANDORACLASSES org.wandora.application.Wandora $1
