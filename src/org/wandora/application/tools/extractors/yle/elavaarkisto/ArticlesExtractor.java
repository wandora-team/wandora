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
 * ArticlesExtractor.java
 *
 * Created on 2015-04-30
 */
package org.wandora.application.tools.extractors.yle.elavaarkisto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONObject;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.CSVParser;

/**
 * Finnish broadcasting company (YLE) has published Elava arkisto 
 * metadata under the CC-BY-SA 4.0 license. Wandora features a set of extractors
 * that transfer the published metadata to Topic Maps. This extractor is one of
 * these extractor. The metadata and it's documentation is available at 
 * 
 * http://elavaarkisto.kokeile.yle.fi/data/
 * http://elavaarkisto.kokeile.yle.fi/data/articles.csv
 * 
 * @author akivela
 */


public class ArticlesExtractor extends AbstractElavaArkistoExtractor {
    
    public static boolean EXTRACT_SERVICE = true;
    public static boolean EXTRACT_LANGUAGE = true;
    public static boolean EXTRACT_PUBLISHED = true;
    
    

    @Override
    public String getName() {
        return "YLE Elava arkisto article extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "YLE Elava arkisto article extractor reads CSV feeds like http://elavaarkisto.kokeile.yle.fi/data/articles.csv";
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
                    String url = stringify(row.get(2));
                    String language = stringify(row.get(3));
                    String title = stringify(row.get(4));
                    String published = stringify(row.get(5));
                    
                    if(isValidData(aid)) {
                        Topic articleTopic = getElavaArkistoArticleTopic(aid, url, tm);
                        Topic articleTypeTopic = getElavaArkistoArticleType(tm);
                        
                        Topic idTypeTopic = getElavaArkistoArticleIdType(tm);
                        Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                        if(idTypeTopic != null && langIndependent != null) {
                            articleTopic.setData(idTypeTopic, langIndependent, aid);
                        }

                        if(isValidData(title)) {
                            articleTopic.setBaseName(title + " ("+aid+")");
                            articleTopic.setDisplayName(null, title);
                        }
                        if(EXTRACT_PUBLISHED && isValidData(published)) {
                            Topic publishedTypeTopic = getElavaArkistoArticlePublishedType(tm);
                            
                            if(publishedTypeTopic != null && langIndependent != null) {
                                articleTopic.setData(publishedTypeTopic, langIndependent, published);
                            }
                            try {
                                published = published.trim();
                                SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                                Date date = dateParser.parse(published);
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                String formattedDate = formatter.format(date);
                                Topic englishLangTopic = tm.getTopic(XTMPSI.getLang("en"));
                                if(publishedTypeTopic != null &&  englishLangTopic != null && formattedDate != null) {
                                    articleTopic.setData(publishedTypeTopic, englishLangTopic, formattedDate);
                                }
                            }
                            catch(Exception e) {
                                log("Coudn't parse article publish date '"+published+"'");
                                e.printStackTrace();
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
                        if(EXTRACT_LANGUAGE && isValidData(language)) {
                            Topic languageTopic = TMBox.getLangTopic(articleTopic, language);
                            Topic languageType = tm.getTopic(TMBox.LANGUAGE_SI);
                            if(languageTopic != null && languageType != null) {
                                Association a = tm.createAssociation(languageType);
                                a.addPlayer(articleTopic, articleTypeTopic);
                                a.addPlayer(languageTopic, languageType);
                            }
                        }
                    }
                    else {
                        log("Invalid article identifier found. Skipping the row.");
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
    
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    

    
    
    
    public Topic getElavaArkistoArticlePublishedType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ARTICLE_PUBLISHED_TYPE_SI, "Elava-arkisto article published", getElavaArkistoType(tm), tm);
        return type;
    }
    
}



/*

Example of extracted CSV:

AID,SERVICE,URL,LANGUAGE,TITLE,PUBLISHED
7-916094,arkivet,http://svenska.yle.fi/artikel/2015/04/08/eurovisionen-ar-1974,sv,"Eurovisionen år 1974",2015-04-08T18:39:57+03:00
7-915599,arkivet,http://svenska.yle.fi/artikel/2015/04/07/eurovisionen-ar-1973,sv,"Eurovisionen år 1973",2015-04-07T19:23:59+03:00
7-915523,arkivet,http://svenska.yle.fi/artikel/2015/04/07/eurovisionen-ar-1972,sv,"Eurovisionen år 1972",2015-04-07T16:41:27+03:00
7-913620,arkivet,http://svenska.yle.fi/artikel/2015/04/06/43-000-ord-men-ingen-karlek,sv,"43 000 ord men ingen kärlek",2015-04-06T13:00:00+03:00
7-913066,arkivet,http://svenska.yle.fi/artikel/2015/04/06/historiekollen-landsbygdspartiet-blev-sannfinlandskt,sv,"Historiekollen: Landsbygdspartiet blev Sannfinländskt",2015-04-06T09:00:00+03:00
7-915098,arkivet,http://svenska.yle.fi/artikel/2015/04/06/eurovisionen-ar-1971,sv,"Eurovisionen år 1971",2015-04-06T03:36:42+03:00


where

    AID = artikkelin Yle ID (Article ID)
    SERVICE = artikkelin lähde (Elävä arkisto vai Arkivet)
    URL = artikkelin HTML-version osoite
    LANGUAGE = artikkelin kieli (ISO 639-1 -muodossa: fi tai sv)
    TITLE = artikkelin otsikko
    PUBLISHED = julkaisuaika (ISO 8601 -muoto)


*/