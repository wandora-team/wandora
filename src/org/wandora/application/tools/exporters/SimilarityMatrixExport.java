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
 * SimilarityMatrixExport.java
 *
 */


package org.wandora.application.tools.exporters;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.io.*;
import java.util.*;
import javax.swing.*; 
import org.wandora.topicmap.similarity.TopicSimilarity;

/**
 *
 * @author akivela
 */
public class SimilarityMatrixExport extends AbstractExportTool implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	
	public static final String TAB_FORMAT = "Tabulator separated plain text";
    public static final String HTML_FORMAT = "HTML table";
    
    private SimilarityMatrixExportDialog similarityDialog = null;
    
    private boolean labelMatrix = true;
    private String outputFormat = null;
    private boolean sortColumns = true;
    private int numberOfDecimals = 3;
    private double filterBelow = 0;
    private double maximizeAbove = 1;
    private boolean outputZeros = false;
    
    private boolean exportSelectionInsteadOfTopicMap = false;

    private TopicSimilarity currentSimilarityMeasure = null;


    
    
    public SimilarityMatrixExport() {
    }
    public SimilarityMatrixExport(boolean exportSelection) {
        exportSelectionInsteadOfTopicMap = exportSelection;
    }



    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export_similarity_matrix.png");
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }



    @Override
    public void execute(Wandora wandora, Context context) {

        Iterator<Topic> topics = null;
        String exportInfo = "";
        try {
            if(exportSelectionInsteadOfTopicMap) {
                topics = context.getContextObjects();
                exportInfo = "selected topics";
            }
            else {
                // --- Solve first topic map to be exported
                TopicMap tm = solveContextTopicMap(wandora, context);
                String topicMapName = this.solveNameForTopicMap(wandora, tm);
                topics = tm.getTopics();

                if(topicMapName == null) exportInfo = "LayerStack";
                if(topicMapName != null) exportInfo = "topic map in layer '" + topicMapName + "'";
            }
            
            if(similarityDialog == null) {
                similarityDialog = new SimilarityMatrixExportDialog();
            }
            similarityDialog.open();
            
            // WAITING FOR USER...
            
            if(similarityDialog.wasAccepted()) {
                labelMatrix = similarityDialog.shouldAddLabels();
                outputFormat = similarityDialog.getOutputFormat();
                currentSimilarityMeasure = similarityDialog.getSimilarityMeasure();
                numberOfDecimals = similarityDialog.getNumberOfDecimals();
                filterBelow = similarityDialog.getFilterBelow();
                maximizeAbove = similarityDialog.getMaximizeAbove();
                outputZeros = similarityDialog.getOutputZeros();

                // --- Then solve target file (and format)
                SimpleFileChooser chooser=UIConstants.getFileChooser();
                chooser.setDialogTitle("Export "+exportInfo+" as similarity matrix...");
                if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION){
                    setDefaultLogger();
                    File file = chooser.getSelectedFile();
                    String fileName = file.getName();

                    // --- Finally write topic map to chosen file
                    OutputStream out=null;
                    try {
                        if(HTML_FORMAT.equals(outputFormat))
                            file=IObox.addFileExtension(file, "html");
                        else
                            file=IObox.addFileExtension(file, "txt");

                        fileName = file.getName(); // Updating filename if file has changed!
                        out=new FileOutputStream(file);
                        log("Exporting topics as similarity matrix to '"+fileName+"'.");
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
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }


    @Override
    public String getName() {
        return "Export similarity matrix";
    }

    @Override
    public String getDescription() {
        return "Exports topic map as similarity matrix.";
    }





    public void exportMatrix(OutputStream out, Iterator<Topic> topicIterator, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
        } 
        catch (UnsupportedEncodingException ex) {
            log("Exception while trying ISO-8859-1 character encoding. Using default encoding.");
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
        if(sortColumns) {
            log("Sorting topic collection...");
            Collections.sort(topics, new TMBox.TopicNameComparator(null));
        }
        totalCount = totalCount * totalCount;
        logger.setProgressMax(totalCount);

        // And finally export....
        log("Exporting similarity matrix...");
        if(HTML_FORMAT.equals(outputFormat)) {
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
        double similarity = 0;
        if(labelMatrix) {
            print(writer, "\t");
            for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
                print(writer, makeString(topics.get(i))+"\t");
            }
            print(writer, "\n");
        }
        for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
            t1=topics.get(i);
            if(labelMatrix) {
                print(writer, makeString(topics.get(i))+"\t");
            }
            for(int j=0; j<topics.size() && !logger.forceStop(); j++) {
                t2=topics.get(j);
                similarity = calculateSimilarity(t1, t2);
                if(similarity != 0 || outputZeros) print(writer, ""+similarity);
                print(writer, "\t");
                setProgress(progress++);
            }
            print(writer, "\n");
        }
    }





    public void exportMatrixAsHTMLTable(PrintWriter writer, ArrayList<Topic> topics, WandoraToolLogger logger) throws TopicMapException {
        int progress = 0;
        Topic t1 = null;
        Topic t2 = null;
        double similarity = 0;
        print(writer, "<table border=1>");
        if(labelMatrix) {
            print(writer, "<tr><td> </td>");
            for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
                print(writer, "<td>"+makeString(topics.get(i))+"</td>");
            }
            print(writer, "</tr>\n");
        }
        for(int i=0; i<topics.size() && !logger.forceStop(); i++) {
            print(writer, "<tr>");
            t1=topics.get(i);
            if(labelMatrix) {
                print(writer, "<td>"+makeString(topics.get(i))+"</td>");
            }
            for(int j=0; j<topics.size() && !logger.forceStop(); j++) {
                t2=topics.get(j);
                similarity = calculateSimilarity(t1, t2);
                print(writer, "<td>");
                if(similarity != 0 || outputZeros) print(writer, ""+similarity);
                print(writer, "</td>");
                setProgress(progress++);
            }
            print(writer, "</tr>\n");
        }
        print(writer, "</table>");
    }

    // -------------------------------------------------------------------------



    
    public double calculateSimilarity(Topic t1, Topic t2) {
        double s = -1;
        try {
            s = currentSimilarityMeasure.similarity(t1, t2);
            s = roundSimilarity(s, numberOfDecimals);
            if(s < filterBelow) s = 0;
            if(s > maximizeAbove) s = 1;
        }
        catch(Exception e) {
            log(e);
        }
        return s;
    }



    
    protected double roundSimilarity(double s, int n) {
        double r = Math.pow(10, n);
        double ns = s * r;
        long ins = Math.round(ns);
        ns = ins / r;
        return ns;
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
