/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wandora.application.Wandora;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.extractors.AbstractExtractor;
import static org.wandora.application.tools.extractors.AbstractExtractor.FILE_EXTRACTOR;
import static org.wandora.application.tools.extractors.AbstractExtractor.URL_EXTRACTOR;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class ExcelExtractor extends AbstractExtractor {


    @Override
    public String getName() {
        return "Excel extractor";
    }
    
    @Override
    public String getDescription() {
        return "Excel extractor enables sheet specific Excel extractor.";
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
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(u.openStream());
            ExcelExtractorUI ui = new ExcelExtractorUI();
            ui.open(getSheets(workbook), getExtractors());
            if(ui.wasAccepted()) {
                String[] extractors = ui.getExtractors();
                int numberOfSheets = workbook.getNumberOfSheets();
                for(int i=0; i<numberOfSheets && !forceStop(); i++) {
                    String sheetName = workbook.getSheetName(i);
                    AbstractExcelExtractor extractor = getExcelExtractorFor(extractors[i]);
                    if(extractor != null) {
                        log("Applying '"+extractor.getName()+"' to sheet '"+sheetName+"'.");
                        extractor.setToolLogger(getDefaultLogger());
                        extractor.processSheet(workbook.getSheetAt(i), tm);
                    }
                    else  {
                        log("Leaving sheet '"+sheetName+"' unprocessed.");
                    }
                }
            }
            else {
                log("Extraction cancelled.");
            }
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


    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        try {
            if(f != null) {
                String fn = f.getAbsolutePath();
                if(fn.toLowerCase().endsWith(".xls")) {
                    HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(f));
                    ExcelExtractorUI ui = new ExcelExtractorUI();
                    ui.open(getSheets(workbook), getExtractors());
                    if(ui.wasAccepted()) {
                        String[] extractors = ui.getExtractors();
                        int numberOfSheets = workbook.getNumberOfSheets();
                        for(int i=0; i<numberOfSheets && !forceStop(); i++) {
                            String sheetName = workbook.getSheetName(i);
                            AbstractExcelExtractor extractor = getExcelExtractorFor(extractors[i]);
                            if(extractor != null) {
                                log("Applying '"+extractor.getName()+"' to sheet '"+sheetName+"'.");
                                extractor.setToolLogger(getDefaultLogger());
                                extractor.processSheet(workbook.getSheetAt(i), tm);
                            }
                            else  {
                                log("Leaving sheet '"+sheetName+"' unprocessed.");
                            }
                        }
                    }
                    else {
                        log("Extraction cancelled.");
                    }
                }
                else {
                    XSSFWorkbook workbook = new XSSFWorkbook(f.getAbsolutePath());
                    ExcelExtractorUI ui = new ExcelExtractorUI();
                    ui.open(getSheets(workbook), getExtractors());
                    if(ui.wasAccepted()) {
                        String[] extractors = ui.getExtractors();
                        int numberOfSheets = workbook.getNumberOfSheets();
                        for(int i=0; i<numberOfSheets && !forceStop(); i++) {
                            String sheetName = workbook.getSheetName(i);
                            AbstractExcelExtractor extractor = getExcelExtractorFor(extractors[i]);
                            if(extractor != null) {
                                log("Applying '"+extractor.getName()+"' to sheet '"+sheetName+"'.");
                                extractor.setToolLogger(getDefaultLogger());
                                extractor.processSheet(workbook.getSheetAt(i), tm);
                            }
                            else  {
                                log("Leaving sheet '"+sheetName+"' unprocessed.");
                            }
                        }
                    }
                    else {
                        log("Extraction cancelled.");
                    }
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
    
    
    
    
    public Collection getSheets(HSSFWorkbook workbook) {
        ArrayList<String> sheets = new ArrayList();
        int numberOfSheets = workbook.getNumberOfSheets();
        for(int i=0; i<numberOfSheets && !forceStop(); i++) {
            sheets.add(workbook.getSheetName(i));
        }
        return sheets;
    }
    
    
    
    
    public Collection getSheets(XSSFWorkbook workbook) {
        ArrayList<String> sheets = new ArrayList();
        int numberOfSheets = workbook.getNumberOfSheets();
        for(int i=0; i<numberOfSheets && !forceStop(); i++) {
            sheets.add(workbook.getSheetName(i));
        }
        return sheets;
    }
    
    
    
    
    public Collection<String> getExtractors() {
        ArrayList<String> extractors = new ArrayList();
        extractors.add("-- Don't extract --");
        extractors.add("Extract topics");
        extractors.add("Extract adjacency list");
        extractors.add("Extract adjacency matrix");
        extractors.add("Extract association tree");
        extractors.add("Extract variant names");
        extractors.add("Extract occurrences");
        return extractors;
    }
    
    
    
    
    public AbstractExcelExtractor getExcelExtractorFor(String extractorName) {
        if("Extract topics".equals(extractorName)) return new ExcelTopicExtractor();
        if("Extract adjacency list".equals(extractorName)) return new ExcelAdjacencyListExtractor();
        if("Extract adjacency matrix".equals(extractorName)) return new ExcelAdjacencyMatrixExtractor();
        if("Extract association tree".equals(extractorName)) return new ExcelTopicTreeExtractor();
        if("Extract variant names".equals(extractorName)) return new ExcelTopicNameExtractor();
        if("Extract occurrences".equals(extractorName)) return new ExcelTopicOccurrenceExtractor();
        else return null;
    }
    

    
    
    
}
