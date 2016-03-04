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
 * TwineExtractor.java
 *
 * Created on 7.2.2009,12:32
 */


package org.wandora.application.tools.extractors.foaf;



import java.net.*;
import java.io.*;
import java.util.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import java.awt.*;
import javax.swing.*;
import java.text.*;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.extractors.geonames.*;
import org.wandora.topicmap.*;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;

import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;

import com.hp.hpl.jena.rdf.model.*;



/**
 *
 * @author akivela
 */
public class FoafRDFExtractor extends AbstractExtractor {
    private String defaultEncoding = "UTF-8";
    public static String defaultLanguage = "en";
    
    
    
    
    
    public FoafRDFExtractor() {
        
    }
    

    @Override
    public String getName() {
        return "Foaf RDF extractor...";
    }
    
    @Override
    public String getDescription(){
        return "Read Foaf RDF feed and convert it to a topic map.";
    }
    

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_foaf.png");
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
        super.execute(wandora, context);
    }
    
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        URLConnection uc = url.openConnection();
        uc.setUseCaches(false);
        boolean r = _extractTopicsFrom(uc.getInputStream(), topicMap);
        return r;
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }

    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception { 
        try {
            importFoafRDF(in, topicMap);
            return true;
        }
        catch(Exception e) {
            log(e);
        }
        return false;
    }
    
    
    public boolean _extractTopicsFrom(String in, TopicMap tm) throws Exception {        
        try {           
            importFoafRDF(new StringBufferInputStream(in), tm);
        }
        catch(Exception e){
            log("Exception when handling request",e);
        }
        return true;
    }
    
    

    
    
    
    public void importFoafRDF(InputStream in, TopicMap map) {
        if(in != null) {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
            // read the RDF/XML file
            model.read(in, "");
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
            if(counter % 100 == 0) hlog("Foaf RDF statements processed: " + counter);

        }
        log("Total Foaf RDF statements processed: " + counter);
    }

    
    
    public String occurrenceScopeSI = TMBox.LANGINDEPENDENT_SI;

   
    
    public void handleStatement(Statement stmt, TopicMap map) throws TopicMapException {
        Resource subject   = stmt.getSubject();     // get the subject
        Property predicate = stmt.getPredicate();   // get the predicate
        RDFNode object     = stmt.getObject();      // get the object

        System.out.println("statement:\n  "+subject+"\n   "+predicate+"\n    "+object);
        
        Topic subjectTopic = getOrCreateTopic(map, subject.toString());
        Topic predicateTopic = getOrCreateTopic(map, predicate.toString());

        if(object.isResource()) {
            Topic objectTopic = getOrCreateTopic(map, object.toString());
            Association association = map.createAssociation(predicateTopic);
            association.addPlayer(subjectTopic, solveSubjectRoleFor(predicate, subject, map));
            association.addPlayer(objectTopic, solveObjectRoleFor(predicate, object, map));
        }
        else if(object.isLiteral()) {
            String occurrenceLang = occurrenceScopeSI;
            String literal = ((Literal) object).getString();
            try { 
                String lang = stmt.getLanguage();
                if(lang != null) occurrenceLang = XTMPSI.getLang(lang);
            }
            catch(Exception e) {  
                /* PASSING BY */
            }
            String oldOccurrence = subjectTopic.getData(predicateTopic, occurrenceLang);
            if(oldOccurrence != null && oldOccurrence.length() > 0) {
                literal = oldOccurrence + "\n\n" + literal;
            }
            subjectTopic.setData(predicateTopic, getOrCreateTopic(map, occurrenceLang), literal);
        }
        else if(object.isURIResource()) {
            log("URIResource found but not handled!");
        }        
    }
    
    
    

    
    
    String[] roles = new String[] {
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                "http://wandora.org/si/core/rdf-type-carrier", "rdf-type-carrier",
                "http://wandora.org/si/core/rdf-type", "rdf-type",
                
        "http://www.w3.org/2000/01/rdf-schema#seeAlso",
                "http://wandora.org/si/core/subject", "subject",
                "http://wandora.org/si/core/rdf-schema/see-also", "rdf-see-also",
                
        "http://xmlns.com/foaf/0.1/nick",
                "http://wandora.org/si/foaf/person", "person",
                "http://wandora.org/si/foaf/nick", "nick",
                
        "http://xmlns.com/foaf/0.1/weblog",
                "http://wandora.org/si/foaf/person", "person",
                "http://wandora.org/si/foaf/weblog", "weblog",
                
        "http://xmlns.com/foaf/0.1/homepage",
                "http://wandora.org/si/foaf/person", "person",
                "http://wandora.org/si/foaf/homepage", "homepage",

        "http://xmlns.com/foaf/0.1/member",
                "http://wandora.org/si/foaf/person", "person",
                "http://wandora.org/si/foaf/group", "group",
        "http://xmlns.com/foaf/0.1/knows",
                "http://wandora.org/si/foaf/person", "person",
                "http://wandora.org/si/foaf/known-person", "known-person",
    };
    
    
 
    
    public Topic solveSubjectRoleFor(Property predicate, Resource subject, TopicMap map) {
        if(map == null) return null;
        String predicateString = predicate.toString();
        String si = "http://wandora.org/si/core/rdf-subject";
        String bn = "subject-role";
        for(int i=0; i<roles.length; i=i+5) {
            if(predicateString.equals(roles[i])) {
                si = roles[i+1];
                bn = roles[i+2];
                break;
            }
        }
        return getOrCreateTopic(map, si, bn);
    }
    
    public Topic solveObjectRoleFor(Property predicate, RDFNode object, TopicMap map) {
        if(map == null) return null;
        String predicateString = predicate.toString();
        String si = "http://wandora.org/si/core/rdf-object";
        String bn = "object-role";
        for(int i=0; i<roles.length; i=i+5) {
            if(predicateString.equals(roles[i])) {
                si = roles[i+3];
                bn = roles[i+4];
                break;
            }
        }
        return getOrCreateTopic(map, si, bn);
    }
    
    
    
    String[] basenames = new String[] {
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "rdf-type",
        "http://www.w3.org/2000/01/rdf-schema#label",
            "label",
        "http://www.radarnetworks.com/core#contains",
            "contains",
        "http://www.radarnetworks.com/shazam#indirectlyContains",
            "indirectly-contains",

        "http://xmlns.com/foaf/0.1/person",
            "person",
        "http://xmlns.com/foaf/0.1/Organization",
            "organization",
        "http://xmlns.com/foaf/0.1/Group",
            "group",
        "http://xmlns.com/foaf/0.1/knows",
            "knows",
        "http://xmlns.com/foaf/0.1/membershipClass",
            "membership",
        "http://xmlns.com/foaf/0.1/member",
            "member",
        "http://xmlns.com/foaf/0.1/homepage",
            "homepage",
        "http://xmlns.com/foaf/0.1/weblog",
            "weblog",
        "http://xmlns.com/foaf/0.1/nick",
            "nickname",
        "http://xmlns.com/foaf/0.1/givenname",
            "given name",
        "http://xmlns.com/foaf/0.1/name",
            "name",
        "http://xmlns.com/foaf/0.1/firstName",
            "first name",
        "http://xmlns.com/foaf/0.1/surname",
            "surname",
        "http://xmlns.com/foaf/0.1/family_name",
            "family name",
        "http://xmlns.com/foaf/0.1/gender",
            "gender",
        "http://xmlns.com/foaf/0.1/geekcode",
            "geekcode",
        "http://xmlns.com/foaf/0.1/msnChatID",
            "msnChatID",
        "http://xmlns.com/foaf/0.1/myersBriggs",
            "Myers Briggs (MBTI) personality classification",
        "http://xmlns.com/foaf/0.1/schoolHomepage",
            "schoolHomepage",
        "http://xmlns.com/foaf/0.1/publications",
            "publications",
        "http://xmlns.com/foaf/0.1/plan",
            "plan-comment",
        "http://xmlns.com/foaf/0.1/phone",
            "phone",
        "http://xmlns.com/foaf/0.1/homepage",
            "homepage",
        "http://xmlns.com/foaf/0.1/holdsAccount",
            "holds account",
        "http://xmlns.com/foaf/0.1/pastProject",
            "past project",
        "http://xmlns.com/foaf/0.1/currentProject",
            "current project",
        "http://xmlns.com/foaf/0.1/Project",
            "project",
        "http://xmlns.com/foaf/0.1/page",
            "page",
        "http://xmlns.com/foaf/0.1/birthday",
            "birthday",
        "http://xmlns.com/foaf/0.1/openid",
            "openid",
        "http://xmlns.com/foaf/0.1/jabberID",
            "Jabber ID",
        "http://xmlns.com/foaf/0.1/aimChatID",
            "AIM chat ID",
        "http://xmlns.com/foaf/0.1/icqChatID",
            "ICQ chat ID",
        "http://xmlns.com/foaf/0.1/dnaChecksum",
            "DNA checksum",
        "http://xmlns.com/foaf/0.1/mbox_sha1sum",
            "personal mailbox URI",
        "http://xmlns.com/foaf/0.1/mbox",
            "a personal mailbox",
        "http://xmlns.com/foaf/0.1/PersonalProfileDocument",
            "personal profile document",
            
        "http://xmlns.com/foaf/0.1/maker",
            "maker",
        "http://xmlns.com/foaf/0.1/made",
            "made",
        "http://xmlns.com/foaf/0.1/logo",
            "logo",
        "http://xmlns.com/foaf/0.1/interest",
            "interest",
        "http://xmlns.com/foaf/0.1/img",
            "image",
        "http://xmlns.com/foaf/0.1/Image",
            "image (2)",
        "http://xmlns.com/foaf/0.1/fundedBy",
            "funded by",
        "http://xmlns.com/foaf/0.1/depicts",
            "depicts",
        "http://xmlns.com/foaf/0.1/depiction",
            "depiction",
        "http://xmlns.com/foaf/0.1/based_near",
            "based near",
        "http://xmlns.com/foaf/0.1/accountServiceHomepage",
            "account service homepage",
        "http://xmlns.com/foaf/0.1/accountName",
            "account name",
        "http://xmlns.com/foaf/0.1/OnlineGamingAccount",
            "online gaming account",
        "http://xmlns.com/foaf/0.1/OnlineEcommerceAccount",
            "online e-commerce account",
        "http://xmlns.com/foaf/0.1/OnlineChatAccount",
            "online chat account",
        "http://xmlns.com/foaf/0.1/OnlineAccount",
            "online account",
        "http://xmlns.com/foaf/0.1/Document",
            "document",
        "http://xmlns.com/foaf/0.1/Agent",
            "agent",
    };
    
    
    
    public String solveBasenameFor(String si) {
        if(si == null) return null;
        String bn = null;
        for(int i=0; i<basenames.length; i=i+2) {
            if(si.equals(basenames[i])) {
                bn = basenames[i+1];
                break;
            }
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
            topic = map.getTopic(si);
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
