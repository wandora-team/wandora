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
 * FOAFMapping.java
 *
 * Created on 13.2.2009,15:25
 */



package org.wandora.application.tools.extractors.rdf.rdfmappings;


import org.wandora.utils.Tuples.*;


/**
 *
 * @author akivela
 */
public class FOAFMapping extends RDF2TopicMapsMapping {
    public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
    public static final String FOAF_ROLE_NS = "http://wandora.org/si/foaf/role/";
    
    

    public static final String[] SI_BASENAME_MAPPING = new String[] {
        FOAF_NS+"Person",
            "foaf:Person",
        FOAF_NS+"Organization",
            "foaf:Organization",
        FOAF_NS+"Group",
            "foaf:Group",
        FOAF_NS+"knows",
            "foaf:knows",
        FOAF_NS+"membershipClass",
            "foaf:membershipClass",
        FOAF_NS+"member",
            "foaf:member",
        FOAF_NS+"homepage",
            "foaf:homepage",
        FOAF_NS+"weblog",
            "foaf:weblog",
        FOAF_NS+"nick",
            "foaf:nick",
        FOAF_NS+"givenname",
            "foaf:givenname",
        FOAF_NS+"name",
            "foaf:name",
        FOAF_NS+"firstName",
            "foaf:firstName",
        FOAF_NS+"surname",
            "foaf:surname",
        FOAF_NS+"family_name",
            "foaf:family_name",
        FOAF_NS+"gender",
            "foaf:gender",
        FOAF_NS+"title",
            "foaf:title",
        FOAF_NS+"geekcode",
            "foaf:geekcode",
        FOAF_NS+"msnChatID",
            "foaf:msnChatID",
        FOAF_NS+"myersBriggs",
            "foaf:myersBriggs",
        FOAF_NS+"schoolHomepage",
            "foaf:schoolHomepage",
        FOAF_NS+"publications",
            "foaf:publications",
        FOAF_NS+"plan",
            "foaf:plan",
        FOAF_NS+"phone",
            "foaf:phone",
        FOAF_NS+"homepage",
            "foaf:homepage",
        FOAF_NS+"holdsAccount",
            "foaf:holdsAccount",
        FOAF_NS+"pastProject",
            "foaf:pastProject",
        FOAF_NS+"currentProject",
            "foaf:currentProject",
        FOAF_NS+"Project",
            "foaf:Project",
        FOAF_NS+"page",
            "foaf:page",
        FOAF_NS+"birthday",
            "foaf:birthday",
        FOAF_NS+"openid",
            "foaf:openid",
        FOAF_NS+"jabberID",
            "foaf:jabberID",
        FOAF_NS+"aimChatID",
            "foaf:aimChatID",
        FOAF_NS+"icqChatID",
            "foaf:icqChatID",
        FOAF_NS+"dnaChecksum",
            "foaf:dnaChecksum",
        FOAF_NS+"mbox_sha1sum",
            "foaf:mbox_sha1sum",
        FOAF_NS+"address_sha1sum",
            "foaf:address_sha1sum",
        FOAF_NS+"mbox",
            "foaf:mbox",
        FOAF_NS+"PersonalProfileDocument",
            "foaf:PersonalProfileDocument",
            
        FOAF_NS+"maker",
            "foaf:maker",
        FOAF_NS+"made",
            "foaf:made",
        FOAF_NS+"logo",
            "foaf:logo",
        FOAF_NS+"interest",
            "foaf:interest",
        FOAF_NS+"img",
            "foaf:img",
        FOAF_NS+"Image",
            "foaf:Image",
        FOAF_NS+"fundedBy",
            "foaf:fundedBy",
        FOAF_NS+"depicts",
            "foaf:depicts",
        FOAF_NS+"depiction",
            "foaf:depiction",
        FOAF_NS+"based_near",
            "foaf:based_near",
        FOAF_NS+"workplaceHomepage",
            "foaf:workplaceHomepage",
        FOAF_NS+"accountServiceHomepage",
            "foaf:accountServiceHomepage",
        FOAF_NS+"accountName",
            "foaf:accountName",
        FOAF_NS+"OnlineGamingAccount",
            "foaf:OnlineGamingAccount",
        FOAF_NS+"OnlineEcommerceAccount",
            "foaf:OnlineEcommerceAccount",
        FOAF_NS+"OnlineChatAccount",
            "foaf:OnlineChatAccount",
        FOAF_NS+"OnlineAccount",
            "foaf:OnlineAccount",
        FOAF_NS+"Document",
            "foaf:Document",
        FOAF_NS+"Agent",
            "foaf:Agent",
    };
    
    

