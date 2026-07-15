/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.diff.TopicMapDiff.AssociationAdded;
import org.wandora.topicmap.diff.TopicMapDiff.AssociationDeleted;
import org.wandora.topicmap.diff.TopicMapDiff.BNChanged;
import org.wandora.topicmap.diff.TopicMapDiff.DiffEntry;
import org.wandora.topicmap.diff.TopicMapDiff.SIAdded;
import org.wandora.topicmap.diff.TopicMapDiff.SIDeleted;
import org.wandora.topicmap.diff.TopicMapDiff.SLChanged;
import org.wandora.topicmap.diff.TopicMapDiff.TopicAdded;
import org.wandora.topicmap.diff.TopicMapDiff.TopicChanged;
import org.wandora.topicmap.diff.TopicMapDiff.TopicDeleted;
import org.wandora.topicmap.diff.TopicMapDiff.TopicDiffEntry;
import org.wandora.topicmap.diff.TopicMapDiff.TypeAdded;
import org.wandora.topicmap.diff.TopicMapDiff.TypeDeleted;
import org.wandora.topicmap.diff.TopicMapDiff.VariantChanged;

/**
 *
 * @author olli
 */
public class HTMLDiffEntryFormatter implements DiffEntryFormatter {
    
    private boolean nochanges;
    
    
    public HTMLDiffEntryFormatter(){
        nochanges=true;
    }
    
    
    protected void formatTopicDiffEntry(ArrayList<TopicDiffEntry> diff,Writer writer) throws IOException,TopicMapException {
        for(TopicDiffEntry d : diff){
            if(d instanceof SIAdded){
                writer.write("Added subject identifier "+((SIAdded)d).si+"<br />\n");
            }
            else if(d instanceof SIDeleted){
                writer.write("Deleted subject identifier "+((SIDeleted)d).si+"<br />\n");                
            }
            else if(d instanceof SLChanged){
                Locator l=((SLChanged)d).sl;
                Locator ol=((SLChanged)d).oldsl;
                if(l!=null) {
                    if(ol==null) writer.write("Added subject locator "+l+"<br />\n");
                    else writer.write("Changed subject locator from "+ol+" to "+l+"<br />\n");
                }                
                else writer.write("Deleted subject locator "+ol+"<br />\n");
            }
            else if(d instanceof BNChanged){
                String bn=((BNChanged)d).bn;
                String oldbn=((BNChanged)d).oldbn;
                if(bn!=null) {
                    if(oldbn==null) writer.write("Added base name \""+bn+"\"<br />\n");
                    else writer.write("Changed base name from \""+oldbn+"\" to \""+bn+"\"<br />\n");
                }                
                else writer.write("Deleted base name \""+oldbn+"\"<br />\n");
            }
            else if(d instanceof TypeAdded){
                Topic t=((TypeAdded)d).t;
                Object t2=((TypeAdded)d).t2;
                writer.write("Added type "+formatTopic(t,t2)+"<br />\n");
            }
            else if(d instanceof TypeDeleted){
                Topic t=((TypeDeleted)d).t;
                Object t2=((TypeDeleted)d).t2;
                writer.write("Deleted type "+formatTopic(t,t2)+"<br />\n");                
            }
            else if(d instanceof VariantChanged){
                VariantChanged vc=(VariantChanged)d;
                String v=vc.name;
                String oldv=vc.oldname;
                StringBuffer scopeString=new StringBuffer("{");
                if(vc.scope!=null){
                    for(Topic t : vc.scope){
                        if(scopeString.length()>1) scopeString.append(", ");
                        scopeString.append(formatTopic(t,null));
                    }
                }
                else{
                    for(Object t : vc.scope2){
                        if(scopeString.length()>1) scopeString.append(", ");
                        scopeString.append(formatTopic(null,t));
                    }                    
                }
                scopeString.append("}");
                
                if(v!=null) {
                    if(oldv==null) writer.write("Added variant "+scopeString+" \""+v+"\"<br />\n");
                    else writer.write("Changed variant "+scopeString+" from \""+oldv+"\" to \""+v+"\"<br />\n");
                }
                else writer.write("Deleted variant "+scopeString+" \""+oldv+"\"<br />\n");
            }
        }
    }
    
    
    protected String formatTopic(Topic t,Object identifier) throws TopicMapException {
        if(t!=null){
            String bn=t.getBaseName();
            if(bn!=null) return "\""+bn+"\"";
            else {
                Locator si = t.getOneSubjectIdentifier();
                if(si != null) return si.toString();
                else return "***null***";
            }
        }
        else {
            if(identifier instanceof Locator) return identifier.toString();
            else return "\""+identifier.toString()+"\"";
        }
    }
    
    
    protected void formatAssociation(Association a,Writer writer) throws IOException,TopicMapException {
        Topic type=a.getType();
        writer.write("Type: "+formatTopic(type,null)+"<br />\n");
        for(Topic role : a.getRoles()){
            writer.write(formatTopic(role,null)+": "+formatTopic(a.getPlayer(role),null)+"<br />\n");
        }
    }
    
    
    protected void formatAssociation(Topic[] a,Writer writer) throws IOException,TopicMapException {
        Topic type=a[0];
        writer.write("Type: "+formatTopic(type,null)+"<br />\n");
        for(int i=1;i+1<a.length;i+=2){
            Topic role=a[i];
            Topic player=a[i+1];
            writer.write(formatTopic(role,null)+": "+formatTopic(player,null)+"<br />\n");
        }
    }
    
    
    protected void formatAssociation(Object[] a,Writer writer) throws IOException,TopicMapException {
        Object type=a[0];
        writer.write("Type: "+formatTopic(null,type)+"<br />\n");
        for(int i=1;i+1<a.length;i+=2){
            Object role=a[i];
            Object player=a[i+1];
            writer.write(formatTopic(null,role)+": "+formatTopic(null,player)+"<br />\n");
        }
    }
    
    
    @Override
    public void header(Writer writer) throws IOException, TopicMapException{
        
    }
    
    
    @Override
    public void footer(Writer writer) throws IOException, TopicMapException{
        if(nochanges){
            writer.write("<h2>No differences</h2>");
        }
    }
    
    
    @Override
    public void formatDiffEntry(DiffEntry entry,Writer writer) throws IOException,TopicMapException {
        nochanges=false;
        if(entry instanceof TopicChanged){
            Topic t=((TopicChanged)entry).topic;
            Object t2=((TopicChanged)entry).topic2;
            ArrayList<TopicDiffEntry> diff=((TopicChanged)entry).diff;
            writer.write("<h2>Changed topic "+formatTopic(t,t2)+"</h2>\n");
            formatTopicDiffEntry(diff,writer);
        }
        else if(entry instanceof TopicDeleted){
            Topic t=((TopicDeleted)entry).topic;
            Object t2=((TopicDeleted)entry).topic2;
            writer.write("<h2>Deleted topic "+formatTopic(t,t2)+"</h2>\n");            
        }
        else if(entry instanceof TopicAdded){
            ArrayList<TopicDiffEntry> diff=((TopicAdded)entry).diff;
            String bn=null;
            Locator si=null;
            for(TopicDiffEntry e : diff){
                if(e instanceof BNChanged){
                    bn=((BNChanged)e).bn;
                    break;
                }
                else if(si==null && e instanceof SIAdded){
                    si=((SIAdded)e).si;
                }
            }
            if(bn!=null) writer.write("<h2>Added topic \""+bn+"\"</h2>\n");
            else writer.write("<h2>Added topic "+si+"</h2>\n");                        
            formatTopicDiffEntry(diff,writer);
        }
        else if(entry instanceof AssociationAdded){
            writer.write("<h2>Added association</h2>\n");
            if(((AssociationAdded)entry).a!=null)
                formatAssociation(((AssociationAdded)entry).a,writer);
            else
                formatAssociation(((AssociationAdded)entry).a2,writer);
        }
        else if(entry instanceof AssociationDeleted){
            writer.write("<h2>Deleted association</h2>\n");            
            if(((AssociationDeleted)entry).a!=null)
                formatAssociation(((AssociationDeleted)entry).a,writer);
            else
                formatAssociation(((AssociationDeleted)entry).a2,writer);
        }
    }
}
