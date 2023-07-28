{     
      "dateTimeFormat": "Gregorian",
			"events":[
##
#set( $count = 0 )##
##
#set( $videoTypeSI = "http://wandora.org/si/youtube/schema/YouTube+video" )##
#set( $videoType = $topicmap.getTopic( $videoTypeSI ) )##
##
#set( $videoDateTypeSI = "http://wandora.org/si/youtube/schema/video-published" )##
#set( $videoDateType = $topicmap.getTopic( $videoDateTypeSI ) )##
##
#set( $videoDescriptionTypeSI = "http://wandora.org/si/youtube/schema/video-description" )##
#set( $videoDescriptionType = $topicmap.getTopic( $videoDescriptionTypeSI ) )##
##
#set( $videos = $topicmap.getTopicsOfType( $videoType ) )##
##
#foreach( $video in $videos )##
  #if($videoDateType && $video.getData( $videoDateType,"en" ) )##
    #set( $startDate = $video.getData( $videoDateType,"en" ) )##
  #end
  #set( $videoLink = $video.getSubjectLocator().toString() )##
  #set( $videoTitle = $video.getDisplayName( $lang ) )##
  #if($video.getData( $videoDescriptionType,"en"))##
    #set ($videoDescription = $video.getData( $videoDescriptionType,"en"))##
    #set( $videoDescription = $videoDescription.replaceAll('\"', '\\\"' ) )##
    #set( $videoDescription = $videoDescription.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #set ($videoDescription = $videoDescription.trim())##
  #end
  #set( $videoTitle = $videoTitle.replaceAll('\"', '\\\"' ) )##
  #set( $videoTitle = $videoTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
  #if( $count>0 ),
  #end##
                              {
                                      "start": "$startDate",
                                      "title": "$videoTitle",
                                      "textColor": "#000000",
                                      "classname": "youtube-video",
                                      "description": "$videoDescription",
                                      "icon" : "${staticbase}api/images/youtube-icon.png",
                                      "link" : "$videoLink"
                              }
  #set( $count = $count + 1 )##
#end##
       ]
};