    public static final String[] ASSOCIATION_TYPE_TO_ROLES_MAPPING = new String[] {
        FOAF_NS+"knows",
            FOAF_ROLE_NS+"knows", "knows (foaf)",
            FOAF_ROLE_NS+"is-known", "is-known (foaf)",

        FOAF_NS+"member",
            FOAF_ROLE_NS+"member", "member (foaf)",
            FOAF_ROLE_NS+"group", "group (foaf)",
            
        FOAF_NS+"homepage",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"homepage", "homepage (foaf)",
            
        FOAF_NS+"weblog",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"weblog", "weblog (foaf)",
            
        FOAF_NS+"nick",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"nick", "nick (foaf)",
            
        FOAF_NS+"givenname",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"givenname", "given-name (foaf)",
            
        FOAF_NS+"name",
            FOAF_ROLE_NS+"agent", "agent (foaf)",
            FOAF_ROLE_NS+"name", "name (foaf)",
            
        FOAF_NS+"firstName",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"first_name", "first_name (foaf)",
            
        FOAF_NS+"surname",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"surname", "surname (foaf)",
            
        FOAF_NS+"family_name",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"family_name", "family_name (foaf)",
            
        FOAF_NS+"gender",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"gender", "gender (foaf)",
            
        FOAF_NS+"geekcode",
            FOAF_ROLE_NS+"geekcode-owner", "geekcode-owner (foaf)",
            FOAF_ROLE_NS+"geekcode", "geekcode (foaf)",
            
        FOAF_NS+"msnChatID",
            FOAF_ROLE_NS+"msnchatid-owner", "msnchatid-owner (foaf)",
            FOAF_ROLE_NS+"msnchatid", "msnchatid (foaf)",
            
        FOAF_NS+"myersBriggs",
            FOAF_ROLE_NS+"myersbriggs-owner", "myersbriggs-owner (foaf)",
            FOAF_ROLE_NS+"myersbriggs", "myersbriggs (foaf)",
            
        FOAF_NS+"schoolHomepage",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"school homepage", "school homepage (foaf)",
            
        FOAF_NS+"workplaceHomepage",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"workplace homepage", "workplace homepage (foaf)",
            
        FOAF_NS+"publications",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"publications", "publications (foaf)",
            
        FOAF_NS+"plan",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"plan", "plan (foaf)",
            
        FOAF_NS+"phone",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"phone", "phone (foaf)",
            
        FOAF_NS+"homepage",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"homepage", "homepage (foaf)",
            
        FOAF_NS+"holdsAccount",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"holds-account", "holds-account (foaf)",
            
        FOAF_NS+"pastProject",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"past-project", "past-project (foaf)",
            
        FOAF_NS+"currentProject",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"current-project", "current-project (foaf)",
            
        FOAF_NS+"Project",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"project", "project (foaf)",
            
        FOAF_NS+"page",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"page", "page (foaf)",
            
        FOAF_NS+"birthday",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"birthday", "birthday (foaf)",
            
        FOAF_NS+"openid",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"openid", "openid (foaf)",
            
        FOAF_NS+"jabberID",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"jabberid", "jabberid (foaf)",
            
        FOAF_NS+"aimChatID",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"aimchatid", "aimchatid (foaf)",
            
        FOAF_NS+"icqChatID",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"icqchatid", "icqchatid (foaf)",
            
        FOAF_NS+"dnaChecksum",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"dna-checksum", "dna-checksum (foaf)",
            
        FOAF_NS+"mbox_sha1sum",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"mailbox-uri", "mailbox-uri (foaf)",
            
        FOAF_NS+"mbox",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"mbox", "mbox (foaf)",
            
        FOAF_NS+"maker",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"maker", "maker (foaf)",
            
        FOAF_NS+"made",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"made", "made (foaf)",
            
        FOAF_NS+"logo",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"logo", "logo (foaf)",
            
        FOAF_NS+"interest",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"interest", "interest (foaf)",
            
        FOAF_NS+"img",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"img", "img (foaf)",

        FOAF_NS+"fundedBy",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"funded-by", "funded-by (foaf)",
            
        FOAF_NS+"depicts",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"depicts", "depicts (foaf)",
            
        FOAF_NS+"depiction",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"depiction", "depiction (foaf)",
            
        FOAF_NS+"based_near",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"based_near", "based_near (foaf)",
            
        FOAF_NS+"accountServiceHomepage",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"account-service-homepage", "account-service-homepage (foaf)",
            
        FOAF_NS+"accountName",
            FOAF_ROLE_NS+"person", "person (foaf)",
            FOAF_ROLE_NS+"account-name", "account-name (foaf)",
    };
    
    
    public String[] getRoleMappings() {
        return ASSOCIATION_TYPE_TO_ROLES_MAPPING;
    }
    public String[] getBasenameMappings() {
        return SI_BASENAME_MAPPING;
    }

}
