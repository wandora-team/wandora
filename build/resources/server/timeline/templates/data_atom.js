{
			"events":[

#set( $count = 0 )##
#set( $atomFeedTypeSI = "http://www.w3.org/2005/Atom/channel" )##
#set( $atomFeedType = $topicmap.getTopic( $atomFeedTypeSI ) )##
#set( $atomEntryTypeSI = "http://www.w3.org/2005/Atom/channel/entry" )##
#set( $atomEntryType = $topicmap.getTopic( $atomEntryTypeSI ) )##
#set( $atomEntryDateTypeSI = "http://www.w3.org/2005/Atom/channel/entry/published" )##
#set( $atomEntryDateType = $topicmap.getTopic( $atomEntryDateTypeSI ) )##
#set( $atomEntryUpdatedTypeSI = "http://www.w3.org/2005/Atom/channel/entry/updated" )##
#set( $atomEntryUpdatedType = $topicmap.getTopic( $atomEntryUpdatedTypeSI ) )##
#set( $atomEntryLinkTypeSI = "http://www.w3.org/2005/Atom/link/href" )##
#set( $atomEntrySummaryTypeSI = "http://www.w3.org/2005/Atom/channel/entry/summary" )##
#set( $atomEntrySummaryType = $topicmap.getTopic( $atomEntrySummaryTypeSI ) )##
#set( $feeds = $topicmap.getTopicsOfType( $atomFeedType ) )##
#if( $feeds.size()!=0 )##
 #foreach( $feed in $feeds )##
  #set( $entrys = $helper.getPlayers( $feed, $atomEntryType, $atomEntryType ) )##
  #if( $entrys.size()!=0 )##
   #foreach( $entry in $entrys )##
    #if($helper.getFirstPlayer( $entry, $atomEntryDateTypeSI, $atomEntryDateTypeSI ) )
      #set( $entryDate = $helper.getFirstPlayer( $entry, $atomEntryDateTypeSI, $atomEntryDateTypeSI ) )##
    #elseif($helper.getFirstPlayer( $entry, $atomEntryUpdatedTypeSI, $atomEntryUpdatedTypeSI ) )
      #set( $entryDate = $helper.getFirstPlayer( $entry, $atomEntryUpdatedTypeSI, $atomEntryUpdatedTypeSI ))##
    #end
    #set( $entryLink = $helper.getFirstPlayer( $entry, "http://wandora.org/si/link", $atomEntryLinkTypeSI ) )##
    #set( $entryTitle = $entry.getDisplayName( $lang ) )##
    #if($entry.getData( $atomEntrySummaryType,"en"))
      #set ($entryDescription = $entry.getData( $atomEntrySummaryType,"en"))##
      #set( $entryDescription = $entryDescription.replaceAll('\"', '\\\"' ) )##
      #set( $entryDescription = $entryDescription.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #end
    #set( $entryTitle = $entryTitle.replaceAll('\"', '\\\"' ) )##
    #set( $entryTitle = $entryTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #if( $count>0 ), 
    #end
				{
					"start": "$entryDate.getBaseName()",
					"title": "$entryTitle",
					"textColor": "#000000",
					"classname": "atom-event",
					"description": "$entryDescription",
          "icon" : "${staticbase}api/images/rss-icon.png",
          "link" : "$entryLink"
                                }
    #set( $count = $count + 1 )##
   #end##
  #end##
 #end##
#end##

			]
};
