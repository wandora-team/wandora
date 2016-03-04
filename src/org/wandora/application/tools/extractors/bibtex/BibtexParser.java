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
 * BibtexParser.java
 *
 * Created on 17. lokakuuta 2007, 11:01
 *
 */

package org.wandora.application.tools.extractors.bibtex;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import static org.wandora.utils.Tuples.*;
/**
 *
 * @author olli
 */
public class BibtexParser {
    
    private ArrayList<BibtexEntry> entries;
    
    /** Creates a new instance of BibtexParser */
    public BibtexParser() {
    }
    
    public ArrayList<BibtexEntry> getEntries(){
        return entries;
    }
    
    public void parse(Reader reader) throws IOException {
        parse(new PushbackReader(reader));
    }
    public void parse(PushbackReader reader) throws IOException {
        entries=new ArrayList<BibtexEntry>();
        int c;
        while( (c=reader.read())!=-1 ){
            if(c=='@'){
                try{
                    String type=readUntil(reader,"{");
                    type=type.substring(0,type.length()-1).trim();
                    // special processing for comment and string entries here
                    BibtexEntry e=readEntryBlock(reader,type);
                    if(e!=null) entries.add(e);
                }catch(BibtexParseException bpe){
                    bpe.printStackTrace();
                }
            }
        }
        reader.close();
    }
    
    private BibtexEntry readEntryBlock(PushbackReader reader,String type) throws IOException,BibtexParseException {
        BibtexEntry entry=new BibtexEntry();
        entry.setType(type);
        StringBuffer key=null;
        StringBuffer value=null;
        boolean first=true;
        
        while( true ){
            T2<String,Object> element=readEntryElement(reader);
            if(element==null) break;
            if(element.e2==null && first && element.e1!=null) entry.setID(element.e1);
            else if(element.e2==null || element.e1==null) throw new BibtexParseException();
            else entry.setValue(element.e1,element.e2);
            first=false;
        }
        return entry;
    }
    
    private T2<String,Object> readEntryElement(PushbackReader reader) throws IOException,BibtexParseException {
        String key=readUntil(reader,",=}");
        char last=key.charAt(key.length()-1);
        key=key.substring(0,key.length()-1).trim().toLowerCase();
        if(last=='}' && key.length()==0) return null;
        if(last==',' && key.length()==0) throw new BibtexParseException();
        if(last==',' || last=='}') {
            if(last=='}') reader.unread(last);
            return t2(key,null);
        }
        
        if(key.equals("author") || key.equals("editor")) {
            ArrayList<Object> values=readElementValue(reader,true);            
            if(values!=null && values.size()>0) return t2(key,(Object)values);
            else return null;
        }
        else {
            ArrayList<Object> values=readElementValue(reader,false);
            if(values!=null && values.size()>0) return t2(key,values.get(0));
            else return null;
        }
    }
    
    private Object makeValueObject(String r1,String r2,boolean people){
        if(!people) return r1.toString();
        else{
            String first=null;
            String initials=null;
            String last=null;
            
            int ind=r1.indexOf(",");
            if(ind!=-1){
                last=r1.substring(0,ind).trim();
                first="";
                if(ind<r1.length()-1) first=r1.substring(ind+1);
                if(r2!=null) first+=r2;
                first=first.trim();
            }
            else if(r2!=null){
                first=r1.trim();
                last=r2.trim();
            }
            else {
                ind=r1.lastIndexOf(" ");
                if(ind!=-1){
                    first=r1.substring(0,ind).trim();
                    if(ind<r1.length()-1) last=r1.substring(ind+1).trim();
                    if(last == null || last.length() == 0) {
                        last=first;
                        first=null;
                    }
                }
                else last=r1.trim();
            }
            
            
            if(first!=null && first.length()==0) first=null;
            if(first!=null){
                Pattern p=Pattern.compile("([^\\s]+)((\\s+[^\\s]\\.)+)");
                Matcher m=p.matcher(first);
                if(m.matches()){
                    initials=m.group(2);
                    first=m.group(1).trim();
                    initials.replaceAll("\\s+","");
                }
            }
            return new BibtexPerson(first,initials,last);
        }
    }
    
