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
 * SKOSMapping.java
 *
 * Created on 13.2.2009,15:25
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class SKOSMapping extends RDF2TopicMapsMapping {
    public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
    public static final String SKOSXL_NS = "http://www.w3.org/2008/05/skos-xl#";
    public static final String SKOS_ROLE_NS = "http://wandora.org/si/skos/role/";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        SKOS_NS+"Concept",
            "skos:Concept",
        SKOS_NS+"ConceptScheme",
            "skos:ConceptScheme",
        SKOS_NS+"inScheme",
            "skos:inScheme",
        SKOS_NS+"hasTopConcept",
            "skos:hasTopConcept",
        SKOS_NS+"topConceptOf",
            "skos:topConceptOf",
            
        SKOS_NS+"prefLabel",
            "skos:prefLabel",
        SKOS_NS+"altLabel",
            "skos:altLabel",
        SKOS_NS+"hiddenLabel",
            "skos:hiddenLabel",
            
        SKOS_NS+"notation",
            "skos:notation",
            
        SKOS_NS+"note",
            "skos:note",
        SKOS_NS+"changeNote",
            "skos:changeNote",
        SKOS_NS+"definition",
            "skos:definition",
        SKOS_NS+"editorialNote",
            "skos:editorialNote",
        SKOS_NS+"example",
            "skos:example",
        SKOS_NS+"historyNote",
            "skos:historyNote",
        SKOS_NS+"scopeNote",
            "skos:scopeNote",
            
        SKOS_NS+"semanticRelation",
            "skos:semanticRelation",
        SKOS_NS+"broader",
            "skos:broader",
        SKOS_NS+"narrower",
            "skos:narrower",
        SKOS_NS+"related",
            "skos:related",
        SKOS_NS+"broaderTransitive",
            "skos:broaderTransitive",
        SKOS_NS+"narrowerTransitive",
            "skos:narrowerTransitive",
            
        SKOS_NS+"Collection",
            "skos:Collection",
        SKOS_NS+"OrderedCollection",
            "skos:OrderedCollection",
        SKOS_NS+"member",
            "skos:member",
        SKOS_NS+"memberList",
            "skos:memberList",
            
        SKOS_NS+"mappingRelation",
            "skos:mappingRelation",
        SKOS_NS+"closeMatch",
            "skos:closeMatch",
        SKOS_NS+"exactMatch",
            "skos:exactMatch",
        SKOS_NS+"broadMatch",
            "skos:broadMatch",
        SKOS_NS+"narrowMatch",
            "skos:narrowMatch",
        SKOS_NS+"relatedMatch",
            "skos:relatedMatch",
            
        // ****** SKOS XL *****
        SKOSXL_NS+"Label",
            "skosxl:Label",
        SKOSXL_NS+"literalForm",
            "skosxl:literalForm",
        SKOSXL_NS+"prefLabel",
            "skosxl:prefLabel",
        SKOSXL_NS+"altLabel",
            "skosxl:altLabel",
        SKOSXL_NS+"hiddenLabel",
            "skosxl:hiddenLabel",
        SKOSXL_NS+"labelRelation",
            "skosxl:labelRelation",
    };
    
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        SKOS_NS+"inScheme",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"scheme", "scheme (skos)",
        SKOS_NS+"hasTopConcept",
            SKOS_ROLE_NS+"scheme", "scheme (skos)",
            SKOS_ROLE_NS+"concept", "concept (skos)",
        SKOS_NS+"topConceptOf",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"scheme", "scheme (skos)",


        SKOS_NS+"semanticRelation",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"semantically-related-concept", "semantically-related-concept (skos)",
        SKOS_NS+"broader",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"broader-concept", "broader-concept (skos)",
        SKOS_NS+"narrower",
            SKOS_ROLE_NS+"concept", "concep (skos)",
            SKOS_ROLE_NS+"narrower-concept", "narrower-concept (skos)",
        SKOS_NS+"related",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"related-concept", "related-concept (skos)",
        SKOS_NS+"broaderTransitive",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"broader-concept", "broader-concept (skos)",
        SKOS_NS+"narrowerTransitive",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"narrower-concept", "narrower-concept (skos)",
            
            
        SKOS_NS+"member",
            SKOS_ROLE_NS+"member", "member (skos)",
            SKOS_ROLE_NS+"collection", "collection (skos)",
        SKOS_NS+"memberList",
            SKOS_ROLE_NS+"member", "member (skos)",
            SKOS_ROLE_NS+"collection", "collection (skos)",
            
            
        SKOS_NS+"mappingRelation",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"mapped-concept", "mapped-concept (skos)",
        SKOS_NS+"closeMatch",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"mapped-concept", "mapped-concept (skos)",
        SKOS_NS+"exactMatch",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"mapped-concept", "mapped-concept (skos)",
        SKOS_NS+"relatedMatch",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"mapped-concept", "mapped-concept (skos)",
        SKOS_NS+"broadMatch",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"mapped-concept", "mapped-concept (skos)",
        SKOS_NS+"narrowMatch",
            SKOS_ROLE_NS+"concept", "concept (skos)",
            SKOS_ROLE_NS+"mapped-concept", "mapped-concept (skos)",
            
        // ****** SKOS XL *****
        SKOSXL_NS+"labelRelation",
            SKOS_ROLE_NS+"label", "label (skos)",
            SKOS_ROLE_NS+"related-label", "related-label (skos)",
    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }
}

