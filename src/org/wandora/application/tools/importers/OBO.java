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
 */

package org.wandora.application.tools.importers;


import org.wandora.topicmap.TMBox;
import org.wandora.utils.IObox;
import org.wandora.application.tools.exporters.*;
import org.wandora.topicmap.*;

import java.io.*;
import java.net.*;
import java.util.*;



/**
 * This class is a static library for OBOImport, OBOExport, and OBORoundtrip
 * tools. Library contains shared constants and services used to
 * map OBO (Open Biological Ontologies) elements to Topic Maps and vice versa.
 *
 * @author akivela
 */
public class OBO {
    public static final String optionPrefix = "obo.";
    
    public static boolean COLLATE_SIMILAR_SYNONYMS = true;
    public static boolean MAKE_DESCRIPTION_TOPICS = true;
    public static boolean MAKE_SIS_FROM_ALT_IDS = false;
    public static boolean USE_SCOPED_XREFS = true;
    public static boolean PROCESS_HEADER_IMPORTS = true;
    
    public static final String QUOTE_STRING_PATTERN = "\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"";
    //public static final String QUOTE_STRING_PATTERN = "\"((?:[^\"]|(?<=\\\\)\")*)\"";
    
    public static String LANG = "en";
    
    public static final String SI = "http://wandora.org/si/obo/";
    public static final String SCHEMA_SI = SI + "schema/";
    public static final String HEADER_SI = SI + "header/";
    public static final String DBXREF_SI = SI + "dbxref/";
    
    
    public static final String SCHEMA_TERM_ID = "obo-id";
    
    public static final String SCHEMA_TERM_ALTERNATIVE_ID = "alternative-id";

    
    //public static final String SCHEMA_TERM_DBXREF_DESCRIPTION = "dbxref-description";
    
    public static final String SCHEMA_TERM_DEFINITION = "definition";
    public static final String SCHEMA_TERM_DEFINITION_ORIGIN = "definition-origin";
    public static final String SCHEMA_TERM_COMMENT = "comment";
    public static final String SCHEMA_TERM_ASSOCIATION_TYPE = "association-type";
    public static final String SCHEMA_TERM_DESCRIPTION = "dbxref-description";
    
    public static final String SCHEMA_TERM_MEMBER = "member";
    public static final String SCHEMA_TERM_CATEGORY = "category";
    
    public static final String SCHEMA_TERM_SUPERCLASS_SUBCLASS = "superclass-subclass";
    public static final String SCHEMA_TERM_SUPERCLASS = "superclass";
    public static final String SCHEMA_TERM_SUBCLASS = "subclass";

    public static final String SCHEMA_TERM_TERM = "term";
    public static final String SCHEMA_TERM_TYPEDEF = "typedef";
    public static final String SCHEMA_TERM_INSTANCE = "instance";
    
    public static final String SCHEMA_TERM_NAMESPACE = "namespace";
    
    public static final String SCHEMA_TERM_IS_ANONYMOUS = "is-anonymous";
    
    public static final String SCHEMA_TERM_CONSIDER_USING = "consider-using";
    
    public static final String SCHEMA_TERM_REPLACED_BY = "replaced-by";
    
    public static final String SCHEMA_TERM_RELATIONSHIP = "relationship";
    public static final String SCHEMA_TERM_RELATED_TO = "related-to";
    
    public static final String SCHEMA_TERM_INTERSECTION_OF ="intersection-of";
    
    public static final String SCHEMA_TERM_PART_OF = "part-of";
    public static final String SCHEMA_TERM_HAS_PART = "has-part";
    
    public static final String SCHEMA_TERM_AGENT_IN = "agent-in";
    public static final String SCHEMA_TERM_HAS_AGENT = "has-agent";
    
    public static final String SCHEMA_TERM_PARTICIPATES_IN = "participates-in";
    public static final String SCHEMA_TERM_HAS_PARTICIPANT = "has-participant";
    
    public static final String SCHEMA_TERM_PRECEDED_BY = "preceded-by";
    public static final String SCHEMA_TERM_PRECEDES = "precedes";
    
    public static final String SCHEMA_TERM_DERIVES_FROM = "derives-from";
    public static final String SCHEMA_TERM_DERIVES_INTO = "derives-into";
    
    public static final String SCHEMA_TERM_TRANSFORMATION_OF = "transformation-of";
    public static final String SCHEMA_TERM_TRANSFORMATION_INTO = "transformed-into";
    
    public static final String SCHEMA_TERM_CONTAINS = "contains";
    public static final String SCHEMA_TERM_CONTAINED_IN = "contained-in";
    
    public static final String SCHEMA_TERM_LOCATION_OF = "location-of";
    public static final String SCHEMA_TERM_LOCATED_IN = "located-in";
    
    public static final String SCHEMA_TERM_DISJOINT_FROM = "disjoint-from";
    public static final String SCHEMA_TERM_ADJACENT_TO = "adjacent-to";
    
    public static final String SCHEMA_TERM_UNION_OF = "union-of";
    
    public static final String SCHEMA_TERM_MODIFIER = "modifier";
    
    // **** TYPE-DEF ****
    public static final String SCHEMA_TERM_DOMAIN = "domain";
    public static final String SCHEMA_TERM_RANGE = "range";
    public static final String SCHEMA_TERM_INVERSE_OF = "inverse-of";
    public static final String SCHEMA_TERM_IS_TRANSITIVE = "is-transitive";
    public static final String SCHEMA_TERM_IS_TRANSITIVE_OVER = "is-transitive-over";
    public static final String SCHEMA_TERM_IS_REFLEXIVE = "is-reflexive";
    public static final String SCHEMA_TERM_IS_CYCLIC = "is-cyclic";
    public static final String SCHEMA_TERM_IS_SYMMETRIC = "is-symmetric";
    public static final String SCHEMA_TERM_IS_ANTISYMMETRIC = "is-antisymmetric";
    public static final String SCHEMA_TERM_IS_METADATA_TAG = "is-metadata-tag";
    
    
    // **** INSTANCE ****
    public static final String SCHEMA_TERM_INSTANCE_OF = "instance-of";
    
    // **** PROPERTY-VALUES *****
    public static final String SCHEMA_TERM_PROPERTY_VALUE ="property-value";
    public static final String SCHEMA_TERM_PROPERTY_VALUE_TYPE  ="property-value-type";
    
    // ***** SYNONYMS ******
    public static final String SCHEMA_TERM_SYNONYM = "synonym";
    public static final String SCHEMA_TERM_SYNONYM_TEXT = "synonym-text";
    public static final String SCHEMA_TERM_SYNONYM_TYPE = "synonym-type";
    public static final String SCHEMA_TERM_SYNONYM_SCOPE = "synonym-scope";
    public static final String SCHEMA_TERM_SYNONYM_ORIGIN = "synonym-origin";
    
