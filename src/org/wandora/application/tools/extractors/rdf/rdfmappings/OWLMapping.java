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
 * OWLMapping.java
 *
 * Created on 13.2.2009,15:25
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class OWLMapping extends RDF2TopicMapsMapping {
    public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    public static final String OWL_ROLE_NS = "http://wandora.org/si/owl/role/";
    
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        OWL_NS+"AllDifferent",
            "owl:AllDifferent",
        OWL_NS+"allValuesFrom",
            "owl:allValuesFrom",
        OWL_NS+"AnnotationProperty",
            "owl:AnnotationProperty",
        OWL_NS+"backwardCompatibleWith",
            "owl:backwardCompatibleWith",
        OWL_NS+"cardinality",
            "owl:cardinality",
        OWL_NS+"Class",
            "owl:Class",
        OWL_NS+"complementOf",
            "owl:complementOf",
        OWL_NS+"DataRange",
            "owl:DataRange",
        OWL_NS+"DatatypeProperty",
            "owl:DatatypeProperty",
        OWL_NS+"DeprecatedClass",
            "owl:DeprecatedClass",
        OWL_NS+"DeprecatedProperty",
            "owl:DeprecatedProperty",
        OWL_NS+"differentFrom",
            "owl:differentFrom",
        OWL_NS+"disjointWith",
            "owl:disjointWith",
        OWL_NS+"distinctMembers",
            "owl:distinctMembers",
        OWL_NS+"equivalentClass",
            "owl:equivalentClass",
        OWL_NS+"equivalentProperty",
            "owl:equivalentProperty",
        OWL_NS+"FunctionalProperty",
            "owl:FunctionalProperty",
        OWL_NS+"hasValue",
            "owl:hasValue",
        OWL_NS+"imports",
            "owl:imports",
        OWL_NS+"incompatibleWith",
            "owl:incompatibleWith",
        OWL_NS+"intersectionOf",
            "owl:intersectionOf",
        OWL_NS+"InverseFunctionalProperty",
            "owl:InverseFunctionalProperty",
        OWL_NS+"inverseOf",
            "owl:inverseOf",
        OWL_NS+"maxCardinality",
            "owl:maxCardinality",
        OWL_NS+"minCardinality",
            "owl:minCardinality",
        OWL_NS+"Nothing",
            "owl:Nothing",
        OWL_NS+"ObjectProperty",
            "owl:ObjectProperty",
        OWL_NS+"oneOf",
            "owl:oneOf",
        OWL_NS+"onProperty",
            "owl:onProperty",
        OWL_NS+"Ontology",
            "owl:Ontology",
        OWL_NS+"OntologyProperty",
            "owl:OntologyProperty",
        OWL_NS+"priorVersion",
            "owl:priorVersion",
        OWL_NS+"Restriction",
            "owl:Restriction",
        OWL_NS+"sameAs",
            "owl:sameAs",
        OWL_NS+"someValuesFrom",
            "owl:someValuesFrom",
        OWL_NS+"SymmetricProperty",
            "owl:SymmetricProperty",
        OWL_NS+"Thing",
            "owl:Thing",
        OWL_NS+"TransitiveProperty",
            "owl:TransitiveProperty",
        OWL_NS+"unionOf",
            "owl:unionOf",
        OWL_NS+"versionInfo",
            "owl:versionInfo",

    };
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        OWL_NS+"allValuesFrom",
            OWL_ROLE_NS+"restriction", "restriction (owl)",
            OWL_ROLE_NS+"class", "class (owl)",
        OWL_NS+"backwardCompatibleWith",
            OWL_ROLE_NS+"ontology", "ontology (owl)",
            OWL_ROLE_NS+"compatible-ontology", "compatible-ontology (owl)",
        OWL_NS+"complementOf",
            OWL_ROLE_NS+"class", "class (owl)",
            OWL_ROLE_NS+"complement-class", "complement-class (owl)",
        OWL_NS+"differentFrom",
            OWL_ROLE_NS+"thing", "thing (owl)",
            OWL_ROLE_NS+"different-thing", "different-thing (owl)",
        OWL_NS+"disjointWith",
            OWL_ROLE_NS+"class", "class (owl)",
            OWL_ROLE_NS+"disjoint-class", "disjoint-class (owl)",
        OWL_NS+"distinctMembers",
            OWL_ROLE_NS+"group", "group (owl)",
            OWL_ROLE_NS+"distinct-group", "distinct-group (owl)",
        OWL_NS+"equivalentClass",
            OWL_ROLE_NS+"class", "class (owl)",
            OWL_ROLE_NS+"equivalent-class", "equivalent-class (owl)",
        OWL_NS+"equivalentProperty",
            OWL_ROLE_NS+"property", "property (owl)",
            OWL_ROLE_NS+"equivalent-property", "equivalent-property (owl)",
        OWL_NS+"imports",
            OWL_ROLE_NS+"ontology", "ontology (owl)",
            OWL_ROLE_NS+"imported-ontology", "imported-ontology (owl)",
        OWL_NS+"incompatibleWith",
            OWL_ROLE_NS+"ontology", "ontology (owl)",
            OWL_ROLE_NS+"incompatible-ontology", "incompatible-ontology (owl)",
        OWL_NS+"intersectionOf",
            OWL_ROLE_NS+"class", "class (owl)",
            OWL_ROLE_NS+"list", "list (owl)",

        OWL_NS+"inverseOf",
            OWL_ROLE_NS+"property", "property (owl)",
            OWL_ROLE_NS+"inverse-property", "inverse-property (owl)",
        OWL_NS+"oneOf",
            OWL_ROLE_NS+"class", "class (owl)",
            OWL_ROLE_NS+"list", "list (owl)",
        OWL_NS+"onProperty",
            OWL_ROLE_NS+"restriction", "restriction (owl)",
            OWL_ROLE_NS+"property", "property (owl)",
        OWL_NS+"priorVersion",
            OWL_ROLE_NS+"ontology", "ontology (owl)",
            OWL_ROLE_NS+"prior-version-ontology", "prior-version-ontology (owl)",
        OWL_NS+"sameAs",
            OWL_ROLE_NS+"thing", "thing (owl)",
            OWL_ROLE_NS+"same-as-thing", "same-as-thing (owl)",
        OWL_NS+"someValuesFrom",
            OWL_ROLE_NS+"restriction", "restriction (owl)",
            OWL_ROLE_NS+"class", "class (owl)",
        OWL_NS+"unionOf",
            OWL_ROLE_NS+"class", "class (owl)",
            OWL_ROLE_NS+"list", "list (owl)",
    };
    
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }
}
