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
 */
package org.wandora.application.tools.fng.opendata.v2b;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.velocity.GenericVelocityHelper;

/**
 *
 * @author akivela
 */


public class FngOpenDataArtistHandler extends FngOpenDataAbstractHandler implements FngOpenDataHandlerInterface {

    
    private String PERSON_BIRTH_SI = "http://www.wandora.org/person_birth"; // A-TYPE
    private String PERSON_DEATH_SI = "http://www.wandora.org/person_death"; // A-TYPE
    private String TIME_SI = "http://www.muusa.net/E52.Time-Span"; // ROLE
    private String PLACE_SI = "http://www.wandora.org/place"; // ROLE
    
    private String GROUP_SI = "http://www.wandora.org/group"; // A-TYPE
    private String ARTIST_GROUP_SI = "http://www.wandora.org/artist_group"; // ROLE
    
    private String IDENTIFIED_SI = "http://www.muusa.net/P131.is_identified_by"; // A-TYPE
    private String IDENTIFIED_ROLE_SI = "http://www.muusa.net/P131.is_identified_by_role_1"; // ROLE
    
    private String AUTHOR_SI = "http://www.wandora.org/author"; // A-TYPE
    private String ARTWORK_SI = "http://www.wandora.org/artwork"; // ROLE
    
    
    
    

    
    
    
    @Override
    public void populate(Topic t, TopicMap tm) throws TopicMapException {
        if(t != null) {

            setResourceURI( getResourceURIBase()+urlEncode(t.getBaseName()));

            // **** IDENTIFIERS ****
            addIdentifier(getResourceURIBase()+urlEncode(t.getBaseName()), "uri");
            for( Locator si : t.getSubjectIdentifiers() ) {
                String sis = si.toExternalForm();
                if(sis.indexOf("http://www.muusa.net/E39.Actor") != -1) {
                    addIdentifier(si.toExternalForm(), "si");
                }
            }
            
            // **** TITLES ****
            String title = t.getBaseName();
            if(title != null && title.trim().length() > 0) {
                addTitle(title.trim());
            }
            Collection<Topic> otherNames = GenericVelocityHelper.getPlayers(t, IDENTIFIED_SI, IDENTIFIED_ROLE_SI);
            for( Topic n : otherNames ) {
                String otherTitle = getNameFor(n);
                if(title == null || !title.equalsIgnoreCase(otherTitle)) {
                    addTypedTitle(otherTitle, "other");
                }
            }
            
            
            // **** TYPES ****
            addType("artist"); // Static type for all artworks!
            
            
            // **** DATES ****
            Topic birthTimeType = tm.getTopic(PERSON_BIRTH_SI);
            Topic timeType = tm.getTopic(TIME_SI);
            Topic locationType = tm.getTopic(PLACE_SI);
            if(birthTimeType != null && timeType != null) {
                Collection<Association> birthTimes = t.getAssociations(birthTimeType);
                for(Association birthTime : birthTimes) {
                    Topic timeTopic = birthTime.getPlayer(timeType);
                    if(timeTopic != null) {
                        HashMap properties = new HashMap();
                        if(locationType != null) {
                            Topic locationTopic = birthTime.getPlayer(locationType);
                            if(locationTopic != null) {
                                properties.put("loc", getNameFor(locationTopic));
                            }
                        }
                        addDate(getNameFor(timeTopic), "birth", properties);
                    }
                }
            }
            
            Topic deathTimeType = tm.getTopic(PERSON_DEATH_SI);
            if(deathTimeType != null && timeType != null) {
                Collection<Association> deathTimes = t.getAssociations(deathTimeType);
                for(Association deathTime : deathTimes) {
                    Topic timeTopic = deathTime.getPlayer(timeType);
                    if(timeTopic != null) {
                        HashMap properties = new HashMap();
                        if(locationType != null) {
                            Topic locationTopic = deathTime.getPlayer(locationType);
                            if(locationTopic != null) {
                                properties.put("loc", getNameFor(locationTopic));
                            }
                        }
                        addDate(getNameFor(timeTopic), "death", properties);
                    }
                }
            }
            
            
            // ARTWORKS
            Collection<Topic> artworks = GenericVelocityHelper.getPlayers(t, AUTHOR_SI, ARTWORK_SI);
            for( Topic artwork : artworks ) {
                HashMap properties = new HashMap();
                properties.put("id", artwork.getBaseName());
                properties.put("uri", getResourceURIBase()+urlEncode(artwork.getBaseName()));
                for( Locator si : artwork.getSubjectIdentifiers() ) {
                    String sis = si.toExternalForm();
                    if(!sis.startsWith("http://www.wandora.net/defaultSI")) {
                        properties.put("si", si.toExternalForm());
                        break;
                    }
                }
                addRelation(getNameFor(artwork), "artwork", properties);
            }
            
            // **** PUBLISHER ****
            addPublisher(getDefaultPublisher());
            
            
            // **** GROUP ****
            Collection<Topic> groups = GenericVelocityHelper.getPlayers(t, GROUP_SI, ARTIST_GROUP_SI);
            for( Topic group : groups ) {
                addRelation(getNameFor(group), "group");
            }
        }
    }
    
    
    