    // ***** XREFS ********
    public static final String SCHEMA_TERM_XREF = "xref";
    public static final String SCHEMA_TERM_XREF_SCOPE = "xref-scope";
    
    // ***** CREATED ******
    public static final String SCHEMA_TERM_CREATED_BY = "created_by";
    public static final String SCHEMA_TERM_CREATION_DATE = "creation_date";
    
    
    public static final String SCHEMA_TERM_EXPAND_ASSERTION = "expand_assertion_to";
    public static final String SCHEMA_TERM_IS_CLASS_LEVEL = "is_class_level";
    public static final String SCHEMA_TERM_HOLS_CHAIN_OVER = "holds_over_chain";
    
    
    
    
    
    
    public static void setOptions(boolean collateSysnonyms, boolean descriptionTopics, boolean altIdSIs, boolean scopedXrefs, String lang) {
        COLLATE_SIMILAR_SYNONYMS = collateSysnonyms;
        MAKE_DESCRIPTION_TOPICS = descriptionTopics;
        MAKE_SIS_FROM_ALT_IDS = altIdSIs;
        USE_SCOPED_XREFS = scopedXrefs;
        LANG = lang;
    }
    public static void setOptions(int options) {
        COLLATE_SIMILAR_SYNONYMS = ((options&1)!=0);
        MAKE_DESCRIPTION_TOPICS = ((options&2)!=0);
        MAKE_SIS_FROM_ALT_IDS = ((options&4)!=0);
        USE_SCOPED_XREFS = ((options&8)!=0);
        PROCESS_HEADER_IMPORTS = ((options&16)!=0);
    }
    public static int getOptions() {
        int options=0;
        if(COLLATE_SIMILAR_SYNONYMS) options+=1;
        if(MAKE_DESCRIPTION_TOPICS) options+=2;
        if(MAKE_SIS_FROM_ALT_IDS) options+=4;
        if(USE_SCOPED_XREFS) options+=8;
        if(PROCESS_HEADER_IMPORTS) options+=16;
        return options;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    
    public static Topic createRootTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace;
        Topic root = tm.getTopic(si);
        if(root == null) {
            root = getOrCreateTopic(tm, si, "obo ("+namespace+")");
            Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
            makeSubclassOf(tm, root, wandoraClass);
        }
        return root;
    }
    
    public static Topic createHeaderTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/header";
        Topic headerTopic = tm.getTopic(si);
        if(headerTopic == null) {
            headerTopic = getOrCreateTopic(tm, si, "header ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, headerTopic, root);
        }
        return headerTopic;
    }
    
    public static Topic createTermTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/term";
        Topic termTopic = tm.getTopic(si);
        if(termTopic == null) {
            termTopic = getOrCreateTopic(tm, si, "term ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, termTopic, root);
        }
        return termTopic;       
    }
    
