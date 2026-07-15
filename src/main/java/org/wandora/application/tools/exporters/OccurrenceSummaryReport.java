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
package org.wandora.application.tools.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */


public class OccurrenceSummaryReport extends AbstractExportTool {

	private static final long serialVersionUID = 1L;


	public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;
    
    
    public static final String TAB_FORMAT = "Tabulator separated plain text";
    public static final String HTML_FORMAT = "HTML table";
    
    private String outputFormat = TAB_FORMAT;
    
    
    
    /** Creates a new instance of OccurrenceSummaryReport */
    public OccurrenceSummaryReport() {
    }
    
    public OccurrenceSummaryReport(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/summary_report.png");
    }
    
    @Override
    public String getName() {
        return "Occurrence summary report";
    }

    @Override
    public String getDescription() {
        return "Occurrence summary report.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    

    
    

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        String topicMapName = null;
        String exportInfo = null;
        
        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting occurrence summary of selected topics";
        }
        else {
            tm = solveContextTopicMap(wandora, context);
            topicMapName = this.solveNameForTopicMap(wandora, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting occurrence summary of layer '" + topicMapName + "'";
            }
            else {
                exportInfo =  "Exporting occurrence summary";
            }
        }
        
        if(tm == null) return;
        
        Iterator<Topic> topics = tm.getTopics();
        
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        chooser.setDialogTitle(exportInfo+"...");
        
        if(chooser.open(wandora, "Export")==SimpleFileChooser.APPROVE_OPTION) {
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
                out = new FileOutputStream(file);
                log("Exporting occurrence summary report to '"+fileName+"'.");
                
                exportReport(out, topics, tm, getCurrentLogger());
                
                out.close();
                log("OK");
            }
            catch(Exception e) {
                log(e);
                try { if(out != null) out.close(); }
                catch(Exception e2) { log(e2); }
            }
            setState(WAIT);
        }
    }
    
    
    public void exportReport(OutputStream out, Iterator<Topic> topicIterator, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(out));
        }
        catch (Exception ex) {
            log("Exception while instantiating default PrintWriter. Aborting.");
            return;
        }

        int totalCount = 0;
        List<Topic> topics = new ArrayList<Topic>();
        Topic t = null;

        log("Collecting topics...");
        while(topicIterator.hasNext() && !logger.forceStop()) {
            t = topicIterator.next();
            if(t != null && !t.isRemoved()) {
                topics.add(t);
                totalCount++;
            }
        }
        Collections.sort(topics, new TMBox.TopicNameComparator(null));
        logger.setProgressMax(totalCount);

        // And finally export....
        log("Exporting report...");
        exportReportAsTabText(writer, topics, tm, logger);

        writer.flush();
        writer.close();
    }

    
    
    
    public void exportReportAsTabText(PrintWriter writer, List<Topic> topics, TopicMap tm, WandoraToolLogger logger) throws TopicMapException {
        List<Topic> occurrenceTypes = getOccurrenceTypes(tm);
        int occurrenceTypeCount = occurrenceTypes.size();

        for(Topic occurrenceType : occurrenceTypes) {
            writer.print("\t");
            writer.print(TopicToString.toString(occurrenceType));
        }
        writer.println();
        
        for(Topic t : topics) {
            writer.print(TopicToString.toString(t));
            for(int i=0; i<occurrenceTypeCount; i++) {
                writer.print("\t");
                Topic otype = occurrenceTypes.get(i);
                Hashtable<Topic,String> occurrence = t.getData(otype);
                if(occurrence != null && occurrence.size() > 0) {
                    boolean isFirst = true;
                    for(Topic scope : occurrence.keySet()) {
                        String occurrenceStr = occurrence.get(scope);
                        occurrenceStr = occurrenceStr.replace('\t', ' ');
                        occurrenceStr = occurrenceStr.replace('\n', ' ');
                        occurrenceStr = occurrenceStr.replace('\r', ' ');
                        writer.print(occurrenceStr);
                        if(!isFirst) writer.print("||||");
                        isFirst = false;
                    }
                }
            }
            writer.println();
            if(forceStop()) break;
        }
        if(forceStop()) {
            log("Operation cancelled!");
        }
    }
    
    
    
    private List<Topic> getOccurrenceTypes(TopicMap tm) throws TopicMapException {
        Set<Topic> occurrenceTypes = new LinkedHashSet<>();
        if(tm != null) {
            Topic t = null;
            Iterator<Topic> topics = tm.getTopics();
            while(topics.hasNext() && !forceStop()) {
                t = topics.next();
                if(t != null && !t.isRemoved()) {
                    occurrenceTypes.addAll(t.getDataTypes());
                }
            }
        }
        List<Topic> ot = new ArrayList<>();
        ot.addAll(occurrenceTypes);
        return ot;
    }
    
    
    
    
    
    
    
    
    
}