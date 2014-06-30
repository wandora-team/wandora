## Helper macros
## Escapes quotation marks inside strings
#macro( pk_plain_text $arg )##
#if( ! $arg )#set( $arg = "" )#end##
#set( $ptxt = $arg.replaceAll('"', ' ') )##
#set( $ptxt = $ptxt.replace('[\p{Cntrl}\p{Space}]', ' ') )##
"$ptxt"##
#end##

#set( $pkUnitSI = "http://www.hel.fi/palvelukarttaws/rest/v1/unit" )##
#set( $pkUnitItem = $topicmap.getTopic( $pkUnitSI ) )##

#set( $pkLocationSi = "http://wandora.org/si/palvelukartta/geo-location" )##
#set( $pkLocationItem = $topicmap.getTopic( $pkLocationSi ) )##

#if($pkUnitItem)##
    #set( $pk_units = $topicmap.getTopicsOfType( $pkUnitItem ) )##
#end##

## Limit for processed topics, set 0 or less for no limit.
#set( $count_limit = 0 )##

## Extract data need for Google Maps
var unit_locations = [
#if( $pk_units && $pk_units.size()!=0 )##
	#foreach( $unit in $pk_units )##
	{
		#set( $location = $unit.getData( $pkLocationItem, $lang ) )##
		#set ( $title = $unit.getDisplayName($lang) )##
		title: #pk_plain_text($title),
		location: #pk_plain_text( $location ),
		#set( $as_map = $mapmaker.make() )## Hashmap for associations
		#set( $associations = $unit.getAssociations() )##
		#foreach( $layer in $associations )##
			#set( $type_title = $layer.getType().getDisplayName($lang) )##
			#if(!$as_map.containsKey($type_title) )##
				#set($temp = $as_map.put($type_title, []))##
			#end##
			#set($vals = $as_map.get($type_title))##
			#set( $roles = $layer.getRoles() )##
			#foreach($role in $roles) ##
				#set( $player = $layer.getPlayer($role) )##
				#if(!$tmbox.isTopicOfType($player, $pkUnitItem) )##
					#set( $player_title = $player.getDisplayName($lang) )##
					#set($temp = $vals.add($player_title))##
				#end ##
			#end ##
			#set($temp = $as_map.put($type_title, $vals) ) ##
		#end##
		
		extra_data: [
		#set( $data_types = $unit.getDataTypes() )##
		#if( $data_types.size()!=0 )##
			#foreach( $type in $data_types )##
				#if($type.getOneSubjectIdentifier().toExternalForm() != $pkLocationSi)##
				{
				name:#pk_plain_text($type.getDisplayName($lang) ),
				values:[#pk_plain_text( $unit.getData($type, $lang) )]
				},
				#end##
			#end##
		#end##
		
		#foreach ($entry in $as_map.entrySet())
		{
			name: #pk_plain_text($entry.key),
			values: [
			#foreach($value in $entry.value)##
			#pk_plain_text($value),
			#end##
			]
		},
		#end
		],
		},
		#set( $count_limit = $count_limit - 1 )##
		#if($count_limit == 0)##
			#break##
		#end##
		
	
	#end##
#end##

];

## Create markers for Google Maps from extracted data
for(var i=0;i<unit_locations.length;i++) {

	var raw_latlng = unit_locations[i].location.split(",");
	var latlng = new google.maps.LatLng(raw_latlng[1],raw_latlng[0]);
	coordinates.push(latlng);
	
	## Marker popup content
	var info_html = '<div id="info-content">';
	info_html = "<h2>"+unit_locations[i].title+"</h2>";
	if(unit_locations[i].extra_data.length > 0) {
		info_html += '<ul id="statistics">';
		for(var j=0;j<unit_locations[i].extra_data.length;j++) {
			
			if(unit_locations[i].extra_data[j].values.length > 1) {
				info_html += "<li><strong>" + unit_locations[i].extra_data[j].name + "</strong></li>";
				info_html += '<ul>';
				for(var k=0;k<unit_locations[i].extra_data[j].values.length;k++) {
					info_html += "<li>" + unit_locations[i].extra_data[j].values[k] + "</li>";
				}
				info_html += '</ul>';
			} else {
				info_html += "<li>";
				info_html += "<strong>"+unit_locations[i].extra_data[j].name + ":</strong> "+unit_locations[i].extra_data[j].values[0];
				info_html += "</li>";
			}
			
			
		}
		info_html += '</ul>';
	}
	info_html += '</div>';
	
	makeMarker({
		position: latlng,
		title: unit_locations[i].title,
		map: map,
		content: info_html,
		icon: "${staticbase}icons/google_maps_icon_palvelukartta.png"
	});
	
}
