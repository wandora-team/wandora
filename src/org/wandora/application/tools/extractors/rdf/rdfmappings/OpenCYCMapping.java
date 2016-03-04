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
 * OpenCYCMapping.java
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 * See http://sw.opencyc.org/
 *
 * @author akivela
 */
public class OpenCYCMapping extends RDF2TopicMapsMapping {
    public static final String OPEN_CYC_NS = "http://sw.opencyc.org/2012/05/10/concept/en/";
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        OPEN_CYC_NS+"wikipediaArticleURL",
            "opencyc:wikipediaArticleURL",
        OPEN_CYC_NS+"seeAlsoURI",
            "opencyc:seeAlsoURI",
        OPEN_CYC_NS+"wikipediaArticleName_Canonical",
            "opencyc:wikipediaArticleName_Canonical",
        OPEN_CYC_NS+"facets_Generic",
            "opencyc:facets_Generic",
        OPEN_CYC_NS+"quotedIsa",
            "opencyc:quotedIsa",
        OPEN_CYC_NS+"superTaxons",
            "opencyc:superTaxons",
        OPEN_CYC_NS+"prettyString",
            "opencyc:prettyString",

        "http://sw.cyc.com/CycAnnotations_v1#label",
            "CycAnnotations_v1#label",
        "http://sw.cyc.com/CycAnnotations_v1#externalID",
            "CycAnnotations_v1#externalID",
    };

    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        /* ****** THINK NEXT AS AN EXAMPLE: ASSOCIATION TYPE AND THEN ROLES.
        RDF_NS+"type",
            RDF_ROLE_NS+"resource", "resource (rdf)",
            RDF_ROLE_NS+"class", "class (rdf)",
        */

    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }

}
