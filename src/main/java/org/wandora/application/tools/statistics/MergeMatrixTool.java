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
 * MergeMatrixTool.java
 *
 * Created on 29. toukokuuta 2007, 10:13
 *
 */

package org.wandora.application.tools.statistics;

import javax.swing.Icon;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.TopicMapException;
import java.util.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author
 * Eero
 */


public class MergeMatrixTool extends AbstractWandoraTool{

	private static final long serialVersionUID = 1L;

	protected HashMap<String, Integer> processedPairs;
    public MergeMatrixTool() {
        processedPairs = new HashMap<String, Integer>();
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_acount.png");
    }
    
    @Override
    public String getName() {
        return "Layer merge statistics";
    }

    @Override
    public String getDescription() {
        return "Gather and show statistics about topic merges between topic map layers.";
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        TopicMap tm = solveContextTopicMap(admin, context);
        String tmTitle = solveNameForTopicMap(admin,tm);
        
        GenericOptionsDialog god = new GenericOptionsDialog(
            admin,
            "Topic merge options",
            "Connection statistics options",
            true,
            new String[][]{
                new String[]{"Count percentage","boolean","true"},
            },admin
         );
        
        god.setVisible(true);
        if(god.wasCancelled()) return;
        setDefaultLogger();
        
        Map<String, String> values = god.getValues();
        boolean countPer = Boolean.parseBoolean(values.get("Count percentage"));
        
        List<Layer> rootLayers = admin.getTopicMap().getLayers();
        List<Layer> allLayers = getChildLayers(rootLayers);
        List<List<String>> sl = new ArrayList<>();
        List<String> currentRow;
        setProgressMax(allLayers.size()*allLayers.size());
        int prog = 0;
        for(Layer l : allLayers){
            if (forceStop()) break;
            currentRow = new ArrayList<String>();
            currentRow.add(l.getName());
            for (Layer ll : allLayers) {
                log("now processing " + l.getName() + " - " + ll.getName());
                prog++;
                setProgress(prog);
                if (forceStop()) break;
                try{
                    int merges;
                    int total;
                    String possiblyProcessed = ll.getName() + "-" + l.getName();
                    if(l.equals(ll)){
                        merges = 1;
                        total = 1;
                    } else if(processedPairs.containsKey(possiblyProcessed)){
                        merges = processedPairs.get(possiblyProcessed);
                        total = l.getTopicMap().getNumTopics();
                    } else {
                        merges = getMatrixMerges(l,ll);
                        processedPairs.put(l.getName() + "-" + ll.getName(), merges);
                        total = l.getTopicMap().getNumTopics();
                    }
                    if(countPer){
                        float per = 100 * (float)merges / (float)total;
                        DecimalFormat df = new DecimalFormat("0.0");
                        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
                        dfs.setDecimalSeparator('.');
                        df.setDecimalFormatSymbols(dfs);
                        String perf  = df.format(per);
                        currentRow.add(perf);
                    } else {
                        String ms = Integer.toString(merges);
                        String ts = Integer.toString(total);
                        String r = ms + "/" + ts;
                        currentRow.add(r);
                    }
                } catch(TopicMapException e){
                    currentRow.add("error");
                }
            }
            sl.add(currentRow);
        }
        if (!forceStop()) new MergeMatrixToolPanel(admin,sl);
        
        setState(CLOSE);
    }
    
    private List<Layer> getChildLayers(List<Layer> ll){
        List<Layer> returnedLayers = new ArrayList<Layer>();
        returnedLayers.addAll(ll);
        for (Layer layer : ll) {
            TopicMap ltm = layer.getTopicMap();
            if(ltm instanceof ContainerTopicMap) {
                ContainerTopicMap ctm = (ContainerTopicMap)ltm;
                returnedLayers.addAll(getChildLayers(ctm.getLayers()));
            } 
        }
        return returnedLayers;
    }
    
    private int getMatrixMerges(Layer l, Layer ll) throws TopicMapException{
        int n = 0;
        Iterator<Topic> ti = l.getTopicMap().getTopics();
        while(ti.hasNext()){
            Topic t = (Topic)ti.next();
            Iterator<Topic> tti = ll.getTopicMap().getTopics();
            while(tti.hasNext()){
                Topic tt = (Topic)tti.next();
                if(t.mergesWithTopic(tt)){
                    n++;
                }
            }
        }
        return n;
    }
    
}
