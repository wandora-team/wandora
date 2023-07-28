{
        "dateTimeFormat": "Gregorian",
	"events":[
#set( $count = 0 )##
#set( $fileTypeSI = "http://wandora.org/si/directory-structure-extractor/file" )##
#set( $fileType = $topicmap.getTopic( $fileTypeSI ) )##
##
#set( $fileModifiedSI = "http://wandora.org/si/directory-structure-extractor/file-modified" )##
#set( $fileModifiedType = $topicmap.getTopic( $fileModifiedSI ) )##
##
#set( $files = $topicmap.getTopicsOfType( $fileType ) )##
##
#if( $files.size() != 0 )##
 #foreach( $file in $files )##
  #set( $fileTitle = $file.getBaseName() )##
  #set( $fileModified = $file.getData($fileModifiedType, "en") )##
  #set( $fileLocation = $file.getSubjectLocator() )##
  #if( $count>0 ), 
  #end
	{
	  "start": "$fileModified",
	  "title": "$fileTitle",
	  "textColor": "#000000",
	  "classname": "file-modified",
          "icon" : "${staticbase}api/images/file-icon.png",
          "description" : "",
          "link" : ""
        }
  #set( $count = $count + 1 )##
 #end##
#end##
]
};