    @Override
    public String toString(String outputFormat) {
        
        // ******* Export Dublin Core XML *******
        if("dc-xml".equalsIgnoreCase(outputFormat)) {
            StringBuilder sb = new StringBuilder("");
            
            appendLine(sb, 1, inject(getResourceURI(), "<dcx:description>", "<dcx:description dcx:resourceURI=\"__1__\">"));

            appendAsDCXMLStatement("title", getTitles(), sb);
            appendAsDCXMLStatement("description", getDescriptions(), sb);
            appendAsDCXMLStatement("type", getTypes(), sb);
            appendAsDCXMLStatement("identifier", getIdentifiers(), sb);
            appendAsDCXMLStatement("creator", getCreators(), sb);
            appendAsDCXMLStatement("publisher", getPublishers(), sb);
            appendAsDCXMLStatement("date", getDates(), sb);
            appendAsDCXMLStatement("subject", getSubjects(), sb);
            appendAsDCXMLStatement("format", getFormats(), sb);
            appendAsDCXMLStatement("relation", getRelations(), sb);

            appendLine(sb, 1, "</dcx:description>");
            
            return sb.toString();
            
        // ******* Export Dublin Core Description Sets using XML *******
        } else if("dc-ds-xml".equalsIgnoreCase(outputFormat)){
            StringBuilder sb = new StringBuilder("");
            
            appendLine(sb, 1, inject(getResourceURI(), "<dcds:description>", "<dcds:description dcds:resourceURI=\"__1__\">"));

            appendAsXMLStatement("title", "Language", getTitles(), sb);
            appendAsXMLStatement("description", "Language", getDescriptions(), sb);
            appendAsXMLStatement("type", "Type", getTypes(), sb);
            appendAsXMLStatement("identifier", "Type", getIdentifiers(), sb);
            appendAsXMLStatement("creator", "Type", getCreators(), sb);
            appendAsXMLStatement("publisher", "Type", getPublishers(), sb);
            appendAsXMLStatement("date", "Type", getDates(), sb);
            appendAsXMLStatement("subject", "Type", getSubjects(), sb);
            appendAsXMLStatement("format", "Type", getFormats(), sb);
            appendAsXMLStatement("relation", "Type", getRelations(), sb);
            
            appendLine(sb, 1, "</dcds:description>");
            
            return sb.toString();
        }
        
        
        // ******* Export Dublin Core JSON *******
        else if("dc-json".equalsIgnoreCase(outputFormat)) {
            StringBuilder sb = new StringBuilder("");

            appendLine(sb, 1, "{");
            
            appendAsJSONArray("title", getTitles(), sb);
            appendAsJSONArray("description", getDescriptions(), sb);
            appendAsJSONArray("type", getTypes(), sb);
            appendAsJSONArray("identifier", getIdentifiers(), sb);
            appendAsJSONArray("creator", getCreators(), sb);
            appendAsJSONArray("publisher", getPublishers(), sb);
            appendAsJSONArray("date", getDates(), sb);
            appendAsJSONArray("subject", getSubjects(), sb);
            appendAsJSONArray("format", getFormats(), sb);
            appendAsJSONArray("relation", getRelations(), sb);

            trimLastComma(sb);
            
            appendLine(sb, 1, "}");
            
            return sb.toString();
        }
        
   
        // ******* Export Dublin Core Text *******
        else {
            StringBuilder sb = new StringBuilder("");

            appendLine(sb, 1, "Description (");
            appendLine(sb, 2, "ResourceURI ( "+getResourceURI()+" )");
            
            appendAsTextStatement("title", "Language", getTitles(), sb);
            appendAsTextStatement("description", "Language", getDescriptions(), sb);
            appendAsTextStatement("type", "Type", getTypes(), sb);
            appendAsTextStatement("identifier", "Type", getIdentifiers(), sb);
            appendAsTextStatement("creator", "Type", getCreators(), sb);
            appendAsTextStatement("publisher", "Type", getPublishers(), sb);
            appendAsTextStatement("date", "Type", getDates(), sb);
            appendAsTextStatement("subject", "Type", getSubjects(), sb);
            appendAsTextStatement("format", "Type", getFormats(), sb);
            appendAsTextStatement("relation", "Type", getRelations(), sb);

            appendLine(sb, 1, ")");
            
            return sb.toString();
        }
    }
    
}
