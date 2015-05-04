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
 * MediaArticleExtractor.java
 *
 * Created on 2015-04-30
 */
package org.wandora.application.tools.extractors.yle.elavaarkisto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.wandora.topicmap.Association;
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
 * http://elavaarkisto.kokeile.yle.fi/data/media-article.csv
 *
 * @author akivela
 */


public class MediaArticleExtractor extends AbstractElavaArkistoExtractor {
    
    
    public static boolean EXTRACT_SERVICE = false;
    
    

    @Override
    public String getName() {
        return "YLE Elava arkisto media-article extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "YLE Elava arkisto terms extractor reads CSV feeds like http://elavaarkisto.kokeile.yle.fi/data/media-article.csv";
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
            if(row.size() == 3) {
                try {
                    String aid = stringify(row.get(0));
                    String service = stringify(row.get(1));
                    String mid = stringify(row.get(2));
                    
                    Topic articleTopic = getElavaArkistoArticleTopic(aid, null, tm);
                    Topic articleType = getElavaArkistoArticleType(tm);

                    Topic mediaTopic = getElavaArkistoMediaTopic(mid, tm);
                    Topic mediaType = getElavaArkistoMediaType(tm);
                    if(mediaTopic != null && mediaType != null) {
                        Association a = tm.createAssociation(mediaType);
                        a.addPlayer(articleTopic, articleType);
                        a.addPlayer(mediaTopic, mediaType);
                        
                        if(EXTRACT_SERVICE && isValidData(service)) {
                            Topic serviceTopic = getElavaArkistoServiceTopic(service, tm);
                            Topic serviceType = getElavaArkistoServiceType(tm);
                            if(serviceTopic != null && serviceType != null) {
                                a.addPlayer(serviceTopic, serviceType);
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
            if(forceStop()) break;
        }
        return true;
    }
    
    
}
