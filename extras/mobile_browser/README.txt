
Description
-----------

This folder contains a JavaScript based jtm topic map browser. It is built using jQuery and
jQueryMobile making it well suited for various mobile platforms. You should be able to use
it with most modern desktop web browsers as well.

This does not require Wandora itself to be running at all. The entire jtm topic map is read
by the JavaScript and dynamic html pages are then built by manipulating the DOM tree.


Installation and running
------------------------

Basic use doesn't require any special installation steps. Simply open tmbrowser.html in
your web browser. To be able to use it on a mobile platform, you will likely need an
http server running and then make all the files in this folder available through that.
Then open the tmbrowser.html in your mobile browser.


Configuration and customization
-------------------------------

By default the included ArtOfNoise.jtm topic map is used. This along with a few other
options can be changed at the start of tmbrowser.html, from line 30 onwards. Most important
things are the URL for the jtm topic map and the start page.

There is also a list of subject identifier prefixes. It is not necessary to have these for
every subject identifier in the topic map, they simply make the URLs of pages neater.

Customization beyond that requires changes in the css stylesheets, the DOM structure of
the page and the JavaScript that builds the pages.


Author
-------

 * Olli Lyytinen <olli@gripstudios.com>
 
