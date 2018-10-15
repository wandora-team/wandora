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
 * AbstractExtractor.java
 *
 * Created on November 10, 2004, 7:44 PM
 */

package org.wandora.application.tools.extractors;



import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.DropExtractor;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.utils.crawler.handlers.*;
import org.wandora.piccolo.utils.crawler.interrupthandlers.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.util.regex.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.utils.XMLbox;



/**
 * @author  akivela
 */
public abstract class AbstractExtractor extends AbstractWandoraTool implements WandoraTool, Runnable, Handler, InterruptHandler, DropExtractor, BrowserPluginExtractor {
    

	private static final long serialVersionUID = 1L;

	public final static String STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE = "String extractor not supported";

    // Extractor types...
    public final static int CUSTOM_EXTRACTOR = 1;
    public final static int RAW_EXTRACTOR = 2;
    public final static int FILE_EXTRACTOR = 4;
    public final static int URL_EXTRACTOR = 8;
    
    // Crawler Modes (used by url extractor)...
    public final static int EXACTLY_GIVEN_URLS = 0;
    public final static int GIVEN_URLS_AND_LINKED_DOCUMENTS = 1;
    public final static int GIVEN_URLS_AND_URL_BELOW = 2;
    public final static int GIVEN_URLS_AND_CRAWLED_DOCUMENTS_IN_URL_DOMAIN = 3;
    public final static int GIVEN_URLS_AND_ALL_CRAWLED_DOCUMENTS = 4;

    public final static int SELECT_DIALOG_TITLE = 1000;
    public final static int POINT_START_URL_TEXT = 1010;
    public final static int INFO_WAIT_WHILE_WORKING = 1020;
    public final static int FILE_PATTERN = 9000;
    public final static int DONE_FAILED = 6000;
    public final static int DONE_ONE = 6010;
    public final static int DONE_MANY = 6020;
    public final static int LOG_TITLE = 6100;
    
    private Wandora wandora = null;
    private TopicMap topicMap;

    private String[] forceUrls = null;
    private File[] forceFiles = null;
    private Object forceContent = null;

    private WebCrawler crawler = null;
    private int maximumCrawlCounter = 99999;
    
    private int extractionCounter = 0;
    private int foundCounter = 0;
    private int browseCounter = 0;
    
    private long errorNapTime = 500;
      
    private AbstractExtractorDialog extractorSourceDialog = null;

    private String masterSubject = null;
    
    
    
