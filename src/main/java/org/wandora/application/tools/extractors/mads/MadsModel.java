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
    
    List<MadsAuthority> authorities = new ArrayList<>();
    List<MadsRelated> related = new ArrayList<>();
    List<MadsVariant> variant = new ArrayList<>();

    List<MadsNote> notes = new ArrayList<>();
    List<MadsAffiliation> affiliations = new ArrayList<>();
    List<MadsUrl> urls = new ArrayList<>();
    List<MadsIdentifier> identifiers = new ArrayList<>();
    List<MadsFieldOfActivity> fieldOfActivities = new ArrayList<>();
    List<MadsExtension> extensions = new ArrayList<>();
    List<MadsRecordInfo> recordInfos = new ArrayList<>();


    



    class MadsAuthority {
        String link = null;
        String language = null;
        String id = null;

        List<MadsTopic> topics = new ArrayList<>();
        List<MadsName> names = new ArrayList<>();
        List<MadsTitleInfo> titleInfos = new ArrayList<>();
        List<MadsTemporal> temporals = new ArrayList<>();
        List<MadsGenre> genres = new ArrayList<>();
        List<MadsGeographic> geographics = new ArrayList<>();
        List<MadsHierarchicalGeographic> hgeographics = new ArrayList<>();
        List<MadsOccupation> occupations = new ArrayList<>();
    }


    class MadsRelated {
        String link = null;
        String language = null;
        String id = null;
        String type = null;

        List<MadsTopic> topics = new ArrayList<>();
        List<MadsName> names = new ArrayList<>();
        List<MadsTitleInfo> titleInfos = new ArrayList<>();
        List<MadsTemporal> temporals = new ArrayList<>();
        List<MadsGenre> genres = new ArrayList<>();
        List<MadsGeographic> geographics = new ArrayList<>();
        List<MadsHierarchicalGeographic> hgeographics = new ArrayList<>();
        List<MadsOccupation> occupations = new ArrayList<>();
    }


    class MadsVariant {
        String link = null;
        String language = null;
        String id = null;
        String type = null;

        List<MadsTopic> topics = new ArrayList<>();
        List<MadsName> names = new ArrayList<>();
        List<MadsTitleInfo> titleInfos = new ArrayList<>();
        List<MadsTemporal> temporals = new ArrayList<>();
        List<MadsGenre> genres = new ArrayList<>();
        List<MadsGeographic> geographics = new ArrayList<>();
        List<MadsHierarchicalGeographic> hgeographics = new ArrayList<>();
        List<MadsOccupation> occupations = new ArrayList<>();
    }


    // ------


    class MadsName {
        String type = null;
        String authority = null;
        
        List<MadsNamePart> nameParts = new ArrayList<>();
        List<MadsDescription> descriptions = new ArrayList<>();
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

        List<MadsOrganization> organizations = new ArrayList<>();
        List<MadsPosition> positions = new ArrayList<>();
        List<MadsAddress> addresses = new ArrayList<>();
        List<MadsEmail> emails = new ArrayList<>();
        List<MadsPhone> phones = new ArrayList<>();
        List<MadsFax> faxs = new ArrayList<>();
        List<MadsHours> hours = new ArrayList<>();
        List<MadsDateValid> dateValids = new ArrayList<>();
    }


    class MadsOrganization {
        String value = null;
    }
    class MadsPosition {
        String value = null;
    }
    class MadsAddress {
        List<String> streets = new ArrayList<>();
        List<String> cities = new ArrayList<>();
        List<String> states = new ArrayList<>();
        List<String> countries = new ArrayList<>();
        List<String> postcodes = new ArrayList<>();
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
