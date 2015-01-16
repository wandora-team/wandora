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
 */

package org.wandora.application.gui.table;



import javax.swing.table.DefaultTableModel;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Topic;




/**
 *
 * @author akivela
 */


public class TopicTableModel extends DefaultTableModel {

    private Topic[][] data = null;
    private Object[] cols = null;


    public TopicTableModel(Topic[][] tableTopics, Object[] columnObjects) {
        data = tableTopics;
        cols = columnObjects;
    }

    @Override
    public Class getColumnClass(int c) {
        return Topic.class;
    }


    @Override
    public int getColumnCount() {
        if(cols != null) return cols.length;
        return 0;
    }



    @Override
    public int getRowCount() {
        if(data != null) return data.length;
        return 0;
    }

    
    
    public Topic getTopicAt(int rowIndex, int columnIndex) {
        try {
            if(data != null && rowIndex >= 0 && columnIndex >= 0) {
                return data[rowIndex][columnIndex];
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
            if(data != null && rowIndex >= 0 && columnIndex >= 0) {
                return data[rowIndex][columnIndex];
            }
        }
        catch (Exception e) {
            Wandora.getWandora().handleError(e);
        }
        return null;
    }


    public Object getColumnObjectAt(int columnIndex) {
        if(cols != null && columnIndex >= 0 && cols.length > columnIndex) {
            return cols[columnIndex];
        }
        return null;
    }


    @Override
    public String getColumnName(int columnIndex){
        try {
            if(cols != null && columnIndex >= 0 && cols.length > columnIndex && cols[columnIndex] != null) {
                if(cols[columnIndex] instanceof Topic) return TopicToString.toString((Topic)cols[columnIndex]);
                else return cols[columnIndex].toString();
            }
            return "";
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

