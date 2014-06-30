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
 *
 * InstanceMaker.java
 *
 * Created on July 12, 2004, 10:55 AM
 */

package org.wandora.modules.velocityhelpers;

/**
 * Use this class in velocity when you need to make new instances of some class in
 * the template.
 *
 * @author  olli
 */
public class InstanceMaker {
    
    private Class c;
    
    /** Creates a new instance of InstanceMaker */
    public InstanceMaker(String cls) throws ReflectiveOperationException {
        c=Class.forName(cls);
    }
    public InstanceMaker(Class cls){
        this.c=cls;
    }
    
    
    public Object make() throws Exception {
        return c.newInstance();
    }
}
