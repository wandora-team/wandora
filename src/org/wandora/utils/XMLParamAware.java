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
 *
 * 
 * XMLParamAware.java
 *
 * Created on July 5, 2004, 1:23 PM
 */

package org.wandora.utils;

/**
 * Used to tag classes that are specifically designed to work well with XMLParamProcessor.
 *
 * Must have a constructor with no parameters. After constructor returns, the init method is called.
 * You should perform most of the initialization here, as it will be provided with the xml element (and its children)
 * from the xml file. You can use processor to parse the parameters inside the element (using XMLParamProcessor.crateArray
 * for example) or do whatever custom initialization that is necessary. The created object will be added to
 * the processor symbol table by the processor where necessary after the initialization returns.
 *
 * Note that the required constructor cannot be enforced in Java with interfaces so there will be no compile
 * time errors if you fail to provide it but implement this interface. This will however result in a run time
 * error when XMLParamProcessor tries to instantiate your class.
 *
 * @author  olli
 */
public interface XMLParamAware {
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor);
}
