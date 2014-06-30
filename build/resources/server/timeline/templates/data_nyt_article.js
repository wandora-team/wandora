{     
            "dateTimeFormat": "Gregorian",
            "events":[
##
#set( $count = 0 )##
##
#set( $articleTypeSI = "http://api.nytimes.com/svc/search/v1/article" )##
#set( $articleType = $topicmap.getTopic( $articleTypeSI ) )##
##
#set( $articleDateTypeSI = "http://wandora.org/si/nytimes/date" )##
#set( $articleDateType = $topicmap.getTopic( $articleDateTypeSI ) )##
##
#set( $articleContentTypeSI = "http://wandora.org/si/nytimes/body" )##
#set( $articleContentType = $topicmap.getTopic( $articleContentTypeSI ) )##
##
#set( $articles = $topicmap.getTopicsOfType( $articleType ) )##
##
#foreach( $article in $articles )##
  #if($articleDateType && $article.getData( $articleDateType,"en" ) )##
    #set( $startDate = $article.getData( $articleDateType,"en" ) )##
  #end
  #set( $articleLink = $article.getOneSubjectIdentifier() )##
  #set( $articleTitle = $article.getDisplayName( $lang ) )##
  #if($article.getData( $articleContentType,"en"))##
    #set ($articleContent = $article.getData( $articleContentType,"en"))##
    #set( $articleContent = $articleContent.replaceAll('\"', '\\\"' ) )##
    #set( $articleContent = $articleContent.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #set ($articleContent = $articleContent.trim())##
  #end
  #set( $articleTitle = $articleTitle.replaceAll('\"', '\\\"' ) )##
  #set( $articleTitle = $articleTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
  #if( $count>0 ),
  #end##
                              {
                                      "start": "$startDate.substring(0,4)-$startDate.substring(4,6)-$startDate.substring(6,8)",
                                      "title": "$articleTitle",
                                      "textColor": "#000000",
                                      "classname": "nyt-article",
                                      "description": "$articleContent",
                                      "icon" : "${staticbase}api/images/nyt-icon.png",
                                      "link" : "$articleLink"
                              }
  #set( $count = $count + 1 )##
#end##
       ]
};
