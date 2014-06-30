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
 */


package org.wandora.application.tools.extractors.hsopen;

import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;
import java.io.*;
import java.net.*;
import org.wandora.utils.Textbox;

/**
 *
 * @author akivela
 */
public class ApuRahatCSVExtract extends AbstractExtractor implements WandoraTool {
    protected String defaultEncoding = "UTF-8";
    public static String LANG = "fi";

    public static final String APURAHAT_SI = "http://wandora.org/si/hsopen/apurahat";

    public static final String KOONTI_SI = APURAHAT_SI+"/koonti";
    public static final String HENKILO_SI = APURAHAT_SI+"/henkilo";
    public static final String KIELI_SI = APURAHAT_SI+"/kieli";
    public static final String SUKUPUOLI_SI = APURAHAT_SI+"/sukupuoli";
    public static final String VUOSI_SI = APURAHAT_SI+"/vuosi";
    public static final String HAKEMUSLUOKKA_SI = APURAHAT_SI+"/hakemusluokka";
    public static final String PAATOS_SI = APURAHAT_SI+"/paatos";
    public static final String JASEN_SI = APURAHAT_SI+"/jasen";
    public static final String SUMMA_SI = APURAHAT_SI+"/summa";
    public static final String MAAKUNTA_SI = APURAHAT_SI+"/maakunta";
    public static final String LAANI_SI = APURAHAT_SI+"/laani";
    public static final String KOTIPAIKKA_SI = APURAHAT_SI+"/kotipaikka";

    public static final String SYNNYINKUUKAUSI_SI = APURAHAT_SI+"/synnyin-kuukausi";
    public static final String SYNNYINPAIVA_SI = APURAHAT_SI+"/synnyin-paiva";
    public static final String SYNNYINVUOSI_SI = APURAHAT_SI+"/synnyin-vuosi";
    public static final String SYNNYINAIKA_SI = APURAHAT_SI+"/syntyma-aika";

    public static final int KOONTI_COLUMN = 0;
    public static final int NIMI_COLUMN = 1;
    public static final int HETU_COLUMN = 2;
    public static final int KIELI_COLUMN = 3;
    public static final int SUKUPUOLI_COLUMN = 4;
    public static final int VUOSI_COLUMN = 5;
    public static final int HAKEMUSLUOKKA_COLUMN = 6;
    public static final int PAATOS_COLUMN = 7;
    public static final int JASENTEN_LKM_COLUMN = 8;
    public static final int SUMMA_COLUMN = 9;
    public static final int MAAKUNTA_COLUMN = 10;
    public static final int LAANI_COLUMN = 11;
    public static final int KOTIPAIKKA_COLUMN = 12;



    
    /**
     * Creates a new instance of ApuRahatCSVExtract
     */
    public ApuRahatCSVExtract() {
    }


    @Override
    public String getName() {
        return "Apurahat CSV Extract (HSOpen)";
    }

    @Override
    public String getDescription() {
        return "Apurahat CSV Extract (HSOpen).";
    }


    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select ApurahaCSV file(s) or directories containing ApurahaCSV files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking ApurahaCSV files!";

            case FILE_PATTERN: return ".*\\.txt";

            case DONE_FAILED: return "Done! No extractions! %1 ApurahaCSV files(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 ApurahaCSV files(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 ApurahaCSV files(s) crawled!";

