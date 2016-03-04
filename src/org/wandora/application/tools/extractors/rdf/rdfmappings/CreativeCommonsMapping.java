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
 * CreativeCommonsMapping.java
 *
 * Created on 7.10.2009,12:02
 */


package org.wandora.application.tools.extractors.rdf.rdfmappings;

/**
 *
 * @author akivela
 */
public class CreativeCommonsMapping extends RDF2TopicMapsMapping {

    
    public static final String CC_NS = "http://creativecommons.org/ns#";
    public static final String CC_ROLE_NS = "http://wandora.org/si/cc/role/";
    
    public static final String[] SI_BASENAME_MAPPING = new String[] {
        CC_NS+"Work",
            "cc:Work",
        CC_NS+"License",
            "cc:License",
        CC_NS+"Jurisdiction",
            "cc:Jurisdiction",
        CC_NS+"Permission",
            "cc:Permission",
        CC_NS+"Requirement",
            "cc:Requirement",
        CC_NS+"Prohibition",
            "cc:Prohibition",

        CC_NS+"Reproduction",
            "cc:Reproduction",
        CC_NS+"Distribution",
            "cc:Distribution",
        CC_NS+"DerivativeWorks",
            "cc:DerivativeWorks",
        CC_NS+"HighIncomeNationUse",
            "cc:HighIncomeNationUse",
        CC_NS+"Sharing",
            "cc:Sharing",

        CC_NS+"Notice",
            "cc:Notice",
        CC_NS+"Attribution",
            "cc:Attribution",
        CC_NS+"ShareAlike",
            "cc:ShareAlike",
        CC_NS+"SourceCode",
            "cc:SourceCode",
        CC_NS+"Copyleft",
            "cc:Copyleft",
        CC_NS+"LesserCopyleft",
            "cc:LesserCopyleft",

        CC_NS+"CommercialUse",
            "cc:CommercialUse",

        CC_NS+"permits",
            "cc:permits",
        CC_NS+"requires",
            "cc:requires",
        CC_NS+"prohibits",
            "cc:prohibits",
        CC_NS+"jurisdiction",
            "cc:jurisdiction",
        CC_NS+"legalcode",
            "cc:legalcode",
        CC_NS+"deprecatedOn",
            "cc:deprecatedOn",

        CC_NS+"license",
            "cc:license",
        CC_NS+"morePermissions",
            "cc:morePermissions",
        CC_NS+"attributionName",
            "cc:attributionName",
        CC_NS+"attributionURL",
            "cc:attributionURL",
    };


    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        CC_NS+"prohibits",
            CC_ROLE_NS+"license", "license (cc)",
            CC_ROLE_NS+"prohibition", "prohibition (cc)",
        CC_NS+"requires",
            CC_ROLE_NS+"license", "license (cc)",
            CC_ROLE_NS+"requirement", "requirement (cc)",
        CC_NS+"jurisdiction",
            CC_ROLE_NS+"license", "license (cc)",
            CC_ROLE_NS+"jurisdiction", "jurisdiction (cc)",
        CC_NS+"permits",
            CC_ROLE_NS+"license", "license (cc)",
            CC_ROLE_NS+"permission", "permission (cc)",
        CC_NS+"attributionName",
            CC_ROLE_NS+"work", "work (cc)",
            CC_ROLE_NS+"name", "name (cc)",
        CC_NS+"deprecatedOn",
            CC_ROLE_NS+"license", "license (cc)",
            CC_ROLE_NS+"date", "date (cc)",
        CC_NS+"morePermissions",
            CC_ROLE_NS+"work", "work (cc)",
            CC_ROLE_NS+"resource", "resource (cc)",
        CC_NS+"license",
            CC_ROLE_NS+"work", "work (cc)",
            CC_ROLE_NS+"license", "license (cc)",
        CC_NS+"legalcode",
            CC_ROLE_NS+"license", "license (cc)",
            CC_ROLE_NS+"resource", "resource (cc)",
        CC_NS+"attributionURL",
            CC_ROLE_NS+"work", "work (cc)",
            CC_ROLE_NS+"resource", "resource (cc)",
    };


    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }

}
