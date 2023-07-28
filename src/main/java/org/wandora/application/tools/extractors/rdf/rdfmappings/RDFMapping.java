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
 * 
 * RDFMapping.java
 *
 * Created on 13.2.2009,15:25
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class RDFMapping extends RDF2TopicMapsMapping {
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDF_ROLE_NS = "http://wandora.org/si/rdf/role/";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        RDF_NS+"type",
            "rdf:type",
        RDF_NS+"Property",
            "rdf:Property",
        RDF_NS+"Statement",
            "rdf:Statement",
        RDF_NS+"subject",
            "rdf:subject",
        RDF_NS+"predicate",
            "rdf:predicate",
        RDF_NS+"object",
            "rdf:object",
        RDF_NS+"Bag",
            "rdf:Bag",
        RDF_NS+"Seq",
            "rdf:Seq",
        RDF_NS+"Alt",
            "rdf:Alt",
        RDF_NS+"value",
            "rdf:value",
        RDF_NS+"List",
            "rdf:List",
        RDF_NS+"nil",
            "rdf:nil",
        RDF_NS+"first",
            "rdf:first",
        RDF_NS+"rest",
            "rdf:rest",
        RDF_NS+"XMLLiteral",
            "rdf:XMLLiteral",
    };

    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        RDF_NS+"type",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            RDF_ROLE_NS+"class", "class (rdf)",

        RDF_NS+"subject",
            RDF_ROLE_NS+"statement", "statement (rdf)",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            
        RDF_NS+"predicate",
            RDF_ROLE_NS+"statement", "statement (rdf)",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            
        RDF_NS+"object",
            RDF_ROLE_NS+"statement", "statement (rdf)",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            
        RDF_NS+"value",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            RDF_ROLE_NS+"other-resource", "other-resource (rdf)",

        RDF_NS+"first",
            RDF_ROLE_NS+"list", "list (rdf)",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            
        RDF_NS+"rest",
            RDF_ROLE_NS+"list", "list (rdf)",
            RDF_ROLE_NS+"rest-of-list", "rest-of-list (rdf)",

    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }

}
