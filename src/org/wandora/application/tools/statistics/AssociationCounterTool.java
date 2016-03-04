/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 * AssociationCounterTool.java
 *
 * Created on 29. toukokuuta 2007, 10:13
 *
 */

package org.wandora.application.tools.statistics;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;
import javax.swing.*;


import org.wandora.topicmap.layered.*;

/**
 *
 * @author olli, akivela
 */
public class AssociationCounterTool extends AbstractWandoraTool {
    

    public AssociationCounterTool() {
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_acount.png");
    }
    
    @Override
    public String getName() {
        return "Association statistics";
    }

    @Override
    public String getDescription() {
        return "Gather and show statistics about associations in the topic map.";
    }

    
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        TopicMap tm = solveContextTopicMap(admin, context);
        String tmTitle = solveNameForTopicMap(admin, tm);

        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Connection statistics options","Connection statistics options",true,new String[][]{
            new String[]{"Type topic","topic","","Which topics are examined, leave blank to include all topics."},
            new String[]{"Association type topic","topic","","Which associations are examined, leave blank to include all associations."},
            new String[]{"Include instances","boolean","true"},
            new String[]{"Include types","boolean","true"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;

        setDefaultLogger();
        
        Map<String,String> values=god.getValues();
        Topic typeTopic=null;
        if(values.get("Type topic")!=null && values.get("Type topic").length()>0) {
            LayeredTopic lt=(LayeredTopic)admin.getTopicMap().getTopic(values.get("Type topic"));
            if(tm!=admin.getTopicMap()){
                Layer l=admin.getTopicMap().getLayer(tm);
                typeTopic=lt.getTopicForLayer(l);
                if(typeTopic==null){
                    log("Selected type topic not found in layer");
                    setState(WAIT);
                    return;
                }
            }
            else typeTopic=lt;
        }
        Topic associationTypeTopic=null;
        if(values.get("Association type topic")!=null && values.get("Association type topic").length()>0) {
            LayeredTopic lt=(LayeredTopic)admin.getTopicMap().getTopic(values.get("Association type topic"));
            if(tm!=admin.getTopicMap()){
                Layer l=admin.getTopicMap().getLayer(tm);
                associationTypeTopic=lt.getTopicForLayer(l);
                if(associationTypeTopic==null){
                    log("Selected association type topic not found in layer");
                    setState(WAIT);
                    return;
                }
            }
            else associationTypeTopic=lt;
        }
        boolean includeInstances=Boolean.parseBoolean(values.get("Include instances"));
        boolean includeTypes=Boolean.parseBoolean(values.get("Include types"));
        
        int[] counters=new int[2000];        
        
        
        int numTopics=-1;
        Iterator<Topic> iter;
        if(typeTopic==null) {
            log("Getting all topics");
            if(!(tm instanceof LayerStack)) numTopics=tm.getNumTopics();
            iter = tm.getTopics();
        }
        else {
            log("Getting topics of selected type");
            Collection<Topic> topics=tm.getTopicsOfType(typeTopic);
            numTopics=topics.size();
            iter=topics.iterator();
        }
        log("Examining topics");
        if(numTopics!=-1) setProgressMax(numTopics);
        
        int topicCounter=0;
        Topic t;
        while( iter.hasNext() && !forceStop()) {
            topicCounter++;
            if(topicCounter>100) setProgress(topicCounter);
            t=iter.next();
            int count=0;
            if(associationTypeTopic != null) 
                count += t.getAssociations(associationTypeTopic).size();
            else 
                count += t.getAssociations().size();
            if(includeInstances) count+=tm.getTopicsOfType(t).size();
            if(includeTypes) count+=t.getTypes().size();
            
            if(count>=counters.length) count=counters.length-1;
            counters[count]++;
        }
        setState(INVISIBLE);
        if(!forceStop()) {
            String histogramTitle = "Association counts"+(tmTitle==null?"":(" for layer '"+tmTitle)+"'");           
            HistogramPanel.showHistogramPanel(counters,admin,histogramTitle,true,
                "topics: %1$d ; connections: %2$d ; average connections per topic: %3$.2f ; median connections per topic: %4$d","%2$d topics with %1$d connections");
        }
    }
}
