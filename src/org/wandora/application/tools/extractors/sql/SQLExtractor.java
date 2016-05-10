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
 * SQLExtractor.java
 *
 */

package org.wandora.application.tools.extractors.sql;


import java.io.File;
import java.net.URL;
import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.wandora.topicmap.TMBox;




/**
 *
 * @author akivela
 */
public class SQLExtractor extends AbstractExtractor {

    public static String DEFAULT_SI_ENCODING = "UTF-8";
    public static String DEFAULT_LANG = "en";

    public static String RESULTSET_SI = "http://wandora.org/si/sql/resultset";
    public static String COLUMN_SI = "http://wandora.org/si/sql/resultset/column";
    public static String VALUE_SI = "http://wandora.org/si/sql/resultset/value";

    private SQLExtractorUI ui = null;
    public static final String defaultOccurrenceScopeSI = TMBox.LANGINDEPENDENT_SI;


    @Override
    public String getName() {
        return "SQL extractor";
    }

    @Override
    public String getDescription(){
        return "Transforms SQL result set to a topic map. Extractor requires SQL database.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_sql.png");
    }



    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            if(ui == null) {
                ui = new SQLExtractorUI(wandora);
            }

            ui.open(wandora);

            if(ui.wasAccepted()) {
                setDefaultLogger();
                //log("Extracting SQL result set...");
                TopicMap tm = wandora.getTopicMap();
                String query = ui.getQuery(this);
                String url = ui.getURL();
                int c = 0;
                if(query != null && query.length() > 0) {
                    Connection connection = null;
                    try {
                        //log("Opening SQL connection...");
                        connection = DriverManager.getConnection(url);
                        Statement stmt = connection.createStatement();
                        log("Executing SQL query...");
                        ResultSet rs = stmt.executeQuery(query);
                        //connection.commit();
                        log("Transforming SQL result set to a topic map...");
                        handleResultSet(rs, tm);
                        c++;
                    }
                    catch(java.sql.SQLException esql) {
                        log(esql.getMessage());
                    }
                    catch(Exception e) {
                        log(e);
                    }
                    finally {
                        if(connection != null) {
                            //log("Closing SQL connection...");
                            connection.close();
                        }
                    }
                    if(c == 0) log("No valid query given. Aborting.");
                    log("Ready.");
                }
            }
            else {
                // log("User cancelled the extraction!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(ui != null && ui.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }



    // -------------------------------------------------------------------------


    public void handleResultSet(ResultSet rs, TopicMap tm) throws Exception {
        if(rs == null) {
            log("Warning: no result set available!");
            return;
        }
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rs.getMetaData().getColumnCount();
        List<String> columns = new ArrayList<String>();
        List<String> tableColumns = new ArrayList<String>();
        for(int i=1; i<numColumns+1; i++) {
            String columnName = rsmd.getColumnName(i);
            columns.add(columnName);
            String tableName = rsmd.getTableName(i);
            if(columnName != null && tableName != null) {
                tableColumns.add(columnName+"."+tableName);
            }
            else if(columnName != null) {
                tableColumns.add(columnName);
            }
            else {
                tableColumns.add("column-"+i);
            }
        }
        int counter = 0;
        while(rs.next() && !forceStop()) {
            HashMap<Topic,Topic> roledPlayers = new HashMap();
            for(int i=1; i<numColumns+1; i++) {
                try {
                    if(forceStop()) break;
                    String x = rs.getString(i);
                    if(x == null || x.length() == 0) continue;
                    String col = columns.get(i-1);
                    String colName = tableColumns.get(i-1);
                    Topic role = getRoleTopic(colName, tm);
                    Topic player = null;

                    String sif = x;
                    if(x.length() > 128) sif = sif.substring(0, 127)+"_"+sif.hashCode();
                    String uri = VALUE_SI + "/" + encode(sif);
                    String name = x;
                    String lang = DEFAULT_LANG;
                    player = createTopic(tm, uri, name);
                    //System.out.println("lang=="+lang+" name=="+name);
                    player.setDisplayName(lang, name);

                    if(role != null && player != null) {
                        roledPlayers.put(role, player);
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            if(!roledPlayers.isEmpty()) {
                Topic associationType = getAssociationType(rs, tm);
                if(associationType != null) {
                    Association a = tm.createAssociation(associationType);
                    for( Topic role : roledPlayers.keySet() ) {
                        a.addPlayer(roledPlayers.get(role), role);
                    }
                }
            }
            counter++;
            setProgress(counter);
            if(counter % 100 == 0) hlog("Result set rows processed: " + counter);
        }
        log("Total result set rows processed: " + counter);
    }


    public Topic getAssociationType(ResultSet rs, TopicMap tm) throws Exception {
        Topic atype = createTopic(tm, RESULTSET_SI+"/"+rs.hashCode(), "SQL Result Set "+rs.hashCode());
        Topic settype = createTopic(tm, RESULTSET_SI, "SQL Result Set");
        Topic wandoratype = createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        settype.addType(wandoratype);
        atype.addType(settype);
        return atype;
    }


    public Topic getRoleTopic(String role, TopicMap tm) throws Exception {
        return createTopic(tm, COLUMN_SI+"/"+encode(role), role);
    }


    public String encode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_SI_ENCODING);
        }
        catch(Exception e) {
            return str;
        }
    }



    // -------------------------------------------------------------------------


    

    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }



    // -------------------------------------------------------------------------






}
