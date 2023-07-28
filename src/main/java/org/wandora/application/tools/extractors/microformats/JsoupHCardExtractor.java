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
 */

package org.wandora.application.tools.extractors.microformats;

import java.util.HashMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


public class JsoupHCardExtractor 
    extends AbstractJsoupMicroformatExtractor {


	private static final long serialVersionUID = 1L;

	private static final String SI_PREFIX = "http://wandora.org/si/hcard/";
    
    private static final String[] CARD_PROPS = {
        "fn",
        "agent",
        "bday",
        "category",
        "class",
        "email",
        "key",
        "label",
        "logo",
        "mailer",
        "nickname",
        "note",
        "org",
        "photo",
        "rev",
        "role",
        "sort-string",
        "sound",
        "url",
        "tel",
        "title",
        "tz",
        "url",
        "uid"
    };
    
    private HashMap<String,Topic> typeTopics;
    private TopicMap tm;
    
    @Override
    public String getName() {
        return "HCard microformat extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Converts HCard microformat HTML snippets to Topic Maps.";
    }
    
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t){
        
        tm = t;
        try {
            
            //typeTopics = generateTypes();
            typeTopics = new HashMap<String, Topic>();
            Topic document = getOrCreateTopic(tm, d.baseUri(), d.title());
            Topic docType = getType("document");
            document.addType(docType);
            Elements cards = d.select(".vcard");
            for(Element card : cards){
                try {
                    parseCard(document,card);
                } catch (TopicMapException tme) {
                    log(tme);
                }
            }
            
        } catch (TopicMapException tme) {
            log(tme);
            return false;
        }
        
        return true;
    }

    private void parseCard(Topic document, Element element) throws TopicMapException {
        
        Topic topic = getOrCreateTopic(tm, null);
        Topic cardType = getType("vcard");
        Topic docType = getType("document");
        
        
        topic.addType(cardType);
        Association a = tm.createAssociation(cardType);
        a.addPlayer(topic,cardType);
        a.addPlayer(document,docType);
        
        for (int i = 0; i < CARD_PROPS.length; i++) {
            String propName = CARD_PROPS[i];
            
            Elements props = element.select("." + propName);
            for(Element prop: props){
                try {
                    addProp(topic,propName,prop);
                } catch (TopicMapException tme) {
                    log(tme);
                }
            }
        }
        
        Element name = element.select(".n").first();
        if(name != null) try {
            parseName(topic, name);
        } catch (TopicMapException tme) {
            log(tme);
        }
        
        Element adr = element.select(".adr").first();
        if(adr != null) try {
            parseAdr(topic, adr, "vcard");
        } catch (TopicMapException tme) {
            log(tme);
        }
        
        Element geo = element.select(".geo").first();
        if(geo != null) try {
            parseGeo(topic, geo, "vcard");
        } catch (TopicMapException tme) {
            log(tme);
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
