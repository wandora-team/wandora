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
 * AbstractRDFExtractor.java
 *
 * Created on 14.2.2009,12:32
 */


package org.wandora.application.tools.extractors.rdf;



import org.wandora.application.tools.extractors.rdf.rdfmappings.RDF2TopicMapsMapping;
import java.net.*;
import java.io.*;
import javax.swing.*;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;

import org.wandora.application.gui.*;
import com.hp.hpl.jena.rdf.model.*;
import java.util.List;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFMapping;
import org.wandora.utils.Tuples.*;



/**
 * 
 * @author akivela
 */
public abstract class AbstractRDFExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;

	private String defaultEncoding = "UTF-8";
    public static String defaultLanguage = "en";
    public static final String defaultOccurrenceScopeSI = TMBox.LANGINDEPENDENT_SI;

    
    
    public AbstractRDFExtractor() {
        
    }
    

    @Override
    public String getName() {
        return "Abstract RDF extractor...";
    }
    
    @Override
    public String getDescription(){
        return "Read RDF feed and convert it to a topic map.";
    }
    

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_rdf.png");
    }
    
    private final String[] contentTypes=new String[] { "application/xml", "application/rdf+xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }

    @Override
    public void execute(Wandora wandora, Context context) {
        baseUrl = null;
        super.execute(wandora, context);
    }
    
    
    
    public String baseUrl = null;
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String url) {
        baseUrl = url;
        if(baseUrl != null) {
            int lindex = baseUrl.lastIndexOf("/");
            int findex = baseUrl.indexOf("/");
            //System.out.println("solving base url: "+lindex+", "+findex+", "+baseUrl);
            if(1+findex < lindex) {
                baseUrl = baseUrl.substring(0, lindex);
            }
        }
    }
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        setBaseUrl(url.toExternalForm());
        URLConnection uc = url.openConnection();
        uc.setUseCaches(false);
        boolean r = _extractTopicsFrom(uc.getInputStream(), topicMap);
        return r;
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        setBaseUrl(file.getAbsolutePath());
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }

    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception { 
        try {
            importRDF(in, topicMap);
            return true;
        }
        catch(Exception e) {
            log(e);
        }
        return false;
    }
    
    
    public boolean _extractTopicsFrom(String in, TopicMap tm) throws Exception {        
        try {           
            importRDF(new StringBufferInputStream(in), tm);
        }
        catch(Exception e){
            log("Exception when handling request",e);
        }
        return true;
    }
    
    
    /**
     * Override this if the RDF is in some other container format than XML/RDF.
     * See Jena documentation for Model.read.
     * @return 
     */
    public String getRDFContainerFormat(){
        return null;
    }
    
    
    public void importRDF(InputStream in, TopicMap map) {
        if(in != null) {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
            // read the RDF/XML file
            model.read(in, "", getRDFContainerFormat());
            RDF2TopicMap(model, map);
        }
    }
    
    


    
    public void RDF2TopicMap(Model model, TopicMap map) {
        // list the statements in the Model
        StmtIterator iter = model.listStatements();
        Statement stmt = null;
        int counter = 0;

        while (iter.hasNext() && !forceStop()) {
            try {
                stmt = iter.nextStatement();  // get next statement
                handleStatement(stmt, map);
            }
            catch(Exception e) {
                log(e);
            }
            counter++;
            setProgress(counter);
            if(counter % 100 == 0) hlog("RDF statements processed: " + counter);

        }
        log("Total RDF statements processed: " + counter);
    }

    
    public static final String RDF_LIST_ORDER="http://wandora.org/si/rdf/list_order";

    public void handleStatement(Statement stmt, TopicMap map) throws TopicMapException {
        Resource subject   = stmt.getSubject();     // get the subject
        Property predicate = stmt.getPredicate();   // get the predicate
        RDFNode object     = stmt.getObject();      // get the object

        String predicateS=predicate.toString();
        if(predicateS!=null && (
                predicateS.equals(RDFMapping.RDF_NS+"first") || 
                predicateS.equals(RDFMapping.RDF_NS+"rest") ) ) {
            // skip broken down list statements, the list
            // should be handled properly when they are the object
            // of some other statement
            return;
        }
            
        
        //System.out.println("statement:\n  "+subject+"\n   "+predicate+"\n    "+object);
        
        Topic subjectTopic = getOrCreateTopic(map, subject.toString());
                
        if(object.isResource()) {
            Topic predicateTopic = getOrCreateTopic(map, predicate.toString());
            if(object.canAs(RDFList.class)){
                List<RDFNode> list=((RDFList)object.as(RDFList.class)).asJavaList();
                int counter=1;
                Topic orderRole = getOrCreateTopic(map, RDF_LIST_ORDER, "List order");
                for(RDFNode listObject : list){
                    if(!listObject.isResource()){
                        log("List element is not a resource, skipping.");
                        continue;
                    }
                    Topic objectTopic = getOrCreateTopic(map, listObject.toString());                
                    Topic orderTopic = getOrCreateTopic(map, RDF_LIST_ORDER+"/"+counter, ""+counter);
                    Association association = map.createAssociation(predicateTopic);
                    association.addPlayer(subjectTopic, solveSubjectRoleFor(predicate, subject, map));
                    association.addPlayer(objectTopic, solveObjectRoleFor(predicate, object, map));
                    association.addPlayer(orderTopic, orderRole);
                    counter++;
                }
            }
            else {
                Topic objectTopic = getOrCreateTopic(map, object.toString());                
                Association association = map.createAssociation(predicateTopic);
                association.addPlayer(subjectTopic, solveSubjectRoleFor(predicate, subject, map));
                association.addPlayer(objectTopic, solveObjectRoleFor(predicate, object, map));
            }
        }
        else if(object.isLiteral()) {
            Topic predicateTopic = getOrCreateTopic(map, predicate.toString());
            String occurrenceLang = defaultOccurrenceScopeSI;
            String literal = ((Literal) object).getString();
            try { 
                String lang = stmt.getLanguage();
                if(lang != null) occurrenceLang = XTMPSI.getLang(lang);
            }
            catch(Exception e) {  
                /* PASSING BY */
            }
            String oldOccurrence = ""; // subjectTopic.getData(predicateTopic, occurrenceLang);
            if(oldOccurrence != null && oldOccurrence.length() > 0) {
                literal = oldOccurrence + "\n\n" + literal;
            }
            subjectTopic.setData(predicateTopic, getOrCreateTopic(map, occurrenceLang), literal);
        }
        else if(object.isURIResource()) {
            log("URIResource found but not handled!");
        }        
    }
    
    

    

    public abstract RDF2TopicMapsMapping[] getMappings();

    
    
 
    
    public Topic solveSubjectRoleFor(Property predicate, Resource subject, TopicMap map) {
        if(map == null) return null;
        RDF2TopicMapsMapping[] mappings = getMappings();
        String predicateString = predicate.toString();
        String subjectString = subject.toString();
        String si = RDF2TopicMapsMapping.DEFAULT_SUBJECT_ROLE_SI;
        String bn = RDF2TopicMapsMapping.DEFAULT_SUBJECT_ROLE_BASENAME;
        T2<String, String> mapped = null;
        for(int i=0; i<mappings.length; i++) {
            mapped = mappings[i].solveSubjectRoleFor(predicateString, subjectString);
            if(mapped.e1 != null) {
                si = mapped.e1;
                bn = mapped.e2;
                if(bn==null) bn=solveBasenameFor(si);
                break;
            }
        }
        return getOrCreateTopic(map, si, bn);
    }
    
    public Topic solveObjectRoleFor(Property predicate, RDFNode object, TopicMap map) {
        if(map == null) return null;
        RDF2TopicMapsMapping[] mappings = getMappings();
        String predicateString = predicate.toString();
        String objectString = object.toString();
        String si = RDF2TopicMapsMapping.DEFAULT_OBJECT_ROLE_SI;
        String bn = RDF2TopicMapsMapping.DEFAULT_OBJECT_ROLE_BASENAME;
        T2<String, String> mapped = null;
        for(int i=0; i<mappings.length; i++) {
            mapped = mappings[i].solveObjectRoleFor(predicateString, objectString);
            if(mapped.e1 != null) {
                si = mapped.e1;
                bn = mapped.e2;
                if(bn==null) bn=solveBasenameFor(si);
                break;
            }
        }
        return getOrCreateTopic(map, si, bn);
    }
    
    
    
    public String solveBasenameFor(String si) {
        if(si == null) return null;
        RDF2TopicMapsMapping[] mappings = getMappings();
        String bn = null;
        for(int i=0; i<mappings.length; i++) {
            bn = mappings[i].solveBasenameFor(si);
            if(bn != null) break;
        }
        return bn;
    }
    
    
    // -------------------------------------------------------------------------
    
    public Topic getOrCreateTopic(TopicMap map, String si) {
        return getOrCreateTopic(map, si, solveBasenameFor(si));
    }
    
    public Topic getOrCreateTopic(TopicMap map, String si, String basename) {
        Topic topic = null;
        try {
            if(si == null || si.length() == 0) {
                si = TopicTools.createDefaultLocator().toExternalForm();
            }
            if(si.indexOf("://") == -1 && getBaseUrl() != null) {
                si = getBaseUrl() + "/" + si;
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
            e.printStackTrace();
        }
        return topic;
    }
    
}
