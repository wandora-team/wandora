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
 *
 */

package org.wandora.application.tools.occurrences;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class FindPointInPolygonOccurrence extends AbstractWandoraTool implements  WandoraTool {
    private boolean requiresRefresh = false;
    private Context preferredContext = null;

    public static final String INCLUSION_TYPE = "http://wandora.org/si/find-point-in-polygon/inclusion";
    public static final String POINT_TYPE = "http://wandora.org/si/find-point-in-polygon/point";
    public static final String POLYGON_TYPE = "http://wandora.org/si/find-point-in-polygon/polygon";
    
    private boolean reversePointCoordinates = false;
    

    public FindPointInPolygonOccurrence() {
        
    }
    public FindPointInPolygonOccurrence(Context context) {
        this.preferredContext = context;
    }


    @Override
    public String getName() {
        return "Find point in polygon occurrences";
    }
    @Override
    public String getDescription() {
        return "Find point occurrences in polygon occurrences and if inclusion is "+
               "detected associate point occurrence carrier topic with the polygon "+
               "occurrence carrier topic. Tool can be used to associate geo location "+
               "topics, for example.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    


    public void execute(Wandora admin, Context context) {
        requiresRefresh = false;
        Iterator topics = null;
        if(preferredContext != null) topics = preferredContext.getContextObjects();
        else topics = context.getContextObjects();

        try {
            int pointcount = 0;
            int polygoncount = 0;
            int acount = 0;
            TopicMap tm = admin.getTopicMap();

            GenericOptionsDialog god=new GenericOptionsDialog(admin,
                "Find point occurrences in polygon occurrence and associate",
                "Select point and polygon occurrence types.",true,new String[][]{
                new String[]{"Type of point carrier topics","topic","","Type of topics carrying the point occurrence"},
                new String[]{"Type of polygon carrier topics","topic","","Type of topics carrying the polygon occurrence"},
                new String[]{"Occurrence type of points","topic","","Type of occurrences that contain points"},
                new String[]{"Occurrence type of polygons","topic","","Type of occurrences that contain polygons"},
                new String[]{"Scope of occurrences","topic","","Scope i.e. language of changed occurrences"},
                new String[]{"Reverse point coordinates","boolean","","Swap latitude with longitude"},
            },admin);
            god.setVisible(true);
            if(god.wasCancelled()) return;

            Map<String,String> values=god.getValues();

            Topic pointType = tm.getTopic(values.get("Type of point carrier topics"));
            Topic polygonType = tm.getTopic(values.get("Type of polygon carrier topics"));
            Topic pointOType = tm.getTopic(values.get("Occurrence type of points"));
            Topic polygonOType = tm.getTopic(values.get("Occurrence type of polygons"));
            Topic scopeTopic = tm.getTopic(values.get("Scope of occurrences"));
            reversePointCoordinates = "true".equalsIgnoreCase(values.get("Reverse point coordinates"));

            setDefaultLogger();
            setLogTitle("Finding point in polygons occurrences...");

            Collection<Topic> pointTopics = new ArrayList();
            if(pointType == null) {
                while(topics.hasNext() && !forceStop()) {
                    pointTopics.add((Topic) topics.next());
                }
            }
            else {
                pointTopics = tm.getTopicsOfType(pointType);
            }

            Collection<Topic> polygonTopics = new ArrayList();
            if(polygonType == null) {
                while(topics.hasNext() && !forceStop()) {
                    polygonTopics.add((Topic) topics.next());
                }
            }
            else {
                polygonTopics = tm.getTopicsOfType(polygonType);
            }

            HashMap<Topic,String> pointOccurrences = new HashMap();
            HashMap<Topic,String> polygonOccurrences = new HashMap();

            try {
                
                log("Looking for point occurrences...");
                for(Topic topic : pointTopics) {
                    if(topic != null && !topic.isRemoved()) {
                        if(pointOType != null) {
                            Hashtable<Topic,String> scopedOccurrences = topic.getData(pointOType);
                            if(scopedOccurrences != null && scopedOccurrences.size() > 0) {
                                if(scopeTopic != null) {
                                    String occurrence = scopedOccurrences.get(scopeTopic);
                                    if(occurrence != null) {
                                        pointcount++;
                                        pointOccurrences.put(topic, occurrence);
                                    }
                                }
                                else {
                                    Enumeration<Topic> scopeTopics = scopedOccurrences.keys();
                                    while(scopeTopics.hasMoreElements()) {
                                        Topic oscopeTopic = scopeTopics.nextElement();
                                        String occurrence = scopedOccurrences.get(oscopeTopic);
                                        if(occurrence != null && occurrence.length() > 0) {
                                            pointcount++;
                                            pointOccurrences.put(topic, occurrence);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                log("Total " + pointcount + " point occurrences found!");
                log("Looking for polygon occurrences...");
                for(Topic topic : polygonTopics) {
                    if(topic != null && !topic.isRemoved()) {
                        if(polygonOType != null) {
                            Hashtable<Topic,String> scopedOccurrences = topic.getData(polygonOType);
                            if(scopedOccurrences != null && scopedOccurrences.size() > 0) {
                                if(scopeTopic != null) {
                                    String occurrence = scopedOccurrences.get(scopeTopic);
                                    if(occurrence != null) {
                                        polygoncount++;
                                        polygonOccurrences.put(topic, occurrence);
                                    }
                                }
                                else {
                                    Enumeration<Topic> scopeTopics = scopedOccurrences.keys();
                                    while(scopeTopics.hasMoreElements()) {
                                        Topic oscopeTopic = scopeTopics.nextElement();
                                        String occurrence = scopedOccurrences.get(oscopeTopic);
                                        if(occurrence != null && occurrence.length() > 0) {
                                            polygoncount++;
                                            polygonOccurrences.put(topic, occurrence);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                log("Total " + polygoncount + " polygon occurrences found!");
                log("Looking for points in polygons...");
                
                int progress = 0;
                setProgress(progress);
                setProgressMax(pointOccurrences.size());
                for(Topic pointKey : pointOccurrences.keySet()) {
                    setProgress(progress++);
                    if(forceStop()) break;
                    String point = pointOccurrences.get(pointKey);
                    for(Topic polygonKey : polygonOccurrences.keySet()) {
                        if(forceStop()) break;
                        String polygon = polygonOccurrences.get(polygonKey);
                        if(isInside(point, polygon)) {
                            associate(pointKey, polygonKey, tm);
                            acount++;
                        }
                    }
                }
            }
            catch (Exception e) {
                log(e);
            }
            if(acount > 0) requiresRefresh = true;
            log("Total " + acount + " associations created!");
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }



    
    
    private boolean isInside(String point, String polygon) {
        Point p = parsePoint(point);
        Polygon pol = parsePolygon(polygon);
        if(p == null || pol == null) return false;
        return pol.contains(p);
    }

    
    private Point parsePoint(String p) {
        String[] ps = p.split("[,; ]");
        if(ps.length >= 2) {
            Double d1 = Double.parseDouble(ps[0]);
            Double d2 = Double.parseDouble(ps[1]);
            if(reversePointCoordinates) {
                Double temp = d1;
                d1 = d2;
                d2 = temp;
            }
            Point point = new Point(getAsInt(d1), getAsInt(d2));
            return point;
        }
        return null;
    }
    
    
    private Polygon parsePolygon(String p) {
        String[] ps = p.split("[,; ]");
        if(ps.length >= 2) {
            Polygon polygon = new Polygon();
            for(int i=0; i+1<ps.length; i=i+2) {
                Double d1 = Double.parseDouble(ps[i]);
                Double d2 = Double.parseDouble(ps[i+1]);
                polygon.addPoint(getAsInt(d1), getAsInt(d2));
            }
            return polygon;
        }
        return null;
    }
    
    
    private int getAsInt(Double d) {
        return (int) Math.round(d * 1000000);
    }
    
    
    private void associate(Topic pointTopic, Topic polygonTopic, TopicMap tm) throws TopicMapException {
        if(tm == null || pointTopic == null || polygonTopic == null) return;
        
        Topic associationType = ExtractHelper.getOrCreateTopic(INCLUSION_TYPE, "Point in polygon", tm);
        Topic pointRole = ExtractHelper.getOrCreateTopic(POINT_TYPE, "Point", tm);
        Topic polygonRole = ExtractHelper.getOrCreateTopic(POLYGON_TYPE, "Polygon", tm);
        
        if(associationType != null && pointRole != null && polygonRole != null) {
            Association a = tm.createAssociation(associationType);
            a.addPlayer(pointTopic, pointRole);
            a.addPlayer(polygonTopic, polygonRole);
        }
    }

    
    
    
    // -------------------------------------------------------------------------
    

    
}