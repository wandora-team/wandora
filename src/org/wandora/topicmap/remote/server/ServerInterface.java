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
 *
 * ServerInterface.java
 *
 * Created on June 8, 2004, 11:23 AM
 */

package org.wandora.topicmap.remote.server;
import org.wandora.application.*;
import org.wandora.exceptions.WandoraException;
import org.wandora.*;
import org.wandora.topicmap.*;
import java.util.*;
/**
 * This is the interface for the remote server used by the old remote topic map
 * implementation. Remote topic maps are not used anymore and this class should
 * be considerede deprecated.
 *
 * @author  olli
 */
public interface ServerInterface {
    
//    public void clearCache();
    
    public Topic getTopic(String topicSI) throws ServerException;
    public Topic[] getTopics(String[] topicSIs) throws ServerException;
    public Topic[] getTopicsOfType(String typeSI) throws ServerException;
    public Topic getTopicByName(String name) throws ServerException;

    public void checkTopics(TopicMap tm) throws ServerException;
    
    public void removeTopic(Topic t) throws ServerException;
    public void removeAssociation(Association a) throws ServerException;
    public void removeBaseName(Topic t) throws ServerException;
    public void removeVariantName(Topic t,Collection scope) throws ServerException;
    public void removeData(Topic t,Topic type,Topic version) throws ServerException;
    public void removeSubjectLocator(Topic t) throws ServerException;
    public void removeTopicType(Topic t,Topic type) throws ServerException;
    public void removeSubjectIdentifier(Topic t,Locator l) throws ServerException;
    public boolean writeTopicMapTo(java.io.OutputStream out) throws java.io.IOException,ServerException ;
    
//    public boolean removeTopicAllowed(Topic t);
    public boolean isUncommitted() throws ServerException;
    
    public void mergeIn(TopicMap tm) throws ServerException;
    
    public void commit(boolean force) throws WandoraException,ServerException ;
    public void rollback() throws ServerException;    
    
    public boolean gzip() throws ServerException;
    public boolean cipher() throws ServerException;
    public boolean login(String user,String password) throws ServerException;
    public boolean needLogin() throws ServerException;
    
    public void close() throws ServerException;
    public void connect() throws java.io.IOException;
    
    public String[] search(String query) throws ServerException;
    
    public boolean openTopicMap(String key) throws ServerException;
    
    public void clearCache() throws ServerException;
    
    public boolean clearTopicMap() throws ServerException;
    
    public void writelog(String lvl,String msg) throws ServerException;
    
    public String customCommand(String command) throws ServerException;
    
    public String upload(java.io.InputStream in,String filename,long length,boolean overwrite) throws ServerException;
    public String[] listDirectories(String dir) throws ServerException;
    public String[] listFiles(String dir) throws ServerException;
    public boolean fileExists(String file) throws ServerException;
    public boolean delete(String filename) throws ServerException;
    
    public boolean isConnected();
    
    public StringBuffer getSession();
    public void applySession(StringBuffer session) throws ServerException;
    public void setServerErrorHandler(ErrorHandler handler);
    public void handleServerError(Exception e);
}
