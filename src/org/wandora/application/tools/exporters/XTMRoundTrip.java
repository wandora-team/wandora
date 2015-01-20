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

package org.wandora.application.tools.exporters;

import org.wandora.utils.IObox;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.io.*;
import org.wandora.application.gui.simple.*;
import javax.swing.*;


/**
 *
 * @author akivela
 */
public class XTMRoundTrip extends AbstractWandoraTool implements WandoraTool {

    @Override
    public String getName() {
        return "XTM round trip";
    }
    @Override
    public String getDescription() {
        return "Imports XTM topic map file, and exports topic map back to XTM file. "+
                "Exported XTM files are saved to 'wandora_round_trip' folder with same file name leaving original files untouched.";
    }
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createImportExportType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_export_xtm.png");
    }
    @Override
    public boolean requiresRefresh() {
        return false;
    }

    
    @Override
    public void execute(Wandora admin, Context context) {      
        try {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle("Select XTM files to round trip");
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
                    setLogTitle("Importing '"+importFile.getName()+"'");
                    log("Importing '"+importFile.getName()+"'");
                    map.importXTM(new FileInputStream(importFile), getCurrentLogger());
                    
                    String exportPath = importFile.getParent()+File.separator+"wandora_round_trip";
                    IObox.createPathFor(new File(exportPath));
                    String exportFileName = exportPath+File.separator+importFile.getName();
                            
                    File exportFile = new File(exportFileName);
                    log("Exporting '"+exportFile.getName()+"'");
                    setLogTitle("Exporting '"+exportFile.getName()+"'");
                    map.exportXTM(exportFileName, this);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            long endtime = System.currentTimeMillis();
            long duration = (Math.round(((endtime-starttime)/1000)));
            if(duration > 1) {
                log("Round tripping XTM files took "+duration+" seconds.");
            }
            log("Ready.");
        }
        else {
            log("No XTM files to import!");
        }
    }
    
}
