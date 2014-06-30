/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
 */


package org.wandora.application.tools.extractors.mads;

import java.util.ArrayList;

/**
 *
 * @author akivela
 */
public class MadsModel {

    String version = null;
    String id = null;
    
    ArrayList<MadsAuthority> authorities = new ArrayList<MadsAuthority>();
    ArrayList<MadsRelated> related = new ArrayList<MadsRelated>();
    ArrayList<MadsVariant> variant = new ArrayList<MadsVariant>();

    ArrayList<MadsNote> notes = new ArrayList<MadsNote>();
    ArrayList<MadsAffiliation> affiliations = new ArrayList<MadsAffiliation>();
    ArrayList<MadsUrl> urls = new ArrayList<MadsUrl>();
    ArrayList<MadsIdentifier> identifiers = new ArrayList<MadsIdentifier>();
    ArrayList<MadsFieldOfActivity> fieldOfActivities = new ArrayList<MadsFieldOfActivity>();
    ArrayList<MadsExtension> extensions = new ArrayList<MadsExtension>();
    ArrayList<MadsRecordInfo> recordInfos = new ArrayList<MadsRecordInfo>();


    



    class MadsAuthority {
        String link = null;
        String language = null;
        String id = null;

        ArrayList<MadsTopic> topics = new ArrayList<MadsTopic>();
        ArrayList<MadsName> names = new ArrayList<MadsName>();
        ArrayList<MadsTitleInfo> titleInfos = new ArrayList<MadsTitleInfo>();
        ArrayList<MadsTemporal> temporals = new ArrayList<MadsTemporal>();
        ArrayList<MadsGenre> genres = new ArrayList<MadsGenre>();
        ArrayList<MadsGeographic> geographics = new ArrayList<MadsGeographic>();
        ArrayList<MadsHierarchicalGeographic> hgeographics = new ArrayList<MadsHierarchicalGeographic>();
        ArrayList<MadsOccupation> occupations = new ArrayList<MadsOccupation>();
    }


    class MadsRelated {
        String link = null;
        String language = null;
        String id = null;
        String type = null;

        ArrayList<MadsTopic> topics = new ArrayList<MadsTopic>();
        ArrayList<MadsName> names = new ArrayList<MadsName>();
        ArrayList<MadsTitleInfo> titleInfos = new ArrayList<MadsTitleInfo>();
        ArrayList<MadsTemporal> temporals = new ArrayList<MadsTemporal>();
        ArrayList<MadsGenre> genres = new ArrayList<MadsGenre>();
        ArrayList<MadsGeographic> geographics = new ArrayList<MadsGeographic>();
        ArrayList<MadsHierarchicalGeographic> hgeographics = new ArrayList<MadsHierarchicalGeographic>();
        ArrayList<MadsOccupation> occupations = new ArrayList<MadsOccupation>();
    }


    class MadsVariant {
        String link = null;
        String language = null;
        String id = null;
        String type = null;

        ArrayList<MadsTopic> topics = new ArrayList<MadsTopic>();
        ArrayList<MadsName> names = new ArrayList<MadsName>();
        ArrayList<MadsTitleInfo> titleInfos = new ArrayList<MadsTitleInfo>();
        ArrayList<MadsTemporal> temporals = new ArrayList<MadsTemporal>();
        ArrayList<MadsGenre> genres = new ArrayList<MadsGenre>();
        ArrayList<MadsGeographic> geographics = new ArrayList<MadsGeographic>();
        ArrayList<MadsHierarchicalGeographic> hgeographics = new ArrayList<MadsHierarchicalGeographic>();
        ArrayList<MadsOccupation> occupations = new ArrayList<MadsOccupation>();
    }


    // ------


    class MadsName {
        String type = null;
        String authority = null;
        
        ArrayList<MadsNamePart> nameParts = new ArrayList<MadsNamePart>();
        ArrayList<MadsDescription> descriptions = new ArrayList<MadsDescription>();
    }


    class MadsNamePart {
        String language = null;
        String type = null;
        String value = null;
    }


    class MadsDescription {
        String language = null;
        String type = null;
        String value = null;

    }

    class MadsTopic {
        String authority = null;
        String value = null;
    }
    
    class MadsGenre {
        String authority = null;
        String value = null;
    }
    
    class MadsGeographic {
        String authority = null;
        String value = null;
    }

    class MadsHierarchicalGeographic {
        String authority = null;
        String geographic = null;
        MadsHierarchicalGeographic next = null;
    }

    class MadsOccupation {
        String authority = null;
        String value = null;
    }

    class MadsTemporal {
        String authority = null;
        String value = null;
    }

    class MadsTitleInfo {
        String type = null;
        String authority = null;
        String value = null;
    }




    class MadsExtension {
        String displayLabel = null;
        String value = null;
    }

    class MadsRecordInfo {
        String lang = null;
        String script = null;
        String transliteration = null;
        
        String displayLabel = null;
        String value = null;
    }

    class MadsIdentifier {
        String type = null;
        String displayLabel = null;
        String invalid = null;
        String altRepGroup = null;

        String value = null;
    }

    class MadsUrl {
        String dateLastAccessed = null;
        String displayLabel = null;
        String note = null;
        String access = null;
        String usage = null;
        
        String value = null;
    }

    class MadsNote {
        String language = null;
        String value = null;
    }

    class MadsAffiliation {
        String language = null;
        String value = null;

        ArrayList<MadsOrganization> organizations = new ArrayList<MadsOrganization>();
        ArrayList<MadsPosition> positions = new ArrayList<MadsPosition>();
        ArrayList<MadsAddress> addresses = new ArrayList<MadsAddress>();
        ArrayList<MadsEmail> emails = new ArrayList<MadsEmail>();
        ArrayList<MadsPhone> phones = new ArrayList<MadsPhone>();
        ArrayList<MadsFax> faxs = new ArrayList<MadsFax>();
        ArrayList<MadsHours> hours = new ArrayList<MadsHours>();
        ArrayList<MadsDateValid> dateValids = new ArrayList<MadsDateValid>();
    }


    class MadsOrganization {
        String value = null;
    }
    class MadsPosition {
        String value = null;
    }
    class MadsAddress {
        ArrayList<String> streets = new ArrayList<String>();
        ArrayList<String> cities = new ArrayList<String>();
        ArrayList<String> states = new ArrayList<String>();
        ArrayList<String> countries = new ArrayList<String>();
        ArrayList<String> postcodes = new ArrayList<String>();
        String value = null;
    }

    class MadsEmail {
        String value = null;
    }
    class MadsPhone {
        String value = null;
    }
    class MadsFax {
        String value = null;
    }
    class MadsHours {
        String value = null;
    }
    class MadsDateValid {
        String authority = null;
        String value = null;
    }

    class MadsFieldOfActivity {
        String value = null;
    }

}
