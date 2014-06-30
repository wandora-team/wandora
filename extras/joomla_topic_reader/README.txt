
Description
-----------

Wandora Topics module uses SOAP and php extensions to read topics from the Wandora application
and displays the html presentation of the currently open topic.

Requirements
------------

This module requires Joomla 3.1.1 or a later version.

Information about running and configuring the Wandora application HTTP serverd can be found here:
http://www.wandora.org.

Installation
------------

1) Install the module package from the administration page
2) Enable the module.

Configuration
-------------

Wandora Topics can be configured at:
  Administration page -> Extensions -> Module manager - Wandora Topics
  
  - Show only instances:
    When is set to yes, the module only displays topic instaces, which take much less
    space and are ideal if the module has very limited space in page layout.
  
  - Default subject identifier:
    Subject identifier for the default topic.
  
  - Path to SOAP client:
    The absolute or relative path to the Wandora SOAP service.
    
Customization
-------------

* To modify the color of the generated tables:
  Open module_directory/tmpl/default.css file and modify the background-color and/or border
  parameters in these classes: .subtitle, .type and .cell.
  
Author
-------
Based on code by Elias Tertsunen / Wandora team

 * Niko Laitinen <niko@gripstudios.com> / Wandora team