/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * WandoraToolManager.java
 *
 * Created on 20. lokakuuta 2005, 16:00
 *
 */

package org.wandora.application;


import org.wandora.utils.Options;
import org.wandora.utils.IObox;
import org.wandora.utils.Textbox;
import org.wandora.application.tools.AbstractWandoraTool;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.io.*;

import org.wandora.utils.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;

import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleMenuItem;

import org.wandora.application.tools.importers.*;
import org.wandora.application.tools.project.*;

/**
 * WandoraToolManager implements Wandora's Tool Manager. Tool Manager is
 * used manage Wandora's Tool menu content as well as Import, Extract, and
 * Export menu contents.
 *
 * @author akivela
 */


public class WandoraToolManager extends AbstractWandoraTool implements WandoraTool {

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

    
    protected Wandora admin;
    //                type             tool      instanceName
    protected HashMap<String,Vector<T2<WandoraTool,String>>> tools;
    //    protected Vector<T2<AdminTool,String>> tools;
    //                type   toolList
    protected HashMap<String,WandoraTool[]> allTools;
    
    public static Vector<String> toolTypes=org.wandora.utils.GripCollections.newVector(
            WandoraToolType.GENERIC_TYPE,
            WandoraToolType.IMPORT_TYPE,
            WandoraToolType.EXPORT_TYPE,
            WandoraToolType.GENERATOR_TYPE,
            WandoraToolType.EXTRACT_TYPE
            /*,"configurable" */
    );
    
    //protected HashMap<String,WandoraTool> configurableTools;
    //protected HashMap<WandoraTool,String> configurableIDs;
    
    
    
    public WandoraToolManager(Wandora admin) {
        this.admin=admin;
        tools=null;
        allTools=new HashMap<String,WandoraTool[]>();
        //configurableTools=new HashMap<String,WandoraTool>();
        //configurableIDs=new HashMap<WandoraTool,String>();
        refreshTools();
    }
    
    
    
    /*
    public WandoraTool getConfigurableTool(Class<? extends WandoraTool> c,String id,String label){
        WandoraTool tool=configurableTools.get(id);
        if(tool!=null) return tool;
        try{
            tool=c.newInstance();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        configurableTools.put(id,tool);
        configurableIDs.put(tool,id);
        
        Vector<T2<WandoraTool,String>> ts=tools.get("configurable");
        if(ts==null) {
            ts=new Vector<T2<WandoraTool,String>>();
            tools.put("configurable",ts);
        }
        ts.add(t2(tool,label));
        
        return tool;
    }
    */
    
    
    
    public Wandora getAdmin(){ return admin; }
    
    public WandoraTool[] getToolList(String type){
        return getToolList(type,false);
    }
    
    
    
