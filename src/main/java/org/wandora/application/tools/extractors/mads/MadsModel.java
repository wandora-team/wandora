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
 */


package org.wandora.application.tools.extractors.mads;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author akivela
 */
public class MadsModel {

    String version = null;
    String id = null;
    
    List<MadsAuthority> authorities = new ArrayList<MadsAuthority>();
    List<MadsRelated> related = new ArrayList<MadsRelated>();
    List<MadsVariant> variant = new ArrayList<MadsVariant>();

    List<MadsNote> notes = new ArrayList<MadsNote>();
    List<MadsAffiliation> affiliations = new ArrayList<MadsAffiliation>();
    List<MadsUrl> urls = new ArrayList<MadsUrl>();
    List<MadsIdentifier> identifiers = new ArrayList<MadsIdentifier>();
    List<MadsFieldOfActivity> fieldOfActivities = new ArrayList<MadsFieldOfActivity>();
    List<MadsExtension> extensions = new ArrayList<MadsExtension>();
    List<MadsRecordInfo> recordInfos = new ArrayList<MadsRecordInfo>();


    



    class MadsAuthority {
        String link = null;
        String language = null;
        String id = null;

        List<MadsTopic> topics = new ArrayList<MadsTopic>();
        List<MadsName> names = new ArrayList<MadsName>();
        List<MadsTitleInfo> titleInfos = new ArrayList<MadsTitleInfo>();
        List<MadsTemporal> temporals = new ArrayList<MadsTemporal>();
        List<MadsGenre> genres = new ArrayList<MadsGenre>();
        List<MadsGeographic> geographics = new ArrayList<MadsGeographic>();
        List<MadsHierarchicalGeographic> hgeographics = new ArrayList<MadsHierarchicalGeographic>();
        List<MadsOccupation> occupations = new ArrayList<MadsOccupation>();
    }


    class MadsRelated {
        String link = null;
        String language = null;
        String id = null;
        String type = null;

        List<MadsTopic> topics = new ArrayList<MadsTopic>();
        List<MadsName> names = new ArrayList<MadsName>();
        List<MadsTitleInfo> titleInfos = new ArrayList<MadsTitleInfo>();
        List<MadsTemporal> temporals = new ArrayList<MadsTemporal>();
        List<MadsGenre> genres = new ArrayList<MadsGenre>();
        List<MadsGeographic> geographics = new ArrayList<MadsGeographic>();
        List<MadsHierarchicalGeographic> hgeographics = new ArrayList<MadsHierarchicalGeographic>();
        List<MadsOccupation> occupations = new ArrayList<MadsOccupation>();
    }


    class MadsVariant {
        String link = null;
        String language = null;
        String id = null;
        String type = null;

        List<MadsTopic> topics = new ArrayList<MadsTopic>();
        List<MadsName> names = new ArrayList<MadsName>();
        List<MadsTitleInfo> titleInfos = new ArrayList<MadsTitleInfo>();
        List<MadsTemporal> temporals = new ArrayList<MadsTemporal>();
        List<MadsGenre> genres = new ArrayList<MadsGenre>();
        List<MadsGeographic> geographics = new ArrayList<MadsGeographic>();
        List<MadsHierarchicalGeographic> hgeographics = new ArrayList<MadsHierarchicalGeographic>();
        List<MadsOccupation> occupations = new ArrayList<MadsOccupation>();
    }


    // ------


    class MadsName {
        String type = null;
        String authority = null;
        
        List<MadsNamePart> nameParts = new ArrayList<MadsNamePart>();
        List<MadsDescription> descriptions = new ArrayList<MadsDescription>();
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

        List<MadsOrganization> organizations = new ArrayList<MadsOrganization>();
        List<MadsPosition> positions = new ArrayList<MadsPosition>();
        List<MadsAddress> addresses = new ArrayList<MadsAddress>();
        List<MadsEmail> emails = new ArrayList<MadsEmail>();
        List<MadsPhone> phones = new ArrayList<MadsPhone>();
        List<MadsFax> faxs = new ArrayList<MadsFax>();
        List<MadsHours> hours = new ArrayList<MadsHours>();
        List<MadsDateValid> dateValids = new ArrayList<MadsDateValid>();
    }


    class MadsOrganization {
        String value = null;
    }
    class MadsPosition {
        String value = null;
    }
    class MadsAddress {
        List<String> streets = new ArrayList<String>();
        List<String> cities = new ArrayList<String>();
        List<String> states = new ArrayList<String>();
        List<String> countries = new ArrayList<String>();
        List<String> postcodes = new ArrayList<String>();
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
