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
 * AverageClusteringCoefficient.java
 *
 * Created on 30. toukokuuta 2007, 14:43
 *
 */

package org.wandora.application.tools.statistics;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;

import java.util.*;
import javax.swing.*;

import org.wandora.application.gui.*;
import org.wandora.topicmap.layered.*;



/**
 *
 * @author olli
 */
public class AverageClusteringCoefficient extends AbstractWandoraTool {
    

    public AverageClusteringCoefficient() {
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/clustering_coefficient.png");
    }
    
    @Override
    public String getName() {
        return "Topic map clustering coefficient";
    }

    @Override
    public String getDescription() {
        return "Tool is used calculate average clustering coefficient in layer.";
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        TopicMap tm = solveContextTopicMap(admin, context);
        String tmTitle = solveNameForTopicMap(admin, tm);
        if(tm==null) return;
        
        int numTopics=-1;
        if(!(tm instanceof LayerStack)) numTopics=tm.getNumTopics();        

        setDefaultLogger();
        if(numTopics!=-1) setProgressMax(numTopics);
        
        TopicClusteringCoefficient.TopicNeighborhood n = new TopicClusteringCoefficient.DefaultNeighborhood();
        log("Calculating clustering coefficient of '"+tmTitle+"'");
        log("Preparing topics 1/3");
        
        Iterator<Topic> iter=tm.getTopics();
        TopicHashMap<Integer> ids=new TopicHashMap<Integer>();
        int counter=0;
        while(iter.hasNext() && !forceStop()){
            if((counter%100)==0) setProgress(counter);
            Topic t = iter.next();
            ids.put(t,counter);
            counter++;
        }
        
        log("Preparing connections 2/3");
        HashMap<Integer,HashSet<Integer>> connections=new HashMap<Integer,HashSet<Integer>>();
        iter=tm.getTopics();
        counter=0;
        while(iter.hasNext() && !forceStop()){
            if((counter%100)==0) setProgress(counter);
            Topic topic = iter.next();
            Integer t=ids.get(topic);
            counter++;
            if(t==null) continue; // happens if topic has no basename, subject identifiers or subject locator
            HashSet<Integer> cs=new HashSet<Integer>();
            connections.put(t,cs);
            for(Topic p : n.getNeighborhood(topic)){
                Integer pint=ids.get(p);
                if(pint==null) continue;
                cs.add(pint);
            }
        }
        
        
        log("Calculating clustering coefficient 3/3");

        double sum=0.0;
        counter=0;
        iter=tm.getTopics();
        while(iter.hasNext() && !forceStop()){
            if((counter%100)==0) setProgress(counter);
            Topic topic = iter.next();
            int associationCounter=0;
            Integer t=ids.get(topic);
            counter++;
            if(t==null) continue; // happens if topic has no basename, subject identifiers or subject locator
            HashSet<Integer> tn=connections.get(t);
            if(tn.size()<=1) continue;
            for(Integer p : tn){
                HashSet<Integer> pn=connections.get(p);
                for(Integer r : pn){
                    if(p.equals(r)) continue;
                    if(tn.contains(r)) associationCounter++;
                }
            }
            
            sum+=(double)associationCounter/(double)(tn.size()*(tn.size()-1));
            
        }
 
/*        
        double sum=0.0;
        int counter=0;
        Iterator<Topic> iter=tm.getTopics();
        while(iter.hasNext()){
            if((counter%100)==0) setProgress(counter);
            Topic t = iter.next();
            sum+=TopicClusteringCoefficient.getClusteringCoefficientFor(t,n);
            counter++;
        }        */
        
        double c=sum/(double)counter;
        if(forceStop()) {
            log("Operation cancelled!");
        }
        else {
            log("Average coefficient for layer "+(tmTitle==null?"":(tmTitle+" "))+"is "+c);
        }
        setState(WAIT);
    }
    
}
