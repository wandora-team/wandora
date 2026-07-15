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

package org.wandora.application.tools.exporters.iiifexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author olli
 */


public class JsonLD {
    protected Object unstructuredValue=null;
    protected final LinkedHashMap<String,Object> map=new LinkedHashMap<>();
    
    public JsonLD(){
    }
    
    public JsonLD(Object unstructuredValue){
        this.unstructuredValue=unstructuredValue;
    }
    
    public JsonLD append(String key,Object val){
        map.put(key, toJsonLD(val));
        return this;
    }
    public JsonLD appendNotNull(String key,Object val){
        if(val!=null) map.put(key,toJsonLD(val));
        return this;
    }
    
    public JsonLD appendNotEmpty(String key, Collection val, boolean unwrapSingle){
        if(!val.isEmpty()) append(key,val,unwrapSingle);
        return this;
    }
    public JsonLD append(String key, Collection val, boolean unwrapSingle){
        if(unwrapSingle && val.size()==1) append(key,val.iterator().next());
        else append(key,val);
        return this;
    }
        
    private Object toJsonLD(Object o){
        if(o==null) return null;
        else if(o instanceof JsonLD) return o;
        else if(o instanceof JsonLDOutput){
            return ((JsonLDOutput)o).toJsonLD();
        }
        else if(o instanceof Number) return o;
        else if(o instanceof Iterable){
            ArrayList<Object> list=new ArrayList<>();
            for(Object o2 : (Iterable)o){
                list.add(toJsonLD(o2));
            }
            return list;
        }
        else if(o instanceof Map){
            JsonLD jsonLD=new JsonLD();
            for(Map.Entry<?,?> e : ((Map<?,?>)o).entrySet()){
                jsonLD.append(e.getKey().toString(), e.getValue());
            }
            return jsonLD;
        }
        else return o.toString();
    }

    public static String jsonLDString(String s){
        return "\""+s.replace("\\", "\\\\").replace("\"","\\\"").replace("\n","\\n")+"\"";
    }    
    
    public String outputJson(boolean pretty){
        StringBuilder sb=new StringBuilder();
        outputJson(sb,pretty?"":null);
        return sb.toString();
    }
    
    public void outputJsonValue(Object val,StringBuilder sb,String indent){
        boolean pretty=(indent!=null);
        if(val==null){
            sb.append("null");
        }
        else if(val instanceof JsonLD){
            ((JsonLD)val).outputJson(sb, indent);
        }
        else if(val instanceof ArrayList){
            ArrayList l=(ArrayList)val;
            if(l.isEmpty()) sb.append("[]");
            else if(l.size()==1){ 
                sb.append("[");
                outputJsonValue(l.get(0),sb,indent);
                sb.append("]");
            }
            else {
                sb.append("[");
                boolean first=true;
                for(Object o : l){
                    if(!first) sb.append(", ");
                    else first=false;
                    
                    if(pretty) sb.append("\n").append(indent).append("\t");
                    outputJsonValue(o,sb,indent+"\t");
                }
                if(pretty) sb.append("\n").append(indent);
                sb.append("]");
            }
        }
        else if(val instanceof Number){
            sb.append(val.toString());
        }
        else {
            sb.append(jsonLDString(val.toString()));
        }
        
    }
    
    public void outputJson(StringBuilder sb,String indent){
        if(unstructuredValue!=null) {
            outputJsonValue(unstructuredValue, sb, indent);
            return;
        }
        
        boolean pretty=(indent!=null);
        sb.append("{");
        boolean first=true;
        for(Map.Entry<String,Object> e : map.entrySet() ){
            if(!first) sb.append(", ");
            else first=false;
            
            if(pretty) sb.append("\n").append(indent).append("\t");
            sb.append(jsonLDString(e.getKey()));
            sb.append(": ");
            
            outputJsonValue(e.getValue(), sb, indent+"\t");            
        }
        if(pretty && !map.isEmpty()) sb.append("\n").append(indent);
        sb.append("}");
    }
    
}
