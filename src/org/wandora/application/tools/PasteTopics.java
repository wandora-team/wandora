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
 * 
 * PasteTopics.java
 *
 * Created on 30. lokakuuta 2005, 20:19
 *
 */

package org.wandora.application.tools;





import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.utils.*;
import java.util.*;




/**
 *
 * @author akivela
 */
public class PasteTopics extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public static final int PASTE_SIS = 99;
    public static final int PASTE_BASENAMES = 1000;
    
    public static final int INCLUDE_NAMES = 1001;
    public static final int INCLUDE_CLASSES = 1002;
    public static final int INCLUDE_INSTANCES = 1003;
    public static final int INCLUDE_SLS = 1004;
    public static final int INCLUDE_SIS = 1005;
    public static final int INCLUDE_PLAYERS = 1006;
    public static final int INCLUDE_TEXTDATAS = 1007;
    public static final int INCLUDE_NOTHING = 9999;
    
    public int includeOrders = INCLUDE_NOTHING;
    public int pasteOrders = PASTE_BASENAMES;
    
    public boolean forceStop = false;

    public boolean PLAYER_SHOULD_NOT_BE_EQUAL_TO_INSTANCE = true;
    public boolean ASK_TOPIC_CREATION = true;
    public boolean ACCEPT_UNKNOWN_TOPICS = false;
    public boolean USER_HAS_BEEN_ASKED = false;
    
    public Iterator contextTopics = null;
    public Topic currentTopic = null;
    public TopicMap topicMap = null;
    
    
    
    public PasteTopics() {
        this(INCLUDE_NOTHING);
    }
    public PasteTopics(int includeOrders) {
        this.includeOrders = includeOrders;
    }
    public PasteTopics(int includeOrders, int pasteOrders) {
        this.pasteOrders = pasteOrders;
        this.includeOrders = includeOrders;
    }
    public PasteTopics(Context context) {
        this(INCLUDE_NOTHING);
        setContext(context);
    }
    public PasteTopics(Context context, int includeOrders) {
        this(includeOrders);
        setContext(context);
    }
    public PasteTopics(Context context, int includeOrders, int pasteOrders) {
        this(includeOrders, pasteOrders);
        setContext(context);
    }
    
  
    
    public void initialize(Wandora wandora) {
        ASK_TOPIC_CREATION = true;
        ACCEPT_UNKNOWN_TOPICS = false;
        USER_HAS_BEEN_ASKED = false;

        forceStop = false;
        topicMap = wandora.getTopicMap();
    }
    
  
    
    @Override
    public void execute(Wandora wandora, Context context) {
        initialize(wandora);

        try {
            if(WandoraOptionPane.showConfirmDialog(
            		wandora,
            		"Are you sure you want to paste topics using text on clipboard?",
            		"Confirm paste", 
            		WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION) {
            	
                String tabText = ClipboardBox.getClipboard();
                StringTokenizer st = new StringTokenizer(tabText, "\n");

                Topic occurrenceType = null;
                Topic occurrenceScope = null;
                if(includeOrders == INCLUDE_TEXTDATAS) {
                    occurrenceType=wandora.showTopicFinder("Select occurrence type...");                
                    if(occurrenceType == null) return;
                    occurrenceScope=wandora.showTopicFinder("Select occurrence language (scope)...");
                    if(occurrenceScope == null) return;
                }
                Topic associationType = null;
                Topic topicRole = null;
                Topic playerRole = null;
                if(includeOrders == INCLUDE_PLAYERS) {
                    associationType=wandora.showTopicFinder("Select association type...");                
                    if(associationType == null) return;
                    topicRole=wandora.showTopicFinder("Select instance's role...");                
                    if(topicRole == null) return;
                    playerRole=wandora.showTopicFinder("Select player's role...");                
                    if(playerRole == null) return;
                }
                Set<Topic> nameScope = new HashSet<>();
                if(includeOrders == INCLUDE_NAMES) {
                    Topic langTopic=wandora.showTopicFinder("Select name language...");                
                    if(langTopic == null) return;
                    Topic scopeTopic=wandora.showTopicFinder("Select name scope...");
                    if(scopeTopic == null) return;
                    nameScope.add(langTopic);
                    nameScope.add(scopeTopic);
                }
                try {
                    String names = null;
                    while(st.hasMoreTokens() && !forceStop && !forceStop()) {
                        names = st.nextToken();
                        StringTokenizer nt = new StringTokenizer(names, "\t");
                        try {
                            String topicIdentifier = null;
                            if(nt.hasMoreTokens()) {
                                topicIdentifier = nt.nextToken();
                                if(topicIdentifier != null && topicIdentifier.length() > 0) {
                                    Topic topic = getTopic(wandora, topicMap, topicIdentifier, pasteOrders == PASTE_BASENAMES);
                                    //log("topic: " + topic);
                                    if(!ACCEPT_UNKNOWN_TOPICS && !isKnownTopic(topic, context) && !USER_HAS_BEEN_ASKED) {
                                        USER_HAS_BEEN_ASKED = true;
                                        ACCEPT_UNKNOWN_TOPICS = WandoraOptionPane.showConfirmDialog(wandora, "Clipboard data addresses topics not in selection! Paste anyway?") == WandoraOptionPane.YES_OPTION;
                                    }
                                    if(ACCEPT_UNKNOWN_TOPICS || isKnownTopic(topic, context)) {
                                        contextTopics = context.getContextObjects();
                                        
                                        while(contextTopics.hasNext()) {
                                            currentTopic = (Topic) contextTopics.next();

                                            if(topic != null && currentTopic != null) {
                                                topicPrologue(topic);
                                                switch(includeOrders) {
                                                    case INCLUDE_NAMES: {
                                                        if(nt.hasMoreTokens()) {
                                                            topic.setVariant(nameScope, nt.nextToken());
                                                        }
                                                        break;
                                                    }
                                                    case INCLUDE_CLASSES: {
                                                        while(nt.hasMoreTokens() && !forceStop) {
                                                            String instanceClass = nt.nextToken();
                                                            Topic instanceClassTopic = getTopic(wandora, topicMap, instanceClass, true);
                                                            if(instanceClassTopic != null) {
                                                                topic.addType(instanceClassTopic);
                                                            }
                                                        }
                                                        break;
                                                    }
                                                    case INCLUDE_INSTANCES: {
                                                        while(nt.hasMoreTokens() && !forceStop) {
                                                            String instance = nt.nextToken();
                                                            Topic instanceTopic = getTopic(wandora, topicMap, instance, true);
                                                            if(instanceTopic != null) {
                                                                instanceTopic.addType(topic);
                                                            }
                                                        }
                                                        break;
                                                    }
                                                    case INCLUDE_SIS: {
                                                        while(nt.hasMoreTokens() && !forceStop) {
                                                            String si = nt.nextToken();
                                                            if(si != null && si.length() > 0) {
                                                                try {
                                                                    //System.out.println("Adding SI '" +si+ "' to " + topic.getBaseName());
                                                                    topic.addSubjectIdentifier(new Locator(si));
                                                                }
                                                                catch (Exception e) {
                                                                    log(e);
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    }
                                                    case INCLUDE_SLS: {
                                                        if(nt.hasMoreTokens() && !forceStop) {
                                                            String sl = nt.nextToken();
                                                            if(sl != null && sl.length() > 0) {
                                                                try {
                                                                    topic.setSubjectLocator(new Locator(sl));
                                                                }
                                                                catch (Exception e) {
                                                                    log(e);
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    }
                                                    case INCLUDE_TEXTDATAS: {
                                                        if(nt.hasMoreTokens()) {
                                                            topic.setData(occurrenceType, occurrenceScope, nt.nextToken());
                                                        }
                                                        break;
                                                    }
                                                    case INCLUDE_PLAYERS: {
                                                        while(nt.hasMoreTokens() && !forceStop) {
                                                            String player = nt.nextToken();
                                                            if(player != null && player.length() > 0) {
                                                                if(PLAYER_SHOULD_NOT_BE_EQUAL_TO_INSTANCE && player.equals(topic)) break;
                                                                Topic playerTopic = getTopic(wandora, topicMap, player, true);
                                                                if(playerTopic != null && !forceStop) {
                                                                    Association a = topicMap.createAssociation(associationType);
                                                                    a.addPlayer(topic, topicRole);
                                                                    a.addPlayer(playerTopic, playerRole);
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    }
                                                }
                                                topicEpilogue(topic);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            log(e);
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }

    
    
    
    public void topicPrologue(Topic topic) {
        
    }
    
    public void topicEpilogue(Topic topic) {
        
    }
    
    
    @Override
    public String getName() {
        return "Paste Topics";
    }

    @Override
    public String getDescription() {
        return "Injects clipboard topics to the topic map.";
    }   
    
    public void setPasteOrders(int pasteOrders) {
        this.pasteOrders = pasteOrders;
    }
    
    
    
    
    public Topic getTopic(Wandora admin, TopicMap topicMap, String identifier, boolean isBasename)  throws TopicMapException {
        if(identifier != null) {
            identifier = Textbox.trimExtraSpaces(identifier);
            if(identifier.length() > 0) {
                Topic t = null;
                // Identifier is base name!
                if(isBasename) {
                    t = topicMap.getTopicWithBaseName(identifier);
                    if(t == null && identifier.indexOf(',') != -1) {
                        t = topicMap.getTopicWithBaseName(identifier.substring(0, identifier.indexOf(',')));
                        if(t != null) {
                            //log("t == " + t.getBaseName());
                            t.setBaseName(identifier);
                        }
                    }
                    //if(t != null) log("t.basename == " + t.getBaseName());
                    //else log("t == null (for " + identifier + ")");
                }
                else {
                    t = topicMap.getTopicWithBaseName(identifier);
                    if(t == null) t = topicMap.getTopic(identifier);
                }
                
                if(t == null) {
                    try {
                        boolean createTopic = true;
                        if(ASK_TOPIC_CREATION) {
                            String[] topicCreationOptions = new String[] { 
                                "Create topic '" + identifier + "'",
                                "Create topic '" + identifier + "' and all subsequent topics",
                                "Don't create topic '" + identifier + "' but continue operation",
                                "Don't create topic '" + identifier + "' and cancel operation",
                            };
                            String answer = (String) WandoraOptionPane.showOptionDialog(admin ,"Topic does not exists! Would you like to create new topic for '" + identifier + "'?","Create new topic?",WandoraOptionPane.PLAIN_MESSAGE, topicCreationOptions, topicCreationOptions[0]);
                            //log("answer == " + answer);
                            if(topicCreationOptions[0].equalsIgnoreCase(answer)) { createTopic = true; }
                            else if(topicCreationOptions[1].equalsIgnoreCase(answer)) { ASK_TOPIC_CREATION = false; createTopic = true; }
                            else if(topicCreationOptions[2].equalsIgnoreCase(answer)) { createTopic = false; }
                            else if(topicCreationOptions[3].equalsIgnoreCase(answer)) { createTopic = false; forceStop = true; }
                            else createTopic = false;
                        }
                        if(createTopic) {
                            if(isBasename) {
                                t = topicMap.createTopic();
                                t.setBaseName(identifier);
                                String si = "http://wandora.org/si/" + System.currentTimeMillis();
                                int counter = 1000;
                                while(topicMap.getTopic(si) != null && --counter > 0) {
                                    si = "http://wandora.org/si/" + System.currentTimeMillis() + Math.floor(Math.random() * 10000);
                                }
                                t.addSubjectIdentifier(new Locator(si));
                            }
                            else { // is subjet locator.....
                                Locator l = new Locator(identifier);
                                if(l != null) {
                                    t = topicMap.createTopic();
                                    t.addSubjectIdentifier(l);
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        WandoraOptionPane.showMessageDialog(admin ,"Unable to create new topic with identifier '" + identifier + "'. Exception occurred: " + e.getMessage(),"Exception occurred!",WandoraOptionPane.ERROR_MESSAGE);
                    }
                }
                return t;
            }
        }
        return null;
    }
    
    
    
    public boolean isKnownTopic(Topic topic, Context context)  throws TopicMapException {
        if(topic == null || context == null) return false;
        Iterator topics = context.getContextObjects();
        while(topics.hasNext()) {
            if(topic.mergesWithTopic((Topic) topics.next())) return true;
        }
        return false;
    }
    
    
    

    public void setDisplayName(Topic t, String lang, String name)  throws TopicMapException {
        if(t != null) {
            String variantName = Textbox.trimExtraSpaces(name);
            if(variantName != null && variantName.length() > 0) {
                String langsi=XTMPSI.getLang(lang);
                Topic langT =t.getTopicMap().getTopic(langsi);
                String dispsi=XTMPSI.DISPLAY;
                Topic dispT=t.getTopicMap().getTopic(dispsi);
                Set<Topic> scope=new LinkedHashSet<>();
                if(langT!=null) scope.add(langT);
                if(dispT!=null) scope.add(dispT);
                t.setVariant(scope, variantName);
            }
        }
    }
    
    
    
    public void setTextdata(Topic t, Topic type, String version, String data)  throws TopicMapException {
        if(t != null) {
            data = Textbox.trimExtraSpaces(data);
            if(data != null && data.length() > 0) {
                String langsi=XTMPSI.getLang(version);
                Topic langT =t.getTopicMap().getTopic(langsi);
                t.setData(type, langT, data);
            }
        }
    }
    
    

}
