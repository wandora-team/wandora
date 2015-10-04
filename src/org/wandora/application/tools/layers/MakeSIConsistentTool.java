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
 * MakeSIConsistentTool.java
 *
 * Created on 6. maaliskuuta 2006, 11:50
 *
 */

package org.wandora.application.tools.layers;

import org.wandora.topicmap.database.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;



/**
 * @author akivela
 */
public class MakeSIConsistentTool extends AbstractLayerTool implements WandoraTool {
    private boolean requiresRefresh = false;
    private static final String title = "Confirm";
    
    
    
    
    /** Creates a new instance of MakeConsistentTool */
    public MakeSIConsistentTool() {
    }

    @Override
    public String getName() {
        return "Make SI Consistent";
    }

    @Override
    public String getDescription() {
        return "Ensure that all topic's in topic map have at least one subject identifier.";
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
        
        TopicMap topicMap = contextLayer.getTopicMap();
        
        if(!(topicMap instanceof DatabaseTopicMap)) {
            WandoraOptionPane.showMessageDialog(admin, "Topic map in layer '"+contextLayer.getName()+"' "+
                    "is not a database topic map. SI consistency check is currently supported only "+
                    "by database topic map.");
            return;
        }
        
        if(WandoraOptionPane.YES_OPTION != WandoraOptionPane.showConfirmDialog(admin,
                "You are about to start topic map SI consistency check for layer '"+contextLayer.getName()+"'. "+
                "Are you sure you want to start SI consistency check?",
                title,
                WandoraOptionPane.YES_NO_OPTION)) {
            return;
        }
        
        setDefaultLogger();
        
        try {
            log("Checking SI consistency of layer '"+contextLayer.getName()+"'.");
            long st = System.currentTimeMillis();
            requiresRefresh = true;
            ((DatabaseTopicMap) topicMap).checkSIConsistency(getCurrentLogger());
            long et = System.currentTimeMillis();
            log("SI Consistency check took " + ((int)((et-st)/1000)) + " seconds.");
            log("Ready.");
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
}
