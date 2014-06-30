/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * SQLTopicMapImport.java
 *
 * Created on 20. kesäkuuta 2006, 10:24
 *
 */

package org.wandora.application.tools.importers;


import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.database.*;

import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;

import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.utils.crawler.handlers.*;

import java.io.*;
import java.net.*;
import java.util.*;
import org.wandora.utils.swing.*;
import org.wandora.utils.*;
import javax.swing.*;
import java.sql.*;



/**
 * @author akivela
 */
public class SQLTopicMapImport extends AbstractImportTool implements WandoraTool {
    
    
    
    /** Creates a new instance of SQLTopicMapImport */
    public SQLTopicMapImport() {
    }
    public SQLTopicMapImport(int options) {
        setOptions(options);
    }
    
    
    public String getDescription() {
        return "Injects SQL file(s) into a database topic map layer.";
    }  
    public String getName() {
        return "SQL topic map import";
    }
    
    
    
    
    public void execute(Wandora admin, Context context) {
        TopicMap topicMap = solveContextTopicMap(admin, context);
        if(topicMap instanceof DatabaseTopicMap) {
            super.execute(admin, context);
        }
        else {
            log("Selected topic map is not a database topic map! Unable to import SQL file.");
        }
    }
    
    
    
    
    public void importStream(Wandora admin, String streamName, InputStream inputStream) {
        BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream) );
        
        TopicMap topicMap = solveContextTopicMap(admin, getContext());
        if(topicMap instanceof DatabaseTopicMap) {
            setDefaultLogger();
            int count = 0;
            int errorCount = 0;
            try {
                DatabaseTopicMap dbTopicMap = (DatabaseTopicMap) topicMap;
                Connection connection = dbTopicMap.getConnection();
                String query = reader.readLine();
                while(query != null && query.length() > 0 && !forceStop()) {
                    try {
                        dbTopicMap.executeUpdate(query, connection);
                        query = reader.readLine();
                        count++;
                    }
                    catch(Exception e) {
                        log(e);
                        errorCount++;
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
            log("Total " + count + " SQL lines injected into the database topic map.");
            if(errorCount > 0) log("Total " + errorCount + " errors encountered during inject.");
            setState(WAIT);
        }
        else {
           log("Selected topic map is not a database topic map! Unable to import SQL file."); 
        }
    }


    public String getGUIText(int textType) {
        switch(textType) {
            case FILE_DIALOG_TITLE_TEXT: {
                return "Select SQL file to import";
            }
            case URL_DIALOG_MESSAGE_TEXT: {
                return "Type the internet address of a SQL document to be imported";
            }
        }
        return "";
    }

}
