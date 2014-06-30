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
 */

package org.wandora.application.tools.exporters;


import org.wandora.utils.IObox;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;

import org.wandora.topicmap.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author akivela
 */
public class GXLExport extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    public boolean EXPORT_CLASSES = true;
    public boolean EXPORT_OCCURRENCES = false;
    public boolean PREFER_EDGES = true;
    public boolean BASE_NAMES_AS_IDS = true;
    
    public GXLExport() {
    }
    public GXLExport(boolean exportSelection) {
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
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"GXL Export options","GXL Export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should Wandora export also topic types (class-instance relations)?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Use base name as id","boolean",(BASE_NAMES_AS_IDS ? "true" : "false"), "Use topic's base name as XML node id" },
            new String[]{"Prefer edges instead of rels","boolean",(PREFER_EDGES ? "true" : "false"),"Prefer edge elements instead of rel elements in XML?"},
            
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;

        Map<String, String> values = god.getValues();
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        BASE_NAMES_AS_IDS = ("true".equals(values.get("Use base name as id")) ? true : false );
        PREFER_EDGES = ("true".equals(values.get("Prefer edges instead of rels")) ? true : false );
    }





    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as GXL (Graph eXchange Language) graph";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(admin, context);
            topicMapName = this.solveNameForTopicMap(admin, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as GXL (Graph eXchange Language) graph";
            }
            else {
                exportInfo =  "Exporting topic map as GXL (Graph eXchange Language) graph";
                topicMapName = "no_name_topic_map";
            }
        }


        // --- Then solve target file
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");

        if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
            setDefaultLogger();
            File file = chooser.getSelectedFile();
            String fileName = file.getName();

            // --- Finally write topicmap as GXL to chosen file
            OutputStream out = null;
            try {
                file = IObox.addFileExtension(file, "gxl"); // Ensure file extension exists!
                fileName = file.getName(); // Updating filename if file has changed!
                out = new FileOutputStream(file);
                log(exportInfo+" to '"+fileName+"'.");
                exportGraphML(out, tm, topicMapName, getCurrentLogger());
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
        return "Export GXL graph";
    }
    @Override
    public String getDescription() {
        return "Exports topic map layer as GXL (Graph eXchange Language) file.";
    }





    public void exportGraphML(OutputStream out, TopicMap topicMap, String graphName, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while instantiating PrintWriter with UTF-8 character encoding. Using default encoding.");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }

        int totalCount = 2*topicMap.getNumTopics() + topicMap.getNumAssociations();
        logger.setProgressMax(totalCount);
        int count = 0;
        
        println(writer, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        println(writer, "<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

        print(writer, " <graph id=\"WandoraExport"+makeID(graphName)+"\"");
        print(writer, " edgemode=\"defaultundirected\"");
        print(writer, " hypergraph=\"true\"");
        println(writer, ">");

        Iterator iter=topicMap.getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            logger.setProgress(count++);
            echoNode(t, writer);
        }

        // Topic types....
        if(EXPORT_CLASSES && !logger.forceStop()) {
            iter=topicMap.getTopics();
            while(iter.hasNext() && !logger.forceStop()) {
                logger.setProgress(count++);
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                Iterator iter2=t.getTypes().iterator();
                while(iter2.hasNext()) {
                    Topic t2=(Topic)iter2.next();
                    if(t2.isRemoved()) continue;
                    echoEdge(t2, t, "class-instance", "class", "instance", writer);
                }
            }
        }

        // Associations....
        if(!logger.forceStop()) {
            iter=topicMap.getAssociations();
            icount=0;
            while(iter.hasNext() && !logger.forceStop()) {
                logger.setProgress(count++);
                Association a=(Association)iter.next();
                echoEdge(a, writer);
            }
        }
        println(writer, " </graph>"); // graph
        println(writer, "</gxl>"); // graphml
        
        writer.flush();
        writer.close();
    }







    // --------------------------------------------------------------- ECHOS ---



    protected void echoNode(Topic t, PrintWriter writer) throws TopicMapException {
        if(t == null || writer == null) return;

        String label = t.getBaseName();
        if(label == null) label = t.getOneSubjectIdentifier().toExternalForm();
        
        println(writer, "  <node id=\""+makeID(t)+"\">");
        if(t.getBaseName() != null) {
            println(writer, "   <attr name=\"basename\"><string>"+makeString(t.getBaseName())+"</string></attr>");
        }

        String sl = t.getSubjectLocator() == null ? null : t.getSubjectLocator().toExternalForm();
        if(sl != null) {
            println(writer, "   <attr name=\"sl\"><string>"+makeString(sl)+"</string></attr>");
        }

        int siCount = 0;
        for(Iterator<Locator> siIter = t.getSubjectIdentifiers().iterator(); siIter.hasNext(); ) {
            Locator si = siIter.next();
            if(si != null) {
                println(writer, "   <attr name=\"si"+(siCount>0?siCount:"")+"\"><string>"+makeString(si.toExternalForm())+"</string></attr>");
                siCount++;
            }
        }

        // Topic occurrences....
        if(EXPORT_OCCURRENCES && t.getDataTypes().size()>0) {
            Collection types=t.getDataTypes();
            Iterator iter2=types.iterator();
            while(iter2.hasNext()){
                Topic type=(Topic)iter2.next();
                String typeName = type.getBaseName();
                Hashtable ht=(Hashtable)t.getData(type);
                Iterator iter3=ht.entrySet().iterator();
                while(iter3.hasNext()){
                    Map.Entry e=(Map.Entry)iter3.next();
                    String data=(String)e.getValue();
                    println(writer, "   <attr name=\"occurrence-"+typeName+"\"><string>"+makeString(data)+"</string></attr>");
                }
            }
        }
        println(writer, "  </node>");
    }



    int icount = 0;
    protected void echoEdge(Association a, PrintWriter writer) throws TopicMapException {
        if(PREFER_EDGES) {
            Collection roles = a.getRoles();
            if(roles.size() < 2) return;
            else if(roles.size() == 2) {
                Topic[] roleArray = (Topic[]) roles.toArray(new Topic[2]);
                Topic source = a.getPlayer(roleArray[0]);
                Topic target = a.getPlayer(roleArray[1]);
                println(writer, "  <edge from=\""+makeID(source)+"\" to=\""+makeID(target)+"\">");
                println(writer, "   <type xlink:href=\""+makeID(a.getType())+"\"/>");
                println(writer, "  </edge>");
            }
            else {
                icount++;
                String target="nameless-intermediator-node-"+icount;
                println(writer, "  <node id=\""+target+"\">");
                Iterator iter2 = roles.iterator();
                while(iter2.hasNext()) {
                    Topic role=(Topic)iter2.next();
                    println(writer, "  <edge from=\""+makeID(a.getPlayer(role))+"\" to=\""+target+"\">");
                    println(writer, "  </edge>");
                }
            }
        }
        else {
            println(writer, "  <rel>");
            println(writer, "   <type xlink:href=\""+makeID(a.getType())+"\"/>");
            for(Iterator<Topic> roleIter=a.getRoles().iterator(); roleIter.hasNext(); ) {
                Topic role = roleIter.next();
                Topic player = a.getPlayer(role);
                print(writer, "   <relend");
                print(writer, " role=\""+makeID(role)+"\"");
                print(writer, " target=\""+makeID(player)+"\"");
                println(writer, "/>");
            }
            println(writer, "  </rel>");
        }
    }


    protected void echoEdge(Topic source, Topic target, String type, String sourceRole, String targetRole, PrintWriter writer) throws TopicMapException {
        if(PREFER_EDGES) {
            println(writer, "  <edge from=\""+makeID(source)+"\" to=\""+makeID(target)+"\">");
            println(writer, "   <type xlink:href=\""+type+"\"/>");
            println(writer, "  </edge>");
        }
        else {
            println(writer, "  <rel>");
            println(writer, "   <type xlink:href=\""+type+"\"/>");
            println(writer, "   <relend role=\""+sourceRole+"\" target=\""+makeID(source)+"\"/>");
            println(writer, "   <relend role=\""+targetRole+"\" target=\""+makeID(target)+"\"/>");
            println(writer, "  </rel>");
        }
    }




    protected String makeID(Topic t) throws TopicMapException {
        if(t.getBaseName() != null) {
            String bn = t.getBaseName();
            bn = makeID(bn);
            return bn;
        }
        return ""+t.getID();
    }
    protected String makeID(String s) {
        if(s != null && s.length() > 0) {
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<s.length(); i++) {
                if(Character.isJavaIdentifierPart(s.charAt(i))) {
                    sb.append(s.charAt(i));
                }
                else {
                    sb.append('_');
                }
            }
            return sb.toString();
        }
        return s;
    }


    protected String makeString(String s) {
        if(s == null) return null;
        //s = s.substring(0,Math.min(s.length(), 240));
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\\&", "&amp;");
        s = s.replaceAll("\"", "'");
        return s;
    }
    protected String makeString(Topic t) throws TopicMapException {
        if(t == null) return null;
        String s = t.getBaseName();
        if(s == null) s = t.getOneSubjectIdentifier().toExternalForm();
        return makeString(s);
    }



    // -------------------------------------------------------------------------
    // Elementary print methods are used to ensure output is UTF-8

    protected void print(PrintWriter writer, String str) {
        writer.print(str);
    }
    protected void println(PrintWriter writer, String str) {
        writer.println(str);
    }
}
