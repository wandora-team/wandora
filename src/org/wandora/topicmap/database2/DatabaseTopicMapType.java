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
 * 
 *
 * DatabaseTopicMapType.java
 *
 * Created on 21. marraskuuta 2005, 13:48
 */

package org.wandora.topicmap.database2;


import org.wandora.utils.Options;
import org.wandora.application.tools.layers.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.DatabaseConfigurationPanel;
import org.wandora.application.gui.UIBox;
import static org.wandora.utils.Tuples.*;
import java.sql.*;
import java.io.*;
import javax.swing.*;


/**
 *
 * @author olli
 */
public class DatabaseTopicMapType implements TopicMapType {
    
    /** Creates a new instance of DatabaseTopicMapType */
    public DatabaseTopicMapType() {
    }
    
    @Override
    public String getTypeName(){
        return "Database";
    }
    
    
    @Override
    public TopicMap createTopicMap(Object params){
        DatabaseConfigurationPanel.StoredConnection sc = (DatabaseConfigurationPanel.StoredConnection)params;
        if(sc != null) {
            T2<String,String> conInfo = DatabaseConfigurationPanel.getConnectionDriverAndString(sc);
            if(conInfo != null) {
                try {
                    return new DatabaseTopicMap(
                            conInfo.e1,
                            conInfo.e2,
                            sc.user,
                            sc.pass,
                            sc.script,
                            params
                    );
                }
                catch(java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                }
            }
        }
        return null;
    }
    
    
    @Override
    public TopicMap modifyTopicMap(TopicMap tm, Object params) {
        TopicMap ret = createTopicMap(params);
        ret.addTopicMapListeners(tm.getTopicMapListeners());
        return ret;
    }
    
    
    @Override
    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options) {
        DatabaseConfiguration dc = new DatabaseConfiguration(admin, options);
        return dc;
    }
    
    
    @Override
    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora wandora, Options options, TopicMap tm) {
        DatabaseConfigurationPanel dcp = new DatabaseConfigurationPanel(wandora);
        DatabaseTopicMap dtm = (DatabaseTopicMap)tm;
        Object params = dtm.getConnectionParams();
        if(params == null) {
            params = DatabaseConfigurationPanel.StoredConnection.generic(
                    "Unknown connection",
                    DatabaseConfigurationPanel.GENERIC_TYPE,
                    dtm.getDBDriver(),
                    dtm.getDBConnectionString(),
                    dtm.getDBUser(),
                    dtm.getDBPassword()
            );
        }
        return dcp.getEditConfigurationPanel(params);
    }    
    
    
    @Override
    public String toString() {
        return getTypeName();
    }

    
    
    @Override
    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) throws IOException {
        String pathpre="";
        if(path.length() > 0) {
            pathpre = path+"/";
        }
        DatabaseTopicMap dtm = (DatabaseTopicMap)tm;
        Options options = new Options();
        Object params = dtm.getConnectionParams();
        if(params != null) {
            DatabaseConfigurationPanel.StoredConnection p=(DatabaseConfigurationPanel.StoredConnection)params;
            p.writeOptions(options,"params.");
        }
        else {
            options.put("driver",dtm.getDBDriver());
            options.put("connectionstring",dtm.getDBConnectionString());
            options.put("user",dtm.getDBUser());
            options.put("password",dtm.getDBPassword());
        }
        out.nextEntry(pathpre+"dboptions.xml");
        options.save(new java.io.OutputStreamWriter(out.getOutputStream()));
    }

    
    @Override
    public TopicMap unpackageTopicMap(TopicMap topicmap, PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws IOException {
        String pathpre = "";
        if(path.length() > 0) {
            pathpre=path+"/";
        }
        in.gotoEntry(pathpre+"dboptions.xml");
        Options options = new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        if(options.get("params.name") != null) {
            DatabaseConfigurationPanel.StoredConnection p = new DatabaseConfigurationPanel.StoredConnection();
            p.readOptions(options,"params.");
            T2<String,String> conInfo = DatabaseConfigurationPanel.getConnectionDriverAndString(p);
            try {
                return new DatabaseTopicMap(
                        conInfo.e1,
                        conInfo.e2,
                        p.user,
                        p.pass,
                        p.script,
                        p
                );
            }
            catch(SQLException sqle){
                logger.log(sqle);
                return null;
            }
        }
        else {
            try {
                DatabaseTopicMap dtm=new DatabaseTopicMap(
                        options.get("driver"),
                        options.get("connectionstring"),
                        options.get("user"),
                        options.get("password")
                );
                return dtm;
            }
            catch(SQLException sqle) {
                logger.log(sqle);
                return null;
            }
        }
    }
    
    
    
    @Override
    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws IOException {
        String pathpre = "";
        if(path.length()>0) {
            pathpre = path+"/";
        }
        in.gotoEntry(pathpre+"dboptions.xml");
        Options options = new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        if(options.get("params.name") != null) {
            DatabaseConfigurationPanel.StoredConnection p = new DatabaseConfigurationPanel.StoredConnection();
            p.readOptions(options,"params.");
            T2<String,String> conInfo = DatabaseConfigurationPanel.getConnectionDriverAndString(p);
            try {
                return new DatabaseTopicMap(
                        conInfo.e1,
                        conInfo.e2,
                        p.user,
                        p.pass,
                        p.script,
                        p
                );
            }
            catch(SQLException sqle){
                logger.log(sqle);
                return null;
            }
        }
        else{
            try{
                DatabaseTopicMap dtm=new DatabaseTopicMap(
                        options.get("driver"),
                        options.get("connectionstring"),
                        options.get("user"),
                        options.get("password")
                );
                return dtm;
            }catch(SQLException sqle){
                logger.log(sqle);
                return null;
            }
        }
    }
    
    
    @Override
    public JMenuItem[] getTopicMapMenu(TopicMap tm,Wandora admin) {
        JMenu menu=UIBox.makeMenu(
                new Object[] {
                    "Make SI consistent...", new MakeSIConsistentTool(),
                },
                admin
        );
        JMenuItem[] items = new JMenuItem[menu.getItemCount()];
        for(int i=0; i<items.length; i++) {
            items[i]=menu.getItem(i);
            items[i].setIcon(null);
        }
        return items;
    }
    
    
    @Override
    public Icon getTypeIcon() {
        //return UIBox.getIcon("gui/icons/layerinfo/layer_type_database.png");
        return UIBox.getIcon(0xf1c0);
    }
}
