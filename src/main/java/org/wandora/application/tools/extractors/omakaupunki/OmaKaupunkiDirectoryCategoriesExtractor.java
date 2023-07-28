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
 */


package org.wandora.application.tools.extractors.omakaupunki;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author akivela
 */


public class OmaKaupunkiDirectoryCategoriesExtractor extends OmaKaupunkiAbstractExtractor {
    

	private static final long serialVersionUID = 1L;




	@Override
    public String getName() {
        return "OmaKaupunki directory categories extractor";
    }
    
    

    public boolean parseOmaKaupunki(JSONObject json, TopicMap topicMap) {
        try {
            JSONArray dataArray = json.getJSONArray("data");
            for(int i=0; i<dataArray.length(); i++) {
                JSONObject singleCategory = dataArray.getJSONObject(i);
                parseSingleCategory(singleCategory, topicMap);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        
        return true;
    }
        
        
    

    public void parseSingleCategory(JSONObject json, TopicMap topicMap) {
        try {
            if(json != null) {
                String name = robustGetString(json, "name");
                String plural = robustGetString(json, "plural");
                String url = robustGetString(json, "url");
                long id = robustGetLong(json, "id");
                long parent = robustGetLong(json, "parent");
                
                String basename = name;
                
                Topic categoryTopic = getServiceCategoryTopic(id, topicMap);
                if(url != null) categoryTopic.addSubjectIdentifier(new Locator(url));
                categoryTopic.setBaseName(basename);
                categoryTopic.setDisplayName(LANG, name);
                
                Topic defaultLangTopic = getDefaultLangTopic(topicMap);
                
                if(id != 0) {
                    Topic categoryIdentifierType = this.getServiceCategoryIdentifierType(topicMap);
                    if(categoryIdentifierType != null && defaultLangTopic != null) {
                        categoryTopic.setData(categoryIdentifierType, defaultLangTopic, ""+id);
                    }
                }
                if(parent != 0) {
                    categoryTopic.addType(getServiceCategoryType(topicMap));
                    Topic parentTopic = getServiceCategoryTopic(parent, topicMap);
                    makeSubclassOf(topicMap, categoryTopic, parentTopic);
                }
                else {
                    categoryTopic.addType(getServiceCategoryType(topicMap));
                }
                if(plural != null) {
                    Topic categoryPluralType = this.getServiceCategoryPluralType(topicMap);
                    if(categoryPluralType != null && defaultLangTopic != null) {
                        categoryTopic.setData(categoryPluralType, defaultLangTopic, plural);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
