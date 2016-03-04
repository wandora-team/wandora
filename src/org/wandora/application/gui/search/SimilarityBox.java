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
 */

package org.wandora.application.gui.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapSearchOptions;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;


/**
 *
 * @author akivela
 */


public class SimilarityBox {
    
    
    
    
    public static Collection<Topic> getSimilarTopics(String query, TopicMapSearchOptions options, Iterator<Topic> topicIterator, InterfaceStringMetric stringMetric, float threshold, boolean differenceInsteadOfSimilarity, boolean useNGrams) {
        ArrayList<Topic> selection = new ArrayList<Topic>();
        int count = 0;
        Topic t = null;
        boolean isSimilar = false;
        float similarity = 0.0f;
        try {
            while(topicIterator.hasNext()) {
                similarity = 0.0f;
                isSimilar = false;
                t = topicIterator.next();
                if(t != null && !t.isRemoved()) {
                    if(options.searchBasenames) {
                        String n = t.getBaseName();
                        if(n != null && n.length() > 0) {
                            similarity = getSimilarity(query, n, stringMetric, useNGrams);
                            isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                        }
                    }
                    if(!isSimilar && options.searchVariants) {
                        String n = null;
                        Set<Set<Topic>> scopes = t.getVariantScopes();
                        Iterator<Set<Topic>> scopeIterator = scopes.iterator();
                        Set<Topic> scope = null;
                        while(!isSimilar && scopeIterator.hasNext()) {
                            scope = scopeIterator.next();
                            if(scope != null) {
                                n = t.getVariant(scope);
                                if(n != null && n.length() > 0) {
                                    similarity = getSimilarity(query, n, stringMetric, useNGrams);
                                    isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                                }
                            }
                        }
                    }
                    if(!isSimilar && options.searchOccurrences) {
                        String o = null;
                        Collection<Topic> types = t.getDataTypes();
                        Iterator<Topic> typeIterator = types.iterator();
                        Topic type = null;
                        Hashtable<Topic, String> os = null;
                        Enumeration<Topic> osEnumeration = null;
                        Topic osTopic = null;
                        while(!isSimilar && typeIterator.hasNext()) {
                            type = typeIterator.next();
                            if(type != null && !type.isRemoved()) {
                                os = t.getData(type);
                                osEnumeration = os.keys();
                                while(osEnumeration.hasMoreElements() && !isSimilar) {
                                    osTopic = osEnumeration.nextElement();
                                    o = os.get(osTopic);
                                    if(o != null && o.length() > 0) {
                                        similarity = getSimilarity(query, o, stringMetric, useNGrams);
                                        isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                                    }
                                }
                            }
                        }
                    }
                    if(!isSimilar && options.searchSIs) {
                        Collection<Locator> locs = t.getSubjectIdentifiers();
                        if(locs != null && locs.size() > 0) {
                            Iterator<Locator> locIter = locs.iterator();
                            Locator loc = null;
                            String l = null;
                            while(locIter.hasNext() && !isSimilar) {
                                loc = locIter.next();
                                if(loc != null) {
                                    l = loc.toExternalForm();
                                    similarity = getSimilarity(query, l, stringMetric, useNGrams);
                                    isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                                }
                            }
                        }
                    }
                    if(!isSimilar && options.searchSL) {
                        Locator loc = t.getSubjectLocator();
                        if(loc != null) {
                            String l = loc.toExternalForm();
                            similarity = getSimilarity(query, l, stringMetric, useNGrams);
                            isSimilar = isSimilar(similarity, threshold, differenceInsteadOfSimilarity);
                        }
                    }
                    // ***** IF TOPIC IS SIMILAR ****
                    if(isSimilar) {
                        selection.add(t);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return selection;
    }
    
    

    
    public static float getSimilarity(String s1, String s2, InterfaceStringMetric stringMetric, boolean useNGrams) {
        float similarity = 0.0f;
        if(s1 != null && s2 != null && stringMetric != null) {
            if(useNGrams) {
                int l1 = s1.length();
                int l2 = s2.length();
                if(l2 > l1) {
                    for(int i=0; i<l2-l1; i++) {
                        float s = stringMetric.getSimilarity(s1, s2.substring(i, i+l1));
                        if(s > similarity) {
                            similarity = s;
                        }
                    }
                }
                else {
                    similarity = stringMetric.getSimilarity(s1, s2);
                }
            }
            else {
                similarity = stringMetric.getSimilarity(s1, s2);
            }
        }
        return similarity;
    }
    
    
    
    public static boolean isSimilar(float similarity, float threshold, boolean differenceInsteadOfSimilarity) {
        if(differenceInsteadOfSimilarity) {
            return (similarity < threshold);
        }
        else {
            return (similarity > threshold);
        }
    }
    
}
