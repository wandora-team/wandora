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
 */

package org.wandora.application.tools.extractors.microformats;

import java.util.HashMap;
import javax.swing.Icon;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractJsoupExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


abstract class AbstractJsoupMicroformatExtractor extends AbstractJsoupExtractor{
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_microformat.png");
    } 
    
    private static final String MICROFORMAT_SI = "http://microformats.org/";
    
    protected static final String[][] TYPE_STRINGS = {
        {"microformat", "microformat"},
        {"document",    "document"},
        {"vcard",       "hCard"},
        {"vcalendar",   "hCalendar"},
        {"vevent",      "hCalendar event"},
        
        {"fn",          "full name"},
        {"agent",       "agent"},
        {"bday",        "birthday"},
        {"category",    "category"},
        {"class",       "class"},
        {"email",       "email address"},
        
        {"key",         "key"},
        {"label",       "label"},
        {"logo",        "logo"},
        {"mailer",      "mailer"},
        {"nickname",    "nickname"},
        {"note",        "note"},
        {"org",         "organization"},
        {"photo",       "photo"},
        {"rev",         "revision"},
        {"role",        "role"},
        {"sort-string", "sort string"},
        {"sound",       "sound"},
        {"url",         "home page"},
        {"tel",         "telephone number"},
        {"title",       "title"},
        {"tz",          "timezone"},
        {"url",         "url"},
        {"uid",         "uid"},
        
        {"n",               "structured name"},
        {"honorific-prefix","honorific prefix"},
        {"given-name",      "given name"},
        {"additional-name", "additional name"},
        {"family-name",     "family name"},
        {"honorific-suffix","honorific suffix"},
        
        {"adr",             "structured address"},
        {"street-address",  "street address"},
        {"locality",        "locality"},
        {"region",          "region"},
        {"postal-code",     "postal code"},
        {"country-name",    "country name"},
        
        {"dstart",      "start time"},
        {"dtend",       "end time"},
        {"duration",    "duration"},
        {"rdate",       "date"},
        {"rrule",       "rule"},
        
        {"location",    "location"},
        {"category",    "category"},
        {"description", "description"},
        
        {"geo",         "geo location"},
        {"latitude",    "latitude"},
        {"longitude",   "longitude"}
    };
    
    protected static final String[] ADR_PROPS = {
        "street-address",
        "locality",
        "region",
        "postal-code",
        "country-name"
    };
    
    protected static final String[] NAME_PROPS = {
        "honorific-prefix",
        "given-name",
        "additional-name",
        "family-name",
        "honorific-suffix"
    };
    
    protected static final String[] GEO_PROPS = {
        "latitude",
        "longitude"
    };
    
    private Topic getMicroformatTopic(TopicMap tm) throws TopicMapException{

        //Is the microformat topic already present?
        Topic microformatTopic = tm.getTopic(MICROFORMAT_SI);
        
        if(microformatTopic != null) return microformatTopic;
        
        //Nope. Create it.
        microformatTopic = getOrCreateTopic(tm, MICROFORMAT_SI, "microformat");
        makeSubclassOf(tm, microformatTopic, getWandoraClassTopic(tm));
        return microformatTopic;
        
    }
    
    //Avoid polluting the TopicMap with unused type topics.
    protected Topic getType(String typeKey) throws TopicMapException{
        
        TopicMap tm = getTopicMap();
        HashMap<String, Topic> typeTopics = getTypeTopics();
        
        if(typeTopics.containsKey(typeKey)) return typeTopics.get(typeKey);
        
        Topic wandoraClass = getMicroformatTopic(tm);
        String typeName = null;
        for (int i = 0; i < TYPE_STRINGS.length; i++) {
            if(TYPE_STRINGS[i][0].equals(typeKey))
                typeName = TYPE_STRINGS[i][1];
        }
        if(typeName == null) 
            throw new TopicMapException("Failed to get type topic");

        Topic type = getOrCreateTopic(tm, getSIPrefix() + typeKey, typeName);
        makeSubclassOf(tm, type, wandoraClass);
        typeTopics.put(typeKey, type);
        return type;
    }
    
    protected void addProp(Topic topic, String propName, Element prop) throws TopicMapException {
        
        Topic topicType = getType("vcard");
        
        addProp(topic, topicType,propName,prop);
        
    }
    
    protected void addProp(Topic topic,Topic topicType, String propName, Element prop) throws TopicMapException {
        
        Topic propType = getType(propName);
        
        String propValue;
        if(propName.equals("url")){
            propValue = prop.attr("href");
            
            topic.setSubjectLocator(new Locator(propValue));
            
        } else if(propName.equals("email")){
            propValue = prop.attr("href");
            if(propValue.length() == 0)
                propValue = prop.text();
            if(propValue.startsWith("mailto:"))
                propValue = propValue.substring(6,propValue.length());
        } else if(propName.equals("title")){
            propValue = prop.text();
            topic.setBaseName(propValue);
        } else if(propName.equals("photo")){
            propValue = prop.attr("src");
        } else {
            propValue = prop.text();
        }
        if(propValue.length() == 0){
            log("Failed to add property: " + propName);
            return;
        }
        String si = getSIPrefix() + propName + "/" + propValue;
        Topic propTopic = getOrCreateTopic(getTopicMap(), si, propValue);
        propTopic.addType(propType);
        Association a = getTopicMap().createAssociation(propType);
        a.addPlayer(topic, topicType);
        a.addPlayer(propTopic, propType);
        
    }
    
    protected void parseName(Topic card, Element element) throws TopicMapException {
        
        TopicMap tm = getTopicMap();
        Topic topic = getOrCreateTopic(tm, null);
        Topic nameType = getType("n");
        Topic cardType = getType("vcard");
        
        topic.addType(nameType);
        
        Association a = tm.createAssociation(nameType);
        a.addPlayer(topic,nameType);
        a.addPlayer(card,cardType);
        
        for (int i = 0; i < NAME_PROPS.length; i++) {
            String propName = NAME_PROPS[i];
            
            Elements props = element.select("." + propName);
            for(Element prop: props){
                try {
                    addProp(topic,nameType,propName,prop);
                } catch (TopicMapException tme) {
                    log(tme);
                }
            }
        }
        
    }

    protected void parseAdr(Topic parent, Element element, String parentTypeName) throws TopicMapException {
        TopicMap tm = getTopicMap();
        Topic topic = getOrCreateTopic(tm, null);
        Topic adrType = getType("adr");
        Topic parentType = getType(parentTypeName);
        
        topic.addType(adrType);
        
        Association a = tm.createAssociation(adrType);
        a.addPlayer(topic,adrType);
        a.addPlayer(parent,parentType);
        
        for (int i = 0; i < ADR_PROPS.length; i++) {
            String propName = ADR_PROPS[i];
            
            Elements props = element.select("." + propName);
            for(Element prop: props){
                try {
                    addProp(topic,adrType,propName,prop);
                } catch (TopicMapException tme) {
                    log(tme);
                }
            }
        }
    
    }
    
    protected void parseGeo(Topic card, Element element, String parentTypeName) throws TopicMapException {
        TopicMap tm = getTopicMap();
        
        Topic topic = getOrCreateTopic(tm, null);
        Topic geoType = getType("geo");
        Topic parentType = getType(parentTypeName);
        
        topic.addType(geoType);
        
        Association a = tm.createAssociation(geoType);
        a.addPlayer(topic,geoType);
        a.addPlayer(card,parentType);
        
        for (int i = 0; i < GEO_PROPS.length; i++) {
            String propName = GEO_PROPS[i];
            
            Elements props = element.select("." + propName);
            for(Element prop: props){
                try {
                    addProp(topic,geoType,propName,prop);
                } catch (TopicMapException tme) {
                    log(tme);
                }
            }
        }
    }
    
    abstract protected String[][] getTypeStrings();

    abstract protected HashMap<String, Topic> getTypeTopics();

    abstract protected TopicMap getTopicMap();

    abstract protected String getSIPrefix();
}
