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
package org.wandora.application.tools.fng.opendata.simberg;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.wandora.application.WandoraToolLogger;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.Options;
import org.wandora.utils.velocity.GenericVelocityHelper;

/**
 *
 * @author akivela
 */


public class SimbergOpenDataHandler {
    
    ArrayList<SimbergPhotograph> data = new ArrayList();

    
    
    public SimbergOpenDataHandler(TopicMap tm, WandoraToolLogger logger) {
        try {
            make(tm, logger);
        }
        catch(Exception e){
            if(logger != null) logger.log(e);
            else e.printStackTrace();
        }
    }
    
    
    

    public static final String VALOKUVA_SI = "http://www.muusa.net/Valokuva";
    
    public static final String IS_ABOUT_SI = "http://www.muusa.net/P129.is_about";
    public static final String IS_ABOUT_ROLE_SI = "http://www.muusa.net/P129.is_about_role_0";
    
    public static final String AUTHOR_SI = "http://www.muusa.net/P14.Production_carried_out_by";
    public static final String AUTHOR_ROLE_SI = "http://www.muusa.net/P14.Production_carried_out_by_role_1";

    public static final String TYPE_SI = "http://www.muusa.net/P2.has_type";
    public static final String TYPE_ROLE_SI = "http://www.muusa.net/P2.has_type_role_0";

    public static final String MATERIAL_SI = "http://www.muusa.net/P45.consists_of";
    public static final String MATERIAL_ROLE_SI = "http://www.muusa.net/P45.consists_of_role_0";

    public static final String TIME_SI = "http://www.muusa.net/P4.1.Production_has_time-span";
    public static final String TIME_ROLE_SI = "http://www.muusa.net/P4.1.Production_has_time-span_role_1";
    
    public static final String KEEPER_SI = "http://www.muusa.net/P50.has_current_keeper";
    public static final String KEEPER_ROLE_SI = "http://www.muusa.net/P50.has_current_keeper_role_1";
    
