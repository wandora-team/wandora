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
 * 
 * TopicGuiWrapper.java
 *
 * Created on 14. lokakuuta 2005, 14:54
 */

package org.wandora.application.gui;


import org.wandora.topicmap.*;
import javax.swing.tree.*;
import org.wandora.application.gui.topicstringify.TopicToString;

/**
 *
 * @author olli, akivela
 */
public class TopicGuiWrapper {

    public static final String PROCESSING_TYPE = "PROCESSING_TYPE";
    
    public Topic topic;
    public String icon;
    public String associationType;
    public TreePath path;
    
    
    
    
    public TopicGuiWrapper(Topic t, String icon, String associationType, TreePath parent) {
        this.topic=t;
        this.icon=icon;
        this.associationType=associationType;
        if(parent==null) path=new TreePath(this);
        else path=parent.pathByAddingChild(this);
    }
    public TopicGuiWrapper(Topic t, String icon) {
        this(t,icon,"",null);
    }
    public TopicGuiWrapper(Topic t) {
        this(t,null,"",null);
    }
    
    
    @Override
    public String toString() {
        try {
            if(PROCESSING_TYPE.equals(associationType)) {
                return "Processing...";
            }
            else {
                return TopicToString.toString(topic);
            }
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION
            return "[Exception retrieving name]";
        }
    }
    
    /*
    public String toString(int stringType) throws TopicMapException {
        try {
            if(PROCESSING_TYPE.equals(associationType)) {
                return "Processing...";
            }
            else {
                return TopicToString.toString(topic, stringType);
            }
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION
            return "[Exception retrieving name]";
        }
    }
     * 
     */
   
}
