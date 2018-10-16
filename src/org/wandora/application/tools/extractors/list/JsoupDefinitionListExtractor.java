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

package org.wandora.application.tools.extractors.list;

import org.wandora.application.tools.extractors.AbstractJsoupExtractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


public class JsoupDefinitionListExtractor extends AbstractJsoupExtractor 
implements WandoraTool, BrowserPluginExtractor {

	
	private static final long serialVersionUID = 1L;

	public static final String SI_PREFIX = "http://wandora.org/si/";
    
    public static final String LIST_SI = SI_PREFIX + "list/";
    public static final String DOCUMENT_SI = SI_PREFIX + "document/";
    public static final String NAME_SI = SI_PREFIX + "name/";
    public static final String DEF_SI  = SI_PREFIX + "definition/";
    
    public static final String CHILD_SI  = SI_PREFIX + "child/";
    public static final String PARENT_SI  = SI_PREFIX + "parent/";
    public static final String RELATION_SI  = SI_PREFIX + "relation/";
    
    private TopicMap tm;
    private Topic wandoraClass;
    private Topic documentTopic;
    private Topic langTopic;
    
    private Topic documentType;
    private Topic listType;
    private Topic definitionType;
    
    private Topic childType;
    private Topic parentType;
    private Topic relationType;
    
    
    //Catch all
    @Override
    public boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception{
        
        Elements lists = d.select("dl");
        
        if(lists.isEmpty())
            throw new Exception("No definition lists found!");
        
        //Basic init
        this.tm = t;
        this.wandoraClass = getWandoraClassTopic(tm);
        this.langTopic = getLangTopic(tm);
        
        this.childType = getOrCreateTopic(tm, CHILD_SI, "child");
        this.parentType = getOrCreateTopic(tm, PARENT_SI, "parent");
        this.relationType = getOrCreateTopic(tm, RELATION_SI, "relationship");

        this.documentType = getOrCreateTopic(tm, DOCUMENT_SI, "document");
        makeSubclassOf(tm, documentType, wandoraClass);
        
        this.listType = getOrCreateTopic(tm, LIST_SI, "list");
        makeSubclassOf(tm, listType, wandoraClass);
        
        this.definitionType = getOrCreateTopic(tm, DEF_SI, "definition");
        makeSubclassOf(tm, definitionType, wandoraClass);
        
        this.documentTopic = getOrCreateTopic(tm, u , d.title());
        documentTopic.addType(documentType);
        
        for(Element list: lists) parseList(list, documentTopic);
        
        return true;
    }

    
    
    private void parseList(Element list, Topic documentTopic) throws TopicMapException {
        
        Topic listTopic = getOrCreateTopic(tm, null);
        listTopic.addType(getOrCreateTopic(tm, LIST_SI));
        
        declareChild(documentTopic, listTopic);
        
        Elements names = list.select("dt");
        
        for(Element name: names) parseName(name,listTopic);
        
    }
    
    

    private void parseName(Element name, Topic listTopic) throws TopicMapException {
        
        Topic nameTopic = getOrCreateTopic(tm, null, name.text());
        nameTopic.addType(definitionType);
        
        declareChild(listTopic, nameTopic);
        
        Element defCandidate = name.nextElementSibling();
        
        while(defCandidate != null && defCandidate.tagName().equals("dd")){
            nameTopic.setData(definitionType, langTopic, defCandidate.text());
            
            defCandidate = defCandidate.nextElementSibling();
        }
        
    }
    
    
    //--------------------------------------------------------------------------
    
    
    void declareChild(Topic parent, Topic child) throws TopicMapException{
        Association a = tm.createAssociation(relationType);
        a.addPlayer(parent, parentType);
        a.addPlayer(child, childType);
    }
    
}
