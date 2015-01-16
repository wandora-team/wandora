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
 * TreeGraphGenerator.java
 *
 * Created on 31. toukokuuta 2007, 13:21
 *
 */

package org.wandora.application.tools.generators;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class TreeGraphGenerator extends AbstractGenerator implements WandoraTool {
    public static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    
    /** Creates a new instance of TreeGraphGenerator */
    public TreeGraphGenerator() {
    }
    

    @Override
    public String getName() {
        return "Tree graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a tree graph topic map.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Tree graph generator",
            "Tree graph generator options",
            true,new String[][]{
            new String[]{"Tree depth","string"},
            new String[]{"Number of child nodes","string"},
            new String[]{"Cayman tree","boolean","false","Should the root contain +1 edges?"},
            
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int d = 0;
        int n = 0;
        boolean isCayman = false;
        try {
            d = Integer.parseInt(values.get("Tree depth"));
            n = Integer.parseInt(values.get("Number of child nodes"));
            isCayman=Boolean.parseBoolean(values.get("Cayman tree"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Tree graph generator");
        log("Creating tree graph");
        
        ArrayList<Topic> topics = new ArrayList<Topic>();
        ArrayList<Topic> nextTopics = null;
        Topic parent = null;
        Topic child = null;
        int j = 0;
        String id = "";
        
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"associationType", "Association");
        Topic parentT = getOrCreateTopic(topicmap, SI_PREFIX+"parent", "Parent");
        Topic childT = getOrCreateTopic(topicmap, SI_PREFIX+"child", "Child");
        Association a = null;
        
        topics.add(getOrCreateTopic(topicmap, SI_PREFIX+"root", "Topic root"));
        
        setProgressMax((int) Math.pow(n, d-1));
        int progress=0;
        int nn = n;
        for(int dep=0; dep<d && !forceStop(); dep++) {
            j = 0;
            nextTopics = new ArrayList<Topic>();
            if(isCayman && dep == 0) nn = n+1;
            else nn = n;
            for(Iterator<Topic> topicIterator = topics.iterator(); topicIterator.hasNext() && !forceStop();) {
                j++;
                parent = topicIterator.next();
                for(int k=0; k<nn && !forceStop(); k++) {
                    id = dep+"-"+j+"-"+k;
                    child = getOrCreateTopic(topicmap, SI_PREFIX+id, "Topic "+id);
                    nextTopics.add(child);
                    
                    a = topicmap.createAssociation(aType);
                    a.addPlayer(parent, parentT);
                    a.addPlayer(child, childT);
                    
                    setProgress(progress++);
                }
            }
            topics = nextTopics;
        }
        setState(CLOSE);
    }
    
    
    
    
}
