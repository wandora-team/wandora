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
 */


package org.wandora.topicmap.similarity;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;


/**
 *
 * @author akivela
 */



public class AssociationStringSimilarity implements TopicSimilarity {

    public static final String TOPIC_DELIMITER = "***"; 
    private InterfaceStringMetric stringMetric = null;
    
    
    public AssociationStringSimilarity() {
        stringMetric = new Levenshtein();
    }

    @Override
    public String getName() {
        return "Association string similarity";
    }
    
    @Override
    public double similarity(Topic t1, Topic t2) {
        double similarity = 0;
        double overAllSimilarity = 1;
        double bestSimilarity = 0;
        
        try {
            Collection<String> as1 = getAssociationsAsStrings(t1);
            Collection<String> as2 = getAssociationsAsStrings(t2);
            if(as1.isEmpty() || as2.isEmpty()) return 0;
            
            for(String s1 : as1) {
                bestSimilarity = 0;
                for(String s2 : as2) {
                    similarity = stringMetric.getSimilarity(s1, s2);
                    if(similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                    }
                }
                overAllSimilarity = overAllSimilarity * bestSimilarity;
                //overAllSimilarity = overAllSimilarity / 2;
            }
        }
        catch(Exception e) {}
        return overAllSimilarity;
    }
    
    
    
    
    public Collection<String> getAssociationsAsStrings(Topic t) throws TopicMapException {
        Collection<Association> as = t.getAssociations();
        ArrayList<String> asStr = new ArrayList<>();
        
        for(Association a : as) {
            StringBuilder sb = new StringBuilder("");
            sb.append(getAsString(a.getType()));
            sb.append(TOPIC_DELIMITER);
            Collection<Topic> roles = a.getRoles();
            ArrayList<Topic> sortedRoles = new ArrayList<>();
            sortedRoles.addAll(roles);
            Collections.sort(sortedRoles, new TopicStringComparator());
            
            boolean found = false;
            for(Topic r : sortedRoles) {
                Topic p = a.getPlayer(r);
                if(!found && p.mergesWithTopic(t)) {
                    found = true;
                    continue;
                }
                sb.append(getAsString(r));
                sb.append(TOPIC_DELIMITER);
                sb.append(getAsString(p));
                sb.append(TOPIC_DELIMITER);
            }
            asStr.add(sb.toString());
        }
        Collections.sort(asStr);
        
        return asStr;
    }
    
    
    
    
    
    public String getAsString(Topic t) throws TopicMapException {
        if(t == null) return null;
        else {
            return t.getOneSubjectIdentifier().toExternalForm();
        }
    }
    
    
    
    private class TopicStringComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            try {
                if(o1 != null && o2 != null) {
                    if(o1 instanceof Topic && o2 instanceof Topic) {
                        Topic t1 = (Topic) o1;
                        Topic t2 = (Topic) o2;
                        String s1 = getAsString(t1);
                        String s2 = getAsString(t2);
                        return s1.compareTo(s2);
                    }
                    else if(o1 instanceof Comparable && o2 instanceof Comparable) {
                        Comparable c1 = (Comparable) o1;
                        Comparable c2 = (Comparable) o2;
                        return c1.compareTo(c2);
                    }
                }
            }
            catch(Exception e) {
                // EXCEPTION
            }
            return 0;
        }
        
        
    }
    
    
}