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
 * WandoraAdminManager.java
 *
 * Created on June 8, 2004, 12:33 PM
 */

package org.wandora.application;



import org.wandora.topicmap.*;






/**
 * <p>
 * WandoraAdminManager is a deprecated class used wrap TopicMap and
 * topic map related services in Wandora.
 * </p>
 * 
 * @deprecated
 * @author  olli, ak
 */
public class WandoraAdminManager  {
    

    private Wandora admin;
    
    
    
    public WandoraAdminManager(Wandora admin) throws java.io.IOException,TopicMapException {
        this.admin = admin;
    }
    

    
    /** @deprecated */
    public TopicMap getWorkspace(){
        return admin.getTopicMap();
    }
    
    
    /** @deprecated */
    public String upload(java.io.InputStream in,String filename,long length) throws ServerException {
        return upload(in,filename,length,false);
    }
    /** @deprecated */
    public String upload(java.io.InputStream in,String filename,long length,boolean overwrite) throws ServerException{
        return null;
    }
    /** @deprecated */
    public String[] listDirectories(String dir) throws ServerException{
        return null;
    }
    /** @deprecated */
    public String[] listFiles(String dir) throws ServerException{
        return null;
    }
    /** @deprecated */
    public boolean fileExists(String file) throws ServerException{
        return false;
    }
    /** @deprecated */
    public boolean deletefile(String filename) throws ServerException{
        return false;
    }    
    

}
