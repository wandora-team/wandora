/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * SIContext.java
 *
 * Created on 23. lokakuuta 2007, 12:49
 *
 */

package org.wandora.application.contexts;

import org.wandora.application.gui.tree.TopicTreePanel;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.gui.table.LocatorTable;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.gui.simple.TopicLinkBasename;
import org.wandora.topicmap.*;

import java.util.*;
import java.awt.event.*;
import javax.swing.table.*;


/**
 *
 * @author akivela
 */
public class SIContext implements Context {
    
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora admin = null;
    
    
    
    /** Creates a new instance of SIContext */
    public SIContext() {
    }
    public SIContext(Wandora admin, ActionEvent actionEvent, WandoraTool contextOwner) {
        initialize(admin, actionEvent, contextOwner);
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
    
    
    public void digSIs(ArrayList locators, Topic topic) {
        if(topic == null) return;
        try {
            Collection<Locator> sis = topic.getSubjectIdentifiers();
            for(Iterator<Locator> i=sis.iterator(); i.hasNext(); ) {
                locators.add(i.next());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void digSIs(ArrayList locators, Collection topics) {
        if(topics == null) return;
        for(Iterator i=topics.iterator(); i.hasNext(); ) {
            digSIs(locators, (Topic) i.next());
        }
    }
    
    public Iterator getContextObjects(Object contextSource) {
        if(contextSource == null) return null;
        
        ArrayList contextLocators = new ArrayList();
        if(contextSource instanceof LocatorTable) {
            try {
                Locator[] selection = ((LocatorTable) contextSource).getSelectedLocators();
                contextLocators.addAll(Arrays.asList(selection));
            }
            catch (Exception e) {
                log(e);
            }
        }
        else if(contextSource instanceof Wandora) {
            try {
                Wandora w = (Wandora) contextSource;
                Topic currentTopic = w.getOpenTopic();
                if(currentTopic != null) {
                    digSIs(contextLocators, currentTopic);
                }
            }
            catch (Exception e) {
                log(e);
            }
        }
        else if(contextSource instanceof TopicLinkBasename) {
            try {
                digSIs(contextLocators, ((TopicLinkBasename) contextSource).getTopic());
            }
            catch (Exception e) {
                log(e);
            }
        }
        else if(contextSource instanceof Topic) {
            digSIs(contextLocators, (Topic) contextSource );
        }
        else if(contextSource instanceof Topic[]) {
            Topic[] topicArray = (Topic[]) contextSource;
            for(int i=0; i<topicArray.length; i++) {
                digSIs(contextLocators, topicArray[i] );
            }
        }
        else if(contextSource instanceof GraphTopicPanel) {
            digSIs(contextLocators, ((GraphTopicPanel) contextSource).getContextTopics() );
        }
        else if(contextSource instanceof TopicTable) {
            Topic[] topicArray = ((TopicTable) contextSource).getSelectedTopics();
            for(int i=0; i<topicArray.length; i++) {
                digSIs(contextLocators, topicArray[i] );
            }
        }
        else if(contextSource instanceof JTableHeader) {
            if(((JTableHeader) contextSource).getTable() instanceof TopicTable) {
                TopicTable topicTable = (TopicTable) ((JTableHeader) contextSource).getTable();
                digSIs(contextLocators, topicTable.getSelectedHeaderTopic() );
            }
        }
        
        else if(contextSource instanceof TopicTreePanel) {
            digSIs(contextLocators, ((TopicTreePanel) contextSource).getSelection() );
        }
        else if(contextSource instanceof TopicTree) {
            TopicTree tree = (TopicTree) contextSource;
            if( tree.getSelection() instanceof Topic ) {
                digSIs(contextLocators, tree.getSelection() );
            }
        }
        /*
        else if(contextSource instanceof LayerStatusPanel) {
            try {
                Iterator i = ((LayerStatusPanel) contextSource).getLayer().getTopicMap().getTopics();
                return i;
            }
            catch(Exception e) {
                log(e);
            }
        }
        else if(contextSource instanceof TopicMap) {
            TopicMap topicmap = (TopicMap) contextSource;
            try {
                digSIs(contextLocators, topicmap.getTopics());
            }
            catch(TopicMapException tme){
                log(tme);
            }
        }
         **/
        return contextLocators.iterator();
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
                contextSource instanceof LocatorTable ||
                contextSource instanceof Wandora ||
                contextSource instanceof TopicLinkBasename ||
                contextSource instanceof Topic ||
                contextSource instanceof Topic[] ||
                contextSource instanceof GraphTopicPanel ||
                contextSource instanceof TopicTable ||
                contextSource instanceof TopicTreePanel ||
                contextSource instanceof TopicTree ||
                // contextSource instanceof LayerStatusPanel ||
                contextSource instanceof JTableHeader && ((JTableHeader) contextSource).getTable() instanceof TopicTable)) {
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
