/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * BasicRDFImport.java
 *
 * Created on 10.7.2006, 11:39
 *
 */

package org.wandora.application.tools.importers;


import org.wandora.topicmap.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import java.util.List;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFMapping;



/**
 *
 * @author olli
 */
public class BasicRDFImport extends SimpleRDFImport {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of BasicRDFImport */
    public BasicRDFImport() {
    }
    public BasicRDFImport(int options){
        super(options);
    }
    @Override
    public String getName() {
        return "Basic RDF import";
    }
    @Override
    public String getDescription() {
        return "Tool imports RDF XML file and merges RDF triplets to current topic map. \n"+
               "Used schema is slightly more advanced than Simple RDF XML import. Topic \n"+
               "base names are set when rdfs:label predicates are used and topic types \n"+
               "with rdf:type predicates.";
    }
    
    public static String BASENAME_LOCATOR=RDFS.label.toString();
    public static String TYPE_LOCATOR=RDF.type.toString();
    
    
    @Override
    public void handleStatement(Statement stmt,TopicMap map,Topic subjectType,Topic predicateType,Topic objectType) throws TopicMapException {
        Resource subject    = stmt.getSubject();     // get the subject
        Property predicate  = stmt.getPredicate();   // get the predicate
        RDFNode object      = stmt.getObject();      // get the object
        String lan          = null;

        String predicateS=predicate.toString();
        if(predicateS!=null && (
                predicateS.equals(RDFMapping.RDF_NS+"first") || 
                predicateS.equals(RDFMapping.RDF_NS+"rest") ) ) {
            // skip broken down list statements, the list
            // should be handled properly when they are the object
            // of some other statement
            return;
        }
        
        
        try {
            lan = stmt.getLanguage();
        }
        catch(Exception e) { /*NOTHING!*/ }
        
        Topic subjectTopic = getOrCreateTopic(map, subject);
        Topic predicateTopic = getOrCreateTopic(map, predicate);

        subjectTopic.addType(subjectType);
        predicateTopic.addType(predicateType);

        if(object.isLiteral()) {
            if(predicate.toString().equals(BASENAME_LOCATOR)){
                subjectTopic.setBaseName(((Literal)object).getString()+" ("+subject.toString()+")");
            }
            else {
                if(lan == null) {
                    subjectTopic.setData(predicateTopic, getOrCreateTopic(map, occurrenceScopeSI), ((Literal) object).getString());
                }
                else {
                    subjectTopic.setData(predicateTopic, getOrCreateTopic(map, XTMPSI.getLang(lan)), ((Literal) object).getString());
                }
            }
        }
        else if(object.isResource()) {
            if(object.canAs(RDFList.class)){
                List<RDFNode> list=((RDFList)object.as(RDFList.class)).asJavaList();
                int counter=1;
                Topic orderRole = getOrCreateTopic(map, RDF_LIST_ORDER);
                for(RDFNode listObject : list){
                    if(!listObject.isResource()){
                        log("List element is not a resource, skipping.");
                        continue;
                    }
                    Topic objectTopic = getOrCreateTopic(map, listObject.toString());                
                    objectTopic.addType(objectType);
                    
                    Topic orderTopic = getOrCreateTopic(map, RDF_LIST_ORDER+"/"+counter);
                    if(orderTopic.getBaseName()==null) orderTopic.setBaseName(""+counter);
                    Association association = map.createAssociation(predicateTopic);
                    association.addPlayer(subjectTopic, subjectType);
                    association.addPlayer(objectTopic, objectType);
                    association.addPlayer(orderTopic, orderRole);
                    counter++;
                }
            }
            else {
                Topic objectTopic = getOrCreateTopic(map, (Resource)object);

                if(predicate.toString().equals(TYPE_LOCATOR)){
                    subjectTopic.addType(objectTopic);
                }
                else {
                    Association association = map.createAssociation(predicateTopic);
                    association.addPlayer(subjectTopic, subjectType);
                    association.addPlayer(objectTopic, objectType);

                    objectTopic.addType(objectType);
                }
            }
        }
        else if(object.isURIResource()) {
            log("URIResource found but not handled!");
        }        
    }
    
    
}
