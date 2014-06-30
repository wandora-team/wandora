@echo off
echo *****************************************************
echo ***   WANDORA - THE KNOWLEDGE MANAGEMENT STUDIO   ***
echo ***     Copyright (C) 2004-2014 Wandora Team      ***
echo ***              http://wandora.org               *** 
echo *****************************************************
echo Xms750m Xmx1000m

set WANDORALIB=lib/fmj/native

call SetClasspath.bat
call SetR.bat
call SetProcessing.bat
call SetTesseract.bat

cd ..\build
PATH=%PATH%;lib/fmj/native
PATH=%PATH%;lib/jdic/windows/x86
PATH=%PATH%;lib/jdicplus/bin/
PATH=%PATH%;lib/jdicplus/lib/
PATH=%PATH%;lib/jdicplus/windows/x86/
"java" -Xms750m -Xmx1000m "-Djava.library.path=%WANDORALIB%" -classpath "%WANDORACLASSES%" -Dorg.wandora.mediafw="FMJ" org.wandora.application.Wandora %*

