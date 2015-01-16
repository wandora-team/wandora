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
 *
 * FragmentContainer.java
 *
 * Created on 24. helmikuuta 2003, 12:43
 */

package org.wandora.utils.regexextractor;




import org.wandora.utils.regexextractor.bag.GenericBag;
import org.wandora.utils.regexextractor.bag.Bag;
import org.wandora.utils.IObox;
import org.wandora.utils.Textbox;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;
import org.wandora.utils.regexextractor.codecs.*;
import org.wandora.utils.*;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;




public class FragmentContainer {
    
    
    private String fragmentDelimiter = "_NO_FRAGMENTS_234523457875768_"; 
    Hashtable fragments;
    Hashtable discarded;
    Bag preferences;
    int exportFlushSize = 0;
    boolean firstExport = true;
    int totalValidFragments = 0;
    int totalDiscardedFragments = 0;
    VelocityEngine velocityEngine = new VelocityEngine();
    
    String importEncoding = null;
    String template = null;
    String templateEncoding = null;
    String exportPath = null;
    String exportType = null;
    Object exportCodec = null;
    String exportDircardedPath = null;
    
    
    
    /** Creates a new instance of MatrikkeliContainer */
    public FragmentContainer(String prefencesFile) {
        fragments = new Hashtable();
        discarded = new Hashtable();
        initPreferences(prefencesFile);
        importExport();
    }
 
    public FragmentContainer(String preferencesFile, Properties props) {
        fragments = new Hashtable();
        discarded = new Hashtable();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try { 
            props.store(bos, null);
        } catch (java.io.IOException ioe) {
            System.err.println("Could not convert properties to byte array. Can not initialize FragmentContainer properly! Exiting.");
        }
        initPreferences(preferencesFile, props);
        importExport();
    }

    
    
    
    
    public void importExport() {
        importUsingPreferences();
        exportUsingPreferences();
    }
     
    
    
    
    
    
    // -------------------------------------------------------------------------
    // preferencesFile is either string containing a file name or an input stream reader!

    public void initPreferences(String preferencesFile) {
        initPreferences(preferencesFile, null);
    }
    
