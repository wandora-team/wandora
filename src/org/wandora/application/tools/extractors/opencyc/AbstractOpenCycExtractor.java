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
 * AbstractOpenCycExtractor.java
 * 
 */



package org.wandora.application.tools.extractors.opencyc;


import java.net.*;
import java.io.*;
import org.wandora.topicmap.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;
import javax.swing.*;



/**
 *
 * @author akivela
 */
public abstract class AbstractOpenCycExtractor extends AbstractExtractor {
    protected static boolean ISA_EQUALS_INSTANCE = false;
    
    
    public static String termSIBase = "http://www.opencyc.org/";
    
    
    public static String SIPREFIX="http://wandora.org/si/opencyc/";

    public static String COMMENT_SI = SIPREFIX+"comment";
    public static String SIBLING_SI = SIPREFIX+"sibling";
    public static String FUNCTOR_SI = SIPREFIX+"functor";
    public static String ARG_SI = SIPREFIX+"arg";
    public static String ISA_SI = SIPREFIX+"isa";
    public static String GENLS_SI = SIPREFIX+"genls";
    public static String TERM_SI = SIPREFIX+"term";
    
    public static String COLLECTION_SI = SIPREFIX+"collection";
    public static String INSTANCE_SI = SIPREFIX+"instance";
    
    
    
    // Default language of occurrences and variant names.
    public static String LANG = "en";
    
    
    /** 
     * Try to retrieve topic before new to creation. Setting this true may speed
     * the extraction but extraction may loose some data as topic is created only once.
     */
    public static boolean USE_EXISTING_TOPICS = false;
    
    
    
    

    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_opencyc.png");
    }

    
    
    private final String[] contentTypes=new String[] { "text/xml", "application/xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        setMasterSubject( getMasterTerm(url.toExternalForm()) );
        return _extractTopicsFrom(url.openStream(),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        setMasterSubject( getMasterTerm(file.toURI().toURL().toExternalForm()) );
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }
    

    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        setMasterSubject( "http://wandora.org/si/opencyc/" );
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }




    public abstract boolean _extractTopicsFrom(InputStream inputStream, TopicMap topicMap) throws Exception;
    
    
    
    
    
    public abstract String getMasterTerm(String u);

    
    
    
    // -------------------------------------------------------------------------
    
    

    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }



    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    
    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }
    
    protected static Topic getTermTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TERM_SI, "OpenCyc term");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    protected static Topic getCommentTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, COMMENT_SI, "OpenCyc comment");
        return type;
    }
    
    protected static Topic getSiblingTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SIBLING_SI, "OpenCyc sibling");
        return type;
    } 
    
    protected static Topic getIsaTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ISA_SI, "OpenCyc isa");
        return type;
    } 
    
    protected static Topic getFunctorType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, FUNCTOR_SI, "OpenCyc functor");
        return type;
    }
    
    protected static Topic getArgType(TopicMap tm, int argNum) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ARG_SI+argNum, "OpenCyc arg"+argNum);
        return type;
    }
    
    protected static Topic getCollectionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, COLLECTION_SI, "OpenCyc collection");
        return type;
    }
    protected static Topic getInstanceTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, INSTANCE_SI, "OpenCyc instance");
        return type;
    }
    
    protected static Topic getTermTopic(String guid, String basename, TopicMap tm) throws TopicMapException {
        String si = termSIBase+guid;
        Topic t = null;
        if(basename != null&& basename.length() > 0) {
            t = tm.getTopicWithBaseName(basename);
        }
        if(t == null && si != null && si.length() > 0) {
            t = tm.getTopic(si);
        }
        if(t == null) {
            t = tm.createTopic();
            if(si != null && si.length() > 0) t.addSubjectIdentifier(new org.wandora.topicmap.Locator( si ) );
            else t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
            if(basename != null && basename.length() > 0) t.setBaseName(basename);
        }
        Topic termType = getTermTypeTopic(tm);
        t.addType(termType);
        return t;
    }
    
}
