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
 */


package org.wandora.application.tools.webview;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.SchemaBox;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 * This tool should be executed in context of the WebViewTopicPanel.
 * Create topic using the web location and add web location to the created topic
 * as a subject identifier. Depending on the configuration, the tool associates
 * the created topic with the current topic. Association may be 
 * instance-class, subclass-superclass or document-relation.
 *
 * @author akivela
 */


public class CreateWebLocationTopic extends AbstractWebViewTool {
    

	private static final long serialVersionUID = 1L;
	
	public boolean MAKE_INSTANCE_OF_CURRENT = false;
    public boolean MAKE_SUBCLASS_OF_CURRENT = false;
    public boolean MAKE_INSTANCE_OF_DOCUMENT_TOPIC = true;
    public boolean ASSOCIATE_WITH_CURRENT = true;

    public CreateWebLocationTopic(boolean p1, boolean p2, boolean p3, boolean p4) {
        MAKE_INSTANCE_OF_CURRENT = p1;
        MAKE_SUBCLASS_OF_CURRENT = p2;
        MAKE_INSTANCE_OF_DOCUMENT_TOPIC = p3;
        ASSOCIATE_WITH_CURRENT = p4;
    }
    
    
    public CreateWebLocationTopic() {
        
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        try {
            String location = getWebLocation(context);
            if(location != null && location.length() > 0) {
                TopicMap tm = wandora.getTopicMap();
                Topic locationTopic = tm.getTopic(location);
                if(locationTopic == null) {
                    locationTopic = tm.createTopic();
                    locationTopic.addSubjectIdentifier(new Locator(location));
                }
                if(MAKE_INSTANCE_OF_CURRENT) {
                    Topic currentTopic = getTopic(context);
                    if(currentTopic != null) {
                        locationTopic.addType(currentTopic);
                    }
                }
                if(MAKE_SUBCLASS_OF_CURRENT) {
                    Topic currentTopic = getTopic(context);
                    if(currentTopic != null) {
                        Topic superClassTopic = tm.getTopic(XTMPSI.SUPERCLASS);
                        Topic subClassTopic = tm.getTopic(XTMPSI.SUBCLASS);
                        Topic superSubClassTopic = tm.getTopic(XTMPSI.SUPERCLASS_SUBCLASS);
                        if(superClassTopic != null && subClassTopic != null && superSubClassTopic != null) {
                            Association a = tm.createAssociation(superSubClassTopic);
                            a.addPlayer(locationTopic, subClassTopic);
                            a.addPlayer(currentTopic, superClassTopic);
                        }
                    }
                }
                if(MAKE_INSTANCE_OF_DOCUMENT_TOPIC) {
                    Topic documentTopic = getDocumentTopic(tm);
                    if(documentTopic != null) {
                        locationTopic.addType(documentTopic);
                    }
                }
                if(ASSOCIATE_WITH_CURRENT) {
                    Topic associationType = tm.getTopic(SchemaBox.DEFAULT_ASSOCIATION_SI);
                    if(associationType == null) {
                        associationType = tm.createTopic();
                        associationType.addSubjectIdentifier(new Locator(SchemaBox.DEFAULT_ASSOCIATION_SI));
                    }
                    Topic role1 = tm.getTopic("http://wandora.org/si/core/default-role-1");
                    if(role1 == null) {
                        role1 = tm.createTopic();
                        role1.addSubjectIdentifier(new Locator("http://wandora.org/si/core/default-role-1"));
                    }
                    Topic role2 = tm.getTopic("http://wandora.org/si/core/default-role-2");
                    if(role2 == null) {
                        role2 = tm.createTopic();
                        role2.addSubjectIdentifier(new Locator("http://wandora.org/si/core/default-role-2"));
                    }
                    Topic currentTopic = getTopic(context);
                    if(currentTopic != null) {
                        Association a = tm.createAssociation(associationType);
                        a.addPlayer(currentTopic, role1);
                        a.addPlayer(locationTopic, role2);
                    }
                }
            }
            else {
                log("No location available, aborting.");
            }
        }
        catch(Exception ex) {
            log(ex);
        }
    }
    
    
    private static final String DOCUMENT_SI = "http://wandora.org/si/document";
    private Topic getDocumentTopic(TopicMap tm) {
        if(tm != null) {
            try {
                Topic documentTopic = tm.getTopic(DOCUMENT_SI);
                if(documentTopic == null) {
                    documentTopic = tm.createTopic();
                    documentTopic.addSubjectIdentifier(new Locator(DOCUMENT_SI));
                    documentTopic.setBaseName("Document");
                    
                    Topic wandoraTopic = tm.getTopic(TMBox.WANDORACLASS_SI);
                    Topic superClassTopic = tm.getTopic(XTMPSI.SUPERCLASS);
                    Topic subClassTopic = tm.getTopic(XTMPSI.SUBCLASS);
                    Topic superSubClassTopic = tm.getTopic(XTMPSI.SUPERCLASS_SUBCLASS);
                    if(wandoraTopic != null && superClassTopic != null && subClassTopic != null && superSubClassTopic != null) {
                        Association a = tm.createAssociation(superSubClassTopic);
                        a.addPlayer(documentTopic, subClassTopic);
                        a.addPlayer(wandoraTopic, superClassTopic);
                    }
                }
                return documentTopic;
            }
            catch(Exception e) {}
        }
        return null;
    }
    
    
    @Override
    public String getDescription() {
        return "Creates topic and sets the current web location as topic's subject identifier." +
           (MAKE_INSTANCE_OF_CURRENT ? " Make the topic instance of current topic." : "") +
           (MAKE_SUBCLASS_OF_CURRENT ? " Make the topic subclass of current topic." : "") + 
           (MAKE_INSTANCE_OF_DOCUMENT_TOPIC ? " Make the topic instance of specific document topic." : "") +
           (ASSOCIATE_WITH_CURRENT ? " Associate the topic with current topic." : "");
    }
    
    
    @Override
    public String getName() {
        return "Create topic using the web location";
    }
}
