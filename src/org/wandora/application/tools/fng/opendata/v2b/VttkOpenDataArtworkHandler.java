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
package org.wandora.application.tools.fng.opendata.v2b;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.modules.velocityhelpers.JSONBox;
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


public class VttkOpenDataArtworkHandler extends FngOpenDataArtworkHandler implements FngOpenDataHandlerInterface {
    
    
    protected String ARTWORK_CLASS_TYPE_SI = "http://wandora.org/si/fng/generic_type";

    
    @Override
    public String getResourceURIBase() {
        return "http://kokoelmat.fng.fi/vttk/app?si=";
    }
    
    
    @Override
    public String getDefaultPublisher() {
        return "Finnish State Art Commission, Finnish National Gallery";
    }
    
    
    

    @Override
    public void populate(Topic t, TopicMap tm) throws TopicMapException {
        if(t != null) {
            setResourceURI( getResourceURIBase()+urlEncode(t.getBaseName()));

            // **** IDENTIFIERS ****
            addIdentifier(t.getBaseName(), "id");
            addIdentifier(getResourceURIBase()+urlEncode(t.getBaseName()), "uri");
            for( Locator si : t.getSubjectIdentifiers() ) {
                String sis = si.toExternalForm();
                if(sis.startsWith("http://kansallisgalleria.fi/")) {
                    addIdentifier(si.toExternalForm(), "si");
                }
            }

            // **** TITLES ****
            Set<Set<Topic>> scopes = t.getVariantScopes();
            for( Set<Topic> scope : scopes ) {
                String lang = null;
                String title = t.getVariant(scope);
                boolean isDisplay = false;
                for( Topic scopeTopic : scope ) {
                    for(Locator l : scopeTopic.getSubjectIdentifiers()) {
                        String si = l.toExternalForm();
                        if(XTMPSI.getLang("fi").equalsIgnoreCase(si)) {
                            lang = "fi";
                            break;
                        }
                        else if(XTMPSI.getLang("se").equalsIgnoreCase(si)) {
                            lang = "se";
                            break;
                        }
                        else if(XTMPSI.getLang("en").equalsIgnoreCase(si)) {
                            lang = "en";
                            break;
                        }
                        else if(XTMPSI.DISPLAY.equalsIgnoreCase(si)) {
                            isDisplay = true;
                            break;
                        }
                        else {
                            for(int i=0; i<languages.length; i=i+2) {
                                if(languages[i].equalsIgnoreCase(si)) {
                                    lang = languages[i+1];
                                    break;
                                }
                            }
                        }
                    }
                }
                if(isDisplay && title != null && title.trim().length() > 0 && lang != null) {
                    addTitle(title.trim(), lang);
                }
            }

            // **** TYPES ****
            addType("artwork"); // Static type for all artworks!

            Collection<Topic> headClasses = GenericVelocityHelper.getPlayers(t, ARTWORK_CLASS_SI, ARTWORK_CLASS_TYPE_SI);
            for( Topic headClass : headClasses ) {
                String className = getNameForArtworkClass(headClass);
                if(className != null) {
                    try {
                        int p = className.toLowerCase().indexOf(" hahmio");
                        if(p > 0) className = className.substring(0, p);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    addType(className, "artwork-class");
                }
            }

            // **** CREATORS ****
            Collection<Topic> authors = GenericVelocityHelper.getPlayers(t, AUTHOR_SI, ARTIST_SI, AUTHOR_ROLE_SI, ARTIST_SI);
            for( Topic author : authors ) {
                HashMap properties = new HashMap();
                for( Locator si : author.getSubjectIdentifiers() ) {
                    String sis = si.toExternalForm();
                    if(!sis.startsWith("http://wandora.org/si/defaultSI")) {
                        properties.put("si", si.toExternalForm());
                        break;
                    }
                }
                properties.put("uri", getResourceURIBase()+urlEncode(author.getBaseName()));
                addCreator(getNameFor(author), properties);
            }

            // **** SUBJECTS ****
            /*
            Collection<Association> subjectAssociations = t.getAssociations(tm.getTopic(KEYWORD_SI));
            for( Association subjectAssociation : subjectAssociations ) {
                Topic subjectTopic = subjectAssociation.getPlayer(tm.getTopic(KEYWORD_SI));
                Topic subjectType = subjectAssociation.getPlayer(tm.getTopic(KEYWORD_TYPE_SI));
                if(subjectTopic != null && subjectType != null) {
                    String subjectTypeName = getNameForSubjectType(subjectType);
                    if("iconclass-subject".equals(subjectTypeName) || "yso-subject".equals(subjectTypeName)) {
                        String bn = subjectTopic.getBaseName();
                        if(bn != null) {
                            if(bn.endsWith(" (iconclass)")) {
                                bn = bn.substring(0, bn.length()-12);
                            }
                            addSubject(bn, subjectTypeName);
                        }
                    }
                    else if(subjectTypeName != null) {
                        addSubject(getNameFor(subjectTopic), subjectTypeName);
                    }
                }
            }
            */
                    
            // **** MATERIAL ****
            Collection<Topic> materials = GenericVelocityHelper.getPlayers(t, MATERIAL_SI, MATERIAL_SI);
            for( Topic material : materials ) {
                String materialName = getNameFor(material);
                try {
                    int p = materialName.toLowerCase().indexOf(" hahmio");
                    if(p > 0) materialName = materialName.substring(0, p);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                addFormat(materialName, "material");
            }
            // **** DIMENSIONS ****
            Collection<Association> dimensionAssociations = t.getAssociations(tm.getTopic(DIMENSION_SI));
            for( Association dimensionAssociation : dimensionAssociations ) {
                Topic dimensionType = dimensionAssociation.getPlayer(tm.getTopic(DIMENSION_TYPE_SI));
                Topic dimensionValue = dimensionAssociation.getPlayer(tm.getTopic(DIMENSION_VALUE_SI));
                Topic dimensionUnit = dimensionAssociation.getPlayer(tm.getTopic(DIMENSION_UNIT_SI));
                
                if(dimensionType != null && dimensionValue != null && dimensionUnit != null) {
                    String dimensionTypeStr = getNameForDimensionType(dimensionType);
                    String dimensionValueStr = getNameForDimensionValue(dimensionValue);
                    String dimensionUnitStr = getNameFor(dimensionUnit);
                    addFormat(dimensionTypeStr + " " + dimensionValueStr + " " + dimensionUnitStr, "dimension");
                }
            }
                       
            // **** DATE ****
            Collection<Topic> times = GenericVelocityHelper.getPlayers(t, TIME_SI, TIME_SI);
            for( Topic time : times ) {
                addDate(getNameFor(time), "creation");
            }
            
            Collection<Association> acquisitionAssociations = t.getAssociations(tm.getTopic(ACQUISITION_SI));
            for( Association acquisitionAssociation : acquisitionAssociations ) {
                Topic acquisitionDate = acquisitionAssociation.getPlayer(tm.getTopic(TIME_SI));
                Topic acquisitionType = acquisitionAssociation.getPlayer(tm.getTopic(ACQUISITION_SI));
                if(acquisitionDate != null) {
                    addDate(getNameFor(acquisitionDate), "acquisition", getNameFor(acquisitionType));
                    
                }
            }

            
            // **** PUBLISHER ****
            /*
            Collection<Topic> museums = GenericVelocityHelper.getPlayers(t, KEEPER_SI, KEEPER_SI);
            for( Topic museum : museums ) {
                addPublisher(getNameFor(museum), "unit");
            }
            */
            addPublisher(getDefaultPublisher());
            
            
            
            
            // **** IMAGES ****
            Collection<Topic> images = GenericVelocityHelper.getPlayers(t, IMAGE_SI, IMAGE_SI);
            for( Topic image : images ) {
                String bn = image.getBaseName();
                if(bn != null) {
                    addRelation(bn, "image");
                }
            }
            
            // **** COLLECTION ****
            Collection<Topic> collections = GenericVelocityHelper.getPlayers(t, COLLECTION_SI, COLLECTION_SI);
            for( Topic collection : collections ) {
                addRelation(getNameFor(collection), "collection");
            }
            
            
            // **** USAGES ****
            /*
            Collection<Topic> usages = GenericVelocityHelper.getPlayers(t, USAGE_SI, USAGE_SI);
            for( Topic usage : usages ) {
                addRelation(getNameFor(usage), "usage");
            }
            */
            
            
            // **** DOCUMENTS ****
            /*
            Collection<Topic> documents = GenericVelocityHelper.getPlayers(t, DOCUMENTS_SI, DOCUMENTS_SI);
            for( Topic document : documents ) {
                addRelation(getNameFor(document), "document");
            }
            */
            
            // **** RIKASTETEKSTI ****
            /*
            Collection<Topic> enrichments = GenericVelocityHelper.getPlayers(t, ENRICHMENT_SI, TEXT_DOCUMENT_ROLE_SI);
            Topic otype = tm.getTopic(TEXT_OCCURRENCE_TYPE_SI);
            for( Topic enrichment : enrichments ) {
                addDescription(getOccurrenceFor(enrichment, otype, "fi"), "fi");
                addDescription(getOccurrenceFor(enrichment, otype, "en"), "en");
                addDescription(getOccurrenceFor(enrichment, otype, "se"), "se");
            }
            */
            
            String geoLocation = getRemoteGeoLocation(t.getBaseName());
            if(geoLocation != null) {
                addTypedDescription(geoLocation, "geo-location");
            }
            
            String description = getRemoteDescription(t.getBaseName());
            if(description != null) {
                addDescription(description);
            }
        }
    }
    
    
    
    // --------------------------------------------------------- REMOTE DATA ---
    
    
    
    public static JSONObject remoteData = null;
    
    
    public void getRemoteData() {
        String url = "http://www.lahteilla.fi/extras/api/data/all";
        remoteData = JSONBox.load(url);
    }
    
    
    public String getRemoteGeoLocation(String bn) {
        try {
            if(remoteData == null) {
                getRemoteData();
            }
            if(remoteData != null) {
                if(remoteData.has("nodes")) {
                    JSONArray nodes = remoteData.getJSONArray("nodes");
                    for(int i=0; i<nodes.length(); i++) {
                        JSONObject node = nodes.getJSONObject(i);
                        if(node.has("node")) {
                            node = node.getJSONObject("node");
                            if(node.has("title")) {
                                String title = node.getString("title");
                                if(bn.equals(title)) {
                                    if(node.has("latitude") && node.has("longitude")) {
                                        String lat = node.getString("latitude");
                                        String lon = node.getString("longitude");
                                        if(lat != null && lon != null) {
                                            if(lat.length()>0 && lon.length()>0 && !"null".equalsIgnoreCase(lat) && !"null".equalsIgnoreCase(lon)) {
                                                return lat+","+lon;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    public String getRemoteDescription(String bn) {
        try {
            if(remoteData == null) {
                getRemoteData();
            }
            if(remoteData != null) {
                if(remoteData.has("nodes")) {
                    JSONArray nodes = remoteData.getJSONArray("nodes");
                    for(int i=0; i<nodes.length(); i++) {
                        JSONObject node = nodes.getJSONObject(i);
                        if(node.has("node")) {
                            node = node.getJSONObject("node");
                            if(node.has("title")) {
                                String title = node.getString("title");
                                if(bn.equals(title)) {
                                    if(node.has("document")) {
                                        String document = node.getString("document");
                                        if(document != null && document.length()>0 && !"null".equalsIgnoreCase(document)) {
                                            return document;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
}

