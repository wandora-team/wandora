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
package org.wandora.application.tools.fng.opendata.v2;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.velocity.GenericVelocityHelper;

/**
 *
 * @author akivela
 */


public class FngOpenDataStruct {
    
    private String resourceURI = null;
    
    private Collection<T2<String,String>> titles = null;
    private Collection<T2<String,String>> types = null;
    private Collection<T2<String,String>> identifiers = null;
    private Collection<T2<String,String>> subjects = null;
    private Collection<T2<String,String>> creators = null;
    private Collection<T2<String,String>> dates = null;
    private Collection<T2<String,String>> formats = null;
    private Collection<T2<String,String>> rights = null;
    private Collection<T2<String,String>> publishers = null;
    private Collection<T2<String,String>> relations = null;
    private Collection<T2<String,String>> descriptions = null;
    
    
    
    public FngOpenDataStruct() {
        titles = new ArrayList<T2<String,String>>();
        identifiers = new ArrayList<T2<String,String>>();
        types = new ArrayList<T2<String,String>>();
        subjects = new ArrayList<T2<String,String>>();
        creators = new ArrayList<T2<String,String>>();
        dates = new ArrayList<T2<String,String>>();
        formats = new ArrayList<T2<String,String>>();
        rights = new ArrayList<T2<String,String>>();
        publishers = new ArrayList<T2<String,String>>();
        relations = new ArrayList<T2<String,String>>();
        descriptions = new ArrayList<T2<String,String>>();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void setResourceURI(String str) {
        str = str.replace(" ", "%20");
        resourceURI = str;
    }
    
    
    
    public String getResourceURI() {
        return resourceURI;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addTitle(String str) {
        addTitle(str, null);
    }
    
    public void addTitle(String str, String lang) {
        if(str != null) {
            titles.add(new T2<String,String>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getTitles() {
        return titles;
    }
    
    // -------------------------------------------------------------------------
    
    public void addType(String str) {
        addType(str, null);
    }
    
    public void addType(String str, String lang) {
        if(str != null) {
            types.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getTypes() {
        return types;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addIdentifier(String str) {
        addIdentifier(str, null);
    }
    
    public void addIdentifier(String str, String lang) {
        if(str != null) {
            identifiers.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getIdentifiers() {
        return identifiers;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addCreator(String str) {
        addCreator(str, null);
    }
    
    public void addCreator(String str, String lang) {
        if(str != null) {
            creators.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getCreators() {
        return creators;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addDate(String str) {
        addDate(str, null);
    }
    
    public void addDate(String str, String lang) {
        if(str != null) {
            dates.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getDates() {
        return dates;
    }
    
    // -------------------------------------------------------------------------
    
    public void addSubject(String str) {
        addSubject(str, null);
    }
    
    public void addSubject(String str, String lang) {
        if(str != null) {
            subjects.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getSubjects() {
        return subjects;
    }
        
    // -------------------------------------------------------------------------
    
    
    public void addFormat(String str) {
        addFormat(str, null);
    }
    
    public void addFormat(String str, String lang) {
        if(str != null) {
            formats.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getFormats() {
        return formats;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addRights(String str) {
        addRights(str, null);
    }
    
    public void addRights(String str, String lang) {
        if(str != null) {
            rights.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getRights() {
        return rights;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addPublisher(String str) {
        addPublisher(str, null);
    }
    
    
    public void addPublisher(String str, String lang) {
        if(str != null) {
            publishers.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getPublishers() {
        return publishers;
    }
    
    // -------------------------------------------------------------------------
    
    public void addRelation(String str) {
        addRelation(str, null);
    }
    
    public void addRelation(String str, String lang) {
        if(str != null) {
            relations.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getRelations() {
        return relations;
    }
    
    
    // -------------------------------------------------------------------------
    
    public void addDescription(String str) {
        addRelation(str, null);
    }
    
    public void addDescription(String str, String lang) {
        if(str != null) {
            descriptions.add(new T2<>(str, lang));
        }
    }
    
    public Collection<T2<String,String>> getDescriptions() {
        return descriptions;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    private String ARTWORK_SI = "http://www.wandora.net/artwork";
    private String ARTWORK_CLASS_SI = "http://www.wandora.net/generic_type";
    private String ARTWORK_CLASS_TYPE_SI = "http://www.wandora.net/generic_type_carrier";
    
    private String AUTHOR_SI = "http://www.wandora.net/author";
    private String AUTHOR_ROLE_SI = "http://kansallisgalleria.fi/P14.Production_carried_out_by_role_3";
    private String ARTIST_SI = "http://www.wandora.org/artists";
    
    private String TECHNIQUE_SI = "http://www.wandora.net/technique";
    private String MATERIAL_SI = "http://www.wandora.net/material";
    
    private String KEEPER_SI = "http://www.wandora.net/keeper";
    private String AQUISITION_SI = "http://www.wandora.net/aqcuisition";
    
    private String DIMENSION_SI = "http://www.wandora.net/dimension";
    private String DIMENSION_TYPE_SI = "http://www.wandora.net/dimension_type";
    private String DIMENSION_VALUE_SI = "http://www.wandora.net/dimension_value";
    private String DIMENSION_UNIT_SI = "http://www.wandora.net/dimension_unit";
    
    private String IMAGE_SI = "http://www.wandora.net/imageoccurrence";
    private String COLLECTION_SI = "http://www.wandora.net/collection";
    
    
    private String KEYWORD_SI = "http://www.wandora.net/keyword";
    private String KEYWORD_TYPE_SI = "http://www.wandora.org/keyword-type";
    
    private String TIME_SI = "http://www.wandora.net/time";
    
    private String MUSEUM_SI = "http://www.wandora.net/museum";
    
    private String USAGE_SI = "http://www.wandora.net/usages";
    private String DOCUMENTS_SI = "http://www.wandora.net/documents";
    
    private String ENRICHMENT_SI = "http://www.wandora.org/rikasteet/teosviite";
    private String TEXT_DOCUMENT_ROLE_SI = "http://www.wandora.net/text";
    private String TEXT_OCCURRENCE_TYPE_SI = "http://www.wandora.org/rikasteet/teksti";
    
    
    
    private String[] languages = new String[] {
        "http://kansallisgalleria.fi/E55.Type_teosnimi_tanska",                "dn",
        "http://kansallisgalleria.fi/E55.Type_teosnimi__ru",                   "ru",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_ru",                    "ru",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_ve",                    "ru",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_it",                    "it",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_italia",                "it",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_ra",                    "fr",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_ransk",                 "fr",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_ranska",                "fr",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_alkuperainen_fr",       "fr",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_unkari",                "hu",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_portugal",              "pr",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_saks_",                 "ge",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_saksa",                 "ge",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_norja",                 "no",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_puola",                 "po",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_esp_",                  "sp",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_sp",                    "sp",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_sp",                    "sp",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_espanja",               "sp",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_latina",                "la",
        "http://kansallisgalleria.fi/E55.Type_teosnimi__muu_se",               "se",
        "http://kansallisgalleria.fi/E55.Type_teosnimi__muu_fr",               "fr",
        "http://kansallisgalleria.fi/E55.Type_teosnimi__entinen_se",           "se",
        "http://kansallisgalleria.fi/E55.Type_teosnimi_hollanti",              "de"
    };
    
    
    
    public String[] subjectMapping = new String[] {
        "http://kansallisgalleria.fi/P71_lists_aihe",              "subject",
        "http://kansallisgalleria.fi/P71_lists_Kuvauksen_kohde",   "subject-person",
        "http://kansallisgalleria.fi/P71_lists_kohdehenkilo",      "subject-person",
        "http://kansallisgalleria.fi/P71_lists_aiheesta",          "about-the-subject",
        "http://kansallisgalleria.fi/P71_lists_Musiikki",          "about-the-music",
        "http://kansallisgalleria.fi/P71_lists_teosteksti",        "text-in-work",
        "http://kansallisgalleria.fi/P71_lists_yso_aihe",          "yso-subject",
        "http://kansallisgalleria.fi/P71_lists_kuvauksen_kohde",   "depicts-subject",
        "http://kansallisgalleria.fi/P71_lists_lisatieto",         "additional-info",
        "http://kansallisgalleria.fi/P71_lists_iconclass",         "iconclass-subject",
        "http://kansallisgalleria.fi/P71_lists_asiasanat",         "keyword",
        "http://kansallisgalleria.fi/P71_lists_malli",             "model",
        "http://kansallisgalleria.fi/P71_lists_mallit",            "model",
    };
    
    
    
    public void populate(Topic t) throws TopicMapException {
        if(t != null) {
            TopicMap tm = t.getTopicMap();
            
            setResourceURI( "http://kokoelmat.fng.fi/app?si="+urlEncode(t.getBaseName()));

            // **** IDENTIFIERS ****
            addIdentifier(t.getBaseName(), "id");
            addIdentifier("http://kokoelmat.fng.fi/app?si="+urlEncode(t.getBaseName()), "uri");
            for( Locator si : t.getSubjectIdentifiers() ) {
                String sis = si.toExternalForm();
                if(!sis.startsWith("http://www.wandora.net/defaultSI")) {
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
                addType(getNameForArtworkClass(headClass), "artwork-class");
            }

            // **** CREATORS ****
            Collection<Topic> authors = GenericVelocityHelper.getPlayers(t, AUTHOR_SI, ARTIST_SI, AUTHOR_ROLE_SI, ARTIST_SI);
            for( Topic author : authors ) {
                addCreator(getNameFor(author));
            }

            // **** SUBJECTS ****
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
            
            // **** MATERIAL ****
            Collection<Topic> materials = GenericVelocityHelper.getPlayers(t, MATERIAL_SI, MATERIAL_SI);
            for( Topic material : materials ) {
                addFormat(getNameFor(material), "material");
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
            Collection<Topic> acquisitionTimes = GenericVelocityHelper.getPlayers(t, AQUISITION_SI, TIME_SI);
            for( Topic aquisitionTime : acquisitionTimes ) {
                addDate(getNameFor(aquisitionTime), "acquisition");
            }
            
            // **** PUBLISHER ****
            Collection<Topic> museums = GenericVelocityHelper.getPlayers(t, KEEPER_SI, KEEPER_SI);
            for( Topic museum : museums ) {
                addPublisher(getNameFor(museum), "unit");
            }
            addPublisher("Finnish National Gallery");
            
            
            
            
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
            Collection<Topic> usages = GenericVelocityHelper.getPlayers(t, USAGE_SI, USAGE_SI);
            for( Topic usage : usages ) {
                addRelation(getNameFor(usage), "usage");
            }
            
            // **** DOCUMENTS ****
            Collection<Topic> documents = GenericVelocityHelper.getPlayers(t, DOCUMENTS_SI, DOCUMENTS_SI);
            for( Topic document : documents ) {
                addRelation(getNameFor(document), "document");
            }
            
            
            // **** RIKASTETEKSTI ****
            Collection<Topic> enrichments = GenericVelocityHelper.getPlayers(t, ENRICHMENT_SI, TEXT_DOCUMENT_ROLE_SI);
            Topic otype = tm.getTopic(TEXT_OCCURRENCE_TYPE_SI);
            for( Topic enrichment : enrichments ) {
                addDescription(getOccurrenceFor(enrichment, otype, "fi"), "fi");
                addDescription(getOccurrenceFor(enrichment, otype, "en"), "en");
                addDescription(getOccurrenceFor(enrichment, otype, "se"), "se");
            }
        }
    }
    
    
    
    private String getNameFor(Topic t) throws TopicMapException {
        return t.getDisplayName("fi");
    }
    
    private String getNameFor(Topic t, String lang) throws TopicMapException {
        return t.getDisplayName(lang);
    }
    
    
    
    private String getNameForDimensionType(Topic t) throws TopicMapException {
        String s = t.getDisplayName("fi");
        s = s.replaceAll("http://kansallisgalleria.fi/", "");
        s = s.replaceAll("E55.Type_", "");
        s = s.replaceAll("_", " ");
        s = s.replaceAll("(Dimension unit)", "");
        return s;
    }
    
    
    private String getNameForDimensionValue(Topic t) throws TopicMapException {
        String s = t.getDisplayName("fi");
        s = s.replaceAll("http://kansallisgalleria.fi/", "");
        s = s.replaceAll("Number_", "");
        s = s.replaceAll("Number", "");
        s = s.replaceAll("_", ",");
        return s;
    }
    
    
    
    private String getNameForSubjectType(Topic t) throws TopicMapException {
        if(t != null) {
            for(Locator l : t.getSubjectIdentifiers()) {
                String si = l.toExternalForm();
                for(int i=0; i<subjectMapping.length; i=i+2) {
                    if(subjectMapping[i].equalsIgnoreCase(si)) {
                        return subjectMapping[i+1];
                    }
                }
            }
        }
        return null;
    }
    
    
    
    
    
    
    private String getNameForArtworkClass(Topic t) throws TopicMapException {
        String s = t.getDisplayName("fi");
        if(s != null) s = s.toLowerCase();
        return s;
    }
    
    
    
    
    private String getOccurrenceFor(Topic t, Topic type, String lang) throws TopicMapException {
        if(type != null) {
            String o = t.getData(type, lang);
            if(o != null) {
                o = o.replaceAll("\\<.+?\\>", "");
            }
            return o;
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    public String toString(String outputFormat) {
        
        
        // ******* Export Dublin Core XML *******
        if("dc-xml".equalsIgnoreCase(outputFormat)) {
            StringBuilder sb = new StringBuilder("");
            
            appendLine(sb, 1, inject(getResourceURI(), "<dcx:description>", "<dcx:description dcx:resourceURI=\"__1__\">"));

            for(T2<String,String> title : getTitles()) {
                appendLine(sb, 2, injectT2(encodeXML(title), "<dc:title>__1__</dc:title>", "<dc:title lang=\"__2__\">__1__</dc:title>"));
            }
            for(T2<String,String> description : getDescriptions()) {
                appendLine(sb, 2, injectT2(encodeXML(description), "<dc:description>__1__</dc:description>", "<dc:description lang=\"__2__\">__1__</dc:description>"));
            }
            for(T2<String,String> type : getTypes()) {
                appendLine(sb, 2, injectT2(encodeXML(type), "<dc:type>__1__</dc:type>", "<dc:type type=\"__2__\">__1__</dc:type>"));
            }
            for(T2<String,String> identifier : getIdentifiers()) {
                appendLine(sb, 2, injectT2(encodeXML(identifier), "<dc:identifier>__1__</dc:identifier>", "<dc:identifier type=\"__2__\">__1__</dc:identifier>"));
            }
            for(T2<String,String> creator : getCreators()) {
                appendLine(sb, 2, injectT2(encodeXML(creator), "<dc:creator>__1__</dc:creator>", "<dc:creator type=\"__2__\">__1__</dc:creator>"));
            }
            for(T2<String,String> publisher : getPublishers()) {
                appendLine(sb, 2, injectT2(encodeXML(publisher), "<dc:publisher>__1__</dc:publisher>", "<dc:publisher type=\"__2__\">__1__</dc:publisher>"));
            }
            for(T2<String,String> date : getDates()) {
                appendLine(sb, 2, injectT2(encodeXML(date), "<dc:date>__1__</dc:date>", "<dc:date type=\"__2__\">__1__</dc:date>"));
            }
            for(T2<String,String> subject : getSubjects()) {
                appendLine(sb, 2, injectT2(encodeXML(subject), "<dc:subject>__1__</dc:subject>", "<dc:subject type=\"__2__\">__1__</dc:subject>"));
            }
            for(T2<String,String> format : getFormats()) {
                appendLine(sb, 2, injectT2(encodeXML(format), "<dc:format>__1__</dc:format>", "<dc:format type=\"__2__\">__1__</dc:format>"));
            }
            for(T2<String,String> relation : getRelations()) {
                appendLine(sb, 2, injectT2(encodeXML(relation), "<dc:relation>__1__</dc:relation>", "<dc:relation type=\"__2__\">__1__</dc:relation>"));
            }
            
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
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    private void appendAsXMLStatement(String propertyUri, String valueType, Collection<T2<String,String>> values, StringBuilder sb) {
        if(values != null && !values.isEmpty()) {
            appendLine(sb, 2, "<dcds:statement dcds:propertyURI=\""+propertyUri +"\">");
            for(T2<String,String> value : values) {
                String type = encodeTextString(value.e2);
                String str = encodeTextString(value.e1);
                if(type == null) {
                    appendLine(sb, 3, "<dcds:valueString>"+str+"</dcds:valueString>" );
                }
                else {
                    appendLine(sb, 3, "<dcds:valueString " + valueType + "=\"" + type + "\">" + str + "</dcds:valueString>");
                }
            }
            appendLine(sb, 2, "</dcds:statement>");
        }
    }
    
    // -------------------------------------------------------------------------
    
    private void appendAsTextStatement(String propertyUri, String valueType, Collection<T2<String,String>> values, StringBuilder sb) {
        if(values != null && !values.isEmpty()) {
            appendLine(sb, 2, "Statement (");
            appendLine(sb, 3, "PropertyURI ( dcterms:"+propertyUri+" )");
            for(T2<String,String> value : values) {
                String type = encodeTextString(value.e2);
                String str = encodeTextString(value.e1);
                if(type == null) {
                    appendLine(sb, 3, "ValueString ( \""+str+"\" )" );
                }
                else {
                    appendLine(sb, 3, "ValueString ( \""+str+"\"" );
                    if("Language".equals(valueType)) {
                        appendLine(sb, 4, valueType+" ( "+type+" )" );
                        appendLine(sb, 3, ")" );
                    }
                    else {
                        appendLine(sb, 4, valueType+" ( \""+type+"\" )" );
                        appendLine(sb, 3, ")" );
                    }
                }
            }
            appendLine(sb, 2, ")");
        }
    }
    
    
    private String encodeTextString(String str) {
        if(str != null) {
            str = str.replace("\"", "\\\"");
        }
        return str;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    private void appendAsJSONArray(String label, Collection<T2<String,String>> values, StringBuilder sb) {
        if(values != null && !values.isEmpty()) {
            appendLine(sb, 2, "\""+label+"\": [");
            for(T2<String,String> value : values) {
                String key = encodeJSONKey(value.e2);
                String str = encodeJSONString(value.e1);
                if(key == null) key = label;
                appendLine(sb, 3, "{ \"" +key+"\": \""+str+"\" }," );
            }
            trimLastComma(sb);
            appendLine(sb, 2, "],");
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private void trimLastComma(StringBuilder sb) {
        if(sb != null) {
            int l = sb.lastIndexOf(",");
            if(l > 0) {
                sb.deleteCharAt(l);
            }
        }
    }
    
    
    
    private T2<String,String> encodeJSON(T2<String,String> t2) {
        return new T2<String,String>(encodeJSONString(t2.e1), encodeJSONString(t2.e2));
    }
    
    private String encodeJSONString(String string) {
        if (string == null || string.length() == 0) {
             return "\"\"";
         }

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         String       t;

         for (i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
                 sb.append('\\');
                 sb.append(c);
                 break;
             case '/':
    //                if (b == '<') {
                     sb.append('\\');
    //                }
                 sb.append(c);
                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\r");
                break;
             default:
                 if (c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         return sb.toString();
    }
    
    
    
    
    private String encodeJSONKey(String str) {
        if(str != null) {
            str = str.replaceAll("\\W", "_");
        }
        return str;
    }
    
    
    
    
    private T2<String,String> encodeXML(T2<String,String> t2) {
        return new T2<String,String>(encodeXMLValue(t2.e1), encodeXMLAttribute(t2.e2));
    }

    
    
    private String encodeXMLValue(String str) {
        if(str != null) {
            str = str.replace("&", "&amp;");
            str = str.replace("<", "&lt;");
            str = str.replace(">", "&gt;");
        }
        return str;
    }
    
    private String encodeXMLAttribute(String str) {
        if(str != null) {
            str = str.replace("\"", "\\\"");
        }
        return str;
    }
    
    
    
    
    
    private String inject(String d, String str0, String str1) {
        if(str1 != null) {
            if(d != null) {
                return str1.replace("__1__", d);
            }
        }
        return str0;
    }
    
    
    
    private String injectT2(T2<String,String> d, String str1, String str2) {
        if(d != null && str1 != null && str2 != null) {
            if(d.e1 != null && d.e2 == null) {
                return str1.replace("__1__", d.e1);
            }
            if(d.e1 != null && d.e2 != null) {
                String str = str2.replace("__1__", d.e1);
                return str.replace("__2__", d.e2);
            }
        }
        return "";
    }
    
    
    private void appendLine(StringBuilder sb, String str) {
        appendLine(sb, 0, str);
    }
    
    private void appendLine(StringBuilder sb, int tabs, String str) {
        switch(tabs) {
            case 1: { sb.append("  "); break; }
            case 2: { sb.append("    "); break; }
            case 3: { sb.append("      "); break; }
            case 4: { sb.append("        "); break; }
            case 5: { sb.append("          "); break; }
        }
        sb.append(str);
        sb.append("\n");
    }
    
    
    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {}
        return str;
    }
    
}
