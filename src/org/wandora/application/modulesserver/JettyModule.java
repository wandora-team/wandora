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
package org.wandora.application.modulesserver;

import org.mortbay.jetty.Server;
import org.wandora.modules.AbstractModule;

/**
 * A module that exposes the Jetty server object itself. This can be used
 * for low level access to the web server. Note that using this makes the module
 * bundle dependent on Jetty being the servlet container.
 *
 * @author olli
 */


public class JettyModule extends AbstractModule {

    protected Server jettyServer;
    
    public JettyModule(Server jettyServer){
        this.jettyServer=jettyServer;
    }
    
    public Server getJetty(){
        return jettyServer;
    }
}
