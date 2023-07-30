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
 *
 * LayeredTopicMapType.java
 *
 * Created on 17. maaliskuuta 2006, 14:50
 *
 */

package org.wandora.topicmap.layered;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapConfigurationPanel;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapLogger;
import org.wandora.topicmap.TopicMapType;
import org.wandora.topicmap.TopicMapTypeManager;
import org.wandora.topicmap.packageio.PackageInput;
import org.wandora.topicmap.packageio.PackageOutput;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
import org.wandora.utils.Options;


/**
 *
 * @author olli
 */
public class LayeredTopicMapType implements TopicMapType {
    
    public static boolean USE_UNDO_WRAPPED_TOPICMAPS = true;
    
    
    /** Creates a new instance of LayeredTopicMapType */
    public LayeredTopicMapType() {
    }

    
    @Override
    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) throws IOException,TopicMapException {
        LayerStack ls = (LayerStack) tm;
        Options options = new Options();
        int lcounter = 0;
        Layer selectedLayer = ls.getSelectedLayer();
        for(Layer l : ls.getLayers()) {
            options.put("layer"+lcounter+".name",l.getName());
            TopicMap ltm = getWrappedTopicMap(l.getTopicMap());
            
            options.put("layer"+lcounter+".type",ltm.getClass().getName());
            options.put("layer"+lcounter+".visiblity", l.isVisible() ? "true" : "false");
            options.put("layer"+lcounter+".readonly", l.isReadOnly() ? "true" : "false");

            if(selectedLayer == l) {
                options.put("layer"+lcounter+".selected", "true");
            }
            lcounter++;
        }
        out.nextEntry(path, "options.xml");
        // save options before everything else because we will need it before other files
        options.save(new OutputStreamWriter(out.getOutputStream()));
        
        lcounter = 0;
        for(Layer l : ls.getLayers()) {
            TopicMap ltm = getWrappedTopicMap(l.getTopicMap());
            TopicMapType tmtype = TopicMapTypeManager.getType(ltm);
            logger.log("Saving layer '" + l.getName() + "'.");
            tmtype.packageTopicMap(ltm,out, out.joinPath(path, "layer"+lcounter), logger);
            lcounter++;
        }
        for(int i=lcounter; i<lcounter+99; i++) {
            out.removeEntry(path, "layer"+lcounter);
        }
    }

    
    
    private TopicMap getWrappedTopicMap(TopicMap tm) {
        if(tm != null) {
            if(tm.getClass().equals(UndoTopicMap.class)) {
                tm = ((UndoTopicMap) tm).getWrappedTopicMap();
            }
        }
        return tm;
    }
    
    
    @Override
    public TopicMap createTopicMap(Object params) {
        return new LayerStack();
    }
    
    
    @Override
    public TopicMap modifyTopicMap(TopicMap tm,Object params) throws TopicMapException {
        return tm;
    }

    
    @Override
    public TopicMap unpackageTopicMap(TopicMap topicmap, PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws IOException,TopicMapException {
        if(topicmap != null && !(topicmap instanceof LayerStack)) {
            return topicmap;
        }
        if(topicmap == null) {
            topicmap = new LayerStack();
        }
        
        if(!in.gotoEntry(path, "options.xml")) {
            logger.log("Can't find options.xml in the package.");
            logger.log("Aborting.");
            return null;
        }
        Options options = new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        
        LayerStack ls = (LayerStack) topicmap;
        Layer selectedLayer = null;

        int counter = 0;
        while(true) {
            String layerName = options.get("layer"+counter+".name");
            if(layerName == null) break;
            
            String proposedLayerName = layerName;
            int layerCount = 1;
            while(ls.getLayer(proposedLayerName) != null) {
                proposedLayerName = layerName + " ("+layerCount+")";
                layerCount++;
            }
            layerName = proposedLayerName;

            String typeClass=options.get("layer"+counter+".type");
            
            try {
                Class c = Class.forName(typeClass);
                TopicMapType type = TopicMapTypeManager.getType((Class<? extends TopicMap>)c);
                logger.log("Preparing layer '" + layerName + "'.");
                TopicMap tm = type.unpackageTopicMap(in, in.joinPath(path, "layer"+counter), logger, wandora);
                Layer l = new Layer(tm,layerName,ls); 
                ls.addLayer(l);
                
                String layerVisibility = options.get("layer"+counter+".visiblity");
                String layerReadonly = options.get("layer"+counter+".readonly");
                String isSelected = options.get("layer"+counter+".selected");
                if("true".equalsIgnoreCase(layerVisibility)) l.setVisible(true);
                else l.setVisible(false);
                if("true".equalsIgnoreCase(layerReadonly)) l.setReadOnly(true);
                else l.setReadOnly(false);
                if("true".equalsIgnoreCase(isSelected)) selectedLayer = l;
            }
            catch(ClassNotFoundException cnfe) {
                logger.log("Can't find class '"+typeClass+"', skipping layer.");
            }
            
            counter++;
        }
        if(selectedLayer != null) {
            ls.selectLayer(selectedLayer);
        }
        return ls;
    }
    
    
    
    @Override
    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger, Wandora wandora) throws IOException,TopicMapException {
        if(!in.gotoEntry(path, "options.xml")) {
            if(logger != null) {
                logger.log("Can't find options.xml in the package.");
                logger.log("Aborting.");
            }
            else {
                System.out.println("Can't find options.xml in the package.");
                System.out.println("Aborting.");
            }
            return null;
        }
        Options options = new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        
        LayerStack ls = new LayerStack();
        Layer selectedLayer = null;
        if(logger == null) logger = ls;
        
        int counter = 0;
        while(true && counter < 9999) {
            String layerName = options.get("layer"+counter+".name");
            if(layerName == null) break;
            String typeClass = options.get("layer"+counter+".type");
            try {
                Class c = Class.forName(typeClass);
                TopicMapType type = TopicMapTypeManager.getType((Class<? extends TopicMap>)c);
                logger.log("Loading layer '" + layerName + "'.");
                TopicMap tm = type.unpackageTopicMap(in, in.joinPath(path, "layer"+counter), logger, wandora);
                
                Layer l = new Layer(tm,layerName,ls); 
                ls.addLayer(l);
                
                String layerVisibility = options.get("layer"+counter+".visiblity");
                String layerReadonly = options.get("layer"+counter+".readonly");
                String isSelected = options.get("layer"+counter+".selected");
                if("true".equalsIgnoreCase(layerVisibility)) l.setVisible(true);
                else l.setVisible(false);
                if("true".equalsIgnoreCase(layerReadonly)) l.setReadOnly(true);
                else l.setReadOnly(false);
                if("true".equalsIgnoreCase(isSelected)) selectedLayer = l;
            }
            catch(ClassNotFoundException cnfe){
                logger.log("Can't find topic map class '"+typeClass+"', skipping layer.");
            }
            counter++;
        }
        if(selectedLayer != null) {
            ls.selectLayer(selectedLayer);
        }
        return ls;
    }

    
    @Override
    public String getTypeName() {
        return "Layered";
    }
    
    
    @Override
    public String toString() {
        return getTypeName();
    }

    
    @Override
    public TopicMapConfigurationPanel getConfigurationPanel(Wandora wandora, Options options) {
        return new TopicMapConfigurationPanel() {
            @Override
            public Object getParameters() {
                return new Object();
            }
        };
    }
    
    
    @Override
    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora wandora, Options options, TopicMap tm) {
        return null;
    }
    
    
    @Override
    public javax.swing.JMenuItem[] getTopicMapMenu(TopicMap tm, Wandora wandora) {
        return null;
    }
    
    
    @Override
    public Icon getTypeIcon() {
        //return UIBox.getIcon("gui/icons/layerinfo/layer_type_layered.png");
        return UIBox.getIcon(0xf1b3);
    }
    
}
