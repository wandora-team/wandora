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
 */
package org.wandora.application.modulesserver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.ModuleManager.ModuleSettings;
import org.wandora.modules.topicmap.ViewTopicAction;

/**
 *
 * @author olli
 */


public class ViewTopicWebApp extends ViewTopicAction implements ModulesWebApp {

    protected String webAppName;
    protected String appStartPage;
    
    public ViewTopicWebApp(){
        
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        // NOTE: ViewTopicWebApp does not extend AbstractWebApp so you may want
        // to do any modifications here in AbstractWebApp also.
        Object o=settings.get("webAppName");
        if(o!=null) webAppName=o.toString().trim();
        else {
            ModuleSettings ms=manager.getModuleSettings(this);
            if(ms!=null && ms.name!=null) webAppName=ms.name;
            else webAppName="Unnamed Webapp";
        }
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        // NOTE: ViewTopicWebApp does not extend AbstractWebApp so you may want
        // to do any modifications here in AbstractWebApp also.
        super.start(manager); 
        
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
        // NOTE: ViewTopicWebApp does not extend AbstractWebApp so you may want
        // to do any modifications here in AbstractWebApp also.
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
