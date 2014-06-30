<?php
defined( '_JEXEC' ) or die( 'Restricted access' ); 
?>
<div id="wandora-r-div">
<a id="wand_sp_link" href="<?php echo JURI::root();?>?wanaction=return">Default topic</a>

<h2>Instances of <?php echo $topicReader->getTitle();?></h2>
<?php echo $topicReader->getInstances();?>

</div>