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
 * 
 */

package org.wandora.utils.regexextractor.bag;

import org.wandora.utils.IObox;
import org.wandora.utils.Rexbox;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.things.*;
import org.wandora.utils.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;



/**
 *  GenericBag is the default Bag-interface implementation. It functions mainly
 * as a data store for all objects.
 *
 * Misc notes:
 *  o Trims nodes from XML input
 *  o Added dirty flagging for persistence. If things are
 *    modified outside this code (directly), flagging won't work.
 *
 * @version 1.1
 */
public class GenericBag implements Bag, Serializable, Cloneable, ThingFactory {
    // TODO consider putting bag.* into a private table
    protected Hashtable myTable = new Hashtable();
    
    private transient Hashtable extraInitObjs = new Hashtable();
    
    private ThingFactory myThingFactory = null; // NOTE This should be transient if thing factory were really detached from GenericBag
    
    // Dirtying is implemented BOTH on Thing level and Bag level independently!
    // I.e. Bag may be dirty even though no Thing is dirty and
    // any Thing may be dirty even though Bag is clean. In both cases, the
    // aggregate object will be dirty.
    protected int dirty = 0;
    
    private transient boolean transientMembersNeedPlatform = false; // after deserialization the writeExternal calls setTable which sets this to true which causes the setup to execute when Platform is passed to this
    
    protected static final int defaultMaxSize = 20;
    
    {
        setDefaultThingClass(INITIAL_THING_CLASS);
    }
    
    // -------------------------------------------------------------------------
   
    
    public GenericBag() {
        myThingFactory = this; // NOTE this must be set for deserializer before init() !
    }
    
    
    public void init() {
        setDefaultThingClass(INITIAL_THING_CLASS);
        
        clear();
        
        put(new Object[][] {
            { "bag.maximum.size", "org.wandora.utils.regexextractor.bag.things.ObjectThing", "1000" },
            { "bag.version", "org.wandora.utils.regexextractor.bag.things.ObjectThing", "GenericBag v1.1" },
            { "bag.name", "org.wandora.utils.regexextractor.bag.things.ObjectThing", "GenericBag" }
        } );
    }
    
    
    public void init( Object obj ) {
        
    }
    
  
    
    public boolean isDirty() {
        if( dirty!=0 ) return true;
        
        synchronized( myTable ) {
            Collection things = myTable.values();
            Iterator iter = things.iterator();
            while( iter.hasNext() ) {
                Thing th = (Thing)iter.next();
                if( th.isDirty() )
                    return true;
            }
        }
        return false;
    }
    
    
    public void clearDirty() {
        dirty = 0;
        synchronized( myTable ) {
            Collection things = myTable.values();
            Iterator iter = things.iterator();
            while( iter.hasNext() ) {
                Thing th = (Thing)iter.next();
                th.clearDirty();
            }
        }
    }
    
    
    public void smudge(int level, Object key) {
        dirty = level;
    }
    
    
    // --- ThingFactory impl ---------------------------------------------------
    // -------------------------------------------------------------------------
    
    private Class defaultThingClass;
    //private Platform platform;
    private static Hashtable wrappings = null;
    
    public static void setThingFactoryPreferences(Bag prefBag) {
        if( null==wrappings ) {
            wrappings = new Hashtable();
            Enumeration keys = prefBag.keys();
            while( keys.hasMoreElements() ) {
                String key = (String)keys.nextElement();
                if( key.startsWith(ThingFactory.KEY_WRAPPINGS) ) {
                    String thingname = key.substring(ThingFactory.KEY_WRAPPINGS.length());
                    Vector v_wrap = new Vector();
                    String s_wrap = prefBag.getStringDefault(key,"");
                    if( !s_wrap.equals("") ) {
                        StringTokenizer tknz = new StringTokenizer(s_wrap,",");
                        while( tknz.hasMoreTokens() ) {
                            v_wrap.add( tknz.nextToken() );
                        }
                        wrappings.put( thingname, v_wrap );
                    }
                }
            }
        }
    }
    
    public Class thingClassForName(String thingClassName) {
        try {
            Class thingClass = Class.forName(thingClassName);
            return thingClass;
        } catch (ClassNotFoundException e) {
            LogWriter.println("dbg","Class not found for the thing class " + thingClassName + "! Using default class!");
            return getDefaultThingClass();
        }
    }
    