            case LOG_TITLE: return "ApurahaCSV Extraction Log";
        }
        return "";
    }


    @Override
    public boolean useTempTopicMap() {
        return false;
    }




    


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }

    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        BufferedReader urlReader = new BufferedReader( new InputStreamReader ( url.openStream() ) );
        return _extractTopicsFrom(urlReader, topicMap);
    }

    public boolean _extractTopicsFrom(File keywordFile, TopicMap topicMap) throws Exception {
        boolean result = false;
        BufferedReader breader = null;
        try {
            if(keywordFile == null) {
                log("No CSV file addressed! Using default file name 'apurahat.txt'!");
                keywordFile = new File("apurahat.txt");
            }
            FileReader fr = new FileReader(keywordFile);
            breader = new BufferedReader(fr);
            result = _extractTopicsFrom(breader, topicMap);
        }
        finally {
            if(breader != null) breader.close();
        }
        return result;
    }





    public boolean _extractTopicsFrom(BufferedReader breader, TopicMap tm) throws Exception {
        int rowCounter = 0;
        log("Extracting...");
        try {
            String line = "";
            String[] tokens = null;
            line = breader.readLine();
            while(line != null && !forceStop()) {
                tokens = line.split("\t");
                for(int i=0; i<tokens.length; i++) { // Trim results!
                    if(tokens[i] != null && tokens[i].length() > 0) {
                        //System.out.print("trimming " + tokens[i]);
                        tokens[i] = Textbox.trimExtraSpaces(tokens[i]);
                        //System.out.print(" --> " + tokens[i]);
                    }
                }
                Topic koontiT = null;
                Topic henkiloT = null;
                Topic kieliT = null;
                Topic sukupuoliT = null;
                Topic vuosiT = null;
                Topic hakemusluokkaT = null;
                Topic paatosT = null;
                Topic jasenT = null;
                Topic summaT = null;
                Topic maakuntaT = null;
                Topic laaniT = null;
                Topic kotipaikkaT = null;
                if(tokens.length > 3) {
                    String h = fix(tokens[HETU_COLUMN]);
                    if(h == null || h.length() == 0) h = "SSN-"+System.currentTimeMillis();

                    koontiT = getKoontiTopic( fix(tokens[KOONTI_COLUMN]), tm );
                    henkiloT = getHenkiloTopic( h, fix(tokens[NIMI_COLUMN]), tm );
                    kieliT = getKieliTopic( fix(tokens[KIELI_COLUMN]), tm );
                    sukupuoliT = getSukupuoliTopic( fix(tokens[SUKUPUOLI_COLUMN]), tm );
                    vuosiT = getVuosiTopic( fix(tokens[VUOSI_COLUMN]), tm );
                    hakemusluokkaT = getHakemusluokkaTopic( fix(tokens[HAKEMUSLUOKKA_COLUMN]), tm );
                    paatosT = getPaatosTopic( fix(tokens[PAATOS_COLUMN]), tm );
                    jasenT = getJasenTopic( fix(tokens[JASENTEN_LKM_COLUMN]), tm );
                    summaT = getSummaTopic( fix(tokens[SUMMA_COLUMN]), tm );
                    if(tokens.length > MAAKUNTA_COLUMN) maakuntaT = getMaakuntaTopic( fix(tokens[MAAKUNTA_COLUMN]), tm );
                    if(tokens.length > LAANI_COLUMN) laaniT = getLaaniTopic( fix(tokens[LAANI_COLUMN]), tm );
                    if(tokens.length > KOTIPAIKKA_COLUMN) kotipaikkaT = getKotipaikkaTopic( fix(tokens[KOTIPAIKKA_COLUMN]), tm );

                    if(henkiloT != null && kieliT != null) {
                        Association a = tm.createAssociation(getKieliType(tm));
                        a.addPlayer(kieliT, getKieliType(tm));
                        a.addPlayer(henkiloT, getHenkiloType(tm));
                    }
                    if(henkiloT != null && sukupuoliT != null) {
                        Association a = tm.createAssociation(getSukupuoliType(tm));
                        a.addPlayer(sukupuoliT, getSukupuoliType(tm));
                        a.addPlayer(henkiloT, getHenkiloType(tm));
                    }

                    if(henkiloT!= null && kotipaikkaT != null) {
                        Association a = tm.createAssociation(getKotipaikkaType(tm));
                        a.addPlayer(kotipaikkaT, getKotipaikkaType(tm));
                        a.addPlayer(henkiloT, getHenkiloType(tm));
                        if(maakuntaT != null) {
                            a.addPlayer(maakuntaT, getMaakuntaType(tm));
                        }
                        if(laaniT != null) {
                            a.addPlayer(laaniT, getLaaniType(tm));
                        }
                    }

                    if(koontiT != null && henkiloT != null && vuosiT != null && hakemusluokkaT != null && paatosT != null && jasenT != null && summaT != null) {
                        Association a = tm.createAssociation(getPaatosType(tm));
                        a.addPlayer(paatosT, getPaatosType(tm));
                        a.addPlayer(koontiT, getKoontiType(tm));
                        a.addPlayer(vuosiT, getVuosiType(tm));
                        a.addPlayer(jasenT, getJasenType(tm));
                        a.addPlayer(summaT, getSummaType(tm));
                        a.addPlayer(henkiloT, getHenkiloType(tm));
                        a.addPlayer(hakemusluokkaT, getHakemusluokkaType(tm));
                    }

                    if(henkiloT != null && fix(tokens[HETU_COLUMN]) != null) {
                        String hetu = fix(tokens[HETU_COLUMN]);
                        if(hetu.length() == 5) hetu = "0"+hetu;
                        if(hetu.length() == 6) {
                            //String d = hetu.substring(0,2);
                            //String m = hetu.substring(2,4);
                            String y = hetu.substring(4,6);
                            y = "19"+y;

                            //Topic dT = getSynnyinPaivaTopic( d, tm );
                            //Topic mT = getSynnyinKuukausiTopic( m, tm );
                            Topic yT = getSynnyinVuosiTopic( y, tm );

                            if(yT != null) {
                                Association a = tm.createAssociation(getSynnyinAikaType(tm));
                                //a.addPlayer(dT, getSynnyinPaivaType(tm));
                                //a.addPlayer(mT, getSynnyinKuukausiType(tm));
                                a.addPlayer(yT, getSynnyinVuosiType(tm));
                                a.addPlayer(henkiloT, getHenkiloType(tm));
                            }
                        }
                    }
                }
                else {
                    log("Skipping row "+rowCounter);
                }
                setProgress(rowCounter++);
                line = breader.readLine();
            }
        }
        catch(Exception e) {
            log(e);
        }
        log("Processed " + rowCounter + " rows.");
        return true;
    }



    public String fix(String str) {
        if(str != null) {
            str = str.trim();
            if(str.startsWith("\"")) str = str.substring(1);
            if(str.endsWith("\"")) str = str.substring(0, str.length()-1);
        }
        return str;
    }



    // -------------------------------------------------------------------------

    public Topic getWandoraType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(TMBox.WANDORACLASS_SI, tm);
    }

    public Topic getApuRahatType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(APURAHAT_SI, "Apurahat", getWandoraType(tm), tm);
    }


    public Topic getKoontiType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(KOONTI_SI, "Koonti", getApuRahatType(tm), tm);
    }
    public Topic getHenkiloType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(HENKILO_SI, "Henkilö", getApuRahatType(tm), tm);
    }
    public Topic getKieliType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(KIELI_SI, "Kieli", getApuRahatType(tm), tm);
    }
    public Topic getSukupuoliType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(SUKUPUOLI_SI, "Sukupuoli", getApuRahatType(tm), tm);
    }
    public Topic getVuosiType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(VUOSI_SI, "Vuosi", getApuRahatType(tm), tm);
    }
    public Topic getHakemusluokkaType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(HAKEMUSLUOKKA_SI, "Hakemusluokka", getApuRahatType(tm), tm);
    }
    public Topic getPaatosType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(PAATOS_SI, "Päätös", getApuRahatType(tm), tm);
    }
    public Topic getJasenType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(JASEN_SI, "Jäsen", getApuRahatType(tm), tm);
    }
    public Topic getSummaType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(SUMMA_SI, "Summa", getApuRahatType(tm), tm);
    }
    public Topic getMaakuntaType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(MAAKUNTA_SI, "Maakunta", getApuRahatType(tm), tm);
    }
    public Topic getLaaniType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(LAANI_SI, "Lääni", getApuRahatType(tm), tm);
    }
    public Topic getKotipaikkaType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(KOTIPAIKKA_SI, "Kotipaikka", getApuRahatType(tm), tm);
    }


    public Topic getSynnyinKuukausiType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(SYNNYINKUUKAUSI_SI, "Synnyinkuukausi", getApuRahatType(tm), tm);
    }
    public Topic getSynnyinPaivaType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(SYNNYINPAIVA_SI, "Synnyinpäivä", getApuRahatType(tm), tm);
    }
    public Topic getSynnyinVuosiType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(SYNNYINVUOSI_SI, "Synnyinvuosi", getApuRahatType(tm), tm);
    }
    public Topic getSynnyinAikaType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(SYNNYINAIKA_SI, "Syntyma-aika", getApuRahatType(tm), tm);
    }

    

    // -------------------------------------------------------------------------


    public Topic getKoontiTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, KOONTI_SI, getKoontiType(tm), tm);
    }
    
    public Topic getHenkiloTopic( String hetu, String nimi, TopicMap tm ) throws Exception {
        Topic t = null;
        if(hetu != null) {
            if(hetu.length() == 5) hetu = "0"+hetu;
            t = getATopic(hetu, HENKILO_SI, getHenkiloType(tm), tm);
            if(t != null && nimi != null) {
                t.setBaseName(nimi);
                t.setDisplayName(LANG, nimi);
            }
        }
        return t;
    }

    public Topic getKieliTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, KIELI_SI, getKieliType(tm), tm);
    }
    public Topic getSukupuoliTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, SUKUPUOLI_SI, getSukupuoliType(tm), tm);
    }
    public Topic getVuosiTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, VUOSI_SI, getVuosiType(tm), tm);
    }
    public Topic getHakemusluokkaTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, HAKEMUSLUOKKA_SI, getHakemusluokkaType(tm), tm);
    }
    public Topic getPaatosTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, PAATOS_SI, getPaatosType(tm), tm);
    }
    public Topic getJasenTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, JASEN_SI, getJasenType(tm), tm);
    }
    public Topic getSummaTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, SUMMA_SI, getSummaType(tm), tm);
    }
    public Topic getMaakuntaTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, MAAKUNTA_SI, getMaakuntaType(tm), tm);
    }
    public Topic getLaaniTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, LAANI_SI, getLaaniType(tm), tm);
    }
    public Topic getKotipaikkaTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, KOTIPAIKKA_SI, getKotipaikkaType(tm), tm);
    }


    public Topic getSynnyinVuosiTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, SYNNYINVUOSI_SI, getSynnyinVuosiType(tm), tm);
    }
    public Topic getSynnyinPaivaTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, SYNNYINPAIVA_SI, getSynnyinPaivaType(tm), tm);
    }
    public Topic getSynnyinKuukausiTopic( String token, TopicMap tm ) throws Exception {
        return getATopic(token, SYNNYINKUUKAUSI_SI, getSynnyinKuukausiType(tm), tm);
    }



    private Topic getATopic(String str, String si, Topic type, TopicMap tm) throws TopicMapException {
        if(str != null && si != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic topic=ExtractHelper.getOrCreateTopic(si+"/"+urlEncode(str), str, tm);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }




    public Topic getOrCreateTopic(TopicMap tm, String si) {
        return getOrCreateTopic(tm, new Locator(si));
    }

    public Topic getOrCreateTopic(TopicMap tm, Locator si) {
        Topic topic = null;
        try {
            topic = tm.getTopic(si);
            if(topic == null) {
                topic = tm.createTopic();
                topic.addSubjectIdentifier(si);
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }





}
