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
package org.wandora.modules;


import java.util.Map;

import javax.script.ScriptException;

import org.w3c.dom.Element;

/**
 * An interface indicating that your module will process the contents of
 * the module element instead of relying on the automatic processing of
 * ModuleManager. When you implement this interface, ModuleManager will give
 * your module a chance to process the contents of the module element on its own
 * instead of the module manager parsing the parameters. It's completely up to the
 * module then what to do with the contents. However, some common elements, like
 * useService elements, should be allowed to still be present and they will still
 * be processed by the module manager. Also, any module you derive from may expect
 * some parameters, you will have to accommodate these somehow. Note that
 * you can still use the parseXMLOptionsElement method in module manager to
 * do standard parameters parsing in addition to any custom handling.
 * 
 * @author olli
 */


public interface XMLOptionsHandler {
    /**
     * Parses the given module element and returns a map containing the
     * module parameters. The module parameters will later be passed to your
     * module again in init method. You may of course retain parsed information
     * outside this map as well and refer to it later in the initialisation.
     * 
     * @param manager The module manager handling this module.
     * @param e The module element which is to be parsed.
     * @param source The source identifier for the module, could be the file name
     *                where the module comes from or something else.
     * @return The parsed module parameters which will be passed to the init
     *          method of your module later.
     */
    public Map<String,Object> parseXMLOptionsElement(ModuleManager manager,Element e,String source) throws ReflectiveOperationException, ScriptException;
//    public void writeXMLOptions(ModuleManager manager,Writer out) throws IOException;
}
