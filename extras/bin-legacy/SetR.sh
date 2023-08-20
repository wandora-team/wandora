#!/bin/bash
# This bat script is executed automatically by Wandora.sh.
# You don't need to execute it manually.
R_HOME=/usr/lib/R
R_SHARE=/usr/share/R
R_SHARE_DIR=$R_SHARE/share
R_INCLUDE_DIR=$R_SHARE/include
R_DOC_DIR=$R_SHARE/doc
export R_HOME
export R_SHARE_DIR
export R_INCLUDE_DIR
export R_DOC_DIR
JRI_LD_PATH=$R_HOME/lib:$R_HOME/bin:
if test -z "$LD_LIBRARY_PATH"; then
  LD_LIBRARY_PATH=$JRI_LD_PATH
else
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JRI_LD_PATH
fi
export LD_LIBRARY_PATH
R_JAR_PATH=$R_HOME/site-library/rJava/jri/JRI.jar
WANDORACLASSES=$WANDORACLASSES:$R_JAR_PATH
WANDORALIB=$R_HOME/site-library/rJava/jri
