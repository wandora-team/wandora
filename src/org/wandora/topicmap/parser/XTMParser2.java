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
 */

package org.wandora.topicmap.parser;




import java.util.*;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapLogger;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.Options;
import org.xml.sax.*;




/**
 *
 * @author olli
 */
public class XTMParser2 implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

    public static final String OCCURRENCE_RESOURCE_REF_KEY = "topicmap.xtm2.convertOccurrenceResourceRefsToResourceDatas";
    public static final String IMPORT_XML_IDENTIFIERS_KEY = "topicmap.xtm2.importIdentifiers";
    public static final String ENSURE_UNIQUE_BASENAMES_KEY = "topicmap.xtm2.ensureUniqueBasenames";
    
    public static final int STATE_ROOT=0;
    public static final int STATE_TOPICMAP=1;
    public static final int STATE_TOPIC=2;
    public static final int STATE_NAME=3;
    public static final int STATE_VALUE=4;
    public static final int STATE_VARIANT=5;
    public static final int STATE_SCOPE=6;
    public static final int STATE_INSTANCEOF=7;
    public static final int STATE_TYPE=8;
    public static final int STATE_OCCURRENCE=9;
    public static final int STATE_RESOURCEDATA=10;
    public static final int STATE_ASSOCIATION=11;
    public static final int STATE_ROLE=12;
    
    public static final String E_TOPICMAP = "topicMap";
    public static final String E_VERSION = "versoin";
    public static final String E_TOPIC = "topic";
    public static final String E_NAME = "name";
    public static final String E_VALUE = "value";
    public static final String E_VARIANT = "variant";
    public static final String E_SCOPE = "scope";
    public static final String E_INSTANCEOF = "instanceOf";
    public static final String E_TYPE = "type";
    public static final String E_OCCURRENCE = "occurrence";
    public static final String E_RESOURCEDATA = "resourceData";
    public static final String E_ASSOCIATION = "association";
    public static final String E_ROLE = "role";
    public static final String E_TOPICREF = "topicRef";
    public static final String E_RESOURCEREF = "resourceRef";
    public static final String E_SUBJECTLOCATOR = "subjectLocator";
    public static final String E_SUBJECTIDENTIFIER = "subjectIdentifier";
    public static final String E_SUBJECTIDENTITY = "subjectIdentity";
    public static final String E_MERGEMAP = "mergeMap";
    public static final String E_ITEMIDENTITY = "itemIdentity";
    
    
    public static boolean CONVERT_OCCURRENCE_RESOURCE_REF_TO_RESOURCE_DATA = true;
    public static boolean ENSURE_UNIQUE_BASENAMES = false;
    public static boolean IMPORT_XML_IDENTIFIERS = false;
    
    
    protected TopicMap tm;
    
    protected TopicMapLogger logger;
    
    protected HashMap<String,String> idmapping;
    protected Stack<Integer> stateStack;
    protected int state;
    
    protected int topicCount;
    protected int associationCount;
    protected int occurrenceCount;
    protected int elementCount;
    
    
    public XTMParser2(TopicMap tm,TopicMapLogger logger){
        this.tm=tm;
        this.logger=logger;
        state=STATE_ROOT;
        stateStack=new Stack<Integer>();
        idmapping=new HashMap<String,String>();
        
        Wandora w = Wandora.getWandora();
        if(w != null) {
            Options options = w.getOptions();
            setOccurrenceResourceRef2ResourceData(options.getBoolean(OCCURRENCE_RESOURCE_REF_KEY, CONVERT_OCCURRENCE_RESOURCE_REF_TO_RESOURCE_DATA));
            setImportXmlIdentifiers(options.getBoolean(IMPORT_XML_IDENTIFIERS_KEY, IMPORT_XML_IDENTIFIERS));
            setEnsureUniqueBasenames(options.getBoolean(ENSURE_UNIQUE_BASENAMES_KEY, ENSURE_UNIQUE_BASENAMES));
        }
    }
    
    
    public void setOccurrenceResourceRef2ResourceData(boolean f) {
        CONVERT_OCCURRENCE_RESOURCE_REF_TO_RESOURCE_DATA = f;
    }
    
    
    public void setImportXmlIdentifiers(boolean f) {
        IMPORT_XML_IDENTIFIERS = f;
    }
    
    
    public void setEnsureUniqueBasenames(boolean f) {
        ENSURE_UNIQUE_BASENAMES = f;
    }
    
    
    protected void handleRoot(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TOPICMAP)){
            startTopicMap();
        }
        else logger.log("Expecting root element "+E_TOPICMAP+", got "+qName);
    }
    
    
    protected void endRoot(String uri, String localName, String qName) {
    }
    
    
    protected void startTopicMap(){
        stateStack.push(state);
        state=STATE_TOPICMAP;
    }
    
    
    protected void handleTopicMap(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_VERSION)){
            logger.log("Encountered "+E_VERSION+", ignoring");
        }
        else if(qName.equals(E_MERGEMAP)){
            logger.log("Encountered unsupported "+E_MERGEMAP+", ignoring");
        }
        else if(qName.equals(E_TOPIC)){
            startTopic(atts);
        }
        else if(qName.equals(E_ASSOCIATION)){
            startAssociation();
        }
        else logger.log("Expecting one of "+E_VERSION+", "+E_MERGEMAP+", "+E_TOPIC+", "+E_ASSOCIATION+", got "+qName);
    }
    
    
    protected void endTopicMap(String uri, String localName, String qName){
        if(qName.equals(E_TOPICMAP)){
            postProcessTopicMap();
        }
    }
    
    
    /**
     * Remove temporary subject identifiers created during parse. This is
     * necessary as temporary identifiers are reused during next parse.
     * If a topic has only temporary subject identifier, a permanent
     * subject identifier is created for the topic.
     */
    protected void postProcessTopicMap(){
        try {
            // remove temporary subject identifiers
            Iterator<Topic> topics=tm.getTopics();
            Collection topicCollection = new ArrayList<Topic>();
            while(topics.hasNext()) {
                Topic t=topics.next();
                topicCollection.add(t);
            }
            
            topics = topicCollection.iterator();
            while(topics.hasNext()) {
                Topic t=topics.next();
                ArrayList<Locator> sis = new ArrayList<Locator>(t.getSubjectIdentifiers());
                int sisSize = sis.size();
                for(Locator si : sis) {
                    if(si.toExternalForm().startsWith(temporarySI)) {
                        if(sisSize < 2) {
                            // create permanent subject identifier before temporary can be removed.
                            String permanentSI = "http://wandora.org/si/xtm2/permanent/" + System.currentTimeMillis() + "-" + Math.round(Math.random()*999999);
                            // System.out.println("adding si "+permanentSI);
                            t.addSubjectIdentifier(new Locator( permanentSI ));
                        }
                        // System.out.println("Removing si "+si.toExternalForm());
                        t.removeSubjectIdentifier(si);
                    }
                }
            }
        }
        catch(TopicMapException tme){
            logger.log(tme);
        }        
    }
    
    
    protected ParsedTopic parsedTopic;
    protected void startTopic(Attributes atts) {
        stateStack.push(state);
        state=STATE_TOPIC;
        parsedTopic=new ParsedTopic();
        parsedTopic.id=atts.getValue("id");
        topicCount++;
    }
    
    
    protected void handleTopic(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_ITEMIDENTITY)) {
            String href=handleHRef(qName, atts);
            if(href!=null) parsedTopic.itemIdentities.add(href);
        }
        else if(qName.equals(E_SUBJECTLOCATOR)) {
            String href=handleHRef(qName, atts);
            if(href!=null) parsedTopic.subjectLocators.add(href);

        }
        else if(qName.equals(E_SUBJECTIDENTIFIER)) {
            String href=handleHRef(qName, atts);
            if(href!=null) parsedTopic.subjectIdentifiers.add(href);
        }
        else if(qName.equals(E_INSTANCEOF)) startInstanceOf();
        else if(qName.equals(E_NAME)) startName();
        else if(qName.equals(E_OCCURRENCE)) startOccurrence();
        else logger.log("Expecting one of "+E_ITEMIDENTITY+", "+E_SUBJECTLOCATOR+", "+E_SUBJECTIDENTIFIER+", "+E_INSTANCEOF+", "+E_NAME+", "+E_OCCURRENCE+", got "+qName);
    }
    
    
    protected void endTopic(String uri, String localName, String qName){
        if(qName.equals(E_TOPIC)){
            state=stateStack.pop();
            processTopic();
        }
    }
    
    
    protected void processTopic(){
        try{
            Topic t=null;
            if(parsedTopic.id!=null) {
                t=getOrCreateTopicID("#"+parsedTopic.id);
            }
            else {
                t=tm.createTopic();
            }

            if(parsedTopic.types!=null){
                ArrayList<Topic> types=processTopicRefs(parsedTopic.types);
                for(Topic type : types){
                    t.addType(type);
                }
            }
            for(String si : parsedTopic.subjectIdentifiers){
                t.addSubjectIdentifier(tm.createLocator(si));
            }
            if(parsedTopic.itemIdentities.size()>0){
                logger.log("Warning, converting item identities to subject identifiers");
                for(String si : parsedTopic.itemIdentities){
                    t.addSubjectIdentifier(tm.createLocator(si));
                }
            }
            if(parsedTopic.subjectLocators.size()>1)
                logger.log("Warning, more than one subject locator found, ignoring all but one");
            if(parsedTopic.subjectLocators.size()>0){
                t.setSubjectLocator(tm.createLocator(parsedTopic.subjectLocators.get(0)));
            }

            for(ParsedName name : parsedTopic.names){

                ArrayList<Topic> scope=processTopicRefs(name.scope);
                if(name.type!=null) {
                    logger.log("Warning, name has type, moving to scope");
//                    if(name.scope==null) name.scope=new ArrayList<Topic>();
                    if(scope==null) scope=new ArrayList<Topic>();
                    scope.add(getOrCreateTopicRef(name.type));
                }

                if(name.value!=null && name.value.length()>0){
//                    if(name.scope==null || name.scope.size()==0){
                    if(scope==null || scope.isEmpty()){
                        if(ENSURE_UNIQUE_BASENAMES) {
                            int i = 2;
                            if(tm.getTopicWithBaseName(name.value) != null) {
                                while(tm.getTopicWithBaseName(name.value+" ("+i+")") != null) i++;
                                name.value = name.value+" ("+i+")";
                            }
                        }
                        t.setBaseName(name.value);
                    }
                    else {
//                        t.setVariant(new HashSet<Topic>(name.scope), name.value);
                        t.setVariant(new LinkedHashSet<Topic>(scope), name.value);
                    }
                }

                for(ParsedVariant v : name.variants) {
                    HashSet<Topic> s=new LinkedHashSet<Topic>();
//                    if(name.scope!=null) s.addAll(name.scope);
                    if(name.scope!=null) s.addAll(scope);
//                    if(v.scope!=null) s.addAll(v.scope);
                    if(v.scope!=null) s.addAll(processTopicRefs(v.scope));
                    t.setVariant(s,v.data);
                }


            }

            for(ParsedOccurrence o : parsedTopic.occurrences){
                if(o.type==null) {
                    logger.log("Warning, occurrence has no type, skipping.");
                    continue;
                }
                if(o.ref!=null) {
                    if(CONVERT_OCCURRENCE_RESOURCE_REF_TO_RESOURCE_DATA) {
                        logger.log("Converting resource ref occurrence to resource data occurrence");
                        Topic version=null;
                        if(o.scope==null || o.scope.isEmpty()) {
                            logger.log("Warning, occurrence has no scope, using lang independent");
                            version=getOrCreateTopic(XTMPSI.getLang(null));
                        }
                        else {
                            if(o.scope.size()>1) logger.log("Warning, variant scope has more than one topic, ignoring all but one.");
                            version=getOrCreateTopicRef(o.scope.get(0));
                        }
                        t.setData(getOrCreateTopicRef(o.type), version, o.ref);
                    }
                    else {
                        logger.log("Converting resource ref occurrence to association");

                        Topic t2=tm.createTopic();
                        t2.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());

                        t2.setBaseName("Occurrence file: "+o.ref);
                        t2.setSubjectLocator(tm.createLocator(o.ref));
                        Topic orole=getOrCreateTopic("http://wandora.org/si/compatibility/occurrencerolereference");
                        Topic trole=getOrCreateTopic("http://wandora.org/si/compatibility/occurrenceroletopic");
    //                    Association a=tm.createAssociation(o.type);
                        Association a=tm.createAssociation(getOrCreateTopicRef(o.type));
                        a.addPlayer(t,trole);
                        a.addPlayer(t2,orole);
                    }
                }
                else if(o.data==null){
                    logger.log("Warning, occurrence has no data or resource ref, skipping.");
                    continue;
                }
                else{
                    Topic version=null;
                    if(o.scope==null || o.scope.isEmpty()) {
                        logger.log("Warning, occurrence has no scope, using lang independent");
                        version=getOrCreateTopic(XTMPSI.getLang(null));
                    }
                    else {
                        if(o.scope.size()>1) logger.log("Warning, variant scope has more than one topic, ignoring all but one.");
//                        version=o.scope.get(0);
                        version=getOrCreateTopicRef(o.scope.get(0));
                    }
//                    t.setData(o.type, version, o.data);
                    t.setData(getOrCreateTopicRef(o.type), version, o.data);
                }
            }
        }
        catch(TopicMapException tme){
            logger.log(tme);
        }
    }

    
    protected ParsedName parsedName;
    protected void startName(){
        stateStack.push(state);
        state=STATE_NAME;
        parsedName=new ParsedName();
    }
    
    
    protected void handleName(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TYPE)) startType();
        else if(qName.equals(E_SCOPE)) startScope();
        else if(qName.equals(E_VALUE)) startValue();
        else if(qName.equals(E_RESOURCEREF)){
            logger.log("Unsupported resourceRef in name, skipping.");
        }
        else if(qName.equals(E_VARIANT)) startVariant();
        else logger.log("Expecting one of "+E_TYPE+", "+E_SCOPE+", "+E_RESOURCEDATA+", "+E_RESOURCEREF+", got "+qName);
    }
    
    
    protected void endName(String uri, String localName, String qName){
        if(qName.equals(E_NAME)){
            state=stateStack.pop();
            if(state==STATE_TOPIC) parsedTopic.names.add(parsedName);
        }
    }
    
    
    protected String parsedCharacters;
    protected String parsedValue;
    protected void startValue(){
        stateStack.push(state);
        state=STATE_VALUE;        
        parsedCharacters="";
        parsedValue=null;        
    }
    
    
    protected void handleValue(String uri, String localName, String qName, Attributes atts){
    }
    
    
    protected void endValue(String uri, String localName, String qName){
        if(qName.equals(E_VALUE)){
            parsedValue=parsedCharacters;
            state=stateStack.pop();
            if(state==STATE_NAME) parsedName.value=parsedValue;
        }
    }

    
    protected ParsedVariant parsedVariant;
    protected void startVariant(){
        stateStack.push(state);
        state=STATE_VARIANT;
        parsedVariant=new ParsedVariant();
    }
    
    
    protected void handleVariant(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_SCOPE)) startScope();
        else if(qName.equals(E_RESOURCEREF)){
            logger.log("Unsupported variant element "+E_RESOURCEDATA);
        }
        else if(qName.equals(E_RESOURCEDATA)) startResourceData();
        else logger.log("Expecting one of "+E_SCOPE+", "+E_RESOURCEREF+", "+E_RESOURCEDATA+", got "+qName);
    }
    
    
    protected void endVariant(String uri, String localName, String qName){
        if(qName.equals(E_VARIANT)){
            state=stateStack.pop();
            if(state==STATE_NAME) parsedName.variants.add(parsedVariant);
        }
    }
    
    
    protected String handleHRef(String qName, Attributes atts) {
        String href=atts.getValue("href");
        if(href==null) logger.log("Expecting attribute href in "+qName);
        return href;
    }
    
    
