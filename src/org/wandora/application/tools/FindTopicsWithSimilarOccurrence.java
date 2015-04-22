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
 */


package org.wandora.application.tools;


import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import org.wandora.application.gui.search.SearchTopicsResults;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;

import uk.ac.shef.wit.simmetrics.similaritymetrics.*;


/**
 *
 * @author akivela
 */
public class FindTopicsWithSimilarOccurrence extends AbstractWandoraTool implements WandoraTool {
    public static final String OPTIONS_PREFIX = "options.occurrence.similarity.";
    
    
    private InterfaceStringMetric stringMetric = null;
    private float similarityThreshold = 0.5f;
    private boolean allowOverride = true;
    
    
    public FindTopicsWithSimilarOccurrence() {
        stringMetric = new Levenshtein();
    }
    public FindTopicsWithSimilarOccurrence(Context preferredContext) {
        stringMetric = new Levenshtein();
        this.setContext(preferredContext);
    }
    public FindTopicsWithSimilarOccurrence(Context preferredContext, InterfaceStringMetric stringMetric, float threshold) {
        this.setContext(preferredContext);
        this.stringMetric = stringMetric;
        this.similarityThreshold = threshold;
        allowOverride = false;
    }
    public FindTopicsWithSimilarOccurrence(InterfaceStringMetric stringMetric, float threshold) {
        this.stringMetric = stringMetric;
        this.similarityThreshold = threshold;
        allowOverride = false;
    }
    
    
    

    @Override
    public void initialize(Wandora wandora, org.wandora.utils.Options options,String prefix) throws TopicMapException {
        if(allowOverride) {
            try {
                String metric=options.get(OPTIONS_PREFIX+"metric");
                if(metric != null){
                    setMetricByClassName(metric);
                }
                similarityThreshold=options.getFloat(OPTIONS_PREFIX+"threshold", 0.75f);
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
    }

    @Override
    public boolean isConfigurable(){
        return true;
    }

    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        if(allowOverride) {
            try {
                initialize(wandora, options, prefix);
                GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Configurable options for similar occurrence topic finder","Find topic with similar occurrence options",true,new String[][]{
                    new String[]{"Similarity metric","string",stringMetric.getClass().getCanonicalName(),"Class name for similarity metric."},
                    new String[]{"Similarity threshold","string", ""+similarityThreshold, "0.0 - 1.0"},
                },wandora);
                god.setVisible(true);
                if(god.wasCancelled()) return;
                // ---- ok ----
                Map<String,String> values=god.getValues();
                setMetricByClassName(values.get("Similarity metric"));
                String thresholdString = values.get("Similarity threshold");
                similarityThreshold = Float.parseFloat(thresholdString);
                writeOptions(wandora, options, prefix);
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
    }

    @Override
    public void writeOptions(Wandora wandora, org.wandora.utils.Options options, String prefix){
        if(allowOverride) {
            options.put(OPTIONS_PREFIX+"metric", stringMetric.getClass().getCanonicalName());
            options.put(OPTIONS_PREFIX+"threshold", ""+similarityThreshold);
        }
    }
    
    private void setMetricByClassName(String metric) throws Exception {
        if(metric != null){
            Class metricClass = Class.forName(metric);
            if(metricClass != null) {
                stringMetric = (InterfaceStringMetric) metricClass.newInstance();
            }
        }
    }
    
    
    
    @Override
    public String getName() {
        return "Find topics with similar occurrence";
    }

    @Override
    public String getDescription() {
        return "Find topics with similar occurrence.";
    }
    

    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        String o1 = null;
        Topic o1Type = null;
        Topic o1Scope = null;

        try {
            Object source = getContext().getContextSource();
            if(source instanceof OccurrenceTextEditor) {
                OccurrenceTextEditor editor = (OccurrenceTextEditor) source;
                o1Type = editor.getOccurrenceType();
                o1Scope = editor.getOccurrenceVersion();
                o1 = editor.getSelectedText();
                if(o1 == null || o1.length() == 0) {
                    o1 = editor.getText();
                }
            }

            // Ensure occurrence type and scope are really ok...
            if(o1Type == null) {
                o1Type=wandora.showTopicFinder("Select occurrence type...");                
                if(o1Type == null) return;
            }
            if(o1Scope == null) {
                o1Scope=wandora.showTopicFinder("Select occurrence scope...");                
                if(o1Scope == null) return;
            }
            if(o1 == null) {
                Topic o1Topic = (Topic) topics.next();
                o1 = o1Topic.getData(o1Type, o1Scope);
            }

            // Initialize tool logger and variable...
            setDefaultLogger();
            setLogTitle("Find topics with similar occurrences");
            log("Searching for topics with similar occurrences");

            if(o1 == null || o1.length() == 0) {
                log("Found no reference occurrence or reference occurrence length is zero.");
            }
            else {
                HashSet<Topic> results = new HashSet<Topic>();
                TopicMap tm = wandora.getTopicMap();
                Iterator<Topic> allTopics = tm.getTopics();
                Topic t = null;
                String o2;
                float similarity = 0.0f;
                int progress = 0;

                while(allTopics.hasNext() && !forceStop()) {
                    t = allTopics.next();
                    if(t != null && !t.isRemoved()) {
                        o2 = t.getData(o1Type, o1Scope);
                        if(o2 != null && o2.length() > 0) {
                            similarity = stringMetric.getSimilarity(o1, o2);
                            if(similarity > similarityThreshold) {
                                results.add(t);
                                log("Found '"+getTopicName(t)+"'");
                            }
                        }
                    }
                    setProgress(progress++);
                }

                if(results.size() > 0) {
                    SearchTopicsResults resultDialog = new SearchTopicsResults(wandora, results);
                    resultDialog.hideAgainButton();
                    setState(INVISIBLE);
                    resultDialog.setVisible(true);
                    return;
                }
                else {
                    log("Found no topics with similar occurrences.");
                }
            }
        }
        catch(Exception e){
            log(e);
        }
        setState(WAIT);
    }

    
    @Override
    public boolean requiresRefresh() {
        return false;
    }

}
