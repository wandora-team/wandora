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
 * GellishExtractor.java
 *
 * Created on 12. huhtikuuta 2009, 18:08
 *
 */

package org.wandora.application.tools.extractors;


import org.wandora.topicmap.*;
import org.wandora.application.*;


import java.util.*;
import java.io.*;
import java.net.*;
/**
 *
 * @author akivela
 */
public class GellishExtractor extends AbstractExtractor implements WandoraTool {


	private static final long serialVersionUID = 1L;

	public String locatorPrefix = "http://wandora.org/si/gellish/";

    public static String DEFAULT_LANG = "en";
    public static boolean EXTRACT_EXTRAS = true;
    public static boolean REMOVE_SPACES_IN_IDS = true;
    public static boolean CONNECT_TO_WANDORA_CLASS = true;
    
    
    /** Creates a new instance of GellishExtractor */
    public GellishExtractor() {
    }
    
    
    @Override
    public String getName() {
        return "Gellish ontology extractor";
    }
    
    @Override
    public String getDescription() {
        return "Convert tab text formatted Gellish ontologies to Topic Maps.";
    }
    
    


    
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select Gellish ontology file(s) or directories containing Gellish ontology files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking Gellish ontology files!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions! %1 Gellish ontology file(s) processed!";
            case DONE_ONE: return "Ready. Successful extraction! %1 Gellish ontology file(s) processed!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 Gellish ontology file(s) processed!";
            
