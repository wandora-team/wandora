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
 * FindSubjectLocator.java
 *
 * Created on 30. toukokuuta 2006, 11:30
 *
 */

package org.wandora.application.tools.subjects;

import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;

import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.utils.crawler.handlers.*;
import org.wandora.piccolo.utils.crawler.interrupthandlers.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;




/**
 * <p>
 * <code>FindSubjectLocator</code> crawls URL resources and tries to match each
 * found URL to the search pattern. If URL matches the search pattern topic is
 * given URL as the subject locator.
 * </p>
 * <p>
 * <code>FindSubjectLocator</code> is used to fix missing subject locators
 * for example.
 * </p>
 * <p>
 * This tool has NOT been tested.
 * </p>
 * 
 * @author akivela
 */

public class FindSubjectLocator extends AbstractWandoraTool implements WandoraTool,  Handler, InterruptHandler {

    private Wandora admin = null;
    private WebCrawler crawler = null;
    private int crawlCounter = 9000;
    
    protected int extractionCounter = 0;
    protected int foundCounter = 0;
    protected int browseCounter = 0;
    
    private String urlPattern = null;
    private String startUrl = null;
    private String subjectLocatorString = null;
    
    private HashMap<String,String> topicPatterns;
    
    
    /** Creates a new instance of FindSubjectLocator */
    public FindSubjectLocator() {
    }
    
    
    public FindSubjectLocator(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "Find subject locator";
    }
    @Override
    public String getDescription() {
        return "Looks for a real URL resource with given pattern and sets found URL as a subject locator of topic.";
    }
    
  
    
    /**
     * <code>solveStartURL</code> returns the URL where crawling is 
     * started.
    */
    public String solveStartURL() {
        return WandoraOptionPane.showInputDialog(admin,"Where should I start looking for subject locators?");
    }
    
    /**
     * <code>solveURLPattern</code> returns pattern that is compared to each
     * URL.
     */
    public String solveURLPattern(Topic topic) {
        try {
            return WandoraOptionPane.showInputDialog(admin,"Url pattern to look for '"+ getTopicName(topic) +"'?");
        }
        catch(Exception e) {}
        return null;
    }
    
    
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        this.admin = admin;
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        try {
            startUrl = solveStartURL();
            if(startUrl == null || startUrl.length() == 0) return;

            Topic topic = null;
            topicPatterns = new HashMap();
            while(topics.hasNext()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        // --- Solve URL pattern ---
                        urlPattern = solveURLPattern(topic);
                        if(urlPattern == null || urlPattern.length() == 0) continue;
                        topicPatterns.put(topic.getOneSubjectIdentifier().toExternalForm(), urlPattern);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            // --- open info dialog ---
            setDefaultLogger();
            setLogTitle("Find subject locator");
            log("Find subject locator...");

            // --- Fork crawler thread ---
            setupCrawler(startUrl);
            Thread[] threads=new Thread[1];

            long t1 = System.currentTimeMillis();		
            for(int i=0;i<threads.length;i++){
                threads[i]=new Thread(crawler,"WebCrawler thread "+(i+1));
                threads[i].start();
            }

            // --- Loop until crawler thread finishes ---
            boolean running=true;
            while(running) {
                try {
                    Thread.sleep(1000);
                }
                catch(Exception e) {}
                if(forceStop() && running) {
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
            browseCounter = crawler.getDonePages().size();
        }
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    
    

    
    
    // -------------------------------------------------------------------------
    
    
    
            
     public void setupCrawler(String startUrl) {
        if(startUrl != null && startUrl.length() > 0) {
            try {
                crawler = new WebCrawler();
                String urlMaskPrefix = startUrl;
                crawler.setMask(new URLMask(startUrl+".*", URLMask.TYPE_ALLOW));
                crawler.add(new URL(startUrl), crawlCounter);
                crawler.addHandler(this);
                crawler.addHandler(new HTMLHandler());
                crawler.addInterruptHandler(this);
                crawler.setVerbose(true);
                crawler.setCrawlCounter(crawlCounter);
            }
            catch (Exception e) {
                //info.setMessage("Exception occurrend\n" + e.toString());
                e.printStackTrace();
            }
        }
    }
    
     
     
     public Topic isMyURL(URL url) {
         log("Checking url " + url.toExternalForm() );
         String removedSI = null;
         Topic topic = null;
         String urlPattern = null;
         Pattern pattern = null;
         Matcher matcher = null;
         for(String si : topicPatterns.keySet()) {
             urlPattern = (String) topicPatterns.get(si);
             if(urlPattern != null) {
                int flags = Pattern.CASE_INSENSITIVE;
                pattern = Pattern.compile(urlPattern, flags);
                matcher = pattern.matcher(url.toExternalForm());
                if(matcher.find()) {
                    try {
                        removedSI = si;
                        topic = admin.getTopicMap().getTopic(si);
                        break;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
             }
         }
         if(topic != null) {
             topicPatterns.remove(removedSI);
         }
         return topic;
     }
     

    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------


     
     
    @Override
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL url) {
        try {
            if(!forceStop()) {
                foundCounter++;
                Topic topic = isMyURL(url);
                if(topic != null) {
                    log("Found suitable url\n" + url.toExternalForm() );
                    subjectLocatorString = url.toExternalForm();
                    
                    // --- If subject locator was found set topics SL ---
                    if(subjectLocatorString != null) {
                        log("Adding topic '"+getTopicName(topic)+"' new subject identifief:\n"+subjectLocatorString +"");
                        topic.setSubjectLocator(new Locator(subjectLocatorString));

                        if(topicPatterns.isEmpty()) {
                            crawler.forceExit();
                        }
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



    public final String[] contentTypes=new String[] { "image/jpeg", "image/jpg" };

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
                        admin.wandoraHttpAuthorizer.getAuthorizedAccess(url);
                        crawler.setProperty("httpUser", admin.wandoraHttpAuthorizer.getAuthorizedUserFor(url));
                        crawler.setProperty("httpPassword", admin.wandoraHttpAuthorizer.getAuthorizedPasswordFor(url));
                        break;
                    }
                    default: {
                        log("Unregocnized interruption (" + interrupt + ") raised in web crawler!");
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

}
