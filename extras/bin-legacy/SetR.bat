REM This bat script is executed automatically by Wandora.bat.
REM You don't need to execute it manually.

set R_HOME=%ProgramFiles%\R\R-3.1.1
set R_SHARE=%R_HOME%

REM - This must be same as your Java installation
REM set R_ARCH=i386
set R_ARCH=x64

set R_SHARE_DIR=%R_SHARE%\share
set R_INCLUDE_DIR=%R_SHARE%\include
set R_DOC_DIR=%R_SHARE%\doc

PATH=%PATH%;%R_HOME%\bin\%R_ARCH%

set R_JAR_PATH=%R_HOME%\library\rJava\jri\JRI.jar

set WANDORACLASSES=%WANDORACLASSES%;%R_JAR_PATH%
set WANDORALIB=%WANDORALIB%;%R_HOME%\library\rJava\jri\%R_ARCH%