//    protected ArrayList<Topic> parsedScope;
    protected ArrayList<String> parsedScope;
    protected void startScope(){
        stateStack.push(state);
        state=STATE_SCOPE;
//        parsedScope=new ArrayList<Topic>();
        parsedScope=new ArrayList<String>();
    }
    
    
    protected void handleScope(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TOPICREF)){
//            try{
                String href=handleHRef(qName,atts);
                if(href!=null){
//                    Topic t=getOrCreateTopicRef(href);
//                    if(t!=null) parsedScope.add(t);
                    parsedScope.add(href);
                }
//            }catch(TopicMapException tme){logger.log(tme);}
        }
        else logger.log("Expecting "+E_TOPICREF+", got "+qName);
    }
    
    
    protected void endScope(String uri, String localName, String qName){
        if(qName.equals(E_SCOPE)){
            state=stateStack.pop();
            if(state==STATE_VARIANT) parsedVariant.scope=parsedScope;
            else if(state==STATE_OCCURRENCE) parsedOccurrence.scope=parsedScope;
            else if(state==STATE_ASSOCIATION) parsedAssociation.scope=parsedScope;
            else if(state==STATE_NAME) parsedName.scope=parsedScope;
        }
    }
    
    
    
//    protected ArrayList<Topic> parsedInstances;
    protected ArrayList<String> parsedInstances;
    protected void startInstanceOf(){
        stateStack.push(state);
        state=STATE_INSTANCEOF;
//        parsedInstances=new ArrayList<Topic>();
        parsedInstances=new ArrayList<String>();
    }
    
    
    protected void handleInstanceOf(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TOPICREF)){
//            try{
                String href=handleHRef(qName,atts);
                if(href!=null){
//                    Topic t=getOrCreateTopicRef(href);
//                    if(t!=null) parsedInstances.add(t);
                    parsedInstances.add(href);
                }
//            }catch(TopicMapException tme){logger.log(tme);}
        }
        else logger.log("Expecting "+E_TOPICREF+", got "+qName);        
    }
    
    
    protected void endInstanceOf(String uri, String localName, String qName){
        if(qName.equals(E_INSTANCEOF)){
            state=stateStack.pop();
            if(state==STATE_TOPIC) parsedTopic.types=parsedInstances;
        }
    }
    
    
