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
package org.wandora.modules.usercontrol;

import java.io.IOException;
import java.util.Map;

import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.UserAuthenticator.AuthenticationResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author olli
 */


public class RoleRestrictedContext extends AbstractControlledContext {

    protected String requiredRole;
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=settings.get("requiredRole");
        if(o!=null) this.requiredRole=o.toString();
        super.init(manager, settings);
    }
    

    @Override
    protected ForwardResult doForwardRequest(HttpServletRequest req, HttpServletResponse resp, HttpMethod method) throws ServletException, IOException, ActionException {
        try{
            AuthenticationResult res=authenticate(requiredRole, req, resp, method);
            if(res.authenticated) return new ForwardResult(true, false, res.user);
            else return new ForwardResult(false, res.responded, res.user);
        }catch(AuthenticationException ae){
            throw new ActionException(ae);
        }
    }
    
}
