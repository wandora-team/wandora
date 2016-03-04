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
 * GraphAllNodesContext.java
 *
 * Created on 14. kesäkuuta 2007, 13:18
 *
 */

package org.wandora.application.contexts;

import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import java.util.*;
import java.awt.event.*;


/**
 *
 * @author akivela
 */
public class GraphAllNodesContext implements Context {
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora admin = null;
    
    
    /** Creates a new instance of NodeContext */
    public GraphAllNodesContext() {
    }
    
    
    @Override
    public void initialize(Wandora admin, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.admin = admin;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
        
        Object proposedContextSource = UIBox.getActionsRealSource(actionEvent);
        if( !isContextSource(proposedContextSource) ) {
            proposedContextSource = admin.getFocusOwner();
            if( !isContextSource(proposedContextSource) ) {
                proposedContextSource = admin;
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
        
        ArrayList contextNodes = new ArrayList();
        if(contextSource instanceof Wandora) {
            try {
                Wandora w = (Wandora) contextSource;
                TopicPanel currentTopicPanel = w.getTopicPanel();
                if(currentTopicPanel  instanceof GraphTopicPanel) {
                    contextNodes.addAll( ((GraphTopicPanel) currentTopicPanel).getGraphPanel().getModel().getNodes() );
                }
            }
            catch (Exception e) {
                log(e);
            }
        }
        else if(contextSource instanceof GraphTopicPanel) {
            contextNodes.addAll( ((GraphTopicPanel) contextSource).getGraphPanel().getModel().getNodes() );
        }
        else if(contextSource instanceof TopicMapGraphPanel) {
            contextNodes.addAll( ((TopicMapGraphPanel) contextSource).getModel().getNodes() );
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
