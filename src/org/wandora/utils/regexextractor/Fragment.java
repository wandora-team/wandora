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
 * Fragment.java
 *
 * Created on 24. helmikuuta 2003, 12:30
 */

package org.wandora.utils.regexextractor;

import org.wandora.utils.regexextractor.bag.Bag;
import org.wandora.utils.Textbox;
import org.wandora.utils.IObox;
import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import org.wandora.utils.regexextractor.bag.*;
import org.wandora.utils.regexextractor.extractors.*;
import org.wandora.utils.*;




public class Fragment {
    
    protected String postCharset = "FILTER";
    protected String rawData;
    protected String sourceFileName;
    protected boolean isValid;
    protected Bag preferences = null;

    protected String[][] propertyExtractors = {};
    protected String[] validityExtractors = {};
    protected String[] fileNameExtractors = {};
    
    


    public Fragment() {
        this.isValid = false;
    }
    

    public Fragment(String rawData) {
        this.isValid = true;
        this.rawData = rawData;
        LogWriter.println("INF", "No preferences available for fragment object! Fragment may not work properly!");
    }
    

    public Fragment(String sourceFileName, String rawData, Bag prefs) {
        this(rawData, prefs);
        this.sourceFileName = sourceFileName;
    }
        
    
    public Fragment(String rawData, Bag prefs) {
        this.isValid = true;
        this.rawData = rawData;
        this.preferences = prefs;
        this.propertyExtractors = getPropertyExtractors();
        this.validityExtractors = getValidityExtractors();
        this.fileNameExtractors = getFileNameExtractors();
        this.postCharset = getPostCharSet();
    }
    
    
    
    public String getPostCharSet() {
        String charSet = "FILTER";
        try { charSet = preferences.getStringIn("fragment.filter"); }
        catch (Exception e) {}
        return charSet;
    }
    
    
    
    public String[][] getPropertyExtractors() {
        String[][] extractors = new String[][] {};
        Hashtable extractorHash = new Hashtable();
        if(preferences != null) {
            Enumeration keys = preferences.keys();
            while(keys.hasMoreElements()) {
                try {
                    String key = (String) keys.nextElement();
                    if(key.startsWith("fragment.extractor.")) {
                        String value = preferences.getStringIn(key);
                        extractorHash.put(key.substring("fragment.extractor.".length()), value);
                    }
                }
                catch (Exception e) {}
            }
            extractors = Toolbox.hash2StringTable(extractorHash, true);
        }
        return extractors;
    }
    
    
    public void setFileName(String fileName) {
        sourceFileName = fileName;
    }
    
    
    
    public String[] getValidityExtractors() {
        return getExtractors("fragment.validity.extractor.");    
    }
    
    
    public String[] getFileNameExtractors() {
        return getExtractors("fragment.filename.extractor.");    
    }
    
        
    
    public String[] getExtractors(String prefix) {
        String[] extractors = new String[] {};
        Vector extractorVector = new Vector();
        String extractor = "";
        boolean stillMore = true;
        
        if(preferences != null) {
            for(int i=0; i<10 || stillMore; i++) {
                try {
                    extractor = preferences.getStringIn(prefix + i);
                    if (extractor != null && extractor.length() > 0) {
                        extractorVector.add(extractor);
                        stillMore = true;
                    }
                    else {
                        stillMore = false;
                    }
                }
                catch (Exception e) {
                    stillMore = false;
                }
            }
            extractors = Toolbox.vectorToArray(extractorVector);
        }
        return extractors;    
    }
    
    
    
    // -------------------------------------------------------------------------
    

