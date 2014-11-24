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
 *
 * JTMParser.java
 *
 * Created on May 18, 2009, 10:38 AM
 */




package org.wandora.topicmap.parser;



import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.wandora.application.Wandora;
import org.wandora.topicmap.*;
import org.wandora.utils.Options;
import org.wandora.utils.UnicodeBOMInputStream;
import org.wandora.utils.UnicodeBOMInputStream.BOM;




/**
 *
 * @author akivela
 */
public class LTMParser {
    
    public static final String OPTIONS_BASE_KEY = "topicmap.ltm";
    public static final String OPTIONS_KEY_ALLOW_SPECIAL_CHARS_IN_QNAMES = OPTIONS_BASE_KEY+"allowSpecialCharsInQNames";
    public static final String OPTIONS_KEY_NEW_OCCURRENCE_FOR_EACH_SCOPE = OPTIONS_BASE_KEY+"newOccurrenceForEachScope";
    public static final String OPTIONS_KEY_REJECT_ROLELESS_MEMBERS = OPTIONS_BASE_KEY+"rejectRolelessMembers";
    public static final String OPTIONS_KEY_PREFER_CLASS_AS_ROLE = OPTIONS_BASE_KEY+"preferClassAsRole";
    public static final String OPTIONS_KEY_FORCE_UNIQUE_BASENAMES = OPTIONS_BASE_KEY+"forceUniqueBasenames";
    public static final String OPTIONS_KEY_TRIM_BASENAMES = OPTIONS_BASE_KEY+"trimBasenames";
    public static final String OPTIONS_KEY_OVERWRITE_VARIANTS = OPTIONS_BASE_KEY+"overwriteVariants";
    public static final String OPTIONS_KEY_OVERWRITE_BASENAME = OPTIONS_BASE_KEY+"overwriteBasename";
    public static final String OPTIONS_KEY_DEBUG = OPTIONS_BASE_KEY+"debug";
    public static final String OPTIONS_KEY_MAKE_SUBJECT_IDENTIFIER_FROM_ID = OPTIONS_BASE_KEY+"makeSIfromID";
    
    public static boolean ALLOW_SPECIAL_CHARS_IN_QNAMES = false;
    public static boolean NEW_OCCURRENCE_FOR_EACH_SCOPE = true;
    public static boolean REJECT_ROLELESS_MEMBERS = false;
    public static boolean PREFER_CLASS_AS_ROLE = false; // true;
    public static boolean MAKE_SUBJECT_IDENTIFIER_FROM_ID = false;
    public static boolean MAKE_BASENAME_FROM_ID = false;
    //public static boolean MAKE_VARIANT_FROM_BASENAME = false; // TODO
    public static boolean FORCE_UNIQUE_BASENAMES = false;
    public static boolean TRIM_BASENAMES = true;
    public static boolean OVERWRITE_VARIANTS = true;
    public static boolean OVERWRITE_BASENAME = true;
    public static boolean OVERWRITE_SUBJECT_LOCATORS = false;

    public static int MAX_SI_LEN = 9999;
    public static int MAX_NAME_LEN = 9999;
    public static int MAX_STRING_LEN = 99999;

    public static String DEFAULT_ROLE_IDENTIFIER = "role";
    public static String DEFAULT_SCOPE_FOR_OCCURRENCES = TMBox.LANGINDEPENDENT_SI;
    public static String DEFAULT_SCOPE_FOR_VARIANTS = TMBox.LANGINDEPENDENT_SI;
    public static String DEFAULT_TYPE_FOR_VARIANTS = "http://www.topicmaps.org/xtm/1.0/core.xtm#display";

    public static String DEFAULT_BASE_URI = "http://wandora.org/si/ltm-import/";
    public static String TEMP_SI_PREFIX = "http://wandora.org/si/temp/ltm-import/";
    
    private Topic defaultRoleForAssociations = null;
    private Topic defaultScopeForOccurrences = null;
    private Topic defaultScopeForVariants = null;
    private Topic defaultTypeForVariants = null;

    private File currentFile = null;
    private String ltmuri = null;
    private String baseuri = null;
    private String encoding = null;
    private String version = null;
    private HashMap<String,String> indicatorPrefixes = new HashMap();
    private HashMap<String,String> locatorPrefixes = new HashMap();

    private ArrayList includes = new ArrayList();
    private Pattern prefixPattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*\\:.+");

    private int numberOfTopics = 0;
    private int numberOfAssociations = 0;
    private int numberOfOccurrences = 0;
    private int numberOfFailedTopics = 0;
    private int numberOfFailedAssociations = 0;
    private int numberOfFailedOccurrences = 0;

    private int lineCounter = 0;

    private BufferedReader in = null;

    private TopicMapLogger logger = null;

    public static boolean debug = false;
    private boolean proceed = true;

    private TopicMap topicMap = null;





    public LTMParser(TopicMap tm, TopicMapLogger logger) {
        this.topicMap = tm;
        if(logger != null) this.logger = logger;
        else this.logger = topicMap;
        
        Wandora w = Wandora.getWandora();
        if(w != null) {
            Options o = w.getOptions();
            if(o != null) {
                loadOptions(o);
            }
        }
    }

    
    
