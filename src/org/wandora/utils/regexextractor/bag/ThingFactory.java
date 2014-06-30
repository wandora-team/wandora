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
 */

package org.wandora.utils.regexextractor.bag;

import org.wandora.utils.regexextractor.bag.things.Thing;

public interface ThingFactory {
    public static final String KEY_WRAPPINGS = "thingfactory.wrappings.";
    
    public Class getDefaultThingClass();
    public void setDefaultThingClass(String className);
    public Class thingClassForName(String thingClassName);
    
    public Thing createThing(String thingname);
    public Thing createThing(String thingClassName,String thingname);
    public Thing createThing(Class thingClass,String thingname);
    public Thing createThing(Class[] thingClass,String thingname);
}

