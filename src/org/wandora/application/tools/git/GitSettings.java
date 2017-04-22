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
package org.wandora.application.tools.git;

/**
 *
 * @author akikivela
 */
public class GitSettings {

    private String remotePath; // == cloneUrl
    private String localPath;
    
    private String username;
    private String password;
    
    
    public GitSettings() {
        
    }
    
    
    
    public GitSettings(String remotePath, String localPath, String username, String password) {
        this.remotePath = remotePath;
        this.localPath = localPath;
        this.username = username;
        this.password = password;
    }
    
    
    public String getRemotePath() {
        return remotePath;
    }
    
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
    
    
    
    
    public String getLocalPath() {
        return localPath;
    }
    
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    
    
    
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    

}
