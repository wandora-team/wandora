REM This bat script is executed automatically by Wandora.bat.
REM You don't need to execute it manually.

REM Depending on JRE version you may have to change
REM the P_ARCH value to 32 or 64.

set P_ARCH=64;

PATH=%PATH%;lib\processing\serial\windows%P_ARCH%;lib\processing\opengl\windows%P_ARCH%

set WANDORACLASSES=%WANDORACLASSES%;lib\processing\*
set WANDORACLASSES=%WANDORACLASSES%;lib\processing\serial\*
set WANDORACLASSES=%WANDORACLASSES%;lib\processing\opengl\*

set WANDORALIB=%WANDORALIB%;lib\processing\serial\windows%P_ARCH%;lib\processing\opengl\windows%P_ARCH%