    /** Creates a new instance of AbstractExtractor */
    public AbstractExtractor() {
        this.wandora = Wandora.getWandora();
        if(wandora != null) this.topicMap = wandora.getTopicMap();
    }
    
    
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createExtractType();
    }
    @Override
    public String getName(){
        return "Abstract Extractor";
    }
    @Override
    public String getDescription(){
        return "AbstractExtractor is a base implementation for extractors.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract.png");
    }
    @Override
    public boolean runInOwnThread(){
        return true;
    }
    public boolean useTempTopicMap(){
        return false;
    }
    public boolean useURLCrawler() {
        return false;
    }


    // ------------------------------------------------------- DropExtractor ---
    
    
    public boolean instantDropHandle() {
        return false;
    }
    
    
    @Override
    public void dropExtract(File[] files) throws TopicMapException {
        if(instantDropHandle()) {
            this.wandora = Wandora.getWandora();
            topicMap = wandora.getTopicMap();
            handleFiles(files, topicMap);
            wandora.doRefresh();
        }
        else {
            this.forceFiles = files;
            this.execute(Wandora.getWandora());
        }
    }
    
    @Override
    public void dropExtract(String[] urls) throws TopicMapException {
        if(instantDropHandle()) {
            this.wandora = Wandora.getWandora();
            topicMap = wandora.getTopicMap();
            handleUrls(urls, topicMap);
            wandora.doRefresh();
        }
        else {
            this.forceUrls = urls;
            this.execute(Wandora.getWandora());
        }
    }
    
    @Override
    public void dropExtract(String content) throws TopicMapException {
        if(instantDropHandle()) {
            this.wandora = Wandora.getWandora();
            topicMap = wandora.getTopicMap();
            handleContent(content, topicMap);
            wandora.doRefresh();
        }
        else {
            this.forceContent = content;
            this.execute(Wandora.getWandora());
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public String getGUIText(int textType, Object[] params) {
        String guiText = getGUIText(textType);
        for(int i=0; i<params.length; i++) {
            guiText = guiText.replace("%"+i, params[i].toString());
        }
        return guiText;
    }
    
    
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select file(s) or directories!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking files with metadata!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions!";
            case DONE_ONE: return "Ready.";
            case DONE_MANY: return "Ready. Total %0 successful extractions!";
            
            case LOG_TITLE: return "Extraction Log";
        }
        System.out.println("Requesting Illegal extraction GUI text: " + textType);
        return "";
    }
    
    
    
    
    
    
    public void setForceFiles(File[] ffiles) {
        this.forceFiles = ffiles;
    }
    
    public void setForceUrls(String[] furls) {
        this.forceUrls = furls;
    }
    
    public void setForceContent(Object fcontent) {
        this.forceContent = fcontent;
    }
    
    public File[] getForceFiles() {
        return this.forceFiles;
    }
    
    public String[] getForceUrls() {
        return this.forceUrls;
    }
    
    public Object getForceContent() {
        return this.forceContent;
    }
    
    
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR | RAW_EXTRACTOR;
    }
    

    public void initializeCustomType() {
        // TO BE OVERLOADED IN USER WRITTEN EXTRACTORS
    }
   public void handleCustomType() {
        // TO BE OVERLOADED IN USER WRITTEN EXTRACTORS
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        setWandora(wandora);
        if(wandora != null) topicMap = wandora.getTopicMap();
        Object contextSource = context.getContextSource();
        if(contextSource instanceof OccurrenceTextEditor) {
            try {
                OccurrenceTextEditor occurrenceEditor = (OccurrenceTextEditor) contextSource;
                if(context.getContextObjects().hasNext()) {
                    Topic masterTopic = (Topic) context.getContextObjects().next();
                    setMasterSubject(masterTopic);
                    String str = occurrenceEditor.getSelectedText();
                    if(str == null || str.length() == 0) {
                        str = occurrenceEditor.getText();
                    }
                    _extractTopicsFrom(str, topicMap);
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        else {
            try {
                extractionCounter = 0;
                foundCounter = 0;
                browseCounter = 0;

                boolean handledForcedContent = handleForcedContent();
                
                if(!handledForcedContent) {
                    // --- ASK CONTENT FOR EXTRACTION ---
                    
                    int possibleTypes = getExtractorType();
                    extractorSourceDialog = new AbstractExtractorDialog(wandora, true);
                    extractorSourceDialog.initialize(this);
                    if((possibleTypes & FILE_EXTRACTOR) != 0) extractorSourceDialog.registerFileSource();
                    if((possibleTypes & URL_EXTRACTOR) != 0) extractorSourceDialog.registerUrlSource();
                    if((possibleTypes & RAW_EXTRACTOR) != 0) extractorSourceDialog.registerRawSource();
                    if((possibleTypes & CUSTOM_EXTRACTOR) != 0) initializeCustomType();
                    extractorSourceDialog.setVisible(true);
                    if(!extractorSourceDialog.wasAccepted()) return;

                    int selectedType = extractorSourceDialog.getSelectedSource();

                    setDefaultLogger();
                    log(getGUIText(INFO_WAIT_WHILE_WORKING));


                    // --- FILE TYPE ---
                    if((selectedType & FILE_EXTRACTOR) != 0) {
                        handleFiles( extractorSourceDialog.getFileSources(), topicMap );
                    }

                    // --- URL TYPE ---
                    if((selectedType & URL_EXTRACTOR) != 0) {
                        handleUrls( extractorSourceDialog.getURLSources(), topicMap );
                    }

                    // --- RAW TYPE ---
                    if((selectedType & RAW_EXTRACTOR) != 0) {
                        handleContent( extractorSourceDialog.getContent(), topicMap );
                    }

                    // --- CUSTOM TYPE ---
                    if((selectedType & CUSTOM_EXTRACTOR) != 0) {
                        handleCustomType();
                    }

                    lockLog(false);
                    Object[] params = new Object[] { ""+extractionCounter, +foundCounter, ""+browseCounter };
                    if(extractionCounter == 0) {
                        log(getGUIText(DONE_FAILED, params));
                    }
                    else if(extractionCounter == 1) {
                        log(getGUIText(DONE_ONE, params));
                    }
                    else {
                        log(getGUIText(DONE_MANY, params));
                    }

                    setState(WAIT);
                }
            }
            catch(Exception e) {
                log(e);
                setState(WAIT);
            }
        }
        clearMasterSubject();
    }
    

    public boolean handleForcedContent() {
        boolean handledForcedContent = false;
        if(forceUrls != null) {
            handleUrls(forceUrls, topicMap);
            handledForcedContent = true;
            forceUrls = null;
        }
        else if(forceFiles != null) {
            handleFiles(forceFiles, topicMap);
            handledForcedContent = true;
            forceFiles = null;
        }
        else if(forceContent != null){
            handleContent(forceContent, topicMap);
            handledForcedContent = true;
            forceContent = null;
        }
        return handledForcedContent;
    }
    
    
    
    
     public void handleFiles(File[] files, TopicMap tm) {
        if(files == null || files.length == 0) return;
        for(int i=0; i<files.length && !forceStop(); i++) {
            if(files[i] != null) {
                // FILES
                if(files[i].isFile()) {
                    try {
                        extractTopicsFrom(files[i], tm);
                    }
                    catch(Exception e) {
                        log(e);
                        log("Extraction from '" + croppedFilename(files[i].getName()) + "' failed.");
                        takeNap(errorNapTime);
                    }
                }
                // DIRECTORIES
                if(files[i].isDirectory()) {
                    try {
                        String absName = files[i].getPath();
                        extractTopicsFrom(absName, Pattern.compile(getGUIText(FILE_PATTERN)), 30, 900000);
                    }
                    catch (Exception e) {
                        log(e);
                        takeNap(errorNapTime);
                    }
                }
            }
        }
     }
     


     
     
     public void handleUrls(String[] urls, TopicMap tm) {
        if(urls == null || urls.length == 0) return;
        if(useURLCrawler()) {
            setupCrawler(urls); 
            if(crawler != null) {
                 Thread[] threads=new Thread[1];

                 long t1 = System.currentTimeMillis();		
                 for(int i=0;i<threads.length;i++){
                     threads[i]=new Thread(crawler,"WebCrawler thread "+(i+1));
                     threads[i].start();
                 }

                 boolean running=true;
                 while(running){
                     try {
                         Thread.sleep(1000);
                     } catch(Exception e) {}
                     if(forceStop()) {
                         crawler.forceExit();
                     }
                     running=false;
                     for(int i=0;i<threads.length;i++){
                         if(threads[i].isAlive()) {
                             running=true;
                             break;
                         }
                     }
                 }
                 log("Crawler browsed "+ crawler.getDonePages().size()+" documents.");
                 log("Crawler handled "+ crawler.getHandledDocumentCount()+" documents.");
             }
        }
        
        // HANDLE URLS WITHOUT CRAWLER!
        else {
            int counter = 0;
            for(int i=0; i<urls.length && !forceStop(); i++) {
                try {
                    extractTopicsFrom(new URL(urls[i]), tm);
                    counter++;
                }
                catch(FileNotFoundException fmfe) {
                    log("File not found exception occurred while reading '"+urls[i]+"'.");
                }
                catch(Exception e) {
                    log(e);
                }
            }
            if(counter > 1) {
                log("Total "+ counter + " URLs extracted.");
            }
            if(counter == 0) {
                log("No URLs extracted.");
            }
            else {
                //log("Ready.");
            }
        }
     }
     
     
     
     
     
     
     public void handleContent(Object content, TopicMap tm) {
         if(content instanceof String) {
            handleStringContent((String) content, tm);
         }
     }
     
     
     public void handleStringContent(String stringContent, TopicMap tm) {
        if(stringContent != null){
            try {
                extractTopicsFromText(stringContent, tm);
            }
            catch (Exception e) {
                log(e);
            }
        }
     }
     
     


    
    // -------------------------------------------------------------------------
    
     public void setTopicMap(TopicMap tm) {
         this.topicMap = tm;
     } 
     
     
     
     
     private void setupCrawler(String[] startUrls) {
         int crawlerMode = getCrawlerMode();
         crawler = new WebCrawler();
         switch(crawlerMode) {
             case EXACTLY_GIVEN_URLS: {
                crawler.setMask(new URLMask(".*", URLMask.TYPE_ALLOW));
                crawler.addHandler(this);
                crawler.addInterruptHandler(this);
                crawler.setVerbose(true);
                crawler.setCrawlCounter(startUrls.length);
                for(int i=0; i<startUrls.length; i++) {
                    try { crawler.add(new URL(startUrls[i]), 1); }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                break;
             }
             
             case GIVEN_URLS_AND_LINKED_DOCUMENTS: {
                crawler.setMask(new URLMask(".*", URLMask.TYPE_ALLOW));
                crawler.addHandler(this);
                crawler.addHandler(new HTMLHandler()); // This ensures link detection!
                crawler.addInterruptHandler(this);
                crawler.setVerbose(true);
                crawler.setCrawlCounter(maximumCrawlCounter);
                for(int i=0; i<startUrls.length; i++) {
                    try { crawler.add(new URL(startUrls[i]), 2); }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                break;
             }
             
             case GIVEN_URLS_AND_URL_BELOW: {
                crawler.addHandler(this);
                crawler.addHandler(new HTMLHandler()); // This ensures link detection!
                crawler.addInterruptHandler(this);
                crawler.setVerbose(true);
                crawler.setCrawlCounter(maximumCrawlCounter);
                
                ArrayList<String> maskURLs = new ArrayList<String>();
                ArrayList<Integer> maskTypes = new ArrayList<Integer>();
                
                for(int i=0; i<startUrls.length; i++) {
                    try { 
                        URL url = new URL(startUrls[i]);
                        String urlStr = url.toString();
                        urlStr = urlStr.replaceAll("\\.", "\\\\.");
                        urlStr = urlStr.replaceAll("\\:", "\\\\:");
                        urlStr = urlStr.replaceAll("\\/", "\\\\/");
                        urlStr = urlStr.replaceAll("\\?", "\\\\?");
                        urlStr = urlStr.replaceAll("\\&", "\\\\&");
                        maskURLs.add( urlStr+".*" );
                        maskTypes.add( URLMask.TYPE_ALLOW );
                        System.out.println(" ****** MASK = "+urlStr+".*");
                    }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                crawler.setMask(new URLMask( maskURLs, maskTypes ));
                
                for(int i=0; i<startUrls.length; i++) {
                    try { 
                        URL url = new URL(startUrls[i]);
                        crawler.add(url, maximumCrawlCounter);
                    }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                
                log("Crawling domain may take very long. Press Stop button to end crawl.");
                takeNap(1000);
                break;
             }
                 
                 
             
             case GIVEN_URLS_AND_CRAWLED_DOCUMENTS_IN_URL_DOMAIN: {
                crawler.addHandler(this);
                crawler.addHandler(new HTMLHandler()); // This ensures link detection!
                crawler.addInterruptHandler(this);
                crawler.setVerbose(true);
                crawler.setCrawlCounter(maximumCrawlCounter);
                
                ArrayList<String> maskURLs = new ArrayList<String>();
                ArrayList<Integer> maskTypes = new ArrayList<Integer>();
                
                for(int i=0; i<startUrls.length; i++) {
                    try { 
                        URL url = new URL(startUrls[i]);
                        String protocol = url.getProtocol();
                        String host = url.getHost().replaceAll("\\.", "\\\\.");
                        System.out.println(" ****** MASK = "+protocol+"\\:\\/\\/"+host+".*");
                        maskURLs.add( protocol+"\\:\\/\\/"+host+".*" );
                        maskTypes.add( URLMask.TYPE_ALLOW );
                    }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                crawler.setMask(new URLMask( maskURLs, maskTypes ));
                
                for(int i=0; i<startUrls.length; i++) {
                    try { 
                        URL url = new URL(startUrls[i]);
                        crawler.add(url, maximumCrawlCounter);
                    }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                
                log("Crawling domain may take very long. Press Stop button to end crawl.");
                takeNap(1000);
                break;
             }
             
             case GIVEN_URLS_AND_ALL_CRAWLED_DOCUMENTS: {
                crawler.setMask(new URLMask(".*", URLMask.TYPE_ALLOW));
                crawler.addHandler(this);
                crawler.addHandler(new HTMLHandler()); // This ensures link detection!
                crawler.addInterruptHandler(this);
                crawler.setVerbose(true);
                crawler.setCrawlCounter(maximumCrawlCounter);
                for(int i=0; i<startUrls.length; i++) {
                    try { crawler.add(new URL(startUrls[i]), maximumCrawlCounter); }
                    catch(Exception e) { log("Exception occurred while setting up url crawler!", e); }
                }
                log("Crawl may never end. Press Stop button to end crawl.");
                takeNap(1000);
                break;
             }
         }
     }
     
    

        
    public void setupCrawler(String startUrl) {
         setupCrawler(new String[] { startUrl } );
    }

     

    
    /*
     * Use this method very carefully! If you set the crawlerMode to anything
     * else than EXACTLY_GIVEN_URLS then crawler already adds HTML links
     * to the crawler. You don't have to do it with this. Use this method
     * only in special cases when the HTMLHandler doesn't work!
     */
    public void addCrawlerUrl(URL url, int depth) {
        if(crawler != null) {
            crawler.add(url, depth);
        }
    }

    
    
    public int getCrawlerMode() {
        if(extractorSourceDialog != null) {
            return extractorSourceDialog.getCrawlerMode(EXACTLY_GIVEN_URLS);
        }
        else {
            return EXACTLY_GIVEN_URLS;
        }
    }
    


    protected void takeNap(long napTime) {
        try { Thread.currentThread().sleep(napTime); }
        catch (Exception e) { }            
    }



    
    
    
    
    // ---------------------------------------------------------------------
    // --- extractTopicsFrom -----------------------------------------------
    // ---------------------------------------------------------------------


    public int extractTopicsFrom(String fileName, Pattern fileMask, int depth, int space) {
        return extractTopicsFrom(fileName, new ArrayList(), fileMask, depth, space);
    }
    public int extractTopicsFrom(String fileName, Collection visited, Pattern fileMask, int depth, int space) {
        browseCounter++;
        if(!forceStop()) {
            //System.out.println("depth: "+depth);
            if(depth >= 0) {
                if(space >= 0) {
                    if(!visited.contains(fileName)) {
                        visited.add(fileName);
                        File file = new File(fileName);
                        if (file.exists()) {
                            try {
                                if(fileMask == null) {
                                    extractTopicsFrom(file, topicMap);
                                }
                                else {
                                    Matcher m = fileMask.matcher(fileName);
                                    if(m.matches()) {
                                        extractTopicsFrom(file, topicMap);
                                        //log("matching file found: " + fileName);
                                    }
                                }
                            }
                            catch (Exception e) {
                                log("Extracting from '" + croppedFilename(file) + "' failed.", e);
                                takeNap(errorNapTime);
                            }
                            if(file.isDirectory()) {
                                try {
                                    //log("a dir found: " + fileName); 
                                    String[] directoryFiles = file.list();
                                    for(int i=0; i<directoryFiles.length; i++) {
                                        //log(" trying: " + File.separator + directoryFiles[i]);
                                        space = extractTopicsFrom(fileName + File.separator + directoryFiles[i], visited, fileMask, depth-1, space);
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else {
                    log("Maximum number of files exceeded! Rejecting next files!");
                }
            }
            else {
                log("Maximum browse depth exceeded!");
            }
        }
        return --space;
    }        




    public synchronized void extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        foundCounter++;
        setProgress(foundCounter);
        if(url != null) {
            log("Extracting from " + croppedUrlString(url));
            TopicMap tempMap=null;
            if(useTempTopicMap()) {
                tempMap=new org.wandora.topicmap.memory.TopicMapImpl();
            }
            else {
                tempMap=topicMap;
            }

            if(_extractTopicsFrom(url, tempMap)) {
                if(useTempTopicMap()) {
                    topicMap.mergeIn(tempMap);
                }
                extractionCounter++;
            }
            else {
                log("Found no valid metadata in " + croppedUrlString(url));
                try { Thread.currentThread().sleep(errorNapTime); }
                catch (Exception e) {}
            }
        }
    }
        
    
    public void extractTopicsFromText(String content, TopicMap topicMap) throws Exception {
        foundCounter++;
        setProgress(foundCounter);
        if(content != null) {
            // log("Extracting from given string.");
            TopicMap tempMap=null;
            if(useTempTopicMap()) {
                tempMap=new org.wandora.topicmap.memory.TopicMapImpl();
            }
            else {
                tempMap=topicMap;
            }

            if(_extractTopicsFrom(content, tempMap)) {
                if(useTempTopicMap()) {
                    topicMap.mergeIn(tempMap);
                }
                extractionCounter++;
            }
            else {
                log("Found no valid metadata in given string.");
                try { Thread.currentThread().sleep(errorNapTime); }
                catch (Exception e) {}
            }
        }
    }



    public void extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        foundCounter++;
        setProgress(foundCounter);
        if(file != null) {
            log("Extracting from " + croppedFilename(file));
            TopicMap tempMap=null;
            if(useTempTopicMap()) {
                tempMap=new org.wandora.topicmap.memory.TopicMapImpl();
            }
            else {
                tempMap=topicMap;
            }

            if(_extractTopicsFrom(file, tempMap)) {
                if(useTempTopicMap()) {
                    topicMap.mergeIn(tempMap);
                }
                extractionCounter++;
            }
            else {
                log("Found no valid metadata in " + croppedFilename(file));
                try { Thread.currentThread().sleep(errorNapTime); }
                catch (Exception e) {}
            }

        }
    }

    public Locator buildSL(File file) {
        return new Locator(file.toURI().toString());
//        String path = file.getAbsolutePath();
//        path = path.replaceAll("\\\\", "\\/");
//        return new Locator("file://" + path);
    }



    
    
    
    
    // =========================================================================
    // ================ SUBCLASS SHOULD OVERWRITE THESE METHODS! ===============
    // =========================================================================
    
    public abstract boolean _extractTopicsFrom(File f, TopicMap t) throws Exception;
    public abstract boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception;
    public abstract boolean _extractTopicsFrom(String str, TopicMap t) throws Exception;





    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------- TOPIC METHODS---
    // -------------------------------------------------------------------------
    
    
    protected static String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {}
        return str;
    }

    
    public Locator buildSI(String siend) {
        if(siend == null) siend = "" + System.currentTimeMillis() + Math.random()*999999;
        if(siend.startsWith("http:")) return new Locator(siend);
        if(siend.startsWith("file:")) return new Locator(siend);
        if(siend.startsWith("https:")) return new Locator(siend);
        if(siend.startsWith("ftp:")) return new Locator(siend);
        if(siend.startsWith("ftps:")) return new Locator(siend);
        if(siend.startsWith("mailto:")) return new Locator(siend);
        if(siend.startsWith("/")) siend = siend.substring(1);
        return new Locator("http://wandora.org/si/default/" + urlEncode(siend));
    }

    public Topic createTopic(TopicMap topicMap, String baseString) throws TopicMapException {
        return createTopic(topicMap, baseString, "", baseString, new Topic[] { });
    }
    
    public Topic createTopic(TopicMap topicMap, String siString, String baseString) throws TopicMapException {
        return createTopic(topicMap, siString, "", baseString, new Topic[] { });
    }
    
    public Topic createTopic(TopicMap topicMap, String siString, String baseString, Topic type) throws TopicMapException {
        return createTopic(topicMap, siString, "", baseString, new Topic[] { type });
    }
    
    public Topic createTopic(TopicMap topicMap, String baseString, Topic type)  throws TopicMapException {
        return createTopic(topicMap, baseString, "", baseString, new Topic[] { type });
    }

    public Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString)  throws TopicMapException {
        return createTopic(topicMap, siString, baseNameString, baseString, new Topic[] { });
    }


    public Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic type)  throws TopicMapException {
        return createTopic(topicMap, siString, baseNameString, baseString, new Topic[] { type });
    }


    public Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic[] types)  throws TopicMapException {
        if(baseString != null && baseString.length() > 0) {
            Locator si = buildSI(siString);
            Topic t = topicMap.getTopic(si);
            if(t == null) {
                t = topicMap.getTopicWithBaseName(baseString + baseNameString);
                if(t == null) {
                    t = topicMap.createTopic();
                    t.setBaseName(baseString + baseNameString);
                }
                t.addSubjectIdentifier(si);
            }

            //setDisplayName(t, "fi", baseString);
            setDisplayName(t, "en", baseString);
            for(int i=0; i<types.length; i++) {
                Topic typeTopic = types[i];
                if(typeTopic != null) {
                    if(!t.isOfType(typeTopic)) {
                        t.addType(typeTopic);
                    }
                }
            }
            return t;
        }
        System.out.println("Failed to create topic!");
        return null;
    }


    public Association createAssociation(TopicMap topicMap, Topic aType, Topic[] players)  throws TopicMapException {
        Association a = topicMap.createAssociation(aType);
        Topic player;
        Topic role;
        Collection playerTypes;
        for(int i=0; i<players.length; i++) {
            player = players[i];
            if(player != null && !player.isRemoved()) {
                playerTypes = player.getTypes();
                if(playerTypes.size() > 0) {
                    role = (Topic) playerTypes.iterator().next();
                    if(role != null && !role.isRemoved()) {
                        a.addPlayer(player, role);
                    }
                }
            }
        }
        return a;
    }
    
    
    public Association createAssociation(TopicMap topicMap, Topic aType, Topic[] players, Topic[] roles)  throws TopicMapException {
        Association a = null;
        if(aType != null) {
            a = topicMap.createAssociation(aType);
            Topic player;
            Topic role;
            for(int i=0; i<players.length; i++) {
                player = players[i];
                role = roles[i];
                if(player != null && role != null && !player.isRemoved() && !role.isRemoved()) {
                    a.addPlayer(player, role);
                }
            }
        }
        return a;
    }

    
    
    public void setDisplayName(Topic t, String lang, String name)  throws TopicMapException {
        if(t != null & lang != null && name != null) {
            String langsi=XTMPSI.getLang(lang);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(new Locator(langsi));
                try {
                    langT.setBaseName("Language " + lang.toUpperCase());
                }
                catch (Exception e) {
                    langT.setBaseName("Language " + langsi);
                }
            }
            String dispsi=XTMPSI.DISPLAY;
            Topic dispT=t.getTopicMap().getTopic(dispsi);
            if(dispT == null) {
                dispT = t.getTopicMap().createTopic();
                dispT.addSubjectIdentifier(new Locator(dispsi));
                dispT.setBaseName("Scope Display");
            }
            HashSet scope=new HashSet();
            if(langT!=null) scope.add(langT);
            if(dispT!=null) scope.add(dispT);
            t.setVariant(scope, name);
        }
    }
    
    
    public void setData(Topic t, Topic type, String lang, String text) throws TopicMapException {
        if(t != null & type != null && lang != null && text != null) {
            String langsi=XTMPSI.getLang(lang);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(new Locator(langsi));
                try {
                    langT.setBaseName("Language " + lang.toUpperCase());
                }
                catch (Exception e) {
                    langT.setBaseName("Language " + langsi);
                }
            }
            t.setData(type, langT, text);
        }
    }
    
    
    public void makeSubclassOfWandoraClass(Topic t, TopicMap tm) throws TopicMapException {
        if(tm != null && t != null) {
            Topic wandoraClass = tm.getTopic(TMBox.WANDORACLASS_SI);
            Topic superclassSubclass = tm.getTopic(XTMPSI.SUPERCLASS_SUBCLASS);
            Topic superclass = tm.getTopic(XTMPSI.SUPERCLASS);
            Topic subclass = tm.getTopic(XTMPSI.SUBCLASS);
            if(wandoraClass != null &&
                    superclassSubclass != null &&
                    superclass != null &&
                    subclass != null) {
                
                Association a = tm.createAssociation(superclassSubclass);
                a.addPlayer(t, subclass);
                a.addPlayer(wandoraClass, superclass);
            }
        }
    }
    
    

    // -------------------------------------------------------------------------
    
    

    protected String croppedFilename(String filename) {
        if(filename != null) {
            if(filename.length() > 70) {
                filename = filename.substring(0,45) + "..." + filename.substring(filename.length()-22);
            }
            return filename;
        }
        return "";
    }
    
    protected String croppedFilename(File file) {
        if(file != null) { return croppedFilename(file.getPath()); }
        return "";
    }
    
    protected String croppedUrlString(URL url) {
        return croppedUrlString(url.toExternalForm());
    }
    
    protected String croppedUrlString(String urlString) {
        return (urlString.length() > 70 ? urlString.substring(0,45) + "..." + urlString.substring(urlString.length()-22): urlString);
    }

    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------


    @Override
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL url) {
        try {
            if(!forceStop()) {
                extractTopicsFrom(url, topicMap);
            }
            else {
                crawler.forceExit();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }



    private final String[] contentTypes=new String[] { };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------


    @Override
    public void handleInterrupt(CrawlerAccess crawler, int interrupt, URL url) {
        try {
            if(!forceStop()) {
                switch(interrupt) {
                    case WebCrawler.HTTP_UNAUTHORIZED_INTERRUPTION: {
                        try {
                            Wandora app = getWandora();
                            app.wandoraHttpAuthorizer.getAuthorizedAccess(url);
                            String authUser = app.wandoraHttpAuthorizer.getAuthorizedUserFor(url);
                            String authPass = app.wandoraHttpAuthorizer.getAuthorizedPasswordFor(url);
                            if(authUser != null && authPass != null) {
                                crawler.setProperty("httpUser", authUser);
                                crawler.setProperty("httpPassword", authPass);
                            }
                        }
                        catch(Exception ex) {

                        }
                        break;
                    }
                    default: {
                        log("Unrecognized interruption (" + interrupt + ") raised in web crawler!");
                        break;
                    }
                }
            }
            else {
                crawler.forceExit();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int[] getInterruptsHandled() {
        return new int[] { WebCrawler.HTTP_UNAUTHORIZED_INTERRUPTION };
    }







    // ---------------------------------------------------- PLUGIN EXTRACTOR ---


    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            setWandora(wandora);
            String content = ExtractHelper.getContent(request);
            if(content != null) {
                if(browserExtractorConsumesPlainText()) {
                    String tidyContent = XMLbox.cleanUp( content );
                    if(tidyContent != null && tidyContent.length() > 0) {
                        content = XMLbox.getAsText(tidyContent, "ISO-8859-1");
                    }
                }
                
                System.out.println("--- browser plugin processing content ---");
                System.out.println(content);
                _extractTopicsFrom(content, wandora.getTopicMap());
                wandora.doRefresh();
                return null;
            }
            else {
                return BrowserPluginExtractor.RETURN_ERROR+"Couldn't solve browser extractor content. Nothing extracted.";
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
    }


    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        return true;
    }

    @Override
    public String getBrowserExtractorName() {
        return getName();
    }

    public boolean browserExtractorConsumesPlainText() {
        return false;
    }

    
    // -------------------------------------------------------------------------


    public String getMasterSubject() {
        return masterSubject;
    }

    public void setMasterSubject(Topic t) {
        try {
            masterSubject = t.getOneSubjectIdentifier().toExternalForm();
        }
        catch(Exception e) {
            masterSubject = null;
        }
    }

    public void setMasterSubject(String subject) {
        masterSubject = subject;
    }

    public void clearMasterSubject() {
        masterSubject = null;
    }



    // -------------------------------------------------------------------------



    public void setWandora(Wandora app) {
        wandora = app;
    }

    public Wandora getWandora() {
        if(wandora != null) return wandora;
        else return Wandora.getWandora();
    }

}
