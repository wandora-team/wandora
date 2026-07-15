
{
			"dateTimeFormat": "Gregorian",
			"events":[

#set( $count = 0 )##
#set( $rssChannelTypeSI = "http://wandora.org/si/rss/2.0/channel" )##
#set( $rssChannelType = $topicmap.getTopic( $rssChannelTypeSI ) )##
#set( $rssChannelItemSI = "http://wandora.org/si/rss/2.0/channel/item" )##
#set( $rssChannelItem = $topicmap.getTopic( $rssChannelItemSI ) )##
#set( $rssChannelItemDateSI = "http://wandora.org/si/rss/2.0/channel/item/pubdate" )##
#set( $rssChannelItemDate = $topicmap.getTopic( $rssChannelItemDateSI ) )##
#set( $channels = $topicmap.getTopicsOfType( $rssChannelType ) )##
#set( $rssChannelItemLinkType = $topicmap.getTopic( "http://wandora.org/si/rss/2.0/channel/item/link" ) )##
#if( $channels.size()!=0 )##
 #foreach( $channel in $channels )##
  #set( $items = $helper.getPlayers( $channel, $rssChannelItem, $rssChannelItem ) )##
  #if( $items.size()!=0 )##
   #foreach( $item in $items )##
    #set( $itemDate = $helper.getFirstPlayer( $item, $rssChannelItemDateSI, "http://wandora.org/si/date" ) )##
    #set( $itemLink = $item.getData( $rssChannelItemLinkType, "en" ) )##
    #set( $itemTitle = $item.getDisplayName( $lang ) )##
    #set( $itemTitle = $itemTitle.replaceAll('\"', '\\\"' ) )##
    #set( $itemTitle = $itemTitle.replaceAll('[\p{Cntrl}\p{Space}]', ' ') )##
    #if( $count>0 ), 
    #end
				{
					"start": "$itemDate.getBaseName()",
					"title": "$itemTitle",
					"textColor": "#000000",
					"classname": "rss-event",
					"description": "$itemTitle",
					"link": "$itemLink",
                                        "icon" : "${staticbase}api/images/rss-icon.png"
				}
    #set( $count = $count + 1 )##
   #end##
  #end##
 #end##
#end##

			]
};
