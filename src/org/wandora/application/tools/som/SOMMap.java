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
 * SOMMap.java
 *
 * Created on 29. heinakuuta 2008, 17:42
 *
 */


package org.wandora.application.tools.som;

import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import static org.wandora.utils.Tuples.T2;
import static org.wandora.utils.Tuples.T3;


/**
 *
 * @author akivela
 */
public class SOMMap {
    public static boolean RANDOM_TRAINING_VECTOR_SELECTION = false;
    
    private HashMap<Topic,SOMVector> samples = null;
    
    private SOMNeuron[][] map = null;
    private int size = 0;

    private WandoraTool parent = null;
    
    private long startTime = 0;
    private long trainingStartTime = 0;
    private long trainingEndTime = 0;
    
    int progress = 0;
    int maxIterations = 1;
    
    
    
    public SOMMap(HashMap<Topic,SOMVector> samples, WandoraTool parent) {
        startTime = System.currentTimeMillis();
        this.samples = samples;
        this.parent = parent;
        size = (int) Math.ceil(Math.sqrt(samples.size()));
        map = new SOMNeuron[size][size];
        parent.log("Number of samples used to train SOM is "+samples.size());
        parent.log("SOM size is "+size+"x"+size);
        int dim = samples.values().iterator().next().dimension();
        initialize(dim);
        randomize();
        parent.log("Training SOM with samples!");
        trainingStartTime = System.currentTimeMillis();
        train();
        trainingEndTime = System.currentTimeMillis();
        if(parent.forceStop()) parent.log("Training cancelled!");
        parent.log("Training took "+((trainingEndTime-trainingStartTime)/1000)+" seconds and "+progress+" rounds (of total "+maxIterations+")");
    }
    
    
    public SOMMap(int numberOfNeurons) {
        size = (int) Math.ceil(Math.sqrt(numberOfNeurons));
        map = new SOMNeuron[size][size];
    }

    
    
