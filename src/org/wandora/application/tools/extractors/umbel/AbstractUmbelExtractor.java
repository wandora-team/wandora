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


package org.wandora.application.tools.extractors.umbel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import javax.swing.Icon;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import static org.wandora.application.tools.extractors.AbstractExtractor.FILE_EXTRACTOR;
import static org.wandora.application.tools.extractors.AbstractExtractor.RAW_EXTRACTOR;
import static org.wandora.application.tools.extractors.AbstractExtractor.URL_EXTRACTOR;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.CSVParser;
import org.wandora.utils.CSVParser.Row;
import org.wandora.utils.IObox;
import org.wandora.utils.Tuples.T3;

/**
 *
 * @author akivela
 */


public abstract class AbstractUmbelExtractor extends AbstractExtractor {
    public static final String UMBEL_CONCEPT_STRING_SPLITTER = "\\s+";
    
    public static final int FILE_CONTAINS_UMBEL_CONCEPT_URLS = 2;
    public static final int FILE_CONTAINS_PLAIN_UMBEL_CONCEPT = 4;
    public static final int FILE_IS_CSV_OF_UMBEL_CONCEPTS = 8;
    public static int fileProcessor = FILE_CONTAINS_UMBEL_CONCEPT_URLS;
    
    public static boolean ADD_DISTANCE_AS_PLAYER = false;
    public static int filterDistancesBelow = 1;
    public static boolean getOnlyImmediateNeighbours = true;
    
    
    public static boolean useXTMSuperclassSubclassTopics = true;
    
    protected static char csvStringCharacter = '"';
    protected static char csvLineSeparator = '\n';
    protected static char csvValueSeparator = ',';
    protected static String csvEncoding = "UTF-8";
    
    // Default language of occurrences and variant names.
    public static String LANG = "en";
    
    public static final String UMBEL_CONCEPT_URI_BASE = "http://umbel.org/umbel/rc/";
    public static final String UMBEL_SUPER_TYPE_URI_BASE = "http://umbel.org/umbel#";
    
    public static final String UMBEL_TYPE_SI = "http://umbel.org";
    public static final String UMBEL_TYPE_NAME = "Umbel";
    
    public static final String UMBEL_CONCEPT_TYPE_SI = "http://wandora.org/si/umbel/concept";
    public static final String UMBEL_CONCEPT_TYPE_NAME = "concept (umbel)";
    
    
    public static final String UMBEL_BROADER_NARROWER_TYPE_SI = "http://wandora.org/si/umbel/broader-narrower";
    public static final String UMBEL_BROADER_NARROWER_TYPE_NAME = "broader-narrower (umbel)";
    
    public static final String UMBEL_NARROWER_TYPE_SI = "http://wandora.org/si/umbel/narrower";
    public static final String UMBEL_NARROWER_TYPE_NAME = "narrower (umbel)";
    
    public static final String UMBEL_BROADER_TYPE_SI = "http://wandora.org/si/umbel/broader";
    public static final String UMBEL_BROADER_TYPE_NAME = "broader (umbel)";
    
    
    public static final String UMBEL_SUPERCLASS_SUBCLASS_TYPE_SI = "http://wandora.org/si/umbel/superclass-subclass";
    public static final String UMBEL_SUPERCLASS_SUBCLASS_TYPE_NAME = "superclass-subclass (umbel)";
    
    public static final String UMBEL_SUBCLASS_TYPE_SI = "http://wandora.org/si/umbel/subclass";
    public static final String UMBEL_SUBCLASS_TYPE_NAME = "subclass (umbel)";
    
    public static final String UMBEL_SUPERCLASS_TYPE_SI = "http://wandora.org/si/umbel/superclass";
    public static final String UMBEL_SUPERCLASS_TYPE_NAME = "superclass (umbel)";
    
    public static final String UMBEL_TYPE_TYPE_SI = "http://wandora.org/si/umbel/type";
    public static final String UMBEL_TYPE_TYPE_NAME = "type (umbel)";
    
    public static final String UMBEL_DISTANCE_TYPE_SI = "http://wandora.org/si/umbel/distance";
    public static final String UMBEL_DISTANCE_TYPE_NAME = "distance (umbel)";
    
