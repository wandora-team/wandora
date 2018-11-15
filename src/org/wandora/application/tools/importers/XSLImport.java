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
 * XSLImport.java
 *
 * Created on October 4, 2004, 3:18 PM
 */

package org.wandora.application.tools.importers;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.io.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.swing.*;


/**
 *
 * @author  olli, akivela
 */
public class XSLImport extends AbstractImportTool implements WandoraTool {



	private static final long serialVersionUID = 1L;

	String forceXSL = null;
    String forceXML = null;
    
    
    /**
     * Creates a new instance of XSLImport
     */
    public XSLImport() {
    }
    public XSLImport(int options) {
        setOptions(options); 
    }
    public XSLImport(String xsl, String xml) {
        forceXSL = xsl;
        forceXML = xml;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        String xmlIn = null;
        String xslIn = null;
        XSLImportDialog d = null;
        
        if(forceXSL == null || forceXML == null) {
            d = new XSLImportDialog(wandora,true);
            xmlIn = d.getXML();
            xslIn = d.getXSL();
        }
        else {
            xmlIn = forceXML;
            xslIn = forceXSL;
        }
        
        
        if(d == null || d.accept) {
            setDefaultLogger();
            if(resetWandoraFirst) {
                log("Resetting Wandora!");
                wandora.resetWandora();
            }
            
            final Wandora fadmin = wandora;
            final String fxmlIn = xmlIn;
            final String fxslIn = xslIn;
            
            try {
                final Transformer trans=TransformerFactory.newInstance().newTransformer(new StreamSource(createInputStreamFor(xslIn)));
                final Source xmlSource=new StreamSource(createInputStreamFor(xmlIn));

                final PipedOutputStream pout=new PipedOutputStream();
                final PipedInputStream pin=new PipedInputStream(pout);
                
                final XSLImport thisf = this;
                
                // --- TRANSFORMING ---
                Thread transformThread=new Thread(){
                    public void run() {
                        try {
                            thisf.log("Transforming XML '" + fxmlIn + "'." );
                            trans.transform(xmlSource, new StreamResult(pout));
                            pout.close();
                            thisf.log("Transformation succeeded!" );
                        }
                        catch(Exception e) {
                            thisf.log("Transformation fails!" );
                            thisf.log(e);
                        }
                    }
                };
                transformThread.start();
                
                
                // --- PARSING ---
                Thread parserThread=new Thread(){
                    public void run(){
                        try {
                            try {
                                fadmin.getTopicMap().clearTopicMapIndexes();
                            }
                            catch(Exception e) {
                                log(e);
                            }
                            
                            String filename = fxmlIn;
                            TopicMap map = null;
                            if(directMerge) {
                                map = thisf.solveContextTopicMap(fadmin, getContext());
                            }
                            else {
                                map = new org.wandora.topicmap.memory.TopicMapImpl();
                            }
                            
                            map.importXTM(pin);

                            if(!directMerge) {
                                if(newLayer) {
                                    createNewLayer(map, filename, fadmin);
                                }
                                else {
                                    thisf.log("Merging '" + filename + "'.");
                                    solveContextTopicMap(fadmin, getContext()).mergeIn(map);
                                }
                            }
                            pin.close();
                            thisf.log("Ready.");
                        }
                        catch(TopicMapReadOnlyException tmroe) {
                            thisf.log("Topic map is write protected. Import failed.");
                        }
                        catch(Exception e){
                            thisf.log(e);
                        }
                    }
                };
                parserThread.start();

            }
            catch(Exception e){
                log(e);
            }
        }
    }
    
    @Override
    public String getName() {
        return "XSL transformer Topic Maps import";
    }
    
    @Override
    public String getDescription() {
        return "Apply XSL transformation to loaded XML file and merge resulting XTM topic map "+
               "to current layer.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/merge_xml.png");
    }
    
    
    @Override
    public void importStream(Wandora admin, String streamName, InputStream inputStream) {
        // --- XSL IMPORT HAS NO STREAM IMPORT! --------------------------------
    }


    private InputStream createInputStreamFor(String streamSource) {
        if(streamSource == null) return null;
        if(streamSource.startsWith("http:/") || streamSource.startsWith("https:/") || streamSource.startsWith("ftp:/")  || streamSource.startsWith("ftps:/")) {
            try {
                URL sourceURL = new URL(streamSource);
                return sourceURL.openStream();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        try {
            File sourceFile = new File(streamSource);
            return new FileInputStream(sourceFile);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
