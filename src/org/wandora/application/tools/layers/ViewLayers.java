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
 * ViewLayers.java
 *
 * Created on 21. huhtikuuta 2006, 20:38
 *
 */

package org.wandora.application.tools.layers;


import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;
import javax.swing.*;



/**
 * Controls topic map layer visibility. Visibility
 * is used to view or hide topics and associations in layer. Tools specific
 * function is defined in constructor with <code>options</code> parameter.
 *
 * @author akivela
 */

public class ViewLayers extends AbstractLayerTool implements WandoraTool {
    public static final int VIEW_ALL = 1000;
    public static final int HIDE_ALL = 1010;
    public static final int HIDE_ALL_BUT_CURRENT = 1020;
    public static final int REVERSE_VISIBILITY = 1030;

    
    private int option = VIEW_ALL;
    
    
    /** Creates a new instance of ViewLayers */
    public ViewLayers(int options) {
        this.option = options;
    }
    
    @Override
    public Icon getIcon() {
        if(option == VIEW_ALL) {
            return UIBox.getIcon("gui/icons/view.png");
        }
        if(option == HIDE_ALL || option == HIDE_ALL_BUT_CURRENT) {
            return UIBox.getIcon("gui/icons/view_no.png");
        }
        if(option == REVERSE_VISIBILITY) {
            return UIBox.getIcon("gui/icons/view_reverse.png");
        }
        return UIBox.getIcon("gui/icons/view.png");
    }



    @Override
    public String getName() {
        if(option == VIEW_ALL) {
            return "View all layers";
        }
        if(option == HIDE_ALL) {
            return "Hide all layers";
        }
        if(option ==  HIDE_ALL_BUT_CURRENT) {
            return "Hide all but current layer";
        }
        if(option == REVERSE_VISIBILITY) {
            return "Reverse layer order";
        }
        return "View layer";
    }

    @Override
    public String getDescription() {
        if(option == VIEW_ALL) {
            return "Change all layers visible.";
        }
        if(option == HIDE_ALL) {
            return "Change all layers invisible.";
        }
        if(option ==  HIDE_ALL_BUT_CURRENT) {
            return "Change current layer visible and all other layers invisible.";
        }
        if(option == REVERSE_VISIBILITY) {
            return "Change visible layers invisible and invisible layers visible.";
        }
        return "Tool is used to change layer visibility in layer stack.";
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        try {
            Layer selected = solveContextLayer(admin, context);
            if(selected == null) {
                WandoraOptionPane.showMessageDialog(admin, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
                return;
            }
            
            ArrayList<Layer> layers = admin.layerTree.getAllLayers();
            try {
                admin.getTopicMap().clearTopicMapIndexes();
            }
            catch(Exception e) {
                log(e);
            }
            for(Layer layer : layers){
                switch(option) {
                    case VIEW_ALL: {
                        layer.setVisible(true);
                        break;
                    }
                    case HIDE_ALL: {
                        layer.setVisible(false);
                        break;
                    }
                    case HIDE_ALL_BUT_CURRENT: {
                        if(selected.equals(layer)) layer.setVisible(true);
                        else layer.setVisible(false);
                        break;
                    }
                    case REVERSE_VISIBILITY: {
                        if(layer.isVisible()) layer.setVisible(false);
                        else layer.setVisible(true);
                        break;
                    }
                    default: {
                        log("Unknown option '"+option+"' used in ViewLayers.");
                        break;
                    }
                }
            }
            admin.layerTree.resetLayers();
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
}
