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


	private static final long serialVersionUID = 1L;

	
	public static final boolean OUTPUT_LOG = false;
    public static String NEW_LINE_SUBSTITUTE_STRING = "\\n";
    public static String TAB_SUBSTITUTE_STRING = "\\t";
    public static String NO_BASENAME_STRING = "[no basename found]";
  
    public static final int COPY_BASENAMES = 100;
    public static final int COPY_SIS = 101;
    
    public static final int INCLUDE_NOTHING = 1000;
    public static final int INCLUDE_NAMES = 1001;
    public static final int INCLUDE_NAMES_AND_SCOPES = 1901;
    public static final int INCLUDE_SLS = 1002;
    public static final int INCLUDE_SIS = 1003;
    public static final int INCLUDE_CLASSES = 1004;
    public static final int INCLUDE_INSTANCES = 1005;
    public static final int INCLUDE_PLAYERS = 1006;
    public static final int INCLUDE_PLAYED_ROLES = 1904;
    public static final int INCLUDE_OCCURRENCES = 1007;
    public static final int INCLUDE_ALL_OCCURRENCES = 1008;
    public static final int INCLUDE_OCCURRENCE_TYPES = 1009;
    public static final int INCLUDE_ASSOCIATION_TYPES = 1010;
    
    public static final int INCLUDE_SI_COUNT = 1103;
    public static final int INCLUDE_CLASS_COUNT = 1104;
    public static final int INCLUDE_INSTANCE_COUNT = 1105;
    public static final int INCLUDE_ASSOCIATION_COUNT = 1106;
    public static final int INCLUDE_TYPED_ASSOCIATION_COUNT = 1107;
    public static final int INCLUDE_OCCURRENCE_COUNT = 1108;
    
    public static final int INCLUDE_LAYER_DISTRIBUTION = 1200;
    public static final int INCLUDE_CLUSTER_COEFFICIENT = 1400;
    
    public int copyOrders = COPY_BASENAMES;
    public int includeOrders = 0;
   
    
    Topic associationType = null;
    Topic role = null;
    Topic occurrenceType = null;
    List<Topic> allOccurrenceTypes = null;
    Set<Topic> scopeMemory = null;
    Wandora wandora = null;
    Iterator topics = null;
    
    
    public CopyTopics() throws TopicMapException {
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
    
    
    protected void initialize(int copyOrders, int includeOrders) {
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
        StringBuilder sb = new StringBuilder("Copy selected topics to the clipboard.");
        switch(includeOrders) {
            case INCLUDE_SLS: sb.append(" Include subject locators."); break;
            case INCLUDE_SIS: sb.append(" Include subject indentifiers."); break;
            case INCLUDE_NAMES: sb.append(" Include variant names."); break;
            case INCLUDE_NAMES_AND_SCOPES: sb.append(" Include variant names and name scopes."); break;
            case INCLUDE_INSTANCES: sb.append(" Include instance topics."); break;
            case INCLUDE_CLASSES: sb.append(" Include class topics."); break;
            case INCLUDE_PLAYERS: sb.append(" Include player topics."); break;
            case INCLUDE_PLAYED_ROLES: sb.append(" Include played roles."); break;
            case INCLUDE_OCCURRENCES: sb.append(" Include occurrences."); break;
            case INCLUDE_OCCURRENCE_TYPES: sb.append(" Include occurrence types."); break;
            case INCLUDE_ALL_OCCURRENCES: sb.append(" Include all occurrences."); break;
            case INCLUDE_ASSOCIATION_TYPES: sb.append(" Include association types."); break;
            case INCLUDE_SI_COUNT: sb.append(" Include subject identifier count."); break;
            case INCLUDE_CLASS_COUNT: sb.append(" Include class count."); break;
            case INCLUDE_INSTANCE_COUNT: sb.append(" Include instance count."); break;
            case INCLUDE_ASSOCIATION_COUNT: sb.append(" Include association count."); break;
            case INCLUDE_TYPED_ASSOCIATION_COUNT: sb.append(" Include association count of specific type."); break;
            case INCLUDE_OCCURRENCE_COUNT: sb.append(" Include occurrence count."); break;
            case INCLUDE_LAYER_DISTRIBUTION: sb.append(" Include topic's distribution over topic map layers."); break;
            case INCLUDE_CLUSTER_COEFFICIENT: sb.append(" Include topic's clustering coefficient."); break;
        };
        return sb.toString();
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
        List<Topic> topicList = new ArrayList<>();
        if(topicArray != null) {
            for(int i=0; i<topicArray.length; i++) {
                topicList.add(topicArray[i]);
            }
        }
        topics = topicList.iterator();
    }
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        this.wandora = wandora;
        try {
            if(wandora != null) {
                scopeMemory = new LinkedHashSet<>();
                associationType = null;
                role = null;
                if(includeOrders == INCLUDE_TYPED_ASSOCIATION_COUNT) {
                    associationType=wandora.showTopicFinder("Select association type...");                
                    if(associationType == null) return;
                }
                
                if(includeOrders == INCLUDE_PLAYERS) {
                    associationType=wandora.showTopicFinder("Select association type...");                
                    if(associationType == null) return;
                    role=wandora.showTopicFinder("Select role...");                
                    if(role == null) return;
                }

                occurrenceType = null;
                if(includeOrders == INCLUDE_OCCURRENCES) {
                    occurrenceType=wandora.showTopicFinder("Select occurrence type...");                
                    if(occurrenceType == null) return;
                }
                
                allOccurrenceTypes = new ArrayList<Topic>();
                if(includeOrders == INCLUDE_ALL_OCCURRENCES) {
                    allOccurrenceTypes = getOccurrenceTypes(wandora.getTopicMap());
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
            List<String> lines = new ArrayList<>();
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
                        if(OUTPUT_LOG) {
                            hlog("Copying from '" + topicName + "'.");
                        }
                        switch(copyOrders) {
                            case COPY_BASENAMES: {
                                if(topic.getBaseName() != null) {
                                    sb.append(topic.getBaseName());
                                }
                                else {
                                    sb.append(NO_BASENAME_STRING);
                                }
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
                                Collection<Topic> types = topic.getTypes();
                                for(Topic type : types) {
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
                                Collection<Topic> instances = topic.getTopicMap().getTopicsOfType(topic);
                                for(Topic instance : instances) {
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
                                Set<Set<Topic>> scopes = topic.getVariantScopes();
                                if(scopes != null && !scopes.isEmpty()) {
                                    for(Set<Topic> scope : scopes) {
                                        if(scope != null && !scope.isEmpty()) {
                                            sb.append("\t").append(topic.getVariant(scope));
                                        }
                                    }
                                }
                                break;
                            }
                            
                            case INCLUDE_NAMES_AND_SCOPES: {
                                Set<Set<Topic>> scopes = topic.getVariantScopes();
                                if(scopes != null && !scopes.isEmpty()) {
                                    for(Set<Topic> scope : scopes) {
                                        if(scope != null && !scope.isEmpty()) {
                                            sb.append("\t").append(topic.getVariant(scope));
                                            for(Topic scopeTopic : scope) {
                                                sb.append("\t").append(TopicToString.toString(scopeTopic));
                                            }
                                        }
                                        sb.append("\n");
                                    }
                                    sb.deleteCharAt(sb.length()-1);
                                }
                                break;
                            }
                            
                            case INCLUDE_OCCURRENCES: {
                                Hashtable<Topic,String> occurrence = topic.getData(occurrenceType);
                                scopeMemory.addAll(occurrence.keySet());
                                for(Topic scope : scopeMemory) {
                                    String occurrenceStr = occurrence.get(scope);
                                    sb.append("\t");
                                    if(occurrenceStr != null && occurrenceStr.length() > 0) {
                                        occurrenceStr = occurrenceStr.replace("\t", TAB_SUBSTITUTE_STRING);
                                        occurrenceStr = occurrenceStr.replace("\n", NEW_LINE_SUBSTITUTE_STRING);
                                        sb.append(occurrenceStr);
                                    }
                                }
                                if(OUTPUT_LOG) {
                                    log("Replaced all tab characters with '"+TAB_SUBSTITUTE_STRING+"'.");
                                    log("Replaced all new line characters with '"+NEW_LINE_SUBSTITUTE_STRING+"'.");
                                }
                                break;
                            }
                                
                            case INCLUDE_ALL_OCCURRENCES: {
                                if(allOccurrenceTypes != null && !allOccurrenceTypes.isEmpty()) {
                                    for(Topic occurrenceType : allOccurrenceTypes) {
                                        if(occurrenceType != null && !occurrenceType.isRemoved()) {
                                            Hashtable<Topic,String> occurrence = topic.getData(occurrenceType);
                                            if(occurrence != null && !occurrence.keySet().isEmpty()) {
                                                scopeMemory.addAll(occurrence.keySet());
                                                sb.append("\t");
                                                sb.append(TopicToString.toString(occurrenceType));
                                                for(Topic scope : scopeMemory) {
                                                    sb.append("\t");
                                                    String occurrenceStr = occurrence.get(scope);
                                                    if(occurrenceStr != null && occurrenceStr.length() > 0) {
                                                        occurrenceStr = occurrenceStr.replace("\t", TAB_SUBSTITUTE_STRING);
                                                        occurrenceStr = occurrenceStr.replace("\n", NEW_LINE_SUBSTITUTE_STRING);
                                                        sb.append(occurrenceStr);
                                                    }
                                                }
                                                sb.append("\n");
                                            }
                                        }
                                    }
                                    // Trim last new line character.
                                    sb.deleteCharAt(sb.length()-1);
                                    if(OUTPUT_LOG) {
                                        log("Replaced all tab characters with '"+TAB_SUBSTITUTE_STRING+"'.");
                                        log("Replaced all new line characters with '"+NEW_LINE_SUBSTITUTE_STRING+"'.");
                                    }
                                }
                                break;
                            }
                                
                            case INCLUDE_OCCURRENCE_TYPES: {
                                ArrayList<Topic> occurrenceTypes = new ArrayList(topic.getDataTypes());
                                Collections.sort(occurrenceTypes, new TMBox.TopicBNAndSIComparator());
                                for(Topic t : occurrenceTypes) {
                                    sb.append("\t").append(TopicToString.toString(t));
                                }
                                break;
                            }
                            
                            case INCLUDE_OCCURRENCE_COUNT: {
                                Collection<Topic> occurrenceTypes = topic.getDataTypes();
                                int occurrenceCount = 0;
                                for(Topic occurrenceType : occurrenceTypes) {
                                    Hashtable<Topic,String> scopedOccurrences = topic.getData(occurrenceType);
                                    occurrenceCount = occurrenceCount + scopedOccurrences.size();
                                }
                                sb.append("\t");
                                sb.append(occurrenceCount);
                                break;
                            }
                            
                            case INCLUDE_ASSOCIATION_TYPES: {
                                Collection<Association> associations = topic.getAssociations();
                                Set<String> associationTypes = new LinkedHashSet<>();
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
                                    Collection<Association> associations = topic.getAssociations(associationType);
                                    for(Association association : associations) {
                                        if(association != null) {
                                            Topic player = association.getPlayer(role);
                                            if(player != null) {
                                                sb.append("\t").append(TopicToString.toString(player));
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            
                            case INCLUDE_PLAYED_ROLES: {
                                Collection<Association> associations = topic.getAssociations();
                                Set<Topic> usedRoles = new HashSet<>();
                                for(Association association : associations) {
                                    if(association != null) {
                                        Collection<Topic> roles = association.getRoles();
                                        for(Topic role : roles) {
                                            Topic player = association.getPlayer(role);
                                            if(player != null) {
                                                if(player.mergesWithTopic(topic)) {
                                                    if(!usedRoles.contains(role)) {
                                                        usedRoles.add(role);
                                                        sb.append("\t").append(TopicToString.toString(role));
                                                    }
                                                }
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
                                    String s = makeDistributionVector((LayeredTopic)topic, wandora.getTopicMap());
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
            Collections.sort(lines);
            ClipboardBox.setClipboard(stringSerialize(lines));
            if(OUTPUT_LOG) log("Total " + count + " topics copied.");
            if(OUTPUT_LOG) log("Ready.");
            if(OUTPUT_LOG) setState(WAIT);
            
            topics = null;
        }
        catch(Exception e) {
            log("Copying "+ getTopicTypeName() +" to clipboard failed!", e);
        }
    }
    
    
    
    
    private String stringSerialize(List<String> lines) {
        StringBuilder sb = new StringBuilder("");
        if(lines != null) {
	        for(String line : lines) {
	            sb.append(line);
	            sb.append("\n");
	        }
        }
        return sb.toString();
    };
    
    
    
    private String makeDistributionVector(LayeredTopic topic,ContainerTopicMap tm) throws TopicMapException {
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
    
    
    
    private String getDisplayName(Topic t, String lang)  throws TopicMapException {
        String langsi=XTMPSI.getLang(lang);
        Topic langT =t.getTopicMap().getTopic(langsi);
        String dispsi=XTMPSI.DISPLAY;
        Topic dispT=t.getTopicMap().getTopic(dispsi);
        Set<Topic> scope=new LinkedHashSet<>();
        if(langT!=null) scope.add(langT);
        if(dispT!=null) scope.add(dispT);
        String variantName = t.getVariant(scope);
        return (variantName == null ? "" : variantName);
    }
    
    
    private String getTextData(Topic t, Topic type, String versions)  throws TopicMapException {
        String langsi=XTMPSI.getLang(versions);
        Topic version=t.getTopicMap().getTopic(langsi);
        String textData = t.getData(type, version);
        return (textData == null ? "" : textData);
    }
    
    
    

    private List<Topic> getOccurrenceTypes(TopicMap tm) throws TopicMapException {
        Set<Topic> occurrenceTypes = new LinkedHashSet<>();
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
        List<Topic> ot = new ArrayList<>();
        ot.addAll(occurrenceTypes);
        return ot;
    }
    
    
}
