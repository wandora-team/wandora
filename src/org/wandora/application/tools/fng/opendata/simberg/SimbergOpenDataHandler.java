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
package org.wandora.application.tools.fng.opendata.simberg;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.wandora.application.WandoraToolLogger;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.velocity.GenericVelocityHelper;

/**
 *
 * @author akivela
 */


public class SimbergOpenDataHandler {
    private static final boolean SPLIT_KEYWORDS = false;
    
    
    ArrayList<SimbergPhotograph> data = new ArrayList<>();

    
    
    public SimbergOpenDataHandler(TopicMap tm, WandoraToolLogger logger) {
        try {
            make(tm, logger);
        }
        catch(Exception e){
            if(logger != null) logger.log(e);
            else e.printStackTrace();
        }
    }
    
    
    

    public static final String VALOKUVA_SI = "http://kansallisgalleria.fi/Valokuva";
    
    public static final String IS_ABOUT_SI = "http://kansallisgalleria.fi/P129.is_about";
    public static final String IS_ABOUT_ROLE_SI = "http://kansallisgalleria.fi/P129.is_about_role_0";
    
    public static final String AUTHOR_SI = "http://kansallisgalleria.fi/P14.Production_carried_out_by";
    public static final String AUTHOR_ROLE_SI = "http://kansallisgalleria.fi/P14.Production_carried_out_by_role_1";

    public static final String TYPE_SI = "http://kansallisgalleria.fi/P2.has_type";
    public static final String TYPE_ROLE_SI = "http://kansallisgalleria.fi/P2.has_type_role_0";

    public static final String MATERIAL_SI = "http://kansallisgalleria.fi/P45.consists_of";
    public static final String MATERIAL_ROLE_SI = "http://kansallisgalleria.fi/P45.consists_of_role_0";

    public static final String TIME_SI = "http://kansallisgalleria.fi/P4.1.Production_has_time-span";
    public static final String TIME_ROLE_SI = "http://kansallisgalleria.fi/P4.1.Production_has_time-span_role_1";
    
    public static final String KEEPER_SI = "http://kansallisgalleria.fi/P50.has_current_keeper";
    public static final String KEEPER_ROLE_SI = "http://kansallisgalleria.fi/P50.has_current_keeper_role_1";
    
