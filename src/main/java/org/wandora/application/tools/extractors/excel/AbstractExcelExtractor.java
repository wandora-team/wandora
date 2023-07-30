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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.Icon;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */


public abstract class AbstractExcelExtractor extends AbstractExtractor implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	public static String DEFAULT_LANG = "en";
    
    
    
    
    @Override
    public String getName() {
        return "Abstract Excel extractor";
    }
    
    @Override
    public String getDescription() {
        return "Abstract Excel extractor.";
    }
    
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_excel.png");
    }
    
    @Override
    public boolean runInOwnThread(){
        return true;
    }
    
    @Override
    public boolean useTempTopicMap(){
        return false;
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    


    @Override
    public boolean _extractTopicsFrom(File f, TopicMap topicMap) throws Exception {
        try {
            if(f != null) {
                String fn = f.getAbsolutePath();
                if(fn.toLowerCase().endsWith(".xls")) {
                    HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(f));
                    processWorkbook(workbook, topicMap);
                }
                else {
                    XSSFWorkbook workbook = new XSSFWorkbook(f.getAbsolutePath());
                    processWorkbook(workbook, topicMap);
                }
                log("Ok!");
            }
        }
        catch (FileNotFoundException ex) {
            log(ex);
        } 
        catch (IOException ex) {
            log(ex);
        }
        catch (Exception ex) {
            log(ex);
        }
        setState(WAIT);
        return true;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap topicMap) throws Exception {
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(u.openStream());
            processWorkbook(workbook, topicMap);
            log("Ok!");
        }
        catch (FileNotFoundException ex) {
            log(ex);
        } 
        catch (IOException ex) {
            log(ex);
        }
        catch (Exception ex) {
            log(ex);
        }
        setState(WAIT);
        return true;
    }
    
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        return false;
    }
    
    
    
    public abstract void processWorkbook(HSSFWorkbook workbook, TopicMap topicMap);
    public abstract void processWorkbook(XSSFWorkbook workbook, TopicMap topicMap);
    public abstract void processSheet(HSSFSheet sheet, TopicMap topicMap);
    public abstract void processSheet(XSSFSheet sheet, TopicMap topicMap);
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    protected String getCellValueAsString(Cell cell) {
        if(cell != null) {
            if(cell.getCellType() == CellType.FORMULA) {
                return getCellValueAsString(cell, cell.getCachedFormulaResultType());
            }
            else {
                return getCellValueAsString(cell, cell.getCellType());
            }
        }
        return null;
    }
    
    
   
    
    private DataFormatter formatter = new DataFormatter();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    protected String getCellValueAsString(Cell cell, CellType type) {
        if(cell != null) {
            switch(type) {
                case ERROR: {
                    return "ERROR"+cell.getErrorCellValue();
                }
                case BOOLEAN: {
                    return ""+cell.getBooleanCellValue();
                }
                case NUMERIC: {
                    if(DateUtil.isCellDateFormatted(cell)) {
                        return dateFormat.format(cell.getDateCellValue());
                    }
                    else {
                        double value = cell.getNumericCellValue();
                        String formatString = cell.getCellStyle().getDataFormatString();
                        int formatIndex = cell.getCellStyle().getDataFormat();
                        return formatter.formatRawCellContents(value, formatIndex, formatString);
                    }
                }
                case STRING: {
                    return cell.getRichStringCellValue().getString();
                }
            }
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    

    public void associateToSheet(Cell cell, TopicMap tm) throws TopicMapException {
        if(cell.getSheet() != null) {
            Topic sheetTypeTopic = getSheetTypeTopic(tm);
            Topic sheetTopic = getSheetTopic(cell, tm);
            Topic cellTypeTopic = getCellTypeTopic(tm);
            Topic cellTopic = getCellTopic(cell, tm);

            if(sheetTypeTopic != null && sheetTopic != null && cellTypeTopic != null && cellTopic != null) {
                Association a = tm.createAssociation(sheetTypeTopic);
                a.addPlayer(cellTopic, cellTypeTopic);
                a.addPlayer(sheetTopic, sheetTypeTopic);
            }
        }
    }
    
    public void associateToLocation(Cell cell, TopicMap tm) throws TopicMapException {
        Topic locationTypeTopic = getCellLocationTypeTopic(tm);
        Topic rowTypeTopic = getRowTypeTopic(tm);
        Topic rowTopic = getRowTopic(cell, tm);
        Topic columnTypeTopic = getColumnTypeTopic(tm);
        Topic columnTopic = getColumnTopic(cell, tm);
        Topic cellTypeTopic = getCellTypeTopic(tm);
        Topic cellTopic = getCellTopic(cell, tm);
        
        if(locationTypeTopic != null && rowTypeTopic != null && rowTopic != null && columnTopic != null && columnTypeTopic != null && cellTypeTopic != null && cellTopic != null) {
            Association a = tm.createAssociation(locationTypeTopic);
            a.addPlayer(cellTopic, cellTypeTopic);
            a.addPlayer(rowTopic, rowTypeTopic);
            a.addPlayer(columnTopic, columnTypeTopic);
        }
    }
    
    
    public void associateToColors(Cell cell, TopicMap tm) throws TopicMapException {
        if(cell.getCellStyle() != null) {
            Topic colorTypeTopic = getBackgroundColorTypeTopic(tm);
            Topic colorTopic = getColorTopic(cell, tm);
            Topic cellTypeTopic = getCellTypeTopic(tm);
            Topic cellTopic = getCellTopic(cell, tm);

            if(colorTypeTopic != null && colorTopic != null && cellTypeTopic != null && cellTopic != null) {
                Association a = tm.createAssociation(colorTypeTopic);
                a.addPlayer(cellTopic, cellTypeTopic);
                a.addPlayer(colorTopic, colorTypeTopic);
            }
            
            colorTypeTopic = getForegroundColorTypeTopic(tm);
            colorTopic = getColorTopic(cell, tm);

            if(colorTypeTopic != null && colorTopic != null && cellTypeTopic != null && cellTopic != null) {
                Association a = tm.createAssociation(colorTypeTopic);
                a.addPlayer(cellTopic, cellTypeTopic);
                a.addPlayer(colorTopic, colorTypeTopic);
            }
        }
    }
    
    public void associateToType(Cell cell, TopicMap tm) throws TopicMapException {
        Topic typeTypeTopic = getCellTypeTypeTopic(tm);
        Topic typeTopic = getCellTypeTopic(cell, tm);
        Topic cellTypeTopic = getCellTypeTopic(tm);
        Topic cellTopic = getCellTopic(cell, tm);
        
        if(typeTypeTopic != null && typeTopic != null && cellTypeTopic != null && cellTopic != null) {
            Association a = tm.createAssociation(typeTypeTopic);
            a.addPlayer(cellTopic, cellTypeTopic);
            a.addPlayer(typeTopic, typeTypeTopic);
        }
    }
    
    public void associateToComment(Cell cell, TopicMap tm) throws TopicMapException {
        if(cell.getCellComment() != null) {
            Topic commentTypeTopic = getCommentTypeTopic(tm);
            Topic commentTopic = getCommentTopic(cell, tm);
            Topic cellTypeTopic = getCellTypeTopic(tm);
            Topic cellTopic = getCellTopic(cell, tm);

            if(commentTypeTopic != null && commentTopic != null && cellTypeTopic != null && cellTopic != null) {
                Association a = tm.createAssociation(commentTypeTopic);
                a.addPlayer(cellTopic, cellTypeTopic);
                a.addPlayer(commentTopic, commentTypeTopic);
            }
        }
    }
    
    public void associateToFormula(Cell cell, TopicMap tm) throws TopicMapException {
        if(cell.getCellType() == CellType.FORMULA) {
            if(cell.getCellFormula() != null) {
                Topic formulaTypeTopic = getFormulaTypeTopic(tm);
                Topic formulaTopic = getFormulaTopic(cell, tm);
                Topic cellTypeTopic = getCellTypeTopic(tm);
                Topic cellTopic = getCellTopic(cell, tm);

                if(formulaTypeTopic != null && formulaTopic != null && cellTypeTopic != null && cellTopic != null) {
                    Association a = tm.createAssociation(formulaTypeTopic);
                    a.addPlayer(cellTopic, cellTypeTopic);
                    a.addPlayer(formulaTopic, formulaTypeTopic);
                }
            }
        }
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    public static final int CELL_VALUE = 1;
    public static final int CELL_LOCATION = 2;
    public static final int CELL_SHEET_AND_LOCATION = 4;
    public static final int CELL_HASH = 8;
    
    
    public static int CELL_TOPIC_IS_BASED_ON = CELL_VALUE;
    

    public static String EXCEL_SI_PREFIX = "http://wandora.org/si/excel";
    public static String EXCEL_COLUMN_SI_PREFIX = EXCEL_SI_PREFIX + "/column";
    public static String EXCEL_ROW_SI_PREFIX = EXCEL_SI_PREFIX + "/row";
    public static String EXCEL_SHEET_SI_PREFIX = EXCEL_SI_PREFIX + "/sheet";
    public static String EXCEL_CELL_SI_PREFIX = EXCEL_SI_PREFIX + "/cell";
    public static String EXCEL_COLOR_SI_PREFIX = EXCEL_SI_PREFIX + "/color";
    public static String EXCEL_FORMULA_SI_PREFIX = EXCEL_SI_PREFIX + "/formula";
    public static String EXCEL_COMMENT_SI_PREFIX = EXCEL_SI_PREFIX + "/comment";
    public static String EXCEL_CELL_TYPE_SI_PREFIX = EXCEL_SI_PREFIX + "/cell-type";
    public static String EXCEL_CELL_LOCATION_SI_PREFIX = EXCEL_SI_PREFIX + "/cell-location";
    
    public static String DEFAULT_ASSOCIATION_TYPE_SI = EXCEL_SI_PREFIX+"/association-type";
    
    public static String DEFAULT_ROLE_TYPE_SI = EXCEL_SI_PREFIX+"/role";
    public static String DEFAULT_UPPER_ROLE_SI = DEFAULT_ROLE_TYPE_SI+"/upper";
    public static String DEFAULT_LOWER_ROLE_SI = DEFAULT_ROLE_TYPE_SI+"/lower";
    
    public static String DEFAULT_OCCURRENCE_TYPE_SI = EXCEL_SI_PREFIX+"/occurrence-type";
    
    public static String EXCEL_CELL_VALUE_SI = EXCEL_SI_PREFIX + "/cell-value";
    
    // -----
    
    
    
    
    public Topic getExcelTypeTopic(TopicMap tm) {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_SI_PREFIX, "Excel");
        try { typeTopic.addType(tm.getTopic(TMBox.WANDORACLASS_SI)); }
        catch(Exception e) {}
        return typeTopic;
    }
    
    
    
    // -----
    
    public Topic getCellTopic(Cell cell, TopicMap tm) throws TopicMapException {
        String cellIdentifier = null;
        switch(CELL_TOPIC_IS_BASED_ON) {
            case CELL_VALUE: {
                cellIdentifier = getCellValueAsString(cell);
                break;
            }
            case CELL_SHEET_AND_LOCATION: {
                Sheet sheet = cell.getSheet();
                String sheetName = sheet.getSheetName();
                cellIdentifier = sheetName+"-"+cell.getColumnIndex()+"-"+cell.getRowIndex();
                break;
            }
            case CELL_LOCATION: {
                cellIdentifier = cell.getColumnIndex()+"-"+cell.getRowIndex();
                break;
            }
            case CELL_HASH: {
                cellIdentifier = Integer.toString(cell.hashCode());
                break;
            }
        }
        if(cellIdentifier != null) {
            String si = EXCEL_CELL_SI_PREFIX +"/"+ urlEncode(cellIdentifier);
            Topic cellTopic = getOrCreateTopic(tm, si, cellIdentifier);
            cellTopic.addType(getCellTypeTopic(tm));
            return cellTopic;
        }
        return null;
    }
    
    
    
    
    public Topic getCellTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_CELL_SI_PREFIX, "Excel cell");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    
    
    
    public Topic getCellValueTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_CELL_VALUE_SI, "Excel cell value");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    
    
    // -----
    
    
    
    public Topic getCellTypeTopic(Cell cell, TopicMap tm) throws TopicMapException {
        CellType type = cell.getCellType();
        String typeStr = "string";
        switch(type) {
            case BLANK: {
                typeStr = "blank";
                break;
            }
            case BOOLEAN: {
                typeStr = "boolean";
                break;
            }
            case ERROR: {
                typeStr = "error";
                break;
            }
            case FORMULA: {
                typeStr = "formula";
                break;
            }
            case NUMERIC: {
                typeStr = "numeric";
                break;
            }
            case STRING: {
                typeStr = "string";
                break;
            }   
        }
        Topic t = getOrCreateTopic(tm, EXCEL_CELL_TYPE_SI_PREFIX+"/"+typeStr, "Excel cell type "+typeStr);
        t.addType(getCellTypeTypeTopic(tm));
        return t;
    }
    
    
    
    public Topic getCellTypeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_CELL_TYPE_SI_PREFIX, "Excel cell type");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
        
    
    // -----
    
    
    public Topic getColorTopic(Cell cell, TopicMap tm) throws TopicMapException {
        CellStyle style = cell.getCellStyle();
        int color = style.getFillBackgroundColor();
        String si = EXCEL_COLOR_SI_PREFIX + "/" + urlEncode(Integer.toString(color));
        Topic topic = getOrCreateTopic(tm, si, "Color "+color);
        topic.addType(getColorTypeTopic(tm));
        return topic;
    }
    
    
    
    public Topic getColorTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_COLOR_SI_PREFIX, "Excel color");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    public Topic getBackgroundColorTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_COLOR_SI_PREFIX+"/background", "Excel background color");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    public Topic getForegroundColorTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_COLOR_SI_PREFIX+"/foreground", "Excel foreground color");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    // -----
    
    
    
    public Topic getDefaultAssociationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, DEFAULT_ASSOCIATION_TYPE_SI, "Excel association type");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    public Topic getDefaultUpperRoleTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, DEFAULT_UPPER_ROLE_SI, "Excel upper role");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    public Topic getDefaultLowerRoleTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, DEFAULT_LOWER_ROLE_SI, "Excel lower role");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    // -----
    
    public Topic getRowTopic(Cell cell, TopicMap tm) throws TopicMapException {
        Topic topic=getOrCreateTopic(tm, EXCEL_ROW_SI_PREFIX+"/"+urlEncode(Integer.toString(cell.getRowIndex())), "Excel row "+cell.getRowIndex());
        topic.addType(getRowTypeTopic(tm));
        return topic;
    }

    
    public Topic getRowTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_ROW_SI_PREFIX, "Excel row");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    // -----
    
    public Topic getColumnTopic(Cell cell, TopicMap tm) throws TopicMapException {
        Topic topic=getOrCreateTopic(tm, EXCEL_COLUMN_SI_PREFIX+"/"+urlEncode(Integer.toString(cell.getColumnIndex())), "Excel column "+cell.getColumnIndex());
        topic.addType(getColumnTypeTopic(tm));
        return topic;
    }
    
    public Topic getColumnTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_COLUMN_SI_PREFIX, "Excel column");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    
    // -----
    


    
    public Topic getCommentTopic(Cell cell, TopicMap tm) throws TopicMapException {
        Comment comment = cell.getCellComment();
        if(comment != null) {
            RichTextString rts = comment.getString();
            String str = rts.getString();
            String basename = str.replace('\n', ' ');
            basename = basename.replace('\r', ' ');
            basename = basename.replace('\t', ' ');
            Topic topic=getOrCreateTopic(tm, EXCEL_COMMENT_SI_PREFIX+"/"+urlEncode(basename), basename);
            topic.setData(getCommentTypeTopic(tm), tm.getTopic(XTMPSI.getLang(DEFAULT_LANG)), str);
            topic.addType(getCommentTypeTopic(tm));
            return topic;
        }
        return null;
    }

    
    public Topic getCommentTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_COMMENT_SI_PREFIX, "Excel comment");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    
    // ------
    
    
    public Topic getFormulaTopic(Cell cell, TopicMap tm) throws TopicMapException {
        String formula = cell.getCellFormula();
        if(formula != null) {
            Topic topic=getOrCreateTopic(tm, EXCEL_FORMULA_SI_PREFIX+"/"+urlEncode(formula), formula);
            topic.setData(getFormulaTypeTopic(tm), tm.getTopic(XTMPSI.getLang(DEFAULT_LANG)), formula);
            topic.addType(getFormulaTypeTopic(tm));
            return topic;
        }
        return null;
    }
    
    
    public Topic getFormulaTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_FORMULA_SI_PREFIX, "Excel formula");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    

    // ------
    
    
    
    
    public Topic getCellLocationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_CELL_LOCATION_SI_PREFIX, "Excel cell location");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    
    // ------
    
    
    public Topic getSheetTopic(Cell cell, TopicMap tm) throws TopicMapException {
        Sheet sheet = cell.getSheet();
        if(sheet != null) {
            String sheetName = sheet.getSheetName();
            Topic topic=getOrCreateTopic(tm, EXCEL_SHEET_SI_PREFIX+"/"+urlEncode(sheetName), sheetName);
            topic.addType(getSheetTypeTopic(tm));
            return topic;
        }
        return null;
    }
    
    
    
    public Topic getSheetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic typeTopic = getOrCreateTopic(tm, EXCEL_SHEET_SI_PREFIX, "Excel sheet");
        typeTopic.addType(getExcelTypeTopic(tm));
        return typeTopic;
    }
    
    
    
    
    // -------------------------------------------------------------------------


    
    
    public Topic getOrCreateTopic(TopicMap map, String si, String basename) {
        Topic topic = null;
        try {
            topic = map.getTopic(si);
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                if(basename != null && basename.length() > 0) topic.setBaseName(basename);
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        return topic;
    }
    
    
    
    

    
}
