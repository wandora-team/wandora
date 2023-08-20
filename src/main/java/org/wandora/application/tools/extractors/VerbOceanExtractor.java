/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * SimpleTextDocumentExtractor.java
 *
 * Created on 2008-04-18, 12:05
 *
 */

package org.wandora.application.tools.extractors;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 * Converts VerbOcean data files to topic maps. VerbOcean data file contains
 * simple relations between verb words. Each verb relation is quaret with two
 * verb words and relation name and relation strength. Example relations of
 * VerbOcean:
 * 
 * <p><code>
 * renounce [stronger-than] abandon :: 13.617344<br>
 * reject [happens-before] abandon :: 11.731130<br>
 * abandon [similar] reject :: 12.048992<br>
 * abandon [similar] scrap :: 13.725957<br>
 * </code></p>
 * <p>
 * More about VerbOcean see http://demo.patrickpantel.com/Content/verbocean/
 * or paper
 * </p>
 * <p>
 * Timothy Chklovski and Patrick Pantel. 2004.VerbOcean: Mining the Web for
 * Fine-Grained Semantic Verb Relations. In Proceedings of Conference on Empirical
 * Methods in Natural Language Processing (EMNLP-04). Barcelona, Spain.
 * </p>
 * 
 * @author akivela
 */
public class VerbOceanExtractor extends AbstractExtractor implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	boolean INCLUDE_STRENGHTS = true;
    private Wandora admin = null;
    private File verbOceanFile = null;
    
    
    
    /** Creates a new instance of VerbOceanExtractor */
    public VerbOceanExtractor() {
    }
    
    @Override
    public String getName() {
        return "VerbOcean Extractor";
    }
    
    @Override
    public String getDescription() {
        return "Extracts topics and associations from VerbOcean datafiles. More about VerbOcean see http://demo.patrickpantel.com/Content/verbocean/";
    }
    

    @Override
    public boolean useTempTopicMap(){
        return false;
    }
    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select VerbOcean document(s) or directories containing VerbOcean documents!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking VerbOcean documents!";
        
            case FILE_PATTERN: return ".*\\.txt";
            
            case DONE_FAILED: return "Ready. No extractions! %1 VerbOcean(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 VerbOcean(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 VerbOcean(s) crawled!";
            
            case LOG_TITLE: return "VerbOcean Extraction Log";
        }
        return "";
    }
    

    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }


    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        
        try {
            URLConnection uc = null;
            if(admin != null) {
                uc = admin.wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(url.toExternalForm(), uc.getInputStream(), topicMap);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url\n" + url.toExternalForm(), e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || file.isDirectory()) return false;
        try {
            _extractTopicsFromStream(file.getPath(), new FileInputStream(file), topicMap);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file " + file.getName(), e);
            takeNap(1000);
        }
        return false;
    }




    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        String stringLocator = "http://wandora.org/si/verb-ocean-extractor/";
        try {
            _extractTopicsFromStream(stringLocator, new ByteArrayInputStream(str.getBytes()), tm);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from string.", e);
            takeNap(1000);
        }
        return false;
    }


    
    
    public void _extractTopicsFromStream(String locator, InputStream inputStream, TopicMap topicMap) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            Pattern linePattern = Pattern.compile("(\\w+)\\s+\\[?([\\w\\-]+)\\]?\\s+(\\w+)\\s*\\:+\\s*(\\d+\\.\\d+)");
            Matcher m = null;
            String verb1 = null;
            String verb2 = null;
            String relation = null;
            String strength = null;
            Topic verb1Topic = null;
            Topic verb2Topic = null;
            int lineCount = 0;
            int associationCount = 0;
            //Topic relationTopic = null;
            Topic strengthTopic = null;
            log("Prosessing VerbOcean stream!");
            while(line != null && line.length() > 0) {
                lineCount++;
                setProgress(lineCount / 100);
                if(!line.startsWith("#")) {
                    m = linePattern.matcher(line);
                    if(m.matches()) {
                        verb1 = m.group(1);
                        relation = m.group(2);
                        verb2 = m.group(3);
                        strength = m.group(4);
                        verb1Topic = getOrCreateTopic(verb1, "verb", topicMap);
                        verb2Topic = getOrCreateTopic(verb2, "verb", topicMap);
                        //relationTopic = getOrCreateTopic(relation, "relation", topicMap);
                        if(INCLUDE_STRENGHTS) {
                            strengthTopic = getOrCreateTopic(strength, "strength", topicMap);
                            associationCount++;
                            createAssociation(relation, verb1Topic, "verb1", verb2Topic, "verb2", strengthTopic, "strength", topicMap);
                        }
                        else {
                            associationCount++;
                            createAssociation(relation, verb1Topic, "verb1", verb2Topic, "verb2", topicMap);
                        }
                    }
                }
                line = reader.readLine();
            }
            log("Total "+lineCount+" lines processed!");
            log("Total "+associationCount+" VerbOcean associations created!");
            log("Ok");
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    String SI_BASE = "http://wandora.org/si/verbocean/";
    public Topic getOrCreateTopic(String name, String path, TopicMap topicMap) {
        try {
            if(topicMap != null) {
                String si = SI_BASE+path+"/"+name;
                Topic t = topicMap.getTopic(si);
                if(t == null) {
                    t = topicMap.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                    t.setBaseName(name);
                    if(path != null && path.length() > 0) {
                        String typeSi = SI_BASE+path;
                        Topic type = topicMap.getTopic(typeSi);
                        if(type == null) {
                            type = topicMap.createTopic();
                            type.addSubjectIdentifier(new Locator(typeSi));
                            type.setBaseName(path);
                        }
                        t.addType(type);
                    }
                }
                return t;
            }
        }
        catch(Exception e) {
            log(e);
        }
        return null;
    }
    
    
    public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, TopicMap topicMap) throws TopicMapException {
        Topic associationTypeTopic = getOrCreateTopic(associationType, "schema", topicMap);
        Association association = topicMap.createAssociation(associationTypeTopic);
        Topic role1Topic = getOrCreateTopic(role1, "schema", topicMap);
        Topic role2Topic = getOrCreateTopic(role2, "schema", topicMap);
        association.addPlayer(player1Topic, role1Topic);
        association.addPlayer(player2Topic, role2Topic);
        return association;
    }


    public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3, TopicMap topicMap) throws TopicMapException {
        Topic associationTypeTopic = getOrCreateTopic(associationType, "schema", topicMap);
        Association association = topicMap.createAssociation(associationTypeTopic);
        Topic role1Topic = getOrCreateTopic(role1, "schema", topicMap);
        Topic role2Topic = getOrCreateTopic(role2, "schema", topicMap);
        Topic role3Topic = getOrCreateTopic(role3, "schema", topicMap);
        association.addPlayer(player1Topic, role1Topic);
        association.addPlayer(player2Topic, role2Topic);
        association.addPlayer(player3Topic, role3Topic);
        return association;
    }
        
    

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    public static final String[] contentTypes=new String[] { "text/plain" };
    public String[] getContentTypes() {
        return contentTypes;
    }
}
