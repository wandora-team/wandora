/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
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

package org.wandora.application.modulesserver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.AbstractAction;

/**
 * 
 *
 * @author olli
 */


public abstract class AbstractWebApp extends AbstractAction implements ModulesWebApp {
    protected String webAppName;
    protected String appStartPage;
    
    protected String topicRequestKey="topic";

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        // NOTE: ViewTopicWebApp does not extend this so all modifications here
        // should probably be done there as well.
        Object o=settings.get("webAppName");
        if(o!=null) webAppName=o.toString().trim();
        else {
            ModuleManager.ModuleSettings ms=manager.getModuleSettings(this);
            if(ms!=null && ms.name!=null) webAppName=ms.name;
            else webAppName="Unnamed Webapp";
        }
        
        o=settings.get("appStartPage");
        if(o!=null) appStartPage=o.toString().trim();
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        super.start(manager); 

        // NOTE: ViewTopicWebApp does not extend this so all modifications here
        // should probably be done there as well.
        if(appStartPage==null){
            if(isDefaultAction){
                appStartPage=servletModule.getServletURL();
            }
            else if(!handledActions.isEmpty()){
                String action=handledActions.iterator().next();
                appStartPage=servletModule.getServletURL();
                if(appStartPage!=null){
                    try {
                        appStartPage+="?"+actionParamKey+"="+URLEncoder.encode(action, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex); // shouldn't happen
                    }
                }
            }
        }
    }
    
    
    
    @Override
    public String getAppName() {
        return webAppName;
    }

    @Override
    public String getAppStartPage() {
        return appStartPage;
    }

    @Override
    public String getAppTopicPage(String si) {
        // NOTE: ViewTopicWebApp does not extend this so all modifications here
        // should probably be done there as well.
        if(appStartPage==null) return null;
        try{
            String page=appStartPage;
            if(page.indexOf("?")<0) page+="?";
            else page+="&";
            
            return page+topicRequestKey+"="+URLEncoder.encode(si, "UTF-8");        
        }catch(UnsupportedEncodingException uee){
            throw new RuntimeException(uee); // shouldn't happen
        }
    }
    
}
