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
 *
 * 
 *
 * MSOfficeBox.java
 *
 * Created on 13. kesäkuuta 2006, 14:20
 *
 */

package org.wandora.utils;


import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.apache.poi.*;
import org.apache.poi.extractor.*;
import org.apache.poi.hslf.extractor.*;
import org.apache.poi.hwpf.extractor.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;




/**
 * Class to extract the text from MS office documents.
 * Based on Apache's POI framework
 *
 * @author akivela
 */



public class MSOfficeBox {
    
    /**
     * Creates a new instance of MSOfficeBox
     */
    public MSOfficeBox() {
    }
    
    
    
    
    // ----------------------------------------------------------- WORD TEXT ---
    
    
    public static String getWordTextOld(InputStream is) {
        try {
            return getWordTextOld(new HWPFDocument(is));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
	/**
	 * Get the text from the word file, as an array with one String
	 *  per paragraph
	 */
	public static String[] getWordParagraphText(HWPFDocument doc) {
		String[] ret;
		
		// Extract using the model code
		try {
	    	Range r = doc.getRange();

			ret = new String[r.numParagraphs()];
			for(int i=0; i<ret.length; i++) {
				Paragraph p = r.getParagraph(i);
				ret[i] = p.text();
				
				// Fix the line ending
				if(ret[i].endsWith("\r")) {
					ret[i] = ret[i] + "\n";
				}
			}
		}
                catch(Exception e) {
			// Something's up with turning the text pieces into paragraphs
			// Fall back to ripping out the text pieces
			ret = new String[1];
			ret[0] = getWordTextFromPieces(doc);
		}
		
		return ret;
	}
	
	/**
	 * Grab the text out of the text pieces. Might also include various
	 *  bits of crud, but will work in cases where the text piece -> paragraph
	 *  mapping is broken. Fast too.
	 */
	public static String getWordTextFromPieces(HWPFDocument doc) {
    	StringBuilder textBuf = new StringBuilder();
    	
    	Iterator textPieces = doc.getTextTable().getTextPieces().iterator();
    	while (textPieces.hasNext()) {
    		TextPiece piece = (TextPiece) textPieces.next();

    		String encoding = "Cp1252";
    		if (piece.isUnicode()) {
    			encoding = "UTF-16LE";
    		}
    		try {
    			String text = new String(piece.getRawBytes(), encoding);
    			textBuf.append(text);
    		} catch(UnsupportedEncodingException e) {
    			throw new InternalError("Standard Encoding " + encoding + " not found, JVM broken");
    		}
    	}
    	
    	String text = textBuf.toString();
    	
    	// Fix line endings (Note - won't get all of them
    	text = text.replaceAll("\r\r\r", "\r\n\r\n\r\n");
    	text = text.replaceAll("\r\r", "\r\n\r\n");
    	
    	if(text.endsWith("\r")) {
    		text += "\n";
    	}
    	
    	return text;
	}
	
	/**
	 * Grab the text, based on the paragraphs. Shouldn't include any crud,
	 *  but slightly slower than getTextFromPieces().
	 */
	public static String getWordTextOld(HWPFDocument doc) {
		StringBuilder ret = new StringBuilder();
		String[] text = getWordParagraphText(doc);
		for(int i=0; i<text.length; i++) {
			ret.append(text[i]);
		}
		return ret.toString();
	}




	public static String getWordText(InputStream is) {
        try {
            WordExtractor extractor = new WordExtractor(is);
            return extractor.getText();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
	}
    
    
    // ---------------------------------------------------------- PowerPoint ---
    
    
    public static String getPowerPointText(InputStream is) {
        try {
            PowerPointExtractor extractor = new PowerPointExtractor(is);
            return extractor.getText(true, true);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    


    // ----------------------------------------------------------------- Any ---


    
    public static String getText(URL url) {
        try {
            return getText(url.openStream());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static String getDocxText(File file) {
        try {
            XWPFDocument docx = new XWPFDocument(new FileInputStream(file));
            XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
            String text = extractor.getText();
            return text;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    

    public static String getText(InputStream is) {
        try {
            POITextExtractor extractor = ExtractorFactory.createExtractor(is);
            return extractor.getText();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String getText(File f) {
        try {
            POITextExtractor extractor = ExtractorFactory.createExtractor(f);
            return extractor.getText();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
