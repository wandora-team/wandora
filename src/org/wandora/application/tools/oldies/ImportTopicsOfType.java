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
 * ImportTopicsOfType.java
 *
 * Created on July 28, 2004, 11:14 AM
 */

package org.wandora.application.tools.oldies;

import org.wandora.piccolo.WandoraManager;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  olli
 */
public class ImportTopicsOfType extends AbstractWandoraTool implements WandoraTool {
    private String typeName;
    private String typeSI;
    private boolean shallow;
    /** Creates a new instance of ImportTopicsOfType */
    public ImportTopicsOfType(String type,String typeSI,boolean shallow) {
        this.typeName=type;
        this.typeSI=typeSI;
        this.shallow=shallow;
    }
    
    public String getName() {
        return "Import topics of type "+typeName;
    }
    
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        if(chooser.open(admin, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION){
            work(admin, chooser.getSelectedFile());
        }
    }
    
    private Topic copyShallow(TopicMap workspace,Topic t) throws TopicMapException {
        Topic edit=null;
        Collection merging=workspace.getMergingTopics(t);
        if(!merging.isEmpty()) edit=(Topic)merging.iterator().next();
        if(edit==null){
            edit=workspace.createTopic();
        }
        Iterator iter2=t.getSubjectIdentifiers().iterator();
        while(iter2.hasNext()){
            Locator l=(Locator)iter2.next();
            edit.addSubjectIdentifier(l);
        }
        edit.setBaseName(t.getBaseName());
        return edit;
    }

    
    
    public void work(Wandora parent,File file)  throws TopicMapException {
        try{
            log("Reading topic map...");
            InputStream in=new FileInputStream(file);
            TopicMap temp=new org.wandora.topicmap.memory.TopicMapImpl();
            temp.importXTM(in);
            Topic roleCategories=temp.getTopic(WandoraManager.ASSOCIATIONROLECATEGORIES_SI);
            in.close();
            log("Copying topics...");
            TopicMap workspace=parent.getTopicMap();
            Topic type=temp.getTopic(typeSI);
            if(type!=null){

                Topic editType=null;
                if(shallow){
                    editType=copyShallow(workspace,type);
                }
                else{
                    editType=workspace.copyTopicIn(type, false);
                }

                Collection c=temp.getTopicsOfType(type);
                Iterator iter=c.iterator();
                while(iter.hasNext()){
                    Topic t=(Topic)iter.next();
                    Topic edit=null;
                    if(shallow){
                        edit=copyShallow(workspace,t);
                        edit.addType(editType);
                    }
                    else{
                        edit=workspace.copyTopicIn(t,false);
                    }
                }
            }
            else{
                WandoraOptionPane.showMessageDialog(parent,"Type not found in topicmap. Nothing copied.");
            }
            log("Ready.");
            try { parent.doRefresh(); }
            catch(Exception e) {}
        }
        catch(IOException e){
            WandoraOptionPane.showMessageDialog(parent,"Reading topic map failed: "+e.getMessage());
        }
    }        
   
 
}
