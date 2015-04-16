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




package org.wandora.application.tools.extractors.uclassify;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;


/**
 *
 * @author akivela
 */


public class UClassifier extends AbstractUClassifier {
    
    protected static final String OPTIONS_KEY = "uclassify";
    
    private static String[] defaultClassifiers = new String[] {
        "Text Language", "uClassify",
        "Sentiment", "uClassify",
        "Topics", "uClassify",
    };
    
    
    

    private UClassifierDialog uClassifierDialog = null;
    
    private String forceClassifier = null;
    private String forceOwner = null;
    private double forceProbability = -1.0;
    
    
    public UClassifier() {
        
    }
    
    
    public UClassifier(String classifier, String owner, double probability) {
        forceClassifier = classifier;
        forceOwner = owner;
        forceProbability = probability;
    }
    
    
    

    @Override
    public String getName() {
        return "UClassifier";
    }

    @Override
    public String getDescription(){
        return "Extracts classes out of given text using uClassifier. Read more at http://www.uclassify.com.";
    }

    
    // -------------------------------------------------------------------------
    
    

    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        readOptions(options);
        UClassifierConfiguration conf = new UClassifierConfiguration(wandora, options, this);
        conf.setClassifiers(defaultClassifiers);
        conf.open();
        if(conf.wasAccepted()) {
            defaultClassifiers = conf.getClassifiers();
            options.removeAll(OPTIONS_KEY+".uclassifiers");
            for(int i=0; i<defaultClassifiers.length; i=i+2) {
                options.put(OPTIONS_KEY+".uclassifiers.uclassifier["+i/2+"].name", defaultClassifiers[i]);
                options.put(OPTIONS_KEY+".uclassifiers.uclassifier["+i/2+"].author", defaultClassifiers[i+1]);
            }
        }
    }
    
    
    
    @Override
    public void writeOptions(Wandora wandora, org.wandora.utils.Options options, String prefix) {
        if(options != null) {
            options.removeAll(OPTIONS_KEY+".uclassifiers");
            for(int i=0; i<defaultClassifiers.length; i=i+2) {
                options.put(OPTIONS_KEY+".uclassifiers.uclassifier["+i/2+"].name", defaultClassifiers[i]);
                options.put(OPTIONS_KEY+".uclassifiers.uclassifier["+i/2+"].author", defaultClassifiers[i+1]);
            }
        }
    }
    
    
    private void readOptions(Options options) {
        if(options != null) {
            int i = 0;
            ArrayList<String> classifiers = new ArrayList<String>();
            try {
                while(true) {
                    String classifierName = options.get(OPTIONS_KEY+".uclassifiers.uclassifier["+i+"].name");
                    String classifierAuthor = options.get(OPTIONS_KEY+".uclassifiers.uclassifier["+i+"].author");
                    if(classifierName != null && classifierAuthor != null) {
                        classifiers.add(classifierName);
                        classifiers.add(classifierAuthor);
                    }
                    else break;
                    i++;
                }
                if(classifiers.isEmpty()) {
                    System.out.print("Warning: UClassifier didn't find any classifiers in Wandora options.");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            defaultClassifiers = classifiers.toArray( new String[] {} );
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        setWandora(wandora);
        readOptions(wandora.getOptions());
        Object contextSource = context.getContextSource();
        if(contextSource instanceof OccurrenceTextEditor) {
            try {
                OccurrenceTextEditor occurrenceEditor = (OccurrenceTextEditor) contextSource;
                if(context.getContextObjects().hasNext()) {
                    Topic masterTopic = (Topic) context.getContextObjects().next();
                    setMasterSubject(masterTopic);
                    String str = occurrenceEditor.getSelectedText();
                    if(str == null || str.length() == 0) {
                        str = occurrenceEditor.getText();
                    }
                    _extractTopicsFrom(str, wandora.getTopicMap());
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        else {
            try {
                TopicMap tm = wandora.getTopicMap();
                setTopicMap(tm);
                int possibleTypes = getExtractorType();
                uClassifierDialog = new UClassifierDialog(wandora, true);
                uClassifierDialog.initialize(this, defaultClassifiers);
                if((possibleTypes & RAW_EXTRACTOR) != 0) uClassifierDialog.registerRawSource();
                if((possibleTypes & FILE_EXTRACTOR) != 0) uClassifierDialog.registerFileSource();
                if((possibleTypes & URL_EXTRACTOR) != 0) uClassifierDialog.registerUrlSource();
                if((possibleTypes & CUSTOM_EXTRACTOR) != 0) initializeCustomType();
                uClassifierDialog.setVisible(true);
                if(!uClassifierDialog.wasAccepted()) return;

                int selectedType = uClassifierDialog.getSelectedSource();

                setDefaultLogger();
                log(getGUIText(INFO_WAIT_WHILE_WORKING));


                // --- FILE TYPE ---
                if((selectedType & FILE_EXTRACTOR) != 0) {
                    handleFiles( uClassifierDialog.getFileSources(), tm );
                }

                // --- URL TYPE ---
                if((selectedType & URL_EXTRACTOR) != 0) {
                    handleUrls( uClassifierDialog.getURLSources(), tm );
                }

                // --- RAW TYPE ---
                if((selectedType & RAW_EXTRACTOR) != 0) {
                    handleContent( uClassifierDialog.getContent(), tm );
                }

                // --- CUSTOM TYPE ---
                if((selectedType & CUSTOM_EXTRACTOR) != 0) {
                    handleCustomType();
                }

                lockLog(false);
                setState(WAIT);
            }
            catch(Exception e) {
                log(e);
                setState(WAIT);
            }
        }
        clearMasterSubject();
    }
    
    


    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String data = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(data, topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(String data, TopicMap tm) throws Exception {
        if(data != null && data.length() > 0) {
            String content = ExtractHelper.getTextData(data);
            
            String classifier = "Text Language";
            String classifierOwner = "uClassify";
            double thresholdProbability = 0.0001;
            
            if(forceClassifier != null) classifier = forceClassifier;
            if(forceOwner != null) classifierOwner = forceOwner;
            if(forceProbability != -1.0) thresholdProbability = forceProbability;
            
            if(uClassifierDialog != null) {
                thresholdProbability = uClassifierDialog.getThresholdValue();
                classifier = uClassifierDialog.getClassifier();
                classifierOwner = uClassifierDialog.getClassifierOwner();
            }

            String result = uClassify(content, classifier, classifierOwner, thresholdProbability, tm);
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }




    // -------------------------------------------------------------------------





}