    private ArrayList<Object> readElementValue(PushbackReader reader,boolean people) throws IOException,BibtexParseException{
        int c;
        int openBraces=0;
        int openQuotes=0;
        ArrayList<Object> parsed=new ArrayList<Object>();
        StringBuffer read1=new StringBuffer();
        StringBuffer read2=null;
        StringBuffer read=read1;
        while( (c=reader.read())!=-1 ){
            if(c=='{') {
                if(people && read==read1 && openBraces+openQuotes==1 && 
                        read1.toString().trim().length()>0 && Character.isWhitespace(read1.charAt(read1.length()-1))){
                    read2=new StringBuffer();
                    read=read2;
                }
                openBraces++;
                continue;
            }
            else if(c=='}') {
                openBraces--;
                if(openBraces<0) {
                    reader.unread(c);
                    break;
                }
                else continue;
            }
            if(c=='\"' && openBraces==0){
                if(openQuotes==0) openQuotes=1;
                else openQuotes=0;
                continue;
            }
            
            if(openQuotes>0 || openBraces>0){
                if(c=='\\'){
                    String cmd=readEscapeCommand(reader);
                    String block="";
                    int c2=reader.read();
                    if(c2=='{') block=readUntil(reader,"}");
                    else block=Character.toString((char)c2);
                    boolean consumed=false;

                    if(cmd.length()==1){
                        if(cmd.equals("\\")) read.append("\n");
                        else if("#$%&_{}".indexOf(cmd)!=-1) read.append(cmd);
                    }
                    // rest of special symbols at http://www.bibtex.org/SpecialSymbols/
                    if(cmd.equals("aa")) read.append("å");
                    else if(cmd.equals("AA")) read.append("Å");
                    else if(cmd.equals("\"") && block.equals("a")) {read.append("ä"); consumed=true;}
                    else if(cmd.equals("\"") && block.equals("A")) {read.append("Ä"); consumed=true;}
                    else if(cmd.equals("\"") && block.equals("o")) {read.append("ö"); consumed=true;}
                    else if(cmd.equals("\"") && block.equals("O")) {read.append("Ö"); consumed=true;}
                    else if(cmd.equals("\'") && block.equals("e")) {read.append("é"); consumed=true;}
                    else if(cmd.equals("\'") && block.equals("E")) {read.append("É"); consumed=true;}
                    else read.append("\\"+cmd);
                    
                    if(!consumed) reader.unread(c2);
                    continue;
                }
                
                read.append((char)c);
                if(people && read.length()>=5){
                    if(read.substring(read.length()-4,read.length()-1).toLowerCase().equals("and") &&
                            Character.isWhitespace(read.charAt(read.length()-5)) &&
                            Character.isWhitespace(read.charAt(read.length()-1))){
                        
                        String r1,r2;
                        if(read==read2){
                            r1=read1.toString();
                            r2=read.substring(0,read.length()-5).trim();
                        }
                        else{
                            r1=read.substring(0,read.length()-5).trim();
                            r2=null;
                        }
                        Object o=makeValueObject(r1,r2,true);
                        if(o!=null) parsed.add(o);
                        read1=new StringBuffer();
                        read2=null;
                        read=read1;
                    }
                }
            }
            else if(c=='#'){} // append operator
            else if(c==',') break; // end of entry
            else if(Character.isLetterOrDigit((char)c)) read.append((char)c); // numbers and some other values may be outside quotes
        }        
        Object o=makeValueObject(read1.toString(),(read2==null?null:read2.toString()),people);
        if(o!=null) parsed.add(o);
        return parsed;
    }
    
    private String readEscapeCommand(PushbackReader reader) throws IOException {
        int c;
        StringBuffer read=new StringBuffer();
        while( (c=reader.read())!=-1 ){
            if(Character.isLetterOrDigit((char)c)){
                read.append((char)c);
            }
            else{
                if(read.length()==0) read.append((char)c);
                else reader.unread(c);
                break;
            }
        }
        return read.toString();
    }
    
    private String readUntil(PushbackReader reader,String chars) throws IOException,BibtexParseException {
        int c;
        StringBuffer read=new StringBuffer();
        while( (c=reader.read())!=-1 ){
            read.append((char)c);
            if(chars.indexOf((char)c)!=-1) break;
        }
        return read.toString();
    }
    
    private int readWhitespace(PushbackReader reader) throws IOException{
        int c;
        while( (c=reader.read())!=-1 ){
            if(!Character.isWhitespace((char)c)) return c;
        }
        return -1;
    }
    
    public static String removeBraces(String text){
        return text.replace("{","").replace("}","");
    }
    
    public static void main(String[] args) throws Exception {
        BibtexParser parser=new BibtexParser();
//        parser.parse(new InputStreamReader(System.in));
        parser.parse(new InputStreamReader(new FileInputStream("C:\\wandora\\build\\classes\\test.bib")));
        for(BibtexEntry e : parser.getEntries()){
            System.out.print("@"+e.getType()+"{");
            if(e.getID()!=null) System.out.print(e.getID()+",");
            System.out.println();
            Map<String,Object> values=e.getValues();
            for(String key : values.keySet()){
                Object value=values.get(key);
                if(value instanceof String) System.out.println("\t"+key+" = \""+value+"\",");
                else{
                    ArrayList<BibtexPerson> list=(ArrayList<BibtexPerson>)value;
                    System.out.print("\t"+key+" = \"");
                    boolean first=true;
                    for(BibtexPerson p : list){
                        if(!first) System.out.print(" and ");
                        else first=false;
                        System.out.print(p.getLastName());
                        if(p.getFirstName()!=null){
                            System.out.print(", "+p.getFirstName());
                            if(p.getInitials()!=null) System.out.print(" "+p.getInitials());
                        }
                    }
                    System.out.println("\",");
                }
            }
            System.out.println("}");
        }
    }
}
