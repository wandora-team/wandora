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
 * CopyTopics.java
 *
 * Created on 30. lokakuuta 2005, 19:04
 *
 */

package org.wandora.application.tools;



import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.statistics.*;
import org.wandora.application.gui.*;
import org.wandora.utils.*;

import java.util.*;
import javax.swing.*;
import org.wandora.application.gui.topicstringify.TopicToString;



/**
 *
 * @author akivela
 */
public class CopyTopics extends AbstractWandoraTool implements WandoraTool {
    public static final boolean OUTPUT_LOG = false;
  
    public static final int COPY_BASENAMES = 100;
    public static final int COPY_SIS = 101;
    
    public static final int INCLUDE_NOTHING = 1000;
    public static final int INCLUDE_NAMES = 1001;
    public static final int INCLUDE_SLS = 1002;
    public static final int INCLUDE_SIS = 1003;
    public static final int INCLUDE_CLASSES = 1004;
    public static final int INCLUDE_INSTANCES = 1005;
    public static final int INCLUDE_PLAYERS = 1006;
    public static final int INCLUDE_OCCURRENCES = 1007;
    public static final int INCLUDE_ALL_OCCURRENCES = 1008;
    public static final int INCLUDE_OCCURRENCE_TYPES = 1009;
    public static final int INCLUDE_ASSOCIATION_TYPES = 1010;
    
    public static final int INCLUDE_SI_COUNT = 1103;
    public static final int INCLUDE_CLASS_COUNT = 1104;
    public static final int INCLUDE_INSTANCE_COUNT = 1105;
    public static final int INCLUDE_ASSOCIATION_COUNT = 1106;
    public static final int INCLUDE_TYPED_ASSOCIATION_COUNT = 1107;
    
    public static final int INCLUDE_LAYER_DISTRIBUTION = 1200;
    public static final int INCLUDE_CLUSTER_COEFFICIENT = 1400;
    
    public int copyOrders = COPY_BASENAMES;
    public int includeOrders = 0;
   
    
    Topic associationType = null;
    Topic role = null;
    Topic occurrenceType = null;
    ArrayList<Topic> allOccurrenceTypes = null;
    Wandora admin = null;
    Iterator topics = null;
    
    
    public CopyTopics()  throws TopicMapException {
        this((Collection) null, COPY_BASENAMES, INCLUDE_NOTHING);
    }
    
