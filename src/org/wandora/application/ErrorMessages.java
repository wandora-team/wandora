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
 */


package org.wandora.application;

import org.wandora.exceptions.OpenTopicNotSupportedException;

/**
 * This class contains static methods to transform some common Java exceptions and errors to
 * error messages. Messages can be viewed to the user using the ErrorHandler, for example.
 *
 * @author akivela
 */


public class ErrorMessages {
    
    
    
    public static String getMessage(Throwable e) {
        String msg = "An exception has occurred in Wandora.";
        if(e != null) {
            if(e instanceof org.wandora.topicmap.TopicMapReadOnlyException) {
                msg = "Can't change current topic map because current topic map is read-only topic map. "+
                      "Check if the current topic map layer is locked and unlock the layer "+
                      "or discard changes. Topic map layer is unlocked by clicking the lock icon beside the layer name.";
            }
            else if(e instanceof org.wandora.topicmap.TopicMapException) {
                msg = "Can't perform topic map operations because of the TopicMapException. "+
                      "Look at the stacktrace for details. "+
                      "If this message appears again, we suggest you save your project project and restart Wandora application.";
            }
            else if(e instanceof java.util.regex.PatternSyntaxException) {
                msg = "Wandora uses regular expressions for search and replace. " +
                      "The given regular expression pattern contains an error: <br><br>"+
                      "<pre>"+
                      e.getMessage()+
                      "</pre><br><br>"+
                      "For more information see Java's Pattern API documentation at "+
                      "https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html";
            }
            else if(e instanceof OpenTopicNotSupportedException) {
                msg = "The topic panel can't open a topic. "+
                      "In other words, the open topic operation is not supported by the topic panel. "+
                      "You can select or create another topic panel and try again.";
            }
           
           
            
            
            else if(e instanceof java.lang.OutOfMemoryError) {
                msg = "Wandora is running out of memory. "+
                      "Save your project and restart Wandora application. "+
                      "If possible, use startup script with larger memory.";
            }
            else if(e instanceof java.lang.StackOverflowError) {
                msg = "Wandora is running out of stack memory (StackOverflowError). "+
                      "Save your project and restart Wandora application.";
            }
            else if(e instanceof java.lang.VirtualMachineError) {
                msg = "Wandora uses Java Virtual Machine for execution and the Java Virtual Machine is broken or has run out of resources necessary for it to continue operating. "+
                      "Save your project and restart Wandora application. "+
                      "If this message appears again, try to reinstall Java Virtual Machine (JRE).";
            }
            
            
            
            else {
                msg = "An error has occurred in Wandora. Error message follows:\n\n"+e.getMessage();
            }
            
            
        }
        return msg;
    }
    
    
    public static String getMessage(Throwable e, WandoraTool t) {
        String msg = getMessage(e);
        return msg;
    }
    
}
