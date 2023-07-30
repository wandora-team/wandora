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
 * NodeContext.java
 *
 * Created on 13.6.2007, 15:04
 *
 */

package org.wandora.application.contexts;



import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.GraphTopicPanel;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;

/**
 *
 * @author akivela
 */
public class GraphNodeContext implements Context {
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora wandora = null;
    
    
    /** Creates a new instance of GraphNodeContext */
    public GraphNodeContext() {
    }
    public GraphNodeContext(TopicMapGraphPanel gp) {
        
    }
    
    @Override
    public void initialize(Wandora wandora, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.wandora = wandora;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
        
        Object proposedContextSource = UIBox.getActionsRealSource(actionEvent);
        if( !isContextSource(proposedContextSource) ) {
            proposedContextSource = wandora.getFocusOwner();
            if( !isContextSource(proposedContextSource) ) {
                proposedContextSource = wandora;
            }
        }
        setContextSource( proposedContextSource );
    }
    
    
    
    @Override
    public ActionEvent getContextEvent() {
        return actionEvent;
    }
    
    
    
    @Override
    public Iterator getContextObjects() {
        return getContextObjects( getContextSource() );
    }
    
    
    
    public Iterator getContextObjects(Object contextSource) {
        if(contextSource == null) return null;
        
        System.out.println("GraphNodeContext contextsource == "+contextSource);
        
        List<VNode> contextNodes = new ArrayList<>();
        if(contextSource instanceof Wandora) {
            try {
                Wandora wandora = (Wandora) contextSource;
                TopicPanel currentTopicPanel = wandora.getTopicPanel();
                if(currentTopicPanel != null && currentTopicPanel instanceof DockingFramePanel) {
                    currentTopicPanel = ((DockingFramePanel) currentTopicPanel).getCurrentTopicPanel();
                }
                if(currentTopicPanel != null && currentTopicPanel instanceof GraphTopicPanel) {
                    contextNodes.addAll( ((GraphTopicPanel) currentTopicPanel).getGraphPanel().getSelectedNodes() );
                    if(contextNodes.isEmpty()) {
                        //contextNodes.add( ((GraphTopicPanel) currentTopicPanel).getGraphPanel().getMouseOverNode() );
                    }
                }
                if(currentTopicPanel != null && currentTopicPanel instanceof TopicMapGraphPanel) {
                    contextNodes.addAll( ((TopicMapGraphPanel) currentTopicPanel).getSelectedNodes() );
                    if(contextNodes.isEmpty()) {
                        //contextNodes.add( ((TopicMapGraphPanel) currentTopicPanel).getMouseOverNode() );
                    }
                }
            }
            catch (Exception e) {
                log(e);
            }
        }

        else if(contextSource instanceof GraphTopicPanel) {
            contextNodes.addAll( ((GraphTopicPanel) contextSource).getGraphPanel().getSelectedNodes() );
            if(contextNodes.isEmpty()) {
                //contextNodes.add( ((GraphTopicPanel) contextSource).getGraphPanel().getMouseOverNode() );
            }
        }
        else if(contextSource instanceof TopicMapGraphPanel) {
            contextNodes.addAll( ((TopicMapGraphPanel) contextSource).getSelectedNodes() );
            if(contextNodes.isEmpty()) {
                //contextNodes.add( ((TopicMapGraphPanel) contextSource).getMouseOverNode() );
            }
        }
        return contextNodes.iterator();
    }
    
    

    @Override
    public void setContextSource(Object proposedContextSource) {
        if(isContextSource(proposedContextSource)) {
            contextSource = proposedContextSource;
        }
        else {
            contextSource = null;
        }
    }
    
    
    public boolean isContextSource(Object contextSource) {
        if(contextSource != null && (
                contextSource instanceof Wandora ||
                contextSource instanceof GraphTopicPanel) ) {
                    return true;
        }
        return false;
    }
    
    
    
    @Override
    public Object getContextSource() {
        return contextSource;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void log(Exception e) {
        if(contextOwner != null) contextOwner.log(e);
        else e.printStackTrace();
    }
}