    public void print() {
        String propertyName;
        Extractor extractor;
        Object extraction;
        Vector vectorExtraction;
        
        LogWriter.println("--- Fragment ---------------------------------------------------------------");
        
        for (int i=0; i<propertyExtractors.length; i++) {
            propertyName = propertyExtractors[i][0];
            extractor = getExtractor(propertyExtractors[i][1]);
            
            if(extractor != null) {
                extraction = extractor.extract(rawData);
                
                if(extraction instanceof String) {
                    LogWriter.println(propertyName + " = " + (String) extraction);
                }

                else if(extraction instanceof Vector) {
                    vectorExtraction = (Vector) extraction;
                    if (vectorExtraction != null) {
                        for(int j=0; j<vectorExtraction.size(); j++) {
                            LogWriter.println(propertyName + " = '" + (String) vectorExtraction.elementAt(j) + "'");
                        }
                    }
                }

                else {
                    LogWriter.println(propertyName + " = " + extraction + " (CLASS=" + extraction.getClass().toString() + ")");
                }
            }
            else {
                LogWriter.println("No extractor for property '" + propertyName + "'. Tried '" + propertyExtractors[i][1] + "'.");
            }
        }
    }
    
    
    public Hashtable getAll() {
        Hashtable data = new Hashtable();
        String propertyName = null;
        Extractor extractor;
        Object extraction;
        
        if(rawData != null) {
            data.put("raw", rawData);
            if(sourceFileName != null) data.put("sourcefilename", sourceFileName);
            for (int i=0; i<propertyExtractors.length; i++) {
                try {
                    propertyName = propertyExtractors[i][0];
                    extractor = getExtractor(propertyExtractors[i][1]);
                    if (extractor != null) {
                        extraction = extractor.extract(rawData);
                        if(extraction != null) {
                            extraction = polishExtraction(extraction);
                            data.put(propertyName, extraction);
                        }
                        else {
                            // LogWriter.println("No extractor for property '" + propertyName + "'.");
                        }
                    }
                    else {
                        LogWriter.println("No extractor for property '" + propertyName + "'.");
                    }
                }
                catch (Exception e) {
                    LogWriter.println("Exception '" + e.toString() + "' occurred while extracting " + propertyName + ".");
                }
            }
        }
        return data;
    }
    

    public Object polishExtraction(Object extraction) {
        if(extraction != null) {
            if(extraction instanceof Vector) extraction = Textbox.encode((Vector) extraction, postCharset);
            if(extraction instanceof String) extraction = Textbox.encode((String) extraction, postCharset);
        }
        return extraction;
    }
    
    
    // -------------------------------------------------------------------------
    
       
    public boolean isValid() {
        Extractor extractor;
        Object extraction;
        
        for (int i=0; i<validityExtractors.length && isValid; i++) {
            extractor = getExtractor(validityExtractors[i]);
            
            if(extractor != null) {
                extraction = extractor.extract(rawData);
                if(extraction == null) {
                    isValid = false;
                }
                if(extraction instanceof String) {
                    isValid = isValid && ((String) extraction).length() > 0;
                }
                else if(extraction instanceof Vector) {
                    isValid = isValid && ((Vector) extraction).size() > 0;
                }
            }
            else {
                LogWriter.println("Failed to solve validity using 'null' extractor.");
            }
        }
        
        return isValid;
    }
    
    // -------------------------------------------------------------------------
    
 
    
    public Object getProperty(String propertyName) {
        int propertyIndex = findPropertyIndex(propertyName);
        if(propertyIndex != -1) {
            Extractor extractor = getExtractor(propertyExtractors[propertyIndex][1]);
            if(extractor != null) return polishExtraction(extractor.extract(rawData));
        }
        return null;
    }
    
    
    
    
    
    public int findPropertyIndex(String propertyName) {
        for (int i=0; i<propertyExtractors.length; i++) {
            if (propertyName.equals(propertyExtractors[i][0])) return i;
        }
        return -1;
    }

    
    // -------------------------------------------------------------------------
    
    
    private Hashtable extractorCache = new Hashtable();
    
   
    
    public Extractor getExtractor(String extractorClassName) {

        Extractor extractor = (Extractor) extractorCache.get(extractorClassName);
        if (extractor == null) {
            try {
                Class extractorClass = Class.forName(extractorClassName);
                extractor = (Extractor) extractorClass.newInstance();
                extractorCache.put(extractorClassName, extractor);
            } catch (ClassNotFoundException e1) {
                // LogWriter.println("Class not found for the extractor class '" + extractorClassName + "'!");
            } catch (InstantiationException e2) {
                LogWriter.println("Extractor object can not be instantiated for class '" + extractorClassName + "'!");
            } catch (IllegalAccessException e3) {
                LogWriter.println("Extractor class '" + extractorClassName + "' can not be accessed here!");
            } catch (ClassCastException e3) {
                LogWriter.println("Extractor class '" + extractorClassName + "' does not implement the Extractor interface!");
            } catch (Exception e4) {
                LogWriter.println("General exception while solving Extractor '" + extractorClassName + "'!");
            }
        }
        return extractor;
    }

    
    // -------------------------------------------------------------------------
    
    
    public void exportRaw() {
        exportRaw(solveFileName());
    }
    
    
    public void exportRaw(String filename) {
        try {
            IObox.saveFile(filename, rawData);
        }
        catch (Exception e) {
            LogWriter.println("Unable to save raw fragment to a file '" + filename + "'!");
        }
    }


