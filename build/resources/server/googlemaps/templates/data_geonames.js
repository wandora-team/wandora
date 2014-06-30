## Helper macros
## Escapes quotation marks inside strings
#macro( geo_plain_text $arg )##
#if( ! $arg )#set( $arg = "" )#end##
#set( $ptxt = $arg.replaceAll('"', ' ') )##
#set( $ptxt = $ptxt.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
"$ptxt"##
#end##

// Three roles of location association type
#set( $geoLatitudeSI = "http://www.geonames.org/lat" )##
#set( $geoLatitudeItem = $topicmap.getTopic( $geoLatitudeSI ) )##
#set( $geoLongitudeSI = "http://www.geonames.org/lng" )##
#set( $geoLongitudeItem = $topicmap.getTopic( $geoLongitudeSI ) )##
#set( $geoLocationSI = "http://www.geonames.org/located" )##
#set( $geoLocationItem = $topicmap.getTopic( $geoLocationSI ) )##

#set( $geoLocationTypeSI = "http://www.geonames.org/location" )##
#set( $geoLocationType = $topicmap.getTopic( $geoLocationTypeSI ) )##
#if( $geoLocationType )##
    #set( $geoLocations = $topicmap.getAssociationsOfType( $geoLocationType ) )##
#end##

var geo_loc_data = new Array();

## Limit for processed topics, set 0 or less for no limit.
#set( $count_limit = 0 )##

## Extract data need for Google Maps
#if( $geoLocations && $geoLocations.size()!=0 )##
    #foreach( $location in $geoLocations )##
        #set( $latitude = $location.getPlayer($geoLatitudeItem)  )##
        #set( $longitude = $location.getPlayer($geoLongitudeItem) )##
        #set( $location_info = $location.getPlayer($geoLocationItem) )##
        geo_loc_data.push({
            latlng: new google.maps.LatLng( $latitude.getDisplayName($lang), $longitude.getDisplayName($lang) ),
            title: #geo_plain_text( $location_info.getDisplayName($lang) ),
            statistics: [
                #set( $data_types = $location_info.getDataTypes() )##
                #if( $data_types.size()!=0 )##
                    #foreach( $type in $data_types )##
                        {name:#geo_plain_text( $type.getDisplayName($lang) ), value:#geo_plain_text( $location_info.getData($type, $lang ) )},
                    #end##
                #end##
            ]
        });
        #set( $count_limit = $count_limit - 1 )##
        #if($count_limit == 0)##
            #break##
        #end##
    #end##
#end##

## Create markers for Google Maps from extracted data
for(var i=0;i<geo_loc_data.length;i++) {
    var latlng = geo_loc_data[i].latlng;
    coordinates.push(latlng);
    ## Marker popup content
    var info_html = '<div id="info-content">';
    info_html = "<h2>"+geo_loc_data[i].title+"</h2>";
    if(geo_loc_data[i].statistics.length > 0) {
        info_html += '<ul id="statistics">';
        for(var j=0;j<geo_loc_data[i].statistics.length;j++) {
            info_html += "<li>";
            info_html += geo_loc_data[i].statistics[j].name + ": "+geo_loc_data[i].statistics[j].value;
            info_html += "</li>";
        }
        info_html += '</ul>';
    }
    info_html += '</div>';

    makeMarker({
        position: latlng,
        title: geo_loc_data[i].title,
        map: map,
        content: info_html,
        icon: "${staticbase}icons/google_maps_icon_geonames.png"
    });
}
