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
 * ArticlesAdditionalFieldsExtractor.java
 *
 * Created on 2015-04-30
 */

package org.wandora.application.tools.extractors.yle.elavaarkisto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.json.JSONObject;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.CSVParser;

/**
 * Finnish broadcasting company (YLE) has published Elava arkisto 
 * metadata under the CC-BY-SA 4.0 license. Wandora features a set of extractors
 * that transfer the published metadata to Topic Maps. This extractor is one of
 * these extractor. The metadata and it's documentation is available at 
 * 
 * http://elavaarkisto.kokeile.yle.fi/data/
 * http://elavaarkisto.kokeile.yle.fi/data/articles-additional-fields.csv
 *
 * @author akivela
 */


public class ArticlesAdditionalFieldsExtractor extends AbstractElavaArkistoExtractor {
    

    public static boolean EXTRACT_SERVICE = true;
    public static boolean EXTRACT_CONTRIBUTORS = true;
    public static boolean EXTRACT_EDITORS = true;
    public static boolean EXTRACT_ARKKIID = true;
    public static boolean EXTRACT_DELTAID = true;
    
    

    @Override
    public String getName() {
        return "YLE Elava arkisto article fields extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "YLE Elava arkisto terms extractor reads CSV feeds like http://elavaarkisto.kokeile.yle.fi/data/articles-additional-fields.csv";
    }
    
    
    // -------------------------------------------------------------------------

    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        CSVParser csvParser = new CSVParser();
        CSVParser.Table table = csvParser.parse(f, "UTF-8");
        return _extractTopicsFrom(table, tm);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        InputStream in = u.openStream();
        CSVParser.Table table = null;
        try {
            CSVParser csvParser = new CSVParser();
            table = csvParser.parse(in, "UTF-8");
        } 
        finally {
            in.close();
        }
        if(table != null) return _extractTopicsFrom(table, tm);
        return false;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        CSVParser csvParser = new CSVParser();
        CSVParser.Table table = csvParser.parse(in, "UTF-8");
        in.close();
        return _extractTopicsFrom(table, tm);
    }
    


    public boolean _extractTopicsFrom(CSVParser.Table table, TopicMap tm) throws Exception {
        if(table == null || tm == null) return false;
        setProgressMax(table.size());
        int i = 0;
        
        for(CSVParser.Row row : table) {
            setProgress(i++);
            if(i == 1) continue; // Skip title row
            if(row.size() == 6) {
                try {
                    String aid = stringify(row.get(0));
                    String service = stringify(row.get(1));
                    String contributors = stringify(row.get(2));
                    String editors = stringify(row.get(3));
                    String deltaid = stringify(row.get(4));
                    String arkkiid = stringify(row.get(5));
                    
                    Topic articleTopic = getElavaArkistoArticleTopic(aid, null, tm);
                    Topic articleTypeTopic = getElavaArkistoArticleType(tm);
                    
                    if(EXTRACT_CONTRIBUTORS && isValidData(contributors)) {
                        Topic contributorsTypeTopic = getElavaArkistoArticleContributorsType(tm);
                        Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                        if(contributorsTypeTopic != null && langIndependent != null) {
                            articleTopic.setData(contributorsTypeTopic, langIndependent, contributors);
                        }
                    }
                    if(EXTRACT_EDITORS && isValidData(editors)) {
                        Topic editorsTypeTopic = getElavaArkistoArticleEditorsType(tm);
                        Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                        if(editorsTypeTopic != null && langIndependent != null) {
                            articleTopic.setData(editorsTypeTopic, langIndependent, editors);
                        }
                    }
                    if(EXTRACT_ARKKIID && isValidData(arkkiid)) {
                        Topic arkkiIdTypeTopic = getElavaArkistoArticleArkkiIdType(tm);
                        Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                        if(arkkiIdTypeTopic != null && langIndependent != null) {
                            articleTopic.setData(arkkiIdTypeTopic, langIndependent, arkkiid);
                        }
                    }
                    if(EXTRACT_DELTAID && isValidData(deltaid)) {
                        Topic deltaIdTypeTopic = getElavaArkistoArticleDeltaIdType(tm);
                        Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                        if(deltaIdTypeTopic != null && langIndependent != null) {
                            articleTopic.setData(deltaIdTypeTopic, langIndependent, deltaid);
                        }
                    }
                    if(EXTRACT_SERVICE && isValidData(service)) {
                        Topic serviceTopic = getElavaArkistoServiceTopic(service, tm);
                        Topic serviceTypeTopic = getElavaArkistoServiceType(tm);
                        if(serviceTopic != null && serviceTypeTopic != null) {
                            Association a = tm.createAssociation(serviceTypeTopic);
                            a.addPlayer(articleTopic, articleTypeTopic);
                            a.addPlayer(serviceTopic, serviceTypeTopic);
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                    break;
                }
                
            }
            else {
                System.out.println("Row has invalid number of values. Skipping the row.");
            }
            if(forceStop()) break;
        }
        return true;
    }
    
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    public static final String ELAVA_ARKISTO_ARTICLE_EDITORS_TYPE_SI = ELAVA_ARKISTO_SI+"/editors";
    public static final String ELAVA_ARKISTO_ARTICLE_CONTRIBUTORS_TYPE_SI = ELAVA_ARKISTO_SI+"/contributors";
    public static final String ELAVA_ARKISTO_ARKKIID_TYPE_SI = ELAVA_ARKISTO_SI+"/arkki-id";
    public static final String ELAVA_ARKISTO_DELTAID_TYPE_SI = ELAVA_ARKISTO_SI+"/delta-id";
    
    
    
    
    public Topic getElavaArkistoArticleEditorsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ARTICLE_EDITORS_TYPE_SI, "Elava-arkisto article editors", getElavaArkistoType(tm), tm);
        return type;
    }
    public Topic getElavaArkistoArticleContributorsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ARTICLE_CONTRIBUTORS_TYPE_SI, "Elava-arkisto article contributors", getElavaArkistoType(tm), tm);
        return type;
    }
    public Topic getElavaArkistoArticleArkkiIdType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ARKKIID_TYPE_SI, "Elava-arkisto arkkiid", getElavaArkistoType(tm), tm);
        return type;
    }
    public Topic getElavaArkistoArticleDeltaIdType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_DELTAID_TYPE_SI, "Elava-arkisto deltaid", getElavaArkistoType(tm), tm);
        return type;
    }
}
