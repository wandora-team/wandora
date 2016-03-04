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
 * WebCrawler.java
 *
 * Created on November 30, 2001, 2:54 PM
 */

package org.wandora.piccolo.utils.crawler;


import org.wandora.utils.Base64;
import org.wandora.piccolo.utils.crawler.*;
import org.wandora.application.Wandora;
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.SimpleAttributeSet;
import java.io.*;

import org.apache.xml.serialize.*;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.wandora.piccolo.*;
import org.wandora.utils.*;
import org.wandora.piccolo.utils.crawler.handlers.Handler;
import org.wandora.piccolo.utils.crawler.interrupthandlers.InterruptHandler;
import org.wandora.application.*;
import org.wandora.utils.Tuples.T2;

/**
 * A generic class for crawling web pages (or possibly other objects/files too).
 * Must be setup by giving <code>ContentHandler</code> objects and a start page
 * (or several start pages) and an <code>URLMask</code>. Files are retrieved with URLs and the
 * content is then passed to one of the given <code>ContentHandler</code>s according to the
 * content-type of the <code>URL</code>. If no suitable <code>ContentHandler</code> is found, the file is
 * simply discarded.
 *
 * ContentHandlers may add other pages to the crawl queue according to the links they find in the content they
 * are processing. The WebCrawler will take care that the same page is not crawled more than once.
 *
 * WebCrawer implements Runnable and can be used with multiple Threads. Simply initialize the WebCraler object.
 * Then create as many Threads with the single WebCrawler instance as you want and start each with Thread.start().
 * Use Thread.isAlive() to tell if the thread is still running.
 * Using more than one thread can be more efficient because threads can download and parse pages simultaneously.
 *
 * @see ContentHandler
 * @see URLMask
 * @see CrawlerAccess
 *
 * @author  olli, akivela
 */


public class WebCrawler extends AbstractCrawler implements Runnable, CrawlerAccess, Crawler {

    public static final int HTTP_UNAUTHORIZED_INTERRUPTION = 1000;
    

    private HashSet donePages=new HashSet();    
    private LinkedList<T2<Object,Integer>> queue=new LinkedList();
    private int counter = 0;   
    private int processing = 0;
    private boolean checkDonePages;

    
    
    private HashMap pagesDone;
    private HashMap timeTaken;
    
    
    
    /** Creates new WebCrawler */
    public WebCrawler() {
        setCallBack(this);
        checkDonePages=true;
        pagesDone=new HashMap();
        timeTaken=new HashMap();
    }
    

    
    
    
    /**
     * Clears the crawl queue.
     */
    public void clearQueue(){
        queue.clear();
    }
    
    
    // -------------------------------------------------------------------------
    
    
    /**
     * Starts crawling by first adding the given page to the queue. Uses a depth
     * of 5.
     */
    public void crawl(Object crawlObject){
        crawl(crawlObject, 5);
    }
    

    /**
     * Starts crawling by first adding the given page to the queue. Uses the
     * given depth.
     */
    public void crawl(Object crawlObject, int depth) {
        System.out.println("Added crawl object "+crawlObject+" with depth "+depth);
        queue.add(new T2(crawlObject, depth) );
        crawl();
    }
    
