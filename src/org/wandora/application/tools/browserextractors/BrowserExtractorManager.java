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
 */



package org.wandora.application.tools.browserextractors;


import org.wandora.topicmap.*;
import org.wandora.application.Wandora;
import org.wandora.utils.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolSet;
import org.wandora.application.WandoraToolType;


/**
 *
 * @author olli
 */


public class BrowserExtractorManager {
    private static boolean USE_TOOL_MANAGER = true;
    private Map<String,BrowserPluginExtractor> browserTools = null;
    private Wandora wandora;
    
    
    public BrowserExtractorManager(Wandora wandora){
        this.wandora=wandora;
        updateExtractorList();
    }


    
    public String[] getExtractionMethods(BrowserExtractRequest request) {
        List<String> methods=new ArrayList<String>();
        BrowserPluginExtractor tool = null;
        for(String key : browserTools.keySet()) {
            try {
                tool = browserTools.get(key);
                if(tool.acceptBrowserExtractRequest(request, wandora)) {
                    methods.add(key);
                }
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        // System.out.println("methods size: "+methods.size());
        // Collections.sort(methods);
        return methods.toArray(new String[methods.size()]);
    }


    public String doPluginExtract(BrowserExtractRequest request) {
        String method=request.getMethod();
        BrowserPluginExtractor tool = browserTools.get(method);
        if(tool != null) {
            try {
                return tool.doBrowserExtract(request, wandora);
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }            
        return BrowserPluginExtractor.RETURN_ERROR+"Method not found";
    }




    public Map<String,BrowserPluginExtractor> getExtractorList(){
        return browserTools;
    }

    public Map<String,BrowserPluginExtractor> updateExtractorList() {
        if(browserTools == null) browserTools = new LinkedHashMap<String,BrowserPluginExtractor>();
        if(USE_TOOL_MANAGER) {
            WandoraToolSet tools = wandora.toolManager.getToolSet(WandoraToolType.BROWSER_EXTRACTOR_TYPE);
            Map<String,WandoraTool> toolMap = tools.getAsMap();
            WandoraTool tool = null;
            for(String key : toolMap.keySet()) {
                tool = toolMap.get(key);
                if(tool instanceof BrowserPluginExtractor) {
                    browserTools.put(key, (BrowserPluginExtractor) tool);
                }
            }
        }
        else {
            browserTools = readExtractorList(false);
        }
        return browserTools;
    }



    public Map<String,BrowserPluginExtractor> readExtractorList(boolean strictlyBrowserPlugins) {
        Map<String,BrowserPluginExtractor> tools = new LinkedHashMap<String,BrowserPluginExtractor>();
        try {
            int pathCounter = 0;
            boolean continueSearch = true;
            List<String> paths = new ArrayList<String>();
            while(continueSearch) {
                String toolResourcePath = wandora.options.get("tool.path["+pathCounter+"]");
                pathCounter++;
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
                        Set<String> toolFileNames = IObox.getFilesAsHash(baseDir, ".*\\.class", 1, 1000);
                        for(String classFileName : toolFileNames) {
                            try {
                                File classFile = new File(classFileName);
                                String className = classPath + "." + classFile.getName().replaceFirst("\\.class", "");
                                if(className.indexOf("$")>-1) continue;
                                BrowserPluginExtractor extractor=null;
                                Class cls=Class.forName(className);
                                if(strictlyBrowserPlugins && WandoraTool.class.isAssignableFrom(cls)) {
                                    continue;
                                }
                                if(!BrowserPluginExtractor.class.isAssignableFrom(cls)) {
                                    //System.out.println("Rejecting '" + className + "'. Does not implement BrowserPluginExtractor interface!");
                                    continue;
                                }
                                if(cls.isInterface()) {
                                    //System.out.println("Rejecting '" + className + "'. Is interface!");
                                    continue;
                                }
                                try {
                                    cls.getConstructor();
                                }
                                catch(NoSuchMethodException nsme){
                                    //System.out.println("Rejecting '" + className + "'. No constructor!");
                                    continue;
                                }
                                extractor=(BrowserPluginExtractor)Class.forName(className).newInstance();
                                tools.put(extractor.getBrowserExtractorName(), extractor);
                            }
                            catch(Exception ex) {
                                //System.out.println("Rejecting tool. Exception '" + ex.toString() + "' occurred while investigating tool class '" + classFileName + "'.");
                                //ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
        return tools;
    }




}
