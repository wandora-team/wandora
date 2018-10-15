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
 * BibtexExtractor.java
 *
 * Created on 17. lokakuuta 2007, 10:21
 *
 */

package org.wandora.application.tools.extractors;

import org.wandora.application.contexts.Context;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.xpath.*;

import bibtex.dom.*;
import bibtex.parser.*;
import bibtex.expansions.*;




/**
 *
 * @author olli
 */
public class JavabibExtractor extends AbstractExtractor implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	
	private String baseLocator = "http://wandora.org/si/bibtex/";
    private String defaultEncoding = "ISO-8859-1";

    private boolean encounteredExceptions=false;
    
    /** Creates a new instance of JavabibExtractor */
    public JavabibExtractor() {
    }

    @Override
    public String getName() {
        return "BibTeX extractor";
    }
    @Override
    public String getDescription(){
        return "Extracts information from BibTeX files!";
    }
    
    
    @Override
    public String getGUIText(int textType) {
        return super.getGUIText(textType);
    }

    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }


    
    private Topic getOrCreateTopic(TopicMap tm,String topic) throws TopicMapException {
        return getOrCreateTopic(tm,topic,false);
    }
    private Topic getOrCreateTopic(TopicMap tm,String topic,boolean addToBibtexClass) throws TopicMapException {
        Locator l=buildSI(topic.toLowerCase());
        Topic t=tm.getTopic(l);
        if(t!=null) return t;
        t=tm.createTopic();
        t.addSubjectIdentifier(l);
        t.setBaseName(topic);
        if(addToBibtexClass) {
            Topic bc = TMBox.getOrCreateTopic(tm, baseLocator);
            bc.setBaseName("Bibtex");
            Topic c = TMBox.getOrCreateTopic(tm, TMBox.WANDORACLASS_SI);
            t.addType(bc);
            bc.addType(c);
        }
        return t;
    }
    
    private static final Pattern charMapPattern=Pattern.compile("\\\\(['`~\"^])(?:\\{(\\\\?[A-Za-z])\\}|(\\\\?[A-Za-z]))");
    // this map only contains mappings matching the above regex
    private static final Map<String,String> charMap=GripCollections.addArrayToMap(new HashMap<String,String>(), new Object[]{
        "\\'{a}","�", "\\'{A}","�",
        "\\`{a}","�", "\\`{A}","�",
        "\\^{a}","�", "\\^{A}","�",
        "\\~{a}","�", "\\~{A}","�",
        "\\\"{a}","�", "\\\"{A}","�",
        
        "\\'{e}","�", "\\'{E}","�",
        "\\`{e}","�", "\\`{E}","�",
        "\\^{e}","�", "\\^{E}","�",
        "\\\"{e}","�", "\\\"{E}","�",
        
        "\\'{i}","�", "\\'{\\i}","�", "\\'{I}","�", "\\'{\\I}","�",
        "\\`{i}","�", "\\`{\\i}","�", "\\`{I}","�", "\\`{\\I}","�",
        "\\^{i}","�", "\\^{\\i}","�", "\\^{I}","�", "\\^{\\I}","�",
        "\\~{i}","?", "\\~{\\i}","?", "\\~{I}","?", "\\~{\\I}","?",
        "\\\"{i}","�", "\\\"{\\i}","�", "\\\"{I}","�", "\\\"{\\I}","�",

        "\\^{j}","?", "\\^{\\j}","?", "\\^{J}","?", "\\^{\\J}","?",
        
        "\\'{o}","�", "\\'{O}","�",
        "\\`{o}","�", "\\`{O}","�",
        "\\^{o}","�", "\\^{O}","�",
        "\\~{o}","�", "\\~{O}","�",
        "\\\"{o}","�", "\\\"{O}","�",
        
        "\\'{u}","�", "\\'{U}","�",
        "\\`{u}","�", "\\`{U}","�",
        "\\^{u}","�", "\\^{U}","�",
        "\\~{u}","?", "\\~{U}","?",
        "\\\"{u}","�", "\\\"{U}","�",
        
        "\\'{y}","�", "\\'{Y}","�",
        "\\`{y}","?", "\\`{Y}","?",
        "\\^{y}","?", "\\^{Y}","?",
        "\\\"{y}","�", "\\\"{Y}","�",
        
        "\\c{c}","�", "\\c{C}","�",
    });
    // this map contains all other mappings
    private static final Map<String,String> charMap2=GripCollections.addArrayToMap(new HashMap<String,String>(), new Object[]{
        "\\aa","�", "\\AA","�",
        "--", "-",
        "``", "\"", "''", "\"", 
        "{","", "}","",
    });
    
    
    
    private String expandCharacters(String s){
        s=s.trim();
        
        StringBuilder sb=new StringBuilder(s);
        int offs=0;
        Matcher matcher=charMapPattern.matcher(s);
        while(matcher.find()){
            String cmd=matcher.group(1);
            String char1=matcher.group(2);
            String char2=matcher.group(3);
            if(char1==null){
                if(char2==null) continue;
                char1=char2;
            }
            String key="\\"+cmd+"{"+char1+"}";
            String val=charMap.get(key);
            if(val==null) continue;
            sb.replace(matcher.start()+offs, matcher.end()+offs, val);
            offs+=val.length()-(matcher.end()-matcher.start());
        }
        
        s=sb.toString();
        for(String k : charMap2.keySet()){
            s=s.replace(k, charMap2.get(k));
        }
        s=s.replaceAll("\\s\\s+", " ");
        
        return s;
    }
    
    private String valueToString(BibtexNode v){
        if(v instanceof BibtexConcatenatedValue){
            BibtexConcatenatedValue bcv=(BibtexConcatenatedValue)v;
            return valueToString(bcv.getLeft())+valueToString(bcv.getRight());
        }
        else if(v instanceof BibtexMultipleValues){
            BibtexMultipleValues bmv=(BibtexMultipleValues)v;
            java.util.List<BibtexAbstractValue> values=bmv.getValues();
            StringBuilder sb=new StringBuilder();
            for(BibtexAbstractValue v2 : values){
                if(sb.length()>0) sb.append(", ");
                sb.append(valueToString(v2));
            }
            return sb.toString();
        }
        return v.toString();
    }
        
    private void addOccurrence(TopicMap tm,Topic entryTopic,BibtexEntry entry,String key) throws TopicMapException {
        BibtexAbstractValue o=entry.getFieldValue(key);
        if(o==null) return;
        Topic type=getOrCreateTopic(tm,key);
        Topic lang=TMBox.getOrCreateTopic(tm,XTMPSI.LANG_INDEPENDENT);
        String val=expandCharacters(valueToString(o)).trim();
        if(val.length()>0)
            entryTopic.setData(type,lang,val);
    }
    private void addAssociation(TopicMap tm,Topic entryTopic,BibtexEntry entry,String key) throws TopicMapException {
        BibtexAbstractValue o=entry.getFieldValue(key);
        if(o==null) return;
        Topic type=getOrCreateTopic(tm,key);
        Topic citation=getOrCreateTopic(tm,"citation");
        Topic lang=TMBox.getOrCreateTopic(tm,XTMPSI.LANG_INDEPENDENT);
        
        
        java.util.List<BibtexNode> a;
        if(o instanceof BibtexMultipleValues) a=((BibtexMultipleValues)o).getValues();
        else if(o instanceof BibtexPersonList) a=((BibtexPersonList)o).getList();
        else {
            a=new ArrayList();
            a.add(o);
        }
        for(BibtexNode v : a){
            String bn=expandCharacters(valueToString(v)).trim();
            if(bn.length()==0) continue;
            Association as=tm.createAssociation(type);
            as.addPlayer(entryTopic,citation);
            Topic p=null;
            if(v instanceof BibtexPerson){
                p=createPersonTopic(tm,(BibtexPerson)v);
            }
            else{
                p=tm.createTopic();
                p.addType(type);
                p.setBaseName(bn);
                if(p.getSubjectIdentifiers().isEmpty()){
                    p.addSubjectIdentifier(tm.createLocator(baseLocator+"other/"+TopicTools.cleanDirtyLocator(bn)));
                }
            }
            if(p==null) System.out.println("p is null");
            if(type==null) System.out.println("type is null");
            as.addPlayer(p,type);
        }
    }
    
    private Topic createPersonTopic(TopicMap tm,BibtexPerson p) throws TopicMapException {
        String name=p.getLast();
        if(p.getPreLast()!=null) name=p.getPreLast()+" "+name;
        if(p.getFirst()!=null) name+=", "+p.getFirst();
        if(p.getLineage()!=null) name+=" "+p.getLineage();
        name=expandCharacters(name);
        Topic t=tm.createTopic();
        t.setBaseName(name);
        t.addSubjectIdentifier(tm.createLocator(baseLocator+"person/"+TopicTools.cleanDirtyLocator(name)));
        Topic type=getOrCreateTopic(tm,"Person");
        t.addType(type);
        return t;
    }

    @Override
    public boolean useTempTopicMap(){
        return false;
    }
    
    @Override
    public void execute(Wandora admin, Context context) {
        setDefaultLogger();
        encounteredExceptions=false;
        super.execute(admin,context);
        if(encounteredExceptions) this.setState(WAIT);        
    }
    

    private boolean handleExceptions(Exception[] exceptions){
        if(exceptions!=null && exceptions.length>0){
            for(int i=0;i<exceptions.length;i++){
                this.log("Nonfatal exception "+exceptions[i].getMessage());
            }
            return true;
        }
        else return false;
    }
    
    public static final HashSet<String> associationFields=GripCollections.newHashSet("author","editor","institution","organization","booktitle","journal",
                                                                                    "publisher","school","series","year","volume","number","month","type","chapter","edition","howpublished");
    public static final HashSet<String> occurrenceFields=GripCollections.newHashSet("address","annote","crossref",
                                                                                    "key","note","pages");
    public boolean _extractTopicsFrom(Reader reader, TopicMap tm) throws Exception {
        this.log("Parsing BiBTex file");
        boolean handled=false;
        try{
            BibtexFile bibfile=new BibtexFile();
            BibtexParser parser=new BibtexParser(false);
            parser.parse(bibfile,reader);
            this.setProgress(30);
            this.log("Processing BiBTex info");
            HashMap<String,String> originals=new HashMap<String,String>();
            java.util.List<BibtexAbstractEntry> entries=bibfile.getEntries();
            int counter=0;
            for(BibtexAbstractEntry aEntry : entries){
                counter++;
                if((counter%100)==0)
                    this.setProgress(30+(int)((double)counter/(double)entries.size()*20.0));

                if(!(aEntry instanceof BibtexEntry)) continue;
                BibtexEntry e=(BibtexEntry)aEntry;
                String entryKey=e.getEntryKey();
                if(entryKey==null) continue;
                entryKey=entryKey.trim();
                if(entryKey.length()==0) continue;
                originals.put(entryKey,e.toString());
            }        
            this.setProgress(50);

            encounteredExceptions|=handleExceptions(parser.getExceptions());


            //MacroReferenceExpander macroExp=new MacroReferenceExpander(true,true,true,false);
            //macroExp.expand(bibfile);
            //exceptions|=handleExceptions(macroExp.getExceptions());
            CrossReferenceExpander xrefExp=new CrossReferenceExpander(false);
            xrefExp.expand(bibfile);
            encounteredExceptions|=handleExceptions(xrefExp.getExceptions());
            PersonListExpander perExp=new PersonListExpander(true, true);
            perExp.expand(bibfile);
            encounteredExceptions|=handleExceptions(perExp.getExceptions());

            Topic keyTopic=getOrCreateTopic(tm, "bibtex key");
            Topic originalTopic=getOrCreateTopic(tm, "original bibtex");
            Topic lang=TMBox.getOrCreateTopic(tm,XTMPSI.LANG_INDEPENDENT);

            counter=0;
            entries=bibfile.getEntries();
            for(BibtexAbstractEntry aEntry : entries){
                counter++;
                if((counter%100)==0)
                    this.setProgress(50+(int)((double)counter/(double)entries.size()*50.0));

                if(!(aEntry instanceof BibtexEntry)) continue;
                BibtexEntry e=(BibtexEntry)aEntry;

                String entryKey=e.getEntryKey();
                if(entryKey!=null) entryKey=entryKey.trim();
                if(entryKey.length()==0) entryKey=null;

                String typeS=e.getEntryType();
                if(typeS==null) continue;
                Topic type=getOrCreateTopic(tm,typeS,true);
                BibtexAbstractValue titleV=e.getFieldValue("title");
                String titleS=null;
                if(titleV!=null) titleS=expandCharacters(valueToString(titleV));
                Topic entry=null;
                String bn="bibtex unnamed ("+entryKey+")";
                if(titleS!=null){
                    bn=titleS+" ("+entryKey+")";
                    entry=tm.getTopicWithBaseName(bn);
                }
                if(entry==null) entry=tm.createTopic();
                entry.addType(type);
                entry.setBaseName(bn);
                if(entry.getSubjectIdentifiers().isEmpty()){
                    entry.addSubjectIdentifier(tm.createLocator(baseLocator+"citation/"+TopicTools.cleanDirtyLocator(bn)));
                }

                if(entryKey!=null) {
                    entry.setData(keyTopic, lang, entryKey);
                    String original=originals.get(entryKey);
                    if(original!=null) entry.setData(originalTopic, lang, original);
                }

                Map<String,BibtexAbstractValue> values=e.getFields();
                for(String key : values.keySet()){
                    if(associationFields.contains(key)){
                        addAssociation(tm,entry,e,key);
                    }
                    else {
                        addOccurrence(tm,entry,e,key);
                    }
                }
            }
            this.setProgress(100);
            if(entries.size()>0) handled=true;
        }
        catch(ParseException e){
            encounteredExceptions=true;
            this.log(e);
        }
        catch(ExpansionException e){
            encounteredExceptions=true;
            this.log(e);            
        }
        return handled;
    }
    
    
    public boolean _extractTopicsFrom(URL file, TopicMap topicMap) throws Exception {
        URLConnection con=file.openConnection();
        Wandora.initUrlConnection(con);
        String enc=con.getContentEncoding();
        if(enc==null) {
            String mime=con.getContentType();
            int ind=mime.indexOf("charset=");
            if(ind!=-1){
                int ind2=mime.indexOf(";",ind+1);
                if(ind2==-1) enc=mime.substring(ind+"charset=".length());
                else enc=mime.substring(ind+"charset=".length(),ind2);
            }
            else enc=defaultEncoding;
        }

        
        Reader reader=null;
        if(con.getContentType().toLowerCase().startsWith("text/html")){
            Pattern bibtexEntryPattern=Pattern.compile("@[a-zA-Z]{1,20}\\{");
            try{
                this.log("Converting html to plain text");
                org.w3c.tidy.Tidy tidy=new org.w3c.tidy.Tidy();
                tidy.setInputEncoding(enc);
                org.w3c.dom.Document doc=tidy.parseDOM(con.getInputStream(),null);

                XPath xpath=XPathFactory.newInstance().newXPath();
                StringBuffer text=new StringBuffer((String)xpath.evaluate("/",doc.getDocumentElement(),XPathConstants.STRING));
                
                // Bibtex entries must start on a new line so simply add \n in front of ever @.
                // This is a very crude way to make this work and has several problems:
                //    - New lines are added in front of @ even if they are found inside bibtex entry
                //    - If @ is anywhere in the document where it doesn't start a bibtex entry 
                //      (such as email addresses) it might confuse bibtex praser
                
                int pos=0;
                while(pos<text.length()){
                    char c=text.charAt(pos);
                    if(c=='\u00a0'){ // nbsp
                        text.setCharAt(pos,' ');
                    }
                    else if(c=='@') {
                        Matcher m=bibtexEntryPattern.matcher(text);
                        boolean match=false;
                        if(m.find(pos)){
                            if(m.start()==pos) match=true;
                        }
                        if(match) {
                            text.insert(pos, '\n');
                            pos+=2;
                        }
                        else {
                            text.setCharAt(pos, '_');
                            pos++;
                        }
                    }
                    else pos++;
                    
                }
                
                reader=new StringReader(text.toString());
            }catch(Exception e){
                log(e);
                return false;
            }
        }
        else{
            reader=new InputStreamReader(con.getInputStream(),enc);
        }
        return _extractTopicsFrom(reader,topicMap);
    }

    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        Reader reader=new InputStreamReader(new FileInputStream(file),defaultEncoding);
        return _extractTopicsFrom(reader,topicMap);
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }

    
    
    @Override
    public Locator buildSI(String siend) {
        if(!baseLocator.endsWith("/")) baseLocator = baseLocator + "/";
        return new Locator(TopicTools.cleanDirtyLocator(baseLocator + siend));
    }

    

    private final String[] contentTypes=new String[] { "text/plain", "application/x-bibtex", "text/x-bibtex", "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }    
}
