/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * OBOImport.java
 *
 * Created on 22. elokuuta 2007, 19:17
 *
 */

package org.wandora.application.tools.importers;



import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;

import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.utils.Tuples.T2;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.util.regex.*;


/**
 * OBOImport imports OBO flat file ontology, converts it to a topic map and merges 
 * the result to current topic map.
 *
 * @author akivela
 */




public class OBOImport extends AbstractImportTool implements WandoraTool {
    protected ArrayList<String> namespaces = null;
    
    
    
    /** Creates a new instance of OBOImport */
    public OBOImport() {
    }
    public OBOImport(int options) {
        setOptions(options);
    }
    

    @Override
    public void initialize(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        String o=options.get(OBO.optionPrefix+"options");
        if(o!=null){
            int i=Integer.parseInt(o);
            OBO.setOptions(i);
        }
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        //System.out.println(prefix);
        OBOConfiguration dialog=new OBOConfiguration(admin,true);
        dialog.setOptions(OBO.getOptions());
        dialog.setVisible(true);
        if(!dialog.wasCancelled()){
            int i=dialog.getOptions();
            OBO.setOptions(i);
            options.put(OBO.optionPrefix+"options",""+i);
        }
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
        options.put(OBO.optionPrefix+"options",""+OBO.getOptions());
    }    

