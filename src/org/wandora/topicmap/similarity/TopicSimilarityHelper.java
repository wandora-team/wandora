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
 */



package org.wandora.topicmap.similarity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import org.wandora.utils.IObox;

/**
 * This helper class reads dynamically all available similarity measures in 
 * the DEFAULT_SIMILARITY_PATH package. Thus, adding a new similarity model
 * requires no special actions. Wandora finds the similarity measure automatically
 * if the similarity measure class implements TopicSimilarity interface and the
 * class package is the DEFAULT_SIMILARITY_PATH.
 *
 * @author akivela
 */


public class TopicSimilarityHelper {
    public static final String DEFAULT_SIMILARITY_PATH = "org/wandora/topicmap/similarity";
    private static boolean ADDITIONAL_DEBUG = true;
    
    
    private static ArrayList<String> similarityPaths = new ArrayList<String>();
    
    
    public static void addSimilarityMeasuresPath(String path) {
        if(!similarityPaths.contains(path)) {
            similarityPaths.add(path);
        }
    }
    public static ArrayList<String> getSimilarityMeasuresPath() {
        return similarityPaths;
    }
    public static void resetSimilarityMeasuresPath() {
        similarityPaths = new ArrayList<String>();
    }
    
    
    
    public static ArrayList<TopicSimilarity> getTopicSimilarityMeasures() {
        ArrayList<TopicSimilarity> measures=new ArrayList<TopicSimilarity>();

        if(!similarityPaths.contains(DEFAULT_SIMILARITY_PATH)) {
            similarityPaths.add(DEFAULT_SIMILARITY_PATH);
        }
        for(String path : similarityPaths) {
            try {
                String classPath = path.replace('/', '.');
                Enumeration measureResources = ClassLoader.getSystemResources(path);

                while(measureResources.hasMoreElements()) {
                    URL measureResourceBaseUrl = (URL) measureResources.nextElement();
                    if(measureResourceBaseUrl.toExternalForm().startsWith("file:")) {
                        String baseDir = IObox.getFileFromURL(measureResourceBaseUrl);
                        // String baseDir = URLDecoder.decode(toolBaseUrl.toExternalForm().substring(6), "UTF-8");
                        if(!baseDir.startsWith("/") && !baseDir.startsWith("\\") && baseDir.charAt(1)!=':') 
                            baseDir="/"+baseDir;
                        //System.out.println("Basedir: " + baseDir);
                        HashSet<String> measureResourceFileNames = IObox.getFilesAsHash(baseDir, ".*\\.class", 1, 1000);
                        for(String classFileName : measureResourceFileNames) {
                            try {
                                File classFile = new File(classFileName);
                                String className = classPath + "." + classFile.getName().replaceFirst("\\.class", "");
                                if(className.indexOf("$")>-1) continue;
                                TopicSimilarity measureResource=null;

                                Class measureResourceClass=Class.forName(className);
                                if(!TopicSimilarity.class.isAssignableFrom(measureResourceClass)) {
                                    if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + measureResourceClass.getSimpleName() + "'. Does not implement TopicSimilarity interface!");
                                    continue;
                                }
                                if(measureResourceClass.isInterface()) {
                                    if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + measureResourceClass.getSimpleName() + "'. Is interface!");
                                    continue;
                                }
                                try {
                                    measureResourceClass.getConstructor();
                                }
                                catch(NoSuchMethodException nsme){
                                    if(ADDITIONAL_DEBUG) System.out.println("Rejecting '" + measureResourceClass.getSimpleName() + "'. No constructor!");
                                    continue;
                                }
                                measureResource=(TopicSimilarity)Class.forName(className).newInstance();

                                if(measureResource != null) {
                                    measures.add(measureResource);
                                }
                            }
                            catch(Exception ex) {
                                if(ADDITIONAL_DEBUG) System.out.println("Rejecting similarity. Exception '" + ex.toString() + "' occurred while investigating '" + classFileName + "'.");
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
        return measures;
    }
    
}
