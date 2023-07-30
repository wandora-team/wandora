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
 * TopicMap.java
 *
 * Created on June 10, 2004, 11:12 AM
 */

package org.wandora.topicmap;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.wandora.topicmap.parser.JTMParser;
import org.wandora.topicmap.parser.LTMParser;
import org.wandora.topicmap.parser.XTMAdaptiveParser;
import org.wandora.utils.Tuples.T2;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * <p>
 * The abstract topic map class that all topic map implementations must extend.
 * Simple primitive methods are left abstract and more complex methods have 
 * been implemented using the primitive methods. Some implementations may contain
 * data structures that could be used to make complex methods more efficient.
 * In this case it is advisable to override and reimplement them.
 * </p>
 * <p>
 * Usually any topic map implementation will also have its own topic and association
 * implementations. The createTopic and createAssociation methods are used to
 * create topics and associations instead of creating them with new operator.
 * </p>
 * <p>
 * Generally all methods that take topic or association parameters expect them to
 * be topics or associations belonging in this topic map. This means that you
 * can't get a topic from one topic map and then use it as a parameter of a method
 * in some other topic map. There are a few methods where this is allowed. Most
 * notably the copy methods that copy topics or associations from one topic map
 * in to another and a few methods that test if two topics would merge with
 * each other.
 * </p>
 * @author  olli, akivela
 */



public abstract class TopicMap implements TopicMapLogger {
    protected TopicMapLogger logger = null;
    protected boolean consistencyCheck = true;
    protected boolean consistencyCheckWhenXTMImport = false;
    protected boolean isReadOnly = false;
    
    public static final String EDITTIME_SI="http://wandora.org/si/core/edittime";

    protected TopicMap parentTopicMap;
        
    public Locator createLocator(String reference) throws TopicMapException {
        return createLocator("URI",reference);
    }
    public Locator createLocator(String notation,String reference) throws TopicMapException {
        return new Locator(notation,reference);
    }
    /**
     * Gets a topic with subject identifier.
     */
    public abstract Topic getTopic(Locator si) throws TopicMapException ;
    /**
     * Gets a topic with subject identifier. Subject identifier is given as a 
     * URI reference string instead of Locator.
     */
    public Topic getTopic(String si) throws TopicMapException {
        return getTopic(createLocator(si));
    }
    /**
     * Gets a topic with subject locator.
     */
    public abstract Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException ;
    /**
     * Gets a topic with subject locator. Subject locator is given as a 
     * URI reference string instead of Locator.
     */
    public Topic getTopicBySubjectLocator(String sl) throws TopicMapException {
        return getTopicBySubjectLocator(createLocator(sl));
    }
    /**
     * Creates a new topic without base name, subject identifier, data or any associations.
     */
    public abstract Topic createTopic(String id) throws TopicMapException ;
    /**
     * Creates a new topic without base name, subject identifier, data or any associations.
     */
    public abstract Topic createTopic() throws TopicMapException ;
    /**
     * Creates a new association of the given type without any members.
     */
    public abstract Association createAssociation(Topic type) throws TopicMapException ;
    /**
     * Gets all topics in the topic map that are of the given type.
     */
    public abstract Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException ;
    /**
     * Gets a topic with topic base name.
     */
    public abstract Topic getTopicWithBaseName(String name) throws TopicMapException ;
    /**
     * Gets all topics in the topic map. The topic map may contain a very large number
     * of topics and this operation may take a long time to finish even though it
     * returns an iterator instead of all topics in a collection.
     * 
     * Ideally this should return a TopicIterator with the special dispose method
     * instead of a generic Iterator. The interface remains specifies just Iterator
     * for backwards compatibility. See notes in TopicIterator.
     */
    public abstract Iterator<Topic> getTopics() throws TopicMapException ;
    /**
     * Gets the topics whose subject identifiers are given in the array.
     * Will always return
     * an array of equal size as the parameter array with null elements where
     * topics are not found with the given subject identifiers.
     */
    public abstract Topic[] getTopics(String[] sis) throws TopicMapException ;
    /**
     * Returns all associations in the topic map. The topic map may contain a very large
     * number of associations and this operation may take a long time to finish even
     * though it returns an iterator instead of all associations in a collection.
     */
    public abstract Iterator<Association> getAssociations() throws TopicMapException ;
    /**
     * Returns all associations in the topic map that are of the given type.
     * Usually topic maps will either contain none or a very large number of
     * associations of any type. Thus this method may potentially be very 
     * time consuming and require a very large amount of memory.
     */
    public abstract Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException ;
    
    /**
     * Returns the number of topics in topic map. For some implementations this
     * may be a very time consuming operation while for some it is a very simple
     * thing to do.
     */
    public abstract int getNumTopics() throws TopicMapException ;
    /**
     * Returns the number of associations in topic map. For some implementations
     * this may be a very time consuming operation while for some it is a very
     * simple thing to do.
     */
    public abstract int getNumAssociations() throws TopicMapException ;
    
    /**
     * Copies a topic of different topic map in this topic map. If deep is true then
     * topic types, variants and data are also copied. Only stubs of types, data types
     * and variant scopes are copied.
     */
    public abstract Topic copyTopicIn(Topic t,boolean deep) throws TopicMapException ;
    /**
     * Copies an association of a different topic map in this topic map. If a topic
     * related to the association does not exist in the topic map, then that topic
     * is copied with copyTopicIn(t,false). Otherwise the existing topic is used.
     * The existence of a topic is checked by trying to get it with one
     * (chosen arbitrarily) subject identifier.
     */
    public abstract Association copyAssociationIn(Association a) throws TopicMapException ;
    /**
     * Copies all associations of a topic of a different topic map in this topic
     * map using copyAssociationIn.
     */ 
    public abstract void copyTopicAssociationsIn(Topic t) throws TopicMapException ;
    
    /**
     * Merges the contents of the given topic map in this topic map. At least one of
     * mergeIn(TopicMap) or mergeIn(TopicMap,TopicMapLogger) must be overridden as
     * they call each other in default implementation.
     */
    public void mergeIn(TopicMap tm) throws TopicMapException {
        mergeIn(tm,this);
    }
    
    /**
     * Merges the contents of the given topic map in this topic map. At least one of
     * mergeIn(TopicMap) or mergeIn(TopicMap,TopicMapLogger) must be overridden as
     * they call each other in default implementation.
     */
    public void mergeIn(TopicMap tm,TopicMapLogger tmLogger) throws TopicMapException {
        mergeIn(tm);
    }
    
    /**
     * NOTE: The two trackDependent methods are mostly deprecated. They are used
     *       when caching html pages from topics to decide when cached pages need
     *       to be refreshed. A page needs to be updated when any topic visible
     *       in the page is changed. 
     */
    public abstract void setTrackDependent(boolean v) throws TopicMapException ;
    public abstract boolean trackingDependent() throws TopicMapException ;

    /**
     * Sets the topic map listener. Returns the old listener. Usually you will
     * either want to chain the listeners calling the old listener after you have
     * processed the event or restore the old listener later.
     */
//    public abstract TopicMapListener setTopicMapListener(TopicMapListener listener) ;
    
    /**
     * Adds a topic map listener. 
     */
    public abstract void addTopicMapListener(TopicMapListener listener) ;
    /**
     * Removes a topic map listener.
     */
    public abstract void removeTopicMapListener(TopicMapListener listener) ;
    
    /**
     *  Gets all topic map listeners.
     */
    public abstract List<TopicMapListener> getTopicMapListeners();
    
    public void addTopicMapListeners(List<TopicMapListener> listeners){
        for(TopicMapListener l : listeners){
            addTopicMapListener(l);
        }
    }
    
    public abstract void disableAllListeners();
    public abstract void enableAllListeners();
    
    

    
    /**
     * Checks if the topic map has been changed since the last call to resetTopicMapChanged.
     */
    public abstract boolean isTopicMapChanged() throws TopicMapException ;
    /**
     * @see #isTopicMapChanged()
     */
    public abstract boolean resetTopicMapChanged() throws TopicMapException ;
    /**
     * Searches the topic map for the given string. Search options are given in a
     * separate search options object.
     */
    public abstract Collection<Topic> search(String query, TopicMapSearchOptions options) throws TopicMapException ;
    
    public abstract TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException ;
    
    /**
     * Completely clears the topic map. This deletes all associations and topics
     * and all information related to them.
     */
    public abstract void clearTopicMap() throws TopicMapException;
    /**
     * If the topic map implementation keeps indexes or caches of topics, calling
     * this method will clear such data structures and cause further methods to
     * retrieve data directly from the original data source.
     * 
     * (NOTE: should probably be named clearTopicMapCache?)
     */
    public abstract void clearTopicMapIndexes() throws TopicMapException;
    
