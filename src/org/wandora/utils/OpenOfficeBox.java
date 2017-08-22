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
 */

package org.wandora.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.odftoolkit.simple.ChartDocument;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.GraphicsDocument;
import org.odftoolkit.simple.PresentationDocument;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

/**
 * Simple collection of methods used to access and extract Open Office
 * documents.
 *
 * @author akivela
 */
public class OpenOfficeBox {
    
    
    
    
    public static String getText(URL url) {
        try {
            return getText(Document.loadDocument(url.openStream()));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static String getText(File file) {
        try {
            return getText(Document.loadDocument(file));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static String getText(InputStream is) {
        try {
            return getText(Document.loadDocument(is));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static String getText(Document document) {
        if(document instanceof TextDocument) {
            return getText((TextDocument) document);
        }
        else if(document instanceof SpreadsheetDocument) {
            return getText((SpreadsheetDocument) document);
        }
        else if(document instanceof PresentationDocument) {
            return getText((PresentationDocument) document);
        }
        else if(document instanceof ChartDocument) {
            return getText((ChartDocument) document);
        }
        else if(document instanceof GraphicsDocument) {
            return getText((GraphicsDocument) document);
        }
        return null;
    }
    
    
    
    public static String getText(ChartDocument chartDocument) {
        try {
            String text = chartDocument.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static String getText(GraphicsDocument gfxDocument) {
        try {
            String text = gfxDocument.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static String getText(PresentationDocument presentationDocument) {
        try {
            String text = presentationDocument.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static String getText(TextDocument textDocument) {
        try {
            String text = textDocument.getContentRoot().getTextContent();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static String getText(SpreadsheetDocument spreadsheetDocument) {
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            int sheetCount = spreadsheetDocument.getSheetCount();
            for(int i=0; i<sheetCount; i++) {
                Table sheet = spreadsheetDocument.getSheetByIndex(i);
                int rowCount = sheet.getRowCount();
                for(int y=0; y<rowCount; y++) {
                    Row row = sheet.getRowByIndex(y);
                    int cellCount = row.getCellCount();
                    for(int x=0; x<cellCount; x++) {
                        Cell cell = row.getCellByIndex(x);
                        String value = cell.getStringValue();
                        stringBuilder.append(value);
                        stringBuilder.append("\t");
                    }
                    stringBuilder.append("\n");
                }
                stringBuilder.append("\n\n");
            }
            return stringBuilder.toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
