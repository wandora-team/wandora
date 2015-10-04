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
 * MakeConsistentTool.java
 *
 * Created on 6. maaliskuuta 2006, 11:50
 *
 */

package org.wandora.application.tools.layers;

import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;



/**
 * <p>
 * Wandora tries to merge all identical associations. However, there are
 * occasions when a topic map ends up to contain two or more identical
 * associations. This tool is used to merge all identical associations
 * in selected topic map. As a result, the number of associations may
 * decrease.
 * </p>
 *
 * @author olli
 */


public class MakeAssociationConsistentTool extends AbstractLayerTool implements WandoraTool {
    private boolean requiresRefresh = false;
    private static final String title = "Confirm";
    
    
    
    
    /** Creates a new instance of MakeConsistentTool */
    public MakeAssociationConsistentTool() {
    }

    @Override
    public String getName() {
        return "Make Association Consistent";
    }

    @Override
    public String getDescription() {
        return "Ensure the topic map does not contain identical associations.";
    }

    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    @Override
    public void execute(Wandora admin, Context context) {
        requiresRefresh = false;
        Layer contextLayer =  solveContextLayer(admin, context);
        
        if(contextLayer == null) {
            WandoraOptionPane.showMessageDialog(admin, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if(WandoraOptionPane.YES_OPTION != WandoraOptionPane.showConfirmDialog(admin,
                "You are about to start topic map consistency check for layer '"+contextLayer.getName()+"'. "+
                "Depending on your topic map size this operation may take a long time. "+
                "Are you sure you want to start consistency check?",
                title,
                WandoraOptionPane.YES_NO_OPTION)) {
            return;
        }
        
        setDefaultLogger();
        
        try {
            requiresRefresh = true;
            long st = System.currentTimeMillis();
            TopicMap tm = contextLayer.getTopicMap();
            int an = tm.getNumAssociations();
            log("Layer's topic map contains " + an + " associations.");
            log("Checking association consistency of layer '"+contextLayer.getName()+"'.");
            tm.checkAssociationConsistency(getCurrentLogger());
            int ana = tm.getNumAssociations();
            long et = System.currentTimeMillis();
            log("Consistency check took " + ((int)((et-st)/1000)) + " seconds.");
            log("After association consistency check the topic map contains " + ana + " associations.");
            log("Total " + (ana -an) + " associations removed.");
            log("Ready.");
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
}