    @Override
    public String getName() {
        return "OBO import";
    }
    @Override
    public String getDescription() {
        return "Imports OBO flat file ontology, converts it to a topic map and merges the result to current topic map.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_obo.png");
    }
    

    public ArrayList<String> getNamespaces() {
        return namespaces;
    }
    protected void addNamespace(String newNamespace) {
        if(namespaces == null) namespaces = new ArrayList<String>();
        if(!namespaces.contains(newNamespace)) {
            namespaces.add(newNamespace);
        }
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
            
            importOBO(inputStream, map);
            
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
    
    
    
    
    public void importOBO(InputStream in, TopicMap map) {
        if(in != null) {
            namespaces = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            long startTime = System.currentTimeMillis();
            OBOParser oboParser = new OBOParser(reader, map, this);
            oboParser.parse();
            long endTime = System.currentTimeMillis();
            long importTime = Math.round((endTime-startTime)/1000);
            if(importTime > 1) log("Import took "+importTime+" seconds.");
        }
    }
    
    

    

    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- OBO PARSER ---
    // -------------------------------------------------------------------------
    
    
    
    
    
    /**
     *  OBOParser class implements parser for OBO flat file format. OBOImport
     *  uses the parser to convert OBO flat files to Topic Maps.
     */
    public class OBOParser {
       

        
        public boolean debug = true;
        public int deprecationMsgLimit = 1000;
        private TopicMap tm;
        private OBOImport parent;
        private BufferedReader in;
        
        
        private Header header = null;
        private Stanza stanza = null;
        private int stanzaCounter = 0;
        
        Topic root = null;
        Topic wandoraClass = null;
        

        /**
         * Constructor for OBOParser.
         * 
         * @param in is the BufferedReader for OBO flat format file.
         * @param tm is the Topic Map object where conversion is stored.
         * @param parent is the OBOImport callback interface.
         */
        public OBOParser(BufferedReader in, TopicMap tm, OBOImport parent) {
            this.tm=tm;
            this.parent=parent;
            this.in=in;
            
            deprecationMsgLimit = 1000;
            
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
        
        
        
        
        /**
         * Starts the parse.
         */
        public void parse() {
            try {
                parent.setProgressMax(1000);
                
                stanzaCounter = 0;
                stanza = null;
                header = new Header();
                boolean headerProcessed = false;
                String line = null;
                
                String tag = null;
                String value = null;
                String comment = null;
                int splitPoint = -1;
                
                line = in.readLine();
                while(line!=null && !parent.forceStop()) {
                    line = line.trim();
                    if(line!=null && line.length()>0) {
                        if(line.startsWith("[")) {
                            String stanzaType = line.substring(1);
                            if(stanzaType.endsWith("]")) {
                                stanzaType = stanzaType.substring(0,stanzaType.length()-1);
                            }

                            if(!headerProcessed) {
                                processHeader(header);
                                headerProcessed = true;
                            }
                            if(stanza!=null) {
                                // Process previous stanza....
                                // The idea is to collect stanza lines until next stanza title is found and
                                // then just before new stanza is initialized process the collected stanza.
                                stanzaCounter++;
                                processStanza(stanza);
                                parent.setProgress(stanzaCounter);
                            }
                            
                            stanza = new Stanza(stanzaType);
                        }
                        else {
                            //System.out.println("parsing line: " + line);
                            /*
                            comment = null;
                            splitPoint = line.indexOf('!');
                            if(splitPoint > -1) {
                                comment = line.substring(splitPoint+1).trim();
                                line = line.substring(0, splitPoint);
                            }
                            */
                            comment = null;
                            String preline = null;
                            boolean precedesBackslash = true;
                            splitPoint = line.indexOf('!');
                            while(splitPoint > -1) {
                                preline = line.substring(0, splitPoint);
                                precedesBackslash = (splitPoint > 0 ? line.charAt(splitPoint-1) == '\\' : false);
                                if(!precedesBackslash) {
                                    int quoteCounter = 0;
                                    int findPoint = preline.indexOf("\"");
                                    while(findPoint > -1) {
                                        precedesBackslash = (findPoint > 0 ? line.charAt(findPoint-1) == '\\' : false);
                                        if(!precedesBackslash) quoteCounter++;
                                        findPoint = preline.indexOf("\"", findPoint+1);
                                    }
                                    if(((quoteCounter % 2) == 0)) {
                                        comment = line.substring(splitPoint+1).trim();
                                        line = preline;
                                        break;
                                    }
                                }
                                splitPoint = line.indexOf('!', splitPoint+1);
                            }
                            
                            splitPoint = line.indexOf(':');
                            if(splitPoint > -1) {
                                tag = line.substring(0, splitPoint).trim();
                                value = line.substring(splitPoint+1).trim();

                                if(stanza != null) {
                                    stanza.addTagValuePair(tag, value, comment);
                                }
                                else {
                                    header.addTagValuePair(tag, value, comment);
                                }
                            }
                        }
                    }
                    line = in.readLine();
                }
                if(stanza != null) {
                    // Process last stanza....
                    stanzaCounter++;
                    processStanza(stanza);
                }
                if(parent.forceStop()) parent.log("Import interrupted by user!");
                parent.log("Total "+stanzaCounter+" stanzas processed!");
            }
            catch(Exception e) {
                parent.log(e);
            }
            
            /*
            // **** POST PROCESS ****
            parent.log("Solving is-a root topics.");
            try {
                Topic supersubClassTopic = tm.getTopic(XTMPSI.SUPERCLASS_SUBCLASS);
                Topic subclassTopic = tm.getTopic(XTMPSI.SUBCLASS);
                Topic superclassTopic = tm.getTopic(XTMPSI.SUPERCLASS);
                
                if(supersubClassTopic != null && subclassTopic != null && superclassTopic != null) {
                    Iterator<Topic> typeIter = namespace.iterator();
                    Topic type = null;
                    while(typeIter.hasNext()) {
                        type = typeIter.next();
                        ArrayList<Topic> edges = new ArrayList<Topic>();
                        ArrayList<Topic> alone = new ArrayList<Topic>();
                        
                        if(type != null && !type.isRemoved()) {
                            Collection<Topic> instances = tm.getTopicsOfType(type);
                            Iterator<Topic> instanceIterator = instances.iterator();
                            Topic instance = null;
                            while(instanceIterator.hasNext()) {
                                instance = instanceIterator.next();
                                if(instance != null && !instance.isRemoved()) {
                                    Collection<Association> supersubAssociations = instance.getAssociations(supersubClassTopic);
                                    if(supersubAssociations.size() > 0) {
                                        if( instance.getAssociations(supersubClassTopic, superclassTopic).size() > 0 && instance.getAssociations(supersubClassTopic, subclassTopic).size() == 0) {
                                            edges.add(instance);
                                        }
                                    }
                                    else {
                                        alone.add(instance);
                                    }
                                }
                            }
                            if(edges.size() > 0) {
                                Topic isaRoot = OBO.createISARootTopic(tm, header.getDefaultNameSpace());
                                Topic t = null;
                                Iterator<Topic> edgeIterator = edges.iterator();
                                while(edgeIterator.hasNext()) {
                                    t = edgeIterator.next();
                                    if(t != null && !t.isRemoved())
                                        t.addType(isaRoot);
                                }
                            }
             * /
                            /*
                            if(alone.size() > 0) {
                                Topic typeRoot = getOrCreateTopic(type.getOneSubjectIdentifier().toExternalForm()+"/alone", type.getBaseName()+" alone");
                                Topic t = null;
                                makeSubclassOf(typeRoot, root);
                                Iterator<Topic> aloneIterator = alone.iterator();
                                while(aloneIterator.hasNext()) {
                                    t = aloneIterator.next();
                                    t.addType(typeRoot);
                                }
                            }
                             * */ /*
                        }
                    }
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
                                   * */
        }

        
        

        
        
        /**
         * Handles header of the OBO flat format file.
         * @param header
         */
        private void processHeader(Header header) {
            if(tm == null) return;
            try {
                String namespace = header.getDefaultNameSpace();
                parent.addNamespace(namespace);
                
                root = OBO.createRootTopic(tm, namespace);
                Topic headerTopic = OBO.createHeaderTopic(tm, namespace);
                makeSubclassOf(headerTopic, root);
                
                String tag = null;
                String value = null;
                ArrayList<T2<String, String>> modifiers = null;
                String comment = null;
                
                TagValuePair tagValue = null;
                ArrayList<TagValuePair> tagValuePairs = header.getTagValuePairs();
                
                for(Iterator<TagValuePair> i=tagValuePairs.iterator(); i.hasNext() && !parent.forceStop(); ) {
                    try {
                        tagValue = i.next();
                        tag = tagValue.getTag();
                        value = tagValue.getValueWithoutModifiers();
                        modifiers = tagValue.getModifiers();
                        comment = tagValue.getComment();
                        
                        if(false && debug) {
                            System.out.println("---------------------------------");
                            System.out.println("header");
                            System.out.println("tag: "+ tag);
                            System.out.println("value: "+ value);
                            System.out.println("modifiers:");
                            for(Iterator<T2<String,String>> mi=modifiers.iterator(); mi.hasNext();) {
                                T2<String,String> mo = mi.next();
                                System.out.println("   "+mo.e1+":"+mo.e2);
                            }
                            System.out.println("comment: "+ comment);
                        }
                        
                        
                        if("subsetdef".equals(tag)) {
                            Category category = new Category(value);
                            String categoryName = category.getId();
                            String description = category.getDescription();
                            Topic categoryTopic = OBO.createTopicForCategory(tm, categoryName, description);
                            Topic categoryTypeTopic = OBO.createCategoryTopic(tm, namespace);
                            categoryTopic.addType(categoryTypeTopic);
                        }
                        else {
                            if(("import".equals(tag) || "typeref".equals(tag)) && OBO.PROCESS_HEADER_IMPORTS) {
                                if("typeref".equals(tag)) this.logDeprecation(tag, "header", "import");
                                try {
                                    BufferedReader reader = null;
                                    if(value.startsWith("http:")) {
                                        URL url = new URL(value);
                                        URLConnection urlConnection = url.openConnection();
                                        Wandora.initUrlConnection(urlConnection);
                                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                                    }
                                    else {
                                        reader = new BufferedReader(new FileReader(value));
                                    }
                                    OBOParser oboParser = new OBOParser(reader, tm, parent);
                                    oboParser.parse();
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                            
                            Topic tagTopic = OBO.createTopicForSchemaTerm(tm, tag);
                            String oldData = headerTopic.getData(tagTopic, OBO.LANG);
                            if(oldData == null)
                                setData(headerTopic, tagTopic, OBO.LANG, value);
                            else
                                setData(headerTopic, tagTopic, OBO.LANG, oldData+"\n"+value);
                        }

                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
        }
        
        
        
        private void processStanza(Stanza stanza) {
            String st = stanza.getType();
            if("Term".equalsIgnoreCase(st))
                processTermStanza(stanza);
            else if("Typedef".equalsIgnoreCase(st))
                processTypedefStanza(stanza);
            else if("Instance".equalsIgnoreCase(st))
                processInstanceStanza(stanza);
            else
                parent.log("Unsupported stanza type '"+st+"' used.");
        }
        
        
        
        
        
        private void processTypedefStanza(Stanza stanza) {
            if(tm == null || stanza == null) return;
            try {
                String id = stanza.getId();
                String name = stanza.getName();
                String namespace = solveNamespace(stanza, id, header, "obo-rel");
                parent.addNamespace(namespace);
                
                Topic stanzaTopic = OBO.createTopicForTypedef(tm, id, name, namespace);

                String tag = null;
                String value = null;
                ArrayList<T2<String, String>> modifiers = null;
                String comment = null;
                
                TagValuePair tagValue = null;
                ArrayList<TagValuePair> tagValuePairs = stanza.getTagValuePairs();

                int defCount = 0;
                int commentCount = 0;
                
                for(Iterator<TagValuePair> i=tagValuePairs.iterator(); i.hasNext() && !parent.forceStop(); ) {
                    try {
                        tagValue = i.next();
                        tag = tagValue.getTag();
                        value = tagValue.getValueWithoutModifiers();
                        modifiers = tagValue.getModifiers();
                        comment = tagValue.getComment();
                        
                        if(false && debug) {
                            System.out.println("---------------------------------");
                            System.out.println("id: "+ id);
                            System.out.println("tag: "+ tag);
                            System.out.println("value: "+ value);
                            System.out.println("modifiers:");
                            for(Iterator<T2<String,String>> mi=modifiers.iterator(); mi.hasNext();) {
                                T2<String,String> mo = mi.next();
                                System.out.println("   "+mo.e1+":"+mo.e2);
                            }
                            System.out.println("comment: "+ comment);
                        }
                        
                        
                        
                        // **** PROCESS CURRENT TAG AND VALUE ****
                        if("alt_id".equals(tag)) {
                            if(OBO.MAKE_SIS_FROM_ALT_IDS) {
                                try {
                                    stanzaTopic.addSubjectIdentifier(OBO.makeLocator(value));
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                             }
                            else {
                               createAssociation(OBO.SCHEMA_TERM_ALTERNATIVE_ID, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_ALTERNATIVE_ID);
                            }
                        }
                        
                        
                        
                        // ***** XREFS *****
                        else if("xref_unknown".equals(tag)) {
                            logDeprecation(tag, id, "xref");
                            processXref(stanzaTopic, "UNKNOWN", value, null, OBO.SCHEMA_TERM_TYPEDEF);
                        }
                        else if("xref_analog".equals(tag)) {
                            logDeprecation(tag, id, "xref");
                            processXref(stanzaTopic, "ANALOG", value, null, OBO.SCHEMA_TERM_TYPEDEF);
                        }
                        else if("xref".equals(tag)) {
                            processXref(stanzaTopic, null, value, null, OBO.SCHEMA_TERM_TYPEDEF);
                        }
 
                        
                        // ********** SUPERCLASS-SUBCLASS *********
                        else if("is_a".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_SUPERCLASS_SUBCLASS, stanzaTopic, OBO.SCHEMA_TERM_SUBCLASS, OBO.createTopicForTerm(tm, value), OBO.SCHEMA_TERM_SUPERCLASS);
                        }
                        
                        
                        // ****** RELATIONSHIP ********
                        else if("relationship".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(relatedTerm.getModifier(), stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_RELATIONSHIP, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                        }
                        
                        
                        
                        // ******** SUBSET - CATEGORY *********
                        else if("subset".equals(tag)) {
                            processSubset(stanzaTopic, value);
                        }
                        
                        
                        // ******** NAMESPACE *********
                        else if("namespace".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_NAMESPACE, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_NAMESPACE);
                        }
                        

                        // ***** SYNONYMS *****
                        else if("narrow_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "NARROW", value, namespace);
                        }
                        else if("broad_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "BROAD", value, namespace);
                        }
                        else if("exact_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "EXACT", value, namespace);
                        }
                        else if("related_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "RELATED", value, namespace);
                        }
                        else if("synonym".equals(tag)) {
                            processSynonym(stanzaTopic, value, namespace);
                        }
                        
                        
                        
                        // ******** CONSIDER USING ********
                        else if("consider".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_CONSIDER_USING, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForTerm(tm, value), OBO.SCHEMA_TERM_CONSIDER_USING);
                        }
                        else if("use_term".equals(tag)) {
                            logDeprecation(tag, id, "consider");
                            createAssociation(OBO.SCHEMA_TERM_CONSIDER_USING, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForTerm(tm, value), OBO.SCHEMA_TERM_CONSIDER_USING);
                        }
                        else if("replaced_by".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_REPLACED_BY, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForTerm(tm, value), OBO.SCHEMA_TERM_REPLACED_BY);
                        }

                        // ****** OCCURRENCES ******
                        else if("def".equals(tag)) {
                            defCount++;
                            if(defCount > 1) {
                                parent.log("Error: More that one definition in '"+id+"'.");
                            }
                            else {
                                processDefinition(stanzaTopic, value, OBO.SCHEMA_TERM_TERM, namespace);
                            }
                        }
                        else if("comment".equals(tag)) {
                            commentCount++;
                            if(commentCount > 1) {
                                parent.log("Error: More that one comment in '"+id+"'.");
                            }
                            else {
                                Topic commentType = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_COMMENT);
                                setData(stanzaTopic, commentType, OBO.LANG, OBO.OBO2Java(value));
                            }
                        }

                        
                        
                        // ***** TYPEDEF SPECIFIC RELATIONS *******
                        else if("domain".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_DOMAIN, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_DOMAIN);
                        }
                        else if("range".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_RANGE, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_RANGE);
                        }
                        else if("inverse_of".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_INVERSE_OF, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_INVERSE_OF);
                        }
                        else if("is_transitive".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_TRANSITIVE, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_TRANSITIVE);
                        }
                        else if("is_transitive_over".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_TRANSITIVE_OVER, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_TRANSITIVE_OVER);
                        }
                        else if("transitive_over".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_TRANSITIVE_OVER, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_TRANSITIVE_OVER);
                        }
                        else if("is_reflexive".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_REFLEXIVE, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_REFLEXIVE);
                        }    
                        else if("is_cyclic".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_CYCLIC, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_CYCLIC);
                        }
                        else if("is_symmetric".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_SYMMETRIC, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_SYMMETRIC);
                        }
                        else if("is_anti_symmetric".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_ANTISYMMETRIC, stanzaTopic,OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_ANTISYMMETRIC);
                        }
                        else if("is_metadata_tag".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_METADATA_TAG, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_METADATA_TAG);
                        }
                        else if("is_class_level".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_CLASS_LEVEL, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_CLASS_LEVEL);
                        }
                        else if("holds_over_chain".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_HOLS_CHAIN_OVER, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_HOLS_CHAIN_OVER);
                        }
                        else if("expand_assertion_to".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_EXPAND_ASSERTION, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForAssertionExpansion(tm,value), OBO.SCHEMA_TERM_EXPAND_ASSERTION);
                        }

                        
                        // ******** IS_ANONYMOUS *********
                        else if("is_anonymous".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_ANONYMOUS, stanzaTopic, OBO.SCHEMA_TERM_TYPEDEF, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_ANONYMOUS);
                        }
                        
                        
                        // ******** OBSOLETE *********
                        else if("is_obsolete".equals(tag)) {
                            if("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
                                stanzaTopic.addType(OBO.createObsoleteTopic(tm, namespace));
                            }
                        }
                        else {
                            parent.log("Unprocessed typedef tag found!\n  tag: '"+tag+"'\n  value: '"+value+"'");
                        }
                    }
                    catch(Exception ei) {
                        parent.log("Exception while processing '"+tag+"' and value '"+value+"'.");
                        parent.log(ei);
                    }
                }
                
                
                // **** POSTPROCESS ****
                
            }
            catch(Exception e) {
                parent.log(e);
            }
            
        }
        
        

        
        
        private void processTermStanza(Stanza stanza) {
            if(tm == null || stanza == null) return;
            try {
                String id = stanza.getId();
                String name = stanza.getName();
                String namespace = solveNamespace(stanza, id, header, "obo-term");
                parent.addNamespace(namespace);
                
                Topic stanzaTopic = OBO.createTopicForTerm(tm, id, name, namespace);
                
                ArrayList<TagValuePair> tagValuePairs = stanza.getTagValuePairs();
                TagValuePair tagValue = null;
                
                String tag = null;
                String value = null;
                ArrayList<T2<String, String>> modifiers = null;
                String comment = null;
                
                int defCount = 0;
                int commentCount = 0;
                
                for(Iterator<TagValuePair> i=tagValuePairs.iterator(); i.hasNext() && !parent.forceStop(); ) {
                    try {
                        tagValue = i.next();
                        tag = tagValue.getTag();
                        value = tagValue.getValueWithoutModifiers();
                        modifiers = tagValue.getModifiers();
                        comment = tagValue.getComment();
                        
                        if(false && debug) {
                            System.out.println("---------------------------------");
                            System.out.println("id: "+ id);
                            System.out.println("tag: "+ tag);
                            System.out.println("value: "+ value);
                            System.out.println("modifiers:");
                            for(Iterator<T2<String,String>> mi=modifiers.iterator(); mi.hasNext();) {
                                T2<String,String> mo = mi.next();
                                System.out.println("   "+mo.e1+":"+mo.e2);
                            }
                            System.out.println("comment: "+ comment);
                        }
                        
                        // **** PROCESS CURRENT TAG AND VALUE ****
                        if("alt_id".equals(tag)) {
                            if(OBO.MAKE_SIS_FROM_ALT_IDS) {
                                try {
                                    stanzaTopic.addSubjectIdentifier(OBO.makeLocator(value));
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                             }
                            else {
                               createAssociation(OBO.SCHEMA_TERM_ALTERNATIVE_ID, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_ALTERNATIVE_ID);
                            }
                        }
                        
                        
                        // ***** XREFS *****
                        else if("xref_unknown".equals(tag)) {
                            logDeprecation(tag, id, "xref");
                            processXref(stanzaTopic, "UNKNOWN", value, namespace, OBO.SCHEMA_TERM_TERM);
                        }
                        else if("xref_analog".equals(tag)) {
                            logDeprecation(tag, id, "xref");
                            processXref(stanzaTopic, "ANALOG", value, namespace, OBO.SCHEMA_TERM_TERM);
                        }
                        else if("xref".equals(tag)) {
                            processXref(stanzaTopic, null, value, namespace, OBO.SCHEMA_TERM_TERM);
                        }
                        
                        

                        // ***** SYNONYMS *****
                        else if("narrow_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "NARROW", value, namespace);
                        }
                        else if("broad_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "BROAD", value, namespace);
                        }
                        else if("exact_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "EXACT", value, namespace);
                        }
                        else if("related_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "RELATED", value, namespace);
                        }
                        else if("synonym".equals(tag)) {
                            processSynonym(stanzaTopic, value, namespace);
                        }

                        

                        // ********* ISA *********
                        else if("is_a".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_SUPERCLASS_SUBCLASS, stanzaTopic, OBO.SCHEMA_TERM_SUBCLASS, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_SUPERCLASS);
                        }
                        
                        
                        
                        // ********* SUBSET *********
                        else if("subset".equals(tag)) {
                            processSubset(stanzaTopic, value);
                        }

                        
                        // ******** NAMESPACE *********
                        else if("namespace".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_NAMESPACE, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_NAMESPACE);
                        }
                        
                        
                         // ****** RELATIONSHIP ********
                        else if("relationship".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(relatedTerm.getModifier(), stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_RELATIONSHIP, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                        }
                        
                         // ****** PROPERTY_VALUE ********
                        else if("property_value".equals(tag)) {
                            processProperty(stanzaTopic, OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_TERM), value);
                        }
                        
                        // ****** INTERSECTION OF ********
                        else if("intersection_of".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(OBO.SCHEMA_TERM_INTERSECTION_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,relatedTerm.getModifier()), OBO.SCHEMA_TERM_MODIFIER);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_INTERSECTION_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                        }
                        
                        
                        
                        // ****** UNION OF ********
                        else if("union_of".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(OBO.SCHEMA_TERM_UNION_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,relatedTerm.getModifier()), OBO.SCHEMA_TERM_MODIFIER);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_UNION_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                        }
                        
                        
                        // ****** DISJOINT FROM ********
                        else if("disjoint_from".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(OBO.SCHEMA_TERM_DISJOINT_FROM, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,relatedTerm.getModifier()), OBO.SCHEMA_TERM_MODIFIER);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_DISJOINT_FROM, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,relatedTerm.getTerm()), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                        }
                        
                        
                        // ****** PART-OF *******
                        else if("part_of".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,relatedTerm.getModifier()), OBO.SCHEMA_TERM_MODIFIER);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                            }
                        }
                        else if("has_part".equals(tag)) {
                            RelatedTerm relatedTerm = new RelatedTerm(value);
                            if(relatedTerm.getTerm() != null && relatedTerm.getModifier() != null ) {
                                createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM, OBO.createTopicForModifier(tm,relatedTerm.getModifier()), OBO.SCHEMA_TERM_MODIFIER);
                            }
                            else if(relatedTerm.getTerm() != null) {
                                createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                            }
                        }
                        else if("integral_part_of".equals(tag)) {
                            logDeprecation(tag, id, "part_of");
                            createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,"INTEGRAL"), OBO.SCHEMA_TERM_MODIFIER);
                        }
                        else if("proper_part_of".equals(tag)) {
                            logDeprecation(tag, id, "part_of");
                            createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,"PROPER"), OBO.SCHEMA_TERM_MODIFIER);
                        }
                        else if("improper_part_of".equals(tag)) {
                            logDeprecation(tag, id, "part_of");
                            createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForModifier(tm,"IMPROPER"), OBO.SCHEMA_TERM_MODIFIER);
                        }
                        else if("has_improper_part".equals(tag)) {
                            logDeprecation(tag, id, "part_of");
                            createAssociation(OBO.SCHEMA_TERM_PART_OF, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM, OBO.createTopicForModifier(tm,"IMPROPER"), OBO.SCHEMA_TERM_MODIFIER);
                        }
                        
                        
                        
                        

                        else if("adjacent_to".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_ADJACENT_TO, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }                        
                        
                        
                        // ******** LOCATION_OF *******
                        else if("located_in".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_LOCATED_IN, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("location_of".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_LOCATED_IN, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        
                        // ******** CONTAINS *******
                        else if("contained_in".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_CONTAINS, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("contains".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_CONTAINS, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        
                        
                        // ******** TRANSFORMATION *******
                        else if("transformation_of".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_TRANSFORMATION_OF, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("transformed_into".equals(tag)) {
                            logDeprecation(tag, id, "transformation_of");
                            createAssociation(OBO.SCHEMA_TERM_TRANSFORMATION_OF, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        
                        // ******** DERIVES *******
                        else if("derives_from".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_DERIVES_FROM, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("derives_into".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_DERIVES_FROM, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        // ******* PRECEDES ********
                        else if("preceded_by".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_PRECEDED_BY, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("precedes".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_PRECEDED_BY, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        // ******* PARTICIPATES *******
                        else if("has_participant".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_PARTICIPATES_IN, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("participates_in".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_PARTICIPATES_IN, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        
                        // ******* AGENT-IN ******
                        else if("has_agent".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_AGENT_IN, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_RELATED_TO);
                        }
                        else if("agent_in".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_AGENT_IN, stanzaTopic, OBO.SCHEMA_TERM_RELATED_TO, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }
                        
                        
                        // ****** CONSIDER ******
                        else if("consider".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_CONSIDER_USING, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_CONSIDER_USING);
                        }
                        else if("use_term".equals(tag)) {
                            logDeprecation(tag, id, "consider");
                            createAssociation(OBO.SCHEMA_TERM_CONSIDER_USING, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_CONSIDER_USING);
                        }
                        else if("replaced_by".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_REPLACED_BY, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_REPLACED_BY);
                        }                        
                        
                        
                       

                        // ****** OCCURRENCES ******
                        else if("def".equals(tag)) {
                            defCount++;
                            if(defCount > 1) {
                                parent.log("Error: More that one definition in '"+id+"'.");
                            }
                            else {
                                processDefinition(stanzaTopic, value, OBO.SCHEMA_TERM_TERM, namespace);
                            }
                        }
                        else if("comment".equals(tag)) {
                            commentCount++;
                            if(commentCount > 1) {
                                parent.log("Error: More that one comment in '"+id+"'.");
                            }
                            else {
                                Topic commentType = OBO.createTopicForSchemaTerm(tm,"comment");
                                setData(stanzaTopic, commentType, OBO.LANG, OBO.OBO2Java(value));
                            }
                        }
                        
                        // ******** IS_ANONYMOUS *********
                        else if("is_anonymous".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_ANONYMOUS, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_ANONYMOUS);
                        }
                        
                        
                        // ******** OBSOLETE TERM *********
                        else if("is_obsolete".equals(tag)) {
                            if("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
                                stanzaTopic.addType(OBO.createObsoleteTopic(tm, namespace));
                            }
                        }
                        
                        // ******** CREATED *********
                        else if("created_by".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_CREATED_BY, stanzaTopic, OBO.SCHEMA_TERM_TERM, OBO.createTopicForAuthor(tm,value,namespace), OBO.SCHEMA_TERM_CREATED_BY);
                        }
                        else if("creation_date".equals(tag)) {
                            Topic creationDateType = OBO.createTopicForSchemaTerm(tm,"creation_date");
                            setData(stanzaTopic, creationDateType, OBO.LANG, OBO.OBO2Java(value));
                        }
                        

                        else {
                            parent.log("Unprocessed term tag found!\n  tag: '"+tag+"'\n  value: '"+value+"'");
                        }
                    }
                    catch(Exception ei) {
                        parent.log("Exception while processing '"+tag+"' and value '"+value+"'.");
                        parent.log(ei);
                    }
                }
                
                
                // **** POSTPROCESS ****
                
            }
            catch(Exception e) {
                parent.log(e);
            }
            
        }

        

        
        
        private void processInstanceStanza(Stanza stanza) {
            if(tm == null || stanza == null) return;
            try {
                String id = stanza.getId();
                String name = stanza.getName();
                String namespace = solveNamespace(stanza, id, header, "obo-instance");
                parent.addNamespace(namespace);
                
                Topic stanzaTopic = OBO.createTopicForInstance(tm,id, name, namespace);

                String tag = null;
                String value = null;
                ArrayList<T2<String, String>> modifiers = null;
                String comment = null;
                
                TagValuePair tagValue = null;
                ArrayList<TagValuePair> tagValuePairs = stanza.getTagValuePairs();

                int commentCount = 0;
                
                for(Iterator<TagValuePair> i=tagValuePairs.iterator(); i.hasNext() && !parent.forceStop(); ) {
                    try {
                        tagValue = i.next();
                        tag = tagValue.getTag();
                        value = tagValue.getValueWithoutModifiers();
                        modifiers = tagValue.getModifiers();
                        comment = tagValue.getComment();
                        
                        if(false && debug) {
                            System.out.println("---------------------------------");
                            System.out.println("id: "+ id);
                            System.out.println("tag: "+ tag);
                            System.out.println("value: "+ value);
                            System.out.println("modifiers:");
                            for(Iterator<T2<String,String>> mi=modifiers.iterator(); mi.hasNext();) {
                                T2<String,String> mo = mi.next();
                                System.out.println("   "+mo.e1+":"+mo.e2);
                            }
                            System.out.println("comment: "+ comment);
                        }
                        
                        
                        
                        // **** PROCESS CURRENT TAG AND VALUE ****
                        if("alt_id".equals(tag)) {
                            if(OBO.MAKE_SIS_FROM_ALT_IDS) {
                                try {
                                    stanzaTopic.addSubjectIdentifier(OBO.makeLocator(value));
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                             }
                            else {
                               createAssociation(OBO.SCHEMA_TERM_ALTERNATIVE_ID, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_ALTERNATIVE_ID);
                            }
                        }
                        
                        // ******** NAMESPACE *********
                        else if("namespace".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_NAMESPACE, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_NAMESPACE);
                        }
                        
                        // ****** INSTANCE OF *******
                        else if("instance_of".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_INSTANCE_OF, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_TERM);
                        }

                        // ******* PROPERTY VALUE *******
                        else if("property_value".equals(tag)) {
                            processProperty(stanzaTopic, OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_INSTANCE), value);
                        }
                        
                        // ***** XREFS *****
                        else if("xref_unknown".equals(tag)) {
                            logDeprecation(tag, id, "xref");
                            processXref(stanzaTopic, "UNKNOWN", value, namespace, OBO.SCHEMA_TERM_INSTANCE);
                        }
                        else if("xref_analog".equals(tag)) {
                            logDeprecation(tag, id, "xref");
                            processXref(stanzaTopic, "ANALOG", value, namespace, OBO.SCHEMA_TERM_INSTANCE);
                        }
                        else if("xref".equals(tag)) {
                            processXref(stanzaTopic, null, value, namespace, OBO.SCHEMA_TERM_INSTANCE);
                        }
                        
                        

                        // ***** SYNONYMS *****
                        else if("narrow_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "NARROW", value, namespace);
                        }
                        else if("broad_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "BROAD", value, namespace);
                        }
                        else if("exact_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "EXACT", value, namespace);
                        }
                        else if("related_synonym".equals(tag)) {
                            logDeprecation(tag, id, "synonym");
                            processSynonym(stanzaTopic, "RELATED", value, namespace);
                        }
                        else if("synonym".equals(tag)) {
                            processSynonym(stanzaTopic, value, namespace);
                        }

                        
                        
                        // ******** CONSIDER INSTANCE **********
                        else if("consider".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_CONSIDER_USING, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_CONSIDER_USING);
                        }
                        else if("use_term".equals(tag)) {
                            logDeprecation(tag, id, "consider");
                            createAssociation(OBO.SCHEMA_TERM_CONSIDER_USING, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_CONSIDER_USING);
                        }
                        else if("replaced_by".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_REPLACED_BY, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForTerm(tm,value), OBO.SCHEMA_TERM_REPLACED_BY);
                        }
                        
                        
                        // ****** OCCURRENCES ******
                        else if("comment".equals(tag)) {
                            commentCount++;
                            if(commentCount > 1) {
                                parent.log("Error: More that one comment in '"+id+"'.");
                            }
                            else {
                                Topic commentType = OBO.createTopicForSchemaTerm(tm,"comment");
                                setData(stanzaTopic, commentType, OBO.LANG, OBO.OBO2Java(value));
                            }
                        }
                        
                        
                        // ******** IS_ANONYMOUS *********
                        else if("is_anonymous".equals(tag)) {
                            createAssociation(OBO.SCHEMA_TERM_IS_ANONYMOUS, stanzaTopic, OBO.SCHEMA_TERM_INSTANCE, OBO.createTopicForSchemaTerm(tm,value), OBO.SCHEMA_TERM_IS_ANONYMOUS);
                        }
                        
                        // ******* IS OBSOLE *********
                        else if("is_obsolete".equals(tag)) {
                            if("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
                                stanzaTopic.addType(OBO.createObsoleteTopic(tm, namespace));
                            }
                        }
                        
                        
                        
                        else {
                            parent.log("Unprocessed instance tag found!\n  tag: '"+tag+"'\n  value: '"+value+"'");
                        }
                    }
                    catch(Exception ei) {
                        parent.log("Exception while processing '"+tag+"' and value '"+value+"'.");
                        parent.log(ei);
                    }
                }
                
                
                // **** POSTPROCESS ****
                
            }
            catch(Exception e) {
                parent.log(e);
            }
            
        }
        
        
        
        
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        
        
        
        
        private String solveNamespace(Stanza stanza, String id, Header header, String defaultNamespace) {
            String namespace = null;
            if(stanza != null) namespace = stanza.getNameSpace();
            if(namespace == null && header != null) namespace = header.getDefaultNameSpace();
            if(namespace == null && id != null) {
                String[] idParts = id.split(":");
                if(idParts.length > 1) namespace = idParts[0].toLowerCase();
            }
            if(namespace == null) namespace = defaultNamespace;
            return namespace;
        }
        
        
        
        public void logDeprecation(String tag, String id) {
            logDeprecation(tag, id, null);
        }
        public void logDeprecation(String tag, String id, String replacingTag) {
            if(deprecationMsgLimit > 0) {
                deprecationMsgLimit--;
                if(replacingTag != null) {
                    parent.log("Warning: Deprecated tag '"+tag+"' used in '"+id+"'. Use '"+replacingTag+"' instead.");
                }
                else {
                    parent.log("Warning: Deprecated tag '"+tag+"' used in '"+id+"'.");
                }
                if(deprecationMsgLimit == 0) {
                    parent.log("Supressing further deprecation messages.");
                }
            }
        }
        
        
        
        public void processSubset(Topic stanzaTopic, String value) {
            if(value != null) {
                try {
                    String category = value.trim();
                    Topic categoryTopic = OBO.createTopicForCategory(tm, category);
                    createAssociation(OBO.SCHEMA_TERM_CATEGORY, stanzaTopic, OBO.SCHEMA_TERM_MEMBER, categoryTopic, OBO.SCHEMA_TERM_CATEGORY);
                    if(!header.isSubset(category)) {
                        parent.log("Error: Subset category '"+value+"' not defined in header!");
                    }
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
        }
        
        
        
        
        public void processDefinition(Topic stanzaTopic, String value, String stanzatype, String namespace) {
            try {
                Definition definition = new Definition(value);
                Dbxrefs dbxrefs = definition.getOrigins();
                if(dbxrefs != null) {
                    Dbxref[] origins = dbxrefs.toDbxrefArray();

                    if(origins != null && origins.length > 0) {
                        for(int k=0; k<origins.length; k++) {
                            Dbxref origin = origins[k];
                            if(origin != null) {
                                String description = origin.getDescription();
                                if(OBO.MAKE_DESCRIPTION_TOPICS && description != null && description.length() > 0) {
                                    createAssociation(OBO.SCHEMA_TERM_DEFINITION_ORIGIN, stanzaTopic, stanzatype, OBO.createTopicForDbxref(tm,origin.getId()), OBO.SCHEMA_TERM_DEFINITION_ORIGIN, OBO.createTopicForDescription(tm,description, namespace), OBO.SCHEMA_TERM_DESCRIPTION);
                                }
                                else {
                                    createAssociation(OBO.SCHEMA_TERM_DEFINITION_ORIGIN, stanzaTopic, stanzatype, OBO.createTopicForDbxref(tm,origin.getId(),origin.getDescription()), OBO.SCHEMA_TERM_DEFINITION_ORIGIN);
                                }
                            }
                        }
                    }
                }

                Topic definitionType = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_DEFINITION);
                setData(stanzaTopic, definitionType, OBO.LANG, OBO.OBO2Java(definition.getDefinition()));
            }
            catch(Exception e) {
                parent.log(e);
            }
        }
        
        
        
        
        public void processXref(Topic stanzaTopic, String scope, String everything, String namespace, String stanzatype) {
            Dbxrefs dbxrefs = new Dbxrefs(everything);
            Dbxref[] xrefs = dbxrefs.toDbxrefArray();
            if(stanzatype == null) stanzatype = "term";
            for(int i=0; i<xrefs.length; i++) {
                try {
                    Association a = null;
                    String description = xrefs[i].getDescription();
                    if(OBO.MAKE_DESCRIPTION_TOPICS && description != null && description.length() > 0) {
                        a = createAssociation(OBO.SCHEMA_TERM_XREF, stanzaTopic, stanzatype, OBO.createTopicForDbxref(tm, xrefs[i].getId()), OBO.SCHEMA_TERM_XREF, OBO.createTopicForDescription(tm, description, namespace), OBO.SCHEMA_TERM_DESCRIPTION);
                    }
                    else {
                        a = createAssociation(OBO.SCHEMA_TERM_XREF, stanzaTopic, stanzatype, OBO.createTopicForDbxref(tm, xrefs[i].getId(), xrefs[i].getDescription()), OBO.SCHEMA_TERM_XREF);
                    }
                    if(OBO.USE_SCOPED_XREFS && scope != null && a != null) {
                        a.addPlayer(OBO.createTopicForSchemaTerm(tm,scope), OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_XREF_SCOPE));
                    }
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
        }
        
        
        
        
        
        public void processSynonym(Topic base, String everything, String namespace) {
            processSynonym(base, null, everything, namespace);
        }
        
        public void processSynonym(Topic base, String synonymScope, String everything, String namespace) {
            try {
                String synonymType = null;
                Synonym synonym = new Synonym(everything);
                if(synonymScope == null) synonymScope = synonym.getScope();
                if(synonymType == null) synonymType = synonym.getType();
                //if(synonymType == null) synonymType = header.getDefaultSynonymType();
                
                if(synonym.getSynonym() != null) {
                    Dbxrefs originDbxrefs = synonym.getOrigins();
                    Dbxref[] origins = null;
                    if(originDbxrefs != null) origins = originDbxrefs.toDbxrefArray();
                    if(origins == null || origins.length == 0) origins = new Dbxref[] { new Dbxref("", null) };
                    
                    for(int o=0; o<origins.length; o++) {
                        Dbxref origin = origins[o];
                        Topic associationTypeTypeTopic = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ASSOCIATION_TYPE);
                        Topic associationType = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_SYNONYM);
                        associationType.addType(associationTypeTypeTopic);

                        Association a = tm.createAssociation(associationType);

                        a.addPlayer(base, OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_TERM));
                        Topic nameTopic = OBO.createTopicForSynonym(tm,synonym.getSynonym(), namespace);
                        a.addPlayer(nameTopic, OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_SYNONYM));


                        Topic nameTypeTopic = OBO.createSynonymTopic(tm, namespace);
                        nameTopic.addType(nameTypeTopic);

                        if(synonymType != null) {
                            Topic synonymTypeTopic = OBO.createTopicForSchemaTerm(tm,synonymType);
                            String typeDescription = header.getDefaultSynonymDescription(synonymType);
                            if(header.getDefaultSynonymScope(synonymType) != null) {
                                synonymScope = header.getDefaultSynonymScope(synonymType);
                            }
                            if(typeDescription != null && typeDescription.length() > 0) {
                                Topic descriptionType = OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_DESCRIPTION);
                                this.setData(synonymTypeTopic, descriptionType, OBO.LANG, typeDescription);
                            }
                            a.addPlayer(synonymTypeTopic, OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_SYNONYM_TYPE));
                        }
                        if(synonymScope != null) {
                            a.addPlayer(OBO.createTopicForSchemaTerm(tm,synonymScope), OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_SYNONYM_SCOPE));
                        }
                        if(origin != null && origin.getId() != null && origin.getId().length() > 0) {
                            String odescription = origin.getDescription();
                            if(OBO.MAKE_DESCRIPTION_TOPICS && odescription != null && odescription.length() > 0) {
                                a.addPlayer(OBO.createTopicForDbxref(tm,origin.getId()), OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_SYNONYM_ORIGIN));
                                a.addPlayer(OBO.createTopicForDescription(tm, odescription, namespace), OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_DESCRIPTION));
                            }
                            else {
                                a.addPlayer(OBO.createTopicForDbxref(tm,origin.getId(), odescription), OBO.createTopicForSchemaTerm(tm,OBO.SCHEMA_TERM_SYNONYM_ORIGIN));
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
        }
        
        

        
        public void processProperty(Topic base, Topic baseType, String everything) {
            PropertyValue property = new PropertyValue(everything);
            if(property.getRelationship() != null && property.getValue() != null) {
                try {
                    Topic propertyRelationshipTopic = OBO.createTopicForPropertyRelationship(tm, property.getRelationship());
                    Topic propertyRelationshipType = OBO.createPropertyRelationshipTopic(tm);

                    Topic propertyValueTopic = OBO.createTopicForPropertyValue(tm, property.getValue());
                    Topic propertyValueType = OBO.createPropertyValueTopic(tm);
                    
                    Association a = tm.createAssociation(propertyRelationshipType);
                    a.addPlayer(propertyRelationshipTopic, propertyRelationshipType);
                    a.addPlayer(base, baseType);
                    a.addPlayer(propertyValueTopic, propertyValueType);
                    
                    if(property.getDatatype() != null) {
                        Topic propertyDatatypeTopic = OBO.createTopicForPropertyDatatype(tm, property.getDatatype());
                        Topic propertyDatatypeType = OBO.createPropertyDatatypeTopic(tm);
                        
                        a.addPlayer(propertyDatatypeTopic, propertyDatatypeType);
                    }
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
        }
        
        
        
        // ---------------------------------------------------------------------
        // ---------------------------------------------- TOPIC MAP HELPERS ----
        // ---------------------------------------------------------------------
        
        
        
        

        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2) throws TopicMapException {
            Topic associationTypeTopic = OBO.createTopicForSchemaTerm(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic associationTypeTypeTopic = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ASSOCIATION_TYPE);
            associationTypeTopic.addType(associationTypeTypeTopic);
            Topic role1Topic = OBO.createTopicForSchemaTerm(tm,role1);
            Topic role2Topic = OBO.createTopicForSchemaTerm(tm,role2);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            return association;
        }
        
        
        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3) throws TopicMapException {
            Topic associationTypeTopic = OBO.createTopicForSchemaTerm(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic associationTypeTypeTopic = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ASSOCIATION_TYPE);
            associationTypeTopic.addType(associationTypeTypeTopic);
            Topic role1Topic = OBO.createTopicForSchemaTerm(tm,role1);
            Topic role2Topic = OBO.createTopicForSchemaTerm(tm,role2);
            Topic role3Topic = OBO.createTopicForSchemaTerm(tm,role3);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            association.addPlayer(player3Topic, role3Topic);
            return association;
        }
        
        
        
        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3, Topic player4Topic, String role4) throws TopicMapException {
            Topic associationTypeTopic = OBO.createTopicForSchemaTerm(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic associationTypeTypeTopic = OBO.createTopicForSchemaTerm(tm, OBO.SCHEMA_TERM_ASSOCIATION_TYPE);
            associationTypeTopic.addType(associationTypeTypeTopic);
            Topic role1Topic = OBO.createTopicForSchemaTerm(tm,role1);
            Topic role2Topic = OBO.createTopicForSchemaTerm(tm,role2);
            Topic role3Topic = OBO.createTopicForSchemaTerm(tm,role3);
            Topic role4Topic = OBO.createTopicForSchemaTerm(tm,role4);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            association.addPlayer(player3Topic, role3Topic);
            association.addPlayer(player4Topic, role4Topic);
            return association;
        }
        
        
        
        

        
        
        // ---------------------------------------------------------------------
        
        
        
        

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
                    if(basename != null) topic.setBaseName(OBO.OBO2Java(basename));
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
        
        
        
        
    // -------------------------------------------------------------------------
    // ----------------------------------------------------- HELPER CLASSES ----
    // -------------------------------------------------------------------------

    
    
    protected class Header {
        private String defaultNameSpace = null;
        private HashMap<String,String> idMapping = new HashMap<String,String>();
        private HashMap<String,String[]> idSpaceMapping = new HashMap<String,String[]>();
        private HashMap<String,String[]> synonymTypes = new HashMap<String,String[]>();
        private String defaultRelationshipIdPrefix = null;
        private HashMap<String,Category> subsets = new HashMap<String,Category>();
        
        private ArrayList<TagValuePair> tagValuePairs = new ArrayList<TagValuePair>();
        private ArrayList<String> comments = new ArrayList<String>();
        
        
        public Header() {
            defaultNameSpace = "obo";
        }
        
        
        public void addTagValuePair(String tag, String value, String comment) {
            if("default-namespace".equals(tag)) {
                defaultNameSpace = value;
            }
            else if("id-mapping".equals(tag)) {
                if(value != null) {
                    value = value.trim();
                    String[] mapping = value.split(" ");
                    if(mapping.length == 2) {
                        idMapping.put(mapping[0], mapping[1]);
                    }
                    else {
                        
                    }
                }
            }
            else if("idspace".equals(tag)) {
                if(value != null) {
                    value = value.trim();
                    int splitpoint = value.indexOf(" ");
                    if(splitpoint != -1) {
                        String source = value.substring(0, splitpoint);
                        String target = value.substring(splitpoint);
                        String description = null;
                        splitpoint = target.indexOf(" ");
                        if(splitpoint != -1) {
                            target = target.substring(0, splitpoint);
                            description = target.substring(splitpoint);
                        }
                        idSpaceMapping.put(source, new String[] { target, description });
                    }
                    else {
                        idSpaceMapping.put(value, new String[] { null, null });
                    }
                }
            }
            else if("synonymtypedef".equals(tag)) {
                if(value != null) {
                    value = value.trim();
                    Pattern synonymTypeDefinitionPattern = Pattern.compile("(\\S+)(\\s+"+OBO.QUOTE_STRING_PATTERN+"(\\s+(\\S+))?)?");
                    Matcher m =  synonymTypeDefinitionPattern.matcher(value);
                    
                    String defaultSynonymType = value;
                    String defaultSynonymTypeDescription = null;
                    String defaultSynonymTypeScope = null;
                    
                    if(m.matches()) {
                        if(m.groupCount() > 0) {
                            defaultSynonymType = m.group(1);
                        }
                        if(m.groupCount() > 2) {
                            defaultSynonymTypeDescription = m.group(3);
                        }
                        if(m.groupCount() > 4) {
                            defaultSynonymTypeScope = m.group(5);
                        }
                    }
                    synonymTypes.put(defaultSynonymType, new String[] { defaultSynonymTypeDescription, defaultSynonymTypeScope } );
                }
            }
            else if("default-relationship-id-prefix".equals(tag)) {
                defaultRelationshipIdPrefix = value;
            }
            else if("subsetdef".equals(tag)) {
                Category subset = new Category(value);
                subsets.put(subset.getId(), subset);
            }
            
            // STORE ALL TAG-VALUES
            tagValuePairs.add(new TagValuePair(tag, value, comment));
        }
        
        
        public void addComment(String comment) {
            comments.add(comment);
        }
        
        public int size() {
            return tagValuePairs.size();
        }
        
        public ArrayList<TagValuePair> getTagValuePairs() {
            return tagValuePairs;
        }
        public String getDefaultNameSpace() {
            return defaultNameSpace;
        }
        
        public String getMapping(String source) {
            String target = idMapping.get(source);
            if(target != null) return target;
            else return source;
        }
        
        public String getDefaultSynonymType() {
            Set types = synonymTypes.keySet();
            if(types != null && !types.isEmpty()) {
                return (String) types.iterator().next();
            }
            return null;
        }
        public String getDefaultSynonymDescription(String type) {
            String description = synonymTypes.get(type)[0];
            return description;
        }
        public String getDefaultSynonymScope(String type) {
            String scope = synonymTypes.get(type)[1];
            return scope;
        }
        
        public String getDefaultRelationshipIdPrefix() {
            return defaultRelationshipIdPrefix;
        }
        
        public boolean isSubset(String key) {
            Set<String> keys = subsets.keySet();
            if(keys != null) return keys.contains(key);
            return false;
        }
        public String[] getSubsets() {
            Set<String> keys = subsets.keySet();
            if(keys != null) return keys.toArray( new String[] {} );
            return new String[] {};
        }
        public String getSubsetDescription(String key) {
            Category c = subsets.get(key);
            if(c != null) return c.getDescription();
            return null;
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    protected class Stanza {
        private String type = "";
        private String nameSpace = null;
        private String id = null;
        private String name = null;
        private boolean isObsolete = false;
        private ArrayList<TagValuePair> tagValuePairs = new ArrayList<TagValuePair>();
        private ArrayList<String> comments = new ArrayList<String>();
        
        
        public Stanza(String type) {
            this.type = type;
        }
        
        public void addTagValuePair(String tag, String value, String comment) {
            if("id".equals(tag)) id = value;
            else if("name".equals(tag)) name = value;
            else {
                if("namespace".equals(tag)) nameSpace = value;
                else if("is_obsolete".equals(tag) && "true".equalsIgnoreCase(value)) isObsolete = true;
                tagValuePairs.add(new TagValuePair(tag, value, comment));
            }
        }
        
        public void addComment(String comment) {
            comments.add(comment);
        }
        
        public int size() {
            return tagValuePairs.size();
        }
        
        
        public String getType() {
            return type;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getNameSpace() {
            return nameSpace;
        }
        
        public ArrayList<TagValuePair> getTagValuePairs() {
            return tagValuePairs;
        }
        
        public boolean isObsolete() {
            return isObsolete;
        }
    }
    
    
    
 
    // -------------------------------------------------------------------------
    
    
    
    protected class TagValuePair {
        private String tag = null;
        private String value = null;
        private String comment = null;
        
        
        public TagValuePair(String tag, String value, String comment) {
            this.tag = tag;
            this.value = value;
            this.comment = comment;
        }
        
        public String getTag() {
            return tag;
        }
        
        public String getValue() {
            return value;
        }
        
        public String getComment() {
            return comment;
        }
        
        
        public String getValueWithoutModifiers() {
            if(value == null) return null;
            String valueWithoutModifiers = value;
            
            /*
            int modifierIndex = valueWithoutModifiers.indexOf('{');
            if(modifierIndex != -1) {
                valueWithoutModifiers = valueWithoutModifiers.substring(0, modifierIndex);
                valueWithoutModifiers = valueWithoutModifiers.trim();
            }
            */
            String preModifierString  = null;
            boolean precedesBackslash = true;
            int splitPoint = valueWithoutModifiers.indexOf('{');
            while(splitPoint > -1) {
                preModifierString = value.substring(0, splitPoint);
                precedesBackslash = (splitPoint > 0 ? value.charAt(splitPoint-1) == '\\' : false);
                if(!precedesBackslash) {
                    int quoteCounter = 0;
                    int findPoint = preModifierString.indexOf("\"");
                    while(findPoint > -1) {
                        precedesBackslash = (findPoint > 0 ? value.charAt(findPoint-1) == '\\' : false);
                        if(!precedesBackslash) quoteCounter++;
                        findPoint = preModifierString.indexOf("\"", findPoint+1);
                    }
                    if(((quoteCounter % 2) == 0)) {
                        //modifierString = value.substring(splitPoint+1).trim();
                        valueWithoutModifiers = preModifierString;
                        break;
                    }
                }
                splitPoint = valueWithoutModifiers.indexOf('{', splitPoint+1);
            }
            
            
            
            return valueWithoutModifiers;
        }
        
        
        
        public ArrayList<T2<String, String>> getModifiers() {
            if(value == null) return null;
            ArrayList<T2<String, String>> modifiers = new ArrayList<T2<String, String>>();
            String modifierString = null;
            
            /*
            int modifierIndex = value.indexOf('{');
            if(modifierIndex != -1) {
                modifierString = value.substring(modifierIndex);
            }
            */
            
            
            String preModifierString  = null;
            boolean precedesBackslash = true;
            int splitPoint = value.indexOf('{');
            while(splitPoint > -1) {
                preModifierString = value.substring(0, splitPoint);
                precedesBackslash = (splitPoint > 0 ? value.charAt(splitPoint-1) == '\\' : false);
                if(!precedesBackslash) {
                    int quoteCounter = 0;
                    int findPoint = preModifierString.indexOf("\"");
                    while(findPoint > -1) {
                        precedesBackslash = (findPoint > 0 ? value.charAt(findPoint-1) == '\\' : false);
                        if(!precedesBackslash) quoteCounter++;
                        findPoint = preModifierString.indexOf("\"", findPoint+1);
                    }
                    if(((quoteCounter % 2) == 0)) {
                        modifierString = value.substring(splitPoint+1).trim();
                        value = preModifierString;
                        break;
                    }
                }
                splitPoint = value.indexOf('{', splitPoint+1);
            }
            
            
            
            
            if(modifierString != null && modifierString.length() > 0) {
                int i = modifierString.indexOf('=');
                String modName = null;
                String modValue = null;
                while(i > 0 && i < modifierString.length()) {
                    modName = modifierString.substring(0, i).trim();
                    modValue = null;
                    StringBuilder modValueBuffer = new StringBuilder("");
                    
                    // ***** Pass preceeding spaces!
                    i++;
                    while(i<modifierString.length() && Character.isSpaceChar(modifierString.charAt(i))) {
                        i++;
                    }
                    // ***** Check if the value is "String" or just value.
                    if(i<modifierString.length() && (modifierString.charAt(i) == '\"' || modifierString.charAt(i) == '\'')) {
                        char startChar = modifierString.charAt(i);
                        i++;
                        while(i<modifierString.length() && (modifierString.charAt(i) != startChar || modifierString.charAt(i) == '\\')) {
                            modValueBuffer.append(modifierString.charAt(i));
                            i++;
                        }
                        while(i<modifierString.length() && modifierString.charAt(i) == ',' && modifierString.charAt(i) == '}' ) {
                            i++;
                        }
                        modValue = modValueBuffer.toString();
                    }
                    // ***** Found just value!
                    else {
                        while(i<modifierString.length() && modifierString.charAt(i) == ',' && modifierString.charAt(i) == '}' ) {
                            modValueBuffer.append(modifierString.charAt(i));
                            i++;
                        }
                        modValue = modValueBuffer.toString().trim();
                    }
                    // ***** Finally add the modifier if name is reasonable!
                    if(modName != null) {
                        modifiers.add(new T2<String, String>(modName, modValue) );
                    }
                    
                    modifierString = modifierString.substring(i);
                    i = modifierString.indexOf('=');
                }
                
            }
            return modifiers;
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    protected class RelatedTerm {
        private String term = null;
        private String modifier = null;
        
        private Pattern withModifierPattern = Pattern.compile("(.+)\\s+(.+\\:.+)");
        private Pattern withUncertainModifierPattern = Pattern.compile("(.+)\\s+(.+)");

        
        public RelatedTerm(String everything) {
            parseRelatedTerm(everything);
        }
        
        
        
        public void parseRelatedTerm(String str) {
            str = str.trim();
            boolean parsed = false;
            if(!parsed) {
                Matcher m = withModifierPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) modifier = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) term = m.group(2);
                    parsed = true;
                }
            }
            if(!parsed) {
                Matcher m = withUncertainModifierPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) modifier = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) term = m.group(2);
                    parsed = true;
                }
            }
            if(!parsed) {
                term = str;
            }
        }
        
        
        
        public String getTerm() {
            return term;
        }
        
        public String getModifier() {
            return modifier;
        }

    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected class Synonym {
        private String synonym = null;
        private String type = null;
        private String scope = null;
        private Dbxrefs origins = null;
        
        private Pattern STOSynonymPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+(\\w+\\S*)\\s+(\\w+\\S*)\\s+\\[(.*)\\]");
        private Pattern SOSynonymPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+(\\w+\\S*)\\s+\\[(.*)\\]");
        private Pattern STSynonymPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+(\\w+\\S*)\\s+(\\w+\\S*)");
        private Pattern SSynonymPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+(\\w+\\S*)");
        private Pattern OSynonymPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+\\[(.*)\\]");
        private Pattern plainStringPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN);
        
        public Synonym(String everything) {
            parseSynonym(everything);
        }
        
        
        
        public void parseSynonym(String str) {
            str = str.trim();
            //System.out.println("parsing synonym: "+str);
            boolean parsed = false;
            if(!parsed) {
                Matcher m = STOSynonymPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) synonym = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) scope = m.group(2);
                    if(m.group(3) != null && m.group(3).length() > 0) type = m.group(3);
                    if(m.group(4) != null && m.group(4).length() > 0) origins = new Dbxrefs(m.group(4));
                    //System.out.println("found synonym 1: "+synonym);
                    parsed = true;
                }
            }
            if(!parsed) {
                Matcher m = SOSynonymPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) synonym = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) scope = m.group(2);
                    if(m.group(3) != null && m.group(3).length() > 0) origins = new Dbxrefs(m.group(3));
                    //System.out.println("found synonym 2: "+synonym);
                    parsed = true;
                }
            }
            if(!parsed) {
                Matcher m = STSynonymPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) synonym = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) scope = m.group(2);
                    if(m.group(3) != null && m.group(3).length() > 0) type = m.group(3);
                    //System.out.println("found synonym 3: "+synonym);
                    parsed = true;
                }
            }
            if(!parsed) {
                Matcher m = SSynonymPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) synonym = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) scope = m.group(2);
                    //System.out.println("found synonym 4: "+synonym);
                    parsed = true;
                }
            }
            if(!parsed) {
                Matcher m = OSynonymPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) synonym = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) origins = new Dbxrefs(m.group(2));
                    //System.out.println("found synonym 5: "+synonym);
                    parsed = true;
                }
            }
            if(!parsed) {
                Matcher m = plainStringPattern.matcher(str);
                if(m.matches()) {
                    //System.out.println("found synonym 6: "+synonym);
                    if(m.group(1) != null && m.group(1).length() > 0) synonym = m.group(1);
                    parsed = true;
                }
            }
            if(!parsed) {
                synonym = str;
                //System.out.println("found synonym 7: "+synonym);
            }
        }
        
        
      
        
        public String getSynonym() {
            return synonym;
        }
        
        public String getType() {
            return type;
        }
        
        public String getScope() {
            return scope;
        }
        
        public Dbxrefs getOrigins() {
            return origins;
        }

    }
    
  
    // -------------------------------------------------------------------------
    
    
    
    
    
    protected class Definition {
        private String definition = null;
        private Dbxrefs origins = null;

        
        //private Pattern definitionPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+\\[(.*)\\]");
        //private Pattern textPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN);
        //private Pattern definitionPattern = Pattern.compile("\"(.*)\""+"\\s+\\[(.*)\\]");
        //private Pattern textPattern = Pattern.compile("\"(.*)\"");
       
        public Definition(String everything) {
            parse(everything);
        }
        
        
        
        public void parse(String str) {
            str = str.trim();
            boolean parsed = false;
            if(!parsed) {
                Pattern definitionPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN+"\\s+\\[(.*)\\]");
                Matcher m = definitionPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) definition = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) origins = new Dbxrefs(m.group(2));
                    parsed = true;
                }
            }
            if(!parsed) {
                Pattern textPattern = Pattern.compile(OBO.QUOTE_STRING_PATTERN);
                Matcher m = textPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) definition = m.group(1);
                    parsed = true;
                }
            }
            if(!parsed) {
                definition = str;
            }
        }
        
        
        public String getDefinition() {
            return definition;
        }
        
        public Dbxrefs getOrigins() {
            return origins;
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected class Category {
        private String id = null;
        private String description = null;
        
        public Category(String everything) {
            parse(everything);
        }
        
        
        Pattern fullCategoryPattern = Pattern.compile("(\\S+)\\s+"+OBO.QUOTE_STRING_PATTERN);
                
        public void parse(String str) {
            str = str.trim();
            boolean parsed = false;
            if(!parsed) {
                Matcher m = fullCategoryPattern.matcher(str);
                if(m.matches()) {
                    if(m.group(1) != null && m.group(1).length() > 0) id = m.group(1);
                    if(m.group(2) != null && m.group(2).length() > 0) description = m.group(2);
                    parsed = true;
                }
            }
            if(!parsed) {
                id = str;
            }
        }
        
        
        public String getDescription() {
            return description;
        }
        
        public String getId() {
            return id;
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    protected class Dbxrefs {
        private ArrayList<Dbxref> xrefs = new ArrayList<Dbxref>();
        
        //private Pattern dbxrefPattern = Pattern.compile("(\\w+[^\\,]+)(\\s+\"([^\"]*)\")?");
        private Pattern dbxrefPattern = Pattern.compile("((?:[^\\,\"]|(?<=\\\\)\\,|(?<=\\\\)\")+)(\\s*"+OBO.QUOTE_STRING_PATTERN+")?");
        
        
        public Dbxrefs(String everything) {
            parse(everything);
        }
        
        public void parse(String str){
            Matcher om = dbxrefPattern.matcher(str);
            int startpoint = 0;
            while(om.find(startpoint)) {
                if(om.group(0) != null && om.group(0).length() > 0) {
                    Dbxref dbxref = null;
                    if(om.groupCount() > 2) {
                        dbxref = new Dbxref(om.group(1), om.group(3));
                    }
                    else {
                        dbxref = new Dbxref(om.group(1), null);
                    } 
                    xrefs.add(dbxref);
                    startpoint = om.end(0);
                }
            }
            
        }
        
        
        public Dbxref[] toDbxrefArray() {
            return xrefs.toArray(new Dbxref[] {} );
        }
        
        public ArrayList<Dbxref> getDbxrefs() {
            return xrefs;
        }
        
    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected class Dbxref {
        private String id = null;
        private String description = null;
        

        public Dbxref(String id, String description) {
            this.id = (id != null ? id.trim() : id);
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getId() {
            return id;
        }
    }
    
    
    
    
    protected class PropertyValue {
        private String relationship = null;
        private String value = null;
        private String datatype = null;
        
        
        private Pattern propertyPattern1 = Pattern.compile("(\\w+[^\\s]+)\\s+"+OBO.QUOTE_STRING_PATTERN+"\\s*(\\w+[^\\s]+)?");
        private Pattern propertyPattern2 = Pattern.compile("(\\w+[^\\s]+)\\s+(\\w+[^\\s]+)\\s*?");
        
        public PropertyValue(String everything) {
            parse(everything);
        }
        
        public void parse(String str){
            Matcher m = propertyPattern1.matcher(str);
            if(m.matches()) {
                if(m.group(1) != null && m.group(1).length() > 0) relationship = m.group(1);
                if(m.group(2) != null && m.group(2).length() > 0) value = m.group(2);
                if(m.group(3) != null && m.group(3).length() > 0) datatype = m.group(3);
            }
            else {
                m = propertyPattern2.matcher(str);
                if(m.matches()) {
                    if(m.group(0) != null && m.group(0).length() > 0) relationship = m.group(0);
                    if(m.group(1) != null && m.group(1).length() > 0) value = m.group(1);
                }
            }
        }

        
        public String getRelationship() {
            return relationship;
        }
        public String getValue() {
            return value;
        }
        public String getDatatype() {
            return datatype;
        }
        
    }
    
    
}
