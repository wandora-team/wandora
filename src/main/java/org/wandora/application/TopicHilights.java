/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * 
 * TopicHilights.java
 *
 * Created on 17. lokakuuta 2005, 16:21
 *
 */

package org.wandora.application;


import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import java.awt.Color;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicMapException;

import org.wandora.topicmap.layered.LayeredTopic;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.ContainerTopicMap;

import static org.wandora.utils.Tuples.T2;
import static org.wandora.utils.Tuples.T3;


/**
  * <p>
  * TopicHilights is used to color topics in Wandora's user interface, typically
  * in topic tables and trees. Generally a topic is colored black. If the topic 
  * doesn't locate in current layer, the topic is colored dark red.
  * </p>
  * <p>
  * This class doesn't set colors in GraphTopicPanel nor TopicTreePanel.
  * </p>
  * 
  * @author akivela
 **/

public class TopicHilights {
    
    private Map<String, Color> hilighted = new LinkedHashMap<>();
    private Wandora wandora = null;
    private Map<Topic, Color> hilightedTopics = new LinkedHashMap<>();
    
    public static Color removedTopicColor = new Color(0xa00000);
    
    
    
    
    /** Creates a new instance of TopicHilights */
    public TopicHilights(Wandora w) {
        this.wandora = w;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public void add(String si, Color color) throws TopicMapException {
        if(si == null || color == null) return;
        hilighted.put(si, color);
        Topic t = wandora.getTopicMap().getTopic(si);
        if(t != null) hilightedTopics.put(t, color);
    }
    public void add(Topic topic, Color color) throws TopicMapException {
        if(topic != null) {
            remove(topic);
            if(color != null) {
                hilighted.put(topic.getOneSubjectIdentifier().toExternalForm(), color);
                hilightedTopics.put(topic, color);
            }
        }
    }
    public void add(Topic[] topics, Color color) throws TopicMapException  {
        if(topics != null && topics.length > 0) {
            for(int i=0; i<topics.length; i++) {
                add(topics[i], color);
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public Color get(String si) {
        try {
            if(si == null) return null;
            else return (Color) hilighted.get(si);
        }
        catch (Exception e) {
            System.out.println("Exception occurred while getting topic hilight color!");
            e.printStackTrace();
        }
        return null;
    }
    
    public Color getBaseNameColor(Topic topic) throws TopicMapException {
        if(topic instanceof LayeredTopic){
            LayeredTopic lt=(LayeredTopic)topic;
            
            Topic t=lt.getBaseNameSource();
            Layer l=lt.getLayerStack().getLayer(t);
            if(t==null) return null;
            while(t instanceof LayeredTopic){
                Topic t2=((LayeredTopic)t).getBaseNameSource();
                if(t2==null) return null;
                l=((LayeredTopic)t).getLayerStack().getLayer(t2);
                t=t2;
            }
            
            if(l==null) return null;
            if(lt.getLayerStack().getSelectedTreeLayer()!=l) return notActiveLayerColor;
            return null;
        }
        else return null;
    }
    
    
    
    public Color getSubjectLocatorColor(Topic topic) throws TopicMapException {
        if(topic instanceof LayeredTopic){
            LayeredTopic lt=(LayeredTopic)topic;
            
            Topic t=lt.getSubjectLocatorSource();
            Layer l=lt.getLayerStack().getLayer(t);
            if(t==null) return null;
            while(t instanceof LayeredTopic){
                Topic t2=((LayeredTopic)t).getSubjectLocatorSource();
                if(t2==null) return null;
                l=((LayeredTopic)t).getLayerStack().getLayer(t2);
                t=t2;
            }
            
            if(l==null) return null;
            if(lt.getLayerStack().getSelectedTreeLayer()!=l) return notActiveLayerColor;
            return null;
        }
        else return null;        
    }
    
    
    
    public Color getVariantColor(Topic topic,Set<Topic> scope) throws TopicMapException {
        if(topic instanceof LayeredTopic){
            LayeredTopic lt=(LayeredTopic)topic;
            
            T2<Topic,Set<Topic>> source=lt.getVariantSource(scope);
            if(source==null) return null;
            Layer l=lt.getLayerStack().getLayer(source.e1);
            while(source.e1 instanceof LayeredTopic){
                T2<Topic,Set<Topic>> source2=((LayeredTopic)source.e1).getVariantSource(source.e2);
                if(source2==null) return null;
                l=((LayeredTopic)source.e1).getLayerStack().getLayer(source2.e1);
                source=source2;
            }
            if(l==null) return null;
            if(lt.getLayerStack().getSelectedTreeLayer()!=l) return notActiveLayerColor;
            return null;
        }
        else return null;
    }
    

    public Color getSIColor(Topic topic,Locator l) throws TopicMapException {
        if(l==null) return null;
        if(topic instanceof LayeredTopic){
            LayeredTopic lt=(LayeredTopic)topic;
            Collection<Topic> c=lt.getLayerStack().getTopicsForSelectedTreeLayer(lt);
            for(Topic t : c){
                if(t.getSubjectIdentifiers().contains(l)) return null;
            }
            return notActiveLayerColor;
        }
        else return null;
    }
    
    
    
    public Color getOccurrenceColor(Topic topic,Topic type,Topic lang) throws TopicMapException {

        if(topic instanceof LayeredTopic){
            LayeredTopic lt=(LayeredTopic)topic;
            
            T3<Topic,Topic,Topic> source=lt.getDataSource(type,lang);
            if(source==null) return null;
            Layer l=lt.getLayerStack().getLayer(source.e1);
            while(source.e1 instanceof LayeredTopic){
                T3<Topic,Topic,Topic> source2=((LayeredTopic)source.e1).getDataSource(source.e2,source.e3);
                if(source2==null) return null;
                l=((LayeredTopic)source.e1).getLayerStack().getLayer(source2.e1);
                source=source2;
            }
            if(l==null) return null;
            if(lt.getLayerStack().getSelectedTreeLayer()!=l) return notActiveLayerColor;
            return null;
        }
        else return null;
    }
    

    public Color get(Topic topic) {
        try {
            if(topic == null) return null;
            else if(topic.isRemoved()) return removedTopicColor;
            else return (Color) hilightedTopics.get(topic);
        }
        catch(Exception e) {
            System.out.println("Exception occurred while getting topic hilight color!");
            e.printStackTrace();
        }
        return null;
    }
    
    
    
//    public Color notActiveLayerColor = new Color(0xfff6f6);
    public static Color notActiveLayerColor = new Color(0x800000);
    public Color getLayerColor(Topic topic) {
        try {
            if(topic == null || !(topic.getTopicMap() instanceof ContainerTopicMap)) return null;
            else {
                if(((ContainerTopicMap) topic.getTopicMap()).getTopicForSelectedTreeLayer(topic) == null) {
                    return notActiveLayerColor;
                }
            }
        }
        catch(Exception e) {
            System.out.println("Exception occurred while getting topic hilight color!");
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void remove(Topic topic)  throws TopicMapException {
        if(topic != null) {
            for(Iterator<Locator> i = topic.getSubjectIdentifiers().iterator(); i.hasNext();) {
                try {
                    String si = ((Locator) i.next()).toExternalForm();
                    hilighted.remove(si);
                    hilightedTopics.remove(topic);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void remove(Topic[] topics)  throws TopicMapException {
        if(topics != null && topics.length > 0) {
            for(int i=0; i<topics.length; i++) {
                remove(topics[i]);
            }
        }
    }
    public void removeAll() {
        hilighted.clear();
        hilightedTopics.clear();
    }
    
    
}
