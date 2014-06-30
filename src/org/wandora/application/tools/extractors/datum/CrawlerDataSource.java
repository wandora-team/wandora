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
 * URLDataSource.java
 *
 * Created on 24. marraskuuta 2004, 17:31
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.application.tools.extractors.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
/**
 *
 * DataSource that can be used to process a predetermined list of URLs or a list of urls
 * that can grow as it is processed. To support such crawling, you must provide the
 * crawler thread that is responsible for adding urls to the list. Call setCrawler to
 * set the crawler thread and start the thread before you start using this DataSource.
 * The next method will block while the list is empty but crawler thread is still alive.
 * When the crawler dies (or if it is null to begin with) and list becomes empty, next will
 * return null. You may want to override makeDataStructure method.
 *
 * @author  olli
 */
public class CrawlerDataSource implements ConfigurableDataSource {
    
    protected int processed=0;
    
    protected LinkedList queue;
    
    protected Thread crawlerThread;
    
    protected ExtractTool extractTool;
    
//    protected JPanel configurationPanel;
    
    /** Creates a new instance of URLDataSource */
    public CrawlerDataSource() {
        queue=new LinkedList();
    }
    
    public CrawlerDataSource(Collection urls) {
        queue=new LinkedList(urls);
    }
    
    public double getProgress() {
        int total=queue.size()+processed;
        return (double)processed/(double)total;
    }
    
    public void addToQueue(Object o){
        synchronized(this){
            queue.add(o);
            this.notifyAll();
        }
    }
    
    public void setCrawler(Thread crawlerThread){
        this.crawlerThread=crawlerThread;
    }
    
    protected DataStructure makeDataStructure(Object o){
        return new DataStructure(o,null,null);
    }
    
    public DataStructure next(org.wandora.piccolo.Logger logger) throws ExtractionException {
        Object o=null;
        synchronized(this){
            if(queue.isEmpty()){
                while(crawlerThread!=null && crawlerThread.isAlive() && queue.isEmpty()){
                    try{
                        this.wait(1000);
                    }catch(InterruptedException ie){}
                }
            }
            if(!queue.isEmpty()) o=queue.removeFirst();
        }
        if(o==null) return null;
        processed++;
        return makeDataStructure(o);
    }
    
    public javax.swing.JPanel getConfigurationPanel() {
//        if(configurationPanel==null){
            JPanel configurationPanel=new CrawlerConfigurationPanel(this);
//        }
        return configurationPanel;
    }
    
    public void guiCancel(){
        extractTool.guiCancel();
    }
    
    public void guiStart(){
        extractTool.guiStart(this);
    }
    
    public void setExtractTool(ExtractTool extractTool) {
        this.extractTool=extractTool;
    }
    
    public String getSourceName() {
        return "File/URL";
    }
    
}
