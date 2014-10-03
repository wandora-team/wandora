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
 * RandomGraphGenerator.java
 *
 * Created on 30. toukokuuta 2007, 14:11
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
public class RandomGraphGenerator extends AbstractGenerator implements WandoraTool {
    public static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    
    
    /** Creates a new instance of RandomGraphGenerator */
    public RandomGraphGenerator() {
    }

    @Override
    public String getName() {
        return "Random graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a random graph topic map.";
    }
    
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Random graph generator",
            "Random graph generator creates a topic map with numbered topics (nodes) "+
              "and random associations (edges). If 'number of associations' is a valid number "+
              "it overrides 'association probality'. Number is a positive integer. "+
              "Probability is a floating point number between 0.1 and 1.0.",
            true,new String[][]{
            new String[]{"Number of topics","string"},
            new String[]{"Number of random associations","string"},
            new String[]{"Random associations probability","string"}
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        int an = 0;
        double ap = 0.0;
        boolean useAssociationNumber = false;
        try {
            n = Integer.parseInt(values.get("Number of topics"));
            String ans = values.get("Number of random associations");
            if(ans != null && ans.length() > 0) {
                an = Integer.parseInt(values.get("Number of random associations"));
                useAssociationNumber = true;
            }
            else {
                ap = Double.parseDouble(values.get("Random associations probability"));
                useAssociationNumber = false;
            }
        }
        catch(NumberFormatException nfe) {
            singleLog("Parse error.\n"+
                      "Number of topics and number of associations should be integers.\n"+
                      "Association probability should be floating point number between 0.0 and 1.0.");
            return;
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Random graph generator");
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
        
        log("Creating random associations");
        setProgress(0);
        
        // Creating excact number of random associations!
        if(useAssociationNumber) {
            setProgressMax(an);
            for(int j=0; j<an && !forceStop(); j++) {
                int n1 = (int) Math.floor( Math.random() * n );
                int n2 = (int) Math.floor( Math.random() * n );

                t1 = topics[n1];
                t2 = topics[n2];

                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(j);
            }
        }
        
        // Creating associations with given probability!
        else {
            setProgressMax(n*n);
            double rd = 0.0;
            for(int j=0; j<n && !forceStop(); j++) {
                t1 = topics[j];
                for(int k=0; k<n && !forceStop(); k++) {
                    rd = Math.random();
                    if(rd < ap) {
                        t2 = topics[k];

                        a = topicmap.createAssociation(aType);
                        a.addPlayer(t1, role1);
                        a.addPlayer(t2, role2);
                    }
                    setProgress(j*n+k);
                }
            }
        }
        
        setState(CLOSE);
        
    }
    
    
    
}
