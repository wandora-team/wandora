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
 * FileServerService.java
 *
 * Created on 1. elokuuta 2006, 11:46
 *
 */

package org.wandora.piccolo.services;
import org.wandora.utils.fileserver.VirtualFileSystem;
import org.wandora.utils.fileserver.SimpleFileServer;
import org.wandora.utils.fileserver.*;
import org.wandora.piccolo.*;
/**
 *
 * @author olli
 */
public class FileServerService extends SimpleFileServer implements PiccoloShutdownHook, Service {
    
    public FileServerService(int port,VirtualFileSystem fileSystem) {
        super(port,fileSystem);
    }
    public FileServerService(String port,VirtualFileSystem fileSystem,String useSSL,String credentials) {
        super(port,fileSystem,useSSL,credentials);
    }
    public FileServerService(int port,VirtualFileSystem fileSystem,boolean useSSL,String credentials) {
        super(port,fileSystem,useSSL,credentials);        
    }
    
    public void doShutdown(){
        stopServer();
    }
    
    public String getServiceType(){
        return "FileServer";
    }
    public String getServiceName(){
        return "SimpleFileServer";
    }
    
}