    public static Topic createTypedefTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+"/typedef";
        Topic termTopic = tm.getTopic(si);
        if(termTopic == null) {
            termTopic = getOrCreateTopic(tm, si, "typedef");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, termTopic, root);
        }
        return termTopic;       
    }
    public static Topic createInstanceTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/instance";
        Topic termTopic = tm.getTopic(si);
        if(termTopic == null) {
            termTopic = getOrCreateTopic(tm, si, "instance ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, termTopic, root);
        }
        return termTopic;       
    }
    public static Topic createObsoleteTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/obsolete";
        Topic obsoleteTopic = tm.getTopic(si);
        if(obsoleteTopic == null) {
            obsoleteTopic = getOrCreateTopic(tm, si, "obsolete ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, obsoleteTopic, root);
        }
        return obsoleteTopic;
    }
    public static Topic createCategoryTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/category";
        Topic categoryTypeTopic = tm.getTopic(si);
        if(categoryTypeTopic == null) {
            categoryTypeTopic = getOrCreateTopic(tm, si, "category ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, categoryTypeTopic, root);
        }
        return categoryTypeTopic;
    }
    public static Topic createDescriptionTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/description";
        Topic categoryTypeTopic = tm.getTopic(si);
        if(categoryTypeTopic == null) {
            categoryTypeTopic = getOrCreateTopic(tm, si, "description ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, categoryTypeTopic, root);
        }
        return categoryTypeTopic;
    }
    public static Topic createSynonymTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/synonym";
        Topic synonymTopic = tm.getTopic(si);
        if(synonymTopic == null) {
            synonymTopic = getOrCreateTopic(tm, si, "synonym ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, synonymTopic, root);
        }
        return synonymTopic;
    }
    public static Topic createSchemaTopic(TopicMap tm) throws TopicMapException {
        String si = OBO.SI+"schema";
        Topic synonymTopic = tm.getTopic(si);
        if(synonymTopic == null) {
            synonymTopic = getOrCreateTopic(tm, si, "obo-schema");
            Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
            makeSubclassOf(tm, synonymTopic, wandoraClass);
        }
        return synonymTopic;
    }
    public static Topic createISARootTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si =  OBO.SI+namespace+"/is-a-root";
        Topic isaRootTopic = tm.getTopic(si);
        if(isaRootTopic == null) {
            isaRootTopic = getOrCreateTopic(tm, si, "is-a-root ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, isaRootTopic, root);
        }
        return isaRootTopic;
    }
    public static Topic createAuthorTopic(TopicMap tm, String namespace) throws TopicMapException {
        String si = OBO.SI+namespace+"/author";
        Topic authorTopic = tm.getTopic(si);
        if(authorTopic == null) {
            authorTopic = getOrCreateTopic(tm, si, "author ("+namespace+")");
            Topic root = createRootTopic(tm, namespace);
            makeSubclassOf(tm, authorTopic, root);
        }
        return authorTopic;
    }
    
    
    
    // ******

    public static Topic getRootTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic root = getTopic(tm, OBO.SI+namespace);
        return root;
    }
    
    public static Topic getHeaderTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic headerTopic = getTopic(tm, OBO.SI+namespace+"/header");
        return headerTopic;
    }
    
    public static Topic getTermTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic termTopic = getTopic(tm, OBO.SI+namespace+"/term");
        return termTopic;       
    }
    
    public static Topic getTypedefTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic termTopic = getTopic(tm, OBO.SI+"/typedef");
        return termTopic;       
    }
    public static Topic getInstanceTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic termTopic = getTopic(tm, OBO.SI+namespace+"/instance");
        return termTopic;       
    }
    public static Topic getObsoleteTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic obsoleteTopic = getTopic(tm, OBO.SI+namespace+"/obsolete");
        return obsoleteTopic;
    }
    public static Topic getCategoryTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic categoryTypeTopic = getTopic(tm, OBO.SI+namespace+"/category");
        return categoryTypeTopic;
    }
    public static Topic getDescriptionTopic(TopicMap tm, String namespace) throws TopicMapException {
        Topic categoryTypeTopic = getTopic(tm, OBO.SI+namespace+"/description");
        return categoryTypeTopic;
    }
    
    
    // ********
    
    
    public static String solveOBOId(Topic t) throws TopicMapException {
        Topic idType = OBO.getTopicForSchemaTerm(t.getTopicMap(), SCHEMA_TERM_ID);
        String id = null;
        if(idType != null) {
            id = t.getData(idType, LANG);
        }
        if(id==null) {
            id = solveOBOId(t.getOneSubjectIdentifier());
        }
        return id;
    }

    
    public static String solveOBOName(Topic t) throws TopicMapException {
        String name = t.getDisplayName(LANG);
        return name;
    }
    
    public static String solveOBODescription(Association a, Topic t) throws TopicMapException {
        Topic descriptionType = OBO.getTopicForSchemaTerm(t.getTopicMap(), SCHEMA_TERM_DESCRIPTION);
        String description = null;
        if(descriptionType != null) {
            if(MAKE_DESCRIPTION_TOPICS && a != null) {
                Topic t2 = a.getPlayer(descriptionType);
                if(t2 != null && !t2.isRemoved()) {
                    description = t2.getData(descriptionType, LANG);
                }
            }
            if(description == null) {
                description = t.getData(descriptionType, LANG);
                if(description == null && a != null) {
                    Topic t2 = a.getPlayer(descriptionType);
                    if(t2 != null && !t2.isRemoved()) {
                        description = t2.getData(descriptionType, LANG);
                    }
                }
            }
        }
        return description;
    }
    
    public static String solveOBODescription(Topic t) throws TopicMapException {
        Topic descriptionType = OBO.getTopicForSchemaTerm(t.getTopicMap(), SCHEMA_TERM_DESCRIPTION);
        String description = null;
        if(descriptionType != null) {
            description = t.getData(descriptionType, LANG);
        }
        return description;
    }
    
    
    
    
    
    public static Topic createTopicForTypedef(TopicMap tm, String id) throws TopicMapException {
        if(tm == null || id == null || id.length() == 0) return null;

        Locator si = makeLocator(id);
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(topic, idType, LANG, id);
        }
        return topic;
    }
    
    
    public static Topic createTopicForTypedef(TopicMap tm, String id, String name, String namespace) throws TopicMapException {
        if(tm == null || id == null || id.length() == 0) return null;

        Locator si = makeLocator(id);
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(topic, idType, LANG, id);
        }

        if(name != null) {
            topic.setBaseName(id);
            topic.setDisplayName(LANG, name);
        }

        topic.addType(createTypedefTopic(tm, namespace));
        return topic;
    }
    
    // *******
    
    
    
    
    public static Topic createTopicForTerm(TopicMap tm, String id) throws TopicMapException {
        if(tm == null || id == null || id.length() == 0) return null;

        Locator si = makeLocator(id);
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(topic, idType, LANG, id);
        }
        return topic;
    }




    public static Topic createTopicForTerm(TopicMap tm, String id, String name, String namespace) throws TopicMapException {
        if(tm == null || id == null || id.length() == 0) return null;

        Locator si = makeLocator(id);
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(topic, idType, LANG, id);
        }

        if(name != null) {
            topic.setBaseName(name+" ("+id+")");
            topic.setDisplayName(LANG, name);
        }
        
        Topic termType = createTermTopic(tm, namespace);
        if(!topic.isOfType(termType)) topic.addType(termType);
        
        return topic;
    }


    
    public static Topic createTopicForInstance(TopicMap tm, String id) throws TopicMapException {
        if(tm == null || id == null || id.length() == 0) return null;

        Locator si = makeLocator(id);
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(topic, idType, LANG, id);
        }

        return topic;
    }




    public static Topic createTopicForInstance(TopicMap tm, String id, String name, String namespace) throws TopicMapException {
        if(tm == null || id == null || id.length() == 0) return null;

        Locator si = makeLocator(id);
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(topic, idType, LANG, id);
        }

        if(name != null) {
            topic.setBaseName(name+" ("+id+")");
            topic.setDisplayName(LANG, name);
        }

        topic.addType(createInstanceTopic(tm, namespace));
        return topic;
    }

    
    
    public static Topic createTopicForAssertionExpansion(TopicMap tm, String exp) throws TopicMapException {
        if(tm == null) return null;
        exp = OBO2Java(exp);

        Locator si = new Locator(makeSI("assertion-expansion/",exp));
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            //topicCounter++;
            topic.addSubjectIdentifier(si);
            topic.setBaseName(exp + " (assertion-expansion)");
            topic.setDisplayName(LANG, exp);
        }
        return topic;
    }
    

    public static Topic createTopicForAuthor(TopicMap tm, String name, String namespace) throws TopicMapException {
        if(tm == null) return null;
        name = OBO2Java(name);

        Locator si = new Locator(makeSI("author/",name));
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            //topicCounter++;
            topic.addSubjectIdentifier(si);
            topic.setBaseName(name + " (author)");
            topic.setDisplayName(LANG, name);
            topic.addType(createAuthorTopic(tm, namespace));
        }
        return topic;
    }
    
    
    
    

    public static Topic createTopicForSynonym(TopicMap tm, String name, String namespace) throws TopicMapException {
        if(tm == null) return null;
        name = OBO2Java(name);

        Locator si = new Locator(makeSI("synonym/",name));
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            //topicCounter++;
            topic.addSubjectIdentifier(si);
            topic.setBaseName(name + " (synonym)");
            topic.setDisplayName(LANG, name);
        }
        return topic;
    }


    public static Topic createTopicForDescription(TopicMap tm, String description, String namespace) throws TopicMapException {
        if(tm == null) return null;
        description = OBO2Java(description);

        Locator si = new Locator(makeSI("description/",description));
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            topic.setBaseName(description + " (description)");
            topic.setDisplayName(LANG, description);
            Topic type = createDescriptionTopic(tm, namespace);
            setData(topic, createTopicForSchemaTerm(tm, SCHEMA_TERM_DESCRIPTION), LANG, description);
            topic.addType(type);
        }
        return topic;
    }
    
    
    

    public static Topic createTopicForRelation(TopicMap tm, String id, String namespace) throws TopicMapException {
        return createTopicForRelation(tm, id, null, namespace);
    }
    public static Topic createTopicForRelation(TopicMap tm, String id, String name, String namespace) throws TopicMapException {
        if(tm == null) return null;

        Locator si = new Locator(makeSI(id));
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            //topicCounter++;
            topic.addSubjectIdentifier(si);
            if(name != null) {
                topic.setBaseName(name);
                topic.setDisplayName(LANG, name);
            }
            else {
                topic.setBaseName(id);
            }
        }
        return topic;
    }


    public static Topic createTopicForModifier(TopicMap tm, String id) throws TopicMapException {
        return createTopicForRelation(tm, id, null);
    }
    
    public static Topic createTopicForModifier(TopicMap tm, String id, String name) throws TopicMapException {
        if(tm == null) return null;

        Locator si = new Locator(makeSI(id));
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            //topicCounter++;
            topic.addSubjectIdentifier(si);
            if(name != null) {
                topic.setBaseName(name);
                topic.setDisplayName(LANG, name);
            }
            else {
                topic.setBaseName(id);
            }
        }
        return topic;
    }

    
    
    
    public static Topic getTopicForSchemaTerm(TopicMap tm, String schemaTerm) throws TopicMapException {
        if(schemaTerm == null) return null;
        String si = OBO.SCHEMA_SI+schemaTerm;
        return getTopic(tm, TopicTools.cleanDirtyLocator(si));
    }


    public static Topic createTopicForSchemaTerm(TopicMap tm, String schemaTerm) throws TopicMapException {
        if(schemaTerm == null) return null;
        String si = OBO.SCHEMA_SI+schemaTerm;
        
        Topic schemaTermTopic = tm.getTopic(si);
        if(schemaTermTopic == null) {
            schemaTermTopic = getOrCreateTopic(tm, new Locator(TopicTools.cleanDirtyLocator(si)), schemaTerm);
            Topic schemaType = createSchemaTopic(tm);
            schemaTermTopic.addType(schemaType);
            // Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            // setData(schemaTermTopic, idType, LANG, schemaTerm);
        }
        return schemaTermTopic;
    }


    
    public static Topic createTopicForDbxref(TopicMap tm, String id) throws TopicMapException {
        return createTopicForDbxref(tm, id, null);
    }
    public static Topic createTopicForDbxref(TopicMap tm, String id, String description) throws TopicMapException {
        if(tm == null || id == null) return null;
        Topic dbxrefTopic = null;
        if(id.startsWith("http:")) {
            dbxrefTopic = getOrCreateTopic(tm, new Locator(id), id);
        }
        else {
            String si = makeSI(id);
            dbxrefTopic = getOrCreateTopic(tm, new Locator(si), id);
            Topic idType = OBO.createTopicForSchemaTerm(tm, SCHEMA_TERM_ID);
            setData(dbxrefTopic, idType, LANG, id);
        }
        if(description != null && description.length() > 0) {
            Topic descriptionTopic = createTopicForSchemaTerm(tm, SCHEMA_TERM_DESCRIPTION);
            setData(dbxrefTopic, descriptionTopic, LANG, description);
        }
        return dbxrefTopic;
    }


    
    public static Topic createTopicForCategory(TopicMap tm, String id) throws TopicMapException {
        return createTopicForCategory(tm, id, null);
    }
    public static Topic createTopicForCategory(TopicMap tm, String id, String description) throws TopicMapException {
        if(tm == null || id == null) return null;
        Topic categoryTopic = null;
        if(id.startsWith("http:")) {
            categoryTopic = getOrCreateTopic(tm, new Locator(id), id);
        }
        else {
            String si = makeSI(id);
            String categoryName = id;
            if(description!=null) categoryName =description+" ("+id+")";
            categoryTopic = getOrCreateTopic(tm, new Locator(si), categoryName);
        }
        if(description != null && description.length() > 0) {
            Topic descriptionTopic = createTopicForSchemaTerm(tm, SCHEMA_TERM_DESCRIPTION);
            setData(categoryTopic, descriptionTopic, LANG, description);
            categoryTopic.setDisplayName(LANG, description);
        }
        return categoryTopic;
    }


    public static Topic createTopicForPropertyRelationship(TopicMap tm, String id) throws TopicMapException {
        if(tm == null || id == null) return null;
        Topic propertyTopic = null;
        if(id.startsWith("http:")) {
            propertyTopic = getOrCreateTopic(tm, new Locator(id), id);
            propertyTopic.addType(createPropertyRelationshipTopic(tm));
        }
        else {
            String si = makeSI(id);
            String propertyName = id;
            propertyTopic = getOrCreateTopic(tm, new Locator(si), propertyName);
            propertyTopic.addType(createPropertyRelationshipTopic(tm));
        }
        return propertyTopic;
    }

    
    
    
    public static Topic createPropertyRelationshipTopic(TopicMap tm) throws TopicMapException {
        String si = OBO.SI+"property-relationship";
        Topic authorTopic = tm.getTopic(si);
        if(authorTopic == null) {
            authorTopic = getOrCreateTopic(tm, si, "property-relationship");
        }
        return authorTopic;
    }
    
    

    public static Topic createTopicForPropertyValue(TopicMap tm, String value) throws TopicMapException {
        if(tm == null || value == null) return null;
        Topic propertyTopic = null;
        if(value.startsWith("http:")) {
            propertyTopic = getOrCreateTopic(tm, new Locator(value), value);
        }
        else {
            String si = makeSI(value);
            String name = value;
            propertyTopic = getOrCreateTopic(tm, new Locator(si), name);
        }
        return propertyTopic;
    }

    
    
    
    public static Topic createPropertyValueTopic(TopicMap tm) throws TopicMapException {
        String si = OBO.SI+"/property-value";
        Topic valueTopic = tm.getTopic(si);
        if(valueTopic == null) {
            valueTopic = getOrCreateTopic(tm, si, "property-value");
        }
        return valueTopic;
    }
    
    
    
    
    public static Topic createTopicForPropertyDatatype(TopicMap tm, String str) throws TopicMapException {
        if(tm == null || str == null) return null;
        Topic t = null;
        if(str.startsWith("http:")) {
            t = getOrCreateTopic(tm, new Locator(str), str);
            t.addType(createPropertyDatatypeTopic(tm));
        }
        else {
            String si = makeSI(str);
            String name = str;
            t = getOrCreateTopic(tm, new Locator(si), name);
            t.addType(createPropertyDatatypeTopic(tm));
        }
        return t;
    }

    
    
    
    public static Topic createPropertyDatatypeTopic(TopicMap tm) throws TopicMapException {
        String si = OBO.SI+"/property-datatype";
        Topic valueTopic = tm.getTopic(si);
        if(valueTopic == null) {
            valueTopic = getOrCreateTopic(tm, si, "property-datatype");
        }
        return valueTopic;
    }
    
    
    // -------------------------------------------------------------------------
    
    

    private static Topic getOrCreateTopic(TopicMap tm, String si, String basename) throws TopicMapException {
        return getOrCreateTopic(tm, new Locator(si), basename);
    }

    private static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, new Locator(si), null);
    }


    private static Topic getOrCreateTopic(TopicMap tm, Locator si, String basename) throws TopicMapException {
        if(tm == null) return null;
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
            if(basename != null) topic.setBaseName(OBO2Java(basename));
        }
        return topic;
    }
    

    
    private static Topic getTopic(TopicMap tm, String si) throws TopicMapException {
        if(tm == null) return null;
        Topic topic = tm.getTopic(si);
        return topic;
    }
    
    
    private static void setData(Topic t, Topic type, String lang, String text) throws TopicMapException {
        if(t != null & type != null && lang != null && text != null) {
            String langsi=XTMPSI.getLang(LANG);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(new Locator(langsi));
                try {
                    langT.setBaseName("Language " + lang.toUpperCase());
                }
                catch (Exception e) {
                    langT.setBaseName("Language " + langsi);
                }
            }
            t.setData(type, langT, text);
        }
    }



    private static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        Topic supersubClassTopic = getOrCreateTopic(tm, XTMPSI.SUPERCLASS_SUBCLASS, "superclass-subclass");
        Topic subclassTopic = getOrCreateTopic(tm, XTMPSI.SUBCLASS, "subclass");
        Topic superclassTopic = getOrCreateTopic(tm, XTMPSI.SUPERCLASS, "superclass");
        Association ta = tm.createAssociation(supersubClassTopic);
        ta.addPlayer(t, subclassTopic);
        ta.addPlayer(superclass, superclassTopic);
    }


    
    // -------------------------------------------------------------------------
    
    
    
    
    public static String solveOBOId(Locator l) throws TopicMapException {
        if(l == null) return null;
        String id = l.toExternalForm();
        if(id.startsWith(SCHEMA_SI) && id.length()>SCHEMA_SI.length()) {
            id = id.substring(SCHEMA_SI.length());
            try { id = URLDecoder.decode(id, "UTF-8"); } catch(Exception e) {}
            //id = id.replace('/', ':');
        }
        else if(id.startsWith(SI) && id.length()>SI.length()) {
            id = id.substring(SI.length());
            try { id = URLDecoder.decode(id, "UTF-8"); } catch(Exception e) {}
            //id = id.replace('/', ':');
        }
        return id;
    }
    
    public static Locator makeLocator(String id) {
        return new Locator(makeSI(id));
    }
   
    public static String makeSI(String id) {
        if(id == null) return null;
        id = OBO2Java(id);
        String si = OBO.SI + id; // WAS: id.replace(':', '/');
        return si;
    }
    
    
    
    public static String makeSICarefully(String id) {
        if(id == null) return null;
        id = OBO2Java(id);
        try {
            String si = OBO.SI + URLEncoder.encode(id, "UTF-8");
            return si;
        }
        catch(Exception e) { }
        return OBO.SI + id;
    }
    
    
    public static String makeSI(String prefix, String str) {
        if(str == null) return null;
        str = OBO2Java(str);
        try {
            String si = OBO.SI + prefix + URLEncoder.encode(str, "UTF-8");
            return si;
        }
        catch(Exception e) { }
        return OBO.SI +  prefix + str;
    }

    
    public static String makeTermSI(String termId) {
        String termSI = makeSI(termId);
        String[] termIdParts = termId.split(":");
        if(termIdParts.length > 1) {
            String prefix = termIdParts[0].trim();
            String id = termIdParts[1].trim();
            if("Wikipedia".equalsIgnoreCase(prefix))
                termSI = "http://en.wikipedia.org/wiki/"+id;
            else if("AGI_LacusCode".equalsIgnoreCase(prefix))
                termSI = "http://arabidopsis.org/servlets/TairObject?type=locus&name="+id;
            else if("AGI_LacusCode".equalsIgnoreCase(prefix))
                termSI = "http://www.plasmodb.org/gene/"+id;
            else if("AraCyc".equalsIgnoreCase(prefix))
                termSI = "http://www.arabidopsis.org:1555/ARA/NEW-IMAGE?type=NIL&object="+id;
            else if("BIOMD".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/compneur-srv/biomodels-main/publ-model.do?mid="+id;
            else if("BRENDA".equalsIgnoreCase(prefix))
                termSI = "http://www.brenda.uni-koeln.de/php/result_flat.php4?ecno="+id;
            else if("Broad_MGG".equalsIgnoreCase(prefix))
                termSI = "http://www.broad.mit.edu/annotation/genome/magnaporthe_grisea/GeneLocus.html?sp=S"+id;
            else if("CAS_SPC".equalsIgnoreCase(prefix))
                termSI = "http://research.calacademy.org/research/ichthyology/catalog/getname.asp?rank=Species&id="+id;
            else if("CBS".equalsIgnoreCase(prefix))
                termSI = "http://www.cbs.dtu.dk/services/"+id+"/";
            else if("CDD".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid="+id+"";
            else if("CGD".equalsIgnoreCase(prefix))
                termSI = "http://www.candidagenome.org/cgi-bin/locus.pl?dbid="+id+"";
            else if("CGD_LOCUS".equalsIgnoreCase(prefix))
                termSI = "http://www.candidagenome.org/cgi-bin/locus.pl?locus="+id+"";
            else if("CGD_REF".equalsIgnoreCase(prefix))
                termSI = "http://www.candidagenome.org/cgi-bin/reference/reference.pl?refNo="+id+"";
            else if("CGSC".equalsIgnoreCase(prefix))
                termSI = "http://cgsc.biology.yale.edu/Site.php?ID="+id+"";
            else if("ChEBI".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:"+id+"";
            else if("CL".equalsIgnoreCase(prefix)) {
                ;//termSI = ""+id+"";
            }
            else if("COG_Cluster".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/COG/new/release/cow.cgi?cog="+id+"";
            else if("COG_Function".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/COG/grace/shokog.cgi?fun="+id+"";
            else if("COG_Pathway".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/COG/new/release/coglist.cgi?pathw="+id+"";
            else if("dictyBase".equalsIgnoreCase(prefix))
                termSI = "http://dictybase.org/db/cgi-bin/gene_page.pl?dictybaseid="+id+"";
            else if("dictyBase_gene_name".equalsIgnoreCase(prefix))
                termSI = "http://dictybase.org/db/cgi-bin/gene_page.pl?gene_name="+id+"";
            else if("dictyBase_REF".equalsIgnoreCase(prefix))
                termSI = "http://dictybase.org/db/cgi-bin/dictyBase/reference/reference.pl?refNo="+id+"";
            else if("DOI".equalsIgnoreCase(prefix))
                termSI = "http://dx.doi.org/"+id+"";
            else if("EC".equalsIgnoreCase(prefix))
                termSI = "http://www.expasy.org/enzyme/"+id+"";
            else if("ECK".equalsIgnoreCase(prefix))
                termSI = "http://www.ecogene.org/geneInfo.php?eck_id="+id+"";
            else if("EcoCyc".equalsIgnoreCase(prefix))
                termSI = "http://biocyc.org/ECOLI/NEW-IMAGE?type=PATHWAY&object="+id+"";
            else if("EcoCyc_REF".equalsIgnoreCase(prefix))
                termSI = "http://biocyc.org/ECOLI/reference.html?type=CITATION-FRAME&object="+id+"";
            else if("ECOGENE".equalsIgnoreCase(prefix))
                termSI = "http://www.ecogene.org/geneInfo.php?eg_id="+id+"";
            else if("EMBL".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&Submit=Go&id="+id+"";
            else if("DDBJ".equalsIgnoreCase(prefix))
                termSI = "http://arsa.ddbj.nig.ac.jp/arsa/ddbjSplSearch?KeyWord="+id+"";
            else if("GenBank".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=nucleotide&val="+id+"";
            else if("ENSEMBL".equalsIgnoreCase(prefix))
                termSI = "http://www.ensembl.org/perl/protview?peptide="+id+"";
            else if("ENZYME".equalsIgnoreCase(prefix))
                termSI = "http://www.expasy.ch/cgi-bin/nicezyme.pl?"+id+"";
            else if("FB".equalsIgnoreCase(prefix))
                termSI = "http://flybase.org/reports/"+id+".html";
            else if("GDB".equalsIgnoreCase(prefix))
                termSI = "http://www.gdb.org/gdb-bin/genera/accno?accessionNum=GDB:"+id+"";
            else if("GeneDB_Gmorsitans".equalsIgnoreCase(prefix))
                termSI = "http://www.genedb.org/genedb/Search?organism=glossina&name="+id+"";
            else if("GeneDB_Lmajor".equalsIgnoreCase(prefix))
                termSI = "http://www.genedb.org/genedb/Search?organism=leish&name="+id+"";
            else if("GeneDB_Pfalciparum".equalsIgnoreCase(prefix))
                termSI = "http://www.genedb.org/genedb/Search?organism=malaria&name="+id+"";
            else if("GeneDB_Spombe".equalsIgnoreCase(prefix))
                termSI = "http://www.genedb.org/genedb/Search?organism=pombe&name="+id+"";
            else if("GeneDB_Tbrucei".equalsIgnoreCase(prefix))
                termSI = "http://www.genedb.org/genedb/Search?organism=pombe&name="+id+"";
            else if("GO".equalsIgnoreCase(prefix))
                termSI = "http://amigo.geneontology.org/cgi-bin/amigo/term-details.cgi?term="+id+"";
            else if("GO_REF".equalsIgnoreCase(prefix))
                termSI = "http://www.geneontology.org/cgi-bin/references.cgi#GO_REF:"+id+"";
            else if("GR".equalsIgnoreCase(prefix))
                termSI = "http://www.gramene.org/db/searches/browser?search_type=All&RGN=on&query="+id+"";
            else if("GR_GENE".equalsIgnoreCase(prefix))
                termSI = "http://www.gramene.org/db/genes/search_gene?acc="+id+"";
            else if("GR_PROTEIN".equalsIgnoreCase(prefix))
                termSI = "http://www.gramene.org/db/protein/protein_search?acc="+id+"";
            else if("GR_QTL".equalsIgnoreCase(prefix))
                termSI = "http://www.gramene.org/db/qtl/qtl_display?qtl_accession_id="+id+"";
            else if("GR_REF".equalsIgnoreCase(prefix))
                termSI = "http://www.gramene.org/db/literature/pub_search?ref_id="+id+"";
            else if("H-invDB_cDNA".equalsIgnoreCase(prefix))
                termSI = "http://www.h-invitational.jp/hinv/spsoup/transcript_view?acc_id="+id+"";
            else if("H-invDB_locus".equalsIgnoreCase(prefix))
                termSI = "http://www.h-invitational.jp/hinv/spsoup/locus_view?hix_id="+id+"";
            else if("HAMAP".equalsIgnoreCase(prefix))
                termSI = "http://us.expasy.org/unirules/"+id+"";
            else if("HGNC".equalsIgnoreCase(prefix))
                termSI = "http://www.genenames.org/data/hgnc_data.php?hgnc_id=HGNC:"+id+"";
            else if("HGNC_gene".equalsIgnoreCase(prefix))
                termSI = "http://www.genenames.org/data/hgnc_data.php?app_sym="+id+"";
            else if("IMG".equalsIgnoreCase(prefix))
                termSI = "http://img.jgi.doe.gov/cgi-bin/pub/main.cgi?section=GeneDetail&page=geneDetail&gene_oid="+id+"";
            else if("IntAct".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/intact/search/do/search?searchString="+id+"";
            else if("InterPro".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/interpro/DisplayIproEntry?ac="+id+"";
            else if("ISBN".equalsIgnoreCase(prefix))
                termSI = "http://my.linkbaton.com/get?lbCC=q&nC=q&genre=book&item="+id+"";
            else if("IUPHAR_GPCR".equalsIgnoreCase(prefix))
                termSI = "http://www.iuphar-db.org/GPCR/ChapterMenuForward?chapterID="+id+"";
            else if("IUPHAR_RECEPTOR".equalsIgnoreCase(prefix))
                termSI = "http://www.iuphar-db.org/GPCR/ReceptorDisplayForward?receptorID="+id+"";
            else if("KEGG_PATHWAY".equalsIgnoreCase(prefix))
                termSI = "http://www.genome.ad.jp/dbget-bin/www_bget?path:"+id+"";
            else if("KEGG_LIGAND".equalsIgnoreCase(prefix))
                termSI = "http://www.genome.ad.jp/dbget-bin/www_bget?cpd:"+id+"";
            else if("LIFEdb".equalsIgnoreCase(prefix))
                termSI = "http://www.dkfz.de/LIFEdb/LIFEdb.aspx?ID="+id+"";
            else if("MA".equalsIgnoreCase(prefix))
                termSI = "http://www.informatics.jax.org/searches/AMA.cgi?id=MA:"+id+"";
            else if("MaizeGDB".equalsIgnoreCase(prefix))
                termSI = "http://www.maizegdb.org/cgi-bin/id_search.cgi?id="+id+"";
            else if("MaizeGDB_Locus".equalsIgnoreCase(prefix))
                termSI = "http://www.maizegdb.org/cgi-bin/displaylocusresults.cgi?term="+id+"";
            else if("MEROPS".equalsIgnoreCase(prefix))
                termSI = "http://merops.sanger.ac.uk/cgi-bin/pepsum?mid="+id+"";
            else if("MEROPS_fam".equalsIgnoreCase(prefix))
                termSI = "http://merops.sanger.ac.uk/cgi-bin/famsum?family="+id+"";
            else if("MeSH".equalsIgnoreCase(prefix))
                termSI = "http://www.nlm.nih.gov/cgi/mesh/2005/MB_cgi?mode=&term="+id+"";
            else if("MetaCyc".equalsIgnoreCase(prefix))
                termSI = "http://biocyc.org/META/NEW-IMAGE?type=NIL&object="+id+"";
            else if("MGI".equalsIgnoreCase(prefix))
                termSI = "http://www.informatics.jax.org/searches/accession_report.cgi?id=MGI:"+id+"";
            else if("MIPS_funcat".equalsIgnoreCase(prefix))
                termSI = "http://mips.gsf.de/cgi-bin/proj/funcatDB/search_advanced.pl?action=2&wert="+id+"";
            else if("MO".equalsIgnoreCase(prefix))
                termSI = "http://mged.sourceforge.net/ontologies/MGEDontology.php#"+id+"";
            else if("NASC_code".equalsIgnoreCase(prefix))
                termSI = "http://seeds.nottingham.ac.uk/NASC/stockatidb.lasso?code="+id+"";
            else if("NCBI".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val="+id+"";
            else if("NCBI_Gene".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/sites/entrez?cmd=Retrieve&db=gene&list_uids="+id+"";
            else if("NCBI_gi".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val="+id+"";
            else if("NCBI_GP".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&val="+id+"";
            else if("NMPDR".equalsIgnoreCase(prefix))
                termSI = "http://www.nmpdr.org/linkin.cgi?id="+id+"";
            else if("OMIM".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/dispomim.cgi?id="+id+"";
            else if("PAMGO".equalsIgnoreCase(prefix))
                termSI = "http://agro.vbi.vt.edu/public/servlet/GeneEdit?&Search=Search&level=2&genename="+id+"";
            else if("PAMGO_MGG".equalsIgnoreCase(prefix))
                termSI = "http://scotland.fgl.ncsu.edu/cgi-bin/adHocQuery.cgi?adHocQuery_dbName=smeng_goannotation&Action=Data&QueryName=Functional+Categorization+of+MGG+GO+Annotation&P_DBObjectSymbol=&P_EvidenceCode=&P_Aspect=&P_DBObjectSynonym=&P_KeyWord="+id+"";
            else if("PAMGO_VMD".equalsIgnoreCase(prefix))
                termSI = "http://vmd.vbi.vt.edu/cgi-bin/browse/go_detail.cgi?gene_id="+id+"";
            else if("PDB".equalsIgnoreCase(prefix))
                termSI = "http://www.rcsb.org/pdb/cgi/explore.cgi?pid=223051005992697&pdbId="+id+"";
            else if("Pfam".equalsIgnoreCase(prefix))
                termSI = "http://www.sanger.ac.uk/cgi-bin/Pfam/getacc?"+id+"";
            else if("PharmGKB_PA".equalsIgnoreCase(prefix))
                termSI = "http://www.pharmgkb.org/do/serve?objId="+id+"";
            else if("PharmGKB_PGKB".equalsIgnoreCase(prefix))
                termSI = "http://www.pharmgkb.org/do/serve?objId="+id+"";
            else if("PIR".equalsIgnoreCase(prefix))
                termSI = "http://pir.georgetown.edu/cgi-bin/pirwww/nbrfget?uid="+id+"";
            else if("PIRSF".equalsIgnoreCase(prefix))
                termSI = "http://pir.georgetown.edu/cgi-bin/ipcSF?id="+id+"";
            else if("PMID".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/pubmed/"+id+"";
            else if("PO".equalsIgnoreCase(prefix))
                termSI = "http://www.plantontology.org/amigo/go.cgi?action=query&view=query&search_constraint=terms&query="+id+"";
            else if("PRINTS".equalsIgnoreCase(prefix))
                termSI = "http://www.bioinf.manchester.ac.uk/cgi-bin/dbbrowser/sprint/searchprintss.cgi?display_opts=Prints&category=None&queryform=false&regexpr=off&prints_accn="+id+"";
            else if("ProDom".equalsIgnoreCase(prefix))
                termSI = "http://prodes.toulouse.inra.fr/prodom/current/cgi-bin/request.pl?question=DBEN&query="+id+"";
            else if("Prosite".equalsIgnoreCase(prefix))
                termSI = "http://www.expasy.ch/cgi-bin/prosite-search-ac?"+id+"";
            else if("PseudoCAP".equalsIgnoreCase(prefix))
                termSI = "http://v2.pseudomonas.com/getAnnotation.do?locusID="+id+"";
            else if("PSI-MOD".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/ontology-lookup/?termId=MOD:"+id+"";
            else if("PubChem_BioAssay".equalsIgnoreCase(prefix))
                termSI = "http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid="+id+"";
            else if("PubChem_Compound".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?CMD=search&DB=pccompound&term="+id+"";
            else if("PubChem_Substance".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?CMD=search&DB=pcsubstance&term="+id+"";
            else if("Reactome".equalsIgnoreCase(prefix))
                termSI = "http://www.reactome.org/cgi-bin/eventbrowser_st_id?ST_ID="+id+"";
            else if("REBASE".equalsIgnoreCase(prefix))
                termSI = "http://rebase.neb.com/rebase/enz/"+id+".html";
            else if("RefSeq".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val="+id+"";
            else if("RefSeq_NA".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val="+id+"";
            else if("RefSeq_Prot".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val="+id+"";
            else if("RGD".equalsIgnoreCase(prefix))
                termSI = "http://rgd.mcw.edu/generalSearch/RgdSearch.jsp?quickSearch=1&searchKeyword="+id+"";
            else if("RNAmods".equalsIgnoreCase(prefix))
                termSI = "http://medlib.med.utah.edu/cgi-bin/rnashow.cgi?"+id+"";
            else if("SEED".equalsIgnoreCase(prefix))
                termSI = "http://www.theseed.org/linkin.cgi?id="+id+"";
            else if("SGD".equalsIgnoreCase(prefix))
                termSI = "http://db.yeastgenome.org/cgi-bin/locus.pl?dbid="+id+"";
            else if("SGD_LOCUS".equalsIgnoreCase(prefix))
                termSI = "http://db.yeastgenome.org/cgi-bin/locus.pl?locus="+id+"";
            else if("SGD_REF".equalsIgnoreCase(prefix))
                termSI = "http://db.yeastgenome.org/cgi-bin/reference/reference.pl?dbid="+id+"";
            else if("SGN".equalsIgnoreCase(prefix))
                termSI = "http://www.sgn.cornell.edu/phenome/locus_display.pl?locus_id="+id+"";
            else if("SGN_ref".equalsIgnoreCase(prefix))
                termSI = "http://www.sgn.cornell.edu/chado/publication.pl?pub_id="+id+"";
            else if("SMART".equalsIgnoreCase(prefix))
                termSI = "http://smart.embl-heidelberg.de/smart/do_annotation.pl?BLAST=DUMMY&DOMAIN="+id+"";
            else if("SO".equalsIgnoreCase(prefix))
                termSI = "http://song.sourceforge.net/SOterm_tables.html#"+id+"";
            else if("SP_KW".equalsIgnoreCase(prefix))
                termSI = "http://www.expasy.org/cgi-bin/get-entries?KW="+id+"";
            else if("Swiss-Prot".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.uniprot.org/entry/"+id+"";
            else if("TAIR".equalsIgnoreCase(prefix))
                termSI = "http://arabidopsis.org/servlets/TairObject?accession="+id+"";
            else if("taxon".equalsIgnoreCase(prefix))
                termSI = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id="+id+"";
            else if("TC".equalsIgnoreCase(prefix))
                termSI = "http://www.tcdb.org/tcdb/index.php?tc="+id+"";
            else if("TGD_LOCUS".equalsIgnoreCase(prefix))
                termSI = "http://db.ciliate.org/cgi-bin/locus.pl?locus="+id+"";
            else if("TGD_REF".equalsIgnoreCase(prefix))
                termSI = "http://db.ciliate.org/cgi-bin/reference/reference.pl?dbid="+id+"";
            else if("TIGR_CMR".equalsIgnoreCase(prefix))
                termSI = "http://cmr.jcvi.org/tigr-scripts/CMR/shared/GenePage.cgi?locus="+id+"";
            else if("TIGR_Ath1".equalsIgnoreCase(prefix))
                termSI = "http://www.tigr.org/tigr-scripts/euk_manatee/shared/ORF_infopage.cgi?db=ath1&orf="+id+"";
            else if("TIGR_Pfa1".equalsIgnoreCase(prefix))
                termSI = "http://www.tigr.org/tigr-scripts/euk_manatee/shared/ORF_infopage.cgi?db=pfa1&orf="+id+"";
            else if("TIGR_Tba1".equalsIgnoreCase(prefix))
                termSI = "http://www.tigr.org/tigr-scripts/euk_manatee/shared/ORF_infopage.cgi?db=tba1&orf="+id+"";
            else if("TIGR_TIGRFAMS".equalsIgnoreCase(prefix))
                termSI = "http://cmr.jcvi.org/cgi-bin/CMR/HmmReport.cgi?hmm_acc="+id+"";
            else if("TIGR_EGAD".equalsIgnoreCase(prefix))
                termSI = "http://www.tigr.org/tigr-scripts/CMR2/ht_report.spl?prot_id="+id+"";
            else if("TIGR_GenProp".equalsIgnoreCase(prefix))
                termSI = "http://www.tigr.org/tigr-scripts/CMR2/genome_property_def.spl?prop_acc="+id+"";
            else if("TrEMBL".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.uniprot.org/entry/"+id+"";
            else if("UM-BBD_enzymeID".equalsIgnoreCase(prefix))
                termSI = "http://umbbd.msi.umn.edu/servlets/pageservlet?ptype=ep&enzymeID="+id+"";
            else if("UM-BBD_reactionID".equalsIgnoreCase(prefix))
                termSI = "http://umbbd.msi.umn.edu/servlets/pageservlet?ptype=r&reacID="+id+"";
            else if("UM-BBD_ruleID".equalsIgnoreCase(prefix))
                termSI = "http://umbbd.msi.umn.edu/servlets/rule.jsp?rule="+id+"";
            else if("UniParc".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.ac.uk/cgi-bin/dbfetch?db=uniparc&id="+id+"";
            else if("UniProtKB".equalsIgnoreCase(prefix))
                termSI = "http://www.ebi.uniprot.org/entry/"+id+"";
            else if("VEGA".equalsIgnoreCase(prefix))
                termSI = "http://vega.sanger.ac.uk/perl/searchview?species=all&idx=All&q="+id+"";
            else if("VMD".equalsIgnoreCase(prefix))
                termSI = "http://vmd.vbi.vt.edu/cgi-bin/browse/browserDetail_new.cgi?gene_id="+id+"";
            else if("WB".equalsIgnoreCase(prefix))
                termSI = "http://www.wormbase.org/db/gene/gene?name="+id+"";
            else if("WB_REF".equalsIgnoreCase(prefix))
                termSI = "http://www.wormbase.org/db/misc/paper?name="+id+"";
            else if("WP".equalsIgnoreCase(prefix))
                termSI = "http://www.wormbase.org/db/get?class=Protein;name=WP:"+id+"";
            else if("ZFIN".equalsIgnoreCase(prefix))
                termSI = "http://zfin.org/cgi-bin/ZFIN_jump?record="+id+"";

            
        }
        return termSI;
    }
    
    
    public static String makeBasename(String termid) {
        if(termid == null) return null;
        String basename = OBO2Java(termid);
        return basename;
    }

    public static String OBO2Java(String str) {
        if(str == null) return null;
        str = str.replace("\\n", "\n");
        str = str.replace("\\W", " ");
        str = str.replace("\\t", "\t");
        str = str.replace("\\:", ":");
        str = str.replace("\\,", ",");
        str = str.replace("\\\"", "\"");
        str = str.replace("\\(", "(");
        str = str.replace("\\)", ")");
        str = str.replace("\\{", "{");
        str = str.replace("\\}", "}");
        str = str.replace("\\[", "[");
        str = str.replace("\\]", "]");
        str = str.replace("\\!", "!");
        str = str.replaceAll("\\\\(.)", "$1");
        //str = str.replace("\\\\", "\\");
        return str;
    }
    public static String Java2OBO(String str) {
        if(str == null) return null;
        str = str.replace("\\", "\\\\");
        str = str.replace("\"", "\\\"");
        str = str.replace("\n", "\\n");
        //str = str.replace(" ", "\\W");
        str = str.replace("\t", "\\t");
        str = str.replace(":", "\\:");
        str = str.replace(",", "\\,");
        str = str.replace("!", "\\!");
        //str = str.replace("(", "\\(");
        //str = str.replace(")", "\\)");
        //str = str.replace("[", "\\[");
        //str = str.replace("]", "\\]");
        return str;
    }
    public static String Java2OBOLite(String str) {
        if(str == null) return null;
        str = str.replace("\\", "\\\\");
        str = str.replace("\"", "\\\"");
        str = str.replace("\n", "\\n");
        //str = str.replace(" ", "\\W");
        str = str.replace("\t", "\\t");
        //str = str.replace(":", "\\:");
        //str = str.replace(",", "\\,");
        str = str.replace("!", "\\!");
        //str = str.replace("(", "\\(");
        //str = str.replace(")", "\\)");
        //str = str.replace("[", "\\[");
        //str = str.replace("]", "\\]");
        return str;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public static void importExport(String dir) {
        File[] importFiles = IObox.getFiles(dir, ".+\\.obo", 1, 999);
		importExport(importFiles);
    }
    
    
    
    public static void importExport(File[] importFiles) {
        if(importFiles != null && importFiles.length > 0) {
            for(int i=0; i<importFiles.length; i++) {
                try {
                    TopicMap map = new org.wandora.topicmap.memory.TopicMapImpl();

                    File importFile = importFiles[i];
                    OBOImport importer = new OBOImport();
                    importer.importOBO(new FileInputStream(importFile), map);

                    OBOExport exporter = new OBOExport();
                    String exportFileName = importFile.getAbsolutePath();
                    exportFileName = exportFileName.substring(exportFileName.length()-4)+"_wandoraexport.obo";
                    File exportFile = new File(exportFileName);
                    ArrayList<String> namespaces = importer.getNamespaces();
                    if(namespaces != null && namespaces.size() >0) {
                        exporter.exportOBO(exportFile, namespaces.toArray( new String[] {} ), map);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            System.out.println("No OBO files to import!");
        }
    }
    

}
