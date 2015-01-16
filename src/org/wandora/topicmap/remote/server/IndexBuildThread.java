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
 * IndexBuildThread.java
 *
 * Created on August 31, 2004, 12:40 PM
 */

package org.wandora.topicmap.remote.server;
import org.wandora.*;
import org.wandora.topicmap.remote.server.AdminSocketServer;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.PiccoloShutdownHook;
import org.wandora.piccolo.*;
import org.wandora.exceptions.*;
/**
 *
 * @author  olli
 */
public class IndexBuildThread extends Thread implements PiccoloShutdownHook {
    
    private WandoraManager manager;
    private AdminSocketServer server;
    private boolean running;
    private long openSince;
    private long delayTime;
    private Logger logger;
    
    /** Creates a new instance of IndexBuildThread */
    public IndexBuildThread(WandoraManager manager,AdminSocketServer server,long delayTime,Logger logger) {
        this.manager=manager;
        this.server=server;
        this.delayTime=delayTime;
        this.logger=logger;
        openSince=-1;
        running=true;
    }
    
    public void run(){
        if(!manager.isLazyIndexUpdate()){
            logger.writelog("WRN","Lazy update is not set, terminating updater thread.");
            return;
        }
        while(running){
            try{
                Thread.sleep(1000);
            }catch(InterruptedException ie){}
            if(!running) return;
            if(manager.indexNeedsUpdate()){
                if(server.getConnectionCount()==0){
                    if(openSince==-1) openSince=System.currentTimeMillis();
                    else{
                        if(System.currentTimeMillis()-openSince>delayTime){
                            logger.writelog("INF","Doing lazy index update.");
                            try{
                                manager.rebuildSearchIndex(true);
                            }catch(WandoraException we){
                                logger.writelog("WRN","Couldn't update index.",we);
                            }
                            openSince=-1;
                        }
                    }
                }
                else{
                    openSince=-1;
                }
            }
            else {
                openSince=-1;
            }
        }
    }
    
    public void doShutdown() {
        running=false;
        this.interrupt();
    }
    
}
