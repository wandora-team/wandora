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
 */



package org.wandora.application.tools.extractors.excel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
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


public class ExcelAdjacencyListExtractor extends AbstractExcelExtractor {

    public static boolean FIRST_ROW_CONTAINS_ROLES = true;
    
    
    private HashMap<String,String> rolesPerColumn = new HashMap();
    
    @Override
    public String getName(){
        return "Excel adjacency list extractor";
    }
    @Override
    public String getDescription(){
        return "Excel adjacency list extractor interprets Excel sheet as an adjacency list.";
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
        rolesPerColumn = new HashMap();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst && FIRST_ROW_CONTAINS_ROLES) {
                processRowAsRoles(row, tm);
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
        rolesPerColumn = new HashMap();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst && FIRST_ROW_CONTAINS_ROLES) {
                processRowAsRoles(row, tm);
                isFirst = false;
            }
            else {
                processRow(row, tm);
            }
        }
    }
    
    
    
    
    public void processRow(Row row, TopicMap tm) {
        Iterator<Cell> cellIterator = row.cellIterator();
        Association a = null;
        while(cellIterator.hasNext() && !forceStop()) {
            try {
                Cell cell = cellIterator.next();
                if(getCellValueAsString(cell) != null) {
                    Topic player = getCellTopic(cell, tm);
                    if(player != null) {
                        if(a == null) {
                            a = tm.createAssociation(getDefaultAssociationTypeTopic(tm));
                        }
                        if(a != null) {
                            String roleSI = rolesPerColumn.get(Integer.toString(cell.getColumnIndex()));
                            Topic role = tm.getTopic(roleSI);
                            if(role == null) role = getDefaultRoleTopic(cell, tm);
                            a.addPlayer(player, role);
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
    }
    
    
    
    
    public void processRowAsRoles(Row row, TopicMap topicMap) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while(cellIterator.hasNext()) {
            try {
                Cell cell = cellIterator.next();
                if(getCellValueAsString(cell) != null) {
                    Topic cellTopic = getCellTopic(cell, topicMap);
                    rolesPerColumn.put(Integer.toString(cell.getColumnIndex()), cellTopic.getOneSubjectIdentifier().toExternalForm());
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
    

    
    public Topic getDefaultRoleTopic(Cell cell, TopicMap tm) {
        int i = cell.getColumnIndex();
        Topic typeTopic = getOrCreateTopic(tm, DEFAULT_ROLE_TYPE_SI+"/"+i, "Excel role "+i);
        return typeTopic;
    }
    
    
    
    
    
    // ---------------------------------------------------------- CONFIGURE ----
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        Wandora wandora = Wandora.getWandora();
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Excel adjacency list extractor options","Excel adjacency list extractor options",true,new String[][]{
            new String[]{"First row contains association role topics?","boolean",(FIRST_ROW_CONTAINS_ROLES ? "true" : "false"),null },
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        FIRST_ROW_CONTAINS_ROLES = ("true".equals(values.get("First row contains association role topics?")) ? true : false );
    }
    
    
    
}
