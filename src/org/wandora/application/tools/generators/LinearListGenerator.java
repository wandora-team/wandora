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
 * LinearListGenerator.java
 *
 * Created on 31. toukokuuta 2007, 16:28
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
public class LinearListGenerator extends AbstractGenerator implements WandoraTool {
    public static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    
    /** Creates a new instance of LinearListGenerator */
    public LinearListGenerator() {
    }


    @Override
    public String getName() {
        return "Linear list graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a linear list graph topic map.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Linear list graph generator",
            "Linear list graph generator creates a topic map of given number of " +
              "topics associated as a linked list.",
            true,new String[][]{
            new String[]{"Number of list nodes","string"},
            new String[]{"Make cycle","boolean","false","Link last and first node?"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        boolean makeCycle = false;
        try {
            n = Integer.parseInt(values.get("Number of list nodes"));
            makeCycle=Boolean.parseBoolean(values.get("Make cycle"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Linear list graph generator");
        log("Creating linear list graph");
               
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"associationType", "Association");
        Topic previousT = getOrCreateTopic(topicmap, SI_PREFIX+"previous", "Previous");
        Topic nextT = getOrCreateTopic(topicmap, SI_PREFIX+"next", "Next");
        Association a = null;
        
        Topic first = getOrCreateTopic(topicmap, SI_PREFIX+"0", "Topic 0");
        Topic previous = first;
        Topic next = null;
        
        setProgressMax(n);
        int progress=0;
        for(int i=1; i<n && !forceStop(); i++) {
            next = getOrCreateTopic(topicmap, SI_PREFIX+i, "Topic "+i);
            a = topicmap.createAssociation(aType);
            a.addPlayer(previous, previousT);
            a.addPlayer(next, nextT);
            setProgress(progress++);
            previous = next;
        }
        if(makeCycle) {
            a = topicmap.createAssociation(aType);
            a.addPlayer(previous, previousT);
            a.addPlayer(first, nextT);
        }
        setState(CLOSE);
    }
    
    
}