//    protected Topic parsedType;
    protected String parsedType;
    protected void startType(){
        stateStack.push(state);
        state=STATE_TYPE;
        parsedType=null;        
    }
    
    
    protected void handleType(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TOPICREF)){
            if(parsedType!=null) logger.log("Encountered another topicRef in type, overwriting previous.");
            String href=handleHRef(qName,atts);
            if(href!=null){
//                try{
//                    parsedType=getOrCreateTopicRef(href);
                    parsedType=href;
//                }catch(TopicMapException tme){logger.log(tme);}
            }
        }
        else logger.log("Expecting "+E_TOPICREF+", got "+qName);                
    }
    
    
    protected void endType(String uri, String localName, String qName){
        if(qName.equals(E_TYPE)){
            state=stateStack.pop();
            if(state==STATE_OCCURRENCE) parsedOccurrence.type=parsedType;
            else if(state==STATE_ROLE) parsedRole.type=parsedType;
            else if(state==STATE_ASSOCIATION) parsedAssociation.type=parsedType;
            else if(state==STATE_NAME) parsedName.type=parsedType;
        }
    }
    
    
    protected ParsedOccurrence parsedOccurrence;
    protected void startOccurrence(){
        stateStack.push(state);
        state=STATE_OCCURRENCE;
        parsedOccurrence=new ParsedOccurrence();
        occurrenceCount++;
    }
    
    
    protected void handleOccurrence(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TYPE)) startType();
        else if(qName.equals(E_SCOPE)) startScope();
        else if(qName.equals(E_RESOURCEREF)){
            String href=handleHRef(qName,atts);
            if(href!=null) parsedOccurrence.ref=href;
        }
        else if(qName.equals(E_RESOURCEDATA)) startResourceData();
        else logger.log("Expecting one of "+E_TYPE+", "+E_SCOPE+", "+E_RESOURCEREF+", "+E_RESOURCEDATA+", got "+qName);
    }
    
    
    protected void endOccurrence(String uri, String localName, String qName){
        if(qName.equals(E_OCCURRENCE)){
            state=stateStack.pop();
            if(state==STATE_TOPIC) parsedTopic.occurrences.add(parsedOccurrence);
        }
    }
    
    
    protected String parsedResourceData;
    protected void startResourceData(){
        stateStack.push(state);
        state=STATE_RESOURCEDATA;        
        parsedCharacters="";
        parsedResourceData=null;
    }
    
    
    protected void handleResourceData(String uri, String localName, String qName, Attributes atts){
    }
    
    
    protected void endResourceData(String uri, String localName, String qName){
        if(qName.equals(E_RESOURCEDATA)){
            parsedResourceData=parsedCharacters;
            state=stateStack.pop();
            if(state==STATE_VARIANT) parsedVariant.data=parsedResourceData;
            else if(state==STATE_OCCURRENCE) parsedOccurrence.data=parsedResourceData;
        }
    }

    
    protected ParsedAssociation parsedAssociation;
    protected void startAssociation(){
        stateStack.push(state);
        state=STATE_ASSOCIATION;
        parsedAssociation=new ParsedAssociation();
        associationCount++;
    }
    
    
    protected void handleAssociation(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TYPE)) startType();
        else if(qName.equals(E_SCOPE)) {
            logger.log("Warning, scope not supported in association");
            startScope();
        }
        else if(qName.equals(E_ROLE)) startRole();
        else logger.log("Expecting one of "+E_TYPE+", "+E_SCOPE+", "+E_ROLE+", got "+qName);
    }
    
    
    protected void endAssociation(String uri, String localName, String qName){
        if(qName.equals(E_ASSOCIATION)){
            state=stateStack.pop();
            processAssociation();
        }
    }
    
    
    protected void processAssociation() {
        if(parsedAssociation.type==null) logger.log("No type in association");
        else if(parsedAssociation.roles.isEmpty()) logger.log("No players in association");
        else {
            try {
//                Association a=tm.createAssociation(parsedAssociation.type);
                Association a=tm.createAssociation(getOrCreateTopicRef(parsedAssociation.type));
                for(ParsedRole r : parsedAssociation.roles) {
//                    a.addPlayer(r.topic,r.type);
                    a.addPlayer(getOrCreateTopicRef(r.topic),getOrCreateTopicRef(r.type));
                }
            }
            catch(TopicMapException tme){logger.log(tme);}
        }        
    }
    
    
    protected ParsedRole parsedRole;
    protected void startRole() {
        stateStack.push(state);
        state=STATE_ROLE;
        parsedRole=new ParsedRole();
    }
    
    
    protected void handleRole(String uri, String localName, String qName, Attributes atts){
        if(qName.equals(E_TYPE)){
            startType();
        }
        else if(qName.equals(E_TOPICREF)){
            String href=handleHRef(qName,atts);
            if(href!=null){
//                try{
//                    Topic t=getOrCreateTopicRef(href);
//                    if(t!=null) parsedRole.topic=t;
                    parsedRole.topic=href;
//                } catch(TopicMapException tme){logger.log(tme);}
            }
        }
        else logger.log("Expecting one of "+E_TYPE+", "+E_TOPICREF+", got "+qName);
    }
    
    
    protected void endRole(String uri, String localName, String qName){
        if(qName.equals(E_ROLE)) {
            state=stateStack.pop();
            if(state==STATE_ASSOCIATION) parsedAssociation.roles.add(parsedRole);
        }
    }
    
    
    protected static class ParsedTopic{
        public String id;
        public ArrayList<String> itemIdentities;
        public ArrayList<String> subjectLocators;
        public ArrayList<String> subjectIdentifiers;
//        public ArrayList<Topic> types;
        public ArrayList<String> types;
        public ArrayList<ParsedName> names;
        public ArrayList<ParsedOccurrence> occurrences;
        public ParsedTopic(){
            itemIdentities=new ArrayList<String>();
            subjectLocators=new ArrayList<String>();
            subjectIdentifiers=new ArrayList<String>();
            names=new ArrayList<ParsedName>();
            occurrences=new ArrayList<ParsedOccurrence>();
        }
    }

    
    protected static class ParsedAssociation{
        public ArrayList<ParsedRole> roles;
//        public ArrayList<Topic> scope;
        public ArrayList<String> scope;
//        public Topic type;
        public String type;
        public ParsedAssociation(){
            roles=new ArrayList<ParsedRole>();
        }
    }

    
    protected static class ParsedName{
//        public Topic type;
        public String type;
//        public ArrayList<Topic> scope;
        public ArrayList<String> scope;
        public String value;
        public ArrayList<ParsedVariant> variants;
        public ParsedName(){
            variants=new ArrayList<ParsedVariant>();
        }
    }
    
    
    protected static class ParsedOccurrence{
//        public Topic type;
        public String type;
//        public ArrayList<Topic> scope;
        public ArrayList<String> scope;
        public String ref;
        public String data;
        public ParsedOccurrence(){
        }
    }
    
    
    protected static class ParsedVariant{
//        public ArrayList<Topic> scope;
        public ArrayList<String> scope;
        public String data;
        public ParsedVariant(){
        }
    }
    
    
    protected static class ParsedRole {
//        public Topic type;
        public String type;
//        public Topic topic;
        public String topic;
        public ParsedRole(){
        }
    }
    
    //////////////////////////////
    
    protected ArrayList<Topic> processTopicRefs(ArrayList<String> hrefs) throws TopicMapException {
        if(hrefs==null) return null;
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(String href : hrefs) {
            ret.add(getOrCreateTopicRef(href));
        }
        return ret;
    }
    
    
    protected Topic getOrCreateTopicRef(String ref) throws TopicMapException {
        String si=idmapping.get(ref);
        if(si!=null) return getOrCreateTopic(si);
        Topic t=tm.getTopic(ref);
        if(t!=null) return t;
        si=temporarySI+ref;
        idmapping.put(ref,si);
        return getOrCreateTopic(si, hrefToId(ref));        
    }
    
    
    protected String hrefToId(String ref) {
        if(ref != null) {
            if(ref.startsWith("#")) {
                return ref.substring(1);
            }
        }
        return ref;
    }
    
    
    protected Topic getOrCreateTopic(String si) throws TopicMapException {
        Topic t=tm.getTopic(si);
        if(t==null) {
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(si));
        }
        return t;
    }
    
    
    protected Topic getOrCreateTopic(String si, String id) throws TopicMapException {
        Topic t=tm.getTopic(si);
        if(t==null) {
            if(id != null && IMPORT_XML_IDENTIFIERS) {
                t = tm.createTopic(hrefToId(id));
            }
            else {
                t = tm.createTopic();
            }
            t.addSubjectIdentifier(tm.createLocator(si));
        }
        return t;
    }
    
    
    /*
     * TemporarySI should *NOT* be same as the default temporary subject identifier
     * path. XTMParser2 removes subject identifiers based on the temporarySI 
     * after parse. Wandora removes temporary subject identifiers after parse.
     * Look at the method postProcessTopicMap above.
     */
    protected String temporarySI="http://wandora.org/si/xtm2/temp/";
    
    protected Topic getOrCreateTopicID(String id) throws TopicMapException {
        String si=idmapping.get(id);
        if(si!=null) {
            return getOrCreateTopic(si, hrefToId(id));
        }
        
        si=temporarySI+id;
        idmapping.put(id,si);
        return getOrCreateTopic(si, hrefToId(id));
    }
    
    
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(state){
            case STATE_RESOURCEDATA: case STATE_VALUE:
                parsedCharacters+=new String(ch,start,length);
                break;
        }
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if(logger.forceStop()) {
            throw new org.xml.sax.SAXException("user_interrupt");
        }
