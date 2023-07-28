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
package org.wandora.application.tools.exporters.simberg;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import org.wandora.application.tools.exporters.simberg.ModelField.Type;

/**
 *
 * @author olli
 */


public class ModelTools {
    private static void scanData(ModelTopic topic,LinkedHashMap<ModelTopic,Integer> topicIndex,LinkedHashMap<ModelClass,Integer> classIndex){
        if(topic==null) return;
        if(topicIndex.containsKey(topic)) return;
        
        topicIndex.put(topic, topicIndex.size());
        ModelClass cls=topic.getCls();
        if(!classIndex.containsKey(cls)){
            classIndex.put(cls,classIndex.size());
        }
        for(ModelField field : cls.getFields()){
            Object o=topic.getField(field);
            if(o==null) continue;
            
            Type type=field.getType();
            if(type==Type.Topic){
                scanData((ModelTopic)o, topicIndex, classIndex);
            }
            else if(type==Type.TopicList){
                for(ModelTopic t : (List<ModelTopic>)o){
                    scanData(t, topicIndex, classIndex);
                }
            }
        }
    }
    private static void scanData(Collection<ModelTopic> topics,LinkedHashMap<ModelTopic,Integer> topicIndex,LinkedHashMap<ModelClass,Integer> classIndex){
        for(ModelTopic t : topics){
            scanData(t, topicIndex, classIndex);
        }
    }
    
    private static String escapeJSON(String s){
        return s.replace("\\", "\\\\").replace("\"","\\\"");
    }
    
    public static void exportJSON(Collection<ModelTopic> topics,Writer out) throws IOException {
        LinkedHashMap<ModelTopic,Integer> topicIndex=new LinkedHashMap<ModelTopic,Integer>();
        LinkedHashMap<ModelClass,Integer> classIndex=new LinkedHashMap<ModelClass,Integer>();
        scanData(topics,topicIndex,classIndex);
        
        out.write("imagesJsonpCallback({\n");
        
        out.write("\"classes\":[\n");
        boolean first=true; 
        for(ModelClass cls : classIndex.keySet()){
            if(!first) out.write(",\n");
            out.write("[\""+escapeJSON(cls.getName())+"\"");
            for(ModelField field : cls.getFields()){
                out.write(",");
                out.write("[\""+escapeJSON(field.getName())+"\",\""+field.getType().toString()+"\"]");
            }
            out.write("]");
            first=false;
        }
        out.write("\n],\n");

        out.write("\"topics\":[\n");
        first=true;
        for(ModelTopic topic : topicIndex.keySet()){
            if(!first) out.write(",\n");
            out.write("[");
            out.write(""+classIndex.get(topic.getCls()));
            for(ModelField field : topic.getCls().getFields()){
                out.write(","); // the class index is always first, no need for the flag varliable
                Type type=field.getType();
                Object o=topic.getField(field);
                if(o==null){
                    out.write("null");
                }
                else if(type==Type.String){
                    out.write("\""+escapeJSON(o.toString())+"\"");
                }
                else if(type==Type.StringList){
                    out.write("[");
                    first=true;
                    for(String s : (List<String>)o){
                        if(!first) out.write(",");
                        first=false;
                        out.write("\""+escapeJSON(s)+"\"");
                    }
                    out.write("]");
                }
                else if(type==Type.Topic){
                    out.write(""+topicIndex.get((ModelTopic)o));
                }
                else if(type==Type.TopicList){
                    out.write("[");
                    first=true;
                    for(ModelTopic t : (List<ModelTopic>)o){
                        if(!first) out.write(",");
                        first=false;
                        out.write(""+topicIndex.get(t));
                    }
                    out.write("]");                    
                }
                else throw new RuntimeException("Unknown field type "+type);
            }
            out.write("]");
            first=false;
        }
        out.write("\n],\n");
        
        out.write("});");
    }
}
