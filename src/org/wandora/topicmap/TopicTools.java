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
 * 
 *
 * TopicTools.java
 *
 * Created on 1. maaliskuuta 2006, 21:06
 *
 */

package org.wandora.topicmap;


import java.util.*;
import org.wandora.topicmap.layered.*;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class TopicTools {
    
    /** Creates a new instance of TopicTools */
    public TopicTools() {
    }
    
    
    /**
     * Gets one player of given role in the associations of the topic.
     */
    public static Topic getFirstPlayerWithRole(Topic topic, String roleSI) {
        try {
            Vector<Topic> players = getPlayersWithRole(topic, roleSI);
            if(players.size() > 0) return players.elementAt(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * Get all players of given role in the associations of the topic. 
     */
    
    public static Vector<Topic> getPlayersWithRole(Topic topic, String roleSI) throws TopicMapException  {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null && roleSI != null) {
            Topic role = topic.getTopicMap().getTopic(roleSI);

            if(role != null) {
                Collection associations = topic.getAssociations();
                for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                    try {
                        Association a = (Association) iter.next();
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
    
    
    /**
     * Get all players of given role in the associations of a collection of topics.
     * The returned collection may contain same
     * topic several times. (TODO: is this intended???)
     */
    public static Vector<Topic> getPlayersWithRole(Collection topics, String roleSI) {
        Vector<Topic> players = new Vector<Topic>();
        
        for(Iterator i = topics.iterator(); i.hasNext(); ) {
            try {
                Topic topic = (Topic) i.next();
                players.addAll(getPlayersWithRole(topic, roleSI));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    /**
     * Get all players with one of specified roles in the associations of the topic.
     */
    public static Vector<Topic> getPlayersWithRoles(Topic topic, String[] roleSIs) {
        Vector<Topic> players = new Vector<Topic>();
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
    
    

    /**
     * Get all players with one of specified roles in the associations of the topic.
     */
    public static Vector<Topic> getPlayersWithRoles(Topic topic, Collection roleSIs) {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null) {
            String roleSI = null;
            for(Iterator i=roleSIs.iterator(); i.hasNext(); ) {
                try {
                    roleSI = (String) i.next();
                    players.addAll(getPlayersWithRole( topic, roleSI ));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    
    /**
     * Get all players with one of specified roles in the associations of topics
     * in the collection.
     */
    public static Vector<Topic> getPlayersWithRoles(Collection topics, Collection roleSIs) {
        Vector<Topic> players = new Vector<Topic>();
        
        for(Iterator i = topics.iterator(); i.hasNext(); ) {
            try {
                Topic topic = (Topic) i.next();
                players.addAll(getPlayersWithRoles(topic, roleSIs));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    

    
    /**
     * Get all players with one of specified roles in the associations of topics
     * in the collection.
     */
    public static Vector<Topic> getPlayersWithRoles(Collection topics, String[] roleSIs) {
        Vector<Topic> players = new Vector<Topic>();
        
        for(Iterator i = topics.iterator(); i.hasNext(); ) {
            try {
                Topic topic = (Topic) i.next();
                players.addAll(getPlayersWithRoles(topic, roleSIs));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    /**
     * Get all players of all associations of a given type in the topic.
     */
    public static Vector<Topic> getPlayers(Topic topic, String associationTypeSI) throws TopicMapException  {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null && associationTypeSI != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);

            if(type != null) {
                Collection associations = topic.getAssociations(type);
                for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                    try {
                        Association a = (Association) iter.next();
                        Collection roles = a.getRoles();
                        for(Iterator iterRoles = roles.iterator(); iterRoles.hasNext(); ) {
                            Topic role = (Topic) iterRoles.next();
                            if(a.getPlayer(role) != topic) players.add(a.getPlayer(role));
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
    
    
    
    /**
     * Get one player in the associations of given type in the topic that is of given role.
     */
    public static Topic getFirstPlayer(Topic topic, String associationTypeSI, String roleSI) {
        try {
            Vector players = getPlayers(topic, associationTypeSI, roleSI);
            if(players.size() > 0) return (Topic) players.elementAt(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public static Vector<Topic> getPlayers(Topic topic, String associationTypeSI, String roleSI)  throws TopicMapException {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null && associationTypeSI != null && roleSI != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            Topic role = topic.getTopicMap().getTopic(roleSI);

            if(type != null && role != null) {
                Collection associations = topic.getAssociations(type);
                for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                    try {
                        Association a = (Association) iter.next();
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
    

    public static Vector<Topic> getPlayers(Topic topic, Topic associationType, Topic role)  throws TopicMapException {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null && associationType != null && role != null) {

            if(associationType != null && role != null) {
                Collection associations = topic.getAssociations(associationType);
                for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                    try {
                        Association a = (Association) iter.next();
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
    
    
    public static Vector<Topic> getPlayers(Collection topics, String associationTypeSI, String roleSI) {
        Vector<Topic> players = new Vector<Topic>();
        
        for(Iterator i = topics.iterator(); i.hasNext(); ) {
            try {
                Topic topic = (Topic) i.next();
                players.addAll(getPlayers(topic, associationTypeSI, roleSI));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    
    public static Vector<Topic> getPlayers(Topic topic, String associationTypeSI, String roleSI, String hasRole, String hasPlayer) throws TopicMapException  {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null && associationTypeSI != null && roleSI != null && hasRole != null && hasPlayer != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            Topic role = topic.getTopicMap().getTopic(roleSI);
            Topic hasRoleTopic = topic.getTopicMap().getTopic(hasRole);
            Topic hasPlayerTopic = topic.getTopicMap().getTopic(hasPlayer);

            if(type != null && role != null && hasRoleTopic != null && hasPlayerTopic != null) {
                Collection associations = topic.getAssociations(type);
                for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                    try {
                        Association a = (Association) iter.next();
                        if(a.getPlayer(role) != null) {
                            if(a.getPlayer(hasRoleTopic).equals(hasPlayerTopic)) {
                                players.add(a.getPlayer(role));
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
    
    
    
    public static Vector<Topic> getPlayers(Topic topic, Topic type, Topic role, Topic hasRole, Topic hasPlayer)  throws TopicMapException {
        Vector<Topic> players = new Vector<Topic>();
        if(topic != null && type != null && role != null && hasRole != null && hasPlayer != null) {
            Collection associations = topic.getAssociations(type);
            for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                try {
                    Association a = (Association) iter.next();
                    if(a.getPlayer(role) != null) {
                        if(a.getPlayer(hasRole).equals(hasPlayer)) {
                            players.add(a.getPlayer(role));
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    
    public static Vector<Topic> getPlayers(Collection topics, String associationTypeSI, String roleSI, String hasRole, String hasPlayer)  throws TopicMapException {
        Vector<Topic> players = new Vector<Topic>();
        
        for(Iterator i = topics.iterator(); i.hasNext(); ) {
            try {
                Topic topic = (Topic) i.next();
                players.addAll(getPlayers(topic, associationTypeSI, roleSI, hasRole, hasPlayer));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    
    
    public static Vector<Topic> getSortedPlayers(Topic topic, String associationTypeSI, String roleSI, String sortRole, String lang)  throws TopicMapException {
        Vector players = new Vector();
        Vector<Topic> sortedPlayers = new Vector<Topic>();
        if(topic != null && associationTypeSI != null && roleSI != null && sortRole != null) {
            Topic type = topic.getTopicMap().getTopic(associationTypeSI);
            Topic role = topic.getTopicMap().getTopic(roleSI);
            Topic sortRoleTopic = topic.getTopicMap().getTopic(sortRole);

            if(type != null && role != null && sortRole != null) {
                Collection associations = topic.getAssociations(type);
                for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                    try {
                        Association a = (Association) iter.next();
                        Topic player = a.getPlayer(role);
                        if(player != null) {
                            Topic sortPlayer = a.getPlayer(sortRoleTopic);
                            if(sortPlayer != null) {
                                int i=0;
                                while(i<players.size()) {
                                    Topic[] tc = (Topic[]) players.elementAt(i);
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
            Topic[] tc = (Topic[]) players.elementAt(i);
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
    
    

  
    
    
    public static Vector<Topic> getSortedPlayers(Topic topic, Collection associationTypesSI, Collection rolesSI, String sortRole, String lang)  throws TopicMapException {
        Vector players = new Vector();
        Vector<Topic> sortedPlayers = new Vector<Topic>();
        
        Iterator ai = associationTypesSI.iterator();
        Iterator ri = rolesSI.iterator();
        
        while(ai.hasNext() && ri.hasNext()) {
            String associationTypeSI = (String) ai.next();
            String roleSI = (String) ri.next();
            if(topic != null && associationTypeSI != null && roleSI != null && sortRole != null) {
                Topic type = topic.getTopicMap().getTopic(associationTypeSI);
                Topic role = topic.getTopicMap().getTopic(roleSI);
                Topic sortRoleTopic = topic.getTopicMap().getTopic(sortRole);

                if(type != null && role != null && sortRole != null) {
                    Collection associations = topic.getAssociations(type);
                    for(Iterator iter = associations.iterator(); iter.hasNext(); ) {
                        try {
                            Association a = (Association) iter.next();
                            Topic player = a.getPlayer(role);
                            if(player != null) {
                                Topic sortPlayer = a.getPlayer(sortRoleTopic);
                                if(sortPlayer != null) {
                                    int i=0;
                                    while(i<players.size()) {
                                        Topic[] tc = (Topic[]) players.elementAt(i);
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
            Topic[] tc = (Topic[]) players.elementAt(i);
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
    
    
    
    public static Vector<Topic> getTypesOfRequiredType(Topic topic, String requiredTypeSI) {
        Vector<Topic> selectedTypes = new Vector<Topic>();
        if(topic != null && requiredTypeSI != null) {
            try {
                Collection types = topic.getTypes();
                Topic requiredType = topic.getTopicMap().getTopic(requiredTypeSI);
                if(requiredType != null) {
                    for(Iterator iter = types.iterator(); iter.hasNext(); ) {
                        Topic type = (Topic) iter.next();
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
    
    
    
    public static Vector getSLsOfPlayers(Topic topic, String associationTypeSI, String roleSI)  throws TopicMapException {
        Vector players = getPlayers(topic, associationTypeSI, roleSI);
        Vector locators = new Vector();
        
        for(int i=0; i<players.size(); i++) {
            if(players.elementAt(i) != null) {
                String sl = ((Topic) players.elementAt(i)).getSubjectLocator().toExternalForm();
                if(sl != null && sl.length() > 0) {
                    locators.add(sl);
                }
            }
        }
        return locators;
    }

    
    
    
    public static Vector getSLsOfTopics(Collection topics) {
        Vector locators = new Vector();
        for(Iterator iter=topics.iterator(); iter.hasNext(); ) {
            try {
                Topic topic = (Topic) iter.next();
                String sl = topic.getSubjectLocator().toExternalForm();
                if(sl != null && sl.length() > 0) {
                    locators.add(sl);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return locators;
    }
    
    
    
    public static Collection<Topic> getEdgeTopics(Collection<Topic> source, Topic associationType, Topic baseRole, Topic outRole) {
        ArrayList<Topic> rootTopics = new ArrayList<Topic>();
        Iterator<Topic> sourceIterator = source.iterator();
        while(sourceIterator.hasNext()) {
            Topic base = sourceIterator.next();
            Topic edge = getEdgeTopic(base, associationType, baseRole, outRole);
            if(!rootTopics.contains(edge)) {
                rootTopics.add(edge);
            }
        }
        return rootTopics;
    }
    
    
    
    public static Topic getEdgeTopic(Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        ArrayList<Topic> pathTopics = new ArrayList<Topic>();
        int MAXDEPTH = 9999;
                
        while(true && --MAXDEPTH > 0) {
            try {
                Collection<Association> associations = base.getAssociations(associationType, baseRole);
                if(associations.size() > 0) {
                    Iterator<Association> iterator = associations.iterator();
                    Association association = null;
                    if(iterator.hasNext()) {
                        association = iterator.next();
                        Topic pathTopic = association.getPlayer(outRole);
                        if(pathTopic != null) {
                            if(!pathTopics.contains(pathTopic)) {
                                pathTopics.add(pathTopic);
                                base = pathTopic;
                            }
                            else {
                                // PATH IS CYCLIC!
                                break;
                            }
                        }
                    }
                }
                else break;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return base;
    }
    
    
    public static ArrayList<Topic> getSinglePath(Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        ArrayList<Topic> pathTopics = new ArrayList<Topic>();
        int MAXDEPTH = 9999;
                
        while(true && --MAXDEPTH > 0) {
            try {
                Collection<Association> associations = base.getAssociations(associationType, baseRole);
                if(associations.size() > 0) {
                    Iterator<Association> iterator = associations.iterator();
                    Association association = null;
                    if(iterator.hasNext()) {
                        association = iterator.next();
                        Topic pathTopic = association.getPlayer(outRole);
                        if(pathTopic != null) {
                            if(!pathTopics.contains(pathTopic)) {
                                pathTopics.add(pathTopic);
                                base = pathTopic;
                            }
                            else {
                                // PATH IS CYCLIC!
                                break;
                            }
                        }
                    }
                }
                else break;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return pathTopics;
    }



    public static ArrayList<ArrayList<Topic>> getCyclePaths(Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        ArrayList<ArrayList<Topic>> cycles = new ArrayList<ArrayList<Topic>>();
        ArrayList<Topic> path = new ArrayList<Topic>();
        path.add(base);
        getCyclePaths(cycles, path, base, associationType, baseRole, outRole);
        return cycles;
    }



    
    private static void getCyclePaths(ArrayList<ArrayList<Topic>> cycles, ArrayList<Topic> path, Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        try {
            Collection<Association> associations = base.getAssociations(associationType, baseRole);
            if(associations.size() > 0) {
                Iterator<Association> iterator = associations.iterator();
                Association association = null;
                if(iterator.hasNext()) {
                    association = iterator.next();
                    Topic pathTopic = association.getPlayer(outRole);
                    if(pathTopic != null) {
                        if(path.contains(pathTopic)) {
                           ArrayList<Topic> cycle = new ArrayList<Topic>();
                           int index = path.indexOf(pathTopic);
                           for(int i=index; i<path.size(); i++) {
                               cycle.add(path.get(i));
                           }
                           cycle.add(pathTopic);
                           cycles.add(cycle);
                        }
                        else {
                            path.add(pathTopic);
                            base = pathTopic;
                            getCyclePaths(cycles, (ArrayList<Topic>) path.clone(), base, associationType, baseRole, outRole);
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    //----------------------------------------------------- LOCATOR CLEANING ---
    

    public static boolean isDirtyLocator(Locator l) {
        String s = l.toExternalForm();
        if(DataURL.isDataURL(s)) return false;
        for(int k=0; k<s.length(); k++) {
            if(isDirtyLocatorCharacter(s.charAt(k))) return true;
        }
        return false;
    }
    
 
    public static boolean isDirtyLocatorCharacter(char c) {
        if("1234567890poiuytrewqasdfghjklmnbvcxzPOIUYTREWQASDFGHJKLMNBVCXZ$-_.+!*'(),/?:=&#~;%".indexOf(c) != -1) return false;
        else return true;
    }
    
    
    public static String schars = "ÄÀÁÂÃÆÕÔÓÒÖòóôõÅæãäöâåàáéüùúûüÜÛÚÙÝŸýÿŽžŒÇœçÈÉÊËèéêëìíîïÌÍÎÏÐðÑñ×ƒØøÞþßŠš";
    public static String dchars = "AAAAAAOOOOOooooAaaaoaaaaeuuuuuUUUUYYyyZzCCccEEEEeeeeiiiiIIIIDdNnxfOottsSs";
    public static char repacementLocatorCharacterFor(char c) {
        int i = schars.indexOf(c);
        if(i == -1) {
            if(c < 32) return 0;
            return '_';
        }
        else return dchars.charAt(i);
    }
    
    
    public static String cleanDirtyLocator(String s) {
        if(DataURL.isDataURL(s)) {
            return s;
        }
        else {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for(int k=0; k<s.length(); k++) {
                char c = s.charAt(k);
                if(isDirtyLocatorCharacter(c)) {
                    c = repacementLocatorCharacterFor(c);
                    if(c != 0) sb.append(c);
                }
                else sb.append(c);
            }
            return sb.toString();
        }
    }
    
    public static Locator cleanDirtyLocator(Locator l) {
        if(l != null) {
            l = new Locator(cleanDirtyLocator(l.toExternalForm()));
        }
        return l;
    }
    
    
    
    public static int locatorCounter = 0;
    public static Locator createDefaultLocator() {
        locatorCounter++;
        long stamp = System.currentTimeMillis();
        String defaultSIprefix = "http://wandora.org/si/temp/";
        Locator newLocator = new Locator(defaultSIprefix + stamp + "-" + locatorCounter);
        // System.out.println("Creating default SI '"+newLocator.toExternalForm());
        return newLocator;
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    public static final String nameOfInstanceType = "instance";
    public static final String nameOfTypeType = "type";
    
    
    
    public static String getTopicName(Topic t) {
        if(t == null) return null;
        String name = null;
        try {
            name = t.getBaseName();
            if(name == null) {
                name = t.getOneSubjectIdentifier().toExternalForm();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return name;
    }
    
    
    public static String[] getAssociationsAsTriplets(Topic t) {
        if(t == null) return null;
        ArrayList<String> triplets = new ArrayList<String>();
        String tstr = getTopicName(t);
                
        try {
            Collection<Topic> types = t.getTypes();
            for(Topic type : types) {
                triplets.add(tstr);
                triplets.add(nameOfTypeType);
                triplets.add(getTopicName(type));
            }
            
            Collection<Topic> instances = t.getTopicMap().getTopicsOfType(t);
            for(Topic instance : instances) {
                triplets.add(tstr);
                triplets.add(nameOfInstanceType);
                triplets.add(getTopicName(instance));
            }
            
            Collection<Association> associations = t.getAssociations();
            for(Association a : associations) {
                Topic type = a.getType();
                Collection<Topic> roles = a.getRoles();
                boolean skipSame = true;
                for(Topic role : roles) {
                    Topic player = a.getPlayer(role);
                    if(player.mergesWithTopic(t) && skipSame) {
                        continue;
                    }
                    triplets.add(tstr);
                    triplets.add(getTopicName(type));
                    triplets.add(getTopicName(player));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return triplets.toArray( new String[] {} );
    }
    
    

    public static String[] getAssociationsAsTypePlayerTuples(Topic t) {
        if(t == null) return null;
        ArrayList<String> tuples = new ArrayList<String>();
   
        try {
            Collection<Topic> types = t.getTypes();
            for(Topic type : types) {
                tuples.add(nameOfTypeType);
                tuples.add(getTopicName(type));
            }
            
            Collection<Topic> instances = t.getTopicMap().getTopicsOfType(t);
            for(Topic instance : instances) {
                tuples.add(nameOfInstanceType);
                tuples.add(getTopicName(instance));
            }
            
            Collection<Association> associations = t.getAssociations();
            for(Association a : associations) {
                Topic type = a.getType();
                Collection<Topic> roles = a.getRoles();
                boolean skipSame = true;
                for(Topic role : roles) {
                    Topic player = a.getPlayer(role);
                    if(player.mergesWithTopic(t) && skipSame) {
                        continue;
                    }
                    tuples.add(getTopicName(type));
                    tuples.add(getTopicName(player));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return tuples.toArray( new String[] {} );
    }
    
    
    
    
    public static String[] getAsTriplet(Association a) {
        if(a == null) return null;
        ArrayList<String> triplet = new ArrayList<String>();
 
        try {
            Topic type = a.getType();
            triplet.add(getTopicName(type));
            Collection<Topic> roles = a.getRoles();
            for(Topic role : roles) {
                Topic player = a.getPlayer(role);
                triplet.add(getTopicName(player));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return triplet.toArray( new String[] {} );
    }
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    public static ArrayList<Layer> getLayers(ContainerTopicMap tm) {
        if (tm == null) return null;
        ArrayList<Layer> layers = new ArrayList<Layer>();
        
        for (Layer l : tm.getLayers()){
            TopicMap ltm = l.getTopicMap();
            if(ltm instanceof ContainerTopicMap){
             layers.addAll(getLayers((ContainerTopicMap)ltm));   
            }
            layers.add(l);
        }
        return layers;
    }
    
    public static boolean isTopicMapContainer(TopicMap tm){
        return tm instanceof ContainerTopicMap;
    }
}
