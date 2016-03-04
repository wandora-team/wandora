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
package org.wandora.application.modulesserver;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wandora.modules.Module;
import org.wandora.modules.ScopedModuleManager;

/**
 * An extension of the ScopedModuleManager, meant to represent a
 * bundle of modules. Currently the only added feature is reading the bundle
 * name from an element named bundleName (note, not initialisation parameter but
 * an actual &lt;bundleName> element).
 * 
 * @author olli
 */


public class ModuleBundle extends ScopedModuleManager {

    protected String bundleName;
    
    public String getBundleName(){
        return bundleName;
    }
    
    public void setBundleName(String s){
        bundleName=s;
    }
    
    @Override
    public Collection<Module> parseXMLConfigElement(Node doc, String source) {
        try{
            ArrayList<Module> ret=new ArrayList<Module>();
            XPath xpath=XPathFactory.newInstance().newXPath();
            
            NodeList nl=(NodeList)xpath.evaluate("//bundleName",doc,XPathConstants.NODESET);
            if(nl.getLength()>0){
                Element e2=(Element)nl.item(0);
                String s=e2.getTextContent().trim();
                if(s.length()>0) setBundleName(s);
            }
            return super.parseXMLConfigElement(doc, source);
        }catch(Exception ex){
            if(log!=null) log.error("Error reading options file", ex);
            return null;
        }
    }


    
}
