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
 */
package org.wandora.application.tools.subjects.expand;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.subjects.AddSubjectIdentifierPanel;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */


public class SameAsSubjectExpander extends AbstractWandoraTool implements WandoraTool {
    
    private static boolean makeAssociationsInstead = false; 
    
    private boolean shouldRefresh = false;
    
    public SameAsSubjectExpander() {}
    public SameAsSubjectExpander(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Expand subject with sameas.org";
    }

    @Override
    public String getDescription() {
        return "Add topic subjects returned by sameas.org service.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        shouldRefresh = false;
        Iterator contextObjects = getContext().getContextObjects();
        TopicMap tm = wandora.getTopicMap();
        
        if(contextObjects != null && contextObjects.hasNext()) {
            Object contextObject = contextObjects.next();
            if(contextObject != null) {
                if(contextObject instanceof Topic) {
                    Topic topic = (Topic) contextObject;
                    if(!topic.isRemoved()) {
                        String topicName = ( topic.getBaseName() == null ? topic.getOneSubjectIdentifier().toExternalForm() :  topic.getBaseName());
                        for(Locator subject : topic.getSubjectIdentifiers()) {
                            expandSubject(topic, subject, tm);
                        }
                    }
                }
                else if(contextObject instanceof Locator) {
                    Locator subjectLocator = (Locator) contextObject;
                    Topic topic = tm.getTopic(subjectLocator);
                    if(topic == null) {
                        topic = tm.getTopicBySubjectLocator(subjectLocator);
                    }
                    if(topic != null) {
                        expandSubject(topic, subjectLocator, tm);
                    }
                }
                else if(contextObject instanceof String) {
                    String subjectLocatorString = (String) contextObject;
                    Topic topic = tm.getTopic(subjectLocatorString);
                    if(topic == null) {
                        topic = tm.getTopicBySubjectLocator(subjectLocatorString);
                    }
                    if(topic == null) {
                        topic = tm.getTopicWithBaseName(subjectLocatorString);
                    }
                    if(topic != null) {
                        expandSubject(topic, new Locator(subjectLocatorString), tm);
                    }
                }
            }
        }
    }
    

    @Override
    public boolean requiresRefresh() {
        return shouldRefresh;
    }
    
    
    public void expandSubject(Topic topic, Locator subject, TopicMap tm) {
        if(topic != null && subject != null) {
            Collection<URL> additionalSubjects = getExpandingSubjects(subject.toString());
            for(URL aSubject : additionalSubjects) {
                if(aSubject != null) {
                    try {
                        Locator l = tm.createLocator(aSubject.toExternalForm());
                        if(makeAssociationsInstead) {
                            Topic subjectTopic = tm.getTopic(l);
                            if(subjectTopic == null) {
                                subjectTopic = tm.createTopic();
                                subjectTopic.addSubjectIdentifier(l);
                            }
                            if(subjectTopic != null && !subjectTopic.mergesWithTopic(topic)) {
                                Topic sameAsType = getOrCreateTopic("http://sameas.org/", "sameas.org", tm);
                                Topic sourceRole = getOrCreateTopic("http://wandora.org/si/core/rdf-source", "rdf-source", tm);
                                Topic targetRole = getOrCreateTopic("http://wandora.org/si/core/rdf-target", "rdf-target", tm);
                                if(sameAsType != null && sourceRole != null && targetRole != null) {
                                    Association sameAsAssociation = topic.getTopicMap().createAssociation(sameAsType);
                                    sameAsAssociation.addPlayer(topic, sourceRole);
                                    sameAsAssociation.addPlayer(subjectTopic, targetRole);
                                    shouldRefresh = true;
                                }
                            }
                        }
                        else {
                            if(TMBox.checkSubjectIdentifierChange(Wandora.getWandora(),topic,l,true) == ConfirmResult.yes) {
                                shouldRefresh = true;
                                topic.addSubjectIdentifier(l);
                            }
                        }
                    }
                    catch(Exception mue) {
                        log("Exception occurred while expanding subject '"+subject+"' with '"+aSubject.toExternalForm()+"'!");
                    }
                }
            }
        }
    }
    
    
    
    private Topic getOrCreateTopic(String si, String bn, TopicMap tm) throws TopicMapException {
        Topic t = tm.getTopic(si);
        if(t == null) t = tm.getTopicWithBaseName(bn);
        if(t == null) {
            t = tm.createTopic();
            t.addSubjectIdentifier(new Locator(si));
            t.setBaseName(bn);
        }
        return t;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected String getExpandingRequestBase() {
        return "http://sameas.org/json";
    }
    
    
    

    protected URL getExpandingRequestURL(String subject) throws MalformedURLException {
        String base = getExpandingRequestBase();
        URL url = null;
        if(base != null) {
            String expanderRequestString = base + "?uri=" + subject;
            try {
                expanderRequestString = base + "?uri=" + URLEncoder.encode(subject, "UTF-8");
            }
            catch(Exception e) {}
            
            try {
                url = new URL(expanderRequestString);
            }
            catch(Exception e) {
                log("Exception '"+e.getMessage()+"' occurred while building sameas request URL with base '"+base+"'.");
            }
        }
        return url;
    }


    
    
    protected Collection<URL> getExpandingSubjects(String subject) {
        HashSet<URL> additionalSubjects = new HashSet();
        
        try {
            URL requestURL = getExpandingRequestURL(subject);
            if(requestURL != null) {
                String responseString = IObox.doUrl(requestURL);
                JSONArray response = new JSONArray(responseString);
                for(int i=0; i<response.length(); i++) {
                    JSONObject json = response.getJSONObject(i);
                    if(json.has("uri")) {
                        String uri = json.getString("uri");
                        if(!subject.equals(uri)) {
                            additionalSubjects.add(new URL(uri));
                        }
                    }
                    if(json.has("duplicates")) {
                        JSONArray duplicates = json.getJSONArray("duplicates");
                        for(int j=0; j<duplicates.length(); j++) {
                            String duplicate = duplicates.getString(j);
                            if(duplicate != null) {
                                additionalSubjects.add(new URL(duplicate));
                            }
                        }
                    }
                    if(!additionalSubjects.isEmpty()) {

                    }
                    else {
                        log("Sameas.org didn't found additional subjects for '"+subject+"'.");
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return additionalSubjects;
    }
    
    
    // ---------------------------------------------------------- CONFIGURE ----
    
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        GenericOptionsDialog god = new GenericOptionsDialog(
            wandora, 
            "SameAs.org subject expander options", 
            "SameAs.org subject expander options", 
            true, 
            new String[][] {
                new String[] { "Make associations instead", "boolean", makeAssociationsInstead ? "true" : "false" },
            },
            wandora
        );
        god.setVisible(true);
        if(god.wasCancelled()) return;

        Map<String,String> values = god.getValues();
        
        makeAssociationsInstead = Boolean.parseBoolean(values.get("Make associations instead"));
    }
    
    @Override
    public void writeOptions(Wandora w, org.wandora.utils.Options options, String prefix){
    }
    
    
    
}
