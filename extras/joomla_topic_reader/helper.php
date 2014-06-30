<?php
/**
 * Wandora Topic Reader Module Entry Point
 * 
 * @author Niko Laitinen / Wandora team
 */


/*
 * Handles the data communication with wandora application and generates
 * HTML presentatation of the whole topic or part of it.
 */
class modWandoraTopicsHelper
{
	
	var $_data;
	var $_soapClient;
	var $_functions;
	var $_types;
	var $_topic;
	var $_curTopicSi;


	// Here connection is established to soapclient and current topic
	// is loaded from there.
	function __construct( &$params )
	{  	
		if(JRequest::getVar('si') != null && JRequest::getVar('si') != "" && JRequest::getVar('wanaction') != "return") {
	      $this->_curTopicSi = JRequest::getVar('si');
	    } else {
	    	$this->_curTopicSi = $params->get( 'defaulttopic' );
	    }
	    
	    try {
	    	$this->_soapClient = new SoapClient($params->get( 'soapclient' ), array('features' => SOAP_SINGLE_ELEMENT_ARRAYS) );
		} catch (SoapFault $soapFault) {
		    echo "Error occurred when trying to connect to SOAP client: " . $soapFault;
		}
		
		$this->_functions = $this->_soapClient->__getFunctions();
		$this->_types = $this->_soapClient->__getTypes();
		$this->_topic = $this->_getMyTopic($this->_curTopicSi, true);
	}


	function getTitle()
  	{
  		$output = $this->_topic->baseName;
		if( empty($output) ) {
			$output = "[NO VALUE]";
		}
  		return $output;
 	}


  	// Generates a html table of topic variant names 
	function getVariants()
	{		
		if ( empty($this->_topic->variantNames[0]->array) && empty($this->_topic->variantLanguages[0]->array) ) {
			return;
		}
		
		$variants_data = $this->_topic->variantNames;
		$varlang_data = $this->_topic->variantLanguages;
		
		$variants = "";
		$languagePairs = array();
		
		for ($index = 0; $index < count($varlang_data); $index++) {
			foreach ($varlang_data[$index]->array as $langSI) {
				array_push($languagePairs, array('name' => $this->_getMyTopic($langSI, false)->baseName, 'si' => $langSI));
			}
		}
		
		$variants = '<table cellspacing="0" cellpadding="5" width="100%">';
		
			for($idx = 0; $idx < count($variants_data); $idx++) {
				for ($ix = 0; $ix < count($variants_data[$idx]->array); $ix++) {
					$variants .= '<tr>';
					$variants .= '<td class="table-type">' . $variants_data[$idx]->array[$ix] . '</td>';
					$variants .= '<td class="table-cell">' . $this->_getTopicUrl($languagePairs[$ix]['name'], $languagePairs[$ix]['si']) . '</td>';
					$variants .= '</tr>';
				}
			}
		
		$variants .= '</table>';
		
		return $variants;
	}


	// Generates html table of topic occurrences
	function getOccurrences()
	{		
		if( empty($this->_topic->occurrences) ) {
	  		return;
	  	}
		
		$occurrencess = $this->_topic->occurrences;
		
		$output = "";
	  	$occurrencesTypes = array();
	  	
	  	foreach ($occurrencess as $occurrences) {
	    	if(isset($occurrencesTypes[$occurrences->type]) == false) {
	      		$occurrencesTypes[$occurrences->type] = array();
	    	}
	    	
	    	array_push($occurrencesTypes[$occurrences->type], $occurrences);
	  	}
	  
	  	reset($occurrencesTypes);
		
	  	while (current($occurrencesTypes)) {
		    $occurrencesAr = current($occurrencesTypes);
		    $occuTypeTopic = $this->_getMyTopic(key($occurrencesTypes), false);
		    
		    $output .= '<table cellspacing="0" cellpadding="5" width="100%"><tr>';
		    $output .= '<td class="table-subtitle" colspan="2">' . 
		      $this->_getTopicUrl($occuTypeTopic->baseName, key($occurrencesTypes)) . '</td></tr>';
		    
		    foreach ($occurrencesAr as $pair) {
		    	$langTopic = $this->_getMyTopic($pair->version, false);
		      
		      	$output .= '<tr>';
		      
		      	$output .= '<td class="table-type">' . $this->_getTopicUrl($langTopic->baseName, $pair->version) . '</td>';
		      	$output .= '<td class="table-cell">' . $pair->content . '</td>';
		      
		      	$output .= '</tr>';
		    }
		    
		    $output .= '</table>';
		    next($occurrencesTypes);
	  	}
	  
	  return $output;
	}


