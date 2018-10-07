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



package org.wandora.application.tools.extractors.any23;



import java.io.*;

import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.http.HTTPClient;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.FileDocumentSource;
import org.apache.any23.source.HTTPDocumentSource;
import org.apache.any23.source.StringDocumentSource;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.openrdf.model.Value;
import org.wandora.application.Wandora;
import org.wandora.application.gui.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDF2TopicMapsMapping;
import org.wandora.topicmap.*;


/**
 * Extract information out of given resources using Any23 (See https://any23.apache.org/index.html).
 * Transform extracted information to topics and associations. Sign all extracted
 * information with the information source. Information source is added to associations
 * as an extra player topic.
 *
 * @author akivela
 */


public class Any23Extractor extends AbstractExtractor {

    public static final String SOURCE_TYPE = "http://wandora.org/si/any23/source"; // Used also as a role-topic.

    public static final String ANY23_PREDICATE_TYPE = "http://wandora.org/si/any23/predicate";
    public static final String ANY23_SUBJECT_TYPE = "http://wandora.org/si/any23/subject";
    public static final String ANY23_OBJECT_TYPE = "http://wandora.org/si/any23/object";
    public static final String ANY23_BASE = "http://any23.org/";
    
    public static boolean SIGN_ALL_TRIPLETS = true;
    public static boolean TYPE_ALL_PREDICATES = true;
    public static boolean TYPE_ALL_SUBJECTS = true;
    public static boolean TYPE_ALL_OBJECTS = true;

    private String namespace = null;
    private String tripletSource = null;


    

    /** Creates a new instance of Any23Extractor */
    public Any23Extractor() {
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }


