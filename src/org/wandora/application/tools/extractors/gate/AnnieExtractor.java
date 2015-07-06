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
 */


package org.wandora.application.tools.extractors.gate;


import java.util.*;
import java.io.*;
import java.net.*;

import gate.*;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.creole.*;
import gate.util.*;
import gate.util.persistence.PersistenceManager;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;
import org.wandora.utils.Options;
import org.wandora.utils.Textbox;
import org.wandora.utils.XMLbox;


/**
 * See: http://gate.ac.uk/wiki/code-repository/src/sheffield/examples/StandAloneAnnie.java
 *
 * @author akivela
 */
public class AnnieExtractor extends AbstractGate {



    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String ENTITY_SI = "http://wandora.org/si/gate/entity";
    public static final String ENTITY_TYPE_SI = "http://wandora.org/si/gate/entity-type";
    public static final String GATE_ANNIE_SI = "http://gate.ac.uk";


    // ----- Configuration -----
    private AnnieConfiguration configuration = null;
    private static CorpusController annieController = null;



    @Override
    public String getName() {
        return "Gate Annie extractor";
    }

    @Override
    public String getDescription(){
        return "Look for entities out of given text with Gate Annie. See http://gate.ac.uk";
    }
    


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_gate.png");
    }

    private static final String[] contentTypes=new String[] { "text/plain", "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }


// -------------------------------------------------------------------------



    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora wandora, Options options, String prefix) throws TopicMapException {
        if(configuration == null && wandora != null) {
            configuration = new AnnieConfiguration(wandora, this);
        }
        configuration.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin, Options options, String prefix) {
    }


    // -------------------------------------------------------------------------


    @Override
    public void execute(Wandora wandora, Context context) {
        if(configuration == null && wandora != null) {
            configuration = new AnnieConfiguration(wandora, this);
        }
        super.execute(wandora, context);
    }



    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        URLConnection uc = url.openConnection();
        uc.setUseCaches(false);
        boolean r = _extractTopicsFrom(uc.getInputStream(), topicMap);
        return r;
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        try {
            String inStr = IObox.loadFile(in, "utf-8");
            doAnnie(inStr, topicMap);
            return true;
        }
        catch(Exception e) {
            log(e);
        }
        return false;
    }


    @Override
    public boolean _extractTopicsFrom(String in, TopicMap tm) throws Exception {
        try {
            doAnnie(in, tm);
        }
        catch(Exception e){
            log("Exception when handling request",e);
        }
        return true;
    }



    public void initializeAnnie() throws GateException, MalformedURLException, IOException {
        Gate.setGateHome(new File(GATE_HOME));
        Gate.setPluginsHome(new File(GATE_PLUGIN_HOME));
        Gate.setSiteConfigFile(new File(CONFIG_FILE));

        Gate.init();

        log("GATE initialised");
        
        // Load ANNIE plugin
        File pluginsHome = Gate.getPluginsHome();
        File anniePlugin = new File(pluginsHome, "ANNIE");
        File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
        annieController = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
        log("ANNIE loaded successfully");
    }






    public void doAnnie(String in, TopicMap topicMap) throws GateException, MalformedURLException, IOException, TopicMapException {
        in = XMLbox.naiveGetAsText(in); // Strip HTML
        String originalContent = in;
        Topic masterTopic = null;
        String masterSubject = getMasterSubject();
        if(masterSubject != null) {
            try {
                masterTopic = topicMap.getTopicWithBaseName(masterSubject);
                if(masterTopic == null) masterTopic = topicMap.getTopic(masterSubject);
            }
            catch(Exception e) {
                log(e);
            }
        }
        if(masterTopic == null && in != null && in.length() > 0) {
            try {
                masterTopic = topicMap.createTopic();
                masterTopic.addSubjectIdentifier(topicMap.makeSubjectIndicatorAsLocator());
                fillDocumentTopic(masterTopic, topicMap, in);
            }
            catch(Exception e) {
                log(e);
            }
        }

        if(annieController == null) {
            initializeAnnie();
        }
        else {
            log("GATE and Annie already initialized. Reusing old installations.");
        }

        // -------------------------------------------------
        // create a GATE corpus and add a document 
        Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");

        FeatureMap params = Factory.newFeatureMap();
        params.put("preserveOriginalContent", true);
        params.put("collectRepositioningInfo", true);
        DocumentImpl doc = (DocumentImpl) Factory.createResource("gate.corpora.DocumentImpl", params);
        DocumentContent content = new DocumentContentImpl(in);
        doc.setContent(content);
        corpus.add(doc);

        // -------------------------------------------------
        // tell the pipeline about the corpus and run it
        annieController.setCorpus(corpus);
        log("Running ANNIE...");
        annieController.execute();
        log("ANNIE completed. Processing results next...");


        // -------------------------------------------------
        // for each document, get the annotations
        Iterator iter = corpus.iterator();
        int count = 0;

        while(iter.hasNext() && !forceStop()) {
            Document docu = (Document) iter.next();
            AnnotationSet defaultAnnotSet = docu.getAnnotations();
            Set<String> annotTypes = defaultAnnotSet.getAllTypes();

            for(String annotType : annotTypes) {
                if(forceStop()) break;
                if(acceptAnnotationType(annotType)) {
                    Set<Annotation> annotations = new HashSet<Annotation>(defaultAnnotSet.get(annotType));
                    ++count;

                    setProgressMax(annotations.size());
                    int c = 0;
                    log("Extracting "+annotations.size()+" entities of type '"+annotType+"'...");

                    Iterator it = annotations.iterator();
                    Annotation annotation;
                    long positionEnd;
                    long positionStart;

                    while(it.hasNext() && !forceStop()) {
                        setProgress(c++);
                        annotation = (Annotation) it.next();
                        positionStart = annotation.getStartNode().getOffset();
                        positionEnd = annotation.getEndNode().getOffset();
                        if(positionEnd != -1 && positionStart != -1) {
                            String token = originalContent.substring((int) positionStart, (int) positionEnd);
                            // log("Found token '"+token+"' of type "+annotation.getType());
                            doAnnieAnnotation(token, annotType, masterTopic, topicMap);
                        }
                    }
                }
            }
        }
    }



    public void doAnnieAnnotation(String word, String annotationType, Topic masterTopic, TopicMap tm) throws TopicMapException {
        Topic entityTopic = getEntityTopic(word, annotationType, tm);
        Topic entityType = getEntityTypeTopic(annotationType, tm);
        if(entityTopic != null && masterTopic != null) {
            Topic entityMetaType = getEntityMetaType(tm);
            Association a = tm.createAssociation(entityMetaType);
            a.addPlayer(masterTopic, getTopicType(tm));
            a.addPlayer(entityTopic, getEntityType(tm));
            if(entityType != null) {
                a.addPlayer(entityType, entityMetaType);
            }
        }
    }



    public boolean acceptAnnotationType(String annotationType) {
        try {
            if(configuration == null) {
                configuration = new AnnieConfiguration(getWandora(), this);
            }
        }
        catch(Exception e) { /* DONT HANDLE */ }
        if(configuration != null) {
            if(configuration.acceptAllAnnotationTypes()) {
                return true;
            }
            if(configuration.acceptAnnotationType(annotationType))
                return true;
            else
                return false;
        }
        return true;
    }



    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    public Topic getEntityMetaType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, ENTITY_TYPE_SI, "GATE Annie entity type");
        t.addType(getGATEAnnieClass(tm));
        return t;
    }

    public Topic getEntityTypeTopic(String type, TopicMap tm) throws TopicMapException {
        String encodedType = type;
        try { encodedType = URLEncoder.encode(type, "utf-8"); }
        catch(Exception e) {}
        Topic t = getOrCreateTopic(tm, ENTITY_TYPE_SI+"/"+encodedType, type);
        t.addType(getEntityMetaType(tm));
        return t;
    }



    public Topic getEntityType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, ENTITY_SI, "GATE Annie entity");
        t.addType(getGATEAnnieClass(tm));
        return t;
    }


    public Topic getEntityTopic(String entity, String type, TopicMap tm) throws TopicMapException {
        if(entity != null) {
            entity = entity.trim();
            if(entity.length() > 0) {
                String encodedEntity = entity;
                try { encodedEntity = URLEncoder.encode(entity, "utf-8"); }
                catch(Exception e) {}
                Topic entityTopic=getOrCreateTopic(tm, ENTITY_SI+"/"+encodedEntity, entity);
                if(type != null && type.length() > 0) {
                    Topic entityTypeTopic = getEntityTypeTopic(type, tm);
                    entityTopic.addType(entityTypeTopic);
                    entityTopic.addType(getEntityType(tm));
                }
                return entityTopic;
            }
        }
        return null;
    }

    public Topic getGATEAnnieClass(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, GATE_ANNIE_SI, "GATE Annie");
        makeSubclassOf(tm, t, getWandoraClass(tm));
        return t;
    }

    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    public Topic getTopicType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TOPIC_SI, "Topic");
    }

    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }

    public Topic getDocumentType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, DOCUMENT_SI, "Document");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }



    // -------------------------------------------------------------------------






    public String solveTitle(String content) {
        if(content == null || content.length() == 0) return "empty-document";

        boolean forceTrim = false;
        String title = null;
        int i = content.indexOf("\n");
        if(i > 0) title = content.substring(0, i);
        else {
            title = content.substring(0, Math.min(80, content.length()));
            forceTrim = true;
        }

        if(title != null && (forceTrim || title.length() > 80)) {
            title = title.substring(0, Math.min(80, title.length()));
            while(!title.endsWith(" ") && title.length()>10) {
                title = title.substring(0, title.length()-1);
            }
            title = Textbox.trimExtraSpaces(title) + "...";
        }
        return title;
    }




    public void fillDocumentTopic(Topic textTopic, TopicMap topicMap, String content) {
        try {
            String trimmedText = Textbox.trimExtraSpaces(content);
            if(trimmedText != null && trimmedText.length() > 0) {
                Topic contentType = createTopic(topicMap, "document-text");
                setData(textTopic, contentType, "en", trimmedText);
            }
            String title = solveTitle(trimmedText);
            if(title != null) {
                textTopic.setBaseName(title + " (" + content.hashCode() + ")");
                textTopic.setDisplayName("en", title);
            }
            Topic documentType = getDocumentType(topicMap);
            textTopic.addType(documentType);
        }
        catch(Exception e) {
            log(e);
        }
    }




    // -------------------------------------------------------------------------


    @Override
    public void log(String msg) {
        if(getCurrentLogger() != null) super.log(msg);
    }


}
