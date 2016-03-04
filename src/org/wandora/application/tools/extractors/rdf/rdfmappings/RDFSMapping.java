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
 * RDFSMapping.java
 *
 * Created on 13.2.2009,15:25
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class RDFSMapping extends RDF2TopicMapsMapping {
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String RDFS_ROLE_NS = "http://wandora.org/si/rdfs/role/";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {

        RDFS_NS+"Resource",
            "rdfs:Resource",
        RDFS_NS+"Class",
            "rdfs:Class",
        RDFS_NS+"subClassOf",
            "rdfs:subClassOf",
        RDFS_NS+"subPropertyOf",
            "rdfs:subPropertyOf",
        RDFS_NS+"comment",
            "rdfs:comment",
        RDFS_NS+"Property",
            "rdfs:Property",
        RDFS_NS+"label",
            "rdfs:label",
        RDFS_NS+"domain",
            "rdfs:domain",
        RDFS_NS+"range",
            "rdfs:range",
        RDFS_NS+"seeAlso",
            "rdfs:seeAlso",
        RDFS_NS+"isDefinedBy",
            "rdfs:isDefinedBy",
        RDFS_NS+"Literal",
            "rdfs:Literal",
        RDFS_NS+"Container",
            "rdfs:Container",
        RDFS_NS+"ContainerMembershipProperty",
            "rdfs:ContainerMembershipProperty",
        RDFS_NS+"member",
            "rdfs:member",
        RDFS_NS+"Datatype",
            "rdfs:Datatype",
        RDFS_NS+"Datatype",
            "rdfs:Datatype",
    };
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        RDFS_NS+"subClassOf",
            RDFS_ROLE_NS+"subclass", "subclass (rdfs)",
            RDFS_ROLE_NS+"class", "class (rdfs)",
            
        RDFS_NS+"subPropertyOf",
            RDFS_ROLE_NS+"subproperty", "subproperty (rdfs)",
            RDFS_ROLE_NS+"property", "property (rdfs)",
            
        RDFS_NS+"domain",
            RDFS_ROLE_NS+"property", "property (rdfs)",
            RDFS_ROLE_NS+"domain", "domain (rdfs)",
            
        RDFS_NS+"range",
            RDFS_ROLE_NS+"property", "property (rdfs)",
            RDFS_ROLE_NS+"range", "range (rdfs)",
            
        RDFS_NS+"seeAlso",
            RDFS_ROLE_NS+"resource", "resource (rdfs)",
            RDFS_ROLE_NS+"other-resource", "other-resource (rdfs)",
        
        RDFS_NS+"isDefinedBy",
            RDFS_ROLE_NS+"defined-resource", "defined-resource (rdfs)",
            RDFS_ROLE_NS+"definer-resource", "definer-resource (rdfs)",

        RDFS_NS+"member",
            RDFS_ROLE_NS+"member-resource", "member-resource (rdfs)",
            RDFS_ROLE_NS+"group-resource", "group-resource (rdfs)",
    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }


}