    public Class getDefaultThingClass() {
        return defaultThingClass;
    }
    
    public void setDefaultThingClass(String thingClassName) {
        try {
            defaultThingClass = Class.forName(thingClassName);
        } catch (ClassNotFoundException e0) {
            LogWriter.println("WRN","Unable to set bag's default thing class " + thingClassName + "! Class cast exception!");
        } catch (Exception e1) {
            LogWriter.println("WRN","Unable to set bag's default thing class " + thingClassName + "!");
        }
    }
    
    public Thing createThing( String thingname ) {
        return createThing(getDefaultThingClass(), thingname);
    }
    
    public Thing createThing( String thingClassName, String thingname ) {
        if (null==thingClassName) return createThing(getDefaultThingClass(), thingname);
        try {
            return createThing( thingClassForName(thingClassName), thingname );
        }
        catch (Exception e) {
            LogWriter.println("WRN","Unable to create thing of class " + thingClassName + "!");
        }
        return null;
    }
    
    public Thing createThing( Class thingClass, String thingname ) {
        return createThing( new Class[] { thingClass }, thingname );
    }
    
    public Thing createThing( Class[] thingClass, String thingname ) {
        if( null==thingClass ) {
            return null;
        }
        
        if( thingname!=null && wrappings!=null && wrappings.containsKey(thingname) ) {
            Vector w = (Vector)wrappings.get(thingname);
            if( w.size()>0 ) {
                Class[] chain = new Class[thingClass.length+w.size()];
                try {
                    for( int i=0;i<w.size();i++ )
                        chain[i] = Class.forName((String)w.get(i));
                    for( int i=0;i<thingClass.length;i++ )
                        chain[i+w.size()] = thingClass[i];
                    return createChain( chain, thingname );
                } catch( ClassNotFoundException cnfe ) {}
            }
        }
        return createChain( thingClass, thingname );
    }
    
    private Thing createChain( Class[] thingClass, String thingname ) {
        Thing thing = null;
        if( null==extraInitObjs )
            extraInitObjs = new Hashtable();
        try {
            thing = (Thing) thingClass[thingClass.length-1].newInstance();
            thing.init(this);
            if( thing.supportsRule("SET_PLATFORM") ) {
                LogWriter.println("WRN","extraInitObj \"Platform\" not set!");
            }
            if( thing.supportsRule("SET_THINGFACTORY") ) {
                thing.add( this, "SET_THINGFACTORY" );
            }
        } catch (InstantiationException e2) {
            LogWriter.println("ERR","Thing object can not be instantiated for class " + thingClass.toString() + "!");
        } catch (IllegalAccessException e3) {
            LogWriter.println("ERR","Thing class " + thingClass.toString() + " can not be accessed here! Illegal access exception!");
        } catch (ClassCastException e3) {
            LogWriter.println("ERR","Thing class " + thingClass.toString() + " does not extend the Thing class!");
        } catch (Exception e4) {
            LogWriter.println("ERR","Caught exception while initializing thing " + thingClass.toString() + ": "+e4);
        }
        if( thingClass.length>1 ) {
            Class[] wrapperChain = new Class[thingClass.length-1];
            for( int i=0;i<thingClass.length-1;i++ )
                wrapperChain[i] = thingClass[i];
            Thing wrapper = createChain( wrapperChain,thingname );
            if( wrapper.supportsRule("SET_INNER") ) {
                wrapper.add( thing, "SET_INNER" );
            }
            if( wrapper.supportsRule("SET_KEY") ) {
                wrapper.add( thingname, "SET_KEY" );
            }
            return wrapper;
        }
        return thing;
    }
    
