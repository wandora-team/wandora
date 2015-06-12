#!/bin/bash
# This bat script is executed automatically by Wandora.sh.
# You don't need to execute it manually.

# Depending on JRE version you may have to change
# the P_ARCH value to 32 or 64.
P_ARCH=32
# JRI_LD_PATH=lib/processing/serial/linux$P_ARCH:lib/processing/opengl/linux$P_ARCH:
if test -z "$LD_LIBRARY_PATH"; then
  LD_LIBRARY_PATH=$JRI_LD_PATH
else
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JRI_LD_PATH
fi
export LD_LIBRARY_PATH
WANDORACLASSES=$WANDORACLASSES:lib/processing/*
# WANDORACLASSES=$WANDORACLASSES:lib/processing/serial/*
# WANDORACLASSES=$WANDORACLASSES:lib/processing/opengl/*
# WANDORALIB=$WANDORALIB:lib/processing/serial/linux$P_ARCH:lib/processing/opengl/linux$P_ARCH
