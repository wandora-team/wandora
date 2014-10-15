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


import java.io.*;
import java.util.*;
import javax.swing.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import static org.wandora.modules.velocityhelpers.JSONBox.JSONEncode;

import org.wandora.topicmap.*;
import org.wandora.utils.IObox; 
    

/**
 * This is a simple Solr index (JSON) builder for a topic map. It creates a JSON
 * file used to update Solr index. Followed the documentation at 
 * http://wiki.apache.org/solr/UpdateJSON
 * 
 * @author akivela
 */


public class SolrIndexBuilder extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    public static boolean EXPORT_OCCURRENCES = true;

    
    
    public SolrIndexBuilder() {
    }
    public SolrIndexBuilder(boolean exportSelection) {
        EXPORT_SELECTION_INSTEAD_TOPIC_MAP = exportSelection;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/solr.png");
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
        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Solr index export options","Solr index export options",true,new String[][]{
            new String[]{"Export occurrences","boolean",(EXPORT_OCCURRENCES ? "true" : "false"),"Should topic occurrences also export?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        EXPORT_OCCURRENCES = ("true".equals(values.get("Export occurrences")) ? true : false );
    }
    
    
    
    

    @Override
    public void execute(Wandora admin, Context context) {
       String topicMapName = null;
       String exportInfo = null;

        // --- Solve first topic map to be exported
        TopicMap tm = null;
        if(EXPORT_SELECTION_INSTEAD_TOPIC_MAP) {
            tm = makeTopicMapWith(context);
            exportInfo = "Exporting selected topics as Solr index";
            topicMapName = "selection_in_wandora";
        }
        else {
            tm = solveContextTopicMap(admin, context);
            topicMapName = this.solveNameForTopicMap(admin, tm);
            if(topicMapName != null) {
                exportInfo =  "Exporting topic map in layer '" + topicMapName + "' as Solr index";
            }
            else {
                exportInfo =  "Exporting topic map as Solr index";
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
                file = IObox.addFileExtension(file, "json"); // Ensure file extension exists!
                fileName = file.getName(); // Updating filename if file has changed!
                out = new FileOutputStream(file);
                log(exportInfo+" to '"+fileName+"'.");
                exportSolrIndex(out, tm, topicMapName, getCurrentLogger());
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
        return "Export Solr index";
    }

    @Override
    public String getDescription() {
        return "Exports topic map as Solr index json file.";
    }

    
    
    
    
    public void exportSolrIndex(OutputStream out, TopicMap topicMap, String graphName, WandoraToolLogger logger) throws TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer = null;
        try {
            writer=new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        }
        catch(Exception e) {
            log("Unable to use UTF-8 charset. Using default charset.");
            writer=new PrintWriter(new OutputStreamWriter(out));
        }
        int totalCount = topicMap.getNumTopics();
        logger.setProgressMax(totalCount);
        int count = 0;
        
        writer.println("[");

        Iterator<Topic> topics=topicMap.getTopics();
        while(topics.hasNext() && !logger.forceStop()) {
            Topic t = topics.next();
            logger.setProgress(count++);
            echoTopic(t, writer);
            if(topics.hasNext()) writer.println(",");
        }

        writer.println("]");
        
        writer.flush();
        writer.close();
    }

    
    
    // --------------------------------------------------------------- ECHOS ---
    
    
    
    protected void echoTopic(Topic t, PrintWriter writer) throws TopicMapException {
        writer.println("{");
        echoPair("id", t.getBaseName(), writer);
        writer.println(",");
        
        ArrayList<String> titles = new ArrayList();
        Set<Set<Topic>> variantScopes = t.getVariantScopes();
        for(Set<Topic> variantScope : variantScopes) {
            String title = t.getVariant(variantScope);
            if(title != null && title.length() > 0) {
                titles.add(title);
            }
        }
        echoArray("title", titles, writer);
        writer.println(",");
        
        ArrayList<String> datas = new ArrayList();
        Collection<Topic> dataTypes = t.getDataTypes();
        for(Topic dataType : dataTypes) {
            Hashtable<Topic,String> dataTable = t.getData(dataType);
            for(Topic key : dataTable.keySet()) {
                if(key != null) {
                    String data = dataTable.get(key);
                    if(data != null) datas.add(data);
                }
            }
        }
        echoArray("data", titles, writer);
        
        writer.println("}");
    }
    
    
    protected void echoPair(String key, String value, PrintWriter writer) {
        writer.print(makeString(key));
        writer.print(":");
        writer.println(makeString(value));
    }

    protected void echoArray(String key, ArrayList<String> values, PrintWriter writer) {
        writer.print(makeString(key));
        writer.print(":[");
        for(int i=0; i<values.size(); i++) {
            writer.print(makeString(values.get(i)));
            if(i<values.size()) {
                writer.print(",");
            }
        }
        writer.println("]");
    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected int makeID(Topic t) {
        int id = t.hashCode();
        if(id < 0) return 2*Math.abs(id)+1;
        else return 2*Math.abs(id);
    }

    protected String makeString(String s) {
        return JSONEncode(s);
    }
}
