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
 * GOAImport.java
 *
 * Created on 22. elokuuta 2007, 19:17
 *
 */


package org.wandora.application.tools.importers;


import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.io.*;
import javax.swing.*;


/**
 * Gene Ontology Annotation file format import.
 * 
 * See http://www.geneontology.org/GO.format.annotation.shtml
 *
 * @author akivela
 */
public class GOAImport extends AbstractImportTool implements WandoraTool {
    
    
    public GOAImport() {
    }
    
    @Override
    public String getName() {
        return "GOA import";
    }
    @Override
    public String getDescription() {
        return "Import Gene Ontology Annotation file, convert file to a topic map and merge it to current layer.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_goa.png");
    }


    @Override
    public void importStream(Wandora wandora, String streamName, InputStream inputStream) {
        try {
            try {
                wandora.getTopicMap().clearTopicMapIndexes();
            }
            catch(Exception e) {
                log(e);
            }
            
            TopicMap map = null;
            if(directMerge) {
                map = solveContextTopicMap(wandora, getContext());
            }
            else {
                map = new org.wandora.topicmap.memory.TopicMapImpl();
            }
            
            importGOA(inputStream, map);
            
            if(!directMerge) {
                if(newLayer) {
                    createNewLayer(map, streamName, wandora);
                }
                else {
                    log("Merging '" + streamName + "'.");
                    solveContextTopicMap(wandora, getContext()).mergeIn(map);
                }
            }
        }
        catch(TopicMapReadOnlyException tmroe) {
            log("Topic map is write protected. Import failed.");
        }
        catch(Exception e) {
            log("Reading '" + streamName + "' failed!", e);
        }
    }
    
    
    
    
    public void importGOA(InputStream in, TopicMap map) {
        if(in != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            long startTime = System.currentTimeMillis();
            GOAParser goaParser = new GOAParser(reader, map, this);
            goaParser.parse();
            long endTime = System.currentTimeMillis();
            long importTime = Math.round((endTime-startTime)/1000);
            if(importTime > 1) log("Import took "+importTime+" seconds.");
        }
    }
    
    

    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case FILE_DIALOG_TITLE_TEXT: {
                return "Select Gene Ontology Annotation file to import";
            }
            case URL_DIALOG_MESSAGE_TEXT: {
                return "Type internet address of a Gene Ontology Annotation document to be imported";
            }
        }
        return "";
    }
    
    
    
    
        
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- OBO PARSER ---
    // -------------------------------------------------------------------------
    
    
    
    
    
    /**
     *  OBOParser class implements parser for OBO flat file format. OBOImport
     *  uses the parser to convert OBO flat files to Topic Maps.
     */
    public class GOAParser {
        
        public static final String SCHEMA_SI  = "http://wandora.org/si/goa/schema";
        
        public static final String GOA_SI = "http://wandora.org/si/goa/";
        public static final String GOA_OBJECT_SI = GOA_SI + "object";
        public static final String GOA_DATABASE_SI = GOA_SI + "db";
        public static final String GOA_SYMBOL_SI = GOA_SI + "symbol";
        public static final String GOA_QUALIFIER_SI = GOA_SI + "qualifier";
        public static final String GOA_REFERENCE_SI = GOA_SI + "reference";
        public static final String GOA_EVIDENCECODE_SI = GOA_SI + "reference";
        public static final String GOA_WITHORFROM_SI = GOA_SI + "withorfrom";
        public static final String GOA_ASPECT_SI = GOA_SI + "aspect";
        public static final String GOA_SYNONYM_SI = GOA_SI + "synonym";
        public static final String GOA_OBJECTTYPE_SI = GOA_SI + "object-type";
        public static final String GOA_TAXON_SI = GOA_SI + "taxon";
        public static final String GOA_DATE_SI = GOA_SI + "date";
        public static final String GOA_ASSIGNEDBY_SI = GOA_SI + "assigned-by";
        public static final String GOA_ORDER_SI = GOA_SI + "order";
        
        public boolean debug = true;
        private TopicMap tm;
        private GOAImport parent;
        private BufferedReader in;
        
        
        private Topic root = null;
        private Topic wandoraClass = null;
        
        
        
        /**
         * Constructor for GOAParser.
         * 
         * @param in is the BufferedReader for GOA formatted file.
         * @param tm is the Topic Map object where conversion is stored.
         * @param parent is the GOAImport callback interface.
         */
        public GOAParser(BufferedReader in, TopicMap tm, GOAImport parent) {
            this.tm=tm;
            this.parent=parent;
            this.in=in;
            
            initializeTopicMap(tm);
        }
        
        
        /**
         * Creates frequently used topics into the Topic Map before
         * conversion starts.
         * 
         * @param tm is the Topic Map where initialization is targeted.
         */
        public void initializeTopicMap(TopicMap tm) {
            wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
            getOrCreateTopic(XTMPSI.getLang(OBO.LANG));
            getOrCreateTopic(XTMPSI.DISPLAY, "Scope Display");
        }
        
        
        public void parse() {
            parent.setProgressMax(1000);
            try {
                int lineCounter = 0;
                String line = in.readLine();
                String[] fields = null;
                while(line!=null && !parent.forceStop()) {
                    if(!line.startsWith("!")) {
                        parent.setProgress(lineCounter++);
                        fields = line.split("\t");
                        if(fields.length > 0 && fields.length != 15) {
                            parent.log("Warning: Line "+lineCounter+" has illegal number of fields ("+fields.length+")");
                            parent.log("  Line: "+line);
                        }
                        else {
                            processFields(fields);
                        }
                    }
                    line = in.readLine();
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
        }

        
        
        
        
        private void processFields(String[] fields) {
            String db                 = fields[0];
            String objectID           = fields[1];
            String objectSymbol       = fields[2];
            String qualifiers         = fields[3];
            String goID               = fields[4];
            String references         = fields[5];
            String evidenceCode       = fields[6];
            String withOrFrom         = fields[7];
            String aspect             = fields[8];
            String objectName         = fields[9];
            String objectSynonyms     = fields[10];
            String objectType         = fields[11];
            String taxons             = fields[12];
            String date               = fields[13];
            String assignedBy         = fields[14];
            

            Topic annotationTopic = null;
            
            
            try {
                if(objectID != null && objectID.trim().length() > 0) {
                    annotationTopic = tm.createTopic();
                    annotationTopic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                    
                    Topic objectTopic = getOrCreateTopic(GOA_OBJECT_SI+"/"+objectID, objectID);
                    createAssociation("annotation-object", annotationTopic, "annotation", objectTopic, "object");
                    
                    // ***** DB *****
                    if(db != null && db.trim().length() > 0) {
                        Topic dbTopic = getOrCreateTopic(GOA_DATABASE_SI+"/"+db, db);
                        createAssociation("database-object", objectTopic, "object", dbTopic, "database");
                    }
                    else {
                        parent.log("Warning: GOA has no valid database id (field 1)");
                    }
                    
                    // ***** OBJECT SYMBOL *****
                    if(objectSymbol != null && objectSymbol.trim().length() > 0) {
                        if(!objectSymbol.equals(objectID)) {
                            objectTopic.setBaseName(objectSymbol + " ("+objectID+")");
                        }
                    }
                    else {
                        parent.log("Warning: GOA has no valid object symbol (field 3)");
                    }
                    
                    
                    // ***** QUALIFIERS *****
                    if(qualifiers != null && qualifiers.trim().length() > 0) {
                        String[] qs = null;
                        if(qualifiers.indexOf("|") != -1) {
                            qs = qualifiers.split("|");
                        }
                        else {
                            qs = new String[] { qualifiers };
                        }
                        for(int i=0; i<qs.length; i++) {
                            String q = qs[i];
                            if(q != null && q.trim().length() > 0) {
                                Topic qualifierTopic = getOrCreateTopic(GOA_QUALIFIER_SI+"/"+q, q);
                                createAssociation("annotation-qualifier", annotationTopic, "annotation", qualifierTopic, "qualifier");
                            }
                        }
                    }
                    
                    // ***** GO ID *****
                    if(goID != null && goID.trim().length() > 0) {
                        Topic goTopic = OBO.createTopicForTerm(tm, goID);
                        createAssociation("annotation-go term", annotationTopic, "annotation", goTopic, "go term");
                    }
                    else {
                        parent.log("Warning: GOA has no valid go id (field 5)");
                    }
                    
                    
                    // ***** REFERENCES *****
                    if(references != null && references.trim().length() > 0) {
                        String[] rs = null;
                        if(references.indexOf("|") != -1) {
                            rs = references.split("|");
                        }
                        else {
                            rs = new String[] { references };
                        }
                        for(int i=0; i<rs.length; i++) {
                            String r = rs[i];
                            if(r != null && r.trim().length() > 0) {
                                Topic refTopic = getOrCreateTopic(GOA_REFERENCE_SI+"/"+r, r);
                                createAssociation("annotation-references", annotationTopic, "annotation", refTopic, "reference");
                            }
                        }
                    }
                    else {
                        parent.log("Warning: GOA has no valid references (field 6)");
                    }
                    
                    
                    // ***** EVIDENCE CODE *****
                    if(evidenceCode != null && evidenceCode.trim().length() > 0) {
                        Topic evidenceTopic = getOrCreateTopic(GOA_EVIDENCECODE_SI+"/"+evidenceCode, evidenceCode);
                        createAssociation("annotation-evidence", annotationTopic, "annotation", evidenceTopic, "evidence-code");
                    }
                    else {
                        parent.log("Warning: GOA has no valid evidence code (field 7)");
                    }
                    
                    
                    // ***** WITH (OR) FROM *****
                    if(withOrFrom != null && withOrFrom.trim().length() > 0) {
                        String[] p = null;
                        if(withOrFrom.indexOf("|") != -1) {
                            p = withOrFrom.split("|");
                        }
                        else {
                            p = new String[] { withOrFrom };
                        }
                        for(int i=0; i<p.length; i++) {
                            String r = p[i];
                            if(r != null && r.trim().length() > 0) {
                                Topic withOrFromTopic = getOrCreateTopic(GOA_WITHORFROM_SI+"/"+r, r);
                                createAssociation("annotation-evidence-addons", annotationTopic, "annotation", withOrFromTopic, "evidence-addon");
                            }
                        }
                    }

                    // ***** ASPECT *****
                    if(aspect != null && aspect.trim().length() > 0) {
                        Topic aspectTopic = getOrCreateTopic(GOA_ASPECT_SI+"/"+aspect, aspect);
                        createAssociation("annotation-aspect", annotationTopic, "annotation", aspectTopic, "aspect");
                    }
                    else {
                        parent.log("Warning: GOA has no valid aspect (field 9)");
                    }
                    
                    
                    // ***** OBJECT NAME *****
                    if(objectName != null && objectName.trim().length() > 0) {
                        objectTopic.setDisplayName("en", objectName);
                    }

                    // ***** SYNONYMS *****
                    if(objectSynonyms != null && objectSynonyms.trim().length() > 0) {
                        String[] ss = null;
                        if(objectSynonyms.indexOf("|") != -1) {
                            ss = objectSynonyms.split("|");
                        }
                        else {
                            ss = new String[] { objectSynonyms };
                        }
                        for(int i=0; i<ss.length; i++) {
                            String s = ss[i];
                            if(s != null && s.trim().length() > 0) {
                                Topic synonymTopic = getOrCreateTopic(GOA_SYNONYM_SI+"/"+s, s);
                                createAssociation("object-synonym", objectTopic, "object", synonymTopic, "synonym");
                            }
                        }
                    }
                    
                    // ***** OBJECT TYPE *****
                    if(objectType != null && objectType.trim().length() > 0) {
                        Topic objectTypeTopic = getOrCreateTopic(GOA_OBJECTTYPE_SI+"/"+objectType, objectType);
                        createAssociation("object-type", objectTopic, "object", objectTypeTopic, "type");
                    }
                    else {
                        parent.log("Warning: GOA has no valid object type (field 12)");
                    }
                    
                    
                    // ***** TAXONS *****
                    if(taxons != null && taxons.trim().length() > 0) {
                        String[] ts = null;
                        if(objectSynonyms.indexOf("|") != -1) {
                            ts = taxons.split("|");
                        }
                        else {
                            ts = new String[] { taxons };
                        }
                        for(int i=0; i<ts.length; i++) {
                            String t = ts[i];
                            if(t != null && t.trim().length() > 0) {
                                Topic taxonTopic = getOrCreateTopic(GOA_TAXON_SI+"/"+t, t);
                                if(ts.length > 1) {
                                    Topic orderTopic = getOrCreateTopic(GOA_ORDER_SI+"/"+i, ""+i);
                                    createAssociation("annotation-taxon", annotationTopic, "annotation", taxonTopic, "taxon", orderTopic, "order");
                                }
                                else {
                                    createAssociation("annotation-taxon", annotationTopic, "annotation", taxonTopic, "taxon");
                                }
                            }
                        }
                    }
                    else {
                        parent.log("Warning: GOA has no valid taxon (field 13)");
                    }
                    
                    
                    // ***** DATE *****
                    if(date != null && date.trim().length() > 0) {
                        Topic dateTopic = getOrCreateTopic(GOA_DATE_SI+"/"+date, date);
                        createAssociation("annotation-date", annotationTopic, "annotation", dateTopic, "date");
                    }
                    else {
                        parent.log("Warning: GOA has no valid date (field 14)");
                    }
                    
                    
                    // ***** ASSIGNED BY *****
                    if(assignedBy != null && assignedBy.trim().length() > 0) {
                        Topic assignedByTopic = getOrCreateTopic(GOA_ASSIGNEDBY_SI+"/"+assignedBy, assignedBy);
                        createAssociation("annotation-assigned-by", annotationTopic, "annotation", assignedByTopic, "assigned-by");
                    }
                    else {
                        parent.log("Warning: GOA has no valid assigned by field (field 15)");
                    }
                }
                else {
                    parent.log("Warning: GOA has no valid database object id (field 1). Rejecting GOA.");
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            
        }
        
        
        // ---------------------------------------------------------------------
        // ---------------------------------------------- TOPIC MAP HELPERS ----
        // ---------------------------------------------------------------------
        
        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3) throws TopicMapException {
            Topic associationTypeTopic = createTopicForSchemaTerm(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic associationTypeTypeTopic = createSchemaTypeTopic(tm);
            associationTypeTopic.addType(associationTypeTypeTopic);
            Topic role1Topic = createTopicForSchemaTerm(tm,role1);
            Topic role2Topic = createTopicForSchemaTerm(tm,role2);
            Topic role3Topic = createTopicForSchemaTerm(tm,role3);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            association.addPlayer(player3Topic, role3Topic);
            player1Topic.addType(role1Topic);
            player2Topic.addType(role2Topic);
            player3Topic.addType(role3Topic);
            return association;
        }
        
        
        
        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2) throws TopicMapException {
            Topic associationTypeTopic = createTopicForSchemaTerm(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic associationTypeTypeTopic = createSchemaTypeTopic(tm);
            associationTypeTopic.addType(associationTypeTypeTopic);
            Topic role1Topic = createTopicForSchemaTerm(tm,role1);
            Topic role2Topic = createTopicForSchemaTerm(tm,role2);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            player1Topic.addType(role1Topic);
            player2Topic.addType(role2Topic);
            return association;
        }
        
        
        
        
        public Topic createSchemaTypeTopic(TopicMap tm) throws TopicMapException {
            String si = SCHEMA_SI;
            Topic typeTopic = tm.getTopic(si);
            if(typeTopic == null) {
                typeTopic = getOrCreateTopic(si, "goa-schema");
                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                makeSubclassOf(typeTopic, wandoraClass);
            }
            return typeTopic;
        }
        
        
        public Topic createTopicForSchemaTerm(TopicMap tm, String schemaTerm) throws TopicMapException {
            if(schemaTerm == null) return null;
            String si = SCHEMA_SI+"/"+schemaTerm;

            Topic schemaTermTopic = tm.getTopic(si);
            if(schemaTermTopic == null) {
                schemaTermTopic = getOrCreateTopic(new Locator(TopicTools.cleanDirtyLocator(si)), schemaTerm);
                Topic schemaType = createSchemaTypeTopic(tm);
                schemaTermTopic.addType(schemaType);
                // Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
                // setData(schemaTermTopic, idType, LANG, schemaTerm);
            }
            return schemaTermTopic;
        }

        
        private Topic getOrCreateTopic(String si, String basename) {
            return getOrCreateTopic(new Locator(si), basename);
        }
        
        private Topic getOrCreateTopic(String si) {
            return getOrCreateTopic(new Locator(si), null);
        }
        

        private Topic getOrCreateTopic(Locator si, String basename) {
            if(tm == null) return null;
            Topic topic = null;
            try {
                topic = tm.getTopic(si);
                if(topic == null) {
                    topic = tm.createTopic();
                    topic.addSubjectIdentifier(si);
                    if(basename != null) topic.setBaseName(basename);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            return topic;
        }

        
        
        
        private void setData(Topic t, Topic type, String lang, String text) throws TopicMapException {
            if(t != null & type != null && lang != null && text != null) {
                String langsi=XTMPSI.getLang("en");
                Topic langT=t.getTopicMap().getTopic(langsi);
                if(langT == null) {
                    langT = t.getTopicMap().createTopic();
                    langT.addSubjectIdentifier(new Locator(langsi));
                    try {
                        langT.setBaseName("Language " + lang.toUpperCase());
                    }
                    catch (Exception e) {
                        langT.setBaseName("Language " + langsi);
                    }
                }
                t.setData(type, langT, text);
            }
        }
        
        
        
        private void makeSubclassOf(Topic t, Topic superclass) {
            try {
                Topic supersubClassTopic = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS, OBO.SCHEMA_TERM_SUPERCLASS_SUBCLASS);
                Topic subclassTopic = getOrCreateTopic(XTMPSI.SUBCLASS, OBO.SCHEMA_TERM_SUBCLASS);
                Topic superclassTopic = getOrCreateTopic(XTMPSI.SUPERCLASS, OBO.SCHEMA_TERM_SUPERCLASS);
                Association ta = tm.createAssociation(supersubClassTopic);
                ta.addPlayer(t, subclassTopic);
                ta.addPlayer(superclass, superclassTopic);
            }
            catch(Exception e) {
                parent.log(e);
            }
        }
        
        
        
    }
       

}
