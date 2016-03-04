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
 * ArticleTagsExtractor.java
 *
 * Created on 2015-04-30
 */

package org.wandora.application.tools.extractors.yle.elavaarkisto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.CSVParser;

/**
 * Finnish broadcasting company (YLE) has published Elava arkisto 
 * metadata under the CC-BY-SA 4.0 license. Wandora features a set of extractors
 * that transfer the published metadata to Topic Maps. This extractor is one of
 * these extractor. The metadata and it's documentation is available at 
 * 
 * http://elavaarkisto.kokeile.yle.fi/data/
 * http://elavaarkisto.kokeile.yle.fi/data/article-tags.csv
 * 
 * @author akivela
 */


public class ArticleTagsExtractor extends AbstractElavaArkistoExtractor {
    
    public static boolean EXTRACT_SERVICE = false;
    public static boolean EXTRACT_RELATION = true;
    public static boolean EXTRACT_LANGUAGE = true;
    
    

    @Override
    public String getName() {
        return "YLE Elava arkisto article tags extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "YLE Elava arkisto article tag extractor reads CSV feeds like http://elavaarkisto.kokeile.yle.fi/data/article-tags.csv";
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        CSVParser csvParser = new CSVParser();
        CSVParser.Table table = csvParser.parse(f, "UTF-8");
        //CSVParser.Table table = csvParser.parse(f);
        return _extractTopicsFrom(table, tm);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        InputStream in = u.openStream();
        CSVParser.Table table = null;
        try {
            CSVParser csvParser = new CSVParser();
            table = csvParser.parse(in, "UTF-8");
            //table = csvParser.parse(in);
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
            if(i == 1) continue;
            if(row.size() == 6) {
                try {
                    String aid = stringify(row.get(0));
                    String service = stringify(row.get(1));
                    String kid = stringify(row.get(2));
                    String language = stringify(row.get(3));
                    String label = stringify(row.get(4));
                    String rel = stringify(row.get(5));
                    
                    Topic articleTopic = getElavaArkistoArticleTopic(aid, null, tm);
                    Topic articleType = getElavaArkistoArticleType(tm);
                    
                    Topic tagTopic = getElavaArkistoTagTopic(kid, label, tm);
                    Topic tagType = getElavaArkistoTagType(tm);
                    if(tagTopic != null && tagType != null) {
                        Association a = tm.createAssociation(tagType);
                        a.addPlayer(articleTopic, articleType);
                        a.addPlayer(tagTopic, tagType);
                        
                        if(EXTRACT_SERVICE && isValidData(service)) {
                            Topic serviceTopic = getElavaArkistoServiceTopic(service, tm);
                            Topic serviceType = getElavaArkistoServiceType(tm);
                            if(serviceTopic != null && serviceType != null) {
                                a.addPlayer(serviceTopic, serviceType);
                            }
                        }
                        
                        if(EXTRACT_RELATION && isValidData(rel)) {
                            Topic relationTopic = getElavaArkistoTagArticleRelationTopic(rel, tm);
                            Topic relationType = getElavaArkistoTagArticleRelationType(tm);
                            if(relationTopic != null && relationType != null) {
                                a.addPlayer(relationTopic, relationType);
                            }
                        }
                        
                        if(EXTRACT_LANGUAGE && isValidData(language)) {
                            Topic languageTopic = TMBox.getLangTopic(articleTopic, language);
                            Topic languageType = tm.getTopic(TMBox.LANGUAGE_SI);
                            if(languageTopic != null && languageType != null) {
                                Association al = tm.createAssociation(languageType);
                                al.addPlayer(tagTopic, tagType);
                                al.addPlayer(languageTopic, languageType);
                            }
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
            if(forceStop()) {
                log("Extraction stopped.");
                break;
            }
        }
        return true;
    }
    
}


/*

Example of extracted CSV:

AID,SERVICE,KID,LANGUAGE,LABEL,REL
7-908526,arkivet,/program/spotlight,sv,Spotlight,
7-908526,arkivet,/term/finto/httpwwwysofiontokokop32958/politik,sv,politik,
7-908526,arkivet,http://www.yso.fi/onto/koko/p32958,sv,politik,dc:subject
7-908526,arkivet,/term/finto/httpwwwysofiontokokop32580/partier,sv,partier,
7-908526,arkivet,http://www.yso.fi/onto/koko/p32580,sv,partier,dc:subject
7-908526,arkivet,/term/finto/httpwwwysofiontokokop32366/val,sv,val,
7-908526,arkivet,http://www.yso.fi/onto/koko/p32366,sv,val,dc:subject


where

    AID = artikkelin Yle ID (Article ID)
    SERVICE = artikkelin lähde (Elävä arkisto vai Arkivet)
    KID = termin Yle ID (Keyword ID)
    LANGUAGE = termin kieli
    LABEL = termin nimi
    REL = termin suhde artikkeliin: dc:subject (=aihe), dc:partOf (=osa kokonaisuus)


*/