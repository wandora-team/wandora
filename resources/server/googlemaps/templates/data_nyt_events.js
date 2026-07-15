## Helper macros
## Escapes quotation marks inside strings
#macro( nyt_service_plain_text $arg )##
#if( ! $arg )#set( $arg = "" )#end##
#set( $ptxt = $arg.replaceAll('"', ' ') )##
#set( $ptxt = $ptxt.replace('[\p{Cntrl}\p{Space}]', ' ') )##
"$ptxt"##
#end##

#set( $nytEventSI = "http://api.nytimes.com/svc/events/v2" )##
#set( $nytEventItem = $topicmap.getTopic( $nytEventSI ) )##

#set( $nytLatitudeSi = "http://wandora.org/si/nytimes/event/latitude" )##
#set( $nytLatitudeItem = $topicmap.getTopic( $nytLatitudeSi ) )##
#set( $nytLongitudeSi = "http://wandora.org/si/nytimes/event/longitude" )##
#set( $nytLongitudeItem = $topicmap.getTopic( $nytLongitudeSi ) )##
#if( $nytEventItem )##
    #set( $nyt_events = $topicmap.getTopicsOfType( $nytEventItem ) )##
#end##

## Limit for processed topics, set 0 or less for no limit.
#set( $count_limit = 0 )##

## Extract data need for Google Maps
var nyt_event_locations = [
#if( $nyt_events && $nyt_events.size()!=0 )##
    #foreach( $event in $nyt_events )##
    {
        #set( $lat = $event.getData( $nytLatitudeItem, $lang ) )##
        #set( $long = $event.getData( $nytLongitudeItem, $lang ) )
        #set ( $title = $event.getDisplayName($lang) )##
        title: "$esc.escapeJavaScript($title)",
        location: "$lat,$long",
        #set( $as_map = $mapmaker.make() )## Hashmap for associations
        #set( $associations = $event.getAssociations() )##
        #foreach( $layer in $associations )##
            #set( $type_title = $layer.getType().getDisplayName($lang) )##
            #if(!$as_map.containsKey($type_title) )##
                #set($temp = $as_map.put($type_title, []))##
            #end##
            #set($vals = $as_map.get($type_title))##
            #set( $roles = $layer.getRoles() )##
            #foreach($role in $roles) ##
                #set( $player = $layer.getPlayer($role) )##
                #if(!$tmbox.isTopicOfType($player, $nytEventItem) )##
                    #set( $player_title = $player.getDisplayName($lang) )##
                    #set($temp = $vals.add($player_title))##
                #end ##
            #end ##
            #set($temp = $as_map.put($type_title, $vals) ) ##
        #end##
        
        extra_data: [
        #set( $data_types = $event.getDataTypes() )##
        #if( $data_types.size()!=0 )##
            #foreach( $type in $data_types )##
                #if($type.getOneSubjectIdentifier().toExternalForm() != $nytLocationSi)##
                {
                name:"$esc.escapeJavaScript($type.getDisplayName($lang) )",
                values:["$esc.escapeJavaScript( $event.getData($type, $lang) )"]
                },
                #end##
            #end##
        #end##
        #foreach ($entry in $as_map.entrySet())
        {
            name: "$esc.escapeJavaScript($entry.key)",
            values: [
            #foreach($value in $entry.value)##
            "$esc.escapeJavaScript($value)",
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
for(var i=0;i<nyt_event_locations.length;i++) {

    var raw_latlng = nyt_event_locations[i].location.split(",");
    var latlng = new google.maps.LatLng(raw_latlng[0],raw_latlng[1]);
    
    var loc_data = nyt_event_locations[i];
    
    coordinates.push(latlng);
    
    ## Marker popup content
    var info_html = '<div id="info-content">';
    info_html = "<h2>"+loc_data.title+"</h2>";
    if(loc_data.extra_data.length > 0) {
        info_html += '<ul id="statistics">';
        for(var j=0;j<loc_data.extra_data.length;j++) {
            
            if(loc_data.extra_data[j].values.length > 1) {
                info_html += "<li><strong>" + loc_data.extra_data[j].name + "</strong></li>";
                info_html += '<ul>';
                for(var k=0;k<loc_data.extra_data[j].values.length;k++) {
                    info_html += "<li>" + loc_data.extra_data[j].values[k] + "</li>";
                }
                info_html += '</ul>';
            } else {
                info_html += "<li>";
                info_html += "<strong>"+loc_data.extra_data[j].name + ":</strong> "+loc_data.extra_data[j].values[0];
                info_html += "</li>";
            }
            
            
        }
        info_html += '</ul>';
    }
    info_html += '</div>';
    
    makeMarker({
        position: latlng,
        title: loc_data.title,
        map: map,
        content: info_html,
        icon: "${staticbase}icons/google_maps_icon_nyt.png"
    });
    
}
