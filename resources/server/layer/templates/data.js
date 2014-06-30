#set( $wandoraClass = $topic.getTopicMap().getTopic("http://wandora.org/si/core/wandora-class") )##
#set( $ctopic = $topic )##
#if($request.getParameter("nl"))##
#**##set($nl =  $request.getParameter("nl"))##
#else##
#**##set($nl = 5)##
#end##
#if($request.getParameter("nt"))##
#**##set($nt =  $request.getParameter("nt"))##
#else##
#**##set($nt = 500)##
#end##

#set($tm = $topic.getTopicMap())##
#set($topicList = $listmaker.make())##
#set($mergedTopicList = $listmaker.make())##
#set($layerList = $listmaker.make())##
{
  "nodes" : {
    "children" : [
#*     *##recurseTm($tm)##
    ]
  },
#**##set($k = 0)##
  "links" : [
#**##foreach($t in $topicList)##
#*  *##foreach($tt in $topicList)##
#*    *##if($t.mergesWithTopic($tt) && !$t.getID().equals($tt.getID()))##
#*      *##if(!$mergedTopicList.contains($t.getID()))##
#*        *##set($temp = $mergedTopicList.add($t.getID()))##
#*      *##end##
#*      *##if(!$mergedTopicList.contains($tt.getID()))##
#*        *##set($temp = $mergedTopicList.add($tt.getID()))##
#*      *##end##
#*      *##if($k != 0)##
        ,
#*      *##end##
        {
          "source" : "$t.getID()",
          "target" : "$tt.getID()"
        }
#*      *##set($k = $k + 1 )##
#*  *##end##
#**##end##
#end##
  ]
},$topicList.size(),$layerList.size(),$mergedTopicList.size()

#macro(recurseTm $tm)##
#**##set( $layers = $tm.getLayers() )##
#**##set ($temp = $layerList.addAll($layers))##
#**##set( $i = 0 )##
#**##foreach( $layer in $layers )##
#*  *##set( $layertopicmap = $layer.getTopicMap() )##
#*  *##if($i != 0)
      ,
#*  *##end
    { 
      "name" : "$layer.getName()",
      "type" : "layer",
      "id" : "$layer.getName()",
#*    *##if( $helper.isTopicMapContainer($layertopicmap) )##
      "children" : [
#*    *##recurseTm($layertopicmap)##
      ]
#*    *##else##
      "size" : "$layer.getTopicMap().getNumTopics()",
#*      *##set($curDepth = $curDepth + 1)##
      "children" : [
#*        *##set($topics = $layer.getTopicMap().getTopics())##
#*        *##set($j = 0)##
#*        *##foreach($topic in $topics)##
#*          *##set($temp = $topicList.add($topic))##
#*          *##if($j != 0)##
              ,
#*          *##end##
            {
              "type" : "topic",
              "inLayer" : "$layer.getName()",
              "name" : "$urlencoder.encode( $topic.getDisplayName() )",
#*            *##if($topic.getSubjectLocator())##
              "sl" : "$topic.getSubjectLocator().toString()",
#*            *##else##
              "sl" : "",
#*            *##end##
              "bn" : "$urlencoder.encode( $topic.getBaseName() )",
              "id" : "$topic.getID()",
              "size" : "$topic.getAssociations().size()"
            }
#*        *##set( $j = $j + 1 )##
#*        *##if($j == $nt)##
#*          *##break##
#*        *##end##
#*    *##end##
      ]
#*    *##set($curDepth = $curDepth - 1)##
#*  *##end##
    }
#*  *##set($i = $i + 1)##
#*  *##if($i == $nl)##
#*    *##break##
#*  *##end##
#**##end##
#end##