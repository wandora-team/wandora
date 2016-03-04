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
 * SimpleVirtualFileSystem.java
 *
 * Created on October 4, 2004, 12:56 PM
 */

package org.wandora.topicmap.remote.server;
import org.wandora.utils.XMLParamProcessor;
import org.wandora.utils.XMLParamAware;
import java.io.*;
import java.util.*;
import org.wandora.utils.*;
import org.w3c.dom.*;

/**
 *
 * @author  olli
 */
public class SimpleVirtualFileSystem implements VirtualFileSystem,XMLParamAware {
    
    private HashMap directories;
    private HashMap urls;
    
    /** Creates a new instance of SimpleVirtualFileSystem */
    public SimpleVirtualFileSystem() {
        directories=new HashMap();
        urls=new HashMap();
    }
    
    public String cleanFileName(String f){
        f=f.replaceAll("[ \\\\/\\\"\\\'+&]","_");
        return f;
    }
    
    public java.io.File getRealFileFor(String file) {
        int ind=file.indexOf("/");
        String dir="/";
        if(ind>0){
            dir=file.substring(0,ind).trim();
        }
        String f=file.substring(ind+1);
        if(f.indexOf("/")!=-1) return null;
        String real=(String)directories.get(dir);
        if(real==null) return null;
        real+=cleanFileName(f);
        return new File(real);
    }
    
    public String getURLFor(String file) {
        int ind=file.indexOf("/");
        String dir="/";
        if(ind>0){
            dir=file.substring(0,ind).trim();
        }
        String f=file.substring(ind+1);
        if(f.indexOf("/")!=-1) return null;
        String url=(String)urls.get(dir);
        if(url==null) return null;
        url+=cleanFileName(f);
        return url;
    }
    
    public String[] listDirectories(String dir) {
        if(dir.equals("/")){
            Vector v=new Vector();
            Iterator iter=directories.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry e=(Map.Entry)iter.next();
                String d=(String)e.getKey();
                if(!d.equals("/")){
                    v.add(d);
                }
            }
            return (String[])v.toArray(new String[0]);
        }
        else return new String[0];
    }
    
    public String[] listFiles(String dir) {
        File f=getRealFileFor(dir);
        if(f==null || !f.isDirectory()) return null;
        File[] files=f.listFiles();
        Vector v=new Vector();
        for(int i=0;i<files.length;i++){
            if(!files[i].isDirectory()){
                v.add(files[i].getName());
            }
        }
        return (String[])v.toArray(new String[0]);
    }
    
    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        NodeList nl=element.getElementsByTagName("directory");
        for(int i=0;i<nl.getLength();i++){
            Element e=(Element)nl.item(i);
            String dir=e.getAttribute("dir");
            String loc=e.getAttribute("loc");
            String url=e.getAttribute("url");
            directories.put(dir,loc);
            if(url!=null) urls.put(dir,url);
        }
    }

}
