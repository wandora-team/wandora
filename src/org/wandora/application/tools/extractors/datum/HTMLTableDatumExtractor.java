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
 * HTMLTableDatumExtractor.java
 *
 * Created on 24. marraskuuta 2004, 16:50
 */

package org.wandora.application.tools.extractors.datum;



import org.wandora.application.tools.extractors.datum.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import org.wandora.utils.HTMLEntitiesCoder;
import org.wandora.application.*;


/**
 *
 * Note that this class isn't very robust at parsing the html table.
 *
 * @author  olli
 */
public class HTMLTableDatumExtractor implements DatumExtractor {
    
    protected List header;
    protected URL lastURL;
    protected CountInputStream in;
    protected Reader reader;
    protected long contentLength;
    protected long read;
    
    /** Creates a new instance of HTMLTableDatumExtractor */
    public HTMLTableDatumExtractor() {
    }
    
    public double getProgress() {
        if(contentLength<=0) return -1;
        return (double)in.getCount()/(double)contentLength;
    }
    
    protected void extractHeader() throws IOException,ExtractionException {
        StringBuffer buffer=new StringBuffer();
        StringBuffer search=new StringBuffer("</TR>");
        int ptr=0;
        while(true){
            int r=reader.read();
            if(r==-1) break;
            char c=(char)r;
            buffer.append(c);
            if(search.charAt(ptr)==Character.toUpperCase(c)) ptr++;
            else ptr=0;
            if(ptr==search.length()){
                Vector header=new Vector();
                Pattern pattern=Pattern.compile("<[tT][hH]>(.*?)</[tT][hH]>");
                Matcher matcher=pattern.matcher(buffer);
                while(matcher.find()){
                    String h=matcher.group(1);
                    h=HTMLEntitiesCoder.decode(h);
                    //System.out.println("header found == "+ h);
                    header.add(h);
                }
                this.header=header;
                return;
            }
        }
        throw new ExtractionException("Couldn't find table header.");
    }
    
    public Map extractRow() throws IOException,ExtractionException {
        StringBuffer buffer=new StringBuffer();
        StringBuffer searchnest=new StringBuffer("<TR>");
        StringBuffer search=new StringBuffer("</TR>");
        int ptr=0;
        int ptrnest=0;
        int stack=0;
        while(true){
            int r=reader.read();
            if(r==-1) break;
            char c=(char)r;
            buffer.append(c);
            if(search.charAt(ptr)==Character.toUpperCase(c)) ptr++;
            else ptr=0;
            if(searchnest.charAt(ptrnest)==Character.toUpperCase(c)) ptrnest++;
            else ptrnest=0;
            if(ptrnest==searchnest.length()){
                ptrnest=0;
                stack++;
            }
            if(ptr==search.length()){
                ptr=0;
                stack--;
                if(stack<=0){
                    int counter=0;
                    HashMap map=new HashMap();
                    Pattern pattern=Pattern.compile("<[tT][dD]>(.*?)</[tT][dD]>");
                    Matcher matcher=pattern.matcher(buffer);
                    while(matcher.find()){
                        String h=matcher.group(1);
                        h=HTMLEntitiesCoder.decode(h);
                        h=h.trim();
                        while(h.endsWith("<BR>")) {
                            h = h.substring(0, h.length()-4);
                            h=h.trim();
                        }
                        while(h.endsWith("<br>")) {
                            h = h.substring(0, h.length()-4);
                            h=h.trim();
                        }
                        if(header.size()>counter) map.put(header.get(counter++),h);
                    }
                    return map;
                }
            }
        }
        return null;        
    }
    
    public java.util.Map next(DataStructure data, org.wandora.piccolo.Logger logger) throws ExtractionException {
        URL u=(URL)data.handle;
        if(lastURL==null || !u.equals(lastURL)){
            logger.writelog("DBG","New url");
            if(in!=null) try{in.close();}catch(IOException ioe){}
            lastURL=u;
            try{
                URLConnection uc=u.openConnection();
                Wandora.initUrlConnection(uc);
                contentLength=uc.getContentLength();
                read=0;
                in=new CountInputStream(uc.getInputStream());
                reader=new InputStreamReader(in);
                logger.writelog("DBG","Extracting header");
                extractHeader();
                logger.writelog("DBG","Done extracting header");
/*              // DEBUG  
                StringBuffer sb=new StringBuffer();
                for(int i=0;i<header.size();i++){
                    sb.append(header.get(i).toString()+"; ");
                }
                logger.writelog("DBG","Got header "+sb);
 */
            }catch(IOException ioe){
                throw new ExtractionException("Couldn't extract table header.",ioe);
            }
        }
        try{
            logger.writelog("DBG","Extracting row");
            Map map=extractRow();
            if(map==null){
                logger.writelog("DBG","Got null row");
                in.close();
                in=null;
            }
            return map;
        }catch(IOException ioe){
            throw new ExtractionException("IOException reading source.",ioe);
        }
        
    }
    
}

class CountInputStream extends FilterInputStream {
    private long count;
    public CountInputStream(InputStream in){
        super(in);
        count=0;
    }
    public long getCount(){
        return count;
    }
    public int read() throws IOException {
        int c=super.read();
        if(c!=-1) count++;
        return c;
    }
    public int read(byte[] b) throws IOException {
        int c=super.read(b);
        if(c!=-1) count+=c;
        return c;
    }
    public int read(byte[] b,int off,int len) throws IOException {
        int c=super.read(b,off,len);
        if(c!=-1) count+=c;
        return c;
    }
    public long skip(long n) throws IOException {
        long c=super.skip(n);
        if(c!=-1) count+=c;
        return c;
    }
}
