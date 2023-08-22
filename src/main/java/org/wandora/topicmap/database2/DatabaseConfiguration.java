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
 * 
 *
 * DatabaseConfiguration.java
 *
 */

package org.wandora.topicmap.database2;


import java.util.ArrayList;
import java.util.Collection;

import org.wandora.application.Wandora;
import org.wandora.application.gui.DatabaseConfigurationPanel;
import org.wandora.topicmap.TopicMapConfigurationPanel;
import org.wandora.utils.Options;


/**
 *
 * @author  olli
 */
public class DatabaseConfiguration extends TopicMapConfigurationPanel {
    
    private static final long serialVersionUID = 1L;
    
    private DatabaseConfigurationPanel confPanel;
    private Options options;

    
    /** Creates new form DatabaseConfiguration */
    public DatabaseConfiguration(Wandora wandora, Options options) {
        initComponents();
        this.options=options;
        confPanel=new DatabaseConfigurationPanel(wandora);
        initialize(options);
        this.add(confPanel);
    }
    
    
    public static Collection<DatabaseConfigurationPanel.StoredConnection> parseConnections(Options options){
        Collection<DatabaseConfigurationPanel.StoredConnection> connections=
                new ArrayList<DatabaseConfigurationPanel.StoredConnection>();
        String prefix="options.dbconnections.connection";
        int counter=0;
        while(true){
            String type=options.get(prefix+"["+counter+"].type");
            if(type==null) break;
            String name=options.get(prefix+"["+counter+"].name");
            String user=options.get(prefix+"["+counter+"].user");
            String pass=options.get(prefix+"["+counter+"].pass");
            if(type.equals(DatabaseConfigurationPanel.GENERIC_TYPE)){
                String driver=options.get(prefix+"["+counter+"].driver");
                String conString=options.get(prefix+"["+counter+"].constring");
                String script=options.get(prefix+"["+counter+"].script");
                connections.add(DatabaseConfigurationPanel.StoredConnection.generic(name,type,driver,conString,user,pass,script));
            }
            else{
                String server=options.get(prefix+"["+counter+"].server");
                String database=options.get(prefix+"["+counter+"].database");
                connections.add(DatabaseConfigurationPanel.StoredConnection.known(name,type,server,database,user,pass));
            }
            counter++;
        }
        return connections;
    }
    
    
    
    public void initialize(Options options){
        Collection<DatabaseConfigurationPanel.StoredConnection> connections=parseConnections(options);
        confPanel.setConnections(connections);
    }
    
    
    
    public void writeOptions(Options options){
        writeOptions(options,confPanel);
    }
    
    
    public static void writeOptions(Options options,DatabaseConfigurationPanel confPanel){
        int counter=0;
        String prefix="options.dbconnections.connection";
        while(true){
            String type=options.get(prefix+"["+counter+"].type");
            if(type==null) break;
            options.put(prefix+"["+counter+"].type",null);
            options.put(prefix+"["+counter+"].name",null);
            options.put(prefix+"["+counter+"].user",null);
            options.put(prefix+"["+counter+"].pass",null);
            options.put(prefix+"["+counter+"].driver",null);
            options.put(prefix+"["+counter+"].constring",null);
            options.put(prefix+"["+counter+"].server",null);
            options.put(prefix+"["+counter+"].database",null);
            counter++;
        }
        Collection<DatabaseConfigurationPanel.StoredConnection> connections=confPanel.getAllConnections();
        counter=0;
        for(DatabaseConfigurationPanel.StoredConnection sc : connections){
            options.put(prefix+"["+counter+"].type",sc.type);
            options.put(prefix+"["+counter+"].name",sc.name);
            options.put(prefix+"["+counter+"].user",sc.user);
            options.put(prefix+"["+counter+"].pass",sc.pass);
            if(sc.type.equals(DatabaseConfigurationPanel.GENERIC_TYPE)){
                options.put(prefix+"["+counter+"].driver",sc.driver);
                options.put(prefix+"["+counter+"].constring",sc.conString);
                options.put(prefix+"["+counter+"].script",sc.script);
            }
            else{
                options.put(prefix+"["+counter+"].server",sc.server);
                options.put(prefix+"["+counter+"].database",sc.database);                
            }
            counter++;
        }
    }
    
    
    @Override
    public Object getParameters(){
        writeOptions(options);
        DatabaseConfigurationPanel.StoredConnection sc=confPanel.getSelectedConnection();
        if(sc==null) return null;
        return sc;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
