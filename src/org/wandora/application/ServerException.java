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
 *
 * 
 * 
 * ServerException.java
 *
 * Created on September 8, 2004, 11:19 AM
 */

package org.wandora.application;

/**
 * This class is mostly used by the old remote topic map implementation. However,
 * it is not completely deprecated as it is also used by some tools that perform
 * actions that previously were an integral part of remote topic maps but are
 * now separate tools. These tools are related to uploading files to a remote
 * server.
 *
 * @author  olli
 */
public class ServerException extends Exception {
    
	private static final long serialVersionUID = 1L;
	
	
    /** Creates a new instance of ServerException */
    public ServerException(){
        super();
    }
    public ServerException(String message) {
        super(message);
    }
    public ServerException(Throwable cause){
        super(cause);
    }
    public ServerException(String message,Throwable cause){
        super(message,cause);
    }
    
}
