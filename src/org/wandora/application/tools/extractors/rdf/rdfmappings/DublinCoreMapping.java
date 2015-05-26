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
 * 
 * DublinCoreMapping.java
 *
 * Created on 13.2.2009,15:25
 */



package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class DublinCoreMapping extends RDF2TopicMapsMapping {
    public final static String DC_ELEMENTS_NS = "http://purl.org/dc/elements/1.1/";
    public final static String DC_TERMS_NS = "http://purl.org/dc/terms/";
    public final static String DC_TYPES_NS = "http://purl.org/dc/dcmitype/";
    
    public static final String DC_ROLE_NS = "http://wandora.org/si/dc/role/";
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        DC_ELEMENTS_NS+"title",
            "dc:title",
        DC_ELEMENTS_NS+"creator",
            "dc:creator",
        DC_ELEMENTS_NS+"subject",
            "dc:subject",
        DC_ELEMENTS_NS+"description",
            "dc:description",
        DC_ELEMENTS_NS+"publisher",
            "dc:publisher",
        DC_ELEMENTS_NS+"contributor",
            "dc:contributor",
        DC_ELEMENTS_NS+"date",
            "dc:date",
        DC_ELEMENTS_NS+"type",
            "dc:type",
        DC_ELEMENTS_NS+"format",
            "dc:format",
        DC_ELEMENTS_NS+"identifier",
            "dc:identifier",
        DC_ELEMENTS_NS+"source",
            "dc:source",
        DC_ELEMENTS_NS+"language",
            "dc:language",
        DC_ELEMENTS_NS+"relation",
            "dc:relation",
        DC_ELEMENTS_NS+"coverage",
            "dc:coverage",
        DC_ELEMENTS_NS+"rights",
            "dc:rights",

        // ****** DUBLIN CORE TERM *****
        DC_TERMS_NS+"title",
            "dc-term:title",
        DC_TERMS_NS+"creator",
            "dc-term:creator",
        DC_TERMS_NS+"subject",
            "dc-term:subject",
        DC_TERMS_NS+"description",
            "dc-term:description",
        DC_TERMS_NS+"publisher",
            "dc-term:publisher",
        DC_TERMS_NS+"contributor",
            "dc-term:contributor",
        DC_TERMS_NS+"date",
            "dc-term:date",
        DC_TERMS_NS+"type",
            "dc-term:type",
        DC_TERMS_NS+"format",
            "dc-term:format",
        DC_TERMS_NS+"identifier",
            "dc-term:identifier",
        DC_TERMS_NS+"source",
            "dc-term:source",
        DC_TERMS_NS+"language",
            "dc-term:language",
        DC_TERMS_NS+"relation",
            "dc-term:relation",
        DC_TERMS_NS+"coverage",
            "dc-term:coverage",
        DC_TERMS_NS+"audience",
            "dc-term:audience",
        DC_TERMS_NS+"alternative",
            "dc-term:alternative",
        DC_TERMS_NS+"tableOfContents",
            "dc-term:table of contents",
        DC_TERMS_NS+"abstract",
            "dc-term:abstract",
        DC_TERMS_NS+"created",
            "dc-term:date created",
        DC_TERMS_NS+"valid",
            "dc-term:date valid",
        DC_TERMS_NS+"available",
            "dc-term:date available",
        DC_TERMS_NS+"issued",
            "dc-term:date issued",
        DC_TERMS_NS+"modified",
            "dc-term:date modified",
        DC_TERMS_NS+"extent",
            "dc-term:extent",
        DC_TERMS_NS+"medium",
            "dc-term:medium",
        DC_TERMS_NS+"isVersionOf",
            "dc-term:is version of",
        DC_TERMS_NS+"hasVersion",
            "dc-term:has version",
        DC_TERMS_NS+"isReplacedBy",
            "dc-term:is replaced by",
        DC_TERMS_NS+"replaces",
            "dc-term:replaces",
        DC_TERMS_NS+"isRequiredBy",
            "dc-term:is required by",
        DC_TERMS_NS+"requires",
            "dc-term:requires",
        DC_TERMS_NS+"isPartOf",
            "dc-term:is part of",
        DC_TERMS_NS+"hasPart",
            "dc-term:has part",
        DC_TERMS_NS+"isReferencedBy",
            "dc-term:is referenced by",
        DC_TERMS_NS+"references",
            "dc-term:references",
        DC_TERMS_NS+"isFormatOf",
            "dc-term:isFormatOf",
        DC_TERMS_NS+"hasFormat",
            "dc-term:hasFormat",
        DC_TERMS_NS+"conformsTo",
            "dc-term:conformsTo",
        DC_TERMS_NS+"spatial",
            "dc-term:spatial",
        DC_TERMS_NS+"temporal",
            "dc-term:temporal",
        DC_TERMS_NS+"mediator",
            "dc-term:mediator",
        DC_TERMS_NS+"dateAccepted",
            "dc-term:dateAccepted",
        DC_TERMS_NS+"dateCopyrighted",
            "dc-term:dateCopyrighted",
        DC_TERMS_NS+"dateSubmitted",
            "dc-term:dateSubmitted",
        DC_TERMS_NS+"educationLevel",
            "dc-term:educationLevel",
        DC_TERMS_NS+"accessRights",
            "dc-term:accessRights",
        DC_TERMS_NS+"bibliographicCitation",
            "dc-term:bibliographicCitation",
        DC_TERMS_NS+"license",
            "dc-term:license",
        DC_TERMS_NS+"rightsHolder",
            "dc-term:rightsHolder",
        DC_TERMS_NS+"provenance",
            "dc-term:provenance",
        DC_TERMS_NS+"instructionalMethod",
            "dc-term:instructionalMethod",
        DC_TERMS_NS+"accrualMethod",
            "dc-term:accrualMethod",
        DC_TERMS_NS+"accrualPeriodicity",
            "dc-term:accrualPeriodicity",
        DC_TERMS_NS+"accrualPolicy",
            "dc-term:accrualPolicy",
        DC_TERMS_NS+"Agent",
            "dc-term:Agent",
        DC_TERMS_NS+"AgentClass",
            "dc-term:AgentClass",
        DC_TERMS_NS+"BibliographicResource",
            "dc-term:BibliographicResource",
        DC_TERMS_NS+"FileFormat",
            "dc-term:FileFormat",
        DC_TERMS_NS+"Frequency",
            "dc-term:Frequency",
        DC_TERMS_NS+"Jurisdiction",
            "dc-term:Jurisdiction",
        DC_TERMS_NS+"LicenseDocument",
            "dc-term:LicenseDocument",
        DC_TERMS_NS+"LinguisticSystem",
            "dc-term:LinguisticSystem",
        DC_TERMS_NS+"Location",
            "dc-term:Location",
        DC_TERMS_NS+"LocationPeriodOrJurisdiction",
            "dc-term:LocationPeriodOrJurisdiction",
        DC_TERMS_NS+"MediaType",
            "dc-term:MediaType",
        DC_TERMS_NS+"MediaTypeOrExtent",
            "dc-term:MediaTypeOrExtent",
        DC_TERMS_NS+"MethodOfInstruction",
            "dc-term:MethodOfInstruction",
        DC_TERMS_NS+"MethodOfAccrual",
            "dc-term:MethodOfAccrual",
        DC_TERMS_NS+"PeriodOfTime",
            "dc-term:PeriodOfTime",
        DC_TERMS_NS+"PhysicalMedium",
            "dc-term:PhysicalMedium",
        DC_TERMS_NS+"PhysicalResource",
            "dc-term:PhysicalResource",
        DC_TERMS_NS+"Policy",
            "dc-term:Policy",
        DC_TERMS_NS+"ProvenanceStatement",
            "dc-term:ProvenanceStatement",
        DC_TERMS_NS+"RightsStatement",
            "dc-term:RightsStatement",
        DC_TERMS_NS+"SizeOrDuration",
            "dc-term:SizeOrDuration",
        DC_TERMS_NS+"Standard",
            "dc-term:Standard",
        DC_TERMS_NS+"ISO639-2",
            "dc-term:ISO639-2",
        DC_TERMS_NS+"RFC1766",
            "dc-term:RFC1766",
        DC_TERMS_NS+"URI",
            "dc-term:URI",
        DC_TERMS_NS+"Point",
            "dc-term:Point",
        DC_TERMS_NS+"ISO3166",
            "dc-term:ISO3166",
        DC_TERMS_NS+"Box",
            "dc-term:Box",
        DC_TERMS_NS+"Period",
            "dc-term:Period",
        DC_TERMS_NS+"W3CDTF",
            "dc-term:W3CDTF",
        DC_TERMS_NS+"RFC3066",
            "dc-term:RFC3066",
        DC_TERMS_NS+"RFC4646",
            "dc-term:RFC4646",
        DC_TERMS_NS+"ISO639-3",
            "dc-term:ISO639-3",
        DC_TERMS_NS+"LCSH",
            "dc-term:LCSH",
        DC_TERMS_NS+"MESH",
            "dc-term:MESH",
        DC_TERMS_NS+"DDC",
            "dc-term:DDC",
        DC_TERMS_NS+"LCC",
            "dc-term:LCC",
        DC_TERMS_NS+"UDC",
            "dc-term:UDC",
        DC_TERMS_NS+"DCMIType",
            "dc-term:DCMIType",
        DC_TERMS_NS+"IMT",
            "dc-term:IMT",
        DC_TERMS_NS+"TGN",
            "dc-term:Getty Thesaurus of Geographic Names",
        DC_TERMS_NS+"NLM",
            "dc-term:National Library of Medicine Classification",
            
            
        /* DC Types */
        DC_TYPES_NS+"Collection",
            "dctypes:Collection",
        DC_TYPES_NS+"Dataset",
            "dctypes:Dataset",
        DC_TYPES_NS+"Event",
            "dctypes:Event",
        DC_TYPES_NS+"Image",
            "dctypes:Image",
        DC_TYPES_NS+"InteractiveResource",
            "dctypes:InteractiveResource",
        DC_TYPES_NS+"MovingImage",
            "dctypes:MovingImage",
        DC_TYPES_NS+"PhysicalObject",
            "dctypes:PhysicalObject",
        DC_TYPES_NS+"Service",
            "dctypes:Service",
        DC_TYPES_NS+"Software",
            "dctypes:Software",
        DC_TYPES_NS+"Sound",
            "dctypes:Sound",
        DC_TYPES_NS+"StillImage",
            "dctypes:StillImage",
        DC_TYPES_NS+"Text",
            "dctypes:Text",

    };
    
    
    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
    };

    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }

}
