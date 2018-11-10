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
package org.wandora.application.tools.fng.opendata.simberg;

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
import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.topicmap.TopicMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author akivela
 */


public class SimbergOpenDataExporter extends AbstractExportTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	public static boolean CHECK_EXPORTED_FILE = true;
    
    
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
        return false;
    }


    // -------------------------------------------------------------------------
    
    

    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = solveContextTopicMap(admin, context);
        topicMapName = this.solveNameForTopicMap(admin, tm);
        if(topicMapName != null) {
            exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as FNG Simberg open data";
        }
        else {
            exportInfo =  "Exporting topic map as FNG Simberg open data";
            topicMapName = "no_name_topic_map";
        }

        // --- Then solve target file (and format)
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");

        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            String fileName = file.getName();

            OutputStream out=null;
            try {
                fileName = file.getName(); // Updating filename if file has changed!
                out=new FileOutputStream(file);
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
                } 
                catch (UnsupportedEncodingException ex) {
                    log(ex);
                }
                
                log(exportInfo+" to '"+fileName+"'.");
                //System.out.println("tm == "+ tm);
                
                SimbergOpenDataHandler data = new SimbergOpenDataHandler(tm, getCurrentLogger());
                if(forceStop()) {
                    log("Aborting.");
                    return;
                }

                if(fileName.toLowerCase().endsWith(".json")) {
                    data.exportJSON(writer, getCurrentLogger());
                }
                else if(fileName.toLowerCase().endsWith(".xml")) {
                    data.exportXML(writer, getCurrentLogger());
                }
                else {
                    data.exportCSV(writer, getCurrentLogger());
                }

                if(writer != null){
                    writer.flush();
                    writer.close();
                }
                
                if(CHECK_EXPORTED_FILE) {
                    log("Checking exported file.");
                    if(fileName.toLowerCase().endsWith(".json")) {
                        try {
                            JSONParser parser = new JSONParser();
                            parser.parse(new FileReader(file));
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
                            log("Error while parsing XML file.");
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
        return "Export FNG Simberg open data";
    }

    @Override
    public String getDescription() {
        return "Export FNG Simberg open data file.";
    }
    
    

}