    public static final String IMAGE_SI = "http://kansallisgalleria.fi/P62.depicts";
    public static final String IMAGE_TYPE_ROLE_SI = "http://kansallisgalleria.fi/P62.depicts_role_2";
    public static final String IMAGE_ROLE_SI = "http://kansallisgalleria.fi/Source";
    
    
    
    
    public void make(TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
        if(tm != null) {
            logger.log("Collecting photograph data.");
            for(Topic t : tm.getTopicsOfType(VALOKUVA_SI)) {
                data.add(make(t, tm, logger));
            }
        }
    }
    
    
    
    
    public SimbergPhotograph make(Topic t, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
        SimbergPhotograph d = new SimbergPhotograph();
        
        if(t != null) {
            
            // topic given in t argument is a photograph topic
            
            // PHOTOGRAPH TITLE (identifier)
            d.title = t.getBaseName();
            if(d.title == null) d.title = "generated-title-"+System.currentTimeMillis();
            
            // KEYWORDS
            Collection<Topic> keywords = GenericVelocityHelper.getPlayers(t, IS_ABOUT_SI, IS_ABOUT_ROLE_SI);
            for( Topic keyword : keywords ) {
                String keywordString = keyword.getBaseName();
                
                SimbergKeyword originalKeyword = new SimbergKeyword();
                originalKeyword.key = keywordString;
                originalKeyword.type = "original";
                d.keywords.add(originalKeyword);
                
                if(SPLIT_KEYWORDS) {
                    String[] keywordStrs = keywordString.split(",");
                    for(String keywordStr : keywordStrs) {
                        keywordStr = keywordStr.trim();
                        if(keywordStr.length() > 0) {
                            SimbergKeyword simKeyword = new SimbergKeyword();
                            simKeyword.key = keywordStr;
                            d.keywords.add(simKeyword);
                        }
                    }
                }
            }
            
            // AUTHOR
            Collection<Topic> authors = GenericVelocityHelper.getPlayers(t, AUTHOR_SI, AUTHOR_ROLE_SI);
            for( Topic author : authors ) {
                String authorString = author.getBaseName();
                if(authorString != null) {
                    authorString = authorString.trim();
                    if(authorString.length() > 0) {
                        if(d.author == null) {
                            d.author = authorString;
                        }
                        else {
                            logger.log("Warning. Author already set. Skipping second author.");
                        }
                    }
                }
            }
            
            // TIME
            Collection<Topic> times = GenericVelocityHelper.getPlayers(t, TIME_SI, TIME_ROLE_SI);
            for( Topic time : times ) {
                String timeString = time.getBaseName();
                if(timeString != null) {
                    timeString = timeString.trim();
                    if(timeString.length() > 0) {
                        if(d.date == null) {
                            d.date = timeString;
                        }
                        else {
                            logger.log("Warning. Date already set. Skipping second date.");
                        }
                    }
                }
            }
            
            // KEEPER
            Collection<Topic> keepers = GenericVelocityHelper.getPlayers(t, KEEPER_SI, KEEPER_ROLE_SI);
            for( Topic keeper : keepers ) {
                String keeperString = keeper.getBaseName();
                if(keeperString != null) {
                    keeperString = keeperString.trim();
                    if(keeperString.equals("KKA")) keeperString = "Kokoelmienhallinta, Kansallisgalleria";
                    if(keeperString.length() > 0) {
                        if(d.keeper == null) {
                            d.keeper = keeperString;
                        }
                        else {
                            logger.log("Warning. Keeper already set. Skipping second keeper.");
                        }
                    }
                }
            }
            
            
            // TYPE
            Collection<Topic> types = GenericVelocityHelper.getPlayers(t, TYPE_SI, TYPE_ROLE_SI);
            for( Topic type : types ) {
                String typeString = type.getBaseName();
                if(typeString != null && typeString.length() > 0) {
                    if(typeString.startsWith("valokuvatyyppi")) typeString = typeString.substring("valokuvatyyppi".length());
                    int i = typeString.indexOf("vpakkane");
                    if(i > -1) typeString = typeString.substring(0, i);
                    typeString = typeString.trim();
                    if(typeString.length() > 0) {
                        if(d.type == null) {
                            d.type = typeString;
                        }
                        else {
                            logger.log("Warning. Type already set. Skipping second type.");
                        }
                    }
                }
            }
            
            // MATERIAL
            Collection<Topic> materials = GenericVelocityHelper.getPlayers(t, MATERIAL_SI, MATERIAL_ROLE_SI);
            for( Topic material : materials ) {
                String materialString = material.getBaseName();
                if(materialString != null && materialString.length() > 0) {
                    if(materialString.startsWith("valokuvamateriaali")) materialString = materialString.substring("valokuvamateriaali".length());
                    int i = materialString.indexOf("vpakkane");
                    if(i > -1) materialString = materialString.substring(0, i);
                    materialString = materialString.trim();
                    if(materialString.length() > 0) {
                        if(d.material == null) {
                            d.material = materialString;
                        }
                        else {
                            logger.log("Warning. Material already set. Skipping second material.");
                        }
                    }
                }
            }
            
            // IMAGES
            Collection<Association> imageAssociations = t.getAssociations(tm.getTopic(IMAGE_SI));
            if(imageAssociations != null) {
                for( Association imageAssociation : imageAssociations ) {
                    Topic imageTypeTopic = imageAssociation.getPlayer(tm.getTopic(IMAGE_TYPE_ROLE_SI));
                    Topic imageTopic = imageAssociation.getPlayer(tm.getTopic(IMAGE_ROLE_SI));
                    
                    if(imageTopic != null) {
                        SimbergImage simbergImage = new SimbergImage();
                        simbergImage.key = imageTopic.getBaseName();
                        
                        if(imageTypeTopic != null) {
                            String imageTypeSI = imageTypeTopic.getFirstSubjectIdentifier().toExternalForm();
                            if("http://kansallisgalleria.fi/P62_1_mode_of_depiction_kuvan_originaali".equals(imageTypeSI)) {
                                simbergImage.type = "negative";
                            }
                            else {
                                simbergImage.type = "positive";
                            }
                        }
                        d.images.add(simbergImage);
                    }
                }
            }
        }
        return d;
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------- DATA OBJECTS ---
    // -------------------------------------------------------------------------
    
    
    
    public class SimbergPhotograph {
        public String title = null;
        public String date = null;
        public String type = null;
        public String author = null;
        public String material = null;
        public String keeper = null;
        public ArrayList<SimbergImage> images = new ArrayList<>();
        public ArrayList<SimbergKeyword> keywords = new ArrayList<>();
        
        
        
        
        public String toString(String t) {
            StringBuilder s = new StringBuilder("");
            
            // ===== XML =====
            if("xml".equals(t)) {
                String tabs = "  ";
                s.append(" <photograph>\n");
                s.append(makeXMLElement("author", author, tabs));
                s.append(makeXMLElement("title", title, tabs));
                s.append(makeXMLElement("type", type, tabs));
                s.append(makeXMLElement("date", date, tabs));
                s.append(makeXMLElement("keeper", keeper, tabs));
                s.append(makeXMLElement("material", material, tabs));
                
                StringBuilder ke = new StringBuilder("");
                for(int i=0; i<keywords.size(); i++) {
                    ke.append(keywords.get(i).toString(t));
                }
                s.append((ke.toString()));
                
                StringBuilder im = new StringBuilder("");
                for(int i=0; i<images.size(); i++) {
                    im.append(images.get(i).toString(t));
                }
                s.append(im.toString());
                s.append(" </photograph>\n");
            }
            
            
            // ===== JSON =====
            else if("json".equals(t)) {
                String tabs = "  ";
                s.append(" {\n");
                s.append(makeJSONObject("author", author, tabs)).append(",\n");
                s.append(makeJSONObject("title", title, tabs)).append(",\n");
                s.append(makeJSONObject("type", type, tabs)).append(",\n");
                s.append(makeJSONObject("date", date, tabs)).append(",\n");
                s.append(makeJSONObject("keeper", keeper, tabs)).append(",\n");
                s.append(makeJSONObject("material", material, tabs)).append(",\n");
                
                s.append("  \"keywords\":[\n");
                StringBuilder ke = new StringBuilder("");
                for(int i=0; i<keywords.size(); i++) {
                    ke.append(keywords.get(i).toString(t));
                    if(i<keywords.size()-1) ke.append(",");
                    ke.append("\n");
                }
                s.append((ke.toString()));
                s.append("  ],\n");
                
                s.append("  \"images\":[\n");
                StringBuilder im = new StringBuilder("");
                for(int i=0; i<images.size(); i++) {
                    im.append(images.get(i).toString(t));
                    if(i<images.size()-1) im.append(",");
                    im.append("\n");
                }
                s.append(im.toString());
                s.append("  ]\n"); // images
                s.append(" }\n"); // photograph
            }
            
            
            // ===== OAI DUBLIN CORE =====
            else if("oai-dc".equals(t)) {
                String tabs = "  ";
                // s.append("<?xml version=\"1.0\"?>\n");
                s.append("<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n");
                s.append(makeXMLElement("dc:identifier", title, tabs));
                s.append(makeXMLElement("dc:title", title, tabs));
                s.append(makeXMLElement("dc:creator", author, tabs));
                s.append(makeXMLElement("dc:type", type, tabs));
                s.append(makeXMLElement("dc:date", date, tabs));
                s.append(makeXMLElement("dc:language", "fin", tabs));
                s.append(makeXMLElement("dc:rights", "http://creativecommons.org/licenses/by/4.0/", tabs));
                s.append(makeXMLElement("dc:source", "Finnish National Gallery", tabs));
                s.append(makeXMLElement("dc:source", keeper, tabs));
                //s.append(makeXMLElement("material", material, tabs));
                
                StringBuilder ke = new StringBuilder("");
                for(int i=0; i<keywords.size(); i++) {
                    ke.append(keywords.get(i).toString(t));
                }
                s.append((ke.toString()));
                
                StringBuilder im = new StringBuilder("");
                for(int i=0; i<images.size(); i++) {
                    im.append(images.get(i).toString(t));
                }
                s.append(im.toString());
                s.append("</oai_dc:dc>\n");
            }
            
            
            // ===== LIDO =====
            else if("lido".equals(t)) {
                if(title == null || title.length() == 0) title = ""+System.currentTimeMillis()+"-"+Math.floor(Math.random()*9999);
                s.append("<lido:lidoWrap xmlns:lido=\"http://www.lido-schema.org\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.lido-schema.org http://www.lido-schema.org/schema/v1.0/lido-v1.0.xsd\">\n");
                
                s.append("<lido:lido>\n");
                               
                    s.append("<lido:lidoRecID lido:type=\"picture\">");
                        s.append(encodeXMLValue(title));
                    s.append("</lido:lidoRecID>\n");

                    //s.append("<category>");
                    //    s.append("<conceptID type=\"URI\">http://www.cidoc-crm.org/crm-concepts/E22</conceptID>");
                    //    s.append("<term>Man-Made Object</term>");
                    //s.append("</category>\n");

                    // --- description
                    s.append("<lido:descriptiveMetadata>");
                                       
                        // --- classification
                        s.append("<lido:objectClassificationWrap>");

                            s.append("<lido:objectWorkTypeWrap>");
                                s.append("<lido:objectWorkType>");
                                    s.append("<lido:term>valokuva</lido:term>");
                                s.append("</lido:objectWorkType>");
                            s.append("</lido:objectWorkTypeWrap>");
                            
                            s.append("<lido:classificationWrap>");
                                s.append("<lido:classification>");
                                    s.append("<lido:term lido:label=\"kuvatyyppi\">valokuva</lido:term>");
                                s.append("</lido:classification>");
                            s.append("</lido:classificationWrap>");

                        s.append("</lido:objectClassificationWrap>");

                        
                        // --- relations
                        s.append("<lido:objectRelationWrap>");
                            
                            s.append("<lido:subjectWrap>");
                                s.append("<lido:subjectSet>");
                                    s.append("<lido:subject>");
                                        s.append("<lido:subjectConcept>");
                                            s.append("<lido:term lido:label=\"asiasana\">valokuva</lido:term>");
                                        s.append("</lido:subjectConcept>");
                                    s.append("</lido:subject>");
                                    s.append("<lido:subject>");
                                        s.append("<lido:subjectConcept>");
                                            s.append("<lido:term lido:label=\"asiasana\">Hugo Simberg</lido:term>");
                                        s.append("</lido:subjectConcept>");
                                    s.append("</lido:subject>");
                                s.append("</lido:subjectSet>");                               
                            s.append("</lido:subjectWrap>");
                            
                            s.append("<lido:relatedWorksWrap>");
                                s.append("<lido:relatedWorkSet>");
                                    s.append("<lido:relatedWork>");
                                        s.append("<lido:displayObject>Hugo Simbergin arkisto</lido:displayObject>");
                                    s.append("</lido:relatedWork>");
                                    s.append("<lido:relatedWorkRelType>");
                                        s.append("<lido:term>Arkisto</lido:term>");
                                    s.append("</lido:relatedWorkRelType>");
                                s.append("</lido:relatedWorkSet>");
                                
                                s.append("<lido:relatedWorkSet>");
                                    s.append("<lido:relatedWork>");
                                        s.append("<lido:object>");
                                            s.append("<lido:objectWebResource>");
                                                String f = title;
                                                f = f.replaceAll(" ", "%20");
                                                s.append("http://www.lahteilla.fi/simberg/#gallery/0/"+f);
                                            s.append("</lido:objectWebResource>");
                                        s.append("</lido:object>");
                                    s.append("</lido:relatedWork>");
                                s.append("</lido:relatedWorkSet>");
                                
                            s.append("</lido:relatedWorksWrap>");

                        s.append("</lido:objectRelationWrap>");
                    
                        
                        // --- identification
                        s.append("<lido:objectIdentificationWrap>");

                            s.append("<lido:titleWrap>");
                                s.append("<lido:titleSet>");
                                    s.append("<lido:appellationValue>");
                                        s.append(encodeXMLValue(title));
                                    s.append("</lido:appellationValue>");
                                s.append("</lido:titleSet>");
                            s.append("</lido:titleWrap>");


                            s.append("<lido:repositoryWrap>");
                                s.append("<lido:repositorySet>");
                                    s.append("<lido:repositoryName>");
                                        s.append("<lido:legalBodyName>");
                                            s.append("<lido:appellationValue lido:label=\"Museo/Arkisto\">Kansallisgalleria/Arkisto ja kirjasto</lido:appellationValue>");
                                        s.append("</lido:legalBodyName>");
                                        s.append("<lido:legalBodyWeblink lido:label=\"Www-osoite\">http://www.kansallisgalleria.fi</lido:legalBodyWeblink>");
                                    s.append("</lido:repositoryName>");
                                s.append("</lido:repositorySet>");
                            s.append("</lido:repositoryWrap>");

                            if(keywords != null && keywords.size() > 0) {
                                s.append("<lido:objectDescriptionWrap>");
                                    s.append("<lido:objectDescriptionSet>");
                                        s.append("<lido:descriptiveNoteValue>");
                                            StringBuilder ke = new StringBuilder("");
                                            for(int i=0; i<keywords.size(); i++) {
                                                ke.append(keywords.get(i).toString(t));
                                            }
                                            s.append((ke.toString()));
                                        s.append("</lido:descriptiveNoteValue>");
                                    s.append("</lido:objectDescriptionSet>");
                                s.append("</lido:objectDescriptionWrap>");
                            }

                        s.append("</lido:objectIdentificationWrap>");

                        
                        
                        // --- events
                        s.append("<lido:eventWrap>");
                            s.append("<lido:eventSet>");
                                s.append("<lido:event>");
                                    s.append("<lido:eventType>");
                                        s.append("<lido:term>valmistus</lido:term>");
                                    s.append("</lido:eventType>");
                                    String authorString = (author != null && author.length() > 0 ? author : "Tuntematon");
                                    s.append("<lido:eventActor>");
                                        s.append("<lido:actorInRole>");
                                            s.append("<lido:actor>");
                                                s.append("<lido:nameActorSet>");
                                                    s.append("<lido:appellationValue>"+encodeXMLValue(authorString)+"</lido:appellationValue>");
                                                s.append("</lido:nameActorSet>");
                                            s.append("</lido:actor>");
                                            s.append("<lido:roleActor><lido:term>valokuvaaja</lido:term></lido:roleActor>");
                                        s.append("</lido:actorInRole>");
                                    s.append("</lido:eventActor>");
                                 s.append("</lido:event>");
                            s.append("</lido:eventSet>");

                            s.append("<lido:eventSet>");
                                s.append("<lido:event>");
                                    s.append("<lido:eventType>");
                                        s.append("<lido:term>valokuvaus</lido:term>");
                                    s.append("</lido:eventType>");
                                    if(author != null && author.length() > 0) {
                                        s.append("<lido:eventActor>");
                                            s.append("<lido:actorInRole>");
                                                s.append("<lido:actor>");
                                                    s.append("<lido:nameActorSet>");
                                                        s.append("<lido:appellationValue lido:label=\"Valokuvaaja\">"+encodeXMLValue(author)+"</lido:appellationValue>");
                                                    s.append("</lido:nameActorSet>");
                                                s.append("</lido:actor>");
                                                s.append("<lido:roleActor><lido:term>valokuvaaja</lido:term></lido:roleActor>");
                                            s.append("</lido:actorInRole>");
                                        s.append("</lido:eventActor>");
                                    }
                                    if(date != null && date.length() > 0) {
                                        s.append("<lido:eventDate>");
                                            s.append("<lido:displayDate lido:label=\"Valmistusaika\">"+encodeXMLValue(date)+"</lido:displayDate>");
                                        s.append("</lido:eventDate>");
                                    }
                                    if(material != null && material.length() > 0) {
                                        s.append("<lido:eventMaterialsTech>");
                                            s.append("<lido:displayMaterialsTech lido:label=\"Materiaali\">"+encodeXMLValue(material)+"</lido:displayMaterialsTech>");
                                        s.append("</lido:eventMaterialsTech>");
                                    }
                                 s.append("</lido:event>");
                            s.append("</lido:eventSet>");
                        s.append("</lido:eventWrap>");

                    s.append("</lido:descriptiveMetadata>\n");
                
                    // ----- administrative
                    s.append("<lido:administrativeMetadata>");
                        s.append("<lido:rightsWorkWrap>");
                            s.append("<lido:rightsWorkSet>");
                                s.append("<lido:creditLine>Kansallisgalleria</lido:creditLine>");
                            s.append("</lido:rightsWorkSet>");
                        s.append("</lido:rightsWorkWrap>");
                            
                        s.append("<lido:recordWrap>");
                            s.append("<lido:recordID lido:type=\"local\" lido:label=\"Sis&#xE4;inen ID-numero\">");
                                s.append(encodeXMLValue(title));
                            s.append("</lido:recordID>");

                            s.append("<lido:recordType>");
                                s.append("<lido:term>valokuva</lido:term>");
                            s.append("</lido:recordType>");

                            s.append("<lido:recordSource>");
                                s.append("<lido:legalBodyName>");
                                    s.append("<lido:appellationValue>Kansallisgalleria</lido:appellationValue>");
                                s.append("</lido:legalBodyName>");
                                s.append("<lido:legalBodyWeblink lido:label=\"Www-osoite\">http://www.kansallisgalleria.fi</lido:legalBodyWeblink>");
                            s.append("</lido:recordSource>");
                            
                            s.append("<lido:recordRights>");
                                s.append("<lido:rightsType>");
                                    s.append("<lido:conceptID type=\"Copyright\">CC BY 4.0</lido:conceptID>");
                                    s.append("<lido:term>CC BY 4.0</lido:term>");
                                s.append("</lido:rightsType>");
                            s.append("</lido:recordRights>");
                            
                            s.append("<lido:recordRights>");
                                s.append("<lido:rightsHolder>");
                                    s.append("<lido:legalBodyName>");
                                        s.append("<lido:appellationValue>Kansallisgalleria</lido:appellationValue>");
                                    s.append("</lido:legalBodyName>");
                                s.append("</lido:rightsHolder>");
                            s.append("</lido:recordRights>");
                            
                        s.append("</lido:recordWrap>");

                        s.append("<lido:resourceWrap>");
                            StringBuilder im = new StringBuilder("");
                            for(int i=0; i<images.size(); i++) {
                                if("positive".equalsIgnoreCase(images.get(i).type)) {
                                    im.append(images.get(i).toString(t));
                                }
                            }
                            for(int i=0; i<images.size(); i++) {
                                if(!"positive".equalsIgnoreCase(images.get(i).type)) {
                                    im.append(images.get(i).toString(t));
                                }
                            }
                            s.append(im.toString());
                        s.append("</lido:resourceWrap>");

                    s.append("</lido:administrativeMetadata>\n");
               
                s.append("</lido:lido>\n");
                s.append("</lido:lidoWrap>\n");
            }
            
            
            // ===== CSV =====
            else {
                // Column order:  "author", "title", "type", "date", "keeper", "material", "keywords", "images"
                s.append(encodeCSVString(author)).append("\t");
                s.append(encodeCSVString(title)).append("\t");
                s.append(encodeCSVString(type)).append("\t");
                s.append(encodeCSVString(date)).append("\t");
                s.append(encodeCSVString(keeper)).append("\t");
                s.append(encodeCSVString(material)).append("\t");
                
                StringBuilder ke = new StringBuilder("");
                for(int i=0; i<keywords.size(); i++) {
                    ke.append(keywords.get(i).toString(t));
                    if(i<keywords.size()-1) {
                        ke.append(",");
                    }
                }
                s.append(encodeCSVString(ke.toString())).append("\t");
                
                StringBuilder im = new StringBuilder("");
                for(int i=0; i<images.size(); i++) {
                    im.append(images.get(i).toString(t));
                    if(i<images.size()-1) {
                        im.append(",");
                    }
                }
                s.append(encodeCSVString(im.toString()));
                
                s.append("\n");
            }
            return s.toString();
        }
    }
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    public static String IMAGE_BASE_URL = "http://www.lahteilla.fi/simberg/images/";
    
    
    
    
    public class SimbergImage {
        public String key;
        public String type;
        
        public String toString(String t) {
            StringBuilder k = new StringBuilder("");
            if("xml".equals(t)) {
                if(type != null) {
                    k.append("  <image type=\"").append(type).append("\">");
                }
                else {
                    k.append("  <image>");
                }
                k.append(encodeXMLValue(key));
                k.append("</image>\n");
            }
            else if("oai-dc".equals(t)) {
                k.append("  <identifier>");
                k.append(IMAGE_BASE_URL+encodeXMLValue(key)+".jpg");
                k.append("</identifier>\n");
            }
            else if("lido".equals(t)) {
                k.append("<lido:resourceSet>");
                    k.append("<lido:resourceRepresentation>");
                        if(type != null) k.append("<lido:linkResource lido:label=\"").append(type).append("\">");
                        else k.append("<lido:linkResource>");
                            k.append(IMAGE_BASE_URL+encodeXMLValue(key)+".jpg");
                        k.append("</lido:linkResource>");
                    k.append("</lido:resourceRepresentation>");

                    k.append("<lido:rightsResource>");
                        k.append("<lido:rightsType>");
                            k.append("<lido:conceptID type=\"Copyright\">CC BY 4.0</lido:conceptID>");
                            k.append("<lido:term>CC BY 4.0</lido:term>");
                        k.append("</lido:rightsType>");

                        k.append("<lido:rightsHolder>");
                            k.append("<lido:legalBodyName>");
                                k.append("<lido:appellationValue>Kansallisgalleria</lido:appellationValue>");
                            k.append("</lido:legalBodyName>");
                        k.append("</lido:rightsHolder>");
                    k.append("</lido:rightsResource>");
                    
                k.append("</lido:resourceSet>");
            }
            else if("json".equals(t)) {
                k.append("   {\n");
                k.append("    \"image\":").append(encodeCSVString(key)).append(",\n");
                k.append("    \"type\":").append(encodeCSVString(type)).append("\n");
                k.append("   }");
            }
            else {
                k.append(key);
                if(type != null) {
                    k.append(":");
                    k.append(type);
                }
            }
            return k.toString();
        }
    }
    
    
    
    public class SimbergKeyword {
        public String key;
        public String type;
        
        public String toString(String t) {
            StringBuilder k = new StringBuilder("");
            if("xml".equals(t)) {
                if(type != null) {
                    k.append("  <keyword type=\"").append(type).append("\">");
                }
                else {
                    k.append("  <keyword>");
                }
                k.append(encodeXMLValue(key));
                k.append("</keyword>\n");
            }
            else if("oai-dc".equals(t)) {
                k.append("  <dc:subject>");
                k.append(encodeXMLValue(key));
                k.append("</dc:subject>\n");
            }
            else if("lido".equals(t)) {
                k.append(encodeXMLValue(key));
            }
            else if("json".equals(t)) {
                k.append(encodeCSVString(key));
                // k.append("   {\n");
                // k.append("    \"keyword\":").append(encodeCSVString(key));
                // if(type != null) {
                //     k.append(",\n");
                //     k.append("    \"type\":").append(encodeCSVString(type));
                // }
                // k.append("\n");
                // k.append("   }");
            }
            else {
                k.append(key);
                if(type != null) {
                    k.append(":");
                    k.append(type);
                }
            }
            return k.toString();
        }
    }
    
    
    
    // -------------------------------------------------------------------------

    
    

    public void exportLIDO(ZipOutputStream zipStream, WandoraToolLogger logger) throws TopicMapException, IOException {
        logger.log("Saving LIDO data.");

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zipStream, "UTF-8"));

        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( SimbergPhotograph d : data ) {
            if(logger.forceStop()) break;
            if(d != null) {
                String filename = d.title;
                StringBuilder sanitizedFilename = new StringBuilder("");
                for(int i=0; i<filename.length(); i++) {
                    char c = filename.charAt(i);
                    if("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0987654321_".indexOf(c) >= 0) {
                        sanitizedFilename.append(c);
                    }
                    else {
                        sanitizedFilename.append('_');
                    }
                }

                ZipEntry e = new ZipEntry("simberg_opendata_"+sanitizedFilename);
                zipStream.putNextEntry(e);
                writer.print("<?xml version=\"1.0\"?>\n");
                print(writer, d.toString("lido"));
                writer.flush();
                zipStream.closeEntry();
            }
            logger.setProgress(p++);
        }

