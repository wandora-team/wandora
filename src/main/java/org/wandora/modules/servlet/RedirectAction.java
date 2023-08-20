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
 */
package org.wandora.modules.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author olli
 */


public class RedirectAction extends AbstractAction {

    protected String redirectUrl;
    
    /*
     * There are small differences between the redirect codes. 302 is the original
     * redirect, but its behavior is somewhat unspecified when it comes to the method
     * (GET or POST). 303 is a http/1.1 code that dictates that GET should be used
     * for the redirected request. 307 redirect should use the original method. 303
     * and 307 being http/1.1 might not work in VERY old browsers.
     * 
     * Furthermore, 301 means moved permanently as opposed to the temporary
     * nature of the previous codes.
     */
    protected int redirectCode=303;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        
        Object o;
        o=settings.get("redirectUrl");
        if(o!=null) redirectUrl=o.toString();
        
        o=settings.get("redirectCode");
        if(o!=null) redirectCode=Integer.parseInt(o.toString());
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(redirectUrl==null) logging.warn("redirectUrl not specified");
        
        super.start(manager);
    }

    
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        resp.setStatus(redirectCode);
        resp.setHeader("Location", redirectUrl);
        return true;
    }
    
}
