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
 * CommitStoredSession.java
 *
 * Created on September 9, 2004, 12:24 PM
 */

package org.wandora.application.tools;


import org.wandora.topicmap.remote.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.contexts.*;
import java.io.*;


/**
 *
 * @author  olli
 */
public class CommitStoredSession extends AbstractWandoraTool implements WandoraTool {
    
    RemoteTopicMap remoteTopicMap = null;
    

    @Override
    public String getName() {
        return "Commit Stored Session";
    }

    @Override
    public String getDescription() {
        return "Tool is used to commit remote topic map session ie. changes to the topic map. "+
               "Succesful execution requires a saved session file.";
    }
    
    public void execute(Wandora admin, Context context) {
        Object contextSource = context.getContextSource();
        if(contextSource instanceof LayerStatusPanel) {
            Layer l = ((LayerStatusPanel) contextSource).getLayer();
            if(l.getTopicMap() instanceof RemoteTopicMap) {
                remoteTopicMap = (RemoteTopicMap) l.getTopicMap();
                try {
                    if(remoteTopicMap.isUncommitted()){
                        if(WandoraOptionPane.showConfirmDialog(admin,"Uncommitted changes exist. Are you sure you want to rollback?","Uncommitted changes",WandoraOptionPane.YES_NO_OPTION) != WandoraOptionPane.YES_OPTION){
                            return;
                        }
                    }
                    remoteTopicMap.rollback();
                }
                catch(ServerException se){
                    admin.handleError(se);
                    return;
                }

                SimpleFileChooser chooser=UIConstants.getFileChooser();
                if(chooser.open(admin, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION){
                    setDefaultLogger();
                    work(admin, chooser.getSelectedFile());
                }
            }
        }
    }
    
    public static String readLine(InputStream in) throws IOException {
        byte[] read=new byte[256];
        int ptr=0;
        int c;
        do{
            c=in.read();
            if(c==-1) throw new IOException("Socket closed");
            if(c!='\n'){
                if(ptr==read.length){
                    byte[] nread=new byte[read.length*2];
                    System.arraycopy(read, 0, nread, 0, read.length);
                    read=nread;
                }
                read[ptr++]=(byte)c;
            }
        }while(c!='\n');
        return new String(read,0,ptr, "UTF-8");
    }

    
    public void work(Wandora parent,File file) {
        try{               
            log("Reading session");
            InputStream in=new FileInputStream(file);
            String line=readLine(in);
            StringBuffer session=new StringBuffer();
            while(line!=null && !line.equals("merge")){
                session.append(line);
                session.append("\n");
                line=readLine(in);
            }
            TopicMap tm=new org.wandora.topicmap.memory.TopicMapImpl();
            tm.importXTM(in);
            in.close();
            log("Commiting session");
            remoteTopicMap.commitSession(session,tm);
            log("Done");
        } catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }

    
       
}
