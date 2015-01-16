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
package org.wandora.modules;

/**
 *
 * @author olli
 */
public class MissingDependencyException extends ModuleException {
    public MissingDependencyException(Class<? extends Module> cls){
        this(null,cls,null);
    }
    public MissingDependencyException(Class<? extends Module> cls,String instanceName){
        this(null,cls,instanceName);
    }
    public MissingDependencyException(String message,Class<? extends Module> cls){
        this(message,cls,null);
    }
    public MissingDependencyException(String message,Class<? extends Module> cls,String instanceName){
        super("Missing module dependency class="+cls.getName()+(instanceName==null?"":" instanceName=\""+instanceName+"\""));
    }
}
