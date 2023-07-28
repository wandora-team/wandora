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
 * 
 * NewTopic.java
 *
 * Created on 28. joulukuuta 2005, 21:49
 *
 */

package org.wandora.application.tools;



import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;

import org.wandora.topicmap.*;

import javax.swing.*;
import java.net.*;
import java.util.*;



/**
 * WandoraTool for topic creation. User may enter topic's base name and subject
 * identifier. If constructor argument is given, created topic is associated with
 * a context topic.
 *
 * @author akivela
 */


public class NewTopic extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public static final int MAKE_INSTANCE_OF_CONTEXT = 100;
    public static final int MAKE_SUBCLASS_OF_CONTEXT = 101;

    public int orders = 0;
    public boolean confirm = true;
    
    
    /** Creates a new instance of NewTopic */
    public NewTopic() {
    }
    
    
    public NewTopic(int orders) {
        this.orders = orders;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Topic newTopic = null;
        String confirmMessage = null;
        String basename = null;
        int answer = 0;
        Topic contextTopic = null;
        
        // --- New topic is created using a custom dialog panel.
        newTopic = createNewTopic(wandora, "Create new topic", context);
        if(newTopic==null) return;
        
        // --- Now we are going to post process the created topic.
        switch(orders) {
            case MAKE_INSTANCE_OF_CONTEXT: {
                Iterator contextTopics = getContext().getContextObjects();
                if(contextTopics != null && contextTopics.hasNext()) {
                    while(contextTopics.hasNext()) {
                        contextTopic = (Topic) contextTopics.next();
                        if(confirm) {
                            basename = contextTopic.getBaseName();
//                                confirmMessage = "Make created topic instance of '" + basename + "'?";
//                                answer = WandoraOptionPane.showConfirmDialog(admin, confirmMessage,"Make instance" );
//                                if(answer == WandoraOptionPane.YES_OPTION) {
                                newTopic.addType(contextTopic);
//                                }
//                                else if(answer == WandoraOptionPane.CANCEL_OPTION) {
//                                    break;
//                                }
                        }
                        else {
                            newTopic.addType(contextTopic);
                        }
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(
                    		wandora, 
                    		"Select class topic for the new topic.", 
                    		"Select class topic", 
                    		WandoraOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            case MAKE_SUBCLASS_OF_CONTEXT: {
                Iterator contextTopics = getContext().getContextObjects();
                if(contextTopics != null && contextTopics.hasNext()) {
                    while(contextTopics.hasNext()) {
                        contextTopic = (Topic) contextTopics.next();
                        if(confirm) {
                            basename = contextTopic.getBaseName();
//                                confirmMessage = "Make created topic subclass of '" + basename + "'?";
//                                answer = WandoraOptionPane.showConfirmDialog(admin, confirmMessage,"Make subclass of" );
//                                if(answer == WandoraOptionPane.YES_OPTION) {
                                SchemaBox.setSuperClass(newTopic, contextTopic);
//                                }
//                                else if(answer == WandoraOptionPane.CANCEL_OPTION) {
//                                    break;
//                                }
                        }
                        else {
                            SchemaBox.setSuperClass(newTopic, contextTopic);
                        }
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(
                    		wandora, 
                    		"Select superclass topic for the new topic.", 
                    		"Select superclass topic", 
                    		WandoraOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            default: {
                break;
            }
        }
    }
    

    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/new_topic.png");
    }
    @Override
    public String getName() {
        return "New Topic";
    }

    @Override
    public String getDescription() {
        return "Creates new topic in Wandora. "+
               "User may enter initial basename and SI for the new topic.";
    }
    

    
    
    // -------------------------------------------------------------------------
    
    
    
    public Topic createNewTopic(Wandora wandora, String windowTitle, Context context)  throws TopicMapException {
        Topic newTopic = null;
        TopicMap topicMap = wandora.getTopicMap();

        JDialog newTopicDialog = new JDialog(wandora, windowTitle, true);
        newTopicDialog.setSize(600, 250);
        wandora.centerWindow(newTopicDialog);
        NewTopicPanel newTopicPanel = new NewTopicPanel(newTopicDialog);

        //String name=WandoraOptionPane.showInputDialog(admin, "Enter topic base name");
        String name = newTopicPanel.getBasename();
        String si = newTopicPanel.getSI();
        
        if(name != null) name = name.trim();
        if(si != null) si = si.trim();
        
        if(newTopicPanel.getAccepted()) {
            if((name != null && name.length() > 0) || (si != null && si.length() > 0)) {
                newTopic=topicMap.createTopic();
                if(name != null && name.length() > 0) {
                    if(TMBox.checkBaseNameChange(wandora,newTopic,name)!=ConfirmResult.yes){
                        newTopic.remove();
                        return null;
                    }
                }
                if(si!=null && si.length()>0) {
                    if(TMBox.checkSubjectIdentifierChange(wandora,newTopic,topicMap.createLocator(si),true)!=ConfirmResult.yes){
                        newTopic.remove();
                        return null;
                    }
                }
                if(name != null && name.length()>0) {
                    newTopic.setBaseName(name);
                }
                if(si != null && si.length() > 0) {
                    try {
                        URL siUrl = new URL(si);
                        newTopic.addSubjectIdentifier(topicMap.createLocator(siUrl.toExternalForm()));
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                if(newTopic.getSubjectIdentifiers().isEmpty()) {
                    Locator defaultSI = topicMap.makeSubjectIndicatorAsLocator();
                    newTopic.addSubjectIdentifier(defaultSI);
                    WandoraOptionPane.showMessageDialog(wandora, "Valid subject identifier was not available. Topic was given default subject identifier '"+defaultSI.toExternalForm()+"'.", "Default SI given to new topic", WandoraOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    // newTopic is layeredTopic so it may not have a SI in the selected layer
                    newTopic.addSubjectIdentifier(newTopic.getOneSubjectIdentifier());
                }
            }
            else {
                WandoraOptionPane.showMessageDialog(wandora, "No subject identifier nor basename was given. No topic created!", "No topic created", WandoraOptionPane.WARNING_MESSAGE);
            }
        }
        return newTopic;
    }
        
    
    
}
