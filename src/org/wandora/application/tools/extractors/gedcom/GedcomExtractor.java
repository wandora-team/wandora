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
 * GedcomExtractor.java
 *
 * Created on 2009-10-1, 20:52
 */


package org.wandora.application.tools.extractors.gedcom;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.*;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;



/**
 * Converts GEDCOM (GEnealogical Data COMmunication) formatted files to topic maps.
 *
 * Extractor is based on Mike Dean's work on GEDCOM to DAML converter. DAML converter is
 * described at http://www.daml.org/2001/01/gedcom/
 *
 * @author akivela
 */


public class GedcomExtractor extends AbstractExtractor implements WandoraTool {
    public static final boolean DEBUG = true;

    public static boolean ADD_SPACE_BEFORE_CONCATENATION = false;
    
    public static final String DEFAULT_LANG = "en";

    public String SI_PREFIX = "http://wandora.org/si/gedcom/";
    public String SCHEMA_PREFIX = "schema/";


    

    /** Creates a new instance of GedcomExtractor */
    public GedcomExtractor() {
    }



    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_gedcom.png");
    }

    @Override
    public String getName() {
        return "GEDCOM extractor";
    }

    @Override
    public String getDescription() {
        return "Convert GEDCOM (GEnealogical Data COMmunication) formatted files to topic maps.";
    }




    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select GEDCOM data file(s) or directories containing GEDCOM data files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking GEDCOM data files!";

            case FILE_PATTERN: return ".*\\.ged";

            case DONE_FAILED: return "Done! No extractions! %1 GEDCOM data file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 GEDCOM data file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 GEDCOM data file(s) crawled!";

            case LOG_TITLE: return "GEDCOM extraction Log";
        }
        return "";
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
    }

    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        BufferedReader urlReader = new BufferedReader( new InputStreamReader ( url.openStream() ) );
        return _extractTopicsFrom(urlReader, topicMap);
    }

    public boolean _extractTopicsFrom(File GEDCOMFile, TopicMap topicMap) throws Exception {
        boolean result = false;
        BufferedReader breader = null;
        try {
            if(GEDCOMFile == null) {
                log("No GEDCOM data file addressed!");
            }
            else {
                FileReader fr = new FileReader(GEDCOMFile);
                breader = new BufferedReader(fr);
                result = _extractTopicsFrom(breader, topicMap);
            }
        }
        finally {
            if(breader != null) breader.close();
        }
        return result;
    }




    public boolean _extractTopicsFrom(BufferedReader breader, TopicMap topicMap) throws Exception {
        int nindividuals = 0;
        int nfamilies = 0;
        int nsources = 0;
        int nobje = 0;
        int nnote = 0;
        int nrepo = 0;
        int nsubm = 0;
        int nhead = 0;

        int progress = 0;

        try {
            log("Parsing GEDCOM file to a tree representation.");
            Gedcom gedcom = Gedcom.parse(breader, this);

            log("Converting parsed tree representation to a topic map.");
            setProgress(0);
            setProgressMax(gedcom.numberOfTopLevelNodes);
            Iterator iterator = gedcom.keys.values().iterator();
            while(iterator.hasNext() && !forceStop()) {
		Gedcom.Node node = (Gedcom.Node) iterator.next();

		//if(DEBUG) node.print(0);
                progress++;
                setProgress(progress);

		if("INDI".equals(node.tag)) {
                    nindividuals++;
                    processIndividual(node, topicMap);
                }
                else if("FAM".equals(node.tag)) {
                    nfamilies++;
                    processFamily(node, topicMap);
                }
                else if("OBJE".equals(node.tag)) {
                    nobje++;
                }
                else if("NOTE".equals(node.tag)) {
                    nnote++;
                }
                else if("REPO".equals(node.tag)) {
                    nrepo++;
                }
                else if("SOUR".equals(node.tag)) {
                    nsources++;
                    processSource(node, topicMap);
                }
                else if("SUBM".equals(node.tag)) {
                    nsubm++;
                }
                else if("HEAD".equals(node.tag)) {
                    nhead++;
                }
                else {
                    // DO NOTHING
                }
            }
        }
        catch(Exception e) {
            log(e);
        }

        if(forceStop()) {
            log("User has stopped extraction. Aborting...");
        }
        log("Found "+nindividuals+" individuals");
        log("Found "+nfamilies+" families");
        log("Found "+nsources+" sources");

        if(nobje > 0) log("Skipped "+nobje+" object records");
        if(nnote > 0) log("Skipped "+nnote+" note records");
        if(nrepo > 0) log("Skipped "+nrepo+" repository records");
        if(nsubm > 0) log("Skipped "+nsubm+" submitter records");
        return true;
    }





    private void processIndividual(Gedcom.Node node, TopicMap tm) throws TopicMapException {
        if(node.key == null) {
            log("Individual has no key. Skipping.");
            return;
        }
        boolean firstName = true;
        Topic individualTopic = getOrCreateTopic(tm, fixNodeKey(node.key));
        Topic individualTypeTopic = getOrCreateIndividualType(tm);
        individualTopic.addType(individualTypeTopic);

        Iterator childIterator = node.children.iterator();
        while(childIterator.hasNext()) {
            Gedcom.Node child = (Gedcom.Node) childIterator.next();
            if("SEX".equals(child.tag)) {
                addAssociation(tm, "sex", individualTopic, individualTypeTopic, child.value, "sex");
            }
            else if("NAME".equals(child.tag)) {
                if(firstName) {
                    if(child.value != null) {
                        String name = child.value.trim();
                        individualTopic.setBaseName(name + " ("+fixNodeKey(node.key)+")");
                        individualTopic.setDisplayName(DEFAULT_LANG, name);
                        firstName = false;
                    }
                }
                processName(child, tm, individualTopic, individualTypeTopic);
            }
            else if("TITL".equals(child.tag)) {
                processEvent(child, tm, "title", individualTopic, individualTypeTopic);
            }
            else if("ASSO".equals(child.tag)) {
                Topic assoTopic = getOrCreateTopic(tm, fixNodeKey(child.value));
                Association assoAssociation = addAssociation(tm, "associated-individual", individualTopic, individualTypeTopic, assoTopic, "associated-individual");
                Iterator childchildIterator = child.children.iterator();
                while(childchildIterator.hasNext()) {
                    Gedcom.Node childchild = (Gedcom.Node) childchildIterator.next();
                    if("RELA".equals(childchild.tag)) {
                        Topic relationTopic = getOrCreateTopic(tm, node.value);
                        fillAssociation(tm, assoAssociation, relationTopic, "relation");
                    }
                }
            }
            else if("NOTE".equals(child.tag)) {
                String note = getText(child);
                addOccurrence(tm, individualTopic, "note", note);
            }

            // --- INDIVIDUAL EVENTS ---
            else if("BIRT".equals(child.tag)) {
                Association eventAssociation = processEvent(child, tm, "birth", individualTopic, individualTypeTopic);
                Iterator childchildIterator = child.children.iterator();
                while (childchildIterator.hasNext()) {
                    Gedcom.Node childchild = (Gedcom.Node) childchildIterator.next();
                    if("FAMC".equals(childchild.tag)) {
                        Topic familyTopic = getOrCreateTopic(tm, fixNodeKey(node.key));
                        Topic familyTypeTopic = getOrCreateFamilyType(tm);
                        familyTopic.addType( familyTypeTopic );
                        fillAssociation(tm, eventAssociation, familyTopic, "birth-family");
                    }
                }
            }
            else if("ADOP".equals(child.tag)) {
                Association eventAssociation = processEvent(child, tm, "adoptation", individualTopic, individualTypeTopic);
                Iterator childchildIterator = child.children.iterator();
                while (childchildIterator.hasNext()) {
                    Gedcom.Node childchild = (Gedcom.Node) childchildIterator.next();
                    if("FAMC".equals(childchild.tag)) {
                        Topic familyTopic = getOrCreateTopic(tm, fixNodeKey(node.key));
                        Topic familyTypeTopic = getOrCreateFamilyType(tm);
                        familyTopic.addType( familyTypeTopic );
                        fillAssociation(tm, eventAssociation, familyTopic, "adoptation-family");
                    }
                }
            }
            else if("DEAT".equals(child.tag)) {
                processEvent(child, tm, "death", individualTopic, individualTypeTopic);
            }
            else if("BURI".equals(child.tag)) {
                processEvent(child, tm, "burial", individualTopic, individualTypeTopic);
            }
            else if("CREM".equals(child.tag)) {
                processEvent(child, tm, "crematorion", individualTopic, individualTypeTopic);
            }
            else if("IMMI".equals(child.tag)) {
                processEvent(child, tm, "immigration", individualTopic, individualTypeTopic);
            }
            else if("NATU".equals(child.tag)) {
                processEvent(child, tm, "naturalization", individualTopic, individualTypeTopic);
            }
            else if("EMIG".equals(child.tag)) {
                processEvent(child, tm, "emigration", individualTopic, individualTypeTopic);
            }
            else if("GRAD".equals(child.tag)) {
                processEvent(child, tm, "graduation", individualTopic, individualTypeTopic);
            }
            else if("RETI".equals(child.tag)) {
                processEvent(child, tm, "retirement", individualTopic, individualTypeTopic);
            }

            else if("CENS".equals(child.tag)) {
                processEvent(child, tm, "cencus", individualTopic, individualTypeTopic);
            }
            else if("PROB".equals(child.tag)) {
                processEvent(child, tm, "probate", individualTopic, individualTypeTopic);
            }
            else if("WILL".equals(child.tag)) {
                processEvent(child, tm, "will", individualTopic, individualTypeTopic);
            }

            else if("CHR".equals(child.tag)) {
                processEvent(child, tm, "christening", individualTopic, individualTypeTopic);
            }
            else if("CHRA".equals(child.tag)) {
                processEvent(child, tm, "adult-christening", individualTopic, individualTypeTopic);
            }
            else if("CONF".equals(child.tag)) {
                processEvent(child, tm, "confirmation", individualTopic, individualTypeTopic);
            }
            else if("FCOM".equals(child.tag)) {
                processEvent(child, tm, "first-communion", individualTopic, individualTypeTopic);
            }
            else if("ORDN".equals(child.tag)) {
                processEvent(child, tm, "ordination", individualTopic, individualTypeTopic);
            }

            // ------

            else if("OCCU".equals(child.tag)) {
                processEvent(child, tm, "occupation", individualTopic, individualTypeTopic);
            }
            else if("CAST".equals(child.tag)) {
                processEvent(child, tm, "caste", individualTopic, individualTypeTopic);
            }
            else if("DSCR".equals(child.tag)) {
                processEvent(child, tm, "physical-description", individualTopic, individualTypeTopic);
            }
            else if("EDUC".equals(child.tag)) {
                processEvent(child, tm, "education", individualTopic, individualTypeTopic);
            }
            else if("IDNO".equals(child.tag)) {
                processEvent(child, tm, "national-id-number", individualTopic, individualTypeTopic);
            }
            else if("NATI".equals(child.tag)) {
                processEvent(child, tm, "national-or_tribal-origin", individualTopic, individualTypeTopic);
            }
            else if("NCHI".equals(child.tag)) {
                processEvent(child, tm, "count-of-children", individualTopic, individualTypeTopic);
            }
            else if("NMR".equals(child.tag)) {
                processEvent(child, tm, "count-of-marriages", individualTopic, individualTypeTopic);
            }
            else if("PROP".equals(child.tag)) {
                processEvent(child, tm, "possessions", individualTopic, individualTypeTopic);
            }
            else if("RELI".equals(child.tag)) {
                processEvent(child, tm, "religious-affiliation", individualTopic, individualTypeTopic);
            }
            else if("SSN".equals(child.tag)) {
                processEvent(child, tm, "social-security-number", individualTopic, individualTypeTopic);
            }
            else if("RESI".equals(child.tag)) {
                processEvent(child, tm, "residence", individualTopic, individualTypeTopic);
            }

            else if("EVEN".equals(child.tag)) {
                processEvent(child, tm, "event", individualTopic, individualTypeTopic);
            }


            else if("FAMC".equals(child.tag)) {
                Topic familyTopic = getOrCreateTopic(tm, fixNodeKey(child.value));
                Topic familyTypeTopic = getOrCreateFamilyType(tm);
                addAssociation(tm, "child", familyTopic, familyTypeTopic, individualTopic, "child");
            }
            /*
            else if("FAMS".equals(child.tag)) {
                Topic familyTopic = getOrCreateTopic(tm, fixNodeKey(child.value));
                Topic familyTypeTopic = getOrCreateFamilyType(tm);
                addAssociation(tm, "spouse", familyTopic, familyTypeTopic, individualTopic, "spouse");
            }
            */

            else if("SOUR".equals(child.tag)) {
                addAssociation(tm, "source", individualTopic, individualTypeTopic, fixNodeKey(child.value), "source");
            }
        }
    }






    private void processFamily(Gedcom.Node node, TopicMap tm) throws TopicMapException {
        Topic familyTopic = getOrCreateTopic(tm, fixNodeKey(node.key));
        Topic familyTypeTopic = getOrCreateFamilyType(tm);
        familyTopic.addType( familyTypeTopic );

        Iterator childIterator = node.children.iterator();
        while(childIterator.hasNext()) {
            Gedcom.Node child = (Gedcom.Node) childIterator.next();
            if("HUSB".equals(child.tag)) {
                Topic husbandTopic = getOrCreateTopic(tm, fixNodeKey(child.value));
                addAssociation(tm, "husband", familyTopic, familyTypeTopic, husbandTopic, "husband");
            }
            if("WIFE".equals(child.tag)) {
                Topic wifeTopic = getOrCreateTopic(tm, fixNodeKey(child.value));
                addAssociation(tm, "wife", familyTopic, familyTypeTopic, wifeTopic, "wife");
            }
            if("CHIL".equals(child.tag)) {
                Topic childTopic = getOrCreateTopic(tm, fixNodeKey(child.value));
                addAssociation(tm, "child", familyTopic, familyTypeTopic, childTopic, "child");
            }

            else if("ENGA".equals(child.tag)) {
                processEvent(child, tm, "engaged", familyTopic, familyTypeTopic);
            }
            else if("MARR".equals(child.tag)) {
                processEvent(child, tm, "married", familyTopic, familyTypeTopic);
            }
            else if("MARB".equals(child.tag)) {
                processEvent(child, tm, "marriage-bann", familyTopic, familyTypeTopic);
            }
            else if("MARC".equals(child.tag)) {
                processEvent(child, tm, "marriage-contract", familyTopic, familyTypeTopic);
            }
            else if("MARL".equals(child.tag)) {
                processEvent(child, tm, "marriage-license", familyTopic, familyTypeTopic);
            }
            else if("MARS".equals(child.tag)) {
                processEvent(child, tm, "marriage-settlement", familyTopic, familyTypeTopic);
            }

            else if("ANUL".equals(child.tag)) {
                processEvent(child, tm, "anulment", familyTopic, familyTypeTopic);
            }
            else if("CENS".equals(child.tag)) {
                processEvent(child, tm, "census", familyTopic, familyTypeTopic);
            }
            else if("DIVF".equals(child.tag)) {
                processEvent(child, tm, "divorce-filed", familyTopic, familyTypeTopic);
            }
            else if("DIV".equals(child.tag)) {
                processEvent(child, tm, "divorced", familyTopic, familyTypeTopic);
            }
            
            else if("EVEN".equals(child.tag)) {
                processEvent(child, tm, "event", familyTopic, familyTypeTopic);
            }
            else if("NOTE".equals(child.tag)) {
                String note = getText(child);
                addOccurrence(tm, familyTopic, "note", note.toString());
            }
            else if("SOUR".equals(child.tag)) {
                addAssociation(tm, "source", familyTopic, familyTypeTopic, fixNodeKey(child.value), "source");
            }
        }
        familyTopic.setBaseName(fixNodeKey(node.key));
    }





    private Association processEvent(Gedcom.Node node, TopicMap tm, String eventType, Topic baseTopic, Topic baseTypeTopic) throws TopicMapException {
        Association eventAssociation = null;
        if(node.value != null && node.value.length() > 0 && !"Y".equals(node.value)) {
            eventAssociation = addAssociation(tm, eventType, baseTopic, baseTypeTopic, node.value, eventType);
        }
        else {
            eventAssociation = addAssociation(tm, eventType, baseTopic, baseTypeTopic);
        }

        Iterator childIterator = node.children.iterator();
        while (childIterator.hasNext()) {
            Gedcom.Node child = (Gedcom.Node) childIterator.next();
            if("PLAC".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, child.value, "place");
            }
            else if("DATE".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, child.value, "date");
            }
            else if("TYPE".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, child.value, "type");
            }
            else if("AGE".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, child.value, "age");
            }
            else if("AGNC".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, child.value, "agency");
            }
            else if("CAUS".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, child.value, "cause");
            }
            else if("SOUR".equals(child.tag)) {
                fillAssociation(tm, eventAssociation, fixNodeKey(child.value), "source");
            }
            else if("NOTE".equals(child.tag)) {
                String note = getText(child);
                String noteBaseName = (note.length() > 255 ? note.substring(0,255)+"..." : note.toString());
                Topic noteTopic = getOrCreateTopic(tm, "note/"+System.currentTimeMillis()+((int)Math.random()*10000), noteBaseName);
                addOccurrence(tm, noteTopic, "note", note.toString());
                fillAssociation(tm, eventAssociation, noteTopic, "note");
            }
        }
        return eventAssociation;
    }




    private void processSource(Gedcom.Node node, TopicMap tm) throws TopicMapException {
        Topic sourceTopic = getOrCreateTopic(tm, fixNodeKey(node.key));
        Topic sourceTypeTopic = getOrCreateSourceType(tm);
        sourceTopic.addType( sourceTypeTopic );

        Iterator childIterator = node.children.iterator();
        while(childIterator.hasNext()) {
            Gedcom.Node child = (Gedcom.Node) childIterator.next();

            if("AUTH".equals(child.tag)) {
                processEvent(child, tm, "author", sourceTopic, sourceTypeTopic);
            }
            else if("TITL".equals(child.tag)) {
                processEvent(child, tm, "title", sourceTopic, sourceTypeTopic);
            }
            else if("ABBR".equals(child.tag)) {
                processEvent(child, tm, "abbreviation", sourceTopic, sourceTypeTopic);
            }
            else if("PUBL".equals(child.tag)) {
                processEvent(child, tm, "publication", sourceTopic, sourceTypeTopic);
            }
            else if("TEXT".equals(child.tag)) {
                String text = getText(child);
                addOccurrence(tm, sourceTopic, "text", text.toString());
            }
            else if("REFN".equals(child.tag)) {
                processEvent(child, tm, "reference", sourceTopic, sourceTypeTopic);
            }
            else if("RIN".equals(child.tag)) {
                processEvent(child, tm, "record-id-number", sourceTopic, sourceTypeTopic);
            }
            else if("NOTE".equals(child.tag)) {
                String note = getText(child);
                addOccurrence(tm, sourceTopic, "note", note.toString());
            }
        }
        sourceTopic.setBaseName(fixNodeKey(node.key));
    }





    private String getText(Gedcom.Node node) {
        StringBuffer note = new StringBuffer(node.value == null ? "" : node.value);
        Iterator childIterator = node.children.iterator();
        while(childIterator.hasNext()) {
            Gedcom.Node child = (Gedcom.Node) childIterator.next();
            if("CONC".equals(child.tag)) {
                if(child.value != null) {
                    if(ADD_SPACE_BEFORE_CONCATENATION) {
                        if(note.length() > 0 && note.charAt(note.length()-1) != ' ') {
                            note.append(" ");
                        }
                    }
                    note.append(child.value);
                }
            }
            else if("CONT".equals(child.tag)) {
                note.append("\n");
                if(child.value != null) {
                    note.append(child.value);
                }
            }
        }
        //System.out.println("---note:"+note.toString());
        return note.toString();
    }




    private void processName(Gedcom.Node node, TopicMap tm, Topic nameCarrier, Topic nameCarrierType) throws TopicMapException {
        Iterator nameIterator = node.children.iterator();
        if(nameIterator.hasNext()) {
            Association nameAssociation = addAssociation(tm, "name", nameCarrier, nameCarrierType);

            while(nameIterator.hasNext()) {
                Gedcom.Node name = (Gedcom.Node) nameIterator.next();
                if (name.tag.equals("GIVN")) {
                    fillAssociation(tm, nameAssociation, name.value, "given-name");
                }
                else if("SURN".equals(name.tag)) {
                    fillAssociation(tm, nameAssociation, name.value, "surname");
                }
                else if("NICK".equals(name.tag)) {
                    fillAssociation(tm, nameAssociation, name.value, "nickname");
                }
                else if("NPFX".equals(name.tag)) {
                    fillAssociation(tm, nameAssociation, name.value, "prefix");
                }
                else if("SPFX".equals(name.tag)) {
                    fillAssociation(tm, nameAssociation, name.value, "surname-prefix");
                }
                else if("NSFX".equals(name.tag)) {
                    fillAssociation(tm, nameAssociation, name.value, "suffix");
                }
                else if("SOUR".equals(name.tag)) {
                    fillAssociation(tm, nameAssociation, fixNodeKey(name.value), "source");
                }
                else if("NOTE".equals(name.tag)) {
                    String note = getText(name);
                    String noteBaseName = (note.length() > 255 ? note.substring(0,255)+"..." : note.toString());
                    Topic noteTopic = getOrCreateTopic(tm, "note/"+System.currentTimeMillis()+((int)Math.random()*10000), noteBaseName);
                    addOccurrence(tm, noteTopic, "note", note.toString());
                    fillAssociation(tm, nameAssociation, noteTopic, "name-note");
                }
            }
        }
    }



    private String fixNodeKey(String key) {
        if(key != null) {
            if(key.startsWith("@")) {
                key = key.substring(1);
            }
            if(key.endsWith("@")) {
                key = key.substring(0, key.length()-1);
            }
        }
        return key;
    }


    // -------------------------------------------------------------------------



    public Topic getOrCreateGedcomType(TopicMap tm) throws TopicMapException {
        Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        Topic t = getOrCreateTopic(tm, new Locator(SI_PREFIX), "GEDCOM");
        makeSubclassOf(tm, t, wandoraClass);
        return t;
    }


    public Topic getOrCreateIndividualType(TopicMap tm) throws TopicMapException {
        Topic t = createTopicForSchemaTerm(tm, "individual");
        return t;
    }

    public Topic getOrCreateFamilyType(TopicMap tm) throws TopicMapException {
        Topic t = createTopicForSchemaTerm(tm, "family");
        return t;
    }

    public Topic getOrCreateNameType(TopicMap tm) throws TopicMapException {
        Topic t = createTopicForSchemaTerm(tm, "name");
        return t;
    }

    public Topic getOrCreateSourceType(TopicMap tm) throws TopicMapException {
        Topic t = createTopicForSchemaTerm(tm, "source");
        return t;
    }


    public void addOccurrence(TopicMap tm, Topic carrier, String occurrenceType, String occurrenceText) throws TopicMapException {
        if(tm == null) return;
        if(carrier == null) return;
        if(occurrenceType == null || occurrenceText == null) return;

        Topic occurrenceTypeTopic = createTopicForSchemaTerm(tm, occurrenceType);
        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang(DEFAULT_LANG), DEFAULT_LANG);
        carrier.setData(occurrenceTypeTopic, langTopic, occurrenceText);
    }




    public Association fillAssociation(TopicMap tm, Association association, Topic playerTopic, String role) throws TopicMapException  {
        if(tm == null) return null;
        if(playerTopic == null || role == null) return null;

        Topic roleTopic = createTopicForSchemaTerm(tm,role);
        association.addPlayer(playerTopic, roleTopic);
        return association;
    }


    public Association fillAssociation(TopicMap tm, Association association, String player, String role) throws TopicMapException  {
        if(tm == null) return null;
        if(player == null || role == null) return null;

        Topic roleTopic = createTopicForSchemaTerm(tm,role);
        Topic playerTopic = getOrCreateTopic(tm,player);
        association.addPlayer(playerTopic, roleTopic);
        return association;
    }




    public Association addAssociation(TopicMap tm, String associationType, Topic player1Topic, Topic role1Topic) throws TopicMapException {
        if(tm == null) return null;
        if(associationType == null) return null;
        if(player1Topic == null || role1Topic == null) return null;

        Topic associationTypeTopic = createTopicForSchemaTerm(tm,associationType);
        Association association = tm.createAssociation(associationTypeTopic);
        association.addPlayer(player1Topic, role1Topic);
        return association;
    }


    public Association addAssociation(TopicMap tm, String associationType, Topic player1Topic, Topic role1Topic, String player2, String role2) throws TopicMapException {
        if(tm == null) return null;
        if(associationType == null) return null;
        if(player1Topic == null || role1Topic == null) return null;
        if(player2 == null || role2 == null) return null;

        Topic associationTypeTopic = createTopicForSchemaTerm(tm,associationType);
        Association association = tm.createAssociation(associationTypeTopic);
        Topic role2Topic = createTopicForSchemaTerm(tm,role2);
        Topic player2Topic = getOrCreateTopic(tm,player2);
        association.addPlayer(player1Topic, role1Topic);
        association.addPlayer(player2Topic, role2Topic);
        return association;
    }




    public Association addAssociation(TopicMap tm, String associationType, Topic player1Topic, Topic role1Topic, Topic player2Topic, String role2) throws TopicMapException {
        if(tm == null) return null;
        if(associationType == null) return null;
        if(player1Topic == null || role1Topic == null) return null;
        if(player2Topic == null || role2 == null) return null;

        Topic associationTypeTopic = createTopicForSchemaTerm(tm,associationType);
        Association association = tm.createAssociation(associationTypeTopic);
        Topic role2Topic = createTopicForSchemaTerm(tm,role2);
        association.addPlayer(player1Topic, role1Topic);
        association.addPlayer(player2Topic, role2Topic);
        return association;
    }


    public Topic createTopicForSchemaTerm(TopicMap tm, String schemaTerm) throws TopicMapException {
        if(tm == null) return null;
        if(schemaTerm == null) return null;
        String si = SCHEMA_PREFIX+schemaTerm;
        Topic schemaTopic = getOrCreateTopic(tm, TopicTools.cleanDirtyLocator(si));
        schemaTopic.setBaseName(schemaTerm);
        makeSubclassOf(tm, schemaTopic, getOrCreateGedcomType(tm));
        return schemaTopic;
    }





    public void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }



    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------





    public boolean associationExists(Topic t1, Topic t2, Topic at) {
        if(t1 == null || t2 == null || at == null) return false;
        try {
            Collection<Association> c = t1.getAssociations(at);
            Association a = null;
            Collection<Topic> roles = null;
            Topic player = null;
            for(Iterator<Association> i=c.iterator(); i.hasNext(); ) {
                a = i.next();
                roles = a.getRoles();
                for(Iterator<Topic> it = roles.iterator(); it.hasNext(); ) {
                    player = a.getPlayer(it.next());
                    if(player != null && t2.mergesWithTopic(player)) return true;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    public Topic getOrCreateTopic(TopicMap tm, String si, String basename) throws TopicMapException {
        return getOrCreateTopic(tm, new Locator(si.trim()), basename);
    }

    public Topic getOrCreateTopic(TopicMap tm, String base) throws TopicMapException {
        return getOrCreateTopic(tm, this.makeSI(base), base);
    }


    public Topic getOrCreateTopic(TopicMap tm, Locator si, String basename) throws TopicMapException {
        if(tm == null) return null;
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            topic = tm.createTopic();
            topic.addSubjectIdentifier(si);
        }
        if(basename != null && topic.getBaseName() == null) {
            topic.setBaseName(basename.trim());
        }
        return topic;
    }



    public Topic getTopic(TopicMap tm, String si) throws TopicMapException {
        if(tm == null) return null;
        Topic topic = tm.getTopic(si);
        return topic;
    }


    public Topic getOrCreateTopic(TopicMap topicmap, String si, String baseName, String displayName) {
        return getOrCreateTopic(topicmap, new Locator(si), baseName, displayName, null);
    }
    public Topic getOrCreateTopic(TopicMap topicmap, Locator si, String baseName, String displayName) {
        return getOrCreateTopic(topicmap, si, baseName, displayName, null);
    }
    public Topic getOrCreateTopic(TopicMap topicmap, Locator si, String baseName, String displayName, Topic typeTopic) {
        try {
            return ExtractHelper.getOrCreateTopic(si, baseName, displayName, typeTopic, topicmap);
        }
        catch(Exception e) {
            log(e);
        }
        return null;
    }

    public Locator makeSI(String str) {
        if(str == null) str = "";
        return new Locator( TopicTools.cleanDirtyLocator(SI_PREFIX + str.trim()) );
    }


    @Override
    public boolean useTempTopicMap() {
        return false;
    }
}
