/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.wandora.application.tools.sqlconsole.data.TableView;
import org.wandora.utils.Delegate;
import org.wandora.utils.logger.Log4j2Logger;


/**
 *
 * @author  akivela
 */
public class SQLTablePage extends JPanel {
    

	private static final long serialVersionUID = 1L;
	private static final Log4j2Logger logger = Log4j2Logger.getLogger(SQLTablePage.class);

	private long refreshTime;
    TableView dataTable = null;

    SQLTablePanel guiTable;
    JScrollPane scrollPane;
    
    String titleText = "";
    
    private String componentid;
    
    private Delegate<TableView,Delegate.Void> tableMaker;


    public SQLTablePage(TableView dataTable, String title) {
        this.dataTable = dataTable;
        initComponents();
        tableMaker=null;
        setTitle(title);
        tableMaker=null;
    }
    
        
        
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
        gridBagConstraints.fill=java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx=1.0;

        gridBagConstraints.anchor=java.awt.GridBagConstraints.NORTHWEST;
        scrollPane=new JScrollPane();//guiTable);
        gridBagConstraints.weighty=1.0;
        gridBagConstraints.fill=java.awt.GridBagConstraints.BOTH;
        add(scrollPane,gridBagConstraints);
        setTable();
        
        // -------
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 7, 10);
        gridBagConstraints.weighty=0;
        gridBagConstraints.anchor=java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.fill=java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 5;
        final SQLTablePage thisf=this;
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