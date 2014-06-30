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
 * 
 *
 * AssemblyTopicFilter.java
 *
 * Created on 19. heinäkuuta 2005, 9:55
 */

package org.wandora.utils.velocity;
import org.wandora.piccolo.actions.AbstractTopicFilter;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.*;
import org.wandora.piccolo.actions.*;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.GripCollections;
import java.util.*;

/**
 *
 * @author olli
 */
public class VelocityMediaTopicFilter extends AbstractTopicFilter {
    
    private static String unmoderatedSI;
    private Vector<String> applyFilterTo;
    private Vector<String> filter;
    
    /** Creates a new instance of AssemblyTopicFilter */
    private VelocityMediaTopicFilter(Vector<String> applyFilterTo,String unmoderatedSI,Vector<String> filter) {
        this.applyFilterTo=applyFilterTo;
        this.unmoderatedSI=unmoderatedSI;
        this.filter=filter;
    }
    public VelocityMediaTopicFilter(String[] applyFilterSIs,String unmoderatedSI) {
        this.unmoderatedSI=unmoderatedSI;
        applyFilterTo=GripCollections.arrayToCollection(applyFilterSIs);
    }
    
    public boolean isUnmoderated(Topic t) throws TopicMapException {
        Topic unm=t.getTopicMap().getTopic(unmoderatedSI);
        if(unm!=null && t.isOfType(unm)) return true;        
        else return false;
    }

    public boolean topicVisible(Topic t) throws TopicMapException {
        if(!TMBox.topicVisible(t)) return false;
        if(isUnmoderated(t)) return false;
        if(filter!=null && filter.size()>0){
            for(String afsi : applyFilterTo){
                Topic aft=t.getTopicMap().getTopic(afsi);
                if(aft==null) continue;
                if(t.isOfType(aft)){
                    Topic picture=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/picture");
                    Topic video=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/video");
                    Topic keyword=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/keyword");
                    Topic work=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/work");
                    Topic author=t.getTopicMap().getTopic("http://www.gripstudios.com/wandora/common/author");
                    FilterTopics: for(String fsi : filter){
                        Topic ft=t.getTopicMap().getTopic(fsi);
                        if(ft==null || keyword==null || work==null) return false;
                        if(ft==picture || ft==video){
                            if(t.isOfType(ft)) continue FilterTopics;
                            else return false;
                        }
                        else if(author!=null && ft.isOfType(author)){
                            for(Association a : (Collection<Association>)t.getAssociations(author, work)){
                                if(ft==a.getPlayer(author)) continue FilterTopics;
                            }
                            return false;                            
                        }
                        else{
                            Collection subs=TMBox.getSubCategoriesRecursive(ft);
                            subs.add(ft);
                            for(Association a : (Collection<Association>)t.getAssociations(keyword, work)){
                                if(subs.contains(a.getPlayer(keyword))) continue FilterTopics;
                            }
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return true;
    }

    public String getFilterCacheKey(){
        if(filter==null) return "";
        String key="";
        for(String s : filter){
            if(key.length()>0) key+=";;";
            key+=s;
        }
        return key;
    }
    
    public org.wandora.piccolo.actions.TopicFilter makeNew(Object request) {
        Vector<String> filter=new Vector<String>();
        if(request instanceof javax.servlet.ServletRequest) {
            String[] filters = ((javax.servlet.ServletRequest) request).getParameterValues("filter");
            if(filters==null || filters.length==0) return this;
            for(int i=0;i<filters.length;i++){
                if(filters[i]!=null && filters[i].length()>0) filter.add(filters[i]);
            }
        }
        return new VelocityMediaTopicFilter(applyFilterTo,unmoderatedSI,filter);
        
    }
    
    public Vector<String> getFilter(){return filter;}

    public String getParamString(org.wandora.piccolo.utils.URLEncoder urlEncoder){
        return getParamString(getFilter(),urlEncoder);
    }
    
    public static String getParamString(Collection<String> filter,org.wandora.piccolo.utils.URLEncoder urlEncoder){
        String params="";
        if(filter==null) return params;
        for(String si : filter){
            params+="&filter="+urlEncoder.encode(si);
        }
        return params;
    }
    
    public String getParamString(Collection<Topic> filter,Topic remove,org.wandora.piccolo.utils.URLEncoder urlEncoder)  throws TopicMapException {
        String params="";
        if(filter==null) return params;
        for(Topic t : filter){
            if(t==remove) continue;
            params+="&filter="+urlEncoder.encode(t.getOneSubjectIdentifier().toExternalForm());
        }
        return params;        
    }
    
}
