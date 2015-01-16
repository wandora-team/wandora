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
 */
package org.wandora.modules.fng;

import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.*;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 *
 * @author olli
 */


public class FNGAPIExceptionHandler extends TemplateActionExceptionHandler {

    protected String outputModeParamKey="format";
    protected String defaultOutputMode="dc-xml";
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        
        Object o=settings.get("defaultOutput");
        if(o!=null) defaultOutputMode=o.toString();
        
        o=settings.get("outputParamKey");
        if(o!=null) outputModeParamKey=o.toString();

        super.init(manager, settings);
    }
    
    protected String getOutputMode(HttpServletRequest req){
        String outputMode=req.getParameter(outputModeParamKey);
        if(outputMode==null || outputMode.length()==0) {
            if(defaultOutputMode==null) return null;
            else outputMode=defaultOutputMode;
        }
        
        if(!outputMode.equals("dc-ds-xml") 
             && !outputMode.equals("dc-xml") 
             && !outputMode.equals("dc-json")
             && !outputMode.equals("dc-text")) return null;
        else return outputMode;
    }
    
    @Override
    protected Template getTemplate(HttpServletRequest req, HttpMethod method, ActionException exception) {
        String outputMode=getOutputMode(req);
        if(outputMode!=null) {
            Template template=templateManager.getTemplate(templateKey,outputMode);
            if(template!=null) return template;            
        }
        
        return super.getTemplate(req, method, exception);
    }
    
    
}