    // --- END ThingFactory impl -----------------------------------------------
    // -------------------------------------------------------------------------
    
    
    public void merge(Object object) throws Exception {
        if (object instanceof Document) load((Document) object);
        if (object instanceof String) load((String) object);
        if (object instanceof InputStreamReader) load((InputStreamReader) object);
        if (object instanceof Object[][]) put((Object[][]) object);
    }
    
    
    public int size(Object key) {
        try { return ((Thing) this.get(key)).size(); }
        catch (Exception e2) { return 0; }
    }
    
    
    public boolean keyContains(Object key, Object value) {
        try { return ((Thing) this.get(key)).contains(value); }
        catch (Exception e) { return false; }
    }

    
    public Object whichKeyContains(Object value) {
        Object key;       
        for (Enumeration keys = this.keys(); keys.hasMoreElements();) {
            key = keys.nextElement();
            if (keyContains(key, value)) return key;
        }
        return null;
    }

    // corrected misspelled method name. TODO Should phase out machingKeys, when
    // all apps have replaced it with matchingKeys...
    public Vector matchingKeys(String key) {
        return machingKeys(key);
    }
    
    public Vector machingKeys(String key) {
        Vector matchingKeys = new Vector();
        Object rexkey;       
        for (Enumeration keys = this.keys(); keys.hasMoreElements();) {
            rexkey = keys.nextElement();
            if (rexkey.equals(key)) { matchingKeys.add(rexkey); }
            else if (rexkey instanceof String) {
                if (Rexbox.match((String) rexkey, key)) { matchingKeys.add(rexkey); } 
            }
        }
        return matchingKeys;
    }
    
    
 
    
    // -------------------------------------------------------------------------

    private Class solveThingClass(Object key) {
        Class thingClass = getThingClass(key);
        if (thingClass == null) thingClass = defaultThingClass;
        return thingClass;
    }
    
 
    // -----
    public Object put(Object key, int value) {
        return put(solveThingClass(key), key, value, null);
    } 
    
    public Object put(Object key, long value) {
        return put(solveThingClass(key), key, value, null);
    }

    public Object put(Object key, Vector vector) {
        return put(solveThingClass(key), key, vector, null);
    }
    
    public Object put(Object key, Object value) {
        return put(solveThingClass(key), key, value, null);
    }
    
    
    
    // -----    
    public Object put(Object key, int value, Object criteria) {
        return put(solveThingClass(key), key, value, criteria);
    } 
    
    public Object put(Object key, long value, Object criteria) {
        return put(solveThingClass(key), key, value, criteria);
    }

    public Object put(Object key, Vector vector, Object criteria) {
        return put(solveThingClass(key), key, vector, criteria);
    }
    
    public Object put(Object key, Object value, Object criteria) {
        return put(solveThingClass(key), key, value, criteria);
    }   
        
    
    
    // -----    
    public Object put(Class thingClass, Object key, int value) {
        return put(thingClass, key, (Object) new Integer(value).toString(), null);
    }
    
    public Object put(Class thingClass, Object key, long value) {
        return put(thingClass, key, (Object) new Long(value).toString(), null);
    }

    public Object put(Class thingClass, Object key, Vector vector) {
        if (vector != null && vector.size() > 0) {
            put (thingClass, key, (Object) vector.elementAt(0));
            for (int i=1; i < vector.size() ; i++) {
                add (key, (Object) vector.elementAt(i), null);
            }
        }
        else { this.remove(key); }
        return null;
    }

    
    
    
    // -----
    public Object put(Class thingClass, Object key, int value, Object criteria) {
        return put(thingClass, key, (Object) new Integer(value).toString(), criteria);
    }
    
    public Object put(Class thingClass, Object key, long value, Object criteria) {
        return put(thingClass, key, (Object) new Long(value).toString(), criteria);
    }

    public Object put(Class thingClass, Object key, Vector vector, Object criteria) {
        if (vector != null && vector.size() > 0) {
            put (thingClass, key, (Object) vector.elementAt(0));
            for (int i=1; i < vector.size() ; i++) {
                add (key, (Object) vector.elementAt(i), criteria);
            }
        }
        else { this.remove(key); }
        return null;
    }
    
    
    
    
    
