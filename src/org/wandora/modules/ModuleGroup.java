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
 */
package org.wandora.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.script.ScriptException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wandora.utils.Tuples;

/**
 *
 * @Deprecated Untested and probably unneeded. ScopedModuleManager can be used
 * to group modules.
 * @author olli
 */


public class ModuleGroup extends ScriptModule implements XMLOptionsHandler {
    protected final ArrayList<Module> childModules=new ArrayList<Module>();
    
    public ModuleGroup(){
        
    }
    
    public void startGroup(boolean withDependencies) throws ModuleException {
        synchronized(childModules){
            for(Module m : childModules){
                if(m instanceof ModuleGroup){
                    ((ModuleGroup)m).startGroup(withDependencies);
                }
                else {
                    if(withDependencies) moduleManager.startModuleWithDependencies(m);
                    else moduleManager.startModule(m);
                }
            }
        }
    }
    
    public void stopGroup(boolean cascading) throws ModuleException {
        synchronized(childModules){
            for(Module m : childModules){
                if(m instanceof ModuleGroup){
                    ((ModuleGroup)m).stopGroup(cascading);
                }
                else {
                    if(cascading) moduleManager.stopCascading(m);
                    else moduleManager.stopModule(m);
                }
            }
        }
        
    }
    
    public void addChildModule(Module m){
        synchronized(childModules){
            childModules.add(m);
        }
    }
    
    public Collection<Module> getChildModules(){
        synchronized(childModules){
            return new ArrayList<Module>(childModules);
        }
    }
    
    public Collection<Module> getChildModulesRecursive(){
        synchronized(childModules){
            ArrayList<Module> ret=new ArrayList<Module>();
            for(Module m : childModules){
                ret.add(m);
                if(m instanceof ModuleGroup) ret.addAll(getChildModulesRecursive());
            }
            return ret;
        }
    }
    
    protected void parseChildModules(ModuleManager manager, Element e, String source) throws ReflectiveOperationException, ScriptException {
   
        XPath xpath=XPathFactory.newInstance().newXPath();
        
        try{
            NodeList nl=(NodeList)xpath.evaluate("module",e,XPathConstants.NODESET);
            for(int i=0;i<nl.getLength();i++){
                Tuples.T3<Module,HashMap<String,Object>,ModuleManager.ModuleSettings> tuple=manager.parseXMLModuleElement(e,source);
                manager.addModule(tuple.e1, tuple.e2, tuple.e3);
                addChildModule(tuple.e1);
            }
        }
        catch(XPathExpressionException xpee){
            throw new RuntimeException(xpee); // shouldn't happen, hard coded xpath expression
        }        
    }
    
    @Override
    public HashMap<String, Object> parseXMLOptionsElement(ModuleManager manager, Element e, String source) throws ReflectiveOperationException, ScriptException {
        parseChildModules(manager, e, source);
        
        return manager.parseXMLOptionsElement(e);
    }


}
