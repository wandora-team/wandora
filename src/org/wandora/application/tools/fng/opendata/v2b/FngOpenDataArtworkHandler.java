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
import java.util.LinkedHashMap;
import java.util.Set;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.Tuples;
import org.wandora.utils.velocity.GenericVelocityHelper;

/**
 *
 * @author akivela
 */


public class FngOpenDataArtworkHandler extends FngOpenDataAbstractHandler implements FngOpenDataHandlerInterface {
    
    
    
    // -------------------------------------------------------------------------
    
    protected String BASE_SI = "http://wandora.org/si/fng/";
    
    
    protected String ARTWORK_SI = BASE_SI+"artwork";
    protected String ARTWORK_CLASS_SI = BASE_SI+"generic_type";
    protected String ARTWORK_CLASS_TYPE_SI = BASE_SI+"generic_type_carrier";
    
    protected String AUTHOR_SI = BASE_SI+"author";
    protected String AUTHOR_ROLE_SI = BASE_SI+"author-role";
    protected String ARTIST_SI = BASE_SI+"artists";
    
    protected String TECHNIQUE_SI = BASE_SI+"technique";
    protected String MATERIAL_SI = BASE_SI+"material";
    
    protected String KEEPER_SI = BASE_SI+"keeper";
    protected String ACQUISITION_SI = BASE_SI+"aqcuisition";

    protected String DIMENSION_SI = BASE_SI+"dimension";
    protected String DIMENSION_TYPE_SI = BASE_SI+"dimension_type";
    protected String DIMENSION_VALUE_SI = BASE_SI+"dimension_value";
    protected String DIMENSION_UNIT_SI = BASE_SI+"dimension_unit";
    
    protected String IMAGE_SI = BASE_SI+"imageoccurrence";
    protected String LICENSE_SI = BASE_SI+"image-license";
    protected String COLLECTION_SI = BASE_SI+"collection";
    
    
    protected String KEYWORD_SI = BASE_SI+"keyword";
    protected String KEYWORD_TYPE_SI = BASE_SI+"keyword-type";
    
    protected String TIME_SI = BASE_SI+"time";
    
    protected String MUSEUM_SI = BASE_SI+"museum";
    
    protected String USAGE_SI = BASE_SI+"usages";
    protected String DOCUMENTS_SI = BASE_SI+"documents";
    
    protected String ENRICHMENT_SI = BASE_SI+"rikasteet/teosviite";
    protected String TEXT_DOCUMENT_ROLE_SI = BASE_SI+"text";
    protected String TEXT_OCCURRENCE_TYPE_SI = BASE_SI+"rikasteet/teksti";
    
    
    
    protected String[] languages = new String[] {
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
    
    
    
    protected String[] subjectMapping = new String[] {
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
                addType(getNameForArtworkClass(headClass), "artwork-class");
            }

            // **** CREATORS ****
            Collection<Topic> authors = GenericVelocityHelper.getPlayers(t, AUTHOR_SI, ARTIST_SI, AUTHOR_ROLE_SI, ARTIST_SI);
            for( Topic author : authors ) {
                HashMap properties = new HashMap();
                for( Locator si : author.getSubjectIdentifiers() ) {
                    String sis = si.toExternalForm();
                    if(sis.startsWith("http://kansallisgalleria.fi/E39.Actor")) {
                        properties.put("si", si.toExternalForm());
                        break;
                    }
                }
                if(!properties.containsKey("si")) {
                    properties.put("si", author.getOneSubjectIdentifier().toExternalForm());
                }
                properties.put("uri", getResourceURIBase()+urlEncode(author.getBaseName()));
                addCreator(getNameFor(author), properties);
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
            Collection<Topic> acquisitionTimes = GenericVelocityHelper.getPlayers(t, ACQUISITION_SI, TIME_SI);
            for( Topic aquisitionTime : acquisitionTimes ) {
                addDate(getNameFor(aquisitionTime), "acquisition");
            }
            
            // **** PUBLISHER ****
            Collection<Topic> museums = GenericVelocityHelper.getPlayers(t, KEEPER_SI, KEEPER_SI);
            for( Topic museum : museums ) {
                addPublisher(getNameFor(museum), "unit");
            }
            addPublisher(getDefaultPublisher());
            
            
            
            
            // **** IMAGES ****
            Collection<Topic> images = GenericVelocityHelper.getPlayers(t, IMAGE_SI, IMAGE_SI);
            for( Topic image : images ) {
                if(image != null) {
                    String bn = image.getBaseName();
                    HashMap<String,String> imageParams = null;
                    Collection<Topic> licenses = GenericVelocityHelper.getPlayers(image, LICENSE_SI, LICENSE_SI);
                    if(licenses != null) {
                        StringBuilder licenseString = new StringBuilder("");
                        int i=0;
                        for( Topic license : licenses ) {
                            String licenseName = license.getDisplayName("en");
                            if(licenseName != null && !"".equals(licenseName)) {
                                if(i > 0) {
                                    licenseString.append(";");
                                }
                                licenseString.append(licenseName);
                                i++;
                            }
                        }
                        if(i > 0) {
                            imageParams = new LinkedHashMap<String,String>();
                            imageParams.put("license", licenseString.toString());
                            if("CC0".equalsIgnoreCase(licenseString.toString())) {
                                imageParams.put("url", "http://kokoelmat.fng.fi/app?action=image&profile=CC0&iid="+bn);
                            }
                        }
                    }
                    
                    if(bn != null) {
                        if(imageParams == null) {
                            addRelation(bn, "image");
                        }
                        else {
                            addRelation(bn, "image", imageParams);
                        }
                    }
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
    
    
    
    
    
    protected String getNameForDimensionType(Topic t) throws TopicMapException {
        String s = t.getDisplayName("fi");
        s = s.replaceAll("http://kansallisgalleria.fi/", "");
        s = s.replaceAll("E55.Type_", "");
        s = s.replaceAll("_", " ");
        s = s.replaceAll("(Dimension unit)", "");
        return s;
    }
    
    
    protected String getNameForDimensionValue(Topic t) throws TopicMapException {
        String s = t.getDisplayName("fi");
        s = s.replaceAll("http://kansallisgalleria.fi/", "");
        s = s.replaceAll("Number_", "");
        s = s.replaceAll("Number", "");
        s = s.replaceAll("_", ",");
        return s;
    }
    
    
    
    protected String getNameForSubjectType(Topic t) throws TopicMapException {
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
    
    
    
    
    
    
    protected String getNameForArtworkClass(Topic t) throws TopicMapException {
        String s = t.getDisplayName("fi");
        if(s != null) s = s.toLowerCase();
        return s;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
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

