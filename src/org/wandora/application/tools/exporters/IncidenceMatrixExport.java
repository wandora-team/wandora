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
 * IncidenceMatrixExport.java
 *
 */
package org.wandora.application.tools.exporters;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*; 


/**
 *
 * @author akivela
 */
public class IncidenceMatrixExport  extends AbstractExportTool implements WandoraTool {
    public static boolean LABEL_MATRIX = true;
    public static boolean EXPORT_AS_HTML_TABLE = true;
    public static boolean SORT = true;

    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;




    public IncidenceMatrixExport() {
    }
    public IncidenceMatrixExport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }



    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_incidence_matrix.png");
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
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Incidence matrix export options","Incidence matrix export options",true,new String[][]{
            new String[]{"Label matrix columns and rows","boolean",(LABEL_MATRIX ? "true" : "false"), "Should Wandora label matrix columns and rows using topic names?"},
            new String[]{"Export as HTML table","boolean",(EXPORT_AS_HTML_TABLE ? "true" : "false"), "Export HTML table instead of tab text?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;

        Map<String, String> values = god.getValues();

        LABEL_MATRIX = ("true".equals(values.get("Label matrix columns and rows")) ? true : false );
        EXPORT_AS_HTML_TABLE = ("true".equals(values.get("Export as HTML table")) ? true : false );
    }





    @Override
    public void execute(Wandora admin, Context context) {

        Iterator<Topic> topics = null;
        Iterator<Association> associations = null;
        String exportInfo = "";
        try {
            if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
                topics = context.getContextObjects();
                HashSet<Association> as = new HashSet<Association>();
                while(topics.hasNext()) {
                    Topic t = topics.next();
                    as.addAll( t.getAssociations() );
                }
                associations = as.iterator();
                exportInfo = "selected topics";
            }
            else {
                // --- Solve first topic map to be exported
                TopicMap tm = solveContextTopicMap(admin, context);
                String topicMapName = this.solveNameForTopicMap(admin, tm);
                topics = tm.getTopics();
                associations = tm.getAssociations();

                if(topicMapName == null) exportInfo = "LayerStack";
                if(topicMapName != null) exportInfo = "topic map in layer '" + topicMapName + "'";
            }


            // --- Then solve target file (and format)
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle("Export "+exportInfo+" as incidence matrix...");
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
                    log("Exporting topics as incidence matrix to '"+fileName+"'.");
                    exportMatrix(out, associations, topics, getCurrentLogger());
                    
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
        return "Export incidence matrix";
    }

    @Override
    public String getDescription() {
        return "Exports topic map as incidence matrix.";
    }





    public void exportMatrix(OutputStream out, Iterator<Association> associationIterator, Iterator<Topic> topicIterator, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while instantiating PrintWriter with character encoding ISO-8859-1. Using default encoding.");
            writer = new PrintWriter(new OutputStreamWriter(out));
        }

        int totalCount = 0;
        ArrayList<Topic> topics = new ArrayList<Topic>();
        ArrayList<Association> associations = new ArrayList<Association>();
        Topic t = null;
        Association a = null;

        log("Collecting associations...");
        while(associationIterator.hasNext() && !logger.forceStop()) {
            a = associationIterator.next();
            if(a != null && !a.isRemoved()) {
                associations.add(a);
                totalCount++;
            }
        }

        log("Collecting topics...");
        while(topicIterator.hasNext() && !logger.forceStop()) {
            t = topicIterator.next();
            if(t != null && !t.isRemoved()) {
                topics.add(t);
                totalCount++;
            }
        }
        if(SORT) {
            log("Sorting topics...");
            Collections.sort(topics, new TMBox.TopicNameComparator(null));
            log("Sorting associations...");
            Collections.sort(associations, new TMBox.AssociationTypeComparator((String) null));
        }
        logger.setProgressMax(totalCount);

        // And finally export....
        log("Exporting incidence matrix...");
        if(EXPORT_AS_HTML_TABLE) {
            exportMatrixAsHTMLTable(writer, associations, topics, logger);
        }
        else {
            exportMatrixAsTabText(writer, associations, topics, logger);
        }

        writer.flush();
        writer.close();
    }





    public void exportMatrixAsTabText(PrintWriter writer, ArrayList<Association> associations, ArrayList<Topic> topics, WandoraToolLogger logger) throws TopicMapException {
        int progress = 0;
        Association a = null;
        Topic t = null;
        int connections = 0;

        for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
            t=topics.get(i);
            if(LABEL_MATRIX) {
                print(writer, makeString(t)+"\t");
            }
            for(int j=0; j<associations.size() && !logger.forceStop(); j++) {
                a=associations.get(j);
                connections = hasConnections(a, t);

                print(writer, connections+"\t");
                setProgress(progress++);
            }
            print(writer, "\n");
        }
    }





    public void exportMatrixAsHTMLTable(PrintWriter writer, ArrayList<Association> associations, ArrayList<Topic> topics, WandoraToolLogger logger) throws TopicMapException {
        int progress = 0;
        Association a = null;
        Topic t = null;
        int connections = 0;
        print(writer, "<table border=1>");

        for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
            print(writer, "<tr>");
            t=topics.get(i);
            if(LABEL_MATRIX) {
                print(writer, "<td>"+makeString(t)+"</td>");
            }
            for(int j=0; j<associations.size() && !logger.forceStop(); j++) {
                a=associations.get(j);
                connections = hasConnections(a, t);
                print(writer, "<td>"+connections+"</td>");
                setProgress(progress++);
            }
            print(writer, "</tr>\n");
        }
        print(writer, "</table>");
    }

    // -------------------------------------------------------------------------





    public int hasConnections(Association a, Topic t) {
        int count = 0;
        try {
            Collection<Topic> roles = a.getRoles();
            Topic role = null;
            Topic player = null;
            for(Iterator<Topic> roleIterator = roles.iterator(); roleIterator.hasNext(); ) {
                role = roleIterator.next();
                player = a.getPlayer(role);
                if(player != null) {
                    if(player.mergesWithTopic(t)) {
                        count++;
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return count;
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