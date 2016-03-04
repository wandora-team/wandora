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
 * SeparatedListDatumExtractor.java
 *
 * Created on 3. joulukuuta 2004, 10:14
 */

package org.wandora.application.tools.extractors.datum;


import org.wandora.application.tools.extractors.datum.*;
import org.wandora.application.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author  olli
 */
public class SeparatedListDatumExtractor implements DatumExtractor {

    protected List header;
    protected URL lastURL;
    protected CountInputStream in;
    protected BufferedReader reader;
    protected long contentLength;
    protected long read;
    protected String pattern;
    protected Pattern compiled;
    
    /** Creates a new instance of SeparatedListDatumExtractor */
    public SeparatedListDatumExtractor(String pattern) {
        this.pattern=pattern;
        compiled=Pattern.compile(pattern);
    }
    
    public double getProgress() {
        if(contentLength<=0) return -1;
        return (double)in.getCount()/(double)contentLength;
    }
    
    protected void extractHeader() throws IOException,ExtractionException {
        String line=reader.readLine();
        if(line==null) throw new ExtractionException("Could not extract header. End of file.");
        String[] h=compiled.split(line,-1);
        header=new Vector();
        for(int i=0;i<h.length;i++){
            header.add(h[i]);
        }
    }

    public Map extractRow() throws IOException,ExtractionException {
        String line="";
        while(line.trim().length()==0){
            line=reader.readLine();
            if(line==null) return null;
        }
        String[] m=compiled.split(line,-1);
        Map map=new HashMap();
        for(int i=0;i<m.length;i++){
            map.put(header.get(i),m[i]);
        }
        return map;
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
                reader=new BufferedReader(new InputStreamReader(in));
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
//            logger.writelog("DDD","Got datum "+map);
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