    public WandoraTool[] getToolList(String type, boolean forceRefresh){
        if(!forceRefresh && allTools.get(type)!=null) return allTools.get(type);
        try {
            Vector<WandoraTool> tools=new Vector<WandoraTool>();
            int pathCounter = 0;
            boolean continueSearch = true;
            ArrayList<String> paths = new ArrayList<String>();
            while(continueSearch) {
                pathCounter++;
                String toolResourcePath = admin.options.get("tools.path["+pathCounter+"]");
                if(toolResourcePath == null || toolResourcePath.length() == 0) {
                    toolResourcePath = "org/wandora/application/tools";
                    //System.out.println("Using default tool resource path: " + toolResourcePath);
                    continueSearch = false;
                }
                if(paths.contains(toolResourcePath)) continue;
                paths.add(toolResourcePath);
                String classPath = toolResourcePath.replace('/', '.');
                Enumeration toolResources = ClassLoader.getSystemResources(toolResourcePath);

                while(toolResources.hasMoreElements()) {
                    URL toolBaseUrl = (URL) toolResources.nextElement();
                    if(toolBaseUrl.toExternalForm().startsWith("file:")) {
                        String baseDir = IObox.getFileFromURL(toolBaseUrl);
//                        String baseDir = URLDecoder.decode(toolBaseUrl.toExternalForm().substring(6), "UTF-8");
                        if(!baseDir.startsWith("/") && !baseDir.startsWith("\\") && baseDir.charAt(1)!=':') 
                            baseDir="/"+baseDir;
                        //System.out.println("Basedir: " + baseDir);
                        HashSet<String> toolFileNames = IObox.getFilesAsHash(baseDir, ".*\\.class", 1, 1000);
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
                                        System.out.println("Rejecting '" + className + "'. Does not implement AdminTool interface!");
                                        continue;
                                    }
                                    if(cls.isInterface()) {
                                        System.out.println("Rejecting '" + className + "'. Is interface!");
                                        continue;
                                    }
                                    try {
                                        cls.getConstructor();
                                    }
                                    catch(NoSuchMethodException nsme){
                                        System.out.println("Rejecting '" + className + "'. No constructor!");
                                        continue;
                                    }
                                    tool=(WandoraTool)Class.forName(className).newInstance();
                                }
                                if(tool.getType().isOfType(type)) {
                                    tools.add(tool);
                                }
                            }
                            catch(Exception ex) {
                                System.out.println("Rejecting tool. Exception '" + ex.toString() + "' occurred while investigating tool class '" + classFileName + "'.");
                                //ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            allTools.put(type,GripCollections.collectionToArray(tools, WandoraTool.class));
            return allTools.get(type);
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
        return null;
    }

    public void execute(Wandora admin, Context context) {
        JDialog d=new JDialog(admin,"Tool Manager",true);
        WandoraToolManagerPanel panel=new WandoraToolManagerPanel(this, d, admin);
        d.getContentPane().add(panel);
        d.setSize(400,400);
        org.wandora.utils.swing.GuiTools.centerWindow(d, admin);
        d.setVisible(true);
    }
    
    
    public String getName() {
        return "Tool manager...";
    }
    public String getDescription() {
        return "Manage Wandora's tools.";
    }
    
    
    
    public JMenu getToolMenu(){
        JMenu toolMenu = new SimpleMenu("Tools");
        toolMenu.setIcon(null);
        toolMenu = getToolMenu(toolMenu);
        return toolMenu;
    }
    public JMenu getToolMenu(JMenu toolMenu){
        toolMenu = getMenu(toolMenu, WandoraToolType.GENERIC_TYPE, accelerators);
        if(toolMenu.getMenuComponentCount() > 0) toolMenu.add(new JSeparator());
        toolMenu.add(this.getToolMenuItem(admin, getName(), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK)));
        ClearToolLocks clearToolLocks = new ClearToolLocks();
        toolMenu.add(clearToolLocks.getToolMenuItem(admin, clearToolLocks.getName(), KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)));
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
    public JMenu getMenu(JMenu toolMenu, String type){
        return getMenu(toolMenu, type, null);
    }
    public JMenu getMenu(JMenu toolMenu, String type, KeyStroke[] keyStrokes){
        int strokeIndex = 0;
        toolMenu.removeAll();
        Collection<T2<WandoraTool,String>> ts=tools.get(type);
        if(ts==null) return toolMenu;
        for(T2<WandoraTool,String> tool : ts) {
            if(tool.e2 != null && tool.e2.startsWith("---")) {
                toolMenu.addSeparator();
            }
            else {
                SimpleMenuItem item=tool.e1.getToolMenuItem(admin, tool.e2);
                item.setToolTipText(Textbox.makeHTMLParagraph(tool.e1.getDescription(), 40));
                if(keyStrokes != null && strokeIndex < keyStrokes.length) item.setAccelerator(keyStrokes[strokeIndex++]);
                toolMenu.add(item);
            }
        }
        return toolMenu;
    }
    
    public Vector<T2<WandoraTool,String>> getTools(String type){
        Vector<T2<WandoraTool,String>> ts=tools.get(type);
        if(ts==null) return new Vector<T2<WandoraTool,String>>();
        else return ts;
    }
    
    public boolean checkName(String instanceName){
        for(Vector<T2<WandoraTool,String>> ts : tools.values()){
            for(T2<WandoraTool,String> t : ts){
                if(t.e2.equals(instanceName)) return false;
            }
        }
        return true;
    }
    
    public boolean addTool(WandoraTool tool,String instanceName, String type){
        if(type == null) return false;
        if(!checkName(instanceName)) return false;
        Vector<T2<WandoraTool,String>> ts=tools.get(type);
        if(ts==null) {
            ts=new Vector<T2<WandoraTool,String>>();
            tools.put(type,ts);
        }
        ts.add(t2(tool,instanceName));
        Options options=admin.getOptions();
        int id=tools.get(type).size()-1;
        options.put("tools."+type+".item["+id+"].class",tool.getClass().getName());
        options.put("tools."+type+".item["+id+"].instanceName",instanceName);
        tool.writeOptions(admin,options, "tools."+type+".item["+id+"].options.");
        admin.toolsChanged();
        return true;
    }
    
    
    public boolean removeTool(WandoraTool removedTool, String instanceName, String type) {
        boolean success = false;
        Vector<T2<WandoraTool,String>> ts = tools.get(type);
        if(ts != null) {
            for(T2<WandoraTool,String> tool : ts){
                if(tool != null) {
                    if(tool.e1.equals(removedTool) && tool.e2.equals(instanceName)) {
                        ts.remove(tool);
                        success = true;
                        break;
                    }
                }
            }
        }
        return success;
    }
   
    
    public void refreshTools(){
        tools=new HashMap<String,Vector<T2<WandoraTool,String>>>();
        //configurableTools=new HashMap<String,WandoraTool>();
        //configurableIDs=new HashMap<WandoraTool,String>();
        Options options=admin.getOptions();
        for(String type : toolTypes){
            int counter=0;
            while(true){
                String cls=options.get("tools."+type+".item["+counter+"].class");
                if(cls==null) break;
                String instanceName=options.get("tools."+type+".item["+counter+"].instanceName");
                try {
                    WandoraTool tool=null;
                    if(cls.equals(this.getClass().getName())) tool=this;
                    else tool=(WandoraTool)Class.forName(cls).newInstance();
                    tool.initialize(admin,options,"tools."+type+".item["+counter+"].options.");
//                    String type=tool.getType();
                    Vector<T2<WandoraTool,String>> ts=tools.get(type);
                    if(ts==null) {
                        ts=new Vector<T2<WandoraTool,String>>();
                        tools.put(type,ts);
                    }
                    ts.add(t2(tool,instanceName));
                    /*
                    if(type.equals("configurable")){
                        String id=options.get("tools."+type+".item"+counter+".id");
                        configurableTools.put(id,tool);
                        configurableIDs.put(tool,id);
                    }
                    */
                }
                catch(ClassNotFoundException cnfe) {
                    System.out.println("Options refer tool class '" + cls + "' not available! Discarding tool!");
                }
                catch(NoClassDefFoundError ncdfe) {
                    System.out.println("A tool configured in options requires a class that was not found. This is most likely caused by a missing library. Missing class was "+ncdfe.getMessage()+". Discarding tool!");
                }
                catch(Exception e) {
                    System.out.println(e);
                }
                counter++;
            }
        }
    }
    

    public String getOptionsPrefix(WandoraTool adminTool){
        HashMap<String,Integer> counters=new HashMap<String,Integer>();
        for(Vector<T2<WandoraTool,String>> ts : tools.values()){
            for(T2<WandoraTool,String> tool : ts){
                String type=tool.e1.getType().oneType();
                int counter=0;
                if(counters.containsKey(type)) counter=counters.get(type);
                if(adminTool==tool.e1) return "tools."+type+".item["+counter+"].options.";
                counter++;
                counters.put(type,counter);
            }
        }        
        return null;
    }

    
    public void rewriteOptions(){
        Options options=admin.getOptions();
        Collection<String> keys=options.keySet();
        for(String key : keys){
            if(key.startsWith("options.tools") && !key.startsWith("options.tools.path")){
                options.put(key,null);
            }
        }
        HashMap<String,Integer> counters=new HashMap<String,Integer>();
        for(Vector<T2<WandoraTool,String>> ts : tools.values()){
            if(ts != null) {
                for(T2<WandoraTool,String> tool : ts){
                    if(tool != null) {
                        String type=tool.e1.getType().oneType();
                        int counter=0;
                        if(counters.containsKey(type)) counter=counters.get(type);
                        System.out.println("rewriting option "+type+" == "+ tool.e2);
                        options.put("tools."+type+".item["+counter+"].class",tool.e1.getClass().getName());
                        options.put("tools."+type+".item["+counter+"].instanceName",tool.e2);
                        /*
                        if(type.equals("configurable")){
                            String id=configurableIDs.get(tool);
                            options.put("tools."+type+".item"+counter+".id",id);
                        }
                        */
                        tool.e1.writeOptions(admin, options, "tools."+type+".item["+counter+"].options.");                
                        counter++;
                        counters.put(type,counter);
                    }
                }
            }
        }
    }
    
    
    public static ArrayList<WandoraTool> getImportTools(File file, int orders) {
        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        ArrayList<WandoraTool> toolList = getImportTools(files, orders);
        return toolList;
    }
    
    
    public static ArrayList<WandoraTool> getImportTools(java.util.List<File> files, int orders) {
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
            else if(fileName.endsWith(".ttl")) {
                SimpleRDFTurtleImport importer = new SimpleRDFTurtleImport(orders);
                importer.forceFiles = file;
                importTools.add(importer);
            }
            else if(fileName.endsWith(".jsonld")) {
                SimpleRDFJsonLDImport importer = new SimpleRDFJsonLDImport(orders);
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
        }
        return importTools;
    }
    
    
    

    public static ArrayList<WandoraTool> getURIImportTools(java.util.List<URI> uris, int orders) {
        String fileName = null;
        ArrayList<WandoraTool> importTools = new ArrayList<WandoraTool>();
        for( URI uri : uris ) {
            try {
                fileName = uri.toURL().toExternalForm();
                if(fileName.endsWith(".xtm20") || fileName.endsWith(".xtm2") || fileName.endsWith(".xtm10") || fileName.endsWith(".xtm1") || fileName.endsWith(".xtm") || fileName.endsWith(".ltm") || fileName.endsWith(".jtm")) {
                    TopicMapImport importer = new TopicMapImport(orders);
                    importer.forceFiles = uri.toURL();
                    importTools.add(importer);
                }
                else if(fileName.endsWith(".rdf") || fileName.endsWith(".rdfs") || fileName.endsWith(".owl") || fileName.endsWith(".daml")) {
                    SimpleRDFImport importer = new SimpleRDFImport(orders);
                    importer.forceFiles = uri.toURL();
                    importTools.add(importer);
                }
                else if(fileName.endsWith(".n3")) {
                    SimpleN3Import importer = new SimpleN3Import(orders);
                    importer.forceFiles = uri.toURL();
                    importTools.add(importer);
                }
                else if(fileName.endsWith(".obo")) {
                    OBOImport importer = new OBOImport(orders);
                    importer.forceFiles = uri.toURL();
                    importTools.add(importer);
                }
                /*
                else if(fileName.endsWith(".wpr")) {
                    if(importTools.size() == 0) {
                        LoadWandoraProject loader = new LoadWandoraProject(uri.toURL());
                        importTools.add(loader);
                    }
                    else {
                        MergeWandoraProject merger = new MergeWandoraProject(uri.toURL());
                        importTools.add(merger);
                    }
                }
                */
                else {
                    TopicMapImport importer = new TopicMapImport(orders);
                    importer.forceFiles = uri.toURL();
                    importTools.add(importer);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }        
        return importTools;
    }
    
    
}
