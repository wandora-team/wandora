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
 * MarcField.java
 *
 * Created on 2010-06-30
 *
 */



package org.wandora.application.tools.extractors.marcxml;



import java.util.*;


/**
 *
 * @author akivela
 */
public class MarcField {



    private String tag = null;
    private String ind1 = null;
    private String ind2 = null;
    private ArrayList<MarcSubfield> subfields = null;


    public MarcField(String tag, String ind1, String ind2) {
        this.tag = tag;
        this.ind1 = ind1;
        this.ind2 = ind2;

        subfields = new ArrayList<MarcSubfield>();
    }



    public void addSubfield(String code, String value) {
        subfields.add(new MarcSubfield(code, value));
    }


    public String getTag() {
        return tag;
    }
    public String getInd1() {
        return ind1;
    }
    public String getInd2() {
        return ind2;
    }

    public Collection<MarcSubfield> getSubfields() {
        return subfields;
    }



    // -------------------------------------------------------------------------



    private static final String [] fieldNames = new String[] {
        "001", "001 - Control Number (NR)",
        "003", "003 - Control Number Identifier (NR)",
        "005", "005 - Date and Time of Latest Transaction (NR)",
        "006", "006 - Fixed-Length Data Elements-Additional Material Characteristics",
        "007", "007 - Physical Description Fixed Field-General Information (R)",
        "008", "008 - Fixed-Length Data Elements-General Information (NR)",

        "010", "010 - Library of Congress Control Number (NR)",
        "013", "013 - Patent Control Information (R)",
        "015", "015 - National Bibliography Number (R)",
        "016", "016 - National Bibliographic Agency Control Number (R)",
        "017", "017 - Copyright or Legal Deposit Number (R)",
        "018", "018 - Copyright Article-Fee Code (NR)",

        "020", "020 - International Standard Book Number (R)",
        "022", "022 - International Standard Serial Number (R)",
        "024", "024 - Other Standard Identifier (R)",
        "025", "025 - Overseas Acquisition Number (R)",
        "026", "026 - Fingerprint Identifier (R)",
        "027", "027 - Standard Technical Report Number (R)",
        "028", "028 - Publisher Number (R)",

        "030", "030 - CODEN Designation (R)",
        "031", "031 - Musical Incipits Information (R)",
        "032", "032 - Postal Registration Number (R)",
        "033", "033 - Date/Time and Place of an Event (R)",
        "034", "034 - Coded Cartographic Mathematical Data (R)",
        "035", "035 - System Control Number (R)",
        "036", "036 - Original Study Number for Computer Data Files (NR)",
        "037", "037 - Source of Acquisition (R)",
        "038", "038 - Record Content Licensor (NR)",

        "040", "040 - Cataloging Source (NR)",
        "041", "041 - Language Code (R)",
        "042", "042 - Authentication Code (NR)",
        "043", "043 - Geographic Area Code (NR)",
        "044", "044 - Country of Publishing/Producing Entity Code (NR)",
        "045", "045 - Time Period of Content (NR)",
        "046", "046 - Special Coded Dates (R)",
        "047", "047 - Form of Musical Composition Code (R)",
        "048", "048 - Number of Musical Instruments or Voices Code (R)",

        "050", "050 - Library of Congress Call Number (R)",
        "051", "051 - Library of Congress Copy, Issue, Offprint Statement (R)",
        "052", "052 - Geographic Classification (R)",
        "055", "055 - Classification Numbers Assigned in Canada (R)",

        "060", "060 - National Library of Medicine Call Number (R)",
        "061", "061 - National Library of Medicine Copy Statement (R)",
        "066", "066 - Character Sets Present (NR)",

        "070", "070 - National Agricultural Library Call Number (R)",
        "071", "071 - National Agricultural Library Copy Statement (R)",
        "072", "072 - Subject Category Code (R)",
        "074", "074 - GPO Item Number (R)",

        "080", "080 - Universal Decimal Classification Number (R)",
        "082", "082 - Dewey Decimal Classification Number (R)",
        "083", "083 - Additional Dewey Decimal Classification Number (R)",
        "084", "084 - Other Classification Number (R)",
        "085", "085 - Synthesized Classification Number Components (R)",
        "086", "086 - Government Document Classification Number (R)",
        "088", "088 - Report Number (R)",

        "100", "100 - Main Entry-Personal Name (NR)",
        "110", "110 - Main Entry-Corporate Name (NR)",
        "111", "111 - Main Entry-Meeting Name (NR)",

        "130", "130 - Main Entry-Uniform Title (NR)",

        "210", "210 - Abbreviated Title (R)",
        "222", "222 - Key Title (R)",

        "245", "245 - Title Statement (NR)",
        "246", "246 - Varying Form of Title (R)",
        "247", "247 - Former Title (R)",

        "250", "250 - Edition Statement (NR)",
        "254", "254 - Musical Presentation Statement (NR)",
        "255", "255 - Cartographic Mathematical Data (R)",
        "256", "256 - Computer File Characteristics (NR)",
        "257", "257 - Country of Producing Entity (R)",
        "258", "258 - Philatelic Issue Data (R)",

        "260", "260 - Publication, Distribution, etc. (Imprint) (R)",
        "261", "261 - Imprint Statement for Films (Pre-AACR 1 Revised) (NR)",
        "262", "262 - Imprint Statement for Sound Recordings (Pre-AACR 1) (NR)",
        "263", "263 - Projected Publication Date (NR)",

        "270", "270 - Address (R)",

        "300", "300 - Physical Description (R)",
        "306", "306 - Playing Time (NR)",
        "307", "307 - Hours, Etc. (R)",
        "310", "310 - Current Publication Frequency (NR)",
        "321", "321 - Former Publication Frequency (R)",
        "336", "336 - Content Type (R)",
        "337", "337 - Media Type (R)",
        "338", "338 - Carrier Type (R)",
        "340", "340 - Physical Medium (R)",
        "342", "342 - Geospatial Reference Data (R)",
        "343", "343 - Planar Coordinate Data (R)",
        "351", "351 - Organization and Arrangement of Materials (R)",
        "352", "352 - Digital Graphic Representation (R)",
        "355", "355 - Security Classification Control (R)",
        "357", "357 - Originator Dissemination Control (NR)",
        "362", "362 - Dates of Publication and/or Sequential Designation (R)",
        "363", "363 - Normalized Date and Sequential Designation (R)",
        "365", "365 - Trade Price (R)",
        "366", "366 - Trade Availability Information (R)",
        "380", "380 - Form of Work (R)",
        "381", "381 - Other Distinguishing Characteristics of Work or Expression (R)",
        "382", "382 - Medium of Performance (R)",
        "383", "383 - Numeric Designation of Musical Work (R)",
        "384", "384 - Key (NR)",

        "400", "400 - Series Statement/Added Entry-Personal Name (R)",
        "410", "410 - Series Statement/Added Entry-Corporate Name (R)",
        "440", "440 - Series Statement/Added Entry-Title (R)",
        "490", "490 - Series Statement (R)",

        "500", "500 - General Note (R)",
        "501", "501 - With Note (R)",
        "502", "502 - Dissertation Note (R)",
        "504", "504 - Bibliography, Etc. Note (R)",
        "505", "505 - Formatted Contents Note (R)",
        "506", "506 - Restrictions on Access Note (R)",
        "507", "507 - Scale Note for Graphic Material (NR)",
        "508", "508 - Creation/Production Credits Note (R)",
        "510", "510 - Citation/References Note (R)",
        "511", "511 - Participant or Performer Note (R)",
        "513", "513 - Type of Report and Period Covered Note (R)",
        "514", "514 - Data Quality Note (NR)",
        "515", "515 - Numbering Peculiarities Note (R)",
        "516", "516 - Type of Computer File or Data Note (R)",
        "518", "518 - Date/Time and Place of an Event Note (R)",

        "520", "520 - Summary, Etc. (R)",

        "521", "521 - Target Audience Note (R)",
        "522", "522 - Geographic Coverage Note (R)",
        "524", "524 - Preferred Citation of Described Materials Note (R)",
        "525", "525 - Supplement Note (R)",
        "526", "526 - Study Program Information Note (R)",
        "530", "530 - Additional Physical Form Available Note (R)",
        "533", "533 - Reproduction Note (R)",
        "534", "534 - Original Version Note (R)",
        "535", "535 - Location of Originals/Duplicates Note (R)",
        "536", "536 - Funding Information Note (R)",
        "538", "538 - System Details Note (R)",
        "540", "540 - Terms Governing Use and Reproduction Note (R)",
        "541", "541 - Immediate Source of Acquisition Note (R)",
        "542", "542 - Information Relating to Copyright Status (R)",
        "544", "544 - Location of Other Archival Materials Note (R)",
        "545", "545 - Biographical or Historical Data (R)",
        "546", "546 - Language Note (R)",
        "547", "547 - Former Title Complexity Note (R)",
        "550", "550 - Issuing Body Note (R)",
        "552", "552 - Entity and Attribute Information Note (R)",
        "555", "555 - Cumulative Index/Finding Aids Note (R)",
        "556", "556 - Information About Documentation Note (R)",
        "561", "561 - Ownership and Custodial History (R)",
        "562", "562 - Copy and Version Identification Note (R)",
        "563", "563 - Binding Information (R)",
        "565", "565 - Case File Characteristics Note (R)",
        "567", "567 - Methodology Note (R)",
        "580", "580 - Linking Entry Complexity Note (R)",
        "581", "581 - Publications About Described Materials Note (R)",
        "583", "583 - Action Note (R)",
        "584", "584 - Accumulation and Frequency of Use Note (R)",

        "600", "600 - Subject Added Entry-Personal Name (R)",
        "610", "610 - Subject Added Entry-Corporate Name (R)",
        "611", "611 - Subject Added Entry-Meeting Name (R)",
        "630", "630 - Subject Added Entry-Uniform Title (R)",
        "648", "648 - Subject Added Entry-Chronological Term (R)",
        "650", "650 - Subject Added Entry-Topical Term (R)",
        "651", "651 - Subject Added Entry-Geographic Name (R)",
        "653", "653 - Index Term-Uncontrolled (R)",
        "654", "654 - Subject Added Entry-Faceted Topical Terms (R)",
        "655", "655 - Index Term-Genre/Form (R)",
        "656", "656 - Index Term-Occupation (R)",
        "657", "657 - Index Term-Function (R)",
        "658", "658 - Index Term-Curriculum Objective (R)",
        "662", "662 - Subject Added Entry-Hierarchical Place Name (R)",

        "700", "700 - Added Entry-Personal Name (R)",
        "710", "710 - Added Entry-Corporate Name (R)",
        "711", "711 - Added Entry-Meeting Name (R)",
        "720", "720 - Added Entry-Uncontrolled Name (R)",
        "730", "730 - Added Entry-Uniform Title (R)",
        "740", "740 - Added Entry-Uncontrolled Related/Analytical Title (R)",
        "751", "751 - Added Entry-Geographic Name (R)",
        "752", "752 - Added Entry-Hierarchical Place Name (R)",
        "753", "753 - System Details Access to Computer Files (R)",
        "754", "754 - Added Entry-Taxonomic Identification (R)",

        "760", "760 - Main Series Entry (R)",
        "762", "762 - Subseries Entry (R)",
        "765", "765 - Original Language Entry (R)",
        "767", "767 - Translation Entry (R)",
        "770", "770 - Supplement/Special Issue Entry (R)",
        "772", "772 - Supplement Parent Entry (R)",
        "773", "773 - Host Item Entry (R)",
        "774", "774 - Constituent Unit Entry (R)",
        "775", "775 - Other Edition Entry (R)",
        "776", "776 - Additional Physical Form Entry (R)",
        "777", "777 - Issued With Entry (R)",
        "780", "780 - Preceding Entry (R)",
        "785", "785 - Succeeding Entry (R)",
        "786", "786 - Data Source Entry (R)",
        "787", "787 - Other Relationship Entry (R)",

        "800", "800 - Series Added Entry-Personal Name (R)",
        "810", "810 - Series Added Entry-Corporate Name (R)",
        "811", "811 - Series Added Entry-Meeting Name (R)",
        "830", "830 - Series Added Entry-Uniform Title (R)",

        "850", "850 - Holding Institution (R)",
        "852", "852 - Location (R)",
        "856", "856 - Electronic Location and Access (R)",
        "880", "880 - Alternate Graphic Representation (R)",
        "882", "882 - Replacement Record Information (NR)",
        "886", "886 - Foreign MARC Information Field (R)",
        "887", "887 - Non-MARC Information Field (R)",
    };
    private static HashMap<String,String> fieldNameHash = null;


