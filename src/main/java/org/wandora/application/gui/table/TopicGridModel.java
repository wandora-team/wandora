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
 */


package org.wandora.application.gui.table;


import javax.swing.table.DefaultTableModel;

import org.wandora.application.Wandora;
import org.wandora.topicmap.Topic;




/**
 *
 * @author akivela
 */


public class TopicGridModel extends DefaultTableModel {
    private static final long serialVersionUID = 1L;

    private TopicGrid topicGrid;
    private static final String columnNames = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    
    
    public TopicGridModel(TopicGrid tg) {
        topicGrid = tg;
    }

    
    @Override
    public Class getColumnClass(int c) {
        return Topic.class;
    }


    @Override
    public int getColumnCount() {
        if(topicGrid != null) return topicGrid.getGridColumnCount();
        return 0;
    }



    @Override
    public int getRowCount() {
        if(topicGrid != null) return topicGrid.getGridRowCount();
        return 0;
    }

    
    
    public Topic getTopicAt(int rowIndex, int columnIndex) {
        try {
            if(topicGrid != null && rowIndex >= 0 && columnIndex >= 0) {
                return topicGrid.getTopicAt(rowIndex, columnIndex);
            }
        }
        catch (Exception e) {
            Wandora.getWandora().handleError(e);
        }
        return null;
    }
    



    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if(topicGrid != null && rowIndex >= 0 && columnIndex >= 0) {
                return topicGrid._getTopicAt(rowIndex, columnIndex);
            }
        }
        catch (Exception e) {
            Wandora.getWandora().handleError(e);
        }
        return null;
    }


    public Object getColumnObjectAt(int columnIndex) {
        return getColumnName(columnIndex);
    }


    @Override
    public String getColumnName(int columnIndex){
        try {
            int d1 = (columnIndex) / columnNames.length();
            int d2 = (columnIndex) % columnNames.length();
            if(d1 == 0) {
                return ""+columnNames.charAt(d2);
            }
            else {
                return ""+columnNames.charAt(d1)+columnNames.charAt(d2);
            }
        }
        catch (Exception e) {
            Wandora.getWandora().handleError(e);
        }
        return "ERROR";
    }



    @Override
    public boolean isCellEditable(int row,int col){
        return false;
    }
}
