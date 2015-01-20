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
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


public class JsoupGeoExtractor extends AbstractJsoupMicroformatExtractor{

   
    private static final String SI_PREFIX = "http://wandora.org/si/adr/";
    
    private HashMap<String,Topic> typeTopics;
    private TopicMap tm;
    
    @Override
    public String getName() {
        return "Geo microformat extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Converts Geo microformat HTML snippets to Topic Maps.";
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

    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception {
        
        tm = t;
        try {
            
            //typeTopics = generateTypes();
            typeTopics = new HashMap<String, Topic>();
            Topic document = getOrCreateTopic(tm, d.baseUri(), d.title());
            Topic docType = getType("document");
            document.addType(docType);
            Elements geos = d.select(".geo");
            for(Element geo : geos){
                try {
                    parseGeo(document,geo, "document");
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
    
}
