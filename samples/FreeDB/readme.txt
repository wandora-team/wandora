This directory contains a sample project 'FreeDB-sample.wpr' for
Wandora. Project file demonstrates Wandora's FreeDB extractor and
simple modification layer used to merge topics with typos.




Project contains three layers: Artist merges, FreeDB-samples,
and Base.

Base topic map is Wandora's default topic map.

FreeDB-samples layer was created with Wandora's FreeDB extractor and
contains discographical information of 3701 rock albums. This is just
a small portion of freedb's total database. More about
freebd is found at http://www.freedb.org/.

Artist merges layer demonstrates simple modification layer that
merges some of the underlying topics into a single topic. Merge
is required to fix different artist name variants for example. See
'Jerry Lee Lewis' for example. Artist merges layer collects all name
variants

http://wandora.org/si/freedb/artist/Jerry_Lee_Lewis_
http://wandora.org/si/freedb/artist/Jerry_Lee_Lewis
http://wandora.org/si/freedb/artist/JERRY_LEE_LEWIS
http://wandora.org/si/freedb/artist/Jerry_lee_Lewis

into a single topic. Because fixes locate in a separate layer, the
FreeDB-sample layer may be replaced any time. It may even locate in
a third party database you just connect with your Wandora application.




  