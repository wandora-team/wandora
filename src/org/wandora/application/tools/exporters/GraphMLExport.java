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
 * GraphMLExport.java
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*; 

   

/**
 * <p>Exports selected topic map in Wandora as GraphML file. See
 * http://graphml.graphdrawing.org/ or
 * http://en.wikipedia.org/wiki/GraphML
 * for more info about GraphML format.</p>
 * <p>
 * Note that the GraphML file format is rather simple and can't represent all
 * details of Topic Maps. Export converts all topics and occurrences as graph
 * nodes. All associations are exported as graph edges. If association has more
 * than two players, an intermediator node is added to link all players.
 * </p>
 * <p>
 * Export doesn't convert subject locators, subject identifiers, different
 * variant names, association roles, occurrence scopes etc.
 * </p>
 * <p>
 * Purpose of the export feature is to enable topic map visualization in GraphML
 * supporting applications such as yEd.
 * </p>
 *
 * @author akivela
 */


public class GraphMLExport extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    public static boolean EXPORT_CLASSES = true;
    public static boolean EXPORT_OCCURRENCES = true;
    public static boolean EXPORT_N_ASSOCIATIONS = true;
    public static boolean EXPORT_DIRECTED = false;
    
    
    public GraphMLExport() {
    }
    public GraphMLExport(boolean exportSelection) {
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
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"GraphML Export options","GraphML Export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should topic classes also export?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Export n associations","boolean",(EXPORT_N_ASSOCIATIONS ? "true" : "false"), "Should associations with more than 2 players also export?"},
            new String[]{"Is directed","boolean",(EXPORT_DIRECTED ? "true" : "false"), "Export directed or undirected graph" },
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_N_ASSOCIATIONS = ("true".equals(values.get("Export n associations")) ? true : false );
        EXPORT_DIRECTED = ("true".equals(values.get("Is directed")) ? true : false );
    }
    
    
    
    

    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as GraphML graph";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(admin, context);
            topicMapName = this.solveNameForTopicMap(admin, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as GraphML graph";
            }
            else {
                exportInfo =  "Exporting topic map as GraphML graph";
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
            OutputStream out = null;
            try {
                file = IObox.addFileExtension(file, "graphml"); // Ensure file extension exists!
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
        return "Export GraphML graph";
    }

    @Override
    public String getDescription() {
        return "Exports topic map layer as GraphML file.";
    }

    
    
    
    
    public void exportGraphML(OutputStream out, TopicMap topicMap, String graphName, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while instantiating PrintWriter with ISO-8859-1 character encoding. Using default encoding.");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }

        int totalCount = 2*topicMap.getNumTopics() + topicMap.getNumAssociations();
        logger.setProgressMax(totalCount);
        int count = 0;
        
        println(writer, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        println(writer, "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
        
        println(writer, " <key id=\"d0\" for=\"all\" attr.name=\"label\" attr.type=\"string\"/>");
        println(writer, " <key id=\"d1\" for=\"all\" attr.name=\"name\" attr.type=\"string\"/>");
        
        println(writer, " <graph id=\""+makeID(graphName)+"\" edgedefault=\""+(EXPORT_DIRECTED ? "directed" : "undirected")+"\">");

        Iterator iter=topicMap.getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            logger.setProgress(count++);
            echoNode(t, writer);
            
            // Topic occurrences....
            if(EXPORT_OCCURRENCES && t.getDataTypes().size()>0){
                Collection types=t.getDataTypes();
                Iterator iter2=types.iterator();
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    Hashtable ht=(Hashtable)t.getData(type);
                    Iterator iter3=ht.entrySet().iterator();
                    while(iter3.hasNext()){
                        Map.Entry e=(Map.Entry)iter3.next();
                        String data=(String)e.getValue();
                        echoNode(data, writer);
                        echoEdge(t, data, type, writer);
                    }
                }
            }
        }

        // Topic types....
        if(EXPORT_CLASSES) {
            iter=topicMap.getTopics();
            while(iter.hasNext() && !logger.forceStop()) {
                Topic t=(Topic)iter.next();
                if(t.isRemoved()) continue;
                logger.setProgress(count++);
                if(t.getTypes().size()>0){
                    Iterator iter2=t.getTypes().iterator();
                    while(iter2.hasNext()){
                        Topic t2=(Topic)iter2.next();
                        if(t2.isRemoved()) continue;
                        echoEdge(t2, t, "class-instance", writer);
                    }
                }
            }
        }

        // Associations....
        if(!logger.forceStop()) {
            iter=topicMap.getAssociations();
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
                        String target="nameless-intermediator-node-"+icount;
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
        println(writer, " </graph>"); // graph
        println(writer, "</graphml>"); // graphml
        
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
        println(writer, "  <node id=\""+id+"\">");
        if(name != null) println(writer, "   <data key=\"d0\">"+makeString(name)+"</data>");
        if(label != null) println(writer, "   <data key=\"d1\">"+makeString(label)+"</data>");
        println(writer, "  </node>");
    }
    

    
    
    
    
    protected void echoEdge(Topic source, Topic target, String label, PrintWriter writer) throws TopicMapException {
        print(writer, "  <edge");
        print(writer, " source=\""+makeID(source)+"\"");
        print(writer, " target=\""+makeID(target)+"\"");
        println(writer, ">");
        if(label != null) println(writer, "   <data key=\"d1\">"+makeString(label)+"</data>");
        println(writer, "  </edge>");
    }
    
    protected void echoEdge(String source, String target, String label, PrintWriter writer) {
        print(writer, "  <edge");
        print(writer, " source=\""+makeID(source)+"\"");
        print(writer, " target=\""+makeID(target)+"\"");
        println(writer, ">");
        if(label != null) println(writer, "   <data key=\"d1\">"+makeString(label)+"</data>");
        println(writer, "  </edge>");
    }
    
    protected void echoEdge(Topic source, String target, Topic label, PrintWriter writer) throws TopicMapException {
        String l = label.getBaseName();
        if(l == null) l = label.getOneSubjectIdentifier().toExternalForm();
        print(writer, "  <edge");
        print(writer, " source=\""+makeID(source)+"\"");
        print(writer, " target=\""+makeID(target)+"\"");
        println(writer, ">");
        if(label != null) println(writer, "   <data key=\"d1\">"+makeString(label)+"</data>");
        println(writer, "  </edge>");
    }
    
    protected void echoEdge(Topic source, Topic target, Topic label, PrintWriter writer) throws TopicMapException {
        String l = label.getBaseName();
        if(l == null) l = label.getOneSubjectIdentifier().toExternalForm();
        echoEdge(source, target, l, writer);
    }
    
    
    
    protected String makeID(Topic t) throws TopicMapException {
        return ""+t.getID();
    }
    protected String makeID(String s) {
        int id = s.hashCode();
        if(id < 0) id = 2*Math.abs(id)+1;
        else id = 2*Math.abs(id);
        return ""+id;
    }
    
    
    protected String makeString(String s) {
        if(s == null) return null;
        //s = s.substring(0,Math.min(s.length(), 240));
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\\&", "&amp;");
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

