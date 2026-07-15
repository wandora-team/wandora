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
public class TwineMapping extends RDF2TopicMapsMapping {
    public static final String TWINE_BASIC_NS = "http://www.radarnetworks.com/2007/09/12/basic#";
    public static final String TWINE_RADAR_NS = "http://www.radarnetworks.com/core#";
    public static final String TWINE_APP_NS = "http://www.radarnetworks.com/shazam#";
    public static final String TWINE_WEB_NS = "http://www.radarnetworks.com/web#";
    public static final String TWINE_DI_NS = "http://www.radarnetworks.com/data-intelligence#";
    public static final String TWINE_ENRICHMENT_NS = "http://www.radarnetworks.com/enrichment#";
    
    public static final String TWINE_ROLE_NS = "http://wandora.org/si/twine/role/";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        TWINE_BASIC_NS+"url",
            "twine:url",
        TWINE_BASIC_NS+"description",
            "twine:description",
        TWINE_BASIC_NS+"tag",
            "twine:tag",
        TWINE_BASIC_NS+"Bookmark",
            "twine:Bookmark",
        TWINE_BASIC_NS+"isTopicOf",
            "twine:isTopicOf",
        TWINE_BASIC_NS+"isAbout",
            "twine:isAbout",
        TWINE_BASIC_NS+"author",
            "twine:author",
        TWINE_BASIC_NS+"manufacturer",
            "twine:manufacturer",
            
        TWINE_RADAR_NS+"createdDate",
            "twine:createdDate",
        TWINE_RADAR_NS+"lastModifiedDate",
            "twine:lastModifiedDate",
        TWINE_RADAR_NS+"wasCreatedBy",
            "twine:wasCreatedBy",
        TWINE_RADAR_NS+"isContainedIn",
            "twine:isContainedIn",
        TWINE_RADAR_NS+"key"+
            "twine:key",
        TWINE_RADAR_NS+"contains"+
            "twine:contains",
            
        TWINE_APP_NS+"commentCount",
            "twine:commentCount",
        TWINE_APP_NS+"wasCreatedInSpot",
            "twine:wasCreatedInSpot",
        TWINE_APP_NS+"Post",
            "twine:Post",
        TWINE_APP_NS+"Spot",
            "twine:Spot",
        TWINE_APP_NS+"spotType",
            "twine:spotType",
        TWINE_APP_NS+"openMembership",
            "twine:openMembership",
        TWINE_APP_NS+"membersInvitingMembers",
            "twine:membersInvitingMembers",
        TWINE_APP_NS+"indirectlyContains",
            "twine:indirectlyContains",
        TWINE_APP_NS+"itemCount",
            "twine:itemCount",
        TWINE_APP_NS+"allowsComments",
            "twine:allowsComments",
            
        TWINE_WEB_NS+"views",
            "twine:views",

        TWINE_ENRICHMENT_NS+"language",
            "twine:language",
        TWINE_ENRICHMENT_NS+"enrichmentVersion",
            "twine:enrichmentVersion",
        TWINE_ENRICHMENT_NS+"convertedBinaryContent",
            "twine:convertedBinaryContent",
            
        TWINE_DI_NS+"annotationPositionalData",
            "twine:annotationPositionalData",
        TWINE_DI_NS+"isAboutTopic",
            "twine:isAboutTopic",
        TWINE_DI_NS+"hasAnnotation",
            "twine:hasAnnotation",
    };
    
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        TWINE_BASIC_NS+"tag",
            TWINE_ROLE_NS+"object", "object (twine)",
            TWINE_ROLE_NS+"tag", "tag (twine)",
            
           
        TWINE_BASIC_NS+"isTopicOf",
            TWINE_ROLE_NS+"topic", "topic (twine)",
            TWINE_ROLE_NS+"object", "object (twine)",
            
        TWINE_BASIC_NS+"isAbout",
            TWINE_ROLE_NS+"object", "object (twine)",
            TWINE_ROLE_NS+"target", "target (twine)",
            
        TWINE_BASIC_NS+"author",
            TWINE_ROLE_NS+"author", "author (twine)",
            TWINE_ROLE_NS+"object", "object (twine)",
            
        TWINE_BASIC_NS+"manufacturer",
            TWINE_ROLE_NS+"manufacturer", "manufacturer (twine)",
            TWINE_ROLE_NS+"object", "object (twine)",
            
        TWINE_RADAR_NS+"wasCreatedBy",
            TWINE_ROLE_NS+"object", "object (twine)",
            TWINE_ROLE_NS+"creator", "creator (twine)",
            
        TWINE_RADAR_NS+"isContainedIn",
            TWINE_ROLE_NS+"member", "member (twine)",
            TWINE_ROLE_NS+"group", "group (twine)",
            
        TWINE_RADAR_NS+"contains"+
            TWINE_ROLE_NS+"group", "group (twine)",
            TWINE_ROLE_NS+"member", "member (twine)",
            
        TWINE_APP_NS+"wasCreatedInSpot",
            TWINE_ROLE_NS+"object", "object (twine)",
            TWINE_ROLE_NS+"spot", "spot (twine)",

        TWINE_APP_NS+"spotType",
            TWINE_ROLE_NS+"spot", "spot (twine)",
            TWINE_ROLE_NS+"spot-type", "spot-type (twine)",

        TWINE_APP_NS+"indirectlyContains",
            TWINE_ROLE_NS+"group", "group (twine)",
            TWINE_ROLE_NS+"member", "member (twine)",
            
        TWINE_DI_NS+"isAboutTopic",
            TWINE_ROLE_NS+"object", "object (twine)",
            TWINE_ROLE_NS+"topic", "topic (twine)",
            
        TWINE_DI_NS+"hasAnnotation",
            TWINE_ROLE_NS+"object", "object (twine)",
            TWINE_ROLE_NS+"annotation", "annotation (twine)",
    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }
}
