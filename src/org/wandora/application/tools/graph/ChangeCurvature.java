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
import org.wandora.application.gui.topicpanels.graphpanel.projections.HyperbolicProjection;
import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;

/**
 *
 * @author akivela
 */


public class ChangeCurvature extends AbstractSliderTool implements WandoraTool {

    
    private int key = HyperbolicProjection.CURVATURE;
    private int minVal = 1;
    private int maxVal = 100;

    
    /** Creates a new instance of ChangeCurvature */
    public ChangeCurvature(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    
    @Override
    public String getName(){
        return "Change curvature of a graph topic panel";
    }
    
    
    
    
    
    @Override
    protected int getMinValue(TopicMapGraphPanel graphPanel) {
        return minVal;
    }
    
    @Override
    protected int getMaxValue(TopicMapGraphPanel graphPanel) {
        return maxVal;
    }
    
    
    @Override
    protected int getDefaultValue(TopicMapGraphPanel graphPanel) {
        if(graphPanel != null) {
            Projection projection = graphPanel.getProjection();
            double val = projection.get(key);
            int defaultValue = scaleToInteger(val, 0.5, 10.0, minVal, maxVal);
            if(defaultValue < minVal) defaultValue = minVal;
            if(defaultValue > maxVal) defaultValue = maxVal;
            return defaultValue;
        }
        else {
            return minVal;
        }
    }
    


    @Override
    protected void setValue(TopicMapGraphPanel graphPanel, int newValue) {
        if(graphPanel != null) {
            synchronized(graphPanel) {
                Projection projection = graphPanel.getProjection();
                double vald = scaleToDouble(newValue, minVal, maxVal, 0.5, 10.0);
                projection.set(key, vald);
            }
        }
    }
    

}