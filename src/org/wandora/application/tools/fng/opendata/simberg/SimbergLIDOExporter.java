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


package org.wandora.application.tools.fng.opendata.simberg;



import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipOutputStream;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.exporters.AbstractExportTool;
import org.wandora.topicmap.TopicMap;


/**
 *
 * @author akivela
 */


public class SimbergLIDOExporter extends AbstractExportTool implements WandoraTool {

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
            exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as FNG Simberg LIDO";
        }
        else {
            exportInfo =  "Exporting topic map as FNG Simberg LIDO";
            topicMapName = "no_name_topic_map";
        }

        

        // --- Then solve target file (and format)
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");

        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            String fileName = file.getName();

            FileOutputStream out=null;
            try {
                fileName = file.getName(); // Updating filename if file has changed!
                out=new FileOutputStream(file);
                ZipOutputStream zipStream = null;
                try {
                    zipStream = new ZipOutputStream(out);
                } 
                catch (Exception ex) {
                    log(ex);
                }
                
                log(exportInfo+" to '"+fileName+"'.");
                //System.out.println("tm == "+ tm);
                
                SimbergOpenDataHandler data = new SimbergOpenDataHandler(tm, getCurrentLogger());
                if(forceStop()) {
                    log("Aborting.");
                    return;
                }

                data.exportLIDO(zipStream, getCurrentLogger());

                if(zipStream != null){
                    zipStream.flush();
                    zipStream.close();
                }
                
                log("Done");
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
        return "Export FNG Simberg LIDO";
    }

    @Override
    public String getDescription() {
        return "Export FNG Simberg LIDO";
    }
    

}
