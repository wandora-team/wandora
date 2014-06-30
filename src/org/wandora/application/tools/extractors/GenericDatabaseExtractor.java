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
 * DatabaseImport.java
 *
 * Created on 30. kesäkuuta 2006, 14:14
 *
 */

package org.wandora.application.tools.extractors;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.database.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import javax.swing.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * TODO:
 *  - Test that extending tables works properly, extend chains, no unnecessary associations
 *    from extending columns.
 *  - Test that all different kinds of tables work properly, different values of
 *    hasReferences and hasNonReferences (test this with extending another table and not).
 *  - Test that not including columns works properly.
 *
 * @author olli
 */
public class GenericDatabaseExtractor extends AbstractWandoraTool {
    
    /** Creates a new instance of GenericDatabaseExtractor */
    public GenericDatabaseExtractor() {
    }

    @Override
    public String getName() {
        return "Database Extractor";
    }
    @Override
    public String getDescription() {
        return "Extracts and converts data from database to topic map.";
    }
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createExtractType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_database.png");
    }

    
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        setDefaultLogger();        
        DatabaseConfigurationDialog d=new DatabaseConfigurationDialog(admin,true);
        d.setTitle("Select extracted database");
        d.setVisible(true);
        if(d.wasCancelled()) return;
        DatabaseConfigurationPanel.StoredConnection sc=d.getSelection();
        if(sc==null) return;
        
        T2<String,String> params=DatabaseConfigurationPanel.getConnectionDriverAndString(sc);
        
        Connection con=null;
        try{
            log("Connecting to database");
            Class.forName(params.e1);
            con=DriverManager.getConnection(params.e2,sc.user,sc.pass);
            DatabaseMetaData metadata=con.getMetaData();
            DatabaseSchema schema=getDatabaseSchema(metadata,null,sc.user);
                                
            setDefaultBaseNameColumns(schema);
            System.out.println("Found tables:");
            schema.print(System.out);
            
            if(!configureSchema(admin,schema)){ return; }

            TopicMap tm=admin.getTopicMap();
            if(tm instanceof LayerStack){
                tm=((LayerStack)tm).getSelectedLayer().getTopicMap();
            }
            boolean resetIndexes=false;
            if(tm instanceof DatabaseTopicMap){
                ((DatabaseTopicMap)tm).setCompleteIndex();
                resetIndexes=true;
            }
            boolean oldConsistencyCheck=tm.getConsistencyCheck();
            tm.setConsistencyCheck(false);
            tm.disableAllListeners();
            try{
                makeTopicMap(tm,con,schema);
            
                if(resetIndexes) ((DatabaseTopicMap)tm).resetCompleteIndex();
            }
            finally{
                tm.enableAllListeners();
                tm.setConsistencyCheck(oldConsistencyCheck);
            }
            
        }
        catch(Exception e){
            log(e);
            throw new TopicMapException(e);
        }
        finally{
            try{
                if(con!=null) con.close();
            }catch(SQLException sqle){sqle.printStackTrace();}
        }
    }    
    
    private boolean configureSchema(Wandora admin,DatabaseSchema schema){
        GenericDatabaseExtractorConfigurationDialog d=new GenericDatabaseExtractorConfigurationDialog(admin,true,schema);
        d.setVisible(true);
        if(d.wasCancelled()) return false;
        d.updateSchema(schema);
        return true;
    }
    
    
    public static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd=rs.getMetaData();
        while(rs.next()){
            for(int i=0;i<rsmd.getColumnCount();i++){
                System.out.println(rsmd.getColumnLabel(i+1)+": "+rs.getObject(i+1));
            }
            System.out.println("--------------------------");
        }        
    }
    

    public static String makeIdentifier(String[] row, DBColumn[] columns, String table) throws SQLException {
        String identifier="";
        for(int i=0;i<row.length;i++){
            DBColumn column=columns[i];
            if(!column.primaryKey) continue;
            if(identifier.length()>0) identifier+="--";
            identifier+=row[i];
        }
        return identifier;
    }
    
    public static Topic getOrCreateTopic(TopicMap tm,String s) throws TopicMapException {
        Topic t=tm.getTopic(s);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(s));
        }
        return t;
    }
    
    public HashMap<String,Topic> topicCache;
    
    public Topic getOrCreateCached(TopicMap tm,String s) throws TopicMapException {
        Topic t=topicCache.get(s);
        if(t==null) {
            t=getOrCreateTopic(tm,s);
            topicCache.put(s,t);
        }
        return t;
    }
    
    public static DBColumn findColumn(Collection<DBColumn> columns,String name){
        for(DBColumn column : columns){
            if(column.column.equals(name)) return column;
        }
        return null;
    }
    
    public static String cropBaseName(String value, String postfix){
        if(postfix==null){
            if(value.length()>255) return value.substring(0,255);
            else return value;
        }
        if(value.length()+postfix.length()+3>255){
            return value.substring(0,255-3-postfix.length())+" ("+postfix+")";
        }
        else return value+" ("+postfix+")";
    }
    
    public void makeTopicMap(TopicMap tm,Connection con,DatabaseSchema schema) throws SQLException,TopicMapException {
        log("Extracting database");
        topicCache=new HashMap<String,Topic>();
        // Set base names to data types, roles association types etc
        for(String table : schema.tables){
            ArrayList<DBColumn> columns=schema.columns.get(table);
            boolean hasNonReferences=false;
            boolean hasReferences=false;
            boolean includedColumns=false;
            for(DBColumn column : columns){
                if(!column.include) continue;
                includedColumns=true;
                if(column.references!=null) hasReferences=true;
                else hasNonReferences=true;
            }
            if(!includedColumns) continue;
            if(hasNonReferences){
                Topic type=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/type/"+table);
                type.setBaseName(table+" (type)");
                for(DBColumn column : columns){
                    if(!column.include) continue;
                    if(column.references!=null) continue;
                    if(column.makeTopics){
                        Topic atype=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/type/"+table+"/"+column.column);
                        atype.setBaseName(table+"/"+column.column+" (association type)");
                        Topic otherRole=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/role/"+table+"/"+column.column);
                        otherRole.setBaseName(table+"/"+column.column+" (role)");
                        Topic thisRole=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/role/"+table);
                        thisRole.setBaseName(table+" (role)");
                    }
                    else{
                        Topic dataType=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/datatype/"+table+"/"+column.column);
                        dataType.setBaseName(column.column+" (data type)");
                    }
                }                
            }
            if(hasReferences && !hasNonReferences){
                Topic associationType=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/association/"+table);
                associationType.setBaseName(table+" (association type)");
                for(DBColumn column : columns){
                    if(!column.include) continue;
                    Topic role=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/role/"+table+"/"+column.column);
                    role.setBaseName(table+"/"+column.column+" (role)");
                }
            }
            else if(hasReferences) {
                Topic thisRole=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/role/"+table);
                thisRole.setBaseName(table+" (role)");
                for(DBColumn column : columns){
                    if(!column.include) continue;
                    if(column.references==null) continue;
                    Topic type=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/type/"+table+"/"+column.column);
                    type.setBaseName(table+"/"+column.column+" (association type)");
                    Topic otherRole=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/role/"+column.references.table+"/"+column.references.column);
                    otherRole.setBaseName(column.references.table+"/"+column.references.column+" (role)");
                }
            }            
        }
        
        // resolve which table extends what
        HashMap<String,String> extendsTableMap=new HashMap<String,String>();
        for(String table : schema.tables){
            ArrayList<DBColumn> columnsOriginal=schema.columns.get(table);
            String extendsTable=null;
            for(DBColumn column : columnsOriginal){
                if(!column.include) continue;
                if(column.primaryKey && (extendsTable!=null || column.references==null)){
                    extendsTable=null;
                    break;
                }
                if(column.references!=null && column.primaryKey){
                    extendsTable=column.references.table;
                }
            }            
            if(extendsTable!=null) extendsTableMap.put(table,extendsTable);
        }        
        // straighten links when tables extend tables that extend other tables
        for(String table : schema.tables){
            String extendsTable=extendsTableMap.get(table);
            if(extendsTable!=null){
                while(extendsTableMap.get(extendsTable)!=null) extendsTable=extendsTableMap.get(extendsTable);
                extendsTableMap.put(table,extendsTable);
            }
        }
        
        // process data, one table at a time
        for(String table : schema.tables) {
            // check if no column of this table is included, in that case skip table completely
            boolean hasColumns=false;
            ArrayList<DBColumn> columnsOriginal=schema.columns.get(table);
            for(DBColumn column : columnsOriginal){
                if(!column.include) continue;
                hasColumns=true;
                break;
            }
            if(!hasColumns){
                log("No columns for table "+table+". Skipping.");
                continue;
            }
            
            // get table data
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from "+table);
            try{
            
                ResultSetMetaData metaData=rs.getMetaData();
                int numColumns=metaData.getColumnCount();
                // Note columns need to be read in the order they are in result set
                DBColumn[] columns=new DBColumn[numColumns]; 
                for(int i=0;i<numColumns;i++){
                    columns[i]=findColumn(columnsOriginal,metaData.getColumnName(i+1));
                }

                String extendsTable=extendsTableMap.get(table);

                boolean hasNonReferences=false;
                boolean hasReferences=false;
                for(DBColumn column : columns){
                    if(!column.include) continue;
                    if(extendsTable!=null && column.references!=null && column.primaryKey) continue;
                    if(column.references!=null) hasReferences=true;
                    else hasNonReferences=true;
                }
            
                int counter=0;

                log("Processing table "+table);
                String[] row=new String[numColumns];
                while(rs.next()){
                    counter++;
                    if((counter%10)==0){
                        if(forceStop()) {
                            log("Extract aborted");
                            return;
                        }
                    }
                    if((counter%10000)==0) log("Processing table "+table+" row "+counter);
                    for(int i=0;i<numColumns;i++){
                        row[i]=rs.getString(i+1);
                    }
                    String ids=makeIdentifier(row,columns,table);
                    String identifier="http://wandora.org/si/dbimport/"+table+"/"+ids;
                    if(extendsTable!=null) identifier="http://wandora.org/si/dbimport/"+extendsTable+"/"+ids;
                    if(identifier.length()>255) System.out.println("WARNING! Locator over 255 characters");
                    if(hasNonReferences){
                        Topic t=getOrCreateTopic(tm,identifier);
                        Topic type=getOrCreateCached(tm,"http://wandora.org/si/dbimport/type/"+table);
                        t.addType(type);
                        for(int i=0;i<numColumns;i++){
                            DBColumn column=columns[i];
                            if(!column.include) continue;
                            String value=row[i];

                            if(column.references!=null) continue; 
                            if(column.baseName && value!=null) t.setBaseName(cropBaseName(value,ids));
                            if(column.makeTopics && value!=null){
                                Topic atype=getOrCreateCached(tm,"http://wandora.org/si/dbimport/type/"+table+"/"+column.column);
                                Topic otherRole=getOrCreateCached(tm,"http://wandora.org/si/dbimport/role/"+table+"/"+column.column);
                                Topic thisRole=getOrCreateCached(tm,"http://wandora.org/si/dbimport/role/"+table);
                                Topic other=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/"+table+"/"+column.column+"/"+value);
                                other.addType(atype);
                                other.setBaseName(cropBaseName(value,column.column));
                                Association a=tm.createAssociation(atype);
                                a.addPlayer(t,thisRole);
                                a.addPlayer(other,otherRole);                                
                            }
                            else if(value!=null) {
                                Topic dataType=getOrCreateCached(tm,"http://wandora.org/si/dbimport/datatype/"+table+"/"+column.column);
                                Topic version=getOrCreateCached(tm,XTMPSI.getLang(null));
                                t.setData(dataType,version,value);
                            }
                        }
                    }
                    if(hasReferences && !hasNonReferences){
                        Topic associationType=getOrCreateCached(tm,"http://wandora.org/si/dbimport/association/"+table);
                        Association a=tm.createAssociation(associationType);
                        for(int i=0;i<numColumns;i++){
                            DBColumn column=columns[i];
                            if(!column.include) continue;
                            if(extendsTable!=null && column.primaryKey) continue;
                            String value=row[i];
                            if(value==null) continue;
                            Topic other=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/"+column.references.table+"/"+value);
                            Topic role=getOrCreateCached(tm,"http://wandora.org/si/dbimport/role/"+table+"/"+column.column);
                            a.addPlayer(other,role);
                        }
                    }
                    else if(hasReferences) {
                        Topic t=getOrCreateTopic(tm,identifier);
                        Topic thisRole=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/role/"+table);
                        for(int i=0;i<numColumns;i++){
                            DBColumn column=columns[i];
                            if(!column.include) continue;
                            if(column.references==null) continue;
                            if(extendsTable!=null && column.primaryKey) continue;
                            String value=row[i];
                            if(value==null) continue;
                            Topic other=getOrCreateTopic(tm,"http://wandora.org/si/dbimport/"+column.references.table+"/"+value);
                            Topic type=getOrCreateCached(tm,"http://wandora.org/si/dbimport/type/"+table+"/"+column.column);
                            Topic otherRole=getOrCreateCached(tm,"http://wandora.org/si/dbimport/role/"+column.references.table);
                            Association a=tm.createAssociation(type);
                            a.addPlayer(t,thisRole);
                            a.addPlayer(other,otherRole);
                        }
                    }
                }
            }
            finally{
                rs.close();
                stmt.close();
            }
        }
        topicCache=new HashMap<String,Topic>();
        log("Extraction done");
    }
    
    public static DBColumn getOrCreateColumn(HashMap<T2<String,String>,DBColumn> columns,String table,String column){
        DBColumn c=columns.get(t2(table,column));
        if(c==null){
            c=new DBColumn(table,column);
            columns.put(t2(table,column),c);
        }
        return c;
    }
    
    /**
     * Sets the baseName attribute to true in one column of each table. Tries
     * to guess a column that would make the most sense as a base name, however
     * currently the heuristic for this is very simple.
     */
    public static void setDefaultBaseNameColumns(DatabaseSchema schema){
        for(String table : schema.tables){
            ArrayList<DBColumn> columns=schema.columns.get(table);
            boolean set=false;
            for(DBColumn column : columns){
                if(!column.primaryKey && column.references==null){
                    set=true;
                    column.baseName=true;
                    break;
                }
            }
            if(!set){
                for(DBColumn column : columns){
                    if(column.primaryKey){
                        column.baseName=true;
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * <p>
     * Gets a database schema based on DatabaseMetaData. Catalog and schema parameters are descibed
     * in java.sql.DatabaseMetaData.getCrossReference. Practically, catalog is usually null and
     * schema is the database user name. Thus you will usually do something like this:
     * </p>
     * <p><pre>
     * Connection con=DriverManager.getConnection(conString,user,pass);
     * DatabaseMetaData metadata=con.getMetaData();
     * DatabaseSchema schema=getDatabaseSchema(metadata,null,user);
     * </pre></p>
     */
    public static DatabaseSchema getDatabaseSchema(DatabaseMetaData metadata,String catalog,String schema) throws SQLException {
        DatabaseSchema ret=new DatabaseSchema();
        HashMap<T2<String,String>,DBColumn> allColumns=new HashMap<T2<String,String>,DBColumn>();
        ArrayList<String> tables=new ArrayList<String>();
        HashMap<String,ArrayList<DBColumn>> columns=new HashMap<String,ArrayList<DBColumn>>();
        ResultSet rs=metadata.getTables(catalog,schema,null,null);
        while(rs.next()){
            String name=rs.getString(3);
            tables.add(name);
        }
        rs.close();
        for(int i=0;i<tables.size();i++){
            String primaryTable=tables.get(i);
            for(int j=0;j<tables.size();j++){
                String foreignTable=tables.get(j);
                rs=metadata.getCrossReference(catalog,schema,primaryTable,catalog,schema,foreignTable);
                while(rs.next()){
                    DBColumn f=getOrCreateColumn(allColumns,rs.getString(7),rs.getString(8));
                    DBColumn p=getOrCreateColumn(allColumns,rs.getString(3),rs.getString(4));
                    f.references=p;
                }
                rs.close();
            }
            rs=metadata.getColumns(catalog,schema,primaryTable,null);
            ArrayList<DBColumn> c=new ArrayList<DBColumn>();
            while(rs.next()){
                c.add(getOrCreateColumn(allColumns,primaryTable,rs.getString(4)));
            }
            rs.close();
            rs=metadata.getPrimaryKeys(catalog,schema,primaryTable);
            while(rs.next()){
                DBColumn col=getOrCreateColumn(allColumns,primaryTable,rs.getString(4));
                col.primaryKey=true;
            }
            rs.close();
            columns.put(primaryTable,c);
        }
        ret.tables=tables;
        ret.columns=columns;
        return ret;
    }
    
    /**
     * Data structure containing information about database schema. Will have
     * information about all tables in the database and all columns in all
     * tables.
     */
    public static class DatabaseSchema {
        /**
         * A list of all tables in the database.
         */
        public ArrayList<String> tables;
        /**
         * A map containing lists of all the columns in all tables.
         */
        public HashMap<String,ArrayList<DBColumn>> columns;
        
        public DatabaseSchema(){
            tables=new ArrayList<String>();
            columns=new HashMap<String,ArrayList<DBColumn>>();
        }
        public void print(PrintStream out){
            for(String table : tables){
                out.println("------------- "+table+" -------------");
                for(DBColumn column : columns.get(table)){
                    String key="";
                    if(column.primaryKey) key=" (primary key)";
                    if(column.references!=null) out.println(column+" => "+column.references+key);
                    else out.println(column+key);
                }
            }
        }
    }

    /**
     * Datastructure containing information about a single column in a database schema.
     */
    public static class DBColumn {
        /** Table where this column is. */
        public String table;
        /** Column name. */
        public String column;
        public boolean include;
        /** Is this column a primary key. */
        public boolean primaryKey;
        /** The column that this column references or null if it does not reference any column. */
        public DBColumn references;
        /** Should Wandora use this column for base names. */
        public boolean baseName;
        /** Should Wandora make topics from this column. */
        public boolean makeTopics;
        public DBColumn(String table,String column){
            this.table=table;
            this.column=column;
            this.include=true;
            this.baseName=false;
            this.makeTopics=false;
        }
        @Override
        public String toString(){
            return table+"."+column;
        }
    }
    
}
