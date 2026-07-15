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
 */
package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author olli
 */


public class SCMapping extends RDF2TopicMapsMapping{
    public static final String SC_NS = "http://www.shared-canvas.org/ns/";
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        SC_NS+"hasRelatedService",
            "sc:hasRelatedService",
        SC_NS+"hasRelatedDescription",
            "sc:hasRelatedDescription",
        SC_NS+"hasSequences",
            "sc:hasSequences",
        SC_NS+"hasCanvases",
            "sc:hasCanvases",
        SC_NS+"hasAnnotations",
            "sc:hasAnnotations",
        SC_NS+"hasImageAnnotations",
            "sc:hasImageAnnotations",
        SC_NS+"hasLists",
            "sc:hasLists",
        SC_NS+"hasRanges",
            "sc:hasRanges",
        SC_NS+"metadataLabels",
            "sc:metadataLabels",
        SC_NS+"attributionLabel",
            "sc:attributionLabel",
        SC_NS+"viewingDirection",
            "sc:viewingDirection",
        SC_NS+"viewingHint",
            "sc:viewingHint",
            
            
        SC_NS+"Manifest",
            "sc:Manifest",
        SC_NS+"Sequence",
            "sc:Sequence",
        SC_NS+"Canvas",
            "sc:Canvas",
        SC_NS+"painting", // This not capitalised
            "sc:painting",
    };
    
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        SC_NS+"hasSequences",
            SC_NS+"Manifest", null,
            SC_NS+"Sequence", null,
            
        SC_NS+"hasCanvases",
            SC_NS+"Sequence", null,
            SC_NS+"Canvas", null,
            
        SC_NS+"hasImageAnnotations",
            SC_NS+"Canvas", null,
            OAMapping.OA_NS+"Annotation", null,

        SC_NS+"hasRelatedService",
            RDF_NS+"subject", "Subject",
            RDF_NS+"object", "Object",
    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }    
    
}
