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
 * TopicMapDiameter.java
 *
 * Created on 1. kesäkuuta 2007, 12:41
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
public class TopicMapDiameter extends AbstractWandoraTool {
    
    /** Creates a new instance of TopicMapDiameter */
    public TopicMapDiameter() {
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topicmap_diameter.png");
    }
    
    @Override
    public String getName() {
        return "Topic map diameter";
    }

    @Override
    public String getDescription() {
        return "Calculates diameter of a topic map, that is maximum shortest path between two topics. Also calculates average shortest path between all topic pairs. Not usable for topic maps of approximately more than 10000 topics.";
    }

    public void execute(Wandora admin, Context context)  throws TopicMapException {
        TopicMap tm = solveContextTopicMap(admin, context);
        String tmTitle = solveNameForTopicMap(admin, tm);
        boolean useHash = false;
        
        int numTopics=-1;
        if(!(tm instanceof LayerStack)) numTopics=tm.getNumTopics();        

        setDefaultLogger();
        if(numTopics!=-1) setProgressMax(numTopics);
        
        TopicClusteringCoefficient.TopicNeighborhood n = new TopicClusteringCoefficient.DefaultNeighborhood();
        log("Calculating diameter of '"+tmTitle+"'");
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
        
        log("Preparing distance matrix 2/3");
        if(ids.size() > 10000) {
            setState(INVISIBLE);
            int a = WandoraOptionPane.showConfirmDialog(admin, "The topic map has more than 10 000 topics. "+
                    "It is very likely that diameter calculation generates an Out of Memory Exception with a such topic map! "+
                    "Do you want to use the altenative topic map diamater tool to lessen the burden on memory at an expense of processing time?"
                    , "Topic map size warning", WandoraOptionPane.YES_NO_CANCEL_OPTION);
            if(a == WandoraOptionPane.YES_OPTION) {
                setState(VISIBLE);
                useHash = true;
            } else if(a == WandoraOptionPane.CANCEL_OPTION){
                return;
            } else {
                setState(VISIBLE);
            }
        }
        
        double average;
        int max;
        boolean connected;
        
        if(useHash){
            TopicMapDiamaterArray path = new TopicMapDiamaterArray(ids.size());
        
            iter=tm.getTopics();
            counter=0;
            while(iter.hasNext() && !forceStop()){
                if((counter%100)==0) setProgress(counter);
                Topic topic = iter.next();
                Integer tint=ids.get(topic);
                counter++;
                if(tint==null) continue; // happens if topic has no basename, subject identifiers or subject locator
                int ti=tint.intValue();
                for(Topic p : n.getNeighborhood(topic)){
                    Integer pint=ids.get(p);
                    if(pint==null) continue;
                    path.put(ti, pint.intValue(), 1);
                }
            }        

            log("Calculating diameter 3/3");

            int size = ids.size();
            counter=0;
            setProgress(0);
            setProgressMax(size);
            for(int k=0;k<size && !forceStop();k++){
                setProgress(k);
                for(int i=0;i<size;i++){
                    int ik = path.get(i,k);
                    if(ik==-1) continue;
                    for(int j=0;j<size;j++){
                        int kj = path.get(k,j);
                        if(kj==-1) continue;
                        int ij = path.get(i,j);
                        if(ij > ik+kj || ij == -1) path.put(i,j,ik+kj);
                    }
                }
            }

            max=1;
            average=0;
            connected=true;
            counter=0;
            for(int i=0;i<ids.size() && !forceStop();i++){
                for(int j=0;j<ids.size() && !forceStop();j++){
                    if(i==j) continue;
                    if(path.get(i,j)!=-1){
                        if(path.get(i,j)>max) max=path.get(i,j);
                        average+=path.get(i,j);
                        counter++;
                    }
                    else connected=false;
                }
            }
        } else {
            int[][] path=new int[ids.size()][ids.size()];
            for(int i=0;i<path.length;i++){
                for(int j=0;j<path.length;j++){
                    path[i][j]=Integer.MAX_VALUE;
                }
            }

            iter=tm.getTopics();
            counter=0;
            while(iter.hasNext() && !forceStop()){
                if((counter%100)==0) setProgress(counter);
                Topic topic = iter.next();
                Integer tint=ids.get(topic);
                counter++;
                if(tint==null) continue; // happens if topic has no basename, subject identifiers or subject locator
                int ti=tint.intValue();
                for(Topic p : n.getNeighborhood(topic)){
                    Integer pint=ids.get(p);
                    if(pint==null) continue;
                    path[ti][pint.intValue()]=1;
                }
            }        

            log("Calculating diameter 3/3");

            counter=0;
            setProgress(0);
            setProgressMax(path.length);
            for(int k=0;k<path.length && !forceStop();k++){
                setProgress(k);
                for(int i=0;i<path.length && !forceStop();i++){
                    if(path[i][k]==Integer.MAX_VALUE) continue;
                    for(int j=0;j<path.length;j++){
                        if(path[k][j]==Integer.MAX_VALUE) continue;
                        if(path[i][j] > path[i][k]+path[k][j]) path[i][j]=path[i][k]+path[k][j];
                    }
                }
            }

            max=1;
            average=0;
            connected=true;
            counter=0;
            for(int i=0;i<path.length && !forceStop();i++){
                for(int j=0;j<path.length && !forceStop();j++){
                    if(i==j) continue;
                    if(path[i][j]!=Integer.MAX_VALUE){
                        if(path[i][j]>max) max=path[i][j];
                        average+=path[i][j];
                        counter++;
                    }
                    else connected=false;
                }
            }
        }
        
        
        average/=(double)counter;
        
        if(forceStop()) {
            log("Operation cancelled!");
            log("Unfinished topic map diameter is "+max);
            log("Unfinished average shortest path is "+average); 
        }
        else {
            if(connected){
                log("Topic map is connected!");
                log("Topic map diameter is "+max);
                log("Average shortest path is "+average);            
            }
            else{
                log("Topic map is not connected!");
                log("Diameter of largest connected component in topic map is "+max);
                log("Average shortest path in connected components is "+average);            
            }
        }
        
        setState(WAIT);
    }    
}
