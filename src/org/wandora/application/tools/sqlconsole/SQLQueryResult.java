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
 * SQLQueryResult.java
 *
 * Created on 29. joulukuuta 2004, 11:31
 */

package org.wandora.application.tools.sqlconsole;
import java.util.*;
import java.io.StringWriter;
/**
 *
 * @author  olli
 */
public class SQLQueryResult {
    public List<Object[]> rows;
    public int count;
    public boolean resultIsRows;
    public boolean rowCountOverFlow;
    public String[] columnNames;
    public Map<String,Integer> columnIndex;
    /** Creates a new instance of SQLQueryResult */
    public SQLQueryResult(List<Object[]> rows,String[] columnNames,boolean rowCountOverFlow) {
        this.rows=rows;
        this.columnNames=columnNames;
        this.rowCountOverFlow=rowCountOverFlow;
        resultIsRows=true;
        makeColumnIndex();
    }
    public SQLQueryResult(int count){
        this.count=count;
        resultIsRows=false;
    }
    private void makeColumnIndex(){
        columnIndex=new HashMap();
        for(int i=0;i<columnNames.length;i++){
            columnIndex.put(columnNames[i],i);
        }
    }
    public Object get(int row,String columnName){
        return get(rows.get(row),columnName);
    }
    public Object get(Object[] row,String columnName){
        return row[columnIndex.get(columnName)];
    }
    /**
     * Constructs a Collection<String> from the rows in this SQLQueryResult.
     * Each row is formatted into a single String with the given formatString.
     * The formatString is passed to java.util.Formatter.format, with the
     * row as the rest of the parameters.
     */
    public Collection<String> makeStringCollection(String formatString){
        Collection<String> ss=new Vector();
        for(Object[] row : rows){
            StringWriter w=new StringWriter();
            Formatter formatter=new Formatter(w);
            formatter.format(formatString,row);
            ss.add(w.toString());
        }
        return ss;
    }
}
