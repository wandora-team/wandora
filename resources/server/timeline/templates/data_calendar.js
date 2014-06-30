{     
      "dateTimeFormat": "Gregorian",
			"events":[
##
#set( $count = 0 )##
##
#set( $eventTypeSI = "http://tools.ietf.org/html/rfc5545/event/" )##
#set( $eventType = $topicmap.getTopic( $eventTypeSI ) )##
#if($topicmap.getTopic( $eventTypeSI ))
##
#set( $eventStartDateTypeSI = "http://tools.ietf.org/html/rfc5545/start-time" )##
#set( $eventStartDateType = $topicmap.getTopic( $eventStartDateTypeSI ) )##
##
#set( $eventEndDateTypeSI = "http://tools.ietf.org/html/rfc5545/end-time" )##
#set( $eventEndDateType = $topicmap.getTopic( $eventEndDateTypeSI ) )##
##
#set( $eventDescriptionTypeSI = "http://tools.ietf.org/html/rfc5545/description" )##
#set( $eventDescriptionType = $topicmap.getTopic( $eventDescriptionTypeSI ) )##
##
#set( $eventLocationTypeSI = "http://tools.ietf.org/html/rfc5545/location" )##
##
#set( $events = $topicmap.getTopicsOfType( $eventType ) )##
##
#foreach( $event in $events )##
  #if($eventStartDateType && $event.getData( $eventStartDateType,"en" ) )##
    #set( $startDate = $event.getData( $eventStartDateType,"en" ) )##
  #end
  #if($eventEndDateType && $event.getData( $eventEndDateType,"en" ) )##
    #set( $endDate = $event.getData( $eventEndDateType,"en" ) )##
  #else##
    #set( $endDate = "")##
  #end
  #set( $eventLocation = $helper.getFirstPlayer( $event, $eventLocationTypeSI, $eventLocationTypeSI ) )##
  #set( $eventTitle = $event.getDisplayName( $lang ) )##
  #if($event.getData( $eventDescriptionType,"en"))##
    #set ($eventDescription = $event.getData( $eventDescriptionType,"en"))##
    #set( $eventDescription = $eventDescription.replaceAll('\"', '\\\"' ) )##
    #set( $eventDescription = $eventDescription.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #set ($eventDescription = $eventDescription.trim())##
  #else
    #set ($eventDescription = "")
  #end
  #set( $eventTitle = $eventTitle.replaceAll('\"', '\\\"' ) )##
  #set( $eventTitle = $eventTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
  #if( $count>0 ),
  #end##
                              {
                                      "start": "$startDate",
  #if($endDate.length() != 0)##
                                      "end": "$endDate",
  #end##
                                      "title": "$eventTitle",
                                      "textColor": "#000000",
                                      "classname": "calendar-event",
                                      "description": "$eventDescription",
                                      "icon" : "${staticbase}api/images/calendar-icon.png",
                              }
  #set( $count = $count + 1 )##
#end##
#end
       ]
};