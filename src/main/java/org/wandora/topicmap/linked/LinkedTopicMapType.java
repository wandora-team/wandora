/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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

package org.wandora.topicmap.linked;
import org.wandora.topicmap.packageio.PackageOutput;
import org.wandora.topicmap.packageio.PackageInput;
import java.io.*;
import javax.swing.JMenuItem;
import org.wandora.application.Wandora;
import org.wandora.topicmap.*;
import javax.swing.Icon;
import org.wandora.application.gui.UIBox;
import org.wandora.utils.Options;
import org.wandora.topicmap.layered.*;

/**
 *
 * @author olli
 */
public class LinkedTopicMapType implements TopicMapType {

    
    @Override
    public TopicMap createTopicMap(Object params) throws TopicMapException {
        if(params instanceof TopicMap) {
            return new LinkedTopicMap((TopicMap)params);
        }
        else {
            return new LinkedTopicMap(params.toString());
        }
    }

    
    @Override
    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options) {
        LinkedTopicMapConfiguration c = new LinkedTopicMapConfiguration(admin);
        return c;
    }

    
    
    @Override
    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora wandora, Options options, TopicMap tm) {
        TopicMap linked = ((LinkedTopicMap)tm).getLinkedTopicMap();
        ContainerTopicMap root = ((ContainerTopicMap)tm.getRootTopicMap());
        Layer l = root.getTreeLayer(linked);
        String name = l.getName();
        LinkedTopicMapConfiguration c = new LinkedTopicMapConfiguration(wandora, tm);
        c.setSelectedLayer(name);
        return c;        
    }

    
    
    @Override
    public JMenuItem[] getTopicMapMenu(TopicMap tm, Wandora admin) {
        return null;
    }

    
    
    @Override
    public String toString(){
        return getTypeName();
    }
    
    
    
    @Override
    public String getTypeName() {
        return "Linked";
    }

    
    
    @Override
    public TopicMap modifyTopicMap(TopicMap tm, Object params) throws TopicMapException {
        TopicMap ret=createTopicMap(params);
        ret.addTopicMapListeners(tm.getTopicMapListeners());
        return ret;
    }

    
    
    @Override
    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) throws IOException, TopicMapException {
        LinkedTopicMap ltm = (LinkedTopicMap) tm;
        TopicMap linked = ltm.getLinkedTopicMap();
        ContainerTopicMap rootTopicMap = (ContainerTopicMap) ltm.getRootTopicMap();
        Layer l = rootTopicMap.getTreeLayer(linked);
        Options options = new Options();
        options.put("linkedmap",l.getName());
        if(rootTopicMap.getTreeLayer(l.getName()) == null) {
            throw new TopicMapException("Target of linked topic map does not exist. Target layer name is "+l.getName());
        }
        out.nextEntry(path,"linkedoptions.xml");
        options.save(new java.io.OutputStreamWriter(out.getOutputStream()));        
    }

    
    
    @Override
    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger, Wandora wandora) throws IOException, TopicMapException {
        in.gotoEntry(path, "linkedoptions.xml");
        Options options = new Options();
        options.parseOptions(new BufferedReader(new InputStreamReader(in.getInputStream())));
        String wrappedMap = options.get("linkedmap");
        if(wrappedMap == null) {
            // used to be wrappedmap before 2013-07-18
            wrappedMap=options.get("wrappedmap");
        } 
        return new LinkedTopicMap(wrappedMap);
    }

    
    
    @Override
    public TopicMap unpackageTopicMap(TopicMap tm, PackageInput in, String path, TopicMapLogger logger, Wandora wandora) throws IOException, TopicMapException {
        return unpackageTopicMap(in,path,logger,wandora);
    }
    
    
    
    @Override
    public Icon getTypeIcon() {
        //return UIBox.getIcon("gui/icons/layerinfo/layer_type_linked.png");
        return UIBox.getIcon(0xf0c1);
    }

}