    public void loadOptions(Options o) {
        ALLOW_SPECIAL_CHARS_IN_QNAMES = o.getBoolean(OPTIONS_KEY_ALLOW_SPECIAL_CHARS_IN_QNAMES, ALLOW_SPECIAL_CHARS_IN_QNAMES);
        NEW_OCCURRENCE_FOR_EACH_SCOPE = o.getBoolean(OPTIONS_KEY_NEW_OCCURRENCE_FOR_EACH_SCOPE, NEW_OCCURRENCE_FOR_EACH_SCOPE);
        REJECT_ROLELESS_MEMBERS = o.getBoolean(OPTIONS_KEY_REJECT_ROLELESS_MEMBERS, REJECT_ROLELESS_MEMBERS);
        PREFER_CLASS_AS_ROLE = o.getBoolean(OPTIONS_KEY_PREFER_CLASS_AS_ROLE, PREFER_CLASS_AS_ROLE);
        FORCE_UNIQUE_BASENAMES = o.getBoolean(OPTIONS_KEY_FORCE_UNIQUE_BASENAMES, FORCE_UNIQUE_BASENAMES);
        TRIM_BASENAMES = o.getBoolean(OPTIONS_KEY_TRIM_BASENAMES, TRIM_BASENAMES);
        OVERWRITE_VARIANTS = o.getBoolean(OPTIONS_KEY_OVERWRITE_VARIANTS, OVERWRITE_VARIANTS);
        OVERWRITE_BASENAME = o.getBoolean(OPTIONS_KEY_OVERWRITE_BASENAME, OVERWRITE_BASENAME);
        debug = o.getBoolean(OPTIONS_KEY_DEBUG, debug);
        MAKE_SUBJECT_IDENTIFIER_FROM_ID = o.getBoolean(OPTIONS_KEY_MAKE_SUBJECT_IDENTIFIER_FROM_ID, MAKE_SUBJECT_IDENTIFIER_FROM_ID);
    }
    

    public void prepare() {
        encoding = null;
    }



    public void init() {
        indicatorPrefixes = new HashMap();
        locatorPrefixes = new HashMap();
        lineCounter = 1;
    }



