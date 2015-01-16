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
package org.wandora.application.tools.fng.opendata.v2b;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author akivela
 */


public class FngOpenDataStruct implements FngOpenDataHandlerInterface {

    
    
    protected String[] getHandlers() {
        return new String[] {
            "http://www.muusa.net/Teos", "org.wandora.application.tools.fng.opendata.v2b.FngOpenDataArtworkHandler",
            "http://www.muusa.net/taiteilija", "org.wandora.application.tools.fng.opendata.v2b.FngOpenDataArtistHandler",
        };
    }
    
    
    
    private FngOpenDataHandlerInterface handler = null;
    
    
    @Override
    public void populate(Topic t, TopicMap tm) throws TopicMapException {
        if(t != null) {
            String[] handlers = getHandlers();
            for(int i=0; i<handlers.length; i=i+2) {
                Topic type = tm.getTopic(handlers[i]);
                if(type != null) {
                    if(t.isOfType(type)) {
                        try {
                            Class handlerClass = Class.forName(handlers[i+1]);
                            handler = (FngOpenDataHandlerInterface) handlerClass.newInstance();
                            handler.populate(t, tm);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(VttkOpenDataStruct.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InstantiationException ex) {
                            Logger.getLogger(VttkOpenDataStruct.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(VttkOpenDataStruct.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                else {
                    System.out.println("Warning! Type topic not found for "+handlers[i]);
                }
            }
            if(handler == null) {
                System.out.println("Warning. Handler not found for '"+t+"'.");
            }
        }
        else {
            System.out.println("Warning. Can't populate 'null' topic.");
        }
    }
    
    
    

    @Override
    public String toString(String outputFormat) {
        if(handler != null) {
            return handler.toString(outputFormat);
        }
        else {
            System.out.println("No handler specified. Can't stringify.");
        }
        return null;
    }
    
    
    
    
    
    public String toString(Topic t, TopicMap tm, String outputFormat) {
        try {
            if(t != null) {
                String[] handlers = getHandlers();
                for(int i=0; i<handlers.length; i=i+2) {
                    Topic type = tm.getTopic(handlers[i]);
                    if(type != null) {
                        if(t.isOfType(type)) {
                            try {
                                Class handlerClass = Class.forName(handlers[i+1]);
                                FngOpenDataHandlerInterface handler = (FngOpenDataHandlerInterface) handlerClass.newInstance();
                                handler.populate(t, tm);
                                return handler.toString(outputFormat);
                            } catch (ClassNotFoundException ex) {
                                Logger.getLogger(VttkOpenDataStruct.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InstantiationException ex) {
                                Logger.getLogger(VttkOpenDataStruct.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalAccessException ex) {
                                Logger.getLogger(VttkOpenDataStruct.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    else {
                        System.out.println("Warning! Type topic not found for "+handlers[i]);
                    }
                }
            }
        }
        catch(Exception e) {
            
        }
        return "";
    }
}
