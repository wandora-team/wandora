<?php
defined( '_JEXEC' ) or die( 'Restricted access' );
?>
<div id="wandora-r-div">
<a id="wand_sp_link" href="<?php echo JURI::root();?>?wanaction=return">TOP</a>

<h3><?php echo $topicReader->getTitle();?></h3>

<h2>Variant names</h2>
<?php echo $topicReader->getVariants();?>

<h2>Occurrences</h2>
<?php echo $topicReader->getOccurrences();?>

<h2>Classes</h2>
<?php echo $topicReader->getClasses();?>

<h2>Associations</h2>
<?php echo $topicReader->getAssociations();?>

<h2>Instances</h2>
<?php echo $topicReader->getInstances();?>

<br/><br/><br/>
</div>