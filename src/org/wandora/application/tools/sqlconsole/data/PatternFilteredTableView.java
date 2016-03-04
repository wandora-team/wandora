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
 * FilteredTableView.java
 *
 * Created on 3. joulukuuta 2004, 17:18
 */

package org.wandora.application.tools.sqlconsole.data;


import java.util.regex.*;
import org.wandora.application.tools.sqlconsole.*;
import org.wandora.application.tools.sqlconsole.data.utils.*;

/**
 *
 * @author  akivela
 */
public class PatternFilteredTableView extends MappedTableView implements TableView {
    
    
    public static final int OR_MODE = 0;
    public static final int AND_MODE = 1;
    
    public static SQLPattern DEFAULT_PATTERN = null;

    int mode;
    SQLPattern[] patterns;
   
    
    
    /** Creates a new instance of FilteredTableView */
    public PatternFilteredTableView(TableView t) {
        super(t);
        mode = OR_MODE;
        resetView();
    }

    
    // -------------------------------------------------------------------------
    
    
    public String[] getColumnNames() {
        String[] columnNames = super.getColumnNames();
        String[] newColumnNames = new String[columnNames.length];
        for(int i=0; i<columnNames.length ; i++) {
            newColumnNames[i] = columnNames[i];
            if(columnHasPattern(i)) {
                SQLPattern pattern = getPattern(i);
                String patternString = pattern.getPatternString();
                newColumnNames[i] = "" + newColumnNames[i] + " [" + patternString + "]";
                //Logger.println("newColumnNames[i] == " + newColumnNames[i]);
            }
        }
        return newColumnNames;
    }
    
    

    // -------------------------------------------------------------------------

    
    public void setPattern(int column, String f) {
        setPattern(column, null, f, true, true);
    }
    public void setPattern(int column, String name, String f, boolean findInstead, boolean caseInsensitivity) {
        if(f != null) {
            try { 
                patterns[column] = new SQLPattern(name, f, findInstead, caseInsensitivity); 
            }
            catch (Exception e) { 
                //Logger.println(e); 
            }
        }
        else patterns[column] = DEFAULT_PATTERN;
        updateRowIndex();
    }
    
    

    public void setPattern(int column, SQLPattern p) {
        if(p != null) {
            patterns[column] = p;
        }
        else patterns[column] = DEFAULT_PATTERN;
        updateRowIndex();
    }
    
    
    public SQLPattern getPattern(int column) {
        try { return patterns[column]; }
        catch (Exception e) {}
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public boolean columnHasPattern(int column) {
        if(patterns[column] == null || patterns[column] == DEFAULT_PATTERN) return false;
        else return true;
    }
    
    
    // -------------------------------------------------------------------------
    

    public void setMode(boolean andMode) {
        if(andMode) mode = AND_MODE;
        else mode = OR_MODE;
    }
    public void setMode(int newMode) {
        mode = newMode;
    }
    public int getMode() {
        return mode;
    }
      
    
    // -------------------------------------------------------------------------
    
    
    public void resetView() {
        super.resetView();
        patterns = new SQLPattern[table.getColumnCount()];
        for(int i=0; i<patterns.length; i++) {
            patterns[i] = DEFAULT_PATTERN;
        }
    }
    
  
    
    public void updateRowIndex() {
        super.resetView();
        String[][] data = super.getView();
        int[] rowMap = new int[data.length];
        int count=0;
        for(int r=0; r<data.length; r++) {
            String[] row = data[r];
            if(rowMatches(row, patterns)) {
                rowMap[count++] = r;
                //Logger.println("accepting row " + r );
            }
        }
        int[] tightRowMap = new int[count];
        for(int r=0; r<count; r++) {
            tightRowMap[r] = rowMap[r];
        }
        setRowMap(tightRowMap);
    }
    
    
       
    
    
    public boolean rowMatches(String[] row, SQLPattern[] ps) {
        switch(mode) {
            case OR_MODE: { // OR
                //Logger.println("or mode");
                boolean noPatterns = true;
                for(int j=0; j<ps.length; j++) {
                    if(ps[j] != null) {
                        noPatterns = false;
                        if(ps[j].matches(row[j])) return true;
                    }
                }
                return false || noPatterns;
            }
            case AND_MODE: { // AND
                //Logger.println("and mode");
                boolean noPatterns = true;
                for(int j=0; j<ps.length; j++) {
                    if(ps[j] != null) {
                        noPatterns = false;
                        if(!ps[j].matches(row[j])) return false;
                    }
                }
                return true || noPatterns;
            }
        }
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
    

    
    public void deleteRows(int[] rowsToDelete) {
        super.deleteRows(rowsToDelete);
        resetView();
    }
    
    
    public void insertRows(int pos, int number) {
        super.insertRows(pos, number);
        resetView();
    }
     
}
