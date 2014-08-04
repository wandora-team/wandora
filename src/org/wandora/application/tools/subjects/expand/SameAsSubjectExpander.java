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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.subjects.AddSubjectIdentifierPanel;
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
                            expandSubject(topic, subject);
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
                        expandSubject(topic, subjectLocator);
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
                        expandSubject(topic, new Locator(subjectLocatorString));
                    }
                }
            }
        }
    }
    

    @Override
    public boolean requiresRefresh() {
        return shouldRefresh;
    }
    
    
    public void expandSubject(Topic topic, Locator subject) {
        if(topic != null && subject != null) {
            Collection<URL> additionalSubjects = getExpandingSubjects(subject.toString());
            for(URL aSubject : additionalSubjects) {
                if(aSubject != null) {
                    try {
                        Locator l = topic.getTopicMap().createLocator(aSubject.toExternalForm());
                        if(TMBox.checkSubjectIdentifierChange(Wandora.getWandora(),topic,l,true) == ConfirmResult.yes) {
                            shouldRefresh = true;
                            topic.addSubjectIdentifier(l);
                        }
                    }
                    catch(Exception mue) {
                        log("Exception occurred while expanding subject '"+subject+"' with '"+aSubject.toExternalForm()+"'!");
                    }
                }
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected String getExpandingRequestBase() {
        return "http://sameas.org/json";
    }
    
    
    

    protected URL getExpandingRequestURL(String subject) throws MalformedURLException {
        String expanderRequestString = getExpandingRequestBase() + "?uri=" + subject;
        try {
            expanderRequestString = getExpandingRequestBase() + "?uri=" + URLEncoder.encode(subject, "UTF-8");
        }
        catch(Exception e) {
            // Nothing. Continue. We already have the expanderRequestString.
        }
        return new URL(expanderRequestString);
    }


    
    
    protected Collection<URL> getExpandingSubjects(String subject) {
        HashSet<URL> additionalSubjects = new HashSet();
        
        try {
            String responseString = IObox.doUrl(getExpandingRequestURL(subject));
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
        catch(Exception e) {
            log(e);
        }
        return additionalSubjects;
    }
    
}