    public Object put(Object [][] thingArray) {
        if (thingArray != null) {
            for (int i=0; i<thingArray.length; i++) {
                try {
                    Class[] thingClassChain = null;
                    if (thingArray[i][1] instanceof Class) thingClassChain = new Class[] { (Class) thingArray[i][1] };
                    else if (thingArray[i][1] instanceof String) thingClassChain = new Class[] { thingClassForName((String) thingArray[i][1]) };
                    else if (thingArray[i][1] instanceof String[] ) {
                        String[] s_chain = (String[])thingArray[i][1];
                        thingClassChain = new Class[s_chain.length];
                        for( int c=0;c<s_chain.length;c++ ) {
                            thingClassChain[c] = thingClassForName( s_chain[c] );
                        }
                    }
                    else if (thingArray[i][1] != null) thingClassChain = new Class[] { thingArray[i][1].getClass() };
                    else thingClassChain = new Class[] { getDefaultThingClass() };
                    
                    put(thingClassChain, thingArray[i][0], thingArray[i][2]);
                }
                catch (Exception e) {
                    LogWriter.println("WRN","Unable to put index " + i + " of object array into bag! An exception!");
                }
            }
        }
        return null;
    }
    
    
    
    public Object put(Class thingClass, Object key, Object value) {
        return put(thingClass, key, value, null);
    }
    
    public Object put(Class[] thingClassChain, Object key, Object value) {
        return put(thingClassChain, key, value, null);
    }
    
    
    public synchronized Object put(Class thingClass, Object key, Object value, Object criteria) {
        return put( new Class[] { thingClass }, key, value, criteria );
    }
    
    public synchronized Object put(Class[] thingClass, Object key, Object value, Object criteria) {
        if (key != null) {
            if (value != null) {
                if (size() < getMaximumSize()) {
                    try {
                        if (thingClass == null) thingClass = new Class[] { defaultThingClass };
                        Thing thing;
                        if( key instanceof String )
                            thing = myThingFactory.createThing(thingClass,(String)key);
                        else
                            thing = myThingFactory.createThing(thingClass,null);
                        if (criteria == null)
                            thing.add(value);
                        else
                            thing.add(value, criteria);
/* ACTUAL STORING */    myTable.put(key, thing); // smudging now in things
                        return thing;
                    }
                    catch (Exception e2) {
                        LogWriter.println("WRN","Unable to put value '" + value + "' into a bag key '" + key + "'! An exception!");
                        e2.printStackTrace();
                    }
                }
                else
                    LogWriter.println("WRN","Bag size has reached the maximum size [" + getMaximumSize() + "]. Unable to store another object " + key + " into the bag!");
            }
            else { remove(key); }
        }
        return null;
    }

    

    public int getMaximumSize() {
        try {
            return getIntIn("bag.maximum.size");
        } catch (Exception e) {
            return defaultMaxSize;
        }
    }

    // -------------------------------------------------------------------------

    
    
    public synchronized Object add(Object key, Object value, Object rule) {
        try {
            Thing thing = (Thing) this.get(key);
            if (thing == null) {
                if (size() < getMaximumSize()) {
                    if( key instanceof String )
                        thing = myThingFactory.createThing((String)key);
                    else
                        thing = myThingFactory.createThing(null);
                    LogWriter.println( "dbg","GenericBag@"+this.hashCode()+".add(): new key='"+key+"'" );
/* ACTUAL STORING */myTable.put(key, thing);
                }
                else
                    LogWriter.println("WRN","Bag size has reached the maximum size [" + getMaximumSize() + "]. Unable to store another object " + key + " into the bag!");
            }
            else {
                LogWriter.println( "dbg","GenericBag@"+this.hashCode()+".add(): key='"+key+"'" );
            }
            // smudging now in things
            if (rule == null)
                return thing.add(value);
            else
                return thing.add(value, rule);
        }
        catch (Exception e1) {
            LogWriter.println("WRN","Game bag failed to add value into key '" + key.toString() + "'.");
            e1.printStackTrace();
        }
        return null;
    }

   
    
    public Object add(Object key, int value, Object rule) {
        return add(key, new Integer(value).toString(), rule);
    }

    public Object add(Object key, long value, Object rule) {
        return add(key, new Long(value).toString(), rule);
    }

    
        
    public Object add(Object key, Object value) {
        return add(key, value, null);
    }
    
    public Object add(Object key, int value) {
        return add(key, new Integer(value).toString(), null);
    }

    public Object add(Object key, long value) {
        return add(key, new Long(value).toString(), null);
    }
    
