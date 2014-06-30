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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */


public class AsuntojenHintaTiedotExtractor {
    
    
    public AsuntojenHintaTiedotExtractor() {
        
    }
    
    
    
    public static void scrapeData() {
        int p = 100;
        int pend = 1000;
        String ubody = "http://asuntojen.hintatiedot.fi/haku/?p=__p__&z=__z__";
        String saveFolder = "G:/hsopen3/asuntojen_hintatiedot_111011";
        
        for(int i=p; i<pend; i=i+10) {
            int z = 1;
            boolean nextPage = false;
            do {
                System.out.println("Scraping "+i+" and page "+z+".");
                String u = ubody.replace("__p__", ""+i);
                u = u.replace("__z__", ""+z);

                try {
                    URL url = new URL(u);
                    String c = IObox.doUrl(url);
                    IObox.saveFile(saveFolder+"/"+i+"_"+z+".html", c);
                    
                    if(c.indexOf("seuraava sivu") != -1) {
                        nextPage = true;
                    }
                    else {
                        nextPage = false;
                    }
                    z++;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(200);
                }
                catch(Exception e) {
                    // DO NOTHING, JUST WOKE UP
                }
            }
            while(nextPage);
        }
    }
    
    
    public static void scrapeData2() {
        String loadFolder = "G:/hsopen3/asuntojen_hintatiedot_111011";
        String saveFile = "G:/asuntojen_hintatiedot_111011.txt";
        StringBuilder sb = new StringBuilder("");
        File[] files = IObox.getFiles(loadFolder);
        System.out.println("Found total "+files.length+" files.");
        for(int i=0; i<files.length; i++) {
            try {
                String c = IObox.loadFile(files[i]);
                int index = c.indexOf("<table id=\"mainTable\">");
                if(index > 0) c = c.substring(index);
                index = c.indexOf("<div id=\"footer\">");
                if(index > 0) c = c.substring(0, index);
                c = c.replace("&#228;", "ä");
                c = c.replace("&#246;", "ö");
                c = c.replace("&#160;", " ");
                c = c.replace("&#196;", "Ä");
                
                String pn = files[i].getName();
                int pni = pn.indexOf("_");
                pn = pn.substring(0,pni);
                
                //System.out.println("-----------------------------");
                //System.out.println(c);
                //System.out.println("-----------------------------");
                
                Pattern p = Pattern.compile(
                        "<tr(?:\\sclass\\=\\\"last\\\")?>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Kaupungin osa
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Huoneet
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Neliöt
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Hinta
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Neliöhinta
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Rak. vuosi
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Kerros
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Hissi
                        "<\\/td>\\s*?"+
                        "<td>"+
                        "(.*?)"+ // Kunto
                        "<\\/td>\\s*?"+
                        "<\\/tr>",
                        Pattern.MULTILINE);
                
                Matcher m = p.matcher(c);
                
                int fp = 0;
                while(m.find(fp)) {

                    String kpginOsa = m.group(1);
                    String huoneet = m.group(2);
                    String neliot = m.group(3);
                    String hinta = m.group(4);
                    String nelioHinta = m.group(5);
                    String rakVuosi = m.group(6);
                    String kerros = m.group(7);
                    String hissi = m.group(8);
                    String kunto = m.group(9);

                    System.out.print( kpginOsa + " - " );
                    System.out.print( huoneet + " - " );
                    System.out.print( neliot + " - " );
                    System.out.print( hinta + " - " );
                    System.out.print( nelioHinta + " - " );
                    System.out.print( rakVuosi + " - " );
                    System.out.print( kerros + " - " );
                    System.out.print( hissi + " - " );
                    System.out.println( kunto + " - " );
                    
                    sb.append(files[i].getName()).append( "\t");
                    sb.append(pn).append( "\t");
                    sb.append(kpginOsa).append( "\t");
                    sb.append(huoneet).append( "\t");
                    sb.append(neliot).append( "\t");
                    sb.append(hinta).append( "\t");
                    sb.append(nelioHinta).append( "\t");
                    sb.append(rakVuosi).append( "\t");
                    sb.append(kerros).append( "\t");
                    sb.append(hissi).append( "\t");
                    sb.append(kunto).append( "\n");
                    
                    fp = m.end();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            IObox.saveFile(saveFile, sb.toString());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    
    
    public static void calcData() {
        String ilmoituksetFilename = "C:/Users/akivela/Desktop/Projects/hsopen3/ilmoitukset.txt";
        String myydytFilename = "C:/Users/akivela/Desktop/Projects/hsopen3/myydyt.txt";
        String outputFile = "C:/Users/akivela/Desktop/Projects/hsopen3/results.txt";
        StringBuilder sb = new StringBuilder("");
        
        try {
            String ilmoituksetRaw = IObox.loadFile(new File(ilmoituksetFilename));
            String myydytRaw = IObox.loadFile(new File(myydytFilename));
            
            String[] ilmoituksetArray = ilmoituksetRaw.split("\n");
            String[] myydytArray = myydytRaw.split("\n");
            
            System.out.println(ilmoituksetArray.length + " ilmoitusta.");
            System.out.println(myydytArray.length + " myyntia.");
            
            HashMap<String,ArrayList> ilmoituksetHash = new HashMap<String,ArrayList>();
            HashMap<String,ArrayList> myydytHash = new HashMap<String,ArrayList>();
            
            for(String ilmo : ilmoituksetArray) {
                String[] ilmoParts = ilmo.split("\t");
                String pno = ilmoParts[1];
                ArrayList<String[]> pnoIlmos = ilmoituksetHash.get(pno);
                if(pnoIlmos == null) {
                    pnoIlmos = new ArrayList();
                }
                pnoIlmos.add(ilmoParts);
                ilmoituksetHash.put(pno, pnoIlmos);
            }
            
            for(String myydyt : myydytArray) {
                String[] myydytParts = myydyt.split("\t");
                String pno = myydytParts[1];
                ArrayList<String[]> pnoMyydyt = myydytHash.get(pno);
                if(pnoMyydyt == null) {
                    pnoMyydyt = new ArrayList();
                }
                pnoMyydyt.add(myydytParts);
                myydytHash.put(pno, pnoMyydyt);
            }
            
            // **** calculate ****
            HashMap results = new HashMap();
            for(String pno : ilmoituksetHash.keySet()) {
                if(!"100".equals(pno)) continue;
                System.out.println("--------------------");
                
                
                ArrayList iArray = ilmoituksetHash.get(pno);
                ArrayList mArray = myydytHash.get(pno);
                
                int countI = 0;
                double hintaTotalI = 0;
                double nelioHintaTotalI = 0;
                if(iArray != null) {
                    for(Object i : iArray) {
                        String[] is = (String[]) i;
                        double neliot = asNumber(is[3]);
                        double hinta = asNumber(is[4]);
                        double nelioHinta = neliot != 0 ? hinta / neliot : 0;
                        hintaTotalI += hinta;
                        nelioHintaTotalI += nelioHinta;
                        countI++;
                    }
                }
                System.out.println(pno+" kohteitaI="+countI);
                double keskiarvoHintaI = countI != 0 ? hintaTotalI / countI : 0;
                double keskiarvoNelioHintaI = countI != 0 ? nelioHintaTotalI / countI : 0;
                System.out.println(pno+" keskiarvoHintaI="+keskiarvoHintaI);
                System.out.println(pno+" keskiarvoNelioHintaI="+keskiarvoNelioHintaI);
                
                int countM = 0;
                double hintaTotalM = 0;
                double nelioHintaTotalM = 0;
                if(mArray != null) {
                    for(Object m : mArray) {
                        String[] ms = (String[]) m;
                        double neliot = asNumber(ms[4]);
                        double hinta = asNumber(ms[5]);
                        double nelioHinta = neliot != 0 ? hinta / neliot : 0;
                        hintaTotalM += hinta;
                        nelioHintaTotalM += nelioHinta;
                        countM++;
                    }
                }
                System.out.println(pno+" kohteitaM="+countM);
                double keskiarvoHintaM = countM != 0 ? hintaTotalM / countM : 0;
                double keskiarvoNelioHintaM = countM != 0 ? nelioHintaTotalM / countM : 0;
                System.out.println(pno+" keskiarvoHintaM="+keskiarvoHintaM);
                System.out.println(pno+" keskiarvoNelioHintaM="+keskiarvoNelioHintaM);
                
                double hintaJousto = keskiarvoHintaI - keskiarvoHintaM;
                double nelioHintaJousto = keskiarvoNelioHintaI - keskiarvoNelioHintaM;
                
                System.out.println(pno+" hintaJousto="+hintaJousto);
                System.out.println(pno+" nelioHintaJousto="+nelioHintaJousto);
                
                sb.append(pno).append("\t");
                sb.append(countI).append("\t");
                sb.append(countM).append("\t");
                sb.append(keskiarvoHintaI).append("\t");
                sb.append(keskiarvoHintaM).append("\t");
                sb.append(keskiarvoNelioHintaI).append("\t");
                sb.append(keskiarvoNelioHintaM).append("\t");
                sb.append(hintaJousto).append("\t");
                sb.append(nelioHintaJousto).append("\t");
                sb.append("\n");
            }
            IObox.saveFile(outputFile, sb.toString());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
    
    
    
    public static float asNumber(String str) {
        float r = 0;
        try {
            str.replace(',', '.');
            r = Float.parseFloat(str);
        }
        catch(Exception e) {}
        return r;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public static void main(String[] args) {
        //AsuntojenHintaTiedotExtractor.scrapeData2();
        //AsuntojenHintaTiedotExtractor.calcData();
        //AsuntojenHintaTiedotExtractor.scrapeData();
    }
    
}