    public static final String UMBEL_DISJOINT_TYPE_SI = "http://wandora.org/si/umbel/disjoint";
    public static final String UMBEL_DISJOINT_TYPE_NAME = "disjoint (umbel)";
    
    
    // -------------------------------------------------------------------------
    
    
    public static final String UMBEL_PREF_LABEL_URI = "http://www.w3.org/2004/02/skos/core#prefLabel";
    public static final String UMBEL_ALT_LABEL_URI = "http://www.w3.org/2004/02/skos/core#altLabel";
    public static final String UMBEL_DEFINITION_URI = "http://www.w3.org/2004/02/skos/core#definition";

    
    public static final String[] UMBEL_SUBCLASS_URI = {
        "http://umbel.org/umbel#subClassOf",
        "http://www.w3.org/TR/rdf-schema#subClassOf",
        "http://www.w3.org/2000/01/rdf-schema#subClassOf"
    };
    
    public static final String[] UMBEL_SUPERCLASS_URI = {
        "http://umbel.org/umbel#superClassOf",
        "http://www.w3.org/TR/rdf-schema#superClassOf",
        "http://www.w3.org/2000/01/rdf-schema#superClassOf"
    };
    
    public static final String[] UMBEL_NARROWER_URI = {
        "http://www.w3.org/2004/02/skos/core#narrower",
        "http://www.w3.org/2004/02/skos/core#narrowerTransitive",
    };
    
    public static final String[] UMBEL_BROADER_URI = {
        "http://www.w3.org/2004/02/skos/core#broader",
        "http://www.w3.org/2004/02/skos/core#broaderTransitive",
    };
    
    public static final String[] UMBEL_TYPE_URI = {
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
    };
    