    // ---------------------------------------------------------- GET  THING ---
    
    
    protected synchronized Object get(Object key) {
        return myTable.get(key);
    }

    private Class getDeepThingClass(Thing t) {
        if( t.supportsRule("GET_INNER") ) {
            return getDeepThingClass( (Thing)t.get("GET_INNER") );
        }
        return t.getClass();
    }
    
    public synchronized Class getThingClass(Object key) {
        try {
            return getDeepThingClass( (Thing)myTable.get(key) );
        } catch (Exception e1) {}

        return null;
    }
    
   
   // ------------------------------------------------------------------ GET ---

    

    public Object getIn(Object key, Object rule) {
        Thing t = (Thing) get(key);
        if( t!=null ) {
            Object value = t.get(rule);
            return value;
        }
        else
            return null;
    }   
    public Object getIn(Object key) {
        Thing t = (Thing) get(key);
        if( t!=null ) {
            Object value = t.get();
            return value;
        }
        else
            return null;
    }
    
    // NOTE by PH: get*In should have consistent semantics with respect to throwing exceptions.
    // More specifically, if one get*In throws Exception which is caused by missing Bag data,
    // all get*In's should throw Exception when missing Bag data! 
    // -------    
    public int getIntIn(Object key) throws Exception {
        return Integer.parseInt(getStringIn(key));
    }
    public int getIntIn(Object key, Object rule) throws Exception {
        return Integer.parseInt(getStringIn(key, rule));
    }
    
    
    // -------    
    public long getLongIn(Object key) throws Exception {
        return Long.parseLong(getStringIn(key));
    }
    public long getLongIn(Object key, Object rule) throws Exception {
        return Long.parseLong(getStringIn(key, rule));
    }
    
    
    // -------
    public String getStringIn(Object key) throws Exception {
        return (String) getIn(key);
    }
    public String getStringIn(Object key, Object rule) throws Exception {
        return (String) getIn(key, rule);
    }
    
    
    // -------
    public boolean getBooleanIn(Object key, Object rule) throws Exception {
        String bString = getStringIn(key,rule);
        if (bString == null) throw new Exception();
        return new Boolean( bString ).booleanValue();
    }
    public boolean getBooleanIn(Object key) throws Exception {
        String bString = getStringIn(key);
        if (bString == null) throw new Exception();
        if (bString.equalsIgnoreCase("yes") || bString.equalsIgnoreCase("1")) return true;
        return new Boolean(bString).booleanValue();
    }
    
    
    
    //-------
    public Enumeration keys() {
        return myTable.keys();
    }
/*    
    public Collection values() {
        return myTable.values();
    }*/
    
    
    
    // -------------------------------------------------------------------------
    
    public boolean supportsRule(Object key, Object rule) {
        try {
            Thing thing = (Thing) get(key);
            return thing.supportsRule(rule);
        }
        catch (Exception e) {}
        return false;
    }
    
    
    public Object[] supportedRules(Object key) {
        try {
            Thing thing = (Thing) get(key);
            return thing.supportedRules();
        }
        catch (Exception e) {}
        return null;   
    }
    
    
    // -------------------------------------------------------------------------
    
