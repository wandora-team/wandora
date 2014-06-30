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
 *
 * 
 *
 * XMLProperties.java
 *
 * Created on 24. tammikuuta 2007, 13:02
 *
 */

package org.wandora.piccolo;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.XMLParamProcessor;
import java.util.*;
import org.wandora.utils.*;
import org.w3c.dom.*;

/**
 * A <code>XMLParamAware</code> class extending <code>java.util.Properties</code>.
 * With this class you can easily create properties objects in xml configuration
 * files parsed with <code>XMLParamProcessor</code>. The xml element defining
 * <code>XMLProperties</code> class can have any number of "properties" elements,
 * each should contain a "key" and a "value" element. The key and the value of
 * each property are parsed with the <code>XMLParamProcessor</code> individually.
 *
 * @see XMLParamProcessor
 * @see XMLParamAware
 * @author olli
 */
public class XMLProperties extends Properties implements XMLParamAware {
    
    /** Creates a new instance of XMLProperties */
    public XMLProperties() {
        super();
    }
    
    public XMLProperties(Properties defaults){
        super(defaults);
    }

    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
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
                        this.put(key,value);
                    }
                }
            }
        }
    }
    
}
