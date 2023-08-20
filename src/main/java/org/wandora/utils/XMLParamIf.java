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
 * 
 * XMLParamIf.java
 *
 * Created on September 9, 2004, 1:51 PM
 */

package org.wandora.utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * This class allows you to make conditional statements in xml option files parsed
 * with XMLParamProcessor. This class is XMLParamAware and has its own parsing
 * of content elements. To use this class, do something along the lines of
 * </p>
 * <p><pre>
 *   &lt;if xp:class="com.gripstudios.utils.XMLParamIf" xp:method="getObject">
 *     &lt;if xp:idref="someObject"/>
 *     &lt;then>true value&lt;/then>
 *     &lt;else>false value&lt;/else>
 *   &lt;/if>
 * </pre></p>
 * <p>
 * If the object returned by if element is non null, a Boolean with value true or
 * a non boolean object, then the object then element evaluates to is returned by
 * getObject. Otherwise the object else element evaluates to is returned. If there
 * is no else element, then it returns null.
 * </p>
 *
 * @author  olli
 */
public class XMLParamIf implements XMLParamAware {
    private Object object;
    public XMLParamIf(){
    }

    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        NodeList nl=element.getChildNodes();
        Element test=null,then=null,els=null;
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("if")){
                    test=e;
                }else if(e.getNodeName().equals("then")){
                    then=e;
                }else if(e.getNodeName().equals("else")){
                    els=e;
                }
            }
        }
        try{
            Object o=processor.createObject(test);
            if(o!=null && ( !(o instanceof Boolean) || ((Boolean)o).booleanValue() ) ){
                object=processor.createObject(then);
            }
            else if(els!=null){
                object=processor.createObject(els);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Object getObject(){
        return object;
    }
}
