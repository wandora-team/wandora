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
 * ExportRemoteTopicMap.java
 *
 * Created on June 18, 2004, 3:36 PM
 */

package org.wandora.application.tools.exporters;



import org.wandora.application.gui.filechooser.TopicMapFileChooser;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.remote.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class ExportRemoteTopicMap extends AbstractExportTool implements WandoraTool {
    
    
    /** Creates a new instance of ImportTopicMap */
    public ExportRemoteTopicMap() {
    }
    
    
    public String getName() {
        return "Export Remote Topic Map";
    }
    public String getDescription() {
        return "Exports remote topic map layer to a local topic map file.";
    }

    
    public void execute(Wandora admin, Context context) {
        TopicMap topicMap = solveContextTopicMap(admin, context);
        
        if(topicMap instanceof RemoteTopicMap) {
            String topicMapName = solveNameForTopicMap(admin, topicMap);
            RemoteTopicMap remoteTopicMap = (RemoteTopicMap) topicMap;
            
            // --- Then solve target file (and format)
            TopicMapFileChooser chooser=new TopicMapFileChooser();
            if(topicMapName == null) {
                chooser.setDialogTitle("Export remote topic map...");
            }
            else {
                chooser.setDialogTitle("Export remote topic map in layer '" + topicMapName + "'...");
            }
            
            if(chooser.open(admin, "Export") == TopicMapFileChooser.APPROVE_OPTION){
                setDefaultLogger();
                try {
                    File file = chooser.getSelectedFile();
                    log("Exporting remote topic map...");
                    OutputStream out=new FileOutputStream(file);
                    remoteTopicMap.writeTopicMapTo(out);
                    out.close();
                    log("Done");
                    setState(WAIT);
                }
                catch(IOException e){
                    log(e);
                }
                catch(ServerException se) {
                    log(se);
                    //parent.handleError(se);
                }
            }
        }
        else {
            log("Layer topic map type is not remote! Unable to export other types of topic maps.");
        }
    }
    


}
