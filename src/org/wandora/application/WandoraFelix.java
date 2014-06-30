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
 */
package org.wandora.application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
/*
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.framework.util.Util;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
*/
/**
 *
 * This class is meant as a way to include an OSGI framework in Wandora
 * using the Apache Felix implementation. This class is very much incomplete
 * and untested, really only a stub to be fully implemented at some later time.
 * 
 * Everything is commented so that we don't have to include the felix jar in the 
 * project to be able to build it before we really need it.
 * 
 * @author olli
 */


public class WandoraFelix {
/*    
    public static final String PROPS_FILE="resources/conf/felix.properties";
    
    private static WandoraFelix instance=null;
    
    private final Framework framework;
    
    private WandoraFelix(){
        HashMap<String,String> props;
        try{
            props=loadProperties();
        }
        catch(IOException ioe){
            Wandora.getWandora().handleError(ioe);
            props=new HashMap<String,String>();
        }
        
        FrameworkFactory fwFactory=new FrameworkFactory();
        framework=fwFactory.newFramework(props);
        try{
            framework.start();
        }catch(BundleException be){
            Wandora.getWandora().handleError(be);
        }
        
    }
    
    private static HashMap<String,String> loadProperties() throws IOException {
        Properties props = new Properties();
        InputStream is = new FileInputStream(PROPS_FILE);

        HashMap<String,String> map=new HashMap<String, String>();
        for(Enumeration e=props.propertyNames();e.hasMoreElements();){
            String name = (String) e.nextElement();
            map.put(name,Util.substVars(props.getProperty(name), name, null, props));
        }
        return map;
    }
    
    public static synchronized WandoraFelix getFelix(){
        if(instance==null) instance=new WandoraFelix();
        return instance;
    }
    
    public Framework getFramework(){
        return framework;
    }
*/    
}
