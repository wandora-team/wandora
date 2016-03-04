/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2016 Wandora Team
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
 * <p>Exports selected topic map in Wandora as GraphXML file. GraphXML
 * format files can be used with HyperGraph application for example. See
 * http://hypergraph.sourceforge.net/simple_graphs.html
 * for more info about GraphXML format.
 * </p>
 * <p>
 * Note that the GraphXML file format is rather simple and can't represent all
 * details of Topic Maps. Export converts all topics and occurrences as graph
 * nodes. All associations are exported as graph edges. If association has more
 * than two players, an itermediator node is added to link all players.
 * </p>
 * <p>
 * Export doesn't convert subject locators, subject identifiers, different
 * variant names, association roles, occurrence scopes etc.
 * </p>
 *
 * @author akivela
 */


public class GraphXMLExport extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    public boolean EXPORT_CLASSES = true;
    public boolean EXPORT_OCCURRENCES = false;
    public boolean EXPORT_N_ASSOCIATIONS = true;
    public boolean EXPORT_DIRECTED = false;
    public boolean LABEL_EDGES = false;
    public boolean EXPORT_SUBJECT_LOCATORS = false;
    public boolean MARK_AS_FOREST = true;
    
    public GraphXMLExport() {
    }
    public GraphXMLExport(boolean exportSelection) {
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
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"GraphXML Export options","GraphXML Export options",true,new String[][]{
            new String[]{"Export classes","boolean",(EXPORT_CLASSES ? "true" : "false"),"Should topic classes also export?"},
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
            new String[]{"Export n associations","boolean",(EXPORT_N_ASSOCIATIONS ? "true" : "false"), "Should associations with more than 2 players also export?"},
            new String[]{"Is directed","boolean",(EXPORT_DIRECTED ? "true" : "false"), "Export directed or undirected graph" },
            new String[]{"Label edges","boolean",(LABEL_EDGES ? "true" : "false"), "Label edges with association type?" },
            new String[]{"Export subject locators","boolean",(EXPORT_SUBJECT_LOCATORS ? "true" : "false"), "Export subject locators as node links?" },
            new String[]{"Mark as forest","boolean",(MARK_AS_FOREST ? "true" : "false"), "Export subject locators as node links?" },
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXPORT_CLASSES = ("true".equals(values.get("Export classes")) ? true : false );
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
        EXPORT_N_ASSOCIATIONS = ("true".equals(values.get("Export n associations")) ? true : false );
        EXPORT_DIRECTED = ("true".equals(values.get("Is directed")) ? true : false );
        LABEL_EDGES = ("true".equals(values.get("Label edges")) ? true : false );
        EXPORT_SUBJECT_LOCATORS = ("true".equals(values.get("Export subject locators")) ? true : false );
        MARK_AS_FOREST = ("true".equals(values.get("Mark as forest")) ? true : false );
    }
    
    
    
    

    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as GraphXML graph";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(admin, context);
            topicMapName = this.solveNameForTopicMap(admin, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as GraphXML graph";
            }
            else {
                exportInfo =  "Exporting topic map as GraphXML graph";
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

            // --- Finally write topicmap as GraphXML to chosen file
            OutputStream out=null;
            try {
                file=IObox.addFileExtension(file, "xml"); // Ensure file extension exists!
                fileName = file.getName(); // Updating filename if file has changed!
                out=new FileOutputStream(file);
                log(exportInfo+" to '"+fileName+"'.");
                exportGraphML(out, tm, topicMapName, getCurrentLogger());
                
                out.close();
                log("Saved GraphXML file has a reference to a local GraphXML.dtd.");
                log("Download GraphXML.dtd type definition file from http://hypergraph.sourceforge.net/graphs/GraphXML.dtd");
                log("Ready.");
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
        return "Export GraphXML graph";
    }
    @Override
    public String getDescription() {
        return "Exports topic map layer as GraphXML file.";
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
        println(writer, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        println(writer, "<!DOCTYPE GraphXML SYSTEM \"GraphXML.dtd\">");
        println(writer, "<GraphXML>");
        
        print(writer, " <graph id=\"WandoraExport"+makeID(graphName)+"\"");
        if(EXPORT_DIRECTED)
            print(writer, " isDirected=\"true\"");
        else 
            print(writer, " isDirected=\"false\"");
        if(MARK_AS_FOREST)
            print(writer, " isForest=\"true\"");
        else 
            print(writer, " isForest=\"false\"");
        println(writer, ">");

        // First round... topic nodes
        int count = 0;
        Iterator iter=topicMap.getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            logger.setProgress(count++);
            echoNode(t, writer);
        }

        // Second round... class-instance edges and occurrences....
        iter=topicMap.getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            logger.setProgress(count++);

            // Topic types....
            if(EXPORT_CLASSES && t.getTypes().size()>0) {
                Iterator iter2=t.getTypes().iterator();
                while(iter2.hasNext()){
                    Topic t2=(Topic)iter2.next();
                    if(t2.isRemoved()) continue;
                    echoEdge(t2, t, "class-instance", writer);
                }
            }
            // Topic occurrences....
            if(EXPORT_OCCURRENCES && t.getDataTypes().size()>0) {
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

        // Third round and association edges....
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
        println(writer, "</GraphXML>"); // graphml
        
        writer.flush();
        writer.close();
    }

    
    
    
    
    
    
    // --------------------------------------------------------------- ECHOS ---
    
    
    
    protected void echoNode(Topic t, PrintWriter writer) throws TopicMapException {
        String label = t.getBaseName();
        if(label == null) label = t.getOneSubjectIdentifier().toExternalForm();
        String sl = t.getSubjectLocator() == null ? null : t.getSubjectLocator().toExternalForm();
        echoNode(makeID(t), sl, label, writer);
    }
    
    protected void echoNode(String data, PrintWriter writer) {
        echoNode(makeID(data), null, data, writer);
    }
    
    protected void echoNode(String id, String sl, String label, PrintWriter writer) {
        if(writer == null || id == null) return;
        println(writer, "  <node name=\""+id+"\">");
        if(label != null) println(writer, "   <label>"+makeString(label)+"</label>");
        try {
            if(EXPORT_SUBJECT_LOCATORS && sl != null) 
                println(writer, "   <dataref><ref xlink:href=\""+makeString(sl)+"\"/></dataref>");
        }
        catch(Exception e) {
            log(e);
        }
        println(writer, "  </node>");
    }
    

    
    
    
    
    protected void echoEdge(Topic source, Topic target, String label, PrintWriter writer) throws TopicMapException {
        print(writer, "  <edge");
        print(writer, " source=\""+makeID(source)+"\"");
        print(writer, " target=\""+makeID(target)+"\"");
        println(writer, ">");
        if(LABEL_EDGES && label != null) writer.println("   <label>"+makeString(label)+"</label>");
        println(writer, "  </edge>");
    }
    
    protected void echoEdge(String source, String target, String label, PrintWriter writer) {
        print(writer, "  <edge");
        print(writer, " source=\""+makeID(source)+"\"");
        print(writer, " target=\""+makeID(target)+"\"");
        println(writer, ">");
        if(LABEL_EDGES && label != null) println(writer, "   <label>"+makeString(label)+"</label>");
        println(writer, "  </edge>");
    }
    
    protected void echoEdge(Topic source, String target, Topic label, PrintWriter writer) throws TopicMapException {
        String l = label.getBaseName();
        if(l == null) l = label.getOneSubjectIdentifier().toExternalForm();
        print(writer, "  <edge");
        print(writer, " source=\""+makeID(source)+"\"");
        print(writer, " target=\""+makeID(target)+"\"");
        println(writer, ">");
        if(LABEL_EDGES && label != null) println(writer, "   <label>"+makeString(label)+"</label>");
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

