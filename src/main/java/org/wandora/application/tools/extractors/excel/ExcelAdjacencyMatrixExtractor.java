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
 */



package org.wandora.application.tools.extractors.excel;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wandora.application.Wandora;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class ExcelAdjacencyMatrixExtractor extends AbstractExcelExtractor {

	private static final long serialVersionUID = 1L;
	
	
	public static boolean ADD_CELL_VALUE_AS_PLAYER = false;
    public static boolean ADD_CELL_COLOR_AS_PLAYER = false;
    
    public static boolean INTERPRET_FALSE_AS_EMPTY_CELL = true;
    public static boolean INTERPRET_ZERO_AS_EMPTY_CELL = true;
    public static boolean INTERPRET_ZERO_LENGTH_STRING_AS_EMPTY_CELL = true;
    public static boolean INTERPRET_COLOR_AS_VALID_CELL_VALUE = false;
    
    public static boolean USE_SHEET_AS_ASSOCIATION_TYPE = true;
    
    
       
    private Map<String,String> columnLabels = new LinkedHashMap<>();
    private Map<String,String> rowLabels = new LinkedHashMap<>();
            
    
    
    
    @Override
    public String getName(){
        return "Excel adjacency matrix extractor";
    }
    @Override
    public String getDescription(){
        return "Excel adjacency matrix extractor interprets Excel sheet as an adjacency matrix.";
    }
    
    
    
    @Override
    public void processWorkbook(HSSFWorkbook workbook, TopicMap topicMap) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for(int i=0; i<numberOfSheets && !forceStop(); i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            processSheet(sheet, topicMap);
        }
    }
    

    @Override
    public void processWorkbook(XSSFWorkbook workbook, TopicMap topicMap) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for(int i=0; i<numberOfSheets && !forceStop(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            processSheet(sheet, topicMap);
        }
    }
    
    
    public void processSheet(HSSFSheet sheet, TopicMap tm) {
        Iterator<Row> rowIterator = sheet.iterator();
        boolean isFirst = true;
        columnLabels = new LinkedHashMap<>();
        rowLabels = new LinkedHashMap<>();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst) {
                processAsLabels(row, tm);
                isFirst = false;
            }
            else {
                processRow(row, tm);
            }
        }
    }
    
    
    
    public void processSheet(XSSFSheet sheet, TopicMap tm) {
        Iterator<Row> rowIterator = sheet.iterator();
        boolean isFirst = true;
        columnLabels = new LinkedHashMap<>();
        rowLabels = new LinkedHashMap<>();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst) {
                processAsLabels(row, tm);
                isFirst = false;
            }
            else {
                processRow(row, tm);
            }
        }
    }
    
    
    
    
    public void processRow(Row row, TopicMap tm) {
        Association a = null;

        try {
            Cell firstColumnCell = row.getCell(0);
            if(firstColumnCell != null) {
                if(getCellValueAsString(firstColumnCell) != null) {
                    Topic cellTopic = getCellTopic(firstColumnCell, tm);
                    rowLabels.put(Integer.toString(firstColumnCell.getRowIndex()), cellTopic.getOneSubjectIdentifier().toExternalForm());
                }
                else {
                    return;
                }
            }

            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext() && !forceStop()) {
                Cell cell = cellIterator.next();
                if(cell.getColumnIndex() > 0) {
                    processCell(cell, tm);
                }
            }
        }
        catch (TopicMapException ex) {
            log(ex);
        }
        catch (Exception ex) {
            log(ex);
        }
    }
    
    
    
    public void processCell(Cell cell, TopicMap tm) {
        if(cell != null) {
            try {
                
                String rowLabel = rowLabels.get(Integer.toString(cell.getRowIndex()));
                String columnLabel = columnLabels.get(Integer.toString(cell.getColumnIndex()));
                if(rowLabel != null && columnLabel != null) {
                    if(hasValue(cell)) {
                        Topic rowTopic = tm.getTopic(rowLabel);
                        Topic columnTopic = tm.getTopic(columnLabel);
                        if(rowTopic != null && columnTopic != null) {
                            Association a = tm.createAssociation(getAssociationTypeTopic(cell, tm));
                            a.addPlayer(rowTopic, getRowTypeTopic(tm));
                            a.addPlayer(columnTopic, getColumnTypeTopic(tm));
                            if(ADD_CELL_VALUE_AS_PLAYER) {
                                Topic cellTopic = getCellTopic(cell, tm);
                                Topic cellType = getCellTypeTopic(tm);
                                if(cellTopic != null && cellType != null) {
                                    a.addPlayer(cellTopic, cellType);
                                }
                            }
                            if(ADD_CELL_COLOR_AS_PLAYER) {
                                Topic cellColorTopic = getColorTopic(cell, tm);
                                Topic cellType = getColorTypeTopic(tm);
                                if(cellColorTopic != null && cellType != null) {
                                    a.addPlayer(cellColorTopic, cellType);
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
    }
    
    
    
    private Topic getAssociationTypeTopic(Cell cell, TopicMap tm) throws TopicMapException {
        if(USE_SHEET_AS_ASSOCIATION_TYPE) {
            return getDefaultAssociationTypeTopic(tm);
        }
        else {
            return getSheetTopic(cell, tm);
        }
    }
    
    
    
    
    public void processAsLabels(Row row, TopicMap topicMap) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while(cellIterator.hasNext()) {
            try {
                Cell cell = cellIterator.next();
                if(getCellValueAsString(cell) != null) {
                    Topic cellTopic = getCellTopic(cell, topicMap);
                    columnLabels.put(Integer.toString(cell.getColumnIndex()), cellTopic.getOneSubjectIdentifier().toExternalForm());
                }
            }
            catch (TopicMapException ex) {
                log(ex);
            }
            catch (Exception ex) {
                log(ex);
            }
        }
    }
    
    
    
    
    
    public boolean hasValue(Cell cell) {

        if(ADD_CELL_COLOR_AS_PLAYER || INTERPRET_COLOR_AS_VALID_CELL_VALUE) {
            CellStyle style = cell.getCellStyle();
            short color = style.getFillBackgroundColor();
            if(color != 0) {
                return true;
            }
        }
        
        String str = getCellValueAsString(cell);
        if(str == null) return false;
        if(INTERPRET_FALSE_AS_EMPTY_CELL && "false".equalsIgnoreCase(str)) return false;
        if(INTERPRET_ZERO_AS_EMPTY_CELL && "0".equalsIgnoreCase(str)) return false;
        if(INTERPRET_ZERO_LENGTH_STRING_AS_EMPTY_CELL && "".equalsIgnoreCase(str)) return false;
        
        return true;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- CONFIGURE ----
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Excel adjacency matrix extractor options","Excel adjacency matrix extractor options",true,new String[][]{
            new String[]{"Interpret false as empty cell?","boolean",(INTERPRET_FALSE_AS_EMPTY_CELL ? "true" : "false"),null },
            new String[]{"Interpret zero character (0) as empty cell?","boolean",(INTERPRET_ZERO_AS_EMPTY_CELL ? "true" : "false"),null },
            new String[]{"Interpret zero length string as empty cell?","boolean",(INTERPRET_ZERO_LENGTH_STRING_AS_EMPTY_CELL ? "true" : "false"),null },
            new String[]{"Interpret background color as nonempty cell?","boolean",(INTERPRET_COLOR_AS_VALID_CELL_VALUE ? "true" : "false"),null },
            new String[]{"Add cell's background color to association as player?","boolean",(ADD_CELL_COLOR_AS_PLAYER ? "true" : "false"),null },
            new String[]{"Add cell's value to association as player?","boolean",(ADD_CELL_VALUE_AS_PLAYER ? "true" : "false"),null },
            new String[]{"Use sheet as an association type?","boolean",(USE_SHEET_AS_ASSOCIATION_TYPE ? "true" : "false"),null },
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        INTERPRET_FALSE_AS_EMPTY_CELL = ("true".equals(values.get("Interpret false as empty cell?")) ? true : false );
        INTERPRET_ZERO_AS_EMPTY_CELL = ("true".equals(values.get("Interpret zero character (0) as empty cell?")) ? true : false );
        INTERPRET_ZERO_LENGTH_STRING_AS_EMPTY_CELL = ("true".equals(values.get("Interpret zero length string as empty cell?")) ? true : false );
        
        INTERPRET_COLOR_AS_VALID_CELL_VALUE = ("true".equals(values.get("Interpret background color as nonempty cell?")) ? true : false );
        ADD_CELL_COLOR_AS_PLAYER = ("true".equals(values.get("Add cell's background color to association as player?")) ? true : false );
        ADD_CELL_VALUE_AS_PLAYER = ("true".equals(values.get("Add cell's value to association as player?")) ? true : false );
        
        USE_SHEET_AS_ASSOCIATION_TYPE = ("true".equals(values.get("Use sheet as an association type?")) ? true : false );
    }
    
    
    
}