    public void parse(File file) {
        long startTime = System.currentTimeMillis();
        try {
            if(file != null) {
                if(!(file.exists() || file.canRead()) && currentFile != null) {
                    String absParentPath = currentFile.getParentFile().getAbsolutePath();
                    logger.log("Using path from previous file: " + absParentPath + File.separator + file.getName());
                    file = new File(absParentPath + File.separator + file.getName());
                }
                if(file.exists() && file.canRead()) {
                    if(currentFile == null) currentFile = file;
                    File previousFile = currentFile;
                    BufferedReader previousIn = in;
                    String previousBaseuri = baseuri;
                    String previousLtmuri = ltmuri;
                    // ltmuri = "file:/" + file.getAbsolutePath();
                    ltmuri = file.toURI().toString();
                    InputStream is=new FileInputStream(file);
                    parse(is);
                    if(previousIn != null) in = previousIn;
                    if(ltmuri != null) ltmuri = previousLtmuri;
                    if(baseuri != null) baseuri = previousBaseuri;
                    if(previousFile != null) currentFile = previousFile;
                }
                else {
                    logger.log("Warning: LTM import is unable to read file: " + file.getAbsolutePath());
                }
            }
        }
        catch(Exception e) {
            logger.log(e);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime-startTime;
        if(duration > 1000) logger.log("LTM import of '"+file.getAbsolutePath()+"' took "+duration+" ms.");
    }



    public void parse(InputStream is) throws IOException {
        parse(is, "UTF-8");
    }
    
    
    public void parse(InputStream is, String enc) throws IOException {
        if(enc == null || enc.equals("")) enc = "UTF-8";
        prepare();

        InputStreamReader isr = new InputStreamReader(is, enc);
        in = new BufferedReader(isr);
        
        eat('\ufeff'); // skip BOM
        parseEncodind();

        if(encoding != null) {
            if(!"utf-8".equalsIgnoreCase(encoding)) {
                logger.log("Warning: Wandora's LTM import supports UTF-8 encoding only! Imported LTM document has '"+encoding+"' as encoding.");
            }
        }

        parseVersion();
        parseDirectives();
        parseTopicElements();
        
        postProcess();
        
        if(logger.forceStop()) {
            logger.log("User has stopped LTM import!");
        }
    }

    
    
    private void parseEncodind() throws IOException {
        eatMeaningless();
        if(eat('@')) {
            encoding = parseString();
        }
    }

    private void parseVersion() throws IOException {
        eatMeaningless();
        if(eat("#VERSION")) {
            version = parseString();
            logger.log("Found LTM version info: "+version);
        }
    }


    private void parseDirectives() throws IOException {
        boolean directiveFound = true;
        while(directiveFound) {
            eatMeaningless();
            if(eat("#TOPICMAP")) {
                logger.log("Warning: Wandora's LTM import does not handle #TOPICMAP directives!");
            }
            else if(eat("#MERGEMAP")) {
                logger.log("Warning: Wandora's LTM import does not handle #MERGEMAP directives!");
            }
            else if(eat("#BASEURI")) {
                eatMeaningless();
                String uri = parseString();
                if(uri != null && uri.length()>0) {
                    logger.log("Base URI found '"+uri+"'.");
                    baseuri = uri;
                }
            }
            else if(eat("#INCLUDE")) {
                eatMeaningless();
                String filename = parseString();
                if(! includes.contains(filename) ) {
                    includes.add(filename);
                    if(debug) System.out.println("Including '" + filename + "' starts.");
                    int oldLineCounter = lineCounter;
                    lineCounter = 1;
                    parse(new File(filename));
                    proceed = true;
                    lineCounter = oldLineCounter;
                    if(debug) System.out.println("Including '" + filename + "' ends.");
                }
            }
            else if(eat("#PREFIX")) {
                eatMeaningless();
                String name = parseName();
                eatMeaningless();
                if(eat('@')) {
                    String locator = parseString();
                    indicatorPrefixes.put(name, locator);
                    logger.log("Prefix found '" + name + "' = '" + locator + "'");
                }
                else if(eat('%')) {
                    String identier = parseString();
                    locatorPrefixes.put(name, identier);
                    logger.log("Prefix found '" + name + "' = '" + identier + "'");
                }
            }
            else {
                directiveFound = false;
            }
        }
        if(baseuri == null) {
            baseuri = DEFAULT_BASE_URI;
            logger.log("Found no base URI for topic map. Using default base '"+baseuri+"'.");
        }
    }




    private void parseTopicElements() throws IOException {
        int TOPIC = 1;
        int ASSOCIATION = 2;
        int OCCURRENCE = 3;
        int NONE = 0;

        int exceptionLimit = 100;
        int previousFailed = NONE;
        int n=0;

        eatMeaningless();
        while(proceed && !logger.forceStop()) {
            try {
                if(previousFailed != NONE) {
                    syncParse();
                    previousFailed = NONE;
                }

                if(eat('[')) {
                    Topic topic = parseTopic();
                    if(eat(']') == false) {
                        if(debug) logger.log("Warning: Parse error while processing topic '"+ topic +"'!");
                        parseUntil(']');
                    }
                    if(topic != null) {
                        numberOfTopics++;
                        previousFailed = NONE;
                    }
                    else {
                        numberOfFailedTopics++;
                        previousFailed = TOPIC;
                    }
                }
                else if(eat('{')) {
                    if(parseOccurrence()) {
                        numberOfOccurrences++;
                        previousFailed = NONE;
                    }
                    else {
                        numberOfFailedOccurrences++;
                        previousFailed = OCCURRENCE;
                    }
                }
                else {
                    if( parseAssociation() != null ) {
                        numberOfAssociations++;
                        previousFailed = NONE;
                    }
                    else {
                        numberOfFailedAssociations++;
                        previousFailed = ASSOCIATION;
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                if(--exceptionLimit < 0) {
                    logger.log("Too many errors occurred while parsing the LTM file. Aborting...");
                    proceed = false;
                }
                if(proceed) syncParse();
            }
            eatMeaningless();
            if(n++ % 1000 == 0) logger.hlog("Importing LTM topic map. Imported " + numberOfTopics + " topics, " + numberOfAssociations + " associations and " + numberOfOccurrences +" occurrences.");
        }
        logger.log("Found total " + numberOfTopics + " topics, " + numberOfAssociations + " associations and " + numberOfOccurrences +" occurrences.");
        logger.log("Real number of topics, associations and occurrences in topic map may be smaller due to merges.");
        if(numberOfFailedTopics > 0) logger.log("Found also " + numberOfFailedTopics + " broken topics.");
        if(numberOfFailedAssociations > 0) logger.log("Found also " + numberOfFailedAssociations + " broken associations.");
        if(numberOfFailedOccurrences > 0) logger.log("Found also " + numberOfFailedOccurrences + " broken occurrences.");
    }




    private void syncParse() throws IOException {
        try {
            String unrecognized = parseUntil('\n');
            eatMeaningless();
            logger.log("Warning: Unrecognized element: \"" + unrecognized + "\" near line number "+lineCounter+", after topic number "+numberOfTopics+" and association number " + numberOfAssociations);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void postProcess() {
        if(debug) logger.log("Post processing topics. Removing temporary subject identifiers.");
        if(topicMap != null) {
            try {
                Iterator<Topic> topics = topicMap.getTopics();
                Topic t = null;
                while(topics.hasNext()) {
                    t = topics.next();
                    if(t != null && !t.isRemoved()) {
                        ArrayList<Locator> subjects = new ArrayList();
                        subjects.addAll(t.getSubjectIdentifiers());
                        for(Locator si : subjects) {
                            if(si != null) {
                                if(si.toExternalForm().startsWith(TEMP_SI_PREFIX)) {
                                    t.removeSubjectIdentifier(si);
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                logger.log(e);
            }
        }
    }


    // ---------------------------------------------------------------------
    // ---------------------------------------------------------- TOPICS ---
    // ---------------------------------------------------------------------




    private Topic parseTopic() throws IOException, TopicMapException {
        Topic topic = null;
        LTMQName topicQName = parseQName();
        if(topicQName != null) {
            boolean foundWithBasename = false;
            boolean foundWithSI = false;
            boolean foundWithSL = false;
            boolean foundWithQName = false;

            if(debug) logger.log("Topic found: " + topicQName.qname);
            ArrayList<Topic> topicTypes = null;
            ArrayList<Locator> topicTypeSIs = null;
            if(eat(':')) { 
                topicTypes = parseQTopics();
                topicTypeSIs = new ArrayList();
                for(Topic topicType : topicTypes) {
                    topicTypeSIs.add(topicType.getOneSubjectIdentifier());
                }
            }
            ArrayList baseNames = parseBasenames();

            Locator subjectLocator = parseSubjectLocator();
            ArrayList subjectIdentifiers = parseSubjectIdentifiers();

            topic = getOrCreateTopic(topicQName.qname);
            if(topic != null) foundWithQName = true;

            if(topic == null && baseNames != null) {
                Basename basename = null;
                if(baseNames.size() > 0) {
                    basename = (Basename) baseNames.iterator().next();
                    if(baseNames.size() > 1) {
                        if(debug) logger.log("Warning: Wandora supports only one base name per topic!");
                    }
                    if(basename != null) {
                        if(basename.basename != null) {
                            topic = topicMap.getTopicWithBaseName(basename.basename);
                            if(topic != null) foundWithBasename = true;
                        }
                    }
                }
            }

            if(topic == null && subjectIdentifiers != null) {
                Locator identifier = null;
                Iterator identifiers = subjectIdentifiers.iterator();
                while(topic == null && identifiers.hasNext()) {
                    identifier = (Locator) identifiers.next();
                    if(identifier != null) topic = topicMap.getTopic(identifier);
                }
                if(topic != null) foundWithSI = true;
            }

            if(topic == null && subjectLocator != null) {
                topic = topicMap.getTopicBySubjectLocator(subjectLocator);
                if(topic != null) foundWithSL = true;
            }


            // ----- TOPIC SOLVED HERE | PROCESS NOW -----

            if(!foundWithQName && MAKE_SUBJECT_IDENTIFIER_FROM_ID) {
                topic.addSubjectIdentifier(buildLocator(topicQName.qname));
            }
            
            if(!foundWithSL && subjectLocator != null) {
                if(topic.getSubjectLocator() == null || OVERWRITE_SUBJECT_LOCATORS) {
                    topic.setSubjectLocator(subjectLocator);
                }
            }

            // PROCESS SUBJECT IDENTIFIERS...
            Locator newSI = null;
            if(subjectIdentifiers != null && subjectIdentifiers.size() > 0) {
                Iterator indicators = subjectIdentifiers.iterator();
                while(indicators.hasNext()) {
                    newSI = (Locator) indicators.next();
                    if(newSI!=null) {
                        topic.addSubjectIdentifier(newSI);
                    }
                }
            }
            else {
                if(topic != null && topic.getOneSubjectIdentifier() == null) {
                    topic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                }
            }


            // PROCESS BASENAMES AND IT'S VARIANTS...
            if(baseNames != null && baseNames.size() > 0) {
                Basename basename = null;
                basename = (Basename) baseNames.iterator().next();

                if(basename != null) {
                    if(basename.basename != null) {
                        Topic baseNameTopic = topicMap.getTopicWithBaseName(basename.basename);
                        if(topic.getBaseName() != null || OVERWRITE_BASENAME) {
                            if(!foundWithBasename && basename.basename != null) {
                                topic.setBaseName(basename.basename);
                            }
                        }
                    }
                    if(basename.displayname != null) {
                        topic.setDisplayName(XTMPSI.getLang(null), basename.displayname); // LANG INDEPENDENT DISPLAYNAME
                        // logger.log("found displayname name '" + basename.sortname+"'");
                    }
                    if(basename.sortname != null) {
                        HashSet nameScope=new HashSet();
                        nameScope.add(getOrCreateTopic(XTMPSI.getLang(null)));
                        nameScope.add(getOrCreateTopic(XTMPSI.SORT)); 
                        topic.setVariant(nameScope, basename.sortname);
                        // logger.log("found sort name '" + basename.sortname+"'");
                    }
                    if(basename.variantNames != null) {
                        for(Iterator variants = basename.variantNames.iterator(); variants.hasNext(); ) {
                            VariantName variant = (VariantName) variants.next();
                            if(variant != null) {
                                //logger.log("found variant '" + variant.name+"' with scope '"+variant.scope+"'.");
                                if(variant.name != null && variant.scope != null && variant.scope.size() > 0) {
                                    if(topic.getVariant(variant.scope) != null || OVERWRITE_VARIANTS) {
                                        topic.setVariant(variant.scope, variant.name);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // PROCESS TOPIC TYPES...
            if(topicTypes != null && topicTypeSIs != null) {
                Topic topicType = null;
                for( Locator topicTypeSI : topicTypeSIs ) {
                    try {
                        if(topicTypeSI != null) {
                            topicType = topicMap.getTopic(topicTypeSI);
                            if(topicType != null && !topic.isOfType(topicType)) {
                                topic.addType(topicType);
                                if(debug) logger.log("Found type for " + topic);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return topic;
    }







    private Locator parseSubjectLocator() throws IOException {
        Locator locator = null;
        if(eat('%')) {
            locator = buildLocator(parseString());
        }
        return locator;
    }





    private ArrayList parseSubjectIdentifiers() throws IOException {
        ArrayList locators = new ArrayList();
        boolean ready = false;

        do {
            String locator = null;
            if(eat('@')) {
                locator = parseString();
                if(locator != null && locator.length() > 0) {
                    locators.add(buildLocator(locator));
                }
            }
            else {
                ready = true;
            }
        }
        while(! ready);
        return locators;
    }





    private ArrayList parseBasenames() throws IOException, TopicMapException {
        String basename = null;
        String sortname = null;
        String displayname = null;
        ArrayList variantNames = new ArrayList();
        VariantName variantName = null;
        ArrayList basenames = new ArrayList();

        while(eat('=')) {
            basename = parseString();
            //logger.log("Basename '"+ basename +"' found for topic!");
            if(TRIM_BASENAMES) basename = basename.trim();
            if(eat(';')) {
                sortname = parseString();
                if(eat(';')) {
                    displayname = parseString();
                }
            }

            ArrayList scopes = parseScope();
            //if(scopes != null) logger.log("    Found scope for base name "+scopes);

            if(eat('~')) {
                LTMQName reifyId = parseQName();
                // TODO: Handler for reifiers!
            }
            while(eat('(')) {
                variantName = parseVariantName();
                if(variantName != null) variantNames.add(variantName);
                eat(')');
            }

            if(basename != null && basename.length() > 0) {
                if(FORCE_UNIQUE_BASENAMES) {
                    Topic t = topicMap.getTopicWithBaseName(basename);
                    int n = 0;
                    while( t != null ) {
                        n++;
                        t = topicMap.getTopicWithBaseName(basename + " " + n);
                    }
                    if(n > 0) basename = basename + " " + n;
                }
                //logger.log("  Basename '"+ basename +"'");
            }
            if(basename != null || !variantNames.isEmpty()) {
                basenames.add( new Basename(basename, variantNames, displayname, sortname) );
            }
        }
        //logger.log("Found total "+basenames.size()+" basenames for topic!");
        return basenames;
    }





    private VariantName parseVariantName() throws IOException, TopicMapException {
        String variantName = parseString();
        ArrayList scope = parseScope();
        LTMQName reifyId = parseQName();

        if(scope == null) { scope = new ArrayList(); }

        /*
        if(scope.size() == 0) {
            defaultTypeForVariants = getOrCreateMappedTopic(DEFAULT_TYPE_FOR_VARIANTS);
            if(defaultTypeForVariants != null && !scope.contains(defaultTypeForVariants)) {
                scope.add(defaultTypeForVariants);
            }
            defaultScopeForVariants = getOrCreateMappedTopic(DEFAULT_SCOPE_FOR_VARIANTS);
            if(defaultScopeForVariants != null) {
                scope.add(defaultScopeForVariants);
            }
        }
        */

        if(variantName != null) {
            if(scope != null && scope.size() > 0) {
                return new VariantName(variantName, scope);
            }
        }
        return null;
    }


    // ---------------------------------------------------------------------
    // ---------------------------------------------------- ASSOCIATIONS ---
    // ---------------------------------------------------------------------




    private Association parseAssociation() throws IOException, TopicMapException {
        Association association = null;
        LTMQName associationTypeName = parseQName();

        if(associationTypeName != null) {
            LTMQName reifyId = null;
            ArrayList members = new ArrayList();
            Member member = null;
            Topic associationType = null;

            if(eat('(')) {
                int memberCounter = 0;
                do {
                    member = parseAssociationMember(memberCounter++, associationTypeName);
                    if(member != null) {
                        members.add(member);
                    }
                }
                while(eat(','));
                // System.out.println("found "+memberCounter+" members.");
                eat(')');
            }
            ArrayList scopes = parseScope();
            if(eat('~')) {
                reifyId = parseQName();
                // TODO: Handler for reifiers!
            }

            if(members.size() > 0) {
                associationType = getOrCreateTopic(associationTypeName);
                if(associationType != null) {
                    //logger.log("Association type is: "+associationType+ " ---- "+associationTypeName.qname);
                    association = topicMap.createAssociation(associationType);
                    if(association != null) {
                        HashMap<Topic,Topic> players=new HashMap<Topic,Topic>();
                        for(Iterator memberIter = members.iterator(); memberIter.hasNext(); ) {
                            member = (Member) memberIter.next();
                            //if(member != null) association.addPlayer(member.role,member.player);
                            if(member != null && member.role != null && member.player != null) {
                                players.put(member.role, member.player);
                                //logger.log("  Adding association: "+associationType+" player '"+member.player+"' with role '"+member.role+"'." );
                            }
                        }
                        association.addPlayers(players);
                    }
                    /*
                    Iterator i = topicMap.getAssociations();
                    int c = 0;
                    while(i.hasNext()) {
                        i.next();
                        c++;
                    }
                    logger.log("A-count: "+c);
                    *
                    */
                }
            }
        }

        return association;
    }



    private Member parseAssociationMember(int memberNumber, LTMQName associationTypeQName) throws IOException, TopicMapException {
        Topic role = null;
        Topic player = null;
        LTMQName reifyId = null;

        if(eat('[')) {
            player = parseTopic();
            eat(']');
        }
        else {
            player = parseQTopic();
        }

        if(player != null) {

            if(eat(':')) role = parseQTopic();

            if(role == null && !REJECT_ROLELESS_MEMBERS) {
                //logger.log("role == "+role);
                Collection<Topic> types = player.getTypes();
                if(types != null && types.size() > 0 && PREFER_CLASS_AS_ROLE) {
                    role = types.iterator().next();
                    // System.out.println("found role '"+role+"' ("+role.getOneSubjectIdentifier().toExternalForm()+")");
                }
                else {
                    String associationTypeName = "";
                    if(associationTypeQName != null) {
                        associationTypeName = associationTypeQName.qname;
                    }
                    String roleID = associationTypeName + "_" + DEFAULT_ROLE_IDENTIFIER + "_" + memberNumber;
                    role = getOrCreateTopic(roleID);
                }
            }
            if(eat('~')) {
                reifyId = parseQName();
                // TODO: Handler for reifiers!
            }
        }

        if(role != null && player != null) {
            return new Member(player, role);
        }
        return null;
    }







    // ---------------------------------------------------------------------
    // ----------------------------------------------------- OCCURRENCES ---
    // ---------------------------------------------------------------------




    private boolean parseOccurrence() throws IOException, TopicMapException {
        Topic occurrenceTopic = null;
        ArrayList scope = null;
        Topic occurrenceType = null;
        LTMQName reifyId = null;
        boolean occurrenceSucceed = false;

        occurrenceTopic = parseQTopic();
        eat(',');
        occurrenceType = parseQTopic();
        eat(',');
        String resource = parseResource();
        eat('}');
        scope = parseScope();
        if(eat('~')) {
            reifyId = parseQName();
            // TODO: Handler for reifiers!
        }

        if(scope == null) scope = new ArrayList();
        if(scope.isEmpty()) {
            defaultScopeForOccurrences = getOrCreateTopic(DEFAULT_SCOPE_FOR_OCCURRENCES);
            if(defaultScopeForOccurrences != null) {
                scope.add(defaultScopeForOccurrences);
            }
        }

        if(occurrenceTopic != null && occurrenceType != null) {
            if(resource != null) {
                if(scope != null && scope.size() > 0) {
                    //logger.log("Occurrence found");
                    Topic scopeTopic = null;
                    if(NEW_OCCURRENCE_FOR_EACH_SCOPE) {
                        for(Iterator iter = scope.iterator(); iter.hasNext(); ) {
                            scopeTopic = (Topic) iter.next();
                            if(scopeTopic != null) {
                                // System.out.println("CREATING OCCURRENCE: " +occurrenceType + " --- " + scopeTopic + " --- " + resource);
                                occurrenceTopic.setData(occurrenceType, scopeTopic, resource);
                                //logger.log("  Occurrence type: "+ occurrenceType);
                                //logger.log("  Occurrence scope: "+ scopeTopic);
                                //logger.log("  Occurrence resource: "+ resource);
                                occurrenceSucceed = true;
                            }
                        }
                    }
                    else {
                        scopeTopic = (Topic) scope.iterator().next();
                        if(scopeTopic != null) {
                            occurrenceTopic.setData(occurrenceType, scopeTopic, resource);
                            occurrenceSucceed = true;
                        }
                    }
                }
            }
        }
        return occurrenceSucceed;
    }




    private String parseResource() throws IOException {
        String locator = parseString();
        if(locator != null && locator.length() >= 1) {
            return locator;
        }
        else {
            if(eat('[')) {
                if(eatOnly('[')) {
                    String data = parseUntil("]]");
                    int unicodeLocation = -1;
                    String unicodeNumberStr = null;
                    int unicodeNumber = 0;
                    do {
                        unicodeLocation = data.indexOf("\\u");
                        if(unicodeLocation != -1) {
                            try {
                                unicodeNumberStr = data.substring(unicodeLocation+2, unicodeLocation+6);
                                unicodeNumber = Integer.parseInt(unicodeNumberStr, 16);
                                data = data.substring(0, unicodeLocation) + ((char) unicodeNumber) + data.substring(unicodeLocation+6);
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    while( unicodeLocation != -1 );
                    return data;
                }
            }
            else {
                String data = parseString();
                return data;
            }
        }
        return "";
    }




    // ---------------------------------------------------------------------
    // --------------------------------------------------- MISC ELEMENTS ---
    // ---------------------------------------------------------------------





    private ArrayList parseScope() throws IOException, TopicMapException {
        if(eat('/')) {
            return parseQTopics();
        }
        return null;
    }


    private Topic parseQTopic() throws IOException, TopicMapException {
        return getOrCreateTopic(parseQName());
    }



    private ArrayList parseQTopics() throws IOException, TopicMapException {
        ArrayList qtopics = new ArrayList();
        Topic qtopic = null;
        boolean ready = false;

        do {
            qtopic = parseQTopic();
            if(qtopic != null) qtopics.add(qtopic);
            else ready = true;
        }
        while(!ready);
        return qtopics;
    }



    private ArrayList parseQNames() throws IOException {
        ArrayList qnames = new ArrayList();
        LTMQName qname = null;
        boolean ready = false;

        do {
            qname = parseQName();
            if(qname != null) qnames.add(qname);
            else ready = true;
            if(debug) logger.log("Found qname \"" + qname.qname + "\"");
        }
        while(!ready);
        return qnames;
    }



    private LTMQName parseQName() throws IOException {
        String qname = parseName();
        String locatorPrefix = null;
        String indicatorPrefix = null;
        if(qname != null && qname.length() > 0) {
            if(false && eatOnly(':')) {
                locatorPrefix = locatorPrefixes.get(qname);
                indicatorPrefix = indicatorPrefixes.get(qname);
                qname = parseName();
            }
            return new LTMQName(qname, locatorPrefix, indicatorPrefix);
        }
        return null;
    }




    // ---------------------------------------------------------------------
    // ------------------------------------------------------ PRIMITIVES ---
    // ---------------------------------------------------------------------




    private String parseName() throws IOException {
        if(ALLOW_SPECIAL_CHARS_IN_QNAMES) return parseExtendedName();
        else return parseStrictName();
    }



    private String parseExtendedName() throws IOException {
        StringBuilder sb = new StringBuilder("");
        int len = 0;
        if(proceed) {
            boolean ready = false;
            int c = 0;
            eatMeaningless(false);
            do {
                in.mark(1);
                c = in.read();
                if(c == -1) {
                    ready = true;
                    proceed = false;
                }
                else if(isSpace(c) || "=()[]{}/,:;".indexOf(c) != -1) {
                    ready = true;
                    in.reset();
                }
                else {
                    if(!isQNameExtendedCharacter(c)) c = '_';
                    sb.append((char) c);
                    len++;
                }
            } while(!ready && len < MAX_NAME_LEN);
        }
        if(sb.length() > 0) if(debug) logger.log("Name found \"" + sb.toString() + "\"");
        if(len >= MAX_NAME_LEN) logger.log("Warning: Name length > "+ MAX_NAME_LEN);

        return sb.toString();
    }




    private String parseStrictName() throws IOException {
        StringBuilder sb = new StringBuilder("");
        int len = 0;
        if(proceed) {
            boolean ready = false;
            int c = 0;
            eatMeaningless(true);
            do {
                in.mark(1);
                c = in.read();
                if(c == -1) {
                    ready = true;
                    proceed = false;
                }
                else if((len == 0 && isQNameCharacter(c)) || (len > 0 && isQNameExtendedCharacter(c))) {
                    sb.append((char) c);
                    len++;
                }
                else {
                    ready = true;
                    in.reset();
                }
            }
            while(!ready && len < MAX_NAME_LEN);
        }
        if(sb.length() > 0) if(debug) logger.log("Name found \"" + sb.toString() + "\"");
        if(len >= MAX_NAME_LEN) logger.log("Warning: Name length > "+ MAX_NAME_LEN);

        return sb.toString();
    }




    private String parseString() throws IOException {
        StringBuilder sb = new StringBuilder("");
        int c = 0;
        char ch = 0;
        int len = 0;
        if(proceed) {
            if(eat('"')) {
                boolean ready = false;
                while(!ready && len < MAX_STRING_LEN && proceed) {
                    if(eatOnly('"')) {
                        if(eatOnly('"')) {
                            sb.append('"');
                            len++;
                        }
                        else {
                            //System.out.println("found string end.");
                            ready = true;
                        }
                    }
                    else if(eatOnly('\\')) {
                        if(eatOnly('u')) {
                            char[] unicode = new char[4];
                            int c3 = in.read(unicode);
                            int uc = Integer.parseInt(new String(unicode), 16);
                            sb.append((char) uc);
                            //System.out.println("escaped unicode char found '" + ((char) uc) + "' ("+new String(unicode)+")");
                            len++;
                        }
                        else {
                            // TODO: MORE COMPLEX SLASH CHARACTERS
                            c = in.read();
                            if(c == -1) proceed = false;
                            else {
                                ch = (char) c;
                                //System.out.println("escaped char found '" + ch + "'");
                                sb.append(ch);
                                len++;
                            }
                        }
                    }
                    else {
                        c = in.read();
                        if(c == -1) proceed = false;
                        else {
                            ch = (char) c;
                            //System.out.println("char found '" + ch + "'");
                            sb.append(ch);
                            len++;
                        }
                    }
                }
            }
        }
        if(debug) logger.log("String found \"" + sb.toString() + "\"");
        if(len >= MAX_STRING_LEN) logger.log("Warning: String length > "+ MAX_STRING_LEN);

        return sb.toString();
    }



    private String parseUntil(int ch) throws IOException {
        if(!proceed) return null;
        StringBuilder sb = new StringBuilder("");
        int c = -1;
        int maxlen = 99999;

        do {
            if(c != -1) sb.append((char) c);
            c = in.read();
            if(c == '\n') lineCounter++;
        }
        while(c != ch && c != -1 && --maxlen > 0);
        if(c == -1) {
            proceed = false;
        }
        return sb.toString();
    }


    private String parseUntil(String str) throws IOException {
        if(!proceed) return null;
        StringBuilder sb = new StringBuilder("");
        boolean ready = false;
        int c = -1;
        int strLen = str.length();
        char[] charStr = new char[strLen];

        for(int i=0; i<strLen; i++) {
            charStr[i] = 0;
        }

        do {
            if(charStr[0] != -1 && charStr[0] != 0) sb.append(charStr[0]);
            c = in.read();
            if(c == '\n') lineCounter++;

            ready = true;
            for(int i=1; i<strLen; i++) {
                charStr[i-1] = charStr[i];
                if(ready && str.charAt(i-1) != charStr[i]) ready = false;
                //System.out.println("TEST: " + str.charAt(i-1) + " == " + charStr[i]);
            }
            charStr[strLen-1] = (char) c;
            //System.out.println("TEST: " + str.charAt(strLen-1) + " == " + charStr[strLen-1]);
            //System.out.println("--");
            if(ready && str.charAt(strLen-1) != charStr[strLen-1]) ready = false;
        }
        while(!ready && c != -1);
        if(c == -1) {
            if(debug) logger.log("Warning: Unexpected end of occurrence data!");
            proceed = false;
        }
        return sb.toString();
    }





    private boolean parseComment() throws IOException {
        if(!proceed) return false;
        in.mark(2);
        int c1 = in.read();
        int c2 = in.read();
        if(c1 == '/' && c2 == '*') {
            try {
                c2 = in.read();
                do {
                    c1 = c2;
                    c2 = in.read();
                    if(c2 == '\n') lineCounter++;
                }
                while(c1 != '*' || c2 != '/');
                return true;
            }
            catch(Exception e) {
                if(debug) logger.log("Warning: Unexpected end of comment! Missing ending!");
                proceed = false;
                return true;
            }
        }
        else {
            in.reset();
        }
        return false;
    }




    // ---------------------------------------------------------------------



    private boolean eatOnly(int ch) throws IOException {
        if(!proceed) return false;
        in.mark(1);
        int c = in.read();
        if(c == -1) {
            proceed = false;
            return false;
        }
        if(c != ch) {
            in.reset();
            return false;
        }
        else {
            if(ch == '\n') lineCounter++;
        }
        return true;
    }


    
    private boolean eat(int [] str) throws IOException {
        if(!proceed) return false;
        eatMeaningless();
        in.mark(str.length);
        char[] chars = new char[str.length];
        int c = in.read(chars);
        if(c <= 0) {
            in.reset();
            proceed = false;
            return false;
        }
        int i=0;
        while(i < str.length) {
            if(str[i] != chars[i]) {
                in.reset();
                return false;
            }
            i++;
        }
        return true;
    }
    

    private boolean eat(int ch) throws IOException {
        eatMeaningless();
        if(!proceed) return false;
        in.mark(1);
        int c = in.read();
        if(c == -1) {
            proceed = false;
            return false;
        }
        if(c != ch) {
            in.reset();
            return false;
        }
        return true;
    }





    private boolean eat(String str) throws IOException {
        if(!proceed) return false;
        eatMeaningless();
        in.mark(str.length());
        char[] chars = new char[str.length()];
        int c = in.read(chars);
        if(c <= 0) {
            in.reset();
            proceed = false;
            return false;
        }
        String byteString = new String(chars);
        if(! str.equals(byteString)) {
            in.reset();
            return false;
        }
        return true;
    }



    private boolean eatMeaningless() throws IOException {
        return eatMeaningless(true);
    }


    private boolean eatMeaningless(boolean eatAlsoNewlines) throws IOException {
        boolean meaninglessAvailable = false;
        boolean anyMeaningless = false;
        boolean spacesFound = false;
        boolean commentsFound = false;
        do {
            spacesFound = eatSpaces(eatAlsoNewlines);
            commentsFound = parseComment();
            meaninglessAvailable = spacesFound || commentsFound;
            anyMeaningless = anyMeaningless || meaninglessAvailable;
        }
        while(meaninglessAvailable);
        return anyMeaningless;
    }




    private boolean eatSpaces() throws IOException {
        return eatSpaces(true);
    }
    private boolean eatSpaces(boolean eatAlsoNewlines) throws IOException {
        if(!proceed) return false;
        int c = 0;
        boolean cont = false;
        boolean spacesFound = false;
        int n = 0;
        do {
            in.mark(1);
            c = in.read();
            if(c == -1) {
                cont = false;
                proceed = false;
                break;
            }
            else {
                cont = ( c == ' ' || c == '\t' );
                if(eatAlsoNewlines) {
                    cont = cont || isSpace(c);
                    if(c == '\n') lineCounter++;
                }
                if(cont) { 
                    spacesFound = true;
                    n++;
                }
            }
        }
        while(cont);
        // if(debug) if(n>0) logger.log("  Number of eaten meaningless: " + n);
        in.reset();
        return spacesFound;
    }



    // private String WHITE_SPACE_CHARACTERS = " \t\n\r"; // SEE METHOD isSpace(int c)
    private String QNAME_CHARACTERS = "qwertyuioplkjhgfdsazxcvbnmMNBVCXZLKJHGFDSAPOIUYTREWQ_";
    private String EXTENDED_QNAME_CHARACTERS = "qwertyuioplkjhgfdsazxcvbnmMNBVCXZLKJHGFDSAPOIUYTREWQ1234567890_-.";



    private boolean isSpace(int c) {
        if(c == ' ' || c == 0x00A0 || c == 0x2007 || c == 0x202F || c == 0x0009 ||
           c == 0x000A || c == 0x000B || c == 0x000C || c == 0x000D) return true;
        return false;
    }

    private boolean isQNameCharacter(int c) {
        return (QNAME_CHARACTERS.indexOf(c) != -1);
    }

    private boolean isQNameExtendedCharacter(int c) {
        return (EXTENDED_QNAME_CHARACTERS.indexOf(c) != -1);
    }




    // ---------------------------------------------------------------------




    public int read() throws IOException {
        return in.read();
    }


    
    public Locator buildTempLocator(String id) {
        if(id == null) id = "null";
        return new Locator(TEMP_SI_PREFIX + id);
    }
    
    

    public Locator buildLocator(String id) {
        if(id == null) return null;
        String locatorString = id;
        Locator locator = null;

        if(locatorString.charAt(0) == '#' && ltmuri != null) {
            locatorString = ltmuri + locatorString;
        }
        else {
            Matcher prefixPatternMatcher = prefixPattern.matcher(locatorString);
            if(prefixPatternMatcher.matches()) {
                try {
                    locator = new Locator(locatorString);
                }
                catch(Exception e) { }
            }
            else {
                if(baseuri != null) {
                    if(baseuri.endsWith("/"))
                        locatorString = baseuri + locatorString;
                    else
                        locatorString = baseuri + "/" + locatorString;
                }
                locator = new Locator(locatorString);
            }
        }
        if(locator.toExternalForm().length() > MAX_SI_LEN) {
            locator = new Locator(locator.toExternalForm().substring(0, MAX_SI_LEN));
        }
        if(debug) logger.log("New locator: "+locator.toExternalForm());
        return locator;
    }







    public Topic getOrCreateTopic(LTMQName qname) throws TopicMapException  {
        if(qname == null) return null;
        return getOrCreateTopic(qname.qname);
    }


    public Topic getOrCreateTopic(String qname) throws TopicMapException  {
        if(qname == null) return null;
        Topic t = topicMap.getTopic(buildTempLocator(qname));

        if(t==null) {
            t = topicMap.getTopic(buildLocator(qname));
        }

        if(t==null) {
            t=topicMap.createTopic();
            t.addSubjectIdentifier(buildTempLocator(qname));
            if(MAKE_SUBJECT_IDENTIFIER_FROM_ID) t.addSubjectIdentifier(buildLocator(qname));
            if(MAKE_BASENAME_FROM_ID && t.getBaseName() == null) t.setBaseName(qname);
            if(debug) logger.log("New topic created: " +t);
        }
        return t;
    }







    // ---------------------------------------------------------------------
    // ----------------------------------------------- HELPER STRUCTURES ---
    // ---------------------------------------------------------------------



    public class LTMQName {
        public String qname;
        public String locatorPrefix;
        public String indicatorPrefix;
        public LTMQName(String qname, String locatorPrefix, String indicatorPrefix) {
            this.qname = qname;
            this.locatorPrefix = locatorPrefix;
            this.indicatorPrefix = indicatorPrefix;
        }
    }

    public class Basename {
        public String basename;
        public Collection variantNames;
        public String sortname;
        public String displayname;

        public Basename(String basename, Collection variantNames, String displayName, String sortName){
            this.basename=basename;
            this.variantNames=variantNames;
            this.displayname=displayName;
            this.sortname=sortName;
        }
    }

    public class VariantName {
        public String name;
        public Set scope;
        public VariantName(String name, Collection s){
            this.name=name;
            this.scope=new LinkedHashSet();
            for(Iterator i=s.iterator(); i.hasNext(); ) {
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


}
