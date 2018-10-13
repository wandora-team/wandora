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
 * TopicHilighter.java
 *
 * Created on 21. marraskuuta 2005, 21:13
 *
 */

package org.wandora.application.tools;


import org.wandora.application.gui.table.TopicTable;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import java.awt.*;
import javax.swing.*;


/**
 *
 * @author akivela
 */
public class TopicHilighter extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public static final int ADD_HILIGHT = 1000;
    public static final int REMOVE_HILIGHT = 1010;
    public static final int REMOVE_ALL_HILIGHTS = 1020;
    
    public int hilightOrders = ADD_HILIGHT;
    
    

    public TopicHilighter(int hilightOrders) {
        this.hilightOrders = hilightOrders;
    }
    
    
    public void execute(Wandora wandora, Context context) {
        try {
            TopicTable table = (TopicTable) getContext().getContextSource();
            switch(hilightOrders) {
                case ADD_HILIGHT: {
                    Topic[] topics = table.getSelectedTopics();
                    Color newColor = JColorChooser.showDialog(
                    		wandora,
                             "Choose topic hilight color",
                             Color.BLUE);
                    if(newColor != null) {
                    	wandora.topicHilights.add(topics, newColor);
                        table.refreshGUI();
                    }
                    break;
                }
                
                case REMOVE_HILIGHT: {
                    Topic[] topics = table.getSelectedTopics();
                    wandora.topicHilights.remove(topics);
                    table.refreshGUI();
                    break;
                }
                
                case REMOVE_ALL_HILIGHTS: {
                    String message = "Remove all topic hilights?";
                    int r = WandoraOptionPane.showConfirmDialog(
                    		wandora, 
                    		message, 
                    		"Confirm hilight remove", 
                    		WandoraOptionPane.YES_NO_OPTION);
                    
                    if(r == WandoraOptionPane.YES_OPTION) {
                    	wandora.topicHilights.removeAll();
                        table.refreshGUI();
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    @Override
    public String getName() {
        return "Topic Hilighter";
    }

    @Override
    public String getDescription() {
        return "Set hilight color for context topics.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
