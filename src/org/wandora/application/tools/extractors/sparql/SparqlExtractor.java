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
 * SparqlExtractorUI.java
 */



package org.wandora.application.tools.extractors.sparql;


import com.hp.hpl.jena.query.QuerySolution;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.Icon;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDF2TopicMapsMapping;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicTools;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.Tuples.T2;


import com.hp.hpl.jena.sparql.resultset.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import org.wandora.topicmap.TMBox;

/**
 *
 * @author akivela
 */

public class SparqlExtractor extends AbstractExtractor {

    public static String DEFAULT_SI_ENCODING = "UTF-8";

    public static String DEFAULT_RESULTSET_FORMAT = "JSON";
    public static String DEFAULT_HANDLE_METHOD = "RESULTSET-TOPICMAP";

    public static String RESULTSET_SI = "http://wandora.org/si/sparql/resultset";
    public static String COLUMN_SI = "http://wandora.org/si/sparql/resultset/column";
    public static String LITERAL_SI = "http://wandora.org/si/sparql/resultset/literal";

    private SparqlExtractorUI ui = null;
    public static final String defaultOccurrenceScopeSI = TMBox.LANGINDEPENDENT_SI;


    @Override
    public String getName() {
        return "SPARQL extractor";
    }

    @Override
    public String getDescription(){
        return "Transforms SPARQL result set to a topic map. Extractor requires a SPARQL endpoint.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_sparql.png");
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
        try {
            if(ui == null) {
                ui = new SparqlExtractorUI(wandora);
            }

            ui.open(wandora);

            if(ui.wasAccepted()) {
                setDefaultLogger();
                log("Requesting SPARQL result set...");
                TopicMap tm = wandora.getTopicMap();
                String[] urls = ui.getQueryURLs(this);
                String format = ui.getResultSetFormat();
                String handleMethod = ui.getHandleMethod();
                int c = 0;
                if(urls != null && urls.length > 0) {
                    for(int i=0; i<urls.length; i++) {
                        try {
                            URL u = new URL(urls[i]);
                            log("Extracting result set from '"+u.toExternalForm()+"'");
                            URLConnection uc = u.openConnection();
                            uc.setUseCaches(false);
                            ResultSet rs = importResultSet(uc.getInputStream(), format);
                            handleResultSet(rs, handleMethod, tm);
                            c++;
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                    if(c == 0) log("Failed to extract any of given feeds. Aborting.");
                    else log("Total " + c + " result set feeds extracted.");
                }
            }
            else {
                // log("User cancelled the extraction!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(ui != null && ui.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }


    // -----
    

    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        return _extractTopicsFrom(new FileInputStream(f), tm);
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), tm);
    }


    public boolean _extractTopicsFrom(URL url, TopicMap tm) throws Exception {
        URLConnection uc = url.openConnection();
        uc.setUseCaches(false);
        Wandora.initUrlConnection(uc);
        boolean r = _extractTopicsFrom(uc.getInputStream(), tm);
        return r;
    }




    public boolean _extractTopicsFrom(InputStream in, TopicMap tm) throws Exception {
        try {
            ResultSet rs = importResultSet(in, DEFAULT_RESULTSET_FORMAT);
            handleResultSet(rs, DEFAULT_HANDLE_METHOD, tm);
            return true;
        }
        catch(Exception e) {
            log(e);
        }
        return false;
    }





    public ResultSet importResultSet(InputStream in, String format) {
        ResultSet results = null;
        if(in != null) {
            if("JSON".equalsIgnoreCase(format)) {
                System.out.println("Processing SPARQL results set as JSON");
                results = JSONInput.fromJSON(in);
            }
            else if("XML".equalsIgnoreCase(format)) {
                System.out.println("Processing SPARQL results set as XML");
                results = ResultSetFactory.fromXML(in);
            }
            else if("RDF/XML".equalsIgnoreCase(format)) {
                System.out.println("Processing SPARQL results set as RDF/XML");
                results = ResultSetFactory.load(in, ResultSetFormat.syntaxRDF_XML);
            }
        }
        return results;
    }


    public void handleResultSet(ResultSet results, String method, TopicMap tm) throws Exception {
        if(results != null) {
            if("RESULTSET-RDF-TOPICMAP".equals(method)) {
                Model model = ResultSetFormatter.toModel(results);
                RDF2TopicMap(model, tm);
            }
            else {
                resultSet2TopicMap(results, tm);
            }
        }
        else {
            log("Warning: no result set available!");
        }
    }

    
    // -------------------------------------------------------------------------


    public void resultSet2TopicMap(ResultSet rs, TopicMap tm) throws Exception {
        List<String> columns = rs.getResultVars();
        int counter = 0;
        while(rs.hasNext()) {
            QuerySolution soln = rs.nextSolution();
            if(soln != null) {
                HashMap<Topic,Topic> roledPlayers = new HashMap();
                for(String col : columns) {
                    RDFNode x = soln.get(col);
                    if(x == null) continue;
                    Topic role = getRoleTopic(col, tm);
                    Topic player = null;
                    if(x.isURIResource() || x.isResource()) {
                        Resource r = x.asResource();
                        String uri = r.getURI();
                        String name = r.getLocalName();
                        player = createTopic(tm, uri, name);
                    }
                    else if(x.isLiteral()) {
                        Literal l = x.asLiteral();
                        String sif = l.getString();
                        if(sif.length() > 128) sif = sif.substring(0, 127)+"_"+sif.hashCode();
                        String uri = LITERAL_SI + "/" + encode(sif);
                        String name = l.getString();
                        String lang = l.getLanguage();
                        String datatype = l.getDatatypeURI();
                        player = createTopic(tm, uri, name);
                        player.setDisplayName(lang, name);
                    }
                    else {
                        log("Warning: Found illegal column value in SPARQL result set. Skipping.");
                    }

                    if(role != null && player != null) {
                        roledPlayers.put(role, player);
                    }
                }
                if(!roledPlayers.isEmpty()) {
                    Topic associationType = getAssociationType(rs, tm);
                    if(associationType != null) {
                        Association a = tm.createAssociation(associationType);
                        for( Topic role : roledPlayers.keySet() ) {
                            a.addPlayer(roledPlayers.get(role), role);
                        }
                    }
                }
            }
            counter++;
            setProgress(counter);
            if(counter % 100 == 0) hlog("Result set rows processed: " + counter);
        }
    }


    public Topic getAssociationType(ResultSet rs, TopicMap tm) throws Exception {
        Topic atype = createTopic(tm, RESULTSET_SI+"/"+rs.hashCode(), "SPARQL Result Set "+rs.hashCode());
        Topic settype = createTopic(tm, RESULTSET_SI, "SPARQL Result Set");
        Topic wandoratype = createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        settype.addType(wandoratype);
        atype.addType(settype);
        return atype;
    }


    public Topic getRoleTopic(String role, TopicMap tm) throws Exception {
        return createTopic(tm, COLUMN_SI+"/"+encode(role), role);
    }


    public String encode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_SI_ENCODING);
        }
        catch(Exception e) {
            return str;
        }
    }



    // -------------------------------------------------------------------------





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






    public void handleStatement(Statement stmt, TopicMap map) throws TopicMapException {
        Resource subject   = stmt.getSubject();     // get the subject
        Property predicate = stmt.getPredicate();   // get the predicate
        RDFNode object     = stmt.getObject();      // get the object

        //System.out.println("statement:\n  "+subject+"\n   "+predicate+"\n    "+object);

        Topic subjectTopic = getOrCreateTopic(map, subject.toString());

        if(object.isResource()) {
            Topic objectTopic = getOrCreateTopic(map, object.toString());
            Topic predicateTopic = getOrCreateTopic(map, predicate.toString());
            Association association = map.createAssociation(predicateTopic);
            association.addPlayer(subjectTopic, solveSubjectRoleFor(predicate, subject, map));
            association.addPlayer(objectTopic, solveObjectRoleFor(predicate, object, map));
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


    public Topic solveSubjectRoleFor(Property predicate, Resource subject, TopicMap map) {
        if(map == null) return null;
        RDF2TopicMapsMapping[] mappings = null; // getMappings();
        String predicateString = predicate.toString();
        String subjectString = subject.toString();
        String si = RDF2TopicMapsMapping.DEFAULT_SUBJECT_ROLE_SI;
        String bn = RDF2TopicMapsMapping.DEFAULT_SUBJECT_ROLE_BASENAME;
        T2<String, String> mapped = null;
        if(mappings != null) {
            for(int i=0; i<mappings.length; i++) {
                mapped = mappings[i].solveSubjectRoleFor(predicateString, subjectString);
                if(mapped.e1 != null && mapped.e2 != null) {
                    si = mapped.e1;
                    bn = mapped.e2;
                    break;
                }
            }
        }
        return getOrCreateTopic(map, si, bn);
    }





    public Topic solveObjectRoleFor(Property predicate, RDFNode object, TopicMap map) {
        if(map == null) return null;
        RDF2TopicMapsMapping[] mappings = null; // getMappings();
        String predicateString = predicate.toString();
        String objectString = object.toString();
        String si = RDF2TopicMapsMapping.DEFAULT_OBJECT_ROLE_SI;
        String bn = RDF2TopicMapsMapping.DEFAULT_OBJECT_ROLE_BASENAME;
        T2<String, String> mapped = null;
        if(mappings != null) {
            for(int i=0; i<mappings.length; i++) {
                mapped = mappings[i].solveObjectRoleFor(predicateString, objectString);
                if(mapped.e1 != null && mapped.e2 != null) {
                    si = mapped.e1;
                    bn = mapped.e2;
                    break;
                }
            }
        }
        return getOrCreateTopic(map, si, bn);
    }



    // -------------------------------------------------------------------------


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
