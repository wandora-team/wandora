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
 * SimpleVirtualFileSystem.java
 *
 * Created on 24. heinäkuuta 2006, 16:35
 */

package org.wandora.utils.fileserver;
import java.io.*;
import java.util.*;
import org.wandora.utils.*;
import org.w3c.dom.*;

/**
 *
 * This is an implementation of VirtualFileSystem. It must be initialized with
 * a set of mount points. At least one for root directory and any number for other
 * directories. Nested directories are not supported (directories inside directories),
 * only subdirectories at root level. All files in the mounted directories show as
 * virtual files but directories in them are not visible or accessible (unless
 * separately mounted as virtual directories).
 *
 * @author  olli
 */
public class SimpleVirtualFileSystem implements VirtualFileSystem/*,XMLParamAware*/ {
    
    private HashMap directories;
    private HashMap urls;
    
    /** Creates a new instance of SimpleVirtualFileSystem */
    public SimpleVirtualFileSystem(String dir,String loc,String url) {
        this();
        addDirectory(dir,loc,url);
    }
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
        if(ind==0) {
            if(file.length()>1) file=file.substring(1);
            else file="";
            ind=file.indexOf("/");
        }
        if(file.startsWith("..")) return null;
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
            ArrayList v=new ArrayList();
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
        ArrayList v=new ArrayList();
        for(int i=0;i<files.length;i++){
            if(!files[i].isDirectory()){
                v.add(files[i].getName());
            }
        }
        return (String[])v.toArray(new String[0]);
    }
    
    public void addDirectory(String dir,String loc,String url){
        directories.put(dir,loc);
        if(url!=null) urls.put(dir,url);
    }
 /*   
    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        NodeList nl=element.getElementsByTagName("directory");
        for(int i=0;i<nl.getLength();i++){
            Element e=(Element)nl.item(i);
            String dir=e.getAttribute("dir");
            String loc=e.getAttribute("loc");
            String url=e.getAttribute("url");
            addDirectory(dir,loc,url);
        }
    }
*/
}
