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
 * MergeLayers.java
 *
 * Created on 17. helmikuuta 2006, 11:47
 *
 */

package org.wandora.application.tools.layers;



import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.linked.LinkedTopicMap;

import java.util.*;

import javax.swing.*;


/**
 * Merges two or more topic map
 * layers. Mode is set with the constructor attribute <code>options</code>.
 *
 * @author akivela
 */
public class MergeLayers extends AbstractLayerTool implements WandoraTool {
    private static final String message = "You are about to merge topic map layers. "+
            "Depending on topic map sizes this operation may take a long time. "+
            "Are you sure you want to start merge?";
    private static final String title = "Confirm";
    
    
    public static final int MERGE_UP = 100;
    public static final int MERGE_DOWN = 200;
    public static final int MERGE_ALL = 300;
    public static final int MERGE_ALL_UP = 310;
    public static final int MERGE_ALL_DOWN = 320;
    public static final int MERGE_VISIBLE = 500;
    
    
    private int options = 0;
    
    
    /** Creates a new instance of MergeLayers */
    public MergeLayers() {
        this.options = MERGE_UP;
    }
    
    
    public MergeLayers(int options) {
        this.options = options;
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layers_merge.png");
    }

    @Override
    public String getName() {
        return "Merge Topic Map Layers";
    }

    @Override
    public String getDescription() {
        return "Tool merges two or more topic map layers.";
    }
    
    public void setOptions(int opts) {
        this.options = opts;
    }
    
    
    
    public void execute(Wandora admin, Context context) {
        mergeLayers(admin, options, context);
    }
    
    
    public void mergeLayers(Wandora admin, int options, Context context) {
        Layer targetLayer = solveContextLayer(admin, context);
        
        if(targetLayer == null) {
            WandoraOptionPane.showMessageDialog(admin, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if(WandoraOptionPane.YES_OPTION != WandoraOptionPane.showConfirmDialog(admin, message, title, WandoraOptionPane.YES_NO_OPTION)) {
            return;
        }
        
        setDefaultLogger();
        
        ContainerTopicMap layerStack = targetLayer.getContainer();

        Vector<Layer> sourceLayers = getSourceLayers(targetLayer, layerStack, options);
        Layer sourceLayer = null;
        if(sourceLayers.size() < 1) {
            log("Merge couldn't finish as there exists no suitable layers to merge to...");
        }
        else {
            LayerTree layerTree=admin.layerTree;
            for(Layer l : layerTree.getRootStack().getTreeLayers()){
                TopicMap tm=l.getTopicMap();
                if(tm instanceof LinkedTopicMap){
                    for(Layer l2 : sourceLayers){
                        if(((LinkedTopicMap)tm).getLinkedTopicMap()==l2.getTopicMap()){
                            WandoraOptionPane.showMessageDialog(admin, "One of the merged layers is used in the linked topic layer \""+l.getName()+"\" and cannot be merged.","Merge layers",WandoraOptionPane.ERROR_MESSAGE);
                            return;
                        }                        
                    }
                }
            }
            
            
            long startTime = System.currentTimeMillis();
            try {
                admin.getTopicMap().clearTopicMapIndexes();
            }
            catch(Exception e) {
                log(e);
            }
            for(int i=sourceLayers.size()-1; i>=0 && !forceStop(); i--) {
                try {
                    sourceLayer = sourceLayers.elementAt(i);
                    if(!targetLayer.equals(sourceLayer)) {
                        log("Merging topic map layers '"+sourceLayer.getName()+"' and '"+targetLayer.getName() + "'!");
                        targetLayer.getTopicMap().mergeIn(sourceLayer.getTopicMap(),getCurrentLogger());
                        layerStack.removeLayer(sourceLayer);
                    }
                    sourceLayers.remove(sourceLayer);
                    admin.layerTree.resetLayers();
                }
                catch (Exception e) {
                    log("Merging topic map layers\n"+sourceLayer.getName()+" and "+targetLayer.getName()+" failed!", e);
                }
            }
            long endTime = System.currentTimeMillis();
            log("Merge took " + ((int)((endTime-startTime)/1000)) + " seconds.");
        }

        log("Ready.");
        admin.layerTree.resetLayers();
        setState(WAIT);
    }
    
    
    
    public Vector<Layer> getSourceLayers(Layer selected, ContainerTopicMap layers, int options) {
        Vector<Layer>sourceLayers = new Vector<Layer>();
        int selectedIndex = layers.getLayerZPos(selected);
        List<Layer> layerList = layers.getLayers();
        switch(options) {
            case MERGE_ALL: {
                for(int i=0; i<layerList.size(); i++) {
                    if(i != selectedIndex) sourceLayers.add(layerList.get(i));
                }
                break;
            }
            case MERGE_ALL_UP: {
                for(int i=selectedIndex+1; i<layerList.size(); i++) {
                    if(i != selectedIndex) sourceLayers.add(layerList.get(i));
                }
                break;
            }
            case MERGE_ALL_DOWN: {
                int j=0;
                for(int i=0; i<selectedIndex; i++) {
                    if(i != selectedIndex) sourceLayers.add(layerList.get(i));
                }
                break;
            }
            case MERGE_UP: {
                if(selectedIndex > 0) {
                    sourceLayers.add(layerList.get(selectedIndex-1));
                }
                break;
            }
            case MERGE_DOWN: {
                if(selectedIndex < layerList.size()-1) {
                    sourceLayers.add(layerList.get(selectedIndex+1));
                }
                break;
            }
            case MERGE_VISIBLE: {
                List<Layer> visibleLayerList = layers.getVisibleLayers();
                Layer visibleLayer = null;
                for(int i=0; i<visibleLayerList.size(); i++) {
                    visibleLayer = visibleLayerList.get(i);
                    if(!selected.equals(visibleLayer)) {
                        sourceLayers.add(visibleLayer);
                    }
                }
                break;
            }
        }
        return sourceLayers;
    }
    
}
