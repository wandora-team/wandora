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



package org.wandora.application.gui.topicstringify;



import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class TopicStringifierToVariant implements TopicStringifier {
    private Set<Topic> scope = null;
    
    
    public TopicStringifierToVariant() {
        scope = null;
    }
    
    
    public TopicStringifierToVariant(Set<Topic> s) {
        scope = s;
    }
    
    
    @Override
    public boolean initialize(Wandora wandora, Context context) {
        try {
            GenericOptionsDialog god=new GenericOptionsDialog(wandora,
                "Select variant name scope to be viewed",
                "Select variant name scope to be viewed.",true,new String[][]{
                new String[]{"Type of variant name","topic","","Variant name type (display name for example)"},
                new String[]{"Scope of variant name","topic","","Variant name scope i.e. language (English for example)"},
            },wandora);
            god.setVisible(true);
            if(god.wasCancelled()) return false;

            Map<String,String> values=god.getValues();

            TopicMap tm = wandora.getTopicMap();
            Topic typeTopic = tm.getTopic(values.get("Type of variant name"));
            Topic scopeTopic = tm.getTopic(values.get("Scope of variant name"));
            
            scope = new LinkedHashSet<>();
            if(typeTopic != null) scope.add(typeTopic);
            if(scopeTopic != null) scope.add(scopeTopic);
            
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    
    @Override
    public String getDescription() {
        return "Views topic as a variant name";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/view_topic_as_variant.png");
    }
    
    
    
    @Override
    public String toString(Topic t) {
        String n = null;
        try {
            n = t.getVariant(scope);
            if(n != null) return n;
        }
        catch(Exception e) { }
        
        try {
            if(t.getBaseName() == null) {
                return "["+t.getOneSubjectIdentifier().toExternalForm()+"]";
            }
            else { 
                return "["+t.getBaseName()+"]";
            }
        }
        catch(Exception e) { }
        return "[unable to solve name for a topic]";
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean supportsStringIntoTopic() {
        return true;
    }
    
    
    @Override
    public void stringIntoTopic(String oldString, String newString, Topic t) throws TopicMapException {
        if(t != null && !t.isRemoved()) {
            if(scope != null && !scope.isEmpty()) {
                t.setVariant(scope, newString);
            }
        }
    }
    

}
