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
 * DOTExport.java
 *
 */

package org.wandora.application.tools.exporters;

/**
 *
 * @author akivela
 */


import org.wandora.utils.IObox;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;

import org.wandora.topicmap.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*; 

   

public class DOTExport extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    public static boolean LABEL_NODES = true;
    public static boolean EXPORT_CLASSES = true;
    public static boolean EXPORT_OCCURRENCES = true;
    public static boolean EXPORT_N_ASSOCIATIONS = true;
    
    
    public DOTExport() {
    }
    public DOTExport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_graph.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"DOT Export options","DOT Export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should topic classes also export?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Export n associations","boolean",(EXPORT_N_ASSOCIATIONS ? "true" : "false"), "Should associations with more than 2 players also export?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_N_ASSOCIATIONS = ("true".equals(values.get("Export n associations")) ? true : false );
    }
    
    
    
    

    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as DOT graph";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(admin, context);
            topicMapName = this.solveNameForTopicMap(admin, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as DOT graph";
            }
            else {
                exportInfo =  "Exporting topic map as DOT graph";
                topicMapName = "no_name_topic_map";
            }
        }

        
        // --- Then solve target file (and format)
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");

        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            String fileName = file.getName();

            // --- Finally write topicmap as GML to chosen file
            OutputStream out=null;
            try {
                file=IObox.addFileExtension(file, "dot"); // Ensure file extension exists!
                fileName = file.getName(); // Updating filename if file has changed!
                out=new FileOutputStream(file);
                log(exportInfo+" to '"+fileName+"'.");
                //System.out.println("tm == "+ tm);
                exportGraph(out, tm, topicMapName, getCurrentLogger());
                out.close();
                log("Done");
            }
            catch(Exception e) {
                log(e);
                try { if(out != null) out.close(); }
                catch(Exception e2) { log(e2); }
            }
        }
        setState(WAIT);
    }
    
    @Override
    public String getName() {
        return "Export DOT graph";
    }

    @Override
    public String getDescription() {
        return "Exports topic map layer as DOT file.";
    }

    
    
    
    
    public void exportGraph(OutputStream out, TopicMap topicMap, String graphName, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while trying ISO-8859-1 character encoding. Using default encoding");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }

        int totalCount = 2*topicMap.getNumTopics() + topicMap.getNumAssociations();
        logger.setProgressMax(totalCount);
        int count = 0;
        
        println(writer, "graph wandora_export {");

        if(LABEL_NODES) {
            Iterator iter=topicMap.getTopics();
            while(iter.hasNext() && !logger.forceStop()) {
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                logger.setProgress(count++);
                echoNode(t, writer);
            }
        }
        
        if(EXPORT_OCCURRENCES && !logger.forceStop()) {
            Iterator iter=topicMap.getTopics();
            while(iter.hasNext() && !logger.forceStop()) {
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                logger.setProgress(count++);
                // Topic occurrences....
                if(t.getDataTypes().size()>0){
                    Collection types=t.getDataTypes();
                    Iterator iter2=types.iterator();
                    while(iter2.hasNext()) {
                        Topic type=(Topic)iter2.next();
                        Hashtable ht=(Hashtable)t.getData(type);
                        Iterator iter3=ht.entrySet().iterator();
                        while(iter3.hasNext()) {
                            Map.Entry e=(Map.Entry)iter3.next();
                            String data=(String)e.getValue();
                            echoNode(data, writer);
                        }
                    }
                }
            }
            iter=topicMap.getTopics();
            while(iter.hasNext() && !logger.forceStop()) {
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                logger.setProgress(count++);
                // Topic occurrences....
                if(t.getDataTypes().size()>0){
                    Collection types=t.getDataTypes();
                    Iterator iter2=types.iterator();
                    while(iter2.hasNext()){
                        Topic type=(Topic)iter2.next();
                        Hashtable ht=(Hashtable)t.getData(type);
                        Iterator iter3=ht.entrySet().iterator();
                        while(iter3.hasNext()){
                            Map.Entry e=(Map.Entry)iter3.next();
                            String data=(String)e.getValue();
                            echoEdge(t, data, type, writer);
                        }
                    }
                }
            }
        }


        // Topic types....
        if(EXPORT_CLASSES && !logger.forceStop()) {
            Iterator iter=topicMap.getTopics();
            while(iter.hasNext() && !logger.forceStop()) {
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                logger.setProgress(count++);
                if(!t.getTypes().isEmpty()){
                    for (Topic t2 : t.getTypes()) {
                        if(t2.isRemoved()) continue;
                        echoEdge(t2, t, "class_instance", writer);
                    }
                }
            }
        }

        // Associations....
        if(!logger.forceStop()) {
            Iterator iter=topicMap.getAssociations();
            int icount=0;
            while(iter.hasNext() && !logger.forceStop()) {
                logger.setProgress(count++);
                Association a=(Association)iter.next();
                Collection roles = a.getRoles();
                if(roles.size() < 2) continue;
                else if(roles.size() == 2) {
                    Topic[] roleArray = (Topic[]) roles.toArray(new Topic[2]);
                    echoEdge(a.getPlayer(roleArray[0]), a.getPlayer(roleArray[1]), a.getType(), writer);
                }
                else {
                    if(EXPORT_N_ASSOCIATIONS) {
                        icount++;
                        String target="nameless_intermediator_node-"+icount;
                        echoNode(target, writer);
                        Iterator iter2 = roles.iterator();
                        while(iter2.hasNext()) {
                            Topic role=(Topic)iter2.next();
                            echoEdge(a.getPlayer(role), target, a.getType(), writer);
                        }
                    }
                }
            }
        }
        println(writer, "}");
        
        writer.flush();
        writer.close();
    }

    
    
    // --------------------------------------------------------------- ECHOS ---
    
    
    
    protected void echoNode(Topic t, PrintWriter writer) throws TopicMapException {
        String label = t.getBaseName();
        if(label == null) label = t.getOneSubjectIdentifier().toExternalForm();
        echoNode(makeID(t), null, label, writer);
    }
    
    protected void echoNode(String data, PrintWriter writer) {
        echoNode(makeID(data), null, data, writer);
    }
    
    protected void echoNode(String id, String name, String label, PrintWriter writer) {
        if(writer == null || id == null) return;
        if(label != null) println(writer, " "+id+" [label=\""+makeString(label)+"\"]");
    }
    

    
    
    
    
    protected void echoEdge(Topic source, Topic target, String label, PrintWriter writer) throws TopicMapException {
        if(label != null) {
            println(writer, " "+makeID(source)+" -- "+makeID(target)+" [label=\""+makeString(label)+"\"]");
        }
        else {
            println(writer, " "+makeID(source)+" -- "+makeID(target)+"");
        }
    }
    
    protected void echoEdge(String source, String target, String label, PrintWriter writer) {
        if(label != null) {
            println(writer, " "+makeID(source)+" -- "+makeID(target)+" [label=\""+makeString(label)+"\"]");
        }
        else {
            println(writer, " "+makeID(source)+" -- "+makeID(target)+"");
        }
    }
    
    protected void echoEdge(Topic source, String target, Topic label, PrintWriter writer) throws TopicMapException {
        String l = label.getBaseName();
        if(l == null) l = label.getOneSubjectIdentifier().toExternalForm();
        if(l != null) {
            println(writer, " "+makeID(source)+" -- "+makeID(target)+" [label=\""+makeString(l)+"\"]");
        }
        else {
            println(writer, " "+makeID(source)+" -- "+makeID(target)+"");
        }
    }
    
    protected void echoEdge(Topic source, Topic target, Topic label, PrintWriter writer) throws TopicMapException {
        String l = label.getBaseName();
        if(l == null) l = label.getOneSubjectIdentifier().toExternalForm();
        echoEdge(source, target, l, writer);
    }
    
    
    
    protected String makeID(Topic t) throws TopicMapException {
        String id = ""+t.getID();
        id = id.replace("-", "");
        return id;
    }
    protected String makeID(String s) {
        long h = s.hashCode();
        if(h > 0) return "ID"+(Math.abs(h)*2);
        else return "ID"+((Math.abs(h)*2)+1);
    }
    
    
    protected String makeString(String s) {
        if(s == null) return null;
        //s = s.substring(0,Math.min(s.length(), 240));
        s = s.replaceAll("\"", "\\\"");
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("\t", " ");
        s = s.replaceAll("\r", " ");
        return s;
    }
    protected String makeString(Topic t) throws TopicMapException {
        if(t == null) return null;
        String s = t.getBaseName();
        if(s == null) s = t.getOneSubjectIdentifier().toExternalForm();
        return makeString(s);
    }
    
    
    // -------------------------------------------------------------------------
    // Elementary print methods are used to ensure output is ISO-8859-1
    
    protected void print(PrintWriter writer, String str) {
        writer.print(str);
    }
    protected void println(PrintWriter writer, String str) {
        writer.println(str);
    }
}