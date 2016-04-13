@echo off
echo *****************************************************
echo ***   WANDORA - THE KNOWLEDGE MANAGEMENT STUDIO   ***
echo ***     Copyright (C) 2004-2016 Wandora Team      ***
echo ***              http://wandora.org               *** 
echo *****************************************************
echo Xms1000m Xmx1400m

set WANDORALIB=

call SetClasspath.bat
call SetR.bat
call SetProcessing.bat
call SetTesseract.bat

cd ..

"java" -Xms1000m -Xmx1400m "-Djava.library.path=%WANDORALIB%" -classpath "%WANDORACLASSES%" org.wandora.application.Wandora %*

