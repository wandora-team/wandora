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
 */
package org.wandora.application.modulesserver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.wandora.modules.ScopedModuleManager;
import org.wandora.modules.servlet.ServletModule;

/**
 * Module manager extension used by Wandora. The main new feature is
 * reading module bundles contained in subdirectories.
 *
 * @author olli
 */


public class WandoraModuleManager extends ScopedModuleManager {

    protected WandoraModulesServer server;
    
    public WandoraModuleManager(WandoraModulesServer server){
        this.server=server;
        this.setVariable("port",""+server.getPort());
        this.setVariable("serverHome",server.getServerPath());
    }
    
    public void readBundle(File dir) throws IOException {
        File config=new File(dir,"config.xml");
        if(!config.exists()) return;
        
        log.info("Reading bundle "+dir.getPath());
                
        ModuleBundle bundle=new ModuleBundle();
        
        initNewBundle(bundle, dir);
        
        bundle.readXMLOptionsFile(config.getCanonicalPath());
        
        if(bundle.getBundleName()==null){
            bundle.setBundleName(dir.getName());
        }
        addModule(bundle);
        
    }
    
    protected void initNewBundle(ModuleBundle bundle,File dir) throws IOException {
        String relativeHome=dir.getCanonicalPath();
        if(relativeHome.startsWith(getServerHome())){
            relativeHome=relativeHome.substring(getServerHome().length());
            if(relativeHome.startsWith(File.separator) && getServerHome().length()>0 ){
                relativeHome=relativeHome.substring(1);
            }
        }
        
        String urlPrefix=relativeHome;
        if(!File.separator.equals("/")) urlPrefix=urlPrefix.replace(File.separator, "/");
        if(!urlPrefix.endsWith("/")) urlPrefix+="/";
        String urlBase=server.getServletURL();
        if(!urlBase.endsWith("/")) urlBase+="/";
        urlBase+=urlPrefix;
        
        bundle.setVariable("home", dir.getCanonicalPath());
        bundle.setVariable("relativeHome", relativeHome);
        bundle.setVariable("urlbase", urlBase);
        bundle.setVariable("port",""+server.getPort());
        
        {
            // Add a servlet module to the bundle that the bundle services 
            // will use. It's configured to catch requests with the bundle
            // directory in the URL.
            BundleContext bundleServlet=new BundleContext();
            HashMap<String,Object> servletParams=new HashMap<String,Object>();
            servletParams.put("contextDir",relativeHome);
            ModuleSettings settings=new ModuleSettings("bundleServlet", true, 1, null);
            bundle.addModule(bundleServlet, servletParams, settings);
            
            // We also have to add the root servlet as an import so the bundle
            // servlet can hook into that.
            bundle.addImport(new Import(ServletModule.class, false));
        }
        
        bundle.setParentManager(this);
        
        bundle.setLogging(log);
    }
    
    
    public String getServerHome(){
        return server.getServerPath();
    }
    
    public void readBundles() throws IOException {
        readBundleDirectories(new File(server.getServerPath()));
    }
    
    protected void readBundleDirectories(File dir) throws IOException {
        if(!dir.exists() || !dir.isDirectory()) return;
        
        File config=new File(dir,"config.xml");
        if(config.exists()){
            readBundle(dir);
        }
        else {
            File[] l=dir.listFiles();
            for(File f : l) {
                if(f.isDirectory()) {
                    readBundleDirectories(f);
                }
            }
        }
        
    }
    
}
