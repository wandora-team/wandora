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
 * HTMLHandler.java
 *
 * Created on December 5, 2001, 4:26 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;


import javax.swing.text.html.parser.*;
import javax.swing.text.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;

import org.wandora.utils.*;
import org.wandora.piccolo.utils.crawler.*;
/**
 *
 * @author  olli
 */
public class HTMLHandler extends Object implements Handler {

    private boolean outputLucene;
    private String[] linkTypes=new String[0];
    
    
    
    /** Creates new CrawlerParser */
    public HTMLHandler() {
        outputLucene=false;
    }

    public HTMLHandler(String[] linkTypes) {
        outputLucene=false;
        this.linkTypes=linkTypes;
    }
    
    public void setOutputLucene(boolean value) {
        outputLucene=value;
    }

    
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL page) {
        if(depth > 0) {
            HTMLParser p=new HTMLParser(linkTypes);
            try {
                p.parse(page, new InputStreamReader(in));
            } 
            catch(IOException e) { return; }
            
            URL[] newUrls = p.getNewURLs();
            if(newUrls != null && newUrls.length > 0) {
                for(int i=0; i<newUrls.length; i++) {
                    crawler.add(newUrls[i], depth-1);
                }
            }

            if(outputLucene){
                org.apache.lucene.document.Document d=new org.apache.lucene.document.Document();
                d.add(LuceneCrawler.keywords(""));
                d.add(LuceneCrawler.location(page.toString()));
                d.add(LuceneCrawler.subject(""));
                d.add(LuceneCrawler.title(p.getTitle()));
                d.add(LuceneCrawler.content(p.getContent()));
                crawler.addObject(d);
            }
    //        HashMap o=p.getOccurances();
    //        for(int i=0;i<o.length;i++){crawler.addOccurance(o[i]);}
        }
    }
    
    
    
    public static final String[] contentTypes=new String[] {"text/html"};
    public String[] getContentTypes() {
        return contentTypes;
    }
}
