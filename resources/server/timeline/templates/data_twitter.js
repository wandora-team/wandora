{
			"dateTimeFormat": "ISO8601",
			"events": [

#set( $tweetTimeType = $topicmap.getTopic( "http://wandora.org/si/twitter/date" ) )##
#set( $tweetTopic = $topicmap.getTopic( "http://wandora.org/si/twitter/tweet" ) )##
#set( $tweets = $topicmap.getTopicsOfType( $tweetTopic ) )##
#set( $count = 0 )##
#if( $tweets.size()!=0 )##

#foreach( $tweet in $tweets )##
 #set( $tweetTime = $tweet.getData($tweetTimeType,"en") )##
 #set( $tweetText = $tweet.getDisplayName($lang) )##
 #set( $tweetText = $tweetText.replaceAll('\"', '\\\"') )##
 #set( $tweetText = $tweetText.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
 #set( $tweetLink = $tweet.getOneSubjectIdentifier() )##
 ##
 #set( $tweetTitle = $tweetText )##
 #if( $count<2000 )##
  #if( $count>0 ), 
  #end
				{
					"start": "$tweetTime",
					"title": "$tweetTitle",
					"textColor": "#000000",
					"classname": "twitter-event",
					"description": "$tweetText",
					"link": "$tweetLink",
                                        "icon" : "${staticbase}api/images/twitter-icon.png"
				}
  #set( $count = $count + 1 )##
 #end##
#end##

#end##

			]
};
