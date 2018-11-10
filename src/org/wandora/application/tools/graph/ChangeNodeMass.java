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
 * ChangeNodeMass.java
 *
 */
package org.wandora.application.tools.graph;



import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.GraphNodeContext;
import org.wandora.application.gui.topicpanels.graphpanel.AbstractNode;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;



/**
 *
 * @author akivela
 */


public class ChangeNodeMass extends AbstractSliderTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ChangeStiffness */
    public ChangeNodeMass(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    
    @Override
    public String getName(){
        return "Change node mass of graph topic panel";
    }

    
    @Override
    protected int getMinValue(TopicMapGraphPanel graphPanel) {
        return 1;
    }
    
    
    @Override
    protected int getMaxValue(TopicMapGraphPanel graphPanel) {
        return 100;
    }
    
    
    @Override
    protected int getDefaultValue(TopicMapGraphPanel graphPanel) {
        return scaleToInteger(AbstractNode.massMultiplier, 0.1, 10.0, 1, 100);
    }
    

    @Override
    protected void setValue(TopicMapGraphPanel graphPanel, int newValue) {
        AbstractNode.massMultiplier = scaleToDouble(newValue, 1, 100, 0.1, 10.0);
    }

}