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
 * ChangeFramerate.java
 *
 */
package org.wandora.application.tools.graph;


import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.GraphNodeContext;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;



/**
 *
 * @author akivela
 */


public class ChangeFramerate extends AbstractSliderTool implements WandoraTool {
    


    /** Creates a new instance of ChangeFramerate */
    public ChangeFramerate(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    
    @Override
    public String getName(){
        return "Change framerate for graph topic panel";
    }
    

    
    @Override
    protected int getMinValue(TopicMapGraphPanel graphPanel) {
        return 5;
    }
    
    @Override
    protected int getMaxValue(TopicMapGraphPanel graphPanel) {
        return 100;
    }
    
    
    @Override
    protected int getDefaultValue(TopicMapGraphPanel graphPanel) {
        if(graphPanel != null) {
            return (int) graphPanel.getFramerate();
        }
        else {
            return 24;
        }
    }
    


    @Override
    protected void setValue(TopicMapGraphPanel graphPanel, int newValue) {
        if(graphPanel != null) {
            synchronized(graphPanel) {
                graphPanel.setFramerate(newValue);
            }
        }
    }
    


}