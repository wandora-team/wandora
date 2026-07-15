{     
      "dateTimeFormat": "Gregorian",
			"events":[
##
#set( $count = 0 )##
##
#set( $emailTypeSI = "http://wandora.org/si/email/email" )##
#set( $emailType = $topicmap.getTopic( $emailTypeSI ) )##
##
#set( $emailDateTypeSI = "http://wandora.org/si/email/sent-date" )##
#set( $emailDateType = $topicmap.getTopic( $emailDateTypeSI ) )##
##
#set( $emailTextContentTypeSI = "http://wandora.org/si/email/text-content" )##
#set( $emailHTMLContentTypeSI = "http://wandora.org/si/email/html-text-content" )##
#set( $emailXMLContentTypeSI = "http://wandora.org/si/email/xml-content" )##
#set( $emailWordContentTypeSI = "http://wandora.org/si/email/ms-word-text-content" )##
#set( $emailExcelContentTypeSI = "http://wandora.org/si/email/ms-excel-text-content" )##
#set( $emailPPContentTypeSI = "http://wandora.org/si/email/ms-powerpoint-text-content" )##
#set( $emailPDFContentTypeSI = "http://wandora.org/si/email/pdf-text-content" )##
##
#if($topicmap.getTopic( $emailTextContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailTextContentTypeSI ) )##
#elseif($topicmap.getTopic( $emailHTMLContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailHTMLContentTypeSI ) )##
#elseif($topicmap.getTopic( $emailXMLContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailXMLContentTypeSI ) )##
#elseif($topicmap.getTopic( $emailWordContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailWordContentTypeSI ) )##
#elseif($topicmap.getTopic( $emailExcelContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailExcelContentTypeSI ) )##
#elseif($topicmap.getTopic( $emailPPContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailPPContentTypeSI ) )##
#elseif($topicmap.getTopic( $emailPDFContentTypeSI ))
#set( $emailContentType = $topicmap.getTopic( $emailPDFContentTypeSI ) )##
#end
##
#set( $emails = $topicmap.getTopicsOfType( $emailType ) )##
##
#foreach( $email in $emails )##
  #if($emailDateType && $email.getData( $emailDateType,"en" ) )##
    #set( $startDate = $email.getData( $emailDateType,"en" ) )##
  #end
  #set( $emailTitle = $email.getDisplayName( $lang ) )##
  #if($email.getData( $emailContentType,"en"))##
    #set ($emailContent = $email.getData( $emailContentType,"en"))##
    #set( $emailContent = $emailContent.replaceAll('\"', '\\\"' ) )##
    #set( $emailContent = $emailContent.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #set ($emailContent = $emailContent.trim())##
    #set ($emailContent = $emailContent.replaceAll("<[^>]*?>",""))##
  #end
  #set( $emailTitle = $emailTitle.replaceAll('\"', '\\\"' ) )##
  #set( $emailTitle = $emailTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
  #if( $count>0 ),
  #end##
                              {
                                      "start": "$startDate",
                                      "title": "$emailTitle",
                                      "textColor": "#000000",
                                      "classname": "email",
                                      "description": "$emailContent",
                                      "icon" : "${staticbase}api/images/email-icon.png"
                              }
  #set( $count = $count + 1 )##
#end##
       ]
};
