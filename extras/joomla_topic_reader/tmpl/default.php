<?php // no direct access
defined( '_JEXEC' ) or die( 'Restricted access' );

header("Cache-Control: no-cache, must-revalidate"); // HTTP/1.1
header('Expires: ' . gmdate('D, d M Y H:i:s', time()+24*60*60) . ' GMT');
header('Content-type: application/javascript');
?>
<script>

var wantopic_url = '<?php echo JURI::root(); ?>';

window.addEvent('domready', function() {
  linkify();
});

// Changes the links in module to perform ajax requests by using the links
// subject identifier.
function linkify()
{
	var list = $$('a');

	  // Loop every link in the page
	  for ( var int = 0; int < list.length; int++) {
	    
	    if(list[int].id == 'wand_sp_link')
	    {
	      var spLink = list[int];
	      
	      spLink.addEvent('click', function(e) {
	        e = new Event(e).stop();
	        var url = "?wanaction=siload";
	        var si = this.href;

	        // Making the ajax request
	        new Ajax(url, {
	          method: 'post',
	          data: si,
	          update: $('wandora-r-div'),
	          onComplete: linkify
	        }).request();
	      });
	    }
	    
	  }
}
</script>
<?php 
if($params->get( 'showinstances', false )) {
	// Shows limited view of topic for smaller spaces in page layout
	require_once( 'modules/mod_wandora_topics/tmpl/ajax_instances.php' );
} else {
	require_once( 'modules/mod_wandora_topics/tmpl/ajax.php' );
}
?>
