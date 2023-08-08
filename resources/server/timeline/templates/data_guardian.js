{     
            "dateTimeFormat": "Gregorian",
            "events":[
##
#set( $count = 0 )##
##
#set( $articleTypeSI = "http://wandora.org/si/theguardian/content" )##
#set( $articleType = $topicmap.getTopic( $articleTypeSI ) )##
##
#set( $articleDateTypeSI = "http://wandora.org/si/theguardian/pubtime" )##
#set( $articleDateType = $topicmap.getTopic( $articleDateTypeSI ) )##
##
#set( $articleContentTypeSI = "http://wandora.org/si/theguardian/field/body" )##
#set( $articleContentType = $topicmap.getTopic( $articleContentTypeSI ) )##
##
#set( $articleLinkTypeSI = "http://wandora.org/si/theguardian/field/shortUrl" )##
#set( $articleLinkType = $topicmap.getTopic( $articleLinkTypeSI ) )##
##

#set( $articles = $topicmap.getTopicsOfType( $articleType ) )##
##
#foreach( $article in $articles )##
  #if($articleDateType && $article.getData( $articleDateType,"en" ) )##
    #set( $startDate = $article.getData( $articleDateType,"en" ) )##
  #end
  #if($articleLinkType)##
    #set( $articleLink = $article.getData( $articleLinkType, "en" ) )##
  #end##
  #set( $articleTitle = $article.getDisplayName( $lang ) )##
  #if(! $articleTitle )
    #set ( $articleTitle = $article.getBaseName())##
  #end
  #if($articleContentType && $article.getData( $articleContentType,"en"))##
    #set ($articleContent = $article.getData( $articleContentType,"en"))##
    #set( $articleContent = $articleContent.replaceAll('\"', '\\\"' ) )##
    #set( $articleContent = $articleContent.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #set ($articleContent = $articleContent.trim())##
  #else
    #set ($articleContent = "")##
  #end
  #set( $articleTitle = $articleTitle.replaceAll('\"', '\\\"' ) )##
  #set( $articleTitle = $articleTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
  #if( $count>0 ),
  #end##
                              {
                                      "start": "$startDate",
                                      "title": "$esc.escapeJavaScript($articleTitle)",
                                      "textColor": "#000000",
                                      "classname": "nyt-article",
                                      "description": "$articleContent",
                                      "icon" : "${staticbase}api/images/guardian-icon.png",
                                      #if($articleLink)##
                                      "link" : "$articleLink"
                                      #end##
                              }
  #set( $count = $count + 1 )##
#end##
       ]
};

#set( $articleType = false )##
#set( $articles = false )##
