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
 */


package org.wandora.application.tools.extractors.stanfordner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.IObox;
import java.util.List;

import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import java.net.URLEncoder;
import org.wandora.application.Wandora;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;
import org.wandora.utils.Textbox;
import org.wandora.utils.XMLbox;

/**
 *
 * @author akivela
 */
public class StanfordNERClassifier extends AbstractExtractor {
    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String ENTITY_SI = "http://wandora.org/si/stanford-ner/entity";
    public static final String ENTITY_TYPE_SI = "http://wandora.org/si/stanford-ner/type";
    public static final String STANFORD_NER_SI = "http://nlp.stanford.edu/software/CRF-NER.shtml";

    public static final String classifierPath = "lib/stanford-ner/classifiers/";
    public String defaultClassifier = classifierPath + "ner-eng-ie.crf-3-all2008.ser.gz";
    private String selectedClassifier = defaultClassifier;

    private String defaultEncoding = "UTF-8";

    public String optionsPath = "stanfordNERclassifier";




    @Override
    public String getName() {
        return "Stanford Named Entity Recognizer";
    }



    @Override
    public String getDescription(){
        return "Extracts topics out of given text using Stanford Named Entity Recognizer (NER). Read more at http://nlp.stanford.edu/software/CRF-NER.shtml";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_stanford_ner.png");
    }

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }

    private final String[] contentTypes=new String[] { "text/plain", "text/html" };

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
    public void configure(Wandora admin, Options options, String prefix) throws TopicMapException {
        StanfordNERConfiguration dialog = new StanfordNERConfiguration(admin, options, this);
        dialog.setVisible(true);
        if(dialog.wasAccepted()) {
            String classifier = dialog.getSuggestedClassifier();
            if(classifier != null && classifier.trim().length() != 0) {
                options.put(optionsPath, classifier);
                selectedClassifier = classifier;
            }
        }
    }
    @Override
    public void writeOptions(Wandora admin, Options options, String prefix) {
    }


    // -------------------------------------------------------------------------


    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }


    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String data = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(data, topicMap);
    }


    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        if(data != null && data.length() > 0) {
            data = XMLbox.naiveGetAsText(data); // Strip HTML

            int entityCounter = 0;

            String serializedClassifier = selectedClassifier;

            AbstractSequenceClassifier classifier = null;
            
            try {
                classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
            }
            catch(Exception e) {
                log(e);
            }
            catch(Error er) {
                log(er);
            }

            if(classifier != null) {
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
                if(masterTopic == null && data != null && data.length() > 0) {
                    try {
                        masterTopic = topicMap.createTopic();
                        masterTopic.addSubjectIdentifier(topicMap.makeSubjectIndicatorAsLocator());
                        fillDocumentTopic(masterTopic, topicMap, data);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }

                List<List<CoreLabel>> out = classifier.classify(data);
                for(List<CoreLabel> sentence : out) {
                    if(forceStop()) break;
                    for (CoreLabel word : sentence) {
                        if(forceStop()) break;
                        Object an = word.get(AnswerAnnotation.class);
                        if(!"O".equals(an.toString())) {
                            try {
                                processNER(word.word(), an.toString(), masterTopic, topicMap);
                                log("Recognized entity type '"+an+"' for entity '"+word.word()+"'.");
                            }
                            catch(Exception e) {
                                log(e);
                            }
                            entityCounter++;
                            setProgress(entityCounter);
                        }
                    }
                }
                /*
                out = classifier.classifyFile(args[1]);
                for (List<CoreLabel> sentence : out) {
                    for (CoreLabel word : sentence) {
                        System.out.print(word.word() + '/' + word.get(AnswerAnnotation.class) + ' ');
                    }
                    System.out.println();
                }
                System.out.println(classifier.classifyToString(s1));
                System.out.println(classifier.classifyWithInlineXML(s2));
                System.out.println(classifier.classifyToString(s2, "xml", true));
                */
                log("Total " + entityCounter + " entities found by Stanford Named Entity Recognizer.");
            }
            else {
                log("Invalid classifier! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }


    public void processNER(String word, String type, Topic masterTopic, TopicMap tm) throws TopicMapException {
        Topic entityTopic = getEntityTopic(word, type, tm);
        if(entityTopic != null && masterTopic != null) {
            Topic entityType = getEntityTypeType(tm);
            Association a = tm.createAssociation(entityType);
            a.addPlayer(masterTopic, getTopicType(tm));
            a.addPlayer(entityTopic, entityType);
        }
    }




    // -------------------------------------------------------------------------







    public Topic getEntityTypeType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, ENTITY_TYPE_SI, "Stanford NER entity");
        t.addType(getStanfordNERClass(tm));
        return t;
    }



    public Topic getEntityType(String type, TopicMap tm) throws TopicMapException {
        String encodedType = type;
        try { encodedType = URLEncoder.encode(type, "utf-8"); }
        catch(Exception e) {}
        Topic t = getOrCreateTopic(tm, ENTITY_TYPE_SI+"/"+encodedType, type);
        t.addType(getStanfordNERClass(tm));
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
                    Topic entityTypeTopic = getEntityType(type, tm);
                    entityTopic.addType(entityTypeTopic);
                    entityTopic.addType(getEntityTypeType(tm));
                }
                return entityTopic;
            }
        }
        return null;
    }

    public Topic getStanfordNERClass(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, STANFORD_NER_SI, "Stanford NER");
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
