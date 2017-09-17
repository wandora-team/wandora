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
 * 
 * WandoraToolActionListener.java
 *
 * Created on 24. lokakuuta 2005, 14:33
 *
 */

package org.wandora.application;


import org.wandora.topicmap.TopicMapException;
import javax.swing.*;


/**
 * ActionListener that passes execution to a given WandoraTool object
 * whenever the action is performed. More specifically, action event invokes
 * tool's execute method. 
 * 
 * @author akivela
 */
public class WandoraToolActionListener implements java.awt.event.ActionListener {
    
    
    private final WandoraTool tool;
    private final Wandora wandora;
    
    
    
    public WandoraToolActionListener(Wandora wandora, WandoraTool tool) {
        this.wandora = wandora;
        this.tool = tool;
    }

    
    
    /**
     * Invokes tool's execute method by SwingUtilities.invokeLater.
     * 
     * @param event 
     */
    @Override
    public void actionPerformed(final java.awt.event.ActionEvent event) {
        if(wandora != null && tool != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try { 
                        wandora.applyChanges(); 
                    }
                    catch(CancelledException ce) { 
                        return; 
                    }

                    try {
                        tool.execute(wandora, event);
                    }
                    catch(TopicMapException tme) {
                        wandora.handleError(tme);
                    }
                }
            });
        }
        else {
            System.out.println("No Wandora or tool object specified in WandoraToolActionListener. Can't execute.");
        }
    }
    
}