            case LOG_TITLE: return "Gellish ontology extraction Log";
        }
        return "";
    }
    
    
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        BufferedReader urlReader = new BufferedReader( new InputStreamReader ( url.openStream() ) );
        return _extractTopicsFrom(urlReader, topicMap);
    }

    public boolean _extractTopicsFrom(File thesaurusFile, TopicMap topicMap) throws Exception {
        boolean result = false;
        BufferedReader breader = null;
        try {
            if(thesaurusFile == null) {
                log("No Gellish ontology file addressed!");
                return false;
            }
            FileReader fr = new FileReader(thesaurusFile);
            breader = new BufferedReader(fr);
            result = _extractTopicsFrom(breader, topicMap);
        }
        finally {
            if(breader != null) breader.close();
        }
        return result;
    }
    
    
    
    public boolean _extractTopicsFrom(BufferedReader breader, TopicMap topicMap) throws Exception {

        int factCounter = 0;
        
        try {
            Topic associationType = null;
            Topic leftRole = null;
            Topic rightRole = null;
            Topic leftPlayer = null;
            Topic rightPlayer = null;
            Topic extraPlayer = null;
            Topic associationTypeClass = null;
            Topic termClass = null;
            Topic relationType = null;
            Topic relationTypeClass = null;
            
            String leftPlayerId;
            String rightPlayerId;
            String leftPlayerName;
            String rightPlayerName;

            String associationTypeId;
            String associationTypeName;
            String extraPlayerId;
            
            String disciplineId;
            String discipline;
            String definition;
            String fullDefinition;
            String remarks;
            String status;
            String dateOfStart;
            String dateOfChange;
            String signature;
            String referenceSource;
            String collectionName;
            
            String line = "";
            Association association = null;
            String[] tokens;

            
            line = breader.readLine();
            while(line != null && !forceStop()) {
                tokens = line.split("\t");
                if(tokens.length > 12) {
                    factCounter++;
                    for(int i=0; i<tokens.length; i++) {
                        if(tokens[i] != null) tokens[i] = tokens[i].trim();
                    }
                    leftPlayerId = getToken(4, tokens);
                    leftPlayerName = getToken(6, tokens);
                    extraPlayerId = getToken(7, tokens);
                    associationTypeId = getToken(8, tokens);
                    associationTypeName = getToken(9, tokens);
                    rightPlayerId = getToken(10, tokens);
                    rightPlayerName = getToken(12, tokens);
                    
                    disciplineId = getToken(2, tokens);
                    discipline = getToken(3, tokens);
                    definition = getToken(13, tokens);
                    fullDefinition = getToken(14, tokens);
                    remarks = getToken(17, tokens);
                    status = getToken(18, tokens);
                    
                    dateOfStart = getToken(20, tokens);
                    dateOfChange = getToken(21, tokens);
                    signature = getToken(22, tokens);
                    
                    referenceSource = getToken(23, tokens);
                    collectionName = getToken(24, tokens);

                    if(REMOVE_SPACES_IN_IDS) {
                        leftPlayerId = removeSpacesIn(leftPlayerId);
                        extraPlayerId = removeSpacesIn(extraPlayerId);
                        associationTypeId = removeSpacesIn(associationTypeId);
                        rightPlayerId = removeSpacesIn(rightPlayerId);
                        disciplineId = removeSpacesIn(disciplineId);
                    }
                    
                    if(isValid(leftPlayerId) && isValid(rightPlayerId) && isValid(associationTypeId)) {
                        associationType = getOrCreateTopic(topicMap, makeSI("fact"), "fact (gellish)", "fact");
                        
                        leftPlayer = getOrCreateConceptTopic(topicMap, leftPlayerId, leftPlayerName);
                        rightPlayer = getOrCreateConceptTopic(topicMap, rightPlayerId, rightPlayerName);
                        relationType = getOrCreateTopic(topicMap, makeSI(associationTypeId), associationTypeName+" ("+associationTypeId+")", associationTypeName);
                        leftRole = getOrCreateTopic(topicMap, makeSI("meta/", "left-hand-object"), "left-hand-object (gellish)", "left-hand-object");
                        rightRole = getOrCreateTopic(topicMap, makeSI("meta/", "right-hand-object"), "right-hand-object (gellish)", "right-hand-object");
                        relationTypeClass = getOrCreateTopic(topicMap, makeSI("meta/", "relation-type"), "relation-type (gellish)", "relation-type");
                        relationType.addType(relationTypeClass);
                        makeSubclassOfGellish(topicMap, relationTypeClass);
                        termClass = getOrCreateTopic(topicMap, makeSI("meta/", "gellish-concept"), "gellish-concept (gellish)", "gellish-concept");
                        leftPlayer.addType(termClass);
                        rightPlayer.addType(termClass);
                        makeSubclassOfGellish(topicMap, termClass);
                        
                        association = topicMap.createAssociation(associationType);
                        association.addPlayer(leftPlayer, leftRole);
                        association.addPlayer(rightPlayer, rightRole);
                        association.addPlayer(relationType, relationTypeClass);
                        
                        if(EXTRACT_EXTRAS) {
                            if(isValid(extraPlayerId)) {
                                Topic factTopic = getOrCreateTopic(topicMap, makeSI("fact"), "fact (gellish)", "fact");
                                extraPlayer = getOrCreateTopic(topicMap, makeSI("fact/",extraPlayerId), "fact "+extraPlayerId, null);
                                extraPlayer.addType(factTopic);
                                association.addPlayer(extraPlayer, factTopic);

                                if(isValid(definition)) {
                                    Topic definitionType = getOrCreateTopic(topicMap, makeSI("meta/", "definition"), "definition (gellish)", "definition");
                                    Topic definitionLan = getOrCreateTopic(topicMap, XTMPSI.getLang("en"), null, null);
                                    extraPlayer.setData(definitionType, definitionLan, definition);
                                }
                                if(isValid(fullDefinition)) {
                                    Topic definitionType = getOrCreateTopic(topicMap, makeSI("meta/", "full-definition"), "full-definition (gellish)", "full-definition");
                                    Topic definitionLan = getOrCreateTopic(topicMap, XTMPSI.getLang("en"), null, null);
                                    extraPlayer.setData(definitionType, definitionLan, fullDefinition);
                                }
                                if(isValid(dateOfStart)) {
                                    Topic dateOfStartType = getOrCreateTopic(topicMap, makeSI("meta/", "date-of-start"), "date-of-start (gellish)", "date-of-start");
                                    Topic dateOfStartTopic = getOrCreateTopic(topicMap, makeSI("meta/date/",dateOfStart), dateOfStart, dateOfStart);
                                    Topic dateTopic = getOrCreateTopic(topicMap, makeSI("meta", "date"), "date (meta)", "date");
                                    dateOfStartTopic.addType(dateTopic);
                                    Association dateAssociation = topicMap.createAssociation(dateOfStartType);
                                    dateAssociation.addPlayer(dateOfStartTopic, dateTopic);
                                    dateAssociation.addPlayer(extraPlayer, factTopic);
                                    makeSubclassOfGellish(topicMap, dateTopic);
                                }
                                if(isValid(dateOfChange)) {
                                    Topic dateOfChangeType = getOrCreateTopic(topicMap, makeSI("meta/", "date-of-change"), "date-of-change (gellish)", "date-of-change");
                                    Topic dateOfChangeTopic = getOrCreateTopic(topicMap, makeSI("meta/date/",dateOfChange), dateOfChange, dateOfChange);
                                    Topic dateTopic = getOrCreateTopic(topicMap, makeSI("meta", "date"), "date (meta)", "date");
                                    dateOfChangeTopic.addType(dateTopic);
                                    Association dateAssociation = topicMap.createAssociation(dateOfChangeType);
                                    dateAssociation.addPlayer(dateOfChangeTopic, dateTopic);
                                    dateAssociation.addPlayer(extraPlayer, factTopic);
                                }
                                if(isValid(signature)) {
                                    Topic signatureType = getOrCreateTopic(topicMap, makeSI("meta/", "signature"), "signature (gellish)", "signature");
                                    Topic signatureTopic = getOrCreateTopic(topicMap, makeSI("meta/signature/",signature), signature, signature);
                                    signatureTopic.addType(signatureType);
                                    Association signatureAssociation = topicMap.createAssociation(signatureType);
                                    signatureAssociation.addPlayer(signatureTopic, signatureType);
                                    signatureAssociation.addPlayer(extraPlayer, factTopic);
                                    makeSubclassOfGellish(topicMap, signatureType);
                                }
                                if(isValid(referenceSource)) {
                                    Topic referenceType = getOrCreateTopic(topicMap, makeSI("meta/", "reference"), "reference (gellish)", "reference");
                                    Topic referenceTopic = getOrCreateTopic(topicMap, makeSI("meta/reference/",referenceSource), referenceSource, referenceSource);
                                    referenceTopic.addType(referenceType);
                                    Association referenceAssociation = topicMap.createAssociation(referenceType);
                                    referenceAssociation.addPlayer(referenceTopic, referenceType);
                                    referenceAssociation.addPlayer(extraPlayer, factTopic);
                                    makeSubclassOfGellish(topicMap, referenceType);
                                }
                                if(isValid(collectionName)) {
                                    Topic collectionType = getOrCreateTopic(topicMap, makeSI("meta/", "collection"), "collection (gellish)", "collection");
                                    Topic collectionTopic = getOrCreateTopic(topicMap, makeSI("meta/collection/",collectionName), collectionName, collectionName);
                                    collectionTopic.addType(collectionType);
                                    Association collectionAssociation = topicMap.createAssociation(collectionType);
                                    collectionAssociation.addPlayer(collectionTopic, collectionType);
                                    collectionAssociation.addPlayer(extraPlayer, factTopic);
                                    makeSubclassOfGellish(topicMap, collectionType);
                                }
                                if(isValid(status)) {
                                    Topic statusType = getOrCreateTopic(topicMap, makeSI("meta/", "status"), "status (gellish)", "status");
                                    Topic statusTopic = getOrCreateTopic(topicMap, makeSI("meta/status/",status), status, status);
                                    statusTopic.addType(statusType);
                                    Association statusAssociation = topicMap.createAssociation(statusType);
                                    statusAssociation.addPlayer(statusTopic, statusType);
                                    statusAssociation.addPlayer(extraPlayer, factTopic);
                                    makeSubclassOfGellish(topicMap, statusType);
                                }
                                if(isValid(discipline)) {
                                    Topic disciplineType = getOrCreateTopic(topicMap, makeSI("meta/", "discipline"), "discipline (gellish)", "discipline");
                                    Topic disciplineTopic = getOrCreateTopic(topicMap, makeSI("meta/discipline/",disciplineId), discipline+" ("+disciplineId+")", discipline);
                                    disciplineTopic.addType(disciplineType);
                                    Association disciplineAssociation = topicMap.createAssociation(disciplineType);
                                    disciplineAssociation.addPlayer(disciplineTopic, disciplineType);
                                    disciplineAssociation.addPlayer(extraPlayer, factTopic);
                                    makeSubclassOfGellish(topicMap, disciplineType);
                                }
                                if(isValid(remarks)) {
                                    Topic remarksType = getOrCreateTopic(topicMap, makeSI("meta/", "remarks"), "remarks (gellish)", "remarks");
                                    Topic remarksLan = getOrCreateTopic(topicMap, XTMPSI.getLang("en"), null, null);
                                    extraPlayer.setData(remarksType, remarksLan, remarks);
                                }
                            }
                        }
                    }
                }
                else {
                    log("Rejecting input line as it does not contain enough tokens: "+line);
                }

                setProgress(factCounter);
                line = breader.readLine();
            }
            if(CONNECT_TO_WANDORA_CLASS) {
                // Finally connect Gellish class to Wandora class
                makeSubclassOf(topicMap, getGellishClassTopic(topicMap), getOrCreateTopic(topicMap, new Locator(TMBox.WANDORACLASS_SI), null, null));
            }
        }
        catch(Exception e) {
            log(e);
        }
        
       
        log("Found total "+factCounter+" facts");
        return true;
    }
    
    
    
    protected String getToken(int i, String[] arr) {
        if(arr.length > i) return arr[i];
        else return null;
    } 
    

    protected boolean isValid(String str) {
        if(str == null || str.length() == 0) return false;
        return true;
    }
    
    
    protected String removeSpacesIn(String str) {
        if(str == null) return null;
        StringBuffer sb = new StringBuffer("");
        char ch = 0;
        for(int i=0; i<str.length(); i++) {
            ch = str.charAt(i);
            if("0123456789".indexOf(ch) != -1) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    
    protected boolean associationExists(Topic t1, Topic t2, Topic at) {
        if(t1 == null || t2 == null || at == null) return false;
        try {
            Collection<Association> c = t1.getAssociations(at);
            Association a = null;
            Collection<Topic> roles = null;
            Topic player = null;
            for(Iterator<Association> i=c.iterator(); i.hasNext(); ) {
                a = i.next();
                roles = a.getRoles();
                for(Iterator<Topic> it = roles.iterator(); it.hasNext(); ) {
                    player = a.getPlayer(it.next());
                    if(player != null && t2.mergesWithTopic(player)) return true;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    protected Topic getOrCreateConceptTopic(TopicMap tm, String id, String name) {
        int count = -1;
        String newIdPostfix = null;
        Topic existingTopic = null;
        boolean nameFound = false;
        String existingName = null;
        try {
            do {
                count++;
                newIdPostfix = "#" + count;
                existingTopic = tm.getTopic(makeSI(null, id, newIdPostfix));
                if(existingTopic != null) {
                    existingName = existingTopic.getDisplayName(DEFAULT_LANG);
                    if(existingName != null && existingName.equals(name)) {
                        nameFound = true;
                    }
                }
            }
            while(!nameFound && existingTopic != null && count < 9999);
        }
        catch(Exception e) {
            log(e);
        }
        
        if(nameFound) {
            return existingTopic;
        }
        else {
            Topic newTopic = getOrCreateTopic(tm, makeSI(null, id, newIdPostfix), name+" ("+id+newIdPostfix+")", name);
            return newTopic;
        }
    }
  
    
    
    
    protected Topic getOrCreateTopic(TopicMap topicmap, String si, String baseName, String displayName) {
        return getOrCreateTopic(topicmap, new Locator(si), baseName, displayName, null);
    }
    protected Topic getOrCreateTopic(TopicMap topicmap, Locator si, String baseName, String displayName) {
        return getOrCreateTopic(topicmap, si, baseName, displayName, null);
    }
    protected Topic getOrCreateTopic(TopicMap topicmap, Locator si, String baseName, String displayName, Topic typeTopic) {
        try {
            return ExtractHelper.getOrCreateTopic(si, baseName, displayName, typeTopic, topicmap);
        }
        catch(Exception e) {
            log(e);
        }
        return null;
    }
    
    
    
    protected Locator makeSI(String str) {
        return makeSI(null, str, null);
    }
    protected Locator makeSI(String prefix, String str) {
        return makeSI(prefix, str, null);
    }
    protected Locator makeSI(String prefix, String str, String postfix) {
        if(str == null) return null;
        if(str.startsWith("http://")) {
            return new Locator( str );
        }
        else {
            try {
                return new Locator( 
                        locatorPrefix + 
                        (prefix != null ? prefix : "") + 
                        URLEncoder.encode(str, "UTF-8") + 
                        (postfix != null ? postfix : "")
                        );
            }
            catch(Exception e) {
                return new Locator( 
                        locatorPrefix + 
                        (prefix != null ? prefix : "") + 
                        str +
                        (postfix != null ? postfix : "")
                        );
            }
        }
    }

    
    
    
    protected void makeSubclassOfGellish(TopicMap tm, Topic subclass) throws TopicMapException {
        makeSubclassOf(tm, subclass, getGellishClassTopic(tm));
    }
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    protected Topic getGellishClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, new Locator(locatorPrefix), "Gellish class", null);
    }
    
    @Override
    public boolean useTempTopicMap() {
        return false;
    }

}
