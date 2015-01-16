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
 * SQLTablePage.java
 *
 * Created on 1. joulukuuta 2004, 15:01
 */

package org.wandora.application.tools.sqlconsole.gui;

import org.wandora.utils.Delegate;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.wandora.utils.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.utils.swing.*;
import org.wandora.application.tools.sqlconsole.*;
import org.wandora.application.tools.sqlconsole.data.*;
import java.sql.*;

/**
 *
 * @author  akivela
 */
public class SQLTablePage extends JPanel {
    
    
    private long refreshTime;
    //Kirjava kirjava = null;
    //Kirjasto kirjasto = null;
    TableView dataTable = null;

    SQLTablePanel guiTable;
    JScrollPane scrollPane;
    
    //KirjavaAntialisedLabel pageTitleLabel;
    String titleText = "";
    
    private String componentid;
    
    private Delegate<TableView,Delegate.Void> tableMaker;

    //private Bookmark currentBookmark;
    
    /*
     * Note: kirjasto should always be kirjava.getKirjasto()
     */
    public SQLTablePage(TableView dataTable, String title) {
        this.dataTable = dataTable;
        initComponents();
        tableMaker=null;
        setTitle(title);
        tableMaker=null;
    }
    
    /*
    
    
    public KirjavaTablePage(String componentid,Delegate<TableView,Delegate.Void> tableMaker,Bookmark bm,Kirjava kirjava, Kirjasto kirjasto, String title){
        this(componentid,tableMaker,kirjava,kirjasto,title);
        this.setBookmark(bm);
    }
    
    public KirjavaTablePage(String componentid,Delegate<TableView,Delegate.Void> tableMaker,Bookmark bm,Kirjava kirjava,String title){
        this(componentid,tableMaker,kirjava,null,title);
        this.setBookmark(bm);
    }
    
    
    
    public KirjavaTablePage(String componentid,Delegate<TableView,Delegate.Void> tableMaker,Kirjava kirjava,String title){
        this(componentid,tableMaker,kirjava,null,title);
    }
    
    public KirjavaTablePage(String componentid,Delegate<TableView,Delegate.Void> tableMaker,Kirjava kirjava,Kirjasto kirjasto,String title){
        this.componentid=componentid;
        this.kirjava=kirjava;
        this.kirjasto = kirjasto;
        this.tableMaker=tableMaker;
        refreshTime=System.currentTimeMillis();
        this.dataTable = tableMaker.invoke(Delegate.VOID);
        initComponents();        
        setTitle(title);
    }
    
    
    
    public KirjavaTablePage(String componentid,Kirjava kirjava,String title,String dialogTitle){
        this(componentid, kirjava, null, title, dialogTitle);
    }
    
    
    public KirjavaTablePage(String componentid,Kirjava kirjava, Kirjasto kirjasto, String title,String dialogTitle){
        this.componentid=componentid;
        this.kirjava=kirjava;
        this.kirjasto = kirjasto;
        showSelectDialog(dialogTitle);
        setTitle(title);
    }
    
    */
    
    
    // -------------------------------------------------------------------------
    
    
    
/*
    public void showSelectDialog(String dialogTitle) {
        final QueryDialog d=new QueryDialog(kirjava,kirjasto,true,componentid);
        d.setTitle(dialogTitle);
        d.setVisible(true);
        final String query=d.getQuery();
        final String countQuery=d.getCountQuery();
        if(query!=null){
            tableMaker=new Delegate<TableView,Delegate.Void>(){
                public TableView invoke(Delegate.Void v){
                    T2<String,Boolean>[] columns=null;
                    SQLQueryResult result=kirjava.executeQuery(query,countQuery);
                    columns=d.getColumns();
                    if(columns==null) {
                        columns=new T2[result.columnNames.length];
                        for(int i=0;i<columns.length;i++){
                            columns[i]=t2(result.columnNames[i],false);
                        }
                    }
                    TableView tv=new PatternFilteredTableView(new RowTable(columns,result.rows));
                    tv.setUpdater(d.getUpdater());
                    return tv;
                }
            };*/
/*            this.dataTable=new PatternFilteredTableView(new RowTable(columns,result.rows));
            this.dataTable.setUpdater(d.getUpdater());*/
    /*
            refreshTime=System.currentTimeMillis();
            if(d != null) {
                tableMaker=d.getTableViewMaker();
                setBookmark(d.getBookmark());
                if(tableMaker != null) {
                    this.dataTable=tableMaker.invoke(Delegate.VOID);
                }
            }
            else {
                dataTable=null;
            }
/*        }
        else{
            tableMaker=null;
            this.dataTable=null;            
        }        */
            
