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
 * SOMClassifier.java
 *
 * Created on 29. heinakuuta 2008, 17:42
 *
 */
package org.wandora.application.tools.som;


import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import java.util.*;




/**
 * <p>
 * Creates a Self Organized Map (SOM) using given associations. 
 * SOM is a 2D matrix visualization of given association player topics where 
 * similar topics i.e. topics with similar associations locate near each other.
 * Tool is able to group similar topics.
 * </p>
 * <p>
 * This class is strictly speaking the a train vector builder while the
 * actual SOM implementation locates in class SOMMap.
 * </p>
 * <p>
 * About train vector building: Lets say user has selected eight binary associations
 * a-q, a-e, a-t, b-e, b-l, c-q, c-o, and d-e. User selects first player as the
 * grouping role. Training vectors for this setting are:
 * </p>
 * <p>
 * a: [ q=1, e=1, t=1, l=0, o=0 ] <br>
 * b: [ q=0, e=1, t=0, l=1, o=0 ] <br>
 * c: [ q=1, e=0, t=1, l=0, o=1 ] <br>
 * d: [ q=0, e=1, t=0, l=0, o=0 ] <br>
 * </p>
 * <p>
 * in other words: <br>
 * a: [ 1, 1, 1, 0, 0 ] <br>
 * b: [ 0, 1, 0, 1, 0 ] <br>
 * c: [ 1, 0, 1, 0, 1 ] <br>
 * d: [ 0, 1, 0, 0, 0 ] <br>
 * </p>
 * <p>
 * One should note SOM gives best results if vectors are NOT orthogonal.
 * </p>
 * <p>
 * Read more about Self Organizing Maps here:
 * http://en.wikipedia.org/wiki/Self-organizing_map
 * </p>
 * 
 * @author akivela
 */
public class SOMClassifier extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;




	public SOMClassifier(Context preferredContext) {
        this.setContext(preferredContext);
    }
    
    
    
   
    @Override
    public String getName() {
        return "SOM of associations";
    }
    @Override
    public String getDescription() {
        return "Creates a Self Organized Map (SOM) using given associations. "+
               "SOM is a 2D matrix visualization of given association player topics where "+
               "similar topics i.e. topics with similar associations locate near each other. "+
               "Tool is able to group similar topics.";
    }
    
    public void execute(Wandora wandora, Context context) {
        if(context instanceof AssociationContext) {
            try {
                Iterator<Association> associations = context.getContextObjects();
                Set<Topic> roles = getRolesFrom(associations);
                if(roles.size() > 0) { 
                    Topic groupingRole = (Topic) WandoraOptionPane.showOptionDialog(wandora, "Select grouping role topic", "Select grouping role topic", WandoraOptionPane.OK_CANCEL_OPTION, roles.toArray( new Topic[]{} ), roles.iterator().next());
                    // Topic groupingRole = admin.showTopicFinder(admin, "Select grouping role topic");
                    setDefaultLogger();
                    setLogTitle("SOM of associations");
                    if(groupingRole != null) {
                        log("Building input vectors");
                        Map<Topic,Set<Topic>> inputSets = buildInputSets(wandora, groupingRole, context.getContextObjects());
                        Map<Topic,SOMVector> inputVectors = buildInputVectors(inputSets);
                        log("Training SOM");
                        SOMMap map = new SOMMap(inputVectors, this);
                        SOMTopicVisualization visualization = new SOMTopicVisualization(wandora);
                        setState(INVISIBLE);
                        visualization.visualize(map);
                        setState(VISIBLE);
                        log("Ready.");
                    }
                    else {
                        log("Grouping role was not selected. Cancelling.");
                    }
                }
                else {
                    log("No roles found. Cancelling.");
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        else if( context instanceof LayeredTopicContext ) {
            System.out.println("LayeredTopicContext not processed!");
        }
        else {
            System.out.println("Unknown context not processed!");
        }
        setState(WAIT);
    }
    

    
    private Set<Topic> getRolesFrom(Iterator<Association> associations) throws TopicMapException {
        Set<Topic> roles = new HashSet<Topic>();
        if(associations != null && associations.hasNext()) {
            Association a = null;
            Collection<Topic> aroles = null;
            while(associations.hasNext() && !forceStop()) {
                a = associations.next();
                if(a != null && !a.isRemoved()) {
                    aroles = a.getRoles();
                    roles.addAll(aroles);
                }
            }
        }
        return roles;
    }
    
    
    
    
    private Map<Topic,Set<Topic>> buildInputSets(Wandora admin, Topic groupingRole, Iterator<Association> associations) {
        HashMap<Topic,Set<Topic>> resultSets = new LinkedHashMap<Topic,Set<Topic>>();
        Association a = null;
        Topic groupingTopic = null;
        Topic role = null;
        Topic player = null;
        Set<Topic> resultSet;
        
        while(associations.hasNext()) {
            try {
                a = associations.next();
                groupingTopic = a.getPlayer(groupingRole);
                
                resultSet = resultSets.get(groupingTopic);
                if(resultSet == null) {
                    resultSet = new HashSet<Topic>();
                    resultSets.put(groupingTopic, resultSet);
                }
                Collection<Topic> roles = a.getRoles();
                Iterator<Topic> roleIterator = roles.iterator();
                while(roleIterator.hasNext()) {
                    role = roleIterator.next();
                    if(!role.mergesWithTopic(groupingRole)) {
                        player = a.getPlayer(role);
                        if(player != null && !player.isRemoved()) {
                            resultSet.add(player);
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        return resultSets;
    }

    
    
    
    
    private Map<Topic,SOMVector> buildInputVectors(Map<Topic,Set<Topic>> inputSets) {
        ArrayList<Topic> fullVector = new ArrayList<Topic>();
        Collection<Set<Topic>> sets = inputSets.values();
        Set<Topic> set = null;
        Topic t = null;
        for(Iterator<Set<Topic>> setIterator = sets.iterator(); setIterator.hasNext(); ) {
            try {
                set = setIterator.next();
                for(Iterator<Topic> topicIterator = set.iterator(); topicIterator.hasNext(); ) {
                    t = topicIterator.next();
                    if(t != null && !t.isRemoved()) {
                        if(!fullVector.contains(t)) fullVector.add(t);
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        
        Map<Topic,SOMVector> inputVectors = new LinkedHashMap<Topic,SOMVector>();
        int s = fullVector.size();
        Set<Topic> keys = inputSets.keySet();
        Iterator<Topic> keyIterator = keys.iterator();
        Topic key = null;
        set = null;
        Topic[] vector = null;
        while(keyIterator.hasNext()) {
            try {
                key = keyIterator.next();
                if(key != null && !key.isRemoved()) {
                    set = inputSets.get(key);
                    vector = new Topic[s];
                    for(Iterator<Topic> topicIterator = set.iterator(); topicIterator.hasNext(); ) {
                        t = topicIterator.next();
                        if(t != null && !t.isRemoved()) {
                            int i = fullVector.indexOf(t);
                            vector[i] = t;
                        }
                    }
                    inputVectors.put(key, topicVector2SOMVector(vector));
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        
        return inputVectors;
    }
    
    
    
    
    public SOMVector topicVector2SOMVector(Topic[] topicVector) {
        int dim = topicVector.length;
        SOMVector somVector = new SOMVector(dim);
        for(int i=0; i<dim; i++) {
            if(topicVector[i] == null) somVector.set(i, 0);
            else somVector.set(i, 1);
        }
        return somVector;
    }
    
    
    // -------------------------------------------------------------------------
    

    
    

}