    private void initPreferences(String preferencesFile, Properties props) {
        LogWriter.setActive(true);
        preferences = new GenericBag(); 
        preferences.init();
        
        if (preferencesFile != null) {
            if (preferencesFile.endsWith("xml")) {
                LogWriter.println("Sorry - XML preferences not supported currently!");
                System.exit(0);
            }
            else {
                
                // GENERAL
                try {
                    preferences.load(preferencesFile);
                    if (props!=null) {
                        for (Iterator i = props.keySet().iterator(); i.hasNext(); ) {
                            Object key = i.next();
                            Object value = props.get(key);
                            preferences.put(key, value);
                        }
                    }
                    try {
                        if("false".equals(preferences.getStringIn("silent"))) {
                            LogWriter.setActive(true);
                        }
                    }
                    catch (Exception e) {}
                    LogWriter.println("Fragment container initialized using file '" + preferencesFile + "'.");
                    
                    
                    try {
                        exportFlushSize = 0;
                        exportFlushSize = preferences.getIntIn("export.flush.size");
                        LogWriter.println("Export flush size set to '" + exportFlushSize + "'.");
                    }
                    catch (Exception e) {}
                }
                catch (Exception e) {
                    LogWriter.println("INF", "Exception occurred while loading preferences from file '" + preferencesFile + "'.");
                }
                
                
                // IMPORT ----
                
                importEncoding = getPreference(preferences, "import.encoding");
                if(importEncoding != null) {
                    LogWriter.println("INF", "Import encoding is '"+ importEncoding + "'.");
                }
                
                
                fragmentDelimiter = getPreference(preferences, "fragment.delimiter");
                if (fragmentDelimiter == null) {
                    fragmentDelimiter = "_ONLY_ONE_FRAGMENT_IN_FILE_284976239862356_";
                    LogWriter.println("INF", "No fragment delimiter. Each found file contains only one fragment!");
                }
                else {
                    LogWriter.println("INF", "Fragment delimiter '" + fragmentDelimiter + "' found in preferences! Each file may contain multiple fragments!");
                }
        
                
                
                // EXPORT ----

                template = getPreference(preferences, "export.template");
                if (template == null) {
                    LogWriter.println("INF", "No export template defined in preferences. Unable to export fragemnts!");
                    return;
                }
                templateEncoding = getPreference(preferences, "export.template.encoding");
                if (templateEncoding == null) {
                    LogWriter.println("INF", "No export template encoding defined in preferences. Using default encoding!");
                    return;
                }
                exportPath =  getPreference(preferences, "export.directory");
                if (exportPath == null) {
                    LogWriter.println("INF", "No export path defined in preferences. Unable to export fragemnts!");
                    return;
                }
                exportType =  getPreference(preferences, "export.type");
                if (exportType == null) {
                    exportType = "txt";
                    LogWriter.println("INF", "No export type defined in preferences! Using default type '" + exportType + "'.");
                }

                exportCodec = null;
                String exportCodecClassName = getPreference(preferences, "export.codec");
                if (exportCodecClassName != null) {
                    try {
                        Class exportCodecClass = Class.forName(exportCodecClassName);
                        exportCodec = exportCodecClass.newInstance();
                    }
                    catch (Exception e) {
                        LogWriter.println("INF", "Unable to instantiate export codec object of class '" + exportCodecClassName + "'.");
                    }
                }
                
                
                exportDircardedPath =  getPreference(preferences, "export.directory.discarded");
                if (exportDircardedPath != null) {
                    LogWriter.println("INF", "Exporting discarded fragments to path '" + exportDircardedPath + "'.");
                }

                
            }
        }
        //preferences.print();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public void importUsingPreferences() {

        LogWriter.println("INF", "Importing fragments using preferences....");
        

        String importFile = getPreference(preferences, "import.file");
        boolean success = false;
        
        if(importFile != null) {
            LogWriter.println("INF", "Importing single file '"+ importFile + "'.");
            importTextFile(importFile, importEncoding);
            success = true;
        }
        
        String importDirectory = getPreference(preferences, "import.directory");
        if(importDirectory != null) {
            if(importDirectory.endsWith(File.separator)) {
                importDirectory = importDirectory.substring(0, importDirectory.length() - File.separator.length());
            }
            
            String filter = getPreference(preferences, "import.directory.filter");
            if (filter == null) {
                filter = ".*";
                LogWriter.println("INF", "No file filter specified. Using default filter '" + filter + "'.");
            }
            else LogWriter.println("INF", "Using import file filter '" + filter + "'.");
            
            // ----
            String depth = getPreference(preferences, "import.directory.depth");
            int depthAsInt = 1;
            if (depth == null) {
                LogWriter.println("INF", "No browse depth specified. Using default depth of " + depthAsInt + ".");
            }
            else {
                try {
                    depthAsInt = Integer.parseInt(depth);
                }
                catch (Exception e) {
                    LogWriter.println("INF", "Unable to parse browse depth. Illegal integer in '" + depth + "'. Using default depth of " + depthAsInt + ".");
                }
            }

            // ----
            String maxfiles = getPreference(preferences, "import.directory.maxfiles");
            int maxfilesAsInt = 100;
            if (maxfiles == null) {
                LogWriter.println("INF", "No maximum number of files specified. Using default limit of " + maxfilesAsInt + ".");
            }
            else {
                try {
                    maxfilesAsInt = Integer.parseInt(maxfiles);
                }
                catch (Exception e) {
                    LogWriter.println("INF", "Unable to parse maximum number of files. Illegal integer in '" + maxfiles + "'. Using default limit of " + maxfilesAsInt + ".");
                }
            }
                       
            String[] files = IObox.getFileNames(importDirectory, filter, depthAsInt, maxfilesAsInt);
            importTextFiles(files, importEncoding);
            success = true;
        }
        else {
            LogWriter.println("INF", "Import directory not defined!");
        }
        
        
        if (success == false) {
            LogWriter.println("INF", "No fragments imported! Update your preferences file in order to import fragments!");
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public void exportUsingPreferences() {
        LogWriter.println("INF", "Exporting fragments....");
       
        if(firstExport == true) {
            String deleteOld = getPreference(preferences, "export.directory.deleteold");
            if (deleteOld != null && "true".equals(deleteOld)) {
                LogWriter.println("INF", "  Deleting first old files in export directory '" + exportPath + "'.");
                IObox.deleteFiles(exportPath);
            }
            firstExport = false;
        }
        
        if(template != null && exportPath != null && exportType != null) {
            exportVelocity(template, templateEncoding, exportPath, exportType, exportCodec);
        }

        if(exportDircardedPath != null) {
            exportDiscarded(exportDircardedPath);
        }
        
        fragments = new Hashtable();
        discarded = new Hashtable();
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public void importTextFiles(String[] fileNames) {
        importTextFiles(fileNames, null);
    }
    
    public void importTextFiles(String[] fileNames, String enc) {
        if (fileNames != null) {
            for(int i=0; i<fileNames.length; i++) {
                if (fileNames[i] != null) {
                    importTextFile(fileNames[i], enc);
                }
            }
        }
    }
    
    
    
    public void importTextFile(String fileName) {
        importTextFile(fileName, null);
    }
    
    
    public void importTextFile(String fileName, String enc) {
        try {
            int validFragments = 0;
            int discardedFragments = 0;
            String input = null;
            if(enc == null) {
                LogWriter.println("Importing text file '" + fileName + "'!");
                input = IObox.loadFile(fileName);
            }
            else {
                LogWriter.println("Importing text file '" + fileName + "' with '" + enc + "' encoding!");
                input = IObox.loadFile(fileName, enc);            
            }
            LogWriter.println("TEXT:" + input);
            Vector fragmentsexts = Textbox.sliceWithRE(input, fragmentDelimiter);
            //LogWriter.println("  Found " + fragmentsexts.size() + " slices!");
            for (int i=0; i<fragmentsexts.size(); i++) {
                try {
                    Fragment fragment = new Fragment((String) fragmentsexts.elementAt(i), preferences);
                    fragment.setFileName(fileName);
                    if(fragment.isValid()) {
                        fragments.put(fragment, "");
                        validFragments++;
                        totalValidFragments++;
                        //LogWriter.println("Accepting fragment!");
                        
                        // Start export if enough fragmants!!!
                        if(exportFlushSize != 0 && fragments.size() > exportFlushSize) {
                            LogWriter.println("  Flush limit of " +exportFlushSize+ " exceeded! Flushing fragment files!");
                            exportUsingPreferences();
                        }
                    }
                    else {
                        //LogWriter.println("Discarding fragment!");
                        discarded.put(fragment, "");
                        discardedFragments++;
                        totalDiscardedFragments++;
                        fragment.print();
                    }
                }
                catch (Exception e) {
                    LogWriter.println("Exception '" + e.toString() +  " occurred while parsing fragment!");
                    e.printStackTrace();
                }
            }
            LogWriter.println("Found " + validFragments + " valid and " + discardedFragments + " invalid fragments!");
        }
        catch (Exception e) {
            LogWriter.println("Exception '" + e.toString() + "' occurred while importing text file to FragmentContainer!");
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    public void exportVelocity(String templateName, String templateEncoding, String path, String suffix, Object codec) {
        Fragment dataFragment = null;
        String fileName;

        if(!suffix.startsWith(".")) suffix = "." + suffix;
        
        for(Enumeration fragmentKeys = fragments.keys(); fragmentKeys.hasMoreElements();) {
            try {
                dataFragment = (Fragment) fragmentKeys.nextElement();
                fileName = path + File.separator + Textbox.filterNonAlphaNums(Textbox.toLowerCase(dataFragment.solveFileName())) + suffix;
                dataFragment.exportVelocity(velocityEngine, templateName, templateEncoding, fileName, codec);
            }
            catch (Exception e) {
                LogWriter.println("ERR", "FragmentContainer contains illegal (not a Fragment) object. Unable to export velocity!");
                e.printStackTrace();
            }
        }
    }
    
    
    
    public void exportSingleVelocity(String templateName, String templateEncoding, String fname) {
        File pf = new File(fname);
        if (pf.exists()) {
            pf.delete();
            LogWriter.println("Deleting previously existing file '" + fname + "' before save file operation!");
        }

        try {
            FileWriter pfr = new FileWriter(fname);
            exportSingleVelocity(templateName, templateEncoding, pfr);

            pfr.flush();
            pfr.close();
            LogWriter.println("Saving a file '" + fname + "'!"); 
        }
        catch (Exception e) {
            LogWriter.println("ERR", "Unable to export velocity data due to exception '" + e.toString() + "'!");
        }
    }
    
    
    public void exportSingleVelocity(String templateName, String templateEncoding, Writer writer) {
        VelocityContext context;
        Template template = null;
        Vector data = new Vector();
        File templateFile;
        String templatePath;
        VelocityEngine velocityEngine;
        velocityEngine = new VelocityEngine();
        String shortTemplateName = null;
        
        try {
            templateFile = new File(templateName);
            templatePath = templateFile.getParent();
            shortTemplateName = templateFile.getName();
            velocityEngine.setProperty("file.resource.loader.path" , templatePath);
            velocityEngine.init();
        }
        catch (Exception e) {
            LogWriter.println("ERR", "Unable to initialize velocity template path!");
        }
        
        context = new VelocityContext();
        for(Enumeration fragmentKeys = fragments.keys(); fragmentKeys.hasMoreElements();) {
            try {
                data.add(((Fragment) fragmentKeys.nextElement()).getAll());
            }
            catch (Exception e) {
                LogWriter.println("ERR", "FragmentContainer contains illegal (not a Fragment) object. Unable to print!");
            }
        }
        context.put( "data", data );

        try {
            if(templateEncoding != null && templateEncoding.length()>0) {
                template = velocityEngine.getTemplate(shortTemplateName, templateEncoding);
            }
            else {
                template = velocityEngine.getTemplate(shortTemplateName);
            }
            template.merge( context, writer );  
        }
        catch( ResourceNotFoundException rnfe ) {
            // couldn't find the template
            LogWriter.println("ERR", "Unable to find the template file '" + shortTemplateName + "' for velocity export!");
        }
        catch( ParseErrorException pee ) {
            // syntax error : problem parsing the template
            LogWriter.println("ERR", "Unable to parse the velocity template file '" + shortTemplateName + "'!");
        }
        catch( MethodInvocationException mie ) {
            // something invoked in the template threw an exception
            LogWriter.println("ERR", "Velocity template '" + shortTemplateName + "' causes method invocation exception!");
        }
        catch( Exception e ) {
            LogWriter.println("ERR", "Exception '" + e.toString() + "' occurred while working with template '" + shortTemplateName + "'!");        
        }

    }
    
    
    
    
    
    public void exportXML() {
    }
    
    
    
    public void exportTextFiles(String path) {
        Fragment dataFragment;
        for(Enumeration fragmentKeys = fragments.keys(); fragmentKeys.hasMoreElements();) {
            try {
                dataFragment = (Fragment) fragmentKeys.nextElement();
                dataFragment.exportRaw(path + File.separator + Textbox.filterNonAlphaNums(Textbox.toLowerCase(dataFragment.solveFileName())) + ".txt");
            }
            catch (Exception e) {
                LogWriter.println("ERR", "FragmentContainer contains illegal (not a Fragment) object. Unable to save!");
            }
        }
    }
    
    
    
    public void exportDiscarded(String path) {
        Fragment dataFragment;
        int i=totalDiscardedFragments;
        for(Enumeration discardedKeys = discarded.keys(); discardedKeys.hasMoreElements();) {
            try {
                dataFragment = (Fragment) discardedKeys.nextElement();
                dataFragment.exportRaw(path + File.separator + i + ".txt");
                i--;
            }
            catch (Exception e) {
                LogWriter.println("ERR", "FragmentContainer contains illegal (not a Fragment) object. Unable to save!");
            }
        }
    }
    
    
        
    public String getPreference(Bag prefs, String key) {
        String value = null;
        try {
            value = prefs.getStringIn(key);
        }
        catch (Exception e) {}
        return value;
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    public void print() {
        for(Enumeration fragmentKeys = fragments.keys(); fragmentKeys.hasMoreElements();) {
            try {
                ((Fragment) fragmentKeys.nextElement()).print();
            }
            catch (Exception e) {
                LogWriter.println("ERR", "FragmentContainer contains illegal (not a Fragment) object. Unable to print!");
            }
        }
    }
    
    
    
    public static void main(String[] args) {
        long startDate = System.currentTimeMillis();
        LogWriter.println("****************************************************");
        LogWriter.println("***              FRAGMENT CONTAINER              ***");
        LogWriter.println("***   (c) 2003 Grip Studios Interactive, Inc.    ***");
        LogWriter.println("***                                              ***"); 
        LogWriter.println("*** Fragment Container extracts information from ***");
        LogWriter.println("***  unstructured data files and wraps extracted ***");
        LogWriter.println("***            data into a given template.       ***");
        LogWriter.println("****************************************************");
        
        LogWriter.println("Date is " + (new java.text.SimpleDateFormat().format(new Date(startDate))));

        String preferencesFile = "preferences.properties";
        java.util.Properties props = new java.util.Properties(); 
        if(args.length > 0 && args[0].length() > 0) {
            preferencesFile = args[0];
        }
        else LogWriter.println("Default preferences file name '" + preferencesFile + "' used. No command line arguments!");
        if(args.length > 1 && args[1].length() > 0) {
            char separator = args[1].charAt(0);
            String propstring = args[1].substring(1).replaceAll("\\"+separator, "\n");
            try {
                props.load(new ByteArrayInputStream( propstring.getBytes("ISO-8859-1") ));
                System.out.println("argument properties: "+ props.toString());
            } catch (UnsupportedEncodingException use) { 
                System.out.println("Argument string could not be converted to ISO-8859-1 encoded byte array. It is used as a properties file name, not property string.");
            } catch (IOException ioe) {
                System.out.println("Argument string could not be loaded as properties. It is used as a properties file name.");
            }
        }
        
        FragmentContainer fragments = null;
        fragments = new FragmentContainer(preferencesFile, props);
               
        // --- STATS ---
        LogWriter.setActive(true);
        LogWriter.println("INF", "Found total " + fragments.totalValidFragments + " fragments!");
        LogWriter.println("INF", "Discarded total " + fragments.totalDiscardedFragments + " fragments!");
        long endDate = System.currentTimeMillis();
        
        LogWriter.println("APP", "Execution time " + ((endDate - startDate) / 1000) + " secs");
        LogWriter.println("APP", "Ok!");
    }
    
    

    
    
}
