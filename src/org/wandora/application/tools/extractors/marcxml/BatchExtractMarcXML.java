/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
 * MarcField.java
 *
 * Created on 2010-06-30
 *
 */

package org.wandora.application.tools.extractors.marcxml;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.*;
import org.wandora.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;
import org.wandora.utils.Tuples.*;
import org.wandora.application.contexts.*;

import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author akivela
 */
public class BatchExtractMarcXML extends AbstractWandoraTool {

    @Override
    public String getName() {
        return "MarcXML batch extractor";
    }

    @Override
    public String getDescription(){
        return "Batch converts MarcXML documents to XTM topics maps.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_marcxml.png");
    }





    @Override
    public void execute(Wandora admin, Context context) {
        try {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle("Select MARCXML files to convert");
            if(chooser.open(admin)==SimpleFileChooser.APPROVE_OPTION) {
                setDefaultLogger();
                importExport(chooser.getSelectedFiles());
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }


    private void importExport(File[] importFiles) {
        if(importFiles != null && importFiles.length > 0) {
            long starttime = System.currentTimeMillis();
            for(int i=0; i<importFiles.length && !forceStop(); i++) {
                try {
                    TopicMap map = new org.wandora.topicmap.memory.TopicMapImpl();

                    File importFile = importFiles[i];
                    setLogTitle("Extracting '"+importFile.getName()+"'");
                    log("Extracting '"+importFile.getName()+"'");
                    MarcXMLExtractor importer = new MarcXMLExtractor();
                    importer.setToolLogger(this.getCurrentLogger());
                    importer._extractTopicsFrom(importFile, map);

                    String exportPath = importFile.getParent()+File.separator+"wandora_export";
                    IObox.createPathFor(new File(exportPath));

                    String n = importFile.getName();
                    if(n.toLowerCase().endsWith(".xml")) {
                        n = importFile.getName();
                        n = n.substring(0, n.length()-4);
                        n = n + ".xtm";
                    }
                    String exportFileName = exportPath+File.separator+n;

                    log("Exporting '"+n+"'");
                    setLogTitle("Exporting '"+n+"'");
                    map.exportXTM(exportFileName, this.getCurrentLogger());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            long endtime = System.currentTimeMillis();
            long duration = (Math.round(((endtime-starttime)/1000)));
            if(duration > 1) {
                log("Batch conversion of MARCXML files took "+duration+" seconds.");
            }
            log("Ready.");
        }
        else {
            System.out.println("No MARCXML files to import!");
        }
    }
}
