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
 */



package org.wandora.application.tools.extractors.csv;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import org.wandora.application.Wandora;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.CSVParser;
import org.wandora.utils.CSVParser.Row;
import org.wandora.utils.CSVParser.Table;

/**
 *
 * @author akivela
 */


public class SimpleCSVAssociationExtractor extends AbstractCSVExtractor {

    

    @Override
    public String getName() {
        return "Simple CSV association extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert CSV file to topics and associations. Each row in CSV file is transformed "+
               "into an association. Each column is transformed into a player topic in the association.";
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    


    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select CSV file(s) or directories containing CSV files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while reading CSV files!";
        
            case FILE_PATTERN: return ".*\\.(csv|CSV|txt|TXT)";
            
            case DONE_FAILED: return "Done! No extractions! %1 CSV file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 CSV file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 CSV files crawled!";
            
            case LOG_TITLE: return "Simple CSV Extraction Log";
        }
        return "";
    }
    
    
    
    
    @Override
    public boolean _extractTopicsFrom(Table table, TopicMap tm) throws Exception {
        if(table == null || tm == null) return false;
        
        Topic csvType = getCSVAssociationType(tm);
        for(Row row : table) {
            int i = 0;
            Association a = tm.createAssociation(csvType);
            for(Object item : row) {
                Topic role = getCSVRole(i, tm);
                Topic player = getCSVTopic(item, tm);
                a.addPlayer(player, role);
                i++;
            }
            if(i == 0) {
                a.remove();
            }
        }
        return true;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public Topic getCSVAssociationType(TopicMap tm) throws Exception {
        long stamp = System.currentTimeMillis();
        return ExtractHelper.getOrCreateTopic("http://wandora.org/si/csv/association/"+stamp, "CSV association "+stamp, tm);
    }
    
    public Topic getCSVRole(int i, TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic("http://wandora.org/si/csv/role/"+i, "CSV role "+i, tm);
    }
    
    public Topic getCSVTopic(Object o, TopicMap tm) throws Exception {
        String str = urlEncode(o.toString());
        return ExtractHelper.getOrCreateTopic("http://wandora.org/si/csv/"+str, str, tm);
    }
    
    
}
