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
 * AdjacencyMatrixExport.java
 *
 */


package org.wandora.application.tools.exporters;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox; 

/**
 *
 * @author akivela
 */
public class AdjacencyMatrixExport extends AbstractExportTool implements WandoraTool {
	
	private static final long serialVersionUID = 1L;
	
	
    public static boolean COUNT_CLASSES = true;
    public static boolean COUNT_INSTANCES = true;
    public static boolean COUNT_ASSOCIATIONS = true;

    public static boolean LABEL_MATRIX = true;
    public static boolean BINARY_CELL_VALUE = false;
    public static boolean EXPORT_AS_HTML_TABLE = true;
    public static boolean SORT_COLUMNS = true;

    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;




    public AdjacencyMatrixExport() {
    }
    public AdjacencyMatrixExport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }


    @Override
    public boolean requiresRefresh() {
        return false;
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_adjacency_matrix.png");
    }


    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Adjacency matrix export options","Adjacency matrix export options",true,new String[][]{
            new String[]{"Count classes","boolean",(COUNT_CLASSES ? "true" : "false"),"Should Wandora count classes?"},
            new String[]{"Count instances","boolean",(COUNT_INSTANCES ? "true" : "false"),"Should Wandora count instances?"},
            new String[]{"Count associations","boolean",(COUNT_ASSOCIATIONS ? "true" : "false"), "Should Wandora count associations?"},
            new String[]{"Label matrix columns and rows","boolean",(LABEL_MATRIX ? "true" : "false"), "Should Wandora label matrix columns and rows using topic names?"},
            new String[]{"Export as HTML table","boolean",(EXPORT_AS_HTML_TABLE ? "true" : "false"), "Export HTML table instead of tab text?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;

        Map<String, String> values = god.getValues();

        COUNT_CLASSES = ("true".equals(values.get("Count classes")) ? true : false );
        COUNT_INSTANCES = ("true".equals(values.get("Count instances")) ? true : false );
        COUNT_ASSOCIATIONS = ("true".equals(values.get("Count associations")) ? true : false );
        LABEL_MATRIX = ("true".equals(values.get("Label matrix columns and rows")) ? true : false );
        BINARY_CELL_VALUE = ("true".equals(values.get("Binary (0 or 1) matrix cell values")) ? true : false );
        EXPORT_AS_HTML_TABLE = ("true".equals(values.get("Export as HTML table")) ? true : false );
    }





    @Override
    public void execute(Wandora admin, Context context) {

        Iterator<Topic> topics = null;
        String exportInfo = "";
        try {
            if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
                topics = context.getContextObjects();
                exportInfo = "selected topics";
            }
            else {
                // --- Solve first topic map to be exported
                TopicMap tm = solveContextTopicMap(admin, context);
                String topicMapName = this.solveNameForTopicMap(admin, tm);
                topics = tm.getTopics();

                if(topicMapName == null) exportInfo = "LayerStack";
                if(topicMapName != null) exportInfo = "topic map in layer '" + topicMapName + "'";
            }

            // --- Then solve target file (and format)
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle("Export "+exportInfo+" as adjacency matrix...");
            if(chooser.open(admin, "Export")==SimpleFileChooser.APPROVE_OPTION){
                setDefaultLogger();
                File file = chooser.getSelectedFile();
                String fileName = file.getName();

                // --- Finally write topic map to chosen file
                OutputStream out=null;
                try {
                    if(EXPORT_AS_HTML_TABLE)
                        file=IObox.addFileExtension(file, "html");
                    else
                        file=IObox.addFileExtension(file, "txt");
                    
                    fileName = file.getName(); // Updating filename if file has changed!
                    out=new FileOutputStream(file);
                    log("Exporting topics as adjacency matrix to '"+fileName+"'.");
                    exportMatrix(out, topics, getCurrentLogger());
                    out.close();
                    log("OK");
                }
                catch(Exception e) {
                    log(e);
                    try { if(out != null) out.close(); }
                    catch(Exception e2) { log(e2); }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }


    @Override
    public String getName() {
        return "Export adjacency matrix";
    }

    @Override
    public String getDescription() {
        return "Exports topic map as adjacency matrix.";
    }





    public void exportMatrix(OutputStream out, Iterator<Topic> topicIterator, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while trying ISO-8859-1 character encoding. Using default encoding");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }

        int totalCount = 0;
        ArrayList<Topic> topics = new ArrayList<Topic>();
        Topic t = null;

        log("Collecting topics...");
        while(topicIterator.hasNext() && !logger.forceStop()) {
            t = topicIterator.next();
            if(t != null && !t.isRemoved()) {
                topics.add(t);
                totalCount++;
            }
        }
        if(SORT_COLUMNS) {
            log("Sorting topic collection...");
            Collections.sort(topics, new TMBox.TopicNameComparator(null));
        }
        totalCount = totalCount * totalCount;
        logger.setProgressMax(totalCount);

        // And finally export....
        log("Exporting adjacency matrix...");
        if(EXPORT_AS_HTML_TABLE) {
            exportMatrixAsHTMLTable(writer, topics, logger);
        }
        else {
            exportMatrixAsTabText(writer, topics, logger);
        }

        writer.flush();
        writer.close();
    }





    public void exportMatrixAsTabText(PrintWriter writer, ArrayList<Topic> topics, WandoraToolLogger logger) throws TopicMapException {
        int progress = 0;
        Topic t1 = null;
        Topic t2 = null;
        int connections = 0;
        if(LABEL_MATRIX) {
            print(writer, "\t");
            for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
                print(writer, makeString(topics.get(i))+"\t");
            }
            print(writer, "\n");
        }
        for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
            t1=topics.get(i);
            if(LABEL_MATRIX) {
                print(writer, makeString(topics.get(i))+"\t");
            }
            for(int j=0; j<topics.size() && !logger.forceStop(); j++) {
                t2=topics.get(j);
                if(BINARY_CELL_VALUE) {
                    connections = hasConnections(t1, t2);
                }
                else {
                    connections = countAllConnections(t1, t2);
                }
                print(writer, connections+"\t");
                setProgress(progress++);
            }
            print(writer, "\n");
        }
    }





    public void exportMatrixAsHTMLTable(PrintWriter writer, ArrayList<Topic> topics, WandoraToolLogger logger) throws TopicMapException {
        int progress = 0;
        Topic t1 = null;
        Topic t2 = null;
        int connections = 0;
        print(writer, "<table border=1>");
        if(LABEL_MATRIX) {
            print(writer, "<tr><td> </td>");
            for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
                print(writer, "<td>"+makeString(topics.get(i))+"</td>");
            }
            print(writer, "</tr>\n");
        }
        for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
            print(writer, "<tr>");
            t1=topics.get(i);
            if(LABEL_MATRIX) {
                print(writer, "<td>"+makeString(topics.get(i))+"</td>");
            }
            for(int j=0; j<topics.size() && !logger.forceStop(); j++) {
                t2=topics.get(j);
                if(BINARY_CELL_VALUE) {
                    connections = hasConnections(t1, t2);
                }
                else {
                    connections = countAllConnections(t1, t2);
                }
                print(writer, "<td>"+connections+"</td>");
                setProgress(progress++);
            }
            print(writer, "</tr>\n");
        }
        print(writer, "</table>");
    }

    // -------------------------------------------------------------------------



    
    public int countAllConnections(Topic t1, Topic t2) {
        int count = 0;
        try {
            // Class...
            if(COUNT_CLASSES && t1.isOfType(t2)) count++;

            // Instance...
            if(COUNT_INSTANCES && t2.isOfType(t1)) count++;

            // Associations...
            if(COUNT_ASSOCIATIONS) {
                Collection<Association> as = t1.getAssociations();
                Association a = null;
                for(Iterator<Association> asIterator=as.iterator(); asIterator.hasNext(); ) {
                    a = asIterator.next();
                    Collection<Topic> roles = a.getRoles();
                    Topic role = null;
                    Topic player = null;
                    int countA = 0;
                    for(Iterator<Topic> roleIterator = roles.iterator(); roleIterator.hasNext(); ) {
                        role = roleIterator.next();
                        player = a.getPlayer(role);
                        if(player != null) {
                            if(player.mergesWithTopic(t2)) {
                                countA++;
                            }
                        }
                    }
                    if(t1.mergesWithTopic(t2) && countA > 0) countA--;
                    count += countA;
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return count;
    }




    public int hasConnections(Topic t1, Topic t2) {
        try {
            // Class...
            if(COUNT_CLASSES && t1.isOfType(t2)) return 1;

            // Instance...
            if(COUNT_INSTANCES && t2.isOfType(t1)) return 1;

            // Associations...
            if(COUNT_ASSOCIATIONS) {
                Collection<Association> as = t1.getAssociations();
                Association a = null;
                for(Iterator<Association> asIterator=as.iterator(); asIterator.hasNext(); ) {
                    a = asIterator.next();
                    Collection<Topic> roles = a.getRoles();
                    Topic role = null;
                    Topic player = null;
                    for(Iterator<Topic> roleIterator = roles.iterator(); roleIterator.hasNext(); ) {
                        role = roleIterator.next();
                        player = a.getPlayer(role);
                        if(player != null) {
                            if(player.mergesWithTopic(t2)) {
                                return 1;
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return 0;
    }





    protected String makeString(Topic t) throws TopicMapException {
        if(t == null) return null;
        String s = t.getBaseName();
        if(s == null) s = t.getOneSubjectIdentifier().toExternalForm();
        return s;
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