            /*
        removeAll();
        initComponents();

    }
*/
        
        
    public long getRefreshTime(){
        return refreshTime;
    }
    
    public void refreshQuery(){
        refreshTable();
    }
    
    public void refreshTable(){
        Point pos=scrollPane.getViewport().getViewPosition();
        refreshTime=System.currentTimeMillis();
        if(tableMaker!=null){
            dataTable=tableMaker.invoke(Delegate.VOID);
        }
        else dataTable=null;
        setTable();
        scrollPane.getViewport().setViewPosition(pos);
    }
    private void setTable(){
        if(dataTable!=null){
            guiTable=new SQLTablePanel(dataTable,componentid);
            guiTable.setHeaderVisible(false);
            javax.swing.table.JTableHeader tableHeader=guiTable.getTableHeader();
            guiTable.setHeaderListener(new Delegate<Object,javax.swing.table.JTableHeader>(){
                public Object invoke(javax.swing.table.JTableHeader header){
                    scrollPane.setColumnHeaderView(header);
                    return null;
                }
            });
            scrollPane.setViewportView(guiTable);
            scrollPane.setColumnHeaderView(tableHeader);
        }
        else{
            scrollPane.setViewportView(new JPanel());
        }
    }
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        setLayout(new java.awt.GridBagLayout());
        
        JPanel pageTitlePanel = new javax.swing.JPanel();

        JLabel pageTitleLabel = new JLabel();
        pageTitlePanel.setLayout(new BorderLayout(0,0));
        pageTitlePanel.setPreferredSize(new java.awt.Dimension(640, 25));
        pageTitleLabel.setText(titleText);
        pageTitlePanel.add(pageTitleLabel, BorderLayout.WEST);
                
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor=java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 2, 10);
        add(pageTitlePanel, gridBagConstraints);
              
        // -------
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 10);
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill=gridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx=1.0;

        gridBagConstraints.anchor=java.awt.GridBagConstraints.NORTHWEST;
        scrollPane=new JScrollPane();//guiTable);
/*        javax.swing.table.JTableHeader tableHeader=guiTable.getTableHeader();
        guiTable.setHeaderListener(new Delegate<Object,javax.swing.table.JTableHeader>(){
            public Object invoke(javax.swing.table.JTableHeader header){
                scrollPane.setColumnHeaderView(header);
                return null;
            }
        });*/
        gridBagConstraints.weighty=1.0;
        gridBagConstraints.fill=gridBagConstraints.BOTH;
        add(scrollPane,gridBagConstraints);
//        if(dataTable!=null){
/*            guiTable = new KirjavaTablePanel(dataTable, kirjava,componentid);
            guiTable.setHeaderVisible(false);
            scrollPane.setColumnHeaderView(tableHeader);*/
            setTable();
//        }
        
        // -------
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 7, 10);
        gridBagConstraints.weighty=0;
        gridBagConstraints.anchor=java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.fill=gridBagConstraints.BOTH;
        gridBagConstraints.gridy = 5;
        final SQLTablePage thisf=this;
        /*
        add(UIBox.createSavePanel(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                TableViewUpdater updater=null;
                if(updater != null) {
                    try{
                        if(getEditedRows().size() > 0) {
                            updater.update(kirjava,thisf);
                            JOptionPane.showMessageDialog(kirjava, "Tallennettu taulukon muutokset!", "Tallennus!", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(kirjava, "Taulukon rivejä ei ole muutettu!", "Ei talletettavaa!", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(kirjava, "Muutosten tallennus keskeytyi virheeseen\n" + e.toString(), "Tallennus epäonnistui!", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(kirjava, "Taulukko on tyhjä, eikä sisällä muutettuja tietoja!", "Ei talletettavaa!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }), gridBagConstraints);
         **/
    }
    
    public Collection<Integer> getEditedRows(){
        return guiTable.getEditedRows();
    }
    
    public String[] getRowData(int r){
        return guiTable.getRowData(r);
    }
    
    public Object[] getHiddenData(int r){
        return dataTable.getHiddenData(r);
    }
    
    public void setTitle(String newTitle) {
        //if(pageTitleLabel != null) pageTitleLabel.setText(newTitle);
    }
    

}