    @Override
    public String getName() {
        return "Any23 extractor";
    }
    @Override
    public String getDescription(){
        return "Any23 extractor reads triplets out of given sources and "+
               "transforms triplets to associations and topics.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_any23.png");
    }

    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }


    private final String[] contentTypes=new String[] { "text/html", "text/xml", "application/xml", "application/rdf+xml", "application/xhtml+xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }


    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url != null) {
            tripletSource = url.toExternalForm();
            Any23 runner = new Any23();

            runner.setHTTPUserAgent("Wandora ANY23 Extractor");
            HTTPClient httpClient = runner.getHTTPClient();
            DocumentSource source = new HTTPDocumentSource(
                httpClient,
                url.toExternalForm()
            );
            namespace = url.toExternalForm();
            TripleHandler handler = new TopicMapsCreator(topicMap);
            runner.extract(source, handler);
        }
        tripletSource = null;
        return true;
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file != null) {
            Any23 runner = new Any23();
            tripletSource = file.toURI().toURL().toExternalForm();

            DocumentSource source = new FileDocumentSource(file);
            TripleHandler handler = new TopicMapsCreator(topicMap);
            runner.extract(source, handler);
        }
        tripletSource = null;
        return true;
    }


    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        if(str != null && str.length() > 0) {
            Any23 runner = new Any23();

            DocumentSource source = new StringDocumentSource(str, tripletSource);
            TripleHandler handler = new TopicMapsCreator(topicMap);
            runner.extract(source, handler);
        }
        return true;
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        return true;
    }



    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        tripletSource = request.getSource();
        String reply = super.doBrowserExtract(request, wandora);
        tripletSource = null;
        return reply;
    }



    // -------------------------------------------------------------------------


    public Topic getPredicateTopic(TopicMap tm, String predicate) throws TopicMapException {
        Topic predicateTopic = getOrCreateTopic(tm, predicate);
        if(TYPE_ALL_PREDICATES) {
            Topic predicateType = getPredicateType(tm);
            predicateTopic.addType(predicateType);
        }
        return predicateTopic;
    }
    public Topic getPredicateType(TopicMap tm) throws TopicMapException {
        Topic predicateType = ExtractHelper.getOrCreateTopic(ANY23_PREDICATE_TYPE, "Any23 predicate", tm);
        Topic any23Type = getAny23Type(tm);
        ExtractHelper.makeSubclassOf(predicateType, any23Type, tm);
        return predicateType;
    }
    
    
    

    public Topic getObjectTopic(TopicMap tm, String object) throws TopicMapException {
        Topic objectTopic = getOrCreateTopic(tm, object);
        if(TYPE_ALL_OBJECTS) {
            Topic objectType = getObjectType(tm);
            objectTopic.addType(objectType);
        }
        return objectTopic;
    }
    public Topic getObjectType(TopicMap tm) throws TopicMapException {
        Topic objectType = ExtractHelper.getOrCreateTopic(ANY23_OBJECT_TYPE, "Any23 object", tm);
        Topic any23Type = getAny23Type(tm);
        ExtractHelper.makeSubclassOf(objectType, any23Type, tm);
        return objectType;
    }
    
    
    
    
    
    public Topic getSubjectTopic(TopicMap tm, String subject) throws TopicMapException {
        Topic subjectTopic = getOrCreateTopic(tm, subject);
        if(TYPE_ALL_SUBJECTS) {
            Topic subjectType = getSubjectType(tm);
            subjectTopic.addType(subjectType);
        }
        return subjectTopic;
    }
    
    public Topic getSubjectType(TopicMap tm) throws TopicMapException {
        Topic subjectType = ExtractHelper.getOrCreateTopic(ANY23_SUBJECT_TYPE, "Any23 subject", tm);
        Topic any23Type = getAny23Type(tm);
        ExtractHelper.makeSubclassOf(subjectType, any23Type, tm);
        return subjectType;
        
    }

    


    public Topic getSourceRoleType(TopicMap tm) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(SOURCE_TYPE, "Any23 source", tm);
    }
    public Topic getSourcePlayer(TopicMap tm) throws TopicMapException {
        if(tripletSource != null) {
            Topic sourceTopic = ExtractHelper.getOrCreateTopic(tripletSource, tm);
            Topic sourceType = getSourceType(tm);
            sourceTopic.addType(sourceType);
            return sourceTopic;
        }
        return null;
    }
    
    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        Topic sourceType = ExtractHelper.getOrCreateTopic(SOURCE_TYPE, "Any23 triplet source", tm);
        Topic any23Type = getAny23Type(tm);
        ExtractHelper.makeSubclassOf(sourceType, any23Type, tm);
        return sourceType;
    }
    
    
    
    public Topic getAny23Type(TopicMap tm) throws TopicMapException {
        Topic any23Type = ExtractHelper.getOrCreateTopic(ANY23_BASE, "Any23", tm);
        Topic wc = ExtractHelper.getOrCreateTopic(TMBox.WANDORACLASS_SI, tm);
        ExtractHelper.makeSubclassOf(any23Type, wc, tm);
        return any23Type;
    }
    
    
    
    

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    public class TopicMapsCreator implements TripleHandler {
        private TopicMap tm = null;
        private ArrayList<ExtractionContext> extractionContexts = null;
        private long contentLength = -1;
        private int tripletCounter = 0;

        public String defaultLanguage = "en";
        public final String defaultOccurrenceScopeSI = TMBox.LANGINDEPENDENT_SI;



        public TopicMapsCreator(TopicMap topicMap) {
            this.tm = topicMap;
            extractionContexts = new ArrayList<ExtractionContext>();
            namespace = null;
            tripletCounter = 0;
        }


        @Override
        public void startDocument(org.openrdf.model.URI uri) throws TripleHandlerException {
            namespace = uri.getNamespace();
        }

        @Override
        public void openContext(ExtractionContext ec) throws TripleHandlerException {
            extractionContexts.add(ec);
        }

        @Override
        public void receiveTriple(org.openrdf.model.Resource subject, org.openrdf.model.URI predicate, Value object, org.openrdf.model.URI graph, ExtractionContext ec) throws TripleHandlerException {
            try {
                tripletCounter++;

                hlog("Found triplet: "+subject+" --- "+predicate+" --- "+object);

                Topic subjectTopic = getSubjectTopic(tm, subject.toString());

                if(object instanceof org.openrdf.model.Literal) {
                    org.openrdf.model.Literal literal = (org.openrdf.model.Literal) object;
                    Topic predicateTopic = getPredicateTopic(tm, predicate.toString());
                    String occurrenceLang = literal.getLanguage();
                    if(occurrenceLang == null) {
                        occurrenceLang = XTMPSI.getLang(defaultLanguage);
                    }

                    String literalStr = literal.getLabel();

                    String oldOccurrence = ""; // subjectTopic.getData(predicateTopic, occurrenceLang);
                    if(oldOccurrence != null && oldOccurrence.length() > 0) {
                        literalStr = oldOccurrence + "\n\n" + literalStr;
                    }
                    subjectTopic.setData(predicateTopic, getOrCreateTopic(tm, occurrenceLang), literalStr);
                }
                else {
                    Topic objectTopic = getObjectTopic(tm, object.toString());
                    Topic predicateTopic = getPredicateTopic(tm, predicate.toString());
                    Association association = tm.createAssociation(predicateTopic);
                    association.addPlayer(subjectTopic, getSubjectType(tm));
                    association.addPlayer(objectTopic, getObjectType(tm));

                    if(SIGN_ALL_TRIPLETS && tripletSource != null) {
                        try {
                            Topic role = getSourceRoleType(tm);
                            Topic player = getSourcePlayer(tm);
                            if(role != null && player != null) {
                                association.addPlayer(player, role);
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(ec != null) {
                    //System.out.println(" context: "+ec.getDocumentURI());
                }
            }
            catch(Exception e) {
                log(e);
                e.printStackTrace();
            }
        }

        @Override
        public void receiveNamespace(String prefix, String uri, ExtractionContext ec) throws TripleHandlerException {
            namespace = uri;
        }

        @Override
        public void closeContext(ExtractionContext ec) throws TripleHandlerException {
            for(int i=extractionContexts.size()-1; i>=0; i--) {
                if(ec.equals(extractionContexts.get(i))) {
                    extractionContexts.remove(i);
                }
            }
        }

        @Override
        public void endDocument(org.openrdf.model.URI uri) throws TripleHandlerException {
        }

        @Override
        public void setContentLength(long l) {
            contentLength = l;
        }

        @Override
        public void close() throws TripleHandlerException {
            log("Total "+tripletCounter+" triplets found!");
        }

    }


    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    


    public String getBaseSubject() {
        if(namespace != null)
            return namespace;
        else
            return "http://wandora.org/si/default";
    }


    // -------------------------------------------------------------------------



    // -------------------------------------------------------------------------



    public Topic getOrCreateTopic(TopicMap map, String si) {
        return getOrCreateTopic(map, si, null);
    }



    public Topic getOrCreateTopic(TopicMap map, String si, String basename) {
        Topic topic = null;
        try {
            if(si == null || si.length() == 0) {
                si = TopicTools.createDefaultLocator().toExternalForm();
            }
            if(si.startsWith("_:") && getBaseSubject() != null) {
                String base = getBaseSubject();
                if(base.endsWith("/")) si = base + si.substring(2);
                else si = base + "/" + si.substring(2);
            }
            if(si.indexOf("://") == -1 && getBaseSubject() != null) {
                String base = getBaseSubject();
                if(base.endsWith("/")) si = base + si;
                else si = base + "/" + si;
            }
            topic = map.getTopic(si);
            if(topic == null && basename != null) {
                topic = map.getTopicWithBaseName(basename);
                if(topic != null) {
                    topic.addSubjectIdentifier(new Locator(si));
                }
            }
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                if(basename != null) topic.setBaseName(basename);
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }


}
