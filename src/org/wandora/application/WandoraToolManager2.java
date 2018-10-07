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
 * WandoraToolManager2.java
 *
 * Created on 20. lokakuuta 2005, 16:00
 *
 */



package org.wandora.application;



import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.reflections.Reflections;

import org.wandora.utils.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;

import org.wandora.application.gui.simple.SimpleMenu;

import org.wandora.application.tools.importers.*;
import org.wandora.application.tools.project.*;


/**
 *
 * @author akivela
 */
public class WandoraToolManager2 extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;


	public static final boolean ADDITIONAL_DEBUG = false;
    
    
    private KeyStroke[] accelerators = new KeyStroke[] {
        KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK),
    };


    private KeyStroke[] buttonSetAccelerators = new KeyStroke[] {
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.SHIFT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.SHIFT_DOWN_MASK),
    };

    protected Wandora wandora;
    protected Options options;

    protected ArrayList<String> toolPaths;
    
    protected ArrayList<String> jarPaths;
    
    protected ArrayList<WandoraToolSet> toolSets;

    protected HashMap<WandoraTool, String> optionsPrefixes;
    
    protected ArrayList<WandoraTool> allTools;
    protected HashMap<WandoraTool,ToolInfo> toolInfos;
    



    public WandoraToolManager2(Wandora w) {
        wandora = w;
        options = w.getOptions();
        readToolPaths();
        readJarPaths();
        scanAllTools();
        readToolSets();
    }
    
    
    public ArrayList<WandoraToolSet> getToolSets() {
        return toolSets;
    }
    
    
    public void addTool(ToolInfo toolInfo){
        allTools.add(toolInfo.tool);
        toolInfos.put(toolInfo.tool,toolInfo);
    }
    
    
    public void addTool(WandoraTool tool, String sourceType, String source) {
        addTool(new ToolInfo(tool,sourceType,source));
    }
    
    
    public WandoraTool findTool(String cls){
        for(WandoraTool tool : allTools){
            if(tool.getClass().getName().equals(cls)) return tool;
        }
        return null;
    }
    
    public WandoraTool newToolInstance(WandoraTool tool) throws InstantiationException, IllegalAccessException {
        if(tool==null) return null;
        return tool.getClass().newInstance();
    }
    
    
    // ---------------------------------------------------------- TOOL PATHS ---

    public ToolInfo getToolInfo(WandoraTool tool){
        return toolInfos.get(tool);
    }
    
    public ArrayList<String> getToolPaths() {
        return toolPaths;
    }
    
    public ArrayList<String> getJarPaths(){
        return jarPaths;
    }
    
    public void readJarPaths(){
        jarPaths = new ArrayList<String>();
        if(options == null) return;
        int pathCounter=0;
        boolean continueRefresh = true;
        while(true) {
            String jarResourcePath = options.get("tool.jarpath["+pathCounter+"]");
            if(jarResourcePath == null || jarResourcePath.length() == 0) {
                break;
            }
            pathCounter++;
            if(jarPaths.contains(jarResourcePath)) continue;
            jarPaths.add(jarResourcePath);
        }    
    }
    
    public void readToolPaths() {
        toolPaths = new ArrayList<String>();
        if(options == null) return;
        int pathCounter=0;
        boolean continueRefresh = true;
        while(continueRefresh) {
            String toolResourcePath = options.get("tool.path["+pathCounter+"]");
            if(toolResourcePath == null || toolResourcePath.length() == 0) {
                toolResourcePath = "org/wandora/application/tools";
                //System.out.println("Using default tool resource path: " + toolResourcePath);
                continueRefresh = false;
            }
            pathCounter++;
            if(toolPaths.contains(toolResourcePath)) continue;
            toolPaths.add(toolResourcePath);
        }
    }
    
    public void writeJarPaths(ArrayList<String> newJarPaths){
        int c = jarPaths.size();
        int i = 0;
        for( String path : newJarPaths ) {
            options.put("tool.jarpath["+i+"]", path);
            i++;
        }
        for( ; i<c; i++ ) {
            options.put("tool.jarpath["+i+"]", null);
        }        
    }
    
    public void writeToolPaths(ArrayList<String> newToolPaths) {
        int c = toolPaths.size();
        int i = 0;
        for( String path : newToolPaths ) {
            options.put("tool.path["+i+"]", path);
            i++;
        }
        for( ; i<c; i++ ) {
            options.put("tool.path["+i+"]", null);
        }
    }
    
    
    // ----------------------------------------------------- AVAILABLE TOOLS ---
    
    
    public ArrayList<WandoraTool> getAllTools() {
        return allTools;
    }
    
    
    
    public void scanAllTools() {
        readToolPaths();
        readJarPaths();
    
        allTools=new ArrayList<WandoraTool>();
        toolInfos=new HashMap<WandoraTool,ToolInfo>();

        for(String path : toolPaths) {
            try {
                String classPath = path.replace('/', '.');
                Reflections reflections = new Reflections(classPath);

                Set<Class<? extends WandoraTool>> toolClasses = reflections.getSubTypesOf(WandoraTool.class);
                for(Class toolClass : toolClasses) {
                    try {
                        if(isValidWandoraToolClass(toolClass)) {
                            WandoraTool tool = (WandoraTool) toolClass.newInstance();
                            if(tool != null) {
                                addTool(tool, "path", path);
                            }
                        }
                    }
                    catch(Exception e) {
                        if(ADDITIONAL_DEBUG) System.out.println("Rejecting tool. Exception '" + e.toString() + "' occurred while investigating class '" + toolClass + "'.");
                        //e.printStackTrace();
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        for(String jarPath : jarPaths){
            File f=new File(jarPath);
            scanJarPath(f);
        }
    }
    
    
    private boolean isValidWandoraToolClass(Class c) {
        try {
            if(!c.isInterface() && !Modifier.isAbstract( c.getModifiers() )) {
                // throws exception if class has no argumentless constructor.
                c.getConstructors(); 
                return true;
            }
        }
        catch(Exception e) {
            
        }
        return false;
    }
    
    
    
    
    /*
    public void scanAllTools() {
        readToolPaths();
        readJarPaths();
        
        allTools=new ArrayList<WandoraTool>();
        toolInfos=new HashMap<WandoraTool,ToolInfo>();

        for(String path : toolPaths) {
            try {
                String classPath = path.replace('/', '.');
                Enumeration toolResources = ClassLoader.getSystemResources(path);

                while(toolResources.hasMoreElements()) {
                    URL toolBaseUrl = (URL) toolResources.nextElement();
                    if(toolBaseUrl.toExternalForm().startsWith("file:")) {
                        String baseDir = IObox.getFileFromURL(toolBaseUrl);
                        // String baseDir = URLDecoder.decode(toolBaseUrl.toExternalForm().substring(6), "UTF-8");
                        if(!baseDir.startsWith("/") && !baseDir.startsWith("\\") && baseDir.charAt(1)!=':') 
                            baseDir="/"+baseDir;
                        //System.out.println("Basedir: " + baseDir);
                        HashSet<String> toolFileNames = IObox.getFilesAsHash(baseDir, ".*\\.class", 1, 9000);
                        for(String classFileName : toolFileNames) {
                            try {
                                File classFile = new File(classFileName);
                                String className = classPath + "." + classFile.getName().replaceFirst("\\.class", "");
                                if(className.indexOf("$")>-1) continue;
                                WandoraTool tool=null;
                                if(className.equals(this.getClass().getName())) tool=this;
                                else {
                                    Class cls=Class.forName(className);
                                    if(!WandoraTool.class.isAssignableFrom(cls)) {
                                        if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + cls.getSimpleName() + "'. Does not implement Tool interface!");
                                        continue;
                                    }
                                    if(cls.isInterface()) {
                                        if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + cls.getSimpleName() + "'. Is interface!");
                                        continue;
                                    }
                                    try {
                                        cls.getConstructor();
                                    }
                                    catch(NoSuchMethodException nsme){
                                        if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + cls.getSimpleName() + "'. No constructor!");
                                        continue;
                                    }
                                    tool=(WandoraTool)Class.forName(className).newInstance();
                                }
                                if(tool != null) {
                                    addTool(tool, "path", path);
                                    //allTools.add(tool);
                                }
                            }
                            catch(Exception ex) {
                                if(ADDITIONAL_DEBUG) System.out.println("Rejecting tool. Exception '" + ex.toString() + "' occurred while investigating '" + classFileName + "'.");
                                //ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        for(String jarPath : jarPaths){
            File f=new File(jarPath);
            scanJarPath(f);
        }
    }
    */
    
    
    
    
    public void scanJarPath(File f){
        if(!f.exists()) return;
        if(f.isDirectory()){
            for(File f2 : f.listFiles()){
                scanJarPath(f2);
            }
        }
        else {            
            try{
                JarClassLoader jc=new JarClassLoader(f);    
                Collection<String> clsNames=jc.listClasses();
                for(String clsName : clsNames){
                    try{
                        Class cls=jc.loadClass(clsName);
                        if(!WandoraTool.class.isAssignableFrom(cls)){
                            if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + cls.getSimpleName() + "'. Does not implement Tool interface!");
                            continue;
                        }
                        if(cls.isInterface()) {
                            if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + cls.getSimpleName() + "'. Is interface!");
                            continue;
                        }
                        try{
                            Constructor constructor=cls.getConstructor();
                            Object o=constructor.newInstance();
                            WandoraTool tool=(WandoraTool)o;
                            //allTools.add(tool);
                            addTool(tool,"jar",f.getAbsolutePath());
                        }
                        catch(NoSuchMethodException nsme){
                            if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + cls.getSimpleName() + "'. No constructor!");
                            continue;
                        }
                    }
                    catch(Exception ex){
                        if(ADDITIONAL_DEBUG) System.out.println("Rejecting tool. Exception '" + ex.toString() + "' occurred while investigating '" + clsName + "'.");
                    }
                }
            }catch(MalformedURLException mue){
                mue.printStackTrace();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    // ----------------------------------------------------------- TOOL SETS ---
        
        
    public void readToolSets() {
        optionsPrefixes = new HashMap<WandoraTool, String>();
        toolSets = new ArrayList<WandoraToolSet>();
        if(options == null) return;
        int toolSetIndex = 0;
        String toolSetName;
        WandoraToolSet toolSet = null;
        while(true) {
            toolSetName = options.get("tool.set["+toolSetIndex+"].name");
            if(toolSetName != null) {
                toolSet = new WandoraToolSet(toolSetName, toolSetIndex, wandora);
                readToolSet(toolSet, null);
                toolSets.add(toolSet);
            }
            else {
                break;
            }
            toolSetIndex++;
        }
    }

    
    
    private WandoraToolSet readToolSet(WandoraToolSet set, String optionsPath) {
        if(set == null) return null;
        if(optionsPath == null) optionsPath = "tool.set["+set.getIndex()+"]";
        int counter = 0;
        while(true) {
            String newPath = optionsPath + ".item["+counter+"]";
            String name = options.get(newPath + ".name");
            if(name==null) break;
            String cls = options.get(newPath + ".class");
            
            if(cls != null) {
                try {
                    WandoraTool tool=null;
                    if(cls.equals(this.getClass().getName())) tool=this;
                    else tool=newToolInstance(findTool(cls));
                    if(tool!=null){
                        optionsPrefixes.put(tool, newPath+".options.");
                        tool.initialize(wandora,options,newPath+".options.");
                        set.add(name, tool);    
                    }
                    else {
                        System.out.println("Options refer tool class '" + cls + "' not available! Discarding tool!");                        
                    }
                }
                catch(NoClassDefFoundError ncdfe) {
                    System.out.println("A tool configured in options requires a class that was not found. This is most likely caused by a missing library. Missing class was "+ncdfe.getMessage()+". Discarding tool!");
                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
            else {
                //String subtreename = options.get(newPath + ".item[0].name");
                //if(subtreename != null) {
                    WandoraToolSet newToolSet = new WandoraToolSet(name, wandora);
                    set.add(readToolSet(newToolSet, newPath));
                //}
            }
            counter++;
        }
        return set;
    }
    
    
    
    public void writeToolSets() {
        options.removeAll("tool.set");
        int index = 0;
        for(WandoraToolSet toolSet : toolSets) {
            writeToolSet(toolSet, "tool.set["+index+"]");
            index++;
        }
    }
    
    
    private void writeToolSet(WandoraToolSet set, String optionsPath) {
        options.removeAll(optionsPath);
        options.put(optionsPath+".name", set.getName());
        // System.out.println("writeToolSet: " + optionsPath + " ---- "+set);
        Object[] array = set.asArray();
        Object o = null;
        WandoraToolSet.ToolItem toolItem = null;
        WandoraToolSet innerSet = null;
        for( int i=0; i<array.length; i++ ) {
            o = array[i];
            if(o instanceof WandoraToolSet.ToolItem) {
                toolItem = (WandoraToolSet.ToolItem) o;
                // System.out.println("     "+optionsPath+".item["+i+"].name = "+toolItem.getName());
                options.put(optionsPath+".item["+i+"].name", toolItem.getName());
                options.put(optionsPath+".item["+i+"].class", toolItem.getTool().getClass().getName());
            }
            else if(o instanceof WandoraToolSet) {
                innerSet = (WandoraToolSet) o;
                writeToolSet(innerSet, optionsPath+".item["+i+"]");
            }
        }
    }
    
    


    
    
    public boolean deleteToolSet(WandoraToolSet set) {
        if(!allowDelete(set)) return false;
        toolSets.remove(set);
        writeToolSets();
        return true;
    }
    
    
    
    
    
    public WandoraToolSet createToolSet(String name) {
        WandoraToolSet newSet = new WandoraToolSet(name, wandora);
        toolSets.add(newSet);
        writeToolSets();
        return newSet;
    }
    
    
    
    /*
    public void addToolToSet(WandoraToolSet set, WandoraToolSet.ToolItem toolItem) {
        set.add(toolItem);
        writeToolSets();
    }
    
    
    public void addToolToSet(WandoraToolSet set, WandoraTool tool, String instanceName) {
        set.add(instanceName, tool);
        writeToolSets();
    }
    
    public void addSetToSet(WandoraToolSet set, WandoraToolSet child) {
        set.add(child);
        System.out.println("adding set to set: parent == "+set+", child == "+child);
        writeToolSets();
    }
    
    
    public void removeToolOrSet(WandoraToolSet set, Object toolOrSet) {
        set.remove(toolOrSet);
        writeToolSets();
    }
    
    public void renameToolOrSet(WandoraToolSet set, Object toolOrSet, String newName) {
        if(toolOrSet instanceof WandoraToolSet) {
            //System.out.println("WandoraToolSet == "+toolOrSet + " --- new name === "+newName+ "  --- currentSet = "+set);
            ((WandoraToolSet) toolOrSet).setName(newName);
        }
        else if(toolOrSet instanceof WandoraToolSet.ToolItem) {
            //System.out.println("WandoraToolSet.ToolItem == "+toolOrSet + " --- new name === "+newName+ "  --- currentSet = "+set);
            ((WandoraToolSet.ToolItem) toolOrSet).setName(newName);
        }
        writeToolSets();
    }
    */
    
    
    
    public boolean allowDelete(WandoraToolSet set) {
        if(WandoraToolType.IMPORT_TYPE.equals(set.getName())) return false;
        if(WandoraToolType.IMPORT_MERGE_TYPE.equals(set.getName())) return false;
        if(WandoraToolType.EXTRACT_TYPE.equals(set.getName())) return false;
        if(WandoraToolType.EXPORT_TYPE.equals(set.getName())) return false;
        if(WandoraToolType.GENERIC_TYPE.equals(set.getName())) return false;
        if(WandoraToolType.GENERATOR_TYPE.equals(set.getName())) return false;

        if(WandoraToolType.BROWSER_EXTRACTOR_TYPE.equals(set.getName())) return false;
        if(WandoraToolType.WANDORA_BUTTON_TYPE.equals(set.getName())) return false;
        
        return true;
    }
    

    
    public WandoraToolSet getToolSet(String name){
        for( WandoraToolSet set : toolSets ) {
            if(set != null) {
                if(set.getName().equals(name)) {
                    return set;
                }
            }
        }
        return null;
    }
    
    /*
    
    public WandoraTool getToolForName(String name) {
        WandoraTool t = null;
        for( WandoraToolSet set : toolSets ) {
            if(set != null) {
                t = set.getToolForName(name);
                if(t != null) {
                    return t;
                }
            }
        }
        return null;
    }
    
    public WandoraTool getToolForName(String name, WandoraToolSet set) {
        if(set != null) {
            WandoraTool t = set.getToolForName(name);
            if(t != null) {
                return t;
            }
        }
        return null;
    }
    
    public WandoraTool getToolForRealName(String name) {
        WandoraTool t = null;
        for( WandoraToolSet set : toolSets ) {
            if(set != null) {
                t = set.getToolForRealName(name);
                if(t != null) {
                    return t;
                }
            }
        }
        return null;
    }
    
    public WandoraTool getToolForRealName(String name, WandoraToolSet set) {
        if(set != null) {
            WandoraTool t = set.getToolForRealName(name);
            if(t != null) {
                return t;
            }
        }
        return null;
    }
    */
    
    // -------------------------------------------------------------------------
    
    
    public String getOptionsPrefix(WandoraTool tool) {
        String prefix = null;
        try {
            prefix = optionsPrefixes.get(tool);
        }
        catch(Exception e) {}
        return prefix;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public void execute(Wandora w, Context context) {
        scanAllTools();
        readToolSets();
        
        JDialog d=new JDialog(w,"Tool Manager",true);
        WandoraToolManagerPanel2 panel=new WandoraToolManagerPanel2(this, d, wandora);
        d.getContentPane().add(panel);
        d.setSize(900,400);
        org.wandora.utils.swing.GuiTools.centerWindow(d, w);
        d.setVisible(true);
        // ***** WAIT TILL USER CLOSES TOOL MANAGER DIALOG
        writeToolSets();
        scanAllTools();
        readToolSets();
        toolsChanged();
    }
    
    
    
    @Override
    public String getName() {
        return "Tool manager v2...";
    }
    
    
    @Override
    public String getDescription() {
        return "Manage Wandora's tools and tool sets.";
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------






    public Object[] getToolButtonSelectMenuStruct() {
        String[] set = getToolButtonSetNames();
        ArrayList<Object> menuStruct = new ArrayList<Object>();
        for(int i=0; i<set.length; i++) {
            menuStruct.add( set[i] );
            menuStruct.add( new ActivateButtonToolSet( set[i] ) );
            if(i<buttonSetAccelerators.length) {
                menuStruct.add(buttonSetAccelerators[i]);
            }
        }
        return menuStruct.toArray();
    }


    
    public JMenu getToolButtonSelectMenu() {
        Object[] struct = new Object[] { "Select button tool set", getToolButtonSelectMenuStruct() };
        return UIBox.makeMenu(struct, wandora);
    }


    public JPopupMenu getToolButtonSelectPopupMenu() {
        return UIBox.makePopupMenu(getToolButtonSelectMenuStruct(), wandora);
    }



    public String[] getToolButtonSetNames() {
        ArrayList<String> buttonSets = new ArrayList<String>();
        if(toolSets != null) {
            String name = null;
            for(WandoraToolSet set : toolSets) {
                name = set.getName();
                if(name != null && name.indexOf("button") != -1) {
                    buttonSets.add(name);
                }
            }
        }
        return buttonSets.toArray( new String[] {} );
    }

    
    
    public JComponent getToolButtonBar() {
        return getToolButtonBar(WandoraToolType.WANDORA_BUTTON_TYPE);
    }
    
    
    
    public JComponent getToolButtonBar(String setName) {
        WandoraToolSet toolset = getToolSet(setName);
        if(toolset != null && toolset.size() > 0) {
            Object[] struct = toolset.getAsObjectArray();
            JComponent buttonbar = UIBox.makeButtonContainer(struct, wandora);
            if(buttonbar != null) {
                return buttonbar;
            }
        }
        return null;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    

    
    public JMenu getToolMenu(){
        JMenu toolMenu = new SimpleMenu("Tools");
        toolMenu.setIcon(null);
        toolMenu = getToolMenu(toolMenu);
        return toolMenu;
    }

    public JMenu getToolMenu(JMenu toolMenu){
        toolMenu = getMenu(toolMenu, getToolSet(WandoraToolType.GENERIC_TYPE), accelerators, 0);

        ClearToolLocks clearToolLocks = new ClearToolLocks();
        toolMenu.insert(clearToolLocks.getToolMenuItem(wandora, clearToolLocks.getName(), KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)), 0);

        JMenu buttonSelectorSubmenus = this.getToolButtonSelectMenu();
        toolMenu.insert(buttonSelectorSubmenus, 1);
        toolMenu.insert(this.getToolMenuItem(wandora, getName(), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK)), 0);

        toolMenu.insertSeparator(2);
        toolMenu.insertSeparator(4);

        return toolMenu;
    }


    public JMenu getGeneratorMenu() {
        JMenu toolMenu = new SimpleMenu("Generate");
        toolMenu.setIcon( UIBox.getIcon("gui/icons/generate.png") );
        return getGeneratorMenu(toolMenu);
    }
    public JMenu getGeneratorMenu(JMenu toolMenu){
        return getMenu(toolMenu, WandoraToolType.GENERATOR_TYPE);
    }
    
    public JMenu getExtractMenu() {
        JMenu toolMenu = new SimpleMenu("Extract");
        toolMenu.setIcon( UIBox.getIcon("gui/icons/extract.png") );
        return getExtractMenu(toolMenu);
    }
    public JMenu getExtractMenu(JMenu toolMenu){
        return getMenu(toolMenu, WandoraToolType.EXTRACT_TYPE);
    }
    
    public JMenu getImportMergeMenu(JMenu toolMenu) {
        toolMenu.removeAll();
        WandoraToolSet toolSet = getToolSet(WandoraToolType.IMPORT_MERGE_TYPE);
        if(toolSet == null) return toolMenu;
        List toolItems = toolSet.getTools();
        for(Object toolItem : toolItems) {
            if(toolItem != null) {
                if(toolItem instanceof WandoraToolSet.ToolItem) {
                    WandoraTool tool = ((WandoraToolSet.ToolItem) toolItem).getTool();
                    if(tool instanceof AbstractImportTool) {
                        AbstractImportTool aiTool = (AbstractImportTool) tool;
                        aiTool.setOptions(AbstractImportTool.TOPICMAP_DIRECT_MERGE);
                    }
                }
            }
        }
        return toolSet.getMenu(toolMenu, toolSet);
    }
    
    public JMenu getImportMenu(){
        JMenu toolMenu = new SimpleMenu("Import");
        toolMenu.setIcon( UIBox.getIcon("gui/icons/import.png") );
        return getImportMenu(toolMenu);
    }
    public JMenu getImportMenu(JMenu toolMenu){
        return getMenu(toolMenu, WandoraToolType.IMPORT_TYPE);
    }
    
    public JMenu getExportMenu(){
        JMenu toolMenu = new SimpleMenu("Export");
        toolMenu.setIcon( UIBox.getIcon("gui/icons/export.png") );
        return getExportMenu(toolMenu);
    }
    public JMenu getExportMenu(JMenu toolMenu){
        return getMenu(toolMenu, WandoraToolType.EXPORT_TYPE);
    }
    public JMenu getMenu(JMenu toolMenu, String setName){
        return getMenu(toolMenu, getToolSet(setName), null, 0);
    }
    
    public JMenu getMenu(JMenu toolMenu, WandoraToolSet toolSet, KeyStroke[] keyStrokes, int strokeIndex) {
        toolMenu.removeAll();
        if(toolSet == null) return toolMenu;
        return toolSet.getMenu(toolMenu, toolSet);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    

    public static ArrayList<WandoraTool> getImportTools(java.util.List<File> files, int orders){
        String fileName = null;
        ArrayList<WandoraTool> importTools = new ArrayList<WandoraTool>();
        for( File file : files ) {
            fileName = file.getName().toLowerCase();
            if(fileName.endsWith(".xtm20") || fileName.endsWith(".xtm2") || fileName.endsWith(".xtm10") || fileName.endsWith(".xtm1") || fileName.endsWith(".xtm") || fileName.endsWith(".ltm") || fileName.endsWith(".jtm")) {
                TopicMapImport importer = new TopicMapImport(orders);
                importer.forceFiles = file;
                importTools.add(importer);
            }
            else if(fileName.endsWith(".rdf") || fileName.endsWith(".rdfs") || fileName.endsWith(".owl") || fileName.endsWith(".daml")) {
                SimpleRDFImport importer = new SimpleRDFImport(orders);
                importer.forceFiles = file;
                importTools.add(importer);
            }
            else if(fileName.endsWith(".n3")) {
                SimpleN3Import importer = new SimpleN3Import(orders);
                importer.forceFiles = file;
                importTools.add(importer);
            }
            else if(fileName.endsWith(".obo")) {
                OBOImport importer = new OBOImport(orders);
                importer.forceFiles = file;
                importTools.add(importer);
            }
            else if(fileName.endsWith(".wpr")) {
                if(importTools.isEmpty()) {
                    LoadWandoraProject loader = new LoadWandoraProject(file);
                    importTools.add(loader);
                }
                else {
                    MergeWandoraProject merger = new MergeWandoraProject(file);
                    importTools.add(merger);
                }
            }
            else {
                importTools.add(null);
            }
        }        
        return importTools;
    }
    
    
    public void toolsChanged() {
        if(wandora != null) wandora.toolsChanged();
    }
    
    // -------------------------------------------------------------------------
    
    public static class ToolInfo {
        public WandoraTool tool;
        public String sourceType;
        public String source;

        public ToolInfo() {
        }

        public ToolInfo(WandoraTool tool, String sourceType, String source) {
            this.tool = tool;
            this.sourceType = sourceType;
            this.source = source;
        }
        
    }
    
    
}
