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
 * LockLayers.java
 *
 * Created on 21. huhtikuuta 2006, 21:19
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
 * <p>
 * Locks and unlock topic map layers. If topic map layer is locked, the layer accepts
 * only read operations. Reader should note that the layer locking feature of Wandora 
 * application is not bullet proof.
 * </p>
 * <p>
 * Available modes are <code>LOCK_ALL</code>, <code>UNLOCK_ALL</code>,
 * <code>LOCK_ALL_BUT_CURRENT</code> and <code>REVERSE_LOCKS</code>.
 * </p>
 *
 * @author akivela
 */


public class LockLayers extends AbstractLayerTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static final int LOCK_ALL = 1000;
    public static final int UNLOCK_ALL = 1010;
    public static final int LOCK_ALL_BUT_CURRENT = 1020;
    public static final int REVERSE_LOCKS = 1030;

    
    private int option = UNLOCK_ALL;
    
    
    /** Creates a new instance of LockLayers */
    public LockLayers(int options) {
        this.option = options;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layers_merge.png");
    }

    
    @Override
    public String getName() {
        switch(option) {
            case LOCK_ALL: {
                return "Lock all layers";
            }
            case UNLOCK_ALL: {
                return "Unlock all layers";
            }
            case LOCK_ALL_BUT_CURRENT: {
                return "Lock all layers except current";
            }
            case REVERSE_LOCKS: {
                return "Reverse layer locks";
            }
        }
        return "Lock Layers";
    }


    @Override
    public String getDescription() {
        switch(option) {
            case LOCK_ALL: {
                return "Lock all layers.";
            }
            case UNLOCK_ALL: {
                return "Unlock all layers.";
            }
            case LOCK_ALL_BUT_CURRENT: {
                return "Lock all layers except current.";
            }
            case REVERSE_LOCKS: {
                return "Reverse layer locks.";
            }
        }
        return "Tool is used to change layer locks in layer stack.";
    }
    



    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Layer selected = solveContextLayer(wandora, context);
            
            if(selected == null) {
                WandoraOptionPane.showMessageDialog(wandora, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Collection<Layer> layers = wandora.layerTree.getAllLayers();
            Iterator<Layer> layerIterator = layers.iterator();
            Layer layer = null;

            while(layerIterator.hasNext()) {
                layer = (Layer) layerIterator.next();
                switch(option) {
                    case LOCK_ALL: {
                        layer.setReadOnly(true);
                        break;
                    }
                    case UNLOCK_ALL: {
                        layer.setReadOnly(false);
                        break;
                    }
                    case LOCK_ALL_BUT_CURRENT: {
                        if(selected.equals(layer)) layer.setReadOnly(false);
                        else layer.setReadOnly(true);
                        break;
                    }
                    case REVERSE_LOCKS: {
                        if(layer.isReadOnly()) layer.setReadOnly(false);
                        else layer.setReadOnly(true);
                        break;
                    }
                    default: {
                        log("Unknown option '"+option+"' used in LockLayers.");
                        break;
                    }
                }
            }
            wandora.layerTree.resetLayers();
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
    
}
