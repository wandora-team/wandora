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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wandora.application.Wandora;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */


public class ExcelTopicNameExtractor extends AbstractExcelExtractor {

    

	private static final long serialVersionUID = 1L;
	
	
	public static boolean FIRST_ROW_CONTAINS_LANGUAGES = true;
    public static boolean CREATE_MISSING_LANGUAGE_TOPICS = true;
    
    public static boolean ADD_DISPLAY_TO_SCOPE = true;
    public static boolean ADD_SORT_TO_SCOPE = false;
    
    private Map<String,String> languagesPerColumn = new HashMap<>();
    
    
    
    
    
    @Override
    public String getName(){
        return "Excel topic name extractor";
    }
    @Override
    public String getDescription(){
        return "Excel topic name extractor extracts variant names for first column topics.";
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
        languagesPerColumn = new HashMap<>();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst && FIRST_ROW_CONTAINS_LANGUAGES) {
                processRowAsLanguages(row, tm);
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
        languagesPerColumn = new HashMap<>();
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            if(isFirst && FIRST_ROW_CONTAINS_LANGUAGES) {
                processRowAsLanguages(row, tm);
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
                    if(cell.getColumnIndex() != 0) {
                        String name = getCellValueAsString(cell);
                        if(name != null) {
                            String langSI = languagesPerColumn.get(Integer.toString(cell.getColumnIndex()));
                            Topic lang = tm.getTopic(langSI);
                            if(lang == null) lang = getDefaultLanguageTopic(cell, tm);
                            Set<Topic> scope = new LinkedHashSet<>();
                            scope.add(lang);
                            if(ADD_DISPLAY_TO_SCOPE) scope.add(tm.getTopic(XTMPSI.DISPLAY));
                            if(ADD_SORT_TO_SCOPE) scope.add(tm.getTopic(XTMPSI.SORT));
                            topic.setVariant(scope, name);
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
    
    
    
    
    public void processRowAsLanguages(Row row, TopicMap tm) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while(cellIterator.hasNext()) {
            try {
                String langSI = null;
                Cell cell = cellIterator.next();
                String lang = getCellValueAsString(cell);
                if(lang != null) {
                    Topic langTopic = tm.getTopicWithBaseName(lang);
                    if(langTopic != null) langSI = langTopic.getOneSubjectIdentifier().toExternalForm();
                    else langSI = XTMPSI.getLang(lang);
                    langTopic = tm.getTopic(new Locator(langSI));
                    languagesPerColumn.put(Integer.toString(cell.getColumnIndex()), langSI);
                    
                    if(langTopic == null && CREATE_MISSING_LANGUAGE_TOPICS) {
                        langTopic = tm.createTopic();
                        langTopic.addSubjectIdentifier(new Locator(langSI));
                        Topic langTypeTopic = tm.getTopic(XTMPSI.LANGUAGE);
                        if(langTypeTopic != null) {
                            langTopic.addType(langTypeTopic);
                        }
                    }
                }
            }
            catch (Exception ex) {
                log(ex);
            }
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    public static String DEFAULT_LANGUAGE_SI = EXCEL_SI_PREFIX + "/language";

    
    public Topic getDefaultLanguageTopic(Cell cell, TopicMap tm) {
        if(cell != null && tm != null) {
            int i = cell.getColumnIndex();
            Topic typeTopic = getOrCreateTopic(tm, DEFAULT_LANGUAGE_SI+"/"+i, "Excel language "+i);
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
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Excel topic name extractor options","Excel topic name extractor options",true,new String[][]{
            new String[]{"First row contains languages?","boolean",(FIRST_ROW_CONTAINS_LANGUAGES ? "true" : "false"),null },
            new String[]{"Create display names?","boolean",(ADD_DISPLAY_TO_SCOPE ? "true" : "false"),null },
            new String[]{"Create sort names?","boolean",(ADD_SORT_TO_SCOPE ? "true" : "false"),null },
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        FIRST_ROW_CONTAINS_LANGUAGES = ("true".equals(values.get("First row contains languages?")) ? true : false );
        ADD_DISPLAY_TO_SCOPE = ("true".equals(values.get("Create display names?")) ? true : false );
        ADD_SORT_TO_SCOPE = ("true".equals(values.get("Create sort names?")) ? true : false );
        
    }
    
    
}
