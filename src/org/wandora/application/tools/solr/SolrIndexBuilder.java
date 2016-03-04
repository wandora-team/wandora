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


package org.wandora.application.tools.solr;


import java.io.*;
import java.util.*;
import javax.swing.*;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import org.wandora.application.tools.exporters.AbstractExportTool;
import static org.wandora.modules.velocityhelpers.JSONBox.JSONEncode;

import org.wandora.topicmap.*;
import org.wandora.utils.IObox; 
    

/**
 * This index builder assumes the Solr database has fields:
 * 
 *  <pre>
 *  &lt;field name="id" type="string" indexed="true" stored="true" required="true" multiValued="false" /&gt; 
 *  &lt;field name="basename" type="text_general" indexed="true" stored="true" /&gt;  
 *  &lt;field name="sl" type="string" indexed="true" stored="true" multiValued="false" /&gt; 
 *  &lt;field name="si" type="string" indexed="true" stored="true" multiValued="true"/&gt; 
 *  &lt;field name="name" type="text_general" indexed="true" stored="true" multiValued="true"/&gt; 
 *  &lt;field name="occurrence" type="text_general" indexed="true" stored="true" multiValued="true"/&gt; 
 *  </pre>
 * 
 * http://wiki.apache.org/solr/Solrj
 * 
 * 
 * @author akivela
 */


public class SolrIndexBuilder extends AbstractExportTool implements WandoraTool {
    public boolean EXPORT_SELECTION_INSTEAD_TOPIC_MAP = false;

    protected static boolean indexOccurrences = true;
    
    protected boolean deleteEverythingFirst = true;
    protected String serverUrl = "http://localhost:8983/solr/";
    
    
    
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
            new String[]{"Index occurrences","boolean",(indexOccurrences ? "true" : "false"),"Should topic occurrences also index?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        indexOccurrences = ("true".equals(values.get("Index occurrences")) ? true : false );
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
                exportInfo =  "Export topic map in layer '" + topicMapName + "' as Solr index";
            }
            else {
                exportInfo =  "Exporting topic map as Solr index";
                topicMapName = "no_name_topic_map";
            }
        }

        GenericOptionsDialog god=new GenericOptionsDialog(admin,"Solr index export options","Solr index export options",true,new String[][]{
            new String[]{"Server URL", "String", serverUrl, "Where the Solr locates?"},
            new String[]{"Clear database before indexing?", "boolean", (deleteEverythingFirst ? "true" : "false"), "Clear database before indexing?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        deleteEverythingFirst = ("true".equals(values.get("Clear database before indexing?")) ? true : false );
        String serverUrl = values.get("Server URL");
        
        setDefaultLogger();
        if(serverUrl != null && serverUrl.length() > 0) {
            try {
                HttpSolrServer solr = new HttpSolrServer(serverUrl);

                if(deleteEverythingFirst) {
                    log("Clearing the index first.");
                    solr.deleteByQuery( "*:*" );
                }
                log("Indexing topics.");
                makeSolrIndexFor(tm, solr);
                if(forceStop()) {
                    log("Indexing stopped.");
                }
                else {
                    log("Indexing ready.");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                log(e);
            }
        }
        else {
            log("Connection URL is null or zero length. Can't connect Solr.");
        }
        setState(WAIT);
    }
    
    
    @Override
    public String getName() {
        return "Export Solr index";
    }

    @Override
    public String getDescription() {
        return "Exports topic map data to a Solr server instance as a search index.";
    }

    
    
    

    // ---------------------------------------------------------------- MAKE ---
    
    
    
    
    protected void makeSolrIndexFor(TopicMap tm, HttpSolrServer solr) throws TopicMapException, SolrServerException, IOException {
        if(tm != null) {
            int totalCount = tm.getNumTopics();
            setProgressMax(totalCount);
            final Iterator<Topic> topics=tm.getTopics();
            Iterator<SolrInputDocument> solrInputIterator = new Iterator<SolrInputDocument>() {
                int count = 0;

                @Override
                public boolean hasNext() {
                    return (topics.hasNext() && !forceStop());
                }

                @Override
                public SolrInputDocument next() {
                    setProgress(count++);
                    SolrInputDocument topicDocument = null;
                    try {
                        Topic t = topics.next();
                        topicDocument = makeSolrInputDocumentFor(t);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    return topicDocument;
                }

                @Override
                public void remove() {
                }
            };
            solr.add(solrInputIterator);
            solr.commit();
        }
    }
    
    
    
    protected SolrInputDocument makeSolrInputDocumentFor(Topic t) throws TopicMapException {
        SolrInputDocument topicDocument = new SolrInputDocument();
        
        if(t != null && !t.isRemoved()) {
            
            topicDocument.addField("id", getSolrIdFor(t));
            
            if(t.getBaseName() != null) {
                topicDocument.addField("basename", t.getBaseName());
            }

            if(t.getSubjectLocator() != null) {
                topicDocument.addField("sl", t.getBaseName());
            }

            for(Locator si : t.getSubjectIdentifiers()) {
                topicDocument.addField("si", si.toExternalForm());
            }

            Set<Set<Topic>> variantScopes = t.getVariantScopes();
            for(Set<Topic> variantScope : variantScopes) {
                String name = t.getVariant(variantScope);
                if(name != null && name.length() > 0) {
                    topicDocument.addField("name", name);
                }
            }

            if(indexOccurrences) {
                Collection<Topic> dataTypes = t.getDataTypes();
                for(Topic dataType : dataTypes) {
                    Hashtable<Topic,String> dataTable = t.getData(dataType);
                    for(Topic key : dataTable.keySet()) {
                        if(key != null) {
                            String data = dataTable.get(key);
                            if(data != null) {
                                topicDocument.addField("occurrence", data);
                            }
                        }
                    }
                }
            }
        }
        return topicDocument;
    }
    
    
    protected String getSolrIdFor(Topic t) {
        try { 
            return t.getOneSubjectIdentifier().toExternalForm();
        }
        catch(Exception e) {
            return ""+System.currentTimeMillis();
        }
    }
    
    // -------------------------------------------------------------------------
    
    
    /*
        Copy these to Solr's configuration
    
        <field name="id" type="string" indexed="true" stored="true" required="true" multiValued="false" /> 
        <field name="basename" type="text_general" indexed="true" stored="true"/>
        <field name="sl" type="string" indexed="true" stored="true" multiValued="false" />
        <field name="si" type="string" indexed="true" stored="true" multiValued="true"/>
        <field name="name" type="text_general" indexed="true" stored="true" multiValued="true"/>
        <field name="occurrence" type="text_general" indexed="true" stored="true" multiValued="true"/>
    */
    
}
