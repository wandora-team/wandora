{
  "dateTimeFormat": "gregorian",
  "events":[
##
#set( $count = 0 )##
#set( $lang = "en")##
##
#set( $linkTypeSI = "http://wandora.org/si/reddit/link" )##
#set( $linkType = $topicmap.getTopic( $linkTypeSI ) )##
##
#set( $linkCreatedTypeSI = "http://wandora.org/si/reddit/created" )##
#set( $linkCreatedType = $topicmap.getTopic( $linkCreatedTypeSI ) )##
##
#set( $destinationTypeSI = "http://wandora.org/si/reddit/destination")##
#set( $destinationType = $topicmap.getTopic( $destinationTypeSI ) )##
##
##
#set( $links = $topicmap.getTopicsOfType( $linkType ) )##
##
#foreach( $link in $links )##
  #if($linkCreatedType && $link.getData( $linkCreatedType, $lang ) )##
    #set( $linkDate = $link.getData( $linkCreatedType, $lang ) )##
  #end
  #if($destinationType)##
    #set( $destinationAssocs = $link.getAssociations($destinationType))##
    #if( $destinationAssocs )##
      #foreach( $destinationAssoc in $destinationAssocs)
        #set( $assoc = $destinationAssoc)##
        #break##
      #end
      #set( $destinationTopic = $assoc.getPlayer($destinationType))##
      #set( $destination = $destinationTopic.getSubjectLocator().toString())##
    #end##
  #end##
  #if($linkDate)##

    #set( $linkTitle = $link.getDisplayName( $lang ) )##
    #set( $linkTitle = $linkTitle.replaceAll('\"', '\\\"' ) )##
    #if( $count > 0 ),
    #end##
    {
      "start": "$linkDate",
      "title": "$linkTitle",
      #if($destination)
      "link": "$destination",
      #end
      "description": "",
      "textColor": "#000000",
      "classname": "reddit-link",
      "icon" : "${staticbase}api/images/reddit-icon.png"
    }
    #set( $count = $count + 1 )##

  #end##
#end##
       ]
};
