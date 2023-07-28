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
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wandora.application.Wandora;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */


public class ExcelTopicExtractor extends AbstractExcelExtractor {

	private static final long serialVersionUID = 1L;
	
	public static boolean EXTRACT_CELL_TYPE = true;
    public static boolean EXTRACT_CELL_COLORS = true;
    public static boolean EXTRACT_SHEET = true;
    public static boolean EXTRACT_CELL_FORMULA = true;
    public static boolean EXTRACT_CELL_COMMENT = true;
    public static boolean EXTRACT_CELL_LOCATION = true;
    
    
    @Override
    public String getName(){
        return "Excel topic extractor";
    }
    @Override
    public String getDescription(){
        return "Excel topic extractor extracts cell values as topics and cell properties as associations of the topic.";
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
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            processRow(row, tm);
        }
    }
    
    
    
    public void processSheet(XSSFSheet sheet, TopicMap tm) {
        Iterator<Row> rowIterator = sheet.iterator();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            processRow(row, tm);
        }
    }
    
    
    
    
    public void processRow(Row row, TopicMap tm) {
        Iterator<Cell> cellIterator = row.cellIterator();

        try {
            while(cellIterator.hasNext() && !forceStop()) {
                Cell cell = cellIterator.next();
                processCell(cell, tm);
            }
        }
        catch (Exception ex) {
            log(ex);
        }
    }
    
    
    
    public void processCell(Cell cell, TopicMap tm) {
        if(getCellValueAsString(cell) != null) {
            try {
                Topic t = getCellTopic(cell, tm);
                if(t != null) {
                    if(EXTRACT_SHEET)          associateToSheet(cell, tm);
                    if(EXTRACT_CELL_LOCATION)  associateToLocation(cell, tm);
                    if(EXTRACT_CELL_COLORS)    associateToColors(cell, tm);
                    if(EXTRACT_CELL_TYPE)      associateToType(cell, tm);
                    if(EXTRACT_CELL_COMMENT)   associateToComment(cell, tm);
                    if(EXTRACT_CELL_FORMULA)   associateToFormula(cell, tm);
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
    }

    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    

    @Override
    public Topic getCellTopic(Cell cell, TopicMap tm) throws TopicMapException {
        Topic cellTopic = super.getCellTopic(cell,tm);
        if(cellTopic != null) {
            String str = getCellValueAsString(cell);
            if(str != null) {
                cellTopic.setData(getCellValueTypeTopic(tm), tm.getTopic(XTMPSI.getLang(DEFAULT_LANG)), str);
            }
        }
        return cellTopic;
    }
    
    
    
    
    
    // ---------------------------------------------------------- CONFIGURE ----
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        Wandora wandora = Wandora.getWandora();
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Excel topic extractor options","Excel topic extractor options",true,new String[][]{
            new String[]{"Extract cell colors?","boolean",(EXTRACT_CELL_COLORS ? "true" : "false"), "Associate cell to it's background and foreground color?" },
            new String[]{"Extract sheet?","boolean",(EXTRACT_SHEET ? "true" : "false"), "Associate cell to it's sheet?" },    
            new String[]{"Extract cell location?","boolean",(EXTRACT_CELL_LOCATION ? "true" : "false"), "Associate cell to it's row and column?" },
            new String[]{"Extract cell formula?","boolean",(EXTRACT_CELL_FORMULA ? "true" : "false"), "Associate cell to it's formula?" },
            new String[]{"Extract cell comment?","boolean",(EXTRACT_CELL_COMMENT ? "true" : "false"), "Associate cell to it's comment?" },
            new String[]{"Extract cell type?","boolean",(EXTRACT_CELL_TYPE ? "true" : "false"), "Associate cell to it's type?" },
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXTRACT_CELL_COLORS = ("true".equals(values.get("Extract cell colors?")) ? true : false );
        EXTRACT_SHEET = ("true".equals(values.get("Extract sheet?")) ? true : false );
        EXTRACT_CELL_LOCATION = ("true".equals(values.get("Extract cell location?")) ? true : false );
        EXTRACT_CELL_FORMULA = ("true".equals(values.get("Extract cell formula?")) ? true : false );
        EXTRACT_CELL_COMMENT = ("true".equals(values.get("Extract cell comment?")) ? true : false );
        EXTRACT_CELL_TYPE = ("true".equals(values.get("Extract cell type?")) ? true : false );
    }
    
    
}