    /**
     * Starts crawling the pages added to queue with <code>addPageToQueue</code>.
     */
    public void crawl() {
        int depth=-1;
        URL url=null;
        T2<Object,Integer> entry;
        while(getCrawlCounter() > 0 && !forceExit) {
            synchronized(this) {
                try {
                    entry = queue.removeFirst();
                }
                catch (NoSuchElementException e) { entry=null; }
                if (entry==null) {
                    // don't exit if some other thread is currently processing a page, it may produce more entries in the queue.
                    // in this case sleep a while and try again (this is done a few lines below outside the synchronized block).
                    if(processing==0) {
                        break;
                    }
                }
                else {
                    processing++;
                }
            }
            if(entry==null){
                try {
                    Thread.sleep(100);
                }
                catch(Exception e){}
                continue;
            }
			
            Object crawlObject = entry.e1;
            if(crawlObject instanceof URL) {
                url = (URL) crawlObject;
            }
            else if(crawlObject instanceof File){
                File f = (File)crawlObject;
                try {
                    url = f.toURI().toURL();
                }
                catch(MalformedURLException e) {
                    if(isVerbose()) {
                        e.printStackTrace();
                    }
                    synchronized(this) { processing--;continue; }
                }
                if(!f.exists()) {                    
                    Logger.getLogger().writelog("WARNING","File "+f+" doesn't exist");
                    synchronized(this){processing--;continue;}
                }
                if(f.isDirectory()) {
                    URLMask um = getMask();
                    if(um == null || um.allow(url)) {
                        File[] fs=f.listFiles();
                        for(int i=0;i<fs.length;i++){
                            add(fs[i], depth+1);
                        }
                    }
                    else if(isVerbose()) {
                        Logger.getLogger().writelog("VERBOSE","Denied by mask "+url);
                    }
                    synchronized(this) { processing--; continue; }
                }
            }
            else {
                if(isVerbose()) {
                    Logger.getLogger().writelog("VERBOSE","Crawled Object not File nor URL " + crawlObject);
                }
                synchronized(this) { processing--;continue; }
            }
			
            depth = entry.e2;
            
            if(depth > 0) {
                URLMask um = getMask();
                if(um == null || um.allow(url)) {
                    if(isVerbose()) {
                        counter++;
                        Logger.getLogger().writelog("VERBOSE","[" + counter + "] Scanning "+url+" thread "+Thread.currentThread().getName());
                    }

                    synchronized(this) {
                        modifyCrawlCounter(-1);
                    }

                    try {
                        long t1=System.currentTimeMillis();
                        URLConnection uc=url.openConnection();
                        Wandora.initUrlConnection(uc);
                        if(uc instanceof HttpURLConnection) {
                            int res = ((HttpURLConnection)uc).getResponseCode();
                            while(res == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                String user = (String) getProperty("httpUser");
                                String password = (String) getProperty("httpPassword");

                                if("__PASS".equals(user) && "__PASS".equals(password)) {
                                    continue;
                                }

                                if(user == null || password == null) {
                                    Logger.getLogger().writelog("INFO", "Url connection requires username/password!");
                                    InterruptHandler ih = getInterruptHandler(HTTP_UNAUTHORIZED_INTERRUPTION);
                                    if(ih != null) {
                                        ih.handleInterrupt(this, HTTP_UNAUTHORIZED_INTERRUPTION, url);
                                    }
                                    user = (String) getProperty("httpUser");
                                    password = (String) getProperty("httpPassword");
                                    if("__IGNORE".equals(user) && "__IGNORE".equals(password)) {
                                        continue;
                                    }
                                }
                                if(user != null && password != null) {
                                    String userPassword = user + ":" + password;
                                    System.out.println("userPassword == '"+userPassword+"'");
                                    // String encodedUserPassword = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
                                    String encodedUserPassword = Base64.encodeBytes(userPassword.getBytes());
                                    uc=url.openConnection();
                                    Wandora.initUrlConnection(uc);
                                    System.out.println("encodedUserPassword == '"+encodedUserPassword+"'");
                                    uc.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);
                                }
                                res = ((HttpURLConnection)uc).getResponseCode();
                            }
                            if(res != HttpURLConnection.HTTP_OK) {
                                Logger.getLogger().writelog("WARNING",((HttpURLConnection)uc).getResponseMessage()+" "+res+" when getting url "+url);
                                synchronized(this) { processing--; continue; }
                            }
                        }

                        String contentType=uc.getContentType();
                        if(contentType.indexOf(";")>0){
                            contentType=contentType.substring(0,contentType.indexOf(";"));
                        }
                        contentType=contentType.trim();                    
                        if( contentType==null || contentType.length()==0 || 
                            contentType.startsWith("content/unknown") || 
                            contentType.startsWith("application/octet-stream")) {
                                String c=MimeTypes.getMimeType(url);
                                if(c!=null) {
                                    contentType=c;
                                }
                        }

                        Collection<Handler> h = getHandler(contentType);
                        if(h==null) {
                            String temp=contentType;
                            while(temp.length()>0){
                                temp=temp.substring(0,temp.length()-1);
                                h=getHandler(temp+"*");
                                if(h!=null) {
                                    break;
                                }
                            }

                            if(h==null) {
                                if(isVerbose()) {
                                    Logger.getLogger().writelog("WARNING","No content handler for "+contentType+" url: "+url);
                                }
                                synchronized(this) { processing--;continue; }
                            }
                        }

                        InputStream is=uc.getInputStream();

                        try {
                            for(Handler handler : h) {
                                handler.handle(getCallBack(), is, depth, url);
                                handleCount++;
                            }
                        } 
                        catch(Exception e){
                            Logger.getLogger().writelog("WARNING","Handler "+h.getClass().getName()+" threw an exception ("+e+") when handling url "+url);
                            if(isVerbose()) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            is.close();
                        }
                        catch(IOException e){};

                        long t2=System.currentTimeMillis();
                        if(contentType==null) {
                            contentType="null";
                        }

                        synchronized(pagesDone){
                            Integer d=(Integer)pagesDone.get(contentType);
                            if(d==null) {
                                pagesDone.put(contentType,new Integer(1));
                            }
                            else {
                                pagesDone.put(contentType,new Integer(d.intValue()+1));
                            }
                            Long t=(Long)timeTaken.get(contentType);
                            if(t==null) {
                                timeTaken.put(contentType,new Long(t2-t1));
                            }
                            else {
                                timeTaken.put(contentType,new Long(t.longValue()+(t2-t1)));
                            }
                        }
                    }
                    catch(Exception e){
                        Logger.getLogger().writelog("WARNING","Unable to get file "+url);
                        if(isVerbose()) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(isVerbose()) {
                    Logger.getLogger().writelog("WARNING","Denied by mask "+url);
                }
            }
            else {
                Logger.getLogger().writelog("WARNING","Depth exceeded "+url);
            }
            synchronized(this) { processing--; }
        }
    }
    

    


    /**
     * The Runnable implementation. Starts crawling.
     */
    public void run() {
        if(isVerbose()) {
            Logger.getLogger().writelog("VERBOSE","Starting thread "+Thread.currentThread().getName());
        }
        crawl();
        if(isVerbose()) {
            Logger.getLogger().writelog("VERBOSE","Thread "+Thread.currentThread().getName()+" is ready");
        }
    }
    
    


    
    // -------------------------------------------------------------------------

    

    public void add(Object crawlObject, int depth) {
        URLMask urlMask = getMask();
        if(urlMask != null) {
            if(!urlMask.allow(crawlObject)) {
                return;
            }
        }
        // If URL contains hashtag achors, remove these before URL is added
        // to the donePages and the queue.
        /*
        if(crawlObject instanceof URL) {
            String u = ((URL) crawlObject).toString();
            int i = u.indexOf('#');
            if(i != -1) {
                u = u.substring(0, i);
                if(u.length() == 0) {
                    return; // URL was plain hashtag reference!
                } 
                try {
                    crawlObject = new URL(u);
                }
                catch(Exception e) {}
            }
        }
        */
        if(checkDonePages){
            synchronized(donePages) {
                if(donePages.contains(crawlObject)) {
                    return;
                }
                donePages.add(crawlObject);
            }
        }
        synchronized(queue) {
            //System.out.println("Added crawl object: "+crawlObject+" to depth "+depth);
            queue.add( new T2(crawlObject, depth) );
        }
    }
    
    
   
    
    public void addObject(Object data) {
        // if(data instanceof TopicMap) addTopicMapFragment((TopicMap)data);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    /**
     * Sets the HashMap that contains pages that have already been crawled. If
     * they appear again in the queue, they will simply be skipped. With
     * <code>setDonePages</code> and <code>getDonePages</code> you can setup
     * multiple <code>Crawler</code>s that don't crawl pages that some other
     * <code>Crawler</code> have already processed.
     */
    public void setDonePages(HashSet hm) { 
        // do not make a clone of the given hashmap. the idea here is to be able to share a hashmap between multiple WebCrawlers
        donePages=hm; 
    }
    
    
    /**
     * Gets the HashMap that contains pages that have already been crawled.
     */
    public HashSet getDonePages(){ return donePages; }
    
    
    public int pagesProcessed(){ return donePages.size(); }
    
    @Override
    public void loadSettings(org.w3c.dom.Element rootElement) throws Exception {
        NamedNodeMap nnm=rootElement.getAttributes();
        
        try {
            numThreads=Integer.parseInt(nnm.getNamedItem("threads").getNodeValue());
        }
        catch(Exception e) {}
        checkDonePages=true;
        try {
            checkDonePages=Boolean.valueOf(nnm.getNamedItem("onceperpage").getNodeValue()).booleanValue();
        }
        catch(Exception e) {}
	super.loadSettings(rootElement);
    }

    public HashMap getPagesDone(){
        return pagesDone;
    }
    public HashMap getTimeTaken(){
        return timeTaken;
    }
    
    
    
    
    protected int numThreads=1;
    public static void main(String[] args) throws Exception {
        Logger.setLogger(new SimpleLogger());
        Logger.getLogger().writelog("*****************************************************");
        Logger.getLogger().writelog("***                 WEBCRAWLER                   ****");
        Logger.getLogger().writelog("***   (c) 2003 Grip Studios Interactive, Inc.    ****");
        Logger.getLogger().writelog("*****************************************************"); 
        
        String settingsFile="webcrawler.xml";
//        if(args.length > 0) settingsFile=args[0];

        HashMap replace=new HashMap();
        
        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("-") && args[i].length()>=2){
                if(args[i].startsWith("-d")){
                    if(i+2<args.length){
                        Logger.getLogger().writelog("Replacing ${"+args[i+1]+"} with "+args[i+2]+" in settings file.");
                        replace.put(args[i+1],args[i+2]);
                        i+=2;
                    }
                }
                else if(args[i].startsWith("-s")){
                    if(i+1<args[i].length()){
                        settingsFile=args[i+1];
                        i+=1;
                    }
                }
                else if(args[i].startsWith("-h") || args[i].startsWith("-?")){
                    Logger.getLogger().writelog("Command line options:");
                    Logger.getLogger().writelog("-d key value");
                    Logger.getLogger().writelog("    Replaces every occurrence of ${key} in settings file with value. -d may be defined multiple times.");
                    Logger.getLogger().writelog("    If you need to use character sequence ${ in settings file, put it inside ${ and }, like this \"${${}\"");
                    Logger.getLogger().writelog("[-s] filename");
                    Logger.getLogger().writelog("    Reads settings from filename.");
                    Logger.getLogger().writelog("-h, -?");
                    Logger.getLogger().writelog("    Displays this help info.");
                }
            }
            else{
                settingsFile=args[i];
            }
        }
		
        Logger.getLogger().writelog("Reading settings from '" + settingsFile + "'.");
		
        WebCrawler crawler = new WebCrawler();
        crawler.loadSettings(new ReplaceFileInputStream(settingsFile,replace));

        Thread[] threads=new Thread[crawler.numThreads];
		
        long t1 = System.currentTimeMillis();		
        for(int i=0;i<threads.length;i++){
            threads[i]=new Thread(crawler,"WebCrawler thread "+(i+1));
            threads[i].start();
        }
		
        boolean running=true;
        while(running){
            try {
                Thread.sleep(1000);
            }
            catch(Exception e){}
            running=false;
            for(int i=0;i<threads.length;i++){
                if(threads[i].isAlive()){
                    running=true;
                    break;
                }
            }
        }
        long t2 = System.currentTimeMillis();
        
        Logger.getLogger().writelog("INFO","Crawled in "+((t2-t1)/1000)+" seconds");
        
        HashMap done=crawler.getPagesDone();
        HashMap time=crawler.getTimeTaken();
        Iterator iter=done.keySet().iterator();
        while(iter.hasNext()){
            Object key=iter.next();
            int count=((Integer)done.get(key)).intValue();
            long t=((Long)time.get(key)).longValue();
            Logger.getLogger().writelog("INFO","Crawled "+count+" pages of type "+key+" with average time of "+((double)t/(double)count)+" per page.");
        }
        Logger.getLogger().writelog("INFO","Done");
    }
}



