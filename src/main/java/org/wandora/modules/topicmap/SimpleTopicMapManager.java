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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.topicmap.SimpleTopicMapLogger;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapType;
import org.wandora.topicmap.TopicMapTypeManager;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.topicmap.memory.TopicMapImpl;
import org.wandora.topicmap.packageio.PackageOutput;
import org.wandora.topicmap.packageio.ZipPackageOutput;
import org.wandora.utils.ListenerList;

/**
 * <p>
 * Basic implementation of the topic map manager. Simply loads the topic
 * map from a file and gives direct access to that. The file can either be an
 * xtm file or a Wandora project file. Optionally, can be set to save the
 * topic map at regular intervals, or alternatively to reload the topic map file
 * at regular intervals.
 * </p>
 * <p>
 * The topic map file is set with the initialisation parameter topicMapFile.
 * Alternatively you can also set the topic map directly by giving a topic map
 * object in the parameter topicMap. In this case the auto save and reload features
 * cannot be used.
 * </p>
 * <p>
 * Auto saving is set with the autoSave initialisation parameter. The value is
 * the saving interval in minutes. Use 0 to disable auto saving, which is the
 * default. Setting this parameter to "true" enables auto saving every 10 minutes.
 * Note that the topic map will be written to disk whether or not actual changes
 * in it have occurred.
 * </p>
 * <p>
 * Automatic file reloading can be set with autoRefresh parameter, set it to true
 * to enable automatic reloading. The topic map is reloaded whenever its
 * timestamp changes, this is checked roughly once every minute.
 * </p>
 * <p>
 * You should not use both auto saving and reloading at the same time. Or in general,
 * you should not use auto saving at all if you intend to modify the file while
 * this module is running. This can very easily lead to a situation where this module
 * overwrites your changes.
 * </p>
 *
 * @author olli
 */


public class SimpleTopicMapManager extends AbstractModule implements TopicMapManager {

    protected final ListenerList<TopicMapManagerListener> managerListeners=new ListenerList<TopicMapManagerListener>(TopicMapManagerListener.class);
    protected ReadWriteLock tmLock=new ReentrantReadWriteLock(false);
    
    protected TopicMap tm;
    
    protected String topicMapFile=null;
    
    protected String autoSaveFile=null;
    protected boolean autoRefresh=false;
    protected int autoSave=0;
    protected long lastSave=0;
    protected long lastLoad=0;
    
    protected boolean autoThreadRunning=false;
    protected Thread autoThread=null;
    protected final Object autoThreadWait=new Object();

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        Object o;
        o=settings.get("topicMap");
        if(o!=null){
            if(o instanceof TopicMap) tm=(TopicMap)o;
            else topicMapFile=o.toString();
        }
        
        o=settings.get("topicMapFile");
        if(o!=null) topicMapFile=o.toString();
        
        o=settings.get("autoRefresh");
        if(o!=null) {
            if(o.toString().equalsIgnoreCase("true")) autoRefresh=true;
            else autoRefresh=false;
        }
        
        o=settings.get("autoSave");
        if(o!=null) {
            try{
                int i=Integer.parseInt(o.toString());
                autoSave=i;
            }
            catch(NumberFormatException nfe){
                if(o.toString().equalsIgnoreCase("true")) autoSave=10;
                else autoSave=0;
            }
        }
        
        o=settings.get("autoSaveFile");
        if(o!=null) autoSaveFile=o.toString();
        else if(autoSave>0) throw new ModuleException("autoSaveFile parameter is required with auto save.");
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if((tm==null || autoRefresh) && topicMapFile!=null){
            reloadTopicMap();
        }
        
        if(autoRefresh || autoSave>0){
            startAutoThread();
        }
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        this.lockWrite();
        try {
            this.autoThreadRunning=false;
            if(autoThread!=null){
                synchronized(autoThreadWait){
                    autoThreadWait.notify();
                }
                try{
                    autoThread.join();
                }catch(InterruptedException e){}
            }
            autoThread=null;
            
            if(autoSave>0){
                this.saveTopicMap();
            }
        }finally{ this.unlockWrite(); }
        
        super.stop(manager);
    }
    
    protected void startAutoThread(){
        assert autoThread==null;
        
        lastSave=System.currentTimeMillis();
        autoThreadRunning=true;
        autoThread=new Thread(new Runnable(){
            @Override
            public void run() {
                autoThreadRun();
            }
        });
        autoThread.start();
    }
    
    protected void autoThreadRun() {
        while(autoThreadRunning){
            synchronized(autoThreadWait){
                try{
                    autoThreadWait.wait(60000);
                }catch(InterruptedException e){}
            }
            if(!autoThreadRunning) return;
            
            if(autoSave>0 && System.currentTimeMillis()>lastSave+60000l*(long)autoSave){
                this.saveTopicMap();
            }            
            else if(autoRefresh){
                File f=new File(topicMapFile);
                if(f.exists() && f.lastModified()>lastLoad) {
                    this.reloadTopicMap();
                }
            }
        }
    }
    
    public void saveTopicMap(){
        lastSave=System.currentTimeMillis();
        lockRead();
        try{
            if(tm instanceof LayerStack && autoSaveFile.toLowerCase().endsWith(".wpr")){
                TopicMapType tmtype=TopicMapTypeManager.getType(tm);
                PackageOutput out=new ZipPackageOutput(new FileOutputStream(autoSaveFile));
                tmtype.packageTopicMap(tm,out,"",new SimpleTopicMapLogger(new PrintStream(new OutputStream(){
                    @Override public void write(byte[] b) throws IOException {}
                    @Override public void write(byte[] b, int off, int len) throws IOException {}
                    @Override public void write(int b) throws IOException {}
                })));  
                out.close();
            }
            else {
                tm.exportXTM(autoSaveFile);
            }
        }
        catch(TopicMapException tme){
            logging.error("Unable to autosave topic map",tme);
        }
        catch(IOException ioe){
            logging.error("Unable to autosave topic map",ioe);
        }
        finally{ unlockRead(); }
    }
    
    public void reloadTopicMap(){
        TopicMap oldTm=tm;
        lastLoad=System.currentTimeMillis();
        lockWrite();
        try{
            if(topicMapFile.toLowerCase().endsWith(".wpr")){
                LayerStack ls=new LayerStack(topicMapFile);
                tm=ls;
            }
            else {
                TopicMap tmNew=new TopicMapImpl(topicMapFile);
                tm=tmNew;
            }
        }
        finally{ unlockWrite(); }
        fireTopicMapChanged(oldTm, tm);
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
        return tm;
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
