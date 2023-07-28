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
package org.wandora.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.script.ScriptException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * A module manager that can be placed inside other module managers in
 * a hierarchical fashion. Each child module manager can import modules
 * from the direct parent manager using import elements. Otherwise, child managers
 * are isolated from the parent, or sibling, managers. This makes it practical to
 * import module bundles that implement specific features without worry of them
 * conflicting with existing modules.
 * </p>
 * <p>
 * The ScopedModuleManager itself is a Module and can be placed in any other
 * module manager, scoped or otherwise. Its dependencies will be all the modules
 * that are specifically imported into it. All the contained modules that are marked
 * for autostart are started when the module manager itself is started and stopped
 * when the manager is stopped. The child modules are initialised when the manager
 * is initialised.
 * </p>
 * 
 *
 * @author olli
 */


public class ScopedModuleManager extends ModuleManager implements Module, XMLOptionsHandler {

    protected boolean running=false;
    protected boolean initialized=false;
    
    protected ModuleManager parentManager;
    
    protected final ArrayList<Import> imports=new ArrayList<Import>();
    
    public ScopedModuleManager() {
        
    }
    
    @Override
    public synchronized <A extends Module> A findModule(Module context,String instanceName,Class<A> cls){
        A ret=super.findModule(context,instanceName,cls);
        if(ret!=null) return ret;
        
        if(parentManager==null) return null;
        
        for(Import im : imports){
            if(im.cls.getName().equals(cls.getName())){
                return parentManager.findModule(this, instanceName, cls);
            }
        }
        
        return null;
    }
    
    public synchronized <A extends Module> ArrayList<A> findModulesRecursive(Class<A> cls){
        ArrayList<A> ret=new ArrayList<A>();
        
        ret.addAll(findModules(cls));
        
        for(ScopedModuleManager smm : findModules(ScopedModuleManager.class)){
            ret.addAll(smm.findModulesRecursive(cls));
        }
        
        return ret;
    }
    
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> parameters) throws ModuleException {
        this.parentManager=manager;
        initAllModules();
        initialized=true;
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> ret=new ArrayList<Module>();
        
        for(Import im : imports){
            if(im.optional) manager.optionalModule(this, im.cls, ret);
            else manager.requireModule(this, im.cls, ret);
        }
        
        return ret;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        this.autostartModules();
        running=true;
    }

    @Override
    public void stop(ModuleManager manager) {
        this.stopAllModules();
        running=false;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
    
    public void addImport(Import im){
        synchronized(imports){
            for(Import i : imports){
                if(i.cls.getName().equals(im.cls.getName())) {
                    if(i.optional && !im.optional) i.optional=false;
                    return;
                }
            }
            imports.add(im);
        }
    }
    
    public void addImport(Class<? extends Module> cls, boolean optional){
        addImport(new Import(cls,optional));
    }
    
    protected void parseXMLImport(Element e){
        String className=e.getAttribute("class").trim();
        String variable=e.getAttribute("variable").trim();
        if(parentManager==null) log.warn("Parent manager not set, can't import anything");
        if(className.length()==0) {
            if(variable.length()==0) {
                if(log!=null) log.warn("No class or variable specified in import");
                return;
            }
            else {
                setVariable(variable,parentManager.getVariable(variable));
            }
        }
        else {
            try{
                String optional=e.getAttribute("optional");
                Class<?> cls=Class.forName(className);
                if(Module.class.isAssignableFrom(cls)){
                    Import im=new Import((Class<? extends Module>)cls);
                    if(optional.trim().equalsIgnoreCase("true")) im.optional=true;
                    
                    addImport(im);
                }
                else {
                    if(log!=null) log.warn("Imported class "+className+" is not an instance of Module");
                }
            }
            catch(ClassNotFoundException cnfe){
                if(log!=null) log.warn(cnfe);
            }
        }
    }

    @Override
    public Collection<Module> parseXMLConfigElement(Node doc, String source) {
        try{
            XPath xpath=XPathFactory.newInstance().newXPath();
            
            NodeList nl=(NodeList)xpath.evaluate("//import",doc,XPathConstants.NODESET);
            for(int i=0;i<nl.getLength();i++){
                Element e2=(Element)nl.item(i);
                parseXMLImport(e2);
            }
            return super.parseXMLConfigElement(doc, source);
        }catch(Exception ex){
            if(log!=null) log.error("Error reading options file", ex);
            return null;
        }
    }

    
    
    @Override
    public Map<String, Object> parseXMLOptionsElement(ModuleManager manager, Element e, String source) throws ReflectiveOperationException, ScriptException {
        parentManager=manager;
        String src=e.getAttribute("src").trim();
        if(src.length()>0){
            readXMLOptionsFile(src);
        }
        else {
            parseXMLConfigElement(e, source);
        }
        
        return super.parseXMLOptionsElement(e);
    }
    
    public void setParentManager(ModuleManager manager){
        this.parentManager=manager;
    }
    
    public static class Import {
        public Class<? extends Module> cls;
        public boolean optional=false;
        public Import(Class<? extends Module> cls,boolean optional){
            this.cls=cls;
            this.optional=optional;
        }
        public Import(Class<? extends Module> cls){
            this(cls,false);
        }
    }
    
}
