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
 */
package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author olli
 */


public class OAMapping extends RDF2TopicMapsMapping{
    public static final String OA_NS = "http://www.w3.org/ns/oa#";
    
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        // CLASSES
        OA_NS+"Annotation",
            "oa:Annotation",
        OA_NS+"Choice",
            "oa:Choice",
        OA_NS+"Composite",
            "oa:Composite",
        OA_NS+"CssStyle",
            "oa:CssStyle",
        OA_NS+"DataPositionSelector",
            "oa:DataPositionSelector",
        OA_NS+"FragmentSelector",
            "oa:FragmentSelector",
        OA_NS+"HttpRequestState",
            "oa:HttpRequestState",
        OA_NS+"List",
            "oa:List",
        OA_NS+"Motivation",
            "oa:Motivation",
        OA_NS+"Selector",
            "oa:Selector",
        OA_NS+"SemanticTag",
            "oa:SemanticTag",
        OA_NS+"SpecificResource",
            "oa:SpecificResource",
        OA_NS+"State",
            "oa:State",
        OA_NS+"Style",
            "oa:Style",
        OA_NS+"SvgSelector",
            "oa:SvgSelector",
        OA_NS+"Tag",
            "oa:Tag",
        OA_NS+"TextPositionSelector",
            "oa:TextPositionSelector",
        OA_NS+"TextQuoteSelector",
            "oa:TextQuoteSelector",
        OA_NS+"TimeState",
            "oa:TimeState",
            
            
        // ObjectProperties
        OA_NS+"annotatedBy",
            "oa:annotatedBy",
        OA_NS+"cachedSource",
            "oa:cachedSource",
        OA_NS+"default",
            "oa:default",
        OA_NS+"equivalentTo",
            "oa:equivalentTo",
        OA_NS+"hasBody",
            "oa:hasBody",
        OA_NS+"hasScope",
            "oa:hasScope",
        OA_NS+"hasSelector",
            "oa:hasSelector",
        OA_NS+"hasSource",
            "oa:hasSource",
        OA_NS+"hasState",
            "oa:hasState",
        OA_NS+"hasTarget",
            "oa:hasTarget",
        OA_NS+"item",
            "oa:item",
        OA_NS+"motivatedBy",
            "oa:motivatedBy",
        OA_NS+"serializedBy",
            "oa:serializedBy",
        OA_NS+"styledBy",
            "oa:styledBy",
            
        // DatatypeProperties
        OA_NS+"annotatedAt",
            "oa:annotatedAt",
        OA_NS+"end",
            "oa:end",
        OA_NS+"exact",
            "oa:exact",
        OA_NS+"prefix",
            "oa:prefix",
        OA_NS+"serializedAt",
            "oa:serializedAt",
        OA_NS+"start",
            "oa:start",
        OA_NS+"styleClass",
            "oa:styleClass",
        OA_NS+"suffix",
            "oa:suffix",
        OA_NS+"when",
            "oa:when",
            
        
    };
    
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        OA_NS+"annotatedBy",
            OA_NS+"Annotation",null,
            RDF_NS+"object", "Object",

        OA_NS+"cachedSource",
            OA_NS+"TimeState",null,
            RDF_NS+"object", "Object",

        OA_NS+"default",
            OA_NS+"Choice",null,
            RDF_NS+"object", "Object",

        OA_NS+"equivalentTo",
            RDF_NS+"subject", "Subject",
            RDF_NS+"object", "Object",

        OA_NS+"hasBody",
            OA_NS+"Annotation",null,
            RDF_NS+"object", "Object",

        OA_NS+"hasScope",
            OA_NS+"SpecificResource",null,
            RDF_NS+"object", "Object",

        OA_NS+"hasSelector",
            OA_NS+"SpecificResource",null,
            OA_NS+"Selector",null,

        OA_NS+"hasSource",
            OA_NS+"SpecificResource",null,
            RDF_NS+"object", "Object",

        OA_NS+"hasState",
            OA_NS+"SpecificResource",null,
            OA_NS+"State",null,

        OA_NS+"hasTarget",
            OA_NS+"Annotation",null,
            RDF_NS+"object", "Object",

        OA_NS+"item",
            RDF_NS+"object", "Subject",
            RDF_NS+"object", "Object",

        OA_NS+"motivatedBy",
            OA_NS+"Annotation",null,
            OA_NS+"Motivation",null,

        OA_NS+"serializedBy",
            OA_NS+"Annotation",null,
            RDF_NS+"object", "Object",

        OA_NS+"styledBy",
            OA_NS+"Annotation",null,
            OA_NS+"Style",null,
        
    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }        
}