    public static final String IMAGE_SI = "http://www.muusa.net/P62.depicts";
    public static final String IMAGE_TYPE_ROLE_SI = "http://www.muusa.net/P62.depicts_role_2";
    public static final String IMAGE_ROLE_SI = "http://www.muusa.net/Source";
    
    
    
    
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
                            if("http://www.muusa.net/P62_1_mode_of_depiction_kuvan_originaali".equals(imageTypeSI)) {
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
        public ArrayList<SimbergImage> images = new ArrayList();
        public ArrayList<SimbergKeyword> keywords = new ArrayList();
        
        
        
        
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
                s.append(makeXMLElement("dc:source", "fng", tabs));
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
                
                s.append("<lido>\n");
                               
                    s.append("<lidoRecID type=\"NDL\">");
                        s.append(title);
                    s.append("</lidoRecID>\n");

                    s.append("<category>");
                        s.append("<conceptID type=\"URI\">http://www.cidoc-crm.org/crm-concepts/E22</conceptID>");
                        s.append("<term>Man-Made Object</term>");
                    s.append("</category>\n");

                    // --- description
                    s.append("<descriptiveMetadata lang=\"fi\">");

                        // --- identification
                        s.append("<objectIdentificationWrap>");

                            s.append("<titleWrap>");
                                s.append("<titleSet>");
                                    s.append("<appellationValue>");
                                        s.append(title);
                                    s.append("</appellationValue>");
                                s.append("</titleSet>");
                            s.append("</titleWrap>");


                            s.append("<repositoryWrap>");
                                s.append("<repositorySet>");
                                    s.append("<repositoryName>");
                                        s.append("<legalBodyName>");
                                            s.append("<appellationValue label=\"Museo/Osasto/Haltija\">Suomen Kansallisgalleria</appellationValue>");
                                        s.append("</legalBodyName>");
                                        s.append("<legalBodyWeblink label=\"Www-osoite\">http://www.fng.fi</legalBodyWeblink>");
                                    s.append("</repositoryName>");
                                s.append("</repositorySet>");
                            s.append("</repositoryWrap>");

                        s.append("</objectIdentificationWrap>");



                        // --- classification
                        s.append("<objectClassificationWrap>");
                            if(type != null && type.length() > 0) {
                                s.append("<objectWorkType>");
                                    s.append("<term>");
                                        s.append(type);
                                    s.append("</term>");
                                s.append("</objectWorkType>");
                            }
                            
                            if(keywords != null && keywords.size() > 0) {
                                s.append("<classificationWrap>");
                                    StringBuilder ke = new StringBuilder("");
                                    for(int i=0; i<keywords.size(); i++) {
                                        ke.append(keywords.get(i).toString(t));
                                    }
                                    s.append((ke.toString()));
                                s.append("</classificationWrap>");
                            }
                        s.append("</objectClassificationWrap>");


                        // --- events
                        s.append("<eventWrap>");
                            s.append("<eventSet>");
                                s.append("<event>");
                                    s.append("<eventType>");
                                        s.append("<term>valokuvaus</term>");
                                    s.append("</eventType>");
                                    if(author != null && author.length() > 0) {
                                        s.append("<eventActor>");
                                            s.append("<actorInRole>");
                                                s.append("<actor>");
                                                    s.append("<nameActorSet>");
                                                        s.append("<appellationValue label=\"Valokuvaaja\">"+author+"</appellationValue>");
                                                    s.append("</nameActorSet>");
                                                s.append("</actor>");
                                                s.append("<roleActor><term>valokuvaaja</term></roleActor>");
                                            s.append("</actorInRole>");
                                        s.append("</eventActor>");
                                    }
                                    if(date != null && date.length() > 0) {
                                        s.append("<eventDate>");
                                            s.append("<displayDate label=\"Valmistusaika\">"+date+"</displayDate>");
                                        s.append("</eventDate>");
                                    }
                                    if(material != null && material.length() > 0) {
                                        s.append("<eventMaterialsTech>");
                                            s.append("<displayMaterialsTech label=\"Materiaali\">"+material+"</displayMaterialsTech>");
                                        s.append("</eventMaterialsTech>");
                                    }
                                 s.append("</event>");
                            s.append("</eventSet>");
                        s.append("</eventWrap>");

                    s.append("</descriptiveMetadata>\n");
                
                    // ----- administrative
                    s.append("<administrativeMetadata lang=\"fi\">");
                        s.append("<recordWrap>");
                            s.append("<recordID type=\"local\" label=\"Sis&#xE4;inen ID-numero\">");
                                s.append(title);
                            s.append("</recordID>");

                            s.append("<recordType>");
                                s.append("<term>valokuva</term>");
                            s.append("</recordType>");

                            s.append("<recordSource>");
                                s.append("<legalBodyWeblink label=\"Www-osoite\">http://www.fng.fi</legalBodyWeblink>");
                            s.append("</recordSource>");
                        s.append("</recordWrap>");

                        s.append("<resourceWrap>");
                            StringBuilder im = new StringBuilder("");
                            for(int i=0; i<images.size(); i++) {
                                im.append(images.get(i).toString(t));
                            }
                            s.append(im.toString());
                        s.append("</resourceWrap>");

                    s.append("</administrativeMetadata>\n");
               
                s.append("</lido>\n");
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
                k.append("<resourceSet>");
                    k.append("<resourceRepresentation>");
                        if(type != null) k.append("<linkResource label=\"").append(type).append("\">");
                        else k.append("<linkResource>");
                            k.append(IMAGE_BASE_URL+encodeXMLValue(key)+".jpg");
                        k.append("</linkResource>");
                    k.append("</resourceRepresentation>");
                k.append("</resourceSet>");
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
                k.append("<classification>");
                    k.append("<term lang=\"fi\">");
                        k.append(encodeXMLValue(key));
                    k.append("</term>");
                k.append("</classification>");
            }
            else if("json".equals(t)) {
                k.append("   ").append(encodeCSVString(key));
                // k.append("   {\n");
                // k.append("    \"keyword\":").append(encodeCSVString(key)).append(",\n");
                // k.append("    \"type\":").append(encodeCSVString(type)).append("\n");
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


    
    
    // -------------------------------------------------------------------------
    
    
    

    public void exportLIDO(PrintWriter writer, WandoraToolLogger logger) throws TopicMapException, IOException {
        logger.log("Saving LIDO data.");

        writer.print("<?xml version=\"1.0\"?>\n");
        writer.print("<lidoWrap schemaLocation=\"http://www.lido-schema.org http://www.lido-schema.org/schema/v1.0/lido-v1.0.xsd\">\n");

        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( SimbergPhotograph d : data ) {
            if(logger.forceStop()) break;
            if(d != null) {
                print(writer, d.toString("lido"));
            }
            logger.setProgress(p++);
        }

        writer.print("</lidoWrap>\n");
        writer.flush();
        writer.close();
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
