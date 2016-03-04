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
 *
 * 
 *
 * MapParser.java
 *
 * Created on July 9, 2004, 10:37 AM
 */

package org.wandora.piccolo;
import org.wandora.utils.XMLParamAware;
import java.util.*;
import org.wandora.utils.*;
import org.w3c.dom.*;
/**
 *
 * MapParser is an XMLParamAware object that can be used to parse Maps or Properties easily from XML parameters.
 * It takes a set of property elements each containing a key and a value element. Key and value are parsed with
 * XMLParamParser so they can be any objects. However, note that Properties maps Strings to Strings so when you
 * are using this to parse a Properties object, key and value are converted to Strings if they are not that
 * allready. Use getMap to get a Map of the parsed parameters or getProperties to get the parameters as a
 * Properties object. Note that you can place both class and method attributes in one element:
 *
 * <properties xp:class="org.wandora.piccolo.MapParser" xp:method="getProperties">
 *  <property><key>foo</key><value>bar</value></property>
 * </properties>
 *
 * @author  olli
 */
public class MapParser implements XMLParamAware,Map {
    
    private HashMap map;
    
    /** Creates a new instance of PropertiesParser */
    public MapParser() {
        map=new HashMap();
    }
    
    public Map getMap(){
        return map;
    }
    
    public Properties getProperties(){
        Properties properties=new Properties();
        Iterator iter=map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            properties.put(e.getKey().toString(),e.getValue().toString());
        }
        return properties;
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("property")){
                    NodeList nl2=e.getChildNodes();
                    Object key=null;
                    Object value=null;
                    for(int j=0;j<nl2.getLength();j++){
                        Node n2=nl2.item(j);
                        if(n2 instanceof Element){
                            Element e2=(Element)n2;
                            if(e2.getNodeName().equals("key")){
                                try{
                                    key=processor.createObject(e2);
                                }catch(Exception ex){
                                    if(processor.getObject("logger")!=null)
                                        ((Logger)processor.getObject("logger")).writelog("WRN","Exception when creating properties key. ",ex);
                                }
                            }
                            else if(e2.getNodeName().equals("value")){
                                try{
                                    value=processor.createObject(e2);
                                }catch(Exception ex){
                                    if(processor.getObject("logger")!=null)
                                        ((Logger)processor.getObject("logger")).writelog("WRN","Exception when creating properties value. ",ex);                                    
                                }
                            }
                        }
                    }
                    if(key==null || value==null){
                        // TODO: error handling
                    }
                    else{
                        map.put(key,value);
                    }
                }
            }
        }
    }
    
    public int hashCode() {
        return map.hashCode();
    }
    
    public Set entrySet() {
        return map.entrySet();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    
    public Collection values() {
        return map.values();
    }
    
    public Object put(Object key, Object value) {
        return map.put(key,value);
    }
    
    public Object remove(Object key) {
        return map.remove(key);
    }
    
    public Set keySet() {
        return map.keySet();
    }
    
    public void putAll(Map t) {
        map.putAll(t);
    }
    
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof MapParser){
            return map.equals(((MapParser)obj).getMap());
        }
        else return false;
    }
    
    public void clear() {
        map.clear();
    }
    
    public int size() {
        return map.size();
    }
    
    public Object get(Object key) {
        return map.get(key);
    }
    
}
