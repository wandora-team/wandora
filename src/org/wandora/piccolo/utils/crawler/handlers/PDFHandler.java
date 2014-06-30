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
 * PDFHandler.java
 *
 * Created on January 10, 2002, 12:46 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;


import org.wandora.piccolo.utils.crawler.*;

import java.io.*;
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import java.util.*;
import java.net.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;


/**
 *
 * @author  olli
 */
public class PDFHandler extends Object implements Handler {

    
    

    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL page) {
        try{
            Document d=new Document();
            
            PDDocument doc = PDDocument.load(page);
            PDDocumentInformation info = doc.getDocumentInformation();
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(doc);
            doc.close();

            d.add(LuceneCrawler.subject(info.getSubject()));
            d.add(LuceneCrawler.title(info.getTitle()));
            d.add(LuceneCrawler.keywords(info.getKeywords()));
            d.add(LuceneCrawler.content(content));            
            d.add(LuceneCrawler.location(page.toString()));

            crawler.addObject(d);
        }
        catch(IOException e){e.printStackTrace();}
        catch(Exception e){e.printStackTrace();}
    }
    
       
    
    public static final String[] contentTypes=new String[] {"application/pdf"};
    public String[] getContentTypes() {
        return contentTypes;
    }
    
}
