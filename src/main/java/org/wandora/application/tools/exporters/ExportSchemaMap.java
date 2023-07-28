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
 * ExportSchemaMap.java
 *
 * Created on August 26, 2004, 2:59 PM
 */

package org.wandora.application.tools.exporters;



import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;

import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 *
 * @author  olli
 */
public class ExportSchemaMap extends AbstractExportTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	
	public static String[] exportTypes=new String[]{
        SchemaBox.CONTENTTYPE_SI,
        SchemaBox.ASSOCIATIONTYPE_SI,
        SchemaBox.OCCURRENCETYPE_SI,
        SchemaBox.ROLE_SI,
        SchemaBox.ROLECLASS_SI,
        XTMPSI.LANGUAGE,
        XTMPSI.SUPERCLASS_SUBCLASS,
        XTMPSI.DISPLAY,
        XTMPSI.OCCURRENCE,
        XTMPSI.SORT,
        XTMPSI.TOPIC,
        XTMPSI.SUPERCLASS,
        XTMPSI.SUBCLASS,
        XTMPSI.CLASS,
        XTMPSI.CLASS_INSTANCE,
        XTMPSI.INSTANCE
    };
    
    /**
     * Creates a new instance of ExportSchemaMap
     */
    
    
    public ExportSchemaMap() {
    }
    
    @Override
    public String getName() {
        return "Export schema topic map";
    }
    @Override
    public String getDescription() {
        return "Export schema topic map";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_topicmap_schema.png");
    }
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        SimpleFileChooser chooser = UIConstants.getFileChooser();
        chooser.setDialogTitle("Export Wandora schema...");
        
        if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            
            try{
                file = IObox.addFileExtension(file, "xtm"); // Ensure file extension exists!
                TopicMap topicMap = solveContextTopicMap(wandora, context);
                TopicMap schemaTopicMap = exportTypeDefs(topicMap);
                String name = solveNameForTopicMap(wandora, topicMap);
                if(name != null) {
                    log("Exporting Wandora schema topics of layer '"+ name +"' to '"+ file.getName() + "'.");
                }
                else {
                    log("Exporting Wandora schema topics to '"+ file.getName() + "'.");
                }
                OutputStream out = new FileOutputStream(file);
                schemaTopicMap.exportXTM(out);
                out.close();
                log("Ready.");
            }
            catch(IOException e) {
                log(e);
            }
            setState(WAIT);
        }
    }
    
    


    public static TopicMap exportTypeDefs(TopicMap tm) throws TopicMapException {
        TopicMap export=new org.wandora.topicmap.memory.TopicMapImpl();
        
        Set<Topic> copyTypes=new LinkedHashSet<>();
        for(int i=0;i<exportTypes.length;i++){
            Topic t=tm.getTopic(exportTypes[i]);
            if(t==null) {
                continue;
            }
            copyTypes.add(t);
        }
        
        Set<Topic> copied=new LinkedHashSet<>();
        Iterator<Topic> iter=copyTypes.iterator();
        while(iter.hasNext()){
            Topic type=iter.next();
            Iterator<Topic> iter2=tm.getTopicsOfType(type).iterator();
            while(iter2.hasNext()){
                Topic topic=iter2.next();
                if(!copied.contains(topic)){
                    export.copyTopicIn(topic,false);
                    Iterator<Association> iter3=topic.getAssociations().iterator();
                    while(iter3.hasNext()){
                        Association a=iter3.next();
                        if(copyTypes.contains(a.getType())){
                            export.copyAssociationIn(a);
                        }
                    }
                    copied.add(topic);
                }
            }
        }

        copyTypes=new LinkedHashSet<>();
        for(int i=0;i<exportTypes.length;i++){
            Topic t=export.getTopic(exportTypes[i]);
            if(t==null) {
                continue;
            }
            copyTypes.add(t);
        }
        Topic wandoraClass=export.getTopic(TMBox.WANDORACLASS_SI);
        Topic hideLevel=export.getTopic(TMBox.HIDELEVEL_SI);
        iter=export.getTopics();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            Iterator<Set<Topic>> iter2=new ArrayList<>(t.getVariantScopes()).iterator();
            while(iter2.hasNext()){
                Set<Topic> scope=iter2.next();
                t.removeVariant(scope);
            }
            Iterator<Topic> iter2b=new ArrayList<>(t.getDataTypes()).iterator();
            while(iter2b.hasNext()){
                Topic type=iter2b.next();
                if(type!=hideLevel){
                    t.removeData(type);
                }
            }
            Iterator<Topic> iter2c=new ArrayList<>(t.getTypes()).iterator();
            while(iter2c.hasNext()){
                Topic type=iter2c.next();
                if(!copyTypes.contains(type) && type!=wandoraClass) {
                    t.removeType(type);
                }
            }
        }
        iter=export.getTopics();
        List<Topic> v=new ArrayList<>();
        while(iter.hasNext()){
            Topic t=iter.next();
            if(t.getAssociations().isEmpty() && export.getTopicsOfType(t).isEmpty()){
                v.add(t);
            }
        }
        iter=v.iterator();
        while(iter.hasNext()){
            Topic t=iter.next();
            try{
                t.remove();
            }catch(TopicInUseException tiue){}
        }
        TMBox.getOrCreateTopic(export,XTMPSI.getLang("fi"));
        TMBox.getOrCreateTopic(export,XTMPSI.getLang("en"));
        TMBox.getOrCreateTopic(export,XTMPSI.DISPLAY);
        TMBox.getOrCreateTopic(export,XTMPSI.SORT);
        TMBox.getOrCreateTopic(export,TMBox.LANGINDEPENDENT_SI);
        return export;
    }
    
    
}
