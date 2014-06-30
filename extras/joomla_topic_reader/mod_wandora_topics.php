<?php
/**
 * Wandora Wandora Topic Reader Module
 * 
 * Uses soap_php extension to read topics from Wandora application and
 * displays html presentation from the currently open topic.
 * 
 * Copyright (C) 2004-2010 Grip Studios Interactive, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Niko Laitinen / Wandora team
 */
 
// no direct access
defined( '_JEXEC' ) or die( 'Restricted access' );
if(!defined('DS')){
    define('DS',DIRECTORY_SEPARATOR);
}

// Include the syndicate functions only once
require_once (dirname(__FILE__).DS.'helper.php');

$topicReader = new modWandoraTopicsHelper( $params );

// If url request is made by using ajax, then display updated topic map
$isAjaxRequest = isset($_SERVER["HTTP_X_REQUESTED_WITH"])?($_SERVER["HTTP_X_REQUESTED_WITH"] == "XMLHttpRequest") : false;
if ($isAjaxRequest) {
	if($params->get( 'showinstances', false )) {
	  require_once( 'modules/mod_wandora_topics/tmpl/ajax_instances.php' );
	} else {
	  require_once( 'modules/mod_wandora_topics/tmpl/ajax.php' );
	}
  exit;
}

require( JModuleHelper::getLayoutPath( 'mod_wandora_topics' ) );

$doc =& JFactory::getDocument();
$doc->addStyleSheet( 'modules/mod_wandora_topics/tmpl/default.css' );
?>