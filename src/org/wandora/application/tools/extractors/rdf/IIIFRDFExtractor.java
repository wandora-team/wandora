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

package org.wandora.application.tools.extractors.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import org.wandora.application.tools.extractors.rdf.rdfmappings.DublinCoreMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.EXIFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.IIIFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.OAMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDF2TopicMapsMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.RDFSMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.SCMapping;
import org.wandora.application.tools.extractors.rdf.rdfmappings.SKOSMapping;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class IIIFRDFExtractor extends AbstractRDFExtractor {
    protected RDF2TopicMapsMapping[] mappings=new RDF2TopicMapsMapping[]{
        new IIIFMapping(),
        new SCMapping(),
        new OAMapping(),
        new DublinCoreMapping(),
        new EXIFMapping(),
        new RDFMapping(),
        new RDFSMapping(),
        new SKOSMapping(),
    };

    @Override
    public String getName() {
        return "IIIF JSON-LD Extractor";
    }
    
    @Override
    public String getDescription(){
        return "Reads IIIF JSON-LD and converts it to a topic map";
    }
    
    
    @Override
    public String getRDFContainerFormat() {
        return "JSON-LD";
    }
    
    
    protected static final String label_uri=RDFSMapping.RDFS_NS+"label";
    protected static final String type_uri=RDFMapping.RDF_NS+"type";
    @Override
    public void handleStatement(Statement stmt, TopicMap map) throws TopicMapException {
        Resource subject   = stmt.getSubject();     // get the subject
        Property predicate = stmt.getPredicate();   // get the predicate
        RDFNode object     = stmt.getObject();      // get the object
        
        String predicateId=predicate.toString();
        if(label_uri.equals(predicateId) && object.isLiteral()) {
            Topic subjectTopic = getOrCreateTopic(map, subject.toString());
            subjectTopic.setDisplayName(null, ((Literal) object).getString());
        }
        else if(type_uri.equals(predicateId) && object.isResource()){
            Topic subjectTopic = getOrCreateTopic(map, subject.toString());
            Topic objectTopic = getOrCreateTopic(map, object.toString());
            subjectTopic.addType(objectTopic);
            
            if(object.toString().equals(DublinCoreMapping.DC_TYPES_NS+"Image")){
                subjectTopic.setSubjectLocator(map.createLocator(subject.toString()));
            }
        }
        else {
            super.handleStatement(stmt, map);
        }
    }    
    
    @Override
    public RDF2TopicMapsMapping[] getMappings() {
        return mappings;
    }
    
}
