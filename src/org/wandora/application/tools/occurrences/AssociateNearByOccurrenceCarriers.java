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


public class AssociateNearByOccurrenceCarriers extends AbstractWandoraTool implements  WandoraTool {
    private boolean requiresRefresh = false;
    private Context preferredContext = null;

    public static final String NEARBY_TYPE = "http://wandora.org/si/nearby-points/";
    public static final String POINT_A_TYPE = "http://wandora.org/si/nearby-points/point-1";
    public static final String POINT_B_TYPE = "http://wandora.org/si/nearby-points/point-2";
    

    public AssociateNearByOccurrenceCarriers() {
        
    }
    public AssociateNearByOccurrenceCarriers(Context context) {
        this.preferredContext = context;
    }


    @Override
    public String getName() {
        return "Associate near by occurrence carriers";
    }
    @Override
    public String getDescription() {
        return "Associate near by occurrence carriers";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    


    @Override
    public void execute(Wandora admin, Context context) {
        requiresRefresh = false;

        // This tool doesn't use context!
        //Iterator topics = null;
        //if(preferredContext != null) topics = preferredContext.getContextObjects();
        //else topics = context.getContextObjects();

        try {
            int pointAcount = 0;
            int pointBcount = 0;
            int acount = 0;
            TopicMap tm = admin.getTopicMap();

            GenericOptionsDialog god=new GenericOptionsDialog(admin,
                "Find nearby point occurrences and associate occurrence carriers.",
                "Find nearby point occurrences and associate occurrence carriers.",true,new String[][]{
                new String[]{"Occurrence type (A) of points","topic","","Type of occurrences that contain points"},
                new String[]{"Occurrence type (B) of points","topic","","Type of occurrences that contain points"},
                new String[]{"Scope of occurrences","topic","","Scope i.e. language of changed occurrences"},
                new String[]{"Maximum distance (km)","String","","Allowed distance between points in kilometers"},
                new String[]{"Reverse point (B) coordinates","boolean","","Swap latitude with longitude"},
            },admin);
            god.setVisible(true);
            if(god.wasCancelled()) return;

            Map<String,String> values=god.getValues();

            Topic pointAType = tm.getTopic(values.get("Occurrence type (A) of points"));
            Topic pointBType = tm.getTopic(values.get("Occurrence type (B) of points"));
            Topic scopeTopic = tm.getTopic(values.get("Scope of occurrences"));
            String maxDistanceStr = values.get("Maximum distance (km)");
            double maxDistance = 1;
            try {
                maxDistance = Double.parseDouble(maxDistanceStr);
            }
            catch(Exception e) {
                log("Invalid maximum distance. Using default maximum distance "+maxDistance);
                e.printStackTrace();
            }
            boolean reversePointCoordinates = "true".equalsIgnoreCase(values.get("Reverse point (B) coordinates"));

            setDefaultLogger();
            setLogTitle("Finding nearby point occurrences...");

            Iterator<Topic> allTopics = tm.getTopics();
            
            HashMap<Topic,GeoLocation> pointAOccurrences = new HashMap();
            HashMap<Topic,GeoLocation> pointBOccurrences = new HashMap();

            try {
                log("Looking for point occurrences...");
                while(allTopics.hasNext()) {
                    Topic topic = allTopics.next();
                    if(topic != null && !topic.isRemoved()) {
                        if(pointAType != null) {
                            Hashtable<Topic,String> scopedOccurrences = topic.getData(pointAType);
                            if(scopedOccurrences != null && scopedOccurrences.size() > 0) {
                                if(scopeTopic != null) {
                                    String occurrence = scopedOccurrences.get(scopeTopic);
                                    if(occurrence != null) {
                                        GeoLocation location = parseGeoLocation(occurrence);
                                        if(location != null) {
                                            pointAcount++;
                                            pointAOccurrences.put(topic, location);
                                        }
                                    }
                                }
                                else {
                                    Enumeration<Topic> scopeTopics = scopedOccurrences.keys();
                                    while(scopeTopics.hasMoreElements()) {
                                        Topic oscopeTopic = scopeTopics.nextElement();
                                        String occurrence = scopedOccurrences.get(oscopeTopic);
                                        if(occurrence != null && occurrence.length() > 0) {
                                            GeoLocation location = parseGeoLocation(occurrence);
                                            if(location != null) {
                                                pointAcount++;
                                                pointAOccurrences.put(topic, location);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if(pointBType != null) {
                            Hashtable<Topic,String> scopedOccurrences = topic.getData(pointBType);
                            if(scopedOccurrences != null && scopedOccurrences.size() > 0) {
                                if(scopeTopic != null) {
                                    String occurrence = scopedOccurrences.get(scopeTopic);
                                    if(occurrence != null) {
                                        GeoLocation location = parseGeoLocation(occurrence, reversePointCoordinates);
                                        if(location != null) {
                                            pointBcount++;
                                            pointBOccurrences.put(topic, location);
                                        }
                                    }
                                }
                                else {
                                    Enumeration<Topic> scopeTopics = scopedOccurrences.keys();
                                    while(scopeTopics.hasMoreElements()) {
                                        Topic oscopeTopic = scopeTopics.nextElement();
                                        String occurrence = scopedOccurrences.get(oscopeTopic);
                                        if(occurrence != null && occurrence.length() > 0) {
                                            GeoLocation location = parseGeoLocation(occurrence, reversePointCoordinates);
                                            if(location != null) {
                                                pointBcount++;
                                                pointBOccurrences.put(topic, location);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                log("Total " + pointAcount + " point (A) occurrences found!");
                log("Total " + pointBcount + " point (B) occurrences found!");
                log("Looking for nearby points...");
                
                int progress = 0;
                setProgress(progress);
                setProgressMax(pointAcount);
                for(Topic pointAKey : pointAOccurrences.keySet()) {
                    setProgress(progress++);
                    if(forceStop()) break;
                    GeoLocation pointA = pointAOccurrences.get(pointAKey);
                    for(Topic pointBKey : pointBOccurrences.keySet()) {
                        if(forceStop()) break;
                        GeoLocation pointB = pointBOccurrences.get(pointBKey);
                        if(pointB != null) {
                            double distance = pointB.distanceInKM(pointA);
                            if(distance != -1 && distance < maxDistance) {
                                associate(pointAKey, pointBKey, tm);
                                acount++;
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                log(e);
            }
            if(acount > 0) {
                requiresRefresh = true;
                log("Total " + acount + " nearby associations created!");
            }
            else {
                requiresRefresh = false;
                log("No nearby associations created!");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }



    
    
    private GeoLocation parseGeoLocation(String p) {
        return parseGeoLocation(p, false);
    }
    
    private GeoLocation parseGeoLocation(String p, boolean rev) {
        if(p != null) {
            String[] ps = p.split("[,; ]");
            if(ps.length >= 2) {
                Double d1 = Double.parseDouble(ps[0]);
                Double d2 = Double.parseDouble(ps[1]);
                if(rev) {
                    Double temp = d1;
                    d1 = d2;
                    d2 = temp;
                }
                GeoLocation l = new GeoLocation(d1, d2);
                return l;
            }
        }
        return null;
    }
    
    
    
    private void associate(Topic pointTopic, Topic polygonTopic, TopicMap tm) throws TopicMapException {
        if(tm == null || pointTopic == null || polygonTopic == null) return;
        
        Topic associationType = ExtractHelper.getOrCreateTopic(NEARBY_TYPE, "Nearby points", tm);
        Topic pointRole = ExtractHelper.getOrCreateTopic(POINT_A_TYPE, "Point-1", tm);
        Topic polygonRole = ExtractHelper.getOrCreateTopic(POINT_B_TYPE, "Point-2", tm);
        
        if(associationType != null && pointRole != null && polygonRole != null) {
            Association a = tm.createAssociation(associationType);
            a.addPlayer(pointTopic, pointRole);
            a.addPlayer(polygonTopic, polygonRole);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private class GeoLocation {
        
        double lat = 0;
        double lon = 0;
        
        public GeoLocation(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
        
        

        
        private double d2r = Math.PI / 180;
        public double distanceInKM(GeoLocation o) {
            if(o == null) return -1;
            
            double dlong = (o.lon - lon) * d2r;
            double dlat = (o.lat - lat) * d2r;
            double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(lat*d2r) * Math.cos(o.lat*d2r) * Math.pow(Math.sin(dlong/2.0), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double d = 6367 * c;

            return d;
        }
    }
    
}
