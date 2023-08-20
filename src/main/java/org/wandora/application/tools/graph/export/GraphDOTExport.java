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
 */



package org.wandora.application.tools.graph.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.VEdge;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;
import org.wandora.application.tools.graph.AbstractGraphTool;
import org.wandora.utils.IObox;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author akivela
 */


public class GraphDOTExport extends AbstractGraphTool {

	private static final long serialVersionUID = 1L;

	@Override
    public String getName() {
        return "Graph view DOT export";
    }
    
    @Override
    public String getDescription() {
        return "Export graph in Graph panel as a DOT formatted file";
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
    public void executeSynchronized(Wandora admin, Context context) {
        // NOTHING HERE!
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        TopicMapGraphPanel graphPanel = this.solveGraphPanel(wandora, context);
        if(graphPanel != null) {
            VModel model = graphPanel.getModel();
            if(model != null) {
                SimpleFileChooser chooser=UIConstants.getFileChooser();
                chooser.setDialogTitle("Export graph view...");

                if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
                    setDefaultLogger();
                    File file = chooser.getSelectedFile();
                    String fileName = file.getName();

                    // --- Finally write graph to the file
                    OutputStream out=null;
                    try {
                        file=IObox.addFileExtension(file, "dot"); // Ensure file extension exists!
                        fileName = file.getName(); // Updating filename if file has changed!
                        out=new FileOutputStream(file);
                        log("Exporting graph view to '"+fileName+"'.");
                        //System.out.println("tm == "+ tm);
                        exportGraph(out, model);

                        out.close();
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
        }
    }
    

    
    public void exportGraph(OutputStream out, VModel model) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while instantiating PrintWriter with ISO-8859-1 character encoding. Using default encoding.");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }
        
        println(writer, "graph wandora_export {");
        
        for(VNode n : model.getNodes()) {
            printNode(n, writer);
        }
        
        T2<VNode,VNode> nodes;
        for(VEdge e : model.getEdges()) {
            nodes=e.getNodes();
            if(!nodes.e1.equals(nodes.e2)) {
                printEdge(e, writer);
            }
        }
        println(writer, "}");
        
        writer.flush();
        writer.close();
    }
    
        
        
    protected void printNode(VNode node, PrintWriter writer) {
        if(node == null) return;
        
        println(writer, " "+node.getID()+" [label=\""+makeString(node.getNode().getLabel())+"\"]");
        
    }
    
    
    protected void printEdge(VEdge edge, PrintWriter writer) {
        if(edge == null) return;
        T2<VNode,VNode> nodes = edge.getNodes();
        println(writer, " "+nodes.e1.getID()+" -- "+nodes.e2.getID()+" [label=\""+makeString(edge.getEdge().getLabel())+"\"]");
    }
    
    
    protected void print(PrintWriter writer, String str) {
        writer.print(str);
    }
    protected void println(PrintWriter writer, String str) {
        writer.println(str);
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

    
}
