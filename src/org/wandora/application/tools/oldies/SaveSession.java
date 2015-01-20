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
 * SaveSession.java
 *
 * Created on September 9, 2004, 10:19 AM
 */

package org.wandora.application.tools.oldies;


import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.remote.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.*;
import java.io.*;


/**
 *
 * @author  olli
 */
public class SaveSession extends AbstractWandoraTool implements WandoraTool {
    
    RemoteTopicMap remoteTopicMap = null;
    
    
    /** Creates a new instance of SaveSession */
    public SaveSession() {
    }
    
    
    public String getName() {
        return "Save session";
    }
    
    
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Object contextSource = context.getContextSource();
        if(contextSource instanceof LayerStatusPanel) {
            Layer l = ((LayerStatusPanel) context).getLayer();
            if(l.getTopicMap() instanceof RemoteTopicMap) {
                remoteTopicMap = (RemoteTopicMap) l.getTopicMap();
                try{
                    admin.applyChanges();
                }catch(CancelledException ce){return;}

                SimpleFileChooser chooser=UIConstants.getFileChooser();
                if(chooser.open(admin, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                    setDefaultLogger();
                    File file = chooser.getSelectedFile();
                    try{
                        log("Saving session");
                        OutputStream out=new FileOutputStream(file);
                        StringBuffer session=remoteTopicMap.getSession();
                        out.write(session.toString().getBytes("UTF-8"));
                        out.write("merge\n".getBytes("UTF-8"));
                        remoteTopicMap.getEditedTopicMap().exportXTM(out);
                        out.close();
                        log("Done");
                    }
                    catch(IOException e){
                        log("Writing session failed!", e);
                    }
                    setState(WAIT);
                }
            }
        }
    }
    

     

}
