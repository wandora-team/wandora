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
 * BSHLibrary.java
 *
 * Created on 21. lokakuuta 2005, 20:37
 *
 */

package org.wandora.application.beanshell;



import java.util.*;
import java.io.*;
import org.wandora.utils.*;
import bsh.*;



/**
 *
 * @author akivela
 */
public class BSHLibrary extends Vector<BSHComponent> {
    
    
    private HashMap componentCache = null;
    private Interpreter interpreter = null;
    private int limit = 1000;
    public boolean useCache = true;
    
    
    
    public BSHLibrary(Options options) {
        this(options.get("bsh.dir"));
    }
    
    
    
    
    
    /** Creates a new instance of BSHLibrary */
    public BSHLibrary(String bshDir) {
        componentCache = new HashMap();
        interpreter = new Interpreter();
        readBSHScripts(new File(bshDir));
    }
    
    
    
    
    
    

    
    public Vector<BSHComponent> getComponentsByName(String name) {
        if(useCache && componentCache.get(name) != null) {
            return (Vector<BSHComponent>) componentCache.get(name);
        }
        else {
            Vector<BSHComponent> components = new Vector();
            for(BSHComponent bshc : this) {
                if(bshc.includeComponentIn(name)) {
                    components.add(bshc);
                }
            }
            if(useCache) componentCache.put(name, components);
            return components;
        }
    }
    
    
    
    
    public void readBSHScripts(File file) {
        if(!file.exists()) System.out.println("Beanshell directory "+file.getAbsolutePath()+" does not exists.");
        if(!file.isDirectory()) System.out.println("Beanshell directory "+file.getAbsolutePath()+" is not a directory.");
        if(file.exists() && file.isDirectory()) {
            File[] files=file.listFiles(RegexFileChooser.ioFileFilter(RegexFileChooser.suffixChooser("bsh","Beanshell scripts")));
            for(File f: files){
                if(f.isFile()) {
                    try{
                        Reader reader=new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
                        Object bshc=interpreter.eval(reader);
                        reader.close();
                        if(bshc==null) {
                            System.out.println("Beanshell script "+f.getAbsolutePath()+" returned null.");
                        }
                        else if(!(bshc instanceof BSHComponent)){
                            System.out.println("Beanshell script "+f.getAbsolutePath()+" returned object which is not instanceof BSHComponent.");
                        }
                        else{
                            this.add((BSHComponent)bshc);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else {
                    if(--limit > 0) {
                        readBSHScripts(f);
                    }
                }
            }
        }
    }

   
}
