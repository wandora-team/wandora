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
 * Viite.java
 *
 * Created on November 1, 2004, 4:49 PM
 */

package org.wandora.application.tools.sqlconsole.data;



/**
 *
 * @author  akivela
 */
public class Row {

    
    public String[] data;
    public Object[] hiddenData;
    
    
    /** Creates a new instance of Viite */
    public Row(int columns) {
        data = new String[columns];
    }
    
    public Row(Object[] data){
        this(data,data.length);
    }
    public Row(Object[] data,int visibleColumns){
        this.data=new String[visibleColumns];
        for(int i=0;i<visibleColumns;i++){
            if(data[i]==null) this.data[i]="";//"<NULL>";
            else this.data[i]=data[i].toString();
        }
        if(visibleColumns!=data.length){
            hiddenData=new Object[data.length-visibleColumns];
            for(int i=visibleColumns;i<data.length;i++){
                hiddenData[i-visibleColumns]=data[i];
            }
        }
    }
    
    
    public String getColumn(int c) {
        try { return data[c]; }
        catch (Exception e) { return null; }
    }
    
    
    public void setColumn(int c, String v) {
        try { data[c] = v; }
        catch (Exception e) {}
    }

    

    // -------------------------------------------------------------------------
    
    
    public String[] getColumns() {
        return data;
    }
    
    public Object[] getHiddenData(){
        return hiddenData;
    }
}
