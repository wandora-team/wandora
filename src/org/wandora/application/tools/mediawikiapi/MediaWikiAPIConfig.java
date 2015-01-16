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

package org.wandora.application.tools.mediawikiapi;

/**
 *
 * Simple internal container for storing connection configuration.
 * 
 * @author Eero
 */

class MediaWikiAPIConfig {
    
    private String url;
    private String uname;
    private String password;
    
    //--------------------------------------------------------------------------
    
    protected String getURL(){
        return url;
    }
    
    protected String getUName(){
        return uname;
    }
    
    protected String getPassword(){
        return password;
    }
    
    //--------------------------------------------------------------------------
    
    protected void setURL(String url){
        this.url = url;
    }
    
    protected void setUName(String uname){
        this.uname = uname;
    }
    
    protected void setPassword(String password){
        this.password = password;
    }
    
}
