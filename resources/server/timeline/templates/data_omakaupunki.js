{     
      "dateTimeFormat": "Gregorian",
			"events":[
##
#set( $count = 0 )##
##
#set( $eventTypeSI = "http://omakaupunki.fi/event/" )##
#set( $eventType = $topicmap.getTopic( $eventTypeSI ) )##
#if($topicmap.getTopic( $eventTypeSI ))
##
#set( $eventStartDateTypeSI = "http://omakaupunki.fi/event/start-time" )##
#set( $eventStartDateType = $topicmap.getTopic( $eventStartDateTypeSI ) )##
##
#set( $eventEndDateTypeSI = "http://omakaupunki.fi/event/end-time" )##
#set( $eventEndDateType = $topicmap.getTopic( $eventEndDateTypeSI ) )##
##
#set( $eventDescriptionTypeSI = "http://omakaupunki.fi/event/body" )##
#set( $eventDescriptionType = $topicmap.getTopic( $eventDescriptionTypeSI ) )##
##
#set( $events = $topicmap.getTopicsOfType( $eventType ) )##
##
#foreach( $event in $events )##
  #if($eventStartDateType && $event.getData( $eventStartDateType,"fi" ) )##
    #set( $startDate = $event.getData( $eventStartDateType,"fi" ) )##
  #end
  #if($eventEndDateType && $event.getData( $eventEndDateType,"fi" ) )##
    #set( $endDate = $event.getData( $eventEndDateType,"fi" ) )##
  #else##
    #set( $endDate = "")##
  #end
  #set( $eventVenue = $helper.getFirstPlayer( $event, $eventVenueTypeSI, $eventVenueTypeSI ) )##
  #set( $eventLink = $event.getOneSubjectIdentifier() )##
  #set( $eventTitle = $event.getDisplayName( $lang ) )##
  #if($event.getData( $eventDescriptionType,"fi"))##
    #set ($eventDescription = $event.getData( $eventDescriptionType,"fi"))##
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
  #if($endDate.length() != 0)##
                                      "end": "$endDate",
  #end##
                                      "endEqSt" : "$endDate.equals($startDate)",
                                      "title": "$eventTitle",
                                      "textColor": "#000000",
                                      "classname": "omakaupunki-event",
                                      "description": "$eventDescription",
                                      "icon" : "${staticbase}api/images/omakaupunki-icon.png",
                                      "link" : "$eventLink"
                              }
  #set( $count = $count + 1 )##
#end##
#end
       ]

};