/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 *
 * Layer.java
 *
 * Created on 8. syyskuuta 2005, 11:38
 */

package org.wandora.topicmap.layered;
import org.wandora.topicmap.*;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
/**
 * The Layer class represents one layer in a LayerStack. Each Layer contains
 * the topic map of that layer and in addition to that, the name of the layer,
 * boolean flags to indicate visibility and read only states and a colour for
 * the layer.
 *
 * @author olli
 */
public class Layer {
    
    protected ContainerTopicMap container;
    
    protected TopicMap topicMap;
    protected boolean visible;
    protected int color;
    protected String name;
    protected boolean broken=false;
    
    /** Creates a new instance of Layer */
    public Layer(TopicMap topicMap,String name,ContainerTopicMap container) throws TopicMapException {
        this.topicMap=topicMap;
        this.container=container;
        setName(name);
        setVisible(true);
        setColor(0x000000);
        
        if(!topicMap.isConnected()){
            this.broken=true;
        }
    }
    
    public void wrapInUndo(){
        if(!(topicMap instanceof LayerStack) && !(topicMap instanceof UndoTopicMap)){
            topicMap=new UndoTopicMap(topicMap);
        }
    }
    
    public ContainerTopicMap getContainer(){return container;}
    public TopicMap getTopicMap(){return topicMap;}
    
    public boolean isVisible(){return visible;}
    public void setVisible(boolean visible){
        this.visible=visible;
        if(container!=null) container.notifyLayersChanged();
        if(container!=null) container.fireLayerVisibilityChanged(this);
//        layerStack.visibilityChanged(this);
    }
    
    public boolean isReadOnly(){
        if(topicMap != null) {
            return topicMap.isReadOnly();
        }
        return true;
    }
    
    public void setReadOnly(boolean readOnly) {
        if(topicMap != null) {
            topicMap.setReadOnly(readOnly);
        }
    }
    
    public int getColor(){return color;}
    public void setColor(int color){this.color=color;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public int getZPos(){
        return container.getLayerZPos(this);
    }
    public void setBroken(boolean broken){this.broken=broken;}
    public boolean getBroken(){return broken;}
    
}
