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
 * 
 *
 * RTFHandler.java
 *
 * Created on January 11, 2002, 11:37 AM
 */

package org.wandora.piccolo.utils.crawler.handlers;
import java.io.*;
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import java.util.*;
import java.net.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import org.wandora.piccolo.utils.crawler.*;
/**
 *
 * @author  olli
 */
public class RTFHandler extends Object implements Handler {

   
    
    public void handle(CrawlerAccess crawler,InputStream in,int depth,URL page) {
        try{
            String content=RTF2PlainText(in);
            org.apache.lucene.document.Document doc=new org.apache.lucene.document.Document();
            doc.add(LuceneCrawler.keywords(""));
            doc.add(LuceneCrawler.title(""));
            doc.add(LuceneCrawler.subject(""));
            doc.add(LuceneCrawler.location(page.toString()));
            doc.add(LuceneCrawler.content(content));
            crawler.addObject(doc);
        } catch(IOException e){}
    }
    
    
    
    public static final String[] contentTypes=new String[] {"text/rtf"};
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public static String RTF2PlainText(InputStream in) throws IOException{
        StyledDocument doc=new DefaultStyledDocument();
        RTFEditorKit kit=new RTFEditorKit();
        try{
            kit.read(in,doc,0);
            return doc.getText(0,doc.getLength());
        }
        catch(BadLocationException e){e.printStackTrace();return null;}
    }

    
    
}
