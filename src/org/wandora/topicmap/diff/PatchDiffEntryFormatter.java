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
 */
package org.wandora.topicmap.diff;
import org.wandora.topicmap.*;
import static org.wandora.topicmap.diff.TopicMapDiff.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class PatchDiffEntryFormatter implements DiffEntryFormatter {

    protected void formatTopicDiffEntry(ArrayList<TopicDiffEntry> diff,Writer writer) throws IOException,TopicMapException {
        for(TopicDiffEntry d : diff){
            if(d instanceof SIAdded){
                writer.write("+I\""+escapeString(((SIAdded)d).si.toString())+"\"\n");
            }
            else if(d instanceof SIDeleted){
                writer.write("-I\""+escapeString(((SIDeleted)d).si.toString())+"\"\n");
            }
            else if(d instanceof SLChanged){
                Locator l=((SLChanged)d).sl;
                Locator ol=((SLChanged)d).oldsl;
                if(l!=null) {
                    if(ol==null) writer.write("+L\""+escapeString(l.toString())+"\"\n");
                    else writer.write("*L\""+escapeString(l.toString())+"\"/\""+escapeString(ol.toString())+"\"\n");
                }                
                else writer.write("-L\""+escapeString(ol.toString())+"\"\n");
            }
            else if(d instanceof BNChanged){
                String bn=((BNChanged)d).bn;
                String oldbn=((BNChanged)d).oldbn;
                if(bn!=null) {
                    if(oldbn==null) writer.write("+B\""+escapeString(bn)+"\"\n");
                    else writer.write("*B\""+escapeString(bn)+"\"/\""+escapeString(oldbn)+"\"\n");
                }                
                else writer.write("-B\""+escapeString(oldbn)+"\"\n");
            }
            else if(d instanceof TypeAdded){
                Topic t=((TypeAdded)d).t;
                Object t2=((TypeAdded)d).t2;
                writer.write("+T"+formatTopic(t,t2)+"\n");
            }
            else if(d instanceof TypeDeleted){
                Topic t=((TypeDeleted)d).t;
                Object t2=((TypeDeleted)d).t2;
                writer.write("-T"+formatTopic(t,t2)+"\n");
            }
            else if(d instanceof VariantChanged){
                VariantChanged vc=(VariantChanged)d;
                String v=vc.name;
                String oldv=vc.oldname;
                StringBuffer scopeString=new StringBuffer("");
                if(vc.scope!=null){
                    for(Topic t : vc.scope){
                        if(scopeString.length()>0) scopeString.append(" ");
                        scopeString.append(formatTopic(t,null));
                    }
                }
                else{
                    for(Object t : vc.scope2){
                        if(scopeString.length()>0) scopeString.append(" ");
                        scopeString.append(formatTopic(null,t));
                    }
                }
                
                if(v!=null) {
                    if(oldv==null) writer.write("+V{"+scopeString+"}\""+escapeString(v)+"\"\n");
                    else writer.write("*V{"+scopeString+"}\""+escapeString(v)+"\"/\""+escapeString(oldv)+"\"\n");
                }
                else writer.write("-V{"+scopeString+"}\""+escapeString(oldv)+"\"\n");
            }
        }
    }
    
    protected String escapeString(String s){
        s=s.replace("\\", "\\\\");
        s=s.replace("\"", "\\\"");
        return s;
    }
    
    protected String formatTopic(Topic t,Object identifier) throws TopicMapException {
        if(t!=null){
            String bn=t.getBaseName();
            if(bn!=null) return "B\""+escapeString(bn)+"\"";
            else {
                Locator l=t.getOneSubjectIdentifier();
                if(l!=null) return "I\""+escapeString(t.getOneSubjectIdentifier().toString())+"\"";
                return "D\""+t.getID()+"\"";
            }
        }
        else{
            if(identifier instanceof Locator) return "I\""+identifier.toString()+"\"";
            else return "B\""+identifier.toString()+"\"";
        }
    }
    
    protected void formatAssociation(Association a,Writer writer) throws IOException,TopicMapException {
        Topic type=a.getType();
        writer.write(formatTopic(type,null)+"\n");
        for(Topic role : a.getRoles()){
            writer.write(formatTopic(role,null)+":"+formatTopic(a.getPlayer(role),null)+"\n");
        }
    }
    protected void formatAssociation(Topic[] a,Writer writer) throws IOException,TopicMapException {
        Topic type=a[0];
        writer.write(formatTopic(type,null)+"\n");
        for(int i=1;i+1<a.length;i+=2){
            Topic role=a[i];
            Topic player=a[i+1];
            writer.write(formatTopic(role,null)+":"+formatTopic(player,null)+"\n");
        }
    }
    protected void formatAssociation(Object[] a,Writer writer) throws IOException,TopicMapException {
        Object type=a[0];
        writer.write(formatTopic(null,type)+"\n");
        for(int i=1;i+1<a.length;i+=2){
            Object role=a[i];
            Object player=a[i+1];
            writer.write(formatTopic(null,role)+":"+formatTopic(null,player)+"\n");
        }
    }
    
    public void header(Writer writer) throws IOException, TopicMapException{
    }
    public void footer(Writer writer) throws IOException, TopicMapException{
    }
    public void formatDiffEntry(DiffEntry entry,Writer writer) throws IOException,TopicMapException {
        if(entry instanceof TopicChanged){
            Topic t=((TopicChanged)entry).topic;
            Object t2=((TopicChanged)entry).topic2;
            ArrayList<TopicDiffEntry> diff=((TopicChanged)entry).diff;
            writer.write("*T["+formatTopic(t,t2)+"\n");
            formatTopicDiffEntry(diff,writer);
            writer.write("]\n");
        }
        else if(entry instanceof TopicDeleted){
            Topic t=((TopicDeleted)entry).topic;
            Object t2=((TopicDeleted)entry).topic2;
            ArrayList<TopicDiffEntry> diff=((TopicDeleted)entry).diff;
            writer.write("-T["+formatTopic(t,t2)+"\n");
            formatTopicDiffEntry(diff,writer);
            writer.write("]\n");
        }
        else if(entry instanceof TopicAdded){
            ArrayList<TopicDiffEntry> diff=((TopicAdded)entry).diff;
            writer.write("+T[\n");
            formatTopicDiffEntry(diff,writer);
            writer.write("]\n");
        }
        else if(entry instanceof AssociationAdded){
            writer.write("+A[");
            if(((AssociationAdded)entry).a!=null) 
                formatAssociation(((AssociationAdded)entry).a,writer);
            else 
                formatAssociation(((AssociationAdded)entry).a2,writer);
            writer.write("]\n");
        }
        else if(entry instanceof AssociationDeleted){
            writer.write("-A[");            
            if(((AssociationDeleted)entry).a!=null) 
                formatAssociation(((AssociationDeleted)entry).a,writer);
            else
                formatAssociation(((AssociationDeleted)entry).a2,writer);
            writer.write("]\n");
        }
    }

}
