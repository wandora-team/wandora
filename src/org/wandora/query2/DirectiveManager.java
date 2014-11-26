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
 */
package org.wandora.query2;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.wandora.application.Wandora;

/**
 * This class can be used to get a list of directives, possible other similar
 * services in the future. It'll automatically find every directive in the
 * org.wandora.query2 package. Other directives need to register themselves.
 *
 * @author olli
 */


public class DirectiveManager {
    
    private static DirectiveManager instance;
    public static synchronized DirectiveManager getDirectiveManager(){
        if(instance==null) instance=new DirectiveManager();
        return instance;
    }
    
    private final ArrayList<Class<? extends Directive>> scannedDirectives;
    private final ArrayList<Class<? extends Directive>> registeredDirectives;
    
    private DirectiveManager(){
        scannedDirectives=new ArrayList<>();
        scanDirectives();
        registeredDirectives=new ArrayList<>();
    }
    
    public void registerDirective(Class<? extends Directive> cls){
        synchronized(registeredDirectives){
            registeredDirectives.add(cls);
        }
    }
    
    private void scanDirectives() {
        try {
            String pkg="org.wandora.query2";
            
            String path = pkg.replace('.', '/');
            Enumeration<URL> toolResources = ClassLoader.getSystemResources(path);
            
            while(toolResources.hasMoreElements()) {
                URL url=toolResources.nextElement();
                
                if(!url.getProtocol().equals("file")) continue;
                
                try{
                    File dir=new File(url.toURI());
                    if(!dir.isDirectory()) continue;

                    for(File f : dir.listFiles()){
                        String file=f.getAbsolutePath();

                        if(!file.endsWith(".class")) continue;

                        int ind=file.lastIndexOf(path);
                        if(ind<0) continue;

                        String cls=file.substring(ind,file.length()-6);
                        if(cls.startsWith("/")) cls=cls.substring(1);

                        cls=cls.replace('/', '.');
                        try {
                            Class<?> c=Class.forName(cls);
                            if(Directive.class.isAssignableFrom(c)){
                                scannedDirectives.add((Class<? extends Directive>)c);
                            }
                        } catch (ClassNotFoundException ex) {
                            // ignore
                        }
                    }
                } catch(URISyntaxException use){
                    // ignore
                }
            }
        } catch (IOException ex) {
            Wandora.getWandora().handleError(ex);
        }
    }
    
    public List<Class<? extends Directive>> getDirectives(){
        List<Class<? extends Directive>> ret=new ArrayList<>(scannedDirectives.size()+registeredDirectives.size());
        ret.addAll(scannedDirectives);
        synchronized(registeredDirectives){
            ret.addAll(registeredDirectives);
        }
        return ret;
    }
}