    public boolean reset(Object key, Object criteria) {
        try {
            Thing thing = (Thing) get(key);
            if (criteria == null) thing.reset();
            else thing.reset(criteria);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    
    public void reset(Object key) {
        reset(key, null);
    }
    
    
    public Object remove(Object key) {
        if( key!=null ) {
            smudge(3,key);
            return myTable.remove(key);
        }
        return null;
    }
    
    public void reset() {
        clear();
    }
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------------------ IO ---
    // -------------------------------------------------------------------------
    
    
    
    public void load(String inputFile) throws IOException {
        parseBag(IObox.loadFile(inputFile));
        smudge(4,null);
    }
    
    public void load(InputStreamReader input) throws IOException {        
        parseBag(IObox.loadFile(input));
        smudge(5,null);
    }
    
    
    private String textValue( Element e ) {
        StringBuffer text = new StringBuffer();
        NodeList nl = e.getChildNodes();
        for( int i=0;i<nl.getLength();i++ ) {
            Node n = nl.item(i);
            if( n.getNodeType()==Node.TEXT_NODE || n.getNodeType()==Node.CDATA_SECTION_NODE ) {
                String bit = n.getNodeValue().trim();
                text.append( bit );
            }
            else {
                return null;
            }
        }
        return text.toString();
    }
    
    
    private void parseXML(Node node, String key) {
        NodeList nodes = node.getChildNodes();
        int numOfNodes = nodes.getLength();
        for( int nnum=0; nnum<numOfNodes; nnum++ ) {
            Node n = nodes.item(nnum);
            if( n.getNodeType()==Node.ELEMENT_NODE ) {
                if( null==textValue((Element)n) ) {
                    parseXML( n, key+n.getNodeName()+"." );
                }
                else {
                    String value = textValue((Element) n);
                    String currentKey = key + n.getNodeName();
                    add(currentKey, value);
                }
            }
            else {
                // ignoring whitespace TEXT_NODEs
            }
        }
    }
    
    public void load(Document initArgs) throws IOException {        
        try {
            if( initArgs != null ) {
                // LogWriter.println("INF","Parsing initArgs...");
                NodeList root = initArgs.getElementsByTagName("initArgs");
                if( root.getLength()>0 ) {
                    parseXML(root.item(0), "");
                }
                else {
                    LogWriter.println("WRN","initArgs file doesn't have a <initArgs> node.");
                }
            }
            else {
                LogWriter.println("WRN", "Initialization arguments not supplied. Please add 'appArgumentsFile = initArgs.xml' or equiv. to the application configuration file. Running anyway.");
            }
        } catch( Exception e ) {
            LogWriter.println("SYS", "Exception while initializing GameBag: "+e);
            e.printStackTrace();
        }
        smudge(6,null);
    }
    
    
    private synchronized void parseBag(String source) {
        Vector items;
        String line, key, value;
        StringTokenizer lineTok, elementTok;

        LogWriter.println("dbg","Parsing Bag!");
        lineTok = new StringTokenizer(source, "\n\f\r" + System.getProperty("line.separator"));
        while (lineTok.hasMoreTokens()) {
            line = lineTok.nextToken();
            if (line.charAt(0) != '#') {
                elementTok = new StringTokenizer(line, " =\t");
                if (elementTok.countTokens() > 1) {
                    key = elementTok.nextToken();
                    while (elementTok.hasMoreTokens()) {
                        value = elementTok.nextToken();
                        if (value != null) this.add(key, value);
                    }
                    // LogWriter.println("add to bag " + key + " = " + items.toString());
                }
            }
        }
    }

    
    public void print() {        
        LogWriter.print(this.toString());
    }
    
   
    
    // -------------------------------------------------------------------------
    
    
    
    public String toString() {
        return this.toString(null);
    }


    public String toString(Object key) {
        Vector chosenKeys = new Vector();
        chosenKeys.add(key);
        return this.toString(chosenKeys);
    }
    
    
    
    public String toString(Vector chosenKeys) {
        StringBuffer sb = new StringBuffer(5000);
        String key;
        Thing value;
        sb.append("GenericBag [");
        for (Enumeration keys = this.keys(); keys.hasMoreElements();) {
            key = (String) keys.nextElement();
            if (chosenKeys == null || chosenKeys.contains(key)) {
                sb.append(key + " = ");
                value = (Thing) get(key);
                // LogWriter.print(key + " = " + values);
                if (value != null) {
                    sb.append(value.toString());
                    sb.append("\n");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    // --- Previously inherited methods ----------------------------------------
    
    public int size() {
        return myTable.size();
    }
    
    public void clear() {
        myTable.clear();  smudge(7,null);
    }
    
     public Object clone() {
        GenericBag bag = new GenericBag();
        bag.myTable = (Hashtable)this.myTable.clone();
        bag.dirty = this.dirty;
        return bag;
    }
    /*
    public Object clone() {
        GenericBag newBag = new GenericBag();
        newBag.myTable = (Hashtable)myTable.clone();
        return newBag;
    }
    */
    // Convenience methods including defaults for bag data access:
    // add new get*Default methods corresponding to get*In methods
    // Note these rely on get*In methods throwing exceptions consistently.
    public boolean getBooleanDefault(Object key, boolean def) {
        boolean rval = false;
        try {
            rval = getBooleanIn(key);
        } catch (Exception e) {
            rval = def;
        }
        return rval;
    }
    
    public String getStringDefault(Object key, String def) {
        String rval = "";
        try {
            rval = getStringIn(key);
        } catch (Exception e) {
            rval = def;
        }
        if (rval == null) rval = def;
        return rval;
    }
    
    public int getIntDefault(Object key, int def) {
        int rval = 0;
        try {
            rval = getIntIn(key);
        } catch (Exception e) {
            rval = def;
        }
        if (rval == 0) rval = def;
        return rval;
    }
    
    public long getLongDefault(Object key, long def) {
        long rval = 0;
        try {
            rval = getLongIn(key);
        } catch (Exception e) {
            rval = def;
        }
        if (rval == 0) rval = def;
        return rval;
    }
    
    // These two are strictly ONLY for custom serialization methods
    protected Object _getTable() {
        return myTable;
    }
    
    protected void _setTable( Object table ) {
        _setTable(table,false);
    }
    
    protected void _setTable( Object table, boolean setupTransient ) {
        myTable = (Hashtable)table;
        if( setupTransient )
            transientMembersNeedPlatform = true;
    }
    
    
    // ---  Serialization ------------------------------------------------------
    // Two cases: 1) GAF serialize() deserialize(),
    //            2) JAVA serialization writeObject(), readObject()
    // ...both must support any custom procedures (eg. transient member reviving!)
    
    
    public Hashtable serialize(Object rule) {
        return serialize();
    }
    
    
    public Hashtable serialize() {
        Hashtable serialized = new Hashtable();
        String key = null;
        Thing thing = null;
        Hashtable valueTable = null;
        String valueKey = null;
        Object value = null;
        for(Enumeration keys = myTable.keys(); keys.hasMoreElements(); ) {
            try {
                // for debugging:
                valueKey = null; valueTable = null; value = null; key = null;
                key = (String) keys.nextElement();
                thing = (Thing) get(key);
                valueTable = thing.serialize();
                for(Enumeration valueKeys = valueTable.keys(); valueKeys.hasMoreElements(); ) {
                    valueKey = (String) valueKeys.nextElement();
                    value = valueTable.get(valueKey);
                    serialized.put(key + "@" + valueKey, value);
                    // LogWriter.println("dbg", "  serializing " + key + "@" + valueKey + "=" + value);
                }
            }
            catch (Exception e) {
                LogWriter.println("ERR", "Unable to serialize bag key [" + key + "]: "+e);
            }
        }
        return serialized;
    }
    
    
    public void deserialize(Hashtable serialized,Object rule) {
        deserialize(serialized);
    }
    
    
    public void deserialize(Hashtable serialized) {
        Hashtable thingHashes = Toolbox.deserializeHash(serialized, "@");
        Hashtable thingHash = null;
        Object thingKey = null;
        for(Enumeration thingKeys = thingHashes.keys(); thingKeys.hasMoreElements(); ) {
            try {
                thingKey = thingKeys.nextElement();
                thingHash = (Hashtable)thingHashes.get(thingKey);
                Thing thing;
                if( thingKey instanceof String )
                    thing = myThingFactory.createThing( (String)thingHash.get(Thing.thingClassKey), (String)thingKey );
                else
                    thing = myThingFactory.createThing( (String)thingHash.get(Thing.thingClassKey), null );
                thing.deserialize(thingHash);
                myTable.put(thingKey, thing);
            }
            catch (Exception e) {
                LogWriter.println("ERR","Unable to deserialize thing hash with key [" + thingKey + "]: "+e);
                e.printStackTrace();
            }
        }
    }
    
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Setting up things' transient fields, TODO doesn't yet go deep inside...
        Enumeration enu = myTable.elements();
        while( enu.hasMoreElements() ) {
            Object o = enu.nextElement();
            // myTable should only contain Things but you never know...
            if( o instanceof Thing ) {
                Thing thing = (Thing)o;
                thing.init(this);
                
                if( thing.supportsRule("SET_THINGFACTORY") ) {
                    thing.add( this, "SET_THINGFACTORY" );
                }
            }
            else {
            }
        }
    }
    
}

