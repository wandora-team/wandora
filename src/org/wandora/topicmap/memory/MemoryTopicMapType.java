/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * MemoryTopicMapType.java
 *
 * Created on 24. marraskuuta 2005, 13:41
 */

package org.wandora.topicmap.memory;
import org.wandora.utils.Options;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.utils.*;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author olli
 */
public class MemoryTopicMapType implements TopicMapType {
    
    /** Creates a new instance of MemoryTopicMapType */
    public MemoryTopicMapType() {
    }
    @Override
    public String getTypeName(){
        return "Memory";
    }
    
    @Override
    public TopicMap createTopicMap(Object params) throws TopicMapException {
        TopicMapImpl tm=new TopicMapImpl();
        if(params instanceof String && params != null) {
            String load = (String) params;
            if(MemoryConfiguration.LOAD_MINI_SCHEMA_PARAM.equals(params)) {
                load="resources/conf/wandora_mini.xtm";
            }
            if(load!=null && load.length()>0) {
                try {
                    tm.importXTM(load);
                }
                catch(java.io.IOException ioe){
                    ioe.printStackTrace(); // TODO
                }
            }
        }
        return tm;
    }
    
    @Override
    public TopicMap modifyTopicMap(TopicMap tm,Object params) throws TopicMapException {
        return tm;
    }
    
    @Override
    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options){
        MemoryConfiguration mc=new MemoryConfiguration(admin);
        return mc;
    }
    @Override
    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora admin, Options options, TopicMap tm){
        return null;
    }    
    
    @Override
    public String toString(){return getTypeName();}

    @Override
    public void packageTopicMap(TopicMap tm, PackageOutput out, String path, TopicMapLogger logger) throws java.io.IOException,TopicMapException {
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        out.nextEntry(pathpre+"topicmap.xtm");
        tm.exportXTM(out.getOutputStream(), logger);
    }

    @Override
    public TopicMap unpackageTopicMap(PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws java.io.IOException,TopicMapException {
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        TopicMapImpl tm=new TopicMapImpl();
        boolean found=in.gotoEntry(pathpre+"topicmap.xtm");
        if(!found){
            logger.log("Couldn't find topicmap file '"+pathpre+"topicmap.xtm'.");
            return null;
        }
        tm.importXTM(in.getInputStream(), logger);
        return tm;
    }
    @Override
    public TopicMap unpackageTopicMap(TopicMap topicmap, PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws java.io.IOException,TopicMapException {
        if(topicmap == null) topicmap = new TopicMapImpl(); 
        String pathpre="";
        if(path.length()>0) pathpre=path+"/";
        boolean found=in.gotoEntry(pathpre+"topicmap.xtm");
        if(!found){
            logger.log("Couldn't find topicmap file '"+pathpre+"topicmap.xtm'.");
            return null;
        }
        topicmap.importXTM(in.getInputStream(), logger);
        return topicmap;
    }
    @Override
    public javax.swing.JMenuItem[] getTopicMapMenu(TopicMap tm,Wandora admin){
        return null;
    }
    @Override
    public String getTypeIcon(){
        return "gui/icons/layerinfo/layer_type_memory.png";
    }
    
}
