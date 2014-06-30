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
 * 
 * RSSMapping.java
 *
 * Created on 13.2.2009,15:25
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class RSSMapping extends RDF2TopicMapsMapping {
    public static final String RSS_NS = "http://purl.org/rss/1.0/";
    public static final String RSS_SYNDICATION_NS = "http://purl.org/rss/1.0/modules/syndication/";
    public static final String RSS_COMPANY_NS = "http://purl.org/rss/1.0/modules/company/";
    public static final String RSS_TEXTINPUT_NS = "http://purl.org/rss/1.0/modules/textinput/";
    public static final String RSS_CONTENT_NS = "http://purl.org/rss/1.0/modules/content/";
    
    public static final String RSS_ROLE_NS = "http://wandora.org/si/rss/role/";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        RSS_NS+"channel",
            "rss:channel",
        RSS_NS+"title",
            "rss:title",
        RSS_NS+"link",
            "rss:link",
        RSS_NS+"description",
            "rss:description",
        RSS_NS+"image",
            "rss:image",
        RSS_NS+"items",
            "rss:items",
        RSS_NS+"textinput",
            "rss:textinput",
        RSS_NS+"url",
            "rss:url",
        RSS_NS+"item",
            "rss:item",
        RSS_NS+"name",
            "rss:name",
            
        RSS_SYNDICATION_NS+"updatePeriod",
            "rss_syndication:updatePeriod",
        RSS_SYNDICATION_NS+"updateFrequency",
            "rss_syndication:updateFrequency",
        RSS_SYNDICATION_NS+"updateBase",
            "rss_syndication:updateBase",
            
        RSS_COMPANY_NS+"name",
            "rss_company:name",
        RSS_COMPANY_NS+"market",
            "rss_company:market",
        RSS_COMPANY_NS+"symbol",
            "rss_company:symbol",
            
        RSS_TEXTINPUT_NS+"function",
            "rss_textinput:function",
        RSS_TEXTINPUT_NS+"inputType",
            "rss_textinput:inputType",
            
        RSS_CONTENT_NS+"encoded",
            "rss_content:encoded",
        RSS_CONTENT_NS+"items",
            "rss_content:items",
        RSS_CONTENT_NS+"item",
            "rss_content:item",
        RSS_CONTENT_NS+"format",
            "rss_content:format",
    };
    
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        RSS_NS+"title",
            RSS_ROLE_NS+"titled", "titled (rss)",
            RSS_ROLE_NS+"title", "title (rss)",
            
        RSS_NS+"link",
            RSS_ROLE_NS+"linked", "linked (rss)",
            RSS_ROLE_NS+"link", "link (rss)",
            
        RSS_NS+"image",
            RSS_ROLE_NS+"imaged", "imaged (rss)",
            RSS_ROLE_NS+"image", "image (rss)",
            
        RSS_NS+"items",
            RSS_ROLE_NS+"channel", "channel (rss)",
            RSS_ROLE_NS+"item-list", "item-list (rss)",
            
        RSS_NS+"textinput",
            RSS_ROLE_NS+"channel", "channel (rss)",
            RSS_ROLE_NS+"textinput", "textinput (rss)",
                      
        RSS_NS+"item",
            RSS_ROLE_NS+"item-container", "item-container (rss)",
            RSS_ROLE_NS+"item", "item (rss)",

    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }
}
