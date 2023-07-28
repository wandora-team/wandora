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
 * This class is a collection of static helpers for accessing
 * topics and associations.
 * 
 *
 * @author akivela
 */
public class TopicTools {
    
    
    
    
    /** Creates a new instance of TopicTools */
    public TopicTools() {
    }
    
    
    /**
     * Returns one player of given role in the associations of the topic.
     * 
     * @param topic
     * @param roleSI
     * @return 
     */
    public static Topic getFirstPlayerWithRole(Topic topic, String roleSI) {
        try {
            List<Topic> players = getPlayersWithRole(topic, roleSI);
            if(players != null && !players.isEmpty()) {
                return players.get(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * Get all players of given role in the associations of the topic. 
     * 
     * @param topic
     * @param roleSI
     * @return 
     * @throws org.wandora.topicmap.TopicMapException
     */
    public static List<Topic> getPlayersWithRole(Topic topic, String roleSI) throws TopicMapException  {
        List<Topic> players = new ArrayList<Topic>();
        if(topic != null && roleSI != null) {
            Topic role = topic.getTopicMap().getTopic(roleSI);

            if(role != null) {
                Collection associations = topic.getAssociations();
                if(associations != null) {
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
        }
        return players;
    }
    
    
    /**
     * Get all players of given role in the associations of a collection of topics.
     * Returned topic list may contain same
     * topic several times (TODO: is this intended?).
     * 
     * @param topics
     * @param roleSI
     * @return 
     */
    public static List<Topic> getPlayersWithRole(Collection topics, String roleSI) {
        List<Topic> players = new ArrayList<Topic>();
        
        if(topics != null) {
            for(Iterator i = topics.iterator(); i.hasNext(); ) {
                try {
                    Topic topic = (Topic) i.next();
                    players.addAll(getPlayersWithRole(topic, roleSI));
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
     * 
     * @param topic
     * @param roleSIs
     * @return 
     */
    public static List<Topic> getPlayersWithRoles(Topic topic, String[] roleSIs) {
        List<Topic> players = new ArrayList<Topic>();
        if(topic != null && roleSIs != null) {
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
     * 
     * @param topic
     * @param roleSIs
     * @return 
     */
    public static List<Topic> getPlayersWithRoles(Topic topic, Collection roleSIs) {
        List<Topic> players = new ArrayList<Topic>();
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
     * 
     * @param topics
     * @param roleSIs
     * @return 
     */
    public static List<Topic> getPlayersWithRoles(Collection topics, Collection roleSIs) {
        List<Topic> players = new ArrayList<Topic>();
        
        if(topics != null) {
            for(Iterator i = topics.iterator(); i.hasNext(); ) {
                try {
                    Topic topic = (Topic) i.next();
                    players.addAll(getPlayersWithRoles(topic, roleSIs));
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
     * 
     * @param topics
     * @param roleSIs
     * @return 
     */
    public static List<Topic> getPlayersWithRoles(Collection topics, String[] roleSIs) {
        List<Topic> players = new ArrayList<Topic>();
        
        if(topics != null) {
            for(Iterator i = topics.iterator(); i.hasNext(); ) {
                try {
                    Topic topic = (Topic) i.next();
                    players.addAll(getPlayersWithRoles(topic, roleSIs));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    /**
     * Get all players of all associations of a given type in the topic.
     * 
     * @param topic
     * @param associationTypeSI
     * @return 
     * @throws org.wandora.topicmap.TopicMapException
     */
    public static List<Topic> getPlayers(Topic topic, String associationTypeSI) throws TopicMapException  {
        List<Topic> players = new ArrayList<Topic>();
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
     * 
     * @param topic
     * @param associationTypeSI
     * @param roleSI
     * @return 
     */
    public static Topic getFirstPlayer(Topic topic, String associationTypeSI, String roleSI) {
        try {
            List<Topic> players = getPlayers(topic, associationTypeSI, roleSI);
            if(!players.isEmpty()) return players.get(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * Get all player topics within associations of a given topic. Associations
     * are limited to a given association type and player to a given role.
     * 
     * @param topic
     * @param associationTypeSI
     * @param roleSI
     * @return
     * @throws TopicMapException 
     */
    public static List<Topic> getPlayers(Topic topic, String associationTypeSI, String roleSI)  throws TopicMapException {
        List<Topic> players = new ArrayList<Topic>();
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
    

    /**
     * Return a list of topics. List contains player topics within associations
     * of a given topic. Associations are limited with an association type 
     * and player with a role topic.
     * 
     * @param topic
     * @param associationType
     * @param role
     * @return
     * @throws TopicMapException 
     */
    public static List<Topic> getPlayers(Topic topic, Topic associationType, Topic role)  throws TopicMapException {
        List<Topic> players = new ArrayList<Topic>();
        if(topic != null && associationType != null && role != null) {
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
        return players;
    }
    
    
    /**
     * Get all players of associations of a given topics. Associations are
     * limited to a given type. Players are limited to a given role.
     * 
     * @param topics
     * @param associationTypeSI
     * @param roleSI
     * @return 
     */
    public static List<Topic> getPlayers(Collection topics, String associationTypeSI, String roleSI) {
        List<Topic> players = new ArrayList<Topic>();
        
        if(topics != null) {
            for(Iterator i = topics.iterator(); i.hasNext(); ) {
                try {
                    Topic topic = (Topic) i.next();
                    players.addAll(getPlayers(topic, associationTypeSI, roleSI));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return players;
    }
    
    
    /**
     * Get all players of a topic if next conditions apply:
     * Association type must be same as argument associationTypeSI.
     * Player's role must be same as argument roleSI.
     * Association must contain role-player pair that specified with arguments
     * hasRole and hasPlayer.
     * 
     * @param topic
     * @param associationTypeSI
     * @param roleSI
     * @param hasRole
     * @param hasPlayer
     * @return
     * @throws TopicMapException
     */
    public static List<Topic> getPlayers(Topic topic, String associationTypeSI, String roleSI, String hasRole, String hasPlayer) throws TopicMapException  {
        List<Topic> players = new ArrayList<Topic>();
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
    
    /**
     * Get all players of a topic if next conditions apply:
     * Association type must be same as argument type.
     * Player's role must be same as argument role.
     * Association must contain role-player pair that specified with arguments
     * hasRole and hasPlayer.
     * 
     * @param topic
     * @param type
     * @param role
     * @param hasRole
     * @param hasPlayer
     * @return
     * @throws TopicMapException
     */
    public static List<Topic> getPlayers(Topic topic, Topic type, Topic role, Topic hasRole, Topic hasPlayer)  throws TopicMapException {
        List<Topic> players = new ArrayList<Topic>();
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
    
    
    
    /**
     * Get all players of topics if next conditions apply:
     * Association type must be same as argument associationTypeSI.
     * Player's role must be same as argument roleSI.
     * Association must contain role-player pair that specified with arguments
     * hasRole and hasPlayer.
     * 
     * @param topics
     * @param associationTypeSI
     * @param roleSI
     * @param hasRole
     * @param hasPlayer
     * @return
     * @throws TopicMapException
     */
    public static List<Topic> getPlayers(Collection topics, String associationTypeSI, String roleSI, String hasRole, String hasPlayer)  throws TopicMapException {
        List<Topic> players = new ArrayList<Topic>();
        
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
    
    /**
     * Gets a sorted list of player topics of a given topic and association type
     * and role. Argument sortRole is used to pick up sorting player topic.
     * Argument lang is used to get sort name out of sorting player topic.
     *
     * @param topic
     * @param associationTypeSI
     * @param roleSI
     * @param sortRole
     * @param lang
     * @return
     * @throws TopicMapException
     */
    public static List<Topic> getSortedPlayers(Topic topic, String associationTypeSI, String roleSI, String sortRole, String lang) throws TopicMapException {
        List players = new ArrayList();
        List<Topic> sortedPlayers = new ArrayList<Topic>();
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
    
    
    
    /**
     * Gets a sorted list of player topics of a given topic and association types
     * and roles. Argument sortRole is used to pick up sorting player topic.
     * Argument lang is used to get sort name out of sorting player topic.
     * 
     * @param topic
     * @param associationTypesSI
     * @param rolesSI
     * @param sortRole
     * @param lang
     * @return
     * @throws TopicMapException
     */
    public static List<Topic> getSortedPlayers(Topic topic, Collection associationTypesSI, Collection rolesSI, String sortRole, String lang)  throws TopicMapException {
        List players = new ArrayList();
        List<Topic> sortedPlayers = new ArrayList<Topic>();
        
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
    
    
    
    /**
     * Returns a list of type topics of a given topic. List of type topics
     * is limited to topics that have them selves typed with requiredTypeSi.
     * 
     * @param topic
     * @param requiredTypeSI
     * @return
     */
    public static List<Topic> getTypesOfRequiredType(Topic topic, String requiredTypeSI) {
        List<Topic> selectedTypes = new ArrayList<Topic>();
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
    
    
    
    public static List<String> getSLsOfPlayers(Topic topic, String associationTypeSI, String roleSI) throws TopicMapException {
        return getSubjectLocatorsOfPlayers(topic, associationTypeSI, roleSI);
    }

    
    public static List<String> getSubjectLocatorsOfPlayers(Topic topic, String associationTypeSI, String roleSI) throws TopicMapException {
        List<Topic> players = getPlayers(topic, associationTypeSI, roleSI);
        List<String> locators = new ArrayList<String>();
        
        for(int i=0; i<players.size(); i++) {
            if(players.get(i) != null) {
                String sl = players.get(i).getSubjectLocator().toExternalForm();
                if(sl != null && sl.length() > 0) {
                    locators.add(sl);
                }
            }
        }
        return locators;
    }

    
    
    /**
     * Returns a list subject locators of given topics. Delegates
     * operation to method getSubjectLocatorsOfTopics.
     * 
     * @param topics
     * @return
     */
    public static List<String> getSLsOfTopics(Collection topics) {
        return getSubjectLocatorsOfTopics(topics);
    }
    
    /**
     * Returns a list subject locators of given topics. If a topic
     * has no subject locator, the list has no element for the topic.
     * 
     * @param topics
     * @return
     */
    public static List<String> getSubjectLocatorsOfTopics(Collection topics) {
        List<String> locators = new ArrayList<String>();
        if(topics != null) {
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
        }
        return locators;
    }
    
    
    
    /**
     * Return a collection of topics. Returned topics are edges of an association
     * chain. Association chain is topic path where one travels from a given topic
     * through given associations (association type, in-role and out-role). Edge
     * topic is found whenever one can not travel any further.
     * 
     * @param source
     * @param associationType
     * @param baseRole
     * @param outRole
     * @return 
     */
    public static Collection<Topic> getEdgeTopics(Collection<Topic> source, Topic associationType, Topic baseRole, Topic outRole) {
        List<Topic> rootTopics = new ArrayList<Topic>();
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
    
    
    /**
     * Return one edge topic.
     * 
     * @param base
     * @param associationType
     * @param baseRole
     * @param outRole
     * @return 
     */
    public static Topic getEdgeTopic(Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        List<Topic> pathTopics = new ArrayList<Topic>();
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
    
    
    
    
    /**
     * Returns one path (a list of topics) that is created traversing associations 
     * from a given start point to the end point.
     *
     * @param base
     * @param associationType
     * @param baseRole
     * @param outRole
     * @return
     */
    public static List<Topic> getSinglePath(Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        List<Topic> pathTopics = new ArrayList<Topic>();
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

    
    
    /**
     * Returns possible paths where one can traverse given associations 
     * back to the starting topic. 
     * 
     * @param base
     * @param associationType
     * @param baseRole
     * @param outRole
     * @return
     */
    public static List<List<Topic>> getCyclePaths(Topic base, Topic associationType, Topic baseRole, Topic outRole) {
        List<List<Topic>> cycles = new ArrayList<List<Topic>>();
        List<Topic> path = new ArrayList<Topic>();
        path.add(base);
        getCyclePaths(cycles, path, base, associationType, baseRole, outRole);
        return cycles;
    }



    
    private static void getCyclePaths(List<List<Topic>> cycles, List<Topic> path, Topic base, Topic associationType, Topic baseRole, Topic outRole) {
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
                           List<Topic> cycle = new ArrayList<Topic>();
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
                            List<Topic> clonedPath = new ArrayList<Topic>();
                            clonedPath.addAll(path);
                            getCyclePaths(cycles, clonedPath, base, associationType, baseRole, outRole);
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
    
    
    
    
    /**
     * Checks if given locator contains unwanted characters that make
     * the locator "dirty".
     *
     * @param l
     * @return
     */
    public static boolean isDirtyLocator(Locator l) {
        String s = l.toExternalForm();
        if(DataURL.isDataURL(s)) return false;
        for(int k=0; k<s.length(); k++) {
            if(isDirtyLocatorCharacter(s.charAt(k))) return true;
        }
        return false;
    }
    
 
    /**
     * Method isDirtyLocator uses this method to decide whether or not
     * a character is "dirty".
     * 
     * @param c
     * @return 
     */
    public static boolean isDirtyLocatorCharacter(char c) {
        if("1234567890poiuytrewqasdfghjklmnbvcxzPOIUYTREWQASDFGHJKLMNBVCXZ$-_.+!*'(),/?:=&#~;%".indexOf(c) != -1) return false;
        else return true;
    }
    
    
    public static String schars = "ÄÖÅäöå";
    public static String dchars = "AOAAoA";
    
    /**
     * Finds a suitable replacement character for some "dirty" characters.
     * 
     * @param c
     * @return 
     */
    public static char repacementLocatorCharacterFor(char c) {
        int i = schars.indexOf(c);
        if(i == -1) {
            if(c < 32) return 0;
            return '_';
        }
        else return dchars.charAt(i);
    }
    
    
    
    /**
     * Removes or replaces all "dirty" characters in the string.
     * 
     * @param s
     * @return 
     */
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
    /**
     * Returns a default locator that can be given to a topic and it
     * is very likely (but not sure) that the subject identifier doesn't
     * cause accidental merge. Default locator is created with a prefix
     * "http://wandora.org/si/temp/", timestamp and locator counter.
     * 
     * @return 
     */
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
    
    
    /**
     * Returns topic's base name if base name exists. Otherwise returns
     * topic's first subject identifier.
     * 
     * @param t
     * @return 
     */
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
    
    
    
    /**
     * Returns a flat string array that contains associations (including 
     * classes and instances) of a given topic. Every n*3:th element of
     * the string array is argument topic's name. Every n*3+1:th
     * element of the array is association type's name and
     * every n*3+2:th element of the array is associated topic's name.
     * Role topic's are not included in the array.
     * 
     * @param t
     * @return 
     */
    public static String[] getAssociationsAsTriplets(Topic t) {
        if(t == null) return null;
        List<String> triplets = new ArrayList<String>();
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
    
    

    /**
     * Returns a flat string array that contains associations (including classes
     * and instances) of a given topic. Associations are stored as type-player
     * pairs. Every odd element of the string array is type topic's name. Every
     * even element of the string array is player topic's name. Role topics
     * are not stored in the string array.
     * 
     * @param t
     * @return 
     */
    public static String[] getAssociationsAsTypePlayerTuples(Topic t) {
        if(t == null) return null;
        List<String> tuples = new ArrayList<String>();
   
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
    
    
    
    /**
     * Returns given association as a string array where first element
     * is association type and next elements association player topics.
     * Returned string array doesn't contain role topics.
     * 
     * @param a
     * @return 
     */
    public static String[] getAsTriplet(Association a) {
        if(a == null) return null;
        List<String> triplet = new ArrayList<String>();
 
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
    
    
    /**
     * Returns a list of topic map layers of given container topic map.
     * For example, Wandora's layer stack is a container topic map and
     * the method returns all topic maps in the layer stack.
     * 
     * @param tm
     * @return 
     */
    public static List<Layer> getLayers(ContainerTopicMap tm) {
        if (tm == null) return null;
        List<Layer> layers = new ArrayList<Layer>();
        
        for (Layer l : tm.getLayers()) {
            TopicMap ltm = l.getTopicMap();
            if(ltm instanceof ContainerTopicMap){
             layers.addAll(getLayers((ContainerTopicMap)ltm));   
            }
            layers.add(l);
        }
        return layers;
    }
    
    
    
    /**
     * Checks if a given topic map is a container topic map. Container
     * topic map can contain other topics maps. For example, Wandora's
     * layer stack is a container topic map.
     * 
     * @param tm
     * @return 
     */
    public static boolean isTopicMapContainer(TopicMap tm) {
        if(tm == null) {
            return false;
        }
        else {
            return tm instanceof ContainerTopicMap;
        }
    }
}
