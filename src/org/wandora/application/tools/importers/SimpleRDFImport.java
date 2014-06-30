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
 * SimpleRDFLocalImport.java
 *
 * Created on 24. toukokuuta 2006, 20:38
 */

package org.wandora.application.tools.importers;


import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.io.*;
import javax.swing.*;

import com.hp.hpl.jena.rdf.model.*;



/**
 * <p>
 * SimpleRDFImport is used to import and convert RDF files (including RDFS and
 * OWL files) to topic maps.
 * </p>
 * 
 * @author akivela
 */
public class SimpleRDFImport extends AbstractImportTool implements WandoraTool {


    public static final String anonSIPrefix="http://wandora.org/si/rdf/anon/";

    /**
     * Creates a new instance of SimpleRDFLocalImport
     */
    public SimpleRDFImport() {
    }
    public SimpleRDFImport(int options) {
        setOptions(options);
    }
    
    @Override
    public void initialize(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        String o=options.get(prefix+"options");
        if(o!=null){
            int i=Integer.parseInt(o);
            setOptions(i);
        }
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        System.out.println(prefix);
        ImportConfiguration dialog=new ImportConfiguration(admin,true);
        dialog.setOptions(getOptions());
        dialog.setVisible(true);
        if(!dialog.wasCancelled()){
            int i=dialog.getOptions();
            setOptions(i);
            options.put(prefix+"options",""+i);
        }
    }
    
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
        options.put(prefix+"options",""+getOptions());
    }    
    
    @Override
    public String getName() {
        return "Simple RDF XML import";
    }
    
    @Override
    public String getDescription() {
        return "Tool imports RDF XML file and merges RDF triplets to current topic map. \n"+
               "Used schema is very simple: Resources are mapped to topics, literals are \n"+
               "mapped to occurrences and triplets to binary associations.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_rdf.png");
    }
    

    
    @Override
    public void importStream(Wandora admin, String streamName, InputStream inputStream) {
        try {
            try {
                admin.getTopicMap().clearTopicMapIndexes();
            }
            catch(Exception e) {
                log(e);
            }
            
            TopicMap map = null;
            if(directMerge) {
                log("Merging '" + streamName + "' to context layer while reading.");
                map = solveContextTopicMap(admin, getContext());
            }
            else {
                map = new org.wandora.topicmap.memory.TopicMapImpl();
            }
            
            importRDF(inputStream, map);
            
            if(!directMerge) {
                if(newLayer) {
                    LayerStack layerStack = (LayerStack) admin.getTopicMap();
                    String layerName = streamName;
                    int c = 2;
                    if(layerStack.getLayer(layerName) != null) {
                        do {
                            layerName = streamName + " " + c;
                            c++;
                        }
                        while(layerStack.getLayer(layerName) != null);
                    }
                    log("Creating new layer for '" + layerName + "'.");
                    layerStack.addLayer(new Layer(map,layerName,layerStack));
                    admin.layerTree.resetLayers();
                }
                else {
                    log("Merging '" + streamName + "'.");
                    solveContextTopicMap(admin, getContext()).mergeIn(map);
                }
            }
        }
        catch(Exception e) {
            //info.dialog.setVisible(false);
            log("Reading '" + streamName + "' failed!", e);
        }
    }
    
    
    
    
    public void importRDF(InputStream in, TopicMap map) {
        if(in != null) {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
            // read the RDF/XML file
            model.read(in, "");
            RDF2TopicMap(model, map);
        }
    }
    
    

    
    
    
    public static final String occurrenceScopeSI = TMBox.LANGINDEPENDENT_SI;
    public static final String subjectTypeSI = "http://wandora.org/si/core/rdf-subject";
    public static final String objectTypeSI = "http://wandora.org/si/core/rdf-object";
    public static final String predicateTypeSI = "http://wandora.org/si/core/rdf-predicate";
    
    
    public void handleStatement(Statement stmt,TopicMap map,Topic subjectType,Topic predicateType,Topic objectType) throws TopicMapException {
        Resource subject   = stmt.getSubject();     // get the subject
        Property predicate = stmt.getPredicate();   // get the predicate
        RDFNode object    = stmt.getObject();      // get the object
        String lan          = null;
       
        Topic subjectTopic = getOrCreateTopic(map, subject);
        Topic predicateTopic = getOrCreateTopic(map, predicate);

        subjectTopic.addType(subjectType);
        predicateTopic.addType(predicateType);

        if(object.isLiteral()) {
            try { lan = stmt.getLanguage(); } catch(Exception e) { /*NOTHING!*/ }
            if(lan==null || lan.length()==0) {
                subjectTopic.setData(predicateTopic, getOrCreateTopic(map, occurrenceScopeSI), ((Literal) object).getString());
            }
            else {
                subjectTopic.setData(predicateTopic, getOrCreateTopic(map, XTMPSI.getLang(lan)), ((Literal) object).getString());
            }
        }
        else if(object.isResource()) {
            Topic objectTopic = getOrCreateTopic(map, (Resource)object);

            Association association = map.createAssociation(predicateTopic);
            association.addPlayer(subjectTopic, subjectType);
            association.addPlayer(objectTopic, objectType);

            objectTopic.addType(objectType);
        }
        else if(object.isURIResource()) {
            log("URIResource found but not handled!");
        }        
    }
    
    
    public void RDF2TopicMap(Model model, TopicMap map) {
        // list the statements in the Model
        StmtIterator iter = model.listStatements();
        Topic subjectTopic = null;
        Topic predicateTopic = null;
        Topic objectTopic = null;
        Association association = null;
        Statement stmt = null;
        Resource subject = null;
        Property predicate = null;
        RDFNode object = null;
        int counter = 0;
        
        Topic subjectType = getOrCreateTopic(map, subjectTypeSI);
        Topic predicateType = getOrCreateTopic(map, predicateTypeSI);
        Topic objectType = getOrCreateTopic(map, objectTypeSI);
        
        // print out the predicate, subject and object of each statement
        while (iter.hasNext() && !forceStop()) {
            try {
                stmt      = iter.nextStatement();  // get next statement
                
                handleStatement(stmt,map,subjectType,predicateType,objectType);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            counter++;
            setProgress((counter/1000) % 100);
            if(counter % 1000 == 0) hlog("RDF statements processed: " + counter);
            
            
            /*
            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
               System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
            System.out.println(" .");
             **/
        }
        log("Total RDF statements processed: " + counter);
    }

    
    
    // -------------------------------------------------------------------------
    
    public Topic getOrCreateTopic(TopicMap map, Resource res) {
        String uri=res.getURI();
        if(uri==null) {
            try{
                AnonId id=res.getId();
                uri=anonSIPrefix+TopicTools.cleanDirtyLocator(id.toString());
            }catch(Exception e){}
        }
        if(uri==null) {
            log("Warning, can't resolve uri for resource "+res.toString()+". Making random uri.");
            uri=map.makeSubjectIndicator();
        }
        return getOrCreateTopic(map,uri);
    }
    public Topic getOrCreateTopic(TopicMap map, String si) {
        Topic topic = null;
        try {
            topic = map.getTopic(si);
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return topic;
    }
    
    
    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case FILE_DIALOG_TITLE_TEXT: {
                return "Select RDF(S) or OWL file to import";
            }
            case URL_DIALOG_MESSAGE_TEXT: {
                return "Type internet address of a RDF(S) or OWL document to be imported";
            }
        }
        return "";
    }

}
