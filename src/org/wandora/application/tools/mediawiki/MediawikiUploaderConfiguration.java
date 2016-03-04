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
 */



package org.wandora.application.tools.mediawiki;

import java.net.URL;

/**
 *
 * @author nlaitine
 */


public class MediawikiUploaderConfiguration {
    private String wikiUrl = null;
    private String wikiUser = null;
    private String wikiPasswd = null;
    private URL wikiFileUrl = null;
    private String wikiFilename = null;
    private String wikiFileExtension = null;
    private String wikiDescription = null;
    private boolean wikiStream = false;
    
    public MediawikiUploaderConfiguration() { }
    
    public boolean setWikiUrl(String wikiUrl) {
        this.wikiUrl = wikiUrl;
        return true;
    }
    
    public String getWikiUrl() {
        return wikiUrl;
    }
    
    public boolean setWikiUser(String wikiUser) {
        this.wikiUser = wikiUser;
        return true;
    }
    
    public String getWikiUser() {
        return wikiUser;
    }
    
    public boolean setWikiPasswd(String wikiPasswd) {
        this.wikiPasswd = wikiPasswd;
        return true;
    }
    
    public String getWikiPasswd() {
        return wikiPasswd;
    }
    
    public boolean setWikiFileUrl(URL wikiFileUrl) {
        this.wikiFileUrl = wikiFileUrl;
        return true;
    }
    
    public URL getWikiFileUrl() {
        return wikiFileUrl;
    }
    
    public boolean setWikiFilename(String wikiFilename) {
        this.wikiFilename = wikiFilename;
        return true;
    }
    
    public String getWikiFilename() {
        return wikiFilename;
    }
    
    public boolean setWikiFileExtension(String fileExtension) {
        this.wikiFileExtension = fileExtension;
        return true;
    }
    
    public String getWikiFileExtension() {
        return this.wikiFileExtension;
    }
    
    public boolean setWikiDescription(String wikiDescription) {
        this.wikiDescription = wikiDescription;
        return true;
    }
    
    public String getWikiDescription() {
        return wikiDescription;
    }
    
    public boolean setWikiStream(boolean wikiStream) {
        this.wikiStream = wikiStream;
        return true;
    }
    
    public boolean getWikiStream() {
        return wikiStream;
    }
}
