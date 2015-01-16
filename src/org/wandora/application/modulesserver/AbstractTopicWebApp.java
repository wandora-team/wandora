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
 */
package org.wandora.application.modulesserver;

import java.util.Collection;
import org.wandora.application.Wandora;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.topicmap.TopicMapManager;
import org.wandora.modules.topicmap.ViewTopicAction;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public abstract class AbstractTopicWebApp extends AbstractWebApp {

    protected TopicMapManager tmManager;
    
    public Topic resolveTopic(String query){
        TopicMap tm = tmManager.getTopicMap();
        if(tm==null) return null;
        try{
            Topic t=null;
            if(query!=null) t=ViewTopicAction.getTopic(query, tm);
            else {
                Wandora wandora=Wandora.getWandora();
                if(wandora != null) {
                    t = wandora.getOpenTopic();
                    if(t == null) {
                        TopicTree tree = wandora.getCurrentTopicTree();
                        if(tree != null) {
                            t = tree.getSelection();
                        }
                        if(t == null) {
                            t = tm.getTopic(TMBox.WANDORACLASS_SI);
                        }
                    }
                }
            }
            return t;
        }catch(TopicMapException tme){
            return null;
        }
    }
    
    @Override
    public void start(ModuleManager manager) throws ModuleException {
        tmManager=manager.findModule(this,TopicMapManager.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        tmManager=null;
        super.stop(manager);
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager); 
        manager.requireModule(this,TopicMapManager.class, deps);
        return deps;
    }
    
}
