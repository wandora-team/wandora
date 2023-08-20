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
 * 
 *
 * GenericVelocityHelper.java
 *
 * Created on 17. joulukuuta 2004, 10:54
 */

package org.wandora.utils.velocity;



import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapSearchOptions;
import org.wandora.utils.IObox;
import org.wandora.utils.Textbox;


/**
 *
 * @author  olli, akivela
 */
public class GenericVelocityHelper {
    
    /** Creates a new instance of GenericVelocityHelper */
    public GenericVelocityHelper() {
    }
    

    public static Hashtable getBinaryAssociations(Topic topic) {
        return null;
    }
    

    
    public static Topic getFirstPlayerWithRole(Topic topic, String roleSI) {
        try {
            ArrayList<Topic> players = getPlayersWithRole(topic, roleSI);
            if(players.size() > 0) return players.get(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    public static ArrayList<Topic> getPlayersWithRole(Topic topic, String roleSI) throws TopicMapException  {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null && roleSI != null) {
            Topic role = topic.getTopicMap().getTopic(roleSI);

            if(role != null) {
                Collection<Association> associations = topic.getAssociations();
                for(Association a : associations ) {
                    try {
                        if(a.getPlayer(role) != null) players.add(a.getPlayer(role));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return players;
    }
    
    
    public static ArrayList<Topic> getPlayersWithRole(Collection<Topic> topics, String roleSI) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        
        for(Topic topic : topics){
            try {
                players.addAll(getPlayersWithRole(topic, roleSI));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    
    public static ArrayList<Topic> getPlayersWithRoles(Topic topic, String[] roleSIs) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null) {
            String roleSI = null;
            for(int i=0; i<roleSIs.length; i++) {
                try {
                    roleSI = roleSIs[i];
                    players.addAll(getPlayersWithRole( topic, roleSI ));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    

    public static ArrayList<Topic> getPlayersWithRoles(Topic topic, Collection<String> roleSIs) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null) {
            for(String roleSI : roleSIs){
                try {
                    players.addAll(getPlayersWithRole( topic, roleSI ));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    
    public static ArrayList<Topic> getPlayersWithRoles(Collection<Topic> topics, Collection<String> roleSIs) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        
        for(Topic topic : topics){
            try {
                players.addAll(getPlayersWithRoles(topic, roleSIs));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    

    
    public static ArrayList<Topic> getPlayersWithRoles(Collection<Topic> topics, String[] roleSIs) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        
        for(Topic topic : topics){
            try {
                players.addAll(getPlayersWithRoles(topic, roleSIs));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    
    
    public static ArrayList<Topic> getAllPlayers(Topic topic) throws TopicMapException {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null) {
            Collection<Association> associations = topic.getAssociations();
            for(Association a : associations){
                try {
                    Collection<Topic> roles = a.getRoles();
                    for(Topic role : roles){
                        if(!a.getPlayer(role).mergesWithTopic(topic)) players.add(a.getPlayer(role));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    public static ArrayList<Topic> getPlayers(Topic topic, String associationTypeSI) throws TopicMapException {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null && associationTypeSI != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);

            if(type != null) {
                Collection<Association> associations = topic.getAssociations(type);
                for(Association a : associations ){
                    try {
                        Collection<Topic> roles = a.getRoles();
                        for(Topic role : roles){
                            if(!a.getPlayer(role).mergesWithTopic(topic)) players.add(a.getPlayer(role));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }      
        return players;
    }
    
    
    
    public static Topic getFirstPlayer(Topic topic, String associationTypeSI, String roleSI) {
        try {
            ArrayList<Topic> players = getPlayers(topic, associationTypeSI, roleSI);
            if(players.size() > 0) return  players.get(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static ArrayList<Topic> getPlayers(Topic topic, String associationTypeSI, String roleSI) throws TopicMapException {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null && associationTypeSI != null && roleSI != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            Topic role = topic.getTopicMap().getTopic(roleSI);

            if(type != null && role != null) {
                Collection<Association> associations = topic.getAssociations(type);
                for(Association a : associations){
                    try {
                        if(a.getPlayer(role) != null) players.add(a.getPlayer(role));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return players;
    }
    
    
    
    public static ArrayList<Topic> getPlayers(Topic topic, Topic type, Topic role) throws TopicMapException {
        ArrayList<Topic> players = new ArrayList<Topic>();

        if(topic != null && type != null && role != null) {
            Collection<Association> associations = topic.getAssociations(type);
            for(Association a : associations){
                try {
                    if(a.getPlayer(role) != null) players.add(a.getPlayer(role));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return players;
    }
    
    
    
    public static ArrayList<Topic> getPlayers(Collection<Topic> topics, String associationTypeSI, String roleSI) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        
        for(Topic topic : topics){
            try {
                players.addAll(getPlayers(topic, associationTypeSI, roleSI));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    
    public static ArrayList<Topic> getPlayers(Topic topic, String associationTypeSI, String roleSI, String hasRole, String hasPlayer) throws TopicMapException {
        ArrayList<Topic> players = new ArrayList<Topic>();
        if(topic != null && associationTypeSI != null && roleSI != null && hasRole != null && hasPlayer != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            Topic role = topic.getTopicMap().getTopic(roleSI);
            Topic hasRoleTopic = topic.getTopicMap().getTopic(hasRole);
            Topic hasPlayerTopic = topic.getTopicMap().getTopic(hasPlayer);

            if(type != null && role != null && hasRoleTopic != null && hasPlayerTopic != null) {
                Collection<Association> associations = topic.getAssociations(type);
                for(Association a : associations){
                    try {
                        Topic player=a.getPlayer(role);
                        if(player != null) {
                            Topic checkPlayer=a.getPlayer(hasRoleTopic);
                            if(checkPlayer!=null && checkPlayer.mergesWithTopic(hasPlayerTopic)) {
                                players.add(player);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return players;
    }
    

    
    public static ArrayList<Topic> getPlayers(Collection<Topic> topics, String associationTypeSI, String roleSI, String hasRole, String hasPlayer) {
        ArrayList<Topic> players = new ArrayList<Topic>();
        
        for(Topic topic : topics){
            try {
                players.addAll(getPlayers(topic, associationTypeSI, roleSI, hasRole, hasPlayer));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    
    public static ArrayList<Topic> getSortedPlayers(Topic topic, String associationTypeSI, String roleSI, String sortRole, String lang) throws TopicMapException {
        ArrayList<Topic[]> players = new ArrayList<>();
        ArrayList<Topic> sortedPlayers = new ArrayList<Topic>();
        if(topic != null && associationTypeSI != null && roleSI != null && sortRole != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            Topic role = topic.getTopicMap().getTopic(roleSI);
            Topic sortRoleTopic = topic.getTopicMap().getTopic(sortRole);

            if(type != null && role != null && sortRole != null) {
                Collection<Association> associations = topic.getAssociations(type);
                for(Association a : associations) {
                    try {
                        Topic player = a.getPlayer(role);
                        if(player != null) {
                            Topic sortPlayer = a.getPlayer(sortRoleTopic);
                            if(sortPlayer != null) {
                                int i=0;
                                while(i<players.size()) {
                                    Topic[] tc = (Topic[]) players.get(i);
                                    Topic anotherSortPlayer = tc[0];
                                    if(anotherSortPlayer != null) {
                                        String sortName = sortPlayer.getSortName(lang);
                                        String anotherSortName = anotherSortPlayer.getSortName(lang);
                                        if(sortName != null && anotherSortName != null) {
                                            if(sortName.compareTo(anotherSortName) < 0) {
                                                break;
                                            }
                                        }
                                    }
                                    i++;
                                }
                                players.add(i, new Topic[] { sortPlayer, player } );
                            }
                            else {
                                players.add(0, new Topic[] { null, player } );
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //System.out.println("players.size() == " + players.size());
        for(int i=0; i<players.size(); i++) {
            Topic[] tc = (Topic[]) players.get(i);
            if(tc != null && tc.length == 2) {
                Topic player = tc[1];
                if(player != null) {
                    sortedPlayers.add(player);
                }
            }
        }
        //System.out.println("sortedPlayers.size() == " + sortedPlayers.size());
        return sortedPlayers;
    }
    
    
    public static ArrayList<Topic> getSortedPlayers(Topic topic, Collection<String> associationTypesSI, Collection<String> rolesSI, String sortRole, String lang) throws TopicMapException {
        ArrayList<Topic[]> players = new ArrayList<>();
        ArrayList<Topic> sortedPlayers = new ArrayList<Topic>();
        
        Iterator<String> ai = associationTypesSI.iterator();
        Iterator<String> ri = rolesSI.iterator();
        
        while(ai.hasNext() && ri.hasNext()) {
            String associationTypeSI = ai.next();
            String roleSI = ri.next();
            if(topic != null && associationTypeSI != null && roleSI != null && sortRole != null) {
                Topic type = topic.getTopicMap().getTopic(associationTypeSI);
                Topic role = topic.getTopicMap().getTopic(roleSI);
                Topic sortRoleTopic = topic.getTopicMap().getTopic(sortRole);

                if(type != null && role != null && sortRole != null) {
                    Collection<Association> associations = topic.getAssociations(type);
                    for(Association a : associations ) {
                        try {
                            Topic player = a.getPlayer(role);
                            if(player != null) {
                                Topic sortPlayer = a.getPlayer(sortRoleTopic);
                                if(sortPlayer != null) {
                                    int i=0;
                                    while(i<players.size()) {
                                        Topic[] tc = (Topic[]) players.get(i);
                                        Topic anotherSortPlayer = tc[0];
                                        if(anotherSortPlayer != null) {
                                            String sortName = sortPlayer.getSortName(lang);
                                            String anotherSortName = anotherSortPlayer.getSortName(lang);
                                            if(sortName != null && anotherSortName != null) {
                                                if(sortName.compareTo(anotherSortName) < 0) {
                                                    break;
                                                }
                                            }
                                        }
                                        i++;
                                    }
                                    players.add(i, new Topic[] { sortPlayer, player } );
                                }
                                else {
                                    players.add(0, new Topic[] { null, player } );
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        //System.out.println("players.size() == " + players.size());
        for(int i=0; i<players.size(); i++) {
            Topic[] tc = (Topic[]) players.get(i);
            if(tc != null && tc.length == 2) {
                Topic player = tc[1];
                if(player != null) {
                    sortedPlayers.add(player);
                }
            }
        }
        //System.out.println("sortedPlayers.size() == " + sortedPlayers.size());
        return sortedPlayers;
    }
    
    
    
    public static ArrayList<Topic> getTypesOfRequiredType(Topic topic, String requiredTypeSI) {
        ArrayList<Topic> selectedTypes = new ArrayList<Topic>();
        if(topic != null && requiredTypeSI != null) {
            try {
                Collection<Topic> types = topic.getTypes();
                Topic requiredType = topic.getTopicMap().getTopic(requiredTypeSI);
                if(requiredType != null) {
                    for(Topic type : types) {
                        if(type.getTypes().contains(requiredType)) {
                            selectedTypes.add(type);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return selectedTypes;
    }
    
    
    
    public static ArrayList<String> getSLsOfPlayers(Topic topic, String associationTypeSI, String roleSI) throws TopicMapException {
        ArrayList<Topic> players = getPlayers(topic, associationTypeSI, roleSI);
        ArrayList<String> locators = new ArrayList<String>();
        
        for(int i=0; i<players.size(); i++) {
            if(players.get(i) != null) {
                Locator sl = players.get(i).getSubjectLocator();
                if(sl != null) {
                    locators.add(sl.toExternalForm());
                }
            }
        }
        return locators;
    }

    
    
    
    public static ArrayList<String> getSLsOfTopics(Collection<Topic> topics) {
        ArrayList<String> locators = new ArrayList<String>();
        for(Topic topic : topics) {
            try {
                Locator sl = topic.getSubjectLocator();
                if(sl != null) {
                    locators.add(sl.toExternalForm());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return locators;
    }
    
    
    
    public static String getFirstExistingURL(Collection<String> sls) {
        for(String sl : sls) {
            try {
                if(urlExists(sl)) return sl;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    public static Collection<String> replaceInStrings(Collection<String> ss, String regex, String replacement) {
        ArrayList<String> newss = new ArrayList<String>();
        for(String s : ss) {
            try {
                newss.add(s.replaceAll(regex, replacement));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newss;
    }
    
    
    public static Collection<String> addDirToStringPaths(Collection<String> ss, String dir) {
        ArrayList<String> newss = new ArrayList<String>();
        for(String s : ss) {
            try {
                s = addDirToStringPath(s, dir);
                if(s != null) newss.add(s);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newss;
    }
    
    
    
    public static String addDirToStringPath(String s, String dir) {
        if(s != null && dir != null) {
            int i = s.lastIndexOf('/');
            if(i>0) {
                s = s.substring(0, i) + "/" + dir + s.substring(i);
            }
            else {
                s = dir + "/" + s;
            }
            return s;
        }
        return null;
    }
    

    
    public static <K> ArrayList<K> crop(Collection<K> collection, int start, int end) {
        ArrayList<K> cropped = new ArrayList<>();
        if(collection != null) {
            int s = collection.size();
            Iterator<K> iterator = collection.iterator();
            for(int i=0; i<s && i<start; i++) {
                iterator.next();
            }
            for(int i=start; i<s && i<end; i++) {
                cropped.add(iterator.next());
            }
        }
        return cropped;
    }
    
    
    
    
    public static <K> ArrayList<K> cropPage(Collection<K> collection, int page, int pageSize) {
        ArrayList<K> cropped = new ArrayList<K>();
        if(collection != null) {
            int start = Math.max(0, (page-1)*pageSize);
            int end = page * pageSize;
            int s = collection.size();
            Iterator<K> iterator = collection.iterator();
            for(int i=0; i<start && i<s; i++) {
                iterator.next();
            }
            for(int i=start; i<s && i<end; i++) {
                cropped.add(iterator.next());
            }
        }
        return cropped;
    }
    
    
    
    
    public static <K> ArrayList<K> makeIntersection(Collection<? extends K> a, Collection<? extends K> b) {
        ArrayList<K> intersection = new ArrayList<K>();
        if(a != null && b != null && b.size() > 0) {
            K o = null;
            for(Iterator<? extends K> iter = a.iterator(); iter.hasNext(); ) {
                o=iter.next();
                if(b.contains(o)) intersection.add(o);
            }
        }
        return intersection;
    }
    
    
    public static <K> Collection<K> removeDuplicates(Collection<K> objects) {
        LinkedHashSet<K> newCollection = new LinkedHashSet<K>();
        K object = null;
        newCollection.addAll(objects); // LinkedHashSet only allows each element once
/*        
        for(Iterator<K> i = objects.iterator(); i.hasNext(); ) {
            try {
                object = i.next();
                if(!newCollection.contains(object)) newCollection.add(object);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        return newCollection;
    }
    
    
    public static boolean contains(Collection objects, Object o) {
        if(objects == null || o == null) return false;
        else return objects.contains(o);
    }
    
    
    public static ArrayList<Topic> collectTopicsOfType(TopicMap topicmap, Topic typeTopic, int depth) {
        LinkedHashSet<Topic> results = new LinkedHashSet<Topic>();

        ArrayList<Topic> temp = null; // new topics of previous iteration
        ArrayList<Topic> temp2 = new ArrayList<Topic>(); // new topics of current iteration
        ArrayList<Topic> temp3 = null; // candidates for new topics of current iteration
        
        results.add(typeTopic);
        temp2.add(typeTopic);
        
        for(int i=0; i<depth; i++) {
            temp=temp2;
            temp2=new ArrayList<Topic>();
            
            temp3=new ArrayList<Topic>();
            for(Topic t : temp) {
                try {
                    temp3.addAll(topicmap.getTopicsOfType(t));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for(Topic t : temp3) {
                try {
                    if(!results.contains(t)){
                        results.add(t);
                        temp2.add(t);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<Topic>(results);
    }
    
    
    
    
     public static ArrayList<Topic> collectPlayers(TopicMap topicmap, Topic topic, String associationTypeSI, String roleSI, String hasRole, String hasPlayer, int depth) {
        LinkedHashSet<Topic> results = new LinkedHashSet<Topic>();

        ArrayList<Topic> temp = null; // new topics of previous iteration
        ArrayList<Topic> temp2 = new ArrayList<Topic>(); // new topics of current iteration
        ArrayList<Topic> temp3 = null; // candidates for new topics of current iteration
        
        results.add(topic);
        temp2.add(topic);
        
        for(int i=0; i<depth; i++) {
            temp=temp2;
            temp2=new ArrayList<Topic>();
            temp3 = new ArrayList<Topic>();
            
            for(Topic t : temp) {
                try {
                    temp3.addAll(getPlayers(t, associationTypeSI, roleSI, hasRole, hasPlayer));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for(Topic t : temp3) {
                try {
                    if(!results.contains(t)) {
                        results.add(t);
                        temp2.add(t);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<Topic>(results);
    }
    

     
    public static <K> K extractRandomObject(Collection<K> c) {
        if(c == null || c.size() == 0) return null;
        
        int randomIndex = (int) (Math.random() * c.size());
        Iterator<K> iter = c.iterator();
        K o = iter.next();
        while(iter.hasNext() && randomIndex-- > 0) o = iter.next();
        return o;
    }
     
     
    
    
    public static <K> Vector<K> extractRandomVector(Collection<K> c, int newSize) {
        Vector<K> cropped = new Vector<>();
        if( c == null || c.size() < 1 || newSize < 1) return cropped;
        int s = c.size();
        int randomStep = 0;
        Iterator<K> iter = c.iterator();
        for(int i=0; i<newSize; i++) {
            randomStep = (int) Math.floor(Math.random() * (s/newSize)) -1;
            while(iter.hasNext() && randomStep-- > 0) iter.next();
            if(iter.hasNext()) cropped.add(iter.next());
        }
        return cropped;
    }
    
    public static <K> List<K> extractRandomList(Collection<K> c, int newSize) {
        ArrayList<K> cropped = new ArrayList<>();
        if( c == null || c.size() < 1 || newSize < 1) return cropped;
        int s = c.size();
        int randomStep = 0;
        Iterator<K> iter = c.iterator();
        for(int i=0; i<newSize; i++) {
            randomStep = (int) Math.floor(Math.random() * (s/newSize)) -1;
            while(iter.hasNext() && randomStep-- > 0) iter.next();
            if(iter.hasNext()) cropped.add(iter.next());
        }
        return cropped;
    }

    
    
    
    public static ArrayList<Topic> getRoles(Topic topic, String associationTypeSI) throws TopicMapException {
        ArrayList<Topic> roleList = new ArrayList<Topic>();
        if(topic != null && associationTypeSI != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            if(type != null) {
                Collection<Association> associations = topic.getAssociations(type);
                for(Association a : associations) {
                    try {
                        Collection<Topic> roles = a.getRoles();
                        for(Topic role : roles) {
                            if(!roleList.contains(role)) roleList.add(role);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return roleList;
    }
    

    
    public static ArrayList<Association> getVisibleAssociations(Topic topic) throws TopicMapException {
        ArrayList<Association> associationVector = new ArrayList<Association>();
        if(topic != null) {
            Collection<Association> associations = topic.getAssociations();
            for(Association a : associations) {
                if(TMBox.associationVisible(a)) associationVector.add(a);
            }
        }
        return associationVector;
    }
    
    public static Collection<Association> removeAssociationsByType(Collection<Association> v, Topic type) throws TopicMapException {
        ArrayList<Association> ret= new ArrayList<Association>();
        if(v != null && type != null) {
            for(Association a : v) {
                if(a!=null && !a.getType().equals(type)) {
                    ret.add(a);
                }
            }
        }
        return ret;        
    }

    public static Collection<Association> cropAssociationsByType(Topic topic, Collection v, String associationTypeSI) throws TopicMapException {
        if(associationTypeSI==null) return new ArrayList<Association>();
        Topic type = topic.getTopicMap().getTopic(associationTypeSI);
        return cropAssociationsByType(v, type);
    }
    public static Collection<Association> cropAssociationsByType(Collection<Association> v, Topic type)  throws TopicMapException {
        ArrayList<Association> ret = new ArrayList<Association>();
        if(v != null && type != null) {
            for(Association a : v) {
                if(a!=null && a.getType().equals(type)) {
                    ret.add(a);
                }
            }
        }
        return ret;
    }
    
    public static Collection<Topic> cropTopicsByVisibility(Collection<Topic> v) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        if(v != null && v.size() > 0) {
            for(Topic t : v){
                if(t!=null && TMBox.topicVisible(t)) ret.add(t);
            }
        }
        return ret;
    }
    
    

    
    public static ArrayList<Association> cropAssociationsByVisibility(Collection<Association> v) {
        ArrayList<Association> ret = new ArrayList<Association>();
        if(v != null) {
            for(Association a : v){
                try{
                    if(a!=null && TMBox.associationVisible(a)) ret.add(a);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
    
    

    public static Collection<Topic> cropTopicsByClass(Collection<Topic> v, Topic typeTopic) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        if(v != null && v.size() > 0 && typeTopic != null) {
            for(Topic t : v) {
                if(t!=null && t.isOfType(typeTopic)) ret.add(t);
            }
        }
        return ret;
    }

    
    
    public static Collection<Topic> removeTopicsByClass(Collection<Topic> v, Topic typeTopic) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        if(v != null && v.size() > 0 && typeTopic != null) {
            for(Topic t : v) {
                if(t!=null && !t.isOfType(typeTopic)) ret.add(t);
            }
        }
        return ret;
    }
    

    public static Collection<Topic> cropTopicsIfHasAssociations(Collection<Topic> v, Topic associationTypeTopic) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        Collection<Association> typedAssociations = null;
        if(v != null && v.size() > 0 && associationTypeTopic != null) {
            for(Topic t : v) {
                if(t==null) continue;
                typedAssociations = t.getAssociations(associationTypeTopic);
                if(typedAssociations != null && typedAssociations.size() > 0) ret.add(t);
            }
        }
        return ret;
    }
    

    
    
    public static Collection<Topic> cropTopicsIfHasPlayer(Collection<Topic> v, Topic associationTypeTopic, Topic role, Topic player) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        Topic aplayer = null;
        Collection<Association> typedAssociations = null;
        if(v != null && v.size() > 0 && associationTypeTopic != null && role != null && player != null) {
            for(Topic t : v) {
                if(t!=null && !t.isRemoved()) {
                    typedAssociations = t.getAssociations(associationTypeTopic);
                    if(typedAssociations != null && typedAssociations.size() > 0) {
                        for(Association a : typedAssociations) {
                            aplayer = a.getPlayer(role);
                            if(aplayer != null && player.mergesWithTopic(aplayer)) {
                                ret.add(t);
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    
    public static Collection<Topic> cropTopicsIfHasPlayerRegex(Collection<Topic> v, Topic associationTypeTopic, Topic role, String playerRegex) throws TopicMapException {
        // By default we just find the regex -- partial match is valid match!
        return cropTopicsIfHasPlayerRegex(v, associationTypeTopic, role, playerRegex, true);
    }

    public static Collection<Topic> cropTopicsIfHasPlayerRegex(Collection<Topic> v, Topic associationTypeTopic, Topic role, String playerRegex, boolean find) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        Topic aplayer = null;
        String aplayerBasename = null;
        Collection<Association> associations = null;
        Pattern playerPattern = null;
        Matcher playerMatcher = null;
        try {
            if( playerRegex != null ) {
                playerPattern = Pattern.compile(playerRegex);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(v != null && v.size() > 0 && associationTypeTopic != null && role != null && playerPattern != null) {
            for(Topic t : v) {
                associations = t.getAssociations(associationTypeTopic);
                if(associations != null && associations.size() > 0) {
                    for(Association a : associations) {
                        aplayer = a.getPlayer(role);
                        if(aplayer != null) {
                            aplayerBasename = aplayer.getBaseName();
                            if(aplayerBasename != null) {
                                playerMatcher = playerPattern.matcher(aplayerBasename);
                                if(find) {
                                    // Accept partial match!
                                    if(playerMatcher.find()) {
                                        ret.add(t);
                                    }
                                }
                                else {
                                    // accept only full matches!
                                    if(playerMatcher.matches()) {
                                        ret.add(t);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
        
    
    
    
    
    public static Collection<Topic> cropTopicsIfHasAssociationWithInPlayer(Collection<Topic> v, Topic associationTypeTopic, Topic role, Topic associationTypeTopic2) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        Topic player = null;
        Collection<Association> typedAssociations = null;
        Collection<Association> typedAssociations2 = null;
        if(v != null && v.size() > 0 && associationTypeTopic != null && role != null && associationTypeTopic2 != null) {
            for(Topic t : v) {
                if(t!=null){
                    typedAssociations = t.getAssociations(associationTypeTopic);
                    if(typedAssociations != null && typedAssociations.size() > 0) {
                        for(Association a : typedAssociations ) {
                            if(a != null) {
                                player = a.getPlayer(role);
                                if(player != null) {
                                    typedAssociations2 = player.getAssociations(associationTypeTopic2);
                                    if(typedAssociations2 != null && typedAssociations2.size() > 0) {
                                        ret.add(t);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    
    public static Collection<Topic> cropTopicsIfHasPlayerWithInPlayer(Collection<Topic> v, Topic associationTypeTopic, Topic role, Topic associationTypeTopic2, Topic role2, Topic player2) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        Topic player = null;
        Topic playerInPlayer = null;
        Collection<Association> typedAssociations = null;
        Collection<Association> typedAssociations2 = null;
        if(v != null && v.size() > 0 && associationTypeTopic != null && role != null && associationTypeTopic2 != null) {
            VLOOP:
            for(Topic t : v) {
                if(t!=null) {
                    typedAssociations = t.getAssociations(associationTypeTopic);
                    if(typedAssociations != null && typedAssociations.size() > 0) {
                        for(Association a : typedAssociations) {
                            if(a != null) {
                                player = a.getPlayer(role);
                                if(player != null) {
                                    typedAssociations2 = player.getAssociations(associationTypeTopic2);
                                    if(typedAssociations2 != null && typedAssociations2.size() > 0) {
                                        for(Association a3 : typedAssociations2) {
                                            playerInPlayer = a3.getPlayer(role2);
                                            if(playerInPlayer.mergesWithTopic(player2)) {
                                                ret.add(t);
                                                continue VLOOP;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    public static Collection<Topic> cropTopicsByRegex(Collection<Topic> v, String lang, String regex, boolean strictMatch) throws TopicMapException {
        return cropTopicsByRegex(v, lang, regex, strictMatch, false);
    }
    
    public static Collection<Topic> cropTopicsByRegex(Collection<Topic> v, String lang, String regex, boolean strictMatch, boolean caseSensitive) throws TopicMapException {
        ArrayList<Topic> ret = new ArrayList<Topic>();
        String topicName = null;
        if(v != null && v.size() > 0 && lang != null && regex != null && regex.length() > 0) {
            Pattern pattern = (caseSensitive ? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            for(Topic t : v) {
                try {
                    if(t!=null) {
                        if(t.isRemoved()) continue;
                        topicName = t.getDisplayName(lang);
                        if(topicName == null) topicName = t.getBaseName();
                        if(topicName == null) topicName = t.getOneSubjectIdentifier().toExternalForm();

                        if(strictMatch) {
                            if( pattern.matcher(topicName).matches() ) {
                                ret.add(t);
                            }
                        }
                        else {
                            if( pattern.matcher(topicName).find() ) {
                                ret.add(t);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
    
    
    
    public static Hashtable<Topic, ArrayList<Topic>> groupTopicsByType(Collection<Topic> topics) {
        Hashtable<Topic, ArrayList<Topic>> groupedTopics = new Hashtable<Topic, ArrayList<Topic>>();
        if(topics != null) {
            Collection<Topic> types = null;
            Iterator<Topic> typeIter = null;
            ArrayList<Topic> typeGroup = null;
            for(Topic t : topics) {
                try {
                    types = t.getTypes();
                    for(Topic type : types) {
                        if(type != null) {
                            typeGroup = groupedTopics.get(type);
                            if(typeGroup == null) {
                                typeGroup = new ArrayList<Topic>();
                                groupedTopics.put(type, typeGroup);
                            }
                            typeGroup.add(t);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return groupedTopics;
    }
    
    
    public static Collection<Topic> basenamePrefixSearch(TopicMap topicmap, String lang, String prefix, Topic typeTopic) {
        try {
            if(typeTopic != null && topicmap != null && lang != null && prefix != null) {
                ArrayList<Topic> searchResults = new ArrayList<Topic>();
                Collection<Topic> topics = topicmap.getTopicsOfType(typeTopic);
                Topic t = null;
                String bn = null;
                for(Iterator<Topic> iter=topics.iterator(); iter.hasNext(); ) {
                    t = iter.next();
                    bn = t.getBaseName();
                    if(bn != null && bn.startsWith(prefix)) searchResults.add(t);
                }
                return searchResults;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<Topic>();
    }
    
    
    
    public static Collection<Topic> searchTopics(TopicMap topicmap, String lang, String query) {
        try {
            return topicmap.search(query, new TopicMapSearchOptions(true, false, false, false, false));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<Topic>();
    }
    
    
    
    public static String trimNonAlphaNums(String word) {
        if(word == null || word.length() < 1) return "";
        
        int i=0;
        int j=word.length()-1;
        for(; i<word.length() && !Character.isLetterOrDigit(word.charAt(i)); i++);
        for(; j>i+1 && !Character.isLetterOrDigit(word.charAt(j)); j--);
        
        return word.substring(i,j+1);
    }
    
    
    public static String encodeURL(String s) {
    	if(s != null) {
    		return encodeURL(s, "UTF-8");
    	}
    	return null;
    }
    
    public static String encodeURL(String s, String enc) {
        try { if(s != null && enc != null) { return java.net.URLEncoder.encode(s, enc); } }
        catch (Exception e) { e.printStackTrace(); return s; }
        return "";
    }
    
    public static String decodeURL(String s) {
        return decodeURL(s, "UTF-8");
    }
    
    public static String decodeURL(String s, String enc) {
        try { if(s != null) { return java.net.URLDecoder.decode(s, enc); } }
        catch (Exception e) { e.printStackTrace(); return s; }
        return "";
    }
    
    
    public static String populateLinks(String text, String linkTemplate) {
        if(text != null && text.length()>0) {
            String DELIMITERS = " \n\t',.\"";
            StringBuffer newText = new StringBuffer(1000);
            String searchword;
            String link;
            String substring;
            int index=0;
            int wordStart;
            int wordEnd;
            while(index < text.length()) {
                while(index < text.length() && DELIMITERS.indexOf(text.charAt(index)) != -1) {
                    newText.append(text.charAt(index));
                    index++;
                }
                
                // pass html/xml tags
                while(index < text.length() && text.charAt(index) == '<') {
                   while(index < text.length() && text.charAt(index) != '>') {
                        newText.append(text.charAt(index));
                        index++;
                   }
                   newText.append(text.charAt(index));
                   index++;
                }
                // potential word found
                wordStart=index;
                while(index < text.length() && DELIMITERS.indexOf(text.charAt(index)) == -1 && text.charAt(index) != '<') {
                    index++;
                }
                
                if(index > wordStart) {
                    substring = text.substring(wordStart, index);
//                    try { substring = encodeHTML(substring); } catch (Exception e) {}
                    if(index-wordStart > 3) {
                        searchword = trimNonAlphaNums(substring);
                        if(searchword.length() > 3) {
                            link = linkTemplate.replaceAll("%searchw%", encodeURL(searchword));
                            link = link.replaceAll("%word%", substring);
                            newText.append(link);
                        }
                        else {
                            newText.append(substring);
                        }
                    }
                    else {
                        newText.append(substring);
                    }
                }
                
            }
            text = newText.toString();
        }
        return text;
    }

    
    
    public static class TreeNode {
        public Topic content;
        public boolean openChildren;
        public boolean closeChildren;
        public TreeNode(Topic content,boolean openChildren,boolean closeChildren){
            this.content=content;
            this.openChildren=openChildren;
            this.closeChildren=closeChildren;
        }
    }
    
    
    
    
    public static ArrayList<String> capitalizeFirst(List<String> words) {
        ArrayList<String> newWords = new ArrayList<String>();
        if (words != null) {
            for (int i=0; i<words.size(); i++) {
                newWords.add(capitalizeFirst(words.get(i)));
            }
        }
        return newWords;
    }
    
    
    
    
    
    public static String capitalizeFirst(String word) {
        boolean shouldCapitalize = true;
        if(word != null) {
            for (int i=0; i<word.length() && shouldCapitalize; i++) {
                if(Character.isLetter(word.charAt(i)) && shouldCapitalize) {
                    try {
                        word = word.substring(0, i) + (Character.toUpperCase(word.charAt(i))) + word.substring(i+1, word.length());
                        shouldCapitalize = false;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return word;
    }
    

    public static String capitalizeFirsts(String word) {
        StringBuffer newWord = new StringBuffer();
        boolean shouldCapitalize = true;
        if (word != null) {
            for (int i=0; i<word.length(); i++) {
                if (Character.isLetter(word.charAt(i)) && shouldCapitalize) {
                    newWord.append(Character.toUpperCase(word.charAt(i)));
                    shouldCapitalize = false;
                }
                else if (!Character.isLetter(word.charAt(i))) {
                    newWord.append(word.charAt(i));
                    shouldCapitalize = true;
                }
                else newWord.append(word.charAt(i));
            }
        }
        return newWord.toString();
    }
    
    
    
    public static boolean urlExists(String urlString) {
        if(urlString != null) {
            try {
                if(IObox.urlExists(new URL(urlString))) return true;
            }
            catch (Exception e) {}
        }
        return false;
    }
    
    

    
    

    public static Collection<Association> sortAssociationsWithPlayer(Collection<Association> associations, Topic sortRole, String lang) {
        ArrayList<Association> al = new ArrayList<Association>(associations);
        Collections.sort(al, new TMBox.AssociationPlayerComparator(sortRole,lang));
        return al;
        
/*        Vector<Association> sortedAssociations = new Vector();
        
        if(associations == null || sortRole == null || lang == null ) return sortedAssociations;
        
        Vector<Topic> playersCleaned = new Vector();
        Collection players = null;
        
        Iterator<Association> iter = associations.iterator();
        Iterator<Topic> playerIter = null;
        Association association = null;
        Topic player = null;

        // First collects all players
        while(iter.hasNext()) {
            try {
                association = iter.next();
                if(association != null && !association.isRemoved()) {
                    player = association.getPlayer(sortRole);
                    if(!playersCleaned.contains(player)) playersCleaned.add(player);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        Vector<Topic> tempPlayers = new Vector();
        tempPlayers.addAll( TMBox.sortTopics(playersCleaned, lang) );
        
        // Then iterate through sorted players and pick topics that have the player topic
        // as a player...
        Topic cleanPlayer = null;
        int s = tempPlayers.size();
        
        for(int i=0; i<s; i++) {
            cleanPlayer = tempPlayers.elementAt(i);
            iter = associations.iterator();
            while(iter.hasNext()) {
                try {
                    association = iter.next();
                    if(association != null && !association.isRemoved()) {
                        player = association.getPlayer(sortRole);
                        if(player.mergesWithTopic(cleanPlayer)) {
                            sortedAssociations.add(association);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Finally add all remainding topics to sorted array....
        iter = associations.iterator();
        while(iter.hasNext()) {
            try {
                association = iter.next();
                if(association != null && !association.isRemoved()) {
                    if(!sortedAssociations.contains(association)) {
                        sortedAssociations.add(association);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return sortedAssociations;*/
    }
    
    
    public static Collection<Topic> sortTopicsWithOccurrence(Collection<Topic> topics, Topic occurrenceType, String lang) {
        return sortTopicsWithOccurrence(topics, occurrenceType, lang, "false");
    }
    
    public static Collection<Topic> sortTopicsWithOccurrence(Collection<Topic> topics, Topic occurrenceType, String lang, String desc) {
        if(occurrenceType != null && lang != null) {
            try {
                return TMBox.sortTopicsByData(topics, occurrenceType, lang, (desc.equalsIgnoreCase("true") ? true : false));
            }
            catch(Exception e) {
                // 
            }
        }
        return topics;
    }
    
    
    
    public static Collection<Topic> sortTopics(Collection<Topic> topics, String lang) {
        return TMBox.sortTopics(topics, lang);
    }
    
    
    public static Collection<Topic> sortTopicsWithPlayer(Collection<Topic> topics, Topic associationType, Topic role, String lang) {
        List<Topic> al=new ArrayList<Topic>(topics);
        Collections.sort(al,new TMBox.TopicAssociationPlayerComparator(associationType,role,lang));
        return al;
/*        Vector<Topic> sortedTopics = new Vector();
        
        if(topics == null || associationType == null || role == null || lang == null ) return sortedTopics;
        
        Vector<Topic> playersCleaned = new Vector();
        Collection players = null;
        
        Iterator<Topic> iter = topics.iterator();
        Iterator<Topic> playerIter = null;
        Topic topic = null;
        Topic player = null;

        // First collects all players
        while(iter.hasNext()) {
            try {
                topic = iter.next();
                if(topic != null && !topic.isRemoved()) {
                    players = getPlayers(topic, associationType, role);
                    playerIter = players.iterator();
                    while(playerIter.hasNext()) {
                        player = playerIter.next();
                        if(!playersCleaned.contains(player)) playersCleaned.add(player);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        Vector<Topic> tempPlayers = new Vector();
        tempPlayers.addAll( TMBox.sortTopics(playersCleaned, lang) );
        
        // Then iterate through sorted players and pick topics that have the player topic
        // as a player...
        Topic cleanPlayer = null;
        int s = tempPlayers.size();
        
        for(int i=0; i<s; i++) {
            cleanPlayer = tempPlayers.elementAt(i);
            iter = topics.iterator();
            while(iter.hasNext()) {
                try {
                    topic = iter.next();
                    if(topic != null && !topic.isRemoved()) {
                        players = getPlayers(topic, associationType, role);
                        if(players.contains(cleanPlayer) && !sortedTopics.contains(topic)) {
                            sortedTopics.add(topic);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Finally add all remainding topics to sorted array....
        iter = topics.iterator();
        while(iter.hasNext()) {
            try {
                topic = iter.next();
                if(topic != null && !topic.isRemoved()) {
                    if(!sortedTopics.contains(topic)) {
                        sortedTopics.add(topic);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return sortedTopics;*/
    }
    
    
    
    
    
    public static <K> Collection<K> reverseOrder(Collection<K> objects) {
    	if(objects != null) {
	    	List<K> reversedList = new ArrayList<K>();
	    	reversedList.addAll(objects);
	    	Collections.reverse(reversedList);
	        return reversedList;
    	}
    	return null;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    public static String composeLinearTopicNameString(Collection<Topic> topics, String lang, String delimiter) {
        return composeLinearTopicNameString(topics, lang, delimiter, "");
    }
    
    public static String composeLinearTopicNameString(Collection<Topic> topics, String lang, String delimiter, String extraSettings) {
        StringBuffer sb = new StringBuffer("");
        if(delimiter == null) delimiter = "";
        if(lang == null) lang = "en";
        if(topics != null && topics.size() > 0) {
            for(Iterator<Topic> iter = topics.iterator(); iter.hasNext(); ) {
                try {
                    Topic t = (Topic) iter.next();
                    String name = t.getDisplayName(lang);
                    if(extraSettings != null && extraSettings.indexOf("SWAP_ORDER") != -1) name = firstNameFirst(name);
                    sb.append(name);
                    if(iter.hasNext()) sb.append(delimiter);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    
    
    
    public static String makeHTML(String s) {
        if(s != null && s.length() > 0) {
            s = s.replaceAll("\\n", "<br>");
        }
        return s;
    }
    
    
    public static String changeStringEncoding(String s, String sourceenc, String targetenc) {
        if(s != null && targetenc != null && sourceenc != null) {
            try { return new String( s.getBytes(sourceenc), targetenc ); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return s;
    }
    
    
    public static String firstNameFirst(String name) {
        return Textbox.firstNameFirst(name);
    }
    
    
    public static String lastNameFirst(String name) {
        return Textbox.reverseName(name);
    }
    
    
    public static String smartSubstring(String s, int len) {
        if(s != null && s.length() > len) {
            s = s.substring(0, len);
            int si = s.lastIndexOf(' ');
            if(si > len/2) {
                s = s.substring(0, si);
            }
            s = s + "...";
        }
        return s;
    }
    
    
    public static int makeInt(String number) {
        try {
            return Integer.parseInt(number);
        }
        catch(Exception e) {}
        return 0;
    }
    
      
    
    
    // Helper method to get date integers ....
    
    public static int currentYear() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.YEAR) - 1900;
    }
    public static int currentMonth() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.MONTH);
    }
    public static int currentDate() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.DAY_OF_MONTH);
    }
    public static int currentDay() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.DAY_OF_WEEK);
    }
    
    
    
    
    
    // --------------------------------------------------------------- SORT ---
    
    
    
    public static <T extends Comparable> List<T> sort(List<T> list) {
        Collections.sort(list);
        return list;
    }
    
    
}