    public static String getFieldName(String field) {
        if(fieldNameHash == null) {
            fieldNameHash = new HashMap<String,String>();
            for(int i=0; i<fieldNames.length; i=i+2) {
                fieldNameHash.put(fieldNames[i], fieldNames[i+1]);
            }
        }
        if(fieldNameHash.containsKey(field)) {
            return fieldNameHash.get(field);
        }
        return field;
    }










    private static Object[] fieldIndicatorNames = new Object[] {
        "016",
            "National bibliographic agency",
                new String[] { // First
                    "7", "Source specified in subfield $2",
                },
            "Undefined",
                new String[] { // Second
                },
        "017",
            "Undefined",
                new String[] { // First
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated",
                },
        "022",
            "Level of international interest",
                new String[] { // First
                    "0", "Continuing resource of international interest",
                    "1", "Continuing resource not of international interest"
                },
            "Undefined",
                new String[] { // Second
                },
        "024",
            "Type of standard number or code",
                new String[] { // First
                    "0", "International Standard Recording Code",
                    "1", "Universal Product Code",
                    "2", "International Standard Music Number",
                    "3", "International Article Number",
                    "4", "Serial Item and Contribution Identifier",
                    "7", "Source specified in subfield $2",
                    "8", "Unspecified type of standard number or code"
                },
            "Difference indicator",
                new String[] { // Second
                    "0", "No difference",
                    "1", "Difference"
                },
        "028",
            "Type of publisher number",
                new String[] { // First
                    "0", "Issue number",
                    "1", "Matrix number",
                    "2", "Plate number",
                    "3", "Other music number",
                    "4", "Videorecording number",
                    "5", "Other publisher number",
                },
            "Note/added entry controller",
                new String[] { // Second
                    "0", "No note, no added entry",
                    "1", "Note, added entry",
                    "2", "Note, no added entry",
                    "3", "No note, added entry"
                },
        "033",
            "Type of date in subfield $a",
                new String[] { // First
                    "0", "Single date",
                    "1", "Multiple single dates",
                    "2", "Range of dates",
                },
            "Type of event",
                new String[] { // Second
                    "0", "Capture",
                    "1", "Broadcast",
                    "2", "Finding",
                },
        "034",
            "Type of scale",
                new String[] { // First
                    "0", "Scale indeterminable/No scale recorded",
                    "1", "Single scale",
                    "3", "Range of scales",
                },
            "Type of ring",
                new String[] { // Second
                    "0", "Outer ring",
                    "1", "Exclusion ring",
                },
        "041",
            "Translation indication",
                new String[] { // First
                    "0", "Item not a translation/does not include a translation",
                    "1", "Item is or includes a translation",
                },
            "Source of code",
                new String[] { // Second
                    "7", "Source specified in subfield $2",
                },
        "045",
            "Type of time period in subfield $b or $c",
                new String[] { // First
                    "0", "Single date/time",
                    "1", "Multiple single dates/times",
                    "2", "Range of dates/times"
                },
            "Undefined",
                new String[] { // Second
                },
        "047",
            "Undefined",
                new String[] { // First
                },
            "Source of code",
                new String[] { // Second
                    "7", "Source specified in subfield $2"
                },
        "048",
            "Undefined",
                new String[] { // First
                },
            "Source of code",
                new String[] { // Second
                    "7", "Source specified in subfield $2"
                },
        "050",
            "Existence in LC collection",
                new String[] { // First
                    "0", "Item is in LC",
                    "1", "Item is not in LC"
                },
            "Source of call number",
                new String[] { // Second
                    "0", "Assigned by LC",
                    "4", "Assigned by agency other than LC"
                },
        "052",
            "Code source",
                new String[] { // First
                    "1", "U.S. Dept. of Defense Classification",
                    "7", "Source specified in subfield $2"
                },
            "Undefined",
                new String[] { // Second
                },
        "055",
            "Existence in LAC collection",
                new String[] { // First
                    "0", "Work held by LAC",
                    "1", "Work not held by LAC"
                },
            "Type, completeness, source of class/call number",
                new String[] { // Second
                    "0", "LC-based call number assigned by LAC",
                    "1", "Complete LC class number assigned by LAC",
                    "2", "Incomplete LC class number assigned by LAC",
                    "3", "LC-based call number assigned by the contributing library",
                    "4", "Complete LC class number assigned by the contributing library",
                    "5", "Incomplete LC class number assigned by the contributing library",
                    "6", "Other call number assigned by LAC",
                    "7", "Other class number assigned by LAC",
                    "8", "Other call number assigned by the contributing library",
                    "9", "Other class number assigned by the contributing library"
                },
        "060",
            "Existence in NLM collection",
                new String[] { // First
                    "0", "Item is in NLM",
                    "1", "Item is not in NLM"
                },
            "Source of call number",
                new String[] { // Second
                    "0", "Assigned by NLM",
                    "4", "Assigned by agency other than NLM"
                },
        "070",
            "Existence in NAL collection",
                new String[] { // First
                    "0", "Item is in NAL",
                    "1", "Item is not in NAL"
                },
            "Undefined",
                new String[] { // Second
                },
        "072",
            "Undefined",
                new String[] { // First
                },
            "Code source",
                new String[] { // Second
                    "0", "NAL subject category code list",
                    "7", "Source specified in subfield $2"
                },
        "080",
            "Type of edition",
                new String[] { // First
                    "0", "Full",
                    "1", "Abridged"
                },
            "Undefined",
                new String[] { // Second
                },
        "082",
            "Type of edition",
                new String[] { // First
                    "0", "Full edition",
                    "1", "Abridged edition"
                },
            "Source of classification number",
                new String[] { // Second
                    "0", "Assigned by LC",
                    "4", "Assigned by agency other than LC"
                },
        "083",
            "Type of edition",
                new String[] { // First
                    "0", "Full edition",
                    "1", "Abridged edition"
                },
            "Undefined",
                new String[] { // Second
                },
        "086",
            "Number source",
                new String[] { // First
                    "0", "Superintendent of Documents Classification System",
                    "1", "Government of Canada Publications: Outline of Classification"
                },
            "Undefined",
                new String[] { // Second
                },
        "100",
            "Type of personal name entry element",
                new String[] { // First
                    "0", "Forename",
                    "1", "Surname",
                    "3", "Family name"
                },
            "Undefined",
                new String[] { // Second
                },
        "110",
            "Type of corporate name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "3", "Name in direct order"
                },
            "Undefined",
                new String[] { // Second
                },
        "111",
            "Type of meeting name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "3", "Name in direct order"
                },
            "Undefined",
                new String[] { // Second
                },
        "210",
            "Title added entry",
                new String[] { // First
                    "0", "No added entry",
                    "1", "Added entry",
                },
            "Type",
                new String[] { // Second
                    "0", "Other abbreviated title"
                },
        "222",
            "Undefined",
                new String[] { // First
                },
            "Nonfiling characters",
                new String[] { // Second
                    "0", "No nonfiling characters",
                },
        "240",
            "Uniform title printed or displayed",
                new String[] { // First
                    "0", "Not printed or displayed",
                    "1", "Printed or displayed"
                },
            "Nonfiling characters",
                new String[] { // Second
                },
        "242",
            "Title added entry",
                new String[] { // First
                    "0", "No added entry",
                    "1", "Added entry"
                },
            "Nonfiling characters",
                new String[] { // Second
                    "0", "No nonfiling characters"
                },
        "243",
            "Uniform title printed or displayed",
                new String[] { // First
                    "0", "Not printed or displayed",
                    "1", "Printed or displayed"
                },
            "Nonfiling characters",
                new String[] { // Second
                },
        "245",
            "Title added entry",
                new String[] { // First
                    "0", "No added entry",
                    "1", "Added entry"
                },
            "Nonfiling characters",
                new String[] { // Second
                    "0", "No nonfiling characters"
                },
        "246",
            "Note/added entry controller",
                new String[] { // First
                    "0", "Note, no added entry",
                    "1", "Note, added entry",
                    "2", "No note, no added entry",
                    "3", "No note, added entry"
                },
            "Type of title",
                new String[] { // Second
                    "0", "Portion of title",
                    "1", "Parallel title",
                    "2", "Distinctive title",
                    "3", "Other title",
                    "4", "Cover title",
                    "5", "Added title page title",
                    "6", "Caption title",
                    "7", "Running title",
                    "8", "Spine title"
                },
        "247",
            "Title added entry",
                new String[] { // First
                    "0", "No added entry",
                    "1", "Added entry",
                },
            "Note controller",
                new String[] { // Second
                    "0", "Display note",
                    "1", "Do not display note",
                },
        "260",
            "Sequence of publishing statements",
                new String[] { // First
                    "2", "Intervening publisher",
                    "3", "Current/latest publisher",
                },
            "Undefined",
                new String[] { // Second
                },
        "270",
            "Level",
                new String[] { // First
                    "1", "Primary",
                    "3", "Secondary",
                },
            "Type of address",
                new String[] { // Second
                    "0", "Mailing",
                    "7", "Type specified in subfield $i"
                },
        "307",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated",
                },
            "Undefined",
                new String[] { // Second
                },
        "342",
            "Geospatial reference dimension",
                new String[] { // First
                    "0", "Horizontal coordinate system",
                    "1", "Vertical coordinate system",
                },
            "Geospatial reference method",
                new String[] { // Second
                    "0", "Geographic",
                    "1", "Map projection",
                    "2", "Grid coordinate system",
                    "3", "Local planar",
                    "4", "Local",
                    "5", "Geodetic model",
                    "6", "Altitude",
                    "7", "Method specified in $2",
                    "8", "Depth"
                },
        "355",
            "Controlled element",
                new String[] { // First
                    "0", "Document",
                    "1", "Title",
                    "2", "Abstract",
                    "3", "Contents note",
                    "4", "Author",
                    "5", "Record",
                    "8", "None of the above"
                },
            "Undefined",
                new String[] { // Second
                },
        "362",
            "Format of date",
                new String[] { // First
                    "0", "Formatted style",
                    "1", "Unformatted note",
                },
            "Undefined",
                new String[] { // Second
                },
        "363",
            "Start/End designator",
                new String[] { // First
                    "0", "Starting information",
                    "1", "Ending information",
                },
            "State of issuance",
                new String[] { // Second
                    "0", "Closed",
                    "1", "Open",
                },
        "384",
            "Key type",
                new String[] { // First
                    "0", "Original key",
                    "1", "Transposed key",
                },
            "Undefined",
                new String[] { // Second
                },
        "490",
            "Series tracing policy",
                new String[] { // First
                    "0", "Series not traced",
                    "1", "Series traced",
                },
            "Undefined",
                new String[] { // Second
                },
        "505",
            "Display constant controller",
                new String[] { // First
                    "0", "Contents",
                    "1", "Incomplete contents",
                    "2", "Partial contents",
                    "8", "No display constant generated"
                },
            "Level of content designation",
                new String[] { // Second
                    "0", "Enhanced"
                },
        "506",
            "Restriction",
                new String[] { // First
                    "0", "No restrictions",
                    "1", "Restrictions apply",
                },
            "Undefined",
                new String[] { // Second
                },
        "510",
            "Coverage/location in source",
                new String[] { // First
                    "0", "Coverage unknown",
                    "1", "Coverage complete",
                    "2", "Coverage is selective",
                    "3", "Location in source not given",
                    "4", "Location in source given"
                },
            "Undefined",
                new String[] { // Second
                },
        "511",
            "Display constant controller",
                new String[] { // First
                    "0", "No display constant generated",
                    "1", "Cast",
                },
            "Undefined",
                new String[] { // Second
                },
        "516",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated",
                },
            "Undefined",
                new String[] { // Second
                },
        "520",
            "Display constant controller",
                new String[] { // First
                    "0", "Subject",
                    "1", "Review",
                    "2", "Scope and content",
                    "3", "Abstract",
                    "4", "Content advice",
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "521",
            "Display constant controller",
                new String[] { // First
                    "0", "Reading grade level",
                    "1", "Interest age level",
                    "2", "Interest grade level",
                    "3", "Special audience characteristics",
                    "4", "Motivation/interest level",
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "522",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "524",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "526",
            "Display constant controller",
                new String[] { // First
                    "0", "Reading program",
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "535",
            "Display constant controller",
                new String[] { // First
                    "1", "Holder of originals",
                    "2", "Holder of duplicates"
                },
            "Undefined",
                new String[] { // Second
                },
        "541",
            "Privacy",
                new String[] { // First
                    "0", "Private",
                    "1", "Not private"
                },
            "Undefined",
                new String[] { // Second
                },
        "542",
            "Privacy",
                new String[] { // First
                    "0", "Private",
                    "1", "Not private"
                },
            "Undefined",
                new String[] { // Second
                },
        "544",
            "Relationship",
                new String[] { // First
                    "0", "Associated materials",
                    "1", "Related materials"
                },
            "Undefined",
                new String[] { // Second
                },
        "545",
            "Type of data",
                new String[] { // First
                    "0", "Biographical sketch",
                    "1", "Administrative history"
                },
            "Undefined",
                new String[] { // Second
                },
        "555",
            "Display constant controller",
                new String[] { // First
                    "0", "Finding aids",
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "556",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "561",
            "Privacy",
                new String[] { // First
                    "0", "Private",
                    "1", "Not private"
                },
            "Undefined",
                new String[] { // Second
                },
        "565",
            "Display constant controller",
                new String[] { // First
                    "0", "Case file characteristics",
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "567",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "581",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },
        "583",
            "Privacy",
                new String[] { // First
                    "0", "Private",
                    "1", "Not private"
                },
            "Undefined",
                new String[] { // Second
                },
        "586",
            "Display constant controller",
                new String[] { // First
                    "8", "No display constant generated"
                },
            "Undefined",
                new String[] { // Second
                },

        "600",
            "Type of personal name entry element",
                new String[] { // First
                    "0", "Forename",
                    "1", "Surname",
                    "3", "Family name"
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "610",
            "Type of corporate name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "2", "Name in direct order"
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "611",
            "Type of meeting name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "2", "Name in direct order"
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "630",
            "Nonfiling characters",
                new String[] { // First
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "648",
            "Undefined",
                new String[] { // First
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "650",
            "Level of subject",
                new String[] { // First
                    "0", "No level specified",
                    "1", "Primary",
                    "2", "Secondary"
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "651",
            "Undefined",
                new String[] { // First
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "653",
            "Level of index term",
                new String[] { // First
                    "0", "No level specified",
                    "1", "Primary",
                    "2", "Secondary"
                },
            "Type of term or name",
                new String[] { // Second
                    "0", "Topical term",
                    "1", "Personal name",
                    "2", "Corporate name",
                    "3", "Meeting name",
                    "4", "Chronological term",
                    "5", "Geographic name",
                    "6", "Genre/form term",
                },
        "654",
            "Level of subject",
                new String[] { // First
                    "0", "No level specified",
                    "1", "Primary",
                    "2", "Secondary"
                },
            "Undefined",
                new String[] { // Second
                },
        "655",
            "Type of heading",
                new String[] { // First
                    "0", "Faceted"
                },
            "Thesaurus",
                new String[] { // Second
                    "0", "Library of Congress Subject Headings",
                    "1", "LC subject headings for children's literature",
                    "2", "Medical Subject Headings",
                    "3", "National Agricultural Library subject authority file",
                    "4", "Source not specified",
                    "5", "Canadian Subject Headings",
                    "6", "Répertoire de vedettes-matière",
                    "7", "Source specified in subfield $2"
                },
        "656",
            "Undefined",
                new String[] { // First
                },
            "Source of term",
                new String[] { // Second
                    "7", "Source specified in subfield $2"
                },
        "657",
            "Undefined",
                new String[] { // First
                },
            "Source of term",
                new String[] { // Second
                    "7", "Source specified in subfield $2"
                },
        "700",
            "Type of personal name entry element",
                new String[] { // First
                    "0", "Forename",
                    "1", "Surname",
                    "3", "Family name"
                },
            "Type of added entry",
                new String[] { // Second
                    "2", "Analytical entry"
                },
        "710",
            "Type of corporate name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "2", "Name in direct order"
                },
            "Type of added entry",
                new String[] { // Second
                    "2", "Analytical entry"
                },
        "711",
            "Type of meeting name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "2", "Name in direct order"
                },
            "Type of added entry",
                new String[] { // Second
                    "2", "Analytical entry"
                },
        "720",
            "Type of name",
                new String[] { // First
                    "1", "Personal",
                    "2", "Other"
                },
            "Undefined",
                new String[] { // Second
                },
        "730",
            "Nonfiling characters",
                new String[] { // First
                },
            "Type of added entry",
                new String[] { // Second
                    "2", "Analytical entry"
                },
        "740",
            "Nonfiling characters",
                new String[] { // First
                    "0", "No nonfiling characters"
                },
            "Type of added entry",
                new String[] { // Second
                    "2", "Analytical entry"
                },
        "760",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "762",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "765",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "767",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "770",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "772",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "0", "Parent",
                    "8", "No display constant generated"
                },
        "773",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "774",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "775",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "776",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "777",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "780",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Type of relationship",
                new String[] { // Second
                    "0", "Continues",
                    "1", "Continues in part",
                    "2", "Supersedes",
                    "3", "Supersedes in part",
                    "4", "Formed by the union of ... and ...",
                    "5", "Absorbed",
                    "6", "Absorbed in part",
                    "7", "Separated from"
                },
        "785",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Type of relationship",
                new String[] { // Second
                    "0", "Continued by",
                    "1", "Continued in part by",
                    "2", "Superseded by",
                    "3", "Superseded in part by",
                    "4", "Absorbed by",
                    "5", "Absorbed in part by",
                    "6", "Split into ... and ...",
                    "7", "Merged with ... to form ...",
                    "8", "Changed back to"
                },
        "786",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "787",
            "Note controller",
                new String[] { // First
                    "0", "Display note",
                    "1", "Do not display note"
                },
            "Display constant controller",
                new String[] { // Second
                    "8", "No display constant generated"
                },
        "800",
            "Type of personal name entry element",
                new String[] { // First
                    "0", "Forename",
                    "1", "Surname",
                    "3", "Family name"
                },
            "Undefined",
                new String[] { // Second
                },
        "810",
            "Type of personal name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "2", "Name in direct order"
                },
            "Undefined",
                new String[] { // Second
                },
        "811",
            "Type of personal name entry element",
                new String[] { // First
                    "0", "Inverted name",
                    "1", "Jurisdiction name",
                    "2", "Name in direct order"
                },
            "Undefined",
                new String[] { // Second
                },
        "830",
            "Undefined",
                new String[] { // First
                },
            "Nonfiling characters",
                new String[] { // Second
                    "0", "No nonfiling characters",
                },
        "852",
            "Shelving scheme",
                new String[] { // First
                    "0", "Library of Congress classification",
                    "1", "Dewey Decimal classification",
                    "2", "National Library of Medicine classification",
                    "3", "Superintendent of Documents classification",
                    "4", "Shelving control number",
                    "5", "Title",
                    "6", "Shelved separately",
                    "7", "Source specified in subfield $2",
                    "8", "Other scheme"
                },
            "Shelving order",
                new String[] { // Second
                    "0", "Not enumeration",
                    "1", "Primary enumeration",
                    "2", "Alternative enumeration"
                },
        "856",
            "Access method",
                new String[] { // First
                    "0", "Email",
                    "1", "FTP",
                    "2", "Remote login (Telnet)",
                    "3", "Dial-up",
                    "4", "HTTP",
                    "7", "Method specified in subfield $2",
                },
            "Relationship",
                new String[] { // Second
                    "0", "Resource",
                    "1", "Version of resource",
                    "2", "Related resource",
                    "8", "No display constant generated"
                },
        "886",
            "Type of field",
                new String[] { // First
                    "0", "Leader",
                    "1", "Variable control fields (002-009)",
                    "2", "Variable data fields (010-999)",
                },
            "Undefined",
                new String[] { // Second
                },
    };



    private static HashMap<String,String> indicatorNameHash = null;
    private static HashMap<String,String> indicatorValueHash = null;

    public static String getFieldIndicatorName(String field, String indicatorNumber) {
        if(indicatorNameHash == null) {
            fillIndicatorHashMaps();
        }
        return indicatorNameHash.get(field+""+indicatorNumber);
    }

    public static String getFieldIndicatorValue(String field, String indicatorNumber, String indicator) {
        if(indicatorValueHash == null) {
            fillIndicatorHashMaps();
        }
        return indicatorValueHash.get(field+""+indicatorNumber+""+indicator);
    }


    private static void fillIndicatorHashMaps() {
        indicatorNameHash = new HashMap<String,String>();
        indicatorValueHash = new HashMap<String,String>();
        String fn = null;
        String in = null;
        String iva[] = null;
        for(int i=0; i<fieldIndicatorNames.length; i=i+5) {
            fn = (String) fieldIndicatorNames[i];
            in = (String) fieldIndicatorNames[i+1];
            if(!"Undefined".equals(in)) {
                indicatorNameHash.put(fn+"1", in);
            }
            if(fieldIndicatorNames[i+2] instanceof String[]) {
                iva = (String[]) fieldIndicatorNames[i+2];
                for(int j=0; j<iva.length; j=j+2) {
                    if(iva[j] != null && iva[j+1] != null) {
                        indicatorValueHash.put(fn+"1"+""+iva[j], iva[j+1]);
                    }
                }
            }
            in = (String) fieldIndicatorNames[i+3];
            if(!"Undefined".equals(in)) {
                indicatorNameHash.put(fn+"2", in);
            }
            if(fieldIndicatorNames[i+4] instanceof String[]) {
                iva = (String[]) fieldIndicatorNames[i+4];
                for(int j=0; j<iva.length; j=j+2) {
                    if(iva[j] != null && iva[j+1] != null) {
                        indicatorValueHash.put(fn+"2"+""+iva[j], iva[j+1]);
                    }
                }
            }
        }
    }

}
