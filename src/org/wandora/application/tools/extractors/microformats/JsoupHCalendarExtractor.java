/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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

package org.wandora.application.tools.extractors.microformats;

import java.util.HashMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import static org.wandora.application.tools.extractors.microformats.AbstractJsoupMicroformatExtractor.TYPE_STRINGS;
import org.wandora.topicmap.Association;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


public class JsoupHCalendarExtractor
    extends AbstractJsoupMicroformatExtractor {
    
    @Override
    public String getName() {
        return "HCalendar microformat extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Converts HCalendar Microformat HTML snippets to Topic Maps.";
    }

    private static final String SI_PREFIX = "http://wandora.org/si/hcalendar/";
    
    private static final String[] EVENT_PROPS = {
        "dstart",
        "dtend",
        "duration",
        "rdate",
        "rrule",
        
        "location",
        "category",
        "description",
        "url",
        "uid"
    };
    
    private HashMap<String,Topic> typeTopics;
    private TopicMap tm;
    
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t){
        
        tm = t;
        typeTopics = new HashMap<String, Topic>();
        
        Elements calendars = d.select(".vcalendar");
        
        if(calendars.isEmpty()) {
            try {
                parseCalendar(d);
            } catch (TopicMapException tme) {
                log(tme.getMessage());
            }
        } else {
            for(Element calendar: calendars) {
                try {
                    parseCalendar(calendar);

                } catch (TopicMapException tme) {
                    log(tme.getMessage());
                }
            }
        }
        
        return true;
    }

    private void parseCalendar(Document document) throws TopicMapException {
        String title = document.title();
        Topic type = getType("vcalendar");
        Topic topic = getOrCreateTopic(tm,null, title);
        topic.addType(type);
        
        parseCalendar(topic, document.body());
    }

    private void parseCalendar(Element element) throws TopicMapException {
        Topic type = getType("vcalendar");
        Topic topic = getOrCreateTopic(tm,null);
        topic.addType(type);
        
        parseCalendar(topic, element);
    }
    
    private void parseCalendar(Topic topic, Element element) throws TopicMapException{
        Elements events = element.select(".vevent");
        for(Element event: events){
            try {
                parseEvent(topic, event);
            } catch (TopicMapException tme) {
                log(tme);
            }
        }
    }

    private void parseEvent(Topic calendar, Element element) throws TopicMapException{
        
        Topic eventType = getType("vevent");
        Topic calendarType = getType("vcalendar");
        
        Topic topic = getOrCreateTopic(tm, null);
        topic.addType(eventType);
        
        Association a = tm.createAssociation(eventType);
        a.addPlayer(topic, eventType);
        a.addPlayer(calendar, calendarType);
        
        for (int i = 0; i < EVENT_PROPS.length; i++) {
            String propName = EVENT_PROPS[i];
            
            Elements props = element.select("." + propName);
            for(Element prop: props) {
                try {
                    addProp(topic, propName, prop);
                } catch (TopicMapException tme) {
                    log(tme);
                }
            }
        }
        
    }
    
    @Override
    protected String[][] getTypeStrings() {
        return TYPE_STRINGS;
    }

    @Override
    protected HashMap<String, Topic> getTypeTopics() {
        return typeTopics;
    }

    @Override
    protected TopicMap getTopicMap() {
        return tm;
    }

    @Override
    protected String getSIPrefix() {
        return SI_PREFIX;
    }
}
