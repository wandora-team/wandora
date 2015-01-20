/* 
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 */
package org.wandora.topicmap.diff;
import org.wandora.topicmap.*;
import static org.wandora.topicmap.diff.TopicMapDiff.*;
import java.io.*;
import java.util.*;
import java.text.ParseException;

/**
 *
 * @author olli
 */
public class PatchDiffParser {
    protected int line;
    protected PushbackReader reader;
    
    public PatchDiffParser(PushbackReader reader){
        this.reader=reader;
        this.line=1;
    }
    public PatchDiffParser(Reader reader){
        this(new PushbackReader(reader));
    }
    
    public ArrayList<DiffEntry> parse() throws IOException,ParseException  {
        ArrayList<DiffEntry> ret=new ArrayList<DiffEntry>();
        DiffEntry d=null;
        while( (d=parseEntry())!=null ){
            ret.add(d);
        }
        return ret;
    }
    
    protected DiffEntry parseEntry() throws IOException,ParseException {
        int mode=reader.read();
        int type=reader.read();
        if(mode==-1) return null;
        else if(type==-1) throw new ParseException("Unexpected EOF, expecting diff entry type",line);
        
        DiffEntry ret=null;
        
        if(type=='T'){
            readChar('[');
            Object topic=null;
            if(mode!='+') topic=readTopicIdentifier();
            readEOL();
            ArrayList<TopicDiffEntry> tDiff=readTopicDiff();
            if(mode=='+') ret=new TopicAdded(tDiff);
            else if(mode=='-') ret=new TopicDeleted(topic,tDiff);
            else if(mode=='*') ret=new TopicChanged(topic,tDiff);
            else throw new ParseException("Unknown mode for topic diff entry "+(char)mode,line);
        }
        else if(type=='A'){
            readChar('[');
            Object[] a=readAssociation();
            if(mode=='+') ret=new AssociationAdded(a);
            else if(mode=='-') ret=new AssociationDeleted(a);
            else throw new ParseException("Unknown mode for association diff entry "+(char)mode,line);
        }
        else throw new ParseException("Unknown diff entry type "+(char)type,line);
        readEOL();
        
        return ret;
    }
    
    protected ArrayList<TopicDiffEntry> readTopicDiff() throws IOException,ParseException {
        ArrayList<TopicDiffEntry> ret=new ArrayList<TopicDiffEntry>();
        while(true){
            int mode=reader.read();
            if(mode==-1) throw new ParseException("Unexpected EOF, reading topic diff entry",line);
            else if(mode==']') break;
            else{
                int type=reader.read();
                if(type==-1) throw new ParseException("Unexpected EOF, reading type for topic diff entry",line);
                else if(type=='I'){
                    String s=readQuotedString();
                    Locator l=new Locator(s);
                    if(mode=='-') ret.add(new SIDeleted(l));
                    else if(mode=='+') ret.add(new SIAdded(l));
                    else throw new ParseException("Invalid mode \""+(char)mode+"\" for subject identifier topic diff entry",line);
                    readEOL();
                }
                else if(type=='L'){
                    String s=readQuotedString();
                    Locator l=new Locator(s);
                    if(mode=='-') ret.add(new SLChanged(null,l));
                    else if(mode=='+') ret.add(new SLChanged(l,null));
                    else if(mode=='*') {
                        readChar('/');
                        String s2=readQuotedString();
                        Locator l2=new Locator(s2);
                        ret.add(new SLChanged(l,l2));
                    }
                    else throw new ParseException("Invalid mode \""+(char)mode+"\" for subject locator topic diff entry",line);
                    readEOL();                    
                }
                else if(type=='B'){
                    String s=readQuotedString();
                    if(mode=='-') ret.add(new BNChanged(null,s));
                    else if(mode=='+') ret.add(new BNChanged(s,null));
                    else if(mode=='*') {
                        readChar('/');
                        String s2=readQuotedString();
                        ret.add(new BNChanged(s,s2));
                    }
                    else throw new ParseException("Invalid mode \""+(char)mode+"\" for base name topic diff entry",line);
                    readEOL();                    
                }
                else if(type=='V'){
                    readChar('{');
                    Set<Object> scope=readScope();
                    String s=readQuotedString();
                    if(mode=='-') ret.add(new VariantChanged(scope,null,s,false));
                    else if(mode=='+') ret.add(new VariantChanged(scope,s,null,false));
                    else if(mode=='*') {
                        readChar('/');
                        String s2=readQuotedString();
                        ret.add(new VariantChanged(scope,s,s2,false));
                    }
                    else throw new ParseException("Invalid mode \""+(char)mode+"\" for variant topic diff entry",line);
                    readEOL();                    
                }
                else if(type=='T'){
                    Object topic=readTopicIdentifier();
                    if(mode=='-') ret.add(new TypeDeleted(topic));
                    else if(mode=='+') ret.add(new TypeAdded(topic));
                    else throw new ParseException("Invalid mode \""+(char)mode+"\" for type topic diff entry",line);
                    readEOL();
                }
                else throw new ParseException("Unknown topic diff type "+(char)type,line);
            }
        }
        return ret;
    }
    protected HashSet<Object> readScope() throws IOException,ParseException {
        HashSet<Object> ret=new HashSet<Object>();
        while(true){
            Object o=readTopicIdentifier();
            ret.add(o);
            int c=reader.read();
            if(c=='}') break;
            else if(c!=' ') throw new ParseException("Unexpected char \""+(char)c+"\" while reading scope, expecting space",line);
        }
        return ret;
    }
    protected Object[] readAssociation() throws IOException,ParseException {
        Object atype=readTopicIdentifier();
        readEOL();
        ArrayList<Object> playersAndRoles=new ArrayList<Object>();
        while(true){
            int peek=reader.read();
            if(peek==']') break;
            else reader.unread(peek);
            Object r=readTopicIdentifier();
            readChar(':');
            Object p=readTopicIdentifier();
            playersAndRoles.add(r);
            playersAndRoles.add(p);
            readEOL();
        }
        Object[] ret=new Object[playersAndRoles.size()+1];
        ret[0]=atype;
        for(int i=0;i<playersAndRoles.size();i++){
            ret[i+1]=playersAndRoles.get(i);
        }
        return ret;
    }
    
    protected Object readTopicIdentifier() throws IOException,ParseException {
        int type=reader.read();
        
        if(type==-1) throw new ParseException("Unexpected EOF, reading topic identifier",line);
        else if(type=='B'){
            String s=readQuotedString();
            return s;
        }
        else if(type=='I'){
            String s=readQuotedString();
            return new Locator(s);
        }
        else throw new ParseException("Unknown topic idetnifier type "+(char)type,line);
    }
    
    protected String readQuotedString() throws IOException,ParseException {
        int r=reader.read();
        if(r!='"') throw new ParseException("Expecting \"",line);
        StringBuffer read=new StringBuffer();
        boolean escape=false;
        while(true){
            r=reader.read();
            if(r==-1) throw new ParseException("Unexpected EOF, reading quoted string",line);
            else if(r=='\\' && !escape) {
                escape=true;
                continue;
            }
            else if(r=='"' && !escape) break;
            else read.append((char)r);
        }
        return read.toString();
    }
    
    protected void readChar(char c) throws IOException,ParseException {
        int r=reader.read();
        if(r!=c) throw new ParseException("Expecting "+c,line);
    }
    
    protected void readEOL() throws IOException,ParseException {
        int c=reader.read();
        if(c==0x0d) c=reader.read();
        if(c!=0x0a) throw new ParseException("Expecting end of line",line);        
        line++;
    }
    
}
