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
 * FindAssociationsInOccurrenceSimple.java
 *
 * Created on 27.12.2008, 10:57
 *
 */


package org.wandora.application.tools.associations;


import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import java.util.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;


/**
 *
 * @author akivela
 */
public class FindAssociationsInOccurrenceSimple extends AbstractWandoraTool implements WandoraTool {

	
	private static final long serialVersionUID = 1L;


	public static final String OPTIONS_PREFIX = "options.occurrence.findassociations.";
    
    
    private boolean requiresRefresh = false;
    
    public static boolean LOOK_BASE_NAME = true;
    public static boolean LOOK_VARIANT_NAMES = true;
    public static boolean LOOK_SIS = false;
    
    public static boolean CASE_INSENSITIVE = true;
    
    public static String BASE_SI = "http://wandora.org/si/schema/";
    
    
    
    
    /** Creates a new instance of FindAssociationsInOccurrenceSimple */
    public FindAssociationsInOccurrenceSimple() {
    }
    
    public FindAssociationsInOccurrenceSimple(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "Find associations in occurrence";
    }
    @Override
    public String getDescription() {
        return "Recognize topics in occurrence text and associate occurrence's topic to recognized topics.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    

    @Override
    public void initialize(Wandora wandora, org.wandora.utils.Options options,String prefix) throws TopicMapException {
        try {
            LOOK_BASE_NAME = options.getBoolean(OPTIONS_PREFIX+"checkbasename", true);
            LOOK_VARIANT_NAMES = options.getBoolean(OPTIONS_PREFIX+"checkvariantnames", true);
            LOOK_SIS = options.getBoolean(OPTIONS_PREFIX+"checksis", false);
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        try {
            initialize(wandora, options, prefix);
            GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Configurable options for find associations in occurrence","Find associations in occurrence options",true,new String[][]{
                new String[]{"Look in base names","boolean", LOOK_BASE_NAME ? "true" : "false", null},
                new String[]{"Look in variant names","boolean", LOOK_VARIANT_NAMES ? "true" : "false", null},
                new String[]{"Look in subject identifiers","boolean", LOOK_SIS ? "true" : "false", null},
            },wandora);
            god.setVisible(true);
            if(god.wasCancelled()) return;
            // ---- ok ----
            Map<String,String> values=god.getValues();
            LOOK_BASE_NAME = ("true".equals(values.get("Look in base names")));
            LOOK_VARIANT_NAMES = ("true".equals(values.get("Look in variant names")));
            LOOK_SIS = ("true".equals(values.get("Look in subject identifiers")));
            writeOptions(wandora, options, prefix);
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    @Override
    public void writeOptions(Wandora wandora, org.wandora.utils.Options options, String prefix){
        options.put(OPTIONS_PREFIX+"checkbasename", LOOK_BASE_NAME ? "true" : "false" );
        options.put(OPTIONS_PREFIX+"checkvariantnames", LOOK_VARIANT_NAMES ? "true" : "false");
        options.put(OPTIONS_PREFIX+"checksis", LOOK_SIS ? "true" : "false");
    }
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {  
        try {
            int associationCount = 0;
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            String occurrence = null;
            Topic occurrenceType = null;
            Topic occurrenceScope = null;
            Locator occurrenceTypeLocator = null;
            Locator occurrenceScopeLocator = null;
            
            Object source = getContext().getContextSource();
            if(source != null && source instanceof OccurrenceTextEditor) {
                OccurrenceTextEditor editor = (OccurrenceTextEditor) source;
                occurrenceType = editor.getOccurrenceType();
                occurrenceScope = editor.getOccurrenceVersion();
                Topic occurrenceTopic = editor.getOccurrenceTopic();
                
                String newOccurrence = editor.getText();
                occurrence = occurrenceTopic.getData(occurrenceType, occurrenceScope);
                if(!newOccurrence.equals(occurrence)) {
                    int a = WandoraOptionPane.showConfirmDialog(wandora, "You have changed occurrence in editor. Would you like to save changes to topic map before association seek? If you decide not to save changes, old occurrence text is used.", "Save changes?", WandoraOptionPane.QUESTION_MESSAGE);
                    if(a == WandoraOptionPane.OK_OPTION) {
                        occurrenceTopic.setData(occurrenceType, occurrenceScope, newOccurrence);
                    }
                }
            }

            // Ensure occurrence type and scope are really ok...
            if(occurrenceType == null) {
                occurrenceType=wandora.showTopicFinder("Select occurrence type...");                
                if(occurrenceType == null) return;
            }
            occurrenceTypeLocator = occurrenceType.getSubjectIdentifiers().iterator().next();
            if(occurrenceScope == null) {
                occurrenceScope=wandora.showTopicFinder("Select occurrence scope...");                
                if(occurrenceScope == null) return;
            }
            occurrenceScopeLocator = occurrenceScope.getSubjectIdentifiers().iterator().next();

            // Initialize tool logger and variable...
            setDefaultLogger();
            setLogTitle("Find associations in occurrence");
            log("Find associations in occurrence");

            Topic occurrenceTopic = null;
            Topic oldTopic = null;
            int progress = 0;
            TopicMap map = wandora.getTopicMap();
            Association a = null;

            ArrayList<Topic> dtopics = new ArrayList<Topic>();
            while(topics.hasNext() && !forceStop()) {
                dtopics.add((Topic) topics.next());
            }
            topics = dtopics.iterator();

            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    occurrenceTopic = (Topic) topics.next();
                    if(occurrenceTopic != null && !occurrenceTopic.isRemoved()) {
                        progress++;
                        hlog("Inspecting topic's '"+getTopicName(occurrenceTopic)+"' occurrence");

                        occurrenceType = occurrenceTopic.getTopicMap().getTopic(occurrenceTypeLocator);
                        occurrenceScope = occurrenceTopic.getTopicMap().getTopic(occurrenceScopeLocator);
                        
                        if(occurrenceType != null && occurrenceScope != null) {

                            occurrence = occurrenceTopic.getData(occurrenceType, occurrenceScope);

                            // Ok, if topic has sufficient occurrence descent deeper...
                            if(occurrence != null && occurrence.length() > 0) {
                                Iterator<Topic> allTopics = map.getTopics();
                                requiresRefresh = false;
                                while(allTopics.hasNext() && !forceStop()) {
                                    oldTopic = allTopics.next();
                                    if(oldTopic != null && !oldTopic.isRemoved()) {
                                        if(isMatch(occurrence, oldTopic)) {
                                            requiresRefresh = true;
                                            break;
                                        }
                                    }
                                }
                                if(requiresRefresh) {
                                    Topic associationType = getOrCreateTopic(map, "Occurrence association", BASE_SI+"occurrence-association");
                                    Topic topicInOccurrenceRole = getOrCreateTopic(map, "Topic in occurrence", BASE_SI+"topic-in-occurrence");
                                    Topic occurrenceContainerRole = getOrCreateTopic(map, "Occurrence container", BASE_SI+"occurrence-container");
                                    allTopics = map.getTopics();
                                    while(allTopics.hasNext() && !forceStop()) {
                                        oldTopic = allTopics.next();
                                        if(oldTopic != null && !oldTopic.isRemoved()) {
                                            if(isMatch(occurrence, oldTopic)) {
                                                // Creating new association between occurrencce container and found topic
                                                log("Creating association between '"+getTopicName(occurrenceTopic)+"' and '"+getTopicName(oldTopic)+"'.");
                                                a = map.createAssociation(associationType);
                                                a.addPlayer(oldTopic, topicInOccurrenceRole);
                                                a.addPlayer(occurrenceTopic, occurrenceContainerRole);
                                                associationCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            log("Can't find occurrence type or scope topic in the occurrence topic.");
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            if(!requiresRefresh) {
                log("No associations found in occurrence(s).");
            }
            else {
                log("Total "+associationCount+" associations created.");
            }
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
    
    
    public boolean isMatch(String occurrence, Topic t) {
        boolean associate = false;
        try {
            if(occurrence != null && t != null && !t.isRemoved()) {
                if(LOOK_BASE_NAME) {
                    String topicName = t.getBaseName();
                    if(contains(occurrence, topicName)) {
                        associate = true;
                    }
                }
                if(!associate && LOOK_VARIANT_NAMES) {
                    String name = null;
                    Set<Set<Topic>> scopes = t.getVariantScopes();
                    Iterator<Set<Topic>> scopeIterator = scopes.iterator();
                    Set<Topic> scope = null;
                    while(scopeIterator.hasNext()) {
                        scope = scopeIterator.next();
                        if(scope != null) {
                            name = t.getVariant(scope);
                            if(name != null && name.length() > 0) {
                                if(contains(occurrence, name)) {
                                    associate = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if(!associate && LOOK_SIS) {
                    Collection<Locator> sis = t.getSubjectIdentifiers();
                    Iterator<Locator> sisIterator = sis.iterator();
                    Locator si = null;
                    String siStr = null;
                    while(sisIterator.hasNext()) {
                        si = sisIterator.next();
                        siStr = si.toExternalForm();
                        if(contains(occurrence, siStr)) {
                            associate = true;
                            break;
                        }
                        else {
                            
                        }
                    }
                }
            }
        }
        catch(Exception e) { log(e); }
        return associate;
    }
    
    
    
    public boolean contains(String s1, String s2) {
        if(s1 == null || s2 == null) return false;
        if(CASE_INSENSITIVE) {
            return s1.toLowerCase().contains(s2.toLowerCase());
        }
        else {
            return s1.contains(s2);
        }
    }
    
    
    public Topic getOrCreateTopic(TopicMap tm, String basename, String si) throws TopicMapException  {
        if(tm == null || si == null) return null;
        Topic t = tm.getTopic(si);
        if(t == null) {
            t = tm.createTopic();
            t.addSubjectIdentifier(new Locator(si));
            t.setBaseName(basename);
        }
        return t;
    }

}
