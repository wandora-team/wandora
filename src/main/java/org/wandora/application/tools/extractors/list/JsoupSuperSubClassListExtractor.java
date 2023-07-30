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

package org.wandora.application.tools.extractors.list;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.application.tools.extractors.AbstractJsoupExtractor;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author Eero
 */


public class JsoupSuperSubClassListExtractor extends AbstractJsoupExtractor 
implements WandoraTool, BrowserPluginExtractor {


	private static final long serialVersionUID = 1L;
	
	
	private TopicMap tm;
    private Topic wandoraClass;
    
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception {
        
        this.tm = t;
        this.wandoraClass = getWandoraClassTopic(tm);
        
        Elements children = d.body().children();
        
        for(Element listCandidate: children){
            
            System.out.println(listCandidate.outerHtml());
            
            if(listCandidate.tagName().equals("ul"))
                parseList(listCandidate, null);
        }
        
        return true;

    }    
    

    private void parseList(Element list, Topic classTopic) throws TopicMapException {
        
        Elements listElements = list.children();
        
        for(Element outerElement: listElements){
            if(outerElement.children().isEmpty()){
                parseTopic(outerElement, classTopic);
            }
        }
        
    }
    

    private void parseTopic(Element classElement, Topic classTopic) throws TopicMapException {
        String name = classElement.text().trim();
        
        if(name.length() == 0) return;
        
        Topic t = getOrCreateTopic(tm, null , name);
        
        if(classTopic == null) classTopic = wandoraClass;
        makeSubclassOf(tm, t, classTopic);
        
        // See if the next element is a list (of instances)
        Element listWrapper = classElement.nextElementSibling();
        if(listWrapper != null && !listWrapper.children().isEmpty()) {
            for(Element listCandidate: listWrapper.children()){
                if(listCandidate.tagName().equals("ul"))
                    parseList(listCandidate, t);
            }
        }
    }
}
