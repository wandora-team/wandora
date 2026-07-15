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
package org.wandora.modules.topicmap;

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.wandora.application.Wandora;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.ListenerList;

/**
 * <p>
 * A TopicMapManager implementation that uses a running Wandora
 * application for the topic map. This will only work if the Wandora 
 * application is running in the same virtual machine. Wandora is not a module
 * in the framework, instead it is accessed using the static Wandora.getWandora
 * method.
 * </p>
 * <p>
 * This module does not use any initialisation parameters beyond those of
 * AbstractModule.
 * </p>
 *
 * @author olli
 */


public class WandoraTopicMapManager extends AbstractModule implements TopicMapManager {

    protected final ListenerList<TopicMapManagerListener> managerListeners=new ListenerList<TopicMapManagerListener>(TopicMapManagerListener.class);
    protected ReadWriteLock tmLock=new ReentrantReadWriteLock(false);
    
    protected Wandora wandora;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        wandora=Wandora.getWandora();
        if(wandora==null) throw new ModuleException("Wandora not found");
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        wandora=null;
        
        super.stop(manager);
    }
    
    
    @Override
    public boolean lockRead() {
        tmLock.readLock().lock();
        return true;
    }

    @Override
    public void unlockRead() {
        tmLock.readLock().unlock();
    }

    @Override
    public boolean lockWrite() {
        tmLock.writeLock().lock();
        return true;
    }

    @Override
    public void unlockWrite() {
        tmLock.writeLock().unlock();
    }

    @Override
    public TopicMap getTopicMap() {
        return wandora.getTopicMap();
    }

    protected void fireTopicMapChanged(TopicMap old,TopicMap neu){
        managerListeners.fireEvent("topicMapChanged",old,neu);
    }
    
    @Override
    public void addTopicMapManagerListener(TopicMapManagerListener listener) {
        managerListeners.addListener(listener);
    }

    @Override
    public void removeTopicMapManagerListener(TopicMapManagerListener listener) {
        managerListeners.removeListener(listener);
    }
    
}
