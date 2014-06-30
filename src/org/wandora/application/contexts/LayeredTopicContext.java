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
 * LayeredTopicContext.java
 *
 * Created on 7. huhtikuuta 2006, 13:47
 *
 */

package org.wandora.application.contexts;

import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.gui.simple.TopicLinkBasename;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;

import java.util.*;
import java.awt.event.*;
import javax.swing.table.*;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;


/**
 * This is basic context for topics. LayeredTopicContext is used to pass topics into tools
 * from various UI components of Wandora application.
 *
 * @author akivela
 */

public class LayeredTopicContext implements Context {
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora admin = null;
    
    
    
    /**
     * Creates a new instance of LayeredTopicContext
     */
    public LayeredTopicContext() {
        // Nothing here
    }
    public LayeredTopicContext(Wandora admin, ActionEvent actionEvent, WandoraTool contextOwner) {
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
        
        // *** IF CONTEXT WAS WANDORA THEN TRY TO SOLVE WANDORA'S FOCUS OWNER ***
        else {
            if( proposedContextSource instanceof Wandora ) {
                Object wandoraRegisteredContext = ((Wandora) proposedContextSource).getFocusOwner();
                if( isContextSource(wandoraRegisteredContext) ) {
                    proposedContextSource = wandoraRegisteredContext;
                }
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
        
        ArrayList contextTopics = new ArrayList();
        
        // ***** Wandora *****
        if(contextSource instanceof Wandora) {
            try {
                Wandora wandora = (Wandora) contextSource;
                Topic currentTopic = wandora.getOpenTopic();
                if(currentTopic != null) {
                    contextTopics.add(currentTopic);
                }
            }
            catch (Exception e) {
                log(e);
            }
        }
        
        // ***** TopicLinkBasename *****
        else if(contextSource instanceof TopicLinkBasename) {
            try {
                contextTopics.add( ((TopicLinkBasename) contextSource).getTopic() );
            }
            catch (Exception e) {
                log(e);
            }
        }
        
        // ***** Topic *****
        else if(contextSource instanceof Topic) {
            contextTopics.add( (Topic) contextSource );
        }
        
        // ***** Topic[] *****
        else if(contextSource instanceof Topic[]) {
            Topic[] topicArray = (Topic[]) contextSource;
            contextTopics.addAll(Arrays.asList(topicArray));
        }
        
        // ***** GraphTopicPanel *****
        else if(contextSource instanceof GraphTopicPanel) {
            contextTopics.addAll( ((GraphTopicPanel) contextSource).getContextTopics() );
        }
        
        // ***** WebViewPanel *****
        else if(contextSource instanceof WebViewPanel) {
            try {
                contextTopics.add( ((WebViewPanel) contextSource).getTopic());
            }
            catch(Exception e) { /*Ignore*/ }
        }
        
        // ***** TopicTable *****
        else if(contextSource instanceof TopicTable) {
            Topic[] topicArray = ((TopicTable) contextSource).getSelectedTopics();
            contextTopics.addAll(Arrays.asList(topicArray));
        }
        
        // ***** TopicGrid *****
        else if(contextSource instanceof TopicGrid) {
            Topic[] topicArray = ((TopicGrid) contextSource).getSelectedTopics();
            contextTopics.addAll(Arrays.asList(topicArray));
        }
        
        // ***** MixedTopicTable *****
        else if(contextSource instanceof MixedTopicTable) {
            Topic[] topicArray = ((MixedTopicTable) contextSource).getSelectedTopics();
            contextTopics.addAll(Arrays.asList(topicArray));
        }

        // ***** JTableHeader *****
        else if(contextSource instanceof JTableHeader) {
            if(((JTableHeader) contextSource).getTable() instanceof TopicTable) {
                TopicTable topicTable = (TopicTable) ((JTableHeader) contextSource).getTable();
                contextTopics.add( topicTable.getSelectedHeaderTopic() );
            }
        }
        
        // ***** SchemaTreeTopicChooser *****
        else if(contextSource instanceof SchemaTreeTopicChooser) {
            contextTopics.add( ((SchemaTreeTopicChooser) contextSource).getSelection() );
        }
        
        // ***** TopicTree *****
        else if(contextSource instanceof TopicTree) {
            TopicTree tree = (TopicTree) contextSource;
            if( tree.getSelection() instanceof Topic ) {
                contextTopics.add( tree.getSelection() );
            }
        }
        
        // ***** LayerTree *****
        else if(contextSource instanceof LayerTree) {
            TopicMap atm = admin.getTopicMap();
            LayerTree layerTree=(LayerTree)contextSource;
            Layer l=layerTree.getLastClickedLayer();
            TopicMap tm=null;
            if(l==null) tm=admin.getTopicMap();
            else tm=l.getTopicMap();
            try {
                Iterator<Topic> topics = tm.getTopics();
                Topic t = null;
                while(topics.hasNext()) {
                    t = (Topic) topics.next();
                    if(t != null && !t.isRemoved()) {
                        Topic t2 = atm.getTopic(t.getOneSubjectIdentifier());
                        if(t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        
        // ***** Layer *****
        else if(contextSource instanceof Layer){
            TopicMap topicmap = ((Layer)contextSource).getTopicMap();
            try {
                TopicMap tm = admin.getTopicMap();
                Iterator<Topic> topics = topicmap.getTopics();
                Topic t = null;
                while(topics.hasNext()) {
                    t = (Topic) topics.next();
                    if(t != null && !t.isRemoved()) {
                        Topic t2 = tm.getTopic(t.getOneSubjectIdentifier());
                        if(t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch(TopicMapException tme){
                log(tme);
            }            
        }
        
        
        // ***** SITable *****
        else if(contextSource instanceof SITable) {
            Locator[] locators = ((SITable) contextSource).getSelectedLocators();
            TopicMap topicmap = admin.getTopicMap();           
            Topic t = null;
            for(int i=0; i<locators.length; i++) {
                try {
                    t = topicmap.getTopic(locators[i]);
                    if(!contextTopics.contains(t)) {
                        contextTopics.add(t);
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        
        // ***** LayerStatusPanel *****
        else if(contextSource instanceof LayerStatusPanel) {
            try {
                TopicMap topicmap = ((LayerStatusPanel) contextSource).getLayer().getTopicMap();
                TopicMap tm = admin.getTopicMap();
                Iterator<Topic> topics = topicmap.getTopics();
                Topic t = null;
                while(topics.hasNext()) {
                    t = (Topic) topics.next();
                    if(t != null && !t.isRemoved()) {
                        Topic t2 = tm.getTopic(t.getOneSubjectIdentifier());
                        if(t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        
        // ***** TopicMap *****
        else if(contextSource instanceof TopicMap) {
            TopicMap topicmap = (TopicMap) contextSource;
            try {
                TopicMap tm = admin.getTopicMap();
                Iterator<Topic> topics = topicmap.getTopics();
                Topic t = null;
                while(topics.hasNext()) {
                    t = (Topic) topics.next();
                    if(t != null && !t.isRemoved()) {
                        Topic t2 = tm.getTopic(t.getOneSubjectIdentifier());
                        if(t2 != null && !t2.isRemoved()) {
                            contextTopics.add(t2);
                        }
                    }
                }
            }
            catch(TopicMapException tme){
                log(tme);
            }
        }
        
        // ***** OccurrenceTable *****
        else if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable otable = (OccurrenceTable) contextSource;
            contextTopics.add(otable.getTopic());
        }
        
        // ***** OccurrenceTextEditor *****
        else if(contextSource instanceof OccurrenceTextEditor) {
            OccurrenceTextEditor editor = (OccurrenceTextEditor) contextSource;
            contextTopics.add(editor.getOccurrenceTopic());
        }
        return contextTopics.iterator();
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
        // System.out.println("Checking if "+contextSource+" is a valid context source!");
        if(contextSource != null && (
                contextSource instanceof Wandora ||
                contextSource instanceof TopicLinkBasename ||
                contextSource instanceof Topic ||
                contextSource instanceof Topic[] ||
                contextSource instanceof GraphTopicPanel ||
                contextSource instanceof WebViewPanel ||
                contextSource instanceof TopicTable ||
                contextSource instanceof TopicGrid ||
                contextSource instanceof MixedTopicTable ||
                contextSource instanceof OccurrenceTable ||
                contextSource instanceof OccurrenceTextEditor ||
                contextSource instanceof SchemaTreeTopicChooser ||
                contextSource instanceof TopicTree ||
                contextSource instanceof LayerStatusPanel ||
                contextSource instanceof SITable ||
                contextSource instanceof LayerTree ||
                contextSource instanceof Layer ||
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
