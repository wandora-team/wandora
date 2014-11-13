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
 */


package org.wandora.application;

/**
 * This class contains static methods to transform some common Java exceptions and errors to
 * error messages. Messages can be viewed to the user using the ErrorHandler, for example.
 *
 * @author akivela
 */


public class ErrorMessages {
    
    
    
    public static String getMessage(Exception e) {
        String msg = "An exception has occurred in Wandora.";
        if(e != null) {
            if(e instanceof org.wandora.topicmap.TopicMapReadOnlyException) {
                msg = "Can't change current topic map because current topic map is read-only topic map. "+
                      "Check if the current topic map layer is locked and unlock the layer "+
                      "or discard changes. Topic map layer is unlocked by clicking the lock icon beside the layer name.";
            }
            else {
                msg = "An exception has occurred in Wandora. Exception message follows:\n\n"+e.getMessage();
            }
        }
        return msg;
    }
    
    
    public static String getMessage(Exception e, WandoraTool t) {
        String msg = getMessage(e);
        return msg;
    }

    
    public static String getMessage(Error e) {
        String msg = "An error has occurred in Wandora.";
        if(e != null) {
            if(e instanceof java.lang.OutOfMemoryError) {
                msg = "Wandora is running out of memory. "+
                      "Save your project and restart Wandora application. "+
                      "If possible, use startup script with larger memory.";
            }
            else {
                msg = "An error has occurred in Wandora. Error message follows:\n\n"+e.getMessage();
            }
        }
        return msg;
    }
    
    
    public static String getMessage(Error e, WandoraTool t) {
        String msg = getMessage(e);
        return msg;
    }
}
