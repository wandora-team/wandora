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
 * FileSystemCache.java
 *
 * Created on July 9, 2004, 1:47 PM
 */

package org.wandora.piccolo.services;


import org.wandora.utils.XMLParamAware;
import org.wandora.utils.ReaderWriterLock;
import org.wandora.utils.*;
import org.wandora.piccolo.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;
/**
 * Page cache that stores pages on disk. This class is XMLParamAware and needs the element cachedir
 * as a parameter. This element must contain the directory used to store the cached pages. This class
 * will also try to get a logger from the symbol table of the XMLParamProcessor with key "logger". If
 * this is not found, a SimpleLogger is used.
 *
 * TODO: this class should be updated so that filenames are given sequentially and are then stored to
 *       a HashMap with the original key. This avoids both too long filenames and possible (although unlikely)
 *       hash collisions. 
 *
 * @author  olli
 */
public class FileSystemCache implements PageCacheService,XMLParamAware {
    
    private Logger logger;
    private HashMap cached;
    private HashMap filenames;
    private String cacheDir;
    private HashMap locks;
    
    /** Creates a new instance of FileSystemCache */
    public FileSystemCache() {
        cached=new HashMap();
        locks=new HashMap();
        filenames=new HashMap();
    }
        
    public java.io.InputStream getPage(String key, long modifyTime) {
        Long l=(Long)cached.get(key);
        if(l==null) return null;
        if(modifyTime>l.longValue()) return null;
        ReaderWriterLock lock=(ReaderWriterLock)locks.get(key);
        lock.getReaderLock();
        FileInputStream fis=getFile(key);
        if(fis==null){
            lock.releaseReaderLock();
            lock.getWriterLock();
            cached.remove(key);
            locks.remove(key);
            lock.releaseWriterLock();
            return null;
        }
        else return new PageInputStream(lock,fis);
    }
    
    public String getServiceName() {
        return "FileSystemCache";
    }
    
    public String getServiceType() {
        return "PageCacheService";
    }
    
    private FileInputStream getFile(String key){
        try{
            String fileName=makeFileName(key);
            File f=new File(fileName);
            FileInputStream in=new FileInputStream(fileName);
            return in;
        }catch(IOException ioe){
            logger.writelog("DBG","Couldn't open cached page. IOException: "+ioe.getMessage());
            return null;
        }        
    }
    
    private String makeFileName(String key){
        synchronized(filenames){
            String fn=(String)filenames.get(key);
            if(fn!=null) return fn;
            fn=cacheDir+"file"+filenames.size();
            filenames.put(key,fn);
            return fn;
        }
/*        if(useHash){
            try{
                java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-512");
                md.update(key.getBytes());
                byte[] bytes=md.digest();
                StringBuffer buffer=new StringBuffer(bytes.length*2+cacheDir.length());
                buffer.append(cacheDir);
                for(int i=0;i<bytes.length;i++){
                    int b=bytes[i];
                    if(b<0) b+=256;
                    buffer.append(Integer.toHexString(b));
                }
                return buffer.toString();
            }catch(java.security.NoSuchAlgorithmException nsae){nsae.printStackTrace(); return null;}
        }
        else{
            StringBuffer buffer=new StringBuffer(key.length()*2+cacheDir.length());
            buffer.append(cacheDir);
            byte[] bytes=key.getBytes();
            for(int i=0;i<bytes.length;i++){
                buffer.append(Integer.toHexString(bytes[i]));
            }
            return buffer.toString();
        }*/
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
            
        try{
            
            Long l=(Long)cached.get(key);
             // check cache inside synchronized method to make sure we don't overwrite newer file
            if(l!=null && modifyTime<=l.longValue()){
                lock.releaseWriterLock();
                return null;
            }
            
            FileOutputStream out=new FileOutputStream(makeFileName(key));
            return new PageOutputStream(lock,out,key,modifyTime,this);
        }catch(IOException ioe){
            logger.writelog("WRN","Couldn't write cached page. IOException: "+ioe.getMessage());
            lock.releaseWriterLock();
            return null;
        }
        
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                if(n.getNodeName().equals("cachedir")){
                    Element e=(Element)n;
                    cacheDir=XMLParamProcessor.getElementContents(e);
                    if(!cacheDir.endsWith(File.separator)) cacheDir+=File.separator;
                }
/*                else if(n.getNodeName().equals("usehash")){
                    String s=processor.getElementContents((Element)n);
                    useHash=s.equalsIgnoreCase("true");
                }*/
            }
            
        }
    }
    
    public class PageInputStream extends InputStream {
        
        private ReaderWriterLock lock;
        private FileInputStream fis;
        
        private PageInputStream(ReaderWriterLock lock,FileInputStream fis){
            this.lock=lock;
            this.fis=fis;
        }
        
        public int read() throws IOException {
            return fis.read();
        }
        @Override
        public int read(byte[] b) throws IOException {
            return fis.read(b);
        }
        @Override
        public int read(byte[] b,int off,int len) throws IOException {
            return fis.read(b,off,len);
        }
        @Override
        public void close() throws IOException {
            fis.close();
            lock.releaseReaderLock();
        }
    }
    
    public class PageOutputStream extends OutputStream {
        
        private FileOutputStream fos;
        private ReaderWriterLock lock;
        private String key;
        private long modifyTime;
        private FileSystemCache parent;
        
        private PageOutputStream(ReaderWriterLock lock,FileOutputStream fos,String key,long modifyTime,FileSystemCache parent){
            this.lock=lock;
            this.fos=fos;
            this.key=key;
            this.modifyTime=modifyTime;
            this.parent=parent;
        }
        
        public void write(int b) throws IOException {
            fos.write(b);
        }
        @Override
        public void write(byte[] b) throws IOException {
            fos.write(b);
        }
        @Override
        public void write(byte[] b,int off,int len) throws IOException {
            fos.write(b,off,len);
        }
        @Override
        public void close() throws IOException {
            synchronized(parent){
                lock.releaseWriterLock();
                fos.close();
                cached.put(key,new Long(modifyTime));
            }
        }
        
    }
}
