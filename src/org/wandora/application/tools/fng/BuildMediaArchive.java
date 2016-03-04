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
 * BuildMediaArchive.java
 *
 * Created on July 26, 2004, 10:40 AM
 */

package org.wandora.application.tools.fng;


import org.wandora.utils.EasyHash;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.SimpleLogger;
import org.wandora.topicmap.TopicMap;
import org.wandora.piccolo.WandoraManager;
import org.wandora.topicmap.Topic;
import org.wandora.application.tools.DeleteTopicsWithoutBasename;
import org.wandora.application.tools.extractors.datum.CrawlerDataSource;
import org.wandora.application.tools.extractors.datum.ExtractTool;
import org.wandora.application.tools.extractors.datum.FilteringDatumExtractor;
import org.wandora.application.tools.extractors.datum.FirstDatumProcessor;
import org.w3c.dom.*;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.piccolo.*;
import org.wandora.utils.*;

import org.wandora.application.tools.fng.*;
import org.wandora.application.tools.extractors.*;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import bsh.*;


/**
 *
 * @author  akivela
 */
public class BuildMediaArchive extends Thread {
    
    private Logger logger;

    private Interpreter interpreter;
    private long delay;
    private WandoraManager manager;
    private TopicMap mediaarchiveMap;
    
    private boolean updateNow=false;;
    
    /** Creates a new instance of ElaineParser */
    public BuildMediaArchive(){
        this(new SimpleLogger());
        interpreter = new Interpreter();
    }
    public BuildMediaArchive(Logger logger) {
        this.logger=logger;
    }
    
        

    
    public boolean build(Vector urls, TopicMap tm, TopicMap filteredTm) throws IOException {
        try {
            if(urls.size() == 0) {
                logger.writelog("INF", "No source urls defined! Exiting!");
                return false;
            }
            else {
                for(int i=0; i<urls.size(); i++) {
                    logger.writelog("INF", "Source defined " + urls.elementAt(i));
                }
            }
            
            interpreter.source("resources/conf/extract_configs/extractconfig_mediaarchive.bsh");
                       
            CrawlerDataSource crawlerDataSources = new CrawlerDataSource(urls);        
            
            Map dataSources = new EasyHash(new Object[]{
                crawlerDataSources, (FilteringDatumExtractor) interpreter.get("de"),
            });
   
            ExtractTool tool=new ExtractTool(dataSources,(FirstDatumProcessor) interpreter.get("dp"));
            tool.setDataSource(crawlerDataSources);
            tool.setDatumExtractor((FilteringDatumExtractor) interpreter.get("de"));
            tool.guiless = true;
            tool.doExtract(tm,logger);
            
            try {
                Vector deletedTopics = new Vector();
                Topic t = null;
                String basename = null;
                for(Iterator iter=tm.getTopics(); iter.hasNext();) {
                    t = (Topic) iter.next();
                    if(!t.isRemoved()) {
                        basename = t.getBaseName();
                        if(basename == null || basename.length() == 0) {
                            deletedTopics.add(t);
                        }
                    }
                }
                for(int i=deletedTopics.size()-1; i>=0; i--) {
                    try {
                        t = (Topic) deletedTopics.elementAt(i);
                        t.remove();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            // Build mediateekki topicmap
            filteredTm.mergeIn(tm);
            
            try {
                MediateekkiFilter filter = new MediateekkiFilter();
                filter.quiet = true;
                filter.execute(filteredTm, null);

                try {
                    Vector deletedTopics = new Vector();
                    Topic t = null;
                    String basename = null;
                    for(Iterator iter=filteredTm.getTopics(); iter.hasNext();) {
                        t = (Topic) iter.next();
                        if(!t.isRemoved()) {
                            if(t.getAssociations().size() == 0) {
                                deletedTopics.add(t);
                            }
                        }
                    }
                    for(int i=deletedTopics.size()-1; i>=0; i--) {
                        try {
                            t = (Topic) deletedTopics.elementAt(i);
                            t.remove();
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return true;
        } catch(Exception e) {
            logger.writelog("WRN","Exception occurred while building media archive's database", e);
            return false;
        }
    }
    
    
    
    
    public static void main(String[] args) throws Exception {
        BuildMediaArchive builder=new BuildMediaArchive();
        TopicMap mediaArchiveTopicMap=new org.wandora.topicmap.memory.TopicMapImpl();
        TopicMap mediateekkiTopicMap=new org.wandora.topicmap.memory.TopicMapImpl();
        
        Vector urls = new Vector();
        for(int i=0; i<args.length-2; i++) {
            try {
                urls.add(new URL(args[i]));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if(builder.build(urls, mediaArchiveTopicMap, mediateekkiTopicMap)) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(args[args.length-2]));
                mediaArchiveTopicMap.exportXTM(out);
                out = new FileOutputStream(new File(args[args.length-1]));
                mediateekkiTopicMap.exportXTM(out);
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
