Wandora AngularJS Browser

=========================



Description

-----------



This folder contains an [AngularJS](http://angularjs.org/) project for a

JTM Topic Map browser implemented using AngularJS.

[Bootstap](http://getbootstrap.com/2.3.2/) is used for the styling of the app.



The implementation is completely static in the sense that a static JTM file is 

used as the source for the Topic Map. Thus no running Wandora server is

required for the use of this app.



Installation and running

------------------------



The app is contained in the `app` subfolder, and is run by opening `index.html` 

in a browser. The app relies heavily on ajax requests which blocked by default

in Chrome when opening pages from the filesystem. As a workaround the provided

Node.js server should be run with `node scripts/web-server.js`. The app is then

accsessible in `localhost:8000/app/index.html`.



The project also supports building a stripped down distribution version of the 

app using Node.js and Grunt. The required dependencies are installed with



* `npm install -g grunt-cli` for a global installation of the Grunt command 

line interface



* `npm install` for the remaining dependencies



The compilation process is finally run with `grunt dist`, which copies and 

compiles the required files to the build directory.



Configuration and customization

-------------------------------



The main configuration and translation is stored in `config.json`



* `dataUrl` is used to specify the JTM file as a relative path.

* `sis` is used to specify special topics as type-instance and super-sub

associations and players. `root` is the root topic at the root of the

subclass-superclass association tree.

* `langs` is used to specify the languages topics used in the app

* `defaultLang` is used to specify the default langauge

* `translation` is used to translate UI text elements to other languages.

A translation object should have the text literal written in the template files

as one ḱey-value-pair and it's translations as their respective key-value-pairs.



Further customization requires understaning of the Angular MVC framework.



Author

-------



 * Eero Lehtonen <eero@gripstudios.com>

 