        writer.flush();
        writer.close();
        zipStream.close();
    }
    
    
    
    
    public void exportOAIDC(ZipOutputStream zipStream, WandoraToolLogger logger) throws TopicMapException, IOException {
        logger.log("Saving OAI DC data.");

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zipStream, "UTF-8"));

        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( SimbergPhotograph d : data ) {
            if(logger.forceStop()) break;
            if(d != null) {
                ZipEntry e = new ZipEntry("simberg-opendata-oai-dc-"+d.title);
                zipStream.putNextEntry(e);
                print(writer, d.toString("oai-dc"));
                writer.flush();
                zipStream.closeEntry();
            }
            logger.setProgress(p++);
        }

        writer.flush();
        writer.close();
        zipStream.close();
    }
    
    
    
    
    public void exportXML(PrintWriter writer, WandoraToolLogger logger) throws TopicMapException {
        logger.log("Saving XML data.");

        println(writer, "<?xml version=\"1.0\"?>");
        println(writer, "<FNGOpenData name=\"Hugo Simberg Photographs\" date=\""+System.currentTimeMillis()+"\">");

        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( SimbergPhotograph d : data ) {
            if(logger.forceStop()) break;
            if(d != null) {
                print(writer, d.toString("xml"));
            }
            logger.setProgress(p++);
        }

        println(writer, "</FNGOpenData>");
        writer.flush();
        writer.close();
    }
    
    
    
    
    public void exportCSV(PrintWriter writer, WandoraToolLogger logger) throws TopicMapException {
        logger.log("Saving CSV data.");
        
        String[] columns = {
            "author", "title", "type", "date", "keeper", "material", "keywords", "images"
        };
        
        for(int i=0; i<columns.length; i++) {
            print(writer, encodeCSVString(columns[i]));
            if(i<columns.length-1) print(writer, "\t");
        }
        print(writer, "\n");
        

        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( SimbergPhotograph d : data ) {
            if(logger.forceStop()) break;
            if(d != null) {
                print(writer, d.toString("csv"));
            }
            logger.setProgress(p++);
        }
        
        writer.flush();
        writer.close();
    }
    
    
    
    public void exportJSON(PrintWriter writer, WandoraToolLogger logger) throws TopicMapException {
        logger.log("Saving JSON data.");

        int n = data.size();
        logger.setProgressMax(n);
        int p = 0;
        logger.setProgress(p);
        
        println(writer, "{");
        println(writer, "\"photographs\": [");
        for( SimbergPhotograph d : data ) {
            if(logger.forceStop()) break;
            if(d != null) {
                print(writer, d.toString("json"));
            }
            logger.setProgress(p++);
            if(p < n) println(writer, ",");
        }
        println(writer, "]");
        println(writer, "}");
        writer.flush();
        writer.close();
    }
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // Elementary print methods are used to ensure output is ISO-8859-1
    
    protected void print(PrintWriter writer, String str) {
        writer.print(str);
    }
    protected void println(PrintWriter writer, String str) {
        writer.println(str);
    }
    
    // -------------------------------------------------------------------------
    
    protected String makeJSONObject(String key, String value, String tabs) {
        StringBuilder json = new StringBuilder("");
        if(tabs != null) json.append(tabs);
        json.append("\"").append(key).append("\":");
        json.append(encodeJSONString(value));
        return json.toString();
    }
    
    
    
    protected String makeXMLElement(String element, String value, String tabs) {
        StringBuilder xml = new StringBuilder("");
        if(value != null) {
            if(tabs != null) xml.append(tabs);
            xml.append("<").append(element).append(">");
            xml.append(encodeXMLValue(value));
            xml.append("</").append(element).append(">\n");
        }
        return xml.toString();
    }
    
    
    protected String encodeXMLValue(String str) {
        if(str != null) {
            str = str.replace("&", "&amp;");
            str = str.replace("<", "&lt;");
            str = str.replace(">", "&gt;");
        }
        return str;
    }
    
    
    
    protected String encodeCSVString(String str) {
        if(str == null) return "\"\"";
        else {
            str = str.replace("\"", "\"\"");
            str = str.replace("\n", "");
            return "\""+str+"\"";
        }
    }
    

    
    protected String encodeJSONString(String string) {
        if(string == null || string.length() == 0) {
             return "\"\"";
         }

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         sb.append("\"");
         
         String       t;

         for(i = 0; i < len; i += 1) {
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
                 if(c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         
         sb.append("\"");
         return sb.toString();
    }
    
    
    
    
}