	// Generates html list of topic classes
	function getClasses()
	{
	  	if( empty($this->_topic->types) ) {
	  		return;
	  	}

		$classes = array();
	
		foreach ($this->_topic->types as $type) {
	    	array_push($classes, $this->_getTopicUrl($this->_getMyTopic($type, false)->baseName, $type));
	    }
	  
	  	$output = "<ul>";
	  
	  	for ($i = 0; $i < count($classes); $i++) {
	  		$output .= "<li>" . $classes[$i] . "</li>";
	  	}
	  
	  	$output .= "</ul>";
	  
	  	return $output;
	}


	// Generates html table of topic associcatios
	function getAssociations()
	{	
	  	if( empty($this->_topic->associations) ) {
	  		return;
	  	}
	  	
	  	$associations = $this->_topic->associations;
		
	  	$output = "";
	  	$associationTypes = array();
	  
	  	foreach ($associations as $association) {
	    	if(isset($associationTypes[$association->type]) == false) {
	      		$associationTypes[$association->type] = array();
	    	}
	    	array_push($associationTypes[$association->type], $association->players);
	  	}
	  	
	  	reset($associationTypes);
	  	
	  	while (current($associationTypes)) {
	    
		    $assocationAr = current($associationTypes);
		    
		    $typeTopic = $this->_getMyTopic(key($associationTypes), false);
		    
		    $output .= '<table cellspacing="0" cellpadding="5" width="100%"><tr>';
		    $output .= '<td class="table-subtitle" colspan="2">' . $this->_getTopicUrl($typeTopic->baseName, key($associationTypes)) . '</td></tr>';
		    
		    
		    $role1 = $this->_getMyTopic($assocationAr[0][0]->role, false);
		    $role2 = $this->_getMyTopic($assocationAr[0][1]->role, false);
		    
		    $output .= '<tr><td class="table-type">' . $this->_getTopicUrl($role1->baseName, $assocationAr[0][0]->role) . '</td>';
		    
		    $output .= '<td class="table-type">' . $this->_getTopicUrl($role2->baseName, $assocationAr[0][1]->role) . '</td></tr>';
		    
	    
	    	foreach ($assocationAr as $pair) {
		      $member1 = $this->_getMyTopic($pair[0]->member, false);
		      $member2 = $this->_getMyTopic($pair[1]->member, false);
		      
		      $output .= '<tr>';
		      
		      $output .= '<td class="table-cell">' . $this->_getTopicUrl($member1->baseName, $pair[0]->member) . '</td>';
		      $output .= '<td class="table-cell">' . $this->_getTopicUrl($member2->baseName, $pair[1]->member) . '</td>';
		      
		      $output .= '</tr>';
		      
	    	}
	    	$output .= '</table>';
	    
	    	next($associationTypes);
	  	}
	  
	  return $output;
	}


	// Generates a html list of topic instances
	function getInstances()
	{		
		$instances = $this->_getMyTopicsOfType($this->_curTopicSi, false);

		if( empty($instances) ) {
    		return;
    	}
	  	
	  	$linkedInstances = array();
		
		foreach ($instances as $ins) {  
			$linked = array('name' => $ins->baseName, 'si' => NULL);
		   	$linked['si'] = $ins->subjectIdentifiers[0];
			
		    array_push($linkedInstances, $this->_getTopicUrl($linked['name'], $linked['si']));
	  	}
	  		  	
    	$output = "<ul>";
    	
    	for ($i = 0; $i < count($linkedInstances); $i++) {
      		$output .= "<li>" . $linkedInstances[$i] . "</li>";
    	}
    
    	$output .= "</ul>";
	  
	  	return $output;
	}


	// Generates urlencoded url from topic subject identifier
	function _getTopicUrl($name, $si)
	{
		$url = JURI::root() . "?wanaction=siload&si=" . urlencode($si);
	 	#$url = "http://eclipse.dev/Joomla_1_5_source/?option=mod_wantopic&testing=isokay&si=" . urlencode($si);
	  
	  	$link = '<a id="wand_sp_link" href="' . $url . '">' . $name . "</a>";
	  	//$link = JHTML::link($url, $name);

	 	return $link;
	}


	// Loads topic from soapclient and return it in object/array format
	function _getMyTopic($si, $full) 
	{
		$getTopicParam = array("si" => $si, "full" => $full);
	    
	    try {
	    	$getTopicResponse = $this->_soapClient->getTopic($getTopicParam);
	       	return $getTopicResponse->return;
	    } catch (SoapFault $soapFault) {
	       	echo "<p>Error occurred when tried to load following topic: " . $si . "<br/>Error:" . $soapFault;
	       	return array(); 
	    }
	}


	// Returns all the topics that are type of the given subject identifier
	function _getMyTopicsOfType($si, $full)
	{
		$return = null;
		$getTopicsOfTypeParam = array("si" => $si, "full" => $full);
		$getTopicsOfTypeResponse = $this->_soapClient->getTopicsOfType($getTopicsOfTypeParam);
		if( isset($getTopicsOfTypeResponse->return) ) {
			$return = $getTopicsOfTypeResponse->return;
		}
		
		return $return;
	}
}
?>