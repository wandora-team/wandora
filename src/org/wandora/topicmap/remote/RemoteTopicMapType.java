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
 * RemoteTopicMapType.java
 *
 * Created on 22. helmikuuta 2006, 17:01
 */

package org.wandora.topicmap.remote;



import org.wandora.topicmap.packageio.PackageOutput;
import org.wandora.topicmap.packageio.PackageInput;
import org.wandora.topicmap.remote.server.SocketServerInterface;
import org.wandora.utils.Options;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import javax.swing.*;
import org.wandora.application.gui.UIBox;

/**
 *
 * @author olli, aki
 */
public class RemoteTopicMapType implements TopicMapType {
    
    
    /** Creates a new instance of RemoteTopicMapType */
    public RemoteTopicMapType() {
    }
    
    
    @Override
    public String getTypeName(){
        return "Remote";
    }

    
    @Override
    public TopicMap createTopicMap(Object params){
        RemoteTopicMapConfiguration.StoredConnection sc=(RemoteTopicMapConfiguration.StoredConnection)params;
        if(sc != null) {
            try {
                int port = 8989;
                port = Integer.parseInt(sc.port);
                SocketServerInterface connection = new SocketServerInterface(sc.host, port, 30000);
                try{
                    connection.connect();
                    connection.login(sc.user, sc.pass);
                    connection.openTopicMap(sc.map);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return new RemoteTopicMap(connection,sc);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    @Override
    public TopicMap modifyTopicMap(TopicMap tm,Object params){
        return createTopicMap(params);
    }
    
    
    @Override
    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options){
        RemoteTopicMapConfiguration rc=new RemoteTopicMapConfiguration(admin, options);
        return rc;
    }
    
    
    @Override
    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora admin, Options options, TopicMap tm){
        RemoteTopicMapConfiguration rcp=new RemoteTopicMapConfiguration(admin, options);
        RemoteTopicMap rtm=(RemoteTopicMap)tm;
        RemoteTopicMapConfiguration.StoredConnection sc=(RemoteTopicMapConfiguration.StoredConnection)rtm.getConnectionParams();
        if(sc==null) return null;
        return rcp.getEditConfigurationPanel(sc);
    }    
    
    
    @Override
    public String toString(){
        return getTypeName();
    }

    
    @Override
    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) {
    }

    
    @Override
    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger,Wandora wandora) {
        return null;
    }
    
    
    @Override
    public TopicMap unpackageTopicMap(TopicMap topicmap, PackageInput in, String path, TopicMapLogger logger,Wandora wandora) {
        return null;
    }
    
    
    @Override
    public JMenuItem[] getTopicMapMenu(final TopicMap tm,Wandora admin){
        return null;
    }
    
    
    
    @Override
    public Icon getTypeIcon(){
        return UIBox.getIcon("gui/icons/layerinfo/layer_type_remote.png");
    }
}
