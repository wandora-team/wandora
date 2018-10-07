Wandora
=======

[Wandora](http://wandora.org) is a tool application for people who collect and process information, 
especially networked knowledge and knowledge about WWW resources. With Wandora you can 
aggregate and combine information from various different sources. You can manipulate the 
collected knowledge flexible and efficiently, and without programming skills. More 
generally speaking Wandora is a general purpose information extraction, management 
and publishing application based on [Topic Maps](http://en.wikipedia.org/wiki/Topic_Maps) 
and [Java](http://en.wikipedia.org/wiki/Java_%28programming_language%29). Wandora suits well for 
constructing and maintaining vocabularies, taxonomies and ontologies. 
Application areas include linked data, open data, data integration, business 
intelligence, digital preservation and data journalism. 
Wandora's license is [GNU GPL](http://www.gnu.org/licenses/gpl-3.0.txt).

## Install and use Wandora

[Download](http://wandora.org/www/download), 
[install](http://wandora.org/wiki/How_to_install_Wandora) and 
[run](http://wandora.org/wiki/Running_Wandora) Wandora on your computer.

Read the [quick start](http://wandora.org/wiki/Quickstart) and 
browse the [documentation](http://wandora.org/wiki/Main_Page). We also provide 
[screen cast videos](http://wandora.org/tv/) that may help new users.

If you run into any trouble or have questions consult our [forum](http://wandora.org/forum/)
or drop a line.

## Developer Introduction

Wandora was originally developed with the [Netbeans IDE](https://netbeans.apache.org/).
Since October 2018 we have started developing Wandora with Eclipse, preferably 
Eclipse IDE for Java Developers version 4.9.0. Eclipse development has changed
the library retrieval and build process. Since October 2018 Wandora's Git repository
doesn't contain jar libraries any more in the lib directory. To retrieve all
required jar libraries, the developer should run ant task retrieve in the
build.xml. After retrieval the Eclipse user should refresh package explorer view,
and the developer is ready to build Wandora with the build task in the build.xml.

Build task creates Wandora's binary distribution package in to the dist directory.
To run Wandora application enter folder dist/bin and execute startup script Wandora.bat
or Wandora.sh.

To compile and run Wandora, a Java JDK 8 or later is required.

Developers interested in Wandora project should note that our long term plans
include using Eclipse as a primary development tool.

### 