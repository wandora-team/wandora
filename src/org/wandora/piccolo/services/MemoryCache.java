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
 * MemoryCache.java
 *
 * Created on August 2, 2004, 9:18 AM
 */

package org.wandora.piccolo.services;
import org.wandora.utils.ReaderWriterLock;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.*;
import org.wandora.piccolo.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;

/**
 * Page cache that stores pages in memory. Note that this isn't actually very much faster than FileSystemCache.
 * Probably has something to do with operating system disk caching. If the cache size would grow so large that
 * all pages wouldn't fit in OS's disk cache, this might speed things up, but then again, storing the pages in
 * memory would take huge amount of memory in that case.
 *
 * @author  olli
 */
public class MemoryCache  implements PageCacheService,XMLParamAware {
    
    private Logger logger;
    private HashMap cached;
    private String cacheDir;
    private HashMap locks;
    private HashMap pages;
    
    /** Creates a new instance of FileSystemCache */
    public MemoryCache() {
        cached=new HashMap();
        locks=new HashMap();
        pages=new HashMap();
    }
        
    public java.io.InputStream getPage(String key, long modifyTime) {
        Long l=(Long)cached.get(key);
        if(l==null) return null;
        if(modifyTime>l.longValue()) return null;
        ReaderWriterLock lock=(ReaderWriterLock)locks.get(key);
        lock.getReaderLock();
        ByteArrayInputStream bais=getBAIS(key);
        return new PageInputStream(lock,bais);
    }
    
    public String getServiceName() {
        return "MemoryCache";
    }
    
    public String getServiceType() {
        return "PageCacheService";
    }
    
    private ByteArrayInputStream getBAIS(String key){
        return new ByteArrayInputStream((byte[])pages.get(key));
    }
    
    private String makeFileName(String key){
        StringBuffer buffer=new StringBuffer(key.length()*2+cacheDir.length());
        buffer.append(cacheDir);
        byte[] bytes=key.getBytes();
        for(int i=0;i<bytes.length;i++){
            buffer.append(Integer.toHexString(bytes[i]));
        }
        return buffer.toString();
    }
    
    public java.io.OutputStream storePage(String key, long modifyTime) {
        
        ReaderWriterLock lock;
        synchronized(this){
            lock=(ReaderWriterLock)locks.get(key);
            if(lock==null) {
                lock=new ReaderWriterLock();
                locks.put(key,lock);
            }
        }
        lock.getWriterLock(); // this must be outside synchronized blocks !!!
            
        Long l=(Long)cached.get(key);
         // check cache inside synchronized method to make sure we don't overwrite newer file
        if(l!=null && modifyTime<=l.longValue()){
            lock.releaseWriterLock();
            return null;
        }

        return new PageOutputStream(lock,key,modifyTime,this);
        
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element && n.getNodeName().equals("cachedir")){
                Element e=(Element)n;
                cacheDir=processor.getElementContents(e);
                if(!cacheDir.endsWith(File.separator)) cacheDir+=File.separator;
            }
        }
    }
    
    public class PageInputStream extends InputStream {
        
        private ReaderWriterLock lock;
        private ByteArrayInputStream bais;
        
        private PageInputStream(ReaderWriterLock lock,ByteArrayInputStream bais){
            this.lock=lock;
            this.bais=bais;
        }
        
        public int read() throws IOException {
            return bais.read();
        }
        public int read(byte[] b) throws IOException {
            return bais.read(b);
        }
        public int read(byte[] b,int off,int len) throws IOException {
            return bais.read(b,off,len);
        }
        public void close() throws IOException {
            bais.close();
            lock.releaseReaderLock();
        }
    }
    
    public class PageOutputStream extends OutputStream {
        
        private ByteArrayOutputStream baos;
        private ReaderWriterLock lock;
        private String key;
        private long modifyTime;
        private MemoryCache parent;
        
        private PageOutputStream(ReaderWriterLock lock,String key,long modifyTime,MemoryCache parent){
            this.lock=lock;
            this.baos=new ByteArrayOutputStream();
            this.key=key;
            this.modifyTime=modifyTime;
            this.parent=parent;
        }
        
        public void write(int b) throws IOException {
            baos.write(b);
        }
        public void write(byte[] b) throws IOException {
            baos.write(b);
        }
        public void write(byte[] b,int off,int len) throws IOException {
            baos.write(b,off,len);
        }
        public void close() throws IOException {
            synchronized(parent){
                lock.releaseWriterLock();
                baos.close();
                pages.put(key,baos.toByteArray());
                cached.put(key,new Long(modifyTime));
            }
        }
        
    }
}