//        logger.log("Start element "+qName+" state="+state);
        switch(state){
            case STATE_ROOT:
                handleRoot(uri, localName, qName, atts);
                break;
            case STATE_TOPICMAP:
                handleTopicMap(uri, localName, qName, atts);
                break;
            case STATE_TOPIC:
                handleTopic(uri, localName, qName, atts);
                break;
            case STATE_NAME:
                handleName(uri, localName, qName, atts);
                break;
            case STATE_VALUE:
                handleValue(uri, localName, qName, atts);
                break;
            case STATE_VARIANT:
                handleVariant(uri, localName, qName, atts);
                break;
            case STATE_SCOPE:
                handleScope(uri, localName, qName, atts);
                break;
            case STATE_INSTANCEOF:
                handleInstanceOf(uri, localName, qName, atts);
                break;
            case STATE_TYPE:
                handleType(uri, localName, qName, atts);
                break;
            case STATE_OCCURRENCE:
                handleOccurrence(uri, localName, qName, atts);
                break;
            case STATE_RESOURCEDATA:
                handleResourceData(uri, localName, qName, atts);
                break;
            case STATE_ASSOCIATION:
                handleAssociation(uri, localName, qName, atts);
                break;
            case STATE_ROLE:
                handleRole(uri, localName, qName, atts);
                break;
        }
