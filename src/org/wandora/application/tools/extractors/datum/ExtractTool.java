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
 * ExtractTool.java
 *
 * Created on 24. marraskuuta 2004, 16:19
 */

package org.wandora.application.tools.extractors.datum;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.piccolo.*;

import java.util.*;


/**
 *
 * @author  olli, akivela
 */


public class ExtractTool extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public boolean guiless = false;
    
    protected DataSource dataSource;
    protected DatumExtractor datumExtractor;
    protected DatumProcessor datumProcessor;
    
    protected ProgressMonitor progressMonitor;
    
    protected ExtractToolDialog toolDialog;
    
    protected Map/*<ConfigurableDataSource,DatumExtractor>*/ sourceExtractorMap;
    
    protected ExtractProgressDialog progressDialog;
    
    protected Wandora wandoraAdmin;
    
    protected boolean abort;
    
    protected boolean error;
    
    protected boolean extracting;
    
    /** Creates a new instance of ExtractTool */
    public ExtractTool() {
    }
    
    public ExtractTool(Map/*<ConfigurableDataSource,DatumExtractor>*/ sourceExtractorMap, DatumProcessor processor) {
        this.sourceExtractorMap=sourceExtractorMap;
        setDatumProcessor(processor);
    }
    
    protected void updateProgress(double d){
        if(progressDialog!=null && extracting && d>0) d=d*0.95; // if used from gui leave some progress bar space for merging
        if(progressMonitor!=null) progressMonitor.updateProgress(d);
        if(progressDialog!=null){
            progressDialog.setProgress(d);
        }
    }
    
    public void setProgressMonitor(ProgressMonitor progressMonitor){
        this.progressMonitor=progressMonitor;
    }
    
    public void setDataSource(DataSource dataSource){
        this.dataSource=dataSource;
    }
    public void setDatumExtractor(DatumExtractor datumExtractor){
        this.datumExtractor=datumExtractor;
    }
    public void setDatumProcessor(DatumProcessor datumProcessor){
        this.datumProcessor=datumProcessor;
    }
    
   
    
    
    public void guilessStart() {
        try {
            Logger logger = new SimpleLogger();
            if(dataSource == null) {
                logger.writelog("INF", "ExtractTool has no datasource defined! Exiting!");
                return;
            }
            if(datumExtractor == null) {
                logger.writelog("INF", "ExtractTool has no extractors defined! Exiting!");
                return;
            }
            if(datumProcessor == null) {
                logger.writelog("INF", "ExtractTool has no processors defined! Exiting!");
                return;
            }
            TopicMap tm=new org.wandora.topicmap.memory.TopicMapImpl();
            doExtract(tm, logger);
            logger.writelog("INF","Merging results");
            wandoraAdmin.getTopicMap().mergeIn(tm);
            logger.writelog("INF","Ready.");              
        }   
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    public void guiStart(DataSource dataSource){
        DatumExtractor de=(DatumExtractor)sourceExtractorMap.get(dataSource);
        setDataSource(dataSource);
        setDatumExtractor(de);
        toolDialog.setVisible(false);
        progressDialog=new ExtractProgressDialog(wandoraAdmin,true,this);
        final Logger logger=new Logger(){
            public void writelog(String str){
                progressDialog.addLine(str);
            }
            public void writelog(String lvl,String str){
//                if(!lvl.equalsIgnoreCase("DBG")) 
                    writelog("["+lvl+"] "+str);
            }
        };
        final TopicMap tm=new org.wandora.topicmap.memory.TopicMapImpl();
        error=false;
        Thread t=new Thread(){
            public void run(){
                try{
                    doExtract(tm,logger);
                }catch(Exception ee){
                    logger.writelog("ERR",ee);
                    error=true;
                }
            }
        };
        final Object thisf=this;
        Thread t2=new Thread(){
            @Override
            public void run(){
                synchronized(thisf){
                    while(extracting){
                        try{
                            thisf.wait();
                        }
                        catch(InterruptedException ie){
                            error=true;
                            logger.writelog("ERR","Interrupted");
                            break;
                        }
                    }
                    if(!error && !abort){
                        logger.writelog("INF","Merging results");
                        try {
                            wandoraAdmin.getTopicMap().mergeIn(tm);
                            logger.writelog("INF","Ready.");
                            updateProgress(1.0);
                        }
                        catch(TopicMapException tme){
                            tme.printStackTrace(); // TODO EXCEPTION
                            error=true;
                        }
                    }
                    progressDialog.processEnded();
                }
            }
        };
        t.start();

        extracting=true;
        t2.start();
        progressDialog.setVisible(true);        
                
    }
    
    public void guiCancel(){
        toolDialog.setVisible(false);
    }
    
    public void abortExtraction(){
        abort=true;
    }
    
    

    
    
    public void doExtract(TopicMap tm,Logger logger) throws ExtractionException {
        try{
            abort=false;
            extracting=true;
            logger.writelog("INF","Extraction started");
            updateProgress(0);
            DataStructure data=null;
            while(!abort) {
                double p1=dataSource.getProgress();
                updateProgress(p1);
                logger.writelog("DBG","Getting next source");
                data=dataSource.next(logger);
                if(data==null) break;
                double p2=dataSource.getProgress();
                Map datum=null;
                while(!abort) {
                    logger.writelog("DBG","Getting next datum");
                    datum=datumExtractor.next(data,logger);
                    if(datum==null) break;
                    double p3=datumExtractor.getProgress();
                    if(p3>=0.0) updateProgress(p1+(p2-p1)*p3);
                    else updateProgress(p1);

                    /*   // DEBUGGING
                    StringBuffer dbgString=new StringBuffer();
                    Iterator iter=datum.entrySet().iterator();
                    while(iter.hasNext()){
                        Map.Entry e=(Map.Entry)iter.next();
                        dbgString.append(e.getKey().toString()+"=>");
                        Object o=e.getValue();
                        if(o instanceof Collection){
                            dbgString.append("{");
                            Iterator iter2=((Collection)o).iterator();
                            while(iter2.hasNext()){
                                dbgString.append(iter2.next().toString()+(iter2.hasNext()?", ":"") );
                            }
                            dbgString.append("}");
                        }
                        else{
                            dbgString.append(o.toString());
                        }
                        dbgString.append("; ");
                    }
                    logger.writelog("DBG","Got datum: "+dbgString);
                    
                    */
                    logger.writelog("DBG","Processing datum");
                    datumProcessor.processDatum(datum, tm, logger);
                }
            }
            if(!abort) {
                updateProgress(1.0);
                logger.writelog("INF","Extraction finished");
            }
            else {
                logger.writelog("INF","Extraction aborted");
            }
        }catch(Exception e) {
            error=true;
            throw new ExtractionException("Uncaught exception",e);
        }
        finally{
            synchronized(this){
                extracting=false;
                this.notifyAll();
            }
        }
    }
    
    public void execute(Wandora admin, Context context) {
        this.wandoraAdmin=admin;
        ConfigurableDataSource[] possibleSources=new ConfigurableDataSource[sourceExtractorMap.size()];
        Iterator iter=sourceExtractorMap.keySet().iterator();
        int ptr=0;
        while(iter.hasNext()){
            possibleSources[ptr]=(ConfigurableDataSource)iter.next();
            possibleSources[ptr].setExtractTool(this);
            ptr++;
        }
        if(guiless) {
            guilessStart();
        }
        else {
            toolDialog=new ExtractToolDialog(admin,true,possibleSources);
            toolDialog.setVisible(true);
        }
    }
    
    public Wandora getWandora(){
        return wandoraAdmin;
    }
    
    @Override
    public String getName() {
        return "Extract Tool";
    }
    
}
