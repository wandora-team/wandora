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
 * TopicMapDiameter.java
 *
 * Created on 1.6.2007, 12:41
 *
 */

package org.wandora.application.tools.statistics;
import java.util.Iterator;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicHashMap;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.layered.LayerStack;


/**
 *
 * @author olli
 */

// Apparently uses Floyd-Warshall algorithm
public class TopicMapDiameterAlternative extends AbstractWandoraTool {
    
	private static final long serialVersionUID = 1L;

	/** Creates a new instance of TopicMapDiameter */
    public TopicMapDiameterAlternative() {
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topicmap_diameter.png");
    }
    
    @Override
    public String getName() {
        return "Topic map diameter (using a hash structure)";
    }

    @Override
    public String getDescription() {
        return "Calculates diameter of a topic map, that is maximum shortest path between two topics. Also calculates average shortest path between all topic pairs. Not usable for topic maps of approximately more than 10000 topics.";
    }

    public void execute(Wandora admin, Context context)  throws TopicMapException {
        TopicMap tm = solveContextTopicMap(admin, context);
        String tmTitle = solveNameForTopicMap(admin, tm);
        
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
        if(ids.size() > 2000) {
            setState(INVISIBLE);
            int a = WandoraOptionPane.showConfirmDialog(admin, "The topic map has more than 2 000 topics. "+
                    "It is very likely that diameter calculation takes a while."+
                    "Are you sure you want to continue?", "Topic map size warning", WandoraOptionPane.YES_NO_OPTION);
            if(a == WandoraOptionPane.NO_OPTION) {
                return;
            }
            else {
                setState(VISIBLE);
            }
        }
        
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
        
        int max=1;
        double average=0;
        boolean connected=true;
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