    public static final String[] UMBEL_DISJOINT_URI = {
        "http://www.w3.org/2002/07/owl#disjointWith"
    };
    
    
    @Override
    public String getName(){
        return "Abstract Umbel Extractor";
    }
    @Override
    public String getDescription(){
        return "AbstractUmbelExtractor is a base implementation for Umbel extractors.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_umbel.png");
    }

    
    
    private final String[] contentTypes=new String[] { "application/json" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public int getExtractorType() {
        return  URL_EXTRACTOR | RAW_EXTRACTOR;
        //return FILE_EXTRACTOR | URL_EXTRACTOR | RAW_EXTRACTOR;
    }
    
    
    public abstract String getApiRequestUrlFor(String str);
    
    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url != null) {
            String str = url.toExternalForm();
            if(str.startsWith(UMBEL_CONCEPT_URI_BASE) && str.length() > UMBEL_CONCEPT_URI_BASE.length()) {
                str = str.substring(UMBEL_CONCEPT_URI_BASE.length());
            }
            if(str.startsWith(UMBEL_SUPER_TYPE_URI_BASE) && str.length() > UMBEL_SUPER_TYPE_URI_BASE.length()) {
                str = str.substring(UMBEL_SUPER_TYPE_URI_BASE.length());
            }
            return _extractTopicsFrom(str, topicMap);
        }
        return false;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(fileProcessor == FILE_CONTAINS_UMBEL_CONCEPT_URLS) {
            String input = IObox.loadFile(file);
            int i = 0;
            do {
               i = input.indexOf(UMBEL_CONCEPT_URI_BASE, i);
               if(i > 0) {
                   i = i + UMBEL_CONCEPT_URI_BASE.length();
                   StringBuilder conceptBuilder = new StringBuilder("");
                   while(i<input.length()-1 && Character.isJavaIdentifierPart(input.charAt(i))) {
                       conceptBuilder.append(input.charAt(i));
                       i++;
                   }
                   if(conceptBuilder.length() > 0) {
                       String conceptString = conceptBuilder.toString();
                       _extractTopicsFrom(conceptString, topicMap);
                   }
               }
            }
            while(i > 0 && i<input.length()-1);
            return true;
        }
        else if(fileProcessor == FILE_IS_CSV_OF_UMBEL_CONCEPTS) {
            CSVParser parser = new CSVParser();
            parser.setEncoding(csvEncoding);
            parser.setLineSeparator(csvLineSeparator);
            parser.setValueSeparator(csvValueSeparator);
            parser.setStringCharacter(csvStringCharacter);
            CSVParser.Table table = parser.parse(new ByteArrayInputStream(IObox.loadBFile(new FileInputStream(file))));
            Iterator<Row> i = table.iterator();
            while(i.hasNext()) {
                Row r = i.next();
                if(r != null) {
                    Iterator<Object> c = r.iterator();
                    while(c.hasNext()) {
                        Object o = c.next();
                        if(o != null) {
                            String conceptString = o.toString();
                            _extractTopicsFrom(conceptString, topicMap);
                        }
                    }
                }
            }
            return true;
        }
        else {
            String input = IObox.loadFile(file);
            return _extractTopicsFrom(input, topicMap);
        }
    }
    
    

    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    public String getUmbelConceptURI(String label) {
        return UMBEL_CONCEPT_URI_BASE+label;
    }
    
    
    
    
    protected JSONObject performRequest(String urlStr, String concept) {
        JSONObject response = null;
        if(urlStr != null) {
            try {
                URL url = new URL(urlStr);
                URLConnection urlConnection = url.openConnection();
                urlConnection.addRequestProperty("Accept", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);

                if(urlConnection instanceof HttpURLConnection) {
                    ((HttpURLConnection) urlConnection).setRequestMethod("GET");
                }
                
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder inputBuffer = new StringBuilder("");
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    inputBuffer.append(inputLine);
                in.close();
                
                return new JSONObject(inputBuffer.toString());
            }
            catch(FileNotFoundException fnfe) {
                log("Can't find Umbel concept for '"+concept+"'.");
            }
            catch(IOException fnfe) {
                log("IOException occurred while reading URL "+urlStr+" . Skipping concept '"+concept+"'.");
            } 
            catch (JSONException ex) {
                log("Can't parse received Umbel JSON. Skipping concept '"+concept+"'.");
            }
            catch(Exception e) {
                log(e);
            }
        }
        return response;
    }

    protected String robustGet(JSONObject json, String key) {
        if(json == null || key == null) return null;
        if(json.has(key)) {
            try {
                return json.getString(key);
            }
            catch(Exception e) {}
        }
        return null;
    }
    
    protected int robustGetInt(JSONObject json, String key) {
        if(json == null || key == null) return -1;
        if(json.has(key)) {
            try {
                return json.getInt(key);
            }
            catch(Exception e) {}
        }
        return -1;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    protected Topic getConceptTopic(String si, TopicMap topicMap) {
        return getConceptTopic(si, null, topicMap);
    }
        
    
    protected Topic getConceptTopic(String si, String label, TopicMap topicMap) {
        if(si == null || topicMap == null) return null;
        Topic t = null;
        try { 
            t = topicMap.getTopic(si);
            if(t == null && label != null) {
                t = topicMap.getTopicWithBaseName(label);
            }
            if(t == null) {
                t = topicMap.createTopic();
                t.addSubjectIdentifier(new Locator(si));
                t.addType(getConceptTypeTopic(topicMap));
            }
            if(t != null && label != null && label.length() > 0) {
                Topic occurrenceTypeTopic = getTopic(UMBEL_PREF_LABEL_URI, topicMap);
                t.setData(occurrenceTypeTopic, getTopic(XTMPSI.getLang(LANG), topicMap), label);
            }
        }
        catch(Exception e) {}
        return t;
    }
    
    
    
    
    protected boolean isURL(String u) {
        try {
            URL url = new URL(u);
            return true;
        }
        catch(Exception e) {}
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    protected Topic getUmbelTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_TYPE_SI, UMBEL_TYPE_NAME, topicMap);
        this.makeSubclassOfWandoraClass(t, topicMap);
        return t;
    }
    
    
    

    protected Topic getBroaderNarrowerTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_BROADER_NARROWER_TYPE_SI, UMBEL_BROADER_NARROWER_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    protected Topic getNarrowerTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_NARROWER_TYPE_SI, UMBEL_NARROWER_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    protected Topic getBroaderTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_BROADER_TYPE_SI, UMBEL_BROADER_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    protected Topic getDisjointTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_DISJOINT_TYPE_SI, UMBEL_DISJOINT_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    protected Topic getTypeTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_TYPE_TYPE_SI, UMBEL_TYPE_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    

    protected Topic getSuperclassSubclassTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = null;
        if(useXTMSuperclassSubclassTopics) {
            t = getTopic(XTMPSI.SUPERCLASS_SUBCLASS, topicMap);
        }
        else {
            t = getTopic(UMBEL_SUPERCLASS_SUBCLASS_TYPE_SI, UMBEL_SUPERCLASS_SUBCLASS_TYPE_NAME, topicMap);
        }
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    protected Topic getSubclassTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = null;
        if(useXTMSuperclassSubclassTopics) {
            t = getTopic(XTMPSI.SUBCLASS, topicMap);
        }
        else {
            t = getTopic(UMBEL_SUBCLASS_TYPE_SI, UMBEL_SUBCLASS_TYPE_NAME, topicMap);
        }
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    protected Topic getSuperclassTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = null;
        if(useXTMSuperclassSubclassTopics) {
            t = getTopic(XTMPSI.SUPERCLASS, topicMap);
        }
        else {
            t = getTopic(UMBEL_SUPERCLASS_TYPE_SI, UMBEL_SUPERCLASS_TYPE_NAME, topicMap);
        }
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    
    
    
    protected Topic getConceptTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_CONCEPT_TYPE_SI, UMBEL_CONCEPT_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    protected Topic getDistanceTopic(int distance, TopicMap topicMap) throws TopicMapException {
        Topic dt = getTopic(UMBEL_DISTANCE_TYPE_SI+"/"+distance, ""+distance, topicMap);
        Topic dtt = getDistanceTypeTopic(topicMap);
        dt.addType(dtt);
        return dt;
    }
    
    
    protected Topic getDistanceTypeTopic(TopicMap topicMap) throws TopicMapException {
        Topic t = getTopic(UMBEL_DISTANCE_TYPE_SI, UMBEL_DISTANCE_TYPE_NAME, topicMap);
        ExtractHelper.makeSubclassOf(t, getUmbelTypeTopic(topicMap), topicMap);
        return t;
    }
    
    
    
    protected Topic getTopic(String si, String name, TopicMap topicMap) {
        Topic t = null;
        if(si != null && topicMap != null) {
            try {
                t = topicMap.getTopic(si);
                if(t == null && name != null) t = topicMap.getTopicWithBaseName(name);
                if(t == null) {
                    t = topicMap.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                    if(name != null) t.setBaseName(name);
                }
            }
            catch(Exception e) {}
        }
        return t;
    }
    
    
    protected Topic getTopic(String si, TopicMap topicMap) {
        Topic t = null;
        if(si != null && topicMap != null) {
            try {
                t = topicMap.getTopic(si);
                if(t == null) {
                    t = topicMap.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                }
            }
            catch(Exception e) {}
        }
        return t;
    }
    
    
    
    protected T3<Topic,Topic,Topic> getAssociationTopicsForUmbelPredicate(String predicate, TopicMap tm) {
        if(predicate == null) return null;
        try {
            if(equalsAny(predicate, UMBEL_SUBCLASS_URI)) {
                return new T3( getSuperclassSubclassTypeTopic(tm), getSuperclassTypeTopic(tm), getSubclassTypeTopic(tm) );
            }
            else if(equalsAny(predicate, UMBEL_SUPERCLASS_URI)) {
                return new T3( getSuperclassSubclassTypeTopic(tm), getSubclassTypeTopic(tm), getSuperclassTypeTopic(tm) );
            }
            else if(equalsAny(predicate, UMBEL_BROADER_URI)) {
                return new T3( getBroaderNarrowerTypeTopic(tm), getBroaderTypeTopic(tm), getNarrowerTypeTopic(tm) );
            }
            else if(equalsAny(predicate, UMBEL_NARROWER_URI)) {
                return new T3( getBroaderNarrowerTypeTopic(tm), getNarrowerTypeTopic(tm), getBroaderTypeTopic(tm) );
            }
            else if(equalsAny(predicate, UMBEL_TYPE_URI)) {
                return new T3( getTypeTypeTopic(tm), getTypeTypeTopic(tm), getConceptTypeTopic(tm) );
            }
            else if(equalsAny(predicate, UMBEL_DISJOINT_URI)) {
                return new T3( getDisjointTypeTopic(tm), getDisjointTypeTopic(tm), getConceptTypeTopic(tm) );
            }
            else {
                return new T3( getTopic(predicate, tm), getTopic(predicate, tm), getConceptTypeTopic(tm) );
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    protected boolean equalsAny(String a, String[] b) {
        for(int i=0; i<b.length; i++) {
            if(a.equalsIgnoreCase(b[i])) return true;
        }
        return false;
    }
}