    // ------
    
    
    public void exportVelocity(String templateName, String templateEncoding) {
        exportVelocity(templateName, templateEncoding, solveFileName());
    }
    public void exportVelocity(String templateName, String templateEncoding, String filename) {
        exportVelocity(templateName, templateEncoding, solveFileName(), null);
    }
    public void exportVelocity(String templateName, String templateEncoding, String filename, Object codec) {
        exportVelocity(null, templateName, templateEncoding, solveFileName(), null);
    }
    
    
    public void exportVelocity(VelocityEngine velocityEngine, String templateName, String templateEncoding, String filename, Object codec) {
        VelocityContext context;
        Template template;
        FileWriter writer;
        File templateFile;
        String templatePath;

        try {           
            templateFile = new File(templateName);
            templatePath = templateFile.getParent();
            writer = new FileWriter(filename);
            if (velocityEngine == null) velocityEngine = new VelocityEngine();
            velocityEngine.setProperty("file.resource.loader.path", templatePath );
            //velocityEngine.setProperty("runtime.log.error.stacktrace", "false" );
            //velocityEngine.setProperty("runtime.log.warn.stacktrace", "false" );
            //velocityEngine.setProperty("runtime.log.info.stacktrace", "false" );
            velocityEngine.init();
            context = new VelocityContext();
            context.put("fragment", getAll());
            if(codec != null) context.put("codec", codec);
            if(templateEncoding != null && templateEncoding.length()>0) template = velocityEngine.getTemplate(templateFile.getName(), templateEncoding);
            else template = velocityEngine.getTemplate(templateFile.getName());
            template.merge( context, writer );
            writer.flush();
            writer.close();
        }
        catch( ResourceNotFoundException rnfe ) {
            // couldn't find the template
            LogWriter.println("ERR", "Unable to find the template file '" + templateName + "' for velocity export!");
        }
        catch( ParseErrorException pee ) {
            // syntax error : problem parsing the template
            LogWriter.println("ERR", "Unable to parse the velocity template file '" + templateName + "'!");
        }
        catch( MethodInvocationException mie ) {
            // something invoked in the template threw an exception
            LogWriter.println("ERR", "Velocity template '" + templateName + "' causes method invocation exception!");
        }
        catch( Exception e ) {
            LogWriter.println("ERR", "Exception '" + e.toString() + "' occurred while working with velocity template '" + templateName + "'!");        
            e.printStackTrace();
        }

    }

    
    
    
    // -------------------------------------------------------------------------
    
    public String solveFileName() {
        String delimiter = "_";
        StringBuffer fileName = new StringBuffer();
        Object extraction;
        Vector extractVector;
        String extractString;
        
        for (int i=0; i<fileNameExtractors.length; i++) {
            try {
                extraction = getExtractor(fileNameExtractors[i]).extract(rawData);
                if(extraction instanceof Vector) {
                    extractVector = (Vector) extraction;
                    if (extractVector != null) {
                        extractString = (String) extractVector.elementAt(0);
                        if (extractString.length() > 0) {
                            if(i > 0) fileName.append(delimiter);
                            fileName.append(extractString);
                        }
                    }
                }
                else if(extraction instanceof String) {
                    extractString = (String) extraction;
                    if (extractString != null && extractString.length()>0) {
                        if(i > 0) fileName.append(delimiter);
                        fileName.append(extractString);
                    }
                }
            }
            catch (Exception e) {
            }
        }
        if(fileName.length() == 0) {
            LogWriter.println("WRN", "Filename for export has zero length!");
        }
        return fileName.toString();
    }

}