// -----------------------------------------------------------------------------


/*
 * A FileInputStream that replaces occurrences of Strings with other Strings while reading the
 * file. Note that skip() method implementation is not very efficient if skipping large chunks of
 * bytes. The available() method also doesn't return the number of available bytes correctly, it returns
 * the available bytes in the file but does not take into account the replacing of some of the bytes.
 */
class ReplaceFileInputStream extends FileInputStream {
    private HashMap replace;
    private int[] buf;
    private int bufl;
    private int bufoffs;

    public ReplaceFileInputStream(File file, HashMap replace) throws FileNotFoundException {
            super(file);
            this.replace=replace;
            buf=new int[1024];
            bufl=bufoffs=0;
    }
    public ReplaceFileInputStream(FileDescriptor fdObj, HashMap replace){
            super(fdObj);
            this.replace=replace;
            buf=new int[1024];
            bufl=bufoffs=0;
    }
    public ReplaceFileInputStream(String name, HashMap replace) throws FileNotFoundException {
            super(name);
            this.replace=replace;
            buf=new int[1024];
            bufl=bufoffs=0;
    }

    @Override
    public int read() throws IOException {
        // if there are characters in the buffer, return them one at a time
        if(bufl>0){
            int ret=buf[bufoffs++];
            if(bufoffs>=bufl) {
                bufoffs=bufl=0;
            }
            return ret;
        }
        // otherwise read new byte
        int b=super.read();
        if(b=='$'){ // if it starts with '$' check next byte
            b=super.read();
            if(b=='{'){ // if the next byte is '{' process further
                do{ // read until '}' is encountered or the file ends
                    b=super.read();
                    buf[bufl++]=b;
                } while(b!=-1 && b!='}' && bufl<buf.length);
                if(b!=-1){ // if we found a matching '}'
                    bufl--; // last character is '}'
                    byte[] temp=new byte[bufl]; // copy buf into a byte[]
                    for(int i=0;i<bufl;i++) {
                        temp[i]=(byte)buf[i];
                    }
                    String s=new String(temp,0,bufl); // convert byte[] to a String
                    if(replace.containsKey(s)){ // if the key is in replace HashMap, otherwise we will just leave the buffer as it and return it one byte at a time
                        // put the replace string inte buf
                        String v=(String)replace.get(s);
                        byte[] bytes=v.getBytes();
                        if(bytes.length>buf.length) {
                            buf=new int[bytes.length];
                        }
                        for(int i=0;i<bytes.length;i++){
                            buf[i]=bytes[i];
                        }
                        bufl=bytes.length;
                    }
                }
                bufoffs=1; // we return the first byte here so the offs will be 1
                return buf[0];
            }
            else{ // we had a single '$', return it and put whatever was the next byte into the buffer
                buf[0]=b;
                bufl=1;
                return '$';
            }
        }
        else return b; // if the character wasn't '$' return it
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b,0,b.length);
    }
    @Override
    public int read(byte[] b, int offs, int len) throws IOException {
        int l=0;
        int v=0;
        while(v!=-1 && l<len){
            v=read();
            if(v>-1){
                l++;
                b[offs++]=(byte)v;
            }
        }
        if(l==0 && v==-1) return -1;
        else return l;
    }
    @Override
    public long skip(long n) throws IOException {
        long s=0;
        while(s<n){
            if(read()!=-1) s++;
            else break;
        }
        return s;
    }
}
