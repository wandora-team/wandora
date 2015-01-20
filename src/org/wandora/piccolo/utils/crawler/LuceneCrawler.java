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
 *
 * 
 *
 * LuceneCrawler.java
 *
 * Created on January 10, 2002, 12:39 PM
 */

package org.wandora.piccolo.utils.crawler;


import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import java.io.*;
import java.util.*;



/**
 *
 * @author  olli
 */
public class LuceneCrawler extends Object implements CrawlerAccess {

    private HashMap properties = new HashMap();
    CrawlerAccess crawler;
    IndexWriter writer;
    
    /** Creates new LuceneCrawler */
    public LuceneCrawler(CrawlerAccess crawler,IndexWriter writer) {
        this.crawler=crawler;
        this.writer=writer;
    }

    public void add(Object crawlerObject,int depth) {
        crawler.add(crawlerObject,depth);
    }
    
    
    public void addObject(Object data) {
        if(data instanceof Document){
            try{
                writer.addDocument((Document)data);
            }catch(IOException e){e.printStackTrace();}
        }
        else{
            crawler.addObject(data);
        }
    }
    
    
    public static Field keywords(String data){
        if(data==null) data="";
        return new Field("keywords",data,Field.Store.YES,Field.Index.TOKENIZED);        
    }
    public static Field title(String data){
        if(data==null) data="";
        return new Field("title",data,Field.Store.YES,Field.Index.TOKENIZED);        
    }
    public static Field subject(String data){
        if(data==null) data="";
        return new Field("subject",data,Field.Store.YES,Field.Index.TOKENIZED);        
    }
    public static Field content(String data){
        if(data==null) data="";
        return new Field("content",data,Field.Store.NO,Field.Index.TOKENIZED);        
    }
    public static Field location(String data){
        if(data==null) data="";
        return new Field("location",data,Field.Store.YES,Field.Index.NO);        
    }
    
    
    public void forceExit() {
        
    }
    
    

    
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    
}
