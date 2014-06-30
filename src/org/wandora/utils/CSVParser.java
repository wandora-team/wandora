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
 */

package org.wandora.utils;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author olli
 */
public class CSVParser {
    private char valueSeparator=',';
    private char lineSeparator='\n';
    private char stringChar='"';

    private String encoding = "UTF-8";
    
    
    
    public CSVParser(){

    }

    public Table parse(String filename) throws IOException {
        return parse(new File(filename));
    }
    public Table parse(String filename, String encoding) throws IOException {
        return parse(new File(filename),encoding);
    }

    public Table parse(File f) throws IOException {
        return parse(new FileInputStream(f));
    }
    public Table parse(File f, String encoding) throws IOException {
        return parse(new FileInputStream(f), encoding);
    }

    public Table parse(InputStream inRaw) throws IOException {
        return parse(inRaw, encoding);
    }
    public Table parse(InputStream inRaw, String encoding) throws IOException {
        Table ret=new Table();

        PushbackReader in=new PushbackReader(new InputStreamReader(inRaw,encoding),20);

        Row row=null;
        while( (row=readLine(in))!=null ){
            ret.add(row);
        }

        return ret;
    }

    
    private Row readLine(PushbackReader in) throws IOException {
        Row ret=new Row();
        while(true){
            int c=in.read();
            if(c<0) {
                if(ret.size()>0) return ret;
                else return null;
            }
            else in.unread(c);

            Object v=readValue(in);
            ret.add(v);
            boolean next=readValueSeparator(in);
            if(!next) break;
        }

        return ret;
    }

    private Object readValue(PushbackReader in) throws IOException {
        readWhitespace(in);
        while(true){
            int c=in.read();
            if(c==valueSeparator || c==lineSeparator) {
                in.unread(c);
                return null;
            }
            if(c<0){
                return null;
            }
            else if(c==stringChar){
                in.unread(c);
                return parseString(in);
            }
            else {
                in.unread(c);
                return parseNumberOrDate(in);
            }
        }
    }

    private boolean readValueSeparator(PushbackReader in) throws IOException {
        readWhitespace(in);
        while(true) {
            int c=in.read();
            if(c==valueSeparator) return true;
            else if(c==lineSeparator || c<0) return false;
            else {
                throw new RuntimeException("Error parsing CSV file");
            }
        }
    }

    private Object parseNumberOrDate(PushbackReader in) throws IOException {
        readWhitespace(in);

        StringBuilder sb=new StringBuilder();
        while(true) {
            int c=in.read();
            if(c==valueSeparator || c==lineSeparator || c<0) {
                if(c>=0) in.unread(c);
                break;
            }
            else sb.append((char)c);
        }
        String s=sb.toString().trim();

        try {
            int i=Integer.parseInt(s);
            return i;
        } 
        catch(NumberFormatException nfe) {
            try {
                double d=Double.parseDouble(s);
                return d;
            }
            catch(NumberFormatException nfe2) {
                try {
                    return parseDate(s);
                }
                catch(Exception ex) {
                    return s; // FINALLY WHEN EVERYTHING ELSE FAILS RETURN THE STRING AS A VALUE
                }
            }
        }
    }
    
    private SimpleDateFormat[] dateFormats={
        new SimpleDateFormat("yyyy-MM-dd"),
        new SimpleDateFormat("MM/dd/yyyy"),
        new SimpleDateFormat("dd.MM.yyyy")
    };

    private Date parseDate(String s) throws IOException {
        Date d=null;
        for(SimpleDateFormat df : dateFormats) {
            try {
                d=df.parse(s);
                break;
            }
            catch(ParseException pe){
                continue;
            }
        }

        if(d==null) {
            throw new RuntimeException("Error parsing CSV file");
        }
        else return d;
    }

    private String parseIntPart(PushbackReader in) throws IOException {
        StringBuilder sb=new StringBuilder();
        while(true){
            int c=in.read();
            if(c>='0' && c<='9'){
                sb.append((char)c);
            }
            else {
                if(c>=0) in.unread(c);
                if(sb.length()==0) return null;
                else return sb.toString();
            }
        }
    }

    private String parseString(PushbackReader in) throws IOException {
        readWhitespace(in);

        int c=in.read();
        if(c!=stringChar) throw new RuntimeException("Error parsing CSV file");
        StringBuilder sb=new StringBuilder();

        while(true){
            c=in.read();
            if(c<0) throw new RuntimeException("Error parsing CSV file");
            else if(c == stringChar) {
                int c2=in.read();
                if(c2==stringChar){
                    sb.append((char)stringChar);
                }
                else {
                    if(c2>=0) in.unread(c2);
                    return sb.toString();
                }
            }
            else {
                sb.append((char)c);
            }
        }
    }

    private void readWhitespace(PushbackReader in) throws IOException {
        while(true){
            int c=in.read();
            if(c<0) return;
            else if(c!=lineSeparator && Character.isWhitespace(c)) continue;
            else {
                in.unread(c);
                return;
            }
        }
    }

    public static class Row extends ArrayList<Object>{}
    public static class Table extends ArrayList<Row>{}

/*
    public static void main(String[] args) throws Exception {
        InputStream in=System.in;
//        InputStream in=new FileInputStream("Strindberg_lahtenyt_kv_1.csv");

        Table t=new CSVParser().parse(in);

        System.out.println("Parsed "+t.size()+" rows");

        for(Row r : t){
            for(Object o : r){
                if(o==null) System.out.print("null,");
                else System.out.print(o.toString()+",");
            }
            System.out.println();
        }
    }
*/
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public void setValueSeparator(char c) {
        valueSeparator=c;
    }
    
    public void setLineSeparator(char c) {
        lineSeparator=c;
    }
    
    public void setStringCharacter(char c) {
        stringChar=c;
    }
    
    public void setEncoding(String e) {
        encoding = e;
    }
    
    public char getValueSeparator() {
        return valueSeparator;
    }
    
    public char getLineSeparator() {
        return lineSeparator;
    }
    
    public char getStringCharacter() {
        return stringChar;
    }
    public String getEncoding() {
        return encoding;
    }
}