    public CopyTopics(Wandora admin, Collection topics)  throws TopicMapException {
        this(admin, topics, COPY_BASENAMES, INCLUDE_NOTHING);
    }
    public CopyTopics(Wandora admin, Collection topics, int includeOrders)  throws TopicMapException {
        this(admin, topics, COPY_BASENAMES, includeOrders);
    }
    public CopyTopics(Wandora admin, Collection topics, int copyOrders, int includeOrders) throws TopicMapException {
        this.topics = topics.iterator();
        initialize(copyOrders, includeOrders);
        execute(admin);
    }
    
    
    public CopyTopics(Collection topics, int includeOrders) {
        this(topics, COPY_BASENAMES, includeOrders);
    }
    public CopyTopics(Collection topics, int copyOrders, int includeOrders) {
        if(topics != null) this.topics = topics.iterator();
        initialize(copyOrders, includeOrders);
    }
    public CopyTopics(int copyOrders, int includeOrders) {
        this((Collection) null, copyOrders, includeOrders);
    }
    public CopyTopics(int includeOrders) {
        this((Collection) null, COPY_BASENAMES, includeOrders);
    }
    
    
    public CopyTopics(Context context, int copyOrders, int includeOrders) {
        if(context != null) setContext(context);
        initialize(copyOrders, includeOrders);
    }
    public CopyTopics(Context context, int includeOrders) {
        if(context != null) setContext(context);
        initialize(COPY_BASENAMES, includeOrders);
    }
    
    
    public void initialize(int copyOrders, int includeOrders) {
        this.copyOrders = copyOrders;
        this.includeOrders = includeOrders;
    }
    
    
  
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/copy_topic.png");
    }
    
    @Override
    public String getName() {
        return "Copy topics";
    }

    @Override
    public String getDescription() {
        return "Copies selected topics (basenames or SIs) to clipboard.";
    }
    
    
    public String getTopicTypeName() {
        return "topics";
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    

    public void setTopics(Collection topics) {
        this.topics = topics.iterator();
    }
    public void setTopics(Topic[] topicArray) {
        ArrayList topicList = new ArrayList();
        if(topicArray != null) {
            for(int i=0; i<topicArray.length; i++) {
                topicList.add(topicArray[i]);
            }
        }
        topics = topicList.iterator();
    }
    
    
    
    
    public void execute(Wandora admin, Context context) {
        this.admin = admin;
        try {
            if(admin != null) {
                associationType = null;
                role = null;
                if(includeOrders == INCLUDE_TYPED_ASSOCIATION_COUNT) {
                    associationType=admin.showTopicFinder("Select association type...");                
                    if(associationType == null) return;
                }
                
                if(includeOrders == INCLUDE_PLAYERS) {
                    associationType=admin.showTopicFinder("Select association type...");                
                    if(associationType == null) return;
                    role=admin.showTopicFinder("Select role...");                
                    if(role == null) return;
                }

                occurrenceType = null;
                if(includeOrders == INCLUDE_OCCURRENCES) {
                    occurrenceType=admin.showTopicFinder("Select occurrence type...");                
                    if(occurrenceType == null) return;
                }
                
                allOccurrenceTypes = new ArrayList<Topic>();
                if(includeOrders == INCLUDE_ALL_OCCURRENCES) {
                    allOccurrenceTypes = getOccurrenceTypes(admin.getTopicMap());
                }
            }
            work();
        } 
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
 
    
    

    public void work() {
        if(OUTPUT_LOG) setDefaultLogger();
        
        try {
            Collection countCollection = null;
            Vector lines = new Vector();
            if(OUTPUT_LOG) log("Copying "+ getTopicTypeName() +" to clipboard...");
            Topic topic = null;
            if(topics == null) {
                topics = getContext().getContextObjects();
            }
            if(topics == null) topics = (new ArrayList()).iterator();
            int count = 0;
            
            
            while(topics.hasNext() && !forceStop()) {
                StringBuilder sb = new StringBuilder("");
                try {
                    count++;
                    setProgress(count);
                    topic = (Topic) topics.next();
                    String topicName = null;
                    if(topic != null) {
                        topicName = TopicToString.toString(topic);
                        if(OUTPUT_LOG) hlog("Copying from '" + topicName + "'.");
                        switch(copyOrders) {
                            case COPY_BASENAMES: {
                                sb.append(topic.getBaseName());
                                break;
                            }
                            case COPY_SIS: {
                                sb.append(topic.getOneSubjectIdentifier().toExternalForm());
                                break;
                            }
                        }
                        
                        switch(includeOrders) {
                            case INCLUDE_SLS: {
                                Locator sl = topic.getSubjectLocator();
                                if(sl != null) {
                                    sb.append("\t").append(sl.toExternalForm());
                                }
                                break;
                            }
                            case INCLUDE_SIS: {
                                Locator si = topic.getOneSubjectIdentifier();
                                if(si != null) {
                                    sb.append("\t").append(si.toExternalForm());
                                }
                                break;
                            }
                            case INCLUDE_SI_COUNT: {
                                countCollection = topic.getSubjectIdentifiers();
                                if(countCollection != null) {
                                    sb.append("\t").append(countCollection.size());
                                }
                                else {
                                    sb.append("\tNA");
                                }
                                break;
                            }
                            
                            case INCLUDE_CLASSES: {
                                Collection types = topic.getTypes();
                                Topic type = null;
                                for(Iterator iter = types.iterator(); iter.hasNext(); ) {
                                    type = (Topic) iter.next();
                                    if(type != null) {
                                        sb.append("\t").append(TopicToString.toString(type));
                                    }
                                }
                                break;
                            }
                            case INCLUDE_CLASS_COUNT: {
                                countCollection = topic.getTypes();
                                if(countCollection != null) {
                                    sb.append("\t").append(countCollection.size());
                                }
                                else {
                                    sb.append("\tNA");
                                }
                                break;
                            }
                            
                            case INCLUDE_INSTANCES: {
                                Collection instances = topic.getTopicMap().getTopicsOfType(topic);
                                Topic instance = null;
                                for(Iterator iter = instances.iterator(); iter.hasNext(); ) {
                                    instance = (Topic) iter.next();
                                    if(instance != null) {
                                        sb.append("\t").append(TopicToString.toString(instance));
                                    }
                                }
                                break;
                            }
                            
                            case INCLUDE_INSTANCE_COUNT: {
                                countCollection = topic.getTopicMap().getTopicsOfType(topic);
                                if(countCollection != null) {
                                    sb.append("\t").append(countCollection.size());
                                }
                                else {
                                    sb.append("\tNA");
                                }
                                break;
                            }
                            
                            case INCLUDE_NAMES: {
                                Collection scopes = topic.getVariantScopes();
                                if(scopes != null && scopes.size() > 0) {
                                    Iterator scopeIterator = scopes.iterator();
                                    Set scope = null;
                                    while(scopeIterator.hasNext()) {
                                        try {
                                            scope = (Set) scopeIterator.next();
                                            if(scope != null && scope.size() > 0) {
                                                sb.append("\t").append(topic.getVariant(scope));
                                            }
                                        }
                                        catch(Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break;
                            }
                            case INCLUDE_OCCURRENCES: {
                                Hashtable<Topic,String> occurrence = topic.getData(occurrenceType);
                                for(Topic scope : occurrence.keySet()) {
                                    String occurrenceStr = occurrence.get(scope);
                                    if(occurrenceStr != null) occurrenceStr = occurrenceStr.replace('\t', ' ');
                                    sb.append("\t").append(occurrenceStr);
                                }
                                break;
                            }
                                
                            case INCLUDE_ALL_OCCURRENCES: {
                                if(allOccurrenceTypes != null && !allOccurrenceTypes.isEmpty()) {
                                    for(Topic occurrenceType : allOccurrenceTypes) {
                                        sb.append("\t");
                                        Hashtable<Topic,String> occurrence = topic.getData(occurrenceType);
                                        boolean isFirst = true;
                                        for(Topic scope : occurrence.keySet()) {
                                            String occurrenceStr = occurrence.get(scope);
                                            if(occurrenceStr != null) occurrenceStr = occurrenceStr.replace('\t', ' ');
                                            if(!isFirst) sb.append("||||");
                                            sb.append(occurrenceStr);
                                            if(isFirst) isFirst = false;
                                        }
                                    }
                                }
                                break;
                            }
                                
                            case INCLUDE_OCCURRENCE_TYPES: {
                                Collection<Topic> occurrenceTypes = topic.getDataTypes();
                                TreeSet<Topic> sortedOccurrenceTypes = new TreeSet<Topic>(occurrenceTypes);
                                for(Topic t : sortedOccurrenceTypes) {
                                    sb.append("\t").append(TopicToString.toString(t));
                                }
                                break;
                            }
                                
                            case INCLUDE_ASSOCIATION_TYPES: {
                                Collection<Association> associations = topic.getAssociations();
                                HashSet<String> associationTypes = new LinkedHashSet();
                                for(Association association : associations) {
                                    associationTypes.add(TopicToString.toString(association.getType()));
                                }
                                for(String associationType : new TreeSet<String>(associationTypes)) {
                                    sb.append("\t").append(associationType);
                                }
                                break;
                            }
                                
                            case INCLUDE_PLAYERS: {
                                if(associationType != null && role != null) {
                                    Collection players = topic.getAssociations(associationType);
                                    Association association = null;
                                    Topic player = null;
                                    for(Iterator iter = players.iterator(); iter.hasNext(); ) {
                                        association = (Association) iter.next();
                                        if(association != null) {
                                            player = association.getPlayer(role);
                                            if(player != null) {
                                                //log("player ==" + player.getBaseName());
                                                sb.append("\t").append(TopicToString.toString(player));
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            
                            case INCLUDE_ASSOCIATION_COUNT: {
                                countCollection = topic.getAssociations();
                                if(countCollection != null) {
                                    sb.append("\t").append(countCollection.size());
                                }
                                else {
                                    sb.append("\tNA");
                                }
                                break;
                            }
                            
                            case INCLUDE_TYPED_ASSOCIATION_COUNT: {
                                countCollection = topic.getAssociations(associationType);
                                if(countCollection != null) {
                                    sb.append("\t").append(countCollection.size());
                                }
                                else {
                                    sb.append("\tNA");
                                }
                                break;
                            }
                            

                            case INCLUDE_LAYER_DISTRIBUTION: {
                                if(topic instanceof LayeredTopic) {
/*                                    LayeredTopic layeredTopic = (LayeredTopic) topic;
                                    java.util.List<Layer> layers = admin.layerControlPanel.layerStack.getLayers();
                                    Layer layer = null;
                                    Iterator<Layer> layerIterator = layers.iterator();
                                    while(layerIterator.hasNext()) {
                                        layer = layerIterator.next();
                                        sb.append("\t" + layeredTopic.getTopicsForLayer(layer).size());
                                    }*/
                                    String s=makeDistributionVector((LayeredTopic)topic, admin.getTopicMap());
                                    sb.append("\t").append(s);
                                }
                                break;
                            }
                            
                            
                            case INCLUDE_CLUSTER_COEFFICIENT: {
                                sb.append("\t").append(TopicClusteringCoefficient.getClusteringCoefficientFor(topic, new TopicClusteringCoefficient.DefaultNeighborhood()));
                            }
                        }

                        lines.add(sb.toString());
                    }
                }
                catch (Exception e) {
                    log(e);
                }
            }
            if(OUTPUT_LOG) log("Sorting clipboard text.");
            ClipboardBox.setClipboard(Textbox.sortStringVector(lines));
            if(OUTPUT_LOG) log("Total " + count + " topics copied.");
            if(OUTPUT_LOG) log("Ready.");
            if(OUTPUT_LOG) setState(WAIT);
            
            topics = null;
        }
        catch(Exception e) {
            log("Copying "+ getTopicTypeName() +" to clipboard failed!", e);
        }
    }
    
    
    
    
    public String makeDistributionVector(LayeredTopic topic,ContainerTopicMap tm) throws TopicMapException {
        StringBuilder sb=new StringBuilder();
        for(Layer l : tm.getLayers()){
//            if(sb.length()!=0) sb.append(":");
            if(sb.length()!=0) sb.append("\t");
            TopicMap ltm=l.getTopicMap();
            if(ltm instanceof ContainerTopicMap){
                sb.append("(\t");
                sb.append(makeDistributionVector(topic,(ContainerTopicMap)ltm));
                sb.append("\t)");
            }
            else{
                int num=0;
                if(l.isVisible()) num=l.getTopicMap().getMergingTopics(topic).size();
                sb.append("").append(num);
            }
        }
        return sb.toString();
    }
    
    
    
    public String getDisplayName(Topic t, String lang)  throws TopicMapException {
        String langsi=XTMPSI.getLang(lang);
        Topic langT =t.getTopicMap().getTopic(langsi);
        String dispsi=XTMPSI.DISPLAY;
        Topic dispT=t.getTopicMap().getTopic(dispsi);
        HashSet scope=new HashSet();
        if(langT!=null) scope.add(langT);
        if(dispT!=null) scope.add(dispT);
        String variantName = t.getVariant(scope);
        return (variantName == null ? "" : variantName);
    }
    
    
    public String getTextData(Topic t, Topic type, String versions)  throws TopicMapException {
        String langsi=XTMPSI.getLang(versions);
        Topic version=t.getTopicMap().getTopic(langsi);
        String textData = t.getData(type, version);
        return (textData == null ? "" : textData);
    }
    
    
    

    private ArrayList<Topic> getOccurrenceTypes(TopicMap tm) throws TopicMapException {
        HashSet<Topic> occurrenceTypes = new LinkedHashSet();
        if(tm != null) {
            Topic t = null;
            Iterator<Topic> topics = tm.getTopics();
            while(topics.hasNext() && !forceStop()) {
                t = topics.next();
                if(t != null && !t.isRemoved()) {
                    occurrenceTypes.addAll(t.getDataTypes());
                }
            }
        }
        ArrayList<Topic> ot = new ArrayList();
        ot.addAll(occurrenceTypes);
        return ot;
    }
    
    
}
