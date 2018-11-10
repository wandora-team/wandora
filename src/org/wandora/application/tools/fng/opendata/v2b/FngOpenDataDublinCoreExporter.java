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
package org.wandora.application.tools.fng.opendata.v2b;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.application.tools.exporters.DOTExport;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



/**
 *
 * @author akivela
 */


public class FngOpenDataDublinCoreExporter extends AbstractExportTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;
    
    public static boolean EXPORT_ARTISTS = true;
    public static boolean EXPORT_ARTWORKS = true;
    
    public static boolean CHECK_EXPORTED_FILE = true;
    
    public static boolean EXPORT_VTTK = false;
    
    
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
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"FNG open data export options","FNG open data export options",true,new String[][]{
            new String[]{"Export artists","boolean",(EXPORT_ARTISTS ? "true" : "false"),"Should artists topics export?"},
            new String[]{"Export artworks","boolean",(EXPORT_ARTWORKS ? "true" : "false"),"Should artworks topics export?"},
            new String[]{"Check exported file","boolean",(CHECK_EXPORTED_FILE ? "true" : "false"),"Should check the exported file?"},
            new String[]{"Export VTTK instead","boolean",(EXPORT_VTTK ? "true" : "false"),"Export VTTK flavour?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXPORT_ARTISTS = ("true".equals(values.get("Export artists")) ? true : false );
        EXPORT_ARTWORKS = ("true".equals(values.get("Export artworks")) ? true : false );
        CHECK_EXPORTED_FILE = ("true".equals(values.get("Check exported file")) ? true : false );
        EXPORT_VTTK = ("true".equals(values.get("Export VTTK instead")) ? true : false );
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
                
                Collection<Topic> data = make(tm, getCurrentLogger());
                if(forceStop()) {
                    log("Aborting.");
                    out.close();
                    return;
                }
                
                if(fileName.toLowerCase().endsWith(".json")) {
                    exportJSON(out, data, tm, getCurrentLogger());
                }
                else if(fileName.toLowerCase().endsWith(".txt")) {
                    exportText(out, data, tm, getCurrentLogger());
                }
                else if(fileName.toLowerCase().endsWith(".ds.xml")) {
                    exportDSXML(out, data, tm, getCurrentLogger());
                }
                else {
                    exportXML(out, data, tm, getCurrentLogger());
                }

                if(out != null){
                    out.flush();
                    out.close();
                }
                
                if(CHECK_EXPORTED_FILE) {
                    log("Checking exported file.");
                    if(fileName.toLowerCase().endsWith(".json")) {
                        try {
                            JSONParser parser = new JSONParser();
                            Object obj = parser.parse(new FileReader(file));
                            log("JSON export parsed successfully.");
                        }
                        catch(Exception e) {
                            log("Error while parsing JSON file.");
                            log(e);
                        }
                    }
                    else if(fileName.toLowerCase().endsWith(".xml")) {
                        try {
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            factory.setValidating(false);
                            factory.setNamespaceAware(true);

                            DocumentBuilder builder = factory.newDocumentBuilder();

                            builder.setErrorHandler(
                                new DefaultHandler() {
                                    @Override
                                    public void warning(SAXParseException e) throws SAXException {
                                        log(e);
                                    }

                                    @Override
                                    public void error(SAXParseException e) throws SAXException {
                                        log(e);
                                    }

                                    @Override
                                    public void fatalError(SAXParseException e) throws SAXException {
                                        log(e);
                                    }
                                }
                            );    
                            // the "parse" method also validates XML, will throw an exception if misformatted
                            InputStream in = new FileInputStream(file);
                            Document document = builder.parse(new InputSource(new InputStreamReader(in)));
                            log("XML export parsed successfully.");
                        }
                        catch(Exception e) {
                            log("Error while parsing JSON file.");
                            log(e);
                        }
                    }
                }
                
                log("Ready.");
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
    

    private String ARTWORK_SI = "http://kansallisgalleria.fi/Teos";
    private String ARTIST_SI = "http://kansallisgalleria.fi/taiteilija";

    
    public Collection<Topic> make(TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;

        Collection<Topic> data = new ArrayList<Topic>();

        if(tm != null) {
            if(EXPORT_ARTWORKS && !forceStop()) {
                logger.log("Collecting artworks data.");
                data.addAll(tm.getTopicsOfType(ARTWORK_SI));
            }
            if(EXPORT_ARTISTS && !forceStop()) {
                logger.log("Collecting artists data.");
                data.addAll(tm.getTopicsOfType(ARTIST_SI));
            }
        }
        return data;
    }
    
    
    
    // -------------------------------------------------------------------------
    

    
    public void exportXML(OutputStream out, Collection<Topic> data, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
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
        for( Topic topic : data ) {
            if(forceStop()) break;
            if(topic != null && !topic.isRemoved()) {
                print(writer, topicToString(topic, tm, "dc-xml"));
            }
            logger.setProgress(p++);
        }
        
        println(writer, "</dcx:descriptionSet>");
        writer.flush();
        writer.close();
    }
    
    public void exportDSXML(OutputStream out, Collection<Topic> data, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
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
        for( Topic topic : data ) {
            if(forceStop()) break;
            if(topic != null && !topic.isRemoved()) {
                print(writer, topicToString(topic, tm, "dc-ds-xml"));
            }
            logger.setProgress(p++);
        }
        
        println(writer, "</dcds:descriptionSet>");
        writer.flush();
        writer.close();
    }
    
    
    
    public void exportJSON(OutputStream out, Collection<Topic> data, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
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
        
        println(writer, "{");
        println(writer, "\"descriptionSet\": [");
        for( Topic topic : data ) {
            if(forceStop()) break;
            if(topic != null && !topic.isRemoved()) {
                print(writer, topicToString(topic, tm, "dc-json"));
            }
            logger.setProgress(p++);
            if(p < n) println(writer, ",");
        }
        println(writer, "]");
        println(writer, "}");
        writer.flush();
        writer.close();
    }
    
    
    
    
    
    public void exportText(OutputStream out, Collection<Topic> data, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
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
        for( Topic topic : data ) {
            if(forceStop()) break;
            if(topic != null && !topic.isRemoved()) {
                print(writer, topicToString(topic, tm, "dc-text"));
            }
            logger.setProgress(p++);
        }
        println(writer, ")");
        writer.flush();
        writer.close();
    }
    
    
    
    
    
    public String topicToString(Topic topic, TopicMap tm, String format) {
        if(EXPORT_VTTK) return (new VttkOpenDataStruct()).toString(topic, tm, format);
        else return (new FngOpenDataStruct()).toString(topic, tm, format);
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
