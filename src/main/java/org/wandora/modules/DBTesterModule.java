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
package org.wandora.modules;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.wandora.modules.DatabaseInterface.PreparedDatabaseStatement;
import org.wandora.modules.DatabaseInterface.Row;
import org.wandora.modules.DatabaseInterface.Rows;

/**
 * This does some stress testing of DatabaseInterface, mainly intended
 * for debugging memory leaks.
 *
 * @author olli
 */


public class DBTesterModule extends AbstractModule {

    protected boolean usePrepared=false;
    protected PreparedDatabaseStatement prepared=null;
    
    protected DatabaseInterface database;
    
    protected Thread thread;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(this,DatabaseInterface.class, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=settings.get("useprepared");
        if(o!=null) usePrepared=Boolean.parseBoolean(o.toString());
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        database=manager.findModule(this, DatabaseInterface.class);
        
        if(usePrepared) {
            try{
                prepared=database.prepareStatement("select * from USERS,USER_PROPS,USER_ROLES where NOW()>?");
            }catch(SQLException ignore){}
        }
        
        this.isRunning=true;
        
        thread=new Thread(new Runnable(){
            @Override
            public void run() {
                threadRun();
            }
        });
        thread.start();
        
        super.start(manager);
    }
    
    protected void threadRun(){
        int queriesRan=0;
        int connections=0;
        while(this.isRunning){
            for(int i=0;i<100;i++){
                try{ Thread.sleep((int)(Math.random()*10)); } catch(InterruptedException ie){}
                try{
                    Rows rows;
                    if(prepared!=null){
                        prepared.setInt(1, (int)(Math.random()*100));
                        rows=prepared.executeQuery();
                    }
                    else rows=database.query("select * from USERS,USER_PROPS,USER_ROLES where NOW()>0");
                    queriesRan++;
                    for(Row row : rows){
                        if(row.isEmpty()) break; // doesn't really do anything but makes sure that we do something with the rows
                    }
                }catch(SQLException sqle){
                    logging.warn(sqle);
                }
            }
            
//            database.reconnect();
            connections++;
            
            logging.info("Queries ran "+queriesRan);
            logging.info("Connections "+connections);
            
            try{ Thread.sleep((int)(Math.random()*5000)); } catch(InterruptedException ie){}
        }
    }

    @Override
    public void stop(ModuleManager manager) {
        this.isRunning=false;
        if(thread!=null){
            try{ thread.join(2000); } catch(InterruptedException ie){}
        }
        thread=null;
        
        if(prepared!=null) prepared.close();
        
        database=null;
        
        super.stop(manager);
    }
    
}