//        logger.log("       state="+state);
        if( (elementCount++ % 10000) == 9999) {
            logger.hlog("Importing XTM (2.0) topic map.\nFound " + topicCount + " topics, " + associationCount + " associations and "+ occurrenceCount + " occurrences.");
        }
    }
    
    
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
//        logger.log("End element "+qName+" state="+state);
        switch(state){
            case STATE_ROOT:
                endRoot(uri, localName, qName);
                break;
            case STATE_TOPICMAP:
                endTopicMap(uri, localName, qName);
                break;
            case STATE_TOPIC:
                endTopic(uri, localName, qName);
                break;
            case STATE_NAME:
                endName(uri, localName, qName);
                break;
            case STATE_VALUE:
                endValue(uri, localName, qName);
                break;
            case STATE_VARIANT:
                endVariant(uri, localName, qName);
                break;
            case STATE_SCOPE:
                endScope(uri, localName, qName);
                break;
            case STATE_INSTANCEOF:
                endInstanceOf(uri, localName, qName);
                break;
            case STATE_TYPE:
                endType(uri, localName, qName);
                break;
            case STATE_OCCURRENCE:
                endOccurrence(uri, localName, qName);
                break;
            case STATE_RESOURCEDATA:
                endResourceData(uri, localName, qName);
                break;
            case STATE_ASSOCIATION:
                endAssociation(uri, localName, qName);
                break;
            case STATE_ROLE:
                endRole(uri, localName, qName);
                break;
        }
//        logger.log("       state="+state);
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
    public void endDocument() throws SAXException {
        logger.log("Importing XTM (2.0) topic map.\nFound " + topicCount + " topics, " + associationCount + " associations and "+ occurrenceCount + " occurrences.");
    }


    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        logger.log(exception);
    }

}
