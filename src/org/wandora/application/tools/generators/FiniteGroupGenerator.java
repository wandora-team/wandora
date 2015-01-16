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
 * FiniteGroupGenerator.java
 *
 * Created on 31. toukokuuta 2007, 17:01
 *
 */

package org.wandora.application.tools.generators;




import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class FiniteGroupGenerator extends AbstractGenerator implements WandoraTool {
    public static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    
    
    /** Creates a new instance of FiniteGroupGenerator */
    public FiniteGroupGenerator() {
    }

    @Override
    public String getName() {
        return "Finite group graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a finite group (algebra) topic map.";
    }
    
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Finite group graph generator",
            "Finite group graph generator options",
            true,new String[][]{
            new String[]{"Number of group elements","string"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        boolean makeCycle = false;
        try {
            n = Integer.parseInt(values.get("Number of group elements"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Finite group graph generator");
        log("Creating a topic map for finite group (algebra).");
        
        Topic[] topics = new Topic[n];
        Topic operationType = getOrCreateTopic(topicmap, SI_PREFIX+"operation", "Operation");
        Topic operand1T = getOrCreateTopic(topicmap, SI_PREFIX+"operand1", "Operand 1");
        Topic operand2T = getOrCreateTopic(topicmap, SI_PREFIX+"operand2", "Operand 2");
        Topic resultT = getOrCreateTopic(topicmap, SI_PREFIX+"result", "Result");
        Association a = null;
        
        Topic o1;
        Topic o2;
        Topic r;
        
        setProgressMax(n);
        for(int i=0; i<n && !forceStop(); i++) {
            setProgress(n);
            topics[i] = getOrCreateTopic(topicmap, SI_PREFIX+i, "Topic "+i);
        }
        
        setProgressMax(n*n);
        int progress=0;
        for(int i=0; i<n && !forceStop(); i++) {
            for(int j=0; j<n && !forceStop(); j++) {
                o1 = topics[i];
                o2 = topics[j];
                r = topics[((i+j)%n)];
                a = topicmap.createAssociation(operationType);
                a.addPlayer(o1, operand1T);
                a.addPlayer(o2, operand2T);
                a.addPlayer(r, resultT);
                setProgress(progress++);
            }
        }
        setState(CLOSE);
    }
    

}