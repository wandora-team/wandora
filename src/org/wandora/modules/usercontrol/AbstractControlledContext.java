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
package org.wandora.modules.usercontrol;

import org.wandora.modules.servlet.GenericContext;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet;


/**
 *
 * @author olli
 */


public abstract class AbstractControlledContext extends GenericContext {

    protected UserAuthenticator authenticator;
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(this, UserAuthenticator.class, deps);
        return deps;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        authenticator=manager.findModule(this, UserAuthenticator.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        authenticator=null;
        super.stop(manager);
    }

    protected UserAuthenticator.AuthenticationResult authenticate(String requiredRole, final HttpServletRequest req, final HttpServletResponse resp, final ModulesServlet.HttpMethod method) throws IOException, AuthenticationException{
        return authenticator.authenticate(requiredRole, req, resp, method);
    }
    
}
