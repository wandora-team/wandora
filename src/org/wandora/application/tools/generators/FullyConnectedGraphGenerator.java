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
 * FullyConnectedGraphGenerator.java
 *
 * Created on 31. toukokuuta 2007, 11:33
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
public class FullyConnectedGraphGenerator extends AbstractGenerator implements WandoraTool {
    public static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    
    /** Creates a new instance of FullyConnectedGraphGenerator */
    public FullyConnectedGraphGenerator() {
    }

    @Override
    public String getName() {
        return "Fully connected graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a fully connected graph topic map.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Fully connected graph generator",
            "Fully connected graph generator is a topic map where every topic is association to every other topic.",
            true,new String[][]{
            new String[]{"Number of connected topics","string"},
            new String[]{"Directional associations","boolean","false","Create 2 associations per topic pair?"}
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        boolean directional = false;
        try {
            n = Integer.parseInt(values.get("Number of connected topics"));
            directional = Boolean.parseBoolean(values.get("Directional associations"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Fully connected graph generator");
        log("Creating topics");
        
        Topic[] topics = new Topic[n];
        
        setProgressMax(n);
        for(int i=0; i<n && !forceStop(); i++) {
            setProgress(n);
            topics[i] = getOrCreateTopic(topicmap, SI_PREFIX+i, "Topic "+i);
        }
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"associationType", "Association");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "Role 1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "Role 2");
        Association a = null;
        
        log("Creating associations");
        setProgress(0);
        setProgressMax(n*n);
        int nk = 0;
        for(int j=0; j<n && !forceStop(); j++) {
            if(directional) nk = 0;
            else nk = j;
            t1 = topics[j];
            for(int k=nk; k<n && !forceStop(); k++) {
                t2 = topics[k];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(j*n+k);
            }
        }
        setState(CLOSE);
    }
    
    }
