/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * AbstractGraphTool.java
 *
 * Created on 13.6.2007, 15:32
 *
 */

package org.wandora.application.tools.graph;

import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;





/**
 *
 * @author akivela
 */
public abstract class AbstractGraphTool extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	private TopicMapGraphPanel graphPanel = null;
    
    
    
    /** Creates a new instance of AbstractGraphTool */
    public AbstractGraphTool(TopicMapGraphPanel gp) {
        graphPanel = gp;
    }
    public AbstractGraphTool() {
    
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context){
        try {
            synchronized(solveGraphPanel(wandora,context)){
                executeSynchronized(wandora,context);
            }
        }
        catch(Exception e) {
            System.out.println("Exception '"+e.toString()+"' captured in abstract graph tool.");
            singleLog(e);
        }
    }
    
    public abstract void executeSynchronized(Wandora wandora, Context context);
    
    public TopicMapGraphPanel solveGraphPanel(Wandora wandora, Context context) {
        if(graphPanel != null) {
            return graphPanel;
        }
        if(context != null) {
            Object contextSource = context.getContextSource();
            if(contextSource != null && contextSource instanceof GraphTopicPanel) {
                return ((GraphTopicPanel)contextSource).getGraphPanel();
            }
        }
        if(wandora != null) {
            TopicPanel topicPanel = wandora.getTopicPanel();
            if(topicPanel != null && topicPanel instanceof DockingFramePanel) {
                topicPanel = ((DockingFramePanel) topicPanel).getCurrentTopicPanel();
            }
            if(topicPanel != null && topicPanel instanceof GraphTopicPanel) {
                return ((GraphTopicPanel)topicPanel).getGraphPanel();
            }
        }
        return null;
    }
    
    public VModel solveModel(Wandora wandora, Context context){
        TopicMapGraphPanel panel=solveGraphPanel(wandora,context);
        if(panel==null) return null;
        else return panel.getModel();
    }
    
    public TopicMapGraphPanel solveGraphPanel(VNode vnode) {
        if(vnode != null) {
            return vnode.getPanel();
        }
        return null;
    }
    
    @Override
    public boolean allowMultipleInvocations() {
        return false;
    }
}
