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
 *
 * 
 * VariantCleaner.java
 *
 * Created on August 25, 2004, 1:06 PM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.TopicMapException;
import org.wandora.piccolo.WandoraManager;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.utils.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  olli
 */
public class VariantCleaner {
    
    /** Creates a new instance of VariantCleaner */
    public VariantCleaner() {
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying VariantCleaner filter");
        Topic fi=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("fi"));
        Topic en=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("en"));
        Topic sv=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("sv"));
        Topic fr=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("fr"));
        Topic da=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("da"));
        Topic de=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("de"));
        Topic es=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("es"));
        Topic it=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("it"));
        Topic no=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("no"));
        Topic ru=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("ru"));
        Topic indep=TMBox.getOrCreateTopic(tm,WandoraManager.LANGINDEPENDENT_SI);
        Topic disp=TMBox.getOrCreateTopic(tm,XTMPSI.DISPLAY);
        Topic sort=TMBox.getOrCreateTopic(tm,XTMPSI.SORT);
        HashMap map=new org.wandora.utils.EasyHash(new Object[]{
            tm.getTopic(XTMPSI.getLang("sw")),sv,
            tm.getTopic(XTMPSI.getLang("se")),sv,
            tm.getTopic(XTMPSI.getLang("ranska")),fr,
            tm.getTopic(XTMPSI.getLang("?")),indep,
            tm.getTopic(XTMPSI.getLang("tanska")),da,
            tm.getTopic(XTMPSI.getLang("saksa")),de,
            tm.getTopic(XTMPSI.getLang("Saksa")),de,
            tm.getTopic(XTMPSI.getLang("saks.")),de,
            tm.getTopic(XTMPSI.getLang("espanja")),es,
            tm.getTopic(XTMPSI.getLang("esp.")),es,
            tm.getTopic(XTMPSI.getLang("italia")),it,
            tm.getTopic(XTMPSI.getLang("norja")),no,
            tm.getTopic("http://www.topicmaps.org/xtm/1.0/core.xtm#psi-sort"),sort,
        });
        Iterator iter=tm.getTopics();
        int counter=0;
        int counter2=0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            Iterator iter2=new ArrayList(t.getVariantScopes()).iterator();
            while(iter2.hasNext()){
                Set scope=(Set)iter2.next();
                
                Iterator iter3=map.entrySet().iterator();
                while(iter3.hasNext()){
                    Map.Entry e=(Map.Entry)iter3.next();
                    Topic key=(Topic)e.getKey();
                    if(key==null) continue;
                    Topic value=(Topic)e.getValue();
                    if(scope.contains(key)){
                        HashSet newScope=new HashSet();
                        newScope.addAll(scope);
                        String name=t.getVariant(scope);
                        t.removeVariant(scope);
                        newScope.remove(key);
                        newScope.add(value);
                        t.setVariant(newScope,name);
                        scope=newScope;
                        counter2++;
                    }
                }
                
                
                if( (scope.contains(fi) || scope.contains(en) || scope.contains(sv) || scope.contains(indep) ||
                     scope.contains(fr) || scope.contains(da) || scope.contains(de) || scope.contains(es) ||
                     scope.contains(it) || scope.contains(no) || scope.contains(ru) ) &&
                    (scope.contains(disp) || scope.contains(sort)) ){
                        if(scope.size()>2){
                            logger.writelog("Warning scope size is "+scope.size()+" otherwise fine.");
                        }
                }
                else if( scope.size()==1 && scope.contains(disp) ){
                    counter++;
                    HashSet newScope=new HashSet();
                    newScope.addAll(scope);
                    newScope.add(indep);
                    String name=t.getVariant(scope);
                    t.removeVariant(scope);
                    if(t.getVariant(newScope)==null)
                        t.setVariant(newScope,name);
                }
                else{
                    StringBuffer buf=new StringBuffer();
                    Iterator iter4=scope.iterator();
                    while(iter4.hasNext()){
                        Topic st=(Topic)iter4.next();
                        buf.append(" "+st.getOneSubjectIdentifier());
                    }
                    logger.writelog("Unknown case"+buf+"; \""+t.getVariant(scope)+"\" removing.");
                    t.removeVariant(scope);
                }
            }
        }
        
        logger.writelog("Added language independent to "+counter+" scopes. Fixed "+counter2+" incorrect codes.");
        return tm;
    }    
}