    /**
     * Checks if the topic map is in a read only state. This may be because the
     * topic map implementation only allows reading, editing requires special
     * privileges or a method has been called to set the topic map in read only
     * state. When a topic map is read only, all write operations throw
     * TopicMapReadOnlyException.
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }
    
    /**
     * Sets the topic map in a read-only or read-write state depending on the
     * argument. If argument is true, the topic map is set read-only. Notice,
     * some topic map implementations may support only one of the states. Topic
     * map may not support both states.
     */
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }
    
    /**
     * <p>
     * Set consistency check of associations on or off. This should normally be
     * turned on but you may turn it off before some big batch operations to
     * speed them up. After this operation you need to turn the consistency
     * check back on and manually call checkAssociationConsistency unless you
     * are sure that the operation did not cause any incensistencies with associations.
     * </p><p>
     * Note that
     * some implementations may not allow turning consistency check off so 
     * getConsistencyCheck may return a different value that the last parameter
     * used for setConsistencyCheck. If turning consistency check off is not
     * supported, then it is also not needed to manually call the
     * checkAssociationConsistency.
     * </p>
     * @see #checkAssociationConsistency(TopicMapLogger)
     */
    public void setConsistencyCheck(boolean value) throws TopicMapException {
        consistencyCheck=value;
    }
    public boolean getConsistencyCheck() throws TopicMapException {
        return consistencyCheck;
    }
    
    
    public void checkAssociationConsistency() throws TopicMapException {
        checkAssociationConsistency(getLogger());
    }
    
    /**
     * Checks association consistency and fixes any inconsistencies. Two
     * associations are said to be inconsistent if they have the same type and
     * same players with same roles, that is they represent the exact same
     * association. If several such associations exist all but one of them need
     * to be removed.
     */
    public void checkAssociationConsistency(TopicMapLogger logger) throws TopicMapException {

    }
    
    /**
     * Sets the parent of this topic map. 
     */
    public void setParentTopicMap(TopicMap parent){
        this.parentTopicMap=parent;
    }
    
    /**
     * Gets the parent of this topic map. Returns null if no parent has been specified.
     */
    public TopicMap getParentTopicMap(){
        return parentTopicMap;
    }
    
    /**
     * Gets the root topic map. This is done by iteratively getting the parent of
     * this topic map, and the parent of that and so on until getParentTopicMap
     * returns null.
     */
    public TopicMap getRootTopicMap(){
        TopicMap tm=this;
        while(true){
            TopicMap tm2=tm.getParentTopicMap();
            if(tm2==null) return tm;
            else tm=tm2;
        }
    }
    
    /**
     * Checks if this topic map is connected to the service providing the topic map.
     * Some topic maps that are stored locally will be always connected while
     * remotely hosted topic maps may sometime get disconnected.
     */ 
    public boolean isConnected() throws TopicMapException {
        return true;
    }
    
    
    /**
     * Close the topic map. Free resources used by the topic map,
     * the database connection, for example.
     */
    public abstract void close();
    
    
    /**
     * Gets topics with the subject identifiers in the collection given as
     * parameter. The parameter collection may contain subject identifiers as 
     * strings or locators or a mix of both. Returned collection will contain
     * each topic found with. Note that parameter collection and returned
     * collection may be of different size. This can happen when some subject
     * identifiers don't match any topics or some topic matches several of the
     * subject identifiers. Topics in the returned collection are not ordered
     * in any way and it will contain each topic only once.
     */
    // TODO: Equals check used in HashSet. May cause some topics to be included
    //       several times in the returned collection.
    public Collection<Topic> getTopics(Collection sis) throws TopicMapException {
        HashSet ret=new LinkedHashSet();
        Iterator iter=sis.iterator();
        while(iter.hasNext()){
            Object o=iter.next();
            Topic t=null;
            if(o instanceof Locator){
                t=getTopic((Locator)o);
            }
            else if(o instanceof String){
                t=getTopic((String)o);
            }
            if(t!=null) ret.add(t);
        }
        return ret;
    }
    
    /**
     * Gets all topics in the topic map that are of the given type. Parameter
     * is the subject identifier of the type topic. If such a topic is not
     * found in the topic map, will return an empty collection.
     */
    public Collection<Topic> getTopicsOfType(String si) throws TopicMapException {
        Topic t=getTopic(si);
        if(t!=null) return getTopicsOfType(t);
        else return new ArrayList();
    }
    
    /**
     * Like copyTopicIn but does it for a collection of topics.
     */ 
    public Collection<Topic> copyTopicCollectionIn(Collection topics,boolean deep) throws TopicMapException {
        HashSet copied=new LinkedHashSet();
        Iterator iter=topics.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            Topic nt=copyTopicIn(t,deep);
            copied.add(nt);
        }
        return copied;
    }
    
    /**
     * Gets all topics that would merge with the given topic. Note that usually
     * the topic given as a parameter will be a topic in a different topic map
     * because for any topic in this topic map you will get a result
     * containing only the topic you gave as a parameter.
     */
    public Collection<Topic> getMergingTopics(Topic t) throws TopicMapException {
        HashSet set=new LinkedHashSet();
        Collection<Locator> ls=t.getSubjectIdentifiers();
        if(ls!=null) {
            Iterator iter=ls.iterator();
            while(iter.hasNext()){
                Locator l=(Locator)iter.next();
                Topic to=getTopic(l);
                if(to!=null) set.add(to);
            }
        }
        Topic to;
        if(t.getSubjectLocator()!=null) {
            to=getTopicBySubjectLocator(t.getSubjectLocator());
            if(to!=null) set.add(to);
        }
        if(t.getBaseName()!=null){
            to=getTopicWithBaseName(t.getBaseName());
            if(to!=null) set.add(to);
        }
        return set;
    }

    
    
    // ---------------------------------------------------- TOPIC MAP LOGGER ---
    
    
    public void setLogger(TopicMapLogger logger) {
        this.logger = logger;
    }
    public TopicMapLogger getLogger() {
        return this.logger;
    }
    
    
    public void hlog(String message) {
        if(logger != null) logger.hlog(message);
        else {
            System.out.println(message);
        }
    }
    public void log(String message) {
        if(logger != null) logger.log(message);
        else {
            System.out.println(message);
        }
    }
    public void log(String message, Exception e) {
        if(logger != null) logger.log(message, e);
        else {
            System.out.println(message);
            e.printStackTrace();
        }
    }
    public void log(Exception e) {
        if(logger != null) logger.log(e);
        else {
            e.printStackTrace();
        }
    }

    public void setProgress(int n) {
        if(logger != null) logger.setProgress(n);
    }
    public void setProgressMax(int maxn) {
        if(logger != null) logger.setProgressMax(maxn);
    }
    
    public void setLogTitle(String title) {
        if(logger != null) logger.setLogTitle(title);
        else {
            System.out.println(title);
        }
    }
    
    
    public boolean forceStop() {
        if(logger != null) return logger.forceStop();
        else {
            return false;
        }
    }
    
    
    // ----------------------------------------------------- IMPORT / EXPORT ---
    
    
    
    public void importTopicMap(String file) throws IOException,TopicMapException {
        importTopicMap(file, this);
    }
    public void importTopicMap(String file, TopicMapLogger logger) throws IOException,TopicMapException {
        importTopicMap(file, this, false);
    }
    public void importTopicMap(String file, TopicMapLogger logger, boolean checkConsistency) throws IOException,TopicMapException {
        if(file != null) {
            if(file.toLowerCase().endsWith("ltm")) importLTM(file, logger);
            else if(file.toLowerCase().endsWith("jtm")) importJTM(file, logger);
            else importXTM(file, logger);
        }
    }
    
        
    public void exportTopicMap(String file) throws IOException, TopicMapException  {
        exportTopicMap(file, this);
    }

    public void exportTopicMap(String file, TopicMapLogger logger) throws IOException, TopicMapException  {
        if(file != null) {
            String lfile = file.toLowerCase();
            if(lfile.endsWith("ltm")) exportLTM(file, logger);
            else if(lfile.endsWith("jtm")) exportJTM(file, logger);
            else if(lfile.endsWith("xtm10")) exportXTM10(file, logger);
            else if(lfile.endsWith("xtm1")) exportXTM10(file, logger);
            else if(lfile.endsWith("xt1")) exportXTM10(file, logger);
            else exportXTM(file, logger);
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- LTM ---
    // -------------------------------------------------------------------------
    
    public void exportLTM(String file) throws IOException, TopicMapException  {
        exportLTM(file, this);
    }
    
    public void exportLTM(String file, TopicMapLogger logger) throws IOException, TopicMapException  {
        FileOutputStream fos=new FileOutputStream(file);
        exportLTM(fos, logger);
        fos.close();
    }
    
    public void exportLTM(OutputStream out) throws IOException, TopicMapException {
        exportLTM(out, this);
    }
    
    
    public void exportLTM(OutputStream out, TopicMapLogger logger) throws IOException, TopicMapException {
        if(logger == null) logger = this;
        PrintWriter writer=new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
        writer.println("@\"utf-8\"");
        int totalCount = this.getNumTopics() + this.getNumAssociations();
        logger.setProgressMax(totalCount);
        int count = 0;
        Iterator iter=getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t == null || t.isRemoved()) continue;
            logger.setProgress(count++);
            writer.print("[ "+makeLTMTopicId(t));
            if(t.getTypes().size()>0){
                Iterator iter2=t.getTypes().iterator();
                if(iter2.hasNext()) writer.print(" :");
                while(iter2.hasNext()){
                    Topic t2=(Topic)iter2.next();
                    writer.print(" "+makeLTMTopicId(t2));
                }
            }
            if(t.getBaseName()!=null || t.getVariantScopes().size()>0){
                writer.print(" = ");
                if(t.getBaseName()==null)
                    writer.print("");
                else
                    writer.print("\"" + makeLTMString(t.getBaseName()) + "\"");
                Iterator iter2=t.getVariantScopes().iterator();
                while(iter2.hasNext()) {
                    Set c=(Set)iter2.next();
                    String name=t.getVariant(c);
                    if(c.size()>0) {
                        writer.print(" (");
                        Iterator iter3=c.iterator();
                        writer.print(" \""+makeLTMString(name)+"\"");
                        if(iter3.hasNext()) {
                            writer.print(" /");
                        }
                        while(iter3.hasNext()) {
                            Topic st=(Topic)iter3.next();
                            writer.print(" "+makeLTMTopicId(st));
                        }
                        writer.print(" )");
                    }
                }
            }
            if(t.getSubjectIdentifiers().size()>0 || t.getSubjectLocator()!=null) {
                if(t.getSubjectLocator()!=null) {
                    writer.print(" %\"" + t.getSubjectLocator().toExternalForm() + "\"");
                }
                Iterator iter2=t.getSubjectIdentifiers().iterator();
                while(iter2.hasNext()) {
                    Locator l=(Locator)iter2.next();
                    if(l != null) {
                        writer.print(" @\"" + l.toExternalForm() + "\"");
                    }
                }
            }
            writer.println(" ]");

            if(t.getDataTypes().size()>0) {
                Collection types=t.getDataTypes();
                Iterator iter2=types.iterator();
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    Hashtable ht=(Hashtable)t.getData(type);
                    Iterator iter3=ht.entrySet().iterator();
                    while(iter3.hasNext()){
                        Map.Entry e=(Map.Entry)iter3.next();
                        Topic version=(Topic)e.getKey();
                        String data=(String)e.getValue();
                        writer.print("{ "+makeLTMTopicId(t) );
                        writer.print(", "+makeLTMTopicId(type));
                        writer.print(", [[" + makeLTMString(data) +"]]");
                        writer.println(" } / "+makeLTMTopicId(version));
                    }
                }
            }
        }
        if(!logger.forceStop()) {
            iter=getAssociations();
            while(iter.hasNext() && !logger.forceStop()) {
                logger.setProgress(count++);
                Association a=(Association)iter.next();
                if(a.getType()!=null) {
                    writer.print( makeLTMTopicId(a.getType()) + " " );
                }
                writer.print("( ");            
                Iterator iter2=a.getRoles().iterator();
                while(iter2.hasNext()) {
                    Topic role=(Topic)iter2.next();
                    writer.print(makeLTMTopicId( a.getPlayer(role) ) + " : " + makeLTMTopicId( role ));
                    if(iter2.hasNext()) writer.print(", ");
                }
                writer.println(" )");
            }
        }
        writer.flush();
    }
    
    
    public String makeLTMString(String str) {
        if(str != null && str.length() > 0) {
            str = str.replace("\\", "\\u005C" );
            str = str.replace("\"", "\\u0022" );
            str = str.replace("[",  "\\u005B" );
            str = str.replace("]",  "\\u005D" );
            str = str.replace("'",  "\\u0027" );
            str = str.replace("{",  "\\u007B" );
            str = str.replace("}",  "\\u007D" );

            // ***** ESCAPE ALL CONTROL CODES AND NON-ASCII CHARACTERS
            StringBuilder strBuffer = new StringBuilder("");
            int c = -1;
            for(int i=0; i<str.length(); i++) {
                c = str.charAt(i);
                if(c<31 || c>127) {
                    String cStr = Integer.toHexString(c);
                    while(cStr.length()<4) {
                        cStr = "0"+cStr;
                    }
                    strBuffer.append("\\u").append(cStr);
                }
                else {
                    strBuffer.append((char) c);
                }
            }
            str = strBuffer.toString();
        }
        return str;
    }
    public String makeLTMTopicId(Topic t) {
        if(t == null) return "null";
        try {
            return t.getID();
            // int h = t.getID().hashCode();
            // int sign = h / Math.abs(h);
            // h = Math.abs( 2 * h );
            // if(sign == -1) h = h + 1;
            // return "t"+h;
        }
        catch(Exception e) {
            return "null";
        }
    }
    
    
    
    public void importLTM(InputStream in) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importLTM(in, this);
    }
    public void importLTM(File inFile) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importLTM(inFile, this);
    }
    public void importLTM(String file) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importLTM(file, this);
    }
    public void importLTM(InputStream in, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        LTMParser parser = new LTMParser(this, logger);
        parser.parse(in);
        parser.init();
    }
    public void importLTM(File inFile, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(logger == null) logger = this;
        logger.log("Merging LTM file");
        LTMParser parser = new LTMParser(this, logger);
        parser.parse(inFile);
        parser.init();
    }
    public void importLTM(String fileName, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        File file=new File(fileName);
        importLTM(file, logger);
    }



    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- JTM ---
    // -------------------------------------------------------------------------

    public void exportJTM(String file) throws IOException, TopicMapException  {
        exportJTM(file, this);
    }

    public void exportJTM(String file, TopicMapLogger logger) throws IOException, TopicMapException  {
        FileOutputStream fos=new FileOutputStream(file);
        exportJTM(fos, logger);
        fos.close();
    }

    public void exportJTM(OutputStream out) throws IOException, TopicMapException {
        exportJTM(out, this);
    }


    public void exportJTM(OutputStream out, TopicMapLogger logger) throws IOException, TopicMapException {
        if(logger == null) logger = this;

        PrintWriter writer=new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
        ArrayList<T2<Topic,Topic>> typeAssociations = new ArrayList<T2<Topic,Topic>>();
        int numberOfTopics = this.getNumTopics();
        int numberOfAssociations = this.getNumAssociations();
        int totalCount = numberOfTopics + numberOfAssociations;
        logger.setProgressMax(totalCount);
        int count = 0;
        writer.println("{\"version\":\"1.0\",");
        writer.print(" \"item_type\":\"topicmap\"");
        if(totalCount > 0) writer.println(",");
        if(numberOfTopics>0) {
            writer.println(" \"topics\":[");
        }
        Iterator<Topic> topics=getTopics();
        while(topics.hasNext() && !logger.forceStop()) {
            Topic t=topics.next();
            if(t == null || t.isRemoved()) continue;
            logger.setProgress(count++);
            writer.println("  {");
            if(t.getSubjectIdentifiers().size()>0) {
                writer.println("   \"subject_identifiers\":[");
                Iterator iter2=t.getSubjectIdentifiers().iterator();
                while(iter2.hasNext()) {
                    Locator l=(Locator)iter2.next();
                    if(l != null) {
                        writer.print("    \"" + l.toExternalForm() + "\"");
                        if(iter2.hasNext()) {
                            writer.println(", ");
                        }
                        else {
                            writer.println();
                        }
                    }
                }
                writer.print("   ]");
            }

            if(t.getSubjectLocator()!=null) {
                writer.println(",");
                if(t.getSubjectLocator()!=null) {
                    writer.print("   \"subject_locators\": [ \"" + t.getSubjectLocator().toExternalForm() + "\" ]");
                }
            }
            
            if(t.getBaseName()!=null || t.getVariantScopes().size()>0) {
                writer.println(",");
                writer.println("   \"names\":[");
                writer.println("    {");
                if(t.getBaseName()==null)
                    writer.print("     \"value\":\"\"");
                else
                    writer.print("     \"value\":\"" + makeJTMString(t.getBaseName()) + "\"");
                boolean hasVariants = false;
                Iterator variants=t.getVariantScopes().iterator();
                if(variants.hasNext()) {
                    writer.println(",\n     \"variants\":[");
                    hasVariants = true;
                }
                else writer.println();
                while(variants.hasNext()) {
                    Set c=(Set)variants.next();
                    String name=t.getVariant(c);
                    if(c.size()>0) {
                        writer.println("      {");
                        writer.println("       \"value\":\""+makeJTMString(name)+"\",");
                        Iterator variantScopes=c.iterator();
                        if(variantScopes.hasNext()) {
                            writer.println("       \"scope\":[");
                        }
                        while(variantScopes.hasNext()) {
                            Topic st=(Topic)variantScopes.next();
                            writer.print("        \"si:"+st.getOneSubjectIdentifier().toExternalForm()+"\"");
                            if(variantScopes.hasNext()) {
                                writer.println(", ");
                            }
                            else {
                                writer.println("");
                                writer.println("       ]");
                            }
                        }
                        writer.print("      }");
                        if(variants.hasNext()) {
                            writer.println(",");
                        }
                        else {
                            writer.println();
                        }
                    }
                }
                if(hasVariants) {
                    writer.println("     ]");
                }
                writer.println("    }");
                writer.print("   ]");
            }

            if(t.getDataTypes().size()>0) {
                writer.println(",");
                writer.println("   \"occurrences\":[");
                Collection types=t.getDataTypes();
                Iterator iter2=types.iterator();
                boolean colonRequired = false;
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    Hashtable ht=(Hashtable)t.getData(type);
                    Iterator iter3=ht.entrySet().iterator();
                    while(iter3.hasNext()) {
                        if(colonRequired) writer.println(",");
                        writer.println("    {");
                        Map.Entry e=(Map.Entry)iter3.next();
                        Topic version=(Topic)e.getKey();
                        String data=(String)e.getValue();
                        writer.println("     \"value\":\""+makeJTMString(data)+"\",");
                        writer.println("     \"type\":\"si:"+type.getOneSubjectIdentifier().toExternalForm()+"\",");
                        writer.println("     \"scope\": [ \"si:"+version.getOneSubjectIdentifier().toExternalForm()+"\" ]");
                        writer.print("    }");
                        colonRequired = true;
                    }
                }
                writer.println();
                writer.println("   ]");
            }
            else {
                writer.println();
            }
            
            
            if(t.getTypes().size()>0){
                Iterator<Topic> iter2=t.getTypes().iterator();
                while(iter2.hasNext()) {
                    Topic t2=iter2.next();
                    if(t2 != null && !t2.isRemoved()) {
                        typeAssociations.add(new T2(t2, t));
                    }                
                }
            }
            
            writer.print("  }");
            if(topics.hasNext()) {
                writer.println(",");
            }
            else {
                writer.println();
            }
        }
        if(numberOfTopics>0) {
            writer.print(" ]");
        }
        
        if(!logger.forceStop()) {
            if(numberOfAssociations>0 || typeAssociations.size()>0) {
                writer.println(",");
                writer.println(" \"associations\":[");
            }
            else {
                writer.println();
            }
            Iterator associations=getAssociations();
            
            T2<Topic,Topic> typeAssociation = null;
            for(Iterator<T2<Topic,Topic>> types = typeAssociations.iterator(); types.hasNext() && !logger.forceStop(); ) {
                typeAssociation = types.next();
                Topic type = typeAssociation.e1;
                Topic instance = typeAssociation.e2;
                writer.println("  {");
                writer.println("   \"type\":\"si:http://psi.topicmaps.org/iso13250/model/type-instance\",");
                writer.println("   \"roles\":[");
                writer.println("    {");
                writer.println("     \"player\":\"si:"+type.getOneSubjectIdentifier().toExternalForm()+"\",");
                writer.println("     \"type\":\"si:http://psi.topicmaps.org/iso13250/model/type\"");
                writer.println("    },");
                writer.println("    {");
                writer.println("     \"player\":\"si:"+instance.getOneSubjectIdentifier().toExternalForm()+"\",");
                writer.println("     \"type\":\"si:http://psi.topicmaps.org/iso13250/model/instance\"");
                writer.println("    }");
                writer.println("   ]");
                writer.print("  }");
                
                if(types.hasNext() || associations.hasNext()) {
                    writer.println(",");
                }
                else {
                    writer.println();
                }
            }
            
            while(associations.hasNext() && !logger.forceStop()) {               
                logger.setProgress(count++);
                writer.println("  {");
                Association a=(Association)associations.next();
                if(a.getType()!=null) {
                    writer.println("   \"type\":\"si:"+a.getType().getOneSubjectIdentifier().toExternalForm()+"\",");
                }
                Iterator roles=a.getRoles().iterator();
                if(roles.hasNext()) {
                    writer.println("   \"roles\":[");
                }
                while(roles.hasNext()) {
                    writer.println("    {");
                    Topic role=(Topic)roles.next();
                    writer.println("     \"player\":\"si:"+a.getPlayer(role).getOneSubjectIdentifier().toExternalForm()+"\",");
                    writer.println("     \"type\":\"si:"+role.getOneSubjectIdentifier().toExternalForm()+"\"");
                    writer.print("    }");
                    if(roles.hasNext()) {
                        writer.println(",");
                    }
                    else {
                        writer.println();
                        writer.println("   ]");
                    }
                }
                writer.print("  }");
                if(associations.hasNext()) {
                    writer.println(",");
                }
                else {
                    writer.println();
                }
            }
            if(numberOfAssociations>0 || typeAssociations.size()>0) {
                writer.println(" ]");
            }
        }
        writer.println("}");
        writer.flush();
    }





    public String makeJTMString(String str) {
        if(str != null && str.length() > 0) {
            //str = str.replace("\n", "\\u000a" );
            //str = str.replace("\r", "\\u000c" );
            str = str.replace("\\", "\\u005c" );
            str = str.replace("\"", "\\u0022" );
            str = str.replace("[",  "\\u005b" );
            str = str.replace("]",  "\\u005d" );
            str = str.replace("'",  "\\u0027" );
            str = str.replace("{",  "\\u007b" );
            str = str.replace("}",  "\\u007d" );

            // ***** ESCAPE ALL CONTROL CODES AND NON-ASCII CHARACTERS
            StringBuilder strBuffer = new StringBuilder("");
            int c = -1;
            for(int i=0; i<str.length(); i++) {
                c = str.charAt(i);
                if(c<32 || c>126) {
                    String cStr = Integer.toHexString(c);
                    while(cStr.length()<4) {
                        cStr = "0"+cStr;
                    }
                    strBuffer.append("\\u").append(cStr);
                }
                else {
                    strBuffer.append((char) c);
                }
            }
            str = strBuffer.toString();
        }
        return str;
    }


    public String makeJTMTopicId(Topic t) {
        if(t == null) return "null";
        try {
            return t.getID();
        }
        catch(Exception e) {
            return "null";
        }
    }

    public void importJTM(InputStream in) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importJTM(in, this);
    }
    public void importJTM(File inFile) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importJTM(inFile, this);
    }
    public void importJTM(String file) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importJTM(file, this);
    }
    public void importJTM(InputStream in, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        JTMParser parser = new JTMParser(this, logger);
        parser.parse(in);
        //logger.log("JTM support not available yet!");
    }
    public void importJTM(File inFile, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(logger == null) logger = this;
        //logger.log("JTM support not available yet!");
        JTMParser parser = new JTMParser(this, logger);
        parser.parse(inFile);
    }
    public void importJTM(String fileName, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        File file=new File(fileName);
        importJTM(file, logger);
    }




    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- XTM ---
    // -------------------------------------------------------------------------
    
    public void exportXTM(OutputStream out) throws IOException, TopicMapException {
        exportXTM20(out, this);
    }
    
    public void exportXTM(OutputStream out, TopicMapLogger logger) throws IOException, TopicMapException {
        exportXTM20(out,logger);
    }

    public void exportXTM10(OutputStream out) throws IOException, TopicMapException {
        exportXTM10(out, this);
    }
    
    public void exportXTM10(OutputStream out, TopicMapLogger logger) throws IOException, TopicMapException {
        if(logger == null) logger = this;
        int totalCount = this.getNumTopics() + this.getNumAssociations();
        logger.setProgressMax(totalCount);
        int count = 0;
        PrintWriter writer=new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<topicMap xmlns=\"http://www.topicmaps.org/xtm/1.0/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
        Iterator iter=getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            logger.setProgress(count++);
            writer.println("\t<topic id=\""+t.getID()+"\">");
            if(t.getTypes().size()>0){
                Iterator iter2=t.getTypes().iterator();
                while(iter2.hasNext()){
                    Topic t2=(Topic)iter2.next();
                    writer.println("\t\t<instanceOf>");
                    writer.println("\t\t\t<topicRef xlink:href=\"#"+t2.getID()+"\"/>");
                    writer.println("\t\t</instanceOf>");
                }
            }
            if(t.getSubjectIdentifiers().size()>0 || t.getSubjectLocator()!=null){
                writer.println("\t\t<subjectIdentity>");
                Iterator iter2=t.getSubjectIdentifiers().iterator();
                while(iter2.hasNext()){
                    Locator l=(Locator)iter2.next();
                    writer.println("\t\t\t<subjectIndicatorRef xlink:href=\""+escapeXML(l.toExternalForm())+"\"/>");
                }
                if(t.getSubjectLocator()!=null) writer.println("\t\t\t<resourceRef xlink:href=\""+escapeXML(t.getSubjectLocator().toExternalForm())+"\"/>");
                writer.println("\t\t</subjectIdentity>");
            }
            if(t.getBaseName()!=null || t.getVariantScopes().size()>0){
                writer.println("\t\t<baseName>");
                if(t.getBaseName()==null)
                    writer.println("\t\t\t<baseNameString></baseNameString>");
                else
                    writer.println("\t\t\t<baseNameString>"+escapeXML(t.getBaseName())+"</baseNameString>");
                Iterator iter2=t.getVariantScopes().iterator();
                while(iter2.hasNext()){
                    Set c=(Set)iter2.next();
                    String name=t.getVariant(c);
                    if(c.size()>0){
                        writer.println("\t\t\t<variant>");
                        writer.println("\t\t\t\t<parameters>");
                        Iterator iter3=c.iterator();
                        while(iter3.hasNext()){
                            Topic st=(Topic)iter3.next();
                            writer.println("\t\t\t\t\t<topicRef xlink:href=\"#"+st.getID()+"\"/>");
                        }
                        writer.println("\t\t\t\t</parameters>");
                        writer.println("\t\t\t\t<variantName>");
                        writer.println("\t\t\t\t\t<resourceData>"+escapeXML(name)+"</resourceData>");
                        writer.println("\t\t\t\t</variantName>");
                        writer.println("\t\t\t</variant>");
                    }
                }
                writer.println("\t\t</baseName>");
            }
            if(t.getDataTypes().size()>0){
                Collection types=t.getDataTypes();
                Iterator iter2=types.iterator();
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    Hashtable ht=(Hashtable)t.getData(type);
                    Iterator iter3=ht.entrySet().iterator();
                    while(iter3.hasNext()){
                        Map.Entry e=(Map.Entry)iter3.next();
                        Topic version=(Topic)e.getKey();
                        String data=(String)e.getValue();
                        writer.println("\t\t<occurrence>");
                        writer.println("\t\t\t<instanceOf>");
                        writer.println("\t\t\t\t<topicRef xlink:href=\"#"+type.getID()+"\"/>");
                        writer.println("\t\t\t</instanceOf>");
                        writer.println("\t\t\t<scope>");
                        writer.println("\t\t\t\t<topicRef xlink:href=\"#"+version.getID()+"\"/>");
                        writer.println("\t\t\t</scope>");
                        writer.println("\t\t\t<resourceData>"+escapeXML(data)+"</resourceData>");
                        writer.println("\t\t</occurrence>");
                    }
                }
            }
            writer.println("\t</topic>");
        }
        if(!logger.forceStop()) {
            iter=getAssociations();
            while(iter.hasNext() && !logger.forceStop()) {
                logger.setProgress(count++);
                Association a=(Association)iter.next();
                writer.println("\t<association>");
                if(a.getType()!=null){
                    writer.println("\t\t<instanceOf>");
                    writer.println("\t\t\t<topicRef xlink:href=\"#"+a.getType().getID()+"\"/>");
                    writer.println("\t\t</instanceOf>");
                }
                Iterator iter2=a.getRoles().iterator();
                while(iter2.hasNext()){
                    Topic role=(Topic)iter2.next();
                    writer.println("\t\t<member>");
                    writer.println("\t\t\t<roleSpec>");
                    writer.println("\t\t\t\t<topicRef xlink:href=\"#"+role.getID()+"\"/>");
                    writer.println("\t\t\t</roleSpec>");
                    writer.println("\t\t\t<topicRef xlink:href=\"#"+(a.getPlayer(role).getID())+"\"/>");                
                    writer.println("\t\t</member>");
                }
                writer.println("\t</association>");            
            }
        }
        writer.println("</topicMap>");
        writer.flush();
    }
    
    

    public void exportXTM20(OutputStream out, TopicMapLogger logger) throws IOException, TopicMapException {
        if(logger == null) logger = this;
        int totalCount = this.getNumTopics() + this.getNumAssociations();
        logger.setProgressMax(totalCount);
        int count = 0;
        PrintWriter writer=new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<topicMap xmlns=\"http://www.topicmaps.org/xtm/\" version=\"2.0\">");
        Iterator iter=getTopics();
        while(iter.hasNext() && !logger.forceStop()) {
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            logger.setProgress(count++);
            writer.println("\t<topic id=\""+t.getID()+"\">");
            
            if(t.getSubjectLocator()!=null) writer.println("\t\t<subjectLocator href=\""+escapeXML(t.getSubjectLocator().toExternalForm())+"\"/>");
            if(t.getSubjectIdentifiers().size()>0 || t.getSubjectLocator()!=null){
                Iterator iter2=t.getSubjectIdentifiers().iterator();
                while(iter2.hasNext()){
                    Locator l=(Locator)iter2.next();
                    writer.println("\t\t<subjectIdentifier href=\""+escapeXML(l.toExternalForm())+"\"/>");
                }
            }
            
            if(t.getTypes().size()>0){
                Iterator iter2=t.getTypes().iterator();
                writer.println("\t\t<instanceOf>");
                while(iter2.hasNext()){
                    Topic t2=(Topic)iter2.next();
                    writer.println("\t\t\t<topicRef href=\"#"+t2.getID()+"\"/>");
                }
                writer.println("\t\t</instanceOf>");
            }

            if(t.getBaseName()!=null || t.getVariantScopes().size()>0){
                writer.println("\t\t<name>");
                if(t.getBaseName()==null)
                    writer.println("\t\t\t<value></value>");
                else 
                    writer.println("\t\t\t<value>"+escapeXML(t.getBaseName())+"</value>");
                Iterator iter2=t.getVariantScopes().iterator();
                while(iter2.hasNext()){
                    Set c=(Set)iter2.next();
                    String name=t.getVariant(c);
                    if(c.size()>0){
                        writer.println("\t\t\t<variant>");
                        writer.println("\t\t\t\t<scope>");
                        Iterator iter3=c.iterator();
                        while(iter3.hasNext()){
                            Topic st=(Topic)iter3.next();
                            writer.println("\t\t\t\t\t<topicRef href=\"#"+st.getID()+"\"/>");
                        }
                        writer.println("\t\t\t\t</scope>");
                        writer.println("\t\t\t\t<resourceData>"+escapeXML(name)+"</resourceData>");
                        writer.println("\t\t\t</variant>");
                    }
                }
                writer.println("\t\t</name>");
            }
            if(t.getDataTypes().size()>0){
                Collection types=t.getDataTypes();
                Iterator iter2=types.iterator();
                while(iter2.hasNext()){
                    Topic type=(Topic)iter2.next();
                    Hashtable ht=(Hashtable)t.getData(type);
                    Iterator iter3=ht.entrySet().iterator();
                    while(iter3.hasNext()){
                        Map.Entry e=(Map.Entry)iter3.next();
                        Topic version=(Topic)e.getKey();
                        String data=(String)e.getValue();
                        writer.println("\t\t<occurrence>");
                        writer.println("\t\t\t<type>");
                        writer.println("\t\t\t\t<topicRef href=\"#"+type.getID()+"\"/>");
                        writer.println("\t\t\t</type>");
                        writer.println("\t\t\t<scope>");
                        writer.println("\t\t\t\t<topicRef href=\"#"+version.getID()+"\"/>");
                        writer.println("\t\t\t</scope>");
                        writer.println("\t\t\t<resourceData>"+escapeXML(data)+"</resourceData>");
                        writer.println("\t\t</occurrence>");
                    }
                }
            }
            writer.println("\t</topic>");
        }
        if(!logger.forceStop()) {
            iter=getAssociations();
            while(iter.hasNext() && !logger.forceStop()) {
                logger.setProgress(count++);
                Association a=(Association)iter.next();
                writer.println("\t<association>");
                if(a.getType()!=null){
                    writer.println("\t\t<type>");
                    writer.println("\t\t\t<topicRef href=\"#"+a.getType().getID()+"\"/>");
                    writer.println("\t\t</type>");
                }
                Iterator iter2=a.getRoles().iterator();
                while(iter2.hasNext()){
                    Topic role=(Topic)iter2.next();
                    writer.println("\t\t<role>");
                    writer.println("\t\t\t<type>");
                    writer.println("\t\t\t\t<topicRef href=\"#"+role.getID()+"\"/>");
                    writer.println("\t\t\t</type>");
                    writer.println("\t\t\t<topicRef href=\"#"+(a.getPlayer(role).getID())+"\"/>");                
                    writer.println("\t\t</role>");
                }
                writer.println("\t</association>");            
            }
        }
        writer.println("</topicMap>");
        writer.flush();
    }
    
    
    public void importXTM(InputStream in) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importXTM(in, this);
    }
    
    public void importXTM(InputStream in, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importXTM(in, logger, consistencyCheckWhenXTMImport);
    }
    public void importXTM(InputStream in, TopicMapLogger logger, boolean checkConsistency) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(logger == null) logger = this;
        boolean oldCheck = getConsistencyCheck();
        if(checkConsistency != oldCheck) {
            logger.log("Changing consistency check to '"+checkConsistency +"'.");
            setConsistencyCheck(checkConsistency);
        }
        try {
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            XTMParser xtm1parser = new XTMParser(logger);
            // adaptive parser either uses xtm 1.0 or 2.0 parser depending on the version attribute
            XTMAdaptiveParser parserHandler=new XTMAdaptiveParser(this,logger,xtm1parser);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);

            reader.parse(new InputSource(in));
            
          // Debugging            
