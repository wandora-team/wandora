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
 * AbstractCrawler.java
 *
 * Created on 18. maaliskuuta 2003, 12:47
 */

package org.wandora.piccolo.utils.crawler;

import org.wandora.utils.XMLParamProcessor;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.wandora.utils.*;
import org.wandora.piccolo.utils.crawler.handlers.Handler;
import org.wandora.piccolo.utils.crawler.interrupthandlers.InterruptHandler;
import org.wandora.piccolo.*;

/**
 *
 * @author  akivela
 */
public abstract class AbstractCrawler implements CrawlerAccess, Crawler {
    
    private HashMap properties = new HashMap();
    private HashMap<String,ArrayList<Handler>> handlers = new HashMap();
    private HashMap interruptHandlers = new HashMap();
    private URLMask mask = null;
    private boolean verbose = false;    
    private CrawlerAccess callback;
    private int maxLeft = 0;
    protected boolean forceExit = false;
    protected int handleCount = 0;
    
    
    
    /** Creates a new instance of AbstractCrawler */
    public AbstractCrawler() {
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void forceExit() {
        this.forceExit = true;
    }
    

    
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    
    
    
    public void loadSettings(String file) throws Exception {
        loadSettings(new FileInputStream(file));
    }
    
    
    public void loadSettings(InputStream in) throws Exception {
/*        DOMParser parser=new DOMParser();

        parser.setFeature( "http://xml.org/sax/features/validation", false);
        parser.parse(new InputSource(in));
            
        org.w3c.dom.Document doc = parser.getDocument();
        org.w3c.dom.Element docElement = doc.getDocumentElement();

        loadSettings(docElement);*/
        org.w3c.dom.Document doc = XMLParamProcessor.parseDocument(in);
        loadSettings(doc.getDocumentElement());
    }
    
    public void loadSettings(org.w3c.dom.Element rootElement) throws Exception {
        NamedNodeMap nnm;
        Node node;
        NodeList nodeList;
                        
        XMLParamProcessor paramProcessor=new XMLParamProcessor();
        
        nodeList = rootElement.getElementsByTagName("mask");
        String[] urls = new String[nodeList.getLength()];
        int[] types = new int[nodeList.getLength()];
        for(int i=0;i<nodeList.getLength();i++){
            node = nodeList.item(i);
            nnm=node.getAttributes();
            int type=URLMask.TYPE_ALLOW;
            try {
                if(nnm.getNamedItem("type").getNodeValue().equalsIgnoreCase("deny")) {
                    type=URLMask.TYPE_DENY;
                }
            }
            catch(NullPointerException e){}
            String url=nnm.getNamedItem("url").getNodeValue();
            urls[i]=url;
            types[i]=type;
        }
        setMask(new URLMask(urls,types));
        
        nodeList=rootElement.getElementsByTagName("contenthandler");
        for(int i=0;i<nodeList.getLength();i++) {
            node = nodeList.item(i);
            // Object handler=createObject((org.w3c.dom.Element) node);
            Object handler=paramProcessor.createObject((Element)node);
            if (handler != null) {
                addHandler((Handler) handler);
            }
        }
   
        nodeList=rootElement.getElementsByTagName("startpoint");
        for(int i=0;i<nodeList.getLength();i++){
            node=nodeList.item(i);
            nnm=node.getAttributes();
            int depth=Integer.parseInt(nnm.getNamedItem("depth").getNodeValue());
            Node resn=nnm.getNamedItem("file");
            if(resn!=null){
                String res=resn.getNodeValue();
                add(new File(res), depth);
            }
            else{
                String res=nnm.getNamedItem("url").getNodeValue();
                add(new URL(res), depth);
            }
        }
        
        nnm=rootElement.getAttributes();
        try {
            setCrawlCounter(Integer.parseInt(nnm.getNamedItem("maxpages").getNodeValue()));
        }
        catch(Exception e) {
            e.printStackTrace();
            Logger.getLogger().writelog("WNR", "Crawl counter (maxpages) not found in settings! Using default value '1000'.");
            setCrawlCounter(1000);
        }
        
        try {
            String ver=nnm.getNamedItem("verbose").getNodeValue();
            if(!ver.equalsIgnoreCase("false")) verbose=true;
        }
        catch(NullPointerException e) {
            Logger.getLogger().writelog("WNR", "Verbose flag not found in settings! Using default value (false).");
            verbose = false;
        }
 
    }
    
    
    
    // -------------------------------------------------------------------------
    
    /**
     * Sets the used URLMask
     */
    public void setMask(URLMask m){
        mask = m;
    }
    
    public URLMask getMask() {
        return mask;
    }
    
    

    
    // -------------------------------------------------------------------------
    
    /**
     * Adds a ContentHandler to the used content handlers.
     */
    public void addHandler(Handler h) {
        if(h != null) {
            String[] contentTypes = h.getContentTypes();
            for(int i=0; i<contentTypes.length; i++) {
                ArrayList<Handler> handlersForContentType = handlers.get(contentTypes[i]);
                if(handlersForContentType == null) {
                    handlersForContentType = new ArrayList<Handler>();
                }
                handlersForContentType.add(h);
                handlers.put(contentTypes[i], handlersForContentType);
            }
        }
    }

    
    public Collection<Handler> getHandler(String contentType) {
        try {
            return handlers.get(contentType);
        }
        catch (Exception e) {
            Logger.getLogger().writelog("ERROR", "Illegal handler object found in handlers!"); 
            return null;
        }
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Adds a InterruptHandler to the used content handlers.
     */
    public void addInterruptHandler(InterruptHandler h) {
        int[] interruptTypes = h.getInterruptsHandled();
        for(int i=0; i<interruptTypes.length; i++){
            interruptHandlers.put(new Integer(interruptTypes[i]), h);
        }
    }

    
    public InterruptHandler getInterruptHandler(int interruptType) {
        try {
            return (InterruptHandler) interruptHandlers.get(new Integer(interruptType));
        }
        catch (Exception e) {
            Logger.getLogger().writelog("ERROR", "Illegal interrupthandler object found in interrupthandlers!"); 
            return null;
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    /**
     * Sets the callback object. When content handlers recognize an url in the
     * content of a document (or otherwise figure out an url the crawler should
     * crawl), they give this url to the call back object. They can also give
     * any other objects they build from the content to the call back. The
     * default call back is the crawler itself. If you want the crawler to
     * crawl deeper when url are recognized, you should, at some point, use the
     * call back functions of the crawler. That is, if you set your own call
     * back, in its call back functions, you should forward the method calls to
     * the crawler if you want the page to be added to the crawler's queue.
     */
    public void setCallBack(CrawlerAccess cb){
        callback=cb;
    }
    
    public CrawlerAccess getCallBack() {
        return callback;
    }
    
    
    // -------------------------------------------------------------------------
    
    /**
     * Sets the maximum number of pages the crawler will process.
     */
    public void setCrawlCounter(int n) { maxLeft=n; }

    
    public int getCrawlCounter() { return maxLeft; }
    
    public void modifyCrawlCounter(int delta) { maxLeft = maxLeft + delta; }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public int getHandledDocumentCount() {
        return handleCount;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public boolean isVerbose() {
        return verbose;
    }
    
    public void setVerbose(boolean v) {
        verbose = v;
    }
    
    
    // -------------------------------------------------------------------------
    
    /**
	 * Creates an object from the given XML Element. If the element has the 'class' attribute, makes a new instance
	 * of the class given in that attribute. The element is created either by using the constructor with no parameters,
	 * if the element does not have 'param' child elements. If the element has one or more 'param' child elements
	 * an Object is created from each of those params. The parameters are put in an Object array and the object
	 * array is given as a parameter to the constructor.
	 *
	 * If the element does not have the 'class' attribute, the method returns the element itself. This means that, if the
	 * 'param' child element does not have the 'class' attribute, the xml markup is given as a parameter to the
	 * constructed object.
	 *
	 * For example:
	 * <code>
	 		<o class="com.foo.MyObject">
				<param class="com.foo.MyParam">
					<param>
						<a b="1"/>
						<a b="2"/>
					</param>
				</param>
				<param number="3"/>
			</o>
			<o2 class="com.foo.MyObject"/>
	 * </code>
	 * If this method is called with the 'o' element in the example, an instance of com.foo.MyObject is created using
	 * a constructor that takes an Object array as a parameter. The array will have two objects. The object with
	 * index 0 is an instance of com.foo.MyParam and the object with index 1 is an instance of org.w2c.dom.Element,
	 * the xml element &lt;param number="3"/>. The com.foo.MyParam is instantiated with one object in the Objects array,
	 * the xml 'param' element containing two 'a' elements. Calling with the 'o2' element will also produce an instance
	 * of com.foo.MyObject, but this object is instantiated with the constructor taking no parameters.
     * @deprecated
	 */ 
    public static Object createObject(org.w3c.dom.Element e) throws Exception {
        NamedNodeMap nnm = e.getAttributes();
		// If element does not have a class attribute, return the element. This way it is possible to pass
		// any kind of parameters to classes.
		if(nnm.getNamedItem("class")==null) return e;
        String cname = nnm.getNamedItem("class").getNodeValue();
        try {
            Class c=Class.forName(cname);
            NodeList params=e.getElementsByTagName("param");

            if(params.getLength()>0){
                Constructor con = c.getConstructor(new Class[] { new Object[0].getClass() } );
                Object[] os = new Object[params.getLength()];
                for(int i=0; i<params.getLength(); i++) {
                    os[i] = createObject((org.w3c.dom.Element) params.item(i));
                }
                return con.newInstance(new Object[]{os});
            }
            else {
                return c.newInstance();
            }
        }
        catch (ClassNotFoundException e1) {
            Logger.getLogger().writelog("ERR", "Crawler class '" + cname + "' not found! Check your crawler settings!");
        }
        catch (Exception e3) {
            Logger.getLogger().writelog("ERR", "Exception '" + e3.toString() + "' occurred while solving crawler settings (class '"+cname+"')!");
        }
        return null;
    }


    
    
}
