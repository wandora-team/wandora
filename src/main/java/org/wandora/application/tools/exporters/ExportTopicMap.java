/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * ExportTopicMap.java
 *
 * Created on 2. helmikuuta 2006, 15:05
 *
 */

package org.wandora.application.tools.exporters;



import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.filechooser.JTMFileFilter;
import org.wandora.application.gui.filechooser.LTMFileFilter;
import org.wandora.application.gui.filechooser.TopicMapFileChooser;
import org.wandora.application.gui.filechooser.XTM1FileFilter;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.IObox;




/**
 *
 * @author akivela
 */
public class ExportTopicMap extends AbstractExportTool implements WandoraTool {
	

	private static final long serialVersionUID = 1L;

	
	public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    
    public ExportTopicMap() {
    }
    public ExportTopicMap(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_topicmap.png");
    }
    
    

    @Override
    public void execute(Wandora wandora, Context context) {
        TopicMap tm = null;
        String topicMapName = null;
        String exportInfo = "";

        // --- Solve first topic map to be exported
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as topic map";
        }
        else {
            tm = solveContextTopicMap(wandora, context);
            topicMapName = this.solveNameForTopicMap(wandora, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "'";
            }
            else {
                exportInfo =  "Exporting topic map";
            }
        }
        
        // --- Then solve target file (and format)
        TopicMapFileChooser chooser=new TopicMapFileChooser();
        chooser.setDialogTitle(exportInfo+"...");
        
        if(chooser.open(wandora, "Export")==TopicMapFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            String fileName = file.getName();
            String fileNameUpper = file.getName().toUpperCase();
            javax.swing.filechooser.FileFilter fileFilter = chooser.getFileFilter();
            
            // --- Finally write topicmap to chosen file
            try {
                OutputStream out=null;
                if(fileFilter instanceof JTMFileFilter || fileNameUpper.endsWith(".JTM")) {
                    file=IObox.forceFileExtension(file, "jtm");
                    fileName = file.getName();
                    out=new FileOutputStream(file);
                    log(exportInfo+" in JTM format to '"+fileName+"'.");
                    tm.exportJTM(out, getCurrentLogger());
                }
                else if(fileFilter instanceof LTMFileFilter || fileNameUpper.endsWith(".LTM")) {
                    file=IObox.forceFileExtension(file, "ltm");
                    fileName = file.getName();
                    out=new FileOutputStream(file);
                    log(exportInfo+" in LTM format to '"+fileName+"'.");
                    tm.exportLTM(out, getCurrentLogger());
                }
                else if(fileFilter instanceof XTM1FileFilter || fileNameUpper.endsWith(".XTM1") || fileNameUpper.endsWith(".XTM10")){
                    file=IObox.forceFileExtension(file, "xtm1");
                    fileName = file.getName();
                    out=new FileOutputStream(file);
                    log(exportInfo+" in XTM 1.0 format to '"+fileName+"'.");
                    tm.exportXTM10(out, getCurrentLogger());
                }
                else {
                    file=IObox.addFileExtension(file, "xtm"); // Ensure file extension exists!
                    fileName = file.getName(); // Updating filename if file has changed!
                    out=new FileOutputStream(file);
                    log(exportInfo+" in XTM 2.0 format to '"+fileName+"'.");
                    //System.out.println("tm == "+ tm);
                    tm.exportXTM20(out, getCurrentLogger());
                }
                if(out != null) out.close();
                log("Ready.");
            }
            catch(Exception e) {
                log(e);
            }
            setState(WAIT);
        }
    }
    

    @Override
    public String getName() {
        return "Export Topic Map";
    }
    @Override
    public String getDescription() {
        return "Exports a topic map file. " +
               "Wandora supports XTM 1.0, XTM 2.0, LTM and JTM topic map file formats.";
    }

    
}
