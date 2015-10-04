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
 */
package org.wandora.application.tools.fng.opendata.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.json.simple.parser.JSONParser;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolLogger;
import static org.wandora.application.WandoraToolLogger.WAIT;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.application.tools.exporters.DOTExport;
import static org.wandora.application.tools.exporters.DOTExport.EXPORT_CLASSES;
import static org.wandora.application.tools.exporters.DOTExport.EXPORT_N_ASSOCIATIONS;
import static org.wandora.application.tools.exporters.DOTExport.EXPORT_OCCURRENCES;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;


/**
 *
 * @author akivela
 */


public class FngOpenDataDublinCoreExporter extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/fng.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        
        /*
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"FNG open data export options","FNG open data export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should topic classes also export?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Export n associations","boolean",(EXPORT_N_ASSOCIATIONS ? "true" : "false"), "Should associations with more than 2 players also export?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_N_ASSOCIATIONS = ("true".equals(values.get("Export n associations")) ? true : false );
        */
    }
    
    
    
    

    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as FNG open data";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(admin, context);
            topicMapName = this.solveNameForTopicMap(admin, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as FNG open data";
            }
            else {
                exportInfo =  "Exporting topic map as FNG open data";
                topicMapName = "no_name_topic_map";
            }
        }

        
        // --- Then solve target file (and format)
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");

        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            String fileName = file.getName();

            // --- Finally write the Dublin Core to chosen file
            OutputStream out=null;
            try {
                fileName = file.getName(); // Updating filename if file has changed!
                out=new FileOutputStream(file);
                log(exportInfo+" to '"+fileName+"'.");
                //System.out.println("tm == "+ tm);
                
                Collection<FngOpenDataStruct> data = make(tm, getCurrentLogger());
                
                if(fileName.toLowerCase().endsWith(".json")) {
                    exportJSON(out, data, getCurrentLogger());
                }
                else if(fileName.toLowerCase().endsWith(".txt")) {
                    exportText(out, data, getCurrentLogger());
                }
                else if(fileName.toLowerCase().endsWith(".ds.xml")) {
                    exportDSXML(out, data, getCurrentLogger());
                }
                else {
                    exportXML(out, data, getCurrentLogger());
                }

                if(out != null){
                    out.flush();
                    out.close();
                }
                log("Ready.");
                
                if(fileName.toLowerCase().endsWith(".json")) {
                    try {
                        JSONParser parser = new JSONParser();
                        Object obj = parser.parse(new FileReader(file));
                        log("Test OK!");
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                log(e);
                try { if(out != null) out.close(); }
                catch(Exception e2) { log(e2); }
            }
        }
        setState(WAIT);
    }
    
    
    
    
    @Override
    public String getName() {
        return "Export FNG open data";
    }

    @Override
    public String getDescription() {
        return "Export FNG open data Dublin Core file.";
    }
    
    
    
    // -------------------------------------------------------------------------
    

    private String ARTWORK_SI = "http://www.wandora.net/artwork";

    
    public Collection<FngOpenDataStruct> make(TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        logger.log("Creating data.");
        
        Collection<FngOpenDataStruct> data = new ArrayList<FngOpenDataStruct>();

        if(tm != null) {
            Collection<Topic> artworks = tm.getTopicsOfType(ARTWORK_SI);
            logger.setProgressMax(artworks.size());
            logger.setProgress(0);
            int p = 0;
            for( Topic artwork : artworks ) {
                if(artwork != null && !artwork.isRemoved()) {
                    FngOpenDataStruct struct = new FngOpenDataStruct();
                    struct.populate(artwork);
                    data.add(struct);
                    logger.setProgress(p++);
                }
            }
        }
        return data;
    }
    
    
    
    // -------------------------------------------------------------------------
    

    
    public void exportXML(OutputStream out, Collection<FngOpenDataStruct> data, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        logger.log("Saving XML data.");
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DOTExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String nameSpaces="xmlns:dcx=\"http://purl.org/dc/xml/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"";
        
        println(writer, "<?xml version=\"1.0\"?>");
        println(writer, "<dcx:descriptionSet "+nameSpaces+">");
        
        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( FngOpenDataStruct struct : data ) {
            print(writer, struct.toString("dc-xml"));
            logger.setProgress(p++);
        }
        
        println(writer, "</dcx:descriptionSet>");
        writer.flush();
        writer.close();
    }
    
    public void exportDSXML(OutputStream out, Collection<FngOpenDataStruct> data, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        logger.log("Saving DS-XML data.");
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DOTExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String nameSpaces="xmlns:dcds=\"http://purl.org/dc/xmlns/2008/09/01/dc-ds-xml/\" xml:base=\"http://purl.org/dc/terms/\"";
        
        println(writer, "<?xml version=\"1.0\"?>");
        println(writer, "<dcds:descriptionSet "+nameSpaces+">");
        
        logger.setProgressMax(data.size());
        logger.setProgress(0);
        int p = 0;
        for( FngOpenDataStruct struct : data ) {
            print(writer, struct.toString("dc-ds-xml"));
            //writer.flush();
            logger.setProgress(p++);
            
        }
        
        println(writer, "</dcds:descriptionSet>");
        writer.flush();
        writer.close();
    }
    
    
    
    public void exportJSON(OutputStream out, Collection<FngOpenDataStruct> data, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        logger.log("Saving JSON data.");
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DOTExport.class.getName()).log(Level.SEVERE, null, ex);
        }

        int n = data.size();
        logger.setProgressMax(n);
        int p = 0;
        logger.setProgress(p);
        
        println(writer, "{ \"descriptionSet\": [");
        for( FngOpenDataStruct struct : data ) {
            print(writer, struct.toString("dc-json"));
            logger.setProgress(p++);
            if(p < n) println(writer, ",");
        }
        println(writer, "] }");
        writer.flush();
        writer.close();
    }
    
    
    
    
    
    public void exportText(OutputStream out, Collection<FngOpenDataStruct> data, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        logger.log("Saving text data.");
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DOTExport.class.getName()).log(Level.SEVERE, null, ex);
        }

        int n = data.size();
        logger.setProgressMax(n);
        int p = 0;
        logger.setProgress(p);
        
        println(writer, "@prefix dcterms: <http://purl.org/dc/terms/> .");
        println(writer, "");
        
        println(writer, "DescriptionSet (");
        for( FngOpenDataStruct struct : data ) {
            print(writer, struct.toString("dc-text"));
            logger.setProgress(p++);
        }
        println(writer, ")");
        writer.flush();
        writer.close();
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    // Elementary print methods are used to ensure output is ISO-8859-1
    
    protected void print(PrintWriter writer, String str) {
        writer.print(str);
    }
    protected void println(PrintWriter writer, String str) {
        writer.println(str);
    }
    
    
    
    
    
    
    
}
