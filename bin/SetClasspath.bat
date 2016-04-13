REM This bat script is executed automatically by Wandora.bat.
REM You don't need to execute it manually.
set WANDORACLASSES=lib/wandora.jar
set WANDORACLASSES=%WANDORACLASSES%;resources
set WANDORACLASSES=%WANDORACLASSES%;lib/odftoolkit/*
set WANDORACLASSES=%WANDORACLASSES%;lib/solr/*
set WANDORACLASSES=%WANDORACLASSES%;lib/solr/solrj-lib/*
set WANDORACLASSES=%WANDORACLASSES%;lib/jena-2.13.0/*
set WANDORACLASSES=%WANDORACLASSES%;lib/jmbox/*
set WANDORACLASSES=%WANDORACLASSES%;lib/jtidy/*
set WANDORACLASSES=%WANDORACLASSES%;lib/gdata/*
set WANDORACLASSES=%WANDORACLASSES%;lib/jetty/*
set WANDORACLASSES=%WANDORACLASSES%;lib/poi/*
set WANDORACLASSES=%WANDORACLASSES%;lib/musicbrainz/*
set WANDORACLASSES=%WANDORACLASSES%;lib/axis2/*
set WANDORACLASSES=%WANDORACLASSES%;lib/pdfbox/*
set WANDORACLASSES=%WANDORACLASSES%;lib/any23/*
set WANDORACLASSES=%WANDORACLASSES%;lib/stanford-ner/*
set WANDORACLASSES=%WANDORACLASSES%;lib/gate/*
set WANDORACLASSES=%WANDORACLASSES%;lib/gate/lib/*
set WANDORACLASSES=%WANDORACLASSES%;lib/mstor/*
set WANDORACLASSES=%WANDORACLASSES%;lib/twitter/*
set WANDORACLASSES=%WANDORACLASSES%;lib/ical4j/*
set WANDORACLASSES=%WANDORACLASSES%;lib/df/*
set WANDORACLASSES=%WANDORACLASSES%;lib/unirest/*
set WANDORACLASSES=%WANDORACLASSES%;lib/tmql4j/*
set WANDORACLASSES=%WANDORACLASSES%;lib/*
REM  To use the Webview Panel with Java 7 fix next line to address the location of JavaFX jar jfxrt.jar .
REM  Uncomment next line if you are using Java 7. 
REM  Keep next line commented if you are using Java 8.
REM set WANDORACLASSES=%WANDORACLASSES%;C:\Program Files\Java\jre7\lib\jfxrt.jar