/*            Iterator iter=getTopics();
            while(iter.hasNext()){
                Topic t=(Topic)iter.next();
                if(t.getSubjectIdentifiers().size()==0) System.out.println("Parsed topic doesn't have subject identifiers");
//                else System.out.println(t.getSubjectIdentifiers().iterator().next());
            }*/
        }
        catch(org.xml.sax.SAXParseException se) {
            logger.log("Position "+se.getLineNumber()+":"+se.getColumnNumber(), se);
        }
        catch(org.xml.sax.SAXException saxe) {
            if(! "user_interrupt".equals(saxe.getMessage())) {
                logger.log(saxe);
            }
        }
        catch(Exception e){
            logger.log(e);
        }
        finally {
            if(checkConsistency != oldCheck) {
                logger.log("Restoring consistency check to '"+oldCheck+"'.");
                setConsistencyCheck(oldCheck);
            }
        }
    }
    
    
    
    public void importXTM(String file, TopicMapLogger logger) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        FileInputStream fis=new FileInputStream(file);
        importXTM(fis, logger);
        fis.close();
    }
    public void importXTM(String file) throws IOException, TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        importXTM(file, this);
    }
    
    public void exportXTM(String file) throws IOException, TopicMapException {
        exportXTM(file, this);
    }
    public void exportXTM20(String file) throws IOException, TopicMapException {
        exportXTM(file, this);
    }
    public void exportXTM(String file, TopicMapLogger logger) throws IOException, TopicMapException {
        FileOutputStream fos=new FileOutputStream(file);
        exportXTM(fos, logger);
        fos.close();
    }
    public void exportXTM10(String file, TopicMapLogger logger) throws IOException, TopicMapException {
        FileOutputStream fos=new FileOutputStream(file);
        exportXTM10(fos, logger);
        fos.close();
    }
    
    /**
     * Makes a Locator that can be used as subject identifier or locator. The locator
     * is guaranteed to be unique and will not cause merges with other topics
     * in this topic map.
     */
    public Locator makeSubjectIndicatorAsLocator() throws TopicMapException {
        String s=makeSubjectIndicator();
        return createLocator(s);
    }
    
    private int SICounter = 0;
    /**
     * Makes a URI that can be used as subject identifier or locator. The URI
     * is guaranteed to be unique and will not cause merges with other topics
     * in this topic map.
     */
    public String makeSubjectIndicator() {
        String si="http://wandora.org/si/temp/";
        si += System.currentTimeMillis();
        si += "-" + SICounter;
        if( SICounter++ > 10000000 ) SICounter = 0;
        return si;
    }
    
    public String escapeXML(String data){
        if(data.indexOf('&')>=0) data=data.replaceAll("&","&amp;");
        if(data.indexOf('<')>=0) data=data.replaceAll("<","&lt;");
        if(data.indexOf('"')>=0) data=data.replaceAll("\"","&#34;");
        StringBuilder buf=new StringBuilder(data);
        for(int i=0;i<buf.length();i++){
            char c=buf.charAt(i);
            if(c<32){
                if(c!='\t' && c!='\r' && c!='\n') buf.setCharAt(i,' ');
            }
        }
        return buf.toString();
    }
    
    
    
    private class XTMParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
        
        public static final String XMLNS_XTM = "http://www.topicmaps.org/xtm/1.0/";
        public static final String TAG_SCOPE                        = "scope";
        public static final String TAG_INSTANCEOF                   = "instanceOf";
        public static final String TAG_SUBJECTIDENTITY              = "subjectIdentity";
        public static final String TAG_BASENAMESTRING               = "baseNameString";
        public static final String TAG_BASENAME                     = "baseName";
        public static final String TAG_TOPICREF                     = "topicRef";
        public static final String TAG_SUBJECTINDICATORREF          = "subjectIndicatorRef";
        public static final String TAG_VARIANTNAME                  = "variantName";
        public static final String TAG_PARAMETERS                   = "parameters";
        public static final String TAG_ASSOCIATION                  = "association";
        public static final String TAG_MEMBER                       = "member";
        public static final String TAG_VARIANT                      = "variant";
        public static final String TAG_TOPIC_MAP                    = "topicMap";
        public static final String TAG_TOPIC                        = "topic";
        public static final String TAG_ROLESPEC                     = "roleSpec";
        public static final String TAG_OCCURRENCE                   = "occurrence";
        public static final String TAG_RESOURCEREF                  = "resourceRef";
        public static final String TAG_RESOURCEDATA                 = "resourceData";
        public static final String TAG_MERGEMAP                     = "mergeMap";

        public static final String XMLNS_XLINK = "http://www.w3.org/1999/xlink";
        
        private static final int STATE_START = 0;
        private static final int STATE_TOPICMAP = 1;
        private static final int STATE_TOPIC = 2;
        private static final int STATE_INSTANCEOF = 3;
        private static final int STATE_SUBJECTIDENTITY = 4;
        private static final int STATE_BASENAME = 5;
        private static final int STATE_SCOPE = 6;
        private static final int STATE_VARIANT = 7;
        private static final int STATE_PARAMETERS = 8;
        private static final int STATE_VARIANTNAME = 9;
        private static final int STATE_ASSOCIATION = 10;
        private static final int STATE_MEMBER = 11;
        private static final int STATE_ROLESPEC = 12;
        private static final int STATE_BASENAMESTRING = 13;
        private static final int STATE_VARIANTRESOURCEDATA = 14;
        private static final int STATE_OCCURRENCE = 15;
        private static final int STATE_OCCURRENCEINSTANCEOF = 16;
        private static final int STATE_RESOURCEDATA = 17;
        
        private Collection parsedType;
        private Topic parsedRole;
        private Collection parsedTopicCollection;
        private Topic parsedTopicRef;
        private Locator parsedSubjectLocator;
        private Collection parsedSubjectIdentifiers;
        private String parsedBaseName;
        private Collection parsedScope;
        private Collection parsedParameters;
        private Collection parsedVariants;
        private Collection parsedBaseNameVariants;
        private Collection parsedOccurrences;
        private Collection parsedPlayers;
        private Collection parsedMembers;
        private String parsedVariantName;
        private String topicID;
        private Topic parsedOccurrenceType;
        private String parsedOccurrenceData;
        private String parsedOccurrenceRef;
        private long parsedEdittime;
        private String associationID;
        
        /*
         * Occurrences are used to carry wandora specific data. These are marked with specific occurrence
         * types. We must parse the whole topic map before we can be sure that the occurrence type topics
         * have their subject identifiers set, so we collect all parsed occurrence data in allOccurrences
         * and add them when the topicmap element ends (that is at </topicmap>).
         */
        private Hashtable allOccurrences;
        
        private IntegerStack stateStack;
        private int state;
        
        private Hashtable idmapping;
        
        private Hashtable mergemap;
        
        private TopicMapLogger logger;
        private int count;
        private int topicCount;
        private int associationCount;
        private int occurrenceCount;
        
        
        public XTMParser(TopicMapLogger logger) {
            if(logger != null) this.logger = logger;
            else logger = TopicMap.this;
            count = 0;
            topicCount = 0;
            associationCount = 0;
            occurrenceCount = 0;
            idmapping=new Hashtable();
            mergemap=new Hashtable();
            state=STATE_START;
            stateStack=new IntegerStack();
        }
        
        @Override
        public void characters(char[] buf,int start,int length){
            switch(state){
                case STATE_BASENAMESTRING:
                    parsedBaseName+=new String(buf,start,length);
                    break;
                case STATE_VARIANTRESOURCEDATA:
                    parsedVariantName+=new String(buf,start,length);
                    break;
                case STATE_RESOURCEDATA:
                    parsedOccurrenceData+=new String(buf,start,length);
                    break;
            }
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
            if(logger.forceStop()) {
                throw new org.xml.sax.SAXException("user_interrupt");
            }
            try {
                switch(state){
                    case STATE_START:
                        if(qName.equals(TAG_TOPIC_MAP)){
                            stateStack.push(state);
                            state=STATE_TOPICMAP;
                            allOccurrences=new Hashtable();
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPIC_MAP); // TODO: throw exception
                        break;
                    case STATE_TOPICMAP:
                        if(qName.equals(TAG_TOPIC)){
                            topicID=attributes.getValue("id");
                            stateStack.push(state);
                            state=STATE_TOPIC;
                            parsedType=new LinkedHashSet();
                            parsedBaseName=null;
                            parsedSubjectIdentifiers=new LinkedHashSet();
                            parsedSubjectLocator=null;
                            parsedOccurrences=new LinkedHashSet();
                            parsedVariants=new LinkedHashSet();
                            parsedEdittime=0;
                            topicCount++;
                        }
                        else if(qName.equals(TAG_ASSOCIATION)){
                            associationID=attributes.getValue("id");
                            stateStack.push(state);
                            state=STATE_ASSOCIATION;                        
                            parsedType=new LinkedHashSet();
                            parsedMembers=new LinkedHashSet();
                            associationCount++;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPIC+" or "+TAG_ASSOCIATION+" got "+qName);
                        break;
                    case STATE_TOPIC:
                        if(qName.equals(TAG_INSTANCEOF)){
                            stateStack.push(state);
                            state=STATE_INSTANCEOF;
                        }
                        else if(qName.equals(TAG_SUBJECTIDENTITY)){
                            stateStack.push(state);
                            state=STATE_SUBJECTIDENTITY;
                        }
                        else if(qName.equals(TAG_BASENAME)){
                            stateStack.push(state);
                            state=STATE_BASENAME;
                            parsedBaseName=null;
                            parsedScope=new LinkedHashSet();
                            parsedBaseNameVariants=new LinkedHashSet();
                        }
                        else if(qName.equals(TAG_OCCURRENCE)){
                            stateStack.push(state);
                            state=STATE_OCCURRENCE;
                            parsedScope=new LinkedHashSet();
                            parsedOccurrenceType=null;
                            parsedOccurrenceData=null;
                            parsedOccurrenceRef=null;
                            occurrenceCount++;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_INSTANCEOF+", "+TAG_SUBJECTIDENTITY+", "+TAG_BASENAME+" or "+TAG_OCCURRENCE+" got "+qName);
                        break;
                    case STATE_INSTANCEOF:
                        if(qName.equals(TAG_TOPICREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedType.add(getOrCreateTopic(href));
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            Locator l=createLocator(attributes.getValue(XMLNS_XLINK,"href"));
                            Topic t=getTopic(l);
                            if(t==null){
                                t=createTopic();
                                t.addSubjectIdentifier(createLocator(attributes.getValue(XMLNS_XLINK,"href")));
                            }
                            parsedType.add(t);
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPICREF+" or "+TAG_SUBJECTINDICATORREF);                    
                        break;
                    case STATE_SUBJECTIDENTITY:
                        if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedSubjectIdentifiers.add(createLocator(href));
                        }
                        else if(qName.equals(TAG_RESOURCEREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedSubjectLocator=createLocator(href);                        
                        }
                        else logger.log("Parse exception: Expecting "+TAG_SUBJECTINDICATORREF+" or "+TAG_RESOURCEREF);                    
                        // TODO: topicRef
                        break;
                    case STATE_BASENAME:
                        if(qName.equals(TAG_BASENAMESTRING)){
                            stateStack.push(state);
                            state=STATE_BASENAMESTRING;
                            parsedBaseName="";
                        }
                        else if(qName.equals(TAG_VARIANT)){
                            stateStack.push(state);
                            state=STATE_VARIANT;
                            parsedParameters=new LinkedHashSet();
                            parsedVariantName=null;
                        }
                        else if(qName.equals(TAG_SCOPE)){
                            stateStack.push(state);
                            state=STATE_SCOPE;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_BASENAMESTRING+", "+TAG_VARIANT+" or "+TAG_SCOPE);                    
                        break;
                    case STATE_BASENAMESTRING:
                        logger.log("Parse exception: Expecting char data!");                    
                        break;
                    case STATE_VARIANT:
                        if(qName.equals(TAG_PARAMETERS)){
                            stateStack.push(state);
                            state=STATE_PARAMETERS;
                        }
                        else if(qName.equals(TAG_VARIANTNAME)){
                            stateStack.push(state);
                            state=STATE_VARIANTNAME;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_PARAMETERS+" or "+TAG_VARIANTNAME);                    
                        break;
                    case STATE_PARAMETERS:
                        if(qName.equals(TAG_TOPICREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedParameters.add(getOrCreateTopic(href));
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            Locator l=createLocator(attributes.getValue(XMLNS_XLINK,"href"));
                            Topic t=getTopic(l);
                            if(t==null){
                                t=createTopic();
                                t.addSubjectIdentifier(createLocator(attributes.getValue(XMLNS_XLINK,"href")));
                            }
                            parsedParameters.add(t);
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPICREF+" or "+TAG_SUBJECTINDICATORREF);                    
                        break;
                    case STATE_VARIANTNAME:
                        if(qName.equals(TAG_RESOURCEDATA)){
                            stateStack.push(state);
                            state=STATE_VARIANTRESOURCEDATA;
                            parsedVariantName="";
                        }
                        else logger.log("Parse exception: Expecting "+TAG_RESOURCEDATA);
                        break;
                    case STATE_VARIANTRESOURCEDATA:
                        logger.log("Parse exception: Expecting char data");
                        break;
                    case STATE_SCOPE:
                        if(qName.equals(TAG_TOPICREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedScope.add(getOrCreateTopic(href));
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            Locator l=createLocator(attributes.getValue(XMLNS_XLINK,"href"));
                            Topic t=getTopic(l);
                            if(t==null){
                                t=createTopic();
                                t.addSubjectIdentifier(createLocator(attributes.getValue(XMLNS_XLINK,"href")));
                            }
                            parsedScope.add(t);
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPICREF+" or "+TAG_SUBJECTINDICATORREF);                    
                        //TODO: resourceRef
                        break;
                    case STATE_OCCURRENCE:
                        if(qName.equals(TAG_SCOPE)){
                            stateStack.push(state);
                            state=STATE_SCOPE;
                        }
                        else if(qName.equals(TAG_INSTANCEOF)){
                            stateStack.push(state);
                            state=STATE_OCCURRENCEINSTANCEOF;

                        }
                        else if(qName.equals(TAG_RESOURCEREF)){
                            parsedOccurrenceRef=attributes.getValue(XMLNS_XLINK,"href");
                        }
                        else if(qName.equals(TAG_RESOURCEDATA)){
                            stateStack.push(state);
                            state=STATE_RESOURCEDATA;
                            parsedOccurrenceData="";
                        }
                        else logger.log("Parse exception: Expecting "+TAG_SCOPE+", "+TAG_INSTANCEOF+" or "+TAG_RESOURCEDATA+" got "+qName);
                        break;
                    case STATE_OCCURRENCEINSTANCEOF:
                        if(qName.equals(TAG_TOPICREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedOccurrenceType=getOrCreateTopic(href);
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            Locator l=createLocator(attributes.getValue(XMLNS_XLINK,"href"));
                            Topic t=getTopic(l);
                            if(t==null){
                                t=createTopic();
                                t.addSubjectIdentifier(createLocator(attributes.getValue(XMLNS_XLINK,"href")));
                            }
                            parsedOccurrenceType=t;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPICREF+" or "+TAG_SUBJECTINDICATORREF);                    
                        break;
                    case STATE_RESOURCEDATA:
                        logger.log("Parse exception: Expecting char data!");
                        break;
                    case STATE_ASSOCIATION:
                        if(qName.equals(TAG_INSTANCEOF)){
                            stateStack.push(state);
                            state=STATE_INSTANCEOF;
                        }
                        else if(qName.equals(TAG_SCOPE)){
                            stateStack.push(state);
                            state=STATE_SCOPE;
                            logger.log("Warning: Scope not supported in associations. AssociationID=\""+associationID+"\"");
                        }
                        else if(qName.equals(TAG_MEMBER)){
                            stateStack.push(state);
                            state=STATE_MEMBER;
                            parsedRole=null;
                            parsedPlayers=new LinkedHashSet();
                        }
                        else logger.log("Parse exception: Expecting "+TAG_INSTANCEOF+", "+TAG_SCOPE+" or "+TAG_MEMBER+" got "+qName);
                        break;
                    case STATE_MEMBER:
                        if(qName.equals(TAG_TOPICREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedPlayers.add(getOrCreateTopic(href));
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            Locator l=createLocator(attributes.getValue(XMLNS_XLINK,"href"));
                            Topic t=getTopic(l);
                            if(t==null){
                                t=createTopic();
                                t.addSubjectIdentifier(createLocator(attributes.getValue(XMLNS_XLINK,"href")));
                            }
                            parsedPlayers.add(t);
                        }
                        else if(qName.equals(TAG_ROLESPEC)){
                            stateStack.push(state);
                            state=STATE_ROLESPEC;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPICREF+", "+TAG_SUBJECTINDICATORREF+" or "+TAG_ROLESPEC);                    
                        break;
                    case STATE_ROLESPEC:
                        if(qName.equals(TAG_TOPICREF)){
                            String href=attributes.getValue(XMLNS_XLINK,"href");
                            parsedRole=getOrCreateTopic(href);                        
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){
                            Locator l=createLocator(attributes.getValue(XMLNS_XLINK,"href"));
                            Topic t=getTopic(l);
                            if(t==null){
                                t=createTopic();
                                t.addSubjectIdentifier(createLocator(attributes.getValue(XMLNS_XLINK,"href")));
                            }
                            parsedRole=t;
                        }
                        else logger.log("Parse exception: Expecting "+TAG_TOPICREF+" or "+TAG_SUBJECTINDICATORREF);
                        break;
                }
            }
            catch(Exception e){
                logger.log(e);
            }
            if(count++ % 10000 == 9999) {
                logger.hlog("Importing XTM topic map.\nFound " + topicCount + " topics, " + associationCount + " associations and "+ occurrenceCount + " occurrences.");
            }
        }
        
        
        
        
        @Override
        public void endElement(String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if(logger.forceStop()) {
                throw new org.xml.sax.SAXException("user_interrupt");
            }
            Iterator iter;
            Topic topic;
            try {
                switch(state){
                    case STATE_TOPICMAP:
                        if(qName.equals(TAG_TOPIC_MAP)){
                            state=stateStack.pop();

                            Locator eloc=createLocator(EDITTIME_SI);
                            iter=allOccurrences.entrySet().iterator();
                            while(iter.hasNext()){
                                Map.Entry e=(Map.Entry)iter.next();
                                topic=(Topic)e.getKey();
                                if(topic.isRemoved()){
                                    logger.log("Warning: Occurrence topic is removed (probably merged), topic map was inconsistent!");
                                    topic=getTopic(topic.getOneSubjectIdentifier());
                                    if(topic==null){
                                        logger.log("Error: Couldn't find other version of topic, skipping occurrence!");
                                        break;
                                    }
                                }
                                Collection c=(Collection)e.getValue();
                                Iterator iter2=c.iterator();
                                while(iter2.hasNext()){
                                    Occurrence o=(Occurrence)iter2.next();
                                    if(o.type==null) logger.log("Warning: Occurrence has no type!");
                                    if(o.type.getSubjectIdentifiers().contains(eloc)){
                                        topic.setEditTime(Long.parseLong(o.data));
                                    }
                                    else{
                                        if(o.version==null) {
                                            logger.log("Warning: Occurrence has no version, adding a generic one!");
                                            o.version=getTopic(TMBox.LANGINDEPENDENT_SI);
                                            if(o.version==null){
                                                o.version=createTopic();
                                                o.version.addSubjectIdentifier(createLocator(TMBox.LANGINDEPENDENT_SI));
                                                o.version.setBaseName("Language independent");
                                            }
                                        }
                                        if(o.version!=null){
                                            if(o.data!=null){
                                                if(o.type.isRemoved()) logger.log("!!!! type is removed");
                                                if(o.version.isRemoved()) logger.log("!!!! version is removed");
                                                topic.setData(o.type,o.version, o.data);
                                            }
                                            if(o.ref!=null){
                                                logger.log("Converting resourceRef occurrence to new topic and an association");
                                                Topic t=createTopic();
                                                t.addSubjectIdentifier(createLocator(makeSubjectIndicator()));
                                                t.setBaseName("Occurrence file: "+o.ref);
                                                t.setSubjectLocator(createLocator(o.ref));
                                                Topic orole=getTopic("http://wandora.org/si/compatibility/occurrence-role-reference");
                                                if(orole==null){
                                                    orole=createTopic();
                                                    orole.addSubjectIdentifier(createLocator("http://wandora.org/si/compatibility/occurrence-role-reference"));
                                                    orole.setBaseName("Occurrence role reference");
                                                }
                                                Topic trole=getTopic("http://wandora.org/si/compatibility/occurrence-role-topic");
                                                if(trole==null){
                                                    trole=createTopic();
                                                    trole.addSubjectIdentifier(createLocator("http://wandora.org/si/compatibility/occurrence-role-topic"));
                                                    trole.setBaseName("Occurrence role topic");
                                                }
                                                Association a=createAssociation(o.type);
                                                a.addPlayer(topic,trole);
                                                a.addPlayer(t,orole);
                                            }    
                                            if(o.data==null && o.ref==null){
                                                logger.log("Warning: Occurrence has no data and no reference!");
                                            }
                                        }
                                        else logger.log("Warning: Occurrence still has no version (weird)!");
                                    }                                
                                }
                            }
                            
                            // this is for debugging
                            iter=idmapping.entrySet().iterator();
                            while(iter.hasNext()){
                                Map.Entry e=(Map.Entry)iter.next();
                                Object key=e.getKey();
                                Topic t=(Topic)e.getValue();
                                if(t.isRemoved()){
                                    logger.log("Topic was removed, XTM file was probably inconsistent, id was \""+key+"\".");
                                }
                                else if(t.getOneSubjectIdentifier()==null){
                                    logger.log("No subject identifier for topic, id was \""+key+"\".");
                                }
                            }
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_TOPIC_MAP+" but got "+qName);
                        break;
                    case STATE_TOPIC:
                        if(qName.equalsIgnoreCase(TAG_TOPIC)){
                            if(topicID==null) topic=createTopic();
                            else topic=getOrCreateTopic("#"+topicID);
                            iter=parsedType.iterator();
                            while(iter.hasNext()){
                                Topic t=(Topic)iter.next();
                                topic.addType(t);
                            }
//                            if(parsedSubjectIdentifiers.size()==0) logger.log("Warning, couldn't find any subject identifiers for topic "+parsedBaseName);
                            iter=parsedSubjectIdentifiers.iterator();
                            while(iter.hasNext()){
                                Locator l=(Locator)iter.next();
                                topic.addSubjectIdentifier(l);
                            }
                            if(parsedSubjectIdentifiers.isEmpty()){
//                                logger.log("Warning topic has no subject identifiers, creating one.");
                                topic.addSubjectIdentifier(createLocator(makeSubjectIndicator()));
                            }
                            iter=parsedVariants.iterator();
                            while(iter.hasNext()){
                                VariantName v=(VariantName)iter.next();
                                topic.setVariant(v.scope,v.name);
                            }
                            if(parsedSubjectLocator!=null) topic.setSubjectLocator(parsedSubjectLocator);
                            if(parsedBaseName!=null && parsedBaseName.length()>0) topic.setBaseName(parsedBaseName);
                            allOccurrences.put(topic,parsedOccurrences);
                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_TOPIC+" but got "+qName);
                        break;
                    case STATE_ASSOCIATION:
                        if(qName.equalsIgnoreCase(TAG_ASSOCIATION)){
                            if(parsedType.isEmpty()) logger.log("No association type");
                            else{
                                if(parsedType.size()>1) logger.log("Multiple types for association, using first!");
                                topic = (Topic)parsedType.iterator().next();
                                Association a=createAssociation(topic);
                                iter=parsedMembers.iterator();
                                // TODO: check that no multiple members with same role
                                while(iter.hasNext()){
                                    Member m=(Member)iter.next();
                                    a.addPlayer(m.player,m.role);
                                }
                            }
                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_ASSOCIATION+" but got "+qName);
                        break;
                    case STATE_INSTANCEOF:
                        if(qName.equals(TAG_INSTANCEOF)){
                            state=stateStack.pop();
                        }                    
                        else if(qName.equals(TAG_TOPICREF)){}
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_INSTANCEOF+" but got "+qName);
                        break;
                    case STATE_SUBJECTIDENTITY:
                        if(qName.equals(TAG_SUBJECTIDENTITY)){
                            state=stateStack.pop();
                        }
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else if(qName.equals(TAG_RESOURCEREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_SUBJECTIDENTITY+" but got "+qName);
                        break;
                    case STATE_BASENAME:
                        if(qName.equals(TAG_BASENAME)){

                            iter=parsedBaseNameVariants.iterator();
                            while(iter.hasNext()){
                                VariantName v=(VariantName)iter.next();
                                v.scope.addAll(parsedScope);
                            }
                            parsedVariants.addAll(parsedBaseNameVariants);

                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_BASENAME+" but got "+qName);
                        break;
                    case STATE_BASENAMESTRING:
                        if(qName.equals(TAG_BASENAMESTRING)){
                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_BASENAMESTRING+" but got "+qName);
                        break;
                    case STATE_VARIANT:
                        if(qName.equals(TAG_VARIANT)){
                            if(parsedVariantName != null) {
                                parsedBaseNameVariants.add(new VariantName(parsedVariantName,parsedParameters));
                            }
                            else {
                                logger.log("parsedVariantName == " + parsedVariantName + "(topic " + topicID + ")");
                            }
                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_VARIANT+" but got "+qName);
                        break;
                    case STATE_PARAMETERS:
                        if(qName.equals(TAG_PARAMETERS)){
                            state=stateStack.pop();
                        }
                        else if(qName.equals(TAG_TOPICREF)){}
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_PARAMETERS+" but got "+qName);
                        break;
                    case STATE_VARIANTNAME:
                        if(qName.equals(TAG_VARIANTNAME)){
                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_VARIANTNAME+" but got "+qName);
                        break;
                    case STATE_VARIANTRESOURCEDATA:
                        if(qName.equals(TAG_RESOURCEDATA)){
                            state=stateStack.pop();
                        }
                        else logger.log("Parse exception: Expecting end of "+TAG_RESOURCEDATA+" but got "+qName);
                        break;
                    case STATE_OCCURRENCE:
                        if(qName.equals(TAG_OCCURRENCE)){
                            if(parsedOccurrenceType==null) logger.log("No occurrence type");
                            else{
                                if(parsedScope.size()>1) logger.log("Occurrence scope contains more than one topic, using only first!");
                                Occurrence o=new Occurrence(parsedOccurrenceType,(parsedScope.size()>=1?(Topic)parsedScope.iterator().next():null),parsedOccurrenceData,parsedOccurrenceRef);
                                parsedOccurrences.add(o);
                            }
                            state=stateStack.pop();
                        }                    
                        else if(qName.equals(TAG_RESOURCEREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_OCCURRENCE+" but got "+qName);
                        break;
                    case STATE_OCCURRENCEINSTANCEOF:
                        if(qName.equals(TAG_INSTANCEOF)){
                            state=stateStack.pop();
                        }                    
                        else if(qName.equals(TAG_TOPICREF)){}
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_INSTANCEOF+" but got "+qName);
                        break;
                    case STATE_RESOURCEDATA:
                        if(qName.equals(TAG_RESOURCEDATA)){
                            state=stateStack.pop();
                        }                    
                        else logger.log("Parse exception: Expecting end of "+TAG_RESOURCEDATA+" but got "+qName);
                        break;
                    case STATE_SCOPE:
                        if(qName.equals(TAG_SCOPE)){
                            state=stateStack.pop();
                        }
                        else if(qName.equals(TAG_TOPICREF)){}
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_SCOPE+" but got "+qName);
                        break;
                    case STATE_MEMBER:
                        if(qName.equals(TAG_MEMBER)){
                            if(parsedPlayers.size()>0){
                                if(parsedPlayers.size()>1) logger.log("Warning: Only one player per member. Association id=\""+associationID+"\"!");
                                parsedMembers.add(new Member((Topic)parsedPlayers.iterator().next(),parsedRole));
                            }
                            else logger.log("No players found"); // TODO: warning no player found
                            state=stateStack.pop();
                        }
                        else if(qName.equals(TAG_TOPICREF)){}
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_MEMBER+" but got "+qName);
                        break;
                    case STATE_ROLESPEC:
                        if(qName.equals(TAG_ROLESPEC)){
                            state=stateStack.pop();
                        }
                        else if(qName.equals(TAG_TOPICREF)){}
                        else if(qName.equals(TAG_SUBJECTINDICATORREF)){}
                        else logger.log("Parse exception: Expecting end of "+TAG_ROLESPEC+" but got "+qName);
                        break;
                    default:
                        logger.log("Parse exception: Not expecting end of tag but got "+qName);
                        break;
                }
            }
            catch(Exception e){
                logger.log(e);
            }
        }
        
        public Topic getOrCreateTopic(String href) throws TopicMapException {
            Topic t=(Topic)idmapping.get(href);
            if(t!=null) {
                if(t.isRemoved()) {
                    logger.log("ID mapping found for \""+href+"\" but topic has been deleted (or merged). XTM is probably inconsistent. Getting new version of the topic.");
                    Locator l=t.getOneSubjectIdentifier();
                    if(l==null) t=null;
                    else t=getTopic(l);
                    if(t!=null){
                        idmapping.put(href,t);
                    }
                    else{
                        logger.log("Couldn't find new version of deleted topic. This will probably cause problems. Creating new.");
                        t=createTopic();
                        idmapping.put(href,t);
                    }
                }
                return t;
            }
            else {
                t=createTopic();
                idmapping.put(href,t);
                return t;
            }
        }
        
        
        
        @Override
        public void endDocument() throws SAXException {
            logger.log("Found total " + topicCount + " topics, " + associationCount + " associations and "+ occurrenceCount + " occurrences.");
        }        
        
        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
        
        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }
        
        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }
        
        @Override
        public void setDocumentLocator(org.xml.sax.Locator locator) {
        }
        
        @Override
        public void skippedEntity(String name) throws SAXException {
        }
        
        @Override
        public void startDocument() throws SAXException {
        }
        
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }
        
        
       
        @Override
        public void error(SAXParseException e) throws SAXParseException {
            throw e;
        }
        @Override
        public void fatalError(SAXParseException e) throws SAXParseException {
            throw e;
        }
        @Override
        public void warning(SAXParseException e) {
            logger.log(e);
        }
        
        
        
        public class VariantName {
            public String name;
            public Set<Topic> scope;
            public VariantName(String name, Collection c){
                this.name=name;
                scope = new LinkedHashSet<Topic>();
                for(Iterator<Topic> i = c.iterator(); i.hasNext();) {
                    scope.add(i.next());
                }
            }
            public VariantName(String name, Set scope){
                this.name=name;
                this.scope=scope;
            }
        }
        
        public class Member {
            public Topic player;
            public Topic role;
            public Member(Topic player,Topic role){
                this.player=player;
                this.role=role;
            }
        }
        
        public class Occurrence {
            public Topic type;
            public Topic version;
            public String data;
            public String ref;
            public Occurrence(Topic type,Topic version,String data,String ref){
                this.type=type; this.version=version; this.data=data; this.ref=ref;
            }
        }
        
        public class IntegerStack {
            private Stack stack;
            public IntegerStack(){
                this.stack=new Stack();
            }
            public int pop(){
                return ((Integer)stack.pop()).intValue();
            }
            public void push(int i){
                stack.push(Integer.valueOf(i));
            }
            public int peek(){
                return ((Integer)stack.peek()).intValue();
            }
        }
    }
    

    
}

