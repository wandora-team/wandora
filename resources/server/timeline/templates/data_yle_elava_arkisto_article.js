{
			"dateTimeFormat": "ISO8601",
			"events": [

#set( $articleTimeType = $topicmap.getTopic( "http://wandora.org/si/yle/elava-arkisto/article-published" ) )##
#set( $articleTopic = $topicmap.getTopic( "http://wandora.org/si/yle/elava-arkisto/article" ) )##
#set( $articles = $topicmap.getTopicsOfType( $articleTopic ) )##
#set( $count = 0 )##
#if( $articles.size()!=0 )##

#foreach( $article in $articles )##
 #set( $articleTime = $article.getData($articleTimeType,"en") )##
 #set( $articleText = $article.getDisplayName($lang) )##
 #set( $articleText = $articleText.replaceAll('\"', '\\\"') )##
 #set( $articleText = $articleText.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
 #set( $articleLink = $article.getSubjectLocator().toExternalForm() )##
 ##
 #set( $articleTitle = $articleText )##
 #if( $count<2000 )##
  #if( $count>0 ), 
  #end
				{
					"start": "$articleTime",
					"title": "$articleTitle",
					"textColor": "#000000",
					"classname": "yle-event",
					"description": "$articleText",
					"link": "$articleLink",
                                        "icon" : "${staticbase}api/images/yle-icon.png"
				}
  #set( $count = $count + 1 )##
 #end##
#end##

#end##

			]
};
