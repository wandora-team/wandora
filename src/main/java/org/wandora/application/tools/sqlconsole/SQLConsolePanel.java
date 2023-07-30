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
 * SQLConsolePanel.java
 *
 * Created on 28. joulukuuta 2004, 15:09
 */

package org.wandora.application.tools.sqlconsole;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.wandora.application.tools.sqlconsole.data.PatternFilteredTableView;
import org.wandora.application.tools.sqlconsole.data.RowTable;
import org.wandora.application.tools.sqlconsole.data.TableView;
import org.wandora.application.tools.sqlconsole.gui.SQLTablePanel;
import org.wandora.utils.Delegate;
import org.wandora.utils.Options;

/**
 *
 * @author  olli
 */
public class SQLConsolePanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;

	private Map<String,StoredQuery> storedQueries=new TreeMap<String,StoredQuery>();
    private Connection connection;
    private int resultMaxRows;
    private Options options;
    private boolean isSimpleView;
    
    /** Creates new form SQLConsolePanel */
    public SQLConsolePanel(Options options) {
        this.options=options;
        initComponents();
        switchToSimple(null);
        resultMaxRows=500;
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        splitPane = new javax.swing.JSplitPane();
        queryPanelContainer = new javax.swing.JPanel();
        queryPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        resultContainer = new javax.swing.JPanel();
        resultPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(200);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        queryPanelContainer.setLayout(new java.awt.BorderLayout());

        queryPanelContainer.add(queryPanel, java.awt.BorderLayout.CENTER);

        splitPane.setLeftComponent(queryPanelContainer);

        resultContainer.setLayout(new java.awt.BorderLayout());

        resultContainer.add(resultPanel, java.awt.BorderLayout.CENTER);

        resultScrollPane.setViewportView(resultContainer);

        splitPane.setRightComponent(resultScrollPane);

        add(splitPane, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    public static Connection getConnection(String driver,String connectString,String user,String password)
                    throws ClassNotFoundException,SQLException {
        
        System.out.println("Opening DB connection with driver " + driver);
        Class.forName(driver);
        return DriverManager.getConnection(connectString,user,password);        
    }
    
    public void connect(String driver,String connectString,String user,String password) {
        try{
            connection=getConnection(driver,connectString,user,password);
        }catch(ClassNotFoundException cnfe){
            SQLConsole.reportException(cnfe);            
        }catch(SQLException sqlException){
            SQLConsole.reportException(sqlException);            
        }
    }
    
    public void setResultMaxRows(int count){resultMaxRows=count;}
    public int getResultMaxRows(){return resultMaxRows;}
    
    public void executeQuery(String query){
        SQLQueryResult result=executeQueryLowLevel(query);
        if(result==null) return;
        if(result.resultIsRows){
            int h=splitPane.getDividerLocation();
            TableView tableView=new PatternFilteredTableView(new RowTable(result.columnNames,result.rows));
            resultPanel=new SQLTablePanel(tableView);
            resultContainer.removeAll();
            resultContainer.add(resultPanel);
            ((SQLTablePanel)resultPanel).setHeaderVisible(false);
            javax.swing.table.JTableHeader tableHeader=((SQLTablePanel)resultPanel).getTableHeader();
            resultPanel.setPreferredSize(new java.awt.Dimension((int)tableHeader.getPreferredSize().getWidth(),(int)resultPanel.getPreferredSize().getHeight()));
            resultScrollPane.setColumnHeaderView(tableHeader);            
            splitPane.setDividerLocation(h);
            resultPanel.validate();
            resultPanel.repaint();
            if(result.rowCountOverFlow){
                javax.swing.JOptionPane.showMessageDialog(this,"Näytetään vain ensimmäiset "+result.rows.size()+" rivi�.");
            }
        }
        else{
            javax.swing.JOptionPane.showMessageDialog(this,"Statement executed. "+result.count+" rows updated.");
        }
    }
    
    public static SQLQueryResult executeQueryLowLevel(String query,Connection connection,int resultMaxRows) throws SQLException {
        return executeQueryLowLevel(query,connection,resultMaxRows,null);
    }
    
    public static SQLQueryResult executeQueryLowLevel(String query,Connection connection,int resultMaxRows,Delegate<Integer,Integer> onOverflow) throws SQLException {
        System.out.println("Excecuting query "+query);
        Statement statement=connection.createStatement();
        query=query.trim();
        boolean type=statement.execute(query);
        if(type){
            ResultSet resultSet=statement.getResultSet();
            ResultSetMetaData metaData=resultSet.getMetaData();
            int columns=metaData.getColumnCount();
            String[] columnNames=new String[columns];
            for(int i=0;i<columns;i++){
                columnNames[i]=metaData.getColumnName(i+1);
            }
            int count=0;
            Vector<Object[]> rows=new Vector<>();
            boolean hasNext = resultSet.next();
            while(hasNext) {
                try {
                    if(resultMaxRows!=-1 && count>=resultMaxRows){
                        if(onOverflow==null) break;
                        resultMaxRows=onOverflow.invoke(resultMaxRows);
                        if(resultMaxRows==0) break;
                    }
                    Object[] row=new Object[columns];
                    for(int i=0;i<columns;i++){
                        row[i]=resultSet.getObject(i+1);
                    }
                    rows.add(row);
                    count++;
                    hasNext = resultSet.next();
                    System.out.println("count == " + count);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    hasNext = true;
                }
            }
            boolean overflow=resultSet.next();
            resultSet.close();
            statement.close();
            return new SQLQueryResult(rows,columnNames,overflow);
        }
        else{
            int updateCount=statement.getUpdateCount();
            statement.close();
            return new SQLQueryResult(updateCount);
        }
    }
    
    public SQLQueryResult executeQueryLowLevel(String query){
        try{
            return executeQueryLowLevel(query,connection,resultMaxRows);
        }catch(SQLException sqlException){
            SQLConsole.reportException(sqlException);
            return null;
        }        
    }
    
    public void saveQuery(StoredQuery query){
        storedQueries.put(query.getName(),query);
    }
    public void switchToSimple(String name){
        queryPanelContainer.removeAll();
        queryPanel=new StoredQueryPanel(this,storedQueries);
        queryPanelContainer.add(queryPanel);
        if(name!=null){
            ((StoredQueryPanel)queryPanel).selectQuery(name);
        }
        setSimpleSize();
        isSimpleView=true;
    }
    public void setSimpleSize(){
        if(!(queryPanel instanceof StoredQueryPanel)) return;
        int h=((StoredQueryPanel)queryPanel).getPreferredHeight();
        splitPane.setDividerLocation(h+splitPane.getDividerSize());
        this.validate();
        this.repaint();        
    }
    public void switchToEdit(StoredQuery query){
        queryPanelContainer.removeAll();
        queryPanel=new EditQueryPanel(this,query);
        queryPanelContainer.add(queryPanel);
        splitPane.setDividerLocation(300);
        // this.validateTree(); // TRIGGERS EXCEPTION IN JAVA 1.7
        this.repaint();
        isSimpleView=false;
    }
    
    public void updateSimpleView(){
        if(isSimpleView){
            switchToSimple(null);
        }
    }
    
    public void clearStoredQueries(){
        storedQueries=new HashMap<>();
        updateSimpleView();
    }
    public void loadStoredQueries() throws Exception {
        storedQueries=StoredQuery.loadStoredQueries(options.get("options.sqlconsole.storefile"));
        updateSimpleView();
    }
    public void saveStoredQueries() throws IOException {
        StoredQuery.saveStoredQueries(storedQueries,options.get("options.sqlconsole.storefile"));           
        updateSimpleView();
    }
    public void importQueries(Map<String,StoredQuery> queries){
        for(String key : queries.keySet()){
            storedQueries.put(key,queries.get(key));
        }
    }
    
    public Map<String,StoredQuery> getStoredQueries(){
        return storedQueries;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel queryPanel;
    private javax.swing.JPanel queryPanelContainer;
    private javax.swing.JPanel resultContainer;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
    
}
