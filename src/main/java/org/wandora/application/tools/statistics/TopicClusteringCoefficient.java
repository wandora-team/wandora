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
 * TopicClusteringCoefficient.java
 *
 * Created on 30. toukokuuta 2007, 13:34
 *
 */

package org.wandora.application.tools.statistics;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import java.util.*;
import javax.swing.*;



/**
 *
 * @author olli
 */
public class TopicClusteringCoefficient extends AbstractWandoraTool {
    

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of TopicClusteringCoefficient */
    public TopicClusteringCoefficient() {
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/clustering_coefficient.png");
    }
    
    @Override
    public String getName() {
        return "Topic clustering coefficient";
    }

    @Override
    public String getDescription() {
        return "Calculates clustering coefficient for a single topic.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    
    public static double getClusteringCoefficientFor(Topic t,TopicNeighborhood n) throws TopicMapException {
        Collection<Topic> nc=n.getNeighborhood(t);
        if(nc.size()<=1) return 0.0;
        int associationCounter=0;
        
        TopicHashSet l;
        if(nc instanceof TopicHashSet) l=(TopicHashSet)nc;
        else l=new TopicHashSet(nc);
        
        for(Topic it : l){
            for(Topic jt : n.getNeighborhood(it)){
                if(l.contains(jt)) associationCounter++;
            }
        }
        return (double)associationCounter/(double)(l.size()*(l.size()-1));

        /*
        ArrayList<Topic> l=new ArrayList<Topic>(nc);
        l.add(t);
        for(int i=0;i<l.size();i++){
            Topic it=l.get(i);
            for(int j=i+1;j<l.size();j++){
                Topic jt=l.get(j);
                if(n.areLinked(it,jt)) associationCounter++;
            }
        }
        return 2.0*(double)associationCounter/(double)(l.size()*(l.size()-1));
         */
    }
    
    public static double getAverageClusteringCoefficient(TopicMap tm, TopicNeighborhood n) throws TopicMapException {
        double sum=0.0;
        int counter=0;
        Iterator<Topic> iter=tm.getTopics();
        while(iter.hasNext()){
            sum+=getClusteringCoefficientFor(iter.next(),n);
            counter++;
        }
        return sum/(double)counter;
    }

    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Iterator iter=context.getContextObjects();
        TopicNeighborhood n=new DefaultNeighborhood();
        double average=0.0;
        int counter=0;
        setDefaultLogger();
        while(iter.hasNext() && !forceStop()){
            Object co=iter.next();
            Topic t;
            if(co instanceof Topic){
                t=(Topic)co;
            }
            else{
                log("Context object is not a Topic");
                continue;
            }
            double c=getClusteringCoefficientFor(t,n);
            log("Clustering coefficient for topic "+getTopicName(t)+" is "+c);
            average+=c;
            counter++;
        }
        if(forceStop()) {
            log("Operation cancelled!");
            log("Unfinished average clustering coefficient is "+(average/(double)counter));
        }
        else {
            log("Average clustering coefficient is "+(average/(double)counter));
        }
        setState(WAIT);
    }    
    
    public static interface TopicNeighborhood {
        public Collection<Topic> getNeighborhood(Topic t) throws TopicMapException ;
        public boolean areLinked(Topic t1,Topic t2) throws TopicMapException ;
    }
    
    public static class DefaultNeighborhood implements TopicNeighborhood {
        public Collection<Topic> getNeighborhood(Topic t) throws TopicMapException {
            TopicHashSet ret=new TopicHashSet();
            for(Association a : t.getAssociations()){
                for(Topic role : a.getRoles()){
                    Topic u=a.getPlayer(role);
                    if(!u.mergesWithTopic(t)) ret.add(u);
                }
            }
            return ret;
        }
        public boolean areLinked(Topic t1,Topic t2) throws TopicMapException {
            for(Association a : t1.getAssociations()){
                for(Topic role : a.getRoles()){
                    Topic u=a.getPlayer(role);
                    if(u.mergesWithTopic(t2)) return true;
                }
            }            
            return false;
        }
    }
}
