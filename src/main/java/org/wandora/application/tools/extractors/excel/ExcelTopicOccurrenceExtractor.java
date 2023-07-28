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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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


public class ExcelTopicOccurrenceExtractor extends AbstractExcelExtractor {


	private static final long serialVersionUID = 1L;


	public static boolean FIRST_ROW_CONTAINS_OCCURRENCE_TYPES = true;
    
    
    private HashMap<String,String> occurrenceTypes = new LinkedHashMap<>();
    
    
    @Override
    public String getName(){
        return "Excel topic occurrence extractor";
    }
    @Override
    public String getDescription(){
        return "Excel topic occurrence extractor extracts occurrences for first column topics.";
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
        occurrenceTypes = new HashMap<>();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst && FIRST_ROW_CONTAINS_OCCURRENCE_TYPES) {
                processRowAsOccurrenceTypes(row, tm);
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
        occurrenceTypes = new HashMap<>();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst && FIRST_ROW_CONTAINS_OCCURRENCE_TYPES) {
                processRowAsOccurrenceTypes(row, tm);
                isFirst = false;
            }
            else {
                processRow(row, tm);
            }
        }
    }
    
    
    
    
    public void processRow(Row row, TopicMap tm) {
        try {
            Iterator<Cell> cellIterator = row.cellIterator();
            Topic topic = null;
            Cell firstCell = row.getCell(0);
            if(getCellValueAsString(firstCell) != null) {
                topic = getCellTopic(firstCell, tm);
            }
            if(topic != null) {
                while(cellIterator.hasNext() && !forceStop()) {
                    Cell cell = cellIterator.next();
                    if(cell.getColumnIndex() > 0) {
                        String occurrence = getCellValueAsString(cell);
                        if(occurrence != null) {
                            Topic type = null;
                            String typeSI = occurrenceTypes.get(Integer.toString(cell.getColumnIndex()));
                            if(typeSI != null) type = tm.getTopic(typeSI);
                            if(type == null) type = getDefaultOccurrenceTypeTopic(cell, tm);
                            topic.setData(type, tm.getTopic(XTMPSI.getLang(DEFAULT_LANG)), occurrence);
                        }
                    }
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
    
    
    
    
    public void processRowAsOccurrenceTypes(Row row, TopicMap topicMap) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while(cellIterator.hasNext()) {
            try {
                Cell cell = cellIterator.next();
                if(getCellValueAsString(cell) != null) {
                    Topic cellTopic = getCellTopic(cell, topicMap);
                    occurrenceTypes.put(Integer.toString(cell.getColumnIndex()), cellTopic.getOneSubjectIdentifier().toExternalForm());
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
    
    
    
    
    // -------------------------------------------------------------------------
    

    
    public Topic getDefaultOccurrenceTypeTopic(Cell cell, TopicMap tm) {
        if(cell != null && tm != null) {
            int i = cell.getColumnIndex();
            Topic typeTopic = getOrCreateTopic(tm, DEFAULT_OCCURRENCE_TYPE_SI+"/"+i, "Excel occurrence type "+i);
            return typeTopic;
        }
        return null;
    }
    
    
    
    


    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- CONFIGURE ----
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        Wandora wandora = Wandora.getWandora();
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Excel topic occurrence extractor options","Excel topic occurrence extractor options",true,new String[][]{
            new String[]{"First row contains occurrence types?","boolean",(FIRST_ROW_CONTAINS_OCCURRENCE_TYPES ? "true" : "false"),null },
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        FIRST_ROW_CONTAINS_OCCURRENCE_TYPES = ("true".equals(values.get("First row contains occurrence types?")) ? true : false );       
    }
}
