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
 * SimpleNamespaceContext.java
 * 
 */



package org.wandora.utils;

import javax.xml.namespace.NamespaceContext;
import java.util.*;
import javax.xml.XMLConstants;
/**
 *
 * @author olli
 */
public class SimpleNamespaceContext implements NamespaceContext {

    protected HashMap<String,String> map;
    protected HashMap<String,ArrayList<String>> inverse;
    
    public SimpleNamespaceContext(){
        map=new HashMap<String,String>();
        inverse=new HashMap<String,ArrayList<String>>();
        setPrefix(XMLConstants.XML_NS_PREFIX,XMLConstants.XML_NS_URI);
        setPrefix(XMLConstants.XMLNS_ATTRIBUTE,XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    public void setPrefix(String prefix,String namespaceURI){
        map.put(prefix, namespaceURI);
        ArrayList<String> l=inverse.get(namespaceURI);
        if(l==null) {
            l=new ArrayList<String>();
            inverse.put(namespaceURI, l);
        }
        l.add(prefix);
    }

    public String getNamespaceURI(String prefix) {
        if(prefix==null) throw new IllegalArgumentException();
        return map.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        if(namespaceURI==null) throw new IllegalArgumentException();
        Iterator<String> iter=getPrefixes(namespaceURI);
        if(iter.hasNext()) return iter.next();
        else return null;
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
        if(namespaceURI==null) throw new IllegalArgumentException();
        ArrayList<String> l=inverse.get(namespaceURI);
        if(l==null) return new ArrayList<String>().iterator();
        else return l.iterator();
    }

}
