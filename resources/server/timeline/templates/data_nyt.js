{
      "dateTimeFormat": "Gregorian",
			"events":[
##
#set( $count = 0 )##
##
#set( $eventTypeSI = "http://api.nytimes.com/svc/events/v2" )##
#set( $eventType = $topicmap.getTopic( $eventTypeSI ) )##
##
#set( $eventDateTypeSI = "http://wandora.org/si/nytimes/eventDate" )##
#set( $eventDateType = $topicmap.getTopic( $eventDateTypeSI ) )##
##
#set( $eventStartDateTypeSI = "http://wandora.org/si/nytimes/startDate" )##
#set( $eventStartDateType = $topicmap.getTopic( $eventStartDateTypeSI ) )##
##
#set( $eventEndDateTypeSI = "http://wandora.org/si/nytimes/endDate" )##
#set( $eventEndDateType = $topicmap.getTopic( $eventEndDateTypeSI ) )##
##
#set( $eventDescriptionTypeSI = "http://wandora.org/si/nytimes/event/description" )##
#set( $eventDescriptionType = $topicmap.getTopic( $eventDescriptionTypeSI ) )##
##
#set( $eventVenueTypeSI = "http://wandora.org/si/nytimes/event/venue" )##
#set( $eventVenueType = $topicmap.getTopic( $eventVenueTypeSI ) )##
##
#set( $events = $topicmap.getTopicsOfType( $eventType ) )##
##
#foreach( $event in $events )##
  #if($eventDateType && $event.getData( $eventDateType,"en" ) )##
    #set( $startDate = $event.getData( $eventDateType,"en" ) )##
    #set( $endDate = "" )##
  #elseif($eventStartDateType && $eventEndDateType && $event.getData( $eventStartDateType,"en" ) && $event.getData( $eventEndDateType,"en" ) )##
    #set( $startDate = $event.getData( $eventStartDateType,"en" ) )##
    #set( $endDate = $event.getData( $eventEndDateType,"en" ) )##
  #end

  #if($startDate)##

    #set( $eventVenue = $helper.getFirstPlayer( $event, $eventVenueTypeSI, $eventVenueTypeSI ) )##
    #set( $eventLink = $event.getOneSubjectIdentifier() )##
    #set( $eventTitle = $event.getDisplayName( $lang ) )##
    #if($event.getData( $eventDescriptionType,"en"))##
      #set ($eventDescription = $event.getData( $eventDescriptionType,"en"))##
      #set( $eventDescription = $eventDescription.replaceAll('\"', '\\\"' ) )##
      #set( $eventDescription = $eventDescription.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
      #set ($eventDescription = $eventDescription.trim())##
    #end
    #set( $eventTitle = $eventTitle.replaceAll('\"', '\\\"' ) )##
    #set( $eventTitle = $eventTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #if( $count>0 ),
    #end##
                                {
                                        "start": "$startDate",
    #if(!$endDate.isEmpty())##
                                        "end": "$endDate",
    #end##
                                        "title": "$eventTitle @ $eventVenue",
                                        "textColor": "#000000",
                                        "classname": "nyt-event",
                                        "description": "$eventDescription",
                                        "icon" : "${staticbase}api/images/nyt-icon.png",
                                        "link" : "$eventLink"
                                }
    #set( $count = $count + 1 )##

  #end##
#end##
       ]
};


#set( $eventType = false )##
#set( $events = false )##