    public void initialize(int dim) {
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
                map[i][j] = new SOMNeuron(dim);
            }
        }
    }
    


    public void randomize() {
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
                map[i][j].randomize();
            }
        }
    }


    public SOMNeuron getAt(int x, int y) {
        if (x>=0 && x<size && y>=0 && y<size) {
            return map[x][y];
        }
        else {
            return null;
        }
    }

    public int getSize() {
        return size;
    }
    
    public SOMVector getSampleFor(Topic topic) {
        return samples.get(topic);
    }


    public void train() {
        if(RANDOM_TRAINING_VECTOR_SELECTION) {
            double t = 0.0;
            maxIterations = 100*size;
            double step = 1.0 / maxIterations;
            parent.setProgressMax(maxIterations);
            Set<Topic> sampleTopics = samples.keySet();
            Topic[] sampleTopicArray = sampleTopics.toArray(new Topic[] {} );
            Topic sampleTopic = null;

            int s = sampleTopics.size();
            int randomIndex = 0;
            SOMVector sample;

            while(t < 1.0 && !parent.forceStop()) {
                parent.setProgress(progress++);
                randomIndex = (int) Math.round(Math.floor(Math.random()*s));
                sampleTopic = sampleTopicArray[randomIndex];
                sample = samples.get(sampleTopic);
                //parent.log("Teaching with sample: "+sample.toString());
                train(sampleTopic, sample, t);
                t += step;
            }
        }
        // ----- TRAIN WITH EVERY SAMPLE VECTOR -----
        else {
            double t = 0.0;
            maxIterations = 3*size;
            double step = 1.0 / maxIterations;
            parent.setProgressMax(maxIterations);
            Set<Topic> sampleTopics = samples.keySet();
            Topic[] sampleTopicArray = sampleTopics.toArray(new Topic[] {} );
            Topic sampleTopic = null;

            int s = sampleTopics.size();
            SOMVector sample;

            while(t < 1.0 && !parent.forceStop()) {
                parent.setProgress(progress++);
                for(int i=0; i<s; i++) {
                    sampleTopic = sampleTopicArray[i];
                    sample = samples.get(sampleTopic);
                    train(sampleTopic, sample, t);
                }
                t += step;
            }
        }
    }





    public void train(Topic sampleTopic, SOMVector sample, double t) {
        T3<Integer, Integer, SOMNeuron> bmu = getBMU(sample);
        try {
            //String basename = sampleTopic.getBaseName();
            //parent.hlog("Scaling neighbours of "+basename+"'s BMU at "+bmu.e1+","+bmu.e2+".");
        }
        catch(Exception e) {
            parent.log(e);
        }
        scaleNeighbours(bmu, sample, t);
    }


    

    public HashMap<Topic,SOMVector> getSamples() {
        return samples;
    }



    public T3<Integer, Integer, SOMNeuron> getBMU(SOMVector v) {
        ArrayList<T3<Integer, Integer, SOMNeuron>> matchList = new ArrayList<T3<Integer, Integer, SOMNeuron>>();
        T3<Integer, Integer, SOMNeuron> selectedMatch = null;
        SOMNeuron mapNeuron = null;
        double maxDistance = Double.MAX_VALUE;
        double distance = 0;
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
                try {
                    mapNeuron = map[i][j];
                    distance = v.distance( mapNeuron.getSOMVector() );
                    if(distance < maxDistance) {
                        matchList = new ArrayList<T3<Integer, Integer, SOMNeuron>>();
                        matchList.add(new T3(new Integer(i), new Integer(j), mapNeuron));
                        maxDistance = distance;
                    }
                    else if(distance == maxDistance) {
                        matchList.add(new T3(new Integer(i), new Integer(j), mapNeuron));
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(matchList.size() > 1) {
            int randomIndex = (int) Math.floor(Math.random()*matchList.size());
            selectedMatch = matchList.get(randomIndex);
        }
        else {
            selectedMatch = matchList.get(0);
            
        }
        return selectedMatch;
    }

    


    public void scaleNeighbours(T3<Integer, Integer, SOMNeuron> bmu, SOMVector input, double t2) {
        int[] loc = new int[2];
        loc[0] = bmu.e1.intValue();
        loc[1] = bmu.e2.intValue();
                
        int R2 = (int) Math.round(((size)*(1.0f-t2))/2.0f);
        double normalization = Math.sqrt(R2*R2+R2*R2);
        SOMVector v = input.duplicate();
        for (int i=-R2; i<R2 && !parent.forceStop(); i++) {
              for (int j=-R2; j<R2 && !parent.forceStop(); j++) {
                  if ((i+loc[0])>=0 && (i+loc[0])<size && (j+loc[1])>=0 && (j+loc[1])<size) {
                      try {
                          //parent.log("Scaling map vector at "+(i+loc[0])+","+(j+loc[1]));
                          //parent.log("Before: "+map[loc[0]+i][loc[1]+j].getSOMVector().toString());
                          double distance = Math.sqrt(i*i+j*j);
                          distance /= normalization;

                          double scaleFactor = Math.exp( -(distance*distance)/0.15f );
                          scaleFactor /= (t2*4.0f+1.0f);

                          v.multiply(scaleFactor);
                          v.add(map[loc[0]+i][loc[1]+j].getSOMVector());
                          v.multiply(1.0 - scaleFactor);
                          
                          map[loc[0]+i][loc[1]+j].setSOMVector(v);
                          //parent.log("After: "+map[loc[0]+i][loc[1]+j].getSOMVector().toString());
                          
                      }
                      catch(Exception e) {
                          parent.log(e);
                      }
                  }
              }
        }
    }


    public int[] findLocation(SOMNeuron mapNeuron) {
        int[] location = null;
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
                try {
                    if(mapNeuron.equals(map[i][j])) {
                        location = new int[2];
                        location[i] = i;
                        location[j] = j;
                        break;
                    }
                }
                catch(Exception e) {

                }
            }
        }
        return location;
    }
    
    
    
    

    

}
