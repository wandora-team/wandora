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

/**
 * <p>
 * This package contains a modular framework for applications and
 * services. Most of the core logic of the framework is in the ModuleManager
 * class which loads the modules, handles dependencies between them and 
 * starts and stops them. See its documentation for details on how to start
 * using the framework.
 * </p>
 * <p>
 * The application or service built on the framework consists of any number of
 * separate modules, which may have dependencies between them. Some of the
 * modules typically implement certain interfaces and act as services providers
 * for other modules. Then you typically have the modules that interface with
 * the outside world and use the services provided by the other modules.
 * </p>
 * <p>
 * A typical use case is a web server where one module hooks into an http web
 * server, or even acts as one. The incoming requests can then be forwarded to
 * other modules that handle and reply to them. These modules then use other
 * service modules, for example a relational database module, to do their work.
 * The replies will often be created using a templating language so as to separate
 * the logic from the presentation. The templating engine and the templates will
 * be modules of their own too.
 * <p>
 * </p>There are several pre-made modules designed with
 * this type of server in mind, but it is by no means the only way to construct
 * an application or a service with the framework. The org.wandora.modules.servlet
 * package contains several modules which reply to http requests. These are called
 * actions and derive from AbstractAction. The same package also contains the
 * modules related to templates, currently the only implementation uses
 * Apache Velocity but other templating languages could easily be added.
 * </p>
 * <p>
 * Additional documentation is available at http://www.wandora.org/wiki/Wandora_modules_framework
 * </p>
 * 
 */
package org.wandora.modules;
