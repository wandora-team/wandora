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
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */


public class ExcelTopicTreeExtractor extends AbstractExcelExtractor {


	private static final long serialVersionUID = 1L;
	
	public static boolean MAKE_SUPER_SUB_CLASS_RELATION = false;
    public static boolean MAKE_CLASS_INSTANCE_RELATION = true;
    public static boolean MAKE_EXCEL_RELATION = false;
    public static boolean MAKE_CUSTOM_RELATION = false;
    
    
    private String customAssociationTypeSI = null;
    private String customUpperRoleSI = null;
    private String customLowerRoleSI = null;

    private Topic[] hierarchy = null;

    
    
    
    @Override
    public String getName(){
        return "Excel topic tree extractor";
    }
    @Override
    public String getDescription(){
        return "Excel topics and associations in tree layout.";
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
        hierarchy = new Topic[1000];
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            processRow(row, tm);
        }
    }
    
    
    
    public void processSheet(XSSFSheet sheet, TopicMap tm) {
        Iterator<Row> rowIterator = sheet.iterator();
        hierarchy = new Topic[1000];
        while(rowIterator.hasNext() && !forceStop()) {
            Row row = rowIterator.next();
            processRow(row, tm);
        }
    }
    
    
    
    
    public void processRow(Row row, TopicMap tm) {
        int firstColumn = row.getFirstCellNum();
        int lastColumn = row.getLastCellNum();

        for(int j=firstColumn; j<=lastColumn && !forceStop(); j++) {
            try {
                Cell cell = row.getCell(j);
                
                if(getCellValueAsString(cell) != null) {
                    
                    Topic t = getCellTopic(cell, tm);

                    if(t != null) {
                        for(int k=j-1; k>=0; k--) {
                            Topic ct = hierarchy[k];
                            if(ct != null) {
                                try {
                                    if(MAKE_SUPER_SUB_CLASS_RELATION) {
                                        Association a = tm.createAssociation(tm.getTopic(XTMPSI.SUPERCLASS_SUBCLASS));
                                        if(a != null) {
                                            Topic superClassRole = tm.getTopic(XTMPSI.SUPERCLASS);
                                            Topic subClassRole = tm.getTopic(XTMPSI.SUBCLASS);
                                            if(superClassRole != null && subClassRole != null) {
                                                a.addPlayer(ct, superClassRole);
                                                a.addPlayer(t, subClassRole);
                                            }
                                        }
                                    }
                                    if(MAKE_CLASS_INSTANCE_RELATION) {
                                        t.addType(ct);
                                    }
                                    if(MAKE_EXCEL_RELATION) {
                                        Association a = tm.createAssociation(getDefaultAssociationTypeTopic(tm));
                                        if(a != null) {
                                            Topic upperRole = getDefaultUpperRoleTopic(tm);
                                            Topic lowerRole = getDefaultLowerRoleTopic(tm);
                                            if(upperRole != null && lowerRole != null) {
                                                a.addPlayer(ct, upperRole);
                                                a.addPlayer(t, lowerRole);
                                            }
                                        }
                                    }
                                    if(MAKE_CUSTOM_RELATION) {
                                        if(customAssociationTypeSI == null || customUpperRoleSI == null || customLowerRoleSI == null) {
                                            requestCustomTypeAndRoles(tm);
                                        }
                                        if(customAssociationTypeSI != null && customUpperRoleSI != null && customLowerRoleSI != null) {
                                            Association a = tm.createAssociation(tm.getTopic(customAssociationTypeSI));
                                            if(a != null) {
                                                Topic upperRole = tm.getTopic(customUpperRoleSI);
                                                Topic lowerRole = tm.getTopic(customLowerRoleSI);
                                                if(upperRole != null && lowerRole != null) {
                                                    a.addPlayer(ct, upperRole);
                                                    a.addPlayer(t, lowerRole);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                                catch(Exception e) {}
                            }
                        }
                        hierarchy[j] = t;
                        for(int k=j+1; k<1000; k++) {
                            hierarchy[k] = null;
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
    
    
    
    private void requestCustomTypeAndRoles(TopicMap tm) {
        Wandora wandora = Wandora.getWandora();
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Custom association type and roles","Custom association type and roles",true,new String[][]{
            new String[]{"Custom association type topic","topic", null, "If custom relation is chosen, what is the association type?"},
            new String[]{"Custom upper role topic","topic", null, "If custom relation is chosen, what is the upper role topic?"},
            new String[]{"Custom lower role topic","topic", null, "If custom relation is chosen, what is the lower role topic?"},
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();

        customAssociationTypeSI = values.get("Custom association type topic");
        customUpperRoleSI = values.get("Custom upper role topic");
        customLowerRoleSI = values.get("Custom lower role topic");
    }
    
    
    
    // ---------------------------------------------------------- CONFIGURE ----
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Excel topic tree extractor options","Excel topic tree extractor options",true,new String[][]{
            new String[]{"Make superclass-subclass relation?","boolean",(MAKE_SUPER_SUB_CLASS_RELATION ? "true" : "false"),null },
            new String[]{"Make class-instance relation?","boolean",(MAKE_CLASS_INSTANCE_RELATION ? "true" : "false"),null },    
            new String[]{"Make default Excel relation?","boolean",(MAKE_EXCEL_RELATION ? "true" : "false"),null },
            new String[]{"Make custom relation?","boolean",(MAKE_CUSTOM_RELATION ? "true" : "false"),null }, 
            
            new String[]{"Custom association type topic","topic", null, "If custom relation is chosen, what is the association type?"},
            new String[]{"Custom upper role topic","topic", null, "If custom relation is chosen, what is the upper role topic?"},
            new String[]{"Custom lower role topic","topic", null, "If custom relation is chosen, what is the lower role topic?"},
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        MAKE_SUPER_SUB_CLASS_RELATION = ("true".equals(values.get("Make superclass-subclass relation?")) ? true : false );
        MAKE_CLASS_INSTANCE_RELATION = ("true".equals(values.get("Make class-instance relation?")) ? true : false );
        MAKE_EXCEL_RELATION = ("true".equals(values.get("Make default Excel relation?")) ? true : false );
        MAKE_CUSTOM_RELATION = ("true".equals(values.get("Make custom relation?")) ? true : false );
        
        customAssociationTypeSI = values.get("Custom association type topic");
        customUpperRoleSI = values.get("Custom upper role topic");
        customLowerRoleSI = values.get("Custom lower role topic");
    }
    
    
    
}
