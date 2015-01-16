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
 *
 * 
 *
 * User.java
 *
 * Created on July 8, 2004, 4:16 PM
 */

package org.wandora.piccolo;

/**
 *
 * @author  olli
 */
public abstract class User {
    public static final String KEY_NAME="name";
    public static final String KEY_LANG="lang";
    
    public abstract Object getProperty(String key);
    public abstract void setProperty(String key,Object value);
    
    public int getIntegerProperty(String key){
        return ((Integer)getProperty(key)).intValue();
    }
    public boolean getBooleanProperty(String key){
        return ((Boolean)getProperty(key)).booleanValue();
    }
    public String getStringProperty(String key){
        return (String)getProperty(key);
    }
    public void setIntegerProperty(String key,int value){
        setProperty(key,new Integer(value));
    }
    public void setBooleanProperty(String key,boolean value){
        setProperty(key,new Boolean(value));
    }
}
