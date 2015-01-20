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
 * FileSystemCachePure.java
 *
 * Created on 2011
 */

package org.wandora.piccolo.services;



import org.wandora.utils.*;
import org.wandora.piccolo.*;
import org.w3c.dom.*;
import java.io.*;



/**
 * Page cache that stores pages on disk. This class is XMLParamAware and needs the element cachedir
 * as a parameter. This element must contain the directory used to store the cached pages. This class
 * will also try to get a logger from the symbol table of the XMLParamProcessor with key "logger". If
 * this is not found, a SimpleLogger is used.

 * @author  akivela
 */


public class FileSystemCachePure implements PageCacheService, XMLParamAware {

    private Logger logger;
    private String cacheDir;
    private long cacheTime = 1000*60*60*24*10;


    /** Creates a new instance of FileSystemCachePure */
    public FileSystemCachePure() {
    }

    public java.io.InputStream getPage(String key, long modifyTime) {
        String fileName = this.makeFileName(key);
        File cacheFile = new File(fileName);
        if(cacheFile.exists()) {
            long cacheFileModified = cacheFile.lastModified();
            if(cacheFileModified + cacheTime > System.currentTimeMillis()) {
                FileInputStream fis=getFile(key);
                return fis;
            }
        }
        return null;
    }



    
    public String getServiceName() {
        return "FileSystemCachePure";
    }

    public String getServiceType() {
        return "PageCacheService";
    }




    private FileInputStream getFile(String key){
        try{
            String fileName=makeFileName(key);
            FileInputStream in=new FileInputStream(fileName);
            return in;
        }
        catch(IOException ioe){
            logger.writelog("DBG","Couldn't open cached page. IOException: "+ioe.getMessage());
            return null;
        }
    }




    private String makeFileName(String key) {
        String f = "" + key.hashCode();
        String dir = cacheDir + f.substring(f.length()-2);
        File dirf = new File(dir);
        if(!dirf.exists()) {
            dirf.mkdir();
        }
        return dir + File.separator + f;
    }




    public java.io.OutputStream storePage(String key, long modifyTime) {
        try {
            return new FileOutputStream(makeFileName(key));
        }
        catch(IOException ioe){
            logger.writelog("WRN","Couldn't write cached page. IOException: "+ioe.getMessage());
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
                if(n.getNodeName().equals("cachetime")){
                    Element e=(Element)n;
                    try {
                        cacheTime=Long.parseLong( XMLParamProcessor.getElementContents(e) );
                    }
                    catch(Exception ex) {
                        logger.writelog("WRN","Illegal cache time provided. Exception occurred: "+ex.getMessage());
                    }
                }
            }
        }
    }

}
