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

package org.wandora.application.tools.extractors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author Eero
 */


public class JsoupHTMLLinkStructureExtractor extends AbstractJsoupExtractor implements WandoraTool, BrowserPluginExtractor {

    private TopicMap tm;
    private Topic wandoraClass;
    
    private static final String LINK_TYPE = "http://wandora.org/si/link";
    private static final String DOC_TYPE = "http://wandora.org/si/document";

    
    
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception {
        
        this.tm = t;
        this.wandoraClass = getWandoraClassTopic(tm);
        
        Topic documentType = getOrCreateTopic(tm, DOC_TYPE, "Document");
        makeSubclassOf(tm, documentType, wandoraClass);
        
        Topic docTopic = getOrCreateTopic(tm, u);
        docTopic.addType(documentType);
        
        Elements links = d.select("a");
        
        for(Element link: links){
            try {
                parseLink(link,docTopic);
            } 
            catch (TopicMapException tme) {
                log(tme.getMessage()); 
            }
        }
        
        return true;

    }    

    private void parseLink(Element link, Topic docTopic) throws TopicMapException {
        
        //Get the absolute HREF (if document base URI is specified)
        String href = link.attr("abs:href").trim();
        
        //Basic validation
        if(href.length() == 0 
        || href.indexOf("javascript:") != -1
        || href.indexOf("mailto:") != -1)
            return;
        
        Topic linkTopic = getOrCreateTopic(tm, href);
        linkTopic.setSubjectLocator(new Locator(href));
        
        Topic linkType = getOrCreateTopic(tm, LINK_TYPE, "HTML link");
        Topic docType = getOrCreateTopic(tm, DOC_TYPE, "Document");
        
        if(linkType != null) {
            linkTopic.addType(linkType);
            makeSubclassOfWandoraClass(linkType, tm);
        }
        if(linkType != null && docType != null) {
            Association a = tm.createAssociation(linkType);
            a.addPlayer(linkTopic, linkType);
            a.addPlayer(docTopic, docType);
        }
    }

}
